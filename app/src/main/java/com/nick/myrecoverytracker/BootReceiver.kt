// BootReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "onReceive called with action: ${intent?.action}")

        val action = intent?.action ?: return
        if (
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        Log.d(TAG, "Valid action received")

        // WorkManager is already initialised by WorkManagerInitializer before
        // this receiver fires. Do NOT call WorkManager.initialize() manually —
        // it throws IllegalStateException ("WorkManager is already initialized").

        Log.d(TAG, "Scheduling workers")

        WorkManager.getInstance(context).pruneWork()
        WorkScheduler.registerAllWork(context)
        WorkScheduler.scheduleServiceHealthCheck(context)

        // BOOT_COMPLETED is an OS-allowed exemption for startForegroundService()
        ServiceStarter.startAllIfAllowed(context)

        Log.d(TAG, "Workers scheduled and services started")
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
