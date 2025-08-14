package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dev triggers (Bluetooth removed for this build):
 *  - TEST_CALL_TOTALS          -> enqueue CallTotalsWorker
 *  - TEST_DISTANCE_SUMMARY     -> enqueue DistanceSummaryWorker
 *  - TEST_LOCATION_SAMPLE      -> append 2 rows to location_log.csv for today
 */
class TestWorkersReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TEST_CALL_TOTALS -> {
                Log.i(TAG, "▶️ Enqueue CallTotalsWorker (expedited)")
                val req: WorkRequest = OneTimeWorkRequestBuilder<CallTotalsWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
                WorkManager.getInstance(context).enqueue(req)
            }

            ACTION_TEST_DISTANCE_SUMMARY -> {
                Log.i(TAG, "▶️ Enqueue DistanceSummaryWorker (expedited)")
                val req: WorkRequest = OneTimeWorkRequestBuilder<DistanceSummaryWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
                WorkManager.getInstance(context).enqueue(req)
            }

            ACTION_TEST_LOCATION_SAMPLE -> {
                val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val file = File(context.filesDir, "location_log.csv")
                file.parentFile?.mkdirs()
                file.appendText("$day 09:00:00,-33.4311,151.4326,10.0\n")
                file.appendText("$day 09:10:00,-33.4211,151.4426,10.0\n")
                Log.i(TAG, "🧪 Wrote 2 sample locations for $day")
            }

            else -> Log.w(TAG, "Unknown action: ${intent.action}")
        }
    }

    companion object {
        private const val TAG = "TestWorkers"
        const val ACTION_TEST_CALL_TOTALS = "com.nick.myrecoverytracker.TEST_CALL_TOTALS"
        const val ACTION_TEST_DISTANCE_SUMMARY = "com.nick.myrecoverytracker.TEST_DISTANCE_SUMMARY"
        const val ACTION_TEST_LOCATION_SAMPLE = "com.nick.myrecoverytracker.TEST_LOCATION_SAMPLE"
    }
}