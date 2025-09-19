// app/src/main/java/com/nick/myrecoverytracker/TriggerReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager

class TriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_RUN_ROLLUPS,
            ACTION_RUN_ALL_ROLLUPS -> {
                UnlockMigrations.run(context)

                val unlockReq = OneTimeWorkRequestBuilder<UnlockWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("UnlockScan")
                    .build()

                val sleepReq = OneTimeWorkRequestBuilder<SleepRollupWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("SleepRollup")
                    .build()

                val uploadReq = OneTimeWorkRequestBuilder<RedcapUploadWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("RedcapUpload")
                    .build()

                WorkManager.getInstance(context)
                    .beginUniqueWork("rollups-and-upload", ExistingWorkPolicy.REPLACE, unlockReq)
                    .then(sleepReq)
                    .then(uploadReq)
                    .enqueue()
            }

            // Added explicit rollup trigger to support ACTION_RUN_UNLOCK_ROLLUP
            ACTION_RUN_UNLOCK_ROLLUP -> {
                Log.i("TriggerReceiver", "Enqueue UnlockRollupWorker (unique=once-UnlockRollup)")
                val req = OneTimeWorkRequestBuilder<UnlockRollupWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("UnlockRollup")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-UnlockRollup", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_MOVEMENT_ROLLUP -> {
                Log.i("TriggerReceiver", "Enqueue MovementRollupWorker (unique=once-MovementRollup)")
                val req = OneTimeWorkRequestBuilder<MovementRollupWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("MovementRollup")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-MovementRollup", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_MOVEMENT_INTENSITY -> {
                Log.i("TriggerReceiver", "Enqueue MovementIntensityDailyWorker (unique=once-MovementIntensity)")
                val req = OneTimeWorkRequestBuilder<MovementIntensityDailyWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("MovementIntensity")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-MovementIntensity", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_UNLOCK_SCAN -> {
                UnlockMigrations.run(context)
                val req = OneTimeWorkRequestBuilder<UnlockWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("UnlockScan")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-UnlockScan", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_USAGE_ENTROPY -> {
                val req = OneTimeWorkRequestBuilder<UsageEntropyDailyWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("UsageEntropy")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-UsageEntropy", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_HEALTH_SNAPSHOT -> {
                val req = OneTimeWorkRequestBuilder<HealthSnapshotWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("HealthSnapshot")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-HealthSnapshot", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_SLEEP_ROLLUP -> {
                val req = OneTimeWorkRequestBuilder<SleepRollupWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("SleepRollup")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-SleepRollup", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_SLEEP_VALIDATION -> {
                val req = OneTimeWorkRequestBuilder<SleepValidationWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("SleepValidation")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-SleepValidation", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_VERIFY_SLEEP_RESCHEDULE -> {
                val req = OneTimeWorkRequestBuilder<SleepValidationWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("SleepValidation")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("SleepRollup", ExistingWorkPolicy.KEEP, req)
            }

            ACTION_RUN_UNLOCK_VALIDATION -> {
                val req = OneTimeWorkRequestBuilder<UnlockValidationWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("UnlockValidation")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-UnlockValidation", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_USAGE_CAPTURE -> {
                Log.i("TriggerReceiver", "Enqueue UsageCaptureWorker (unique=once-UsageCapture)")
                val req = OneTimeWorkRequestBuilder<UsageCaptureWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("UsageCapture")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-UsageCapture", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_NOTIFICATION_ROLLUP -> {
                Log.i("TriggerReceiver", "Enqueue NotificationRollupWorker (unique=once-NotificationRollup)")
                val req = OneTimeWorkRequestBuilder<NotificationRollupWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("NotificationRollup")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-NotificationRollup", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_NOTIFICATION_VALIDATION -> {
                Log.i("TriggerReceiver", "Enqueue NotificationValidationWorker (unique=once-NotificationValidation)")
                val req = OneTimeWorkRequestBuilder<NotificationValidationWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("NotificationValidation")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-NotificationValidation", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_REDCAP_UPLOAD -> {
                Log.i("TriggerReceiver", "Enqueue RedcapUploadWorker (unique=once-RedcapUpload)")
                val req = OneTimeWorkRequestBuilder<RedcapUploadWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("RedcapUpload")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-RedcapUpload", ExistingWorkPolicy.REPLACE, req)
            }
        }
    }

    companion object {
        const val ACTION_RUN_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ROLLUPS"
        const val ACTION_RUN_ALL_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ALL_ROLLUPS"
        const val ACTION_RUN_MOVEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_ROLLUP"
        const val ACTION_RUN_MOVEMENT_INTENSITY = "com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_INTENSITY"
        const val ACTION_RUN_UNLOCK_SCAN = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_SCAN"
        const val ACTION_RUN_USAGE_ENTROPY = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_ENTROPY"
        const val ACTION_RUN_HEALTH_SNAPSHOT = "com.nick.myrecoverytracker.ACTION_RUN_HEALTH_SNAPSHOT"
        const val ACTION_RUN_SLEEP_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_SLEEP_ROLLUP"
        const val ACTION_RUN_SLEEP_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_SLEEP_VALIDATION"
        const val ACTION_VERIFY_SLEEP_RESCHEDULE = "com.nick.myrecoverytracker.ACTION_VERIFY_SLEEP_RESCHEDULE"
        const val ACTION_RUN_UNLOCK_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_VALIDATION"
        const val ACTION_RUN_USAGE_CAPTURE = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_CAPTURE"
        const val ACTION_RUN_NOTIFICATION_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP"
        const val ACTION_RUN_NOTIFICATION_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_VALIDATION"
        const val ACTION_RUN_REDCAP_UPLOAD = "com.nick.myrecoverytracker.ACTION_RUN_REDCAP_UPLOAD"

        // New: explicit constant to drive rollup of daily_unlocks.csv
        const val ACTION_RUN_UNLOCK_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_ROLLUP"
    }
}