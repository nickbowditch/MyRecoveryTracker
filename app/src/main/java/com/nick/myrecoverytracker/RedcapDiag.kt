// app/src/main/java/com/nick/myrecoverytracker/RedcapDiag.kt
package com.nick.myrecoverytracker

import android.content.Context
import android.util.Log

object RedcapDiag {
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
        // Try BuildConfig first, then strings.xml fallbacks
        val baseUrl = getBuildConfigString(context, "REDCAP_BASE_URL")
            ?: getStringRes(context, "redcap_base_url")
        val apiToken = getBuildConfigString(context, "REDCAP_API_TOKEN")
            ?: getStringRes(context, "redcap_api_token")
        val projectId = getBuildConfigString(context, "REDCAP_PROJECT_ID")
            ?: getStringRes(context, "redcap_project_id")

        val hasBaseUrl = !baseUrl.isNullOrBlank()
        val hasToken = !apiToken.isNullOrBlank()
        val hasProject = !projectId.isNullOrBlank()

        Log.i("RedcapDiag", "Env present -> baseUrl=$hasBaseUrl, apiToken=$hasToken, projectId=$hasProject")

        // mapping: prefer assets/redcap_mapping.json, else res/raw/redcap_mapping
        val (assetOk, assetSize) = readAssetIfPresent(context, "redcap_mapping.json")
        val (rawOk, rawSize) = readRawIfPresent(context, "redcap_mapping")

        val present = assetOk || rawOk
        val source = when {
            assetOk -> "assets/redcap_mapping.json"
            rawOk -> "raw/redcap_mapping"
            else -> "none"
        }
        val size = if (assetOk) assetSize else if (rawOk) rawSize else 0

        Log.i("RedcapDiag", "Mapping present -> $present source=$source size=$size")
    }
}