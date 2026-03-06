package com.nick.myrecoverytracker

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
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
        
        if (!isRunning) {
            Log.w("ServiceHealthCheck", "Service is dead - restarting")
            val intent = Intent(applicationContext, ForegroundUnlockService::class.java)
            ContextCompat.startForegroundService(applicationContext, intent)
        }
        
        logHealthCheck(now, isRunning)
        return Result.success()
    }

    private fun isServiceRunning(serviceName: String): Boolean {
        val manager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceName }
    }

    private fun logHealthCheck(timestamp: String, wasRunning: Boolean) {
        try {
            val file = File(applicationContext.filesDir, "service_health_check.csv")
            val header = "ts,was_running,action_taken\n"
            
            if (!file.exists()) {
                file.writeText(header)
            }
            
            val action = if (wasRunning) "none" else "restarted"
            file.appendText("$timestamp,$wasRunning,$action\n")
        } catch (e: Exception) {
            Log.e("ServiceHealthCheck", "Failed to log health check", e)
        }
    }
}
