package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.SequencesKt;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;

/* compiled from: DistanceWorker.kt */
@Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\b\u001a\u00020\tH\u0016J\u001e\u0010\n\u001a\u0010\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f\u0018\u00010\u000b2\u0006\u0010\r\u001a\u00020\u000eH\u0002J(\u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\fH\u0002J \u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00032\u0006\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0018\u001a\u00020\fH\u0002J\u0018\u0010\u0019\u001a\u00020\u00152\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u000eH\u0002¨\u0006\u001e"}, d2 = {"Lcom/nick/myrecoverytracker/DistanceWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "workerParams", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "parseLatLon", "Lkotlin/Pair;", "", "line", "", "haversineKm", "lat1", "lon1", "lat2", "lon2", "writeDailyDistance", "", "context", "date", "km", "ensureHeader", "f", "Ljava/io/File;", "header", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class DistanceWorker extends Worker {
    private static final String TAG = "DistanceWorker";

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DistanceWorker(Context appContext, WorkerParameters workerParams) {
        super(appContext, workerParams);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(workerParams, "workerParams");
    }

    @Override // androidx.work.Worker
    public ListenableWorker.Result doWork() {
        try {
            Context context = getApplicationContext();
            Intrinsics.checkNotNullExpressionValue(context, "getApplicationContext(...)");
            File locFile = new File(context.getFilesDir(), "location_log.csv");
            if (!locFile.exists()) {
                Log.w(TAG, "location_log.csv not found");
                return ListenableWorker.Result.success();
            }
            final String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            List points = SequencesKt.toList(SequencesKt.mapNotNull(SequencesKt.filter(SequencesKt.filter(CollectionsKt.asSequence(FilesKt.readLines$default(locFile, null, 1, null)), new Function1() { // from class: com.nick.myrecoverytracker.DistanceWorker$$ExternalSyntheticLambda0
                @Override // kotlin.jvm.functions.Function1
                public final Object invoke(Object obj) {
                    return Boolean.valueOf(DistanceWorker.doWork$lambda$0((String) obj));
                }
            }), new Function1() { // from class: com.nick.myrecoverytracker.DistanceWorker$$ExternalSyntheticLambda1
                @Override // kotlin.jvm.functions.Function1
                public final Object invoke(Object obj) {
                    return Boolean.valueOf(DistanceWorker.doWork$lambda$1(today, (String) obj));
                }
            }), new Function1() { // from class: com.nick.myrecoverytracker.DistanceWorker$$ExternalSyntheticLambda2
                @Override // kotlin.jvm.functions.Function1
                public final Object invoke(Object obj) {
                    return DistanceWorker.doWork$lambda$2(this.f$0, (String) obj);
                }
            }));
            if (points.size() < 2) {
                Log.i(TAG, "Not enough points for " + today);
                ListenableWorker.Result resultSuccess = ListenableWorker.Result.success();
                Intrinsics.checkNotNullExpressionValue(resultSuccess, "success(...)");
                return resultSuccess;
            }
            double lat1 = 0.0d;
            int i = 0;
            int lastIndex = CollectionsKt.getLastIndex(points);
            while (i < lastIndex) {
                Pair pair = (Pair) points.get(i);
                double lat12 = ((Number) pair.component1()).doubleValue();
                double lon1 = ((Number) pair.component2()).doubleValue();
                Pair pair2 = (Pair) points.get(i + 1);
                double lat2 = ((Number) pair2.component1()).doubleValue();
                double lon2 = ((Number) pair2.component2()).doubleValue();
                lat1 += haversineKm(lat12, lon1, lat2, lon2);
                i++;
                points = points;
            }
            double totalKm = lat1;
            Intrinsics.checkNotNull(today);
            writeDailyDistance(context, today, totalKm);
            String str = String.format("%.2f", Arrays.copyOf(new Object[]{Double.valueOf(totalKm)}, 1));
            Intrinsics.checkNotNullExpressionValue(str, "format(...)");
            Log.i(TAG, "Distance " + today + " = " + str + " km");
            return ListenableWorker.Result.success();
        } catch (Throwable t) {
            Log.e(TAG, "Error computing distance", t);
            return ListenableWorker.Result.failure();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean doWork$lambda$0(String it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return !StringsKt.isBlank(it) && Character.isDigit(it.charAt(0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean doWork$lambda$1(String $today, String it) {
        Intrinsics.checkNotNullParameter(it, "it");
        Intrinsics.checkNotNull($today);
        return StringsKt.startsWith$default(it, $today, false, 2, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Pair doWork$lambda$2(DistanceWorker this$0, String it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return this$0.parseLatLon(it);
    }

    private final Pair<Double, Double> parseLatLon(String line) {
        Double doubleOrNull;
        List parts = StringsKt.split$default((CharSequence) line, new char[]{','}, false, 0, 6, (Object) null);
        if (parts.size() < 3 || (doubleOrNull = StringsKt.toDoubleOrNull((String) parts.get(1))) == null) {
            return null;
        }
        double lat = doubleOrNull.doubleValue();
        Double doubleOrNull2 = StringsKt.toDoubleOrNull((String) parts.get(2));
        if (doubleOrNull2 == null) {
            return null;
        }
        double lon = doubleOrNull2.doubleValue();
        return TuplesKt.to(Double.valueOf(lat), Double.valueOf(lon));
    }

    private final double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double d = 2;
        double a = (Math.sin(dLat / d) * Math.sin(dLat / d)) + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / d) * Math.sin(dLon / d));
        double c = d * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0d * c;
    }

    private final void writeDailyDistance(Context context, String date, double km) throws IOException {
        File f = new File(context.getFilesDir(), "daily_distance_log.csv");
        ensureHeader(f, "date,distance_km\n");
        String str = String.format("%.2f", Arrays.copyOf(new Object[]{Double.valueOf(km)}, 1));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        FilesKt.appendText$default(f, date + "," + str + "\n", null, 2, null);
    }

    private final void ensureHeader(File f, String header) throws IOException {
        if (!f.exists() || f.length() == 0) {
            File parentFile = f.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FilesKt.writeText$default(f, header, null, 2, null);
            return;
        }
        Reader inputStreamReader = new InputStreamReader(new FileInputStream(f), Charsets.UTF_8);
        String first = (inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192)).readLine();
        if (first == null) {
            first = "";
        }
        if (!StringsKt.startsWith$default(first, "date,", false, 2, (Object) null)) {
            String content = FilesKt.readText$default(f, null, 1, null);
            FilesKt.writeText$default(f, header + content, null, 2, null);
        }
    }
}
