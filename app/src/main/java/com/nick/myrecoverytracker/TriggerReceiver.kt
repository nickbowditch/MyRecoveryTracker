// app/src/main/java/com/nick/myrecoverytracker/TriggerReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import java.io.File

/**
 * TriggerReceiver - Handles core app actions (rollups, reregister, dumps, copy)
 *
 * IMPORTANT: Individual worker triggers (ACTION_RUN_DAILY_SUMMARY, etc.) are handled by
 * WorkerTriggerReceiver due to Android's intent-filter action limit (~10 actions per receiver).
 *
 * See: WorkerTriggerReceiver.kt
 */
class TriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i("TriggerReceiver", "onReceive action=$action")

        when (action) {
            ACTION_REREGISTER_WORKERS -> {
                WorkManager.getInstance(context).cancelUniqueWork("mrt_notification_daily")
                WorkScheduler.registerAllWork(context)
                WorkScheduler.scheduleServiceHealthCheck(context)
            }

            ACTION_RUN_ROLLUPS, ACTION_RUN_ALL_ROLLUPS -> {
                UnlockMigrations.run(context)

                enqueueOnce<DailySummaryWorker>(context, "DailySummary", "dailySummary")
                // ACTION_RUN_UNLOCK_SCAN removed - UnlockWorker deleted (ForegroundUnlockService handles unlock_log.csv; daily_unlocks.csv deprecated)
                enqueueOnce<RedcapUploadWorker>(context, "RedcapUpload", "redcapUpload")
            }

            ACTION_DUMP_HEARTBEAT -> {
                try {
                    val f = File(context.filesDir, "heartbeat.csv")
                    if (f.exists()) {
                        val lines = f.readLines().takeLast(10)
                        lines.forEach { Log.i("HEARTBEAT_DUMP", it) }
                        Log.i("HEARTBEAT_DUMP", "Total rows: ${f.readLines().size}")
                    } else {
                        Log.e("HEARTBEAT_DUMP", "File does not exist")
                    }
                } catch (e: Exception) {
                    Log.e("HEARTBEAT_DUMP", "Error reading heartbeat", e)
                }
            }

            ACTION_DUMP_LOG_EXPORT -> {
                try {
                    val f = File(context.filesDir, "log_export.csv")
                    if (f.exists()) {
                        val lines = f.readLines()
                        lines.forEach { Log.i("LOG_EXPORT_DUMP", it) }
                        Log.i("LOG_EXPORT_DUMP", "Total rows: ${lines.size}")
                    } else {
                        Log.e("LOG_EXPORT_DUMP", "File does not exist")
                    }
                } catch (e: Exception) {
                    Log.e("LOG_EXPORT_DUMP", "Error reading log_export", e)
                }
            }

            ACTION_DUMP_LOG_RETENTION -> {
                try {
                    val f = File(context.filesDir, "log_retention.csv")
                    if (f.exists()) {
                        val lines = f.readLines()
                        lines.forEach { Log.i("LOG_RETENTION_DUMP", it) }
                        Log.i("LOG_RETENTION_DUMP", "Total rows: ${lines.size}")
                    } else {
                        Log.e("LOG_RETENTION_DUMP", "File does not exist")
                    }
                } catch (e: Exception) {
                    Log.e("LOG_RETENTION_DUMP", "Error reading log_retention", e)
                }
            }

            ACTION_COPY_EXPORT_ZIP -> {
                try {
                    val src = File(context.filesDir, "export_logs.zip")
                    val dest = File("/sdcard/Download/export_logs.zip")
                    if (src.exists()) {
                        src.copyTo(dest, overwrite = true)
                        Log.i("COPY_EXPORT_ZIP", "Copied to ${dest.absolutePath}, size=${dest.length()}")
                    } else {
                        Log.e("COPY_EXPORT_ZIP", "Source file does not exist")
                    }
                } catch (e: Exception) {
                    Log.e("COPY_EXPORT_ZIP", "Error copying export_logs.zip", e)
                }
            }

            else -> {
                // Forward all unknown actions to WorkerTriggerReceiver
                Log.i("TriggerReceiver", "Forwarding to WorkerTriggerReceiver: $action")
                WorkerTriggerReceiver().onReceive(context, intent)
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

    companion object {
        const val ACTION_REREGISTER_WORKERS = "com.nick.myrecoverytracker.ACTION_REREGISTER_WORKERS"
        const val ACTION_RUN_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ROLLUPS"
        const val ACTION_RUN_ALL_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ALL_ROLLUPS"
        // ACTION_RUN_DISTANCE_DAILY removed - DistanceWorker deleted
        const val ACTION_RUN_DISTANCE_SUMMARY = "com.nick.myrecoverytracker.ACTION_RUN_DISTANCE_SUMMARY"
        const val ACTION_RUN_NOTIFICATION_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP"
        const val ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP"
        const val ACTION_RUN_USAGE_DIAG = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_DIAG"
        const val ACTION_RUN_DAILY_SUMMARY = "com.nick.myrecoverytracker.ACTION_RUN_DAILY_SUMMARY"
        const val ACTION_RUN_LOG_EXPORT = "com.nick.myrecoverytracker.ACTION_RUN_LOG_EXPORT"
        const val ACTION_RUN_LOG_RETENTION = "com.nick.myrecoverytracker.ACTION_RUN_LOG_RETENTION"
        const val ACTION_RUN_MOVEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_ROLLUP"
        const val ACTION_RUN_MOVEMENT_INTENSITY = "com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_INTENSITY"
        // ACTION_RUN_UNLOCK_SCAN removed - UnlockWorker deleted (ForegroundUnlockService handles unlock_log.csv; daily_unlocks.csv deprecated)
        const val ACTION_RUN_USAGE_ENTROPY = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_ENTROPY"
        const val ACTION_RUN_HEALTH_SNAPSHOT = "com.nick.myrecoverytracker.ACTION_RUN_HEALTH_SNAPSHOT"
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
        const val ACTION_DUMP_HEARTBEAT = "com.nick.myrecoverytracker.ACTION_DUMP_HEARTBEAT"
        const val ACTION_DUMP_LOG_EXPORT = "com.nick.myrecoverytracker.ACTION_DUMP_LOG_EXPORT"
        const val ACTION_DUMP_LOG_RETENTION = "com.nick.myrecoverytracker.ACTION_DUMP_LOG_RETENTION"
        const val ACTION_COPY_EXPORT_ZIP = "com.nick.myrecoverytracker.ACTION_COPY_EXPORT_ZIP"
    }
}