// app/src/main/java/com/nick/myrecoverytracker/RollupValidator.kt
package com.nick.myrecoverytracker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private enum class T { STRING, DATE, INT, FLOAT, ENUM }

object RollupValidator {

    fun validateUnlocks(context: Context): Boolean = validate(context, "daily_unlocks.csv")

    fun validate(context: Context, fileName: String): Boolean {
        val f = File(context.filesDir, fileName)
        if (!f.exists()) return false

        val lines = f.readLines()
        if (lines.isEmpty()) {
            writeQa(context, fileName.removeSuffix(".csv"), false, listOf("EMPTY_FILE"), mapOf())
            return false
        }

        val header = lines.first().trim()
        val cols = header.split(',')
        val dateIdx = cols.indexOf("date")
        val countIdx = when {
            cols.contains("daily_unlocks") -> cols.indexOf("daily_unlocks")
            cols.contains("unlocks") -> cols.indexOf("unlocks")
            else -> -1
        }
        val schemaIdx = cols.indexOf("feature_schema_version")

        var pass = true
        val reasons = mutableListOf<String>()
        var rows = 0
        var badCols = 0
        var badTypes = 0
        var badRange = 0
        val uniq = HashMap<String, Int>()
        val dateRe = Regex("""^\d{4}-\d{2}-\d{2}$""")
        val intRange = 0..2000

        if (dateIdx < 0 || countIdx < 0) {
            reasons += "BAD_HEADER($header)"
            pass = false
        }

        if (pass) {
            lines.drop(1).forEach { line ->
                if (line.isBlank()) return@forEach
                rows++
                val parts = line.split(',')
                val needed = listOf(dateIdx, countIdx).maxOrNull() ?: -1
                if (parts.size <= needed) {
                    badCols++
                    return@forEach
                }

                val dateRaw = parts[dateIdx].trim()
                if (!dateRe.matches(dateRaw)) badTypes++

                val countRaw = parts[countIdx].trim()
                val cVal = countRaw.toIntOrNull()
                if (cVal == null) badTypes++ else if (cVal !in intRange) badRange++

                if (schemaIdx >= 0 && schemaIdx < parts.size) {
                    parts[schemaIdx].trim()
                }

                val key = dateRaw
                uniq[key] = (uniq[key] ?: 0) + 1
            }
        }

        val dupCount = uniq.values.count { it > 1 }
        if (dupCount > 0) { pass = false; reasons += "DUP_KEYS=$dupCount" }
        if (badCols > 0) { pass = false; reasons += "BAD_COLS=$badCols" }
        if (badTypes > 0) { pass = false; reasons += "BAD_TYPES=$badTypes" }
        if (badRange > 0) { pass = false; reasons += "OUT_OF_RANGE=$badRange" }

        val today = LocalDate.now(ZoneId.systemDefault()).toString()
        val todayRows = uniq.filterKeys { it == today }.values.sum()

        val stats = mapOf(
            "rows" to rows,
            "dup_keys" to dupCount,
            "bad_cols" to badCols,
            "bad_types" to badTypes,
            "bad_range" to badRange,
            "today_rows" to todayRows,
            "header" to header
        )

        writeQa(context, fileName.removeSuffix(".csv"), pass, reasons, stats)
        return pass
    }

    private fun writeQa(context: Context, feature: String, pass: Boolean, reasons: List<String>, stats: Map<String, Any>) {
        val dir = context.filesDir ?: return
        val day = LocalDate.now(ZoneId.systemDefault()).toString()
        val outDir = File(dir, "qa/$day").apply { mkdirs() }
        val out = File(outDir, "$feature.json")
        val obj = JSONObject().apply {
            put("pass", pass)
            put("reasons", JSONArray(reasons))
            val s = JSONObject()
            stats.forEach { (k, v) -> s.put(k, v.toString()) }
            put("stats", s)
        }
        out.writeText(obj.toString())
    }
}