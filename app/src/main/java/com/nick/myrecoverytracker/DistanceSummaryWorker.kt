package com.nick.myrecoverytracker

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DistanceSummaryWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ctx = applicationContext
            val file = File(ctx.filesDir, "location_log.csv")
            if (!file.exists()) {
                Log.w(TAG, "location_log.csv missing; writing 0 km")
                write(ctx, today(), 0f); return@withContext Result.success()
            }

            val day = today()
            val points = file.readLines().mapNotNull { line ->
                // Format: "YYYY-MM-DD HH:mm:ss,lat,lon,acc"
                val parts = line.split(",")
                if (parts.size >= 3 && parts[0].startsWith(day)) {
                    val lat = parts[1].toDoubleOrNull()
                    val lon = parts[2].toDoubleOrNull()
                    if (lat != null && lon != null) Location("").apply { latitude = lat; longitude = lon } else null
                } else null
            }

            if (points.size < 2) { write(ctx, day, 0f); return@withContext Result.success() }

            var meters = 0f
            for (i in 1 until points.size) meters += points[i - 1].distanceTo(points[i])
            val km = meters / 1000f
            write(ctx, day, km)
            Log.i(TAG, "Distance($day) = ${"%.2f".format(km)} km")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "DistanceSummaryWorker failed", t)
            Result.retry()
        }
    }

    private fun write(ctx: Context, day: String, km: Float) {
        // reuse your existing saver if present; else write here
        MetricsStore(ctx).save(day, km)
    }

    private fun MetricsStore(ctx: Context) = object {
        private val f = File(ctx.filesDir, "daily_distance_log.csv")
        fun save(day: String, km: Float) {
            val lines = if (f.exists()) f.readLines().filterNot { it.startsWith("$day,") }.toMutableList() else mutableListOf()
            lines += "$day,$km"
            f.writeText(lines.joinToString("\n") + "\n")
        }
    }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    companion object { private const val TAG = "DistanceSummaryWorker" }
}