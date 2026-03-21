// NotificationValidationWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NotificationValidationWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    companion object {
        private const val TAG = "NotificationValidation"
        private const val HEARTBEAT_FILE = "notification_validation_heartbeat.csv"
        private const val HEARTBEAT_HEADER = "ts,message\n"
    }

    // ── Heartbeat helper ─────────────────────────────────────────────────────
    private fun appendHeartbeat(dir: File, message: String) {
        val hbFile = File(dir, HEARTBEAT_FILE)
        try {
            if (!hbFile.exists()) {
                FileOutputStream(hbFile, false).use { it.write(HEARTBEAT_HEADER.toByteArray()) }
            }
            val ts = System.currentTimeMillis()
            FileOutputStream(hbFile, true).use { it.write("$ts,$message\n".toByteArray()) }
        } catch (t: Throwable) {
            Log.e(TAG, "appendHeartbeat failed: $message", t)
        }
    }

    // ── doWork ───────────────────────────────────────────────────────────────
    override fun doWork(): Result {
        val dir = StorageHelper.getDataDir(applicationContext)

        appendHeartbeat(dir, "START")

        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }.format(Date())

            appendHeartbeat(dir, "date=$today")

            // ── Engagement file ───────────────────────────────────────────
            val eng = File(dir, "daily_notification_engagement.csv")
            val engExists = eng.exists()
            appendHeartbeat(dir, "engagement_exists=$engExists")

            val engRow = if (engExists) {
                eng.readLines().firstOrNull { it.startsWith(today) }
            } else null

            appendHeartbeat(dir, "engagement_today=${engRow ?: "(none)"}")
            Log.i(TAG, "engagement_today=${engRow ?: "(none)"}")

            // ── Latency file ──────────────────────────────────────────────
            val lat = File(dir, "daily_notification_latency.csv")
            val latExists = lat.exists()
            appendHeartbeat(dir, "latency_exists=$latExists")

            val latRow = if (latExists) {
                lat.readLines().firstOrNull { it.startsWith(today) }
            } else null

            appendHeartbeat(dir, "latency_today=${latRow ?: "(none)"}")
            Log.i(TAG, "latency_today=${latRow ?: "(none)"}")

            // ── Done ──────────────────────────────────────────────────────
            appendHeartbeat(dir, "END success")
            Result.success()

        } catch (t: Throwable) {
            Log.e(TAG, "validation failed", t)
            appendHeartbeat(dir, "END failure=${t.message}")
            Result.failure()
        }
    }
}
