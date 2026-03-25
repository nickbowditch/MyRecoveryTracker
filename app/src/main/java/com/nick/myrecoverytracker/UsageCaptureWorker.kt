package com.nick.myrecoverytracker

import android.app.AppOpsManager
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
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.math.max
import kotlin.math.round

class UsageCaptureWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val zone = ZoneId.systemDefault()
    private val tsFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private val categories = listOf(
        "app_min_total","app_min_social","app_min_dating","app_min_productivity","app_min_music_audio",
        "app_min_image","app_min_maps","app_min_video","app_min_travel_local",
        "app_min_shopping","app_min_news","app_min_game","app_min_health",
        "app_min_finance","app_min_browser","app_min_comm","app_min_other"
    )

    private val excludedPkgs = setOf(
        "android","com.android.systemui",
        "androidx.work.impl.foreground",
        "com.nick.myrecoverytracker"
    )

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now(zone).toString()
            val now = System.currentTimeMillis()
            Log.i(TAG, "Starting UsageCaptureWorker for $today")

            // Always log to usage_capture_log.csv
            logCapture("START")

            if (!hasUsagePermission()) {
                logCapture("PERMISSION_DENIED")
                writeEmpty(today)
                Log.w(TAG, "Usage permission denied, wrote zeros")
                logCapture("END")
                return@withContext Result.success()
            }

            logCapture("PERMISSION_GRANTED")

            val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE)
                    as UsageStatsManager

            val start = LocalDate.now(zone)
                .atStartOfDay(zone).toInstant().toEpochMilli()
            val end = now

            logCapture("QUERYING_EVENTS")
            val events = usm.queryEvents(start, end)
            var lastPkg: String? = null
            var lastTs = 0L
            val e = UsageEvents.Event()
            val perPkgMillis = hashMapOf<String, Long>()
            var foundAny = false
            var eventCount = 0

            @Suppress("DEPRECATION")
            while (events.getNextEvent(e)) {
                foundAny = true
                eventCount++
                val pkg = e.packageName ?: continue

                @Suppress("DEPRECATION")
                when (e.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        if (lastPkg != null && lastTs > 0) {
                            val dur = max(0, e.timeStamp - lastTs)
                            perPkgMillis[lastPkg!!] =
                                (perPkgMillis[lastPkg!!] ?: 0L) + dur
                        }
                        lastPkg = pkg
                        lastTs = e.timeStamp
                    }

                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED,
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        if (lastPkg != null && lastTs > 0) {
                            val dur = max(0, e.timeStamp - lastTs)
                            perPkgMillis[lastPkg!!] =
                                (perPkgMillis[lastPkg!!] ?: 0L) + dur
                            lastPkg = null
                            lastTs = 0L
                        }
                    }
                }
            }

            logCapture("EVENT_COUNT:$eventCount")

            if (!foundAny) {
                logCapture("NO_EVENTS_FOUND")
                Log.w(TAG, "No usage events found for today")
            } else {
                logCapture("FOUND_EVENTS")
                logCapture("PKG_COUNT:${perPkgMillis.size}")
            }

            writeCategoryTotals(today, perPkgMillis)
            logCapture("WRITE_COMPLETE")
            logCapture("END")
            Log.i(TAG, "UsageCaptureWorker completed successfully")
            return@withContext Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "UsageCaptureWorker failed", t)
            logCapture("ERROR: ${t.message}")
            t.printStackTrace()
            return@withContext Result.retry()
        }
    }

    private fun hasUsagePermission(): Boolean {
        val appOps = applicationContext.getSystemService(Context.APP_OPS_SERVICE)
                as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            applicationContext.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun logCapture(tag: String) {
        try {
            val dataDir = StorageHelper.getDataDir(applicationContext)
            val f = File(dataDir, "usage_capture_log.csv")
            if (!f.exists()) {
                f.writeText("ts,tag\n")
            }
            f.appendText("${tsFmt.format(System.currentTimeMillis())},$tag\n")
            Log.d(TAG, "Logged capture: $tag")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to log capture", t)
            t.printStackTrace()
        }
    }

    private fun writeEmpty(day: String) {
        try {
            val f = File(StorageHelper.getDataDir(applicationContext), "daily_app_usage_minutes.csv")
            if (!f.exists()) {
                val headerRow = "record_id,participant_id,date,feature_schema_version," + categories.joinToString(",")
                f.writeText(headerRow + "\n")
                Log.d(TAG, "Created CSV header")
            }
            val participantId = ParticipantIdManager.getOrCreate(applicationContext)
            val recordId = "${participantId}_$day"
            val row = buildString {
                append("$recordId,$participantId,$day,$FEATURE_SCHEMA_VERSION")
                categories.forEach { append(",0.0") }
            }
            upsert(f, day, row)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to write empty", t)
        }
    }

    private fun writeCategoryTotals(day: String, perPkgMillis: Map<String, Long>) {
        try {
            val f = File(StorageHelper.getDataDir(applicationContext), "daily_app_usage_minutes.csv")
            if (!f.exists()) {
                val headerRow = "record_id,participant_id,date,feature_schema_version," + categories.joinToString(",")
                f.writeText(headerRow + "\n")
                Log.d(TAG, "Created CSV header")
            }

            val participantId = ParticipantIdManager.getOrCreate(applicationContext)
            val recordId = "${participantId}_$day"
            val buckets = categories.associateWith { 0.0 }.toMutableMap()

            fun add(cat: String, mins: Double) {
                buckets[cat] = (buckets[cat] ?: 0.0) + mins
            }

            for ((pkg, ms) in perPkgMillis) {
                if (ms <= 0 || pkg in excludedPkgs) continue
                add(mapPackage(pkg), ms / 60000.0)
            }

            val total = buckets.filterKeys { it != "app_min_total" }
                .values.sum()

            buckets["app_min_total"] = round(total * 100) / 100.0

            val row = buildString {
                append("$recordId,$participantId,$day,$FEATURE_SCHEMA_VERSION")
                categories.forEach {
                    append(",")
                    append(String.format(Locale.US, "%.2f", buckets[it] ?: 0.0))
                }
            }

            upsert(f, day, row)
            Log.i(TAG, "Wrote category totals for $day")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to write category totals", t)
        }
    }

    private fun upsert(f: File, day: String, row: String) {
        try {
            val lines = if (f.exists()) f.readLines().toMutableList() else mutableListOf()
            if (lines.isEmpty()) {
                f.writeText(row + "\n")
                return
            }
            var replaced = false
            for (i in 1 until lines.size) {
                // record_id is participantId_date; date is col index 2
                val cols = lines[i].split(",")
                val rowDate = if (cols.size > 2) cols[2].trim() else ""
                if (rowDate == day) {
                    lines[i] = row
                    replaced = true
                    break
                }
            }
            if (!replaced) lines.add(row)
            f.writeText(lines.joinToString("\n") + "\n")
        } catch (t: Throwable) {
            Log.e(TAG, "Upsert failed", t)
        }
    }

    private fun mapPackage(pkg: String): String {
        val p = pkg.lowercase(Locale.US)
        return when {
            p.contains("facebook") || p.contains("instagram") || p.contains("twitter") ||
                    p.contains("snapchat") || p.contains("tiktok") || p.contains("whatsapp") ||
                    p.contains("telegram") -> "app_min_social"

            p.contains("tinder") || p.contains("bumble") || p.contains("hinge") ->
                "app_min_dating"

            p.contains("docs") || p.contains("office") || p.contains("notion") ->
                "app_min_productivity"

            p.contains("spotify") || p.contains("podcast") ->
                "app_min_music_audio"

            p.contains("camera") || p.contains("gallery") || p.contains("photos") ->
                "app_min_image"

            p.contains("maps") || p.contains("uber") || p.contains("waze") ->
                "app_min_maps"

            p.contains("youtube") || p.contains("netflix") || p.contains("primevideo") ->
                "app_min_video"

            p.contains("amazon") || p.contains("shop") ->
                "app_min_shopping"

            p.contains("news") || p.contains("bbc") || p.contains("guardian") ->
                "app_min_news"

            p.contains("game") ->
                "app_min_game"

            p.contains("health") || p.contains("fit") || p.contains("strava") ->
                "app_min_health"

            p.contains("bank") || p.contains("paypal") ->
                "app_min_finance"

            p.contains("chrome") || p.contains("browser") ->
                "app_min_browser"

            p.contains("dialer") || p.contains("phone") ->
                "app_min_comm"

            else -> "app_min_other"
        }
    }

    companion object {
        private const val TAG = "UsageCaptureWorker"
        private const val FEATURE_SCHEMA_VERSION = "1"
    }
}