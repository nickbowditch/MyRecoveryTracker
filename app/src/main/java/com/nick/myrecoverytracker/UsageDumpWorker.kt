// app/src/main/java/com/nick/myrecoverytracker/UsageDumpWorker.kt
package com.nick.myrecoverytracker

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UsageDumpWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val begin = cal.timeInMillis

        val mgr = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = mgr.queryEvents(begin, now)
        val lastResume = HashMap<String, Long>()
        val total = HashMap<String, Long>()

        val e = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            val pkg = e.packageName ?: continue
            when (e.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    lastResume[pkg] = e.timeStamp
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    val start = lastResume.remove(pkg) ?: continue
                    if (e.timeStamp >= start) {
                        total[pkg] = (total[pkg] ?: 0L) + (e.timeStamp - start)
                    }
                }
            }
        }

        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(begin))
        val out = File(applicationContext.filesDir, "usage_by_pkg_daily.csv")
        out.parentFile?.mkdirs()
        val rows = buildString {
            total.entries.sortedByDescending { it.value }.forEach { (pkg, ms) ->
                val mins = ms.toDouble() / 1000.0 / 60.0
                append(day).append(',').append(pkg).append(',')
                    .append(String.format(Locale.US, "%.1f", mins)).append('\n')
            }
        }
        out.appendText(rows)
        return Result.success()
    }
}