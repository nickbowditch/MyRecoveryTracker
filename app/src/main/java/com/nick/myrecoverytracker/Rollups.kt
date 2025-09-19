// app/src/main/java/com/nick/myrecoverytracker/Rollups.kt
package com.nick.myrecoverytracker

import android.content.Context
import java.io.File
import java.util.TreeMap

object Rollups {

    fun runAll(ctx: Context) {
        runDailyUnlocks(ctx)
        // hook other rollups here as needed
    }

    fun runDailyUnlocks(ctx: Context) {
        val dir = ctx.filesDir
        val raw = File(dir, "unlock_log.csv")
        val out = File(dir, "daily_unlocks.csv")

        if (!raw.exists()) {
            writeDailyUnlocks(out, emptyMap())
            return
        }

        val counts = TreeMap<String, Int>() // date -> count
        raw.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                // format: "YYYY-MM-DD HH:mm:ss,UNLOCK[,...]"
                if (line.length < 20) return@forEach
                val date = line.substring(0, 10)
                val comma = line.indexOf(',')
                if (comma > 0) {
                    val evt = line.substring(comma + 1)
                    if (evt.startsWith("UNLOCK")) {
                        counts[date] = (counts[date] ?: 0) + 1
                    }
                }
            }
        }

        writeDailyUnlocks(out, counts)
    }

    private fun writeDailyUnlocks(outFile: File, counts: Map<String, Int>) {
        val tmp = File(outFile.parentFile, outFile.name + ".tmp")
        tmp.bufferedWriter().use { w ->
            w.write("date,unlocks\n")
            for ((date, count) in counts) {
                w.write("$date,$count\n")
            }
        }
        if (outFile.exists()) outFile.delete()
        tmp.renameTo(outFile)
    }
}