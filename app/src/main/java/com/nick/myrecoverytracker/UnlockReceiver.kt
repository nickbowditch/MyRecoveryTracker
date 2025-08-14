package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_USER_PRESENT || action == ACTION_TEST_UNLOCK) {
            val prefs = context.getSharedPreferences("unlock_prefs", Context.MODE_PRIVATE)
            val last = prefs.getLong(KEY_LAST_UNLOCK_AT, 0L)
            val now = System.currentTimeMillis()

            if (now - last >= 5_000) { // 5s de-dupe
                MetricsStore.saveUnlock(context)
                prefs.edit().putLong(KEY_LAST_UNLOCK_AT, now).apply()
            }
        }
    }

    companion object {
        private const val KEY_LAST_UNLOCK_AT = "last_unlock_at"
        const val ACTION_TEST_UNLOCK = "com.nick.myrecoverytracker.TEST_UNLOCK"
    }
}