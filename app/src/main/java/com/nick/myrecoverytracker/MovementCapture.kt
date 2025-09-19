package com.nick.myrecoverytracker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import android.app.PendingIntent
import android.content.Intent
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

object MovementCapture : SensorEventListener {
    private var ctx: Context? = null
    private var sm: SensorManager? = null
    private var acc: Sensor? = null
    private var lastAccTs: Long = 0L
    private var baselineSet = false
    private var baseX = 0f
    private var baseY = 0f
    private var baseZ = 0f
    private val zone = ZoneId.systemDefault()
    private val fmtTs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
    private var arPi: PendingIntent? = null

    fun attach(c: Context) {
        if (ctx != null) return
        ctx = c.applicationContext
        sm = ctx!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acc = sm!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        acc?.let { sm!!.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        val intent = Intent(ctx, MovementARReceiver::class.java).setAction("mrt.AR")
        arPi = PendingIntent.getBroadcast(ctx, 2101, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val client = ActivityRecognition.getClient(ctx!!)
        val req = ActivityTransitionRequest(listOf(
            ActivityTransition.Builder().setActivityType(DetectedActivity.STILL).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.WALKING).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.RUNNING).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.ON_FOOT).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.ON_BICYCLE).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.IN_VEHICLE).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build()
        ))
        client.requestActivityTransitionUpdates(req, arPi!!)
    }

    fun detach() {
        try { sm?.unregisterListener(this) } catch (_: Throwable) {}
        try {
            val client = ctx?.let { ActivityRecognition.getClient(it) }
            arPi?.let { client?.removeActivityTransitionUpdates(it) }
        } catch (_: Throwable) {}
        ctx = null
        sm = null
        acc = null
        arPi = null
        baselineSet = false
    }

    override fun onSensorChanged(e: android.hardware.SensorEvent) {
        if (e.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        if (!baselineSet) {
            baseX = e.values[0]
            baseY = e.values[1]
            baseZ = e.values[2]
            baselineSet = true
            lastAccTs = 0L
            return
        }
        val dx = e.values[0] - baseX
        val dy = e.values[1] - baseY
        val dz = e.values[2] - baseZ
        val mag = sqrt((dx*dx + dy*dy + dz*dz).toDouble())
        val nowMs = System.currentTimeMillis()
        if (nowMs - lastAccTs >= 10000L) {
            lastAccTs = nowMs
            writeMagnitude(mag)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun writeMagnitude(mag: Double) {
        val f = File(ctx!!.filesDir, "movement_log.csv")
        val ts = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zone).format(fmtTs)
        val v = String.format(Locale.US, "%.6f", mag)
        f.appendText("$ts,$v\n")
    }

    fun writeActive() {
        val f = File(ctx!!.filesDir, "movement_log.csv")
        val ts = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zone).format(fmtTs)
        f.appendText("$ts,1.000000\n")
    }

    fun writeStill() {
        val f = File(ctx!!.filesDir, "movement_log.csv")
        val ts = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zone).format(fmtTs)
        f.appendText("$ts,0.000000\n")
    }
}