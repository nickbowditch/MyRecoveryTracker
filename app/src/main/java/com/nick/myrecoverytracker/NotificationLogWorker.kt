package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Ensures header exists and trims notification_log.csv to last 50k lines.
 * Header: timestamp,pkg,title,text,event,reason,latency_sec
 * (Older rows without header are left as-is; workers handle both.)
 */
class NotificationLogWorker(appContext: Context, params: WorkerParameters)
    : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val f = File(applicationContext.filesDir, "notification_log.csv")
            if (!f.exists()) return@withContext Result.success()

            // Ensure header if file starts with a data row
            val lines = f.readLines()
            val header = "timestamp,pkg,title,text,event,reason,latency_sec"
            val hasHeader = lines.firstOrNull()?.startsWith("timestamp,") == true

            val normalized = ArrayList<String>(lines.size + 1)
            if (!hasHeader) normalized.add(header) else normalized.add(lines.first())
            normalized.addAll(if (hasHeader) lines.drop(1) else lines)

            // Cap file
            val cap = 50_000
            val trimmed = if (normalized.size > cap) {
                val keep = normalized.take(1) + normalized.takeLast(cap - 1)
                keep
            } else normalized

            f.writeText(trimmed.joinToString("\n") + "\n")
            Log.i(TAG, "notification_log normalized: lines=${trimmed.size}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "NotificationLogWorker failed", t)
            Result.retry()
        }
    }

    companion object { private const val TAG = "NotificationLogWorker" }
}