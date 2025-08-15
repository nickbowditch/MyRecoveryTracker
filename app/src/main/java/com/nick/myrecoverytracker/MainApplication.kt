package com.nick.myrecoverytracker

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainApplication : Application() {

    private var userPresentReceiver: UnlockReceiver? = null

    override fun onCreate() {
        super.onCreate()

        // 1) Periodic unlock scan every 15m (existing)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "unlock_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<UnlockWorker>(15, TimeUnit.MINUTES).build()
        )

        // 2) Fast-path USER_PRESENT while process is alive (existing)
        userPresentReceiver = UnlockReceiver().also {
            val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
            registerReceiver(it, filter)
        }

        // 3) Daily DistanceSummaryWorker (~23:55 local)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "distance_summary_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<DistanceSummaryWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(23, 55), TimeUnit.MILLISECONDS)
                .build()
        )

        // 4) Daily CallTotalsWorker (~23:56 local)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "call_totals_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<CallTotalsWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(23, 56), TimeUnit.MILLISECONDS)
                .build()
        )

        // 5) Daily RecoveryVisitsWorker (~23:57 local)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recovery_visits_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<RecoveryVisitsWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(23, 57), TimeUnit.MILLISECONDS)
                .build()
        )

        // 6) Accelerometer sampling every 15m for movement intensity
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "movement_sample_15m",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SensorSampleWorker>(15, TimeUnit.MINUTES).build()
        )

        // 7) Roll-up job once per day (~23:58 local)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "movement_intensity_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<MovementIntensityWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(23, 58), TimeUnit.MILLISECONDS)
                .build()
        )

        // 8) NEW: late-night screen usage rollup (run ~05:10 local)
        // This checks unlocks between 00:00–05:00 of *today* and writes Y/N.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "late_night_usage_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<LateNightScreenUsageWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(5, 10), TimeUnit.MILLISECONDS)
                .build()
        )

        // 9) Daily SMS (sent/received) around 23:59 (already wired earlier)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "messages_sent_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<MessagesSentWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(23, 59), TimeUnit.MILLISECONDS)
                .build()
        )
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "messages_received_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<MessagesReceivedWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(23, 59), TimeUnit.MILLISECONDS)
                .build()
        )

        // 10) App usage categories daily (~23:59)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "app_usage_category_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<AppUsageCategoryWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntil(23, 59), TimeUnit.MILLISECONDS)
                .build()
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        userPresentReceiver?.let { unregisterReceiver(it) }
        userPresentReceiver = null
    }

    /** ms from now until [hour:minute] local time tomorrow/tonight */
    private fun millisUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis - now.timeInMillis
    }
}