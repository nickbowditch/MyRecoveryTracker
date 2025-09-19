// app/src/main/java/com/nick/myrecoverytracker/ForegroundWork.kt
package com.nick.myrecoverytracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo

object ForegroundWork {

    private const val CH_ID = "mrt_wm_channel"
    private const val DEFAULT_ID = 2002

    fun info(
        ctx: Context,
        title: String = "MyRecoveryAssistant",
        text: String = "Working…",
        id: Int = DEFAULT_ID
    ): ForegroundInfo {
        ensureChannel(ctx)

        val notif = NotificationCompat.Builder(ctx, CH_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        val type = if (Build.VERSION.SDK_INT >= 29)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0

        return if (Build.VERSION.SDK_INT >= 31) {
            ForegroundInfo(id, notif, type)
        } else {
            ForegroundInfo(id, notif)
        }
    }

    fun createForegroundInfo(
        ctx: Context,
        title: String = "MyRecoveryAssistant",
        text: String = "Working…",
        id: Int = DEFAULT_ID
    ): ForegroundInfo = info(ctx, title, text, id)

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
}