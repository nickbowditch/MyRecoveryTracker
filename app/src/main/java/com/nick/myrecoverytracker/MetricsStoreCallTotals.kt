package com.nick.myrecoverytracker

import android.content.Context
import java.io.File
import java.io.FileWriter

/**
 * CSV: date,call_out_total,call_in_total
 */
object MetricsStoreCallTotals {

    private const val FILE = "call_totals.csv"

    fun writeDaily(ctx: Context, day: String, outTotal: Int, inTotal: Int) {
        val f = File(ctx.filesDir, FILE)
        val line = "$day,$outTotal,$inTotal"
        val lines = if (f.exists()) f.readLines().filterNot { it.startsWith("$day,") }.toMutableList()
        else mutableListOf()
        lines += line
        FileWriter(f, false).use { w -> lines.forEach { w.appendLine(it) } }
    }
}