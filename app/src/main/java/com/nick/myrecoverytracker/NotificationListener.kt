// app/src/main/java/com/nick/myrecoverytracker/NotificationListener.kt
package com.nick.myrecoverytracker

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationListener : NotificationListenerService() {

    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            appendCsv(
                ts = df.format(Date(sbn.postTime)),
                event = "POSTED",
                notifId = sbn.id.toString()
            )
        } catch (t: Throwable) {
            Log.e(TAG, "onNotificationPosted failed", t)
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        try {
            // Treat user click as an "OPENED"
            val opened = reason == REASON_CLICK
            if (opened) {
                appendCsv(
                    ts = df.format(Date(System.currentTimeMillis())),
                    event = "CLICKED",
                    notifId = sbn.id.toString()
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "onNotificationRemoved failed", t)
        }
    }

    private fun appendCsv(ts: String, event: String, notifId: String) {
        val f = File(applicationContext.filesDir, FILE)
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("ts,event,notif_id\n")
        }
        FileWriter(f, true).use { w ->
            w.appendLine(listOf(ts, event, notifId).joinToString(","))
        }
    }

    companion object {
        private const val TAG = "NotificationListener"
        private const val FILE = "notification_log.csv"
    }
}