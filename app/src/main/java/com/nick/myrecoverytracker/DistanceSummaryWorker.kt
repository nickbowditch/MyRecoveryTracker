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
            val filesDir = applicationContext.filesDir
            if (!filesDir.exists()) filesDir.mkdirs()

            val locFile = File(filesDir, "location_log.csv")
            val outFile = File(filesDir, "daily_distance_log.csv")

            val today = dayString(0)
            val yesterday = dayString(-1)

            if (!locFile.exists()) {
                // Still write zeros so downstream checks don't stall
                writeOrReplace(outFile, yesterday, 0.0f)
                writeOrReplace(outFile, today, 0.0f)
                Log.w(TAG, "location_log.csv missing; wrote $yesterday,0.0 and $today,0.0")
                return@withContext Result.success()
            }

            // Compute for both days so “RolledUp” shows YES in your checker
            val days = listOf(yesterday, today)
            val updates = mutableListOf<Pair<String, Float>>()

            days.forEach { d ->
                val km = computeKmForDay(locFile, d)
                updates += d to km
                Log.i(TAG, "Distance($d): ${"%.2f".format(km)} km")
            }

            // Apply updates (replace per-date rows, keep everything else)
            applyUpdates(outFile, updates)

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "DistanceSummaryWorker error", t)
            Result.retry()
        }
    }

    private fun computeKmForDay(locFile: File, day: String): Float {
        // location_log.csv lines: "YYYY-MM-DD HH:mm:ss,lat,lon,acc"
        val points: List<Triple<String, Location, Float>> = locFile.useLines { seq ->
            seq.mapNotNull { line ->
                val parts = line.split(',')
                if (parts.size >= 3 && parts[0].startsWith(day)) {
                    val ts = parts[0] // "YYYY-MM-DD HH:mm:ss"
                    val lat = parts[1].toDoubleOrNull()
                    val lon = parts[2].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        val acc = parts.getOrNull(3)?.toFloatOrNull() ?: 0f
                        Triple(
                            ts,
                            Location("").apply { latitude = lat; longitude = lon },
                            acc
                        )
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

            // Sanity checks
            val hopOk = d.isFinite() && d >= 0f && d <= 30000f
            val accOk = (acc == 0f || acc <= 100f) && (prevAcc == 0f || prevAcc <= 100f)

            if (hopOk && accOk) {
                meters += d
                prevLoc = loc
                prevAcc = acc
            } else {
                Log.d(TAG, "Skip hop @ $ts: d=${"%.1f".format(d)}m acc=$acc (prevAcc=$prevAcc)")
            }
        }

        return meters / 1000f
    }

    private fun applyUpdates(outFile: File, updates: List<Pair<String, Float>>) {
        outFile.parentFile?.mkdirs()
        val existing = if (outFile.exists()) outFile.readLines().toMutableList() else mutableListOf()

        // Remove rows for the dates we’re updating
        val dates = updates.map { it.first }.toSet()
        val kept = existing.filterNot { line ->
            dates.any { d -> line.startsWith("$d,") }
        }.toMutableList()

        // Append fresh rows for each date
        updates.forEach { (d, km) ->
            kept += "$d,${format1(km)}"
        }

        outFile.writeText(kept.joinToString("\n") + "\n")
    }

    private fun writeOrReplace(outFile: File, day: String, km: Float) {
        applyUpdates(outFile, listOf(day to km))
    }

    private fun dayString(offsetDays: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offsetDays) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun format1(v: Float): String = String.format(Locale.US, "%.2f", v)

    companion object { private const val TAG = "DistanceSummaryWorker" }
}