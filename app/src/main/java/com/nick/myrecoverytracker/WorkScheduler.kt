// app/src/main/java/com/nick/myrecoverytracker/WorkScheduler.kt
package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val SLEEP_PERIODIC = "periodic-SleepRollup"
    private const val ENGAGEMENT_PERIODIC = "periodic-EngagementRollup"

    // Unique names for the three daily metrics
    private const val MOVE_INTENSITY_PERIODIC = "periodic-MovementIntensityDaily"
    private const val DISTANCE_PERIODIC       = "periodic-DistanceDaily"
    private const val SLEEP_DURATION_PERIODIC = "periodic-SleepDurationDaily"

    fun scheduleDailySleepRollup(context: Context) {
        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        val target = LocalDate.now(zone).atTime(LocalTime.of(4, 15))
        val initial = Duration.between(now, nextOccurrence(now, target))
        val req = PeriodicWorkRequestBuilder<SleepRollupWorker>(24L, TimeUnit.HOURS, 1L, TimeUnit.HOURS)
            .setInitialDelay(initial.toMinutes().coerceAtLeast(1L), TimeUnit.MINUTES)
            .addTag("SleepRollup")
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(SLEEP_PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun enqueueOneTimeSleepRollup(context: Context) {
        val one = OneTimeWorkRequestBuilder<SleepRollupWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("boot-once-SleepRollup", ExistingWorkPolicy.KEEP, one)
    }

    fun scheduleDailyEngagementRollup(context: Context) {
        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        val target = LocalDate.now(zone).atTime(LocalTime.of(4, 25))
        val initial = Duration.between(now, nextOccurrence(now, target))
        val req = PeriodicWorkRequestBuilder<NotificationEngagementWorker>(24L, TimeUnit.HOURS, 1L, TimeUnit.HOURS)
            .setInitialDelay(initial.toMinutes().coerceAtLeast(1L), TimeUnit.MINUTES)
            .addTag("NotificationEngagement")
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(ENGAGEMENT_PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun scheduleDailyMovementIntensity(context: Context) {
        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        val target = LocalDate.now(zone).atTime(LocalTime.of(4, 5))
        val initial = Duration.between(now, nextOccurrence(now, target))
        val req = PeriodicWorkRequestBuilder<MovementIntensityDailyWorker>(24L, TimeUnit.HOURS, 1L, TimeUnit.HOURS)
            .setInitialDelay(initial.toMinutes().coerceAtLeast(1L), TimeUnit.MINUTES)
            .addTag("MovementIntensityDaily")
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(MOVE_INTENSITY_PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun scheduleDailyDistance(context: Context) {
        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        val target = LocalDate.now(zone).atTime(LocalTime.of(4, 10))
        val initial = Duration.between(now, nextOccurrence(now, target))
        val req = PeriodicWorkRequestBuilder<DistanceWorker>(24L, TimeUnit.HOURS, 1L, TimeUnit.HOURS)
            .setInitialDelay(initial.toMinutes().coerceAtLeast(1L), TimeUnit.MINUTES)
            .addTag("DistanceDaily")
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(DISTANCE_PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun scheduleDailySleepDuration(context: Context) {
        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        val target = LocalDate.now(zone).atTime(LocalTime.of(4, 20))
        val initial = Duration.between(now, nextOccurrence(now, target))
        val req = PeriodicWorkRequestBuilder<SleepDurationWorker>(24L, TimeUnit.HOURS, 1L, TimeUnit.HOURS)
            .setInitialDelay(initial.toMinutes().coerceAtLeast(1L), TimeUnit.MINUTES)
            .addTag("SleepDurationDaily")
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(SLEEP_DURATION_PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    // Remove obsolete/legacy unique works that could duplicate the new ones.
    fun cleanupObsolete(context: Context) {
        val wm = WorkManager.getInstance(context)
        // Legacy engagement unique name we used before
        wm.cancelUniqueWork("mrt_notification_daily")
        wm.pruneWork()
    }

    // One call to seed everything
    fun registerAllDaily(context: Context) {
        scheduleDailyMovementIntensity(context)
        scheduleDailyDistance(context)
        scheduleDailySleepDuration(context)
        scheduleDailySleepRollup(context)
        scheduleDailyEngagementRollup(context)
    }

    private fun nextOccurrence(now: LocalDateTime, targetToday: LocalDateTime): LocalDateTime {
        return if (now.isBefore(targetToday)) targetToday else targetToday.plusDays(1)
    }
}