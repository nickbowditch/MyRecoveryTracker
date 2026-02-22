// app/src/main/java/com/nick/myrecoverytracker/LocationCaptureService.kt
package com.nick.myrecoverytracker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

class LocationCaptureService : Service(), LocationListener {

    private lateinit var lm: LocationManager

    private val MIN_ACC_METERS = 25f
    private val MIN_KEEP_DIST_M = 15f
    private val MIN_KEEP_INTERVAL_MS = 30_000L
    private val DEBOUNCE_DIST_M = 8f
    private val DEBOUNCE_TIME_MS = 4_000L
    private val MAX_SPEED_M_S = 55.6f
    private val STATIONARY_SPEED_CUTOFF = 0.5f

    private val REQ_MIN_TIME_MS = 5_000L
    private val REQ_MIN_DIST_M = 5f

    private val WRITE_RAW_TOO = true

    private val tsFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    private var lastKept: Location? = null
    private var lastKeptWallMs: Long = 0L

    override fun onCreate() {
        super.onCreate()

        // Hard gate: if we don't have foreground + location perms, never crash; just stop.
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val hasLoc = hasFine || hasCoarse

        val hasFgLoc =
            if (Build.VERSION.SDK_INT >= 34)
                ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
            else true

        if (!hasLoc || !hasFgLoc) {
            stopSelf()
            return
        }

        createLocationFgChannelIfNeeded()
        val notif = buildLocationNotification()
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(FG_NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                @Suppress("DEPRECATION")
                startForeground(FG_NOTIF_ID, notif)
            }
        } catch (_: SecurityException) {
            // If the OS refuses the FGS start for any reason, do not crash the app.
            stopSelf()
            return
        }

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Request updates only when allowed
        if (hasLoc) {
            runCatching {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, REQ_MIN_TIME_MS, REQ_MIN_DIST_M, this)
            }
            runCatching {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, REQ_MIN_TIME_MS, REQ_MIN_DIST_M, this)
            }
        }

        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { p ->
            runCatching { lm.getLastKnownLocation(p) }.getOrNull()?.let { loc ->
                if (isFresh(loc, 10_000L) && isGoodAccuracy(loc)) {
                    lastKept = Location(loc)
                    lastKeptWallMs = nowWall()
                }
            }
        }

        ensureHeader("location_log.csv", "ts,lat,lon,accuracy")
        if (WRITE_RAW_TOO) ensureHeader("location_log_raw.csv", "ts,lat,lon,accuracy,provider")
    }

    override fun onDestroy() {
        runCatching { lm.removeUpdates(this) }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onLocationChanged(loc: Location) {
        val ts = tsFmt.format(System.currentTimeMillis())
        val acc = if (loc.hasAccuracy()) loc.accuracy else Float.MAX_VALUE

        if (WRITE_RAW_TOO) {
            appendLine("location_log_raw.csv", "$ts,${loc.latitude},${loc.longitude},${acc.toInt()},${loc.provider ?: ""}\n")
        }

        if (!isGoodAccuracy(loc)) return
        if (!isFresh(loc, 30_000L)) return
        if (!shouldKeep(loc)) return

        appendLine("location_log.csv", "$ts,${loc.latitude},${loc.longitude},${acc.toInt()}\n")
        lastKept = Location(loc)
        lastKeptWallMs = nowWall()
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    @Deprecated("Unused on modern APIs")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    private fun shouldKeep(cur: Location): Boolean {
        val prev = lastKept ?: return true
        val dtMs = max(1L, cur.time - prev.time)
        val wallDt = nowWall() - lastKeptWallMs
        val dist = cur.distanceTo(prev)
        val spd = if (cur.hasSpeed()) cur.speed else dist / (dtMs / 1000f)
        if (spd > MAX_SPEED_M_S) return false
        val providerChanged = (cur.provider ?: "") != (prev.provider ?: "")
        val accPrev = if (prev.hasAccuracy()) prev.accuracy else Float.MAX_VALUE
        val accNow = if (cur.hasAccuracy()) cur.accuracy else Float.MAX_VALUE
        val accuracyImproved = accPrev - accNow >= 8f
        if (providerChanged && accuracyImproved) return true
        if (wallDt < DEBOUNCE_TIME_MS && dist < DEBOUNCE_DIST_M) return false
        if (spd < STATIONARY_SPEED_CUTOFF && dist < DEBOUNCE_DIST_M) return false
        if (dist >= MIN_KEEP_DIST_M) return true
        if (wallDt >= MIN_KEEP_INTERVAL_MS) return true
        return false
    }

    private fun isGoodAccuracy(loc: Location): Boolean =
        loc.hasAccuracy() && loc.accuracy <= MIN_ACC_METERS

    private fun isFresh(loc: Location, maxAgeMs: Long): Boolean {
        val ageMs = if (Build.VERSION.SDK_INT >= 17 && loc.elapsedRealtimeNanos != 0L) {
            (SystemClock.elapsedRealtimeNanos() - loc.elapsedRealtimeNanos) / 1_000_000L
        } else {
            nowWall() - loc.time
        }
        return ageMs in 0..maxAgeMs
    }

    private fun nowWall(): Long = System.currentTimeMillis()

    private fun buildLocationNotification(): Notification {
        return NotificationCompat.Builder(this, FG_CHANNEL_ID)
            .setContentTitle("My Recovery Assistant")
            .setContentText("University study")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createLocationFgChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(FG_CHANNEL_ID) == null) {
            val importance = if (BuildConfig.DEBUG)
                NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            nm.createNotificationChannel(
                NotificationChannel(FG_CHANNEL_ID, "MyRecovery Tracker (Location)", importance)
            )
        }
    }

    private fun ensureHeader(name: String, header: String) {
        val f = File(filesDir, name)
        if (!f.exists() || f.length() == 0L) {
            FileOutputStream(f, false).channel.use { ch ->
                val bb = ("$header\n").toByteArray()
                ch.write(java.nio.ByteBuffer.wrap(bb)); ch.force(true)
            }
        }
    }

    private fun appendLine(name: String, line: String) {
        val f = File(filesDir, name)
        FileOutputStream(f, true).channel.use { ch ->
            val bb = line.toByteArray()
            ch.write(java.nio.ByteBuffer.wrap(bb)); ch.force(true)
        }
    }

    companion object {
        private const val FG_CHANNEL_ID = "mrt_location_fg_v1"
        private const val FG_NOTIF_ID = 2001

        fun start(ctx: Context) {
            val i = Intent(ctx, LocationCaptureService::class.java)
            ContextCompat.startForegroundService(ctx, i)
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, LocationCaptureService::class.java))
        }
    }
}