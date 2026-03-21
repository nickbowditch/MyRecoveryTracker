// UnlockValidationWorker.kt
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
        val dir = StorageHelper.getDataDir(applicationContext)
        val today = LocalDate.now(zone)

        val fUnlockLog = File(dir, "unlock_log.csv")

        var pass = true
        val reasons = mutableListOf<String>()

        if (!fUnlockLog.exists()) {
            reasons += "unlock_log.csv missing"
            pass = false
        }

        if (fUnlockLog.exists()) {
            val lines = fUnlockLog.readLines()
            if (lines.isEmpty()) {
                reasons += "unlock_log.csv empty"
                pass = false
            } else {
                val header = lines.first().trim()
                val cols = header.split(',')
                val tsIdx = cols.indexOf("ts")
                val eventIdx = cols.indexOf("event")

                if (tsIdx < 0 || eventIdx < 0) {
                    reasons += "header missing required columns: $header"
                    pass = false
                } else {
                    lines.drop(1).forEach { line ->
                        if (line.isBlank()) return@forEach
                        val parts = line.split(',')
                        if (parts.size <= tsIdx || parts.size <= eventIdx) {
                            reasons += "Malformed row: $line"
                            pass = false
                            return@forEach
                        }
                        val tsStr = parts[tsIdx].trim()
                        val event = parts[eventIdx].trim()

                        if (tsStr.isEmpty()) {
                            reasons += "Bad timestamp: $tsStr"
                            pass = false
                        }
                        if (event.isEmpty()) {
                            reasons += "Empty event"
                            pass = false
                        }
                    }
                }
            }
        }

        val qa = JSONObject().apply {
            put("feature", "Unlocks")
            put("date", today.format(fmtDate))
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