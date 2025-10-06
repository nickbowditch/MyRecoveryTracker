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

/**
 * UsageCaptureWorker
 *
 * Output:
 *   files/usage_events.csv -> date,time,event_type,package
 *   files/daily_app_starts_by_package.csv -> date,package,starts
 *   files/daily_app_usage_minutes.csv -> date,<category mins>
 *
 * This worker only captures usage events and daily summaries.
 * It no longer writes or touches daily_app_switching.csv.
 */
class UsageCaptureWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val tsFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    private val categories = listOf(
        "app_min_social", "app_min_dating", "app_min_productivity", "app_min_music_audio",
        "app_min_image", "app_min_maps", "app_min_video", "app_min_travel_local",
        "app_min_shopping", "app_min_news", "app_min_game", "app_min_health",
        "app_min_finance", "app_min_browser", "app_min_comm", "app_min_other", "app_min_total"
    )

    private val excludedPkgs = setOf(
        "android", "com.android.systemui", "androidx.work.impl.foreground",
        "com.google.android.odad", "com.nick.myrecoverytracker"
    )

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = ZonedDateTime.now(zone)
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = now.toInstant().toEpochMilli()
        val ev = usm.queryEvents(start, end)

        if (!ev.hasNextEvent()) {
            upsertDailyCount("daily_usage_events.csv", LocalDate.now(zone).toString(), 0)
            upsertDailyStartsByPackage(emptyMap())
            upsertDailyMinutes(emptyMap())
            return@withContext Result.success()
        }

        // GOLDEN: usage_events.csv header = date,time,event_type,package
        ensureHeader("usage_events.csv", "date,time,event_type,package")

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
            val spaceIdx = tsLocal.indexOf(' ')
            val dateStr = if (spaceIdx > 0) tsLocal.substring(0, spaceIdx) else tsLocal
            val timeStr = if (spaceIdx > 0) tsLocal.substring(spaceIdx + 1) else ""
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
                else -> "EVENT_${e.eventType}"
            }

            lines.append(dateStr).append(',')
                .append(timeStr).append(',')
                .append(typeStr).append(',')
                .append(pkg).append('\n')

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
        upsertDailyStartsByPackage(perPkgStarts)
        upsertDailyMinutes(perPkgMillis)
        Result.success()
    }

    private fun ensureHeader(name: String, header: String) {
        val f = File(applicationContext.filesDir, name)
        if (!f.exists() || f.length() == 0L)
            FileOutputStream(f, false).use { it.write((header + "\n").toByteArray()) }
    }

    private fun ensureHeaderExact(name: String, header: String) {
        val f = File(applicationContext.filesDir, name)
        if (!f.exists()) {
            FileOutputStream(f, false).use { it.write((header + "\n").toByteArray()) }
            return
        }
        val current = f.bufferedReader().use { it.readLine() }?.trim() ?: ""
        if (current != header) {
            val backup = File(applicationContext.filesDir, "$name.legacy")
            runCatching { f.copyTo(backup, overwrite = true) }
            FileOutputStream(f, false).use { it.write((header + "\n").toByteArray()) }
        }
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

    private fun upsertDailyStartsByPackage(starts: Map<String, Int>) {
        val name = "daily_app_starts_by_package.csv"
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

        val perCategory = mutableMapOf<String, Double>().apply {
            categories.forEach { if (it != "app_min_total") put(it, 0.0) }
        }

        val otherAgg = linkedMapOf<String, Double>()
        for ((pkg, millis) in perPkgMillis) {
            if (millis <= 0L || pkg.isBlank() || pkg in excludedPkgs) continue
            val cat = mapPackageToCategory(pkg)
            val mins = millis / 60000.0
            if (cat == "app_min_other") otherAgg[pkg] = (otherAgg[pkg] ?: 0.0) + mins
            else perCategory[cat] = (perCategory[cat] ?: 0.0) + mins
        }

        fun r2(v: Double) = round(v * 100.0) / 100.0
        val otherTotal = otherAgg.values.sum().let(::r2)
        perCategory["app_min_other"] = otherTotal
        perCategory["app_min_total"] = categories.filter { it != "app_min_total" }
            .sumOf { perCategory[it] ?: 0.0 }.let(::r2)

        val row = buildString {
            append(day)
            categories.forEach { append(",").append(String.format(Locale.US, "%.2f", perCategory[it] ?: 0.0)) }
        }
        upsertByFirstColumn(name, day, row)
        writeOtherBreakdown(day, otherAgg)
    }

    private fun writeOtherBreakdown(day: String, other: Map<String, Double>) {
        val tmp = File(applicationContext.filesDir, "daily_app_usage_other.csv.tmp")
        val out = File(applicationContext.filesDir, "daily_app_usage_other.csv")
        val content = buildString {
            appendLine("date,package,minutes")
            other.entries.sortedByDescending { it.value }
                .forEach { (pkg, mins) ->
                    appendLine("$day,$pkg,${"%.2f".format(Locale.US, mins)}")
                }
        }
        FileOutputStream(tmp, false).use { it.write(content.toByteArray()) }
        if (out.exists()) out.delete()
        tmp.renameTo(out)
    }

    private fun mapPackageToCategory(pkg: String): String {
        val p = pkg.lowercase(Locale.US)
        return when {
            p.contains("facebook") || p.contains("instagram") || p.contains("twitter") ||
                    p.contains("snapchat") || p.contains("tiktok") || p.contains("telegram") || p.contains("whatsapp") -> "app_min_social"
            p.contains("tinder") || p.contains("bumble") || p.contains("hinge") || p.contains("grindr") -> "app_min_dating"
            p.contains("docs") || p.contains("sheets") || p.contains("slides") || p.contains("office") ||
                    p.contains("notion") || p.contains("keep") || p.contains("calendar") || p.contains("gmail") -> "app_min_productivity"
            p.contains("spotify") || p.contains("music") || p.contains("podcast") || p.contains("soundcloud") -> "app_min_music_audio"
            p.contains("camera") || p.contains("gallery") || p.contains("photos") -> "app_min_image"
            p.contains("maps") || p.contains("waze") || p.contains("uber") || p.contains("booking") -> "app_min_travel_local"
            p.contains("youtube") || p.contains("netflix") || p.contains("primevideo") || p.contains("disney") -> "app_min_video"
            p.contains("amazon") || p.contains("ebay") || p.contains("shop") -> "app_min_shopping"
            p.contains("news") || p.contains("bbc") || p.contains("cnn") || p.contains("guardian") -> "app_min_news"
            p.contains("game") || p.contains("playgames") || p.contains("supercell") -> "app_min_game"
            p.contains("fit") || p.contains("health") || p.contains("strava") -> "app_min_health"
            p.contains("bank") || p.contains("paypal") || p.contains("finance") -> "app_min_finance"
            p.contains("chrome") || p.contains("browser") -> "app_min_browser"
            p.contains("textra") || p.contains("dialer") || p.contains("phone") -> "app_min_comm"
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
        return if (!f.exists()) listOf() else f.readLines(Charsets.UTF_8).map { it.trimEnd('\r', '\n') }
    }

    private fun writeAll(name: String, content: String) {
        val f = File(applicationContext.filesDir, name)
        FileOutputStream(f, false).use { it.write(content.toByteArray()) }
    }
}