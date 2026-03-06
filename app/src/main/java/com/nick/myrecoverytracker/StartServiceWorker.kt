package com.nick.myrecoverytracker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class StartServiceWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("StartServiceWorker", "Starting ForegroundUnlockService")

        val intent = Intent(applicationContext, ForegroundUnlockService::class.java)
        ContextCompat.startForegroundService(applicationContext, intent)

        return Result.success()
    }
}