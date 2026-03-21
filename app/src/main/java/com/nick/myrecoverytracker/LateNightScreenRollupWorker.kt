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
            val dir = applicationContext.getExternalFilesDir(null)
                ?: throw IllegalStateException("External files dir unavailable")
            val dataDir = File(dir, "data")
            dataDir.mkdirs()

            val out = ensureHeader(
                File(dataDir, "daily_late_night_screen_usage.csv"),
                HEADER
            )

            val fScreen = File(dataDir, "screen_log.csv")
            val fUnlock = File(dataDir, "unlock_log.csv")

            Log.e(
                PROBE,
                "inputs screen_exists=${fScreen.exists()} screen_size=${if (fScreen.exists()) fScreen.length() else 0} " +
                        "unlock_exists=${fUnlock.exists()} unlock_size=${if (fUnlock.exists()) fUnlock.length() else 0}"
            )

            val today = LocalDate.now(zone)
            val yesterday = today.minusDays(1)
            val participantId = ParticipantIdManager.getOrCreate(applicationContext)

            listOf(yesterday, today).forEach { d ->
                val dateStr = d.format(fmtDate)
                val (yn, screenHit, unlockHit) = hadNightActivity(d, fScreen, fUnlock)
                val event = if (yn) "late_night_activity" else "no_late_night_activity"
                upsert(out, dateStr, participantId, if (yn) "Y" else "N", event)
                Log.i(TAG, "LateNight ${dateStr} -> ${if (yn) "Y" else "N"} (screen=$screenHit, unlock=$unlockHit) event=$event")
            }

            Log.e(PROBE, "LATE_NIGHT_PROBE END success out=${out.absolutePath}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(PROBE, "LATE_NIGHT_PROBE END failure", t)
            Result.failure()
        }
    }

    private fun hadNightActivity(date: LocalDate, screen: File, unlock: File): Triple<Boolean, Boolean, Boolean> {
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
        return Triple(screenHit || unlockHit, screenHit, unlockHit)
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

    private fun upsert(file: File, dateStr: String, participantId: String, yn: String, event: String) {
        val existing =
            if (file.exists()) file.readLines().toMutableList() else mutableListOf()

        if (existing.isEmpty()) {
            val recordId = "${participantId}_${dateStr}"
            file.writeText("$HEADER\n$recordId,$participantId,$dateStr,$FEATURE_SCHEMA_VERSION,$event,$yn\n")
            return
        }

        val header = existing.first()
        var replaced = false

        for (i in 1 until existing.size) {
            val cols = existing[i].split(',')
            if (cols.size > 2 && cols[2] == dateStr) {
                val recordId = "${participantId}_${dateStr}"
                existing[i] = "$recordId,$participantId,$dateStr,$FEATURE_SCHEMA_VERSION,$event,$yn"
                replaced = true
                break
            }
        }

        if (!replaced) {
            val recordId = "${participantId}_${dateStr}"
            existing.add("$recordId,$participantId,$dateStr,$FEATURE_SCHEMA_VERSION,$event,$yn")
        }
        file.writeText((listOf(header) + existing.drop(1)).joinToString("\n") + "\n")
    }

    companion object {
        private const val TAG = "LateNightScreenRollup"
        private const val PROBE = "LATE_NIGHT_PROBE"
        private const val FEATURE_SCHEMA_VERSION = "v6.0"
        private const val HEADER = "record_id,participant_id,date,feature_schema_version,event,late_night"
    }
}