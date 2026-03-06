// app/src/main/java/com/nick/myrecoverytracker/RedcapUploadWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RedcapUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val files = ctx.filesDir

        val baseUrl = getConfigString(ctx, "REDCAP_BASE_URL")
        val token = getConfigString(ctx, "REDCAP_API_TOKEN")
        val projectId = getConfigString(ctx, "REDCAP_PROJECT_ID")

        if (baseUrl.isBlank() || token.isBlank() || projectId.isBlank()) {
            Log.e(TAG, "Missing REDCap config")
            return@withContext Result.failure()
        }

        val participantId = ParticipantIdManager.getOrCreate(ctx)
        Log.i(TAG, "Starting upload for participant_id=$participantId")

        try {
            // Aggregate CSV files
            val aggregated = aggregateDailyMetrics(files, participantId)

            // Upload to REDCap
            val uploaded = uploadToRedcap(baseUrl, token, projectId, aggregated)

            // Record receipt
            recordReceipt(files, participantId, uploaded)

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "Upload failed", t)
            Result.retry()
        }
    }

    private fun aggregateDailyMetrics(files: File, participantId: String): String {
        val csvFiles = listOf(
            "daily_late_night_screen_usage.csv",
            "daily_notification_engagement.csv",
            "daily_notification_latency.csv",
            "daily_usage_entropy.csv",
            "daily_app_usage_minutes.csv",
            "daily_distance_log.csv",
            "daily_movement_intensity.csv"
        )

        val mapping = mapOf(
            "daily_late_night_screen_usage.csv" to mapOf(
                "late_night_screen_usage_yn" to "late_night"
            ),
            "daily_notification_engagement.csv" to mapOf(
                "notif_posted" to "notif_posted",
                "notif_engaged" to "notif_engaged",
                "notif_engagement_rate" to "notification_engagement_rate",
                "notif_latency_median_s" to "notif_latency_median_s",
                "notif_latency_n" to "notif_latency_n"
            ),
            "daily_notification_latency.csv" to mapOf(
                "notif_latency_avg_s" to "notif_latency_avg_s"
            ),
            "daily_usage_entropy.csv" to mapOf(
                "entropy" to "usage_entropy"
            ),
            "daily_app_usage_minutes.csv" to mapOf(
                "app_min_total" to "app_min_total"
            ),
            "daily_distance_log.csv" to mapOf(
                "distance_km" to "distance_m"
            ),
            "daily_movement_intensity.csv" to mapOf(
                "intensity" to "move_intensity_score"
            )
        )

        // Build header
        val payload = StringBuilder()
        payload.append("record_id,participant_id,date,feature_schema_version,redcap_repeat_instrument,redcap_repeat_instance,daily_metrics_complete")

        val allRedcapFields = mutableListOf<String>()
        mapping.values.forEach { fields ->
            fields.values.forEach { redcapField ->
                allRedcapFields.add(redcapField)
                payload.append(",$redcapField")
            }
        }
        payload.append("\n")

        // Merge rows by date
        val rowsByDate = mutableMapOf<String, MutableMap<String, String>>()

        csvFiles.forEach { csvFile ->
            val file = File(files, csvFile)
            if (!file.exists()) {
                Log.w(TAG, "CSV file not found: $csvFile")
                return@forEach
            }

            val lines = file.readLines()
            if (lines.isEmpty()) return@forEach

            val headerLine = lines[0]
            val headers = readCols(headerLine)

            // Find date column index
            val dateIdx = headers.indexOf("date")
            if (dateIdx < 0) {
                Log.w(TAG, "No 'date' column in $csvFile. Headers: $headers")
                return@forEach
            }

            // Process data rows (skip header at index 0)
            for (i in 1 until lines.size) {
                val line = lines[i].trim()
                if (line.isEmpty()) continue

                val cols = readCols(line)
                if (cols.isEmpty()) continue

                // Skip incomplete rows (fewer columns than header)
                if (cols.size < headers.size) {
                    Log.w(TAG, "Skipping incomplete row in $csvFile: $line (got ${cols.size} cols, expected ${headers.size})")
                    continue
                }

                val date = cols.getOrNull(dateIdx) ?: continue

                // Skip invalid dates
                if (!isValidDate(date)) {
                    Log.w(TAG, "Skipping invalid date in $csvFile: $date")
                    continue
                }

                val row = rowsByDate.getOrPut(date) { mutableMapOf() }
                row["record_id"] = "${participantId}_$date"
                row["participant_id"] = participantId
                row["date"] = date
                row["feature_schema_version"] = "1"
                row["redcap_repeat_instrument"] = "daily_metrics"
                row["redcap_repeat_instance"] = "1"
                row["daily_metrics_complete"] = "2"

                // Map CSV columns to REDCap fields
                val csvToRedcap = mapping[csvFile] ?: emptyMap()
                csvToRedcap.forEach { (csvCol, redcapField) ->
                    val colIdx = headers.indexOf(csvCol)
                    if (colIdx >= 0 && colIdx < cols.size) {
                        var value = cols[colIdx]

                        // Convert Y/N to 1/0 for late_night field
                        if (redcapField == "late_night") {
                            value = when (value.uppercase()) {
                                "Y" -> "1"
                                "N" -> "0"
                                else -> value
                            }
                        }

                        row[redcapField] = value
                    }
                }
            }
        }

        // Write payload rows
        rowsByDate.keys.sorted().forEach { date ->
            val row = rowsByDate[date] ?: return@forEach
            val values = mutableListOf<String>()
            values.add(row["record_id"] ?: "")
            values.add(row["participant_id"] ?: "")
            values.add(row["date"] ?: "")
            values.add(row["feature_schema_version"] ?: "1")
            values.add(row["redcap_repeat_instrument"] ?: "daily_metrics")
            values.add(row["redcap_repeat_instance"] ?: "1")
            values.add(row["daily_metrics_complete"] ?: "2")

            allRedcapFields.forEach { field ->
                values.add(row[field] ?: "")
            }

            payload.append(values.joinToString(",")).append("\n")
        }

        Log.i(TAG, "Aggregated payload:\n$payload")
        return payload.toString()
    }

    private fun isValidDate(date: String): Boolean {
        return date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
    }

    private fun uploadToRedcap(baseUrl: String, token: String, projectId: String, csvPayload: String): Boolean {
        val client = OkHttpClient()
        val endpoint = baseUrl

        Log.i(TAG, "Uploading to: $endpoint")

        val body = FormBody.Builder()
            .add("token", token)
            .add("content", "record")
            .add("format", "csv")
            .add("data", csvPayload)
            .add("type", "flat")
            .build()

        val request = okhttp3.Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            Log.i(TAG, "Upload response: code=${response.code} success=$success")
            if (!success) {
                val responseBody = response.body?.string()
                Log.e(TAG, "Upload failed. Response body: $responseBody")
            }
            success
        } catch (t: Throwable) {
            Log.e(TAG, "Upload HTTP error", t)
            false
        }
    }

    private fun recordReceipt(files: File, participantId: String, success: Boolean) {
        val receiptDir = File("/sdcard/Documents/MYRA")
        receiptDir.mkdirs()

        val receiptFile = File(receiptDir, "redcap_receipts.csv")
        val now = System.currentTimeMillis()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
        val status = if (success) "success" else "failed"

        if (!receiptFile.exists()) {
            receiptFile.writeText("ts,date,participant_id,status\n")
        }

        receiptFile.appendText("$now,$date,$participantId,$status\n")
        Log.i(TAG, "Receipt recorded: $status for $participantId on $date")
    }

    private fun readCols(line: String): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when (c) {
                '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i += 1
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ',' -> if (inQuotes) {
                    sb.append(c)
                } else {
                    out.add(sb.toString())
                    sb.setLength(0)
                }
                '\r' -> Unit
                else -> sb.append(c)
            }
            i += 1
        }
        out.add(sb.toString())
        return out
    }

    private fun getConfigString(ctx: Context, key: String): String {
        return try {
            val cls = Class.forName("com.nick.myrecoverytracker.BuildConfig")
            val field = cls.getField(key)
            field.get(null) as? String ?: ""
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to read config $key", t)
            ""
        }
    }

    companion object {
        private const val TAG = "RedcapUploadWorker"
    }
}