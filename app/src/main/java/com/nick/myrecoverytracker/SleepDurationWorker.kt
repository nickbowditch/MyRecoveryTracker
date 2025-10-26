package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Placeholder implementation so the project compiles and the periodic is schedulable.
 * TODO: Replace the body with your real sleep-duration aggregation logic.
 */
class SleepDurationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // No-op for now; succeed so the periodic schedule stays healthy.
        Result.success()
    }
}