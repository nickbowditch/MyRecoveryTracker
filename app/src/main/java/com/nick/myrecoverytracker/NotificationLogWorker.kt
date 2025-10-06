// app/src/main/java/com/nick/myrecoverytracker/NotificationLogWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Trims notification_log.csv to the last 50k lines and preserves whatever header exists.
 * Does NOT rewrite the header (supports golden schema and legacy variants).
 */
class NotificationLogWorker(appContext: Context, params: WorkerParameters)
    : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val f = File(applicationContext.filesDir, "notification_log.csv")
            if (!f.exists()) return@withContext Result.success()

            val lines = f.readLines()
            if (lines.isEmpty()) return@withContext Result.success()

            val cap = 50_000
            val trimmed = if (lines.size > cap) {
                listOf(lines.first()) + lines.takeLast(cap - 1)
            } else {
                lines
            }

            f.writeText(trimmed.joinToString("\n") + "\n")
            Log.i(TAG, "notification_log trimmed: lines=${trimmed.size}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "NotificationLogWorker failed", t)
            Result.retry()
        }
    }

    companion object { private const val TAG = "NotificationLogWorker" }
}