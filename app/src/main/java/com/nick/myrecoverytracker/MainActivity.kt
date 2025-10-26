// app/src/main/java/com/nick/myrecoverytracker/MainActivity.kt
package com.nick.myrecoverytracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
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

        val wm = WorkManager.getInstance(this)

        val fgIntent = Intent(this, ForegroundUnlockService::class.java)
        ContextCompat.startForegroundService(this, fgIntent)
        Log.i("MainActivity", "Requested ForegroundUnlockService start")

        val locIntent = Intent(this, LocationCaptureService::class.java)
        ContextCompat.startForegroundService(this, locIntent)
        Log.i("MainActivity", "Requested LocationCaptureService start")

        HeartbeatWorker.ensure(this)

        try {
            RedcapDiag.log(this)
            Log.i("MainActivity", "RedcapDiag.log executed on launch")
        } catch (e: Exception) {
            Log.e("MainActivity", "RedcapDiag.log failed", e)
        }

        val periodicUsage = PeriodicWorkRequestBuilder<UsageCaptureWorker>(24, TimeUnit.HOURS)
            .addTag("UsageCaptureDaily")
            .build()
        wm.enqueueUniquePeriodicWork(
            "mrt_usage_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicUsage
        )

        val distancePeriodic = PeriodicWorkRequestBuilder<DistanceWorker>(24, TimeUnit.HOURS)
            .addTag("DistanceDaily")
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

        // Align name with TriggerReceiver's cancelUniqueWork("mrt_notification_daily")
        val notifPeriodic = PeriodicWorkRequestBuilder<NotificationEngagementWorker>(24, TimeUnit.HOURS)
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
        Log.i("MainActivity", "Enqueued NotificationEngagementWorker (24h periodic)")

        val distanceKick = OneTimeWorkRequestBuilder<DistanceWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("DistanceKick")
            .build()
        wm.enqueueUniqueWork(
            "once-DistanceKick",
            ExistingWorkPolicy.REPLACE,
            distanceKick
        )

        try {
            val distanceWork = PeriodicWorkRequestBuilder<DistanceWorker>(15, TimeUnit.MINUTES)
                .addTag("DistanceAggregator")
                .build()
            wm.enqueueUniquePeriodicWork(
                "mrt_distance_agg",
                ExistingPeriodicWorkPolicy.UPDATE,
                distanceWork
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to enqueue DistanceWorker", e)
        }

        if (BuildConfig.DEBUG) {
            try {
                DistanceRepair.recalcToday(this)
            } catch (e: Exception) {
                Log.e("MainActivity", "DistanceRepair failed", e)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
    }
}