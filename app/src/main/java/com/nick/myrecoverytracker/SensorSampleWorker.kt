package com.nick.myrecoverytracker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Samples accelerometer for ~10s and writes one line to files/movement_log.csv:
 *   yyyy-MM-dd HH:mm:ss,intensity
 *
 * intensity ~= avg( |sqrt(x^2+y^2+z^2) - 9.81| ), clamped to >= 0
 */
class SensorSampleWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        val tag = "SensorSampleWorker"
        val ctx = applicationContext
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (acc == null) {
            Log.w(tag, "No accelerometer; writing 0")
            writeIntensity(0.0)
            return@withContext Result.success()
        }

        val samples = ArrayList<Double>(512)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0].toDouble()
                val y = event.values[1].toDouble()
                val z = event.values[2].toDouble()
                val mag = sqrt(x*x + y*y + z*z)
                val diff = max(0.0, mag - 9.81) // remove gravity baseline
                samples += diff
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        try {
            sm.registerListener(listener, acc, SensorManager.SENSOR_DELAY_GAME)
            // capture ~10 seconds
            delay(10_000)
        } catch (t: Throwable) {
            Log.e(tag, "Sampling error", t)
        } finally {
            sm.unregisterListener(listener)
        }

        val intensity = if (samples.isEmpty()) 0.0 else samples.average()
        Log.i(tag, "Sampled ${samples.size} pts; intensity=$intensity")
        writeIntensity(intensity)
        Result.success()
    }

    private fun writeIntensity(value: Double) {
        val f = File(applicationContext.filesDir, "movement_log.csv")
        if (!f.exists()) f.writeText("timestamp,intensity\n")
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        f.appendText("$ts,$value\n")
    }
}