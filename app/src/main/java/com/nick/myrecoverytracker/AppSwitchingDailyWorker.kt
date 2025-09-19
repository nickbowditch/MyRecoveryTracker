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
import kotlin.math.ln

/**
 * AppSwitchingDailyWorker
 *
 * Input:  files/usage_events.csv  (format: YYYY-MM-DD,HH:MM:SS,FOREGROUND|BACKGROUND,package)
 * Output: files/daily_app_switching.csv
 *         header: date,switches,entropy
 *
 * For each of {yesterday, today}:
 *   - Consider only FOREGROUND rows.
 *   - switches = count of times the foreground app changes from the previous foreground app.
 *   - entropy  = -Σ p_i * log2(p_i), where p_i = (foreground count for app i) / (total foreground rows that day).
 * Idempotent upsert (replaces the date’s row if present).
 */
class AppSwitchingDailyWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    private val zone = ZoneId.systemDefault()
    private val inFile by lazy { File(applicationContext.filesDir, "usage_events.csv") }
    private val outFile by lazy { File(applicationContext.filesDir, "daily_app_switching.csv") }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!inFile.exists()) {
                ensureHeader(outFile, "date,switches,entropy")
                Log.i(TAG, "No usage_events.csv yet")
                return@withContext Result.success()
            }

            ensureHeader(outFile, "date,switches,entropy")

            val today = LocalDate.now(zone)
            val yesterday = today.minusDays(1)

            val targets = setOf(yesterday.toString(), today.toString())

            // per-day tallies
            data class DayStats(
                var lastPkg: String? = null,
                var switches: Int = 0,
                val freq: MutableMap<String, Int> = HashMap()
            )
            val map = HashMap<String, DayStats>()

            inFile.useLines { seq ->
                seq.forEach { line ->
                    if (line.isBlank()) return@forEach
                    // fast-skip header/older formats
                    if (line.startsWith("date,")) return@forEach

                    // Expected: YYYY-MM-DD,HH:MM:SS,TYPE,pkg
                    // Guard against stray commas in pkg by splitting max 4 parts.
                    val parts = line.split(',', limit = 4)
                    if (parts.size < 4) return@forEach
                    val d = parts[0]
                    if (d !in targets) return@forEach
                    val type = parts[2]
                    if (type != "FOREGROUND") return@forEach
                    val pkg = parts[3]

                    val ds = map.getOrPut(d) { DayStats() }

                    // switches: increment when the foreground app changes
                    val last = ds.lastPkg
                    if (last == null) {
                        ds.lastPkg = pkg
                    } else if (pkg != last) {
                        ds.switches += 1
                        ds.lastPkg = pkg
                    }

                    // frequency for entropy
                    ds.freq[pkg] = (ds.freq[pkg] ?: 0) + 1
                }
            }

            // Write rows for the two days we care about (even if zero → write 0,0.0)
            listOf(yesterday.toString(), today.toString()).forEach { d ->
                val ds = map[d]
                val entropy = if (ds == null || ds.freq.isEmpty()) {
                    0.0
                } else {
                    val total = ds.freq.values.sum().toDouble()
                    ds.freq.values.sumOf { c ->
                        val p = c / total
                        -p * log2(p)
                    }
                }
                upsert(outFile, d, listOf(ds?.switches ?: 0, format1(entropy)))
                Log.i(TAG, "AppSwitchingDaily $d -> switches=${ds?.switches ?: 0} entropy=${format1(entropy)}")
            }

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "AppSwitchingDailyWorker failed", t)
            Result.retry()
        }
    }

    private fun ensureHeader(f: File, header: String) {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("$header\n")
        }
    }

    private fun upsert(file: File, dateStr: String, cols: List<Any>) {
        val header = if (file.exists() && file.length() > 0L)
            file.readLines().first()
        else
            "date,switches,entropy"

        val lines = if (file.exists()) file.readLines().toMutableList() else mutableListOf(header)
        var replaced = false
        for (i in 1 until lines.size) {
            val idx = lines[i].indexOf(',')
            val key = if (idx >= 0) lines[i].substring(0, idx) else lines[i]
            if (key == dateStr) {
                lines[i] = dateStr + "," + cols.joinToString(",")
                replaced = true
                break
            }
        }
        if (!replaced) lines.add(dateStr + "," + cols.joinToString(","))
        file.writeText(lines.joinToString("\n") + "\n")
    }

    private fun log2(x: Double): Double = ln(x) / LN2
    private fun format1(v: Double): String = String.format(Locale.US, "%.1f", v)

    companion object {
        private const val TAG = "AppSwitchingDailyWorker"
        private val LN2 = ln(2.0)
    }
}