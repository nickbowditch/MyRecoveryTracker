// ServiceRestartReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receives the exact AlarmManager callback scheduled by ServiceHealthCheckWorker.
 *
 * An exact alarm callback is one of the documented Android 12+ OS exemptions
 * that grants a brief foreground-service-start window (mAllowStartForeground = true).
 * This receiver therefore CAN safely call startForegroundService() where a
 * background Worker cannot.
 */
class ServiceRestartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_RESTART_SERVICE) return

        Log.i(TAG, "ServiceRestartReceiver fired — starting ForegroundUnlockService")

        try {
            ForegroundUnlockService.start(context)
            Log.i(TAG, "ForegroundUnlockService start issued successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ForegroundUnlockService: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    companion object {
        const val ACTION_RESTART_SERVICE = "com.nick.myrecoverytracker.ACTION_RESTART_FOREGROUND_SERVICE"
        private const val TAG = "ServiceRestartReceiver"
    }
}
