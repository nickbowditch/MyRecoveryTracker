package com.nick.myrecoverytracker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BatteryLogger(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val batteryLevel = getBatteryPercentage(applicationContext)
            val timestamp = getCurrentTimestamp()

            if (batteryLevel >= 0) {
                val logFile = File(applicationContext.filesDir, "battery_log.csv")
                logFile.appendText("$timestamp,$batteryLevel%\n")
                Log.i("BatteryLogger", "🔋 Logged battery level: $batteryLevel%")
            } else {
                Log.w("BatteryLogger", "⚠️ Battery level unknown (-1). Skipped logging.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("BatteryLogger", "❌ Error logging battery", e)
            Result.failure()
        }
    }

    private fun getBatteryPercentage(context: Context): Int {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
        return batteryStatus?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                -1
            }
        } ?: -1
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return sdf.format(Date())
    }
}