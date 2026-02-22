package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SleepDurationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val sleepDir = File(applicationContext.filesDir, "sleep")
            if (!sleepDir.exists()) sleepDir.mkdirs()

            val outputFile = File(sleepDir, "daily_sleep_duration.txt")
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ISO_DATE
            val sleepDuration = 480 // placeholder minutes

            outputFile.appendText("${today.format(formatter)},$sleepDuration\n")

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}