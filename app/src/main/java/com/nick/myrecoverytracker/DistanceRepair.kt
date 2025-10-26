// app/src/main/java/com/nick/myrecoverytracker/DistanceRepair.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

object DistanceRepair {

    private const val TAG = "DistanceRepair"

    // Filters (match LocationCaptureService)
    private const val MAX_SPEED_M_S = 55.6f       // ~200 km/h
    private const val MAX_STEP_METERS = 3000.0    // cap single-hop if no time info
    private const val GOOD_ACC_METERS = 25.0

    private val tsFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }
    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    fun recalcToday(ctx: Context) {
        try {
            val files = ctx.filesDir
            val raw = File(files, "location_log.csv")
            if (!raw.exists() || raw.length() == 0L) {
                writeToday(files, 0.0)
                Log.i(TAG, "No location_log.csv, wrote 0.00 km")
                return
            }

            val today = dayFmt.format(System.currentTimeMillis())

            // parse rows for "today"
            data class Pt(val tMs: Long, val lat: Double, val lon: Double, val acc: Double)
            val pts = mutableListOf<Pt>()

            raw.forEachLine { line ->
                if (line.startsWith("ts,") || line.isBlank()) return@forEachLine
                val parts = line.split(',')
                if (parts.size < 4) return@forEachLine
                val ts = parts[0].trim()
                val dateStr = ts.take(10)
                if (dateStr != today) return@forEachLine

                val lat = parts[1].toDoubleOrNull() ?: return@forEachLine
                val lon = parts[2].toDoubleOrNull() ?: return@forEachLine
                val acc = parts[3].toDoubleOrNull() ?: 9999.0

                if (acc <= GOOD_ACC_METERS) {
                    val t = try { tsFmt.parse(ts)!!.time } catch (_: ParseException) { return@forEachLine }
                    pts += Pt(t, lat, lon, acc)
                }
            }

            if (pts.size < 2) {
                writeToday(files, 0.0)
                Log.i(TAG, "Insufficient good points for $today, wrote 0.00 km")
                return
            }

            // sort by time just in case
            pts.sortBy { it.tMs }

            var meters = 0.0
            for (i in 1 until pts.size) {
                val a = pts[i - 1]
                val b = pts[i]
                val d = haversineMeters(a.lat, a.lon, b.lat, b.lon)

                // speed sanity if timestamps differ
                val dt = max(1L, b.tMs - a.tMs) / 1000.0 // seconds
                val spd = d / dt
                val ok = if (dt >= 1.0) {
                    spd <= MAX_SPEED_M_S
                } else {
                    d <= MAX_STEP_METERS
                }

                if (ok) meters += d
            }

            val km = meters / 1000.0
            writeToday(files, km)
            Log.i(TAG, "Recalculated distances. today=$today value=${"%.2f".format(Locale.US, km)} km")

        } catch (t: Throwable) {
            Log.e(TAG, "recalcToday failed", t)
            // still try to write zero to avoid dangling state
            runCatching { writeToday(ctx.filesDir, 0.0) }
        }
    }

    // --- helpers ---

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

    private fun writeToday(dir: File, km: Double) {
        // distance_today.csv (single value for UI/debug)
        ensureHeader(dir, "distance_today.csv", "date,distance_km")
        val today = dayFmt.format(System.currentTimeMillis())
        overwriteOrAppend(dir, "distance_today.csv", today, km)

        // daily_distance_log.csv (history)
        ensureHeader(dir, "daily_distance_log.csv", "date,distance_km")
        overwriteOrAppend(dir, "daily_distance_log.csv", today, km)
    }

    private fun ensureHeader(dir: File, name: String, header: String) {
        val f = File(dir, name)
        if (!f.exists() || f.length() == 0L) {
            FileOutputStream(f, false).channel.use { ch ->
                val bb = ("$header\n").toByteArray()
                ch.write(java.nio.ByteBuffer.wrap(bb))
                ch.force(true)
            }
        }
    }

    /**
     * Replace the line whose first column equals `key` (date), else append.
     * CSV format: "date,distance_km"
     */
    private fun overwriteOrAppend(dir: File, name: String, key: String, km: Double) {
        val f = File(dir, name)
        val tmp = File(dir, "$name.tmp")
        val formatted = "%.2f".format(Locale.US, km)

        if (!f.exists()) {
            ensureHeader(dir, name, "date,distance_km")
        }

        var replaced = false
        tmp.printWriter().use { out ->
            f.forEachLine { line ->
                if (line.startsWith("date,")) {
                    out.println(line)
                    return@forEachLine
                }
                val k = line.substringBefore(',', missingDelimiterValue = "")
                if (k == key) {
                    out.println("$key,$formatted")
                    replaced = true
                } else if (k.isNotEmpty()) {
                    out.println(line)
                }
            }
            if (!replaced) {
                out.println("$key,$formatted")
            }
        }
        // atomic-ish swap
        if (!f.delete()) f.renameTo(File(dir, "$name.bak.${System.currentTimeMillis()/1000}"))
        tmp.renameTo(f)
    }
}