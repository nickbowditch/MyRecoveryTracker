package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

class MovementARReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!ActivityTransitionResult.hasResult(intent)) return
        val res = ActivityTransitionResult.extractResult(intent) ?: return
        for (e in res.transitionEvents) {
            when (e.activityType) {
                DetectedActivity.STILL -> MovementCapture.writeStill()
                DetectedActivity.WALKING,
                DetectedActivity.RUNNING,
                DetectedActivity.ON_FOOT,
                DetectedActivity.ON_BICYCLE,
                DetectedActivity.IN_VEHICLE -> MovementCapture.writeActive()
            }
        }
    }
}