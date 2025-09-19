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

class UsageEventsDailyWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dir = applicationContext.filesDir
            val raw = File(dir, "usage_events.csv")
            val out = ensureHeader(File(dir, "daily_usage_events.csv"), "date,event_count")

            val today = LocalDate.now(zone)
            val yesterday = today.minusDays(1)
            val tStr = today.format(fmtDate)
            val yStr = yesterday.format(fmtDate)

            var tCount = 0
            var yCount = 0

            if (raw.exists()) {
                raw.bufferedReader().useLines { seq ->
                    seq.forEach { line ->
                        if (line.length < 10) return@forEach
                        val datePart = line.substring(0, 10)
                        if (datePart == tStr) tCount++
                        else if (datePart == yStr) yCount++
                    }
                }
            }

            upsert(out, yStr, listOf(yCount.toString()))
            upsert(out, tStr, listOf(tCount.toString()))
            Log.i(TAG, "UsageEventsDaily -> $yStr=$yCount, $tStr=$tCount")

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "UsageEventsDailyWorker failed", t)
            Result.retry()
        }
    }

    private fun ensureHeader(f: File, header: String): File {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("$header\n")
        }
        return f
    }

    private fun upsert(file: File, dateStr: String, cols: List<String>) {
        val lines = if (file.exists()) file.readLines().toMutableList() else mutableListOf()
        if (lines.isEmpty()) return
        val header = lines.first()
        var replaced = false
        for (i in 1 until lines.size) {
            val idx = lines[i].indexOf(',')
            val key = if (idx >= 0) lines[i].substring(0, idx) else lines[i]
            if (key == dateStr) {
                lines[i] = dateStr + "," + cols.joinToString(",")
                replaced = true
                break
            }
        }
        if (!replaced) lines.add(dateStr + "," + cols.joinToString(","))
        file.writeText((listOf(header) + lines.drop(1)).joinToString("\n") + "\n")
    }

    companion object { private const val TAG = "UsageEventsDailyWorker" }
}