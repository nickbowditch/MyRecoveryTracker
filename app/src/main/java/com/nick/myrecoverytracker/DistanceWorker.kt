package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class DistanceWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        try {
            val context = applicationContext
            val logFile = File(context.filesDir, "location_log.csv")

            if (!logFile.exists()) {
                Log.w("DistanceWorker", "📂 No location_log.csv found")
                return Result.success()
            }

            val lines = logFile.readLines()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            val todaysPoints = lines
                .filter { it.startsWith(today) }
                .mapNotNull { parseLocationLine(it) }

            if (todaysPoints.size < 2) {
                Log.w("DistanceWorker", "📉 Not enough points to calculate distance")
                return Result.success()
            }

            var totalDistanceKm = 0.0
            for (i in 0 until todaysPoints.lastIndex) {
                val (lat1, lon1) = todaysPoints[i]
                val (lat2, lon2) = todaysPoints[i + 1]
                totalDistanceKm += haversine(lat1, lon1, lat2, lon2)
            }

            saveDailyDistance(context, today, totalDistanceKm)

            Log.i("DistanceWorker", "📏 Total distance for $today: ${"%.2f".format(totalDistanceKm)} km")

        } catch (e: Exception) {
            Log.e("DistanceWorker", "❌ Error computing distance", e)
            return Result.failure()
        }

        return Result.success()
    }

    private fun parseLocationLine(line: String): Pair<Double, Double>? {
        val parts = line.split(",")
        return if (parts.size >= 3) {
            val lat = parts[1].toDoubleOrNull()
            val lon = parts[2].toDoubleOrNull()
            if (lat != null && lon != null) Pair(lat, lon) else null
        } else null
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun saveDailyDistance(context: Context, date: String, distanceKm: Double) {
        val file = File(context.filesDir, "daily_distance_log.csv")
        val writer = FileWriter(file, true)
        writer.appendLine("$date,${"%.3f".format(distanceKm)}")
        writer.flush()
        writer.close()
    }
}