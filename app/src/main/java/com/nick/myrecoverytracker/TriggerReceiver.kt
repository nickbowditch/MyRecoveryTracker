package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*

class TriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i("TriggerReceiver", "onReceive action=$action")

        when (action) {
            ACTION_REREGISTER_WORKERS -> {
                WorkManager.getInstance(context).cancelUniqueWork("mrt_notification_daily")
                WorkScheduler.registerAllDaily(context)
            }

            ACTION_RUN_SLEEP_DURATION_DAILY -> {
                val request = OneTimeWorkRequestBuilder<SleepDurationWorker>().build()
                WorkManager.getInstance(context).enqueue(request)
                Log.i("TriggerReceiver", "Enqueued SleepDurationWorker")
            }

            ACTION_RUN_ROLLUPS, ACTION_RUN_ALL_ROLLUPS -> {
                UnlockMigrations.run(context)

                enqueueOnce<DailySummaryWorker>(context, "DailySummary", "dailySummary")
                enqueueOnce<UnlockWorker>(context, "UnlockScan", "unlockScan")
                enqueueOnce<SleepRollupWorker>(context, "SleepRollup", "sleepRollup")
                enqueueOnce<RedcapUploadWorker>(context, "RedcapUpload", "redcapUpload")
                enqueueOnce<NotificationLatencyWorker>(context, "NotificationLatency", "latency")
            }

            else -> {
                val workerMap: Map<String, Class<out ListenableWorker>> = mapOf(
                    ACTION_RUN_NOTIFICATION_ROLLUP to NotificationEngagementWorker::class.java,
                    ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP to NotificationEngagementWorker::class.java,
                    ACTION_RUN_USAGE_DIAG to UsageAccessDiagWorker::class.java,
                    ACTION_RUN_DISTANCE_DAILY to DistanceWorker::class.java,
                    ACTION_RUN_DAILY_SUMMARY to DailySummaryWorker::class.java,
                    ACTION_RUN_LOG_EXPORT to LogExportWorker::class.java,
                    ACTION_RUN_LOG_RETENTION to LogRetentionWorker::class.java,
                    ACTION_RUN_UNLOCK_ROLLUP to UnlockRollupWorker::class.java,
                    ACTION_RUN_MOVEMENT_ROLLUP to MovementRollupWorker::class.java,
                    ACTION_RUN_MOVEMENT_INTENSITY to MovementIntensityDailyWorker::class.java,
                    ACTION_RUN_UNLOCK_SCAN to UnlockWorker::class.java,
                    ACTION_RUN_USAGE_ENTROPY to UsageEntropyDailyWorker::class.java,
                    ACTION_RUN_HEALTH_SNAPSHOT to HealthSnapshotWorker::class.java,
                    ACTION_RUN_SLEEP_ROLLUP to SleepRollupWorker::class.java,
                    ACTION_RUN_SLEEP_VALIDATION to SleepValidationWorker::class.java,
                    ACTION_RUN_UNLOCK_VALIDATION to UnlockValidationWorker::class.java,
                    ACTION_RUN_USAGE_CAPTURE to UsageCaptureWorker::class.java,
                    ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP to NotificationLatencyWorker::class.java,
                    ACTION_RUN_NOTIFICATION_VALIDATION to NotificationValidationWorker::class.java,
                    ACTION_RUN_REDCAP_UPLOAD to RedcapUploadWorker::class.java,
                    ACTION_RUN_LNS_ROLLUP to LateNightScreenRollupWorker::class.java,
                    ACTION_RUN_USAGE_EVENTS_DAILY to UsageEventsDailyWorker::class.java,
                    ACTION_RUN_APP_CATEGORY_DAILY to AppUsageByCategoryDailyWorker::class.java,
                    ACTION_RUN_APP_SWITCHING_DAILY to AppSwitchingDailyWorker::class.java,
                    ACTION_RUN_APP_STARTS_DAILY to DailyAppStartsByPackageWorker::class.java
                )

                workerMap[action]?.let { enqueueOnceClass(context, it, action, action) }
            }
        }
    }

    private inline fun <reified W : ListenableWorker> enqueueOnce(
        context: Context,
        tag: String,
        uniqueName: String
    ) {
        val request = OneTimeWorkRequest.Builder(W::class.java)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(tag)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
        Log.i("TriggerReceiver", "Enqueued $tag ($uniqueName)")
    }

    private fun enqueueOnceClass(
        context: Context,
        workerClass: Class<out ListenableWorker>,
        tag: String,
        uniqueName: String
    ) {
        val request = OneTimeWorkRequest.Builder(workerClass)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(tag)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
        Log.i("TriggerReceiver", "Enqueued $tag ($uniqueName)")
    }

    companion object {
        const val ACTION_REREGISTER_WORKERS = "com.nick.myrecoverytracker.ACTION_REREGISTER_WORKERS"
        const val ACTION_RUN_SLEEP_DURATION_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_SLEEP_DURATION_DAILY"
        const val ACTION_RUN_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ROLLUPS"
        const val ACTION_RUN_ALL_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ALL_ROLLUPS"
        const val ACTION_RUN_DISTANCE_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_DISTANCE_DAILY"
        const val ACTION_RUN_NOTIFICATION_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP"
        const val ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
        const val ACTION_RUN_USAGE_DIAG = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_DIAG"
        const val ACTION_RUN_DAILY_SUMMARY = "com.nick.myrecoverytracker.ACTION_RUN_DAILY_SUMMARY"
        const val ACTION_RUN_LOG_EXPORT = "com.nick.myrecoverytracker.ACTION_RUN_LOG_EXPORT"
        const val ACTION_RUN_LOG_RETENTION = "com.nick.myrecoverytracker.ACTION_RUN_LOG_RETENTION"
        const val ACTION_RUN_UNLOCK_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_ROLLUP"
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
        const val ACTION_RUN_LNS_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP"
        const val ACTION_RUN_USAGE_EVENTS_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_EVENTS_DAILY"
        const val ACTION_RUN_APP_CATEGORY_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_APP_CATEGORY_DAILY"
        const val ACTION_RUN_APP_SWITCHING_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_APP_SWITCHING_DAILY"
        const val ACTION_RUN_APP_STARTS_DAILY = "com.nick.myrecoverytracker.ACTION_RUN_APP_STARTS_DAILY"
    }
}