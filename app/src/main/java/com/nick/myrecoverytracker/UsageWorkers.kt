// app/src/main/java/com/nick/myrecoverytracker/UsageWorkers.kt
package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.*

object UsageWorkers {
    private const val UNIQUE_USAGE_DAILY = "mrt_usage_daily"
    private const val UNIQUE_USAGE_EVENTS_DAILY = "mrt_usage_events_daily"

    fun ensure(ctx: Context) {
        // Periodic once-daily run for UsageCaptureWorker
        val captureReq = PeriodicWorkRequestBuilder<UsageCaptureWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(Constraints.NONE)
            .setInitialDelay(15, java.util.concurrent.TimeUnit.MINUTES)
            .addTag(UNIQUE_USAGE_DAILY)
            .build()

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            UNIQUE_USAGE_DAILY,
            ExistingPeriodicWorkPolicy.UPDATE,
            captureReq
        )

        // Periodic once-daily run for UsageEventsDailyWorker
        val eventsReq = PeriodicWorkRequestBuilder<UsageEventsDailyWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(Constraints.NONE)
            .setInitialDelay(20, java.util.concurrent.TimeUnit.MINUTES)
            .addTag(UNIQUE_USAGE_EVENTS_DAILY)
            .build()

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            UNIQUE_USAGE_EVENTS_DAILY,
            ExistingPeriodicWorkPolicy.UPDATE,
            eventsReq
        )
    }

    fun runNow(ctx: Context) {
        val now = OneTimeWorkRequestBuilder<UsageCaptureWorker>().build()
        WorkManager.getInstance(ctx).enqueue(now)
    }
}