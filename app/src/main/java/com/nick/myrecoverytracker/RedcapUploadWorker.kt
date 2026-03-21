package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLEncoder

class RedcapUploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "RedcapUploadWorker"
        private const val SCHEMA_VERSION = "v1.0" // kept for audit log use only — NOT sent to REDCap

        // Sentinel strings that must never be forwarded to REDCap as field values.
        // REDCap expects numeric values for these fields; sending a string causes error_400.
        // Add new sentinels here as they are introduced in worker output files.
        private val SENTINEL_VALUES = setOf(
            "PERMISSION_MISSING",
            "DATA_UNAVAILABLE"
        )
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ctx = applicationContext
            val dir = File(ctx.getExternalFilesDir(null), "data")
            if (!dir.exists()) dir.mkdirs()

            val participantId = ParticipantIdManager.getOrCreate(ctx)
            val today = java.time.LocalDate.now().toString()

            Log.i(TAG, "START participantId=$participantId date=$today")

            // ── Base fields (always present) ──────────────────────────────────
            val payload = mutableMapOf<String, String>()
            payload["record_id"]                = "${participantId}_$today"
            payload["redcap_repeat_instrument"] = "daily_metrics"  // ✅ Required: Repeating Instrument name
            payload["redcap_repeat_instance"]   = "1"              // ✅ Required: one instance per record per day
            payload["participant_id"]           = participantId
            payload["date"]                     = today
            // ✅ FIX 1: feature_schema_version REMOVED from payload — REDCap rejects the "v1.0" format.
            //           It is retained as SCHEMA_VERSION constant for local audit use only.

            // ── Extract from each source CSV ──────────────────────────────────
            var allComplete = true

            allComplete = allComplete and extractLateNight(dir, today, payload)
            allComplete = allComplete and extractNotifEngagement(dir, today, payload)
            allComplete = allComplete and extractNotifLatency(dir, today, payload)
            allComplete = allComplete and extractUsageEntropy(dir, today, payload)
            allComplete = allComplete and extractAppUsage(dir, today, payload)
            allComplete = allComplete and extractDistance(dir, today, payload)
            allComplete = allComplete and extractMovementIntensity(dir, today, payload)

            payload["daily_metrics_complete"] = if (allComplete) "2" else "0"
            // REDCap uses 0=Incomplete, 1=Unverified, 2=Complete for *_complete fields

            // ── Audit log ─────────────────────────────────────────────────────
            writeMetricsUpload(payload, today, dir)

            // ── Upload ────────────────────────────────────────────────────────
            val uploadSuccess = uploadToRedcap(payload, participantId, today, dir)

            Log.i(TAG, "END uploadSuccess=$uploadSuccess allComplete=$allComplete")
            return@withContext if (uploadSuccess) Result.success() else Result.retry()

        } catch (t: Throwable) {
            Log.e(TAG, "doWork() fatal", t)
            return@withContext Result.retry()
        }
    }

    // ── Extractors ─────────────────────────────────────────────────────────────
    // Each extractor returns true if the file existed and a row was found for today.
    // All use the exact REDCap field names as keys.

    /**
     * daily_late_night_screen_usage.csv
     * Schema: record_id, participant_id, date, feature_schema_version, event, late_night
     * Index:  0           1               2     3                       4      5
     * REDCap: late_night  (expects numeric category: 1=Yes, 0=No)
     */
    private fun extractLateNight(dir: File, today: String, out: MutableMap<String, String>): Boolean {
        val f = File(dir, "daily_late_night_screen_usage.csv")
        if (!f.exists()) { Log.w(TAG, "MISSING daily_late_night_screen_usage.csv"); return false }
        f.useLines { seq ->
            seq.drop(1).find { it.contains(",$today,") }?.let { line ->
                val p = line.split(',')
                if (p.size >= 6) {
                    val raw = p[5].trim()
                    // ✅ FIX 2: REDCap expects numeric category codes (1/0), not Y/N strings.
                    val coded = when (raw.uppercase()) {
                        "Y", "YES", "TRUE", "1"  -> "1"
                        "N", "NO",  "FALSE", "0" -> "0"
                        else -> raw
                    }
                    out["late_night"] = coded
                    Log.d(TAG, "late_night raw=$raw coded=$coded")
                    return true
                }
            }
        }
        Log.w(TAG, "No row for $today in daily_late_night_screen_usage.csv")
        return false
    }

    /**
     * daily_notification_engagement.csv
     * Schema: record_id, participant_id, date, feature_schema_version, event,
     *         notif_posted, notif_engaged, notif_engagement_rate,
     *         notif_latency_median_s, notif_latency_n
     * Index:  0             1               2     3                       4
     *         5             6               7
     *         8                      9
     * REDCap: notif_posted, notif_engaged, notif_engagement_rate
     */
    private fun extractNotifEngagement(dir: File, today: String, out: MutableMap<String, String>): Boolean {
        val f = File(dir, "daily_notification_engagement.csv")
        if (!f.exists()) { Log.w(TAG, "MISSING daily_notification_engagement.csv"); return false }
        f.useLines { seq ->
            seq.drop(1).find { it.contains(",$today,") }?.let { line ->
                val p = line.split(',')
                if (p.size >= 8) {
                    out["notif_posted"]          = p[5].trim()
                    out["notif_engaged"]         = p[6].trim()
                    out["notif_engagement_rate"] = p[7].trim()
                    Log.d(TAG, "notif_posted=${p[5].trim()} notif_engaged=${p[6].trim()} " +
                            "notif_engagement_rate=${p[7].trim()}")
                    return true
                }
            }
        }
        Log.w(TAG, "No row for $today in daily_notification_engagement.csv")
        return false
    }

    /**
     * daily_notification_latency.csv
     * Schema: record_id, participant_id, date, feature_schema_version,
     *         notif_latency_median_s, notif_latency_n
     * Index:  0             1               2     3
     *         4                      5
     * REDCap: notif_latency_median_s, notif_latency_n
     */
    private fun extractNotifLatency(dir: File, today: String, out: MutableMap<String, String>): Boolean {
        val f = File(dir, "daily_notification_latency.csv")
        if (!f.exists()) { Log.w(TAG, "MISSING daily_notification_latency.csv"); return false }
        f.useLines { seq ->
            seq.drop(1).find { it.contains(",$today,") }?.let { line ->
                val p = line.split(',')
                if (p.size >= 6) {
                    out["notif_latency_median_s"] = p[4].trim()
                    out["notif_latency_n"]         = p[5].trim()
                    Log.d(TAG, "notif_latency_median_s=${p[4].trim()} notif_latency_n=${p[5].trim()}")
                    return true
                }
            }
        }
        Log.w(TAG, "No row for $today in daily_notification_latency.csv")
        return false
    }

    /**
     * daily_usage_entropy.csv
     * Schema: date, feature_schema_version, daily_usage_entropy_bits
     * Index:  0     1                       2
     * REDCap: daily_usage_entropy_bits
     * NOTE: date is at index 0 — use startsWith not contains.
     *
     * ✅ FIX 3: Validate that the value is a real numeric before adding to payload.
     * If the worker wrote a sentinel (e.g. PERMISSION_MISSING), return false so the
     * field is entirely omitted from the upload rather than sending an invalid string
     * that REDCap will reject with error_400.
     */
    private fun extractUsageEntropy(dir: File, today: String, out: MutableMap<String, String>): Boolean {
        val f = File(dir, "daily_usage_entropy.csv")
        if (!f.exists()) { Log.w(TAG, "MISSING daily_usage_entropy.csv"); return false }
        f.useLines { seq ->
            seq.drop(1).find { it.startsWith("$today,") }?.let { line ->
                val p = line.split(',')
                if (p.size >= 3) {
                    val value = p[2].trim()
                    if (value.toDoubleOrNull() == null) {
                        // Non-numeric sentinel — omit field entirely rather than cause error_400.
                        Log.w(TAG, "daily_usage_entropy_bits is non-numeric sentinel '$value' — omitting from payload")
                        return false
                    }
                    out["daily_usage_entropy_bits"] = value
                    Log.d(TAG, "daily_usage_entropy_bits=$value")
                    return true
                }
            }
        }
        Log.w(TAG, "No row for $today in daily_usage_entropy.csv")
        return false
    }

    /**
     * daily_app_usage_minutes.csv
     * Schema: date, app_min_total, app_min_social, app_min_dating, app_min_productivity,
     *         app_min_music_audio, app_min_image, app_min_maps, app_min_video,
     *         app_min_travel_local, app_min_shopping, app_min_news, app_min_game,
     *         app_min_health, app_min_finance, app_min_browser, app_min_comm, app_min_other
     * Index:  0     1             2              3               4
     *         5                   6              7              8
     *         9                    10               11            12
     *         13               14               15               16              17
     * REDCap: all app_min_* fields match exactly
     * NOTE: date is at index 0 — use startsWith
     */
    private fun extractAppUsage(dir: File, today: String, out: MutableMap<String, String>): Boolean {
        val f = File(dir, "daily_app_usage_minutes.csv")
        if (!f.exists()) { Log.w(TAG, "MISSING daily_app_usage_minutes.csv"); return false }
        f.useLines { seq ->
            seq.drop(1).find { it.startsWith("$today,") }?.let { line ->
                val p = line.split(',')
                if (p.size >= 18) {
                    out["app_min_total"]        = p[1].trim()
                    out["app_min_social"]       = p[2].trim()
                    out["app_min_dating"]       = p[3].trim()
                    out["app_min_productivity"] = p[4].trim()
                    out["app_min_music_audio"]  = p[5].trim()
                    out["app_min_image"]        = p[6].trim()
                    out["app_min_maps"]         = p[7].trim()
                    out["app_min_video"]        = p[8].trim()
                    out["app_min_travel_local"] = p[9].trim()
                    out["app_min_shopping"]     = p[10].trim()
                    out["app_min_news"]         = p[11].trim()
                    out["app_min_game"]         = p[12].trim()
                    out["app_min_health"]       = p[13].trim()
                    out["app_min_finance"]      = p[14].trim()
                    out["app_min_browser"]      = p[15].trim()
                    out["app_min_comm"]         = p[16].trim()
                    out["app_min_other"]        = p[17].trim()
                    Log.d(TAG, "app_min_total=${p[1].trim()}")
                    return true
                }
            }
        }
        Log.w(TAG, "No row for $today in daily_app_usage_minutes.csv")
        return false
    }

    /**
     * daily_distance_log.csv
     * Schema: record_id, participant_id, date, feature_schema_version, event, distance_m
     * Index:  0           1               2     3                       4      5
     * REDCap: distance_m
     */
    private fun extractDistance(dir: File, today: String, out: MutableMap<String, String>): Boolean {
        val f = File(dir, "daily_distance_log.csv")
        if (!f.exists()) { Log.w(TAG, "MISSING daily_distance_log.csv"); return false }
        f.useLines { seq ->
            seq.drop(1).find { it.contains(",$today,") }?.let { line ->
                val p = line.split(',')
                if (p.size >= 6) {
                    out["distance_m"] = p[5].trim()
                    Log.d(TAG, "distance_m=${p[5].trim()}")
                    return true
                }
            }
        }
        Log.w(TAG, "No row for $today in daily_distance_log.csv")
        return false
    }

    /**
     * daily_movement_intensity.csv
     * Schema: record_id, participant_id, date, feature_schema_version, event, movement_intensity
     * Index:  0           1               2     3                       4      5
     * REDCap: movement_intensity, daily_unlocks
     * NOTE: movement_intensity = unlock count — both REDCap fields get the same value.
     */
    private fun extractMovementIntensity(dir: File, today: String, out: MutableMap<String, String>): Boolean {
        val f = File(dir, "daily_movement_intensity.csv")
        if (!f.exists()) { Log.w(TAG, "MISSING daily_movement_intensity.csv"); return false }
        f.useLines { seq ->
            seq.drop(1).find { it.contains(",$today,") }?.let { line ->
                val p = line.split(',')
                if (p.size >= 6) {
                    val value = p[5].trim()
                    out["movement_intensity"] = value
                    out["daily_unlocks"]      = value   // same source until a separate unlock counter exists
                    Log.d(TAG, "movement_intensity=$value daily_unlocks=$value")
                    return true
                }
            }
        }
        Log.w(TAG, "No row for $today in daily_movement_intensity.csv")
        return false
    }

    // ── Audit log ───────────────────────────────────────────────────────────────

    /**
     * Writes a human-readable audit row to daily_metrics_upload.csv.
     * Schema matches the key fields actually sent to REDCap so it can be
     * cross-checked against redcap_receipts.csv.
     */
    private fun writeMetricsUpload(payload: Map<String, String>, today: String, dir: File) {
        try {
            val f = File(dir, "daily_metrics_upload.csv")
            val header = "date,daily_unlocks,late_night,notif_posted,notif_engaged," +
                    "notif_engagement_rate,notif_latency_median_s,notif_latency_n," +
                    "daily_usage_entropy_bits,app_min_total,distance_m," +
                    "movement_intensity,daily_metrics_complete\n"
            if (!f.exists()) f.writeText(header)

            val row = listOf(
                today,
                payload["daily_unlocks"]            ?: "",
                payload["late_night"]               ?: "",
                payload["notif_posted"]             ?: "",
                payload["notif_engaged"]            ?: "",
                payload["notif_engagement_rate"]    ?: "",
                payload["notif_latency_median_s"]   ?: "",
                payload["notif_latency_n"]          ?: "",
                payload["daily_usage_entropy_bits"] ?: "",
                payload["app_min_total"]            ?: "",
                payload["distance_m"]               ?: "",
                payload["movement_intensity"]       ?: "",
                payload["daily_metrics_complete"]   ?: ""
            ).joinToString(",")

            f.appendText("$row\n")
            Log.i(TAG, "Wrote daily_metrics_upload.csv row: $row")
        } catch (t: Throwable) {
            Log.e(TAG, "writeMetricsUpload failed", t)
        }
    }

    // ── REDCap upload ───────────────────────────────────────────────────────────

    private fun uploadToRedcap(
        payload: Map<String, String>,
        participantId: String,
        today: String,
        dir: File
    ): Boolean {
        return try {
            val baseUrl  = BuildConfig.REDCAP_BASE_URL
            val apiToken = BuildConfig.REDCAP_API_TOKEN

            if (baseUrl.isBlank() || apiToken.isBlank()) {
                Log.e(TAG, "REDCap credentials missing in BuildConfig")
                writeRedcapReceipt(System.currentTimeMillis(), today, participantId, "error_missing_credentials", dir)
                return false
            }

            val apiUrl = "${baseUrl.trimEnd('/')}/api/index.php"
            Log.i(TAG, "REDCap API URL: $apiUrl")

            // Build a flat JSON array with exactly one record object.
            // Only include fields that have a non-empty, non-sentinel value.
            // ✅ FIX 4: Expanded filter to cover SENTINEL_VALUES set and SENSOR_* prefix.
            //           Extractors above should already block these at source, but this
            //           is a defensive second layer that catches any future sentinel that
            //           slips through. A string sentinel in any numeric REDCap field
            //           causes an immediate error_400 rejection.
            val fields = payload.entries
                .filter { (_, v) ->
                    v.isNotBlank()
                            && !v.startsWith("SENSOR_")
                            && v !in SENTINEL_VALUES
                }
                .joinToString(",") { (k, v) ->
                    "\"$k\":\"${v.replace("\"", "\\\"")}\""
                }
            val jsonData = "[{$fields}]"
            Log.d(TAG, "REDCap JSON payload: $jsonData")

            val encodedData = URLEncoder.encode(jsonData, "UTF-8")
            val formBody = "token=$apiToken" +
                    "&content=record" +
                    "&format=json" +
                    "&type=flat" +
                    "&overwriteBehavior=normal" +
                    "&data=$encodedData"

            val client      = OkHttpClient()
            val requestBody = formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType())
            val request     = Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val ts       = System.currentTimeMillis()

            return if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                Log.i(TAG, "REDCap upload SUCCESS body=$body")
                writeRedcapReceipt(ts, today, participantId, "success", dir)
                true
            } else {
                val body = response.body?.string() ?: "no body"
                Log.w(TAG, "REDCap upload FAILED HTTP=${response.code} body=$body")
                writeRedcapReceipt(ts, today, participantId, "error_${response.code}", dir)
                false
            }

        } catch (t: Throwable) {
            Log.e(TAG, "uploadToRedcap exception", t)
            writeRedcapReceipt(System.currentTimeMillis(), today, participantId, "error_exception", dir)
            false
        }
    }

    // ── Receipt log ─────────────────────────────────────────────────────────────

    private fun writeRedcapReceipt(
        ts: Long,
        date: String,
        participantId: String,
        status: String,
        dir: File
    ) {
        try {
            val f = File(dir, "redcap_receipts.csv")
            if (!f.exists()) f.writeText("ts,date,participant_id,status\n")
            f.appendText("$ts,$date,$participantId,$status\n")
            Log.i(TAG, "redcap_receipts.csv status=$status")
        } catch (t: Throwable) {
            Log.e(TAG, "writeRedcapReceipt failed", t)
        }
    }
}
