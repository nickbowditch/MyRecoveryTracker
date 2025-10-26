// app/src/main/java/com/nick/myrecoverytracker/BootReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            context.startService(Intent(context, ForegroundUnlockService::class.java))
        } catch (_: Throwable) { }

        val wm = WorkManager.getInstance(context)

        wm.cancelUniqueWork("mrt_notification_daily")
        wm.cancelAllWorkByTag("NotificationEngagementDaily")
        wm.pruneWork()

        WorkScheduler.registerAllDaily(context)
        WorkScheduler.enqueueOneTimeSleepRollup(context)

        val distancePeriodic = PeriodicWorkRequestBuilder<DistanceWorker>(24, TimeUnit.HOURS)
            .addTag("DistanceDaily")
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        wm.enqueueUniquePeriodicWork("mrt_distance_daily", ExistingPeriodicWorkPolicy.UPDATE, distancePeriodic)

        val notifPeriodic = PeriodicWorkRequestBuilder<NotificationEngagementWorker>(24, TimeUnit.HOURS)
            .addTag("NotificationEngagementDaily")
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        wm.enqueueUniquePeriodicWork("mrt_notification_engagement_daily", ExistingPeriodicWorkPolicy.UPDATE, notifPeriodic)

        val sleepDurationPeriodic = PeriodicWorkRequestBuilder<SleepDurationWorker>(24, TimeUnit.HOURS)
            .addTag("SleepDurationDaily")
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        wm.enqueueUniquePeriodicWork("mrt_sleep_duration_daily", ExistingPeriodicWorkPolicy.UPDATE, sleepDurationPeriodic)

        val dailySummaryPeriodic = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
            .addTag("DailySummaryDaily")
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        wm.enqueueUniquePeriodicWork("mrt_daily_summary", ExistingPeriodicWorkPolicy.UPDATE, dailySummaryPeriodic)

        val logExportPeriodic = PeriodicWorkRequestBuilder<LogExportWorker>(24, TimeUnit.HOURS)
            .addTag("LogExportDaily")
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        wm.enqueueUniquePeriodicWork("mrt_log_export_daily", ExistingPeriodicWorkPolicy.UPDATE, logExportPeriodic)

        // NEW: Log retention periodic
        val logRetentionPeriodic = PeriodicWorkRequestBuilder<LogRetentionWorker>(24, TimeUnit.HOURS)
            .addTag("LogRetentionDaily")
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
        wm.enqueueUniquePeriodicWork("mrt_log_retention_daily", ExistingPeriodicWorkPolicy.UPDATE, logRetentionPeriodic)

        val distanceOnce = OneTimeWorkRequestBuilder<DistanceWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("DistanceBoot")
            .build()
        val notifOnce = OneTimeWorkRequestBuilder<NotificationEngagementWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("NotificationEngagementBoot")
            .build()
        val sleepDurOnce = OneTimeWorkRequestBuilder<SleepDurationWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("SleepDurationBoot")
            .build()
        val dailySummaryOnce = OneTimeWorkRequestBuilder<DailySummaryWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("DailySummaryBoot")
            .build()
        val logExportOnce = OneTimeWorkRequestBuilder<LogExportWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("LogExportBoot")
            .build()
        // NEW: Log retention once
        val logRetentionOnce = OneTimeWorkRequestBuilder<LogRetentionWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("LogRetentionBoot")
            .build()

        wm.enqueueUniqueWork("once-DistanceBoot", ExistingWorkPolicy.REPLACE, distanceOnce)
        wm.enqueueUniqueWork("once-NotificationEngagementBoot", ExistingWorkPolicy.REPLACE, notifOnce)
        wm.enqueueUniqueWork("once-SleepDurationBoot", ExistingWorkPolicy.REPLACE, sleepDurOnce)
        wm.enqueueUniqueWork("once-DailySummaryBoot", ExistingWorkPolicy.REPLACE, dailySummaryOnce)
        wm.enqueueUniqueWork("once-LogExportBoot", ExistingWorkPolicy.REPLACE, logExportOnce)
        wm.enqueueUniqueWork("once-LogRetentionBoot", ExistingWorkPolicy.REPLACE, logRetentionOnce)
    }
}