// app/src/main/java/com/nick/myrecoverytracker/NotificationValidationWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NotificationValidationWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        return try {
            val dir = applicationContext.filesDir
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }.format(Date())

            val eng = File(dir, "daily_notification_engagement.csv")
            val lat = File(dir, "daily_notification_latency.csv")

            val engRow = if (eng.exists()) eng.readLines().firstOrNull { it.startsWith(today) } else null
            val latRow = if (lat.exists()) lat.readLines().firstOrNull { it.startsWith(today) } else null

            Log.i("NotificationValidation", "engagement_today=${engRow ?: "(none)"}")
            Log.i("NotificationValidation", "latency_today=${latRow ?: "(none)"}")

            Result.success()
        } catch (t: Throwable) {
            Log.e("NotificationValidation", "validation failed", t)
            Result.failure()
        }
    }
}