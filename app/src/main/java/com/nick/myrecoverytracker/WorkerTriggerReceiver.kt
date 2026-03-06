// app/src/main/java/com/nick/myrecoverytracker/WorkerTriggerReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*

class WorkerTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i("WorkerTriggerReceiver", "onReceive action=$action")

        val workerMap: Map<String, Class<out ListenableWorker>> = mapOf(
            TriggerReceiver.ACTION_RUN_NOTIFICATION_ROLLUP to NotificationEngagementWorker::class.java,
            TriggerReceiver.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP to NotificationEngagementWorker::class.java,
            TriggerReceiver.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP to NotificationLatencyWorker::class.java,
            TriggerReceiver.ACTION_RUN_USAGE_DIAG to UsageAccessDiagWorker::class.java,
            TriggerReceiver.ACTION_RUN_DISTANCE_SUMMARY to DistanceSummaryWorker::class.java,
            TriggerReceiver.ACTION_RUN_DAILY_SUMMARY to DailySummaryWorker::class.java,
            TriggerReceiver.ACTION_RUN_LOG_EXPORT to LogExportWorker::class.java,
            TriggerReceiver.ACTION_RUN_MOVEMENT_INTENSITY to MovementIntensityDailyWorker::class.java,
            TriggerReceiver.ACTION_RUN_USAGE_ENTROPY to UsageEntropyDailyWorker::class.java,
            TriggerReceiver.ACTION_RUN_HEALTH_SNAPSHOT to HealthSnapshotWorker::class.java,
            TriggerReceiver.ACTION_RUN_UNLOCK_VALIDATION to UnlockValidationWorker::class.java,
            TriggerReceiver.ACTION_RUN_USAGE_CAPTURE to UsageCaptureWorker::class.java,
            TriggerReceiver.ACTION_RUN_NOTIFICATION_VALIDATION to NotificationValidationWorker::class.java,
            TriggerReceiver.ACTION_RUN_REDCAP_UPLOAD to RedcapUploadWorker::class.java,
            TriggerReceiver.ACTION_RUN_LNS_ROLLUP to LateNightScreenRollupWorker::class.java,
            TriggerReceiver.ACTION_RUN_USAGE_EVENTS_DAILY to UsageEventsDailyWorker::class.java
        )

        workerMap[action]?.let { workerClass ->
            val request = OneTimeWorkRequest.Builder(workerClass)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag(action)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(action, ExistingWorkPolicy.REPLACE, request)
            Log.i("WorkerTriggerReceiver", "Enqueued $action")
        }
    }
}