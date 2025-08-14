package com.nick.myrecoverytracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

/**
 * Simple debug screen to view recent unlock events from unlock_log.csv
 */
class DebugUnlocksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listView = ListView(this)
        setContentView(listView)

        val unlocks = MetricsStore.getUnlockLog(this)
            .takeLast(20)
            .reversed() // newest first

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            unlocks
        )
        listView.adapter = adapter
    }
}