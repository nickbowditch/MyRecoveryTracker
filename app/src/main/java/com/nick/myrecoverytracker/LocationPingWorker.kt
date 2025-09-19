// app/src/main/java/com/nick/myrecoverytracker/LocationPingWorker.kt
package com.nick.myrecoverytracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters

class LocationPingWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())
        // real work here if needed
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val ctx = applicationContext
        ensureChannel(ctx)

        val notif = NotificationCompat.Builder(ctx, CH_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MyRecoveryAssistant")
            .setContentText("Collecting study data")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        val type = if (Build.VERSION.SDK_INT >= 29)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0

        return if (Build.VERSION.SDK_INT >= 31) {
            ForegroundInfo(NOTIF_ID, notif, type)
        } else {
            ForegroundInfo(NOTIF_ID, notif)
        }
    }

    private fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CH_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CH_ID,
                    "MyRecovery Worker",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    companion object {
        private const val CH_ID = "mrt_wm_channel"
        private const val NOTIF_ID = 2001
    }
}