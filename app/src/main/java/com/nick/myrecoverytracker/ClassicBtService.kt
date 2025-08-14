package com.nick.myrecoverytracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Classic discovery with BLE PendingIntent fallback.
 * CSV: files/bluetooth_log.csv -> timestamp,source,event,addr,name,rssi
 */
class ClassicBtService : Service() {

    private val tag = "ClassicBtService"
    private val h = Handler(Looper.getMainLooper())
    private var registered = false
    private var foreground = false

    private val classicRx = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val dev: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val rssi: Short =
                        intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                    writeEvent(event = "classic_found", addr = dev?.address, name = dev?.name, rssi = rssi)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    writeEvent(event = "classic_finished")
                    Log.i(tag, "Classic discovery finished")
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureForeground()
        when (intent?.action) {
            ACTION_START -> {
                writeEvent(event = "service_started")
                startScanWindow(35_000L)
            }
            ACTION_STOP -> stopSelf()
            else -> Log.i(tag, "onStartCommand with no action; keeping service alive briefly")
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            (getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
                ?.adapter?.let { if (it.isDiscovering) it.cancelDiscovery() }
        } catch (_: Throwable) { /* no-op */ }

        if (registered) {
            try { unregisterReceiver(classicRx) } catch (_: Throwable) { /* no-op */ }
            registered = false
        }
        h.removeCallbacksAndMessages(null)
        writeEvent(event = "service_stopped")
        Log.i(tag, "STOP")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ---- scanning ----

    private fun startScanWindow(windowMs: Long) {
        val startedClassic = tryClassicDiscovery()
        if (!startedClassic) {
            val startedBle = tryBlePendingIntentScan()
            Log.i(tag, "Fallback BLE PI scan start=$startedBle; window=${windowMs}ms")
            if (!startedBle) {
                Log.w(tag, "Neither Classic nor BLE PI could start; stopping")
                stopSelf(); return
            }
        }
        h.postDelayed({ stopSelf() }, windowMs)
    }

    private fun tryClassicDiscovery(): Boolean {
        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        val adapter: BluetoothAdapter? = bm?.adapter
        val locOn = isLocationToggleOn()
        val hasScan = hasPerm(android.Manifest.permission.BLUETOOTH_SCAN)
        val hasConnect = hasPerm(android.Manifest.permission.BLUETOOTH_CONNECT)

        Log.i(tag, "Classic precheck: enabled=${adapter?.isEnabled} locOn=$locOn scanPerm=$hasScan connectPerm=$hasConnect")
        if (adapter == null || !adapter.isEnabled || !hasScan) return false

        return try {
            if (!registered) {
                val f = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                registerReceiver(classicRx, f)
                registered = true
            }
            if (adapter.isDiscovering) adapter.cancelDiscovery()
            val ok = adapter.startDiscovery()
            Log.i(tag, "Classic discovery START: $ok")
            ok
        } catch (t: Throwable) {
            Log.e(tag, "Classic start failed", t)
            false
        }
    }

    private fun tryBlePendingIntentScan(): Boolean {
        val scanner = getScanner() ?: return false
        return try {
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
            val filters: List<ScanFilter>? = null
            scanner.stopScan(resultPi()) // clean previous
            scanner.startScan(filters, settings, resultPi())
            Log.i(tag, "BLE PI scan STARTED")
            true
        } catch (t: Throwable) {
            Log.e(tag, "BLE PI start failed", t)
            false
        }
    }

    private fun getScanner(): BluetoothLeScanner? {
        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        val adapter: BluetoothAdapter? = bm?.adapter
        if (adapter == null || !adapter.isEnabled) {
            Log.w(tag, "BLE: adapter unavailable/disabled")
            return null
        }
        if (!hasPerm(android.Manifest.permission.BLUETOOTH_SCAN)) {
            Log.w(tag, "BLE: missing BLUETOOTH_SCAN permission")
            return null
        }
        return adapter.bluetoothLeScanner
    }

    private fun resultPi(): PendingIntent {
        val action = "${packageName}.BLE_SCAN_RESULT"
        val intent = Intent(this, BleScanReceiver::class.java).setAction(action)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(this, 2002, intent, flags)
    }

    // ---- permissions/helpers ----

    private fun hasPerm(p: String) =
        checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED

    private fun isLocationToggleOn(): Boolean = try {
        val lm = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        lm.isLocationEnabled
    } catch (_: Throwable) { false }

    // ---- file I/O ----

    private fun writeEvent(event: String, addr: String? = null, name: String? = null, rssi: Short? = null) {
        val f = File(filesDir, "bluetooth_log.csv")
        if (!f.exists()) f.writeText("timestamp,source,event,addr,name,rssi\n")
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val a = (addr ?: "")
        val n = (name ?: "").replace(",", " ")
        val r = rssi?.toString() ?: ""
        try {
            f.appendText("$ts,classic,$event,$a,$n,$r\n")
        } catch (t: Throwable) {
            Log.e(tag, "write failed", t)
        }
    }

    // ---- foreground notification (use CONNECTED_DEVICE, not LOCATION) ----

    private fun ensureForeground() {
        if (foreground) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bt_scan"
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(channelId, "Bluetooth scanning", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
        val notif: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle("MyRecoveryTracker")
            .setContentText("Scanning for nearby devices…")
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                42,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(42, notif)
        }
        foreground = true
    }

    companion object {
        const val ACTION_START = "com.nick.myrecoverytracker.bt.CLASSIC_START"
        const val ACTION_STOP  = "com.nick.myrecoverytracker.bt.CLASSIC_STOP"
    }
}