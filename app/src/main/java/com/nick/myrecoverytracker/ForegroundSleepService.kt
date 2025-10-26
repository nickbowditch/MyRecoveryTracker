// app/src/main/java/com/nick/myrecoverytracker/ForegroundSleepService.kt
package com.nick.myrecoverytracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForegroundSleepService : Service() {

    override fun onCreate() {
        super.onCreate()
        createChannel()
        // IMPORTANT: call startForeground immediately on create/start
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If system restarts the service, ensure we stay in foreground
        // (notification already shown in onCreate, but this is harmless if called again)
        if (!started) {
            startForeground(NOTIF_ID, buildNotification())
            started = true
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        started = false
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val importance =
                    if (BuildConfig.DEBUG) NotificationManager.IMPORTANCE_HIGH
                    else NotificationManager.IMPORTANCE_DEFAULT
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "MyRecovery Tracker (Sleep)",
                    importance
                )
                nm.createNotificationChannel(ch)
            }
        }
    }

    private fun buildNotification(): Notification {
        val priority =
            if (BuildConfig.DEBUG) NotificationCompat.PRIORITY_HIGH
            else NotificationCompat.PRIORITY_DEFAULT

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MyRecoveryAssistant")
            .setContentText("Sleep tracking service is active")
            .setSmallIcon(R.mipmap.ic_launcher)  // safe app icon
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(priority)

        if (Build.VERSION.SDK_INT >= 31) {
            builder.setForegroundServiceBehavior(
                NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            )
        }
        return builder.build()
    }

    companion object {
        private const val CHANNEL_ID = "mrt_sleep_fg_v1"
        private const val NOTIF_ID = 1002
        @Volatile private var started = false
    }
}