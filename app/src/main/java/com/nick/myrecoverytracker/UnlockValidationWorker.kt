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

        // --- File existence checks ---
        if (!fUnlocks.exists()) {
            reasons += "daily_unlocks.csv missing"
            pass = false
        }
        if (!fUnlockLog.exists()) {
            reasons += "unlock_log.csv missing"
            pass = false
        }

        // --- Validate daily_unlocks.csv ---
        if (fUnlocks.exists()) {
            fUnlocks.useLines { lines ->
                lines.drop(1).forEach { line ->
                    if (line.isBlank()) return@forEach
                    val parts = line.split(",")
                    if (parts.size >= 2) {
                        val dateStr = parts[0]
                        val unlockStr = parts[1]
                        try {
                            LocalDate.parse(dateStr, fmtDate)
                        } catch (e: Exception) {
                            reasons += "Bad date format: $dateStr"
                            pass = false
                        }
                        val unlocks = unlockStr.toIntOrNull()
                        if (unlocks == null) {
                            reasons += "Non-integer unlock count: $unlockStr"
                            pass = false
                        } else if (unlocks < 0 || unlocks > 2000) {
                            reasons += "Unlock count out of range: $unlocks"
                            pass = false
                        }
                    } else {
                        reasons += "Malformed row in daily_unlocks.csv: $line"
                        pass = false
                    }
                }
            }
        }

        // --- Write QA JSON ---
        val qa = JSONObject()
        qa.put("feature", "Unlocks")
        qa.put("date", today.format(fmtDate))
        qa.put("exists_daily_unlocks", fUnlocks.exists())
        qa.put("exists_unlock_log", fUnlockLog.exists())
        qa.put("pass", pass)
        qa.put("reasons", reasons)

        val outFile = File(dir, "qa_${today.format(fmtDate)}_unlocks.json")
        outFile.writeText(qa.toString())

        Log.i(TAG, "UnlockValidation written -> ${outFile.name} pass=$pass reasons=$reasons")

        return Result.success()
    }
}