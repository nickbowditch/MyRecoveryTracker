// app/src/main/java/com/nick/myrecoverytracker/DistanceWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DistanceWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val startNs = System.nanoTime()
        return try {
            val context = applicationContext
            val locFile = File(context.filesDir, "location_log.csv")
            if (!locFile.exists()) {
                Log.w(TAG, "DistanceWorker: location_log.csv not found — SUCCESS(no-op)")
                return Result.success()
            }

            val today = dayFormatter.format(Date())
            val points = readPointsForDate(locFile, today)

            if (points.size < 2) {
                Log.i(TAG, "DistanceWorker: SUCCESS — not enough points for $today (n=${points.size})")
                writeDailyDistance(context, today, 0.0)
                return Result.success()
            }

            var totalKm = 0.0
            for (i in 0 until points.lastIndex) {
                val (lat1, lon1) = points[i]
                val (lat2, lon2) = points[i + 1]
                totalKm += haversineKm(lat1, lon1, lat2, lon2)
            }

            writeDailyDistance(context, today, totalKm)
            val ms = (System.nanoTime() - startNs) / 1_000_000
            Log.i(TAG, "DistanceWorker SUCCESS: $today distance_km=${"%.2f".format(totalKm)} points=${points.size} timeMs=$ms")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "DistanceWorker FAILURE: error computing distance", t)
            Result.failure()
        }
    }

    private fun readPointsForDate(file: File, day: String): List<Pair<Double, Double>> {
        return buildList {
            file.useLines { seq ->
                seq.forEach { raw ->
                    val line = raw.trim().removeSuffix("\r")
                    if (line.isEmpty() || line.startsWith("#") || line.startsWith("ts,")) return@forEach
                    val parts = line.split(',')
                    if (parts.size < 3) return@forEach
                    val ts = parts[0]
                    val lat = parts[1].toDoubleOrNull() ?: return@forEach
                    val lon = parts[2].toDoubleOrNull() ?: return@forEach
                    val tsDay = tsToDay(ts) ?: return@forEach
                    if (tsDay == day) add(lat to lon)
                }
            }
        }
    }

    private fun tsToDay(ts: String): String? {
        if (ts.length >= 10 && ts[4] == '-' && ts[7] == '-') {
            return try {
                dayFormatter.format(isoLikeParser.parse(tsWithoutTimezone(ts))!!)
            } catch (_: Throwable) {
                ts.take(10)
            }
        }
        ts.toLongOrNull()?.let { millis ->
            return dayFormatter.format(Date(millis))
        }
        for (fmt in tsParsers) {
            try {
                return dayFormatter.format(fmt.parse(ts)!!)
            } catch (_: ParseException) { /* try next */ }
        }
        return null
    }

    private fun tsWithoutTimezone(s: String): String {
        return s.replace(Regex("(Z|[+-]\\d\\d:?\\d\\d)$"), "")
            .replace('T', ' ')
            .trim()
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
        val lines = if (f.exists()) f.readLines().toMutableList() else mutableListOf()
        if (lines.isEmpty()) lines += "date,distance_km"
        var replaced = false
        for (i in 1 until lines.size) {
            val L = lines[i].trim().removeSuffix("\r")
            if (L.startsWith("$date,")) {
                lines[i] = "$date,${"%.2f".format(km)}"
                replaced = true
                break
            }
        }
        if (!replaced) lines += "$date,${"%.2f".format(km)}"
        f.writeText(lines.joinToString(separator = "\n", postfix = "\n"))
    }

    private fun ensureHeader(f: File, header: String) {
        if (!f.exists()) {
            f.parentFile?.mkdirs()
            f.writeText(header)
            return
        }
        if (f.length() == 0L) {
            f.writeText(header)
            return
        }
        val first = f.bufferedReader().readLine() ?: ""
        if (!first.startsWith("date,")) {
            val rest = f.readText()
            f.writeText(header + rest)
        }
    }

    companion object {
        private const val TAG = "DistanceWorker"
        private val dayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
        private val tsParsers = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK),
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.UK),
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK)
        )
        private val isoLikeParser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK)
    }
}