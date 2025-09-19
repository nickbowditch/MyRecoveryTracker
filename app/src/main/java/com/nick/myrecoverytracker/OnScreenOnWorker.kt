// app/src/main/java/com/nick/myrecoverytracker/OnScreenOnWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnScreenOnWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // no-op placeholder to satisfy references; extend later as needed
        Result.success()
    }
}