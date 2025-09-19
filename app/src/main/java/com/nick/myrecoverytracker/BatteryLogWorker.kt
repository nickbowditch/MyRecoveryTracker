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
import java.util.Locale
import java.util.Date

/**
 * Appends "timestamp,NN%" to files/battery_log.csv
 * - Ensures header
 * - De-dupes: only writes if % changed OR >=30min since last write
 */
class BatteryLogWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ctx = applicationContext
            val level = currentBatteryPercent(ctx)
            if (level < 0) {
                Log.w(TAG, "Battery level unknown; skip")
                return@withContext Result.success()
            }

            val ts = nowTs()
            val file = ensureHeader(File(ctx.filesDir, FILE), HEADER)

            // read last data line (ignore header)
            val last = lastDataLine(file)
            val shouldWrite = if (last != null) {
                val parts = last.split(",")
                val lastPct = parts.getOrNull(1)?.removeSuffix("%")?.toIntOrNull()
                val lastTs = parts.getOrNull(0)
                val minutesSince = minutesBetween(lastTs, ts)
                (lastPct == null || lastPct != level) || (minutesSince >= 30)
            } else true

            if (shouldWrite) {
                file.appendText("$ts,${level}%\n")
                Log.i(TAG, "battery_log appended: $ts,${level}%")
            } else {
                Log.i(TAG, "battery_log skipped (no change & <30m)")
            }

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "BatteryLogWorker failed", t)
            Result.failure()
        }
    }

    // --- helpers ---

    private fun ensureHeader(f: File, header: String): File {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText(header + "\n")
        }
        return f
    }

    private fun lastDataLine(f: File): String? {
        if (!f.exists()) return null
        val lines = f.readLines()
        for (i in lines.size - 1 downTo 1) {
            val line = lines[i].trim()
            if (line.isNotEmpty()) return line
        }
        return null
    }

    private fun minutesBetween(prev: String?, now: String): Long {
        if (prev == null) return Long.MAX_VALUE
        return try {
            val dPrev = sdf.parse(prev)?.time ?: return Long.MAX_VALUE
            val dNow = sdf.parse(now)?.time ?: return Long.MAX_VALUE
            (dNow - dPrev) / 60000L
        } catch (_: Throwable) { Long.MAX_VALUE }
    }

    private fun currentBatteryPercent(context: Context): Int {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val status = context.registerReceiver(null, ifilter)
        val level = status?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = status?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else -1
    }

    private fun nowTs(): String = sdf.format(Date())

    companion object {
        private const val TAG = "BatteryLogWorker"
        private const val FILE = "battery_log.csv"
        private const val HEADER = "timestamp,level_pct"
        private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}