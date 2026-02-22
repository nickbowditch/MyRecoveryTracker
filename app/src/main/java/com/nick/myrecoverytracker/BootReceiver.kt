package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_USER_UNLOCKED
        ) return

        ContextCompat.startForegroundService(
            context,
            Intent(context, ForegroundUnlockService::class.java)
        )

        WorkManager.getInstance(context).pruneWork()
        WorkScheduler.registerAllDaily(context)

        val oneTimeSleepReq: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SleepRollupWorker>().build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "boot-once-SleepRollup",
                ExistingWorkPolicy.KEEP,
                oneTimeSleepReq
            )
    }
}