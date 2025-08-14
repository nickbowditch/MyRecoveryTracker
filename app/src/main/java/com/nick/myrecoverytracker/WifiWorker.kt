package com.nick.myrecoverytracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class WifiWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i("WifiWorker", "📶 Starting WiFi scan...")

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (wifiManager == null) {
            Log.e("WifiWorker", "❌ WifiManager is null")
            return Result.failure()
        }

        if (!wifiManager.isWifiEnabled) {
            Log.w("WifiWorker", "📵 WiFi is OFF — please enable manually")
            return Result.retry()
        }

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("WifiWorker", "❌ ACCESS_FINE_LOCATION not granted")
            return Result.retry()
        }

        val scanSuccess = wifiManager.startScan()
        Log.i("WifiWorker", if (scanSuccess) "📡 Scan initiated" else "⚠️ Scan failed to start")

        delay(3000) // wait for results

        val results = wifiManager.scanResults
        if (results.isEmpty()) {
            Log.w("WifiWorker", "🛑 No scan results (location off or restricted?)")
            return Result.retry()
        }

        results.forEach {
            val ssid = it.SSID
            val bssid = it.BSSID
            val rssi = it.level
            Log.i("WifiWorker", "🔹 SSID: $ssid, BSSID: $bssid, RSSI: ${rssi}dBm")
            MetricsStore.saveWifiNetworksLog(applicationContext, ssid, bssid, rssi)
        }

        Log.i("WifiWorker", "✅ WiFi scan and log complete")
        return Result.success()
    }
}