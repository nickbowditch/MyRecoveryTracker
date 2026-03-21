package com.nick.myrecoverytracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.i("MainActivity", "POST_NOTIFICATIONS permission result: $isGranted")
        // Continue with startup regardless
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock app to portrait orientation
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Do NOT initialize WorkManager here — it's already initialized in MainApplication.onCreate()

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

        finish()
    }

    override fun onStart() {
        super.onStart()
        ServiceStarter.startAllIfAllowed(this)
        Log.i("MainActivity", "ServiceStarter.startAllIfAllowed invoked")
    }
}