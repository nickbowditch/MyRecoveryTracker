// app/src/main/java/com/nick/myrecoverytracker/ScreenOnDailyWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ScreenOnDailyWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ctx = applicationContext
            val log = File(ctx.filesDir, "screen_log.csv")
            ensureHeader(File(ctx.filesDir, OUT))
            if (!log.exists()) {
                appendOrReplaceToday(0, 0, 0, 0)
                return@withContext Result.success()
            }

            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply { timeInMillis = now }
            val start = dayStartMillis(cal)
            val end = start + DAY

            val events = parseEvents(log)

            var onAtStart = false
            run {
                var lastBefore: Pair<Long, Boolean>? = null
                for ((ts, stateOn) in events) {
                    if (ts <= start) lastBefore = ts to stateOn else break
                }
                onAtStart = lastBefore?.second ?: false
            }

            var cursor = start
            var isOn = onAtStart
            var idx = events.indexOfFirst { it.first >= start }
            if (idx < 0) idx = events.size

            var earlyMs = 0L
            var diurnalMs = 0L
            var lateMs = 0L

            while (cursor < end) {
                val nextTs = if (idx < events.size) min(events[idx].first, end) else end
                if (isOn && nextTs > cursor) {
                    val spanStart = cursor
                    val spanEnd = nextTs
                    earlyMs   += overlap(spanStart, spanEnd, start + H06)
                    diurnalMs += overlap(max(spanStart, start + H06), spanEnd, start + H22)
                    lateMs    += overlap(max(spanStart, start + H22), spanEnd, end)
                }
                if (idx < events.size && events[idx].first == nextTs) {
                    isOn = events[idx].second
                    idx++
                }
                cursor = nextTs
            }

            val earlyMin = (earlyMs / 60000L).toInt()
            val diurnalMin = (diurnalMs / 60000L).toInt()
            val lateMin = (lateMs / 60000L).toInt()
            val total = earlyMin + diurnalMin + lateMin

            appendOrReplaceToday(earlyMin, diurnalMin, lateMin, total)
            Log.i(TAG, "ScreenOnDaily: early=$earlyMin diurnal=$diurnalMin late=$lateMin total=$total")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "ScreenOnDailyWorker failed", t)
            Result.failure()
        }
    }

    private fun dayStartMillis(calNow: Calendar): Long = Calendar.getInstance().apply {
        timeInMillis = calNow.timeInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun overlap(start: Long, end: Long, winEnd: Long): Long {
        val dayStart = start - (start % DAY)
        val winStart = when (winEnd - dayStart) {
            H06 -> dayStart
            H22 -> dayStart + H06
            DAY -> dayStart + H22
            else -> dayStart
        }
        val a = max(start, winStart)
        val b = min(end, winEnd)
        return max(0L, b - a)
    }

    private fun parseEvents(file: File): MutableList<Pair<Long, Boolean>> {
        val list = mutableListOf<Pair<Long, Boolean>>()
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        file.forEachLine { raw ->
            val p = raw.trim()
            if (p.isEmpty()) return@forEachLine
            val idx = p.lastIndexOf(',')
            if (idx <= 0 || idx == p.lastIndex) return@forEachLine
            val tsStr = p.substring(0, idx)
            val stateStr = p.substring(idx + 1).uppercase(Locale.US)
            val ts = try { df.parse(tsStr)?.time } catch (_: Throwable) { null } ?: return@forEachLine
            val on = when (stateStr) {
                "ON" -> true
                "OFF", "SCREEN_OFF" -> false
                else -> return@forEachLine
            }
            list.add(ts to on)
        }
        list.sortBy { it.first }
        return list
    }

    private fun appendOrReplaceToday(early: Int, diurnal: Int, late: Int, total: Int) {
        val out = File(applicationContext.filesDir, OUT)
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val existing = if (out.exists()) out.readLines() else emptyList()
        val header = existing.firstOrNull().takeUnless { it.isNullOrBlank() } ?: HEADER
        val rest = existing.drop(if (existing.isNotEmpty()) 1 else 0)
        val filtered = rest.filterNot { it.startsWith("$day,") }.toMutableList()
        filtered.add("$day,$early,$diurnal,$late,$total")
        out.writeText(header + "\n" + filtered.joinToString("\n") + "\n")
    }

    private fun ensureHeader(out: File) {
        if (!out.exists() || out.length() == 0L) {
            out.parentFile?.mkdirs()
            out.writeText("$HEADER\n")
        }
    }

    companion object {
        private const val TAG = "ScreenOnDailyWorker"
        private const val OUT = "daily_screen_distribution.csv"

        private const val DAY = 24L * 60 * 60 * 1000
        private const val H06 = 6L * 60 * 60 * 1000
        private const val H22 = 22L * 60 * 60 * 1000

        private const val HEADER = "date,early_minutes,diurnal_minutes,late_minutes,total_minutes"
    }
}