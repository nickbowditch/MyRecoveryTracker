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
import java.util.Locale
import java.util.Date
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class NotificationLatencyWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val inFile = File(ctx.filesDir, IN_FILE)
        val outFile = File(ctx.filesDir, OUT_FILE)

        if (!inFile.exists()) {
            ensureHeader(outFile)
            val today = today()
            upsertRow(outFile, today, 0.0, 0.0, 0.0, 0)
            Log.i(TAG, "No $IN_FILE found; wrote empty latency row for $today")
            return@withContext Result.success()
        }

        return@withContext try {
            val lines = inFile.readLines()
            if (lines.isEmpty()) {
                ensureHeader(outFile)
                val today = today()
                upsertRow(outFile, today, 0.0, 0.0, 0.0, 0)
                Log.i(TAG, "Empty $IN_FILE; wrote empty latency row for $today")
                Result.success()
            } else {
                val entries = lines.drop(1).mapNotNull { parseLogLine(it) }
                val latenciesByClickedDay = computeLatenciesByClickedDay(entries)

                ensureHeader(outFile)
                if (latenciesByClickedDay.isEmpty()) {
                    val today = today()
                    upsertRow(outFile, today, 0.0, 0.0, 0.0, 0)
                    Log.i(TAG, "NotificationLatency: no pairs; wrote empty row for $today")
                } else {
                    for ((day, msList) in latenciesByClickedDay) {
                        val p50 = percentile(msList, 50.0)
                        val p90 = percentile(msList, 90.0)
                        val p99 = percentile(msList, 99.0)
                        upsertRow(outFile, day, p50, p90, p99, msList.size)
                        Log.i(TAG, "NotificationLatency($day): p50=${p50.toInt()}ms p90=${p90.toInt()}ms p99=${p99.toInt()}ms n=${msList.size}")
                    }
                }
                Result.success()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "NotificationLatencyWorker failed", t)
            Result.retry()
        }
    }

    private fun parseLogLine(line: String): LogEvent? {
        // notification_log.csv header: ts,event,notif_id
        // Example: 2025-09-30 12:00:00,POSTED,abc
        val parts = line.split(",")
        if (parts.size < 3) return null
        val ts = parts[0].trim()
        val ev = parts[1].trim().uppercase(Locale.US)
        val id = parts[2].trim()
        val epoch = parseTs(ts) ?: return null
        val day = if (ts.length >= 10) ts.substring(0, 10) else return null
        val kind = when (ev) {
            "POSTED", "DELIVERED" -> Ev.POSTED
            "CLICKED", "CLICK", "OPENED" -> Ev.CLICKED
            else -> return null
        }
        return LogEvent(ts = ts, day = day, epochMs = epoch, id = id, ev = kind)
    }

    private fun computeLatenciesByClickedDay(events: List<LogEvent>): Map<String, MutableList<Double>> {
        // Pair POSTED -> first CLICKED with same notif_id where click time >= post time.
        // Bucket by CLICKED day (yyyy-MM-dd of the CLICKED ts).
        val postedStacks = HashMap<String, ArrayDeque<LogEvent>>()
        for (e in events) {
            if (e.ev == Ev.POSTED) {
                postedStacks.getOrPut(e.id) { ArrayDeque() }.addLast(e)
            }
        }
        val byDay = HashMap<String, MutableList<Double>>()
        for (e in events) {
            if (e.ev == Ev.CLICKED) {
                val q = postedStacks[e.id] ?: continue
                var match: LogEvent? = null
                // Find the most recent POSTED before or at CLICKED (from end)
                while (q.isNotEmpty()) {
                    val last = q.removeLast()
                    if (last.epochMs <= e.epochMs) {
                        match = last
                        break
                    }
                }
                match?.let { p ->
                    val dtMs = e.epochMs - p.epochMs
                    if (dtMs >= 0) {
                        byDay.getOrPut(e.day) { ArrayList() }.add(dtMs.toDouble())
                    }
                }
            }
        }
        return byDay
    }

    private fun ensureHeader(out: File) {
        val header = "date,feature_schema_version,p50_ms,p90_ms,p99_ms,count"
        if (!out.exists()) {
            out.parentFile?.mkdirs()
            out.writeText("$header\n")
        } else {
            val cur = out.readLines()
            if (cur.isEmpty() || cur.first() != header) {
                val rest = if (cur.isNotEmpty()) cur.drop(1) else emptyList()
                out.writeText((listOf(header) + rest).joinToString("\n").trimEnd() + "\n")
            }
        }
    }

    private fun upsertRow(out: File, day: String, p50: Double, p90: Double, p99: Double, n: Int) {
        val header = "date,feature_schema_version,p50_ms,p90_ms,p99_ms,count"
        val rows = if (out.exists()) out.readLines().toMutableList() else mutableListOf(header)
        if (rows.isEmpty() || rows.first() != header) {
            rows.clear()
            rows.add(header)
        }
        rows.removeAll { it.startsWith("$day,") }
        fun r(x: Double) = floor(x + 0.5).toLong().toString()
        rows.add(listOf(day, FEATURE_SCHEMA_VERSION, r(p50), r(p90), r(p99), n.toString()).joinToString(","))
        out.writeText(rows.joinToString("\n").trimEnd() + "\n")
    }

    private fun percentile(values: List<Double>, p: Double): Double {
        if (values.isEmpty()) return 0.0
        val s = values.sorted()
        val clampedP = min(100.0, max(0.0, p))
        val rank = (clampedP / 100.0) * (s.size - 1)
        val lo = floor(rank).toInt()
        val hi = min(s.size - 1, lo + 1)
        val frac = rank - lo
        return s[lo] * (1 - frac) + s[hi] * frac
    }

    private fun parseTs(ts: String): Long? =
        try { SDF.parse(ts)?.time } catch (_: Throwable) { null }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private data class LogEvent(
        val ts: String,
        val day: String,   // day of THIS event (POSTED or CLICKED)
        val epochMs: Long,
        val id: String,
        val ev: Ev
    )

    private enum class Ev { POSTED, CLICKED }

    companion object {
        private const val TAG = "NotificationLatencyWorker"
        private const val IN_FILE = "notification_log.csv"
        private const val OUT_FILE = "daily_notification_latency.csv"
        private const val FEATURE_SCHEMA_VERSION = "v6.0"
        private val SDF = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}