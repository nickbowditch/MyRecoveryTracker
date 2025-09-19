package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class RecoveryVisitsWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    private val tag = "RecoveryVisitsWorker"
    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    data class Point(val lat: Double, val lon: Double)

    data class Place(val name: String, val lat: Double, val lon: Double, val radiusM: Double)

    override fun doWork(): Result {
        val today = dayFmt.format(Date())
        val filesDir = applicationContext.filesDir
        val locFile = File(filesDir, "location_log.csv")
        val outFile = File(filesDir, "daily_recovery_visits.csv")
        val cfgFile = File(filesDir, "participant_recovery_locations.json")

        val places = loadPlaces(cfgFile)
        if (places.isEmpty()) {
            Log.w(tag, "No recovery places configured (participant_recovery_locations.json missing or empty). Writing NO,0.")
            upsert(outFile, today, visited = false, distinct = 0)
            return Result.success()
        }

        if (!locFile.exists()) {
            Log.w(tag, "location_log.csv missing. Writing NO,0.")
            upsert(outFile, today, visited = false, distinct = 0)
            return Result.success()
        }

        val todaysPoints = mutableListOf<Point>()
        locFile.forEachLine { line ->
            // Expect: yyyy-MM-dd HH:mm:ss,lat,lon,acc
            if (line.startsWith(today)) {
                val parts = line.split(',')
                if (parts.size >= 3) {
                    val lat = parts[1].toDoubleOrNull()
                    val lon = parts[2].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        todaysPoints += Point(lat, lon)
                    }
                }
            }
        }

        if (todaysPoints.isEmpty()) {
            Log.i(tag, "No points for $today. Writing NO,0.")
            upsert(outFile, today, visited = false, distinct = 0)
            return Result.success()
        }

        // For each place, see if *any* point is inside its radius
        val visitedNames = mutableSetOf<String>()
        for (p in places) {
            if (todaysPoints.any { within(it, p) }) {
                visitedNames += p.name
            }
        }

        val visited = visitedNames.isNotEmpty()
        val distinct = visitedNames.size
        Log.i(tag, "Visited=$visited distinct=$distinct places=$visitedNames")
        upsert(outFile, today, visited = visited, distinct = distinct)
        return Result.success()
    }

    private fun within(pt: Point, place: Place): Boolean {
        val d = haversineMeters(pt.lat, pt.lon, place.lat, place.lon)
        return d <= place.radiusM
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun loadPlaces(cfg: File): List<Place> {
        if (!cfg.exists()) return emptyList()
        return try {
            val txt = cfg.readText().trim()
            if (txt.isEmpty()) return emptyList()
            val arr = JSONArray(txt)
            val list = mutableListOf<Place>()
            for (i in 0 until arr.length()) {
                val o: JSONObject = arr.getJSONObject(i)
                val name = o.optString("name", "place_$i")
                val lat = o.optDouble("lat", Double.NaN)
                val lon = o.optDouble("lon", Double.NaN)
                val radius = when {
                    o.has("radius_m") -> o.optDouble("radius_m", 100.0)
                    o.has("radiusM") -> o.optDouble("radiusM", 100.0)
                    else -> 100.0
                }
                if (!lat.isNaN() && !lon.isNaN()) {
                    list += Place(name, lat, lon, radius.coerceAtLeast(10.0))
                }
            }
            list
        } catch (t: Throwable) {
            Log.e(tag, "Failed to parse participant_recovery_locations.json", t)
            emptyList()
        }
    }

    private fun upsert(out: File, day: String, visited: Boolean, distinct: Int) {
        val lines = if (out.exists()) out.readLines() else emptyList()
        val keep = lines.filterNot { it.startsWith("$day,") }
        val yn = if (visited) "YES" else "NO"
        val newLine = "$day,$yn,$distinct"
        out.writeText((keep + newLine).joinToString("\n") + "\n")
    }
}