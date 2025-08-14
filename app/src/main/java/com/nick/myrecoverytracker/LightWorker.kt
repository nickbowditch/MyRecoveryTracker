package com.nick.myrecoverytracker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LightWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

            if (lightSensor == null) {
                Log.w("LightWorker", "No light sensor found on device.")
                return@withContext Result.success()
            }

            val luxValue = lightSensor.power  // Not accurate! Replace with proper sensor reading if needed
            val timestamp = getCurrentTimestamp()
            val logFile = File(applicationContext.filesDir, "lux_log.csv")
            logFile.appendText("$timestamp,$luxValue lux\n")

            Log.i("LightWorker", "💡 Logged lux value: $luxValue")
            Result.success()
        } catch (e: Exception) {
            Log.e("LightWorker", "❌ Error logging lux", e)
            Result.failure()
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return sdf.format(Date())
    }
}