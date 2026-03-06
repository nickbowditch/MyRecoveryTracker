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

    // Golden workers
    private const val ENGAGEMENT_PERIODIC = "periodic-EngagementRollup"
    private const val LATENCY_PERIODIC = "periodic-NotificationLatency"
    private const val MOVE_INTENSITY_PERIODIC = "periodic-MovementIntensityDaily"
    private const val USAGE_ENTROPY_PERIODIC = "periodic-UsageEntropyDaily"
    private const val USAGE_EVENTS_PERIODIC = "periodic-UsageEventsDaily"
    private const val USAGE_CAPTURE_PERIODIC = "periodic-UsageCaptureDaily"
    private const val LATE_NIGHT_SCREEN_PERIODIC = "periodic-LateNightScreenRollup"

    // Validation/Diagnostic workers
    private const val HEALTH_SNAPSHOT_PERIODIC = "periodic-HealthSnapshot"
    private const val USAGE_ACCESS_DIAG_PERIODIC = "periodic-UsageAccessDiag"
    private const val UNLOCK_VALIDATION_PERIODIC = "periodic-UnlockValidation"
    private const val NOTIFICATION_VALIDATION_PERIODIC = "periodic-NotificationValidation"

    // Upload worker
    private const val REDCAP_UPLOAD_PERIODIC = "periodic-RedcapUpload"

    // Infrastructure workers
    private const val HEARTBEAT_PERIODIC = "periodic-Heartbeat"
    private const val SERVICE_HEALTH_CHECK_PERIODIC = "periodic-ServiceHealthCheck"

    private val zone: ZoneId = ZoneId.systemDefault()

    fun registerAllWork(context: Context) {
        registerAllDaily(context)
        registerValidationWorkers(context)
        registerUploadWorkers(context)
        registerInfrastructureWorkers(context)
    }

    private fun registerAllDaily(context: Context) {
        scheduleDailyMovementIntensity(context)
        scheduleDailyEngagementRollup(context)
        scheduleDailyNotificationLatency(context)
        scheduleDailyUsageEntropy(context)
        scheduleDailyUsageEvents(context)
        scheduleDailyUsageCapture(context)
        scheduleDailyLateNightScreen(context)
    }

    private fun registerValidationWorkers(context: Context) {
        scheduleHealthSnapshot(context)
        scheduleUsageAccessDiag(context)
        scheduleUnlockValidation(context)
        scheduleNotificationValidation(context)
    }

    private fun registerUploadWorkers(context: Context) {
        scheduleRedcapUpload(context)
    }

    private fun registerInfrastructureWorkers(context: Context) {
        scheduleServiceHealthCheck(context)
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

    private fun schedulePeriodicInterval(
        context: Context,
        workerClass: Class<out ListenableWorker>,
        uniqueName: String,
        intervalMinutes: Long,
        flexMinutes: Long,
        tag: String
    ) {
        val request = PeriodicWorkRequest.Builder(
            workerClass,
            intervalMinutes, TimeUnit.MINUTES,
            flexMinutes, TimeUnit.MINUTES
        )
            .addTag(tag)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                uniqueName,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    // Golden Workers (4:05 AM - 5:05 AM)
    fun scheduleDailyMovementIntensity(context: Context) =
        schedulePeriodicWork(
            context,
            MovementIntensityDailyWorker::class.java,
            MOVE_INTENSITY_PERIODIC,
            4,
            5,
            "MovementIntensityDaily"
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

    fun scheduleDailyNotificationLatency(context: Context) =
        schedulePeriodicWork(
            context,
            NotificationLatencyWorker::class.java,
            LATENCY_PERIODIC,
            4,
            20,
            "NotificationLatency"
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

    fun scheduleDailyLateNightScreen(context: Context) =
        schedulePeriodicWork(
            context,
            LateNightScreenRollupWorker::class.java,
            LATE_NIGHT_SCREEN_PERIODIC,
            5,
            5,
            "LateNightScreenRollup"
        )

    // Validation/Diagnostic Workers (6:00 AM - 6:20 AM)
    fun scheduleHealthSnapshot(context: Context) =
        schedulePeriodicWork(
            context,
            HealthSnapshotWorker::class.java,
            HEALTH_SNAPSHOT_PERIODIC,
            6,
            0,
            "HealthSnapshot"
        )

    fun scheduleUsageAccessDiag(context: Context) =
        schedulePeriodicWork(
            context,
            UsageAccessDiagWorker::class.java,
            USAGE_ACCESS_DIAG_PERIODIC,
            6,
            10,
            "UsageAccessDiag"
        )

    fun scheduleUnlockValidation(context: Context) =
        schedulePeriodicWork(
            context,
            UnlockValidationWorker::class.java,
            UNLOCK_VALIDATION_PERIODIC,
            6,
            15,
            "UnlockValidation"
        )

    fun scheduleNotificationValidation(context: Context) =
        schedulePeriodicWork(
            context,
            NotificationValidationWorker::class.java,
            NOTIFICATION_VALIDATION_PERIODIC,
            6,
            20,
            "NotificationValidation"
        )

    // Upload Workers (7:00 AM)
    fun scheduleRedcapUpload(context: Context) =
        schedulePeriodicWork(
            context,
            RedcapUploadWorker::class.java,
            REDCAP_UPLOAD_PERIODIC,
            7,
            0,
            "RedcapUpload"
        )

    // Infrastructure Workers (periodic intervals)
    fun scheduleServiceHealthCheck(context: Context) =
        schedulePeriodicInterval(
            context,
            ServiceHealthCheckWorker::class.java,
            SERVICE_HEALTH_CHECK_PERIODIC,
            360,  // 6 hours
            30,
            "ServiceHealthCheck"
        )
}