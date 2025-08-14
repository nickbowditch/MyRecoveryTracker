package com.nick.myrecoverytracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat

object Notifier {
    private const val CHANNEL_ID = "onboarding_support_channel"
    private const val CHANNEL_NAME = "Onboarding"
    const val ID_ONBOARDING = 2001

    private fun ensureChannel(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing == null) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Prompts you to add your support contacts"
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(false)
                setShowBadge(false)
            }
            nm.createNotificationChannel(ch)
        }
    }

    fun showPersistentOnboarding(ctx: Context) {
        ensureChannel(ctx)

        val intent = Intent(ctx, OnboardingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val contentPi = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif: Notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Add your support contacts")
            .setContentText("Tap to add 1–5 numbers you consider safe.")
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentPi)
            .build()

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ID_ONBOARDING, notif)
    }

    fun cancelOnboarding(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(ID_ONBOARDING)
    }
}