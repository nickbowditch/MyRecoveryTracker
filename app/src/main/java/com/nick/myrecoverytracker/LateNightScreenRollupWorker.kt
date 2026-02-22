// app/src/main/java/com/nick/myrecoverytracker/LateNightScreenRollupWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Late-night = Y if any SCREEN ON/OFF or any UNLOCK between 00:00–05:00 local.
 * Writes for yesterday and today each run.
 */
class LateNightScreenRollupWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val fmtTs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

    override fun doWork(): Result {
        Log.e(PROBE, "LATE_NIGHT_PROBE START doWork()")

        return try {
            val dir = applicationContext.filesDir
            val out = ensureHeader(
                File(dir, "daily_late_night_screen_usage.csv"),
                "date,late_night_YN"
            )

            val fScreen = File(dir, "screen_log.csv")
            val fUnlock = File(dir, "unlock_log.csv")

            Log.e(
                PROBE,
                "inputs screen_exists=${fScreen.exists()} screen_size=${if (fScreen.exists()) fScreen.length() else 0} " +
                        "unlock_exists=${fUnlock.exists()} unlock_size=${if (fUnlock.exists()) fUnlock.length() else 0}"
            )

            val today = LocalDate.now(zone)
            val yesterday = today.minusDays(1)

            listOf(yesterday, today).forEach { d ->
                val yn = hadNightActivity(d, fScreen, fUnlock)
                upsert(out, d.format(fmtDate), if (yn) "Y" else "N")
                Log.i(TAG, "LateNight ${d.format(fmtDate)} -> ${if (yn) "Y" else "N"}")
            }

            Log.e(PROBE, "LATE_NIGHT_PROBE END success out=${out.absolutePath}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(PROBE, "LATE_NIGHT_PROBE END failure", t)
            Result.failure()
        }
    }

    private fun hadNightActivity(date: LocalDate, screen: File, unlock: File): Boolean {
        val startZ = date.atStartOfDay(zone)
        val endZ = date.atTime(5, 0).atZone(zone)

        fun inWindow(z: ZonedDateTime): Boolean =
            !z.isBefore(startZ) && z.isBefore(endZ)

        fun fileHit(file: File, predicate: (String) -> Boolean): Boolean {
            if (!file.exists() || file.length() == 0L) return false

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

        val screenHit = fileHit(screen) {
            it.endsWith(",ON", ignoreCase = true) || it.endsWith(",OFF", ignoreCase = true)
        }

        val unlockHit = fileHit(unlock) { true }

        Log.e(PROBE, "window date=${date.format(fmtDate)} screenHit=$screenHit unlockHit=$unlockHit")
        return screenHit || unlockHit
    }

    private fun extractTs(line: String): String? {
        if (line.isBlank()) return null
        val first = line.substringBefore(',')
        if (first.equals("ts", ignoreCase = true)) return null
        if (first.equals("timestamp", ignoreCase = true)) return null
        return first
    }

    private fun safeTs(s: String): ZonedDateTime? = try {
        val trimmed = s.trim()

        if (trimmed.isNotEmpty() && trimmed.all { it.isDigit() }) {
            val n = trimmed.toLong()
            val instant =
                if (n < 10_000_000_000L) Instant.ofEpochSecond(n)
                else Instant.ofEpochMilli(n)
            instant.atZone(zone)
        } else {
            val base = if (trimmed.length >= 19) trimmed.substring(0, 19) else trimmed
            LocalDateTime.parse(base, fmtTs).atZone(zone)
        }
    } catch (_: Throwable) {
        null
    }

    private fun ensureHeader(f: File, header: String): File {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("$header\n")
        }
        return f
    }

    private fun upsert(file: File, dateStr: String, yn: String) {
        val existing =
            if (file.exists()) file.readLines().toMutableList() else mutableListOf()

        if (existing.isEmpty()) {
            file.writeText("date,late_night_YN\n$dateStr,$yn\n")
            return
        }

        val header = existing.first()
        var replaced = false

        for (i in 1 until existing.size) {
            if (existing[i].substringBefore(',') == dateStr) {
                existing[i] = "$dateStr,$yn"
                replaced = true
                break
            }
        }

        if (!replaced) existing.add("$dateStr,$yn")
        file.writeText((listOf(header) + existing.drop(1)).joinToString("\n") + "\n")
    }

    companion object {
        private const val TAG = "LateNightScreenRollup"
        private const val PROBE = "LATE_NIGHT_PROBE"
    }
}