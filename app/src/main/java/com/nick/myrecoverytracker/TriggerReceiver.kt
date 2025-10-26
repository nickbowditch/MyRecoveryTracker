// app/src/main/java/com/nick/myrecoverytracker/TriggerReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager

class TriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i("TriggerReceiver", "onReceive action=$action")

        when (action) {
            ACTION_REREGISTER_WORKERS -> {
                val wm = WorkManager.getInstance(context)
                wm.cancelUniqueWork("mrt_notification_daily")
                wm.cancelAllWorkByTag("NotificationEngagementDaily")
                wm.pruneWork()
                WorkScheduler.registerAllDaily(context)
                Log.i("TriggerReceiver", "registerAllDaily() invoked")
            }

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

                val latencyReq = OneTimeWorkRequestBuilder<NotificationLatencyWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag("NotificationLatency")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, latencyReq)
            }

            // ⬇️ NEW: explicitly map both engagement actions to the worker
            ACTION_RUN_NOTIFICATION_ROLLUP,
            ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP -> {
                enqueueOnce<NotificationEngagementWorker>(
                    context,
                    "once-NotificationEngagementRollup",
                    "NotificationEngagement"
                )
            }

            ACTION_RUN_DISTANCE_DAILY -> enqueueOnce<DistanceWorker>(context, "once-DistanceDaily", "DistanceDaily")
            ACTION_RUN_SLEEP_DURATION_DAILY -> enqueueOnce<SleepDurationWorker>(context, "once-SleepDurationDaily", "SleepDurationDaily")
            ACTION_RUN_DAILY_SUMMARY -> enqueueOnce<DailySummaryWorker>(context, "once-DailySummary", "DailySummary")
            ACTION_RUN_LOG_EXPORT -> enqueueOnce<LogExportWorker>(context, "once-LogExport", "LogExport")
            ACTION_RUN_LOG_RETENTION -> enqueueOnce<LogRetentionWorker>(context, "once-LogRetention", "LogRetention")
            ACTION_RUN_UNLOCK_ROLLUP -> enqueueOnce<UnlockRollupWorker>(context, "once-UnlockRollup", "UnlockRollup")
            ACTION_RUN_MOVEMENT_ROLLUP -> enqueueOnce<MovementRollupWorker>(context, "once-MovementRollup", "MovementRollup")
            ACTION_RUN_MOVEMENT_INTENSITY -> enqueueOnce<MovementIntensityDailyWorker>(context, "once-MovementIntensity", "MovementIntensity")
            ACTION_RUN_UNLOCK_SCAN -> enqueueOnce<UnlockWorker>(context, "once-UnlockScan", "UnlockScan")
            ACTION_RUN_USAGE_ENTROPY -> enqueueOnce<UsageEntropyDailyWorker>(context, "once-UsageEntropy", "UsageEntropy")
            ACTION_RUN_HEALTH_SNAPSHOT -> enqueueOnce<HealthSnapshotWorker>(context, "once-HealthSnapshot", "HealthSnapshot")
            ACTION_RUN_SLEEP_ROLLUP -> enqueueOnce<SleepRollupWorker>(context, "once-SleepRollup", "SleepRollup")
            ACTION_RUN_SLEEP_VALIDATION -> enqueueOnce<SleepValidationWorker>(context, "once-SleepValidation", "SleepValidation")
            ACTION_RUN_UNLOCK_VALIDATION -> enqueueOnce<UnlockValidationWorker>(context, "once-UnlockValidation", "UnlockValidation")
            ACTION_RUN_USAGE_CAPTURE -> enqueueOnce<UsageCaptureWorker>(context, "once-UsageCapture", "UsageCapture")
            ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP -> enqueueOnce<NotificationLatencyWorker>(context, "once-NotificationLatencyRollup", "NotificationLatency")
            ACTION_RUN_NOTIFICATION_VALIDATION -> enqueueOnce<NotificationValidationWorker>(context, "once-NotificationValidation", "NotificationValidation")
            ACTION_RUN_REDCAP_UPLOAD -> enqueueOnce<RedcapUploadWorker>(context, "once-RedcapUpload", "RedcapUpload")
            ACTION_RUN_LNS_ROLLUP -> enqueueOnce<LateNightScreenRollupWorker>(context, "once-LateNightRollup", "LateNightRollup")
            ACTION_RUN_USAGE_EVENTS_DAILY -> enqueueOnce<UsageEventsDailyWorker>(context, "once-UsageEventsDaily", "UsageEventsDaily")
            ACTION_RUN_APP_CATEGORY_DAILY -> enqueueOnce<AppUsageByCategoryDailyWorker>(context, "once-AppUsageCategoryDaily", "AppUsageCategoryDaily")
            ACTION_RUN_APP_SWITCHING_DAILY -> enqueueOnce<AppSwitchingDailyWorker>(context, "once-AppSwitchingDaily", "AppSwitchingDaily")
        }
    }

    private inline fun <reified W : ListenableWorker> enqueueOnce(
        context: Context,
        uniqueName: String,
        tag: String
    ) {
        val req = OneTimeWorkRequestBuilder<W>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(tag)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, req)
        Log.i("TriggerReceiver", "Enqueued $tag ($uniqueName)")
    }

    companion object {
        const val ACTION_REREGISTER_WORKERS = "com.nick.myrecoverytracker.ACTION_REREGISTER_WORKERS"
        const val ACTION_RUN_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ROLLUPS"
        const val ACTION_RUN_ALL_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ALL_ROLLUPS"
        const val ACTION_RUN_DISTANCE_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_DISTANCE_DAILY"
        const val ACTION_RUN_SLEEP_DURATION_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_SLEEP_DURATION_DAILY"
        const val ACTION_RUN_DAILY_SUMMARY = "com.nick.myrecoverytracker.ACTION_RUN_DAILY_SUMMARY"
        const val ACTION_RUN_LOG_EXPORT = "com.nick.myrecoverytracker.ACTION_RUN_LOG_EXPORT"
        const val ACTION_RUN_LOG_RETENTION = "com.nick.myrecoverytracker.ACTION_RUN_LOG_RETENTION"
        const val ACTION_RUN_MOVEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_ROLLUP"
        const val ACTION_RUN_MOVEMENT_INTENSITY = "com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_INTENSITY"
        const val ACTION_RUN_UNLOCK_SCAN = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_SCAN"
        const val ACTION_RUN_USAGE_ENTROPY = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_ENTROPY"
        const val ACTION_RUN_HEALTH_SNAPSHOT = "com.nick.myrecoverytracker.ACTION_RUN_HEALTH_SNAPSHOT"
        const val ACTION_RUN_SLEEP_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_SLEEP_ROLLUP"
        const val ACTION_RUN_SLEEP_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_SLEEP_VALIDATION"
        const val ACTION_RUN_UNLOCK_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_VALIDATION"
        const val ACTION_RUN_USAGE_CAPTURE = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_CAPTURE"
        const val ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP"
        const val ACTION_RUN_NOTIFICATION_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_VALIDATION"
        const val ACTION_RUN_REDCAP_UPLOAD = "com.nick.myrecoverytracker.ACTION_RUN_REDCAP_UPLOAD"
        const val ACTION_RUN_UNLOCK_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_ROLLUP"
        const val ACTION_RUN_LNS_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP"
        const val ACTION_RUN_USAGE_EVENTS_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_EVENTS_DAILY"
        const val ACTION_RUN_APP_CATEGORY_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_APP_CATEGORY_DAILY"
        const val ACTION_RUN_APP_SWITCHING_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_APP_SWITCHING_DAILY"

        // ⬇️ NEW: constants for the two actions you’re broadcasting
        const val ACTION_RUN_NOTIFICATION_ROLLUP =
            "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP"
        const val ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP =
            "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
    }
}