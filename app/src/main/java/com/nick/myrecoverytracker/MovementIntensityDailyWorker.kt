// app/src/main/java/com/nick/myrecoverytracker/MovementIntensityDailyWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * MovementIntensityDailyWorker
 *
 * Produces: files/daily_movement_intensity.csv
 * Format:   date,intensity
 *
 * Conservative first pass: uses unlock_log.csv and counts UNLOCK events
 * per day as a proxy "movement intensity". Writes/refreshes rows for
 * yesterday and today (idempotent per date).
 */
class MovementIntensityDailyWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val zone = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val outFile by lazy { File(applicationContext.filesDir, "daily_movement_intensity.csv") }
    private val unlockLog by lazy { File(applicationContext.filesDir, "unlock_log.csv") }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            ensureHeader(outFile, "date,intensity")

            val today = LocalDate.now(zone)
            val yesterday = today.minusDays(1)
            val days = listOf(yesterday, today)

            val intensities = countUnlocks(days) // proxy for intensity

            days.forEach { d ->
                val dateStr = d.format(fmtDate)
                writeOrReplaceRow(outFile, dateStr, intensities[dateStr] ?: 0)
            }

            Log.i(TAG, "MovementIntensityDaily -> ${days.joinToString { "${it.format(fmtDate)}=${intensities[it.format(fmtDate)] ?: 0}" }}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "MovementIntensityDailyWorker failed", t)
            Result.retry()
        }
    }

    private fun countUnlocks(days: List<LocalDate>): Map<String, Int> {
        val datesWanted = days.map { it.format(fmtDate) }.toSet()
        if (!unlockLog.exists()) return datesWanted.associateWith { 0 }

        val map = HashMap<String, Int>(datesWanted.size)
        unlockLog.forEachLine { line ->
            // Expected: "YYYY-MM-DD HH:MM:SS,UNLOCK"
            val datePrefix = if (line.length >= 10) line.substring(0, 10) else return@forEachLine
            if (datePrefix in datesWanted && line.contains("UNLOCK")) {
                map[datePrefix] = (map[datePrefix] ?: 0) + 1
            }
        }
        datesWanted.forEach { map.putIfAbsent(it, 0) }
        return map
    }

    private fun writeOrReplaceRow(file: File, dateStr: String, intensity: Int) {
        val header = "date,intensity"
        val existing = if (file.exists()) file.readLines().toMutableList() else mutableListOf(header)

        val kept = existing.filterIndexed { idx, line ->
            if (idx == 0) true else !line.startsWith("$dateStr,")
        }.toMutableList()

        kept.add("$dateStr,$intensity")
        file.writeText(kept.joinToString("\n") + "\n")
    }

    private fun ensureHeader(f: File, header: String) {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("$header\n")
        }
    }

    companion object {
        private const val TAG = "MovementIntensityDaily"
    }
}