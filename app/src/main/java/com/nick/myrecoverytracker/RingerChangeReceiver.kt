// RingerChangeReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class RingerChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "🔵 onReceive() action=${intent.action}")

        when (intent.action) {
            AudioManager.RINGER_MODE_CHANGED_ACTION,
            "android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION",
            "android.media.EXTERNAL_RINGER_MODE_CHANGED_ACTION",
            TEST_RINGER_LOG -> {
                Log.i(TAG, "🔵 Ringer mode changed detected")
                logRingerMode(context)
            }

            else -> {
                Log.w(TAG, "⚠️ Unknown action: ${intent.action}")
            }
        }
    }

    private fun logRingerMode(context: Context) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val mode = am.ringerMode

            val modeStr = when (mode) {
                AudioManager.RINGER_MODE_SILENT -> "SILENT"
                AudioManager.RINGER_MODE_VIBRATE -> "VIBRATE"
                AudioManager.RINGER_MODE_NORMAL -> "NORMAL"
                else -> "UNKNOWN_$mode"
            }

            val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(System.currentTimeMillis())
            Log.i(TAG, "📢 Current ringer mode: $modeStr")

            val dir = StorageHelper.getDataDir(context)
            if (!dir.exists()) dir.mkdirs()

            val f = File(dir, "ringer_log.csv")
            if (!f.exists()) {
                f.writeText("ts,mode\n")
                Log.d(TAG, "Created ringer_log.csv with header")
            }

            f.appendText("$ts,$modeStr\n")
            Log.i(TAG, "✅ Logged ringer mode: $ts -> $modeStr")
        } catch (t: Throwable) {
            Log.e(TAG, "❌ Failed to log ringer mode", t)
        }
    }

    companion object {
        private const val TAG = "RingerChangeReceiver"
        private const val TEST_RINGER_LOG = "com.nick.myrecoverytracker.TEST_RINGER_LOG"
    }
}