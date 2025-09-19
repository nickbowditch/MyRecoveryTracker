package com.nick.myrecoverytracker

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dumps raw usage events to CSV.
 *
 * Output: files/usage_events.csv
 * Columns: date,time,event_type,package
 *
 * - date: yyyy-MM-dd (local)
 * - time: HH:mm:ss (local)
 * - event_type: FOREGROUND | BACKGROUND
 * - package: package name
 */
class UsageEventsDumpWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!UsagePermissionHelper.isGranted(applicationContext)) {
                Log.w(TAG, "Usage access not granted — skipping dump")
                return@withContext Result.success()
            }

            val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                timeInMillis = endTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = cal.timeInMillis

            val events = usm.queryEvents(startTime, endTime)
            val e = UsageEvents.Event()

            val outFile = File(applicationContext.filesDir, "usage_events.csv")
            if (!outFile.exists()) {
                outFile.writeText("date,time,event_type,package\n")
            }

            val sb = StringBuilder()
            while (events.hasNextEvent()) {
                events.getNextEvent(e)
                val pkg = e.packageName ?: continue
                when (normalizeEventType(e.eventType)) {
                    NormalizedEventType.MOVE_TO_FOREGROUND -> {
                        sb.append(fmtDate(e.timeStamp)).append(',')
                            .append(fmtTime(e.timeStamp)).append(',')
                            .append("FOREGROUND,")
                            .append(pkg).append('\n')
                    }
                    NormalizedEventType.MOVE_TO_BACKGROUND -> {
                        sb.append(fmtDate(e.timeStamp)).append(',')
                            .append(fmtTime(e.timeStamp)).append(',')
                            .append("BACKGROUND,")
                            .append(pkg).append('\n')
                    }
                    else -> {}
                }
            }

            if (sb.isNotEmpty()) {
                outFile.appendText(sb.toString())
            }

            Log.i(TAG, "UsageEventsDumpWorker wrote events to ${outFile.name}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "UsageEventsDumpWorker failed", t)
            Result.retry()
        }
    }

    private fun fmtDate(ts: Long): String = dateFmt.format(Date(ts))
    private fun fmtTime(ts: Long): String = timeFmt.format(Date(ts))

    private fun normalizeEventType(eventType: Int): NormalizedEventType {
        if (eventType == EVENT_ACTIVITY_RESUMED) return NormalizedEventType.MOVE_TO_FOREGROUND
        if (eventType == EVENT_ACTIVITY_PAUSED) return NormalizedEventType.MOVE_TO_BACKGROUND

        @Suppress("DEPRECATION")
        return when (eventType) {
            UsageEvents.Event.MOVE_TO_FOREGROUND -> NormalizedEventType.MOVE_TO_FOREGROUND
            UsageEvents.Event.MOVE_TO_BACKGROUND -> NormalizedEventType.MOVE_TO_BACKGROUND
            else -> NormalizedEventType.IGNORED
        }
    }

    enum class NormalizedEventType { MOVE_TO_FOREGROUND, MOVE_TO_BACKGROUND, IGNORED }

    companion object {
        private const val TAG = "UsageEventsDumpWorker"
        private const val EVENT_ACTIVITY_RESUMED = 7
        private const val EVENT_ACTIVITY_PAUSED = 8
    }
}