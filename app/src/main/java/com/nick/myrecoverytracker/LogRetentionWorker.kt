// app/src/main/java/com/nick/myrecoverytracker/LogRetentionWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
            File(dir, "daily_unlocks.csv"),
            File(dir, "daily_notification_engagement.csv"),
            File(dir, "daily_notification_latency.csv"),
            File(dir, "daily_late_night_screen_usage.csv")
        ).forEach { CsvUtils.rotateByDate(it, keepDays = 400) }

        // Upload / receipts logs (timestamp prefix): keep 90 days
        listOf(
            File(dir, "redcap_upload_log.csv"),
            File(dir, "redcap_receipts.csv"),
            File(dir, "health_snapshot.csv")
        ).forEach { CsvUtils.rotateByTimestampPrefix(it, keepDays = 90) }

        // Write/refresh retention audit row
        try {
            writeRetentionRow(dir, filesDeleted = 0, bytesFreed = 0)
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to append retention row", t)
        }

        return Result.success()
    }

    private fun writeRetentionRow(base: File, filesDeleted: Int, bytesFreed: Long) {
        val f = File(base, "log_retention.csv")
        if (!f.exists()) {
            f.writeText("date,files_deleted,bytes_freed,status\n")
        }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        // de-dupe today: rewrite without any existing today row, then append one fresh row
        val lines = f.readLines().toMutableList()
        val header = lines.firstOrNull() ?: "date,files_deleted,bytes_freed,status"
        val body = lines.drop(1).filterNot { it.startsWith("$today,") }
        val newRow = "$today,$filesDeleted,$bytesFreed,OK"
        f.writeText((sequenceOf(header) + body.asSequence() + sequenceOf(newRow)).joinToString("\n") + "\n")
    }

    companion object {
        private const val TAG = "LogRetentionWorker"
    }
}