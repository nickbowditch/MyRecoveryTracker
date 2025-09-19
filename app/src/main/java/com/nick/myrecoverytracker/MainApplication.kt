package com.nick.myrecoverytracker

import android.app.Application
import android.util.Log
import androidx.work.*
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            schedulePeriodicWork()
            enqueueImmediateCatchUp()
        } catch (t: Throwable) {
            Log.e(TAG, "Work scheduling failed", t)
        }
    }

    private fun schedulePeriodicWork() {
        val wm = WorkManager.getInstance(this)

        val locConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val locationPing = PeriodicWorkRequestBuilder<LocationPingWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        ).setConstraints(locConstraints).addTag(TAG_LOC).build()
        wm.enqueueUniquePeriodicWork(UNIQUE_LOC, ExistingPeriodicWorkPolicy.KEEP, locationPing)

        val distanceDaily = dailyAt(DistanceSummaryWorker::class.java, UNIQUE_DIST, TAG_DIST, 3, 10)
        wm.enqueueUniquePeriodicWork(UNIQUE_DIST, ExistingPeriodicWorkPolicy.UPDATE, distanceDaily)

        val sleepDaily = dailyAt(SleepRollupWorker::class.java, UNIQUE_SLEEP, TAG_SLEEP, 3, 30)
        wm.enqueueUniquePeriodicWork(UNIQUE_SLEEP, ExistingPeriodicWorkPolicy.UPDATE, sleepDaily)

        val lateNightDaily = dailyAt(LateNightScreenRollupWorker::class.java, UNIQUE_LNS, TAG_LNS, 5, 5)
        wm.enqueueUniquePeriodicWork(UNIQUE_LNS, ExistingPeriodicWorkPolicy.UPDATE, lateNightDaily)

        val luxPeriodic = PeriodicWorkRequestBuilder<AmbientLuxWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        ).addTag(TAG_LUX).build()
        wm.enqueueUniquePeriodicWork(UNIQUE_LUX, ExistingPeriodicWorkPolicy.KEEP, luxPeriodic)

        val lightDaily = dailyAt(DailyLightExposureWorker::class.java, UNIQUE_LIGHT_DAILY, TAG_LIGHT_DAILY, 3, 40)
        wm.enqueueUniquePeriodicWork(UNIQUE_LIGHT_DAILY, ExistingPeriodicWorkPolicy.UPDATE, lightDaily)

        val notifLogDaily = dailyAt(NotificationLogWorker::class.java, UNIQUE_NOTIF_LOG, TAG_NOTIF_LOG, 3, 20)
        wm.enqueueUniquePeriodicWork(UNIQUE_NOTIF_LOG, ExistingPeriodicWorkPolicy.UPDATE, notifLogDaily)

        val notifEngDaily = dailyAt(NotificationEngagementWorker::class.java, UNIQUE_NOTIF_ENG, TAG_NOTIF_ENG, 3, 47)
        wm.enqueueUniquePeriodicWork(UNIQUE_NOTIF_ENG, ExistingPeriodicWorkPolicy.UPDATE, notifEngDaily)

        val notifLatDaily = dailyAt(NotificationLatencyWorker::class.java, UNIQUE_NOTIF_LAT, TAG_NOTIF_LAT, 3, 48)
        wm.enqueueUniquePeriodicWork(UNIQUE_NOTIF_LAT, ExistingPeriodicWorkPolicy.UPDATE, notifLatDaily)

        val usageEventsPeriodic = PeriodicWorkRequestBuilder<UsageEventsDumpWorker>(
            3, TimeUnit.HOURS
        ).addTag(TAG_USAGE_EVENTS).build()
        wm.enqueueUniquePeriodicWork(UNIQUE_USAGE_EVENTS, ExistingPeriodicWorkPolicy.KEEP, usageEventsPeriodic)

        val appUsageCatDaily = dailyAt(AppUsageByCategoryDailyWorker::class.java, UNIQUE_APP_CAT, TAG_APP_CAT, 4, 15)
        wm.enqueueUniquePeriodicWork(UNIQUE_APP_CAT, ExistingPeriodicWorkPolicy.UPDATE, appUsageCatDaily)

        val appSwitchDaily = dailyAt(AppSwitchingDailyWorker::class.java, UNIQUE_APP_SWITCH, TAG_APP_SWITCH, 4, 25)
        wm.enqueueUniquePeriodicWork(UNIQUE_APP_SWITCH, ExistingPeriodicWorkPolicy.UPDATE, appSwitchDaily)

        val usageEventsDaily = dailyAt(UsageEventsDailyWorker::class.java, UNIQUE_USAGE_DAILY, TAG_USAGE_DAILY, 4, 35)
        wm.enqueueUniquePeriodicWork(UNIQUE_USAGE_DAILY, ExistingPeriodicWorkPolicy.UPDATE, usageEventsDaily)

        val moveIntensityDaily = dailyAt(MovementIntensityDailyWorker::class.java, UNIQUE_MOVE_INTENSITY, TAG_MOVE_INTENSITY, 4, 45)
        wm.enqueueUniquePeriodicWork(UNIQUE_MOVE_INTENSITY, ExistingPeriodicWorkPolicy.UPDATE, moveIntensityDaily)

        Log.i(TAG, "Periodic work scheduled")
    }

    private fun enqueueImmediateCatchUp() {
        val wm = WorkManager.getInstance(this)

        wm.enqueueUniqueWork(
            UNIQUE_SLEEP_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<SleepRollupWorker>().addTag(TAG_SLEEP + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_LNS_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<LateNightScreenRollupWorker>().addTag(TAG_LNS + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_DIST_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DistanceSummaryWorker>().addTag(TAG_DIST + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_LUX_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<AmbientLuxWorker>().addTag(TAG_LUX + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_LIGHT_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DailyLightExposureWorker>().addTag(TAG_LIGHT_DAILY + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_NOTIF_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<NotificationLogWorker>().addTag(TAG_NOTIF_LOG + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_NOTIF_ENG_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<NotificationEngagementWorker>().addTag(TAG_NOTIF_ENG + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_NOTIF_LAT_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<NotificationLatencyWorker>().addTag(TAG_NOTIF_LAT + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_USAGE_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<UsageEventsDumpWorker>().addTag(TAG_USAGE_EVENTS + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_APP_CAT_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<AppUsageByCategoryDailyWorker>().addTag(TAG_APP_CAT + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_APP_SWITCH_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<AppSwitchingDailyWorker>().addTag(TAG_APP_SWITCH + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_USAGE_DAILY_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<UsageEventsDailyWorker>().addTag(TAG_USAGE_DAILY + "_now").build()
        )

        wm.enqueueUniqueWork(
            UNIQUE_MOVE_INTENSITY_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<MovementIntensityDailyWorker>().addTag(TAG_MOVE_INTENSITY + "_now").build()
        )

        Log.i(TAG, "Immediate catch-up enqueued")
    }

    private fun dailyAt(
        workerClass: Class<out ListenableWorker>,
        uniqueName: String,
        tag: String,
        hour: Int,
        minute: Int
    ): PeriodicWorkRequest {
        val now = ZonedDateTime.now()
        var firstRun = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!firstRun.isAfter(now)) firstRun = firstRun.plusDays(1)
        val initialDelay = Duration.between(now, firstRun).toMinutes()

        return PeriodicWorkRequest.Builder(workerClass, 24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .addTag(tag)
            .build()
    }

    companion object {
        const val TAG = "MainApplication"

        private const val UNIQUE_LOC = "periodic_location_ping"
        private const val UNIQUE_DIST = "periodic_distance_summary"
        private const val UNIQUE_DIST_NOW = "onstart_distance_summary"
        private const val TAG_LOC = "LocationPingPeriodic"
        private const val TAG_DIST = "DistanceSummaryPeriodic"

        private const val UNIQUE_SLEEP = "periodic_sleep_rollup"
        private const val UNIQUE_LNS = "periodic_late_night_rollup"
        private const val UNIQUE_SLEEP_NOW = "onstart_sleep_rollup"
        private const val UNIQUE_LNS_NOW = "onstart_late_night_rollup"
        private const val TAG_SLEEP = "SleepRollupPeriodic"
        private const val TAG_LNS = "LateNightRollupPeriodic"

        private const val UNIQUE_LUX = "periodic_lux_sample"
        private const val UNIQUE_LIGHT_DAILY = "periodic_daily_light_exposure"
        private const val UNIQUE_LUX_NOW = "onstart_lux_sample"
        private const val UNIQUE_LIGHT_NOW = "onstart_light_exposure"
        private const val TAG_LUX = "AmbientLuxPeriodic"
        private const val TAG_LIGHT_DAILY = "DailyLightExposurePeriodic"

        private const val UNIQUE_NOTIF_LOG = "periodic_notification_log_trim"
        private const val UNIQUE_NOTIF_NOW = "onstart_notification_log_trim"
        private const val TAG_NOTIF_LOG = "NotificationLogTrim"

        private const val UNIQUE_NOTIF_ENG = "periodic_notification_engagement"
        private const val UNIQUE_NOTIF_LAT = "periodic_notification_latency"
        private const val UNIQUE_NOTIF_ENG_NOW = "onstart_notification_engagement"
        private const val UNIQUE_NOTIF_LAT_NOW = "onstart_notification_latency"
        private const val TAG_NOTIF_ENG = "NotificationEngagementPeriodic"
        private const val TAG_NOTIF_LAT = "NotificationLatencyPeriodic"

        private const val UNIQUE_USAGE_EVENTS = "periodic_usage_events_dump"
        private const val UNIQUE_USAGE_NOW = "onstart_usage_events_dump"
        private const val TAG_USAGE_EVENTS = "UsageEventsDump"

        private const val UNIQUE_APP_CAT = "periodic_app_usage_category"
        private const val UNIQUE_APP_CAT_NOW = "onstart_app_usage_category"
        private const val TAG_APP_CAT = "AppUsageCategoryDaily"

        private const val UNIQUE_APP_SWITCH = "periodic_app_switching"
        private const val UNIQUE_APP_SWITCH_NOW = "onstart_app_switching"
        private const val TAG_APP_SWITCH = "AppSwitchingDaily"

        private const val UNIQUE_USAGE_DAILY = "periodic_usage_events_daily"
        private const val UNIQUE_USAGE_DAILY_NOW = "onstart_usage_events_daily"
        private const val TAG_USAGE_DAILY = "UsageEventsDaily"

        private const val UNIQUE_MOVE_INTENSITY = "periodic_movement_intensity"
        private const val UNIQUE_MOVE_INTENSITY_NOW = "onstart_movement_intensity"
        private const val TAG_MOVE_INTENSITY = "MovementIntensityDaily"
    }
}