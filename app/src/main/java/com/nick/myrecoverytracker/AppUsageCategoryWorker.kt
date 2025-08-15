package com.nick.myrecoverytracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Rolls up daily app usage minutes by category.
 *
 * Input config: files/app_categories.json
 *   {
 *     "recovery": ["au.org.aa.meetings", ...],
 *     "social": ["com.whatsapp", ...],
 *     "entertainment": ["com.google.android.youtube", ...]
 *   }
 *
 * Output CSV: files/daily_app_usage_minutes.csv
 *   Header: date,total_min,recovery_min,social_min,entertainment_min,other_min
 *   Row:    YYYY-MM-DD,*,*,*,*,*
 */
class AppUsageCategoryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val ctx = applicationContext

            if (!UsagePermissionHelper.isGranted(ctx)) {
                Log.w(TAG, "Usage access not granted; skipping.")
                return@withContext Result.success()
            }

            val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            val categories = readCategories(File(ctx.filesDir, "app_categories.json"))
            val catRecovery = categories["recovery"] ?: emptySet()
            val catSocial = categories["social"] ?: emptySet()
            val catEntertainment = categories["entertainment"] ?: emptySet()

            val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val start = end - ONE_DAY_MS

            val raw: List<UsageStats> = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, start, end
            ) ?: emptyList()

            if (raw.isEmpty()) {
                Log.w(TAG, "No UsageStats in last 24h (no activity or OEM quirk).")
                writeRow(day, 0, 0, 0, 0, 0)
                return@withContext Result.success()
            }

            // Aggregate per package (explicit to avoid inference issues)
            val perPackageMillis = HashMap<String, Long>()
            for (u in raw) {
                val pkg = u.packageName ?: continue
                val t = u.totalTimeInForeground.coerceAtLeast(0L)
                if (t > 0L) {
                    perPackageMillis[pkg] = (perPackageMillis[pkg] ?: 0L) + t
                }
            }

            var totalMs = 0L
            var recMs = 0L
            var socMs = 0L
            var entMs = 0L

            for ((pkg, ms) in perPackageMillis) {
                totalMs += ms
                when {
                    pkg in catRecovery -> recMs += ms
                    pkg in catSocial -> socMs += ms
                    pkg in catEntertainment -> entMs += ms
                }
            }

            val otherMs = (totalMs - recMs - socMs - entMs).coerceAtLeast(0L)

            val totalMin = (totalMs / 60000L).toInt()
            val recMin = (recMs / 60000L).toInt()
            val socMin = (socMs / 60000L).toInt()
            val entMin = (entMs / 60000L).toInt()
            val othMin = (otherMs / 60000L).toInt()

            writeRow(day, totalMin, recMin, socMin, entMin, othMin)
            Log.i(
                TAG,
                "AppUsage $day: total=$totalMin, recovery=$recMin, social=$socMin, entertainment=$entMin, other=$othMin (pkgs=${perPackageMillis.size})"
            )

            Result.success()
        }
    }

    private fun writeRow(day: String, total: Int, rec: Int, soc: Int, ent: Int, oth: Int) {
        val out = File(applicationContext.filesDir, "daily_app_usage_minutes.csv")
        val header = "date,total_min,recovery_min,social_min,entertainment_min,other_min"
        val lines: MutableList<String> = if (out.exists()) {
            out.readLines().toMutableList()
        } else {
            mutableListOf(header)
        }
        val filtered = lines.filterNot { it.startsWith("$day,") }.toMutableList()
        filtered.add("$day,$total,$rec,$soc,$ent,$oth")
        out.writeText(filtered.joinToString("\n") + "\n")
    }

    private fun readCategories(file: File): Map<String, Set<String>> {
        if (!file.exists()) {
            Log.w(TAG, "No app_categories.json found; all time will land in 'other'.")
            return emptyMap()
        }
        return try {
            val text = file.readText()
            val root = JSONObject(text)

            fun arrToSet(key: String): Set<String> {
                if (!root.has(key)) return emptySet()
                val arr = root.getJSONArray(key)
                val s = HashSet<String>(arr.length())
                for (i in 0 until arr.length()) {
                    val v = arr.optString(i, "")
                    if (v.isNotEmpty()) s.add(v)
                }
                return s
            }

            return mapOf(
                "recovery" to arrToSet("recovery"),
                "social" to arrToSet("social"),
                "entertainment" to arrToSet("entertainment")
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to parse app_categories.json", t)
            return emptyMap()
        }
    }

    companion object {
        private const val TAG = "AppUsageCategoryWorker"
        private const val ONE_DAY_MS = 24L * 60L * 60L * 1000L
    }
}