package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Rolls up ambient_lux.csv into daily exposure minutes above a lux threshold.
 *
 * Input:
 *   files/ambient_lux.csv           "timestamp,lux"
 * Output:
 *   files/daily_light_exposure.csv  "date,minutes"
 */
class DailyLightExposureWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    private val tag = "DailyLightExposureWorker"
    private val zone: ZoneId = ZoneId.systemDefault()
    private val csvDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val tsFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

    override fun doWork(): Result {
        val filesDir = applicationContext.filesDir
        val src = File(filesDir, "ambient_lux.csv")
        if (!src.exists()) return Result.success()

        val out = ensureHeader(File(filesDir, "daily_light_exposure.csv"), "date,minutes")
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)

        listOf(yesterday, today).forEach { d ->
            val minutes = computeForDate(src, d)
            upsertRow(out, d.format(csvDate), listOf(minutes.toString()))
            Log.i(tag, "Light exposure ${d.format(csvDate)} = ${minutes}min")
        }

        return Result.success()
    }

    private fun computeForDate(src: File, date: LocalDate): Int {
        val dayStart = date.atStartOfDay(zone)
        val dayEnd = date.plusDays(1).atStartOfDay(zone)

        val samples = ArrayList<Pair<ZonedDateTime, Float>>()
        src.forEachLine { line ->
            if (line.isBlank() || line.startsWith("timestamp,")) return@forEachLine
            if (line.length < 19) return@forEachLine
            val tsStr = line.substring(0, 19)
            val luxStr = line.substringAfter(',', "").trim()
            val zdt = safeParse(tsStr) ?: return@forEachLine
            if (zdt.isBefore(dayStart) || !zdt.isBefore(dayEnd)) return@forEachLine
            val lux = luxStr.toFloatOrNull() ?: return@forEachLine
            samples += zdt to lux
        }
        if (samples.isEmpty()) return 0
        samples.sortBy { it.first.toInstant() }

        var minutes = 0.0
        for (i in samples.indices) {
            val (t, lux) = samples[i]
            val nextT = if (i < samples.lastIndex) samples[i + 1].first else dayEnd
            var deltaMin = Duration.between(t, nextT).toMinutes().toDouble()
            if (deltaMin < 0) continue
            if (deltaMin > 30) deltaMin = 30.0
            if (lux >= 50.0f) minutes += deltaMin
        }

        return minutes.coerceIn(0.0, 1440.0).toInt()
    }

    private fun safeParse(s: String): ZonedDateTime? = try {
        LocalDateTime.parse(s, tsFmt).atZone(zone)
    } catch (_: Throwable) { null }

    private fun ensureHeader(f: File, header: String): File {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText(header + "\n")
        }
        return f
    }

    private fun upsertRow(file: File, dateStr: String, tailCols: List<String>) {
        val lines = if (file.exists()) file.readLines().toMutableList() else mutableListOf()
        if (lines.isEmpty()) return
        val header = lines.first()
        var replaced = false
        for (i in 1 until lines.size) {
            val idx = lines[i].indexOf(',')
            val key = if (idx >= 0) lines[i].substring(0, idx) else lines[i]
            if (key == dateStr) {
                lines[i] = dateStr + "," + tailCols.joinToString(",")
                replaced = true; break
            }
        }
        if (!replaced) lines.add(dateStr + "," + tailCols.joinToString(","))
        file.writeText((listOf(header) + lines.drop(1)).joinToString("\n") + "\n")
    }
}