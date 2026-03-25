// MovementIntensityDailyWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MovementIntensityDailyWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val zone = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    private val movementLog by lazy { File(StorageHelper.getDataDir(applicationContext), MOVEMENT_LOG) }
    private val outFile by lazy { File(StorageHelper.getDataDir(applicationContext), OUT_NAME) }
    private val hbFile by lazy { File(StorageHelper.getDataDir(applicationContext), HEARTBEAT_FILE) }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val today = LocalDate.now(zone)
        val dateStr = today.format(fmtDate)

        val inExists = unlockLog.exists()
        val inSize = if (inExists) unlockLog.length() else 0L
        val outExists = outFile.exists()
        val outSize = if (outExists) outFile.length() else 0L

        probe("START today=$dateStr in_exists=$inExists in_size=$inSize out_exists=$outExists out_size=$outSize")

        return@withContext try {
            val participantId = ParticipantIdManager.getOrCreate(applicationContext)
            val intensity = avgMagnitudeForDate(today)
            upsertDailyCsv(dateStr, participantId, intensity)

            probe("END success today=$dateStr intensity=$intensity out_size_now=${outFile.length()}")
            Log.i(TAG, "MovementIntensityDaily -> $dateStr=$intensity")

            Result.success()
        } catch (t: Throwable) {
            probe("END failure ${t.javaClass.simpleName}: ${t.message}")
            Log.e(TAG, "MovementIntensityDailyWorker failed", t)
            Result.retry()
        }
    }

    // #4 FIX: Read actual accelerometer magnitudes from movement_log.csv.
    // MovementCaptureService writes rows as: "yyyy-MM-dd HH:mm:ss,<magnitude>"
    // Step rows are: "yyyy-MM-dd HH:mm:ss,step,<value>" — skip those.
    // Activity rows (AR) write 0.000000 or 1.000000 — skip those too (non-step numeric but binary).
    // We only want the continuous magnitude samples from writeMagnitude().
    private fun avgMagnitudeForDate(day: LocalDate): Double {
        if (!movementLog.exists()) return 0.0

        val wanted = day.format(fmtDate)
        val magnitudes = mutableListOf<Double>()

        movementLog.forEachLine { raw ->
            val line = raw.trim()
            if (line.length < 10) return@forEachLine
            val datePrefix = line.substring(0, 10)
            if (datePrefix != wanted) return@forEachLine

            val cols = line.split(",")
            // Skip step rows: cols[1] == "step"
            if (cols.size == 3 && cols[1].trim() == "step") return@forEachLine
            // Magnitude rows: cols.size == 2, cols[1] is a Double > 1.0
            // AR rows also have cols.size == 2 but value is exactly 0.0 or 1.0 — exclude
            if (cols.size == 2) {
                val v = cols[1].trim().toDoubleOrNull() ?: return@forEachLine
                if (v != 0.0 && v != 1.0) {  // exclude binary AR events
                    magnitudes.add(v)
                }
            }
        }

        return if (magnitudes.isNotEmpty()) magnitudes.average() else 0.0
    }

    private fun upsertDailyCsv(dateStr: String, participantId: String, intensity: Double) {
        val header = HEADER
        val recordId = "${participantId}_$dateStr"
        val line = "$recordId,$participantId,$dateStr,$FEATURE_SCHEMA_VERSION,$EVENT_NAME,${String.format(Locale.US, "%.6f", intensity)}"

        val existingLines = if (outFile.exists()) {
            outFile.readLines().map { it.trimEnd('\r') }
        } else {
            emptyList()
        }

        val map = LinkedHashMap<String, String>(existingLines.size.coerceAtLeast(16))

        map.clear()

        if (existingLines.isNotEmpty() && existingLines.first() == header) {
            existingLines.drop(1).forEach { row ->
                val d = row.substringBefore(',')
                // Extract date from recordId_date pattern
                if (d.contains('_')) {
                    val dateFromId = d.substringAfterLast('_')
                    if (dateFromId.length == 10 && DATE_RE.matches(dateFromId)) {
                        map[dateFromId] = row
                    }
                }
            }
        }

        map[dateStr] = line

        val rebuilt = buildString {
            append(header).append('\n')
            map.keys.sorted().forEach { d ->
                append(map[d]).append('\n')
            }
        }

        val tmp = File(outFile.parentFile, "${OUT_NAME}.tmp")
        tmp.writeText(rebuilt)
        if (!tmp.renameTo(outFile)) {
            outFile.writeText(rebuilt)
            tmp.delete()
        }
    }

    private fun probe(message: String) {
        try {
            Log.e(PROBE_TAG, message)
            if (!hbFile.exists() || hbFile.length() == 0L) {
                hbFile.writeText("ts,message\n")
            }
            hbFile.appendText("${System.currentTimeMillis()},$message\n")
        } catch (_: Throwable) {
            // never fail work because probe failed
        }
    }

    companion object {
        private const val TAG = "MovementIntensityDaily"
        private const val PROBE_TAG = "MOVE_INTENSITY_PROBE"

        private const val OUT_NAME = "daily_movement_intensity.csv"
        private const val HEADER = "record_id,participant_id,date,feature_schema_version,event,movement_intensity"
        private const val MOVEMENT_LOG = "movement_log.csv"
        private const val HEARTBEAT_FILE = "movement_intensity_heartbeat.csv"
        private const val FEATURE_SCHEMA_VERSION = "1"
        private const val EVENT_NAME = "MovementIntensity"

        private val DATE_RE = Regex("""^\d{4}-\d{2}-\d{2}$""")
    }
}