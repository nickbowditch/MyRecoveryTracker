package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RedcapDebugReceiver : BroadcastReceiver() {

    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.e("RedcapDebugReceiver", "REDCAP RECEIVER FIRED action=$action")

        when (action) {
            "com.nick.myrecoverytracker.ACTION_RUN_REDCAP_UPLOAD",
            "com.nick.myrecoverytracker.DEBUG_FORCE_UPLOAD" -> {
                logRedcapDiag(context, "RECEIVED_$action")
                enqueueRedcapUpload(context)
            }
        }
    }

    private fun enqueueRedcapUpload(ctx: Context) {
        val work = OneTimeWorkRequestBuilder<RedcapUploadWorker>()
            .addTag("REDCAP_UPLOAD_DEBUG")
            .build()

        WorkManager.getInstance(ctx).enqueue(work)
        Log.e("RedcapDebugReceiver", "REDCAP UPLOAD WORK ENQUEUED")
    }

    private fun logRedcapDiag(ctx: Context, event: String) {
        try {
            val file = File(ctx.filesDir, "redcap_diag.csv")

            if (!file.exists() || file.length() == 0L) {
                FileOutputStream(file, false).use {
                    it.write("ts,event\n".toByteArray())
                }
            }

            val ts = fmt.format(System.currentTimeMillis())
            FileOutputStream(file, true).use {
                it.write("$ts,$event\n".toByteArray())
            }
        } catch (_: Throwable) {
            // diagnostics only
        }
    }
}