// app/src/main/java/com/nick/myrecoverytracker/HeartbeatWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class HeartbeatWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork() = withContext(Dispatchers.IO) {
        try {
            val ts = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(System.currentTimeMillis())
            val f = File(applicationContext.filesDir, "heartbeat.csv")
            if (!f.exists()) f.writeText("ts\n")
            f.appendText("$ts\n")
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE = "HeartbeatEveryMinute"
        fun ensure(context: Context) {
            val req = PeriodicWorkRequestBuilder<HeartbeatWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE, ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}