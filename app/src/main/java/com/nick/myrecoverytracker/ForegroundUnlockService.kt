package com.nick.myrecoverytracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ForegroundUnlockService : Service() {

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notif = buildNotification()
        startForeground(NOTIF_ID, notif)
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: android.content.Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "MyRecovery Tracker (Foreground)",
                    NotificationManager.IMPORTANCE_HIGH
                )
                nm.createNotificationChannel(ch)
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MyRecoveryAssistant")
            .setContentText("University Study Running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "mrt_fg_channel_v4"
        private const val NOTIF_ID = 1001
    }
}