// app/src/main/java/com/nick/myrecoverytracker/UnlockRollupWorker.kt
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
import java.time.ZoneOffset
import java.util.Locale

class UnlockRollupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    private val recentDays = 7
    private val zone: ZoneId = ZoneId.systemDefault()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dir = applicationContext.filesDir ?: return@withContext Result.success()
            val raw = File(dir, "unlock_log.csv")
            val out = File(dir, "daily_unlocks.csv")

            val counts = mutableMapOf<String, Int>()
            if (raw.exists()) {
                val lines = raw.readLines()
                val body = if (lines.isNotEmpty() && looksLikeHeader(lines[0])) lines.drop(1) else lines
                for (line in body) {
                    if (line.isBlank()) continue
                    val date = extractDate(line) ?: continue
                    counts[date] = (counts[date] ?: 0) + 1
                }
            }

            val existing = mutableMapOf<String, Int>()
            if (out.exists() && out.length() > 0L) {
                val lines = out.readLines()
                val body = if (lines.isNotEmpty() && looksLikeHeader(lines[0])) lines.drop(1) else lines
                for (line in body) {
                    val parts = line.split(',', limit = 2)
                    val d = parts.getOrNull(0)?.trim().orEmpty()
                    val v = parts.getOrNull(1)?.trim()?.toIntOrNull()
                    if (d.length == 10 && v != null) existing[d] = v
                }
            }

            val merged = existing.toMutableMap()
            for ((d, v) in counts) merged[d] = v

            val todayLocal = LocalDate.now(zone)
            for (i in 0 until recentDays) {
                val d = todayLocal.minusDays(i.toLong()).toString()
                if (!merged.containsKey(d)) merged[d] = 0
            }

            val localToday = todayLocal.toString()
            val utcToday = LocalDate.now(ZoneOffset.UTC).toString()
            if (localToday != utcToday && merged.containsKey(localToday) && merged.containsKey(utcToday)) {
                merged.remove(utcToday)
            }

            val tmp = File(dir, "daily_unlocks.csv.tmp")
            val sb = StringBuilder().apply {
                append("date,unlocks\n")
                merged.keys.sorted().forEach { d ->
                    append(d).append(',').append(merged[d]).append('\n')
                }
            }
            tmp.writeText(sb.toString())
            if (out.exists()) out.delete()
            tmp.renameTo(out)

            runCatching {
                val lines2 = out.takeIf { it.exists() }?.readLines() ?: return@runCatching
                val hasLocal = lines2.drop(1).any { it.startsWith("$localToday,") }
                val hasUtc = lines2.drop(1).any { it.startsWith("$utcToday,") }
                if (!(hasLocal && !hasUtc)) {
                    Log.e("UnlockRollupWorker", "TC-1 violation: local=$hasLocal utc=$hasUtc")
                }
            }

            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }

    private fun looksLikeHeader(first: String): Boolean {
        val l = first.lowercase(Locale.US)
        return l.startsWith("ts,") || l.startsWith("date,")
    }

    private fun extractDate(line: String): String? {
        return if (line.length >= 10 && line[4] == '-' && line[7] == '-') {
            line.substring(0, 10)
        } else {
            null
        }
    }
}