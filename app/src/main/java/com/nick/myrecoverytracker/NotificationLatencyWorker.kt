// app/src/main/java/com/nick/myrecoverytracker/NotificationLatencyWorker.kt
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

class NotificationLatencyWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val files = ctx.filesDir

        val inFile = File(files, IN_FILE)
        val outFile = File(files, OUT_FILE)

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone).toString()
        val yesterday = LocalDate.now(zone).minusDays(1).toString()

        probe("START doWork() today=$today yesterday=$yesterday in_exists=${inFile.exists()} in_size=${if (inFile.exists()) inFile.length() else 0} out_exists=${outFile.exists()} out_size=${if (outFile.exists()) outFile.length() else 0}")
        heartbeat("START today=$today yesterday=$yesterday")

        val participantId = ParticipantIdManager.getOrCreate(ctx)
        val header = DEFAULT_HEADER
        ensureHeader(outFile, header)

        // Map: date -> list of latencies (in seconds)
        val latenciesByDate = mutableMapOf<String, MutableList<Double>>()
        var parseFailed = false

        if (inFile.exists()) {
            try {
                // First pass: collect all events keyed by (date, notification_id)
                val events = mutableMapOf<Pair<String, String>, MutableList<NotifEvent>>()

                inFile.forEachLine { raw ->
                    val line = raw.trimStart('\uFEFF').trim()
                    if (line.isEmpty() || isHeaderLike(line)) return@forEachLine

                    val cols = readCols(line)
                    if (cols.size < 6) return@forEachLine

                    val ts = cols[0].trim()
                    val eventType = cols[1].trim().uppercase(Locale.US)
                    val notifId = cols[3].trim()
                    val rawReason = cols[5].trim().uppercase(Locale.US)

                    // Extract date from timestamp (ISO 8601: yyyy-MM-dd'T'HH:mm:ss.SSSZ)
                    val date = if (ts.length >= 10) ts.substring(0, 10) else ""
                    if (!DATE_RE.matches(date)) return@forEachLine

                    val key = Pair(date, notifId)
                    events.getOrPut(key) { mutableListOf() }.add(
                        NotifEvent(ts = ts, eventType = eventType, rawReason = rawReason)
                    )
                }

                // Second pass: match POSTED with REMOVED(CLICK) and calculate latencies
                events.forEach { (key, eventList) ->
                    val (date, _) = key
                    val posted = eventList.find { it.eventType == "POSTED" }
                    val removed = eventList.find { it.eventType == "REMOVED" && it.rawReason == "CLICK" }

                    if (posted != null && removed != null) {
                        val latencySec = calculateLatency(posted.ts, removed.ts)
                        if (latencySec >= 0.0) {
                            latenciesByDate.getOrPut(date) { mutableListOf() }.add(latencySec)
                        }
                    }
                }
            } catch (t: Throwable) {
                parseFailed = true
                Log.e(TAG, "Failed parsing $IN_FILE (continuing with forced rows)", t)
                probe("PARSE_FAIL ${t.javaClass.simpleName}: ${t.message}")
                heartbeat("PARSE_FAIL ${t.javaClass.simpleName}: ${t.message}")
            }
        } else {
            probe("NO_INPUT_FILE (will still force rows)")
            heartbeat("NO_INPUT_FILE")
        }

        // 🔒 HARD INVARIANT FOR MYRA:
        // Always materialise yesterday (and today)
        latenciesByDate.putIfAbsent(yesterday, mutableListOf())
        latenciesByDate.putIfAbsent(today, mutableListOf())

        val existing = try {
            outFile.readLines()
                .map { it.trimEnd('\r') }
                .drop(1)
                .filter { it.isNotBlank() }
                .filter { isValidRow(it) }  // ← Skip rows with wrong schema
                .associateBy { it.substringBefore(',') }
                .toMutableMap()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed reading existing $OUT_FILE (will rebuild)", t)
            probe("READ_EXISTING_FAIL ${t.javaClass.simpleName}: ${t.message}")
            heartbeat("READ_EXISTING_FAIL ${t.javaClass.simpleName}: ${t.message}")
            mutableMapOf()
        }

        latenciesByDate.forEach { (date, latencies) ->
            val recordId = "${participantId}_$date"
            val median = if (latencies.isNotEmpty()) calculateMedian(latencies) else 0.0
            val count = latencies.size

            existing[date] = String.format(
                Locale.US,
                "%s,%s,%s,%s,%.6f,%d",
                recordId,
                participantId,
                date,
                FEATURE_SCHEMA_VERSION,
                median,
                count
            )
        }

        val rebuilt = buildString {
            append(header).append('\n')
            existing.keys.sorted().forEach { d ->
                append(existing[d]).append('\n')
            }
        }

        try {
            outFile.writeText(rebuilt)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed writing $OUT_FILE", t)
            probe("WRITE_FAIL ${t.javaClass.simpleName}: ${t.message}")
            heartbeat("WRITE_FAIL ${t.javaClass.simpleName}: ${t.message}")
            return@withContext Result.failure()
        }

        // Dump file content to logcat
        try {
            val lines = outFile.readLines()
            probe("END success wrote_rows=${existing.size} forced_yesterday=$yesterday forced_today=$today parseFailed=$parseFailed out=${outFile.absolutePath}")
            lines.forEach { probe("ROW: $it") }
        } catch (t: Throwable) {
            probe("END success wrote_rows=${existing.size} forced_yesterday=$yesterday forced_today=$today parseFailed=$parseFailed out=${outFile.absolutePath} (dump_failed: ${t.message})")
        }

        heartbeat("END success wrote_rows=${existing.size} forced_yesterday=$yesterday forced_today=$today parseFailed=$parseFailed")

        Result.success()
    }

    private fun isValidRow(line: String): Boolean {
        // Expected format: record_id,participant_id,date,feature_schema_version,notif_latency_median_s,notif_latency_n
        val cols = readCols(line)
        if (cols.size != 6) return false

        val date = cols[2].trim()
        val schema = cols[3].trim()

        // Must match current schema version and date format
        return schema == FEATURE_SCHEMA_VERSION && DATE_RE.matches(date)
    }

    private fun calculateLatency(postedTs: String, removedTs: String): Double {
        return try {
            val posted = TS_FMT.parse(postedTs)?.time ?: return -1.0
            val removed = TS_FMT.parse(removedTs)?.time ?: return -1.0
            (removed - posted) / 1000.0 // Convert ms to seconds
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to parse timestamps: $postedTs, $removedTs", t)
            -1.0
        }
    }

    private fun calculateMedian(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2.0
        } else {
            sorted[mid].toDouble()
        }
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

    private fun probe(msg: String) {
        Log.e(PROBE, msg)
    }

    private fun heartbeat(msg: String) {
        try {
            val hb = File(applicationContext.filesDir, HEARTBEAT_FILE)
            if (!hb.exists() || hb.length() == 0L) {
                hb.parentFile?.mkdirs()
                hb.writeText("ts,message\n")
            }
            hb.appendText("${System.currentTimeMillis()},$msg\n")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to write heartbeat", t)
        }
    }

    private data class NotifEvent(
        val ts: String,
        val eventType: String,
        val rawReason: String
    )

    companion object {
        private const val TAG = "NotificationLatencyWorker"
        private const val PROBE = "NOTIF_LAT_PROBE"

        private const val IN_FILE = "notification_log.csv"
        private const val OUT_FILE = "daily_notification_latency.csv"
        private const val HEARTBEAT_FILE = "notification_latency_heartbeat.csv"

        private const val FEATURE_SCHEMA_VERSION = "1"
        private const val DEFAULT_HEADER =
            "record_id,participant_id,date,feature_schema_version,notif_latency_median_s,notif_latency_n"

        private val DATE_RE = Regex("""^\d{4}-\d{2}-\d{2}$""")
        private val TS_FMT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    }
}