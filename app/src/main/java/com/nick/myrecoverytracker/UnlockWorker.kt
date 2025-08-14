package com.nick.myrecoverytracker

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class UnlockWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val prefs by lazy {
        applicationContext.getSharedPreferences("unlock_prefs", Context.MODE_PRIVATE)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            val now = System.currentTimeMillis()

            val hasLastCheck = prefs.contains(KEY_LAST_USAGE_CHECK)
            val lastCheck = prefs.getLong(KEY_LAST_USAGE_CHECK, 0L)
            var lastUnlockAt = prefs.getLong(KEY_LAST_UNLOCK_AT, 0L)

            // First run? Initialize cursors to "now" and bail to avoid 24h backfill spam.
            if (!hasLastCheck) {
                prefs.edit()
                    .putLong(KEY_LAST_USAGE_CHECK, now)
                    .putLong(KEY_LAST_UNLOCK_AT, now)
                    .apply()
                return@withContext Result.success()
            }

            // Normal run: scan only recent delta, hard cap to 15 minutes.
            val windowStart = (now - TimeUnit.MINUTES.toMillis(15))
                .coerceAtMost(now)
                .coerceAtLeast(lastCheck)

            if (windowStart >= now) {
                // Nothing to scan; advance cursor and exit.
                prefs.edit().putLong(KEY_LAST_USAGE_CHECK, now).apply()
                return@withContext Result.success()
            }

            val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val events = usm.queryEvents(windowStart, now)
            val e = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(e)
                val isUnlock =
                    e.eventType == UsageEvents.Event.KEYGUARD_HIDDEN ||
                            e.eventType == UsageEvents.Event.SCREEN_INTERACTIVE

                if (isUnlock) {
                    val ts = e.timeStamp
                    if (ts - lastUnlockAt >= 5_000) {
                        MetricsStore.saveUnlock(applicationContext)
                        lastUnlockAt = ts
                    }
                }
            }

            prefs.edit()
                .putLong(KEY_LAST_USAGE_CHECK, now)
                .putLong(KEY_LAST_UNLOCK_AT, lastUnlockAt)
                .apply()

            Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val KEY_LAST_USAGE_CHECK = "last_usage_check"
        private const val KEY_LAST_UNLOCK_AT = "last_unlock_at"
    }
}