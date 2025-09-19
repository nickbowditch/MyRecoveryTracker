// app/src/main/java/com/nick/myrecoverytracker/BootReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        ContextCompat.startForegroundService(
            context,
            Intent(context, ForegroundUnlockService::class.java)
        )
        ContextCompat.startForegroundService(
            context,
            Intent(context, ForegroundSleepService::class.java)
        )

        WorkScheduler.scheduleDailySleepRollup(context)
        WorkScheduler.enqueueOneTimeSleepRollup(context)

        val periodicUsage = PeriodicWorkRequestBuilder<UsageCaptureWorker>(
            24, TimeUnit.HOURS
        ).addTag("UsageCaptureDaily").build()

        val periodicNotif = PeriodicWorkRequestBuilder<NotificationRollupWorker>(
            24, TimeUnit.HOURS
        ).addTag("NotificationRollupDaily").build()

        val wm = WorkManager.getInstance(context)
        wm.enqueueUniquePeriodicWork("mrt_usage_daily", ExistingPeriodicWorkPolicy.UPDATE, periodicUsage)
        wm.enqueueUniquePeriodicWork("mrt_notification_daily", ExistingPeriodicWorkPolicy.UPDATE, periodicNotif)

        val onceUsage = OneTimeWorkRequestBuilder<UsageCaptureWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("UsageCaptureBoot")
            .build()

        val onceUnlock = OneTimeWorkRequestBuilder<UnlockWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("UnlockScanBoot")
            .build()

        val onceSleep = OneTimeWorkRequestBuilder<SleepRollupWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("SleepRollupBoot")
            .build()

        val onceMovement = OneTimeWorkRequestBuilder<MovementRollupWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("MovementRollupBoot")
            .build()

        val onceNotif = OneTimeWorkRequestBuilder<NotificationRollupWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("NotificationRollupBoot")
            .build()

        wm.enqueueUniqueWork("once-UsageCaptureBoot", ExistingWorkPolicy.REPLACE, onceUsage)
        wm.enqueueUniqueWork("once-UnlockScanBoot", ExistingWorkPolicy.REPLACE, onceUnlock)
        wm.enqueueUniqueWork("once-SleepRollupBoot", ExistingWorkPolicy.REPLACE, onceSleep)
        wm.enqueueUniqueWork("once-MovementRollupBoot", ExistingWorkPolicy.REPLACE, onceMovement)
        wm.enqueueUniqueWork("once-NotificationRollupBoot", ExistingWorkPolicy.REPLACE, onceNotif)
    }
}