// app/src/main/java/com/nick/myrecoverytracker/ScreenEventReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale

class ScreenEventReceiver : BroadcastReceiver() {
    private val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override fun onReceive(context: Context, intent: Intent) {
        val now = ts.format(System.currentTimeMillis())
        val line = when (intent.action) {
            Intent.ACTION_SCREEN_ON  -> "$now,ON\n"
            Intent.ACTION_SCREEN_OFF -> "$now,OFF\n"
            Intent.ACTION_USER_PRESENT -> "$now,UNLOCK\n"
            else -> null
        }
        if (line != null) {
            runCatching {
                context.openFileOutput("screen_log.csv", Context.MODE_APPEND).use {
                    it.write(line.toByteArray())
                }
            }
            Log.i("ScreenEventReceiver", "screen event → ${intent.action}")
        }
    }
}