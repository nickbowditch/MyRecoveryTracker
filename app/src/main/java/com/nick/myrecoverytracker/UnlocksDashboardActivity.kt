package com.nick.myrecoverytracker

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class UnlocksDashboardActivity : AppCompatActivity() {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlocks_dashboard)

        val progress = findViewById<ProgressBar>(R.id.progress)
        val todayCount = findViewById<TextView>(R.id.todayCount)
        val totalCount = findViewById<TextView>(R.id.totalCount)
        val list = findViewById<ListView>(R.id.unlockList)

        progress.visibility = View.VISIBLE

        // Load on UI thread; file IO here is tiny. If file grows large we can move to a coroutine.
        val lines = MetricsStore.getUnlockLog(this)
        val todayPrefix = dateFmt.format(Date())
        val todays = lines.count { it.startsWith(todayPrefix) }

        todayCount.text = todays.toString()
        totalCount.text = lines.size.toString()

        val recent = lines.takeLast(50).reversed() // newest first
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, recent)

        progress.visibility = View.GONE
    }
}