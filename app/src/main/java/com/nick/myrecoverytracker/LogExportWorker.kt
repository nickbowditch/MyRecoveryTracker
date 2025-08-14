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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LogExportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dir = applicationContext.filesDir
            val outFile = File(dir, "export_logs.zip")
            val sources = dir.listFiles { f -> f.isFile && f.name.endsWith(".csv") }?.toList().orEmpty()

            if (sources.isEmpty()) {
                Log.i(TAG, "No CSV files to export")
                // still create an empty zip to keep flow consistent
                ZipOutputStream(outFile.outputStream()).use { /* empty */ }
                return@withContext Result.success()
            }

            ZipOutputStream(outFile.outputStream()).use { zip ->
                sources.forEach { src ->
                    BufferedInputStream(FileInputStream(src)).use { input ->
                        val entry = ZipEntry(src.name)
                        zip.putNextEntry(entry)
                        input.copyTo(zip, 8 * 1024)
                        zip.closeEntry()
                    }
                }
            }
            Log.i(TAG, "Exported ${sources.size} files to ${outFile.absolutePath}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "Export failed", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "LogExportWorker"
    }
}