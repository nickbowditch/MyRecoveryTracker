package com.nick.myrecoverytracker

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

        val wm = WorkManager.getInstance(this)

        // DEBUG-only delayed startup hook (NO direct service start)
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

        HeartbeatWorker.ensure(this)

        try {
            RedcapDiag.log(this)
            Log.i("MainActivity", "RedcapDiag.log executed on launch")
        } catch (e: Exception) {
            Log.e("MainActivity", "RedcapDiag.log failed", e)
        }

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
            PeriodicWorkRequestBuilder<DistanceWorker>(24, TimeUnit.HOURS)
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
            OneTimeWorkRequestBuilder<DistanceWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag("DistanceKick")
                .build()

        wm.enqueueUniqueWork(
            "once-DistanceKick",
            ExistingWorkPolicy.REPLACE,
            distanceKick
        )

        if (BuildConfig.DEBUG) {
            try {
                DistanceRepair.recalcToday(this)
            } catch (e: Exception) {
                Log.e("MainActivity", "DistanceRepair failed", e)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000)
    }

    override fun onStart() {
        super.onStart()
        ServiceStarter.startAllIfAllowed(this)
        Log.i("MainActivity", "ServiceStarter.startAllIfAllowed invoked")
    }
}