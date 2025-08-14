package com.nick.myrecoverytracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class LocationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        logLocation()
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun logLocation() {
        val context = applicationContext
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PermissionChecker.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w("LocationWorker", "🚫 Missing ACCESS_FINE_LOCATION permission")
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
            Log.i("LocationWorker", "📍 Location logged: $logLine")
            appendToFile("location_log.csv", logLine)
        } else {
            Log.w("LocationWorker", "⚠️ No location data available")
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
            Log.e("LocationWorker", "❌ Failed to write location log", e)
        }
    }
}