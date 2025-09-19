package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RingerChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val a = intent.action ?: return
        when (a) {
            "com.nick.myrecoverytracker.TEST_RINGER_LOG" -> {
                val m = intent.getIntExtra("mode", -1)
                if (m != -1) write(context, m)
            }
            "android.media.RINGER_MODE_CHANGED",
            "android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION",
            "android.media.EXTERNAL_RINGER_MODE_CHANGED_ACTION" -> {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                write(context, am.ringerMode)
            }
        }
    }

    private fun write(ctx: Context, mode: Int) {
        val name = when (mode) {
            AudioManager.RINGER_MODE_NORMAL -> "normal"
            AudioManager.RINGER_MODE_VIBRATE -> "vibrate"
            AudioManager.RINGER_MODE_SILENT -> "silent"
            else -> "unknown"
        }
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        try {
            val f = File(ctx.filesDir, "ringer_log.csv")
            f.appendText("$ts,$name\n")
            Log.i(TAG, "ringer: $ts,$name")
        } catch (t: Throwable) {
            Log.e(TAG, "write failed", t)
        }
    }

    companion object {
        private const val TAG = "RingerChangeReceiver"
    }
}