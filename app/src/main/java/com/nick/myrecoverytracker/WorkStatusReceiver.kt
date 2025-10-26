package com.nick.myrecoverytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        val tag = intent.getStringExtra(EXTRA_TAG) ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val infos = WorkManager.getInstance(context)
                    .getWorkInfosByTag(tag).get()

                val runningOrQueued = infos.any { !it.state.isFinished }
                val states = infos.joinToString { it.state.name }

                Log.i(LOGTAG, "tag=$tag runningOrQueued=$runningOrQueued states=[$states] count=${infos.size}")
            } catch (t: Throwable) {
                Log.e(LOGTAG, "Failed to query WorkManager for tag=$tag", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION = "com.nick.myrecoverytracker.ACTION_DUMP_WORK"
        const val EXTRA_TAG = "tag"
        private const val LOGTAG = "WORKSTATUS"
    }
}