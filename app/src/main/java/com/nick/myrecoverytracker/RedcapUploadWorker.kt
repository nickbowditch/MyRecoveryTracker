package com.nick.myrecoverytracker

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.round

class RedcapUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val ctx = applicationContext

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val filesDir = ctx.filesDir ?: return@withContext Result.retry()

            // Stable per-install participant ID
            val participantId = ParticipantIdManager.getOrCreate(ctx)
            Log.i(TAG, "UPLOAD participant_id=$participantId")

            val metricFields = listOf(
                "daily_unlocks",
                "sleep_hours",
                "sleep_has_morning_wake",
                "late_night",
                "notif_posted",
                "notif_engaged",
                "notif_engagement_rate",
                "notif_latency_avg_s",
                "notif_latency_median_s",
                "notif_latency_n",
                "usage_events_count",
                "app_min_social",
                "app_min_dating",
                "app_min_productivity",
                "app_min_music_audio",
                "app_min_image",
                "app_min_maps",
                "app_min_video",
                "app_min_travel_local",
                "app_min_shopping",
                "app_min_news",
                "app_min_game",
                "app_min_health",
                "app_min_finance",
                "app_min_other",
                "app_min_total",
                "app_switch_count",
                "usage_entropy",
                "distance_m",
                "move_intensity_score"
            )

            val rawToRedcap = mapOf(
                "screen_unlocks_per_day" to "daily_unlocks",
                "sleep_duration_hours" to "sleep_hours",
                "has_morning_wake" to "sleep_has_morning_wake",
                "late_night_YN" to "late_night",
                "notifications_delivered" to "notif_posted",
                "notifications_opened" to "notif_engaged",
                "usage_events_count" to "usage_events_count",
                "distance_km" to "distance_m",
                "movement_intensity_score" to "move_intensity_score"
            )

            val yesNoFields = setOf("sleep_has_morning_wake", "late_night")
            val rowsByDate = mutableMapOf<String, MutableMap<String, String>>()

            val candidates = filesDir.listFiles()?.filter {
                it.name.startsWith("daily_") && it.name.endsWith(".csv")
            } ?: emptyList()

            for (file in candidates) {
                val lines = try { file.readLines() } catch (_: Throwable) { continue }
                if (lines.size < 2) continue

                val header = lines.first().split(",")

                for (line in lines.drop(1)) {
                    val cols = line.split(",")
                    val date = cols.firstOrNull()?.trim().orEmpty()
                    if (date.isEmpty()) continue

                    val row = rowsByDate.getOrPut(date) { mutableMapOf() }

                    for (i in 1 until minOf(header.size, cols.size)) {
                        val redcapField = rawToRedcap[header[i]] ?: continue
                        if (redcapField !in metricFields) continue

                        val raw = cols[i].trim()
                        if (raw.isEmpty()) continue

                        val value = when {
                            redcapField in yesNoFields -> normaliseYesNo(raw)
                            redcapField.startsWith("app_min_") -> normaliseMinutes(raw)
                            else -> raw
                        }

                        if (value.isNotEmpty()) row[redcapField] = value
                    }
                }
            }

            val today = DATE_FMT.format(System.currentTimeMillis())
            val rowsForUpload = rowsByDate.filterKeys { it == today }

            if (rowsForUpload.isEmpty()) {
                writeReceipt(File(filesDir, RECEIPTS_FILE), 0, 0, "NO_ROWS_FOR_TODAY")
                return@withContext Result.success()
            }

            val headerOut = buildList {
                add("record_id")
                add("participant_id")
                add("date")
                add("feature_schema_version")
                addAll(metricFields)
                add(DAILY_METRICS_COMPLETE_FIELD)
            }

            val csv = buildString {
                append(headerOut.joinToString(",")).append("\n")
                for ((date, map) in rowsForUpload) {
                    val row = mutableListOf(
                        "$participantId-$date",
                        participantId,
                        date,
                        FEATURE_SCHEMA_VERSION
                    )
                    metricFields.forEach { row.add(map[it].orEmpty()) }
                    row.add("2")
                    append(row.joinToString(",")).append("\n")
                }
            }

            writeEncrypted(File(filesDir, "daily_metrics_upload.csv"), csv)
            writeReceipt(File(filesDir, RECEIPTS_FILE), rowsForUpload.size, 200, "BUILT_ONLY")

            Log.i(TAG, "REDCAP_CSV_PAYLOAD_ROWS=${rowsForUpload.size}")
            Result.success()
        }
    }

    // ---------------- helpers ----------------

    private fun writeEncrypted(file: File, plain: String) {
        try {
            val salt = "MYRA_SALT".toByteArray()
            val spec = PBEKeySpec("local".toCharArray(), salt, 10000, 256)
            val key = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)

            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.encoded, "AES"))
            file.writeText(
                Base64.encodeToString(cipher.doFinal(plain.toByteArray()), Base64.NO_WRAP)
            )
        } catch (_: Throwable) {
            file.writeText(plain)
        }
    }

    private fun writeReceipt(file: File, rows: Int, httpCode: Int, note: String) {
        if (!file.exists()) file.writeText("ts,rows,http,note\n")
        val ts = TS.format(System.currentTimeMillis())
        file.appendText("$ts,$rows,$httpCode,$note\n")
    }

    private fun sha256(s: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun normaliseYesNo(raw: String): String {
        return when (raw.lowercase(Locale.US)) {
            "y", "yes", "1", "true" -> "1"
            "n", "no", "0", "false" -> "0"
            else -> ""
        }
    }

    private fun normaliseMinutes(raw: String): String {
        val d = raw.toDoubleOrNull() ?: return ""
        return round(d).toLong().coerceAtLeast(0).toString()
    }

    companion object {
        private const val TAG = "RedcapUploadWorker"
        private const val FEATURE_SCHEMA_VERSION = "1"
        private const val DAILY_METRICS_COMPLETE_FIELD = "daily_metrics_complete"
        private const val RECEIPTS_FILE = "redcap_receipts.csv"

        private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private val TS = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}