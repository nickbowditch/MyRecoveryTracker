package com.nick.myrecoverytracker;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;

/* compiled from: UsageDumpWorker.kt */
@Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\n¨\u0006\u000b"}, d2 = {"Lcom/nick/myrecoverytracker/UsageDumpWorker;", "Landroidx/work/CoroutineWorker;", "ctx", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UsageDumpWorker extends CoroutineWorker {
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public UsageDumpWorker(Context ctx, WorkerParameters params) {
        super(ctx, params);
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    @Override // androidx.work.CoroutineWorker
    public Object doWork(Continuation<? super ListenableWorker.Result> continuation) {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        long begin = cal.getTimeInMillis();
        Object systemService = getApplicationContext().getSystemService("usagestats");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.usage.UsageStatsManager");
        UsageStatsManager mgr = (UsageStatsManager) systemService;
        UsageEvents events = mgr.queryEvents(begin, now);
        HashMap lastResume = new HashMap();
        HashMap total = new HashMap();
        UsageEvents.Event e = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(e);
            String pkg = e.getPackageName();
            if (pkg != null) {
                switch (e.getEventType()) {
                    case 1:
                        lastResume.put(pkg, Boxing.boxLong(e.getTimeStamp()));
                        break;
                    case 2:
                    case 23:
                        Long l = (Long) lastResume.remove(pkg);
                        if (l != null) {
                            long start = l.longValue();
                            if (e.getTimeStamp() >= start) {
                                HashMap map = total;
                                Long l2 = (Long) total.get(pkg);
                                map.put(pkg, Boxing.boxLong((l2 != null ? l2.longValue() : 0L) + (e.getTimeStamp() - start)));
                                break;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                }
            }
        }
        String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(begin));
        File out = new File(getApplicationContext().getFilesDir(), "usage_by_pkg_daily.csv");
        File parentFile = out.getParentFile();
        if (parentFile != null) {
            Boxing.boxBoolean(parentFile.mkdirs());
        }
        StringBuilder sb = new StringBuilder();
        Iterable iterableEntrySet = total.entrySet();
        Intrinsics.checkNotNullExpressionValue(iterableEntrySet, "<get-entries>(...)");
        Iterable<Map.Entry> iterableSortedWith = CollectionsKt.sortedWith(iterableEntrySet, new Comparator() { // from class: com.nick.myrecoverytracker.UsageDumpWorker$doWork$lambda$3$$inlined$sortedByDescending$1
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return ComparisonsKt.compareValues((Long) ((Map.Entry) t2).getValue(), (Long) ((Map.Entry) t).getValue());
            }
        });
        int i = 0;
        for (Map.Entry entry : iterableSortedWith) {
            Intrinsics.checkNotNull(entry);
            Iterable iterable = iterableSortedWith;
            Object key = entry.getKey();
            int i2 = i;
            Intrinsics.checkNotNullExpressionValue(key, "component1(...)");
            Object value = entry.getValue();
            Calendar cal2 = cal;
            Intrinsics.checkNotNullExpressionValue(value, "component2(...)");
            double dLongValue = (((Long) value).longValue() / 1000.0d) / 60.0d;
            StringBuilder sbAppend = sb.append(day).append(',').append((String) key).append(',');
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            String str = String.format(Locale.US, "%.1f", Arrays.copyOf(new Object[]{Boxing.boxDouble(dLongValue)}, 1));
            Intrinsics.checkNotNullExpressionValue(str, "format(...)");
            sbAppend.append(str).append('\n');
            cal = cal2;
            iterableSortedWith = iterable;
            i = i2;
            begin = begin;
        }
        String rows = sb.toString();
        Intrinsics.checkNotNullExpressionValue(rows, "toString(...)");
        FilesKt.appendText$default(out, rows, null, 2, null);
        ListenableWorker.Result resultSuccess = ListenableWorker.Result.success();
        Intrinsics.checkNotNullExpressionValue(resultSuccess, "success(...)");
        return resultSuccess;
    }
}
