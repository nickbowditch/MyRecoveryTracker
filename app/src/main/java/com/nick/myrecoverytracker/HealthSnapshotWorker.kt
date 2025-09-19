// app/src/main/java/com/nick/myrecoverytracker/HealthSnapshotWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class HealthSnapshotWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    private val ctx = applicationContext
    private val tsFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val filesDir = ctx.filesDir ?: return@withContext Result.success()

            val unlockRaw = File(filesDir, "unlock_log.csv")
            val unlockRoll = File(filesDir, "daily_unlocks.csv")
            val uploadLog = File(filesDir, "redcap_upload_log.csv")
            val out = File(filesDir, "daily_health.csv")

            val rawRows = if (unlockRaw.exists()) unlockRaw.readLines().count { it.isNotBlank() && !it.startsWith("ts,") } else 0
            val rollRows = if (unlockRoll.exists()) unlockRoll.readLines().count { it.isNotBlank() && !it.startsWith("date,") } else 0
            val lastUpload = if (uploadLog.exists()) uploadLog.readLines().lastOrNull { it.contains("daily_metrics.csv") }?.split(',')?.getOrNull(2) ?: "none" else "none"

            if (!out.exists()) out.writeText("ts,unlocks_raw_rows,unlocks_rollup_rows,last_upload_status\n")
            val line = "${tsFmt.format(System.currentTimeMillis())},$rawRows,$rollRows,$lastUpload\n"
            out.appendText(line)

            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}