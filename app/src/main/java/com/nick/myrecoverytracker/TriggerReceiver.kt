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
        val action = intent?.action ?: return
        Log.i("TriggerReceiver", "onReceive action=$action")

        when (action) {
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

                Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)")
                val latencyReq = OneTimeWorkRequestBuilder<NotificationLatencyWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("NotificationLatency")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, latencyReq)
            }

            ACTION_RUN_DISTANCE_DAILY -> {
                Log.i("TriggerReceiver", "Enqueue DistanceWorker (once-DistanceDaily)")
                val req = OneTimeWorkRequestBuilder<DistanceWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("DistanceDaily")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-DistanceDaily", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_UNLOCK_ROLLUP -> {
                Log.i("TriggerReceiver", "Enqueue UnlockRollupWorker (once-UnlockRollup)")
                val req = OneTimeWorkRequestBuilder<UnlockRollupWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("UnlockRollup")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-UnlockRollup", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_MOVEMENT_ROLLUP -> {
                Log.i("TriggerReceiver", "Enqueue MovementRollupWorker (once-MovementRollup)")
                val req = OneTimeWorkRequestBuilder<MovementRollupWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("MovementRollup")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-MovementRollup", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_MOVEMENT_INTENSITY -> {
                Log.i("TriggerReceiver", "Enqueue MovementIntensityDailyWorker (once-MovementIntensity)")
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
                Log.i("TriggerReceiver", "Enqueue UsageCaptureWorker (once-UsageCapture)")
                val req = OneTimeWorkRequestBuilder<UsageCaptureWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("UsageCapture")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-UsageCapture", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_NOTIFICATION_ROLLUP,
            ACTION_RUN_ENGAGEMENT_ROLLUP,
            ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP,
            ACTION_RUN_NOTIF_ENGAGEMENT_ROLLUP -> {
                Log.i("TriggerReceiver", "Enqueue NotificationEngagementWorker (once-EngagementRollup)")
                val engReq = OneTimeWorkRequestBuilder<NotificationEngagementWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("NotificationEngagement")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-EngagementRollup", ExistingWorkPolicy.REPLACE, engReq)

                Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)")
                val latencyReq = OneTimeWorkRequestBuilder<NotificationLatencyWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("NotificationLatency")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, latencyReq)
            }

            ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP -> {
                Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)")
                val req = OneTimeWorkRequestBuilder<NotificationLatencyWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("NotificationLatency")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_NOTIFICATION_VALIDATION -> {
                Log.i("TriggerReceiver", "Enqueue NotificationValidationWorker (once-NotificationValidation)")
                val req = OneTimeWorkRequestBuilder<NotificationValidationWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("NotificationValidation")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-NotificationValidation", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_REDCAP_UPLOAD -> {
                Log.i("TriggerReceiver", "Enqueue RedcapUploadWorker (once-RedcapUpload)")
                val req = OneTimeWorkRequestBuilder<RedcapUploadWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("RedcapUpload")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-RedcapUpload", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_LNS_ROLLUP -> {
                Log.i("TriggerReceiver", "Enqueue LateNightScreenRollupWorker (once-LateNightRollup)")
                val req = OneTimeWorkRequestBuilder<LateNightScreenRollupWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("LateNightRollup")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-LateNightRollup", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_USAGE_EVENTS_DAILY -> {
                Log.i("TriggerReceiver", "Enqueue UsageEventsDailyWorker (once-UsageEventsDaily)")
                val req = OneTimeWorkRequestBuilder<UsageEventsDailyWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("UsageEventsDaily")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-UsageEventsDaily", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_APP_CATEGORY_DAILY -> {
                Log.i("TriggerReceiver", "Enqueue AppUsageByCategoryDailyWorker (once-AppUsageCategoryDaily)")
                val req = OneTimeWorkRequestBuilder<AppUsageByCategoryDailyWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("AppUsageCategoryDaily")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-AppUsageCategoryDaily", ExistingWorkPolicy.REPLACE, req)
            }

            ACTION_RUN_APP_SWITCHING_DAILY -> {
                Log.i("TriggerReceiver", "Enqueue AppSwitchingDailyWorker (once-AppSwitchingDaily)")
                val req = OneTimeWorkRequestBuilder<AppSwitchingDailyWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("AppSwitchingDaily")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-AppSwitchingDaily", ExistingWorkPolicy.REPLACE, req)
            }
        }
    }

    companion object {
        const val ACTION_RUN_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ROLLUPS"
        const val ACTION_RUN_ALL_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ALL_ROLLUPS"

        const val ACTION_RUN_DISTANCE_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_DISTANCE_DAILY"

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

        const val ACTION_RUN_ENGAGEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_ENGAGEMENT_ROLLUP"
        const val ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
        const val ACTION_RUN_NOTIF_ENGAGEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIF_ENGAGEMENT_ROLLUP"

        const val ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP"

        const val ACTION_RUN_NOTIFICATION_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_VALIDATION"
        const val ACTION_RUN_REDCAP_UPLOAD = "com.nick.myrecoverytracker.ACTION_RUN_REDCAP_UPLOAD"
        const val ACTION_RUN_UNLOCK_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_ROLLUP"
        const val ACTION_RUN_LNS_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP"

        const val ACTION_RUN_USAGE_EVENTS_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_EVENTS_DAILY"

        const val ACTION_RUN_APP_CATEGORY_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_APP_CATEGORY_DAILY"

        const val ACTION_RUN_APP_SWITCHING_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_APP_SWITCHING_DAILY"
    }
}