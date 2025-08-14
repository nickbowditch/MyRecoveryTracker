package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DropoutReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            // System after app update/install
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                appendEvent(context, "APP_UPDATED")
            }

            // System after boot
            Intent.ACTION_BOOT_COMPLETED -> {
                appendEvent(context, "BOOT_COMPLETED")
            }

            // Dev-only trigger to verify wiring end-to-end
            ACTION_TEST_DROPOUT -> {
                appendEvent(context, "TEST_DROPOUT")
            }

            else -> {
                Log.w(TAG, "DropoutReceiver: unhandled action: $action")
            }
        }
    }

    private fun appendEvent(context: Context, event: String) {
        try {
            val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val f = File(context.filesDir, "event_log.csv")
            f.appendText("$ts,$event\n")
            Log.i(TAG, "📘 event_log.csv += $ts,$event")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to write event_log.csv", t)
        }
    }

    companion object {
        private const val TAG = "DropoutReceiver"
        const val ACTION_TEST_DROPOUT = "com.nick.myrecoverytracker.TEST_DROPOUT"
    }
}