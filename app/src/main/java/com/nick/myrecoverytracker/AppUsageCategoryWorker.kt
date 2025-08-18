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
 *     "entertainment": ["com.google.android.youtube", ...],
 *     "dating": ["com.tinder", "com.bumble.app", ...]   // NEW
 *   }
 *
 * Output CSV: files/daily_app_usage_minutes.csv
 *   Header (new): date,total_min,recovery_min,social_min,entertainment_min,dating_min,other_min
 *   Row:          YYYY-MM-DD,*,*,*,*,*,*
 *
 * Backward compatibility: if an older CSV without dating_min is present,
 * this worker upgrades it in-place by inserting dating_min=0 for prior rows.
 */
class AppUsageCategoryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
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
        val catDating = categories["dating"] ?: emptySet() // NEW

        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - ONE_DAY_MS

        val raw: List<UsageStats> = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, start, end
        ) ?: emptyList()

        if (raw.isEmpty()) {
            Log.w(TAG, "No UsageStats in last 24h (no activity or OEM quirk).")
            writeRow(day, 0, 0, 0, 0, 0, 0)
            return@withContext Result.success()
        }

        // Aggregate per package
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
        var datMs = 0L

        for ((pkg, ms) in perPackageMillis) {
            totalMs += ms
            when {
                pkg in catRecovery -> recMs += ms
                pkg in catSocial -> socMs += ms
                pkg in catEntertainment -> entMs += ms
                pkg in catDating -> datMs += ms
            }
        }

        val otherMs = (totalMs - recMs - socMs - entMs - datMs).coerceAtLeast(0L)

        val totalMin = (totalMs / 60000L).toInt()
        val recMin = (recMs / 60000L).toInt()
        val socMin = (socMs / 60000L).toInt()
        val entMin = (entMs / 60000L).toInt()
        val datMin = (datMs / 60000L).toInt()
        val othMin = (otherMs / 60000L).toInt()

        writeRow(day, totalMin, recMin, socMin, entMin, datMin, othMin)
        Log.i(
            TAG,
            "AppUsage $day: total=$totalMin, recovery=$recMin, social=$socMin, entertainment=$entMin, dating=$datMin, other=$othMin (pkgs=${perPackageMillis.size})"
        )

        Result.success()
    }

    private fun writeRow(
        day: String,
        total: Int,
        rec: Int,
        soc: Int,
        ent: Int,
        dat: Int,
        oth: Int
    ) {
        val out = File(applicationContext.filesDir, "daily_app_usage_minutes.csv")
        val newHeader = "date,total_min,recovery_min,social_min,entertainment_min,dating_min,other_min"
        val oldHeader = "date,total_min,recovery_min,social_min,entertainment_min,other_min"

        val lines: MutableList<String> = if (out.exists()) {
            val existing = out.readLines().toMutableList()
            if (existing.isNotEmpty() && existing[0].trim() == oldHeader) {
                // Upgrade old file to include dating_min column (insert 0 before other)
                val upgraded = mutableListOf<String>()
                upgraded += newHeader
                for (i in 1 until existing.size) {
                    val row = existing[i].trim()
                    if (row.isEmpty()) continue
                    val parts = row.split(",")
                    if (parts.size == 6) {
                        // date, total, rec, soc, ent, other -> insert dating=0 at index 5
                        val withDating = listOf(
                            parts[0], parts[1], parts[2], parts[3], parts[4], "0", parts[5]
                        ).joinToString(",")
                        upgraded += withDating
                    } else {
                        // Unexpected; keep as-is to avoid data loss
                        upgraded += row
                    }
                }
                upgraded
            } else {
                // Already new header or something custom; ensure header line is correct
                if (existing.isEmpty() || existing[0].trim() != newHeader) {
                    // Replace/insert header safely
                    val rest = if (existing.isNotEmpty()) existing.drop(1) else emptyList()
                    mutableListOf<String>().apply {
                        add(newHeader)
                        addAll(rest)
                    }
                } else {
                    existing
                }
            }.toMutableList()
        } else {
            mutableListOf(newHeader)
        }

        // De-dup today's line then append fresh row
        val filtered = lines.filterNot { it.startsWith("$day,") }.toMutableList()
        filtered.add("$day,$total,$rec,$soc,$ent,$dat,$oth")
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

            mapOf(
                "recovery" to arrToSet("recovery"),
                "social" to arrToSet("social"),
                "entertainment" to arrToSet("entertainment"),
                "dating" to arrToSet("dating") // NEW
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to parse app_categories.json", t)
            emptyMap()
        }
    }

    companion object {
        private const val TAG = "AppUsageCategoryWorker"
        private const val ONE_DAY_MS = 24L * 60L * 60L * 1000L
    }
}