// app/src/main/java/com/nick/myrecoverytracker/LogExportWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LogExportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dir = applicationContext.filesDir
        val zipFile = File(dir, "export_logs.zip")
        val csvFiles = dir.listFiles { f -> f.isFile && f.name.endsWith(".csv") }?.toList().orEmpty()

        try {
            // Always (re)create the zip, even if empty
            ZipOutputStream(zipFile.outputStream()).use { zip ->
                csvFiles.forEach { src ->
                    BufferedInputStream(FileInputStream(src)).use { input ->
                        zip.putNextEntry(ZipEntry(src.name))
                        input.copyTo(zip, 8 * 1024)
                        zip.closeEntry()
                    }
                }
            }

            // Compute metrics for the CSV log
            val count = csvFiles.size
            val bytes = if (zipFile.exists()) zipFile.length() else 0L
            val status = if (bytes > 0L) "OK" else "EMPTY"

            upsertLogCsv(
                file = File(dir, "log_export.csv"),
                date = today(),
                exportFile = "export_logs.zip",
                count = count,
                bytes = bytes,
                status = status
            )

            Log.i(TAG, "Exported $count CSVs -> ${zipFile.absolutePath} ($bytes bytes); status=$status")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "Export failed", t)
            Result.retry()
        }
    }

    private fun upsertLogCsv(
        file: File,
        date: String,
        exportFile: String,
        count: Int,
        bytes: Long,
        status: String
    ) {
        val header = "date,export_file,count,bytes,status"
        val lines = if (file.exists()) file.readLines().toMutableList() else mutableListOf()

        if (lines.isEmpty() || lines.first() != header) {
            lines.clear()
            lines += header
        }

        // Remove any existing row for 'date'
        val body = lines.drop(1).filterNot { it.startsWith("$date,") }.toMutableList()

        // Append the fresh row for today
        body += listOf("$date,$exportFile,$count,$bytes,$status")

        file.writeText((sequenceOf(header) + body.asSequence()).joinToString("\n") + "\n")
    }

    private fun today(): String =
        LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE)

    companion object {
        private const val TAG = "LogExportWorker"
    }
}