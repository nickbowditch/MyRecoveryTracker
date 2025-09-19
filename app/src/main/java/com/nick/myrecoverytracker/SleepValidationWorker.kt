package com.nick.myrecoverytracker

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class SleepValidationWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    override fun doWork(): Result {
        val dir = applicationContext.filesDir
        val today = LocalDate.now(zone).format(fmtDate)

        val qa = JSONObject()
        qa.put("date", today)

        var summaryPass = true
        var durPass = true
        var qualPass = true
        val reasons = mutableListOf<String>()

        val fSummary = File(dir, "daily_sleep_summary.csv")
        if (!fSummary.exists()) {
            summaryPass = false
            reasons += "missing daily_sleep_summary.csv"
        }

        val fDur = File(dir, "daily_sleep_duration.csv")
        if (fDur.exists()) {
            fDur.forEachLine { line ->
                if (!line.startsWith("date") && line.isNotBlank()) {
                    val parts = line.split(",")
                    if (parts.size >= 2) {
                        val hrs = parts[1].toDoubleOrNull()
                        if (hrs == null || hrs < 0.0 || hrs > 24.0) {
                            durPass = false
                            reasons += "duration out of range: $line"
                        }
                    }
                }
            }
        } else {
            durPass = false
            reasons += "missing daily_sleep_duration.csv"
        }

        val fQual = File(dir, "daily_sleep_quality.csv")
        if (fQual.exists()) {
            fQual.forEachLine { line ->
                if (!line.startsWith("date") && line.isNotBlank()) {
                    val parts = line.split(",")
                    if (parts.size >= 2) {
                        val q = parts[1]
                        if (q.isBlank()) {
                            qualPass = false
                            reasons += "empty quality: $line"
                        }
                    }
                }
            }
        } else {
            qualPass = false
            reasons += "missing daily_sleep_quality.csv"
        }

        try {
            val wm = WorkManager.getInstance(applicationContext)

            val names = listOf(
                "SleepRollup",
                "periodic-SleepRollup",
                "once-SleepRollup",
                "once-SleepRollupBoot",
                "boot-once-SleepRollup"
            )
            val tags = listOf(
                "SleepRollup",
                "SleepRollupPeriodic"
            )

            val infosByName = names.flatMap { name ->
                try { wm.getWorkInfosForUniqueWork(name).get() } catch (_: Throwable) { emptyList() }
            }
            val infosByTag = tags.flatMap { tag ->
                try { wm.getWorkInfosByTag(tag).get() } catch (_: Throwable) { emptyList() }
            }

            val allInfos = (infosByName + infosByTag)
            val okWorkMgr = allInfos.any { s ->
                val st = s.state
                st == WorkInfo.State.ENQUEUED || st == WorkInfo.State.RUNNING
            }

            val js = applicationContext.getSystemService(JobScheduler::class.java)
            val jobs: List<JobInfo> = try { js.allPendingJobs } catch (_: Throwable) { emptyList() }
            val okJobs = jobs.any { it.service.className == "androidx.work.impl.background.systemjob.SystemJobService" }

            val ok = okWorkMgr || okJobs

            qa.put("reschedule", if (ok) "pass" else "fail")
            if (!ok) reasons += "SleepRollup not enqueued after boot/update"
        } catch (t: Throwable) {
            qa.put("reschedule", "fail")
            reasons += "WorkManager check failed: ${t.message}"
        }

        qa.put("sleep_summary", if (summaryPass) "pass" else "fail")
        qa.put("sleep_duration", if (durPass) "pass" else "fail")
        qa.put("sleep_quality", if (qualPass) "pass" else "fail")
        qa.put("reasons", reasons)

        val qaFile = File(dir, "qa_${today}_sleep.json")
        qaFile.writeText(qa.toString())

        Log.i(TAG, "SleepValidation written -> ${qaFile.name}")
        return Result.success()
    }

    companion object {
        private const val TAG = "SleepValidationWorker"
    }
}