package com.nick.myrecoverytracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationUtils {
    private const val CHANNEL_ID = "perm_reminders"
    private const val CHANNEL_NAME = "Permission Reminders"
    private const val NID_RESTRICTED = 2001

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
                )
            }
        }
    }

    fun postRestrictedPermsReminder(context: Context) {
        // If notifications are disabled (or permission denied on 33+), don't bother posting
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        if (!UsagePermissionHelper.needsRestricted(context)) return

        ensureChannel(context)

        val pi = PendingIntent.getActivity(
            context,
            0,
            Intent(context, OnboardingActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Enable SMS & Call Logs")
            .setContentText("Tap to grant access. You can do it later anytime.")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NID_RESTRICTED, notif)
    }
}