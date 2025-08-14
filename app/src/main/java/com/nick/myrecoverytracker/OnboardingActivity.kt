package com.nick.myrecoverytracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Minimal placeholder used by existing code paths.
 * No UI. Closes immediately.
 */
class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}