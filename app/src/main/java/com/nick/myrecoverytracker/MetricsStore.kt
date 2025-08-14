package com.nick.myrecoverytracker

import android.content.Context
import android.location.Location
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object MetricsStore {

    // Standardized filename (matches what's on your device)
    private const val UNLOCK_LOG_FILE = "unlocks_log.csv"
    // Legacy filename (for silent migration)
    private const val UNLOCK_LOG_FILE_LEGACY = "unlock_log.csv"

    private const val ENTROPY_LOG_FILE = "usage_entropy.csv"
    private const val LOCATION_LOG_FILE = "location_log.csv"
    private const val LUX_LOG_FILE = "ambient_lux.csv"
    private const val NOTIF_LOG_FILE = "notification_log.csv"
    private const val WIFI_LOG_FILE = "wifi_log.csv"
    private const val DISTANCE_LOG_FILE = "daily_distance_log.csv"

    // --- Unlocks ----

    private fun unlockFile(context: Context): File {
        val dir = context.filesDir
        val legacy = File(dir, UNLOCK_LOG_FILE_LEGACY)
        val current = File(dir, UNLOCK_LOG_FILE)
        // Migrate old -> new once, if needed
        if (legacy.exists()) {
            try {
                if (!current.exists()) {
                    legacy.copyTo(current, overwrite = false)
                }
                legacy.delete()
            } catch (e: Exception) {
                Log.e("MetricsStore", "Unlock log migration failed", e)
            }
        }
        return current
    }

    /** Primary method to save an unlock event */
    fun saveUnlock(context: Context) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        try {
            val file = unlockFile(context)
            val writer = FileWriter(file, true)
            writer.appendLine(timestamp)
            writer.flush()
            writer.close()
            Log.i("MetricsStore", "🔓 Unlock logged: $timestamp")
        } catch (e: Exception) {
            Log.e("MetricsStore", "❌ Failed to log unlock", e)
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

    // --- Entropy ---

    fun saveAppUsageEntropy(context: Context, entropy: Double) {
        val prefs = context.getSharedPreferences("metrics", Context.MODE_PRIVATE)
        prefs.edit().putFloat("entropy", entropy.toFloat()).apply()
        Log.i("MetricsStore", "✅ Saved app usage entropy: $entropy")

        try {
            val file = File(context.filesDir, ENTROPY_LOG_FILE)
            val writer = FileWriter(file, true)
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            writer.appendLine("$now,$entropy")
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            Log.e("MetricsStore", "❌ Failed to write entropy log", e)
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
            val writer = FileWriter(file, true)
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            writer.appendLine("$now,$lux")
            writer.flush()
            writer.close()
            Log.i("MetricsStore", "💡 Logged ambient lux: $lux")
        } catch (e: Exception) {
            Log.e("MetricsStore", "❌ Failed to log ambient lux", e)
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
            val writer = FileWriter(file, true)
            writer.appendLine("$timestamp,$packageName,\"$title\",\"$text\"")
            writer.flush()
            writer.close()
            Log.i("MetricsStore", "🔔 Logged notification from $packageName")
        } catch (e: Exception) {
            Log.e("MetricsStore", "❌ Failed to log notification", e)
        }
    }

    fun clearNotificationLog(context: Context) {
        val file = File(context.filesDir, NOTIF_LOG_FILE)
        if (file.exists()) file.delete()
    }

    fun saveWifiNetworksLog(context: Context, ssid: String, bssid: String, level: Int) {
        try {
            val file = File(context.filesDir, WIFI_LOG_FILE)
            val writer = FileWriter(file, true)
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            writer.appendLine("$now,$ssid,$bssid,$level")
            writer.flush()
            writer.close()
            Log.i("MetricsStore", "📶 Logged WiFi: $ssid ($bssid), RSSI: $level")
        } catch (e: Exception) {
            Log.e("MetricsStore", "❌ Failed to log WiFi network", e)
        }
    }

    fun saveDailyDistance(context: Context, date: String, distanceKm: Float) {
        try {
            val file = File(context.filesDir, DISTANCE_LOG_FILE)
            val writer = FileWriter(file, true)
            writer.appendLine("$date,$distanceKm")
            writer.flush()
            writer.close()
            Log.i("MetricsStore", "🚶 Logged distance: $distanceKm km on $date")
        } catch (e: Exception) {
            Log.e("MetricsStore", "❌ Failed to log daily distance", e)
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

        val lines = file.readLines()
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
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }

        var totalDistance = 0f
        for (i in 1 until lines.size) {
            totalDistance += lines[i - 1].distanceTo(lines[i])
        }

        val km = totalDistance / 1000f
        Log.i("MetricsStore", "📏 Distance travelled today: ${"%.2f".format(km)} km")

        saveDailyDistance(context, today, km)
        return km
    }
}