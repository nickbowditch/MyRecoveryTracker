package com.nick.myrecoverytracker

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class NotificationLogService : NotificationListenerService() {

    private val activeOnConnect = mutableSetOf<String>()
    private val lastPostedTime = ConcurrentHashMap<String, Long>()

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "NotificationLogService connected")
        try {
            val active = activeNotifications ?: emptyArray()
            activeOnConnect.clear()
            for (sbn in active) {
                activeOnConnect.add(notifKey(sbn.packageName, sbn.id))
            }
            Log.i(TAG, "Suppressed ${activeOnConnect.size} pre-existing notifications on connect")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to snapshot active notifications on connect", t)
        }
        appendLine(eventType = "LISTENER_CONNECTED", pkg = "system", id = -1, rawEvent = "CONNECTED", rawReason = "")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: "unknown"
        val id = sbn.id
        val key = notifKey(pkg, id)

        if (activeOnConnect.remove(key)) {
            Log.i(TAG, "Suppressed reconnect re-fire: $key")
            return
        }

        val now = System.currentTimeMillis()
        val last = lastPostedTime[key] ?: 0L
        if (now - last < BURST_WINDOW_MS) {
            Log.i(TAG, "Suppressed burst duplicate: $key (${now - last}ms since last)")
            return
        }
        lastPostedTime[key] = now

        Log.i(TAG, "onNotificationPosted: pkg=$pkg, id=$id")
        appendLine(eventType = "POSTED", pkg = pkg, id = id, rawEvent = "POSTED", rawReason = "")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap?, reason: Int) {
        val pkg = sbn.packageName ?: "unknown"
        val id = sbn.id
        val key = notifKey(pkg, id)

        activeOnConnect.remove(key)
        lastPostedTime.remove(key)

        val reasonStr = when (reason) {
            REASON_CLICK      -> "CLICK"
            REASON_APP_CANCEL -> "APP_CANCEL"
            REASON_TIMEOUT    -> "TIMEOUT"
            REASON_CANCEL     -> "CANCEL"
            REASON_CANCEL_ALL -> "CANCEL_ALL"
            REASON_ERROR      -> "ERROR"
            else              -> "OTHER"
        }

        Log.i(TAG, "onNotificationRemoved: pkg=$pkg, id=$id, reason=$reasonStr")
        appendLine(eventType = "REMOVED", pkg = pkg, id = id, rawEvent = "REMOVED", rawReason = reasonStr)
    }

    private fun notifKey(pkg: String, id: Int) = "$pkg:$id"

    private fun appendLine(eventType: String, pkg: String, id: Int, rawEvent: String, rawReason: String) {
        try {
            val dir = File(getExternalFilesDir(null), "data")
            val logFile = File(dir, LOG_FILE)
            if (!logFile.exists()) {
                logFile.parentFile?.mkdirs()
                logFile.writeText(HEADER + "\n")
            }
            val ts = TS_FMT.format(Date())
            val line = buildString {
                append(ts).append(',')
                append(eventType).append(',')
                append(pkg.replace(",", "_")).append(',')
                append(id).append(',')
                append(rawEvent).append(',')
                append(rawReason)
            }
            logFile.appendText(line + "\n")
            Log.i(TAG, "Logged: $line")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to append notification log line", t)
        }
    }

    companion object {
        private const val TAG = "NotificationLogService"
        private const val LOG_FILE = "notification_log.csv"
        private const val BURST_WINDOW_MS = 3_000L
        private const val HEADER = "ts,event_type,package_name,notification_id,raw_event,raw_reason"
        private val TS_FMT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    }
}
