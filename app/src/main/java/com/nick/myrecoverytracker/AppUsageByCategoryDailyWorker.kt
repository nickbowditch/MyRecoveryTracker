package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Derives app_category_daily.csv from daily_app_usage_minutes.csv.
 *
 * Input schema (observed on device):
 *   date,app_min_social,...,app_min_other,app_min_total
 *
 * Output schema:
 *   date,category,minutes
 *
 * For each row in daily_app_usage_minutes.csv, we emit one row per category
 * where minutes > 0. app_min_total is ignored as an input category.
 */
class AppUsageByCategoryDailyWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "AppUsageByCategoryDaily"
        private const val INPUT_FILE = "daily_app_usage_minutes.csv"
        private const val OUTPUT_FILE = "app_category_daily.csv"
        private const val PREFIX = "app_min_"
        private const val TOTAL_COL = "app_min_total"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val filesDir = applicationContext.filesDir
        val inFile = File(filesDir, INPUT_FILE)

        if (!inFile.exists()) {
            Log.w(TAG, "Missing $INPUT_FILE; nothing to do.")
            return@withContext Result.success()
        }

        val lines = inFile.readLines().filter { it.isNotBlank() }
        if (lines.size <= 1) {
            Log.w(TAG, "$INPUT_FILE has header only; nothing to roll up.")
            return@withContext Result.success()
        }

        val header = lines.first()
        val cols = header.split(",")
        if (cols.isEmpty() || !cols[0].equals("date", ignoreCase = true)) {
            Log.w(TAG, "Unexpected header in $INPUT_FILE: $header")
            return@withContext Result.success()
        }

        // Identify category columns based on the observed schema.
        val catCols = cols.withIndex()
            .filter { (idx, name) ->
                idx > 0 &&
                        name.startsWith(PREFIX) &&
                        !name.equals(TOTAL_COL, ignoreCase = true)
            }
            .map { it.index to it.value }

        if (catCols.isEmpty()) {
            Log.w(TAG, "No app_min_* category columns found in $INPUT_FILE")
            return@withContext Result.success()
        }

        // Build output rows.
        val outRows = mutableListOf<String>()
        for (line in lines.drop(1)) {
            val row = line.trim()
            if (row.isEmpty()) continue
            val parts = row.split(",")
            if (parts.size < cols.size) continue

            val date = parts[0]
            for ((idx, colName) in catCols) {
                val raw = parts[idx]
                val mins = raw.toDoubleOrNull() ?: 0.0
                if (mins <= 0.0) continue

                val category = normaliseCategory(colName)
                outRows.add("$date,$category,${"%.2f".format(Locale.US, mins)}")
            }
        }

        val outFile = File(filesDir, OUTPUT_FILE)
        outFile.printWriter().use { out ->
            out.println("date,category,minutes")
            outRows.forEach { out.println(it) }
        }

        Log.i(TAG, "Wrote ${outRows.size} rows to $OUTPUT_FILE")
        Result.success()
    }

    private fun normaliseCategory(colName: String): String {
        // "app_min_social" -> "social"
        // "app_min_music_audio" -> "music_audio", etc.
        return colName.removePrefix(PREFIX)
    }
}