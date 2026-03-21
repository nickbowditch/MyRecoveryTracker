package com.nick.myrecoverytracker

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object RedcapClient {
    private const val TAG = "RedcapClient"

    data class UploadResponse(
        val code: Int,
        val isSuccessful: Boolean,
        private val _body: String
    ) {
        fun body() = _body
    }

    fun upload(payload: Map<String, String>): UploadResponse {
        return try {
            val baseUrl = RedcapApiClient.getConfigString("REDCAP_BASE_URL") ?: run {
                Log.w(TAG, "REDCAP_BASE_URL not configured")
                return UploadResponse(0, false, "Missing REDCAP_BASE_URL")
            }

            val token = RedcapApiClient.getConfigString("REDCAP_API_TOKEN") ?: run {
                Log.w(TAG, "REDCAP_API_TOKEN not configured")
                return UploadResponse(0, false, "Missing REDCAP_API_TOKEN")
            }

            val jsonPayload = mapToJson(payload)
            Log.d(TAG, "Uploading ${payload.size} fields to REDCap")

            postRecords(baseUrl, token, jsonPayload)
        } catch (t: Throwable) {
            Log.e(TAG, "Upload exception", t)
            UploadResponse(0, false, t.message ?: "Unknown error")
        }
    }

    private fun postRecords(apiUrl: String, token: String, jsonArrayPayload: String): UploadResponse {
        val params = buildString {
            append("token=").append(enc(token))
            append("&content=record")
            append("&format=json")
            append("&type=flat")
            append("&overwriteBehavior=normal")
            append("&data=").append(enc(jsonArrayPayload))
        }

        val url = URL(apiUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 30000
        }

        var code = -1
        return try {
            DataOutputStream(conn.outputStream).use { it.writeBytes(params) }
            code = conn.responseCode
            val isOk = code in 200..299
            val body = if (isOk) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
            }
            Log.i(TAG, "REDCap response: HTTP $code")
            UploadResponse(code, isOk, body)
        } catch (t: Throwable) {
            Log.e(TAG, "POST failed", t)
            UploadResponse(code, false, t.message ?: "error")
        } finally {
            conn.disconnect()
        }
    }

    private fun mapToJson(map: Map<String, String>): String {
        val items = map.entries.joinToString(",") { (k, v) ->
            "\"$k\":\"${escapeJson(v)}\""
        }
        return "[$items]"
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun enc(s: String): String {
        return URLEncoder.encode(s, "UTF-8")
    }
}