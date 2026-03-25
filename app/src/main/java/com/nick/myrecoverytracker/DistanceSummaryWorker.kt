// DistanceSummaryWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DistanceSummaryWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dir = StorageHelper.getDataDir(applicationContext)
            if (!dir.exists()) dir.mkdirs()

            val locFile = File(dir, "location_log.csv")
            val outFile = File(dir, "daily_distance_log.csv")
            val hbFile  = File(dir, "heartbeat.csv")
            val header  = "record_id,participant_id,date,feature_schema_version,event,distance_m,data_quality\n"

            val today       = dayString(0)
            val yesterday   = dayString(-1)
            val participantId = ParticipantIdManager.getOrCreate(applicationContext)

            Log.i(TAG, "Starting DistanceSummaryWorker for today=$today yesterday=$yesterday")

            if (!locFile.exists()) {
                Log.w(TAG, "location_log.csv missing — checking heartbeat")
                val todayService     = wasLocationServiceRunningOnDay(hbFile, today)
                val yesterdayService = wasLocationServiceRunningOnDay(hbFile, yesterday)

                // Service ran but log is gone → missing_log (not a true zero)
                // Service never ran              → SENSOR_FAILURE
                val todayVal     = if (todayService)     "missing_log" else "SENSOR_FAILURE"
                val yesterdayVal = if (yesterdayService) "missing_log" else "SENSOR_FAILURE"

                applyUpdates(outFile, header, participantId, listOf(yesterday to yesterdayVal, today to todayVal))
                Log.w(TAG, "location_log.csv missing; service_today=$todayService service_yesterday=$yesterdayService")
                return@withContext Result.success()
            }

            val days    = listOf(yesterday, today)
            val updates = days.map { d -> d to computeKmForDay(locFile, d) }

            applyUpdates(outFile, header, participantId, updates)
            updates.forEach { (d, km) ->
                val label = when (km) {
                    SENTINEL_FAILURE -> "SENSOR_FAILURE"
                    SENTINEL_SPARSE  -> "sparse_log (0.00)"
                    else             -> "${"%.2f".format(km)} km"
                }
                Log.i(TAG, "Distance($d) = $label")
            }

            Log.i(TAG, "DistanceSummaryWorker completed successfully")
            return@withContext Result.success()

        } catch (t: Throwable) {
            Log.e(TAG, "DistanceSummaryWorker error", t)
            return@withContext Result.retry()
        }
    }

    // Returns:
    //   >= 0f          → valid km
    //   SENTINEL_SPARSE  (-2f) → log exists but < 2 GPS points
    //   SENTINEL_FAILURE (-1f) → compute exception
    private fun computeKmForDay(locFile: File, day: String): Float {
        return try {
            val points = locFile.useLines { seq ->
                seq.filter { it.trim().isNotEmpty() }
                    .drop(1)
                    .mapNotNull { line ->
                        val parts = line.split(',')
                        if (parts.size >= 4 && parts[0].startsWith(day)) {
                            val lat = parts[1].toDoubleOrNull()
                            val lon = parts[2].toDoubleOrNull()
                            if (lat != null && lon != null) {
                                val acc = parts.getOrNull(3)?.toFloatOrNull() ?: 0f
                                Triple(parts[0], Location("").apply {
                                    latitude  = lat
                                    longitude = lon
                                }, acc)
                            } else null
                        } else null
                    }
                    .sortedBy { it.first }
                    .toList()
            }

            if (points.size < 2) {
                Log.d(TAG, "Sparse log for $day (${points.size} point(s)) — flagging as sparse_log")
                return SENTINEL_SPARSE
            }

            var meters   = 0f
            var prevLoc  = points.first().second
            var prevAcc  = points.first().third

            for (i in 1 until points.size) {
                val (ts, loc, acc) = points[i]
                val d = prevLoc.distanceTo(loc)

                val hopOk = d.isFinite() && d >= 0f && d <= 30_000f
                val accOk = (acc == 0f || acc <= 100f) && (prevAcc == 0f || prevAcc <= 100f)

                if (hopOk && accOk) {
                    meters  += d
                    prevLoc  = loc
                    prevAcc  = acc
                } else {
                    Log.d(TAG, "Skip hop @$ts d=${"%.1f".format(d)}m acc=$acc prevAcc=$prevAcc")
                }
            }

            meters / 1000f

        } catch (t: Throwable) {
            Log.e(TAG, "computeKmForDay error for $day", t)
            SENTINEL_FAILURE
        }
    }

    private fun wasLocationServiceRunningOnDay(hbFile: File, day: String): Boolean {
        if (!hbFile.exists()) return false
        return try {
            hbFile.useLines { seq ->
                seq.any { line ->
                    line.length > 10 &&
                            line.substring(0, 10) == day &&
                            line.contains("LocationCaptureService")
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error checking heartbeat for $day", t)
            false
        }
    }

    private fun applyUpdates(
        outFile:       File,
        header:        String,
        participantId: String,
        updates:       List<Pair<String, Any>>
    ) {
        try {
            outFile.parentFile?.mkdirs()
            val existing      = if (outFile.exists()) outFile.readLines() else emptyList()
            val withoutHeader = existing.filterNot { it.startsWith("record_id,") }.toMutableList()

            val dates = updates.map { it.first }.toSet()
            val kept  = withoutHeader
                .filterNot { line -> dates.any { d -> line.contains(",$d,") } }
                .toMutableList()

            updates.forEach { (d, value) ->
                val (kmStr, quality) = when (value) {
                    // String sentinels from the missing-log branch
                    is String -> value to value

                    is Float  -> when (value) {
                        SENTINEL_FAILURE -> "SENSOR_FAILURE" to "SENSOR_FAILURE"
                        SENTINEL_SPARSE  -> "0.00"           to "sparse_log"
                        else             -> String.format(Locale.US, "%.2f", value) to "ok"
                    }

                    else -> "UNKNOWN" to "UNKNOWN"
                }
                val recordId = "${participantId}_$d"
                kept += "$recordId,$participantId,$d,v1.0,distance,$kmStr,$quality"
            }

            outFile.writeText(buildString {
                append(header)
                kept.forEach { append(it).append('\n') }
            })
            Log.d(TAG, "Updated ${outFile.name} with ${updates.size} entries")

        } catch (t: Throwable) {
            Log.e(TAG, "applyUpdates error", t)
        }
    }

    private fun dayString(offsetDays: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offsetDays) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    companion object {
        private const val TAG              = "DistanceSummaryWorker"
        private const val SENTINEL_FAILURE = -1f  // compute exception
        private const val SENTINEL_SPARSE  = -2f  // log exists, < 2 GPS points
    }
}
