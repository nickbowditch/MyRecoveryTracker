// app/src/main/java/com/nick/myrecoverytracker/Csv.kt
package com.nick.myrecoverytracker

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object Csv {

    private const val RETENTION_DAYS = 30
    private val tsSecondFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val tsMinuteFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    private val lastPruneAtMs = ConcurrentHashMap<String, Long>()
    private const val PRUNE_MIN_INTERVAL_MS = 60_000L

    fun append(
        ctx: Context,
        name: String,
        header: String,
        cells: List<String>,
        timestampColumnIndex: Int = 0
    ) {
        val file = File(ctx.filesDir, name)
        ensureHeader(file, header)
        atomicAppendLine(file, toCsvLine(cells))
        maybePrune(ctx, file, header, timestampColumnIndex)
    }

    fun appendRawLine(
        ctx: Context,
        name: String,
        header: String,
        rawLine: String,
        timestampColumnIndex: Int = 0
    ) {
        val file = File(ctx.filesDir, name)
        ensureHeader(file, header)
        atomicAppendLine(file, rawLine)
        maybePrune(ctx, file, header, timestampColumnIndex)
    }

    private fun ensureHeader(file: File, header: String) {
        if (!file.exists()) {
            atomicWrite(file, header + "\n")
        } else if (file.length() == 0L) {
            atomicWrite(file, header + "\n")
        } else {
            file.inputStream().bufferedReader().use { br ->
                val first = br.readLine() ?: ""
                if (first != header) {
                    val tmp = File(file.parentFile, file.name + ".tmp.reheader")
                    tmp.outputStream().buffered().use { out ->
                        out.write((header + "\n").toByteArray())
                        file.inputStream().use { it.copyTo(out) }
                        out.flush()
                        (out as FileOutputStream).fd.sync()
                    }
                    replaceFile(tmp, file)
                }
            }
        }
    }

    private fun atomicAppendLine(file: File, line: String) {
        val tmp = File(file.parentFile, file.name + ".tmp.append")
        file.inputStream().use { input ->
            tmp.outputStream().buffered().use { out ->
                input.copyTo(out)
                out.write((if (line.endsWith("\n")) line else "$line\n").toByteArray())
                out.flush()
                (out as FileOutputStream).fd.sync()
            }
        }
        replaceFile(tmp, file)
    }

    private fun atomicWrite(dst: File, content: String) {
        val tmp = File(dst.parentFile, dst.name + ".tmp.write")
        tmp.outputStream().buffered().use { out ->
            out.write(content.toByteArray())
            out.flush()
            (out as FileOutputStream).fd.sync()
        }
        replaceFile(tmp, dst)
    }

    private fun replaceFile(tmp: File, dst: File) {
        if (dst.exists()) {
            val bak = File(dst.parentFile, dst.name + ".bak")
            if (bak.exists()) bak.delete()
            if (!dst.renameTo(bak)) {
                bak.delete()
                dst.renameTo(bak)
            }
            tmp.renameTo(dst)
            bak.delete()
        } else {
            tmp.renameTo(dst)
        }
    }

    private fun toCsvLine(cells: List<String>): String {
        val sb = StringBuilder()
        cells.forEachIndexed { i, raw ->
            if (i > 0) sb.append(',')
            sb.append(escape(raw))
        }
        sb.append('\n')
        return sb.toString()
    }

    private fun escape(s: String?): String {
        val v = s ?: ""
        val needs = v.any { it == '"' || it == ',' || it == '\n' || it == '\r' }
        if (!needs) return v
        return "\"" + v.replace("\"", "\"\"") + "\""
    }

    private fun maybePrune(ctx: Context, file: File, header: String, tsCol: Int) {
        val now = System.currentTimeMillis()
        val last = lastPruneAtMs[file.path] ?: 0L
        if (now - last < PRUNE_MIN_INTERVAL_MS) return
        lastPruneAtMs[file.path] = now

        val cutoff = now - RETENTION_DAYS * 86_400_000L
        val tmp = File(file.parentFile, file.name + ".tmp.prune")
        var wrote = false

        file.bufferedReader().useLines { lines ->
            tmp.bufferedWriter().use { out ->
                out.write(header)
                out.newLine()
                lines.drop(1).forEach { line ->
                    val cols = line.split(",")
                    if (cols.size <= tsCol) return@forEach
                    val tsStr = cols[tsCol]
                    val ts = parseTs(tsStr)
                    if (ts != null && ts >= cutoff) {
                        out.write(line)
                        out.newLine()
                        wrote = true
                    }
                }
                out.flush()
                (out as java.io.Writer).flush()
            }
        }
        if (wrote) {
            replaceFile(tmp, file)
        } else {
            tmp.delete()
        }
    }

    private fun parseTs(s: String): Long? {
        var pos = ParsePosition(0)
        val d1 = tsSecondFmt.parse(s, pos)
        if (d1 != null && pos.index == s.length) return d1.time
        pos = ParsePosition(0)
        val d2 = tsMinuteFmt.parse(s, pos)
        if (d2 != null && pos.index == s.length) return d2.time
        return null
    }
}