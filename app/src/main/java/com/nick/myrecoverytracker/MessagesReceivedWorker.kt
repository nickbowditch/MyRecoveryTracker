package com.nick.myrecoverytracker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MessagesReceivedWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val inboxUri = Uri.parse("content://sms/inbox")
        var count = 0
        try {
            ctx.contentResolver.query(
                inboxUri,
                arrayOf("_id", "date"),
                "date >= ?",
                arrayOf(startOfDay.toString()),
                null
            )?.use { c ->
                count = c.count
            }

            val out = File(ctx.filesDir, "daily_messages_received.csv")
            writeDailyCount(out, day, count)
            Log.i(TAG, "SMS received today = $count")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "MessagesReceivedWorker failed", t)
            Result.retry()
        }
    }

    private fun writeDailyCount(file: File, day: String, count: Int) {
        val header = "date,count"
        val lines = mutableListOf<String>()
        if (file.exists()) {
            lines += file.readLines().filterNot { it.startsWith("$day,") }.ifEmpty { listOf(header) }
            if (lines.firstOrNull() != header) lines.add(0, header)
        } else {
            lines += header
        }
        lines += "$day,$count"
        file.writeText(lines.joinToString("\n") + "\n")
    }

    companion object { private const val TAG = "MessagesReceivedWorker" }
}