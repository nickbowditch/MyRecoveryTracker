package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UpgradeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i("UpgradeReceiver", "Received action: $action")

        val app = context.applicationContext

        // Start background services again if permissions allow
        ServiceStarter.startAllIfAllowed(app)

        try {
            if (DirectBoot.canAccessFiles(app)) {
                Log.i("UpgradeReceiver", "DirectBoot unlocked, running Reschedule.runNow()")
                Reschedule.runNow(app)
            } else {
                Log.i("UpgradeReceiver", "DirectBoot locked, marking Reschedule needed")
                Reschedule.markNeeded(app)
            }
        } catch (t: Throwable) {
            Log.e("UpgradeReceiver", "Error handling upgrade broadcast", t)
        }
    }
}