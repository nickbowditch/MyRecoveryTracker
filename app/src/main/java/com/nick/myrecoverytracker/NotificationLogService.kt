package com.nick.myrecoverytracker

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationLogService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "NotificationLogService connected")
        appendLine(
            eventType = "LISTENER_CONNECTED",
            pkg = "system",
            id = -1,
            rawEvent = "CONNECTED",
            rawReason = ""
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i(TAG, "onNotificationPosted called: pkg=${sbn.packageName}, id=${sbn.id}")
        val pkg = sbn.packageName ?: "unknown"
        val id = sbn.id
        appendLine(
            eventType = "POSTED",
            pkg = pkg,
            id = id,
            rawEvent = "POSTED",
            rawReason = ""
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap?, reason: Int) {
        Log.i(TAG, "onNotificationRemoved called: pkg=${sbn.packageName}, id=${sbn.id}, reason=$reason")
        val pkg = sbn.packageName ?: "unknown"
        val id = sbn.id

        val reasonStr = when (reason) {
            REASON_CLICK -> "CLICK"
            REASON_APP_CANCEL -> "APP_CANCEL"
            REASON_TIMEOUT -> "TIMEOUT"
            REASON_CANCEL -> "CANCEL"
            REASON_CANCEL_ALL -> "CANCEL_ALL"
            REASON_ERROR -> "ERROR"
            else -> "OTHER"
        }

        appendLine(
            eventType = "REMOVED",
            pkg = pkg,
            id = id,
            rawEvent = "REMOVED",
            rawReason = reasonStr
        )
    }

    private fun appendLine(
        eventType: String,
        pkg: String,
        id: Int,
        rawEvent: String,
        rawReason: String
    ) {
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
        private const val HEADER =
            "ts,event_type,package_name,notification_id,raw_event,raw_reason"

        private val TS_FMT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    }
}