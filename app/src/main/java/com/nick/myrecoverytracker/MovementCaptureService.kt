package com.nick.myrecoverytracker

import android.app.*
import android.content.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import com.google.android.gms.location.*
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MovementCaptureService : Service(), SensorEventListener {
    private lateinit var sm: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var arClient: ActivityRecognitionClient
    private lateinit var arPi: PendingIntent
    private val zone = ZoneId.systemDefault()
    private val fmtTs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

    override fun onCreate() {
        super.onCreate()
        val chId = "movement_fg"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(chId) == null) {
            nm.createNotificationChannel(NotificationChannel(chId, "Movement", NotificationManager.IMPORTANCE_MIN))
        }
        val n = Notification.Builder(this, chId)
            .setContentTitle("Movement capture active")
            .setSmallIcon(android.R.drawable.stat_notify_sync_noanim)
            .setOngoing(true)
            .build()
        startForeground(2001, n)

        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }

        arClient = ActivityRecognition.getClient(this)
        val intent = Intent(this, MovementARReceiver::class.java).setAction("mrt.AR")
        arPi = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val req = ActivityTransitionRequest(listOf(
            ActivityTransition.Builder().setActivityType(DetectedActivity.STILL).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.WALKING).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.ON_FOOT).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.RUNNING).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.IN_VEHICLE).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build(),
            ActivityTransition.Builder().setActivityType(DetectedActivity.ON_BICYCLE).setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build()
        ))
        arClient.requestActivityTransitionUpdates(req, arPi)
    }

    override fun onDestroy() {
        try { sm.unregisterListener(this) } catch (_: Throwable) {}
        try { arClient.removeActivityTransitionUpdates(arPi) } catch (_: Throwable) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        val ts = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zone).format(fmtTs)
        val line = "$ts,step,${event.values[0]}"
        append("movement_log.csv", line)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun append(name: String, line: String) {
        val f = File(filesDir, name)
        f.parentFile?.mkdirs()
        f.appendText(line + "\n")
    }
}