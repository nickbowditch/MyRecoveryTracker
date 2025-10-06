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
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;

/* compiled from: LateNightScreenRollupWorker.kt */
@Metadata(d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\u0018\u0000 '2\u00020\u0001:\u0001'B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\u000f\u001a\u00020\u0010H\u0016J \u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0016H\u0002J\u0012\u0010\u0018\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u001a\u001a\u00020\u0019H\u0002J\u0012\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\u0006\u0010\u001d\u001a\u00020\u0019H\u0002J\u0018\u0010\u001e\u001a\u00020\u00162\u0006\u0010\u001f\u001a\u00020\u00162\u0006\u0010 \u001a\u00020\u0019H\u0002J&\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\u00162\u0006\u0010$\u001a\u00020\u00192\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00190&H\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0018\u0010\n\u001a\n \f*\u0004\u0018\u00010\u000b0\u000bX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\rR\u0018\u0010\u000e\u001a\n \f*\u0004\u0018\u00010\u000b0\u000bX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\r¨\u0006("}, d2 = {"Lcom/nick/myrecoverytracker/LateNightScreenRollupWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "zone", "Ljava/time/ZoneId;", "fmtDate", "Ljava/time/format/DateTimeFormatter;", "kotlin.jvm.PlatformType", "Ljava/time/format/DateTimeFormatter;", "fmtTs", "doWork", "Landroidx/work/ListenableWorker$Result;", "hadNightActivity", "", "date", "Ljava/time/LocalDate;", "screen", "Ljava/io/File;", "unlock", "extractTs", "", "line", "safeTs", "Ljava/time/ZonedDateTime;", "s", "ensureHeader", "f", "header", "upsert", "", "file", "dateStr", "tailCols", "", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class LateNightScreenRollupWorker extends Worker {
    private static final String TAG = "LateNightScreenRollup";
    private final DateTimeFormatter fmtDate;
    private final DateTimeFormatter fmtTs;
    private final ZoneId zone;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public LateNightScreenRollupWorker(Context appContext, WorkerParameters params) {
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
        LateNightScreenRollupWorker lateNightScreenRollupWorker = this;
        File dir = lateNightScreenRollupWorker.getApplicationContext().getFilesDir();
        File out = lateNightScreenRollupWorker.ensureHeader(new File(dir, "daily_late_night_screen_usage.csv"), "date,late_night_YN");
        File fScreen = new File(dir, "screen_log.csv");
        File fUnlock = new File(dir, "unlock_log.csv");
        LocalDate today = LocalDate.now(lateNightScreenRollupWorker.zone);
        LocalDate yesterday = today.minusDays(1L);
        for (LocalDate localDate : CollectionsKt.listOf((Object[]) new LocalDate[]{yesterday, today})) {
            Intrinsics.checkNotNull(localDate);
            boolean zHadNightActivity = lateNightScreenRollupWorker.hadNightActivity(localDate, fScreen, fUnlock);
            String str = localDate.format(lateNightScreenRollupWorker.fmtDate);
            Intrinsics.checkNotNullExpressionValue(str, "format(...)");
            String str2 = "Y";
            File dir2 = dir;
            lateNightScreenRollupWorker.upsert(out, str, CollectionsKt.listOf(zHadNightActivity ? "Y" : "N"));
            String str3 = localDate.format(lateNightScreenRollupWorker.fmtDate);
            if (!zHadNightActivity) {
                str2 = "N";
            }
            Log.i(TAG, "LateNight " + str3 + " -> " + str2);
            lateNightScreenRollupWorker = this;
            dir = dir2;
        }
        ListenableWorker.Result resultSuccess = ListenableWorker.Result.success();
        Intrinsics.checkNotNullExpressionValue(resultSuccess, "success(...)");
        return resultSuccess;
    }

    /* JADX WARN: Type inference failed for: r1v2, types: [java.time.ZonedDateTime] */
    private final boolean hadNightActivity(LocalDate date, File screen, File unlock) {
        ZonedDateTime startZ = date.atStartOfDay(this.zone);
        ?? AtZone = date.atTime(5, 0).atZone(this.zone);
        boolean screenHit = hadNightActivity$fileHit(this, startZ, AtZone, screen, new Function1() { // from class: com.nick.myrecoverytracker.LateNightScreenRollupWorker$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return Boolean.valueOf(LateNightScreenRollupWorker.hadNightActivity$lambda$2((String) obj));
            }
        });
        boolean unlockHit = hadNightActivity$fileHit(this, startZ, AtZone, unlock, new Function1() { // from class: com.nick.myrecoverytracker.LateNightScreenRollupWorker$$ExternalSyntheticLambda1
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return Boolean.valueOf(LateNightScreenRollupWorker.hadNightActivity$lambda$3((String) obj));
            }
        });
        return screenHit || unlockHit;
    }

    private static final boolean hadNightActivity$inWindow(ZonedDateTime startZ, ZonedDateTime endZ, ZonedDateTime z) {
        return !z.isBefore(startZ) && z.isBefore(endZ);
    }

    private static final boolean hadNightActivity$fileHit(LateNightScreenRollupWorker this$0, ZonedDateTime startZ, ZonedDateTime endZ, File file, Function1<? super String, Boolean> function1) {
        ZonedDateTime zonedDateTimeSafeTs;
        if (!file.exists()) {
            return false;
        }
        Reader inputStreamReader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
        BufferedReader bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
        try {
            BufferedReader bufferedReader2 = bufferedReader;
            while (true) {
                String line = bufferedReader2.readLine();
                if (line == null) {
                    Unit unit = Unit.INSTANCE;
                    CloseableKt.closeFinally(bufferedReader, null);
                    return false;
                }
                String strExtractTs = this$0.extractTs(line);
                if (strExtractTs != null && (zonedDateTimeSafeTs = this$0.safeTs(strExtractTs)) != null && hadNightActivity$inWindow(startZ, endZ, zonedDateTimeSafeTs) && function1.invoke(line).booleanValue()) {
                    CloseableKt.closeFinally(bufferedReader, null);
                    return true;
                }
            }
        } finally {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean hadNightActivity$lambda$2(String it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return StringsKt.endsWith(it, ",ON", true) || StringsKt.endsWith(it, ",OFF", true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean hadNightActivity$lambda$3(String it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return true;
    }

    private final String extractTs(String line) {
        if (StringsKt.isBlank(line) || StringsKt.startsWith(line, "ts,", true) || line.length() < 19) {
            return null;
        }
        String strSubstring = line.substring(0, 19);
        Intrinsics.checkNotNullExpressionValue(strSubstring, "substring(...)");
        return strSubstring;
    }

    /* JADX WARN: Type inference failed for: r0v5, types: [java.time.ZonedDateTime] */
    private final ZonedDateTime safeTs(String s) {
        try {
            return LocalDateTime.parse(s, this.fmtTs).atZone(this.zone);
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

    private final void upsert(File file, String dateStr, List<String> tailCols) {
        ArrayList lines = file.exists() ? CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(file, null, 1, null)) : new ArrayList();
        if (lines.isEmpty()) {
            FilesKt.writeText$default(file, "date,late_night_YN\n" + dateStr + "," + CollectionsKt.joinToString$default(tailCols, ",", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
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
            String key = StringsKt.substringBefore$default((String) lines.get(i), ',', (String) null, 2, (Object) null);
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
