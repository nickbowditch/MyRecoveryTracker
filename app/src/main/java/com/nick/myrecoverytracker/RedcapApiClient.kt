// app/src/main/java/com/nick/myrecoverytracker/RedcapApiClient.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object RedcapApiClient {
    private const val TAG = "RedcapApiClient"

    private fun getConfigString(context: Context, field: String): String? {
        return try {
            val cls = Class.forName("${context.packageName}.BuildConfig")
            val f = cls.getDeclaredField(field)
            f.isAccessible = true
            (f.get(null) as? String)?.takeIf { it.isNotBlank() }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Validates the REDCap API token by calling the REDCap version endpoint.
     * Returns true if token is valid, false otherwise.
     */
    fun validateToken(context: Context): Boolean {
        val baseUrl = getConfigString(context, "REDCAP_BASE_URL") ?: run {
            Log.w(TAG, "REDCAP_BASE_URL not configured")
            return false
        }

        val token = getConfigString(context, "REDCAP_API_TOKEN") ?: run {
            Log.w(TAG, "REDCAP_API_TOKEN not configured")
            return false
        }

        val projectId = getConfigString(context, "REDCAP_PROJECT_ID") ?: run {
            Log.w(TAG, "REDCAP_PROJECT_ID not configured")
            return false
        }

        return try {
            val client = OkHttpClient()
            val body = FormBody.Builder()
                .add("token", token)
                .add("content", "version")
                .add("format", "json")
                .build()

            val request = Request.Builder()
                .url(baseUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful

            if (success) {
                val version = response.body?.string() ?: "unknown"
                Log.i(TAG, "✅ REDCap token validated. Version: $version")
            } else {
                Log.w(TAG, "⚠️ REDCap token validation failed. HTTP ${response.code}: ${response.message}")
            }

            response.close()
            success
        } catch (e: IOException) {
            Log.e(TAG, "REDCap token validation error (likely network issue)", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "REDCap token validation error", e)
            false
        }
    }

    /**
     * Tests network reachability to REDCap server.
     * Returns true if server is reachable, false otherwise.
     */
    fun testReachability(context: Context): Boolean {
        val baseUrl = getConfigString(context, "REDCAP_BASE_URL") ?: run {
            Log.w(TAG, "REDCAP_BASE_URL not configured for reachability test")
            return false
        }

        return try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(baseUrl)
                .head()
                .build()

            val response = client.newCall(request).execute()
            val reachable = response.isSuccessful || response.code == 401 || response.code == 403

            if (reachable) {
                Log.i(TAG, "✅ REDCap server reachable. HTTP ${response.code}")
            } else {
                Log.w(TAG, "⚠️ REDCap server unreachable. HTTP ${response.code}: ${response.message}")
            }

            response.close()
            reachable
        } catch (e: IOException) {
            Log.e(TAG, "❌ REDCap reachability test failed (network error)", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ REDCap reachability test error", e)
            false
        }
    }
}