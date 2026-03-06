// app/src/main/java/com/nick/myrecoverytracker/ServiceStarter.kt
package com.nick.myrecoverytracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object ServiceStarter {
    private fun has(ctx: Context, perm: String) =
        ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED

    fun canRunLocation(ctx: Context): Boolean {
        val fine = has(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = has(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fgLocOk = if (Build.VERSION.SDK_INT >= 34)
            has(ctx, Manifest.permission.FOREGROUND_SERVICE_LOCATION) else true
        return (fine || coarse) && fgLocOk
    }

    fun startAllIfAllowed(ctx: Context) {
        // Start the unlock tracking service
        ForegroundUnlockService.start(ctx)

        if (canRunLocation(ctx)) LocationCaptureService.start(ctx)
    }
}