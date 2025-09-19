// app/src/main/java/com/nick/myrecoverytracker/AppSwitchingWorker.kt
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.floor

/**
 * Output CSV: files/daily_app_switching.csv
 * Header: date,switch_count,median_session_seconds
 */
class AppSwitchingWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext

        if (!UsagePermissionHelper.isGranted(ctx)) {
            writeRow(today(), 0, 0.0)
            return@withContext Result.success()
        }

        val (start, end) = todayRange()
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = usm.queryEvents(start, end)

        var currentPkg: String? = null
        var currentStart: Long = start
        var switchCount = 0
        val sessions = ArrayList<Long>()

        val ev = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(ev)
            val pkg = ev.packageName ?: continue

            when (ev.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (currentPkg != null && currentStart > 0) {
                        val dur = ev.timeStamp - currentStart
                        if (dur > 0) sessions.add(dur)
                    }
                    if (currentPkg != null && currentPkg != pkg) switchCount += 1
                    currentPkg = pkg
                    currentStart = ev.timeStamp
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (currentPkg == pkg && currentStart > 0) {
                        val dur = ev.timeStamp - currentStart
                        if (dur > 0) sessions.add(dur)
                        currentPkg = null
                        currentStart = 0L
                    }
                }
            }
        }

        if (currentPkg != null && currentStart > 0) {
            val dur = end - currentStart
            if (dur > 0) sessions.add(dur)
        }

        val medianSec = if (sessions.isEmpty()) 0.0 else medianSeconds(sessions)
        val day = today()
        writeRow(day, switchCount, medianSec)
        Log.i(TAG, "AppSwitching $day: switch_count=$switchCount median_session_seconds=$medianSec (nSessions=${sessions.size})")
        Result.success()
    }

    private fun medianSeconds(durationsMs: List<Long>): Double {
        if (durationsMs.isEmpty()) return 0.0
        val sorted = durationsMs.sorted()
        val n = sorted.size
        return if (n % 2 == 1) {
            sorted[n / 2] / 1000.0
        } else {
            (sorted[n / 2 - 1] / 1000.0 + sorted[n / 2] / 1000.0) / 2.0
        }
    }

    private fun writeRow(day: String, switchCount: Int, medianSeconds: Double) {
        val out = File(applicationContext.filesDir, "daily_app_switching.csv")
        val header = "date,switch_count,median_session_seconds"
        val lines: MutableList<String> = if (out.exists()) out.readLines().toMutableList() else mutableListOf(header)
        val filtered = lines.filterNot { it.startsWith("$day,") }.toMutableList()
        val medianRounded = floor(medianSeconds * 10.0) / 10.0
        filtered.add("$day,$switchCount,$medianRounded")
        out.writeText(filtered.joinToString("\n") + "\n")
    }

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()
        return start to end
    }

    companion object {
        private const val TAG = "AppSwitchingWorker"
    }
}