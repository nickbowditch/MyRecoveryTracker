package com.nick.myrecoverytracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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

class AppUsageByCategoryDailyWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ctx = applicationContext
            val filesDir = ctx.filesDir
            if (!filesDir.exists()) filesDir.mkdirs()

            val byCategoryFile = File(filesDir, "app_category_daily.csv")
            ensureHeader(byCategoryFile, "date,category,minutes")

            val day = today()
            val (startMs, endMs) = dayBoundsMillis()

            // Prefer existing per-app/per-category CSVs when present; fall back to UsageStats if none.
            val fromCsv = readCategoryMinutesFromCsv(filesDir, day)

            val perCatMs: Map<String, Long> = if (fromCsv != null && fromCsv.isNotEmpty()) {
                // minutes -> ms
                fromCsv.mapValues { (_, mins) -> (mins * 60000.0).toLong().coerceAtLeast(0L) }
            } else {
                val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMs, endMs)
                    ?.filter { it.totalTimeInForeground > 0 } ?: emptyList()

                val pm = ctx.packageManager
                val agg = HashMap<String, Long>()
                var packagesCounted = 0
                for (u: UsageStats in stats) {
                    val pkg = u.packageName ?: continue
                    if (shouldSkip(pkg)) continue
                    val ai = try { pm.getApplicationInfo(pkg, 0) } catch (_: PackageManager.NameNotFoundException) { null }
                    val cat = classify(pkg, ai)
                    val ms = u.totalTimeInForeground
                    if (ms > 0) {
                        agg[cat] = (agg[cat] ?: 0L) + ms
                        packagesCounted++
                    }
                }
                Log.i(TAG, "AppUsageByCategory (UsageStats fallback) -> $day categories=${agg.size} (packages=$packagesCounted)")
                agg
            }

            val existing = if (byCategoryFile.exists()) byCategoryFile.readLines() else emptyList()
            val kept = existing.filterNot { it.startsWith("$day,") }.toMutableList()
            val lines = perCatMs.entries
                .sortedByDescending { it.value }
                .map { e -> String.format(Locale.US, "%s,%s,%.1f", day, e.key, e.value / 60000.0) }

            if (kept.isEmpty() || !kept.first().startsWith("date,"))
                kept.add(0, "date,category,minutes")
            kept.addAll(lines)
            byCategoryFile.writeText(kept.joinToString("\n") + "\n")

            Log.i(TAG, "AppUsageByCategory -> $day categories=${perCatMs.size}")
            for ((k, v) in perCatMs.entries.sortedByDescending { it.value }) {
                Log.i(TAG, String.format(Locale.US, "  %s=%.1fm", k, v / 60000.0))
            }

            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "AppUsageByCategoryDailyWorker error", t)
            Result.retry()
        }
    }

    private fun readCategoryMinutesFromCsv(filesDir: File, day: String): Map<String, Double>? {
        val byApp = File(filesDir, "daily_app_usage_minutes_by_app.csv")
        val byCat = File(filesDir, "daily_app_usage_minutes.csv")

        // Try per-app: header "date,category,minutes,package" OR "date,category,minutes"
        if (byApp.exists() && byApp.length() > 0L) {
            try {
                val lines = byApp.readLines()
                if (lines.isNotEmpty() && lines[0].startsWith("date,category,minutes")) {
                    val sums = HashMap<String, Double>()
                    lines.drop(1).forEach { line ->
                        if (line.isBlank()) return@forEach
                        val cols = line.split(',')
                        if (cols.size < 3) return@forEach
                        val d = cols[0].trim()
                        if (d != day) return@forEach
                        val cat = cols[1].trim().ifEmpty { "other" }
                        val mins = cols[2].trim().toDoubleOrNull() ?: return@forEach
                        sums[cat] = (sums[cat] ?: 0.0) + mins
                    }
                    if (sums.isNotEmpty()) return sums
                }
            } catch (_: Throwable) { /* ignore */ }
        }

        // Try pre-aggregated per-category: header "date,category,minutes"
        if (byCat.exists() && byCat.length() > 0L) {
            try {
                val lines = byCat.readLines()
                if (lines.isNotEmpty() && lines[0].startsWith("date,category,minutes")) {
                    val sums = HashMap<String, Double>()
                    lines.drop(1).forEach { line ->
                        if (line.isBlank()) return@forEach
                        val cols = line.split(',')
                        if (cols.size < 3) return@forEach
                        val d = cols[0].trim()
                        if (d != day) return@forEach
                        val cat = cols[1].trim().ifEmpty { "other" }
                        val mins = cols[2].trim().toDoubleOrNull() ?: return@forEach
                        sums[cat] = (sums[cat] ?: 0.0) + mins
                    }
                    if (sums.isNotEmpty()) return sums
                }
            } catch (_: Throwable) { /* ignore */ }
        }

        return null
    }

    private fun ensureHeader(file: File, header: String) {
        if (!file.exists()) {
            file.writeText(header + "\n")
            return
        }
        val first = try { file.bufferedReader().use { it.readLine() } } catch (_: Throwable) { null }
        if (first == null || !first.startsWith("date,")) {
            val body = try { file.readLines().dropWhile { it.startsWith("date,") } } catch (_: Throwable) { emptyList() }
            file.writeText((sequenceOf(header) + body.asSequence()).joinToString("\n").trimEnd() + "\n")
        }
    }

    private fun shouldSkip(pkg: String): Boolean {
        val p = pkg.lowercase(Locale.US)
        if (p == "android") return true
        if (p == "com.nick.myrecoverytracker") return true
        if (p == "com.android.intentresolver") return true
        if (p == "com.google.android.apps.nexuslauncher") return true
        return false
    }

    private fun classify(pkg: String, ai: ApplicationInfo?): String {
        when (ai?.category ?: ApplicationInfo.CATEGORY_UNDEFINED) {
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> return "productivity"
            ApplicationInfo.CATEGORY_SOCIAL -> return "social"
            ApplicationInfo.CATEGORY_GAME -> return "game"
            ApplicationInfo.CATEGORY_IMAGE -> return "image"
            ApplicationInfo.CATEGORY_AUDIO -> return "music_and_audio"
            ApplicationInfo.CATEGORY_VIDEO -> return "video"
            ApplicationInfo.CATEGORY_NEWS -> return "news"
            ApplicationInfo.CATEGORY_MAPS -> return "maps"
        }

        val p = pkg.lowercase(Locale.US)

        if (p.startsWith("com.tinder") || p.startsWith("com.bumble") || p.startsWith("com.hinge") ||
            p.startsWith("com.okcupid") || p.startsWith("com.pof") || p.startsWith("com.match") ||
            p.startsWith("com.happn") || p.startsWith("com.grindr") || p.startsWith("com.scruff") ||
            p.startsWith("com.herapp") || p.startsWith("com.coffeemeetsbagel") || p.startsWith("com.feeld") ||
            p.startsWith("com.zoosk") || p.startsWith("com.badoo") || p.startsWith("com.hily") ||
            p.startsWith("com.rayalabs.raya")) return "dating"

        if (p.startsWith("com.whatsapp") || p.startsWith("com.instagram") || p.startsWith("com.facebook.katana") ||
            p.startsWith("com.facebook.orca") || p.startsWith("com.twitter.android") || p.startsWith("com.snapchat") ||
            p.startsWith("org.telegram") || p.startsWith("com.discord") || p.startsWith("com.tiktok") ||
            p.startsWith("com.google.android.gm") || p.startsWith("com.microsoft.office.outlook") ||
            p.startsWith("com.textra") || p.startsWith("com.google.android.dialer") ||
            p.startsWith("com.google.android.contacts"))
            return "social"

        if (p.startsWith("com.google.android.apps.docs") || p.startsWith("com.google.android.keep") ||
            p.startsWith("com.microsoft.office") || p.startsWith("com.google.android.calendar") ||
            p.startsWith("com.google.android.apps.tasks") ||
            p.startsWith("com.google.android.chrome") || p.startsWith("com.android.chrome") ||
            p.startsWith("org.mozilla.firefox") || p.startsWith("com.microsoft.emmx") ||
            p.startsWith("com.android.vending") || p.startsWith("com.google.android.gms") ||
            p.startsWith("com.google.android.deskclock") || p.startsWith("com.google.android.documentsui") ||
            p.startsWith("com.google.android.googlequicksearchbox") ||
            p.startsWith("org.chromium.webapk"))
            return "productivity"

        if (p.startsWith("com.google.android.youtube") || p.startsWith("com.netflix"))
            return "video"

        if (p.startsWith("com.spotify") || p.startsWith("com.google.android.apps.youtube.music") ||
            p.startsWith("au.com.shiftyjelly.pocketcasts"))
            return "music_and_audio"

        if (p.startsWith("com.amazon") || p.startsWith("com.ebay") ||
            p.startsWith("com.google.android.apps.wallet") || p.startsWith("com.google.android.apps.walletnfcrel") ||
            p.startsWith("com.google.android.apps.nbu.paisa.user"))
            return "shopping"

        if (p.startsWith("com.google.android.apps.maps"))
            return "maps"
        if (p.startsWith("com.ubercab") || p.startsWith("com.grab") || p.startsWith("com.lyft"))
            return "travel_and_local"

        if (p.startsWith("com.google.android.apps.magazines"))
            return "news"

        if (p.startsWith("com.strava"))
            return "health"

        if (p.startsWith("com.google.android.apps.photos") || p.startsWith("com.google.android.photopicker") ||
            p.startsWith("com.google.android.googlecamera") || p.startsWith("com.google.android.GoogleCamera"))
            return "image"

        return "other"
    }

    private fun dayBoundsMillis(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 24L * 60L * 60L * 1000L
        return Pair(start, end)
    }

    private fun today(): String = TS.format(Date())

    companion object {
        private const val TAG = "AppUsageByCategoryDaily"
        private val TS = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}