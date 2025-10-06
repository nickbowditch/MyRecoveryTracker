package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;

/* compiled from: DailyLightExposureWorker.kt */
@Metadata(d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0000\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\u0011\u001a\u00020\u0012H\u0016J\u0018\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J\u0012\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u001b\u001a\u00020\tH\u0002J\u0018\u0010\u001c\u001a\u00020\u00162\u0006\u0010\u001d\u001a\u00020\u00162\u0006\u0010\u001e\u001a\u00020\tH\u0002J&\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\u00162\u0006\u0010\"\u001a\u00020\t2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\t0$H\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R\u0018\u0010\f\u001a\n \u000e*\u0004\u0018\u00010\r0\rX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u000fR\u0018\u0010\u0010\u001a\n \u000e*\u0004\u0018\u00010\r0\rX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u000f¨\u0006%"}, d2 = {"Lcom/nick/myrecoverytracker/DailyLightExposureWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "tag", "", "zone", "Ljava/time/ZoneId;", "csvDate", "Ljava/time/format/DateTimeFormatter;", "kotlin.jvm.PlatformType", "Ljava/time/format/DateTimeFormatter;", "tsFmt", "doWork", "Landroidx/work/ListenableWorker$Result;", "computeForDate", "", "src", "Ljava/io/File;", "date", "Ljava/time/LocalDate;", "safeParse", "Ljava/time/ZonedDateTime;", "s", "ensureHeader", "f", "header", "upsertRow", "", "file", "dateStr", "tailCols", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class DailyLightExposureWorker extends Worker {
    private final DateTimeFormatter csvDate;
    private final String tag;
    private final DateTimeFormatter tsFmt;
    private final ZoneId zone;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DailyLightExposureWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        this.tag = "DailyLightExposureWorker";
        ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
        Intrinsics.checkNotNullExpressionValue(zoneIdSystemDefault, "systemDefault(...)");
        this.zone = zoneIdSystemDefault;
        this.csvDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        this.tsFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    @Override // androidx.work.Worker
    public ListenableWorker.Result doWork() {
        DailyLightExposureWorker dailyLightExposureWorker = this;
        File filesDir = dailyLightExposureWorker.getApplicationContext().getFilesDir();
        File src = new File(filesDir, "ambient_lux.csv");
        if (!src.exists()) {
            ListenableWorker.Result resultSuccess = ListenableWorker.Result.success();
            Intrinsics.checkNotNullExpressionValue(resultSuccess, "success(...)");
            return resultSuccess;
        }
        File out = dailyLightExposureWorker.ensureHeader(new File(filesDir, "daily_light_exposure.csv"), "date,minutes");
        LocalDate today = LocalDate.now(dailyLightExposureWorker.zone);
        LocalDate yesterday = today.minusDays(1L);
        for (LocalDate localDate : CollectionsKt.listOf((Object[]) new LocalDate[]{yesterday, today})) {
            Intrinsics.checkNotNull(localDate);
            int iComputeForDate = dailyLightExposureWorker.computeForDate(src, localDate);
            String str = localDate.format(dailyLightExposureWorker.csvDate);
            Intrinsics.checkNotNullExpressionValue(str, "format(...)");
            dailyLightExposureWorker.upsertRow(out, str, CollectionsKt.listOf(String.valueOf(iComputeForDate)));
            Log.i(dailyLightExposureWorker.tag, "Light exposure " + localDate.format(dailyLightExposureWorker.csvDate) + " = " + iComputeForDate + "min");
            dailyLightExposureWorker = this;
            filesDir = filesDir;
        }
        ListenableWorker.Result resultSuccess2 = ListenableWorker.Result.success();
        Intrinsics.checkNotNullExpressionValue(resultSuccess2, "success(...)");
        return resultSuccess2;
    }

    private final int computeForDate(File src, LocalDate date) {
        final ZonedDateTime dayStart = date.atStartOfDay(this.zone);
        final ZonedDateTime dayEnd = date.plusDays(1L).atStartOfDay(this.zone);
        final ArrayList samples = new ArrayList();
        FilesKt.forEachLine$default(src, null, new Function1() { // from class: com.nick.myrecoverytracker.DailyLightExposureWorker$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return DailyLightExposureWorker.computeForDate$lambda$1(this.f$0, dayStart, dayEnd, samples, (String) obj);
            }
        }, 1, null);
        if (samples.isEmpty()) {
            return 0;
        }
        ArrayList arrayList = samples;
        if (arrayList.size() > 1) {
            CollectionsKt.sortWith(arrayList, new Comparator() { // from class: com.nick.myrecoverytracker.DailyLightExposureWorker$computeForDate$$inlined$sortBy$1
                /* JADX WARN: Multi-variable type inference failed */
                @Override // java.util.Comparator
                public final int compare(T t, T t2) {
                    return ComparisonsKt.compareValues(((ZonedDateTime) ((Pair) t).getFirst()).toInstant(), ((ZonedDateTime) ((Pair) t2).getFirst()).toInstant());
                }
            });
        }
        int i = 0;
        int size = samples.size();
        double minutes = 0.0d;
        while (i < size) {
            Object obj = samples.get(i);
            Intrinsics.checkNotNullExpressionValue(obj, "get(...)");
            Pair pair = (Pair) obj;
            ZonedDateTime t = (ZonedDateTime) pair.component1();
            float lux = ((Number) pair.component2()).floatValue();
            ZonedDateTime nextT = i < CollectionsKt.getLastIndex(samples) ? (ZonedDateTime) ((Pair) samples.get(i + 1)).getFirst() : dayEnd;
            double deltaMin = Duration.between(t, nextT).toMinutes();
            if (deltaMin >= 0.0d) {
                if (deltaMin > 30.0d) {
                    deltaMin = 30.0d;
                }
                if (lux >= 50.0f) {
                    minutes += deltaMin;
                }
            }
            i++;
        }
        return (int) RangesKt.coerceIn(minutes, 0.0d, 1440.0d);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit computeForDate$lambda$1(DailyLightExposureWorker this$0, ZonedDateTime $dayStart, ZonedDateTime $dayEnd, ArrayList $samples, String line) {
        Intrinsics.checkNotNullParameter(line, "line");
        if (StringsKt.isBlank(line) || StringsKt.startsWith$default(line, "timestamp,", false, 2, (Object) null)) {
            return Unit.INSTANCE;
        }
        if (line.length() < 19) {
            return Unit.INSTANCE;
        }
        String tsStr = line.substring(0, 19);
        Intrinsics.checkNotNullExpressionValue(tsStr, "substring(...)");
        String luxStr = StringsKt.trim((CharSequence) StringsKt.substringAfter(line, ',', "")).toString();
        ZonedDateTime zdt = this$0.safeParse(tsStr);
        if (zdt == null) {
            return Unit.INSTANCE;
        }
        if (zdt.isBefore($dayStart) || !zdt.isBefore($dayEnd)) {
            return Unit.INSTANCE;
        }
        Float floatOrNull = StringsKt.toFloatOrNull(luxStr);
        if (floatOrNull == null) {
            return Unit.INSTANCE;
        }
        float lux = floatOrNull.floatValue();
        $samples.add(TuplesKt.to(zdt, Float.valueOf(lux)));
        return Unit.INSTANCE;
    }

    /* JADX WARN: Type inference failed for: r0v5, types: [java.time.ZonedDateTime] */
    private final ZonedDateTime safeParse(String s) {
        try {
            return LocalDateTime.parse(s, this.tsFmt).atZone(this.zone);
        } catch (Throwable th) {
            return null;
        }
    }

    private final File ensureHeader(File f, String header) {
        if (!f.exists() || f.length() == 0) {
            File parentFile = f.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FilesKt.writeText$default(f, header + "\n", null, 2, null);
        }
        return f;
    }

    private final void upsertRow(File file, String dateStr, List<String> tailCols) {
        ArrayList lines = file.exists() ? CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(file, null, 1, null)) : new ArrayList();
        if (lines.isEmpty()) {
            return;
        }
        String header = (String) CollectionsKt.first(lines);
        boolean replaced = false;
        int i = 1;
        int size = lines.size();
        while (true) {
            if (i >= size) {
                break;
            }
            int idx = StringsKt.indexOf$default((CharSequence) lines.get(i), ',', 0, false, 6, (Object) null);
            String key = (String) lines.get(i);
            if (idx >= 0) {
                key = key.substring(0, idx);
                Intrinsics.checkNotNullExpressionValue(key, "substring(...)");
            }
            if (!Intrinsics.areEqual(key, dateStr)) {
                i++;
            } else {
                lines.set(i, dateStr + "," + CollectionsKt.joinToString$default(tailCols, ",", null, null, 0, null, null, 62, null));
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lines.add(dateStr + "," + CollectionsKt.joinToString$default(tailCols, ",", null, null, 0, null, null, 62, null));
        }
        FilesKt.writeText$default(file, CollectionsKt.joinToString$default(CollectionsKt.plus((Collection) CollectionsKt.listOf(header), (Iterable) CollectionsKt.drop(lines, 1)), "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }
}
