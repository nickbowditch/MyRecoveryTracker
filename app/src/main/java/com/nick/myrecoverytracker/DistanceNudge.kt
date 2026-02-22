// app/src/main/java/com/nick/myrecoverytracker/DistanceNudge.kt
package com.nick.myrecoverytracker

import android.annotation.SuppressLint
import android.content.*
import android.location.Location
import android.os.Looper
import androidx.work.*
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.File
import java.time.*
import java.util.concurrent.TimeUnit
import kotlin.math.*

/** Append one line to location_log.csv (header if needed) */
private fun appendLocation(context: Context, loc: Location) {
    val f = File(context.filesDir, "location_log.csv")
    if (!f.exists()) f.writeText("timestamp,lat,lon,accuracy_m\n")
    f.appendText("${System.currentTimeMillis()},${loc.latitude},${loc.longitude},${loc.accuracy}\n")
}

/** Haversine distance in km */
private fun haversineKm(a: Location, b: Location): Double {
    val R = 6371.0088
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val h = sin(dLat/2).pow(2.0) + cos(lat1)*cos(lat2)*sin(dLon/2).pow(2.0)
    return 2*R*asin(min(1.0, sqrt(h)))
}

/** Recompute today’s distance from location_log.csv and upsert daily_distance_log.csv */
private fun recomputeToday(context: Context) {
    val locFile = File(context.filesDir, "location_log.csv")
    if (!locFile.exists()) return
    val today = LocalDate.now()
    val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val end = start + 24L*60*60*1000

    val lines = locFile.readLines().drop(1).mapNotNull { line ->
        val p = line.split(",")
        if (p.size < 4) null else try {
            val t = p[0].trim().toLong()
            if (t in start until end) {
                val loc = Location("recalc").apply {
                    latitude = p[1].toDouble(); longitude = p[2].toDouble(); accuracy = p[3].toFloat()
                }
                t to loc
            } else null
        } catch (_: Throwable) { null }
    }.sortedBy { it.first }.map { it.second }

    if (lines.size < 2) return
    var km = 0.0
    var prev = lines.first()
    for (i in 1 until lines.size) {
        val cur = lines[i]
        // ignore wild jumps ( > 500m and accuracy poor )
        val d = haversineKm(prev, cur)
        if (!(d*1000 > 500 && (prev.accuracy > 50 || cur.accuracy > 50))) km += d
        prev = cur
    }

    val out = File(context.filesDir, "daily_distance_log.csv")
    if (!out.exists()) out.writeText("date,distance_km\n")
    val map = out.readLines().drop(1).associate {
        val p = it.split(","); p[0] to p.getOrElse(1) { "0.00" }
    }.toMutableMap()
    map[today.toString()] = String.format("%.2f", km)
    out.printWriter().use {
        it.println("date,distance_km")
        map.forEach { (d,v) -> it.println("$d,$v") }
    }
}

/** One-shot location nudge + recompute */
class DistanceNudgeReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.action != ACTION) return
        requestOneLocationThenRecompute(c.applicationContext)
    }
    companion object { const val ACTION = "com.nick.myrecoverytracker.ACTION_DISTANCE_NUDGE" }
}

@SuppressLint("MissingPermission")
@OptIn(DelicateCoroutinesApi::class)  // silences the 'delicate API' warning for GlobalScope
private fun requestOneLocationThenRecompute(app: Context) {
    val fused = LocationServices.getFusedLocationProviderClient(app)
    val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
        .setMaxUpdates(1)
        .setMinUpdateIntervalMillis(2_000L)
        .build()
    val cb = object : LocationCallback() {
        override fun onLocationResult(res: LocationResult) {
            val loc = res.lastLocation
            if (loc != null) appendLocation(app, loc)
            fused.removeLocationUpdates(this)
            // recompute after we (likely) have at least one fresh point
            GlobalScope.launch(Dispatchers.IO) { recomputeToday(app) }
        }
    }
    fused.requestLocationUpdates(req, cb, Looper.getMainLooper())
}

/** Optional: daily schedule near midnight so yesterday totals are finalised */
object DistanceDailySchedule {
    fun schedule(context: Context) {
        val target = LocalDate.now().atTime(23, 50)
        val now = LocalDateTime.now()
        val delayMin = Duration.between(now, target).toMinutes().let { if (it < 0) it + 1440 else it }
        val req = PeriodicWorkRequestBuilder<RecomputeWorker>(24, TimeUnit.HOURS, 120, TimeUnit.MINUTES)
            .setInitialDelay(delayMin, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DistanceRecomputeDaily", ExistingPeriodicWorkPolicy.UPDATE, req
        )
    }
    class RecomputeWorker(ctx: Context, p: WorkerParameters): CoroutineWorker(ctx,p){
        override suspend fun doWork(): Result = withContext(Dispatchers.IO) { recomputeToday(applicationContext); Result.success() }
    }
}