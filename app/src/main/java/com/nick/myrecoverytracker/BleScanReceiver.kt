package com.nick.myrecoverytracker

import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BleScanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tag = "BleScanReceiver"
        try {
            val results = intent.getParcelableArrayListExtra<Parcelable>(
                "android.bluetooth.le.extra.LIST_SCAN_RESULT"
            )?.mapNotNull { it as? ScanResult } ?: emptyList()

            if (results.isEmpty()) {
                Log.i(tag, "No BLE results in this batch")
                return
            }

            val f = File(context.filesDir, "bluetooth_log.csv")
            if (!f.exists()) f.writeText("timestamp,source,event,addr,name,rssi\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val sb = StringBuilder()
            for (r in results) {
                val addr = r.device?.address ?: "UNKNOWN"
                val name = (r.device?.name ?: "").replace(",", " ")
                val ts = sdf.format(Date())
                sb.append("$ts,ble,scan_result,$addr,$name,${r.rssi}\n")
            }
            f.appendText(sb.toString())
            Log.i(tag, "Wrote ${results.size} BLE rows")
        } catch (t: Throwable) {
            Log.e(tag, "BLE parse/write failed", t)
        }
    }
}