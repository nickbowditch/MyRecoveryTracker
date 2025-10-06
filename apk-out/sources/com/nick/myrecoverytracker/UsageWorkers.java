package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: UsageWorkers.kt */
@Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u000b"}, d2 = {"Lcom/nick/myrecoverytracker/UsageWorkers;", "", "<init>", "()V", "UNIQUE_USAGE_DAILY", "", "ensure", "", "ctx", "Landroid/content/Context;", "runNow", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UsageWorkers {
    public static final UsageWorkers INSTANCE = new UsageWorkers();
    private static final String UNIQUE_USAGE_DAILY = "mrt_usage_daily";

    private UsageWorkers() {
    }

    public final void ensure(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder((Class<? extends ListenableWorker>) UsageCaptureWorker.class, 24L, TimeUnit.HOURS).setConstraints(Constraints.NONE).setInitialDelay(15L, TimeUnit.MINUTES).addTag(UNIQUE_USAGE_DAILY).build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(UNIQUE_USAGE_DAILY, ExistingPeriodicWorkPolicy.UPDATE, req);
    }

    public final void runNow(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        OneTimeWorkRequest now = new OneTimeWorkRequest.Builder(UsageCaptureWorker.class).build();
        WorkManager.getInstance(ctx).enqueue(now);
    }
}
