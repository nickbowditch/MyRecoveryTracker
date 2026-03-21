// app/src/main/java/com/nick/myrecoverytracker/ForegroundUnlockService.kt
package com.nick.myrecoverytracker

import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ForegroundUnlockService : Service() {

    private lateinit var km: KeyguardManager
    private lateinit var pm: PowerManager
    private lateinit var mainHandler: Handler

    private val CHANNEL_ID = "mrt_device_state"
    private val TAG = "ForegroundUnlockService"

    private val tsMinFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    private val tsLogFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    private var lastHeartbeat = ""
    private var currentSessionId = 0
    private var currentSessionLogged = false
    private val pendingChecks = mutableListOf<Runnable>()

    private var hbThread: HandlerThread? = null
    private var hbHandler: Handler? = null

    private val hbRunnable = object : Runnable {
        override fun run() {
            try {
                ensureHeader("heartbeat.csv", "ts,service_status")
                val ts = tsMinFmt.format(System.currentTimeMillis())
                if (ts != lastHeartbeat) {
                    val locationServiceRunning = isLocationServiceRunning()
                    val status = if (locationServiceRunning) "LocationCaptureService" else "NO_SERVICE"
                    appendLine("heartbeat.csv", "$ts,$status\n")
                    lastHeartbeat = ts
                }
            } catch (_: Throwable) {}
            hbHandler?.postDelayed(this, 60_000L)
        }
    }

    private fun isLocationServiceRunning(): Boolean {
        return try {
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            manager.getRunningServices(Integer.MAX_VALUE).any {
                it.service.className == "com.nick.myrecoverytracker.LocationCaptureService"
            }
        } catch (e: Exception) {
            Log.e(TAG, "isLocationServiceRunning error", e)
            false
        }
    }

    private fun nowStr() = tsLogFmt.format(System.currentTimeMillis())

    @Synchronized
    private fun ensureHeader(name: String, header: String) {
        try {
            val filesDir = StorageHelper.getDataDir(baseContext)
            val f = File(filesDir, name)
            Log.d(TAG, "ensureHeader: $name at ${f.absolutePath}, exists=${f.exists()}, length=${if (f.exists()) f.length() else -1}")
            if (!f.exists() || f.length() == 0L) {
                FileOutputStream(f, false).use {
                    it.write("$header\n".toByteArray())
                    it.fd.sync()
                }
                Log.d(TAG, "ensureHeader: wrote header to $name")
            }
        } catch (e: Exception) {
            Log.e(TAG, "ensureHeader failed for $name", e)
        }
    }

    @Synchronized
    private fun appendLine(name: String, line: String) {
        try {
            val filesDir = StorageHelper.getDataDir(baseContext)
            val f = File(filesDir, name)
            Log.d(TAG, "appendLine: $name, line length=${line.length}")
            FileOutputStream(f, true).use {
                it.write(line.toByteArray())
                it.fd.sync()
            }
            Log.d(TAG, "appendLine: wrote to $name, new size=${f.length()}")
        } catch (e: Exception) {
            Log.e(TAG, "appendLine failed for $name", e)
        }
    }

    private fun logScreen(on: Boolean) {
        Log.d(TAG, "logScreen: $on")
        ensureHeader("screen_log.csv", "ts,state")
        appendLine("screen_log.csv", "${nowStr()},${if (on) "ON" else "OFF"}\n")
    }

    private fun logUnlock(reason: String) {
        Log.d(TAG, "logUnlock: $reason, currentSessionLogged=$currentSessionLogged")
        try {
            if (currentSessionLogged) {
                Log.d(TAG, "logUnlock: returning early, already logged")
                return
            }
            currentSessionLogged = true
            Log.d(TAG, "logUnlock: about to ensureHeader unlock_log.csv")
            ensureHeader("unlock_log.csv", "ts,event")
            Log.d(TAG, "logUnlock: about to appendLine unlock_log.csv")
            appendLine("unlock_log.csv", "${nowStr()},UNLOCK\n")
            Log.d(TAG, "logUnlock: about to ensureHeader unlock_diag.csv")
            ensureHeader("unlock_diag.csv", "ts,tag,extra")
            Log.d(TAG, "logUnlock: about to appendLine unlock_diag.csv")
            appendLine("unlock_diag.csv", "${nowStr()},UNLOCK,$reason\n")
            Log.d(TAG, "logUnlock: about to cancelPendingChecks")
            cancelPendingChecks()
            Log.d(TAG, "logUnlock: completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "logUnlock: EXCEPTION", e)
        }
    }

    private fun cancelPendingChecks() {
        pendingChecks.forEach { mainHandler.removeCallbacks(it) }
        pendingChecks.clear()
    }

    private fun startNewSession() {
        cancelPendingChecks()
        currentSessionId++
        currentSessionLogged = false
    }

    private fun scheduleChecks() {
        val session = currentSessionId
        fun sched(delay: Long) {
            val r = Runnable {
                if (session != currentSessionId || currentSessionLogged) return@Runnable
                if (pm.isInteractive && !km.isKeyguardLocked) {
                    logUnlock("CHECK")
                }
            }
            pendingChecks.add(r)
            mainHandler.postDelayed(r, delay)
        }
        sched(0)
        sched(500)
        sched(1500)
        sched(3000)
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            Log.d(TAG, "screenReceiver.onReceive: ${intent.action}")
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> {
                    logScreen(true)
                    startNewSession()
                    if (pm.isInteractive && !km.isKeyguardLocked) logUnlock("SCREEN_ON")
                    scheduleChecks()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    logScreen(false)
                    cancelPendingChecks()
                }
            }
        }
    }

    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            Log.d(TAG, "userPresentReceiver.onReceive: ${intent.action}")
            if (intent.action == Intent.ACTION_USER_PRESENT) {
                currentSessionLogged = false
                logUnlock("USER_PRESENT")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate, filesDir=${StorageHelper.getDataDir(baseContext).absolutePath}")

        km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mainHandler = Handler(Looper.getMainLooper())

        createNotificationChannel()
        Log.d(TAG, "🔔 About to call startForeground(1001, ...)")
        val notification = buildNotification()
        Log.d(TAG, "🔔 Notification built: $notification")
        try {
            ServiceCompat.startForeground(
                this,
                1001,
                notification,
                1  // FOREGROUND_SERVICE_TYPE_LOCATION
            )
            Log.d(TAG, "✅ startForeground() succeeded with location type")
        } catch (e: Exception) {
            Log.e(TAG, "❌ startForeground() failed", e)
        }

        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        val userPresentFilter = IntentFilter(Intent.ACTION_USER_PRESENT)

        if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.registerReceiver(this, screenReceiver, screenFilter, Context.RECEIVER_NOT_EXPORTED)
            ContextCompat.registerReceiver(this, userPresentReceiver, userPresentFilter, ContextCompat.RECEIVER_EXPORTED)
        } else {
            registerReceiver(screenReceiver, screenFilter)
            registerReceiver(userPresentReceiver, userPresentFilter)
        }
        Log.d(TAG, "screenReceiver and userPresentReceiver registered")

        hbThread = HandlerThread("mrt-heartbeat").also { it.start() }
        hbHandler = Handler(hbThread!!.looper)
        hbHandler?.post(hbRunnable)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        unregisterReceiver(screenReceiver)
        unregisterReceiver(userPresentReceiver)
        cancelPendingChecks()
        hbHandler?.removeCallbacksAndMessages(null)
        hbThread?.quitSafely()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Device State Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            ch.setShowBadge(false)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("My Recovery Assistant")
            .setContentText("University Study")
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

    companion object {
        fun start(ctx: Context) {
            val intent = Intent(ctx, ForegroundUnlockService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }
    }
}