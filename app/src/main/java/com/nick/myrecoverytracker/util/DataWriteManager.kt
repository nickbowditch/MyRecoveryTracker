// app/src/main/java/com/nick/myrecoverytracker/util/DataWriteManager.kt
package com.nick.myrecoverytracker.util

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object DataWriteManager {
    private const val TAG = "DataWriteManager"

    private val locks = ConcurrentHashMap<String, ReentrantLock>()
    private fun lockFor(name: String): ReentrantLock = locks.getOrPut(name) { ReentrantLock() }

    private fun ensureDir(dir: File?): File {
        val d = dir ?: throw IOException("Directory handle is null")
        if (!d.exists() && !d.mkdirs()) throw IOException("Failed to create directory: ${d.absolutePath}")
        return d
    }

    private fun appendLineAtomic(target: File, header: String?, line: String) {
        target.parentFile?.let {
            if (!it.exists() && !it.mkdirs()) throw IOException("Failed to create parent: ${it.absolutePath}")
        }
        val isNew = !target.exists()
        FileOutputStream(target, true).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { w ->
                if (isNew && !header.isNullOrEmpty()) { w.write(header); w.write("\n") }
                w.write(line); w.write("\n"); w.flush()
                fos.fd.sync()
            }
        }
    }

    private fun writeTextAtomic(target: File, text: String) {
        target.parentFile?.let { if (!it.exists()) it.mkdirs() }
        val tmp = File(target.parentFile, target.name + ".tmp")
        FileOutputStream(tmp, false).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { w ->
                w.write(text); w.flush(); fos.fd.sync()
            }
        }
        if (target.exists() && !target.delete()) throw IOException("Failed to delete ${target.absolutePath}")
        if (!tmp.renameTo(target)) throw IOException("Failed to rename temp to ${target.absolutePath}")
    }

    fun persistCsv(context: Context, name: String, header: String?, line: String) {
        val lock = lockFor(name)
        lock.withLock {
            val internalDir = ensureDir(context.filesDir)
            val externalDir = ensureDir(context.getExternalFilesDir("csv"))
            Log.i(TAG, "preflight name=$name line='$line' internalDir=${internalDir.absolutePath} externalDir=${externalDir.absolutePath}")

            val targets = listOf(File(internalDir, name), File(externalDir, name))
            var anySuccess = false
            for (t in targets) {
                try {
                    val before = if (t.exists()) t.length() else -1L
                    appendLineAtomic(t, header, line)
                    val after = t.length()
                    anySuccess = true
                    Log.i(TAG, "write ✓ path=${t.absolutePath} existedBefore=${before>=0} sizeBefore=$before sizeAfter=$after")
                } catch (e: Exception) {
                    Log.e(TAG, "write ✗ path=${t.absolutePath} ex=${e.javaClass.simpleName}: ${e.message}", e)
                }
            }

            try {
                val chosen = if (File(externalDir, name).exists()) File(externalDir, name) else File(internalDir, name)
                audit(context, name, chosen)
            } catch (e: Exception) {
                Log.e(TAG, "audit ✗ $name ex=${e.javaClass.simpleName}: ${e.message}", e)
            }

            if (!anySuccess) Log.wtf(TAG, "NO_TARGETS_SUCCEEDED name=$name :: check storage/appops/permissions/OEM quirks")
        }
    }

    fun upsertCsvByPrefix(context: Context, name: String, header: String, prefix: String, line: String) {
        val lock = lockFor(name)
        lock.withLock {
            val internalDir = ensureDir(context.filesDir)
            val externalDir = ensureDir(context.getExternalFilesDir("csv"))
            val internal = File(internalDir, name)
            val external = File(externalDir, name)

            val source = if (external.exists()) external else internal
            val existingRaw = if (source.exists()) source.readText(StandardCharsets.UTF_8) else ""
            val existing = existingRaw.replace("\r\n", "\n").replace("\r", "\n")
                .lines()
                .filter { it.isNotEmpty() }

            val body = existing
                .asSequence()
                .filter { it != header && !it.startsWith(prefix) }
                .toMutableList()

            // If the exact new line already exists (edge case), ensure single occurrence.
            val filtered = body.filterNot { it == line }

            val finalText = buildString {
                append(header); append('\n')
                filtered.forEach { append(it); append('\n') }
                append(line); append('\n')
            }

            writeTextAtomic(internal, finalText)
            writeTextAtomic(external, finalText)

            val chosen = if (external.exists()) external else internal
            audit(context, name, chosen)
        }
    }

    private fun audit(context: Context, fileName: String, dataFile: File) {
        val auditDir = ensureDir(context.getExternalFilesDir("audit"))
        val auditFile = File(auditDir, "audit.log")

        val sha = try { sha1(dataFile) } catch (_: Exception) { "" }
        val record = JSONObject(
            mapOf(
                "t" to System.currentTimeMillis(),
                "file" to fileName,
                "path" to dataFile.absolutePath,
                "size_bytes" to (if (dataFile.exists()) dataFile.length() else -1L),
                "sha1" to sha
            )
        )

        FileOutputStream(auditFile, true).use { fos ->
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { w ->
                w.write(record.toString()); w.write("\n"); w.flush(); fos.fd.sync()
            }
        }
        Log.i(TAG, "audit ✓ file=${dataFile.absolutePath} -> ${auditFile.absolutePath}")
    }

    private fun sha1(f: File): String {
        val md = MessageDigest.getInstance("SHA-1")
        f.inputStream().use { ins ->
            val buf = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val r = ins.read(buf); if (r <= 0) break; md.update(buf, 0, r)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}