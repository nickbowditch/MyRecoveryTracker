// RedcapDebugReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RedcapDebugReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.i(TAG, "🔵 onReceive() action=${intent.action}")

            when (intent.action) {
                ACTION_RUN_REDCAP_UPLOAD,
                DEBUG_FORCE_UPLOAD,
                ACTION_RUN_REDCAP_DEBUG -> {
                    Log.i(TAG, "🔵 Triggering manual REDCap diag")
                    CoroutineScope(Dispatchers.IO).launch {
                        RedcapDiag.log(context)
                    }
                }

                else -> {
                    Log.w(TAG, "⚠️ Unknown action: ${intent.action}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception in onReceive: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "RedcapDebugReceiver"
        private const val ACTION_RUN_REDCAP_UPLOAD = "com.nick.myrecoverytracker.ACTION_RUN_REDCAP_UPLOAD"
        private const val DEBUG_FORCE_UPLOAD = "com.nick.myrecoverytracker.DEBUG_FORCE_UPLOAD"
        private const val ACTION_RUN_REDCAP_DEBUG = "com.nick.myrecoverytracker.ACTION_RUN_REDCAP_DEBUG"
    }
}