package com.nick.myrecoverytracker

import android.content.Context
import java.io.File
import java.io.FileWriter

/**
 * Writes/supports the daily counts CSV:
 * date,sms_out_whitelist,sms_in_whitelist,sms_out_total,sms_in_total,call_out_whitelist,call_in_whitelist,call_out_total,call_in_total
 */
object MetricsStoreCounts {

    private const val FILE_NAME = "support_counts.csv"

    fun writeDailyCounts(
        ctx: Context,
        day: String,
        smsOutWhitelist: Int,
        smsInWhitelist: Int,
        smsOutTotal: Int,
        smsInTotal: Int,
        callOutWhitelist: Int,
        callInWhitelist: Int,
        callOutTotal: Int,
        callInTotal: Int
    ) {
        val file = File(ctx.filesDir, FILE_NAME)
        val newLine = listOf(
            day,
            smsOutWhitelist, smsInWhitelist, smsOutTotal, smsInTotal,
            callOutWhitelist, callInWhitelist, callOutTotal, callInTotal
        ).joinToString(",")

        val lines = if (file.exists()) file.readLines().filterNot { it.startsWith("$day,") }.toMutableList()
        else mutableListOf()

        lines += newLine
        FileWriter(file, false).use { w -> lines.forEach { w.appendLine(it) } }
    }
}