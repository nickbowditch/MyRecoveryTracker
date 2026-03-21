// app/src/main/java/com/nick/myrecoverytracker/DebugExportReceiver.kt
package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.io.File

class DebugExportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val action = intent?.action ?: return

        when (action) {
            "com.nick.myrecoverytracker.ACTION_DEBUG_EXPORT" -> {
                exportDebugFiles(context)
            }
            "com.nick.myrecoverytracker.ACTION_RUN_WORKER" -> {
                val workerName = intent.getStringExtra("worker") ?: return
                triggerWorker(context, workerName)
            }
        }
    }

    private fun triggerWorker(context: Context, workerName: String) {
        try {
            val workerClass = Class.forName("com.nick.myrecoverytracker.$workerName")
                .asSubclass(androidx.work.ListenableWorker::class.java)

            val request = OneTimeWorkRequest.Builder(workerClass).build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "debug-$workerName-${System.currentTimeMillis()}",
                androidx.work.ExistingWorkPolicy.KEEP,
                request
            )

            Log.i("DebugExport", "Triggered worker: $workerName (workId=${request.id})")
        } catch (e: Exception) {
            Log.e("DebugExport", "Failed to trigger $workerName: ${e.message}", e)
        }
    }

    private fun exportDebugFiles(context: Context) {
        try {
            val debugDir = context.getExternalFilesDir(null)
                ?: throw IllegalStateException("External files dir is null")

            debugDir.mkdirs()

            val filesToExport = listOf(
                "location_log.csv",
                "location_log_raw.csv",
                "daily_distance_log.csv",
                "daily_app_usage_minutes.csv",
                "daily_summary.csv",
                "heartbeat.csv",
                "daily_unlocks.csv"
            )

            for (filename in filesToExport) {
                val sourceFile = File(context.filesDir, filename)
                if (sourceFile.exists()) {
                    val destFile = File(debugDir, "${filename.substringBeforeLast(".")}_debug.csv")
                    sourceFile.copyTo(destFile, overwrite = true)
                    Log.d("DebugExport", "Exported: ${destFile.absolutePath} (${destFile.length()} bytes)")
                } else {
                    Log.w("DebugExport", "File not found: $filename")
                }
            }

            Log.i("DebugExport", "Debug export complete to: ${debugDir.absolutePath}")

        } catch (e: Exception) {
            Log.e("DebugExport", "Debug export failed: ${e.message}", e)
        }
    }
}