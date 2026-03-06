package com.nick.myrecoverytracker

import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filter = IntentFilter().apply {
            addAction("com.nick.myrecoverytracker.ACTION_RUN_REDCAP_UPLOAD")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_LOG_EXPORT")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_DISTANCE_SUMMARY")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_USAGE_CAPTURE")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_DAILY_SUMMARY")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_INTENSITY")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_USAGE_ENTROPY")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_HEALTH_SNAPSHOT")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_VALIDATION")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_VALIDATION")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_USAGE_EVENTS_DAILY")
            addAction("com.nick.myrecoverytracker.ACTION_RUN_USAGE_DIAG")
        }
        registerReceiver(WorkerTriggerReceiver(), filter, Context.RECEIVER_EXPORTED)
        Log.i("MainActivity", "WorkerTriggerReceiver dynamically registered")

        val wm = WorkManager.getInstance(this)

        if (BuildConfig.DEBUG) {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    ServiceStarter.startAllIfAllowed(this)
                    Log.i("MainActivity", "ServiceStarter.startAllIfAllowed (DEBUG)")
                } catch (e: Exception) {
                    Log.e("MainActivity", "ServiceStarter failed", e)
                }
            }, 2000)
        }

        try {
            RedcapDiag.log(this)
            Log.i("MainActivity", "RedcapDiag.log executed on launch")
        } catch (e: Exception) {
            Log.e("MainActivity", "RedcapDiag.log failed", e)
        }

        val exportWork = OneTimeWorkRequestBuilder<LogExportWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("LogExportKick")
            .build()

        wm.enqueueUniqueWork(
            "once-LogExportKick",
            ExistingWorkPolicy.REPLACE,
            exportWork
        )

        val redcapWork = OneTimeWorkRequestBuilder<RedcapUploadWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("RedcapUploadKick")
            .build()

        wm.enqueueUniqueWork(
            "once-RedcapUploadKick",
            ExistingWorkPolicy.REPLACE,
            redcapWork
        )
        Log.i("MainActivity", "RedcapUploadWorker enqueued for QA")

        val periodicUsage =
            PeriodicWorkRequestBuilder<UsageCaptureWorker>(24, TimeUnit.HOURS)
                .addTag("UsageCaptureDaily")
                .build()

        wm.enqueueUniquePeriodicWork(
            "mrt_usage_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicUsage
        )

        val distancePeriodic =
            PeriodicWorkRequestBuilder<DistanceSummaryWorker>(24, TimeUnit.HOURS)
                .addTag("DistanceSummaryDaily")
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

        wm.enqueueUniquePeriodicWork(
            "mrt_distance_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            distancePeriodic
        )

        val notifPeriodic =
            PeriodicWorkRequestBuilder<NotificationEngagementWorker>(24, TimeUnit.HOURS)
                .addTag("NotificationEngagementDaily")
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

        wm.enqueueUniquePeriodicWork(
            "mrt_notification_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            notifPeriodic
        )

        val distanceKick =
            OneTimeWorkRequestBuilder<DistanceSummaryWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag("DistanceSummaryKick")
                .build()

        wm.enqueueUniqueWork(
            "once-DistanceSummaryKick",
            ExistingWorkPolicy.REPLACE,
            distanceKick
        )
    }

    override fun onStart() {
        super.onStart()
        ServiceStarter.startAllIfAllowed(this)
        Log.i("MainActivity", "ServiceStarter.startAllIfAllowed invoked")
    }
}