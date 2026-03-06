// app/src/main/java/com/nick/myrecoverytracker/DistanceDebugReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager

class DistanceDebugReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_RECALC_DISTANCE_TODAY -> {
                // Kick DistanceSummaryWorker instead of calling dead DistanceRepair
                val pending = goAsync()
                try {
                    val work = OneTimeWorkRequestBuilder<DistanceSummaryWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .addTag("DistanceSummaryDebugKick")
                        .build()
                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "debug-DistanceSummaryKick",
                        androidx.work.ExistingWorkPolicy.REPLACE,
                        work
                    )
                    Log.i(TAG, "Kicked DistanceSummaryWorker for today's distance recalc.")
                } catch (t: Throwable) {
                    Log.e(TAG, "Distance recalc kick failed", t)
                } finally {
                    pending.finish()
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