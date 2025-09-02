package com.nick.myrecoverytracker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class AmbientLightWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), SensorEventListener {

    private var lux = -1f
    private lateinit var sensorManager: SensorManager

    override suspend fun doWork(): Result {
        Log.i("AmbientLightWorker", "🔦 Measuring ambient light...")

        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        return if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            delay(2000) // wait 2s to get at least one reading
            sensorManager.unregisterListener(this)

            if (lux >= 0f) {
                Log.i("AmbientLightWorker", "✅ Lux recorded: $lux")
                MetricsStore.saveAmbientLux(applicationContext, lux)
                Result.success()
            } else {
                Log.w("AmbientLightWorker", "⚠️ No valid lux reading")
                Result.retry()
            }
        } else {
            Log.e("AmbientLightWorker", "❌ No light sensor on device")
            Result.failure()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        lux = event.values[0]
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}