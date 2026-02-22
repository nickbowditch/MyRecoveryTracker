package com.nick.myrecoverytracker.notifications

import android.content.Context
import java.io.File

private const val NOTIF_EVENTS_FILE = "notification_events.csv"
private const val DAILY_NOTIF_FILE  = "daily_notification_engagement.csv"
private const val NOTIF_HEARTBEAT   = "notification_heartbeat.csv"

fun notifEventsFile(context: Context): File =
    File(context.filesDir, NOTIF_EVENTS_FILE)

fun dailyNotifFile(context: Context): File =
    File(context.filesDir, DAILY_NOTIF_FILE)

fun notifHeartbeatFile(context: Context): File =
    File(context.filesDir, NOTIF_HEARTBEAT)