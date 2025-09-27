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
import java.util.Calendar
import java.util.Locale

class DistanceSummaryWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dir = applicationContext.filesDir
            if (!dir.exists()) dir.mkdirs()

            val locFile = File(dir, "location_log.csv")
            val outFile = File(dir, "daily_distance_log.csv")
            val header = "date,distance_km\n"

            val today = dayString(0)
            val yesterday = dayString(-1)

            if (!locFile.exists()) {
                applyUpdates(outFile, header, listOf(yesterday to 0f, today to 0f))
                Log.w(TAG, "location_log.csv missing; wrote zeros for $yesterday and $today")
                return@withContext Result.success()
            }

            val days = listOf(yesterday, today)
            val updates = days.map { d -> d to computeKmForDay(locFile, d) }

            applyUpdates(outFile, header, updates)
            updates.forEach { (d, km) -> Log.i(TAG, "Distance($d) = ${"%.2f".format(km)} km") }

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "DistanceSummaryWorker error", t)
            Result.retry()
        }
    }

    private fun computeKmForDay(locFile: File, day: String): Float {
        // "YYYY-MM-DD HH:mm:ss,lat,lon,acc?"
        val points = locFile.useLines { seq ->
            seq.mapNotNull { line ->
                val parts = line.split(',')
                if (parts.size >= 3 && parts[0].startsWith(day)) {
                    val lat = parts[1].toDoubleOrNull()
                    val lon = parts[2].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        val acc = parts.getOrNull(3)?.toFloatOrNull() ?: 0f
                        Triple(parts[0], Location("").apply {
                            latitude = lat; longitude = lon
                        }, acc)
                    } else null
                } else null
            }.sortedBy { it.first }.toList()
        }

        if (points.size < 2) return 0f

        var meters = 0f
        var prevLoc = points.first().second
        var prevAcc = points.first().third

        for (i in 1 until points.size) {
            val (ts, loc, acc) = points[i]
            val d = prevLoc.distanceTo(loc)

            val hopOk = d.isFinite() && d >= 0f && d <= 30000f
            val accOk = (acc == 0f || acc <= 100f) && (prevAcc == 0f || prevAcc <= 100f)

            if (hopOk && accOk) {
                meters += d
                prevLoc = loc
                prevAcc = acc
            } else {
                Log.d(TAG, "Skip hop @$ts d=${"%.1f".format(d)}m acc=$acc prevAcc=$prevAcc")
            }
        }

        return meters / 1000f
    }

    private fun applyUpdates(outFile: File, header: String, updates: List<Pair<String, Float>>) {
        outFile.parentFile?.mkdirs()
        val existing = if (outFile.exists()) outFile.readLines() else emptyList()
        val withoutHeader = existing.filterNot { it.startsWith("date,") }.toMutableList()

        val dates = updates.map { it.first }.toSet()
        val kept = withoutHeader.filterNot { line -> dates.any { d -> line.startsWith("$d,") } }.toMutableList()

        updates.forEach { (d, km) -> kept += "$d,${String.format(Locale.US, "%.2f", km)}" }

        outFile.writeText(buildString {
            append(header)
            kept.forEach { append(it).append('\n') }
        })
    }

    private fun dayString(offsetDays: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offsetDays) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    companion object {
        private const val TAG = "DistanceSummaryWorker"
    }
}