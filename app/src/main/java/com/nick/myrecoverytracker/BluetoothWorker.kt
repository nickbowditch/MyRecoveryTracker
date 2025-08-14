package com.nick.myrecoverytracker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class BluetoothWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.i("BluetoothWorker", "🧠 Starting BluetoothWorker")

        val intent = Intent(applicationContext, BluetoothScanService::class.java)

        try {
            applicationContext.startForegroundService(intent)
            Log.i("BluetoothWorker", "📡 BluetoothScanService started")
        } catch (e: Exception) {
            Log.e("BluetoothWorker", "❌ Failed to start BluetoothScanService", e)
            return Result.failure()
        }

        return Result.success()
    }
}