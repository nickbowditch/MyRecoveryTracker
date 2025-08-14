package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DailySummaryWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    companion object {
        private const val TAG = "DailySummaryWorker"
        private const val LOG_FILE = "unlocks_log.csv"
        private const val SUMMARY_FILE = "daily_summary.json"
    }

    override fun doWork(): Result {
        return try {
            val unlocksToday = countTodayUnlocks()
            saveSummary(unlocksToday)
            Log.i(TAG, "✅ Summary written: $unlocksToday unlocks today")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to write daily summary", e)
            Result.failure()
        }
    }

    private fun countTodayUnlocks(): Int {
        val file = File(applicationContext.filesDir, LOG_FILE)
        if (!file.exists()) return 0

        val now = Calendar.getInstance()
        val todayStart = now.clone() as Calendar
        todayStart.set(Calendar.HOUR_OF_DAY, 0)
        todayStart.set(Calendar.MINUTE, 0)
        todayStart.set(Calendar.SECOND, 0)
        todayStart.set(Calendar.MILLISECOND, 0)

        val startMillis = todayStart.timeInMillis
        return file.readLines()
            .mapNotNull { it.toLongOrNull() }
            .count { it >= startMillis }
    }

    private fun saveSummary(unlockCount: Int) {
        val json = JSONObject().apply {
            put("date", getTodayDate())
            put("unlockCount", unlockCount)
        }

        val summaryFile = File(applicationContext.filesDir, SUMMARY_FILE)
        summaryFile.writeText(json.toString(2)) // Pretty-print JSON
    }

    private fun getTodayDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }
}