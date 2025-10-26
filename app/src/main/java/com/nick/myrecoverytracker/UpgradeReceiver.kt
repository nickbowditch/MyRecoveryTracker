package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UpgradeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i("UpgradeReceiver", "onReceive $action")

        val app = context.applicationContext
        if (DirectBoot.canAccessFiles(app)) {
            Reschedule.runNow(app)
        } else {
            Reschedule.markNeeded(app)
        }
    }
}