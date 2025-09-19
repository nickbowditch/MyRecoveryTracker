package com.nick.myrecoverytracker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppUsageCategoryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val TAG = "AppUsageCategoryWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            val now = System.currentTimeMillis()
            val day = dayString(now)
            val (startOfDay, endOfDay) = dayBounds(now)

            val usm = applicationContext.getSystemService(UsageStatsManager::class.java)
            val stats: List<UsageStats> =
                usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, endOfDay)
                    ?: emptyList()

            val pkgToCat = loadCategoryMap(applicationContext)

            val buckets = mutableMapOf(
                "recovery" to 0f,
                "social" to 0f,
                "entertainment" to 0f,
                "dating" to 0f,
                "other" to 0f
            )

            for (s in stats) {
                val pkg = s.packageName ?: continue
                val mins = (s.totalTimeInForeground / 60000f)
                if (mins <= 0f) continue

                val cat = pkgToCat[pkg.lowercase(Locale.ROOT)] ?: "other"
                buckets[cat] = buckets.getValue(cat) + mins
            }

            writeCategoryDaily(day, buckets)

            Result.success()
        } catch (t: Throwable) {
            android.util.Log.e(TAG, "Failed: ${t.message}", t)
            Result.retry()
        }
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

    private fun loadCategoryMap(ctx: Context): Map<String, String> {
        val f = File(ctx.filesDir, "app_categories.json")
        val text: String? = try {
            if (f.exists()) f.readText() else null
        } catch (_: Throwable) { null }

        val json = text ?: defaultCategoriesJson()
        val map = mutableMapOf<String, String>()

        try {
            val obj = JSONObject(json)

            fun addAll(cat: String) {
                if (!obj.has(cat)) return
                val arr = obj.getJSONArray(cat)
                for (i in 0 until arr.length()) {
                    val pkg = arr.optString(i)?.trim()?.lowercase(Locale.ROOT)
                    if (!pkg.isNullOrEmpty()) map[pkg] = cat
                }
            }

            addAll("recovery")
            addAll("social")
            addAll("entertainment")
            addAll("dating")

            val extras = listOf(
                "com.facebook.orca" to "social",
                "com.facebook.lite" to "social",
                "com.whatsapp.w4b" to "social"
            )
            for ((pkg, cat) in extras) map.putIfAbsent(pkg, cat)
        } catch (_: Throwable) {
            return emptyMap()
        }

        return map
    }

    private fun defaultCategoriesJson(): String = """
        {
          "recovery": [
            "au.org.aa.meetings",
            "com.sobergrid",
            "com.aa.bigbook",
            "com.ias.recoverybox",
            "com.addicaid.app",
            "org.intherooms.intherooms"
          ],
          "social": [
            "com.whatsapp",
            "com.facebook.katana",
            "com.instagram.android",
            "com.snapchat.android",
            "org.telegram.messenger",
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.discord"
          ],
          "entertainment": [
            "com.google.android.youtube",
            "com.netflix.mediaclient",
            "com.spotify.music",
            "com.amazon.avod.thirdpartyclient",
            "com.tiktok.android"
          ],
          "dating": [
            "com.tinder",
            "com.bumble.app",
            "co.hinge.app",
            "com.grindrapp.android",
            "com.okcupid.okcupid",
            "com.pof.android",
            "com.ftw_and_co.happn",
            "com.match.android",
            "com.badoo.mobile"
          ]
        }
    """.trimIndent()

    private fun writeCategoryDaily(day: String, buckets: Map<String, Float>) {
        val file = File(applicationContext.filesDir, "app_category_daily.csv")
        val sb = StringBuilder()
        for (cat in listOf("recovery", "social", "entertainment", "dating", "other")) {
            val mins = buckets[cat] ?: 0f
            sb.append("$day,$cat,${"%.1f".format(mins)}\n")
        }
        file.appendText(sb.toString())
    }
}