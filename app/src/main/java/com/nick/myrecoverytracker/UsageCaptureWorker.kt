// app/src/main/java/com/nick/myrecoverytracker/UsageCaptureWorker.kt
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
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.round

class UsageCaptureWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val tsFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    // Expanded categories (stable order)
    private val categories = listOf(
        "app_min_social",
        "app_min_dating",
        "app_min_productivity",
        "app_min_music_audio",
        "app_min_image",
        "app_min_maps",
        "app_min_video",
        "app_min_travel_local",
        "app_min_shopping",
        "app_min_news",
        "app_min_game",
        "app_min_health",
        "app_min_finance",
        "app_min_browser",
        "app_min_comm",
        "app_min_other",
        "app_min_total"
    )

    // Exclude pure system/noise
    private val excludedPkgs = setOf(
        "android",
        "com.android.systemui",
        "androidx.work.impl.foreground",
        "com.google.android.odad",
        "com.nick.myrecoverytracker"
    )

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val now = ZonedDateTime.now(zone)
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = now.toInstant().toEpochMilli()

        val ev = usm.queryEvents(start, end)
        if (!ev.hasNextEvent()) {
            upsertDailyCount("daily_usage_events.csv", LocalDate.now(zone).toString(), 0)
            upsertDailySwitching(emptyMap())
            upsertDailyMinutes(emptyMap())
            return@withContext Result.success()
        }

        ensureHeader("usage_events.csv", "timestamp,package,event")

        var lastFgPkg: String? = null
        var lastFgStart: Long = 0L
        val perPkgMillis = hashMapOf<String, Long>()
        val perPkgStarts = hashMapOf<String, Int>()
        var totalEvents = 0

        val e = UsageEvents.Event()
        val lines = StringBuilder()

        while (ev.getNextEvent(e)) {
            totalEvents++
            val tsLocal = tsFmt.format(e.timeStamp)
            val pkg = e.packageName ?: ""
            val typeStr = when (e.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> "FOREGROUND"
                UsageEvents.Event.MOVE_TO_BACKGROUND -> "BACKGROUND"
                UsageEvents.Event.ACTIVITY_RESUMED -> "ACTIVITY_RESUMED"
                UsageEvents.Event.ACTIVITY_PAUSED -> "ACTIVITY_PAUSED"
                UsageEvents.Event.ACTIVITY_STOPPED -> "ACTIVITY_STOPPED"
                UsageEvents.Event.SCREEN_INTERACTIVE -> "SCREEN_INTERACTIVE"
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> "SCREEN_NON_INTERACTIVE"
                UsageEvents.Event.SHORTCUT_INVOCATION -> "SHORTCUT_INVOCATION"
                12 -> "NOTIFICATION_INTERRUPTION"
                else -> "EVENT_" + e.eventType
            }
            lines.append(tsLocal).append(',').append(pkg).append(',').append(typeStr).append('\n')

            when (e.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (pkg.isNotEmpty()) {
                        perPkgStarts[pkg] = (perPkgStarts[pkg] ?: 0) + 1
                        if (lastFgPkg != null && lastFgStart > 0) {
                            val dur = max(0, e.timeStamp - lastFgStart)
                            perPkgMillis[lastFgPkg!!] = (perPkgMillis[lastFgPkg!!] ?: 0L) + dur
                        }
                        lastFgPkg = pkg
                        lastFgStart = e.timeStamp
                    }
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND,
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (lastFgPkg != null && lastFgStart > 0) {
                        val dur = max(0, e.timeStamp - lastFgStart)
                        perPkgMillis[lastFgPkg!!] = (perPkgMillis[lastFgPkg!!] ?: 0L) + dur
                        lastFgPkg = null
                        lastFgStart = 0L
                    }
                }
            }
        }

        if (lastFgPkg != null && lastFgStart > 0) {
            val dur = max(0, end - lastFgStart)
            perPkgMillis[lastFgPkg!!] = (perPkgMillis[lastFgPkg!!] ?: 0L) + dur
        }

        appendBulk("usage_events.csv", lines.toString())

        upsertDailyCount("daily_usage_events.csv", LocalDate.now(zone).toString(), totalEvents)
        upsertDailySwitching(perPkgStarts)
        upsertDailyMinutes(perPkgMillis)

        Result.success()
    }

    private fun ensureHeader(name: String, header: String) {
        val f = File(applicationContext.filesDir, name)
        if (!f.exists() || f.length() == 0L) {
            FileOutputStream(f, false).use { it.write((header + "\n").toByteArray()) }
        }
    }

    private fun ensureHeaderExact(name: String, expectedHeader: String) {
        val f = File(applicationContext.filesDir, name)
        if (!f.exists()) {
            FileOutputStream(f, false).use { it.write((expectedHeader + "\n").toByteArray()) }
            return
        }
        val current = f.bufferedReader().use { it.readLine() }?.trim() ?: ""
        if (current == expectedHeader) return
        val backup = File(applicationContext.filesDir, "$name.legacy")
        runCatching { f.copyTo(backup, overwrite = true) }
        FileOutputStream(f, false).use { it.write((expectedHeader + "\n").toByteArray()) }
    }

    private fun appendBulk(name: String, text: String) {
        if (text.isEmpty()) return
        val f = File(applicationContext.filesDir, name)
        FileOutputStream(f, true).use { it.write(text.toByteArray()) }
    }

    private fun upsertDailyCount(name: String, day: String, count: Int) {
        ensureHeader(name, "date,count")
        upsertByFirstColumn(name, day, "$day,$count")
    }

    private fun upsertDailySwitching(starts: Map<String, Int>) {
        val name = "daily_app_switching.csv"
        ensureHeader(name, "date,package,starts")
        val day = LocalDate.now(zone).toString()
        val all = readLines(name).filterNot { it.startsWith(day) && it != "date,package,starts" }
        val newRows = starts.entries.sortedByDescending { it.value }.map { (pkg, n) ->
            "$day,$pkg,$n"
        }
        writeAll(name, (listOf("date,package,starts") + all.drop(1) + newRows).joinToString("\n") + "\n")
    }

    private fun upsertDailyMinutes(perPkgMillis: Map<String, Long>) {
        val name = "daily_app_usage_minutes.csv"
        val header = "date," + categories.joinToString(",")
        ensureHeaderExact(name, header)
        val day = LocalDate.now(zone).toString()

        val perCategoryRaw = mutableMapOf<String, Double>()
        categories.forEach { if (it != "app_min_total") perCategoryRaw[it] = 0.0 }

        val otherAggRaw = linkedMapOf<String, Double>()

        for ((rawPkg, millis) in perPkgMillis) {
            if (millis <= 0L) continue
            val pkg = rawPkg.trim()
            if (pkg.isEmpty() || pkg in excludedPkgs) continue

            val cat = mapPackageToCategory(pkg)
            val minutes = millis / 60000.0
            if (cat == "app_min_other") {
                otherAggRaw[pkg] = (otherAggRaw[pkg] ?: 0.0) + minutes
            } else {
                perCategoryRaw[cat] = (perCategoryRaw[cat] ?: 0.0) + minutes
            }
        }

        fun round2(v: Double) = round(v * 100.0) / 100.0

        val perCategoryRounded = mutableMapOf<String, Double>()
        for ((k, v) in perCategoryRaw) {
            if (k != "app_min_total" && k != "app_min_other") {
                perCategoryRounded[k] = round2(v)
            }
        }

        val otherAggRounded = linkedMapOf<String, Double>()
        for ((pkg, v) in otherAggRaw) {
            otherAggRounded[pkg] = round2(v)
        }
        val otherMinutesRounded = otherAggRounded.values.fold(0.0) { acc, d -> round2(acc + d) }
        perCategoryRounded["app_min_other"] = otherMinutesRounded

        val totalRounded = categories.filter { it != "app_min_total" }
            .map { perCategoryRounded[it] ?: 0.0 }
            .fold(0.0) { acc, d -> round2(acc + d) }
        perCategoryRounded["app_min_total"] = totalRounded

        val row = buildString {
            append(day)
            categories.forEach { c ->
                append(",")
                append(String.format(Locale.US, "%.2f", perCategoryRounded[c] ?: 0.0))
            }
        }
        upsertByFirstColumn(name, day, row)

        writeOtherBreakdown(day, otherAggRounded)
    }

    private fun writeOtherBreakdown(day: String, otherAgg: Map<String, Double>) {
        val content = buildString {
            appendLine("date,package,minutes")
            otherAgg.entries
                .sortedByDescending { it.value }
                .forEach { (pkg, mins) ->
                    appendLine("$day,$pkg,${"%.2f".format(Locale.US, mins)}")
                }
        }
        val dir = applicationContext.filesDir
        val tmp = File(dir, "daily_app_usage_other.csv.tmp")
        FileOutputStream(tmp, false).use { it.write(content.toByteArray()) }
        val f = File(dir, "daily_app_usage_other.csv")
        if (f.exists()) f.delete()
        tmp.renameTo(f)
    }

    private fun mapPackageToCategory(pkg: String): String {
        val p = pkg.lowercase(Locale.US)
        if (p == "com.android.chrome" || p.startsWith("org.chromium.webapk")) return "app_min_browser"
        if (p == "com.google.android.gm") return "app_min_productivity"
        if (p == "com.google.android.apps.nexuslauncher") return "app_min_productivity"
        if (p == "com.google.android.deskclock") return "app_min_productivity"
        if (p == "com.google.android.dialer" || p == "com.textra") return "app_min_comm"

        return when {
            p.contains("textra") || p.contains("dialer") || p.contains("messaging") || p.contains("contacts") || p.contains("phone") ->
                "app_min_comm"
            p.contains("facebook") || p.contains("instagram") || p.contains("twitter") ||
                    p.contains("snapchat") || p.contains("tiktok") || p.contains("telegram") || p.contains("whatsapp") ->
                "app_min_social"
            p.contains("tinder") || p.contains("bumble") || p.contains("hinge") || p.contains("grindr") ->
                "app_min_dating"
            p.contains("docs") || p.contains("sheets") || p.contains("slides") || p.contains("office") ||
                    p.contains("notion") || p.contains("keep") || p.contains("calendar") || p.contains("gmail") || p.contains("gm") ->
                "app_min_productivity"
            p.contains("spotify") || p.contains("music") || p.contains("podcast") || p.contains("soundcloud") || p.contains("pocketcasts") ->
                "app_min_music_audio"
            p.contains("camera") || p.contains("gallery") || p.contains("photos") ->
                "app_min_image"
            p.contains("maps") || p.contains("waze") || p.contains("map") ||
                    p.contains("uber") || p.contains("lyft") || p.contains("airbnb") ||
                    p.contains("booking") || p.contains("trip") || p.contains("transit") ->
                "app_min_travel_local"
            p.contains("youtube") || p.contains("netflix") || p.contains("primevideo") || p.contains("disney") ||
                    p.contains("stan") || p.contains("twitch") || p.contains("iplayer") ->
                "app_min_video"
            p.contains("amazon") || p.contains("ebay") || p.contains("shop") || p.contains("shopping") || p.contains("etsy") ->
                "app_min_shopping"
            p.contains("news") || p.contains("bbc") || p.contains("cnn") || p.contains("guardian") || p.contains("nyt") ->
                "app_min_news"
            p.contains("game") || p.contains("playgames") || p.contains("supercell") || p.contains("riot") ->
                "app_min_game"
            p.contains("fit") || p.contains("health") || p.contains("run") || p.contains("strava") ->
                "app_min_health"
            p.contains("bank") || p.contains("paypal") || p.contains("finance") || p.contains("revolut") || p.contains("wise") ->
                "app_min_finance"
            p.contains("chrome") || p.contains("browser") || p.startsWith("org.chromium") ->
                "app_min_browser"
            else -> "app_min_other"
        }
    }

    private fun upsertByFirstColumn(name: String, key: String, line: String) {
        val lines = readLines(name).toMutableList()
        if (lines.isEmpty()) return
        val header = lines.first()
        var replaced = false
        for (i in 1 until lines.size) {
            val d = lines[i].substringBefore(',')
            if (d == key) {
                lines[i] = line
                replaced = true
                break
            }
        }
        if (!replaced) lines.add(line)
        writeAll(name, (listOf(header) + lines.drop(1)).joinToString("\n") + "\n")
    }

    private fun readLines(name: String): List<String> {
        val f = File(applicationContext.filesDir, name)
        if (!f.exists()) return listOf()
        return f.readLines(Charsets.UTF_8).map { it.trimEnd('\n', '\r') }
    }

    private fun writeAll(name: String, content: String) {
        val f = File(applicationContext.filesDir, name)
        FileOutputStream(f, false).use { it.write(content.toByteArray()) }
    }
}