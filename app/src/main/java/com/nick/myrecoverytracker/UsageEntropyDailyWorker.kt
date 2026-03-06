package com.nick.myrecoverytracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ln

class UsageEntropyDailyWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val ctx = applicationContext
        val dir = ctx.filesDir
        ensureHeader(dir)

        val day = dayFmt.format(System.currentTimeMillis())

        val diag = File(dir, "usage_diag.csv")
        if (diag.exists()) {
            val last = diag.readLines().lastOrNull() ?: ""
            if (!last.contains("MODE_ALLOWED")) {
                writeMissing(dir, day)
                return@withContext Result.success()
            }
        }

        val (startMs, endMs) = dayBoundsMillis()

        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats: List<UsageStats> =
            usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMs, endMs)
                ?.filter { it.totalTimeInForeground > 0 } ?: emptyList()

        val pkgTime = mutableMapOf<String, Long>()
        for (u in stats) {
            val pkg = u.packageName ?: continue
            if (shouldSkip(pkg)) continue
            val ms = u.totalTimeInForeground
            if (ms > 0) pkgTime[pkg] = (pkgTime[pkg] ?: 0L) + ms
        }

        if (pkgTime.isEmpty()) return@withContext Result.success()

        val totalMs = pkgTime.values.sum()
        if (totalMs <= 0) return@withContext Result.success()

        val entropy = shannon(pkgTime.values, totalMs)
        writeOut(dir, day, entropy)

        Result.success()
    }

    private fun shannon(values: Collection<Long>, total: Long): Double {
        var h = 0.0
        for (c in values) {
            val p = c.toDouble() / total.toDouble()
            if (p > 0) h -= p * ln(p)
        }
        return h / ln(2.0)
    }

    private fun ensureHeader(dir: File) {
        val f = File(dir, "daily_usage_entropy.csv")
        if (!f.exists() || f.length() == 0L) {
            f.writeText("date,feature_schema_version,daily_usage_entropy_bits\n")
        }
    }

    private fun writeMissing(dir: File, day: String) {
        val f = File(dir, "daily_usage_entropy.csv")
        if (!f.exists()) f.writeText("date,feature_schema_version,daily_usage_entropy_bits\n")
        val lines = f.readLines().toMutableList()
        val row = "$day,$FEATURE_SCHEMA_VERSION,PERMISSION_MISSING"
        val idx = lines.indexOfFirst { it.startsWith("$day,") }
        if (idx >= 0) lines[idx] = row else lines.add(row)
        f.writeText(lines.joinToString("\n") + "\n")
    }

    private fun writeOut(dir: File, day: String, entropy: Double) {
        val f = File(dir, "daily_usage_entropy.csv")
        val lines = f.readLines().toMutableList()
        val row = "%s,%s,%.4f".format(Locale.US, day, FEATURE_SCHEMA_VERSION, entropy)
        val idx = lines.indexOfFirst { it.startsWith("$day,") }
        if (idx >= 0) lines[idx] = row else lines.add(row)
        f.writeText(lines.joinToString("\n") + "\n")
    }

    private fun dayBoundsMillis(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 86400000L
        return start to end
    }

    private fun shouldSkip(pkg: String): Boolean {
        val p = pkg.lowercase(Locale.US)
        return p == "android" ||
                p == "com.nick.myrecoverytracker" ||
                p == "com.android.intentresolver" ||
                p == "com.google.android.apps.nexuslauncher"
    }

    companion object {
        private const val FEATURE_SCHEMA_VERSION = "v6.0"
    }
}
