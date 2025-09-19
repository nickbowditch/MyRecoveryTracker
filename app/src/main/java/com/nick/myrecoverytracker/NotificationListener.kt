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
import java.util.concurrent.ConcurrentHashMap

class NotificationListener : NotificationListenerService() {

    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val postedAt = ConcurrentHashMap<String, Long>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            postedAt[sbn.key] = sbn.postTime
            appendCsv(
                ts = df.format(Date(sbn.postTime)),
                pkg = sbn.packageName ?: "",
                title = (sbn.notification.extras.getCharSequence("android.title") ?: "").toString(),
                text = (sbn.notification.extras.getCharSequence("android.text") ?: "").toString(),
                event = "posted",
                reason = ""
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
            val now = System.currentTimeMillis()
            val r = reasonToString(reason)
            appendCsv(
                ts = df.format(Date(now)),
                pkg = sbn.packageName ?: "",
                title = (sbn.notification.extras.getCharSequence("android.title") ?: "").toString(),
                text = (sbn.notification.extras.getCharSequence("android.text") ?: "").toString(),
                event = "removed",
                reason = r
            )
            postedAt.remove(sbn.key)
        } catch (t: Throwable) {
            Log.e(TAG, "onNotificationRemoved failed", t)
        }
    }

    private fun reasonToString(code: Int): String = when (code) {
        REASON_CLICK -> "CLICK"
        REASON_CANCEL -> "CANCEL"
        REASON_CANCEL_ALL -> "CANCEL_ALL"
        REASON_APP_CANCEL -> "APP_CANCEL"
        REASON_APP_CANCEL_ALL -> "APP_CANCEL_ALL"
        REASON_GROUP_SUMMARY_CANCELED -> "GROUP_SUMMARY_CANCELED"
        REASON_LISTENER_CANCEL -> "LISTENER_CANCEL"
        REASON_LISTENER_CANCEL_ALL -> "LISTENER_CANCEL_ALL"
        REASON_PACKAGE_BANNED -> "PACKAGE_BANNED"
        REASON_USER_STOPPED -> "USER_STOPPED"
        REASON_PACKAGE_CHANGED -> "PACKAGE_CHANGED"
        REASON_SNOOZED -> "SNOOZED"
        REASON_PROFILE_TURNED_OFF -> "PROFILE_TURNED_OFF"
        REASON_TIMEOUT -> "TIMEOUT"
        REASON_CHANNEL_BANNED -> "CHANNEL_BANNED"
        else -> "UNKNOWN"
    }

    private fun appendCsv(ts: String, pkg: String, title: String, text: String, event: String, reason: String) {
        val f = File(applicationContext.filesDir, FILE)
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("timestamp,package,title,text,event,reason\n")
        }
        FileWriter(f, true).use { w ->
            w.appendLine(
                listOf(
                    ts,
                    pkg,
                    quote(title),
                    quote(text),
                    event,
                    reason
                ).joinToString(",")
            )
        }
    }

    private fun quote(s: String): String {
        val cleaned = s.replace("\r", " ").replace("\n", " ")
        val esc = cleaned.replace("\"", "\"\"")
        return "\"$esc\""
    }

    companion object {
        private const val TAG = "NotificationListener"
        private const val FILE = "notification_log.csv"
    }
}