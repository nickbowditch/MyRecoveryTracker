package com.nick.myrecoverytracker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Tiny disk-backed retry queue at files/retry_queue.json
 *
 * Each entry:
 * {
 *   "id": "2025-08-19",       // unique key (we use the day)
 *   "payload": "...",         // RAW JSON STRING for REDCap POST body (e.g., "[{...}]")
 *   "attempts": 0,            // attempts made
 *   "next_at": 0              // epoch millis when eligible to retry
 * }
 */
class RetryQueue(private val context: Context) {
    private val lock = ReentrantLock()
    private val file = File(context.filesDir, "retry_queue.json")

    private fun read(): JSONArray = lock.withLock {
        if (!file.exists()) return JSONArray()
        val txt = runCatching { file.readText() }.getOrNull().orEmpty()
        if (txt.isBlank()) JSONArray() else runCatching { JSONArray(txt) }.getOrElse { JSONArray() }
    }

    private fun write(arr: JSONArray) = lock.withLock {
        file.parentFile?.mkdirs()
        file.writeText(arr.toString())
    }

    /** Store/replace by id; payload is the raw JSON string we POST to REDCap. */
    fun addOrReplace(id: String, payload: String) {
        val arr = read()
        var replaced = false
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("id") == id) {
                o.put("payload", payload)
                o.put("attempts", 0)
                o.put("next_at", 0)
                replaced = true
                break
            }
        }
        if (!replaced) {
            arr.put(
                JSONObject()
                    .put("id", id)
                    .put("payload", payload) // keep as String
                    .put("attempts", 0)
                    .put("next_at", 0)
            )
        }
        trim(arr, 200)
        write(arr)
    }

    /** Return copies of entries that are ready to retry now (up to [limit]). */
    fun peekReady(limit: Int = 10): List<JSONObject> {
        val now = System.currentTimeMillis()
        val arr = read()
        val out = ArrayList<JSONObject>(limit)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optLong("next_at", 0) <= now) {
                out.add(JSONObject(o.toString())) // defensive copy
                if (out.size >= limit) break
            }
        }
        return out
    }

    /** Mark a given id as success (remove) or failure (backoff + reschedule). */
    fun markResult(id: String, ok: Boolean) {
        val arr = read()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("id") == id) {
                if (ok) {
                    val newArr = JSONArray()
                    for (j in 0 until arr.length()) if (j != i) newArr.put(arr.get(j))
                    write(newArr)
                } else {
                    val attempts = o.optInt("attempts", 0) + 1
                    val delayMin = when (attempts) {
                        1 -> 15    // 15m
                        2 -> 60    // 1h
                        3 -> 180   // 3h
                        4 -> 360   // 6h
                        5 -> 720   // 12h
                        else -> 1440 // 24h cap
                    }
                    o.put("attempts", attempts)
                    o.put("next_at", System.currentTimeMillis() + delayMin * 60_000L)
                    write(arr)
                }
                return
            }
        }
    }

    private fun trim(arr: JSONArray, max: Int) {
        if (arr.length() <= max) return
        val newArr = JSONArray()
        val start = arr.length() - max
        for (i in start until arr.length()) newArr.put(arr.get(i))
        write(newArr)
    }

    companion object {
        fun netOk(ctx: Context): Boolean {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        }

        fun logUpload(ctx: Context, code: Int, ok: Boolean, bodySnippet: String) {
            val f = File(ctx.filesDir, "redcap_upload_log.csv")
            if (!f.exists()) {
                f.parentFile?.mkdirs()
                f.writeText("date,http_code,ok,body_snippet\n")
            }
            val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val snip = bodySnippet.replace("\n", " ").take(200)
            f.appendText("$day,$code,$ok,$snip\n")
        }
    }
}