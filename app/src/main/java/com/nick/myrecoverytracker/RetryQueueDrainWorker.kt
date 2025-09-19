package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class RetryQueueDrainWorker(appContext: Context, params: WorkerParameters)
    : CoroutineWorker(appContext, params) {

    private val ctx = applicationContext
    private val queue = RetryQueue(ctx)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val tag = "RetryQueueDrainWorker"

        if (!RetryQueue.netOk(ctx)) {
            Log.i(tag, "No network; retry later.")
            return@withContext Result.retry()
        }

        val cfg = readConfig(File(ctx.filesDir, "redcap_config.json"))
        if (cfg == null) {
            Log.i(tag, "No redcap_config.json; nothing to send.")
            return@withContext Result.success()
        }

        val items = queue.peekReady(limit = 10)
        if (items.isEmpty()) {
            Log.i(tag, "Queue empty.")
            return@withContext Result.success()
        }

        var allOk = true
        val client = RedcapClient(cfg.url, cfg.token)

        for (item in items) {
            val id = item.optString("id")
            // Stored as RAW JSON STRING (e.g., "[{...}]")
            val payloadStr = item.optString("payload", "[]")
            try {
                val resp = client.postRecords(payloadStr)
                RetryQueue.logUpload(ctx, resp.code, resp.ok, resp.body)
                queue.markResult(id, resp.ok)
                if (!resp.ok) allOk = false
            } catch (t: Throwable) {
                RetryQueue.logUpload(ctx, 0, false, "exception: ${t.message}")
                queue.markResult(id, false)
                allOk = false
            }
        }

        if (allOk) Result.success() else Result.retry()
    }

    private data class Config(val url: String, val token: String, val recordId: String)

    private fun readConfig(file: File): Config? {
        if (!file.exists()) return null
        return runCatching {
            val obj = JSONObject(file.readText())
            val url = obj.optString("url", "").trim()
            val token = obj.optString("token", "").trim()
            val record = obj.optString("record_id", "").trim()
            if (url.isEmpty() || token.isEmpty()) null else Config(url, token, record)
        }.getOrNull()
    }
}