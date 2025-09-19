// app/src/main/java/com/nick/myrecoverytracker/RedcapUploadWorker.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.InetAddress
import java.net.URI
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import com.nick.myrecoverytracker.qa.RollupValidator

class RedcapUploadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    private val ctx = applicationContext

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val filesDir = ctx.filesDir ?: return@withContext Result.retry()
        val prefs = ctx.getSharedPreferences("redcap_upload", Context.MODE_PRIVATE)

        try {
            val qf = File(filesDir, QUEUE_FILE)
            ensureQueueHeader(qf, QUEUE_HEADER)
        } catch (_: Throwable) {}

        val rollup = File(filesDir, "daily_unlocks.csv")
        if (!rollup.exists()) return@withContext Result.success()
        val lines = rollup.readLines()
        if (lines.isEmpty()) return@withContext Result.success()

        val body: List<String> = if (looksLikeHeader(lines.first())) lines.drop(1) else lines
        if (body.isEmpty()) return@withContext Result.success()

        val rows = mutableListOf<Triple<String, String, Int>>()
        for (line in body) {
            if (line.isBlank()) continue
            val parts = line.split(",")
            if (parts.size < 2) continue
            val date = normalizeDate(parts[0].trim()) ?: continue
            val unlocks = parts[1].trim().toIntOrNull() ?: continue
            val recordId = "${DEVICE_ID}-$date-unlocks"
            rows.add(Triple(recordId, date, unlocks))
        }
        if (rows.isEmpty()) return@withContext Result.success()

        val header = "record_id,participant_id,date,feature_schema_version,daily_unlocks\n"
        val csvSb = StringBuilder(header)
        rows.sortedBy { it.second }.forEach { (recId, date, ct) ->
            csvSb.append(recId).append(",")
                .append(DEVICE_ID).append(",")
                .append(date).append(",")
                .append(SCHEMA_VERSION_UNLOCKS).append(",")
                .append(ct).append("\n")
        }
        val csv = csvSb.toString()

        logSchemaVersion(INSTR_UNLOCKS, SCHEMA_VERSION_UNLOCKS)
        writeEncrypted(File(filesDir, "daily_metrics_upload.csv"), csv)
        RollupValidator.validateUnlocks(ctx)

        try {
            val qf = File(filesDir, QUEUE_FILE)
            ensureQueueHeader(qf, QUEUE_HEADER)
            upsertUnlocksQueue(qf, DEVICE_ID, rows)
        } catch (_: Throwable) {}

        val urlRaw = BuildConfig.REDCAP_URL
        val token = BuildConfig.REDCAP_TOKEN
        if (urlRaw.isBlank() || token.isBlank()) return@withContext Result.retry()

        val url = if (urlRaw.endsWith("/")) urlRaw else "$urlRaw/"
        val host = try { URI(url).host.orEmpty() } catch (_: Throwable) { "" }
        try { InetAddress.getByName(host) } catch (_: Throwable) { return@withContext Result.retry() }

        val hash = sha256(csv)
        val lastHash = prefs.getString("hash:daily_unlocks", null)
        if (lastHash != null && lastHash == hash) return@withContext Result.success()

        val form = FormBody.Builder()
            .add("token", token)
            .add("content", "record")
            .add("action", "import")
            .add("format", "csv")
            .add("type", "flat")
            .add("overwriteBehavior", "overwrite")
            .add("returnContent", "count")
            .add("returnFormat", "json")
            .add("dateFormat", "YMD")
            .add("data", csv)
            .build()

        val req = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "MyRecoveryTracker/1.0 (Android)")
            .post(form)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        var ok = false
        try {
            client.newCall(req).execute().use { resp ->
                ok = resp.isSuccessful
                val bodyStr = resp.body?.string().orEmpty()
                if (ok) {
                    writeReceipt(File(filesDir, RECEIPTS_FILE), rows.size, bodyStr)
                    prefs.edit().putString("hash:daily_unlocks", hash).apply()
                }
            }
        } catch (_: Throwable) {}

        if (ok) Result.success() else Result.retry()
    }

    private fun ensureQueueHeader(f: File, header: String) {
        if (!f.exists() || f.length() == 0L) f.writeText(header + "\n")
        else {
            val first = f.bufferedReader().use { it.readLine() } ?: ""
            if (first != header) {
                val body = f.readLines().drop(1).joinToString("\n")
                val tmp = File(f.parentFile, f.name + ".tmp")
                tmp.writeText(header + "\n" + body + if (body.isNotEmpty()) "\n" else "")
                f.delete()
                tmp.renameTo(f)
            }
        }
    }

    private fun upsertUnlocksQueue(queue: File, participantId: String, rows: List<Triple<String, String, Int>>) {
        val map = LinkedHashMap<String, List<String>>()
        if (queue.exists()) {
            queue.readLines().drop(1).forEach { line ->
                if (line.isBlank()) return@forEach
                val cols = line.split(',')
                if (cols.size < 5) return@forEach
                val key = "${cols[0]}|${cols[2]}"
                map[key] = cols
            }
        }
        for ((_, date, unlocks) in rows) {
            val cols = listOf(INSTR_UNLOCKS, participantId, date, SCHEMA_VERSION_UNLOCKS.toString(), unlocks.toString())
            map["$INSTR_UNLOCKS|$date"] = cols
        }
        val tmp = File(queue.parentFile, queue.name + ".tmp")
        tmp.writeText(QUEUE_HEADER + "\n")
        map.values.sortedWith(compareBy({ it[0] }, { it[2] })).forEach { cols ->
            tmp.appendText(cols.joinToString(",") + "\n")
        }
        if (queue.exists()) queue.delete()
        tmp.renameTo(queue)
    }

    private fun writeEncrypted(file: File, plain: String) {
        try {
            val salt = "MyRecoverySalt".toByteArray()
            val spec = PBEKeySpec("local-pass".toCharArray(), salt, 10000, 256)
            val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
            val secret = SecretKeySpec(key.encoded, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secret)
            val iv = cipher.iv
            val enc = cipher.doFinal(plain.toByteArray())
            val blob = Base64.encodeToString(iv + enc, Base64.NO_WRAP)
            file.writeText(blob)
        } catch (_: Throwable) { file.writeText(plain) }
    }

    private fun looksLikeHeader(firstLine: String): Boolean {
        val s = firstLine.lowercase(Locale.US)
        return s.startsWith("date,") || s.startsWith("ts,") || s.startsWith("day,")
    }
    private fun normalizeDate(raw: String): String? {
        val m = DATE_PREFIX.find(raw) ?: return null
        return m.groupValues[1]
    }
    private fun sha256(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
    }
    private fun writeReceipt(file: File, rows: Int, serverBody: String) {
        try {
            if (!file.exists()) file.writeText("ts,endpoint,rows,http_code,note\n")
            val ts = TS.format(System.currentTimeMillis())
            val safe = serverBody.replace(",", " ").replace("\n", " ").take(200)
            file.appendText("$ts,record_import,$rows,200,$safe\n")
        } catch (_: Throwable) {}
    }

    private fun logSchemaVersion(feature: String, version: Int) {
        try {
            val f = File(ctx.filesDir, SCHEMA_LOG_FILE)
            if (!f.exists()) f.writeText("ts,feature,version\n")
            val prefs = ctx.getSharedPreferences("schema_versions", Context.MODE_PRIVATE)
            val key = "v:$feature"
            val last = prefs.getInt(key, -1)
            if (last != version) {
                val ts = TS.format(System.currentTimeMillis())
                f.appendText("$ts,$feature,$version\n")
                prefs.edit().putInt(key, version).apply()
            }
        } catch (_: Throwable) {}
    }

    companion object {
        private const val DEVICE_ID = "TEST-DEVICE"
        private val DATE_PREFIX = Regex("""^(\d{4}-\d{2}-\d{2})""")
        private val TS = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        private const val RECEIPTS_FILE = "redcap_receipts.csv"
        private const val INSTR_UNLOCKS = "daily_unlocks"
        private const val SCHEMA_VERSION_UNLOCKS = 1
        private const val QUEUE_FILE = "redcap_queue.csv"
        private const val QUEUE_HEADER = "instrument,participant_id,date,schema_version,unlocks"
        private const val SCHEMA_LOG_FILE = "schema_versions.csv"
    }
}