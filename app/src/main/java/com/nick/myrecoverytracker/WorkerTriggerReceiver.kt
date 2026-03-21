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
            "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP" to NotificationEngagementWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP" to NotificationLatencyWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_USAGE_ACCESS_DIAG_WORKER" to UsageAccessDiagWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_DISTANCE_SUMMARY_WORKER" to DistanceSummaryWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_DAILY_SUMMARY" to DailySummaryWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_LOG_EXPORT_WORKER" to LogExportWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_INTENSITY_DAILY_WORKER" to MovementIntensityDailyWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_USAGE_ENTROPY_DAILY_WORKER" to UsageEntropyDailyWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_HEALTH_SNAPSHOT" to HealthSnapshotWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_VALIDATION_WORKER" to UnlockValidationWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_USAGE_CAPTURE_WORKER" to UsageCaptureWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_VALIDATION_WORKER" to NotificationValidationWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_LATE_NIGHT_SCREEN_ROLLUP_WORKER" to LateNightScreenRollupWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_USAGE_EVENTS_DUMP_WORKER" to UsageEventsDumpWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_SERVICE_HEALTH_CHECK_WORKER" to ServiceHealthCheckWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_REDCAP_UPLOAD_WORKER" to RedcapUploadWorker::class.java,
            "com.nick.myrecoverytracker.ACTION_RUN_REDCAP_UPLOAD" to RedcapUploadWorker::class.java
        )

        workerMap[action]?.let { workerClass ->
            val request = OneTimeWorkRequest.Builder(workerClass)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag(action)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(action, ExistingWorkPolicy.REPLACE, request)
            Log.i("WorkerTriggerReceiver", "Enqueued $action")
        } ?: Log.w("WorkerTriggerReceiver", "No worker mapping for action=$action")
    }
}