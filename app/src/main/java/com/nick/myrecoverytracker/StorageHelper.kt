package com.nick.myrecoverytracker

import android.content.Context
import java.io.File

object StorageHelper {
    fun getDataDir(context: Context): File {
        return File(context.getExternalFilesDir(null), "data").apply {
            if (!exists()) mkdirs()
        }
    }
}