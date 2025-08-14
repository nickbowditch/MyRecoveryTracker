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

        // === Periodic unlock scan every 15m (existing) ===
        val unlockReq = PeriodicWorkRequestBuilder<UnlockWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "unlock_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            unlockReq
        )

        // === Fast-path USER_PRESENT while process is alive (existing) ===
        userPresentReceiver = UnlockReceiver().also {
            val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
            registerReceiver(it, filter)
        }

        // === Daily DistanceSummaryWorker (runs ~23:55 local time) ===
        val distanceDelayMs = millisUntil(23, 55)
        val distanceReq = PeriodicWorkRequestBuilder<DistanceSummaryWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(distanceDelayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "distance_summary_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            distanceReq
        )

        // === Daily CallTotalsWorker (runs ~23:56 local time) ===
        val callDelayMs = millisUntil(23, 56)
        val callReq = PeriodicWorkRequestBuilder<CallTotalsWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(callDelayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "call_totals_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            callReq
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        userPresentReceiver?.let { unregisterReceiver(it) }
        userPresentReceiver = null
    }

    /** Compute ms from "now" until the next occurrence of [hour:minute] local time. */
    private fun millisUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}