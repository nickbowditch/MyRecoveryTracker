// app/src/main/java/com/nick/myrecoverytracker/LogRetentionWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class LogRetentionWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    override fun doWork(): Result {
        val dir = applicationContext.filesDir

        // Raw logs (timestamp prefix): keep 30 days
        listOf(
            File(dir, "unlock_log.csv"),
            File(dir, "screen_log.csv"),
            File(dir, "notification_log.csv")
        ).forEach { CsvUtils.rotateByTimestampPrefix(it, keepDays = 30) }

        // Rollups (date in first column): keep 400 days
        listOf(
            File(dir, "daily_sleep_summary.csv"),
            File(dir, "daily_sleep_duration.csv"),
            File(dir, "daily_sleep_time.csv"),
            File(dir, "daily_wake_time.csv"),
            File(dir, "daily_sleep_quality.csv"),
            File(dir, "daily_unlocks.csv")
        ).forEach { CsvUtils.rotateByDate(it, keepDays = 400) }

        // Upload / receipts logs (timestamp prefix): keep 90 days
        listOf(
            File(dir, "redcap_upload_log.csv"),
            File(dir, "redcap_receipts.csv"),
            File(dir, "health_snapshot.csv")
        ).forEach { CsvUtils.rotateByTimestampPrefix(it, keepDays = 90) }

        return Result.success()
    }
}