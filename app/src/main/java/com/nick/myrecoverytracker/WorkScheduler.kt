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

    fun scheduleDailySleepRollup(context: Context) {
        val now = LocalDateTime.now()
        the@ run {
            val zone = ZoneId.systemDefault()
            val target = LocalDate.now(zone).atTime(LocalTime.of(4, 15))
            val initial = Duration.between(now, nextOccurrence(now, target))
            val req = PeriodicWorkRequestBuilder<SleepRollupWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initial.toMinutes().coerceAtLeast(1), TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(SLEEP_PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, req)
        }
    }

    fun enqueueOneTimeSleepRollup(context: Context) {
        val one = OneTimeWorkRequestBuilder<SleepRollupWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("boot-once-SleepRollup", ExistingWorkPolicy.KEEP, one)
    }

    private fun nextOccurrence(now: LocalDateTime, targetToday: LocalDateTime): LocalDateTime {
        return if (now.isBefore(targetToday)) targetToday else targetToday.plusDays(1)
    }
}