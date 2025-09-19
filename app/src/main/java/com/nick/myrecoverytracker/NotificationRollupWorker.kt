// app/src/main/java/com/nick/myrecoverytracker/NotificationRollupWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToLong

// Single, canonical DayAgg (visible to this file and callers)
data class DayAgg(
    var posted: Int = 0,
    var removed: Int = 0,
    var clicked: Int = 0,
    val clickLatenciesSec: MutableList<Long> = mutableListOf()
)

class NotificationRollupWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    override fun doWork(): Result {
        return try {
            val dir = applicationContext.filesDir
            val logFile = File(dir, LOG_FILE)
            if (!logFile.exists() || logFile.length() == 0L) {
                Log.i(TAG, "no notification_log.csv; nothing to roll up")
                return Result.success()
            }

            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }
            val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }

            data class PostKey(val pkg: String, val title: String, val text: String)

            val dayMap = linkedMapOf<String, DayAgg>()
            val lastPost = HashMap<PostKey, Long>()

            for (logical in readLogicalCsvRecords(logFile)) {
                val row = logical.trimStart('\uFEFF').trim()
                if (row.isEmpty() || row.startsWith("timestamp,")) continue
                val cols = parseCsv(row)
                if (cols.size < 6) continue

                val tsStr  = cols[0]
                val pkg    = cols[1]
                val title  = cols[2]
                val text   = cols[3]
                val event  = cols[4].trim().lowercase(Locale.US)
                val reason = cols.getOrNull(5)?.trim()?.uppercase(Locale.US) ?: ""

                val t = runCatching { df.parse(tsStr)?.time }.getOrNull() ?: continue
                val day = dayFmt.format(t)

                val agg = dayMap.getOrPut(day) { DayAgg() }
                when (event) {
                    "posted" -> {
                        agg.posted += 1
                        lastPost[PostKey(pkg, title, text)] = t
                    }
                    "removed" -> {
                        agg.removed += 1
                        if (reason.contains("CLICK")) {
                            agg.clicked += 1
                            val key = PostKey(pkg, title, text)
                            val postT = lastPost[key]
                            if (postT != null && t >= postT) {
                                val sec = ((t - postT) / 1000.0).roundToLong()
                                agg.clickLatenciesSec += sec
                            }
                        }
                    }
                }
            }

            writeEngagement(File(dir, ENGAGEMENT_FILE), dayMap)
            writeLatency(File(dir, LATENCY_FILE), dayMap)

            Log.i(TAG, "notification rollup complete days=${dayMap.size}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "rollup failed", t)
            Result.failure()
        }
    }

    private fun writeEngagement(out: File, dayMap: Map<String, DayAgg>) {
        rewriteDaily(out, "date,posted,removed,clicked\n", dayMap.keys) { d ->
            val a = dayMap[d]!!
            "$d,${a.posted},${a.removed},${a.clicked}\n"
        }
    }

    private fun writeLatency(out: File, dayMap: Map<String, DayAgg>) {
        rewriteDaily(out, "date,avg_click_latency_s,median_click_latency_s,count_clicked\n", dayMap.keys) { d ->
            val a = dayMap[d]!!
            val n = a.clickLatenciesSec.size
            if (n == 0) "$d,0,0,0\n"
            else {
                val avg = a.clickLatenciesSec.average().roundToLong()
                val med = median(a.clickLatenciesSec)
                "$d,$avg,$med,$n\n"
            }
        }
    }

    private fun median(vals: List<Long>): Long {
        val s = vals.sorted()
        val n = s.size
        return if (n == 0) 0 else if (n % 2 == 1) s[n / 2] else ((s[n / 2 - 1] + s[n / 2]) / 2.0).roundToLong()
    }

    private fun rewriteDaily(out: File, header: String, days: Set<String>, rowForDay: (String) -> String) {
        val keep = mutableListOf<String>()
        if (out.exists()) {
            out.forEachLine { line ->
                if (line.startsWith("date,") || line.isBlank()) return@forEachLine
                val d = line.take(10)
                if (d !in days) keep += line
            }
        }
        val sb = StringBuilder()
        sb.append(header)
        keep.forEach { sb.append(it).append('\n') }
        days.sorted().forEach { d -> sb.append(rowForDay(d)) }
        out.writeText(sb.toString())
    }

    private fun readLogicalCsvRecords(f: File): Sequence<String> = sequence {
        val text = f.readText()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when (c) {
                '"' -> {
                    if (inQuotes && i + 1 < text.length && text[i + 1] == '"') {
                        sb.append('"'); i += 1
                    } else {
                        inQuotes = !inQuotes
                    }
                    sb.append(c)
                }
                '\n' -> {
                    if (inQuotes) {
                        sb.append(c)
                    } else {
                        yield(sb.toString())
                        sb.setLength(0)
                    }
                }
                '\r' -> {}
                else -> sb.append(c)
            }
            i += 1
        }
        if (sb.isNotEmpty()) yield(sb.toString())
    }

    private fun parseCsv(line: String): List<String> {
        val out = ArrayList<String>(6)
        val sb = StringBuilder()
        var i = 0
        var inQuotes = false
        while (i < line.length) {
            val c = line[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"'); i += 1
                    } else {
                        inQuotes = false
                    }
                } else {
                    sb.append(c)
                }
            } else {
                when (c) {
                    ',' -> { out.add(sb.toString()); sb.setLength(0) }
                    '"' -> inQuotes = true
                    else -> sb.append(c)
                }
            }
            i += 1
        }
        out.add(sb.toString())
        return out
    }

    companion object {
        private const val TAG = "NotificationRollupWorker"
        private const val LOG_FILE = "notification_log.csv"
        private const val ENGAGEMENT_FILE = "daily_notification_engagement.csv"
        private const val LATENCY_FILE = "daily_notification_latency.csv"
    }
}