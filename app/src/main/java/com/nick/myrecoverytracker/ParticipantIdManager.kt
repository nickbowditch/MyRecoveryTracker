package com.nick.myrecoverytracker

import android.content.Context
import java.util.UUID

object ParticipantIdManager {

    private const val PREFS = "participant_id"
    private const val KEY = "participant_id"

    fun getOrCreate(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY, null)
        if (existing != null) return existing

        val id = "MYRA-" + UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .uppercase()

        prefs.edit().putString(KEY, id).apply()
        return id
    }
}