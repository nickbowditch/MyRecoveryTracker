package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "onReceive called with action: ${intent?.action}")

        val action = intent?.action ?: return
        if (
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        Log.d("BootReceiver", "Valid action received, initializing WorkManager")

        try {
            // Initialize WorkManager manually
            WorkManager.initialize(
                context,
                Configuration.Builder().build()
            )
        } catch (e: IllegalStateException) {
            // Already initialized
            Log.d("BootReceiver", "WorkManager already initialized")
        }

        Log.d("BootReceiver", "Scheduling workers")

        WorkManager.getInstance(context).pruneWork()
        WorkScheduler.registerAllWork(context)
        WorkScheduler.scheduleServiceHealthCheck(context)

        val startServiceWorker = OneTimeWorkRequestBuilder<StartServiceWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "boot-start-service",
                ExistingWorkPolicy.REPLACE,
                startServiceWorker
            )

        Log.d("BootReceiver", "Workers scheduled successfully")
    }
}