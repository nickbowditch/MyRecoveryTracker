package com.nick.mylibrary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager

class TriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i("TriggerReceiver", "onReceive action=$action")

        // Stub example to allow compilation
        enqueueOnceStub(context, "TestTag", "TestUnique")
    }

    private fun enqueueOnceStub(
        context: Context,
        tag: String,
        uniqueName: String
    ) {
        val req: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<StubWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag(tag)
                .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, req)
        Log.i("TriggerReceiver", "Enqueued $tag ($uniqueName)")
    }
}

// Minimal stub to satisfy ListenableWorker generic
class StubWorker : ListenableWorker() {
    override fun startWork() = null
}