// WorkScheduler.kt
package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val SLEEP_PERIODIC = "periodic-SleepRollup"
    private const val ENGAGEMENT_PERIODIC = "periodic-EngagementRollup"
    private const val MOVE_INTENSITY_PERIODIC = "periodic-MovementIntensityDaily"
    private const val DISTANCE_PERIODIC = "periodic-DistanceDaily"
    private const val SLEEP_DURATION_PERIODIC = "periodic-SleepDurationDaily"
    private const val USAGE_ENTROPY_PERIODIC = "periodic-UsageEntropyDaily"
    private const val USAGE_EVENTS_PERIODIC = "periodic-UsageEventsDaily"
    private const val USAGE_CAPTURE_PERIODIC = "periodic-UsageCaptureDaily"

    private val zone: ZoneId = ZoneId.systemDefault()

    fun registerAllDaily(context: Context) {
        scheduleDailyMovementIntensity(context)
        scheduleDailyDistance(context)
        scheduleDailySleepDuration(context)
        scheduleDailySleepRollup(context)
        scheduleDailyEngagementRollup(context)
        scheduleDailyUsageEntropy(context)
        scheduleDailyUsageEvents(context)
        scheduleDailyUsageCapture(context)
    }

    private fun schedulePeriodicWork(
        context: Context,
        workerClass: Class<out ListenableWorker>,
        uniqueName: String,
        hour: Int,
        minute: Int,
        tag: String
    ) {
        val now = ZonedDateTime.now(zone)
        val targetToday = LocalDate.now(zone).atTime(hour, minute).atZone(zone)
        val nextRun = if (now.isBefore(targetToday)) targetToday else targetToday.plusDays(1)

        val initialDelayMinutes = Duration
            .between(now, nextRun)
            .toMinutes()
            .coerceAtLeast(1L)

        val request = PeriodicWorkRequest.Builder(
            workerClass,
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .addTag(tag)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                uniqueName,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    fun scheduleDailySleepRollup(context: Context) =
        schedulePeriodicWork(
            context,
            SleepRollupWorker::class.java,
            SLEEP_PERIODIC,
            4,
            15,
            "SleepRollup"
        )

    fun scheduleDailyEngagementRollup(context: Context) =
        schedulePeriodicWork(
            context,
            NotificationEngagementWorker::class.java,
            ENGAGEMENT_PERIODIC,
            4,
            25,
            "NotificationEngagement"
        )

    fun scheduleDailyMovementIntensity(context: Context) =
        schedulePeriodicWork(
            context,
            MovementIntensityDailyWorker::class.java,
            MOVE_INTENSITY_PERIODIC,
            4,
            5,
            "MovementIntensityDaily"
        )

    fun scheduleDailyDistance(context: Context) =
        schedulePeriodicWork(
            context,
            DistanceWorker::class.java,
            DISTANCE_PERIODIC,
            4,
            10,
            "DistanceDaily"
        )

    fun scheduleDailySleepDuration(context: Context) =
        schedulePeriodicWork(
            context,
            SleepDurationWorker::class.java,
            SLEEP_DURATION_PERIODIC,
            4,
            20,
            "SleepDurationDaily"
        )

    fun scheduleDailyUsageEntropy(context: Context) =
        schedulePeriodicWork(
            context,
            UsageEntropyDailyWorker::class.java,
            USAGE_ENTROPY_PERIODIC,
            4,
            30,
            "UsageEntropyDaily"
        )

    fun scheduleDailyUsageEvents(context: Context) =
        schedulePeriodicWork(
            context,
            UsageEventsDailyWorker::class.java,
            USAGE_EVENTS_PERIODIC,
            4,
            35,
            "UsageEventsDaily"
        )

    fun scheduleDailyUsageCapture(context: Context) =
        schedulePeriodicWork(
            context,
            UsageCaptureWorker::class.java,
            USAGE_CAPTURE_PERIODIC,
            4,
            40,
            "UsageCaptureDaily"
        )
}