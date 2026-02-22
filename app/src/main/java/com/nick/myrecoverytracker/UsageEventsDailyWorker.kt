package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.LocalDate
import java.time.ZoneId

class UsageEventsDailyWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val raw = File(applicationContext.filesDir, "screen_events.csv")
        val out = ensureHeader(File(applicationContext.filesDir, "daily_sleep_duration.csv"), "date,sleep_hours")
        val sleepPeriods = mutableListOf<Pair<LocalDate, Double>>()
        var sleepStart: LocalDateTime? = null

        if (raw.exists()) {
            raw.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.length < 10) return@forEach
                    val parts = line.split(',')
                    val ts = LocalDateTime.parse(parts[0], fmtDate)
                    val etype = parts[1]

                    if (etype in listOf("SCREEN_NON_INTERACTIVE", "KEYGUARD_SHOWN")) {
                        if (sleepStart == null) sleepStart = ts
                    } else if (etype in listOf("SCREEN_INTERACTIVE", "KEYGUARD_HIDDEN")) {
                        sleepStart?.let {
                            val duration = (ts.atZone(zone).toEpochSecond() - it.atZone(zone).toEpochSecond()) / 3600.0
                            sleepPeriods.add(it.toLocalDate() to duration)
                            sleepStart = null
                        }
                    }
                }
            }
        }

        sleepStart?.let {
            val lastTs = LocalDateTime.now(zone)
            val duration = (lastTs.atZone(zone).toEpochSecond() - it.atZone(zone).toEpochSecond()) / 3600.0
            sleepPeriods.add(it.toLocalDate() to duration)
        }

        val dailySleep = sleepPeriods.groupBy({ it.first }, { it.second }).map { (date, durations) ->
            date to durations.sum()
        }

        upsertSleep(out, dailySleep)

        Result.success()
    }

    private fun ensureHeader(f: File, header: String): File {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("$header\n")
        }
        return f
    }

    private fun upsertSleep(file: File, data: List<Pair<LocalDate, Double>>) {
        val lines = if (file.exists()) file.readLines().toMutableList() else mutableListOf()
        if (lines.isEmpty()) return
        val header = lines.first()

        data.forEach { (date, duration) ->
            var replaced = false
            for (i in 1 until lines.size) {
                val key = lines[i].substringBefore(',')
                if (key == date.toString()) {
                    lines[i] = "$date,$duration"
                    replaced = true
                    break
                }
            }
            if (!replaced) lines.add("$date,$duration")
        }

        file.writeText((listOf(header) + lines.drop(1)).joinToString("\n") + "\n")
    }

    companion object { private const val TAG = "UsageEventsDailyWorker" }
}