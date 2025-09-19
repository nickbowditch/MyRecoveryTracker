// app/src/main/java/com/nick/myrecoverytracker/CsvUtils.kt
package com.nick.myrecoverytracker

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object CsvUtils {
    private val zone: ZoneId = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun todayLocal(): String = LocalDate.now(zone).format(fmtDate)
    fun todayUtc(): String = LocalDate.now(ZoneOffset.UTC).format(fmtDate)

    fun csvEscape(s: String): String {
        if (s.isEmpty()) return ""
        val needs = s.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (!needs) s else "\"" + s.replace("\"", "\"\"") + "\""
    }

    fun csvJoin(cols: List<String>): String = cols.joinToString(",") { csvEscape(it) }

    fun ensureHeader(f: File, header: String): File {
        if (!f.exists() || f.length() == 0L) {
            f.parentFile?.mkdirs()
            writeAtomic(f, header + "\n")
        }
        return f
    }

    fun upsertByDate(file: File, dateStr: String, tailCols: List<String>) {
        val lines = if (file.exists()) file.readLines().toMutableList() else mutableListOf()
        if (lines.isEmpty()) return
        val header = lines.first()
        var replaced = false
        for (i in 1 until lines.size) {
            val idx = lines[i].indexOf(',')
            val key = if (idx >= 0) lines[i].substring(0, idx) else lines[i]
            if (key == dateStr) {
                lines[i] = csvJoin(listOf(dateStr) + tailCols)
                replaced = true
                break
            }
        }
        if (!replaced) lines.add(csvJoin(listOf(dateStr) + tailCols))
        writeAtomic(file, (listOf(header) + lines.drop(1)).joinToString("\n") + "\n")
    }

    fun healTodayUtcVsLocal(file: File) {
        if (!file.exists()) return
        val lines = file.readLines().toMutableList()
        if (lines.isEmpty()) return
        val header = lines.removeAt(0)
        val local = todayLocal()
        val utc = todayUtc()
        val kept = ArrayList<String>(lines.size)
        val seen = HashSet<String>()
        var hasLocalToday = false
        for (line in lines) {
            val idx = line.indexOf(',')
            val key = if (idx >= 0) line.substring(0, idx) else line
            if (key == local) hasLocalToday = true
            if (!seen.add(key)) continue
            kept.add(line)
        }
        val finalKept = if (hasLocalToday && local != utc) kept.filterNot { it.startsWith("$utc,") } else kept
        writeAtomic(file, (sequenceOf(header) + finalKept.asSequence()).joinToString("\n") + "\n")
    }

    fun rotateByDate(file: File, keepDays: Int) {
        if (!file.exists()) return
        val lines = file.readLines()
        if (lines.isEmpty()) return
        val header = lines.first()
        val cutoff = LocalDate.now(zone).minusDays(keepDays.toLong())
        val kept = lines.drop(1).filter { line ->
            val idx = line.indexOf(',')
            if (idx <= 0) true else {
                val ds = line.substring(0, idx)
                try { LocalDate.parse(ds, fmtDate) >= cutoff } catch (_: Throwable) { true }
            }
        }
        writeAtomic(file, (sequenceOf(header) + kept.asSequence()).joinToString("\n") + "\n")
    }

    fun rotateByTimestampPrefix(file: File, keepDays: Int) {
        if (!file.exists()) return
        val lines = file.readLines()
        if (lines.isEmpty()) return
        val header = if (looksLikeHeader(lines.first())) lines.first() else null
        val body = if (header != null) lines.drop(1) else lines
        val cutoff = LocalDate.now(zone).minusDays(keepDays.toLong())
        val kept = body.filter { line ->
            if (line.length < 10) true else {
                val ds = line.substring(0, 10)
                try { LocalDate.parse(ds, fmtDate) >= cutoff } catch (_: Throwable) { true }
            }
        }
        val out = if (header != null) sequenceOf(header) + kept.asSequence() else kept.asSequence()
        writeAtomic(file, out.joinToString("\n") + "\n")
    }

    fun looksLikeHeader(firstLine: String): Boolean {
        val s = firstLine.lowercase(Locale.US)
        return s.startsWith("date,") || s.startsWith("timestamp,")
    }

    fun writeAtomic(dst: File, content: String) {
        val tmp = File(dst.parentFile ?: dst.parentFile, dst.name + ".tmp")
        FileOutputStream(tmp).channel.use { ch: FileChannel ->
            ch.truncate(0)
            ch.write(ByteBuffer.wrap(content.toByteArray()))
            ch.force(true)
        }
        if (!tmp.renameTo(dst)) {
            dst.delete()
            tmp.renameTo(dst)
        }
    }
}