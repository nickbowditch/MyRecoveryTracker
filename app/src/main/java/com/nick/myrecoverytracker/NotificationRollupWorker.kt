// app/src/main/java/com/nick/myrecoverytracker/NotificationRollupWorker.kt
package com.nick.myrecoverytracker

import android.util.Log
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationRollupWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        Log.i(TAG, "NotificationRollupWorker deprecated: no-op (NotificationEngagementWorker is canonical)")
        return Result.success()
    }

    companion object {
        private const val TAG = "NotificationRollupWorker"
    }
}