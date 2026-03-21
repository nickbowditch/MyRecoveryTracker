// ServiceHealthCheckWorker.kt
package com.nick.myrecoverytracker

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.time.Instant

class ServiceHealthCheckWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val now = Instant.now().toString()
        val isRunning = isServiceRunning(ForegroundUnlockService::class.java.name)

        Log.d("ServiceHealthCheck", "ForegroundUnlockService running: $isRunning")

        var actionTaken = "none"

        if (!isRunning) {
            Log.w("ServiceHealthCheck", "Service is dead - scheduling alarm restart")
            actionTaken = scheduleAlarmRestart()
        }

        // Always log regardless of restart outcome
        logHealthCheck(now, isRunning, actionTaken)

        return Result.success()
    }

    /**
     * On Android 12+, startForegroundService() from a background Worker context
     * is blocked (mAllowStartForeground = false).
     *
     * Exact AlarmManager callbacks ARE a documented OS exemption that grants
     * a brief foreground-start window. We schedule a zero-delay exact alarm
     * pointing to ServiceRestartReceiver, which then calls
     * ForegroundUnlockService.start() safely.
     *
     * On older APIs we can start directly.
     */
    private fun scheduleAlarmRestart(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                // Check we have exact alarm permission (granted by default for
                // apps targeting API < 32, requires SCHEDULE_EXACT_ALARM on 32+)
                if (!am.canScheduleExactAlarms()) {
                    Log.w("ServiceHealthCheck", "Cannot schedule exact alarms — trying inexact")
                    scheduleInexactAlarmRestart()
                    return "restart_inexact_alarm_scheduled"
                }

                val intent = Intent(applicationContext, ServiceRestartReceiver::class.java).apply {
                    action = ServiceRestartReceiver.ACTION_RESTART_SERVICE
                }
                val pi = PendingIntent.getBroadcast(
                    applicationContext,
                    REQUEST_CODE_RESTART,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                // Fire immediately (current time = fires at next alarm batch opportunity)
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1_000L, // 1 second from now
                    pi
                )
                Log.i("ServiceHealthCheck", "Exact alarm scheduled for ServiceRestartReceiver")
                "restart_alarm_scheduled"
            } else {
                // Pre-Android 12: direct start is allowed from background
                ForegroundUnlockService.start(applicationContext)
                Log.i("ServiceHealthCheck", "Direct service start issued (pre-API-31)")
                "restart_direct_issued"
            }
        } catch (e: Exception) {
            Log.e("ServiceHealthCheck", "Failed to schedule restart: ${e.javaClass.simpleName}: ${e.message}")
            "restart_failed"
        }
    }

    private fun scheduleInexactAlarmRestart() {
        val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, ServiceRestartReceiver::class.java).apply {
            action = ServiceRestartReceiver.ACTION_RESTART_SERVICE
        }
        val pi = PendingIntent.getBroadcast(
            applicationContext,
            REQUEST_CODE_RESTART,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5_000L, pi)
    }

    private fun isServiceRunning(serviceName: String): Boolean {
        val manager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceName }
    }

    private fun logHealthCheck(timestamp: String, wasRunning: Boolean, action: String) {
        try {
            val file = File(StorageHelper.getDataDir(applicationContext), "service_health_check.csv")
            if (!file.exists()) {
                file.writeText("ts,was_running,action_taken\n")
            }
            file.appendText("$timestamp,$wasRunning,$action\n")
        } catch (e: Exception) {
            Log.e("ServiceHealthCheck", "Failed to log health check", e)
        }
    }

    companion object {
        private const val REQUEST_CODE_RESTART = 9001
    }
}
