// app/src/main/java/com/nick/myrecoverytracker/MainActivity.kt
package com.nick.myrecoverytracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fgIntent = Intent(this, ForegroundUnlockService::class.java)
        ContextCompat.startForegroundService(this, fgIntent)
        Log.i("MainActivity", "Requested ForegroundUnlockService start")

        HeartbeatWorker.ensure(this)
        Log.i("MainActivity", "HeartbeatWorker.ensure invoked")

        // Idempotent safety: ensure periodic usage capture exists
        val periodicUsage = PeriodicWorkRequestBuilder<UsageCaptureWorker>(
            24, TimeUnit.HOURS
        ).addTag("UsageCaptureDaily").build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "mrt_usage_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicUsage
        )

        finish()
    }
}