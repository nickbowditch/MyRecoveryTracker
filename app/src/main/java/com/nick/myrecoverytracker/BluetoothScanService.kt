package com.nick.myrecoverytracker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Foreground service that scans BLE advertisements and appends lines to:
 *   files/bluetooth_log.csv
 * Format:
 *   YYYY-MM-DD HH:mm:ss,<MAC>,<RSSI>,<name_or_empty>
 *
 * Start/stop via TestWorkersReceiver actions:
 *   - com.nick.myrecoverytracker.TEST_BT_START
 *   - com.nick.myrecoverytracker.TEST_BT_STOP
 */
class BluetoothScanService : Service() {

    private var adapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private var started = false

    private val scanCb = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val addr = result.device?.address ?: return
            val rssi = result.rssi
            val name = result.device?.name?.sanitizeCsv() ?: ""
            appendLog(addr, rssi, name)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan failed: $errorCode")
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotif("Scanning nearby Bluetooth…"))
        Log.i(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Guard: permissions
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN) ||
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.w(TAG, "Missing BLUETOOTH_SCAN or ACCESS_FINE_LOCATION; stopping")
            stopSelf()
            return START_NOT_STICKY
        }

        // Guard: adapter
        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        adapter = bm?.adapter
        if (adapter == null || adapter?.isEnabled != true) {
            Log.w(TAG, "Bluetooth disabled or unavailable; stopping")
            stopSelf()
            return START_NOT_STICKY
        }

        // Start scanning once
        if (!started) {
            scanner = adapter!!.bluetoothLeScanner
            if (scanner == null) {
                Log.w(TAG, "No BluetoothLeScanner; stopping")
                stopSelf()
                return START_NOT_STICKY
            }
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
            try {
                scanner!!.startScan(/* filters = */ null, settings, scanCb)
                started = true
                Log.i(TAG, "BLE scan started")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to start BLE scan", t)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        try {
            scanner?.stopScan(scanCb)
            Log.i(TAG, "BLE scan stopped")
        } catch (_: Throwable) { }
        stopForeground(STOP_FOREGROUND_DETACH)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ---------- helpers ----------

    private fun hasPermission(p: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    }

    private fun appendLog(address: String, rssi: Int, name: String) {
        try {
            val file = File(filesDir, "bluetooth_log.csv")
            FileWriter(file, /* append = */ true).use { w ->
                val ts = TS.format(Date())
                w.appendLine("$ts,$address,$rssi,\"$name\"")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write bluetooth_log.csv", e)
        }
    }

    private fun String.sanitizeCsv(): String {
        // Keep it simple; quotes are handled by wrapping with ""
        return this.replace("\n", " ").replace("\r", " ")
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(CHANNEL_ID, "Bluetooth scanning", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Foreground service for Bluetooth scanning"
                setShowBadge(false)
            }
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotif(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle("MyRecoveryTracker")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        private const val TAG = "BluetoothScanService"
        private const val CHANNEL_ID = "bt_scan"
        private const val NOTIF_ID = 42
        private val TS = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}