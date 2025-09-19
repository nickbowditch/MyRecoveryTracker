// app/src/main/java/com/nick/myrecoverytracker/CrashReporter.kt
package com.nick.myrecoverytracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class CrashReporter : Application() {
    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(java.util.Date())
                val f = File(filesDir, "crash_$ts.txt")
                f.writeText("${e.javaClass.name}: ${e.message}\n${e.stackTrace.joinToString("\n")}")
            } catch (_: Throwable) {}
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(t, e)
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
    }
}