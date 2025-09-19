package com.nick.myrecoverytracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Writes today's total foreground usage minutes to:
 *   files/daily_app_usage_minutes.csv
 * Row: yyyy-MM-dd,total_minutes (1-decimal precision)
 *
 * Requires Usage Access; if not granted, writes "0.0" so downstream stays non-sparse.
 */
class AppUsageMinutesWorker(
    appContext: android.content.Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val tag = "AppUsageMinutesWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ctx = applicationContext

            // Use YOUR existing helper
            val hasUsage = UsagePermissionHelper.isGranted(ctx)
            if (!hasUsage) {
                Log.w(tag, "Usage Access not granted; writing 0.0 for today")
                writeToday(0.0)
                return@withContext Result.success()
            }

            val (start, end) = todayRange()
            val usm = ctx.getSystemService(android.content.Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val stats: List<UsageStats> = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, start, end
            ) ?: emptyList()

            var totalMs = 0L
            for (u in stats) totalMs += u.totalTimeInForeground
            val minutes = totalMs / 60000.0
            Log.i(tag, "Total minutes today=$minutes (pkgs=${stats.size})")

            writeToday(minutes)
            Result.success()
        } catch (t: Throwable) {
            Log.e(tag, "Failed to compute app usage minutes", t)
            Result.failure()
        }
    }

    private fun writeToday(totalMinutes: Double) {
        val file = File(applicationContext.filesDir, "daily_app_usage_minutes.csv")
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val rounded = ((totalMinutes * 10.0).roundToInt() / 10.0)
        file.appendText("$day,$rounded\n")
    }

    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis to System.currentTimeMillis()
    }
}