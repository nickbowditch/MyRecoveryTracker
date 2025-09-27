// app/src/main/java/com/nick/myrecoverytracker/LocationPingWorker.kt
package com.nick.myrecoverytracker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class LocationPingWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        setForeground(createForegroundInfo())
        logLocation()
        Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val ctx = applicationContext
        ensureChannel(ctx)

        val notif = NotificationCompat.Builder(ctx, CH_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MyRecoveryAssistant")
            .setContentText("Collecting study data")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        val type = if (Build.VERSION.SDK_INT >= 29)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0

        return if (Build.VERSION.SDK_INT >= 31) {
            ForegroundInfo(NOTIF_ID, notif, type)
        } else {
            ForegroundInfo(NOTIF_ID, notif)
        }
    }

    private fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CH_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CH_ID,
                    "MyRecovery Worker",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun logLocation() {
        val context = applicationContext
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PermissionChecker.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w(TAG, "🚫 Missing ACCESS_FINE_LOCATION permission")
            return
        }

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null && (bestLocation == null || location.accuracy < bestLocation.accuracy)) {
                bestLocation = location
            }
        }

        if (bestLocation != null) {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val logLine = "$timestamp,${bestLocation.latitude},${bestLocation.longitude},${bestLocation.accuracy}"
            Log.i(TAG, "📍 Location logged: $logLine")
            appendToFile("location_log.csv", logLine)
        } else {
            Log.w(TAG, "⚠️ No location data available")
        }
    }

    private fun appendToFile(filename: String, line: String) {
        try {
            val file = File(applicationContext.filesDir, filename)
            val writer = FileWriter(file, true)
            writer.appendLine(line)
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to write location log", e)
        }
    }

    companion object {
        private const val CH_ID = "mrt_wm_channel"
        private const val NOTIF_ID = 2001
        private const val TAG = "LocationPingWorker"
    }
}