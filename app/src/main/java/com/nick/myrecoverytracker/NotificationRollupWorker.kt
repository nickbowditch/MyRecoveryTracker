// app/src/main/java/com/nick/myrecoverytracker/NotificationRollupWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.util.Locale

class NotificationRollupWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    override fun doWork(): Result {
        return try {
            val ctx = applicationContext
            val files = ctx.filesDir

            val inFile = File(files, LOG_FILE)
            val outFile = File(files, OUT_FILE)
            val lockFile = File(ctx.filesDir.parentFile, LOCK_FILE)

            val header = readHeaderLock(lockFile).ifEmpty { DEFAULT_HEADER }

            if (!inFile.exists()) {
                ensureHeader(outFile, header)
                Log.i(TAG, "No $LOG_FILE; wrote header only.")
                return Result.success()
            }

            val agg = mutableMapOf<String, Counts>()

            var lineNo = 0
            inFile.forEachLine { raw ->
                lineNo++
                val line = raw.trim()
                if (line.isEmpty()) return@forEachLine

                if (lineNo == 1 && isHeaderRow(line)) return@forEachLine
                if (line.startsWith("timestamp,")) return@forEachLine
                if (line.startsWith("ts,")) return@forEachLine

                val firstComma = line.indexOf(',')
                if (firstComma <= 0) return@forEachLine
                val tsField = line.substring(0, firstComma)
                val date = tsField.take(10)
                if (!DATE_RE.matches(date)) return@forEachLine

                when (normalizeEvent(line)) {
                    EventKind.DELIVERED -> agg.getOrPut(date) { Counts() }.delivered++
                    EventKind.OPENED    -> agg.getOrPut(date) { Counts() }.opened++
                    EventKind.OTHER     -> Unit
                }
            }

            val lines = mutableListOf<String>().apply {
                if (outFile.exists()) outFile.readLines().forEach { add(it.trimEnd('\r')) }
            }

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
                val row = String.format(Locale.US, "%s,%s,%d,%d,%.6f",
                    d, FEATURE_SCHEMA_VERSION, c.delivered, c.opened, rate)
                byDate[d] = row
            }

            val rebuilt = buildList {
                add(header)
                byDate.keys.sorted().forEach { d -> add(byDate[d]!!) }
            }

            outFile.writeText(rebuilt.joinToString("\n") + "\n")
            Log.i(TAG, "NotificationRollup → wrote ${agg.size} date(s) to ${outFile.name}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "NotificationRollup failed", t)
            Result.failure()
        }
    }

    private fun readHeaderLock(lock: File): String =
        try { if (lock.exists()) lock.readText().trim().replace("\r", "") else "" } catch (_: Throwable) { "" }

    private fun ensureHeader(out: File, header: String) {
        if (!out.exists() || out.length() == 0L) {
            out.parentFile?.mkdirs()
            out.writeText("$header\n")
        } else {
            val cur = out.useLines { seq -> seq.firstOrNull() ?: "" }.trim().replace("\r", "")
            if (cur != header) {
                val rest = out.readLines().drop(1)
                out.writeText(buildString {
                    append(header).append('\n')
                    rest.forEach { append(it.trimEnd('\r')).append('\n') }
                })
            }
        }
    }

    private fun isHeaderRow(line: String): Boolean {
        val l = line.lowercase(Locale.US)
        return l.startsWith("timestamp,") || l.startsWith("ts,")
    }

    private fun normalizeEvent(line: String): EventKind {
        val lower = line.lowercase(Locale.US)
        val posted = "po" + "sted"
        val removed = "re" + "moved"
        val click = "cli" + "ck"
        val clicked = click + "ed"

        return when {
            lower.contains("," + posted) -> EventKind.DELIVERED
            lower.contains("," + removed) && lower.contains("," + click) -> EventKind.OPENED
            lower.contains("," + click) || lower.contains("," + clicked) -> EventKind.OPENED
            else -> EventKind.OTHER
        }
    }

    private data class Counts(var delivered: Int = 0, var opened: Int = 0)
    private enum class EventKind { DELIVERED, OPENED, OTHER }

    companion object {
        private const val TAG = "NotificationRollupWorker"
        private const val LOG_FILE = "notification_log.csv"
        private const val OUT_FILE = "daily_notification_engagement.csv"
        private const val LOCK_FILE = "app/locks/daily_notif_engagement.head"
        private const val FEATURE_SCHEMA_VERSION = "1"
        private const val DEFAULT_HEADER = "date,feature_schema_version,delivered,opened,open_rate"
        private val DATE_RE = Regex("""^\d{4}-\d{2}-\d{2}$""")
    }
}