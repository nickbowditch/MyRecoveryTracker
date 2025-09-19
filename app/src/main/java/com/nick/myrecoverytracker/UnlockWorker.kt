// app/src/main/java/com/nick/myrecoverytracker/UnlockWorker.kt
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
import java.util.Locale

class UnlockWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val ctx = applicationContext
    private val tag = "UnlockWorker"
    private val dateRe = Regex("""^(\d{4}-\d{2}-\d{2})""")
    private val zone = ZoneId.systemDefault()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val filesDir = ctx.filesDir ?: return@withContext Result.success()

            val raw = File(filesDir, "unlock_log.csv")
            val lines = if (raw.exists()) raw.readLines() else emptyList()
            val body = if (lines.isNotEmpty() && looksLikeHeader(lines[0])) lines.drop(1) else lines

            val counts = mutableMapOf<String, Int>()
            for (line in body) {
                if (line.isBlank()) continue
                val m = dateRe.find(line) ?: continue
                val date = m.groupValues[1]
                counts[date] = (counts[date] ?: 0) + 1
            }

            // Always upsert today's row, even if zero unlocks
            val today = LocalDate.now(zone).toString() // ISO yyyy-MM-dd
            counts.putIfAbsent(today, 0)

            val outFile = File(filesDir, "daily_unlocks.csv")
            val tmpFile = File(filesDir, "daily_unlocks.csv.tmp")

            val sb = StringBuilder()
            sb.append("date,unlocks\n")
            counts.keys.sorted().forEach { d ->
                sb.append(d).append(",").append(counts[d]).append("\n")
            }

            tmpFile.writeText(sb.toString())
            if (outFile.exists()) outFile.delete()
            if (!tmpFile.renameTo(outFile)) {
                // Fallback: write directly if rename fails
                outFile.writeText(sb.toString())
                tmpFile.delete()
            }

            Log.i(tag, "rollup_written rows=${counts.size}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(tag, "error", t)
            Result.retry()
        }
    }

    private fun looksLikeHeader(first: String): Boolean {
        val lower = first.lowercase(Locale.US)
        return lower.startsWith("ts,") || lower.startsWith("date,")
    }
}