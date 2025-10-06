package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.util.Iterator;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: LogRetentionWorker.kt */
@Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\b\u001a\u00020\tH\u0016¨\u0006\n"}, d2 = {"Lcom/nick/myrecoverytracker/LogRetentionWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class LogRetentionWorker extends Worker {
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public LogRetentionWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    @Override // androidx.work.Worker
    public ListenableWorker.Result doWork() {
        File dir = getApplicationContext().getFilesDir();
        Iterator it = CollectionsKt.listOf((Object[]) new File[]{new File(dir, "unlock_log.csv"), new File(dir, "screen_log.csv"), new File(dir, "notification_log.csv")}).iterator();
        while (it.hasNext()) {
            CsvUtils.INSTANCE.rotateByTimestampPrefix((File) it.next(), 30);
        }
        Iterator it2 = CollectionsKt.listOf((Object[]) new File[]{new File(dir, "daily_sleep_summary.csv"), new File(dir, "daily_sleep_duration.csv"), new File(dir, "daily_sleep_time.csv"), new File(dir, "daily_wake_time.csv"), new File(dir, "daily_sleep_quality.csv"), new File(dir, "daily_unlocks.csv")}).iterator();
        while (it2.hasNext()) {
            CsvUtils.INSTANCE.rotateByDate((File) it2.next(), 400);
        }
        Iterator it3 = CollectionsKt.listOf((Object[]) new File[]{new File(dir, "redcap_upload_log.csv"), new File(dir, "redcap_receipts.csv"), new File(dir, "health_snapshot.csv")}).iterator();
        while (it3.hasNext()) {
            CsvUtils.INSTANCE.rotateByTimestampPrefix((File) it3.next(), 90);
        }
        ListenableWorker.Result resultSuccess = ListenableWorker.Result.success();
        Intrinsics.checkNotNullExpressionValue(resultSuccess, "success(...)");
        return resultSuccess;
    }
}
