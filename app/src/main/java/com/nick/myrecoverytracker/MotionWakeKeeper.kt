package com.nick.myrecoverytracker

import android.content.Context
import android.os.PowerManager
import java.util.concurrent.atomic.AtomicInteger

object MotionWakeKeeper {
    private const val TAG = "MRT:MotionWake"
    private const val TIMEOUT_MS = 2 * 60 * 1000L // 2 minutes cap
    private var wl: PowerManager.WakeLock? = null
    private val holds = AtomicInteger(0)

    @Synchronized
    fun onMotionStart(ctx: Context) {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (wl == null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$TAG").apply { setReferenceCounted(true) }
        }
        wl?.acquire(TIMEOUT_MS) // auto-release safeguard
        holds.incrementAndGet()
    }

    @Synchronized
    fun onMotionStop() {
        if (holds.get() > 0) holds.decrementAndGet()
        if (holds.get() == 0) {
            runCatching { wl?.release() }
        }
    }
}