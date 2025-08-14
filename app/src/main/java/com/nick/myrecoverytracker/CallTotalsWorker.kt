package com.nick.myrecoverytracker

import android.content.Context
import android.provider.CallLog
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class CallTotalsWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ctx = applicationContext
            val (start, end, dayStr) = dayBounds()

            if (!has(ctx, android.Manifest.permission.READ_CALL_LOG)) {
                MetricsStoreCallTotals.writeDaily(ctx, dayStr, 0, 0)
                Log.w(TAG, "READ_CALL_LOG missing; wrote zeros for $dayStr")
                return@withContext Result.success()
            }

            val proj = arrayOf(CallLog.Calls.TYPE, CallLog.Calls.DATE)
            val sel = "${CallLog.Calls.DATE}>=? AND ${CallLog.Calls.DATE}<?"
            val args = arrayOf(start.toString(), end.toString())

            var inCount = 0
            var outCount = 0
            ctx.contentResolver.query(CallLog.Calls.CONTENT_URI, proj, sel, args, null)?.use { c ->
                val iType = c.getColumnIndex(CallLog.Calls.TYPE)
                while (c.moveToNext()) when (c.getInt(iType)) {
                    CallLog.Calls.INCOMING_TYPE -> inCount++
                    CallLog.Calls.OUTGOING_TYPE -> outCount++
                }
            }

            MetricsStoreCallTotals.writeDaily(ctx, dayStr, outCount, inCount)
            Log.i(TAG, "CallTotals($dayStr) out=$outCount in=$inCount")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "CallTotalsWorker failed", t)
            Result.retry()
        }
    }

    private fun has(ctx: Context, perm: String) =
        ContextCompat.checkSelfPermission(ctx, perm) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

    private fun dayBounds(): Triple<Long, Long, String> {
        val tz = TimeZone.getDefault()
        val cal = Calendar.getInstance(tz).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = max(start + TimeUnit.DAYS.toMillis(1), start)
        val dayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }.format(Date(start))
        return Triple(start, end, dayStr)
    }

    companion object { private const val TAG = "CallTotalsWorker" }
}