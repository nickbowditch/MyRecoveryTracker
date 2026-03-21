// app/src/main/java/com/nick/myrecoverytracker/RedcapDiag.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object RedcapDiag {
    private const val TAG = "RedcapDiag"

    private fun getBuildConfigString(context: Context, field: String): String? {
        return try {
            val cls = Class.forName("${context.packageName}.BuildConfig")
            val f = cls.getDeclaredField(field)
            f.isAccessible = true
            (f.get(null) as? String)?.takeIf { it.isNotBlank() }
        } catch (_: Throwable) {
            null
        }
    }

    private fun getStringRes(context: Context, name: String): String? {
        return try {
            val id = context.resources.getIdentifier(name, "string", context.packageName)
            if (id != 0) context.getString(id).takeIf { it.isNotBlank() } else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun readAssetIfPresent(context: Context, name: String): Pair<Boolean, Int> {
        return try {
            context.assets.open(name).use { s ->
                val size = s.available()
                true to size
            }
        } catch (_: Throwable) {
            false to 0
        }
    }

    private fun readRawIfPresent(context: Context, name: String): Pair<Boolean, Int> {
        return try {
            val id = context.resources.getIdentifier(name, "raw", context.packageName)
            if (id == 0) return false to 0
            context.resources.openRawResource(id).use { s ->
                val size = s.available()
                true to size
            }
        } catch (_: Throwable) {
            false to 0
        }
    }

    fun log(context: Context) {
        try {
            val baseUrl = getBuildConfigString(context, "REDCAP_BASE_URL")
                ?: getStringRes(context, "redcap_base_url")
            val apiToken = getBuildConfigString(context, "REDCAP_API_TOKEN")
                ?: getStringRes(context, "redcap_api_token")
            val projectId = getBuildConfigString(context, "REDCAP_PROJECT_ID")
                ?: getStringRes(context, "redcap_project_id")

            val hasBaseUrl = !baseUrl.isNullOrBlank()
            val hasToken = !apiToken.isNullOrBlank()
            val hasProject = !projectId.isNullOrBlank()

            Log.i(TAG, "Env present -> baseUrl=$hasBaseUrl, apiToken=$hasToken, projectId=$hasProject")

            val (assetOk, assetSize) = readAssetIfPresent(context, "redcap_mapping.json")
            val (rawOk, rawSize) = readRawIfPresent(context, "redcap_mapping")

            val present = assetOk || rawOk
            val source = when {
                assetOk -> "assets/redcap_mapping.json"
                rawOk -> "raw/redcap_mapping"
                else -> "none"
            }
            val size = if (assetOk) assetSize else if (rawOk) rawSize else 0

            Log.i(TAG, "Mapping present -> $present source=$source size=$size")

            // Write redcap_diag.csv
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC+11")
            }
            val timestamp = sdf.format(Date())

            val dataDir = File(context.getExternalFilesDir(null), "data")
            dataDir.mkdirs()

            val diagFile = File(dataDir, "redcap_diag.csv")
            val header = "ts,env_base_url,env_api_token,env_project_id,mapping_present,mapping_source,mapping_size_bytes"
            val row = "$timestamp,$hasBaseUrl,$hasToken,$hasProject,$present,$source,$size"

            diagFile.writeText("$header\n$row\n")
            Log.i(TAG, "✅ redcap_diag.csv written")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error writing redcap_diag.csv", e)
        }
    }
}