// app/src/main/java/com/nick/myrecoverytracker/NotificationEngagementWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class NotificationEngagementWorker(
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

        val header = DEFAULT_HEADER
        ensureHeader(outFile, header)

        val agg = mutableMapOf<String, Counts>()
        var parseFailed = false

        if (inFile.exists()) {
            try {
                inFile.forEachLine { raw ->
                    val line = raw.trimStart('\uFEFF').trim()
                    if (line.isEmpty() || isHeaderLike(line)) return@forEachLine

                    val firstCol = line.substringBefore(',', "")
                    val date = if (firstCol.length >= 10) firstCol.substring(0, 10) else firstCol
                    if (!DATE_RE.matches(date)) return@forEachLine

                    when (classify(line)) {
                        EventKind.DELIVERED -> agg.getOrPut(date) { Counts() }.delivered++
                        EventKind.OPENED -> agg.getOrPut(date) { Counts() }.opened++
                        EventKind.OTHER -> Unit
                    }
                }
            } catch (t: Throwable) {
                // IMPORTANT: do NOT return early; still force rows + write output
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
        // Always materialise yesterday (and today) so healthchecks never depend on “did any events occur?”
        agg.putIfAbsent(yesterday, Counts())
        agg.putIfAbsent(today, Counts())

        val existing = try {
            outFile.readLines()
                .map { it.trimEnd('\r') }
                .drop(1)
                .filter { it.isNotBlank() }
                .associateBy { it.substringBefore(',') }
                .toMutableMap()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed reading existing $OUT_FILE (will rebuild)", t)
            probe("READ_EXISTING_FAIL ${t.javaClass.simpleName}: ${t.message}")
            heartbeat("READ_EXISTING_FAIL ${t.javaClass.simpleName}: ${t.message}")
            mutableMapOf()
        }

        agg.forEach { (date, c) ->
            val rate = if (c.delivered > 0) c.opened.toDouble() / c.delivered.toDouble() else 0.0
            existing[date] = String.format(
                Locale.US,
                "%s,%s,%d,%d,%.6f",
                date,
                FEATURE_SCHEMA_VERSION,
                c.delivered,
                c.opened,
                rate
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

        probe("END success wrote_rows=${existing.size} forced_yesterday=$yesterday forced_today=$today parseFailed=$parseFailed out=${outFile.absolutePath}")
        heartbeat("END success wrote_rows=${existing.size} forced_yesterday=$yesterday forced_today=$today parseFailed=$parseFailed")

        Result.success()
    }

    private fun ensureHeader(out: File, header: String) {
        if (!out.exists() || out.length() == 0L) {
            out.parentFile?.mkdirs()
            out.writeText("$header\n")
        } else {
            // If header ever drifts, normalise it without nuking data
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
        return l.startsWith("timestamp,") || l.startsWith("ts,")
    }

    // Robust CSV parsing so quoted commas don’t destroy columns
    private fun readCols(line: String): List<String> {
        val out = ArrayList<String>(8)
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

    private fun classify(line: String): EventKind {
        val cols = readCols(line)
        if (cols.size < 2) return EventKind.OTHER

        val ev2 = cols[1].trim().uppercase(Locale.US)
        return when (ev2) {
            "POSTED", "DELIVERED" -> EventKind.DELIVERED
            "CLICKED", "OPENED" -> EventKind.OPENED
            else -> EventKind.OTHER
        }
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

    private data class Counts(
        var delivered: Int = 0,
        var opened: Int = 0
    )

    private enum class EventKind {
        DELIVERED,
        OPENED,
        OTHER
    }

    companion object {
        private const val TAG = "NotificationEngagementWorker"
        private const val PROBE = "NOTIF_ENG_PROBE"

        private const val IN_FILE = "notification_log.csv"
        private const val OUT_FILE = "daily_notification_engagement.csv"
        private const val HEARTBEAT_FILE = "notification_engagement_heartbeat.csv"

        private const val FEATURE_SCHEMA_VERSION = "1"
        private const val DEFAULT_HEADER =
            "date,feature_schema_version,delivered,opened,open_rate"

        private val DATE_RE = Regex("""^\d{4}-\d{2}-\d{2}$""")
    }
}