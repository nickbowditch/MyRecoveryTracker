// app/src/main/java/com/nick/myrecoverytracker/SleepRollupWorker.kt
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
        val fUnlock = File(dir, "unlock_log.csv")
        val fScreen = File(dir, "screen_log.csv")
        val fNotif  = File(dir, "notification_log.csv")

        val outSummary = ensureHeader(File(dir, "daily_sleep_summary.csv"),
            "date,sleep_time,wake_time,duration_hours")
        val outDur     = ensureHeader(File(dir, "daily_sleep_duration.csv"),
            "date,hours")
        val outST      = ensureHeader(File(dir, "daily_sleep_time.csv"),
            "date,HH:MM:SS")
        val outWT      = ensureHeader(File(dir, "daily_wake_time.csv"),
            "date,HH:MM:SS")
        val outQ       = ensureHeader(File(dir, "daily_sleep_quality.csv"),
            "date,quality")

        val today = LocalDate.now(zone)
        val backfillDays = inputData.getInt("backfill_days", 30).coerceIn(1, 400)
        val startDate = today.minusDays(backfillDays.toLong())

        var processed = 0
        var wrote = 0
        var lastLog: String? = null

        var d = startDate
        while (!d.isAfter(today)) {
            val r = computeForDate(d, fUnlock, fScreen, fNotif)
            upsert(outSummary, d.format(fmtDate), listOf(r.sleepTime ?: "", r.wakeTime ?: "", to2(r.hours)))
            upsert(outDur,     d.format(fmtDate), listOf(to2(r.hours)))
            upsert(outST,      d.format(fmtDate), listOf(r.sleepTime ?: ""))
            upsert(outWT,      d.format(fmtDate), listOf(r.wakeTime ?: ""))
            upsert(outQ,       d.format(fmtDate), listOf(r.quality))
            processed++
            if (r.sleepTime != null || r.wakeTime != null || r.hours > 0.0) wrote++
            lastLog = "Sleep ${d.format(fmtDate)} -> sleep=${r.sleepTime ?: "-"} wake=${r.wakeTime ?: "-"} hours=${to2(r.hours)} quality=${r.quality}"
            Log.i(TAG, lastLog!!)
            d = d.plusDays(1)
        }

        // TC-1 heal: if both UTC "today" and LOCAL "today" exist, prefer LOCAL and drop UTC key
        healDropUtcTodayIfBoth(outSummary)
        healDropUtcTodayIfBoth(outDur)
        healDropUtcTodayIfBoth(outST)
        healDropUtcTodayIfBoth(outWT)
        healDropUtcTodayIfBoth(outQ)

        rotateByDate(outSummary, keepDays = 400)
        rotateByDate(outDur,     keepDays = 400)
        rotateByDate(outST,      keepDays = 400)
        rotateByDate(outWT,      keepDays = 400)
        rotateByDate(outQ,       keepDays = 400)

        Log.i(TAG, "Sleep backfill_days=$backfillDays processed=$processed wrote=$wrote last=\"$lastLog\"")
        return Result.success()
    }

    private data class SleepResult(
        val sleepTime: String?,
        val wakeTime: String?,
        val hours: Double,
        val quality: String
    )

    private fun computeForDate(date: LocalDate, fUnlock: File, fScreen: File, fNotif: File): SleepResult {
        // Strict wake: first UNLOCK >= 04:00 with no SCREEN_OFF within 20s after the UNLOCK
        val wakeZ = firstStrictWakeAfter4am(fUnlock, fScreen, date, debug = true)
            ?: return SleepResult(null, null, 0.0, "NO_MORNING_WAKE")

        // Strict sleep: last SCREEN_OFF/LOCK in [21:00 prev day, 04:00 same day] with 60m quiet
        val sleepZ = lastStrictSleepWith60mQuiet(fScreen, fUnlock, date, wakeZ, debug = true)
            ?: return SleepResult(null, wakeZ.toLocalTime().formatHms(), 0.0, "NO_SLEEP_CANDIDATE")

        return finalize(sleepZ, wakeZ, "OK")
    }

    private fun finalize(sleepZ: ZonedDateTime, wakeZ: ZonedDateTime, qual: String): SleepResult {
        val hrs = Duration.between(sleepZ, wakeZ).seconds.coerceAtLeast(0) / 3600.0
        return SleepResult(
            sleepTime = sleepZ.toLocalTime().formatHms(),
            wakeTime  = wakeZ.toLocalTime().formatHms(),
            hours     = round2(hrs.coerceIn(0.0, 12.0)),
            quality   = qual
        )
    }

    private fun firstStrictWakeAfter4am(
        fUnlock: File,
        fScreen: File,
        date: LocalDate,
        debug: Boolean = false
    ): ZonedDateTime? {
        val dayStart = date.atTime(4, 0).atZone(zone)
        val dayEnd   = date.plusDays(1).atStartOfDay(zone) // hard end: 00:00 next day

        // Load unlocks in [04:00, 24:00) of `date`
        val unlocks = mutableListOf<ZonedDateTime>()
        if (fUnlock.exists()) fUnlock.useLines { seq ->
            seq.forEach { line ->
                if (!line.endsWith(",UNLOCK", true)) return@forEach
                val ts = extractTs(line) ?: return@forEach
                val z = safeParse(ts) ?: return@forEach
                if (!z.isBefore(dayStart) && z.isBefore(dayEnd)) unlocks += z
            }
        }
        unlocks.sortBy { it.toInstant().toEpochMilli() }

        // SCREEN_OFFs (for 20s rejection)
        val offs = mutableListOf<ZonedDateTime>()
        if (fScreen.exists()) fScreen.useLines { seq ->
            seq.forEach { line ->
                val isOff = line.endsWith(",SCREEN_OFF", true) || line.endsWith(",OFF", true)
                if (!isOff) return@forEach
                val ts = extractTs(line) ?: return@forEach
                val z = safeParse(ts) ?: return@forEach
                offs += z
            }
        }
        offs.sortBy { it.toInstant().toEpochMilli() }

        for (u in unlocks) {
            val limit = u.plusSeconds(20)
            val offWithin20s = offs.firstOrNull { it.isAfter(u) && it.isBefore(limit) }
            if (debug) {
                if (offWithin20s != null) {
                    val diff = Duration.between(u, offWithin20s).seconds
                    Log.i(TAG, "WAKE_REJECT unlock=$u reason=SCREEN_OFF at $offWithin20s (+${diff}s < 20s)")
                } else {
                    Log.i(TAG, "WAKE_ACCEPT unlock=$u (no SCREEN_OFF in next 20s)")
                }
            }
            if (offWithin20s == null) return u
        }

        if (debug) Log.i(TAG, "WAKE_NONE date=$date reason=No UNLOCK in [${dayStart.toLocalTime()}–${dayEnd.toLocalTime()}) that survives 20s")
        return null
    }

    private fun lastStrictSleepWith60mQuiet(
        fScreen: File,
        fUnlock: File,
        date: LocalDate,
        wakeZ: ZonedDateTime,
        debug: Boolean = false
    ): ZonedDateTime? {
        val windowStart = date.minusDays(1).atTime(21, 0).atZone(zone)
        val windowEndByRule = date.atTime(4, 0).atZone(zone)
        // Don’t look past wake
        val windowEnd = minOf(windowEndByRule, wakeZ)

        if (!windowEnd.isAfter(windowStart)) {
            if (debug) logI("SLEEP_NONE date=$date reason=Empty window")
            return null
        }

        // Candidates: SCREEN_OFF or LOCK within window
        data class C(val z: ZonedDateTime, val type: String)
        val candidates = mutableListOf<C>()

        if (fScreen.exists()) fScreen.useLines { seq ->
            seq.forEach { line ->
                val isOff = line.endsWith(",SCREEN_OFF", true) || line.endsWith(",OFF", true)
                val isOn  = line.endsWith(",SCREEN_ON",  true) || line.endsWith(",ON",  true)
                val ts = extractTs(line) ?: return@forEach
                val z = safeParse(ts) ?: return@forEach
                if (isOff && !z.isBefore(windowStart) && !z.isAfter(windowEnd)) {
                    candidates += C(z, "SCREEN_OFF")
                }
            }
        }

        // LOCKs frequently live in unlock log; include those as candidates
        if (fUnlock.exists()) fUnlock.useLines { seq ->
            seq.forEach { line ->
                if (!line.endsWith(",LOCK", true)) return@forEach
                val ts = extractTs(line) ?: return@forEach
                val z = safeParse(ts) ?: return@forEach
                if (!z.isBefore(windowStart) && !z.isAfter(windowEnd)) {
                    candidates += C(z, "LOCK")
                }
            }
        }

        if (candidates.isEmpty()) {
            if (debug) logI("SLEEP_NONE date=$date reason=No candidates in window")
            return null
        }
        candidates.sortBy { it.z.toInstant().toEpochMilli() }

        // Disqualifiers for the 60-minute quiet period
        val unlocks = mutableListOf<ZonedDateTime>()
        if (fUnlock.exists()) fUnlock.useLines { seq ->
            seq.forEach { line ->
                if (!line.endsWith(",UNLOCK", true)) return@forEach
                val ts = extractTs(line) ?: return@forEach
                val z = safeParse(ts) ?: return@forEach
                if (!z.isBefore(windowStart) && !z.isAfter(wakeZ)) unlocks += z
            }
        }
        unlocks.sortBy { it.toInstant().toEpochMilli() }

        val ons = mutableListOf<ZonedDateTime>()
        if (fScreen.exists()) fScreen.useLines { seq ->
            seq.forEach { line ->
                val isOn = line.endsWith(",SCREEN_ON", true) || line.endsWith(",ON", true)
                if (!isOn) return@forEach
                val ts = extractTs(line) ?: return@forEach
                val z = safeParse(ts) ?: return@forEach
                if (!z.isBefore(windowStart) && !z.isAfter(wakeZ)) ons += z
            }
        }
        ons.sortBy { it.toInstant().toEpochMilli() }

        fun hasNoiseWithin60mAfter(anchor: ZonedDateTime): Boolean {
            val end = minOf(anchor.plusMinutes(60), wakeZ)
            if (unlocks.any { it.isAfter(anchor) && it.isBefore(end) }) return true
            if (ons.any     { it.isAfter(anchor) && it.isBefore(end) }) return true
            return false
        }

        // Choose the LAST candidate that has a clean 60-minute quiet tail
        for (i in candidates.indices.reversed()) {
            val c = candidates[i]
            val noisy = hasNoiseWithin60mAfter(c.z)
            if (debug) {
                if (noisy) logI("SLEEP_REJECT at=${c.z} type=${c.type} reason=UNLOCK/SCREEN_ON within 60m")
                else       logI("SLEEP_ACCEPT at=${c.z} type=${c.type} (60m quiet)")
            }
            if (!noisy) return c.z
        }

        if (debug) logI("SLEEP_NONE date=$date reason=No candidate with 60m quiet")
        return null
    }

    private fun extractTs(line: String): String? {
        val t = line.trim()
        if (t.isEmpty()) return null
        if (t.startsWith("date,", true) || t.startsWith("timestamp,", true)) return null

        val parts = t.split(',').map { it.trim() }
        if (parts.isEmpty()) return null

        // Accept:
        // 1) "yyyy-MM-dd HH:mm:ss,EVENT"
        // 2) "yyyy-MM-ddTHH:mm:ss,EVENT"
        // 3) "yyyy-MM-dd,HH:mm:ss,EVENT"
        return when {
            parts.size >= 1 && parts[0].length >= 19 && parts[0][10] == ' ' ->
                parts[0].substring(0, 19)
            parts.size >= 1 && parts[0].length >= 19 && parts[0][10] == 'T' ->
                parts[0].replace('T', ' ').substring(0, 19)
            parts.size >= 2 && parts[0].length == 10 && parts[1].length >= 8 ->
                parts[0] + " " + parts[1].substring(0, 8)
            else -> null
        }
    }

    private fun safeParse(s: String): ZonedDateTime? = try {
        LocalDateTime.parse(s.substring(0, 19), fmtTs).atZone(zone)
    } catch (_: Throwable) { null }

    private fun ensureHeader(f: File, header: String): File {
        f.parentFile?.mkdirs()
        if (!f.exists()) {
            writeAtomic(f, header + "\n")
            Log.i(TAG, "CREATED ${f.absolutePath}")
        } else if (f.length() == 0L) {
            writeAtomic(f, header + "\n")
            Log.i(TAG, "WROTE_HEADER ${f.absolutePath}")
        } else {
            Log.i(TAG, "EXISTS ${f.absolutePath} size=${f.length()}")
        }
        return f
    }

    private fun upsert(file: File, dateStr: String, tailCols: List<String>) {
        val lines = if (file.exists()) file.readLines().toMutableList() else mutableListOf()
        if (lines.isEmpty()) return
        val header = lines.first()
        var replaced = false
        for (i in 1 until lines.size) {
            val idx = lines[i].indexOf(',')
            val key = if (idx >= 0) lines[i].substring(0, idx) else lines[i]
            if (key == dateStr) {
                lines[i] = csvJoin(listOf(dateStr) + tailCols)
                replaced = true
                break
            }
        }
        if (!replaced) lines.add(csvJoin(listOf(dateStr) + tailCols))
        writeAtomic(file, (listOf(header) + lines.drop(1)).joinToString("\n") + "\n")
    }

    private fun rotateByDate(file: File, keepDays: Int) {
        if (!file.exists()) return
        val lines = file.readLines()
        if (lines.isEmpty()) return
        val header = lines.first()
        val cutoff = LocalDate.now(zone).minusDays(keepDays.toLong())
        val kept = lines.drop(1).filter { line ->
            val idx = line.indexOf(',')
            if (idx <= 0) true else {
                val ds = line.substring(0, idx)
                try {
                    LocalDate.parse(ds, fmtDate) >= cutoff
                } catch (_: Throwable) {
                    true
                }
            }
        }
        writeAtomic(file, (sequenceOf(header) + kept.asSequence()).joinToString("\n") + "\n")
    }

    private fun healDropUtcTodayIfBoth(file: File) {
        if (!file.exists()) return
        val lines = file.readLines()
        if (lines.isEmpty()) return
        val header = lines.first()
        val body = lines.drop(1).toMutableList()

        val localToday = LocalDate.now(zone).format(fmtDate)
        val utcToday = LocalDate.now(ZoneOffset.UTC).format(fmtDate)

        if (localToday != utcToday) {
            val hasLocal = body.any { it.startsWith("$localToday,") }
            val hasUtc = body.any { it.startsWith("$utcToday,") }
            if (hasLocal && hasUtc) {
                val pruned = body.filterNot { it.startsWith("$utcToday,") }
                writeAtomic(file, (sequenceOf(header) + pruned.asSequence()).joinToString("\n") + "\n")
            }
        }
    }

    private fun csvJoin(cols: List<String>): String =
        cols.joinToString(",") { csvEscape(it) }

    private fun csvEscape(s: String): String {
        if (s.isEmpty()) return ""
        val needs = s.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (!needs) s else "\"" + s.replace("\"", "\"\"") + "\""
    }

    private fun writeAtomic(dst: File, content: String) {
        dst.parentFile?.mkdirs()
        val tmp = File(dst.parentFile, dst.name + ".tmp")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(tmp)
            val ch: FileChannel = fos.channel
            ch.truncate(0)
            ch.write(java.nio.ByteBuffer.wrap(content.toByteArray()))
            ch.force(true)
            ch.close()
        } finally {
            try { fos?.close() } catch (_: Throwable) {}
        }
        if (!tmp.renameTo(dst)) {
            try {
                FileOutputStream(dst, false).use { it.write(content.toByteArray()) }
                tmp.delete()
            } catch (_: Throwable) {
                dst.delete()
                tmp.renameTo(dst)
            }
        }
    }

    private fun LocalTime.formatHms(): String =
        String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second)

    private fun logI(msg: String) {
        Log.i(TAG, msg)
    }

    private fun round2(v: Double): Double = round(v * 100.0) / 100.0
    private fun to2(v: Double): String = String.format(Locale.US, "%.2f", v)

    companion object { private const val TAG = "SleepRollupWorker" }
}