package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TestWorkersReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TEST_CALL_TOTALS -> {
                Log.i(TAG, "▶️ Enqueue CallTotalsWorker (expedited)")
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<CallTotalsWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                )
            }

            ACTION_TEST_DISTANCE_SUMMARY -> {
                Log.i(TAG, "▶️ Enqueue DistanceSummaryWorker (expedited)")
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<DistanceSummaryWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                )
            }

            ACTION_TEST_LOCATION_SAMPLE -> {
                val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val file = File(context.filesDir, "location_log.csv")
                file.parentFile?.mkdirs()
                file.appendText("$day 09:00:00,-33.4311,151.4326,10.0\n")
                file.appendText("$day 09:10:00,-33.4211,151.4426,10.0\n")
                Log.i(TAG, "🧪 Wrote 2 sample locations for $day")
            }

            ACTION_TEST_RECOVERY_VISITS -> {
                Log.i(TAG, "▶️ Enqueue RecoveryVisitsWorker (expedited)")
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<RecoveryVisitsWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                )
            }

            ACTION_TEST_MOVEMENT_INTENSITY -> {
                Log.i(TAG, "▶️ Enqueue MovementIntensityWorker (expedited)")
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<MovementIntensityWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                )
            }

            ACTION_TEST_SENSOR_SAMPLE -> {
                Log.i(TAG, "▶️ Enqueue SensorSampleWorker (now)")
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<SensorSampleWorker>().build()
                )
            }

            ACTION_TEST_APP_USAGE_CATEGORY -> {
                Log.i(TAG, "▶️ Enqueue AppUsageCategoryWorker (expedited)")
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<AppUsageCategoryWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                )
            }

            ACTION_TEST_MESSAGES_SENT -> {
                Log.i(TAG, "▶️ Enqueue MessagesSentWorker (expedited)")
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<MessagesSentWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                )
            }

            ACTION_TEST_MESSAGES_RECEIVED -> {
                Log.i(TAG, "▶️ Enqueue MessagesReceivedWorker (expedited)")
                WorkManager.getInstance(context).enqueue(
                    OneTimeWorkRequestBuilder<MessagesReceivedWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()
                )
            }

            else -> Log.w(TAG, "Unknown action: ${intent.action}")
        }
    }

    companion object {
        private const val TAG = "TestWorkers"
        const val ACTION_TEST_CALL_TOTALS = "com.nick.myrecoverytracker.TEST_CALL_TOTALS"
        const val ACTION_TEST_DISTANCE_SUMMARY = "com.nick.myrecoverytracker.TEST_DISTANCE_SUMMARY"
        const val ACTION_TEST_LOCATION_SAMPLE = "com.nick.myrecoverytracker.TEST_LOCATION_SAMPLE"
        const val ACTION_TEST_RECOVERY_VISITS = "com.nick.myrecoverytracker.TEST_RECOVERY_VISITS"
        const val ACTION_TEST_MOVEMENT_INTENSITY = "com.nick.myrecoverytracker.TEST_MOVEMENT_INTENSITY"
        const val ACTION_TEST_SENSOR_SAMPLE = "com.nick.myrecoverytracker.TEST_SENSOR_SAMPLE"
        const val ACTION_TEST_APP_USAGE_CATEGORY = "com.nick.myrecoverytracker.TEST_APP_USAGE_CATEGORY"
        const val ACTION_TEST_MESSAGES_SENT = "com.nick.myrecoverytracker.TEST_MESSAGES_SENT"
        const val ACTION_TEST_MESSAGES_RECEIVED = "com.nick.myrecoverytracker.TEST_MESSAGES_RECEIVED"
    }
}