package com.nick.myrecoverytracker

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class NotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        Log.i("NotifListener", "🔔 Notification listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // ✅ Ignore system packages
        if (packageName.startsWith("com.android.") || packageName.startsWith("com.google.android")) {
            Log.d("NotifListener", "⛔ Skipping system package: $packageName")
            return
        }

        val title = sbn.notification.extras.getString("android.title") ?: "No Title"
        val text = sbn.notification.extras.getCharSequence("android.text")?.toString() ?: "No Text"
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        Log.i("NotifListener", "📲 [$packageName] $title — $text")
        MetricsStore.appendNotificationLog(applicationContext, timestamp, packageName, title, text)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.i("NotifListener", "🗑️ Removed: ${sbn.packageName}")
    }
}