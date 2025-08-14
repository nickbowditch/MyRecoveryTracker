package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Shim helpers expected by workers:
 *  - appendSmsLog(...)
 *  - appendCallLog(...)
 *  - appendSupportDaily(...)
 *  - appendOtherDaily(...)
 *
 * Files:
 *  - files/sms_log.csv              (timestamp, direction, address, body)
 *  - files/call_log.csv             (timestamp, type, number, durationSec)
 *  - files/support_response.csv     (YYYY-MM-DD,inbound_sms,inbound_missed,responded_sms,responded_calls,sms_rate,call_rate)
 *  - files/other_response.csv       (mirror/extra lines for quick tail)
 */
object MetricsStoreSupport {

    private const val SMS_LOG_FILE = "sms_log.csv"
    private const val CALL_LOG_FILE = "call_log.csv"
    private const val SUPPORT_DAILY_FILE = "support_response.csv"
    private const val OTHER_DAILY_FILE = "other_response.csv"

    private val tsFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // ---------------- SMS ----------------

    /** Single unambiguous signature */
    fun appendSmsLog(
        context: Context,
        direction: String,          // e.g., IN | OUT
        address: String,
        timestampMs: Long,
        body: String? = null
    ) {
        writeLine(
            context,
            SMS_LOG_FILE,
            "${tsFmt.format(Date(timestampMs))},$direction,$address,${csvEscape(body ?: "")}"
        )
        Log.i("MetricsStore", "✉️ SMS $direction logged for $address")
    }

    // ---------------- Calls --------------

    /** Single unambiguous signature */
    fun appendCallLog(
        context: Context,
        type: String,               // OUTGOING | INCOMING | MISSED ...
        number: String,
        timestampMs: Long,
        durationSec: Long = 0L
    ) {
        writeLine(
            context,
            CALL_LOG_FILE,
            "${tsFmt.format(Date(timestampMs))},$type,$number,$durationSec"
        )
        Log.i("MetricsStore", "📞 Call $type logged for $number ($durationSec s)")
    }

    // ------------- Support daily ---------

    /**
     * Replace today's row atomically with 7 columns:
     * day,inbound_sms,inbound_missed,responded_sms,responded_calls,sms_rate,call_rate
     */
    fun appendSupportDaily(
        context: Context,
        day: String = dayFmt.format(Date()),
        inboundSms: Int,
        inboundMissedCalls: Int,
        respondedSms: Int,
        respondedCalls: Int,
        smsRate: Double,
        callRate: Double
    ) {
        val file = File(context.filesDir, SUPPORT_DAILY_FILE)
        val newLine =
            "$day,$inboundSms,$inboundMissedCalls,$respondedSms,$respondedCalls,${fmt4(smsRate)},${fmt4(callRate)}"

        val lines = if (file.exists())
            file.readLines().filterNot { it.startsWith("$day,") }.toMutableList()
        else
            mutableListOf()

        lines += newLine
        FileWriter(file, /*append=*/false).use { w -> lines.forEach { w.appendLine(it) } }

        // Mirror to other_response for quick tail in terminal
        FileWriter(File(context.filesDir, OTHER_DAILY_FILE), /*append=*/true).use { w -> w.appendLine(newLine) }

        Log.i("MetricsStore", "🧾 Support daily written: $newLine")
    }

    /** Raw line helper (if some legacy worker builds the CSV itself) */
    fun appendOtherDaily(context: Context, csvLine: String) {
        writeLine(context, OTHER_DAILY_FILE, csvLine)
        Log.i("MetricsStore", "🧾 Other daily (raw) appended: $csvLine")
    }

    // ------------- Utils -----------------

    private fun writeLine(context: Context, name: String, line: String) {
        val f = File(context.filesDir, name)
        FileWriter(f, /*append=*/true).use { it.appendLine(line) }
    }

    private fun csvEscape(s: String): String =
        if (s.contains(',') || s.contains('"') || s.contains('\n'))
            "\"" + s.replace("\"", "\"\"") + "\""
        else s

    private fun fmt4(v: Double) = String.format(Locale.US, "%.4f", v)
}