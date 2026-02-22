package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class DailySummaryWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    companion object {
        private const val TAG = "DailySummaryWorker"
    }

    override fun doWork(): Result {
        return try {
            val dir = applicationContext.filesDir
            val outFile = File(dir, "daily_summary.csv")
            val header = "date,total_unlocks,battery_drain_rate,screen_usage_min,notification_count,app_usage_min"

            if (!outFile.exists()) {
                outFile.writeText("$header\n")
                Log.i(TAG, "CREATED $outFile")
            }

            val date = getTodayDate()
            val totalUnlocks = countTodayUnlocks()
            val batteryDrainRate = 0.05  // placeholder until we wire real battery metric
            val screenUsageMin = readScreenUsageMinutes(dir, date)
            val notificationCount = readNotificationCount(dir, date)
            val appUsageMin = readAppUsageMinutes(dir, date)

            val line = "$date,$totalUnlocks,$batteryDrainRate,$screenUsageMin,$notificationCount,$appUsageMin"

            val existing = outFile.readLines().toMutableList()
            val replacedIdx = existing.indexOfFirst { it.startsWith("$date,") }
            if (replacedIdx > 0) {
                existing[replacedIdx] = line
                outFile.writeText(existing.joinToString("\n") + "\n")
            } else {
                outFile.appendText("$line\n")
            }

            Log.i(
                TAG,
                "Wrote daily summary for $date → unlocks=$totalUnlocks, screen_min=$screenUsageMin, notif=$notificationCount, app_min=$appUsageMin"
            )
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to write daily summary", e)
            Result.failure()
        }
    }

    /**
     * FIXED: Now reads from unlock_diag.csv and counts UNLOCK tag events
     * This is the source of truth for unlock counts, not unlock_log.csv
     */
    private fun countTodayUnlocks(): Int {
        val f = File(applicationContext.filesDir, "unlock_diag.csv")
        if (!f.exists()) {
            Log.w(TAG, "unlock_diag.csv not found, returning 0 unlocks")
            return 0
        }

        val today = getTodayDate()
        val lines = f.readLines()

        // Skip header if present
        val body = if (lines.isNotEmpty() && lines[0].contains("ts,tag,extra")) {
            lines.drop(1)
        } else {
            lines
        }

        var count = 0
        for (line in body) {
            if (line.isBlank()) continue
            val parts = line.split(',')
            if (parts.size < 2) continue

            val tsStr = parts[0].trim()
            val tag = parts[1].trim()

            // Count only rows where tag == "UNLOCK" and date matches today
            if (tsStr.startsWith(today) && tag == "UNLOCK") {
                count++
            }
        }

        Log.d(TAG, "Counted $count UNLOCK events from unlock_diag.csv for $today")
        return count
    }

    private fun readScreenUsageMinutes(dir: File, day: String): Double {
        val f = File(dir, "screen_log.csv")
        if (!f.exists()) return 0.0
        val lines = f.readLines()
        if (lines.size <= 1) return 0.0

        val dayPrefix = "$day "
        var lastOnTime: Long? = null
        var totalOnMs = 0L
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        for (line in lines.drop(1)) {
            if (line.isBlank()) continue
            val parts = line.split(',')
            if (parts.size < 2) continue
            val tsStr = parts[0].trim()
            val state = parts[1].trim()
            if (!tsStr.startsWith(dayPrefix)) continue

            val ts = runCatching { fmt.parse(tsStr)?.time }.getOrNull() ?: continue

            if (state.equals("ON", ignoreCase = true)) {
                if (lastOnTime == null) lastOnTime = ts
            } else if (state.equals("OFF", ignoreCase = true)) {
                if (lastOnTime != null) {
                    totalOnMs += (ts - lastOnTime!!).coerceAtLeast(0L)
                    lastOnTime = null
                }
            }
        }

        if (lastOnTime != null) {
            val endOfDay = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .parse("$day 23:59:59")?.time
            if (endOfDay != null) {
                totalOnMs += (endOfDay - lastOnTime!!).coerceAtLeast(0L)
            }
        }

        return (totalOnMs / 60000.0)
    }

    /**
     * FIXED: Now counts only POSTED events, not all notification events
     * notification_log.csv columns: timestamp,package,notification_id,event_type,title,text
     */
    private fun readNotificationCount(dir: File, day: String): Int {
        val f = File(dir, "notification_log.csv")
        if (!f.exists()) {
            Log.w(TAG, "notification_log.csv not found, returning 0")
            return 0
        }

        val lines = f.readLines()
        if (lines.size <= 1) return 0

        var count = 0
        for (line in lines.drop(1)) {
            if (line.isBlank()) continue
            val parts = line.split(',')

            // notification_log has 6 columns: timestamp, package, notification_id, event_type, title, text
            if (parts.size < 4) continue

            val tsStr = parts[0].trim()
            val eventType = parts[3].trim()

            // Extract date from ISO timestamp (YYYY-MM-DDTHH:MM:SS)
            val dayPart = if (tsStr.length >= 10) tsStr.substring(0, 10) else tsStr
            if (dayPart != day) continue

            // Count only POSTED events (notifications shown to user)
            if (eventType == "POSTED") {
                count++
            }
        }

        Log.d(TAG, "Counted $count POSTED notifications for $day")
        return count
    }

    private fun readAppUsageMinutes(dir: File, day: String): Double {
        val f = File(dir, "daily_app_usage_minutes.csv")
        if (!f.exists()) return 0.0
        val lines = f.readLines()
        if (lines.size <= 1) return 0.0

        // header: find "app_min_total" if it exists, else fall back to last column
        val header = lines.first().split(',').map { it.trim() }
        val appTotalIdx = header.indexOfFirst { it.equals("app_min_total", ignoreCase = true) }
        val idx = if (appTotalIdx >= 0) appTotalIdx else header.size - 1

        val row = lines
            .asReversed()
            .firstOrNull { it.startsWith("$day,") }
            ?: return 0.0

        val cols = row.split(',')
        if (idx !in cols.indices) return 0.0

        val value = cols[idx].trim()
        return value.toDoubleOrNull() ?: 0.0
    }

    private fun getTodayDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }
}