// HealthSnapshotWorker.kt
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
            val dataDir = StorageHelper.getDataDir(ctx)

            val unlockRaw = File(dataDir, "unlock_log.csv")
            val uploadLog = File(dataDir, "redcap_upload_log.csv")
            val out = File(dataDir, "daily_health.csv")

            val rawRows = if (unlockRaw.exists()) unlockRaw.readLines().count { it.isNotBlank() && !it.startsWith("ts,") } else 0
            val lastUpload = if (uploadLog.exists()) uploadLog.readLines().lastOrNull { it.contains("daily_metrics.csv") }?.split(',')?.getOrNull(2) ?: "none" else "none"

            if (!out.exists()) out.writeText("ts,unlocks_raw_rows,last_upload_status\n")
            val line = "${tsFmt.format(System.currentTimeMillis())},$rawRows,$lastUpload\n"
            out.appendText(line)

            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}