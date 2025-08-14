package com.nick.myrecoverytracker

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat

class WiFiScanService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("WiFiScanService", "📡 WiFi scan service started")

        if (!isPermissionGranted()) {
            Log.w("WiFiScanService", "❌ ACCESS_FINE_LOCATION not granted")
            stopSelf()
            return START_NOT_STICKY
        }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val scanResults: List<ScanResult> = try {
            wifiManager.scanResults
        } catch (e: SecurityException) {
            Log.e("WiFiScanService", "❌ SecurityException: ${e.message}")
            stopSelf()
            return START_NOT_STICKY
        }

        val distinctSSIDs = scanResults
            .mapNotNull { it.SSID }
            .filter { it.isNotBlank() }
            .distinct()

        Log.i("WiFiScanService", "📶 Found ${distinctSSIDs.size} unique SSIDs:")
        distinctSSIDs.forEach {
            Log.i("WiFiScanService", "🔹 $it")
        }

        stopSelf() // short-lived service
        return START_NOT_STICKY
    }

    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onBind(intent: Intent?): IBinder? = null
}