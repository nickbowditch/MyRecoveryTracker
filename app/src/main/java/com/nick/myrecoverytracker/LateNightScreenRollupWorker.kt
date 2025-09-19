// app/src/main/java/com/nick/myrecoverytracker/LateNightScreenRollupWorker.kt
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
 * Late-night = Y if any SCREEN ON/OFF or any UNLOCK between 00:00–05:00 local.
 * Writes for yesterday and today each run. Robust to headers/extra columns.
 */
class LateNightScreenRollupWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val fmtTs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

    override fun doWork(): Result {
        val dir = applicationContext.filesDir
        val out = ensureHeader(File(dir, "daily_late_night_screen_usage.csv"), "date,late_night_YN")
        val fScreen = File(dir, "screen_log.csv")
        val fUnlock = File(dir, "unlock_log.csv")

        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)

        listOf(yesterday, today).forEach { d ->
            val yn = hadNightActivity(d, fScreen, fUnlock)
            upsert(out, d.format(fmtDate), listOf(if (yn) "Y" else "N"))
            Log.i(TAG, "LateNight ${d.format(fmtDate)} -> ${if (yn) "Y" else "N"}")
        }
        return Result.success()
    }

    private fun hadNightActivity(date: LocalDate, screen: File, unlock: File): Boolean {
        val startZ = date.atStartOfDay(zone)
        val endZ = date.atTime(5, 0).atZone(zone)
        fun inWindow(z: ZonedDateTime) = !z.isBefore(startZ) && z.isBefore(endZ)

        fun fileHit(file: File, predicate: (String) -> Boolean): Boolean {
            if (!file.exists()) return false
            file.bufferedReader().use { br ->
                while (true) {
                    val line = br.readLine() ?: break
                    val tsStr = extractTs(line) ?: continue
                    val z = safeTs(tsStr) ?: continue
                    if (inWindow(z) && predicate(line)) return true
                }
            }
            return false
        }

        val screenHit = fileHit(screen) { it.endsWith(",ON", true) || it.endsWith(",OFF", true) }
        val unlockHit = fileHit(unlock) { true } // any unlock in window counts
        return screenHit || unlockHit
    }

    private fun extractTs(line: String): String? {
        if (line.isBlank()) return null
        // skip headers like "ts,state" or "ts,event"
        if (line.startsWith("ts,", ignoreCase = true)) return null
        if (line.length < 19) return null
        return line.substring(0, 19)
    }

    private fun safeTs(s: String): ZonedDateTime? = try {
        LocalDateTime.parse(s, fmtTs).atZone(zone)
    } catch (_: Throwable) { null }

    private fun ensureHeader(f: File, header: String): File {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("$header\n")
        }
        return f
    }

    private fun upsert(file: File, dateStr: String, tailCols: List<String>) {
        val lines = if (file.exists()) file.readLines().toMutableList() else mutableListOf()
        if (lines.isEmpty()) {
            file.writeText("date,late_night_YN\n$dateStr,${tailCols.joinToString(",")}\n")
            return
        }
        val header = lines.first()
        var replaced = false
        for (i in 1 until lines.size) {
            val key = lines[i].substringBefore(',')
            if (key == dateStr) {
                lines[i] = "$dateStr,${tailCols.joinToString(",")}"
                replaced = true
                break
            }
        }
        if (!replaced) lines.add("$dateStr,${tailCols.joinToString(",")}")
        file.writeText((listOf(header) + lines.drop(1)).joinToString("\n") + "\n")
    }

    companion object { private const val TAG = "LateNightScreenRollup" }
}