package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: DailySummaryWorker.kt */
@Metadata(d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u0000 \u00112\u00020\u0001:\u0001\u0011B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\b\u001a\u00020\tH\u0016J\b\u0010\n\u001a\u00020\u000bH\u0002J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000bH\u0002J\b\u0010\u000f\u001a\u00020\u0010H\u0002¨\u0006\u0012"}, d2 = {"Lcom/nick/myrecoverytracker/DailySummaryWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "countTodayUnlocks", "", "saveSummary", "", "unlockCount", "getTodayDate", "", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class DailySummaryWorker extends Worker {
    private static final String LOG_FILE = "unlocks_log.csv";
    private static final String SUMMARY_FILE = "daily_summary.json";
    private static final String TAG = "DailySummaryWorker";

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DailySummaryWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    @Override // androidx.work.Worker
    public ListenableWorker.Result doWork() {
        try {
            int unlocksToday = countTodayUnlocks();
            saveSummary(unlocksToday);
            Log.i(TAG, "✅ Summary written: " + unlocksToday + " unlocks today");
            return ListenableWorker.Result.success();
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to write daily summary", e);
            return ListenableWorker.Result.failure();
        }
    }

    private final int countTodayUnlocks() {
        File file = new File(getApplicationContext().getFilesDir(), LOG_FILE);
        if (!file.exists()) {
            return 0;
        }
        Calendar now = Calendar.getInstance();
        Object objClone = now.clone();
        Intrinsics.checkNotNull(objClone, "null cannot be cast to non-null type java.util.Calendar");
        Calendar todayStart = (Calendar) objClone;
        todayStart.set(11, 0);
        todayStart.set(12, 0);
        todayStart.set(13, 0);
        todayStart.set(14, 0);
        long startMillis = todayStart.getTimeInMillis();
        Iterable lines$default = FilesKt.readLines$default(file, null, 1, null);
        Collection arrayList = new ArrayList();
        Iterator it = lines$default.iterator();
        while (it.hasNext()) {
            Long longOrNull = StringsKt.toLongOrNull((String) it.next());
            if (longOrNull != null) {
                arrayList.add(longOrNull);
            }
        }
        Iterable iterable = (List) arrayList;
        if ((iterable instanceof Collection) && ((Collection) iterable).isEmpty()) {
            return 0;
        }
        int i = 0;
        Iterator it2 = iterable.iterator();
        while (it2.hasNext()) {
            if ((((Number) it2.next()).longValue() >= startMillis) && (i = i + 1) < 0) {
                CollectionsKt.throwCountOverflow();
            }
        }
        return i;
    }

    private final void saveSummary(int unlockCount) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("date", getTodayDate());
        json.put("unlockCount", unlockCount);
        File summaryFile = new File(getApplicationContext().getFilesDir(), SUMMARY_FILE);
        String string = json.toString(2);
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        FilesKt.writeText$default(summaryFile, string, null, 2, null);
    }

    private final String getTodayDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String str = formatter.format(new Date());
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }
}
