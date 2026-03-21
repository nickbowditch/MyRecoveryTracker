package com.nick.myrecoverytracker

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

class LocationCaptureService : Service(), LocationListener {

    private lateinit var lm: LocationManager

    private val MIN_ACC_METERS = 100f
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

        Log.i(TAG, "🔵 onCreate() called")

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

        Log.i(TAG, "🔑 Permissions: hasFine=$hasFine, hasCoarse=$hasCoarse, hasFgLoc=$hasFgLoc")

        if (!hasLoc || !hasFgLoc) {
            Log.w(TAG, "❌ Missing required permissions, stopping service")
            stopSelf()
            return
        }

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (hasLoc) {
            Log.i(TAG, "🔵 Attempting to register location listeners...")

            runCatching {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, REQ_MIN_TIME_MS, REQ_MIN_DIST_M, this)
                Log.i(TAG, "✅ GPS_PROVIDER listener registered successfully")
            }.onFailure { e ->
                Log.e(TAG, "❌ GPS_PROVIDER registration FAILED: ${e.message}", e)
            }

            runCatching {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, REQ_MIN_TIME_MS, REQ_MIN_DIST_M, this)
                Log.i(TAG, "✅ NETWORK_PROVIDER listener registered successfully")
            }.onFailure { e ->
                Log.e(TAG, "❌ NETWORK_PROVIDER registration FAILED: ${e.message}", e)
            }

            Log.i(TAG, "🔵 Listener registration complete. Waiting for callbacks...")
        }

        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { p ->
            runCatching { lm.getLastKnownLocation(p) }.getOrNull()?.let { loc ->
                if (isFresh(loc, 10_000L) && isGoodAccuracy(loc)) {
                    lastKept = Location(loc)
                    lastKeptWallMs = nowWall()
                    Log.i(TAG, "📍 Initialized lastKept from $p")
                }
            }
        }

        ensureDataDir()
        ensureHeader("location_log.csv", "ts,lat,lon,acc")
        if (WRITE_RAW_TOO) ensureHeader("location_log_raw.csv", "ts,lat,lon,accuracy,provider")
        Log.i(TAG, "✅ onCreate() complete")
    }

    override fun onDestroy() {
        Log.i(TAG, "🔴 onDestroy() called, removing location updates")
        runCatching { lm.removeUpdates(this) }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "🔵 onStartCommand() called")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onLocationChanged(loc: Location) {
        Log.i(TAG, "📍 onLocationChanged() called! provider=${loc.provider}, lat=${loc.latitude}, lon=${loc.longitude}, accuracy=${loc.accuracy}")

        val ts = tsFmt.format(System.currentTimeMillis())
        val acc = if (loc.hasAccuracy()) loc.accuracy else Float.MAX_VALUE

        if (WRITE_RAW_TOO) {
            appendLine("location_log_raw.csv", "$ts,${loc.latitude},${loc.longitude},${acc.toInt()},${loc.provider ?: ""}\n")
            Log.i(TAG, "✅ Wrote to location_log_raw.csv")
        }

        if (!isGoodAccuracy(loc)) {
            Log.d(TAG, "⚠️ Rejected: poor accuracy ($acc > $MIN_ACC_METERS)")
            return
        }
        if (!isFresh(loc, 30_000L)) {
            Log.d(TAG, "⚠️ Rejected: stale location")
            return
        }
        if (!shouldKeep(loc)) {
            Log.d(TAG, "⚠️ Rejected: shouldKeep() returned false")
            return
        }

        appendLine("location_log.csv", "$ts,${loc.latitude},${loc.longitude},${acc.toInt()}\n")
        Log.i(TAG, "✅ Wrote to location_log.csv")
        lastKept = Location(loc)
        lastKeptWallMs = nowWall()
    }

    override fun onProviderEnabled(provider: String) {
        Log.i(TAG, "✅ Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.w(TAG, "⚠️ Provider disabled: $provider")
    }

    @Deprecated("Unused on modern APIs")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d(TAG, "📊 onStatusChanged: provider=$provider, status=$status")
    }

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

    private fun ensureDataDir() {
        try {
            val dataDir = File(getExternalFilesDir(null), "data")
            if (!dataDir.exists()) {
                dataDir.mkdirs()
                Log.d(TAG, "Created data directory")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "ensureDataDir failed", t)
        }
    }

    private fun ensureHeader(name: String, header: String) {
        try {
            val f = File(File(getExternalFilesDir(null), "data"), name)
            if (!f.exists() || f.length() == 0L) {
                f.writeText("$header\n")
                Log.d(TAG, "Created $name with header")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "ensureHeader failed for $name", t)
        }
    }

    private fun appendLine(name: String, line: String) {
        try {
            val f = File(File(getExternalFilesDir(null), "data"), name)
            f.appendText(line)
        } catch (t: Throwable) {
            Log.e(TAG, "appendLine failed for $name", t)
        }
    }

    companion object {
        private const val TAG = "LocationCaptureService"

        fun start(ctx: Context) {
            val i = Intent(ctx, LocationCaptureService::class.java)
            ctx.startService(i)
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, LocationCaptureService::class.java))
        }
    }
}