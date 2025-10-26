// app/src/main/java/com/nick/myrecoverytracker/RedcapEnv.kt
package com.nick.myrecoverytracker

import android.content.Context

object RedcapEnv {

    private fun buildConfigString(context: Context, field: String): String? {
        return try {
            val cls = Class.forName("${context.packageName}.BuildConfig")
            val f = cls.getDeclaredField(field)
            f.isAccessible = true
            (f.get(null) as? String)?.takeIf { it.isNotBlank() }
        } catch (_: Throwable) {
            null
        }
    }

    private fun stringRes(context: Context, name: String): String? {
        return try {
            val id = context.resources.getIdentifier(name, "string", context.packageName)
            if (id != 0) context.getString(id).takeIf { it.isNotBlank() } else null
        } catch (_: Throwable) {
            null
        }
    }

    fun baseUrl(context: Context): String? {
        val v = buildConfigString(context, "REDCAP_BASE_URL")
            ?: stringRes(context, "redcap_base_url")
        return v
    }

    fun apiToken(context: Context): String? {
        val v = buildConfigString(context, "REDCAP_API_TOKEN")
            ?: stringRes(context, "redcap_api_token")
        return v
    }

    fun projectId(context: Context): String? {
        val v = buildConfigString(context, "REDCAP_PROJECT_ID")
            ?: stringRes(context, "redcap_project_id")
        return v
    }

    fun isConfigured(context: Context): Boolean {
        return !baseUrl(context).isNullOrBlank() &&
                !apiToken(context).isNullOrBlank() &&
                !projectId(context).isNullOrBlank()
    }
}