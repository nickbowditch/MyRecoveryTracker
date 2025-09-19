package com.nick.myrecoverytracker

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class RedcapClient(
    private val apiUrl: String,
    private val token: String
) {

    data class Response(val code: Int, val ok: Boolean, val body: String)

    fun postRecords(jsonArrayPayload: String): Response {
        // REDCap expects x-www-form-urlencoded body
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
            val body = if (code in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
            }
            Response(code, code in 200..299, body)
        } catch (t: Throwable) {
            Response(code, false, t.message ?: "error")
        } finally {
            conn.disconnect()
        }
    }

    private fun enc(s: String): String =
        URLEncoder.encode(s, Charsets.UTF_8.name())
}