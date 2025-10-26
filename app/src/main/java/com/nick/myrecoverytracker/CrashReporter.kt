package com.nick.myrecoverytracker

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class CrashReporter : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                    .format(java.util.Date())
                val f = File(filesDir, "crash_$ts.txt")
                f.writeText("${e.javaClass.name}: ${e.message}\n${e.stackTrace.joinToString("\n")}")
            } catch (_: Throwable) {
            } finally {
                previous?.uncaughtException(t, e)
            }
        }

        val entropyReq =
            PeriodicWorkRequestBuilder<UsageEntropyDailyWorker>(1, TimeUnit.DAYS)
                .addTag("UsageEntropyDaily")
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily-usage-entropy",
            ExistingPeriodicWorkPolicy.UPDATE,
            entropyReq
        )

        Reschedule.runIfNeededOnAppStart(applicationContext)
    }
}