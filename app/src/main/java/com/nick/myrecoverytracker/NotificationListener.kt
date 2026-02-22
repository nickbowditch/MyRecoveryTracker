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

    override fun onListenerConnected() {
        super.onListenerConnected()
        ensureHeader()
        Log.i(TAG, "NotificationListener connected; header ensured for $FILE")
    }

    // Called on ALL Android versions
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
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

    // OLD signature (Android 10–12)
    @Suppress("DEPRECATION")
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        try {
            appendCsv(
                ts = df.format(Date()),
                event = "REMOVED",
                notifId = sbn.id.toString()
            )
        } catch (t: Throwable) {
            Log.e(TAG, "onNotificationRemoved(1) failed", t)
        }
    }

    // NEW signature (Android 12L+)
    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        if (sbn == null) return
        try {
            val event = if (reason == REASON_CLICK) "CLICKED" else "REMOVED"
            appendCsv(
                ts = df.format(Date()),
                event = event,
                notifId = sbn.id.toString()
            )
        } catch (t: Throwable) {
            Log.e(TAG, "onNotificationRemoved(3) failed", t)
        }
    }

    private fun ensureHeader() {
        val f = File(applicationContext.filesDir, FILE)
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("ts,event,notif_id\n")
        }
    }

    private fun appendCsv(ts: String, event: String, notifId: String) {
        val f = File(applicationContext.filesDir, FILE)
        if (!f.exists() || f.length() == 0L) {
            ensureHeader()
        }
        FileWriter(f, true).use { w ->
            w.appendLine("$ts,$event,$notifId")
        }
    }

    companion object {
        private const val TAG = "NotificationListener"
        private const val FILE = "notification_log.csv"
    }
}