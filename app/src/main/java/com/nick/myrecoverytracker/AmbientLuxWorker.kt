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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlin.coroutines.resume

/**
 * Samples the device ambient light sensor briefly and appends one row:
 *   files/ambient_lux.csv  ->  "timestamp,lux"
 */
class AmbientLuxWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val tag = "AmbientLuxWorker"
    private val tsFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sm = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (sensor == null) {
            Log.w(tag, "No TYPE_LIGHT sensor; skipping")
            return@withContext Result.success()
        }

        val samples = sampleLux(sm, sensor, millis = 2000L, minCount = 6)
        if (samples.isEmpty()) {
            Log.w(tag, "No lux samples captured")
            return@withContext Result.retry()
        }
        val avg = samples.average().toFloat()

        val file = File(applicationContext.filesDir, "ambient_lux.csv")
        ensureHeader(file, "timestamp,lux")
        FileWriter(file, true).use { w ->
            w.appendLine("${tsFmt.format(Date())},$avg")
        }
        Log.i(tag, "Logged lux avg=$avg (n=${samples.size})")
        Result.success()
    }

    private suspend fun sampleLux(
        sm: SensorManager,
        sensor: Sensor,
        millis: Long,
        minCount: Int
    ): List<Float> = suspendCancellableCoroutine { cont ->
        val buf = ArrayList<Float>(32)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    buf += event.values[0]
                    if (buf.size >= minCount) {
                        try { sm.unregisterListener(this) } catch (_: Throwable) {}
                        if (!cont.isCompleted) cont.resume(buf.toList())
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        val t = Thread {
            try { Thread.sleep(millis) } catch (_: InterruptedException) {}
            try { sm.unregisterListener(listener) } catch (_: Throwable) {}
            if (!cont.isCompleted) cont.resume(buf.toList())
        }
        t.isDaemon = true
        t.start()

        cont.invokeOnCancellation {
            try { sm.unregisterListener(listener) } catch (_: Throwable) {}
        }
    }

    private fun ensureHeader(f: File, header: String) {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText(header + "\n")
        }
    }
}