package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
            val batteryDrainRate = 0.05  // placeholder metric until you wire in actual
            val screenUsageMin = 0
            val notificationCount = 0
            val appUsageMin = 0

            val line = "$date,$totalUnlocks,$batteryDrainRate,$screenUsageMin,$notificationCount,$appUsageMin"

            val existing = outFile.readLines().toMutableList()
            val replaced = existing.indexOfFirst { it.startsWith("$date,") }
            if (replaced > 0) {
                existing[replaced] = line
                outFile.writeText(existing.joinToString("\n") + "\n")
            } else {
                outFile.appendText("$line\n")
            }

            Log.i(TAG, "✅ Wrote daily summary row for $date → unlocks=$totalUnlocks")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to write daily summary", e)
            Result.failure()
        }
    }

    private fun countTodayUnlocks(): Int {
        val f = File(applicationContext.filesDir, "unlock_log.csv")
        if (!f.exists()) return 0
        val today = getTodayDate()
        return f.readLines().count { it.startsWith(today) && it.contains("UNLOCK", true) }
    }

    private fun getTodayDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }
}