// UsageEventsDumpWorker.kt
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
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class UsageEventsDumpWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    private val zone = ZoneId.systemDefault()
    private val tsFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting UsageEventsDumpWorker")

            val dir = StorageHelper.getDataDir(applicationContext)
            if (!dir.exists()) dir.mkdirs()

            val today = LocalDate.now(zone).toString()
            val outFile = File(dir, "usage_events.csv")

            if (!outFile.exists()) {
                outFile.writeText("date,time,event_type,package\n")
                Log.d(TAG, "Created usage_events.csv with header")
            }

            val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE)
                    as UsageStatsManager

            val start = LocalDate.now(zone)
                .atStartOfDay(zone).toInstant().toEpochMilli()
            val end = System.currentTimeMillis()

            Log.i(TAG, "Querying usage events from $start to $end")

            val events = usm.queryEvents(start, end)
            val e = UsageEvents.Event()
            var eventCount = 0

            @Suppress("DEPRECATION")
            while (events.getNextEvent(e)) {
                try {
                    val pkg = e.packageName ?: "UNKNOWN"
                    val ts = tsFmt.format(e.timeStamp)
                    val date = ts.substring(0, 10)
                    val time = ts.substring(11)

                    @Suppress("DEPRECATION")
                    val eventType = when (e.eventType) {
                        UsageEvents.Event.ACTIVITY_RESUMED -> "ACTIVITY_RESUMED"
                        UsageEvents.Event.ACTIVITY_PAUSED -> "ACTIVITY_PAUSED"
                        UsageEvents.Event.ACTIVITY_STOPPED -> "ACTIVITY_STOPPED"
                        UsageEvents.Event.MOVE_TO_FOREGROUND -> "MOVE_TO_FOREGROUND"
                        UsageEvents.Event.MOVE_TO_BACKGROUND -> "MOVE_TO_BACKGROUND"
                        UsageEvents.Event.USER_INTERACTION -> "USER_INTERACTION"
                        UsageEvents.Event.CONFIGURATION_CHANGE -> "CONFIGURATION_CHANGE"
                        else -> "UNKNOWN_${e.eventType}"
                    }

                    outFile.appendText("$date,$time,$eventType,$pkg\n")
                    eventCount++

                    if (eventCount % 100 == 0) {
                        Log.d(TAG, "Processed $eventCount events so far")
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Error processing event", t)
                }
            }

            Log.i(TAG, "UsageEventsDumpWorker completed: wrote $eventCount events for $today")
            return@withContext Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "UsageEventsDumpWorker failed", t)
            return@withContext Result.retry()
        }
    }

    companion object {
        private const val TAG = "UsageEventsDumpWorker"
    }
}