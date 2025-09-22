// app/src/main/java/com/nick/myrecoverytracker/UnlockValidationWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class UnlockValidationWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    private val zone = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val TAG = "UnlockValidationWorker"

    override fun doWork(): Result {
        val dir = applicationContext.filesDir
        val today = LocalDate.now(zone)

        val fUnlocks = File(dir, "daily_unlocks.csv")
        val fUnlockLog = File(dir, "unlock_log.csv")

        var pass = true
        val reasons = mutableListOf<String>()

        if (!fUnlocks.exists()) {
            reasons += "daily_unlocks.csv missing"
            pass = false
        }
        if (!fUnlockLog.exists()) {
            reasons += "unlock_log.csv missing"
            pass = false
        }

        if (fUnlocks.exists()) {
            val lines = fUnlocks.readLines()
            if (lines.isEmpty()) {
                reasons += "daily_unlocks.csv empty"
                pass = false
            } else {
                val header = lines.first().trim()
                val cols = header.split(',')
                val dateIdx = cols.indexOf("date")
                val countIdx = when {
                    cols.contains("daily_unlocks") -> cols.indexOf("daily_unlocks")
                    cols.contains("unlocks") -> cols.indexOf("unlocks")
                    else -> -1
                }
                if (dateIdx < 0 || countIdx < 0) {
                    reasons += "header missing required columns: $header"
                    pass = false
                } else {
                    lines.drop(1).forEach { line ->
                        if (line.isBlank()) return@forEach
                        val parts = line.split(',')
                        if (parts.size <= countIdx || parts.size <= dateIdx) {
                            reasons += "Malformed row: $line"
                            pass = false
                            return@forEach
                        }
                        val dateStr = parts[dateIdx].trim()
                        val unlockStr = parts[countIdx].trim()

                        try {
                            LocalDate.parse(dateStr, fmtDate)
                        } catch (_: Throwable) {
                            reasons += "Bad date: $dateStr"
                            pass = false
                        }

                        val unlocks = unlockStr.toIntOrNull()
                        if (unlocks == null) {
                            reasons += "Non-integer unlocks: $unlockStr"
                            pass = false
                        } else if (unlocks < 0 || unlocks > 2000) {
                            reasons += "Unlocks out of range: $unlocks"
                            pass = false
                        }
                    }
                }
            }
        }

        val qa = JSONObject().apply {
            put("feature", "Unlocks")
            put("date", today.format(fmtDate))
            put("exists_daily_unlocks", fUnlocks.exists())
            put("exists_unlock_log", fUnlockLog.exists())
            put("pass", pass)
            put("reasons", reasons)
        }
        val outFile = File(dir, "qa_${today.format(fmtDate)}_unlocks.json")
        outFile.writeText(qa.toString())

        Log.i(TAG, "UnlockValidation pass=$pass reasons=$reasons")
        return Result.success()
    }
}