package com.nick.myrecoverytracker

import android.content.Context
import android.location.Location
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object MetricsStore {

    // Current canonical filename (matches your scripts/tools)
    private const val UNLOCK_LOG_FILE = "unlock_log.csv"
    // Legacy filename (plural) for silent migration
    private const val UNLOCK_LOG_FILE_LEGACY = "unlocks_log.csv"

    private const val SCREEN_LOG_FILE = "screen_log.csv"

    private const val ENTROPY_LOG_FILE = "usage_entropy.csv"
    private const val LOCATION_LOG_FILE = "location_log.csv"
    private const val LUX_LOG_FILE = "ambient_lux.csv"
    private const val NOTIF_LOG_FILE = "notification_log.csv"
    private const val WIFI_LOG_FILE = "wifi_log.csv"
    private const val DISTANCE_LOG_FILE = "daily_distance_log.csv"

    // --- Unlocks ----

    private fun unlockFile(context: Context): File {
        val dir = context.filesDir
        val current = File(dir, UNLOCK_LOG_FILE)
        val legacy = File(dir, UNLOCK_LOG_FILE_LEGACY)
        if (legacy.exists()) {
            try {
                if (!current.exists()) legacy.copyTo(current, overwrite = false)
                legacy.delete()
            } catch (e: Exception) {
                Log.e("MetricsStore", "Unlock log migration failed", e)
            }
        }
        return current
    }

    fun saveUnlock(context: Context) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        try {
            val file = unlockFile(context)
            FileWriter(file, true).use { w ->
                w.appendLine("$timestamp,UNLOCK")
            }
            Log.i("MetricsStore", "Unlock logged: $timestamp")
        } catch (e: Exception) {
            Log.e("MetricsStore", "Failed to log unlock", e)
        }
    }

    fun getUnlockLog(context: Context): List<String> {
        val file = unlockFile(context)
        return if (file.exists()) file.readLines() else emptyList()
    }

    fun clearUnlockLog(context: Context) {
        val file = unlockFile(context)
        if (file.exists()) file.delete()
    }

    fun summarizeDailyUnlocks(context: Context): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return getUnlockLog(context).count { it.startsWith(today) }
    }

    // --- Screen ON/OFF ---

    private fun screenFile(context: Context): File = File(context.filesDir, SCREEN_LOG_FILE)

    /**
     * Writes: "yyyy-MM-dd HH:mm:ss,ON" or "yyyy-MM-dd HH:mm:ss,SCREEN_OFF"
     * (keeps the same vocabulary your existing CSV already showed)
     */
    fun saveScreenEvent(context: Context, event: String) {
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val f = screenFile(context)
        try {
            if (!f.exists()) {
                f.parentFile?.mkdirs()
                f.writeText("timestamp,event\n")
            }
            FileWriter(f, true).use { w ->
                w.appendLine("$ts,$event")
            }
            Log.i("MetricsStore", "Screen logged: $event at $ts")
        } catch (e: Exception) {
            Log.e("MetricsStore", "Failed to write screen_log.csv", e)
        }
    }

    fun saveScreenOn(context: Context) = saveScreenEvent(context, "ON")
    fun saveScreenOff(context: Context) = saveScreenEvent(context, "SCREEN_OFF")

    // --- Entropy ---

    fun saveAppUsageEntropy(context: Context, entropy: Double) {
        val prefs = context.getSharedPreferences("metrics", Context.MODE_PRIVATE)
        prefs.edit().putFloat("entropy", entropy.toFloat()).apply()
        Log.i("MetricsStore", "Saved app usage entropy: $entropy")

        try {
            val file = File(context.filesDir, ENTROPY_LOG_FILE)
            FileWriter(file, true).use { w ->
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                w.appendLine("$now,$entropy")
            }
        } catch (e: Exception) {
            Log.e("MetricsStore", "Failed to write entropy log", e)
        }
    }

    fun getAppUsageEntropy(context: Context): Float {
        val prefs = context.getSharedPreferences("metrics", Context.MODE_PRIVATE)
        return prefs.getFloat("entropy", -1f)
    }

    fun exportFile(context: Context, filename: String): File? {
        val file = File(context.filesDir, filename)
        return if (file.exists()) file else null
    }

    // --- Location / Distance ---

    fun getLocationLog(context: Context): List<String> {
        val file = File(context.filesDir, LOCATION_LOG_FILE)
        return if (file.exists()) file.readLines() else emptyList()
    }

    fun clearLocationLog(context: Context) {
        val file = File(context.filesDir, LOCATION_LOG_FILE)
        if (file.exists()) file.delete()
    }

    fun saveAmbientLux(context: Context, lux: Float) {
        try {
            val file = File(context.filesDir, LUX_LOG_FILE)
            FileWriter(file, true).use { w ->
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                w.appendLine("$now,$lux")
            }
            Log.i("MetricsStore", "Logged ambient lux: $lux")
        } catch (e: Exception) {
            Log.e("MetricsStore", "Failed to log ambient lux", e)
        }
    }

    fun appendNotificationLog(
        context: Context,
        timestamp: String,
        packageName: String,
        title: String,
        text: String
    ) {
        try {
            val file = File(context.filesDir, NOTIF_LOG_FILE)
            FileWriter(file, true).use { w ->
                w.appendLine("$timestamp,$packageName,\"$title\",\"$text\"")
            }
            Log.i("MetricsStore", "Logged notification from $packageName")
        } catch (e: Exception) {
            Log.e("MetricsStore", "Failed to log notification", e)
        }
    }

    fun clearNotificationLog(context: Context) {
        val file = File(context.filesDir, NOTIF_LOG_FILE)
        if (file.exists()) file.delete()
    }

    fun saveWifiNetworksLog(context: Context, ssid: String, bssid: String, level: Int) {
        try {
            val file = File(context.filesDir, WIFI_LOG_FILE)
            FileWriter(file, true).use { w ->
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                w.appendLine("$now,$ssid,$bssid,$level")
            }
        } catch (e: Exception) {
            Log.e("MetricsStore", "Failed to log WiFi network", e)
        }
    }

    fun saveDailyDistance(context: Context, date: String, distanceKm: Float) {
        try {
            val file = File(context.filesDir, DISTANCE_LOG_FILE)
            FileWriter(file, true).use { w ->
                w.appendLine("$date,$distanceKm")
            }
            Log.i("MetricsStore", "Logged distance: $distanceKm km on $date")
        } catch (e: Exception) {
            Log.e("MetricsStore", "Failed to log daily distance", e)
        }
    }

    fun getDailyDistanceLog(context: Context): List<Pair<String, Float>> {
        val file = File(context.filesDir, DISTANCE_LOG_FILE)
        return if (file.exists()) {
            file.readLines().mapNotNull {
                val parts = it.split(",")
                if (parts.size == 2) {
                    val date = parts[0]
                    val distance = parts[1].toFloatOrNull()
                    if (distance != null) date to distance else null
                } else null
            }
        } else emptyList()
    }

    fun summarizeDailyDistance(context: Context): Float {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = File(context.filesDir, LOCATION_LOG_FILE)
        if (!file.exists()) return 0f

        val points = file.readLines()
            .filter { it.startsWith(today) }
            .mapNotNull { line ->
                val parts = line.split(",")
                if (parts.size >= 4) {
                    try {
                        val lat = parts[1].toDouble()
                        val lon = parts[2].toDouble()
                        Location("").apply {
                            latitude = lat
                            longitude = lon
                        }
                    } catch (_: Exception) {
                        null
                    }
                } else null
            }

        var totalDistance = 0f
        for (i in 1 until points.size) {
            totalDistance += points[i - 1].distanceTo(points[i])
        }
        val km = totalDistance / 1000f
        Log.i("MetricsStore", "Distance travelled today: ${"%.2f".format(km)} km")

        saveDailyDistance(context, today, km)
        return km
    }
}