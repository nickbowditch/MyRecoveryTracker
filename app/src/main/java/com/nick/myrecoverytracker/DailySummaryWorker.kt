package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class DailySummaryWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    companion object {
        private const val TAG = "DailySummaryWorker"
    }

    override fun doWork(): Result {
        return try {
            val dir = applicationContext.filesDir
            val outFile = File(dir, "daily_summary.csv")
            val header = "date,total_unlocks,battery_drain_rate,screen_usage_min,notification_count,app_usage_min"

            Log.d(TAG, "File path: ${outFile.absolutePath}")
            Log.d(TAG, "File exists: ${outFile.exists()}")
            Log.d(TAG, "Dir writable: ${dir.canWrite()}")

            // Ensure file exists with header
            if (!outFile.exists()) {
                Log.i(TAG, "Creating new daily_summary.csv")
                try {
                    FileOutputStream(outFile).use { fos ->
                        fos.write("$header\n".toByteArray())
                        fos.fd.sync()
                    }
                    Log.i(TAG, "File created, exists now: ${outFile.exists()}, size: ${outFile.length()}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create file", e)
                    throw e
                }
            }

            // Get all dates that need processing (historical + today)
            val datesToProcess = getAllDatesNeedingProcessing(dir)
            Log.i(TAG, "Processing ${datesToProcess.size} dates: ${datesToProcess.joinToString(", ")}")

            // Load existing daily_summary data
            val existing = if (outFile.exists() && outFile.length() > 0) {
                outFile.readLines().toMutableList()
            } else {
                Log.w(TAG, "File missing or empty after creation attempt, starting fresh")
                mutableListOf(header)
            }

            Log.d(TAG, "Read ${existing.size} lines from file before processing")

            val dataStartIdx = if (existing.isNotEmpty() && existing[0].startsWith("date,")) 1 else 0
            val existingDates = existing.drop(dataStartIdx).map { it.split(',')[0].trim() }.toSet()

            var backfilledCount = 0
            var updatedCount = 0

            // Process each date
            for (date in datesToProcess) {
                val totalUnlocks = countUnlocksForDate(date)
                val batteryDrainRate = 0.05
                val screenUsageMin = readScreenUsageMinutes(dir, date)
                val notificationCount = readNotificationCount(dir, date)
                val appUsageMin = readAppUsageMinutes(dir, date)

                val line = "$date,$totalUnlocks,$batteryDrainRate,$screenUsageMin,$notificationCount,$appUsageMin"

                val replacedIdx = existing.drop(dataStartIdx).indexOfFirst { it.startsWith("$date,") }

                if (replacedIdx >= 0) {
                    val actualIdx = dataStartIdx + replacedIdx
                    val oldLine = existing[actualIdx]
                    existing[actualIdx] = line
                    updatedCount++
                    Log.d(TAG, "UPDATED row for $date")
                    Log.d(TAG, "  OLD: $oldLine")
                    Log.d(TAG, "  NEW: $line")
                } else {
                    existing.add(line)
                    backfilledCount++
                    Log.i(TAG, "BACKFILLED new row for $date → $line")
                }
            }

            // Write all data back using FileOutputStream
            val content = existing.joinToString("\n") + "\n"
            val contentBytes = content.toByteArray()
            Log.d(TAG, "About to write ${existing.size} lines (${contentBytes.size} bytes)")
            Log.d(TAG, "First 3 lines to write:")
            existing.take(3).forEach { Log.d(TAG, "  $it") }

            try {
                FileOutputStream(outFile, false).use { fos ->
                    fos.write(contentBytes)
                    fos.flush()
                    fos.fd.sync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Write failed", e)
                throw e
            }

            Log.d(TAG, "Write complete. File exists: ${outFile.exists()}, size: ${outFile.length()}")

            // Verify write by reading back
            if (outFile.length() > 0) {
                val verification = outFile.readLines()
                Log.d(TAG, "Verification: read back ${verification.size} lines")
                verification.take(3).forEach { Log.d(TAG, "  VERIFY: $it") }
            } else {
                Log.e(TAG, "VERIFICATION FAILED: File size is 0 after write!")
            }

            Log.i(TAG, "Daily summary complete: backfilled=$backfilledCount, updated=$updatedCount, total_dates=${datesToProcess.size}")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to write daily summary", e)
            Result.failure()
        }
    }

    private fun getAllDatesNeedingProcessing(dir: File): List<String> {
        val dates = mutableSetOf<String>()

        // Always include today
        dates.add(getTodayDate())

        // Scan unlock_diag.csv for all historical dates
        val unlockDiag = File(dir, "unlock_diag.csv")
        if (unlockDiag.exists()) {
            unlockDiag.readLines().forEach { line ->
                if (line.isBlank() || line.startsWith("ts,")) return@forEach
                val parts = line.split(',')
                if (parts.isNotEmpty()) {
                    val tsStr = parts[0].trim()
                    if (tsStr.length >= 10) {
                        dates.add(tsStr.substring(0, 10))
                    }
                }
            }
        }

        // Scan screen_log.csv for additional dates
        val screenLog = File(dir, "screen_log.csv")
        if (screenLog.exists()) {
            screenLog.readLines().forEach { line ->
                if (line.isBlank() || line.startsWith("ts,")) return@forEach
                val parts = line.split(',')
                if (parts.isNotEmpty()) {
                    val tsStr = parts[0].trim()
                    if (tsStr.length >= 10) {
                        dates.add(tsStr.substring(0, 10))
                    }
                }
            }
        }

        // Scan notification_log.csv for additional dates
        val notifLog = File(dir, "notification_log.csv")
        if (notifLog.exists()) {
            notifLog.readLines().forEach { line ->
                if (line.isBlank() || line.startsWith("ts,")) return@forEach
                val parts = line.split(',')
                if (parts.isNotEmpty()) {
                    val tsStr = parts[0].trim()
                    if (tsStr.length >= 10) {
                        dates.add(tsStr.substring(0, 10))
                    }
                }
            }
        }

        return dates.sorted()
    }

    private fun countUnlocksForDate(date: String): Int {
        val f = File(applicationContext.filesDir, "unlock_diag.csv")
        if (!f.exists()) {
            return 0
        }

        val lines = f.readLines()
        val body = if (lines.isNotEmpty() && lines[0].contains("ts,tag,extra")) {
            lines.drop(1)
        } else {
            lines
        }

        var count = 0
        for (line in body) {
            if (line.isBlank()) continue
            val parts = line.split(',')
            if (parts.size < 2) continue

            val tsStr = parts[0].trim()
            val tag = parts[1].trim()

            if (tsStr.startsWith(date) && tag == "UNLOCK") {
                count++
            }
        }

        return count
    }

    private fun readScreenUsageMinutes(dir: File, day: String): Double {
        val f = File(dir, "screen_log.csv")
        if (!f.exists()) return 0.0
        val lines = f.readLines()
        if (lines.size <= 1) return 0.0

        val dayPrefix = "$day "
        var lastOnTime: Long? = null
        var totalOnMs = 0L
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        for (line in lines.drop(1)) {
            if (line.isBlank()) continue
            val parts = line.split(',')
            if (parts.size < 2) continue
            val tsStr = parts[0].trim()
            val state = parts[1].trim()
            if (!tsStr.startsWith(dayPrefix)) continue

            val ts = runCatching { fmt.parse(tsStr)?.time }.getOrNull() ?: continue

            if (state.equals("ON", ignoreCase = true)) {
                if (lastOnTime == null) lastOnTime = ts
            } else if (state.equals("OFF", ignoreCase = true)) {
                if (lastOnTime != null) {
                    totalOnMs += (ts - lastOnTime!!).coerceAtLeast(0L)
                    lastOnTime = null
                }
            }
        }

        if (lastOnTime != null) {
            val endOfDay = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .parse("$day 23:59:59")?.time
            if (endOfDay != null) {
                totalOnMs += (endOfDay - lastOnTime!!).coerceAtLeast(0L)
            }
        }

        return (totalOnMs / 60000.0)
    }

    private fun readNotificationCount(dir: File, day: String): Int {
        val f = File(dir, "notification_log.csv")
        if (!f.exists()) {
            return 0
        }

        val lines = f.readLines()
        if (lines.size <= 1) return 0

        var count = 0
        for (line in lines.drop(1)) {
            if (line.isBlank()) continue
            val parts = line.split(',')

            if (parts.size < 4) continue

            val tsStr = parts[0].trim()
            val eventType = parts[3].trim()

            val dayPart = if (tsStr.length >= 10) tsStr.substring(0, 10) else tsStr
            if (dayPart != day) continue

            if (eventType == "POSTED") {
                count++
            }
        }

        return count
    }

    private fun readAppUsageMinutes(dir: File, day: String): Double {
        val f = File(dir, "daily_app_usage_minutes.csv")
        if (!f.exists()) return 0.0
        val lines = f.readLines()
        if (lines.size <= 1) return 0.0

        val header = lines.first().split(',').map { it.trim() }
        val appTotalIdx = header.indexOfFirst { it.equals("app_min_total", ignoreCase = true) }
        val idx = if (appTotalIdx >= 0) appTotalIdx else header.size - 1

        val row = lines
            .asReversed()
            .firstOrNull { it.startsWith("$day,") }
            ?: return 0.0

        val cols = row.split(',')
        if (idx !in cols.indices) return 0.0

        val value = cols[idx].trim()
        return value.toDoubleOrNull() ?: 0.0
    }

    private fun getTodayDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }
}