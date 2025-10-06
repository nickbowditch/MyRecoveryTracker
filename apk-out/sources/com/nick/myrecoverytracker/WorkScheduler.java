package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;

/* compiled from: WorkScheduler.kt */
@Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000b\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\f\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u0018\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u000eH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u0011"}, d2 = {"Lcom/nick/myrecoverytracker/WorkScheduler;", "", "<init>", "()V", "SLEEP_PERIODIC", "", "ENGAGEMENT_PERIODIC", "scheduleDailySleepRollup", "", "context", "Landroid/content/Context;", "enqueueOneTimeSleepRollup", "scheduleDailyEngagementRollup", "nextOccurrence", "Ljava/time/LocalDateTime;", "now", "targetToday", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class WorkScheduler {
    private static final String ENGAGEMENT_PERIODIC = "periodic-EngagementRollup";
    public static final WorkScheduler INSTANCE = new WorkScheduler();
    private static final String SLEEP_PERIODIC = "periodic-SleepRollup";

    private WorkScheduler() {
    }

    public final void scheduleDailySleepRollup(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTimeAtTime = LocalDate.now(ZoneId.systemDefault()).atTime(LocalTime.of(4, 15));
        Intrinsics.checkNotNull(now);
        Intrinsics.checkNotNull(localDateTimeAtTime);
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(SLEEP_PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, new PeriodicWorkRequest.Builder((Class<? extends ListenableWorker>) SleepRollupWorker.class, 24L, TimeUnit.HOURS).setInitialDelay(RangesKt.coerceAtLeast(Duration.between(now, nextOccurrence(now, localDateTimeAtTime)).toMinutes(), 1L), TimeUnit.MINUTES).build());
    }

    public final void enqueueOneTimeSleepRollup(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        OneTimeWorkRequest one = new OneTimeWorkRequest.Builder(SleepRollupWorker.class).build();
        WorkManager.getInstance(context).enqueueUniqueWork("boot-once-SleepRollup", ExistingWorkPolicy.KEEP, one);
    }

    public final void scheduleDailyEngagementRollup(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        LocalDateTime now = LocalDateTime.now();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime target = LocalDate.now(zone).atTime(LocalTime.of(4, 25));
        Intrinsics.checkNotNull(now);
        Intrinsics.checkNotNull(target);
        Duration initial = Duration.between(now, nextOccurrence(now, target));
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder((Class<? extends ListenableWorker>) NotificationEngagementWorker.class, 24L, TimeUnit.HOURS).setInitialDelay(RangesKt.coerceAtLeast(initial.toMinutes(), 1L), TimeUnit.MINUTES).addTag("NotificationEngagement").build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(ENGAGEMENT_PERIODIC, ExistingPeriodicWorkPolicy.UPDATE, req);
    }

    private final LocalDateTime nextOccurrence(LocalDateTime now, LocalDateTime targetToday) {
        if (now.isBefore(targetToday)) {
            return targetToday;
        }
        LocalDateTime localDateTimePlusDays = targetToday.plusDays(1L);
        Intrinsics.checkNotNullExpressionValue(localDateTimePlusDays, "plusDays(...)");
        return localDateTimePlusDays;
    }
}
