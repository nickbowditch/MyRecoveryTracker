package com.nick.myrecoverytracker

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Classic Bluetooth discovery fallback. Logs rows to bluetooth_log.csv:
 * yyyy-MM-dd HH:mm:ss,ADDR,NAME,RSSI
 */
object ClassicBtLogger {

    private const val TAG = "ClassicBtLogger"
    private var registered = false
    private val main = Handler(Looper.getMainLooper())

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val rssi: Short = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                    val addr = device?.address ?: "UNKNOWN"
                    val name = (device?.name ?: "").replace(",", " ") // keep CSV clean
                    val ts = now()
                    try {
                        val f = File(context.filesDir, "bluetooth_log.csv")
                        f.appendText("$ts,$addr,$name,$rssi\n")
                        Log.i(TAG, "FOUND $addr ($name) RSSI=$rssi")
                    } catch (t: Throwable) {
                        Log.e(TAG, "write bluetooth_log.csv failed", t)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(TAG, "Classic discovery finished")
                    stop(context)
                }
            }
        }
    }

    fun start(context: Context, windowMs: Long = 30_000L) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            Log.w(TAG, "Bluetooth adapter unavailable/disabled")
            return
        }
        try {
            if (!registered) {
                val f = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                context.registerReceiver(receiver, f)
                registered = true
            }

            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            val ok = adapter.startDiscovery()
            Log.i(TAG, "Classic discovery START: $ok (timeout ${windowMs}ms)")

            // Hard stop after window to avoid long discovery
            main.postDelayed({ stop(context) }, windowMs)
        } catch (t: Throwable) {
            Log.e(TAG, "Classic discovery start failed", t)
        }
    }

    fun stop(context: Context) {
        try {
            BluetoothAdapter.getDefaultAdapter()?.let { a ->
                if (a.isDiscovering) a.cancelDiscovery()
            }
        } catch (_: Throwable) { /* ignore */ }

        if (registered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Throwable) { /* ignore */ }
            registered = false
        }
        main.removeCallbacksAndMessages(null)
        Log.i(TAG, "Classic discovery STOP")
    }

    private fun now(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
}