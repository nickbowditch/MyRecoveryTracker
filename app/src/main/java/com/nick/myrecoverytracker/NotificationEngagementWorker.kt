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

/**
 * Aggregates daily notification engagement.
 *
 * Supports BOTH formats in notification_log.csv:
 *  A) Simplified:
 *     YYYY-MM-DD HH:mm:ss,posted,<package>
 *     YYYY-MM-DD HH:mm:ss,engaged,<latencySeconds>
 *
 *  B) Rich CSV:
 *     YYYY-MM-DD HH:mm:ss,<pkg>,"Title","Text",posted,
 *     YYYY-MM-DD HH:mm:ss,<pkg>,"Title","Text",removed,CLICK|CANCEL|CANCEL_ALL|GROUP_SUMMARY_CANCELED
 *
 * Output: files/daily_notification_engagement.csv
 * Columns: date,engagement_rate,posted,engaged
 */
class NotificationEngagementWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val engagedReasons = setOf("CLICK", "CANCEL", "CANCEL_ALL", "GROUP_SUMMARY_CANCELED")
    private val removedReasonRegex = Regex(""",removed,(CLICK|CANCEL|CANCEL_ALL|GROUP_SUMMARY_CANCELED)\b""", RegexOption.IGNORE_CASE)
    private val oldRemovedRegex   = Regex("""event=removed,? *reason=(CLICK|CANCEL|CANCEL_ALL|GROUP_SUMMARY_CANCELED)\b""", RegexOption.IGNORE_CASE)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val day = sdfDay.format(Date())
        val inFile = File(ctx.filesDir, "notification_log.csv")
        val outFile = File(ctx.filesDir, "daily_notification_engagement.csv")

        if (!inFile.exists()) {
            writeRow(outFile, day, 0, 0)
            return@withContext Result.success()
        }

        var posted = 0
        var engaged = 0

        try {
            inFile.forEachLine { raw ->
                val line = raw.trim()
                if (line.isEmpty()) return@forEachLine
                if (line.startsWith("timestamp,")) return@forEachLine // header from rich CSV
                if (!line.startsWith(day)) return@forEachLine         // only today

                // Count posted:
                // - Simplified: ",posted,"
                // - Rich CSV: last column "posted," (still contains ",posted,")
                if (",posted," in line) posted++

                // Count engaged:
                // - Simplified: ",engaged,<latency>"
                // - Rich CSV:  ",removed,CLICK|CANCEL|CANCEL_ALL|GROUP_SUMMARY_CANCELED"
                // - Old tail style: "... event=removed,reason=CLICK"
                when {
                    ",engaged," in line -> engaged++
                    removedReasonRegex.containsMatchIn(line) -> engaged++
                    oldRemovedRegex.containsMatchIn(line) -> engaged++
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to parse notification_log.csv", t)
            writeRow(outFile, day, 0, 0)
            return@withContext Result.success()
        }

        writeRow(outFile, day, posted, engaged)
        Log.i(TAG, "NotificationEngagement($day) posted=$posted engaged=$engaged rate=${rate(posted, engaged)}")
        Result.success()
    }

    private fun rate(posted: Int, engaged: Int): Double =
        if (posted <= 0) 0.0 else engaged.toDouble() / posted.toDouble()

    private fun writeRow(out: File, day: String, posted: Int, engaged: Int) {
        val header = "date,engagement_rate,posted,engaged"
        val lines = if (out.exists()) out.readLines().toMutableList() else mutableListOf()

        if (lines.isEmpty() || lines.first() != header) {
            lines.clear()
            lines += header
        }
        // Ensure idempotency for the day
        lines.removeAll { it.startsWith("$day,") }

        val rate = rate(posted, engaged)
        lines += String.format(Locale.US, "%s,%.6f,%d,%d", day, rate, posted, engaged)

        out.writeText(lines.joinToString("\n") + "\n")
    }

    companion object {
        private const val TAG = "NotificationEngagementWorker"
    }
}