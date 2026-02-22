package com.nick.myrecoverytracker

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class UsageAccessDiagWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val day = fmt.format(System.currentTimeMillis())

            val aom = applicationContext.getSystemService(Context.APP_OPS_SERVICE)
                    as AppOpsManager

            val mode = aom.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                applicationContext.packageName
            )

            val modeName = when (mode) {
                AppOpsManager.MODE_ALLOWED -> "MODE_ALLOWED"
                AppOpsManager.MODE_IGNORED -> "MODE_IGNORED"
                AppOpsManager.MODE_DEFAULT -> "MODE_DEFAULT"
                AppOpsManager.MODE_ERRORED -> "MODE_ERRORED"
                else -> "MODE_$mode"
            }

            val status =
                if (mode == AppOpsManager.MODE_ALLOWED) "OK"
                else "USAGE_PERMISSION_REVOKED"

            val f = File(applicationContext.filesDir, "usage_diag.csv")
            if (!f.exists() || f.length() == 0L) {
                f.writeText("date,mode,status\n")
            }

            val lines = f.readLines().toMutableList()
            val row = "$day,$modeName,$status"

            var replaced = false
            for (i in 1 until lines.size) {
                if (lines[i].startsWith("$day,")) {
                    lines[i] = row
                    replaced = true
                    break
                }
            }
            if (!replaced) {
                lines.add(row)
            }

            f.writeText(lines.joinToString("\n") + "\n")

            Log.i("UsageAccessDiagWorker", "diag: $row")
            Result.success()

        } catch (e: Exception) {
            Log.e("UsageAccessDiagWorker", "failure", e)
            Result.retry()
        }
    }
}