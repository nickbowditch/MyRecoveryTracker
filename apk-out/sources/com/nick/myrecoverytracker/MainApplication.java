package com.nick.myrecoverytracker;

import android.app.Application;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MainApplication.kt */
@Metadata(d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0016J\b\u0010\u0006\u001a\u00020\u0005H\u0002J\b\u0010\u0007\u001a\u00020\u0005H\u0002J8\u0010\b\u001a\u00020\t2\u000e\u0010\n\u001a\n\u0012\u0006\b\u0001\u0012\u00020\f0\u000b2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011H\u0002¨\u0006\u0014"}, d2 = {"Lcom/nick/myrecoverytracker/MainApplication;", "Landroid/app/Application;", "<init>", "()V", "onCreate", "", "schedulePeriodicWork", "enqueueImmediateCatchUp", "dailyAt", "Landroidx/work/PeriodicWorkRequest;", "workerClass", "Ljava/lang/Class;", "Landroidx/work/ListenableWorker;", "uniqueName", "", "tag", "hour", "", "minute", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class MainApplication extends Application {
    public static final String TAG = "MainApplication";
    private static final String TAG_APP_CAT = "AppUsageCategoryDaily";
    private static final String TAG_APP_SWITCH = "AppSwitchingDaily";
    private static final String TAG_DIST = "DistanceSummaryPeriodic";
    private static final String TAG_LIGHT_DAILY = "DailyLightExposurePeriodic";
    private static final String TAG_LNS = "LateNightRollupPeriodic";
    private static final String TAG_LOC = "LocationPingPeriodic";
    private static final String TAG_LOC_LOG = "LocationLoggerPeriodic";
    private static final String TAG_LUX = "AmbientLuxPeriodic";
    private static final String TAG_MOVE_INTENSITY = "MovementIntensityDaily";
    private static final String TAG_NOTIF_ENG = "NotificationEngagementPeriodic";
    private static final String TAG_NOTIF_LAT = "NotificationLatencyPeriodic";
    private static final String TAG_NOTIF_LOG = "NotificationLogTrim";
    private static final String TAG_SLEEP = "SleepRollupPeriodic";
    private static final String TAG_USAGE_DAILY = "UsageEventsDaily";
    private static final String TAG_USAGE_EVENTS = "UsageEventsDump";
    private static final String UNIQUE_APP_CAT = "periodic_app_usage_category";
    private static final String UNIQUE_APP_CAT_NOW = "onstart_app_usage_category";
    private static final String UNIQUE_APP_SWITCH = "periodic_app_switching";
    private static final String UNIQUE_APP_SWITCH_NOW = "onstart_app_switching";
    private static final String UNIQUE_DIST = "periodic_distance_summary";
    private static final String UNIQUE_DIST_NOW = "onstart_distance_summary";
    private static final String UNIQUE_LIGHT_DAILY = "periodic_daily_light_exposure";
    private static final String UNIQUE_LIGHT_NOW = "onstart_light_exposure";
    private static final String UNIQUE_LNS = "periodic_late_night_rollup";
    private static final String UNIQUE_LNS_NOW = "onstart_late_night_rollup";
    private static final String UNIQUE_LOC = "periodic_location_ping";
    private static final String UNIQUE_LOC_LOG = "periodic_location_logger";
    private static final String UNIQUE_LOC_LOG_NOW = "onstart_location_logger";
    private static final String UNIQUE_LUX = "periodic_lux_sample";
    private static final String UNIQUE_LUX_NOW = "onstart_lux_sample";
    private static final String UNIQUE_MOVE_INTENSITY = "periodic_movement_intensity";
    private static final String UNIQUE_MOVE_INTENSITY_NOW = "onstart_movement_intensity";
    private static final String UNIQUE_NOTIF_ENG = "periodic_notification_engagement";
    private static final String UNIQUE_NOTIF_ENG_NOW = "onstart_notification_engagement";
    private static final String UNIQUE_NOTIF_LAT = "periodic_notification_latency";
    private static final String UNIQUE_NOTIF_LAT_NOW = "onstart_notification_latency";
    private static final String UNIQUE_NOTIF_LOG = "periodic_notification_log_trim";
    private static final String UNIQUE_NOTIF_NOW = "onstart_notification_log_trim";
    private static final String UNIQUE_SLEEP = "periodic_sleep_rollup";
    private static final String UNIQUE_SLEEP_NOW = "onstart_sleep_rollup";
    private static final String UNIQUE_USAGE_DAILY = "periodic_usage_events_daily";
    private static final String UNIQUE_USAGE_DAILY_NOW = "onstart_usage_events_daily";
    private static final String UNIQUE_USAGE_EVENTS = "periodic_usage_events_dump";
    private static final String UNIQUE_USAGE_NOW = "onstart_usage_events_dump";

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        try {
            schedulePeriodicWork();
            enqueueImmediateCatchUp();
        } catch (Throwable t) {
            Log.e(TAG, "Work scheduling failed", t);
        }
    }

    private final void schedulePeriodicWork() {
        WorkManager wm = WorkManager.getInstance(this);
        Intrinsics.checkNotNullExpressionValue(wm, "getInstance(...)");
        Constraints locConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build();
        PeriodicWorkRequest locationPing = new PeriodicWorkRequest.Builder(LocationPingWorker.class, 15L, TimeUnit.MINUTES, 5L, TimeUnit.MINUTES).setConstraints(locConstraints).addTag(TAG_LOC).build();
        wm.enqueueUniquePeriodicWork(UNIQUE_LOC, ExistingPeriodicWorkPolicy.KEEP, locationPing);
        PeriodicWorkRequest locationLogger = new PeriodicWorkRequest.Builder(LocationWorker.class, 15L, TimeUnit.MINUTES, 5L, TimeUnit.MINUTES).setConstraints(locConstraints).addTag(TAG_LOC_LOG).build();
        wm.enqueueUniquePeriodicWork(UNIQUE_LOC_LOG, ExistingPeriodicWorkPolicy.KEEP, locationLogger);
        PeriodicWorkRequest distanceDaily = dailyAt(DistanceSummaryWorker.class, UNIQUE_DIST, TAG_DIST, 3, 10);
        wm.enqueueUniquePeriodicWork(UNIQUE_DIST, ExistingPeriodicWorkPolicy.UPDATE, distanceDaily);
        PeriodicWorkRequest sleepDaily = dailyAt(SleepRollupWorker.class, UNIQUE_SLEEP, TAG_SLEEP, 3, 30);
        wm.enqueueUniquePeriodicWork(UNIQUE_SLEEP, ExistingPeriodicWorkPolicy.UPDATE, sleepDaily);
        PeriodicWorkRequest lateNightDaily = dailyAt(LateNightScreenRollupWorker.class, UNIQUE_LNS, TAG_LNS, 5, 5);
        wm.enqueueUniquePeriodicWork(UNIQUE_LNS, ExistingPeriodicWorkPolicy.UPDATE, lateNightDaily);
        PeriodicWorkRequest luxPeriodic = new PeriodicWorkRequest.Builder(AmbientLuxWorker.class, 15L, TimeUnit.MINUTES, 5L, TimeUnit.MINUTES).addTag(TAG_LUX).build();
        wm.enqueueUniquePeriodicWork(UNIQUE_LUX, ExistingPeriodicWorkPolicy.KEEP, luxPeriodic);
        PeriodicWorkRequest lightDaily = dailyAt(DailyLightExposureWorker.class, UNIQUE_LIGHT_DAILY, TAG_LIGHT_DAILY, 3, 40);
        wm.enqueueUniquePeriodicWork(UNIQUE_LIGHT_DAILY, ExistingPeriodicWorkPolicy.UPDATE, lightDaily);
        PeriodicWorkRequest notifLogDaily = dailyAt(NotificationLogWorker.class, UNIQUE_NOTIF_LOG, TAG_NOTIF_LOG, 3, 20);
        wm.enqueueUniquePeriodicWork(UNIQUE_NOTIF_LOG, ExistingPeriodicWorkPolicy.UPDATE, notifLogDaily);
        PeriodicWorkRequest notifEngDaily = dailyAt(NotificationEngagementWorker.class, UNIQUE_NOTIF_ENG, TAG_NOTIF_ENG, 3, 47);
        wm.enqueueUniquePeriodicWork(UNIQUE_NOTIF_ENG, ExistingPeriodicWorkPolicy.UPDATE, notifEngDaily);
        PeriodicWorkRequest notifLatDaily = dailyAt(NotificationLatencyWorker.class, UNIQUE_NOTIF_LAT, TAG_NOTIF_LAT, 3, 48);
        wm.enqueueUniquePeriodicWork(UNIQUE_NOTIF_LAT, ExistingPeriodicWorkPolicy.UPDATE, notifLatDaily);
        PeriodicWorkRequest usageEventsPeriodic = new PeriodicWorkRequest.Builder((Class<? extends ListenableWorker>) UsageEventsDumpWorker.class, 3L, TimeUnit.HOURS).addTag(TAG_USAGE_EVENTS).build();
        wm.enqueueUniquePeriodicWork(UNIQUE_USAGE_EVENTS, ExistingPeriodicWorkPolicy.KEEP, usageEventsPeriodic);
        PeriodicWorkRequest appUsageCatDaily = dailyAt(AppUsageByCategoryDailyWorker.class, UNIQUE_APP_CAT, TAG_APP_CAT, 4, 15);
        wm.enqueueUniquePeriodicWork(UNIQUE_APP_CAT, ExistingPeriodicWorkPolicy.UPDATE, appUsageCatDaily);
        PeriodicWorkRequest appSwitchDaily = dailyAt(AppSwitchingDailyWorker.class, UNIQUE_APP_SWITCH, TAG_APP_SWITCH, 4, 25);
        wm.enqueueUniquePeriodicWork(UNIQUE_APP_SWITCH, ExistingPeriodicWorkPolicy.UPDATE, appSwitchDaily);
        PeriodicWorkRequest usageEventsDaily = dailyAt(UsageEventsDailyWorker.class, UNIQUE_USAGE_DAILY, TAG_USAGE_DAILY, 4, 35);
        wm.enqueueUniquePeriodicWork(UNIQUE_USAGE_DAILY, ExistingPeriodicWorkPolicy.UPDATE, usageEventsDaily);
        PeriodicWorkRequest moveIntensityDaily = dailyAt(MovementIntensityDailyWorker.class, UNIQUE_MOVE_INTENSITY, TAG_MOVE_INTENSITY, 4, 45);
        wm.enqueueUniquePeriodicWork(UNIQUE_MOVE_INTENSITY, ExistingPeriodicWorkPolicy.UPDATE, moveIntensityDaily);
        Log.i(TAG, "Periodic work scheduled");
    }

    private final void enqueueImmediateCatchUp() {
        WorkManager wm = WorkManager.getInstance(this);
        Intrinsics.checkNotNullExpressionValue(wm, "getInstance(...)");
        wm.enqueueUniqueWork(UNIQUE_SLEEP_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(SleepRollupWorker.class).addTag("SleepRollupPeriodic_now").build());
        wm.enqueueUniqueWork(UNIQUE_LNS_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(LateNightScreenRollupWorker.class).addTag("LateNightRollupPeriodic_now").build());
        wm.enqueueUniqueWork(UNIQUE_DIST_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(DistanceSummaryWorker.class).addTag("DistanceSummaryPeriodic_now").build());
        wm.enqueueUniqueWork(UNIQUE_LUX_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(AmbientLuxWorker.class).addTag("AmbientLuxPeriodic_now").build());
        wm.enqueueUniqueWork(UNIQUE_LIGHT_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(DailyLightExposureWorker.class).addTag("DailyLightExposurePeriodic_now").build());
        wm.enqueueUniqueWork(UNIQUE_NOTIF_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(NotificationLogWorker.class).addTag("NotificationLogTrim_now").build());
        wm.enqueueUniqueWork(UNIQUE_NOTIF_ENG_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(NotificationEngagementWorker.class).addTag("NotificationEngagementPeriodic_now").build());
        wm.enqueueUniqueWork(UNIQUE_NOTIF_LAT_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(NotificationLatencyWorker.class).addTag("NotificationLatencyPeriodic_now").build());
        wm.enqueueUniqueWork(UNIQUE_USAGE_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(UsageEventsDumpWorker.class).addTag("UsageEventsDump_now").build());
        wm.enqueueUniqueWork(UNIQUE_APP_CAT_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(AppUsageByCategoryDailyWorker.class).addTag("AppUsageCategoryDaily_now").build());
        wm.enqueueUniqueWork(UNIQUE_APP_SWITCH_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(AppSwitchingDailyWorker.class).addTag("AppSwitchingDaily_now").build());
        wm.enqueueUniqueWork(UNIQUE_USAGE_DAILY_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(UsageEventsDailyWorker.class).addTag("UsageEventsDaily_now").build());
        wm.enqueueUniqueWork(UNIQUE_MOVE_INTENSITY_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(MovementIntensityDailyWorker.class).addTag("MovementIntensityDaily_now").build());
        wm.enqueueUniqueWork(UNIQUE_LOC_LOG_NOW, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(LocationWorker.class).addTag("LocationLoggerPeriodic_now").build());
        Log.i(TAG, "Immediate catch-up enqueued");
    }

    private final PeriodicWorkRequest dailyAt(Class<? extends ListenableWorker> workerClass, String uniqueName, String tag, int hour, int minute) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime firstRun = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        if (!firstRun.isAfter(now)) {
            firstRun = firstRun.plusDays(1L);
        }
        long initialDelay = Duration.between(now, firstRun).toMinutes();
        return new PeriodicWorkRequest.Builder(workerClass, 24L, TimeUnit.HOURS).setInitialDelay(initialDelay, TimeUnit.MINUTES).addTag(tag).build();
    }
}
