package com.nick.myrecoverytracker

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class RecoveryVisitsWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val day = today()
        val locCsv = File(ctx.filesDir, "location_log.csv")
        val jsonFile = File(ctx.filesDir, "participant_recovery_locations.json")
        val outFile = File(ctx.filesDir, "daily_recovery_visits.csv")

        Log.i(TAG, "Starting RecoveryVisitsWorker for $day")
        Log.i(TAG, "location_log.csv path: ${locCsv.absolutePath}")
        Log.i(TAG, "participant_recovery_locations.json path: ${jsonFile.absolutePath}")

        try {
            if (!jsonFile.exists()) {
                Log.w(TAG, "No participant_recovery_locations.json; writing N")
                writeYN(outFile, day, "N")
                return@withContext Result.success()
            }
            if (!locCsv.exists()) {
                Log.w(TAG, "No location_log.csv; writing N")
                writeYN(outFile, day, "N")
                return@withContext Result.success()
            }

            val geofences = parseFences(jsonFile.readText())
            Log.i(TAG, "Loaded ${geofences.size} recovery locations: $geofences")
            if (geofences.isEmpty()) {
                Log.w(TAG, "Empty recovery locations; writing N")
                writeYN(outFile, day, "N")
                return@withContext Result.success()
            }

            val todaysPoints = locCsv.readLines().mapNotNull { line ->
                val parts = line.split(",")
                if (parts.size >= 3 && parts[0].startsWith(day)) {
                    val lat = parts[1].toDoubleOrNull()
                    val lon = parts[2].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        Log.d(TAG, "Today's point: $lat,$lon from line: $line")
                        lat to lon
                    } else null
                } else null
            }

            val visited = todaysPoints.any { (lat, lon) ->
                geofences.any { gf ->
                    val dist = distanceMeters(lat, lon, gf.lat, gf.lon)
                    Log.d(TAG, "Distance to ${gf.lat},${gf.lon} = $dist m (radius ${gf.radiusM})")
                    dist <= gf.radiusM
                }
            }

            val yn = if (visited) "Y" else "N"
            writeYN(outFile, day, yn)
            Log.i(TAG, "RecoveryVisits($day) = $yn")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "RecoveryVisitsWorker failed", t)
            Result.retry()
        }
    }

    private fun writeYN(out: File, day: String, yn: String) {
        val lines = if (out.exists()) out.readLines().filterNot { it.startsWith("$day,") }.toMutableList() else mutableListOf()
        lines += "$day,$yn"
        out.writeText(lines.joinToString("\n") + "\n")
        Log.i(TAG, "Wrote result to ${out.absolutePath}")
    }

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val res = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, res)
        return res[0]
    }

    private fun parseFences(text: String): List<Gf> {
        val arr = JSONArray(text)
        val out = ArrayList<Gf>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val lat = o.optDouble("lat", Double.NaN)
            val lon = o.optDouble("lon", Double.NaN)
            val r   = o.optDouble("radius_m", Double.NaN)
            if (!lat.isNaN() && !lon.isNaN() && !r.isNaN()) {
                out += Gf(lat, lon, r.toFloat())
            }
        }
        return out
    }

    data class Gf(val lat: Double, val lon: Double, val radiusM: Float)

    companion object { private const val TAG = "RecoveryVisitsWorker" }
}