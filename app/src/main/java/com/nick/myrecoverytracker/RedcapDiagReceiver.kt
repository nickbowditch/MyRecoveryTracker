package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RedcapDiagReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("RedcapDiag", "Invoked via broadcast: ${intent.action}")
        try {
            RedcapDiag.log(context)
        } catch (t: Throwable) {
            Log.e("RedcapDiag", "Diag failed", t)
        }
    }
}