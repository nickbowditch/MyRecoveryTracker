// app/src/main/java/com/nick/myrecoverytracker/DropoutReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class DropoutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ForegroundUnlockService::class.java)
            )
        } catch (_: Throwable) { }
        Log.i("DropoutReceiver", "kickstarted FG service: ${intent.action}")
    }
}