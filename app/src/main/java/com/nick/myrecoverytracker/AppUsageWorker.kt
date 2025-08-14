package com.nick.myrecoverytracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlin.math.ln

class AppUsageWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        // Use the actual helper you have in the project
        if (!UsagePermissionHelper.isGranted(applicationContext)) {
            Log.w("AppUsageWorker", "❌ Usage access not granted")
            // Succeed gracefully so WorkManager doesn't spam retries
            return Result.success()
        }

        val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24L * 60 * 60 * 1000 // last 24h

        // Null-safe call; some OEMs can return null
        val usageStats: List<UsageStats> = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        ) ?: emptyList()

        if (usageStats.isEmpty()) {
            Log.w("AppUsageWorker", "⚠️ No usage data found in past 24h (likely permission not granted or no activity)")
            return Result.success()
        }

        // Aggregate per package and keep only >0 foreground time
        val appTimeMap: Map<String, Long> = usageStats
            .groupBy { it.packageName }
            .mapValues { (_, list) -> list.sumOf { it.totalTimeInForeground.coerceAtLeast(0L) } }
            .filterValues { it > 0L }

        val entropy = computeEntropy(appTimeMap.values)
        Log.i("AppUsageWorker", "📊 Daily app usage entropy: $entropy")

        MetricsStore.saveAppUsageEntropy(applicationContext, entropy)
        return Result.success()
    }

    private fun computeEntropy(usages: Collection<Long>): Double {
        val total = usages.sum()
        if (total == 0L) return 0.0
        var h = 0.0
        for (t in usages) {
            val p = t.toDouble() / total.toDouble()
            h += -p * ln(p) // nats; MetricsStore stores the raw value
        }
        return h
    }
}