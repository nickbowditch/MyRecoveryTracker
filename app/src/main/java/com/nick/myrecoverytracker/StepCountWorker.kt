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
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Reads TYPE_STEP_COUNTER (cumulative since boot), derives today's steps as:
 *   steps_today = max(0, currentCounter - baselineCounterForToday)
 *
 * Baseline is stored in files/step_counter_baseline.json per day.
 *
 * Output CSV: files/daily_steps.csv
 *   Header: date,total_steps
 *   Row:    YYYY-MM-DD,<int>
 */
class StepCountWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val current = readCurrentStepCounter(ctx)
        if (current == null) {
            Log.w(TAG, "No step counter reading (sensor unavailable or timeout).")
            return@withContext Result.success()
        }

        val baseFile = File(ctx.filesDir, "step_counter_baseline.json")
        val baseline = readOrInitBaseline(baseFile, today, current)

        val stepsToday = max(0f, current - baseline).toInt()
        writeDaily(ctx, today, stepsToday)

        Log.i(TAG, "StepCount $today: current=$current baseline=$baseline -> steps=$stepsToday")
        Result.success()
    }

    private fun readCurrentStepCounter(ctx: Context): Float? {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return null

        var value: Float? = null
        val latch = CountDownLatch(1)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
                    value = event.values[0]
                    sm.unregisterListener(this)
                    latch.countDown()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        // Register at fastest; many devices will immediately deliver the latest cumulative value.
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST)

        // Wait briefly for a reading.
        latch.await(1500, TimeUnit.MILLISECONDS)
        sm.unregisterListener(listener)
        return value
    }

    private fun readOrInitBaseline(file: File, day: String, current: Float): Float {
        try {
            if (file.exists()) {
                val json = JSONObject(file.readText())
                val savedDay = json.optString("day", "")
                val base = json.optDouble("baseline", Double.NaN)
                if (savedDay == day && !base.isNaN()) {
                    return base.toFloat()
                }
            }
        } catch (_: Throwable) { /* fall through to re-init */ }

        // Initialize baseline for today to current counter.
        val obj = JSONObject()
            .put("day", day)
            .put("baseline", current.toDouble())
        file.parentFile?.mkdirs()
        file.writeText(obj.toString())
        return current
    }

    private fun writeDaily(ctx: Context, day: String, steps: Int) {
        val out = File(ctx.filesDir, "daily_steps.csv")
        val header = "date,total_steps"
        val lines = if (out.exists()) out.readLines().toMutableList() else mutableListOf(header)
        val filtered = lines.filterNot { it.startsWith("$day,") }.toMutableList()
        filtered.add("$day,$steps")
        out.writeText(filtered.joinToString("\n") + "\n")
    }

    companion object {
        private const val TAG = "StepCountWorker"
    }
}