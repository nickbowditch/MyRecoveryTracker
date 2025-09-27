package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DistanceWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            val context = applicationContext
            val locFile = File(context.filesDir, "location_log.csv")
            if (!locFile.exists()) {
                Log.w(TAG, "location_log.csv not found")
                Result.success()
            } else {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val points = locFile.readLines()
                    .asSequence()
                    .filter { it.isNotBlank() && it[0].isDigit() } // skip headers/garbage
                    .filter { it.startsWith(today) }
                    .mapNotNull { parseLatLon(it) }
                    .toList()

                if (points.size < 2) {
                    Log.i(TAG, "Not enough points for $today")
                    return Result.success()
                }

                var totalKm = 0.0
                for (i in 0 until points.lastIndex) {
                    val (lat1, lon1) = points[i]
                    val (lat2, lon2) = points[i + 1]
                    totalKm += haversineKm(lat1, lon1, lat2, lon2)
                }

                writeDailyDistance(context, today, totalKm)
                Log.i(TAG, "Distance $today = ${"%.2f".format(totalKm)} km")
                Result.success()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error computing distance", t)
            Result.failure()
        }
    }

    private fun parseLatLon(line: String): Pair<Double, Double>? {
        // "YYYY-MM-DD HH:mm:ss,lat,lon,acc?"
        val parts = line.split(',')
        if (parts.size < 3) return null
        val lat = parts[1].toDoubleOrNull() ?: return null
        val lon = parts[2].toDoubleOrNull() ?: return null
        return lat to lon
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun writeDailyDistance(context: Context, date: String, km: Double) {
        val f = File(context.filesDir, "daily_distance_log.csv")
        ensureHeader(f, "date,distance_km\n")
        f.appendText("$date,${"%.2f".format(km)}\n")
    }

    private fun ensureHeader(f: File, header: String) {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText(header)
        } else {
            // If file exists but lacks header, prepend it once.
            val first = f.bufferedReader().readLine() ?: ""
            if (!first.startsWith("date,")) {
                val content = f.readText()
                f.writeText(header + content)
            }
        }
    }

    companion object {
        private const val TAG = "DistanceWorker"
    }
}