// app/src/main/java/com/nick/myrecoverytracker/DistanceDebugReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.concurrent.thread

class DistanceDebugReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_RECALC_DISTANCE_TODAY -> {
                // Do the work off the main thread and honor goAsync() to avoid ANR
                val pending = goAsync()
                thread(name = "mrt-distance-repair") {
                    try {
                        DistanceRepair.recalcToday(context)
                        Log.i(TAG, "Recalculated today’s distance.")
                    } catch (t: Throwable) {
                        Log.e(TAG, "Distance recalc failed", t)
                    } finally {
                        pending.finish()
                    }
                }
            }
            else -> {
                Log.d(TAG, "Ignored action: ${intent.action}")
            }
        }
    }

    companion object {
        const val TAG = "DistanceDebugReceiver"
        const val ACTION_RECALC_DISTANCE_TODAY =
            "com.nick.myrecoverytracker.ACTION_RECALC_DISTANCE_TODAY"
    }
}