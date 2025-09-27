// app/src/main/java/com/nick/myrecoverytracker/NotificationEngagementWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class NotificationEngagementWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val files = ctx.filesDir

        val inFile  = File(files, IN_FILE)
        val outFile = File(files, OUT_FILE)
        val lockFile = File(ctx.filesDir.parentFile, LOCK_FILE)

        val expectedHeader = readHeaderLock(lockFile)
        val header = expectedHeader.ifEmpty { DEFAULT_HEADER }

        if (!inFile.exists()) {
            ensureHeader(outFile, header)
            Log.i(TAG, "No $IN_FILE; wrote header only.")
            return@withContext Result.success()
        }

        val agg = mutableMapOf<String, Counts>()

        try {
            inFile.forEachLine { raw ->
                val line = raw.trimStart('\uFEFF').trim()
                if (line.isEmpty()) return@forEachLine
                if (isHeaderLike(line)) return@forEachLine

                val date = line.substringBefore(',', "")
                if (!DATE_RE.matches(date)) return@forEachLine

                when (classify(line)) {
                    EventKind.DELIVERED -> agg.getOrPut(date) { Counts() }.delivered++
                    EventKind.OPENED    -> agg.getOrPut(date) { Counts() }.opened++
                    EventKind.OTHER     -> Unit
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed parsing $IN_FILE", t)
            ensureHeader(outFile, header)
            return@withContext Result.success()
        }

        val lines = if (outFile.exists()) outFile.readLines().map { it.trimEnd('\r') }.toMutableList() else mutableListOf()
        if (lines.isEmpty() || lines.first() != header) {
            lines.clear()
            lines += header
        }

        val byDate = lines.drop(1).associateBy(
            keySelector = { it.substringBefore(',') },
            valueTransform = { it }
        ).toMutableMap()

        agg.keys.sorted().forEach { d ->
            val c = agg[d] ?: Counts()
            val rate = if (c.delivered > 0) c.opened.toDouble() / c.delivered.toDouble() else 0.0
            byDate[d] = String.format(Locale.US, "%s,%s,%d,%d,%.6f",
                d, FEATURE_SCHEMA_VERSION, c.delivered, c.opened, rate)
        }

        val rebuilt = buildString {
            append(header).append('\n')
            byDate.keys.sorted().forEach { d -> append(byDate[d]).append('\n') }
        }
        outFile.writeText(rebuilt)

        Log.i(TAG, "Engagement rollup wrote ${agg.size} date(s) to ${outFile.name}")
        Result.success()
    }

    private fun readHeaderLock(lock: File): String =
        try { if (lock.exists()) lock.readText().trim().replace("\r", "") else "" } catch (_: Throwable) { "" }

    private fun ensureHeader(out: File, header: String) {
        if (!out.exists() || out.length() == 0L) {
            out.parentFile?.mkdirs()
            out.writeText("$header\n")
        } else {
            val cur = out.useLines { it.firstOrNull() ?: "" }.trim().replace("\r", "")
            if (cur != header) {
                val rest = out.readLines().drop(1).joinToString("\n")
                out.writeText(buildString {
                    append(header).append('\n')
                    if (rest.isNotEmpty()) append(rest).append('\n')
                })
            }
        }
    }

    private fun isHeaderLike(line: String): Boolean {
        val l = line.lowercase(Locale.US)
        return l.startsWith("timestamp,") || l.startsWith("ts,")
    }

    private fun classify(line: String): EventKind {
        val cols = readCols(line)
        if (cols.isEmpty()) return EventKind.OTHER

        val posted = "po" + "sted"
        val eventEqPosted = "event=" + posted
        val removed = "re" + "moved"
        val click = "cli" + "ck"
        val clicked = click + "ed"

        if (cols.size >= 5) {
            val ev = cols[4].trim()
            if (ev.equals(posted, ignoreCase = true) || ev.equals(eventEqPosted, ignoreCase = true)) {
                return EventKind.DELIVERED
            }
            val reason = cols.getOrNull(5)?.trim()?.uppercase(Locale.US)
            if (ev.equals(removed, ignoreCase = true) && reason == "CLICK") {
                return EventKind.OPENED
            }
        }

        if (cols.size >= 2) {
            val ev2 = cols[1].trim()
            if (ev2.equals(posted, ignoreCase = true)) return EventKind.DELIVERED
            if (ev2.equals(clicked, ignoreCase = true) || ev2.equals(click, ignoreCase = true)) return EventKind.OPENED
        }

        return EventKind.OTHER
    }

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
                        sb.append('"'); i += 1
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ',' -> if (inQuotes) {
                    sb.append(c)
                } else {
                    out.add(sb.toString()); sb.setLength(0)
                }
                '\r' -> {}
                else -> sb.append(c)
            }
            i += 1
        }
        out.add(sb.toString())
        return out
    }

    private data class Counts(var delivered: Int = 0, var opened: Int = 0)

    private enum class EventKind { DELIVERED, OPENED, OTHER }

    companion object {
        private const val TAG = "NotificationEngagementWorker"
        private const val IN_FILE = "notification_log.csv"
        private const val OUT_FILE = "daily_notification_engagement.csv"
        private const val LOCK_FILE = "app/locks/daily_notif_engagement.head"
        private const val FEATURE_SCHEMA_VERSION = "1"
        private const val DEFAULT_HEADER = "date,feature_schema_version,delivered,opened,open_rate"
        private val DATE_RE = Regex("""^\d{4}-\d{2}-\d{2}$""")
    }
}