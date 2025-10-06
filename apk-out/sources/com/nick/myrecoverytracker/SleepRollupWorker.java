package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.ranges.RangesKt;
import kotlin.sequences.SequencesKt;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;

/* compiled from: SleepRollupWorker.kt */
@Metadata(d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\u0005\u0018\u0000 C2\u00020\u0001:\u0002BCB\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\u000f\u001a\u00020\u0010H\u0016J(\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0016H\u0002J \u0010\u0019\u001a\u00020\u00122\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u001d\u001a\u00020\u001eH\u0002J,\u0010\u001f\u001a\u0004\u0018\u00010\u001b2\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010 \u001a\u00020!H\u0002J4\u0010\"\u001a\u0004\u0018\u00010\u001b2\u0006\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u001c\u001a\u00020\u001b2\b\b\u0002\u0010 \u001a\u00020!H\u0002J\u0012\u0010#\u001a\u0004\u0018\u00010\u001e2\u0006\u0010$\u001a\u00020\u001eH\u0002J\u0012\u0010%\u001a\u0004\u0018\u00010\u001b2\u0006\u0010&\u001a\u00020\u001eH\u0002J\u0018\u0010'\u001a\u00020\u00162\u0006\u0010(\u001a\u00020\u00162\u0006\u0010)\u001a\u00020\u001eH\u0002J&\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\u00162\u0006\u0010-\u001a\u00020\u001e2\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u001e0/H\u0002J\u0018\u00100\u001a\u00020+2\u0006\u0010,\u001a\u00020\u00162\u0006\u00101\u001a\u000202H\u0002J\u0010\u00103\u001a\u00020+2\u0006\u0010,\u001a\u00020\u0016H\u0002J\u0016\u00104\u001a\u00020\u001e2\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u001e0/H\u0002J\u0010\u00106\u001a\u00020\u001e2\u0006\u0010&\u001a\u00020\u001eH\u0002J\u0018\u00107\u001a\u00020+2\u0006\u00108\u001a\u00020\u00162\u0006\u00109\u001a\u00020\u001eH\u0002J\f\u0010:\u001a\u00020\u001e*\u00020;H\u0002J\u0010\u0010<\u001a\u00020+2\u0006\u0010=\u001a\u00020\u001eH\u0002J\u0010\u0010>\u001a\u00020?2\u0006\u0010@\u001a\u00020?H\u0002J\u0010\u0010A\u001a\u00020\u001e2\u0006\u0010@\u001a\u00020?H\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0018\u0010\n\u001a\n \f*\u0004\u0018\u00010\u000b0\u000bX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\rR\u0018\u0010\u000e\u001a\n \f*\u0004\u0018\u00010\u000b0\u000bX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\r¨\u0006D"}, d2 = {"Lcom/nick/myrecoverytracker/SleepRollupWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "zone", "Ljava/time/ZoneId;", "fmtDate", "Ljava/time/format/DateTimeFormatter;", "kotlin.jvm.PlatformType", "Ljava/time/format/DateTimeFormatter;", "fmtTs", "doWork", "Landroidx/work/ListenableWorker$Result;", "computeForDate", "Lcom/nick/myrecoverytracker/SleepRollupWorker$SleepResult;", "date", "Ljava/time/LocalDate;", "fUnlock", "Ljava/io/File;", "fScreen", "fNotif", "finalize", "sleepZ", "Ljava/time/ZonedDateTime;", "wakeZ", "qual", "", "firstStrictWakeAfter4am", BuildConfig.BUILD_TYPE, "", "lastStrictSleepWith60mQuiet", "extractTs", "line", "safeParse", "s", "ensureHeader", "f", "header", "upsert", "", "file", "dateStr", "tailCols", "", "rotateByDate", "keepDays", "", "healDropUtcTodayIfBoth", "csvJoin", "cols", "csvEscape", "writeAtomic", "dst", "content", "formatHms", "Ljava/time/LocalTime;", "logI", NotificationCompat.CATEGORY_MESSAGE, "round2", "", "v", "to2", "SleepResult", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class SleepRollupWorker extends Worker {
    private static final String TAG = "SleepRollupWorker";
    private final DateTimeFormatter fmtDate;
    private final DateTimeFormatter fmtTs;
    private final ZoneId zone;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SleepRollupWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
        Intrinsics.checkNotNullExpressionValue(zoneIdSystemDefault, "systemDefault(...)");
        this.zone = zoneIdSystemDefault;
        this.fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        this.fmtTs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    @Override // androidx.work.Worker
    public ListenableWorker.Result doWork() {
        File dir = getApplicationContext().getFilesDir();
        Log.i(TAG, "filesDir=" + dir.getAbsolutePath());
        File fUnlock = new File(dir, "unlock_log.csv");
        File fScreen = new File(dir, "screen_log.csv");
        File fNotif = new File(dir, "notification_log.csv");
        File outSummary = ensureHeader(new File(dir, "daily_sleep_summary.csv"), "date,sleep_time,wake_time,duration_hours");
        File outDur = ensureHeader(new File(dir, "daily_sleep_duration.csv"), "date,hours");
        File outST = ensureHeader(new File(dir, "daily_sleep_time.csv"), "date,HH:MM:SS");
        File outWT = ensureHeader(new File(dir, "daily_wake_time.csv"), "date,HH:MM:SS");
        File outQ = ensureHeader(new File(dir, "daily_sleep_quality.csv"), "date,quality");
        LocalDate today = LocalDate.now(this.zone);
        int backfillDays = RangesKt.coerceIn(getInputData().getInt("backfill_days", 30), 1, 400);
        LocalDate startDate = today.minusDays(backfillDays);
        int processed = 0;
        String lastLog = null;
        LocalDate d = startDate;
        int wrote = 0;
        while (true) {
            LocalDate startDate2 = startDate;
            if (d.isAfter(today)) {
                healDropUtcTodayIfBoth(outSummary);
                healDropUtcTodayIfBoth(outDur);
                healDropUtcTodayIfBoth(outST);
                healDropUtcTodayIfBoth(outWT);
                healDropUtcTodayIfBoth(outQ);
                rotateByDate(outSummary, 400);
                rotateByDate(outDur, 400);
                rotateByDate(outST, 400);
                rotateByDate(outWT, 400);
                rotateByDate(outQ, 400);
                Log.i(TAG, "Sleep backfill_days=" + backfillDays + " processed=" + processed + " wrote=" + wrote + " last=\"" + lastLog + "\"");
                ListenableWorker.Result resultSuccess = ListenableWorker.Result.success();
                Intrinsics.checkNotNullExpressionValue(resultSuccess, "success(...)");
                return resultSuccess;
            }
            Intrinsics.checkNotNull(d);
            SleepResult r = computeForDate(d, fUnlock, fScreen, fNotif);
            File fUnlock2 = fUnlock;
            String str = d.format(this.fmtDate);
            File fScreen2 = fScreen;
            Intrinsics.checkNotNullExpressionValue(str, "format(...)");
            File fNotif2 = fNotif;
            String[] strArr = new String[3];
            String sleepTime = r.getSleepTime();
            if (sleepTime == null) {
                sleepTime = "";
            }
            strArr[0] = sleepTime;
            String wakeTime = r.getWakeTime();
            if (wakeTime == null) {
                wakeTime = "";
            }
            strArr[1] = wakeTime;
            int processed2 = processed;
            strArr[2] = to2(r.getHours());
            upsert(outSummary, str, CollectionsKt.listOf((Object[]) strArr));
            String str2 = d.format(this.fmtDate);
            Intrinsics.checkNotNullExpressionValue(str2, "format(...)");
            upsert(outDur, str2, CollectionsKt.listOf(to2(r.getHours())));
            String str3 = d.format(this.fmtDate);
            Intrinsics.checkNotNullExpressionValue(str3, "format(...)");
            String sleepTime2 = r.getSleepTime();
            if (sleepTime2 == null) {
                sleepTime2 = "";
            }
            upsert(outST, str3, CollectionsKt.listOf(sleepTime2));
            String str4 = d.format(this.fmtDate);
            Intrinsics.checkNotNullExpressionValue(str4, "format(...)");
            String wakeTime2 = r.getWakeTime();
            upsert(outWT, str4, CollectionsKt.listOf(wakeTime2 != null ? wakeTime2 : ""));
            String str5 = d.format(this.fmtDate);
            Intrinsics.checkNotNullExpressionValue(str5, "format(...)");
            upsert(outQ, str5, CollectionsKt.listOf(r.getQuality()));
            int processed3 = processed2 + 1;
            if (r.getSleepTime() != null || r.getWakeTime() != null || r.getHours() > 0.0d) {
                wrote++;
            }
            String str6 = d.format(this.fmtDate);
            String sleepTime3 = r.getSleepTime();
            String str7 = "-";
            if (sleepTime3 == null) {
                sleepTime3 = "-";
            }
            String wakeTime3 = r.getWakeTime();
            if (wakeTime3 != null) {
                str7 = wakeTime3;
            }
            lastLog = "Sleep " + str6 + " -> sleep=" + sleepTime3 + " wake=" + str7 + " hours=" + to2(r.getHours()) + " quality=" + r.getQuality();
            Intrinsics.checkNotNull(lastLog);
            Log.i(TAG, lastLog);
            d = d.plusDays(1L);
            startDate = startDate2;
            fUnlock = fUnlock2;
            fScreen = fScreen2;
            fNotif = fNotif2;
            processed = processed3;
            wrote = wrote;
        }
    }

    /* compiled from: SleepRollupWorker.kt */
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B+\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0003¢\u0006\u0004\b\b\u0010\tJ\u000b\u0010\u0010\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u0011\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\t\u0010\u0012\u001a\u00020\u0006HÆ\u0003J\t\u0010\u0013\u001a\u00020\u0003HÆ\u0003J5\u0010\u0014\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0018\u001a\u00020\u0019HÖ\u0001J\t\u0010\u001a\u001a\u00020\u0003HÖ\u0001R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u0011\u0010\u0005\u001a\u00020\u0006¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000b¨\u0006\u001b"}, d2 = {"Lcom/nick/myrecoverytracker/SleepRollupWorker$SleepResult;", "", "sleepTime", "", "wakeTime", "hours", "", "quality", "<init>", "(Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;)V", "getSleepTime", "()Ljava/lang/String;", "getWakeTime", "getHours", "()D", "getQuality", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    private static final /* data */ class SleepResult {
        private final double hours;
        private final String quality;
        private final String sleepTime;
        private final String wakeTime;

        public static /* synthetic */ SleepResult copy$default(SleepResult sleepResult, String str, String str2, double d, String str3, int i, Object obj) {
            if ((i & 1) != 0) {
                str = sleepResult.sleepTime;
            }
            if ((i & 2) != 0) {
                str2 = sleepResult.wakeTime;
            }
            if ((i & 4) != 0) {
                d = sleepResult.hours;
            }
            if ((i & 8) != 0) {
                str3 = sleepResult.quality;
            }
            String str4 = str3;
            return sleepResult.copy(str, str2, d, str4);
        }

        /* renamed from: component1, reason: from getter */
        public final String getSleepTime() {
            return this.sleepTime;
        }

        /* renamed from: component2, reason: from getter */
        public final String getWakeTime() {
            return this.wakeTime;
        }

        /* renamed from: component3, reason: from getter */
        public final double getHours() {
            return this.hours;
        }

        /* renamed from: component4, reason: from getter */
        public final String getQuality() {
            return this.quality;
        }

        public final SleepResult copy(String sleepTime, String wakeTime, double hours, String quality) {
            Intrinsics.checkNotNullParameter(quality, "quality");
            return new SleepResult(sleepTime, wakeTime, hours, quality);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof SleepResult)) {
                return false;
            }
            SleepResult sleepResult = (SleepResult) other;
            return Intrinsics.areEqual(this.sleepTime, sleepResult.sleepTime) && Intrinsics.areEqual(this.wakeTime, sleepResult.wakeTime) && Double.compare(this.hours, sleepResult.hours) == 0 && Intrinsics.areEqual(this.quality, sleepResult.quality);
        }

        public int hashCode() {
            return ((((((this.sleepTime == null ? 0 : this.sleepTime.hashCode()) * 31) + (this.wakeTime != null ? this.wakeTime.hashCode() : 0)) * 31) + Double.hashCode(this.hours)) * 31) + this.quality.hashCode();
        }

        public String toString() {
            return "SleepResult(sleepTime=" + this.sleepTime + ", wakeTime=" + this.wakeTime + ", hours=" + this.hours + ", quality=" + this.quality + ")";
        }

        public SleepResult(String sleepTime, String wakeTime, double hours, String quality) {
            Intrinsics.checkNotNullParameter(quality, "quality");
            this.sleepTime = sleepTime;
            this.wakeTime = wakeTime;
            this.hours = hours;
            this.quality = quality;
        }

        public final String getSleepTime() {
            return this.sleepTime;
        }

        public final String getWakeTime() {
            return this.wakeTime;
        }

        public final double getHours() {
            return this.hours;
        }

        public final String getQuality() {
            return this.quality;
        }
    }

    private final SleepResult computeForDate(LocalDate date, File fUnlock, File fScreen, File fNotif) {
        ZonedDateTime wakeZ = firstStrictWakeAfter4am(fUnlock, fScreen, date, true);
        if (wakeZ == null) {
            return new SleepResult(null, null, 0.0d, "NO_MORNING_WAKE");
        }
        ZonedDateTime sleepZ = lastStrictSleepWith60mQuiet(fScreen, fUnlock, date, wakeZ, true);
        if (sleepZ == null) {
            LocalTime localTime = wakeZ.toLocalTime();
            Intrinsics.checkNotNullExpressionValue(localTime, "toLocalTime(...)");
            return new SleepResult(null, formatHms(localTime), 0.0d, "NO_SLEEP_CANDIDATE");
        }
        return finalize(sleepZ, wakeZ, "OK");
    }

    private final SleepResult finalize(ZonedDateTime sleepZ, ZonedDateTime wakeZ, String qual) {
        double hrs = RangesKt.coerceAtLeast(Duration.between(sleepZ, wakeZ).getSeconds(), 0L) / 3600.0d;
        LocalTime localTime = sleepZ.toLocalTime();
        Intrinsics.checkNotNullExpressionValue(localTime, "toLocalTime(...)");
        String hms = formatHms(localTime);
        LocalTime localTime2 = wakeZ.toLocalTime();
        Intrinsics.checkNotNullExpressionValue(localTime2, "toLocalTime(...)");
        return new SleepResult(hms, formatHms(localTime2), round2(RangesKt.coerceIn(hrs, 0.0d, 12.0d)), qual);
    }

    static /* synthetic */ ZonedDateTime firstStrictWakeAfter4am$default(SleepRollupWorker sleepRollupWorker, File file, File file2, LocalDate localDate, boolean z, int i, Object obj) {
        if ((i & 8) != 0) {
            z = false;
        }
        return sleepRollupWorker.firstStrictWakeAfter4am(file, file2, localDate, z);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private final ZonedDateTime firstStrictWakeAfter4am(File fUnlock, File fScreen, LocalDate date, boolean debug) {
        List offs;
        ZonedDateTime zonedDateTime;
        Object next;
        Throwable th;
        String strExtractTs;
        ZonedDateTime zonedDateTimeSafeParse;
        String strExtractTs2;
        ZonedDateTime zonedDateTimeSafeParse2;
        SleepRollupWorker sleepRollupWorker = this;
        ChronoZonedDateTime<LocalDate> chronoZonedDateTimeAtZone = date.atTime(4, 0).atZone(sleepRollupWorker.zone);
        ZonedDateTime dayEnd = date.plusDays(1L).atStartOfDay(sleepRollupWorker.zone);
        List<ZonedDateTime> unlocks = new ArrayList();
        boolean z = true;
        if (fUnlock.exists()) {
            Reader inputStreamReader = new InputStreamReader(new FileInputStream(fUnlock), Charsets.UTF_8);
            BufferedReader bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
            try {
                for (String str : TextStreamsKt.lineSequence(bufferedReader)) {
                    if (StringsKt.endsWith(str, ",UNLOCK", z) && (strExtractTs2 = sleepRollupWorker.extractTs(str)) != null && (zonedDateTimeSafeParse2 = sleepRollupWorker.safeParse(strExtractTs2)) != null && !zonedDateTimeSafeParse2.isBefore(chronoZonedDateTimeAtZone) && zonedDateTimeSafeParse2.isBefore(dayEnd)) {
                        unlocks.add(zonedDateTimeSafeParse2);
                    }
                    z = true;
                }
                Unit unit = Unit.INSTANCE;
                CloseableKt.closeFinally(bufferedReader, null);
            } catch (Throwable th2) {
                try {
                    throw th2;
                } catch (Throwable th3) {
                    CloseableKt.closeFinally(bufferedReader, th2);
                    throw th3;
                }
            }
        }
        if (unlocks.size() > 1) {
            CollectionsKt.sortWith(unlocks, new Comparator() { // from class: com.nick.myrecoverytracker.SleepRollupWorker$firstStrictWakeAfter4am$$inlined$sortBy$1
                /* JADX WARN: Multi-variable type inference failed */
                @Override // java.util.Comparator
                public final int compare(T t, T t2) {
                    return ComparisonsKt.compareValues(Long.valueOf(((ZonedDateTime) t).toInstant().toEpochMilli()), Long.valueOf(((ZonedDateTime) t2).toInstant().toEpochMilli()));
                }
            });
        }
        List offs2 = new ArrayList();
        if (!fScreen.exists()) {
            offs = offs2;
            zonedDateTime = chronoZonedDateTimeAtZone;
        } else {
            Reader inputStreamReader2 = new InputStreamReader(new FileInputStream(fScreen), Charsets.UTF_8);
            BufferedReader bufferedReader2 = inputStreamReader2 instanceof BufferedReader ? (BufferedReader) inputStreamReader2 : new BufferedReader(inputStreamReader2, 8192);
            try {
                BufferedReader bufferedReader3 = bufferedReader2;
                for (String str2 : TextStreamsKt.lineSequence(bufferedReader3)) {
                    BufferedReader bufferedReader4 = bufferedReader3;
                    List offs3 = offs2;
                    ChronoZonedDateTime<LocalDate> chronoZonedDateTime = chronoZonedDateTimeAtZone;
                    try {
                        if ((StringsKt.endsWith(str2, ",SCREEN_OFF", true) || StringsKt.endsWith(str2, ",OFF", true)) && (strExtractTs = sleepRollupWorker.extractTs(str2)) != null && (zonedDateTimeSafeParse = sleepRollupWorker.safeParse(strExtractTs)) != null) {
                            offs3.add(zonedDateTimeSafeParse);
                        }
                        sleepRollupWorker = this;
                        chronoZonedDateTimeAtZone = chronoZonedDateTime;
                        bufferedReader3 = bufferedReader4;
                        offs2 = offs3;
                    } catch (Throwable th4) {
                        th = th4;
                        try {
                            throw th;
                        } catch (Throwable th5) {
                            CloseableKt.closeFinally(bufferedReader2, th);
                            throw th5;
                        }
                    }
                }
                offs = offs2;
                zonedDateTime = chronoZonedDateTimeAtZone;
                Unit unit2 = Unit.INSTANCE;
                CloseableKt.closeFinally(bufferedReader2, null);
            } catch (Throwable th6) {
                th = th6;
            }
        }
        List list = offs;
        if (list.size() > 1) {
            CollectionsKt.sortWith(list, new Comparator() { // from class: com.nick.myrecoverytracker.SleepRollupWorker$firstStrictWakeAfter4am$$inlined$sortBy$2
                /* JADX WARN: Multi-variable type inference failed */
                @Override // java.util.Comparator
                public final int compare(T t, T t2) {
                    return ComparisonsKt.compareValues(Long.valueOf(((ZonedDateTime) t).toInstant().toEpochMilli()), Long.valueOf(((ZonedDateTime) t2).toInstant().toEpochMilli()));
                }
            });
        }
        for (ZonedDateTime u : unlocks) {
            ZonedDateTime limit = u.plusSeconds(20L);
            Iterator it = offs.iterator();
            while (true) {
                if (it.hasNext()) {
                    next = it.next();
                    ZonedDateTime zonedDateTime2 = (ZonedDateTime) next;
                    if (((zonedDateTime2.isAfter(u) && zonedDateTime2.isBefore(limit)) ? 1 : null) != null) {
                        break;
                    }
                } else {
                    next = null;
                    break;
                }
            }
            ZonedDateTime offWithin20s = (ZonedDateTime) next;
            if (debug) {
                if (offWithin20s != null) {
                    long diff = Duration.between(u, offWithin20s).getSeconds();
                    Log.i(TAG, "WAKE_REJECT unlock=" + u + " reason=SCREEN_OFF at " + offWithin20s + " (+" + diff + "s < 20s)");
                } else {
                    Log.i(TAG, "WAKE_ACCEPT unlock=" + u + " (no SCREEN_OFF in next 20s)");
                }
            }
            if (offWithin20s == null) {
                return u;
            }
        }
        if (debug) {
            Log.i(TAG, "WAKE_NONE date=" + date + " reason=No UNLOCK in [" + zonedDateTime.toLocalTime() + "–" + dayEnd.toLocalTime() + ") that survives 20s");
            return null;
        }
        return null;
    }

    static /* synthetic */ ZonedDateTime lastStrictSleepWith60mQuiet$default(SleepRollupWorker sleepRollupWorker, File file, File file2, LocalDate localDate, ZonedDateTime zonedDateTime, boolean z, int i, Object obj) {
        boolean z2;
        if ((i & 16) == 0) {
            z2 = z;
        } else {
            z2 = false;
        }
        return sleepRollupWorker.lastStrictSleepWith60mQuiet(file, file2, localDate, zonedDateTime, z2);
    }

    /* JADX WARN: Removed duplicated region for block: B:25:0x00dd  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final java.time.ZonedDateTime lastStrictSleepWith60mQuiet(java.io.File r35, java.io.File r36, java.time.LocalDate r37, java.time.ZonedDateTime r38, boolean r39) {
        /*
            Method dump skipped, instructions count: 1260
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.SleepRollupWorker.lastStrictSleepWith60mQuiet(java.io.File, java.io.File, java.time.LocalDate, java.time.ZonedDateTime, boolean):java.time.ZonedDateTime");
    }

    /* compiled from: SleepRollupWorker.kt */
    @Metadata(d1 = {"\u0000)\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002*\u0001\u0000\b\u008a\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0005HÆ\u0003J\"\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005HÆ\u0001¢\u0006\u0002\u0010\u000fJ\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0013\u001a\u00020\u0014HÖ\u0001J\t\u0010\u0015\u001a\u00020\u0005HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b¨\u0006\u0016"}, d2 = {"com/nick/myrecoverytracker/SleepRollupWorker$lastStrictSleepWith60mQuiet$C", "", "z", "Ljava/time/ZonedDateTime;", "type", "", "<init>", "(Ljava/time/ZonedDateTime;Ljava/lang/String;)V", "getZ", "()Ljava/time/ZonedDateTime;", "getType", "()Ljava/lang/String;", "component1", "component2", "copy", "(Ljava/time/ZonedDateTime;Ljava/lang/String;)Lcom/nick/myrecoverytracker/SleepRollupWorker$lastStrictSleepWith60mQuiet$C;", "equals", "", "other", "hashCode", "", "toString", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final /* data */ class C {
        private final String type;
        private final ZonedDateTime z;

        public static /* synthetic */ C copy$default(C c, ZonedDateTime zonedDateTime, String str, int i, Object obj) {
            if ((i & 1) != 0) {
                zonedDateTime = c.z;
            }
            if ((i & 2) != 0) {
                str = c.type;
            }
            return c.copy(zonedDateTime, str);
        }

        /* renamed from: component1, reason: from getter */
        public final ZonedDateTime getZ() {
            return this.z;
        }

        /* renamed from: component2, reason: from getter */
        public final String getType() {
            return this.type;
        }

        public final C copy(ZonedDateTime z, String type) {
            Intrinsics.checkNotNullParameter(z, "z");
            Intrinsics.checkNotNullParameter(type, "type");
            return new C(z, type);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof C)) {
                return false;
            }
            C c = (C) other;
            return Intrinsics.areEqual(this.z, c.z) && Intrinsics.areEqual(this.type, c.type);
        }

        public int hashCode() {
            return (this.z.hashCode() * 31) + this.type.hashCode();
        }

        public String toString() {
            return "C(z=" + this.z + ", type=" + this.type + ")";
        }

        public C(ZonedDateTime z, String type) {
            Intrinsics.checkNotNullParameter(z, "z");
            Intrinsics.checkNotNullParameter(type, "type");
            this.z = z;
            this.type = type;
        }

        public final String getType() {
            return this.type;
        }

        public final ZonedDateTime getZ() {
            return this.z;
        }
    }

    private static final boolean lastStrictSleepWith60mQuiet$hasNoiseWithin60mAfter(ZonedDateTime $wakeZ, List<ZonedDateTime> list, List<ZonedDateTime> list2, ZonedDateTime anchor) {
        Iterable iterable;
        Iterable iterable2;
        ZonedDateTime zonedDateTimePlusMinutes = anchor.plusMinutes(60L);
        Intrinsics.checkNotNullExpressionValue(zonedDateTimePlusMinutes, "plusMinutes(...)");
        ZonedDateTime end = (ZonedDateTime) ComparisonsKt.minOf(zonedDateTimePlusMinutes, $wakeZ);
        List<ZonedDateTime> list3 = list;
        if (!(list3 instanceof Collection) || !list3.isEmpty()) {
            Iterator it = list3.iterator();
            while (true) {
                if (it.hasNext()) {
                    ZonedDateTime zonedDateTime = (ZonedDateTime) it.next();
                    if (((zonedDateTime.isAfter(anchor) && zonedDateTime.isBefore(end)) ? 1 : null) != null) {
                        iterable = 1;
                        break;
                    }
                } else {
                    iterable = null;
                    break;
                }
            }
        } else {
            iterable = null;
        }
        if (iterable != null) {
            return true;
        }
        List<ZonedDateTime> list4 = list2;
        if (!(list4 instanceof Collection) || !list4.isEmpty()) {
            Iterator it2 = list4.iterator();
            while (true) {
                if (it2.hasNext()) {
                    ZonedDateTime zonedDateTime2 = (ZonedDateTime) it2.next();
                    if (((zonedDateTime2.isAfter(anchor) && zonedDateTime2.isBefore(end)) ? 1 : null) != null) {
                        iterable2 = 1;
                        break;
                    }
                } else {
                    iterable2 = null;
                    break;
                }
            }
        } else {
            iterable2 = null;
        }
        return iterable2 != null;
    }

    private final String extractTs(String line) {
        String t = StringsKt.trim((CharSequence) line).toString();
        if ((t.length() == 0) || StringsKt.startsWith(t, "date,", true) || StringsKt.startsWith(t, "timestamp,", true)) {
            return null;
        }
        Iterable iterableSplit$default = StringsKt.split$default((CharSequence) t, new char[]{','}, false, 0, 6, (Object) null);
        Collection arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(iterableSplit$default, 10));
        Iterator it = iterableSplit$default.iterator();
        while (it.hasNext()) {
            arrayList.add(StringsKt.trim((CharSequence) it.next()).toString());
        }
        List parts = (List) arrayList;
        if (parts.isEmpty()) {
            return null;
        }
        if (parts.size() >= 1 && ((String) parts.get(0)).length() >= 19 && ((String) parts.get(0)).charAt(10) == ' ') {
            String strSubstring = ((String) parts.get(0)).substring(0, 19);
            Intrinsics.checkNotNullExpressionValue(strSubstring, "substring(...)");
            return strSubstring;
        }
        if (parts.size() >= 1 && ((String) parts.get(0)).length() >= 19 && ((String) parts.get(0)).charAt(10) == 'T') {
            String strSubstring2 = StringsKt.replace$default((String) parts.get(0), 'T', ' ', false, 4, (Object) null).substring(0, 19);
            Intrinsics.checkNotNullExpressionValue(strSubstring2, "substring(...)");
            return strSubstring2;
        }
        if (parts.size() < 2 || ((String) parts.get(0)).length() != 10 || ((String) parts.get(1)).length() < 8) {
            return null;
        }
        Object obj = parts.get(0);
        String strSubstring3 = ((String) parts.get(1)).substring(0, 8);
        Intrinsics.checkNotNullExpressionValue(strSubstring3, "substring(...)");
        return obj + " " + strSubstring3;
    }

    /* JADX WARN: Type inference failed for: r0v6, types: [java.time.ZonedDateTime] */
    private final ZonedDateTime safeParse(String s) {
        try {
            String strSubstring = s.substring(0, 19);
            Intrinsics.checkNotNullExpressionValue(strSubstring, "substring(...)");
            return LocalDateTime.parse(strSubstring, this.fmtTs).atZone(this.zone);
        } catch (Throwable th) {
            return null;
        }
    }

    private final File ensureHeader(File f, String header) {
        File parentFile = f.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        if (!f.exists()) {
            writeAtomic(f, header + "\n");
            Log.i(TAG, "CREATED " + f.getAbsolutePath());
        } else if (f.length() == 0) {
            writeAtomic(f, header + "\n");
            Log.i(TAG, "WROTE_HEADER " + f.getAbsolutePath());
        } else {
            Log.i(TAG, "EXISTS " + f.getAbsolutePath() + " size=" + f.length());
        }
        return f;
    }

    private final void upsert(File file, String dateStr, List<String> tailCols) {
        String str;
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
                str = dateStr;
                break;
            }
            int idx = StringsKt.indexOf$default((CharSequence) lines.get(i), ',', 0, false, 6, (Object) null);
            String key = (String) lines.get(i);
            if (idx >= 0) {
                key = key.substring(0, idx);
                Intrinsics.checkNotNullExpressionValue(key, "substring(...)");
            }
            str = dateStr;
            if (Intrinsics.areEqual(key, str)) {
                lines.set(i, csvJoin(CollectionsKt.plus((Collection) CollectionsKt.listOf(str), (Iterable) tailCols)));
                replaced = true;
                break;
            }
            i++;
        }
        if (!replaced) {
            lines.add(csvJoin(CollectionsKt.plus((Collection) CollectionsKt.listOf(str), (Iterable) tailCols)));
        }
        writeAtomic(file, CollectionsKt.joinToString$default(CollectionsKt.plus((Collection) CollectionsKt.listOf(header), (Iterable) CollectionsKt.drop(lines, 1)), "\n", null, null, 0, null, null, 62, null) + "\n");
    }

    /* JADX WARN: Removed duplicated region for block: B:29:0x00a1  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00a4 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final void rotateByDate(java.io.File r25, int r26) {
        /*
            Method dump skipped, instructions count: 236
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.SleepRollupWorker.rotateByDate(java.io.File, int):void");
    }

    private final void healDropUtcTodayIfBoth(File file) {
        boolean hasLocal;
        boolean hasLocal2;
        boolean hasLocal3;
        if (file.exists()) {
            List lines = FilesKt.readLines$default(file, null, 1, null);
            if (lines.isEmpty()) {
                return;
            }
            String header = (String) CollectionsKt.first(lines);
            Iterable body = CollectionsKt.toMutableList((Collection) CollectionsKt.drop(lines, 1));
            String localToday = LocalDate.now(this.zone).format(this.fmtDate);
            String utcToday = LocalDate.now(ZoneOffset.UTC).format(this.fmtDate);
            if (!Intrinsics.areEqual(localToday, utcToday)) {
                Iterable iterable = body;
                if ((iterable instanceof Collection) && ((Collection) iterable).isEmpty()) {
                    hasLocal = false;
                } else {
                    Iterator it = iterable.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            hasLocal = false;
                            break;
                        }
                        List lines2 = lines;
                        if (StringsKt.startsWith$default((String) it.next(), localToday + ",", false, 2, (Object) null)) {
                            hasLocal = true;
                            break;
                        }
                        lines = lines2;
                    }
                }
                Iterable iterable2 = body;
                if (!(iterable2 instanceof Collection) || !((Collection) iterable2).isEmpty()) {
                    Iterator it2 = iterable2.iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            hasLocal2 = hasLocal;
                            Iterable iterable3 = iterable2;
                            if (StringsKt.startsWith$default((String) it2.next(), utcToday + ",", false, 2, (Object) null)) {
                                hasLocal3 = true;
                                break;
                            } else {
                                hasLocal = hasLocal2;
                                iterable2 = iterable3;
                            }
                        } else {
                            hasLocal2 = hasLocal;
                            hasLocal3 = false;
                            break;
                        }
                    }
                } else {
                    hasLocal2 = hasLocal;
                    hasLocal3 = false;
                }
                if (hasLocal2 && hasLocal3) {
                    Iterable iterable4 = body;
                    int i = 0;
                    Collection arrayList = new ArrayList();
                    for (Object obj : iterable4) {
                        boolean hasUtc = hasLocal3;
                        Iterable iterable5 = iterable4;
                        int i2 = i;
                        String header2 = header;
                        if (!StringsKt.startsWith$default((String) obj, utcToday + ",", false, 2, (Object) null)) {
                            arrayList.add(obj);
                        }
                        i = i2;
                        header = header2;
                        hasLocal3 = hasUtc;
                        iterable4 = iterable5;
                    }
                    List pruned = (List) arrayList;
                    writeAtomic(file, SequencesKt.joinToString$default(SequencesKt.plus(SequencesKt.sequenceOf(header), CollectionsKt.asSequence(pruned)), "\n", null, null, 0, null, null, 62, null) + "\n");
                }
            }
        }
    }

    private final String csvJoin(List<String> cols) {
        return CollectionsKt.joinToString$default(cols, ",", null, null, 0, null, new Function1() { // from class: com.nick.myrecoverytracker.SleepRollupWorker$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return SleepRollupWorker.csvJoin$lambda$25(this.f$0, (String) obj);
            }
        }, 30, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence csvJoin$lambda$25(SleepRollupWorker this$0, String it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return this$0.csvEscape(it);
    }

    private final String csvEscape(String s) {
        boolean needs = true;
        if (s.length() == 0) {
            return "";
        }
        String str = s;
        int i = 0;
        while (true) {
            if (i < str.length()) {
                char cCharAt = str.charAt(i);
                if (((cCharAt == ',' || cCharAt == '\"' || cCharAt == '\n' || cCharAt == '\r') ? (char) 1 : (char) 0) != 0) {
                    break;
                }
                i++;
            } else {
                needs = false;
                break;
            }
        }
        if (needs) {
            return "\"" + StringsKt.replace$default(s, "\"", "\"\"", false, 4, (Object) null) + "\"";
        }
        return s;
    }

    private final void writeAtomic(File dst, String content) {
        File parentFile = dst.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        File tmp = new File(dst.getParentFile(), dst.getName() + ".tmp");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tmp);
            FileChannel ch = fos.getChannel();
            Intrinsics.checkNotNullExpressionValue(ch, "getChannel(...)");
            ch.truncate(0L);
            byte[] bytes = content.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            ch.write(ByteBuffer.wrap(bytes));
            ch.force(true);
            ch.close();
            try {
                fos.close();
            } catch (Throwable th) {
            }
            if (!tmp.renameTo(dst)) {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(dst, false);
                    try {
                        byte[] bytes2 = content.getBytes(Charsets.UTF_8);
                        Intrinsics.checkNotNullExpressionValue(bytes2, "getBytes(...)");
                        fileOutputStream.write(bytes2);
                        Unit unit = Unit.INSTANCE;
                        CloseableKt.closeFinally(fileOutputStream, null);
                        tmp.delete();
                    } finally {
                    }
                } catch (Throwable th2) {
                    dst.delete();
                    tmp.renameTo(dst);
                }
            }
        } catch (Throwable th3) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable th4) {
                }
            }
            throw th3;
        }
    }

    private final String formatHms(LocalTime $this$formatHms) {
        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
        String str = String.format(Locale.US, "%02d:%02d:%02d", Arrays.copyOf(new Object[]{Integer.valueOf($this$formatHms.getHour()), Integer.valueOf($this$formatHms.getMinute()), Integer.valueOf($this$formatHms.getSecond())}, 3));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    private final void logI(String msg) {
        Log.i(TAG, msg);
    }

    private final double round2(double v) {
        return Math.rint(v * 100.0d) / 100.0d;
    }

    private final String to2(double v) {
        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
        String str = String.format(Locale.US, "%.2f", Arrays.copyOf(new Object[]{Double.valueOf(v)}, 1));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }
}
