// app/src/main/java/com/nick/myrecoverytracker/ForegroundUnlockService.kt
package com.nick.myrecoverytracker

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import androidx.core.app.NotificationCompat
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

    private val tsMinFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }
    private var lastHeartbeat: String = ""

    private val tsLogFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

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
            } catch (_: Throwable) { }
            hbHandler?.postDelayed(this, 60_000L)
        }
    }

    private fun nowStr(ms: Long = System.currentTimeMillis()) = tsLogFmt.format(ms)

    @Synchronized
    private fun ensureHeader(name: String, header: String) {
        val f = File(filesDir, name)
        if (!f.exists() || f.length() == 0L) {
            FileOutputStream(f, false).channel.use { ch ->
                ch.write(("$header\n").toByteArray().let { java.nio.ByteBuffer.wrap(it) })
                ch.force(true)
            }
        }
    }

    @Synchronized
    private fun appendLine(name: String, line: String) {
        val f = File(filesDir, name)
        FileOutputStream(f, true).channel.use { ch ->
            ch.write(line.toByteArray().let { java.nio.ByteBuffer.wrap(it) })
            ch.force(true)
        }
    }

    private fun logDiag(tag: String) {
        ensureHeader("unlock_diag.csv", "ts,tag,extra")
        appendLine("unlock_diag.csv", "${nowStr()},$tag,\n")
    }

    private fun logDiag(tag: String, extra: String) {
        ensureHeader("unlock_diag.csv", "ts,tag,extra")
        val t = tag.replace("\"", "\"\"")
        val e = extra.replace("\"", "\"\"")
        appendLine("unlock_diag.csv", "${nowStr()},\"$t\",\"$e\"\n")
    }

    private fun logScreen(on: Boolean) {
        ensureHeader("screen_log.csv", "ts,state")
        appendLine("screen_log.csv", "${nowStr()},${if (on) "ON" else "OFF"}\n")
    }

    private fun logUnlockForSession(reason: String) {
        if (currentSessionLogged) return
        currentSessionLogged = true
        ensureHeader("unlock_log.csv", "ts,event")
        appendLine("unlock_log.csv", "${nowStr()},UNLOCK\n")
        logDiag("UNLOCK", reason)
        cancelPendingChecks()
    }

    private fun startNewSession() {
        cancelPendingChecks()
        currentSessionId += 1
        currentSessionLogged = false
    }

    private fun cancelPendingChecks() {
        if (pendingChecks.isEmpty()) return
        pendingChecks.forEach { mainHandler.removeCallbacks(it) }
        pendingChecks.clear()
    }

    private fun scheduleChecksForCurrentSession() {
        val session = currentSessionId
        fun schedule(delayMs: Long, label: String, immediate: Boolean = false) {
            val r = Runnable {
                if (session != currentSessionId || currentSessionLogged) return@Runnable
                val interactive = if (Build.VERSION.SDK_INT >= 20) pm.isInteractive else true
                val locked = km.isKeyguardLocked
                val note = "interactive=$interactive,locked=$locked"
                if (immediate) logDiag("CHECK,immediate", note) else logDiag("CHECK,t+$label", note)
                if (interactive && !locked) {
                    logUnlockForSession(if (immediate) "CHECK,immediate" else "CHECK,t+$label")
                }
            }
            pendingChecks.add(r)
            mainHandler.postDelayed(r, delayMs)
        }
        schedule(0L, "0", immediate = true)
        schedule(120L, "120")
        schedule(300L, "300")
        schedule(800L, "800")
        schedule(1200L, "1200")
        schedule(1500L, "1500")
        schedule(3000L, "3000")
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_USER_PRESENT -> {
                    logUnlockForSession("USER_PRESENT")
                }
                Intent.ACTION_SCREEN_ON -> {
                    logScreen(true)
                    startNewSession()
                    val interactive = if (Build.VERSION.SDK_INT >= 20) pm.isInteractive else true
                    val locked = km.isKeyguardLocked
                    logDiag("SCREEN_ON", "locked=$locked")
                    if (interactive && !locked) {
                        logUnlockForSession("SCREEN_ON_immediate")
                    }
                    scheduleChecksForCurrentSession()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    logScreen(false)
                    cancelPendingChecks()
                    currentSessionLogged = false
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mainHandler = Handler(Looper.getMainLooper())
        MovementCapture.attach(this)

        createChannel()
        startForeground(NOTIF_ID, buildNotification())

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.registerReceiver(this, screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(screenReceiver, filter)
        }

        hbThread = HandlerThread("mrt-heartbeat").also { it.start() }
        hbHandler = Handler(hbThread!!.looper)
        hbHandler?.post(hbRunnable)

        ensureHeader("unlock_log.csv", "ts,event")
        ensureHeader("screen_log.csv", "ts,state")
        ensureHeader("unlock_diag.csv", "ts,tag,extra")
        ensureHeader("heartbeat.csv", "ts")
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
        cancelPendingChecks()
        hbHandler?.removeCallbacksAndMessages(null)
        hbThread?.quitSafely()
        hbHandler = null
        hbThread = null
        MovementCapture.detach()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val importance =
                    if (BuildConfig.DEBUG) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
                val ch = NotificationChannel(CHANNEL_ID, "MyRecovery Tracker (Foreground)", importance)
                nm.createNotificationChannel(ch)
            }
        }
    }

    private fun buildNotification(): Notification {
        val priority = if (BuildConfig.DEBUG) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MyRecoveryAssistant")
            .setContentText("University study is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(priority)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "mrt_fg_channel_v4"
        private const val NOTIF_ID = 1001
    }
}