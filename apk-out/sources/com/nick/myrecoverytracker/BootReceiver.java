package com.nick.myrecoverytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: BootReceiver.kt */
@Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u001a\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016¨\u0006\n"}, d2 = {"Lcom/nick/myrecoverytracker/BootReceiver;", "Landroid/content/BroadcastReceiver;", "<init>", "()V", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class BootReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Intrinsics.checkNotNullParameter(context, "context");
        ContextCompat.startForegroundService(context, new Intent(context, (Class<?>) ForegroundUnlockService.class));
        ContextCompat.startForegroundService(context, new Intent(context, (Class<?>) ForegroundSleepService.class));
        WorkScheduler.INSTANCE.scheduleDailySleepRollup(context);
        WorkScheduler.INSTANCE.enqueueOneTimeSleepRollup(context);
        PeriodicWorkRequest periodicUsage = new PeriodicWorkRequest.Builder((Class<? extends ListenableWorker>) UsageCaptureWorker.class, 24L, TimeUnit.HOURS).addTag("UsageCaptureDaily").build();
        PeriodicWorkRequest periodicNotif = new PeriodicWorkRequest.Builder((Class<? extends ListenableWorker>) NotificationRollupWorker.class, 24L, TimeUnit.HOURS).addTag("NotificationRollupDaily").build();
        WorkManager wm = WorkManager.getInstance(context);
        Intrinsics.checkNotNullExpressionValue(wm, "getInstance(...)");
        wm.enqueueUniquePeriodicWork("mrt_usage_daily", ExistingPeriodicWorkPolicy.UPDATE, periodicUsage);
        wm.enqueueUniquePeriodicWork("mrt_notification_daily", ExistingPeriodicWorkPolicy.UPDATE, periodicNotif);
        OneTimeWorkRequest onceUsage = new OneTimeWorkRequest.Builder(UsageCaptureWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("UsageCaptureBoot").build();
        OneTimeWorkRequest onceUnlock = new OneTimeWorkRequest.Builder(UnlockWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("UnlockScanBoot").build();
        OneTimeWorkRequest onceSleep = new OneTimeWorkRequest.Builder(SleepRollupWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("SleepRollupBoot").build();
        OneTimeWorkRequest onceMovement = new OneTimeWorkRequest.Builder(MovementRollupWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("MovementRollupBoot").build();
        OneTimeWorkRequest onceNotif = new OneTimeWorkRequest.Builder(NotificationRollupWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationRollupBoot").build();
        wm.enqueueUniqueWork("once-UsageCaptureBoot", ExistingWorkPolicy.REPLACE, onceUsage);
        wm.enqueueUniqueWork("once-UnlockScanBoot", ExistingWorkPolicy.REPLACE, onceUnlock);
        wm.enqueueUniqueWork("once-SleepRollupBoot", ExistingWorkPolicy.REPLACE, onceSleep);
        wm.enqueueUniqueWork("once-MovementRollupBoot", ExistingWorkPolicy.REPLACE, onceMovement);
        wm.enqueueUniqueWork("once-NotificationRollupBoot", ExistingWorkPolicy.REPLACE, onceNotif);
    }
}
