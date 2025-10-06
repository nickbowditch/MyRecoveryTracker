package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import kotlin.Metadata;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/* compiled from: NotificationValidationWorker.kt */
@Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\b\u001a\u00020\tH\u0016¨\u0006\n"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationValidationWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class NotificationValidationWorker extends Worker {
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public NotificationValidationWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.work.Worker
    public ListenableWorker.Result doWork() {
        String engRow;
        Object next;
        try {
            File dir = getApplicationContext().getFilesDir();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            String today = simpleDateFormat.format(new Date());
            File eng = new File(dir, "daily_notification_engagement.csv");
            File lat = new File(dir, "daily_notification_latency.csv");
            String latRow = null;
            if (!eng.exists()) {
                engRow = null;
            } else {
                Iterator it = FilesKt.readLines$default(eng, null, 1, null).iterator();
                while (true) {
                    if (it.hasNext()) {
                        next = it.next();
                        Intrinsics.checkNotNull(today);
                        if (StringsKt.startsWith$default((String) next, today, false, 2, (Object) null)) {
                            break;
                        }
                    } else {
                        next = null;
                        break;
                    }
                }
                engRow = (String) next;
            }
            if (lat.exists()) {
                Iterator it2 = FilesKt.readLines$default(lat, null, 1, null).iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    Object next2 = it2.next();
                    Intrinsics.checkNotNull(today);
                    if (StringsKt.startsWith$default((String) next2, today, false, 2, (Object) null)) {
                        latRow = next2;
                        break;
                    }
                }
                latRow = latRow;
            }
            String str = "(none)";
            Log.i("NotificationValidation", "engagement_today=" + (engRow == null ? "(none)" : engRow));
            if (latRow != null) {
                str = latRow;
            }
            Log.i("NotificationValidation", "latency_today=" + str);
            return ListenableWorker.Result.success();
        } catch (Throwable t) {
            Log.e("NotificationValidation", "validation failed", t);
            return ListenableWorker.Result.failure();
        }
    }
}
