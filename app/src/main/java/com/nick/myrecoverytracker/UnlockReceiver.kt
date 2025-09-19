// app/src/main/java/com/nick/myrecoverytracker/UnlockReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class UnlockReceiver : BroadcastReceiver() {
    // Explicitly use device default timezone (same as system default) to avoid surprises
    private val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val line = "${ts.format(System.currentTimeMillis())},UNLOCK\n"
            runCatching {
                context.openFileOutput("unlock_log.csv", Context.MODE_APPEND).use {
                    it.write(line.toByteArray())
                }
            }
            Log.i("UnlockReceiver", "USER_PRESENT → UNLOCK row appended")
        }
    }
}