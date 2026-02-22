package com.nick.myrecoverytracker

import android.app.*
import android.content.*
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ForegroundUnlockService : Service() {

    private lateinit var km: KeyguardManager
    private lateinit var pm: PowerManager
    private lateinit var mainHandler: Handler

    private val CHANNEL_ID = "mrt_device_state"

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
                ensureHeader("heartbeat.csv", "ts")
                val ts = tsMinFmt.format(System.currentTimeMillis())
                if (ts != lastHeartbeat) {
                    appendLine("heartbeat.csv", "$ts\n")
                    lastHeartbeat = ts
                }
            } catch (_: Throwable) {}
            hbHandler?.postDelayed(this, 60_000L)
        }
    }

    private fun nowStr() = tsLogFmt.format(System.currentTimeMillis())

    @Synchronized
    private fun ensureHeader(name: String, header: String) {
        val f = File(filesDir, name)
        if (!f.exists() || f.length() == 0L) {
            FileOutputStream(f, false).use {
                it.write("$header\n".toByteArray())
                it.fd.sync()
            }
        }
    }

    @Synchronized
    private fun appendLine(name: String, line: String) {
        FileOutputStream(File(filesDir, name), true).use {
            it.write(line.toByteArray())
            it.fd.sync()
        }
    }

    private fun logScreen(on: Boolean) {
        ensureHeader("screen_log.csv", "ts,state")
        appendLine("screen_log.csv", "${nowStr()},${if (on) "ON" else "OFF"}\n")
    }

    private fun logUnlock(reason: String) {
        if (currentSessionLogged) return
        currentSessionLogged = true
        ensureHeader("unlock_log.csv", "ts,event")
        appendLine("unlock_log.csv", "${nowStr()},UNLOCK\n")
        ensureHeader("unlock_diag.csv", "ts,tag,extra")
        appendLine("unlock_diag.csv", "${nowStr()},UNLOCK,$reason\n")
        cancelPendingChecks()
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
                Intent.ACTION_USER_PRESENT -> logUnlock("USER_PRESENT")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mainHandler = Handler(Looper.getMainLooper())

        createNotificationChannel()
        startForeground(1001, buildNotification())

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.registerReceiver(this, screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }

        hbThread = HandlerThread("mrt-heartbeat").also { it.start() }
        hbHandler = Handler(hbThread!!.looper)
        hbHandler?.post(hbRunnable)
    }

    override fun onDestroy() {
        unregisterReceiver(screenReceiver)
        cancelPendingChecks()
        hbHandler?.removeCallbacksAndMessages(null)
        hbThread?.quitSafely()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Device State Tracking",
                NotificationManager.IMPORTANCE_MIN
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("My Recovery Assistant")
            .setContentText("Research Study")
            .setOngoing(true)
            .build()
}