// UnlockMigrations.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object UnlockMigrations {

    private const val TAG = "UnlockMigrations"

    fun runMigrations(context: Context) {
        try {
            Log.i(TAG, "🔵 Starting unlock migrations")

            val dir = StorageHelper.getDataDir(context)
            if (!dir.exists()) dir.mkdirs()

            val legacyFiles = listOf(
                "unlock_rollup.csv",
                "daily_unlocks_count.csv",
                "daily_unlocks_v0.csv"
            )

            val targetFile = File(dir, "daily_unlocks.csv")
            var winnerFile: File? = null
            var winnerRows: Long = 0L

            for (legacyName in legacyFiles) {
                val f = File(dir, legacyName)
                if (f.exists()) {
                    val rows = f.readLines().size - 1
                    Log.i(TAG, "📄 Found legacy file $legacyName with $rows rows")
                    if (rows > winnerRows) {
                        winnerFile = f
                        winnerRows = rows.toLong()
                    }
                }
            }

            if (winnerFile != null && winnerRows > 0) {
                Log.i(TAG, "🏆 Winner: ${winnerFile.name} with $winnerRows rows")
                migrateFromLegacy(winnerFile, targetFile, context)
                logMigration(context, "daily_unlocks", winnerFile.name, "daily_unlocks.csv", winnerRows)
                Log.i(TAG, "✅ Migration complete: ${winnerFile.name} -> daily_unlocks.csv")
            } else {
                Log.i(TAG, "ℹ️ No legacy files found or all empty")
                ensureTargetExists(targetFile)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "❌ Migration failed", t)
        }
    }

    private fun migrateFromLegacy(legacyFile: File, targetFile: File, context: Context) {
        try {
            val lines = legacyFile.readLines()
            if (lines.isEmpty()) {
                Log.w(TAG, "⚠️ Legacy file is empty")
                return
            }

            val header = lines.first()
            val dataLines = lines.drop(1)

            if (targetFile.exists()) {
                Log.w(TAG, "⚠️ Target file already exists, overwriting")
            }

            targetFile.writeText("$header\n")
            dataLines.forEach { line ->
                if (line.trim().isNotEmpty()) {
                    targetFile.appendText("$line\n")
                }
            }

            Log.i(TAG, "✅ Migrated ${dataLines.size} rows from ${legacyFile.name}")
        } catch (t: Throwable) {
            Log.e(TAG, "❌ Migration from legacy failed", t)
        }
    }

    private fun ensureTargetExists(targetFile: File) {
        try {
            if (!targetFile.exists()) {
                targetFile.writeText("date,total_unlocks\n")
                Log.i(TAG, "📄 Created new daily_unlocks.csv")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to ensure target exists", t)
        }
    }

    private fun logMigration(context: Context, feature: String, fromFile: String, toFile: String, rows: Long) {
        try {
            val dir = StorageHelper.getDataDir(context)
            val f = File(dir, "migrations.log")

            if (!f.exists()) {
                f.writeText("ts,feature,from,to,rows\n")
            }

            val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(System.currentTimeMillis())
            f.appendText("$ts,$feature,$fromFile,$toFile,$rows\n")

            Log.d(TAG, "✅ Logged migration to migrations.log")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to log migration", t)
        }
    }
}