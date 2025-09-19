// app/src/main/java/com/nick/myrecoverytracker/UnlockMigrations.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object UnlockMigrations {
    private const val TAG = "UnlockMigrations"
    private val TS = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private const val MAPLOG = "migrations.log"

    // Known legacy → aligned targets for UNLOCKS feature
    private val LEGACY_FILES: List<Pair<String, String>> = listOf(
        // examples of plausible old names; harmless if absent
        "unlock_rollup.csv" to "daily_unlocks.csv",
        "daily_unlocks_count.csv" to "daily_unlocks.csv",
        "daily_unlocks_v0.csv" to "daily_unlocks.csv"
    )

    fun run(ctx: Context) {
        val filesDir = ctx.filesDir ?: return
        for ((legacy, target) in LEGACY_FILES) {
            val from = File(filesDir, legacy)
            val to = File(filesDir, target)
            if (from.exists()) {
                // if target exists, keep the larger/most recent as winner
                val winner = chooseWinner(from, to)
                // ensure target is winner
                if (winner === from) {
                    if (to.exists()) to.delete()
                    val ok = from.renameTo(to)
                    if (!ok) {
                        Log.w(TAG, "rename failed: $legacy → $target")
                    }
                }
                writeMapping(ctx, "unlocks", legacy, target, safeRowCount(to))
                // normalize perms
                try { to.setReadable(true, /*ownerOnly=*/true); to.setWritable(true, /*ownerOnly=*/true) } catch (_: Throwable) {}
            }
        }
    }

    private fun chooseWinner(a: File, b: File): File {
        return when {
            !b.exists() -> a
            a.lastModified() > b.lastModified() -> a
            a.length() > b.length() -> a
            else -> b
        }
    }

    private fun safeRowCount(f: File): Int {
        return try {
            f.useLines { seq -> seq.count { it.isNotBlank() } }
        } catch (_: Throwable) { 0 }
    }

    private fun writeMapping(ctx: Context, feature: String, from: String, to: String, rows: Int) {
        try {
            val f = File(ctx.filesDir, MAPLOG)
            if (!f.exists()) f.writeText("ts,feature,from,to,rows\n")
            val ts = TS.format(System.currentTimeMillis())
            f.appendText("$ts,$feature,$from,$to,$rows\n")
        } catch (_: Throwable) {}
    }
}