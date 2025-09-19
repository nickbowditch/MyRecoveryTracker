package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Aggregates today's ringer mode change events from files/ringer_log.csv into:
 *   files/daily_ringer_mode_changes.csv
 *
 * Header:
 *   date,normal_changes,vibrate_changes,silent_changes,total_changes
 */
class RingerModeDailyWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val log = File(ctx.filesDir, "ringer_log.csv")
        var normal = 0
        var vibrate = 0
        var silent = 0

        if (log.exists()) {
            log.forEachLine { line ->
                if (line.startsWith(day)) {
                    val mode = line.substringAfter(",").trim().lowercase(Locale.US)
                    when {
                        mode.startsWith("normal") -> normal++
                        mode.startsWith("vibrate") -> vibrate++
                        mode.startsWith("silent") -> silent++
                    }
                }
            }
        }

        val total = normal + vibrate + silent
        val out = File(ctx.filesDir, "daily_ringer_mode_changes.csv")
        val header = "date,normal_changes,vibrate_changes,silent_changes,total_changes"
        val lines = if (out.exists()) out.readLines().toMutableList() else mutableListOf(header)
        val filtered = lines.filterNot { it.startsWith("$day,") }.toMutableList()
        filtered.add("$day,$normal,$vibrate,$silent,$total")
        out.writeText(filtered.joinToString("\n") + "\n")

        Log.i(TAG, "RingerModeDaily($day): normal=$normal vibrate=$vibrate silent=$silent total=$total")
        Result.success()
    }

    companion object { private const val TAG = "RingerModeDailyWorker" }
}