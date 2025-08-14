package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MovementIntensityWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val tag = "MovementIntensityWorker"
        val ctx = applicationContext
        val day = today()
        val src = File(ctx.filesDir, "movement_log.csv")
        val out = File(ctx.filesDir, "daily_movement_intensity.csv")

        try {
            val values = if (src.exists()) {
                src.readLines()
                    .drop(1) // header
                    .mapNotNull { line ->
                        val parts = line.split(",")
                        if (parts.size >= 2 && parts[0].startsWith(day)) {
                            parts[1].toDoubleOrNull()
                        } else null
                    }
            } else emptyList()

            val avg = if (values.isNotEmpty()) values.average() else 0.0
            writeDay(out, day, avg)
            Log.i(tag, "Daily movement intensity $day = $avg  (n=${values.size})")
            Result.success()
        } catch (t: Throwable) {
            Log.e(tag, "Failed to compute daily movement intensity", t)
            Result.retry()
        }
    }

    private fun writeDay(out: File, day: String, value: Double) {
        val rows = if (out.exists()) out.readLines().filterNot { it.startsWith("$day,") }.toMutableList()
        else mutableListOf("date,intensity")
        rows += "$day,$value"
        out.writeText(rows.joinToString("\n") + "\n")
    }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}