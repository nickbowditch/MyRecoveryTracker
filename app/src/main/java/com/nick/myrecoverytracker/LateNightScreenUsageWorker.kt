package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Determines if there was any screen usage between 00:00–05:00 *today*.
 * Reads from files/unlocks_log.csv (falls back to files/unlock_log.csv if needed).
 * Writes Y/N to files/daily_late_night_screen_usage.csv as:
 *   date,late_night_YN
 *   2025-08-15,Y
 */
class LateNightScreenUsageWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val day = today()

        // Prefer unlocks_log.csv; fall back to unlock_log.csv if that’s what exists.
        val primary = File(ctx.filesDir, "unlocks_log.csv")
        val fallback = File(ctx.filesDir, "unlock_log.csv")
        val src = when {
            primary.exists() -> primary
            fallback.exists() -> fallback
            else -> null
        }

        if (src == null) {
            Log.w(TAG, "No unlock log found; writing N for $day")
            writeYN(File(ctx.filesDir, OUT_FILE), day, "N")
            return@withContext Result.success()
        }

        // Count any unlock line for *today* where local hour ∈ [0, 4] (00:00–04:59).
        // We expect lines starting with "yyyy-MM-dd HH:mm:ss" followed by optional commas/data.
        val prefix = "$day "
        var anyLate = false

        src.useLines { seq ->
            seq.forEach { line ->
                if (!line.startsWith(prefix)) return@forEach
                val ts = line.substring(0, 19) // "yyyy-MM-dd HH:mm:ss" length
                val hour = runCatching {
                    ts.substring(11, 13).toInt()
                }.getOrDefault(-1)
                if (hour in 0..4) {
                    anyLate = true
                    return@forEach
                }
            }
        }

        val yn = if (anyLate) "Y" else "N"
        writeYN(File(ctx.filesDir, OUT_FILE), day, yn)
        Log.i(TAG, "LateNight ($day) = $yn  [source=${src.name}]")
        Result.success()
    }

    private fun writeYN(out: File, day: String, yn: String) {
        val header = "date,late_night_YN"
        val lines = if (out.exists()) out.readLines().toMutableList() else mutableListOf(header)
        // Keep header, drop any existing line for this date
        val filtered = lines.filterNot { it.startsWith("$day,") }.toMutableList()
        if (filtered.isEmpty() || filtered.first() != header) filtered.add(0, header)
        filtered += "$day,$yn"
        out.writeText(filtered.joinToString("\n") + "\n")
    }

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    companion object {
        private const val TAG = "LateNightScreenUsage"
        private const val OUT_FILE = "daily_late_night_screen_usage.csv"
    }
}