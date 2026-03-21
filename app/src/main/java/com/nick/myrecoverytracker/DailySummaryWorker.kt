// app/src/main/java/com/nick/myrecoverytracker/DailySummaryWorker.kt
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
            val dir = StorageHelper.getDataDir(applicationContext)
            val outFile = File(dir, "daily_summary.csv")
            val header = "date,total_unlocks,screen_usage_min,notification_count,app_usage_min"

            Log.d(TAG, "File path: ${outFile.absolutePath}")
            Log.d(TAG, "File exists: ${outFile.exists()}")
            Log.d(TAG, "Dir writable: ${dir.canWrite()}")

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

            val datesToProcess = getAllDatesNeedingProcessing(dir)
            Log.i(TAG, "Processing ${datesToProcess.size} dates: ${datesToProcess.joinToString(", ")}")

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

            for (date in datesToProcess) {
                val totalUnlocks = countUnlocksForDate(date)
                val screenUsageMin = readScreenUsageMinutes(dir, date)
                val notificationCount = readNotificationCount(dir, date)
                val appUsageMin = readAppUsageMinutes(dir, date)

                val line = "$date,$totalUnlocks,$screenUsageMin,$notificationCount,$appUsageMin"

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

    private fun getTodayDate(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return fmt.format(Date())
    }

    private fun getAllDatesNeedingProcessing(dir: File): List<String> {
        val dates = mutableSetOf<String>()

        dates.add(getTodayDate())

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
        val dir = StorageHelper.getDataDir(applicationContext)
        val f = File(dir, "unlock_diag.csv")

        Log.d(TAG, "countUnlocksForDate($date): dataDir=$dir, file=${f.absolutePath}, exists=${f.exists()}, length=${f.length()}")

        if (!f.exists()) {
            Log.w(TAG, "countUnlocksForDate($date): unlock_diag.csv not found at ${f.absolutePath}")
            return 0
        }

        return try {
            val lines = f.readLines()
            Log.d(TAG, "countUnlocksForDate($date): read ${lines.size} lines from ${f.absolutePath}")

            val body = if (lines.isNotEmpty() && lines[0].contains("ts,tag,extra")) {
                lines.drop(1)
            } else {
                lines
            }

            Log.d(TAG, "countUnlocksForDate($date): processing ${body.size} body lines")

            var count = 0
            for (line in body) {
                if (line.isBlank()) continue
                val parts = line.split(',')
                if (parts.size < 2) continue

                val tsStr = parts[0].trim()
                val tag = parts[1].trim()

                if (tsStr.startsWith(date) && tag == "UNLOCK") {
                    count++
                    Log.d(TAG, "countUnlocksForDate($date): matched line: $line, count now = $count")
                }
            }

            Log.d(TAG, "countUnlocksForDate($date): final count = $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "countUnlocksForDate($date): error reading unlock_diag.csv", e)
            0
        }
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
            val endOfDay = runCatching {
                fmt.parse("$day 23:59:59")?.time ?: System.currentTimeMillis()
            }.getOrNull() ?: System.currentTimeMillis()
            totalOnMs += (endOfDay - lastOnTime!!).coerceAtLeast(0L)
        }

        return (totalOnMs / 60000.0)
    }

    private fun readNotificationCount(dir: File, day: String): Int {
        val f = File(dir, "notification_log.csv")
        if (!f.exists()) return 0

        val lines = f.readLines()
        val body = if (lines.isNotEmpty() && lines[0].contains("ts,")) {
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
            val eventType = parts[1].trim()

            if (tsStr.startsWith(day) && eventType == "POSTED") {
                count++
            }
        }

        return count
    }

    private fun readAppUsageMinutes(dir: File, day: String): Double {
        val f = File(dir, "daily_app_usage_minutes.csv")
        if (!f.exists()) return 0.0

        val lines = f.readLines()
        val body = if (lines.isNotEmpty() && lines[0].contains("date,")) {
            lines.drop(1)
        } else {
            lines
        }

        for (line in body) {
            if (line.isBlank()) continue
            val parts = line.split(',')
            if (parts.size < 2) continue

            val date = parts[0].trim()
            val appMinTotal = runCatching { parts[1].trim().toDouble() }.getOrNull() ?: 0.0

            if (date == day) {
                return appMinTotal
            }
        }

        return 0.0
    }
}