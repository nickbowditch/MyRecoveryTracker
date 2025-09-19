// app/src/main/java/com/nick/myrecoverytracker/MovementRollupWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class MovementRollupWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val tsFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

    // Heuristics
    private val STRIDE_METERS_PER_STEP = 0.75      // default stride: 75cm
    private val DEFAULT_SAMPLE_WINDOW_MS = 10_000L // MovementCapture logs ~every 10s
    private val MAX_GAP_CAP_MS = 20_000L           // cap huge gaps to avoid over-crediting

    // Intensity thresholds on accelerometer magnitude deltas (rough g-units)
    private val THRESH_IGNORE = 0.02   // below this, treat as still/idle (not counted)
    private val THRESH_LOW = 0.15      // [THRESH_IGNORE, THRESH_LOW) => low
    private val THRESH_MOD = 0.35      // [THRESH_LOW, THRESH_MOD) => moderate
    // >= THRESH_MOD => high

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = ZonedDateTime.now(zone).toInstant().toEpochMilli()

        val logFile = File(applicationContext.filesDir, "movement_log.csv")
        if (!logFile.exists()) {
            Log.w("MovementRollupWorker", "movement_log.csv missing; writing zeros")
            upsertDailyMovement(today.toString(), 0.0, 0.0, 0.0, 0.0)
            return@withContext Result.success()
        }

        var firstStep: Double? = null
        var lastStep: Double? = null

        data class Sample(val t: Long, val mag: Double)
        val samples = ArrayList<Sample>(1024)

        // Parse log rows that fall within [start, end]
        logFile.forEachLine { raw ->
            val line = raw.trim()
            if (line.isEmpty()) return@forEachLine
            val comma1 = line.indexOf(',')
            if (comma1 <= 0) return@forEachLine
            val tsStr = line.substring(0, comma1)
            val rest = line.substring(comma1 + 1)

            val t = try {
                ZonedDateTime.parse(tsStr, tsFmt.withZone(zone)).toInstant().toEpochMilli()
            } catch (_: Throwable) {
                return@forEachLine
            }
            if (t < start || t > end) return@forEachLine

            // Step row: yyyy-MM-dd HH:mm:ss,step,<counter>
            if (rest.startsWith("step,")) {
                val parts = rest.split(',')
                if (parts.size >= 2) {
                    val c = parts.getOrNull(1)?.toDoubleOrNull()
                    if (c != null) {
                        if (firstStep == null) firstStep = c
                        lastStep = c
                    }
                }
                return@forEachLine
            }

            // Magnitude row: yyyy-MM-dd HH:mm:ss,<mag>
            val mag = rest.toDoubleOrNull()
            if (mag != null) {
                samples.add(Sample(t, mag))
            }
        }

        // Distance (km) from step delta * stride
        val steps = when {
            firstStep == null || lastStep == null -> 0.0
            else -> max(0.0, lastStep!! - firstStep!!)
        }
        val km = round2((steps * STRIDE_METERS_PER_STEP) / 1000.0)

        // Intensity minutes (low/mod/high) by integrating over time between samples
        samples.sortBy { it.t }
        var lowMs = 0L
        var modMs = 0L
        var highMs = 0L

        for (i in samples.indices) {
            val cur = samples[i]
            val nextT = if (i + 1 < samples.size) samples[i + 1].t else min(end, cur.t + DEFAULT_SAMPLE_WINDOW_MS)
            var dur = nextT - cur.t
            if (dur <= 0) continue
            dur = min(dur, MAX_GAP_CAP_MS)

            val mag = cur.mag
            when {
                mag < THRESH_IGNORE -> {} // ignore idle
                mag < THRESH_LOW    -> lowMs += dur
                mag < THRESH_MOD    -> modMs += dur
                else                -> highMs += dur
            }
        }

        val lowMin = round2(lowMs / 60000.0)
        val modMin = round2(modMs / 60000.0)
        val highMin = round2(highMs / 60000.0)

        upsertDailyMovement(today.toString(), km, lowMin, modMin, highMin)
        Log.i("MovementRollupWorker", "wrote daily: day=$today km=$km low=$lowMin mod=$modMin high=$highMin")

        Result.success()
    }

    private fun upsertDailyMovement(day: String, km: Double, low: Double, mod: Double, high: Double) {
        val name = "daily_movement.csv"
        val header = "date,km,low_min,mod_min,high_min,total_min"
        ensureHeaderExact(name, header)
        val total = round2(low + mod + high)
        val row = "$day,${fmt2(km)},${fmt2(low)},${fmt2(mod)},${fmt2(high)},${fmt2(total)}"
        upsertByFirstColumn(name, day, row)
    }

    private fun ensureHeaderExact(name: String, expectedHeader: String) {
        val f = File(applicationContext.filesDir, name)
        if (!f.exists()) {
            FileOutputStream(f, false).use { it.write((expectedHeader + "\n").toByteArray()) }
            return
        }
        val current = f.bufferedReader().use { it.readLine() }?.trim() ?: ""
        if (current == expectedHeader) return
        val backup = File(applicationContext.filesDir, "$name.legacy")
        runCatching { f.copyTo(backup, overwrite = true) }
        FileOutputStream(f, false).use { it.write((expectedHeader + "\n").toByteArray()) }
    }

    private fun upsertByFirstColumn(name: String, key: String, line: String) {
        val f = File(applicationContext.filesDir, name)
        if (!f.exists()) return
        val lines = f.readLines().toMutableList()
        if (lines.isEmpty()) return
        val header = lines.first()
        var replaced = false
        for (i in 1 until lines.size) {
            val d = lines[i].substringBefore(',')
            if (d == key) {
                lines[i] = line
                replaced = true
                break
            }
        }
        if (!replaced) lines.add(line)
        FileOutputStream(f, false).use { it.write(((listOf(header) + lines.drop(1)).joinToString("\n") + "\n").toByteArray()) }
    }

    private fun round2(v: Double) = round(v * 100.0) / 100.0
    private fun fmt2(v: Double) = String.format(Locale.US, "%.2f", v)
}