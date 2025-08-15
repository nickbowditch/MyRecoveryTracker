package com.nick.myrecoverytracker

import android.content.Context
import android.provider.Telephony
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

/**
 * Counts SMS sent *today* and writes/updates:
 *   files/daily_messages_sent.csv
 * Format:
 *   date,count
 *   2025-08-15,12
 *
 * NOTE: Requires android.permission.READ_SMS (manifest).
 */
class MessagesSentWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val day = dayKey()
        val start = startOfDayMillis()
        val uri = Telephony.Sms.Sent.CONTENT_URI

        try {
            var count = 0
            ctx.contentResolver.query(
                uri,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.DATE),
                "${Telephony.Sms.DATE} >= ?",
                arrayOf(start.toString()),
                null
            )?.use { c -> count = c.count }

            Log.i(TAG, "SMS sent today = $count")
            writeDayCount(File(ctx.filesDir, "daily_messages_sent.csv"), day, count)
            Result.success()
        } catch (se: SecurityException) {
            Log.w(TAG, "READ_SMS permission missing; writing 0", se)
            writeDayCount(File(ctx.filesDir, "daily_messages_sent.csv"), day, 0)
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "MessagesSentWorker failed", t)
            Result.retry()
        }
    }

    private fun writeDayCount(file: File, day: String, count: Int) {
        val lines = if (file.exists()) file.readLines()
            .filterNot { it.startsWith("$day,") }
            .toMutableList()
        else mutableListOf("date,count")
        lines += "$day,$count"
        file.writeText(lines.joinToString("\n") + "\n")
    }

    private fun dayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun startOfDayMillis(): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    companion object { private const val TAG = "MessagesSentWorker" }
}