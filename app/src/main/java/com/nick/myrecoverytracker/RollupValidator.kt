// app/src/main/java/com/nick/myrecoverytracker/qa/RollupValidator.kt
package com.nick.myrecoverytracker.qa

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

private enum class T { STRING, DATE, INT, FLOAT, ENUM }
private data class Schema(
    val fileName: String,
    val header: List<String>,
    val types: List<T>,
    val range: Map<String, Pair<Double, Double>> = emptyMap(),
    val enums: Map<String, Set<String>> = emptyMap(),
    val uniqueKeys: List<Int> = listOf(0) // default unique on date col
)

object RollupValidator {

    private val unlocksSchema = Schema(
        fileName = "daily_unlocks.csv",
        header = listOf("date","unlocks"),
        types = listOf(T.DATE, T.INT),
        range = mapOf("unlocks" to (0.0 to 2000.0)),
        uniqueKeys = listOf(0)
    )

    private val registry: Map<String, Schema> = mapOf(
        "daily_unlocks.csv" to unlocksSchema
    )

    fun validateUnlocks(context: Context): Boolean = validate(context, "daily_unlocks.csv")

    fun validate(context: Context, fileName: String): Boolean {
        val s = registry[fileName] ?: return false
        val f = File(context.filesDir, s.fileName)
        if (!f.exists()) return false

        val lines = f.readLines()
        if (lines.isEmpty()) return writeQa(context, fileName, false, listOf("EMPTY_FILE"), emptyMap()).let { false }

        val header = lines.first().trim()
        val expectHeader = s.header.joinToString(",")
        var pass = true
        val reasons = mutableListOf<String>()
        var rows = 0
        var badHeader = 0
        var badCols = 0
        var badTypes = 0
        var badEnums = 0
        var badRange = 0
        val uniq = HashMap<String, Int>()
        val dateRe = Regex("""^\d{4}-\d{2}-\d{2}$""")

        if (header != expectHeader) {
            badHeader = 1
            pass = false
            reasons += "BAD_HEADER($header)"
        }

        val nameIdx = s.header.withIndex().associate { it.value to it.index }

        fun typeOk(idx: Int, raw: String): Boolean {
            return when (s.types[idx]) {
                T.STRING -> true
                T.DATE   -> dateRe.matches(raw)
                T.INT    -> raw.toIntOrNull() != null
                T.FLOAT  -> raw.toDoubleOrNull() != null
                T.ENUM   -> {
                    val key = s.header[idx]
                    val set = s.enums[key] ?: return false
                    set.contains(raw)
                }
            }
        }

        fun rangeOk(idx: Int, raw: String): Boolean {
            val key = s.header[idx]
            val bound = s.range[key] ?: return true
            val v = raw.toDoubleOrNull() ?: return false
            return v >= bound.first && v <= bound.second
        }

        lines.drop(1).forEach { line ->
            if (line.isBlank()) return@forEach
            rows++
            val parts = line.split(',')
            if (parts.size != s.header.size) { badCols++; return@forEach }

            var localBadTypes = 0
            var localBadEnums = 0
            var localBadRange = 0

            parts.forEachIndexed { i, raw ->
                if (!typeOk(i, raw)) {
                    localBadTypes++
                } else {
                    if (!rangeOk(i, raw)) localBadRange++
                    if (s.types[i] == T.ENUM) {
                        val key = s.header[i]
                        val set = s.enums[key]!!
                        if (!set.contains(raw)) localBadEnums++
                    }
                }
            }

            if (localBadTypes > 0) badTypes += localBadTypes
            if (localBadRange > 0) badRange += localBadRange
            if (localBadEnums > 0) badEnums += localBadEnums

            val key = s.uniqueKeys.joinToString("|") { parts[it].trim() }
            uniq[key] = (uniq[key] ?: 0) + 1
        }

        val dupCount = uniq.values.count { it > 1 }
        if (dupCount > 0) { pass = false; reasons += "DUP_KEYS=$dupCount" }
        if (badCols > 0) { pass = false; reasons += "BAD_COLS=$badCols" }
        if (badTypes > 0) { pass = false; reasons += "BAD_TYPES=$badTypes" }
        if (badRange > 0) { pass = false; reasons += "OUT_OF_RANGE=$badRange" }
        if (badEnums > 0) { pass = false; reasons += "BAD_ENUMS=$badEnums" }
        if (badHeader > 0) pass = false

        val today = LocalDate.now(ZoneId.systemDefault()).toString()
        val todayRows = uniq.filterKeys { it.split("|").firstOrNull() == today }.values.sum()

        val stats = mapOf(
            "rows" to rows,
            "dup_keys" to dupCount,
            "bad_cols" to badCols,
            "bad_types" to badTypes,
            "bad_range" to badRange,
            "bad_enums" to badEnums,
            "today_rows" to todayRows
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
            stats.forEach { (k, v) -> s.put(k, v) }
            put("stats", s)
        }
        out.writeText(obj.toString())
    }
}