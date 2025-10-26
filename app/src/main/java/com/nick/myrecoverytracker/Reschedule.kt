package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager

object Reschedule {
    private const val PREF = "upgrade_flags"
    private const val KEY_NEEDS = "needs_reschedule"

    fun markNeeded(ctx: Context) {
        DirectBoot.deviceProtectedContext(ctx)
            .getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_NEEDS, true).apply()
    }

    fun runNow(ctx: Context) {
        try {
            val app = ctx.applicationContext
            val wm = WorkManager.getInstance(app)

            val unique = listOf(
                "mrt_notification_daily",
                "rollups-and-upload",
                "once-NotificationLatencyRollup",
                "once-NotificationEngagementRollup",
                "once-DistanceDaily",
                "once-SleepDurationDaily",
                "once-DailySummary"
            )

            try {
                WorkScheduler.registerAllDaily(app)
            } catch (t: Throwable) {
                Log.w("Reschedule", "registerAllDaily failed: ${t.message}")
            }

            DirectBoot.deviceProtectedContext(app)
                .getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_NEEDS, false).apply()

            Log.i("Reschedule", "Reschedule complete")
        } catch (t: Throwable) {
            Log.e("Reschedule", "Reschedule error", t)
        }
    }

    fun runIfNeededOnAppStart(ctx: Context) {
        val dp = DirectBoot.deviceProtectedContext(ctx)
        val needs = dp.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_NEEDS, false)
        if (needs || shouldAuditAfterUpgrade(ctx)) {
            runNow(ctx)
        }
    }

    private fun shouldAuditAfterUpgrade(ctx: Context): Boolean = false
}