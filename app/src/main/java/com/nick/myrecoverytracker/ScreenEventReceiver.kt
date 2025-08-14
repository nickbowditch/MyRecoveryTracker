package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Logs screen-off events for debugging or metrics.
 * Not registered in manifest — dynamically registered in MainApplication.
 */
class ScreenEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_SCREEN_OFF == intent.action) {
            Log.i("ScreenEventReceiver", "📴 Screen turned OFF")
        }
    }
}