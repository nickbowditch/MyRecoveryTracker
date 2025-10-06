// app/src/main/java/com/nick/myrecoverytracker/ForegroundSleepService.kt
package com.nick.myrecoverytracker

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ForegroundSleepService : Service() {

    override fun onCreate() {
        super.onCreate()
        // Foreground notification removed — nothing to show here
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}