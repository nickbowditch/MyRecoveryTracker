package com.nick.myrecoverytracker

import android.content.Context
import android.os.Build

object DirectBoot {
    fun deviceProtectedContext(ctx: Context): Context =
        if (Build.VERSION.SDK_INT >= 24) ctx.createDeviceProtectedStorageContext() else ctx

    fun canAccessFiles(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= 24) ctx.isDeviceProtectedStorage else true
}