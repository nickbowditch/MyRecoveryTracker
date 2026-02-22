package com.nick.myrecoverytracker

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Computes app starts per package using UsageEvents MOVE_TO_FOREGROUND events.
 *
 * Output CSV:
 *   daily_app_starts_by_package.csv
 *
 * Schema:
 *   date,feature_schema_version,package_name,starts
 */
class DailyAppStartsByPackageWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "DailyAppStartsByPackage"
        private const val OUTPUT_FILE = "daily_app_starts_by_package.csv"
        private const val FEATURE_SCHEMA_VERSION = 1
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val usm = applicationContext.getSystemService(UsageStatsManager::class.java)
            ?: run {
                Log.w(TAG, "UsageStatsManager not available")
                return@withContext Result.success()
            }

        val now = System.currentTimeMillis()
        val day = dayString(now)
        val (startOfDay, endOfDay) = dayBounds(now)

        val events = usm.queryEvents(startOfDay, endOfDay)
            ?: run {
                Log.w(TAG, "queryEvents returned null")
                return@withContext Result.success()
            }

        val counts = mutableMapOf<String, Int>()
        val ev = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(ev)
            val pkg = ev.packageName ?: continue
            if (skip(pkg)) continue

            if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                counts[pkg] = (counts[pkg] ?: 0) + 1
            }
        }

        val filesDir = applicationContext.filesDir
        val outFile = File(filesDir, OUTPUT_FILE)
        ensureHeader(outFile, "date,feature_schema_version,package_name,starts")

        // Upsert: remove any existing rows for this date, then write fresh ones.
        val existing = outFile.readLines().toMutableList()
        val header = existing.firstOrNull() ?: "date,feature_schema_version,package_name,starts"
        val body = existing.drop(1).filterNot { it.startsWith("$day,") }.toMutableList()

        for ((pkg, starts) in counts.toSortedMap()) {
            body.add("$day,$FEATURE_SCHEMA_VERSION,$pkg,$starts")
        }

        outFile.writeText(
            (listOf(header) + body).joinToString("\n") + "\n"
        )

        Log.i(TAG, "Wrote ${counts.size} packages to $OUTPUT_FILE for $day")
        Result.success()
    }

    private fun dayString(ts: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(ts))
    }

    private fun dayBounds(now: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 24L * 60L * 60L * 1000L
        return start to end
    }

    private fun ensureHeader(f: File, header: String) {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            f.writeText("$header\n")
            return
        }
        val first = f.bufferedReader().use { it.readLine() } ?: ""
        if (first != header) {
            val body = f.readLines().drop(1)
            f.writeText("$header\n" + body.joinToString("\n").ifEmpty { "" })
            if (f.length() > 0L && !f.readText().endsWith("\n")) {
                f.appendText("\n")
            }
        }
    }

    private fun skip(pkg: String): Boolean {
        val p = pkg.lowercase(Locale.US)
        return p == "android" ||
                p == "com.nick.myrecoverytracker" ||
                p.contains("launcher")
    }
}