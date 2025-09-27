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
    private val schemaVersion = "v6.0"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dir = applicationContext.filesDir ?: return@withContext Result.success()
            val out = File(dir, "daily_unlocks.csv")

            val counts = readUnlockCounts(dir)

            val existing = mutableMapOf<String, Int>()
            if (out.exists() && out.length() > 0L) {
                val lines = out.readLines()
                val body = if (lines.isNotEmpty() && looksLikeHeader(lines[0])) lines.drop(1) else lines
                for (line in body) {
                    val parts = line.split(',')
                    val d = parts.getOrNull(0)?.trim().orEmpty()
                    val v = parts.getOrNull(2)?.trim()?.toIntOrNull()
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
                append("date,feature_schema_version,daily_unlocks\n")
                merged.keys.sorted().forEach { d ->
                    append(d)
                        .append(',')
                        .append(schemaVersion)
                        .append(',')
                        .append(merged[d])
                        .append('\n')
                }
            }
            tmp.writeText(sb.toString())
            if (out.exists()) out.delete()
            tmp.renameTo(out)

            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }

    private fun readUnlockCounts(dir: File): Map<String, Int> {
        val files = listOf(
            File(dir, "unlock_log.csv"),
            File(dir, "unlocks_log.csv") // legacy
        )
        val counts = mutableMapOf<String, Int>()
        files.filter { it.exists() }.forEach { f ->
            val lines = f.readLines()
            val body = if (lines.isNotEmpty() && looksLikeHeader(lines[0])) lines.drop(1) else lines
            for (raw in body) {
                val line = raw.trim()
                if (line.isEmpty()) continue
                // Expect "YYYY-MM-DD HH:MM:SS,UNLOCK"
                val parts = line.split(',')
                if (parts.size >= 2) {
                    val ts = parts[0]
                    val event = parts[1].trim().uppercase(Locale.US)
                    if (ts.length >= 10 && ts[4] == '-' && ts[7] == '-' && event == "UNLOCK") {
                        val d = ts.substring(0, 10)
                        counts[d] = (counts[d] ?: 0) + 1
                    }
                }
            }
        }
        return counts
    }

    private fun looksLikeHeader(first: String): Boolean {
        val l = first.lowercase(Locale.US)
        return l.startsWith("ts,") || l.startsWith("date,")
    }
}