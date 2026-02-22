package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.round

class SleepRollupWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val fmtTs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

    override fun doWork(): Result {
        val dir = applicationContext.filesDir
        Log.i(TAG, "filesDir=${dir.absolutePath}")

        val fScreen = File(dir, "screen_log.csv")
        val outSummary = ensureHeader(File(dir, OUT_FILE), EXPECTED_HEADER)
        val outDuration = ensureHeader(File(dir, OUT_FILE_HOURS), EXPECTED_HEADER_HOURS)

        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val backfillDays = inputData.getInt("backfill_days", 30).coerceIn(1, 400)
        val startDate = today.minusDays(backfillDays.toLong())

        var processed = 0
        var d = startDate
        while (!d.isAfter(today)) {
            val r = computeForDate(d, fScreen)
            upsertSummary(outSummary, d.format(fmtDate), r.totalSleepMinutes, r.sleepEfficiency)
            upsertDuration(outDuration, d.format(fmtDate), r.totalSleepMinutes)
            processed++
            d = d.plusDays(1)
        }

        val y = computeForDate(yesterday, fScreen)
        val t = computeForDate(today, fScreen)
        upsertSummary(outSummary, yesterday.format(fmtDate), y.totalSleepMinutes, y.sleepEfficiency)
        upsertDuration(outDuration, yesterday.format(fmtDate), y.totalSleepMinutes)
        upsertSummary(outSummary, today.format(fmtDate), t.totalSleepMinutes, t.sleepEfficiency)
        upsertDuration(outDuration, today.format(fmtDate), t.totalSleepMinutes)

        Log.i(TAG, "SleepRollup processed=$processed (backfill_days=$backfillDays)")
        Log.e(PROBE_TAG, "END success processed=$processed out=${outSummary.absolutePath}")

        return Result.success()
    }

    private data class SleepResult(val totalSleepMinutes: Int, val sleepEfficiency: Double)

    private fun round2(v: Double) = round(v * 100.0) / 100.0

    private fun ensureHeader(f: File, header: String): File {
        f.parentFile?.mkdirs()
        if (!f.exists() || f.length() == 0L) {
            writeAtomic(f, "$header\n")
        } else {
            val cur = try { f.useLines { it.firstOrNull() ?: "" } } catch (_: Throwable) { "" }
            if (cur.trim().replace("\r", "") != header) {
                val rest = try { f.readLines().drop(1) } catch (_: Throwable) { emptyList() }
                writeAtomic(f, buildString {
                    append(header).append('\n')
                    rest.forEach { line -> if (line.isNotBlank()) append(line.trimEnd('\r')).append('\n') }
                })
            }
        }
        return f
    }

    private fun upsertSummary(file: File, dateStr: String, totalSleepMinutes: Int, sleepEfficiency: Double) {
        val lines = if (file.exists()) file.readLines().map { it.trimEnd('\r') }.toMutableList() else mutableListOf()
        if (lines.isEmpty()) return
        val header = lines.first()
        if (header != EXPECTED_HEADER) return
        val map = lines.drop(1).filter { it.isNotBlank() }.associateBy({ it.substringBefore(',') }, { it }).toMutableMap()
        map[dateStr] = "%s,%d,%.2f".format(Locale.US, dateStr, totalSleepMinutes, sleepEfficiency)
        val rebuilt = buildString { append(header).append('\n'); map.keys.sorted().forEach { append(map[it]).append('\n') } }
        writeAtomic(file, rebuilt)
    }

    private fun upsertDuration(file: File, dateStr: String, totalMinutes: Int) {
        val hours = totalMinutes / 60.0
        val lines = if (file.exists()) file.readLines().map { it.trimEnd('\r') }.toMutableList() else mutableListOf()
        val map = mutableMapOf<String, String>()
        if (lines.isEmpty()) {
            map[dateStr] = "%s,%.2f".format(Locale.US, dateStr, hours)
            writeAtomic(file, buildString { append(EXPECTED_HEADER_HOURS).append('\n'); append(map[dateStr]).append('\n') })
            return
        }
        val header = lines.first()
        lines.drop(1).filter { it.isNotBlank() }.forEach { map[it.substringBefore(',')] = it }
        map[dateStr] = "%s,%.2f".format(Locale.US, dateStr, hours)
        writeAtomic(file, buildString { append(header).append('\n'); map.keys.sorted().forEach { append(map[it]).append('\n') } })
    }

    private fun writeAtomic(dst: File, content: String) {
        dst.parentFile?.mkdirs()
        val tmp = File(dst.parentFile, dst.name + ".tmp")
        FileOutputStream(tmp).use { it.channel.truncate(0); it.write(content.toByteArray()); it.channel.force(true) }
        if (!tmp.renameTo(dst)) { FileOutputStream(dst, false).use { it.write(content.toByteArray()) }; tmp.delete() }
    }

    private fun computeForDate(date: LocalDate, fScreen: File): SleepResult {
        val minutesByDate = mutableMapOf<LocalDate, Int>()
        if (!fScreen.exists()) return SleepResult(0, 0.0)
        data class ScreenEvent(val ts: ZonedDateTime, val isOn: Boolean)
        val events = mutableListOf<ScreenEvent>()
        try {
            fScreen.useLines { seq ->
                seq.forEach { line ->
                    val l = line.trimStart('\uFEFF').trim()
                    if (l.isEmpty() || l.startsWith("ts,", true) || l.startsWith("timestamp,", true)) return@forEach
                    val ts = extractTs(l) ?: return@forEach
                    val z = safeParse(ts) ?: return@forEach
                    val state = l.substringAfter(',', "").trim()
                    events += ScreenEvent(z, state.equals("ON", true))
                }
            }
        } catch (_: Throwable) { return SleepResult(0, 0.0) }
        if (events.size < 2) return SleepResult(0, 0.0)
        events.sortBy { it.ts.toInstant().toEpochMilli() }

        val minGap = 5 * 60 // minimum gap (5 minutes) to include fragmented sleep
        val nightStart = 18 // 6 PM as start of sleep day
        val nightEnd = 10 // 10 AM as end of sleep day
        val sleepBlocks = mutableListOf<Pair<ZonedDateTime, ZonedDateTime>>()
        var sleepStart: ZonedDateTime? = null

        for (event in events) {
            if (!event.isOn) {
                if (sleepStart == null) sleepStart = event.ts
            } else if (event.isOn && sleepStart != null) {
                val gapSec = Duration.between(sleepStart, event.ts).seconds
                if (gapSec >= minGap) sleepBlocks.add(Pair(sleepStart, event.ts))
                sleepStart = null
            }
        }

        // Aggregate blocks into the correct sleep day
        val nightlyMinutes = mutableMapOf<LocalDate, Int>()
        sleepBlocks.forEach { (start, end) ->
            val sleepDay = if (start.hour >= nightStart) start.toLocalDate() else start.toLocalDate().minusDays(1)
            nightlyMinutes[sleepDay] = (nightlyMinutes[sleepDay] ?: 0) + Duration.between(start, end).toMinutes().toInt()
        }

        val totalMinutes = nightlyMinutes[date] ?: 0
        val totalTimeInBed = if (nightlyMinutes[date] != null) {
            val blocks = sleepBlocks.filter {
                val sd = if (it.first.hour >= nightStart) it.first.toLocalDate() else it.first.toLocalDate().minusDays(1)
                sd == date
            }
            if (blocks.isEmpty()) 1 else Duration.between(blocks.first().first, blocks.last().second).toMinutes()
        } else 1
        val eff = if (totalMinutes > 0) totalMinutes.toDouble() / totalTimeInBed.toDouble() else 0.0

        return SleepResult(totalMinutes, round2(eff))
    }

    private fun extractTs(line: String): String? {
        val first = line.substringBefore(',', "").trim()
        return if (first.length >= 19) first.substring(0, 19) else null
    }

    private fun safeParse(s: String): ZonedDateTime? = try {
        LocalDateTime.parse(s.substring(0, 19), fmtTs).atZone(zone)
    } catch (_: Throwable) { null }

    companion object {
        private const val TAG = "SleepRollupWorker"
        private const val PROBE_TAG = "SLEEP_SUM_PROBE"
        private const val OUT_FILE = "daily_sleep_summary.csv"
        private const val OUT_FILE_HOURS = "daily_sleep_duration.csv"
        private const val EXPECTED_HEADER = "date,total_sleep_minutes,sleep_efficiency"
        private const val EXPECTED_HEADER_HOURS = "date,hours"
    }
}