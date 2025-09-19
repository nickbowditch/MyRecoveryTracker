// app/src/main/java/com/nick/myrecoverytracker/UsageEntropyDailyWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ln

class UsageEntropyDailyWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dir = applicationContext.filesDir
            val today = dayFmt.format(System.currentTimeMillis())
            ensureHeader(dir)

            val switching = File(dir, "daily_app_switching.csv")
            val usageEvents = File(dir, "usage_events.csv")

            var entropyFromSwitching: Double? = null
            if (switching.exists()) {
                switching.forEachLine { line ->
                    if (line.startsWith("$today,")) {
                        val parts = line.split(',')
                        if (parts.size >= 3) {
                            val e = parts[2].trim().toDoubleOrNull()
                            if (e != null) entropyFromSwitching = e
                        }
                    }
                }
            }
            if (entropyFromSwitching != null) {
                writeOut(dir, today, entropyFromSwitching!!)
                return@withContext Result.success()
            }

            val freq = mutableMapOf<String, Int>()
            var total = 0
            if (usageEvents.exists()) {
                usageEvents.forEachLine { line ->
                    if (!line.startsWith("$today,")) return@forEachLine
                    val parts = line.split(',')
                    if (parts.size >= 2) {
                        val pkg = parts[1].trim()
                        if (pkg.isNotEmpty()) {
                            freq[pkg] = (freq[pkg] ?: 0) + 1
                            total++
                        }
                    }
                }
            }

            if (total == 0) {
                // no write for today (header already ensured)
                return@withContext Result.success()
            }

            val entropyBits = shannonEntropy(freq.values, total)
            writeOut(dir, today, entropyBits)
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }

    private fun shannonEntropy(counts: Collection<Int>, total: Int): Double {
        var h = 0.0
        for (c in counts) {
            val p = c.toDouble() / total
            h -= p * ln(p)
        }
        return h / ln(2.0)
    }

    private fun ensureHeader(dir: File) {
        val f = File(dir, "daily_usage_entropy.csv")
        if (!f.exists()) {
            f.writeText("date,entropy_bits\n")
        }
    }

    private fun writeOut(dir: File, day: String, entropy: Double) {
        val f = File(dir, "daily_usage_entropy.csv")
        if (!f.exists()) f.writeText("date,entropy_bits\n")
        val lines = f.readLines().toMutableList()
        val row = "%s,%.4f".format(Locale.US, day, entropy)
        val idx = lines.indexOfFirst { it.startsWith("$day,") }
        if (idx >= 0) lines[idx] = row else lines.add(row)
        f.writeText(lines.joinToString("\n") + "\n")
    }
}