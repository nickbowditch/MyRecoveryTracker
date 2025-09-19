package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

/**
 * Daily notification response latency (post -> engagement).
 *
 * Reads both old and new NotificationListener log formats.
 * Writes: files/daily_notification_latency.csv
 * Columns: date,avg_seconds,median_seconds,n_engagements
 */
class NotificationLatencyWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val inFile = File(ctx.filesDir, "notification_log.csv")
        val outFile = File(ctx.filesDir, "daily_notification_latency.csv")
        val day = today()

        if (!inFile.exists()) {
            writeRow(outFile, day, 0.0, 0.0, 0)
            Log.i(TAG, "No notification_log.csv; wrote empty latency row for $day")
            return@withContext Result.success()
        }

        return@withContext try {
            val entries = inFile.readLines()
                .asSequence()
                .mapNotNull { parseLine(it) }
                .filter { it.date.startsWith(day) }
                .toList()

            if (entries.isEmpty()) {
                writeRow(outFile, day, 0.0, 0.0, 0)
                Log.i(TAG, "No today entries; wrote empty latency row for $day")
                Result.success()
            } else {
                val postedStacks = HashMap<String, ArrayDeque<Entry>>()
                fun keyOf(e: Entry): String {
                    fun norm(s: String) = s.trim().lowercase(Locale.US)
                    return "${norm(e.pkg)}|${norm(e.title)}|${norm(e.text)}"
                }

                for (e in entries) {
                    if (e.event == "posted") {
                        postedStacks.getOrPut(keyOf(e)) { ArrayDeque() }.addLast(e)
                    }
                }

                val engagedReasons = setOf("CLICK", "CANCEL", "CANCEL_ALL", "GROUP_SUMMARY_CANCELED")
                val latenciesSec = ArrayList<Double>()

                for (e in entries) {
                    if (e.event == "removed" && engagedReasons.contains((e.reason ?: "").uppercase(Locale.US))) {
                        val stack = postedStacks[keyOf(e)] ?: continue
                        var candidate: Entry? = null
                        while (stack.isNotEmpty()) {
                            val last = stack.removeLast()
                            if (last.epochMs <= e.epochMs) { candidate = last; break }
                        }
                        candidate?.let { post ->
                            val secs = (e.epochMs - post.epochMs) / 1000.0
                            if (secs >= 0) latenciesSec.add(secs)
                        }
                    }
                }

                if (latenciesSec.isEmpty()) {
                    writeRow(outFile, day, 0.0, 0.0, 0)
                    Log.i(TAG, "NotificationLatency($day): no paired engagements")
                } else {
                    val avg = latenciesSec.average()
                    val median = median(latenciesSec)
                    writeRow(outFile, day, avg, median, latenciesSec.size)
                    Log.i(TAG, "NotificationLatency($day): avg=${"%.3f".format(avg)}s median=${"%.3f".format(median)}s n=${latenciesSec.size}")
                }
                Result.success()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "NotificationLatencyWorker failed", t)
            Result.retry()
        }
    }

    private fun parseLine(line: String): Entry? {
        try {
            val parts = smartSplit(line) ?: return null
            if (parts.size < 5) return null

            val tsStr = parts[0]
            val pkg   = parts[1]
            val title = unquote(parts[2])
            val text  = unquote(parts[3])
            val tail  = parts.drop(4).joinToString(",").trim()

            var event: String? = null
            var reason: String? = null

            val lowerTail = tail.lowercase(Locale.US)
            if (lowerTail.startsWith("event=posted") || lowerTail == "posted") {
                event = "posted"
            } else if (lowerTail.startsWith("event=removed")) {
                event = "removed"
                val rx = Regex("""reason=([A-Za-z_]+)""")
                reason = rx.find(tail)?.groupValues?.getOrNull(1)
            } else if (lowerTail.startsWith("removed")) {
                event = "removed"
                val after = tail.split(",", limit = 2)
                if (after.size >= 2) reason = after[1].trim()
            } else if (lowerTail.startsWith("posted")) {
                event = "posted"
            } else {
                return null
            }

            val epoch = parseTs(tsStr) ?: return null
            return Entry(
                date = tsStr.substring(0, 10),
                epochMs = epoch,
                pkg = pkg,
                title = title,
                text = text,
                event = event,
                reason = reason
            )
        } catch (_: Throwable) {
            return null
        }
    }

    private fun smartSplit(line: String): List<String>? {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (c in line) {
            when (c) {
                '"' -> { inQuotes = !inQuotes; sb.append(c) }
                ',' -> if (inQuotes) sb.append(c) else { out.add(sb.toString()); sb.setLength(0) }
                else -> sb.append(c)
            }
        }
        out.add(sb.toString())
        return out
    }

    private fun unquote(s: String): String =
        if (s.length >= 2 && s.first() == '"' && s.last() == '"') s.substring(1, s.length - 1) else s

    private fun parseTs(ts: String): Long? = try { SDF.parse(ts)?.time } catch (_: Throwable) { null }

    private fun writeRow(out: File, day: String, avg: Double, median: Double, n: Int) {
        val header = "date,avg_seconds,median_seconds,n_engagements"
        val lines = if (out.exists()) out.readLines().toMutableList() else mutableListOf()

        if (lines.isEmpty()) {
            lines.add(header)
        } else if (lines.first() != header) {
            // file was corrupted, reset it
            lines.clear()
            lines.add(header)
        }

        // remove today’s row if it exists
        lines.removeAll { it.startsWith("$day,") }

        fun r3(x: Double) = floor(x * 1000.0 + 0.5) / 1000.0
        lines.add("$day,${r3(avg)},${r3(median)},$n")

        out.writeText(lines.joinToString("\n") + "\n")
    }

    private fun median(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val s = values.sorted()
        val n = s.size
        return if (n % 2 == 1) s[n / 2] else (s[n / 2 - 1] + s[n / 2]) / 2.0
    }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    data class Entry(
        val date: String,
        val epochMs: Long,
        val pkg: String,
        val title: String,
        val text: String,
        val event: String?,
        val reason: String?
    )

    companion object {
        private const val TAG = "NotificationLatencyWorker"
        private val SDF = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}