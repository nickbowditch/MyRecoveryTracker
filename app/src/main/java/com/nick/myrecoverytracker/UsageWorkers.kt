package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.*

object UsageWorkers {
    private const val UNIQUE_USAGE_DAILY = "mrt_usage_daily"

    fun ensure(ctx: Context) {
        // Periodic once-daily run, with a small flex so it can coalesce with doze
        val req = PeriodicWorkRequestBuilder<UsageCaptureWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(Constraints.NONE)
            .setInitialDelay(15, java.util.concurrent.TimeUnit.MINUTES) // give the device time after boot
            .addTag(UNIQUE_USAGE_DAILY)
            .build()

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            UNIQUE_USAGE_DAILY,
            ExistingPeriodicWorkPolicy.UPDATE,
            req
        )
    }

    fun runNow(ctx: Context) {
        val now = OneTimeWorkRequestBuilder<UsageCaptureWorker>().build()
        WorkManager.getInstance(ctx).enqueue(now)
    }
}