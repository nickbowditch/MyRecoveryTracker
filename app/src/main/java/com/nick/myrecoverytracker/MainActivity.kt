package com.nick.myrecoverytracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var tvHeader: TextView
    private lateinit var tvNotif: TextView
    private lateinit var tvSmsCall: TextView
    private lateinit var tvUsage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Minimal, programmatic layout (no XML)
        val dp = { v: Int -> (v * resources.displayMetrics.density).toInt() }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }

        tvHeader = TextView(this).apply {
            text = "Permissions status"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
        }

        tvNotif = clickableRow("Notifications: …") {
            openNotificationSettings()
        }

        tvSmsCall = clickableRow("SMS & Call Logs: …") {
            // Opens our one-shot dialog flow
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        tvUsage = clickableRow("Usage Access: …") {
            UsagePermissionHelper.openSettings(this)
        }

        // Small hint
        val tvHint = TextView(this).apply {
            text = "Tap any row to fix.\n(These do not block other metrics — everything else keeps working.)"
            setPadding(0, dp(12), 0, 0)
            setTextColor(0xFF666666.toInt())
        }

        // Spacer
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(8)
            )
        }

        // Compose the layout
        root.addView(tvHeader)
        root.addView(spacer)
        root.addView(tvNotif)
        root.addView(tvSmsCall)
        root.addView(tvUsage)
        root.addView(tvHint)

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
    }

    private fun refreshPermissionStatus() {
        // Notifications (API 33+ requires runtime grant)
        val notifEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
        tvNotif.text = "Notifications: " + (if (notifEnabled) "✅ enabled" else "❌ disabled — tap to enable")

        // SMS + Call Log (Phase 4)
        val smsCallOk = !UsagePermissionHelper.needsRestricted(this)
        tvSmsCall.text = "SMS & Call Logs: " + (if (smsCallOk) "✅ granted" else "❌ missing — tap to grant")

        // Usage Access (Phase 3 AppUsageWorker)
        val usageOk = UsagePermissionHelper.isGranted(this)
        tvUsage.text = "Usage Access: " + (if (usageOk) "✅ granted" else "❌ missing — tap to open settings")
    }

    private fun clickableRow(initialText: String, onClick: () -> Unit): TextView {
        val tv = TextView(this)
        tv.text = initialText
        tv.textSize = 16f
        tv.setPadding(0, dp(12), 0, dp(12))
        tv.setTextColor(0xFF222222.toInt())
        tv.setOnClickListener { onClick() }
        return tv
    }

    private fun openNotificationSettings() {
        // Generic app notification settings screen
        val intent = Intent().apply {
            action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Settings.ACTION_APP_NOTIFICATION_SETTINGS
            } else {
                @Suppress("DEPRECATION")
                "android.settings.APP_NOTIFICATION_SETTINGS"
            }
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            // Fallbacks for older devices
            putExtra("app_package", packageName)
            putExtra("app_uid", applicationInfo.uid)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    // dp helper
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}