// app/src/main/java/com/nick/myrecoverytracker/NotificationEngagementWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class NotificationEngagementWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val baseDir = ctx.getExternalFilesDir(null)
            ?: return@withContext Result.failure()
        val dataDir = File(baseDir, "data")
        dataDir.mkdirs()

        val inFile = File(dataDir, IN_FILE)
        val outFile = File(dataDir, OUT_FILE)
        val heartbeatFile = File(dataDir, HEARTBEAT_FILE)
        val latencyFile = File(dataDir, LATENCY_FILE)

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone).toString()
        val yesterday = LocalDate.now(zone).minusDays(1).toString()
        val nowTs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(Date())

        Log.i(TAG, "START doWork() today=$today yesterday=$yesterday")

        val participantId = ParticipantIdManager.getOrCreate(ctx)
        val header = DEFAULT_HEADER
        ensureHeader(outFile, header)
        ensureHeader(heartbeatFile, HEARTBEAT_HEADER)

        appendHeartbeat(heartbeatFile, "$nowTs,NotificationEngagementWorker started")

        val latencyByDate = mutableMapOf<String, Pair<Double, Int>>()
        if (latencyFile.exists()) {
            try {
                latencyFile.forEachLine { raw ->
                    val line = raw.trimStart('\uFEFF').trim()
                    if (line.isEmpty() || isHeaderLike(line)) return@forEachLine

                    val cols = readCols(line)
                    if (cols.size < 6) return@forEachLine // ✅ FIXED: was < 5, needs index 5

                    val date = cols[2].trim()
                    val medianStr = cols[4].trim()
                    val countStr = cols[5].trim()

                    val median = medianStr.toDoubleOrNull() ?: 0.0
                    val count = countStr.toIntOrNull() ?: 0

                    latencyByDate[date] = Pair(median, count)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to load latency file", t)
            }
        }

        val engagementByDate = mutableMapOf<String, EngagementStats>()
        var parseFailed = false

        if (inFile.exists()) {
            try {
                inFile.forEachLine { raw ->
                    val line = raw.trimStart('\uFEFF').trim()
                    if (line.isEmpty() || isHeaderLike(line)) return@forEachLine

                    val cols = readCols(line)
                    if (cols.size < 6) return@forEachLine

                    val ts = cols[0].trim()
                    val eventType = cols[1].trim().uppercase(Locale.US)
                    val rawEvent = cols[4].trim().uppercase(Locale.US)
                    val rawReason = cols[5].trim().uppercase(Locale.US)

                    val date = if (ts.length >= 10) ts.substring(0, 10) else ""
                    if (!DATE_RE.matches(date)) return@forEachLine

                    val stats = engagementByDate.getOrPut(date) { EngagementStats() }

                    when {
                        eventType == "POSTED" || rawEvent == "POSTED" -> stats.posted++
                        eventType == "REMOVED" && rawReason == "CLICK" -> {
                            stats.posted++
                            stats.engaged++
                        }
                    }
                }
            } catch (t: Throwable) {
                parseFailed = true
                Log.e(TAG, "Failed parsing $IN_FILE (continuing with forced rows)", t)
            }
        }

        engagementByDate.putIfAbsent(yesterday, EngagementStats())
        engagementByDate.putIfAbsent(today, EngagementStats())

        val existing = try {
            outFile.readLines()
                .map { it.trimEnd('\r') }
                .drop(1)
                .filter { it.isNotBlank() }
                .associateBy {
                    val cols = readCols(it)
                    cols.getOrNull(0) ?: ""
                }
                .toMutableMap()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed reading existing $OUT_FILE (will rebuild)", t)
            mutableMapOf()
        }

        engagementByDate.forEach { (date, stats) ->
            val recordId = "${participantId}_$date"
            val engagementRate = if (stats.posted > 0) {
                (stats.engaged.toDouble() / stats.posted.toDouble())
            } else {
                0.0
            }

            val (latencyMedian, latencyCount) = latencyByDate[date] ?: Pair(0.0, 0)

            existing[recordId] = String.format(
                Locale.US,
                "%s,%s,%s,%s,%s,%d,%d,%.6f,%.6f,%d",
                recordId,
                participantId,
                date,
                FEATURE_SCHEMA_VERSION,
                "engagement",
                stats.posted,
                stats.engaged,
                engagementRate,
                latencyMedian,
                latencyCount
            )
        }

        val rebuilt = buildString {
            append(header).append('\n')
            existing.keys.sorted().forEach { k ->
                append(existing[k]).append('\n')
            }
        }

        try {
            outFile.writeText(rebuilt)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed writing $OUT_FILE", t)
            return@withContext Result.failure()
        }

        appendHeartbeat(heartbeatFile, "$nowTs,NotificationEngagementWorker completed successfully")

        Log.i(TAG, "END success wrote_rows=${existing.size} forced_yesterday=$yesterday forced_today=$today parseFailed=$parseFailed")

        Result.success()
    }

    private fun ensureHeader(out: File, header: String) {
        if (!out.exists() || out.length() == 0L) {
            out.parentFile?.mkdirs()
            out.writeText("$header\n")
        } else {
            val cur = out.useLines { it.firstOrNull() ?: "" }.trim().replace("\r", "")
            if (cur != header) {
                val rest = out.readLines().drop(1).joinToString("\n")
                out.writeText(
                    buildString {
                        append(header).append('\n')
                        if (rest.isNotBlank()) append(rest.trimEnd()).append('\n')
                    }
                )
            }
        }
    }

    private fun appendHeartbeat(file: File, line: String) {
        try {
            if (!file.exists()) {
                file.writeText("$HEARTBEAT_HEADER\n$line\n")
            } else {
                file.appendText("$line\n")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to append heartbeat", t)
        }
    }

    private fun isHeaderLike(line: String): Boolean {
        val l = line.lowercase(Locale.US)
        return l.startsWith("ts,") || l.startsWith("timestamp,")
    }

    private fun readCols(line: String): List<String> {
        val out = ArrayList<String>(6)
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

    private data class EngagementStats(
        var posted: Int = 0,
        var engaged: Int = 0
    )

    companion object {
        private const val TAG = "NotificationEngagementWorker"

        private const val IN_FILE = "notification_log.csv"
        private const val OUT_FILE = "daily_notification_engagement.csv"
        private const val HEARTBEAT_FILE = "notification_engagement_heartbeat.csv"
        private const val LATENCY_FILE = "daily_notification_latency.csv"

        private const val FEATURE_SCHEMA_VERSION = "1"
        private const val DEFAULT_HEADER =
            "record_id,participant_id,date,feature_schema_version,event,notif_posted,notif_engaged,notif_engagement_rate,notif_latency_median_s,notif_latency_n"
        private const val HEARTBEAT_HEADER = "ts,message"

        private val DATE_RE = Regex("""^\d{4}-\d{2}-\d{2}$""")
    }
}
