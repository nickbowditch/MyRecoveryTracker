package com.nick.myrecoverytracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln

class AppUsageWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val ctx = applicationContext

        // 0) Permission sanity
        if (!UsagePermissionHelper.isGranted(ctx)) {
            Log.w(TAG, "❌ Usage access not granted")
            return Result.success()
        }

        // 1) Time window (last 24h)
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24L * 60 * 60 * 1000
        val day = dayStamp(endTime)

        // 2) Query usage
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStats: List<UsageStats> = try {
            usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime) ?: emptyList()
        } catch (t: Throwable) {
            Log.e(TAG, "queryUsageStats failed", t)
            emptyList()
        }

        if (usageStats.isEmpty()) {
            Log.w(TAG, "⚠️ No usage data (permission not granted or no activity).")
            // Still write a row of zeros so the day is accounted for
            writeDailyCategories(ctx, day, 0.0, 0.0, 0.0, 0.0, 0.0)
            return Result.success()
        }

        // 3) Aggregate total foreground millis per package
        val appTimeMs: Map<String, Long> = usageStats
            .groupBy { it.packageName }
            .mapValues { (_, list) -> list.sumOf { it.totalTimeInForeground.coerceAtLeast(0L) } }
            .filterValues { it > 0L }

        // 4) Load categories from files/app_categories.json
        val cats = loadCategories(ctx)

        // 5) Sum minutes into categories
        var totalMin = 0.0
        var recMin = 0.0
        var socMin = 0.0
        var entMin = 0.0
        var othMin = 0.0

        for ((pkg, ms) in appTimeMs) {
            val min = ms / 60000.0
            totalMin += min
            when {
                pkg in cats.recovery -> recMin += min
                pkg in cats.social -> socMin += min
                pkg in cats.entertainment -> entMin += min
                else -> othMin += min
            }
        }

        // 6) Entropy (kept for your existing metric)
        val entropy = computeEntropy(appTimeMs.values)
        MetricsStore.saveAppUsageEntropy(ctx, entropy)

        Log.i(TAG, "📊 ${day}: total=${fmt(totalMin)}m rec=${fmt(recMin)}m soc=${fmt(socMin)}m ent=${fmt(entMin)}m oth=${fmt(othMin)}m; entropy=$entropy")

        // 7) Persist daily rollup
        writeDailyCategories(ctx, day, totalMin, recMin, socMin, entMin, othMin)

        return Result.success()
    }

    // ---- files / JSON ----

    private data class Categories(
        val recovery: Set<String>,
        val social: Set<String>,
        val entertainment: Set<String>
    )

    private fun loadCategories(ctx: Context): Categories {
        val f = File(ctx.filesDir, "app_categories.json")
        if (!f.exists()) {
            Log.w(TAG, "app_categories.json missing; using empty sets")
            return Categories(emptySet(), defaultSocial, defaultEntertainment)
        }
        return try {
            val obj = JSONObject(f.readText())
            Categories(
                recovery = obj.optJSONArray("recovery")?.toSet() ?: emptySet(),
                social = obj.optJSONArray("social")?.toSet() ?: defaultSocial,
                entertainment = obj.optJSONArray("entertainment")?.toSet() ?: defaultEntertainment
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to parse app_categories.json; using defaults", t)
            Categories(emptySet(), defaultSocial, defaultEntertainment)
        }
    }

    private fun writeDailyCategories(
        ctx: Context,
        day: String,
        totalMin: Double,
        recMin: Double,
        socMin: Double,
        entMin: Double,
        othMin: Double
    ) {
        val out = File(ctx.filesDir, "daily_app_usage_minutes.csv")
        val header = "date,total_min,recovery_min,social_min,entertainment_min,other_min"
        val row = listOf(day, fmt(totalMin), fmt(recMin), fmt(socMin), fmt(entMin), fmt(othMin)).joinToString(",")
        val lines = if (out.exists()) out.readLines().filterNot { it.startsWith("$day,") }.toMutableList() else mutableListOf()
        if (lines.isEmpty() || lines.first() != header) lines.add(0, header)
        lines += row
        out.writeText(lines.joinToString("\n") + "\n")
    }

    // ---- utils ----

    private fun dayStamp(ts: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(ts))

    private fun computeEntropy(usages: Collection<Long>): Double {
        val total = usages.sum()
        if (total == 0L) return 0.0
        var h = 0.0
        for (t in usages) {
            val p = t.toDouble() / total.toDouble()
            h += -p * ln(p)
        }
        return h
    }

    private fun fmt(v: Double): String = String.format(Locale.US, "%.3f", v)

    // Safe JSONArray → Set<String>
    private fun org.json.JSONArray.toSet(): Set<String> {
        val s = HashSet<String>(length())
        for (i in 0 until length()) {
            val v = optString(i, null)
            if (!v.isNullOrBlank()) s += v
        }
        return s
    }

    companion object {
        private const val TAG = "AppUsageWorker"

        // Reasonable defaults so it "just works" even if file is missing
        private val defaultSocial = setOf(
            "com.whatsapp", "com.facebook.katana", "com.instagram.android",
            "com.snapchat.android", "org.telegram.messenger", "com.twitter.android",
            "com.reddit.frontpage", "com.discord"
        )
        private val defaultEntertainment = setOf(
            "com.google.android.youtube", "com.netflix.mediaclient", "com.spotify.music",
            "com.amazon.avod.thirdpartyclient", "com.tiktok.android"
        )
    }
}