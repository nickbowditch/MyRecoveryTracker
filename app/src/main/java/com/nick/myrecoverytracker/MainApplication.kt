// app/src/main/java/com/nick/myrecoverytracker/MainApplication.kt
package com.nick.myrecoverytracker

import android.app.Application
import android.util.Log
import androidx.work.*
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.e("APP_PROBE", "MainApplication.onCreate CALLED")

        if (!schedulerRan.compareAndSet(false, true)) {
            Log.w(TAG, "Scheduler already ran, skipping")
            return
        }

        Log.e("CATCHUP_PROBE", "MainApplication onCreate() scheduling + catchup starting")

        try {
            // Call centralized WorkScheduler
            WorkScheduler.registerAllWork(this)

            // Validate REDCap token
            try {
                val tokenValid = RedcapApiClient.validateToken(this)
                if (tokenValid) {
                    Log.i(TAG, "✅ REDCap token validated successfully")
                } else {
                    Log.w(TAG, "⚠️ REDCap token validation failed – check credentials")
                }
            } catch (e: Exception) {
                Log.e(TAG, "REDCap token validation threw exception", e)
            }

            // Test REDCap reachability
            try {
                val reachable = RedcapApiClient.testReachability(this)
                if (reachable) {
                    Log.i(TAG, "✅ REDCap server reachable")
                } else {
                    Log.w(TAG, "⚠️ REDCap server unreachable – check network or URL")
                }
            } catch (e: Exception) {
                Log.e(TAG, "REDCap reachability test threw exception", e)
            }

            schedulePeriodicWork()
            enqueueImmediateCatchUp()
            Log.e("CATCHUP_PROBE", "MainApplication onCreate() scheduling + catchup finished")
        } catch (t: Throwable) {
            Log.e(TAG, "Work scheduling failed", t)
        }
    }

    private fun schedulePeriodicWork() {
        val wm = WorkManager.getInstance(this)

        wm.enqueueUniquePeriodicWork(
            UNIQUE_DIST,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyAt(DistanceSummaryWorker::class.java, TAG_DIST, 3, 10)
        )

        wm.enqueueUniquePeriodicWork(
            UNIQUE_LNS,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyAt(LateNightScreenRollupWorker::class.java, TAG_LNS, 5, 5)
        )

        wm.enqueueUniquePeriodicWork(
            UNIQUE_NOTIF_LOG,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyAt(NotificationLogWorker::class.java, TAG_NOTIF_LOG, 3, 20)
        )

        wm.enqueueUniquePeriodicWork(
            UNIQUE_NOTIF_ENG,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyAt(NotificationEngagementWorker::class.java, TAG_NOTIF_ENG, 3, 47)
        )

        val usageEventsPeriodic = PeriodicWorkRequestBuilder<UsageEventsDumpWorker>(
            3, TimeUnit.HOURS
        ).addTag(TAG_USAGE_EVENTS).build()
        wm.enqueueUniquePeriodicWork(UNIQUE_USAGE_EVENTS, ExistingPeriodicWorkPolicy.KEEP, usageEventsPeriodic)

        wm.enqueueUniquePeriodicWork(
            UNIQUE_USAGE_DAILY,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyAt(UsageEventsDailyWorker::class.java, TAG_USAGE_DAILY, 4, 35)
        )

        wm.enqueueUniquePeriodicWork(
            UNIQUE_MOVE_INTENSITY,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyAt(MovementIntensityDailyWorker::class.java, TAG_MOVE_INTENSITY, 4, 45)
        )

        wm.enqueueUniquePeriodicWork(
            UNIQUE_DAILY_SUMMARY,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyAt(DailySummaryWorker::class.java, TAG_DAILY_SUMMARY, 3, 0)
        )
    }

    private fun enqueueImmediateCatchUp() {
        val wm = WorkManager.getInstance(this)

        wm.enqueueUniqueWork(
            UNIQUE_LNS_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<LateNightScreenRollupWorker>().addTag("${TAG_LNS}_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_DIST_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DistanceSummaryWorker>().addTag("${TAG_DIST}_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_NOTIF_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<NotificationLogWorker>().addTag("${TAG_NOTIF_LOG}_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_NOTIF_ENG_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<NotificationEngagementWorker>().addTag("${TAG_NOTIF_ENG}_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_USAGE_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<UsageEventsDumpWorker>().addTag("${TAG_USAGE_EVENTS}_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_USAGE_DAILY_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<UsageEventsDailyWorker>().addTag("${TAG_USAGE_DAILY}_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_MOVE_INTENSITY_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<MovementIntensityDailyWorker>().addTag("${TAG_MOVE_INTENSITY}_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_DAILY_SUMMARY_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DailySummaryWorker>().addTag("${TAG_DAILY_SUMMARY}_now").build()
        )
    }

    private fun dailyAt(
        workerClass: Class<out ListenableWorker>,
        tag: String,
        hour: Int,
        minute: Int
    ): PeriodicWorkRequest {
        val now = ZonedDateTime.now()
        var firstRun = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!firstRun.isAfter(now)) firstRun = firstRun.plusDays(1)

        val initialDelay = Duration.between(now, firstRun).toMinutes().coerceAtLeast(1)

        return PeriodicWorkRequest.Builder(workerClass, 24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .addTag(tag)
            .build()
    }

    companion object {
        private val schedulerRan = AtomicBoolean(false)
        const val TAG = "MainApplication"

        private const val UNIQUE_DIST = "periodic_distance_summary"
        private const val UNIQUE_DIST_NOW = "onstart_distance_summary"
        private const val TAG_DIST = "DistanceSummaryPeriodic"

        private const val UNIQUE_LNS = "periodic_late_night_rollup"
        private const val UNIQUE_LNS_NOW = "onstart_late_night_rollup"
        private const val TAG_LNS = "LateNightRollupPeriodic"

        private const val UNIQUE_NOTIF_LOG = "periodic_notification_log_trim"
        private const val UNIQUE_NOTIF_NOW = "onstart_notification_log_trim"
        private const val TAG_NOTIF_LOG = "NotificationLogTrim"

        private const val UNIQUE_NOTIF_ENG = "periodic_notification_engagement"
        private const val UNIQUE_NOTIF_ENG_NOW = "onstart_notification_engagement"
        private const val TAG_NOTIF_ENG = "NotificationEngagementPeriodic"

        private const val UNIQUE_USAGE_EVENTS = "periodic_usage_events_dump"
        private const val UNIQUE_USAGE_NOW = "onstart_usage_events_dump"
        private const val TAG_USAGE_EVENTS = "UsageEventsDump"

        private const val UNIQUE_USAGE_DAILY = "periodic_usage_events_daily"
        private const val UNIQUE_USAGE_DAILY_NOW = "onstart_usage_events_daily"
        private const val TAG_USAGE_DAILY = "UsageEventsDaily"

        private const val UNIQUE_MOVE_INTENSITY = "periodic_movement_intensity"
        private const val UNIQUE_MOVE_INTENSITY_NOW = "onstart_movement_intensity"
        private const val TAG_MOVE_INTENSITY = "MovementIntensityDaily"

        private const val UNIQUE_DAILY_SUMMARY = "periodic_daily_summary"
        private const val UNIQUE_DAILY_SUMMARY_NOW = "onstart_daily_summary"
        private const val TAG_DAILY_SUMMARY = "DailySummaryPeriodic"
    }
}