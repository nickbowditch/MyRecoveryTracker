package com.nick.myrecoverytracker;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.collections.SetsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.internal.LockFreeTaskQueueCore;

/* compiled from: UsageCaptureWorker.kt */
@Metadata(d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\"\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\t\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u0011\u001a\u00020\u0012H\u0096@¢\u0006\u0002\u0010\u0013J\u0018\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u0017\u001a\u00020\u000eH\u0002J\u0018\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u000eH\u0002J\u0018\u0010\u001a\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u001b\u001a\u00020\u000eH\u0002J \u0010\u001c\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u000e2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u001c\u0010 \u001a\u00020\u00152\u0012\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u001f0\"H\u0002J\u001c\u0010#\u001a\u00020\u00152\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020%0\"H\u0002J$\u0010&\u001a\u00020\u00152\u0006\u0010\u001d\u001a\u00020\u000e2\u0012\u0010'\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020(0\"H\u0002J\u0010\u0010)\u001a\u00020\u000e2\u0006\u0010*\u001a\u00020\u000eH\u0002J \u0010+\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000e2\u0006\u0010,\u001a\u00020\u000e2\u0006\u0010-\u001a\u00020\u000eH\u0002J\u0016\u0010.\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\u0006\u0010\u0016\u001a\u00020\u000eH\u0002J\u0018\u0010/\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000e2\u0006\u00100\u001a\u00020\u000eH\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rX\u0082\u0004¢\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0010X\u0082\u0004¢\u0006\u0002\n\u0000¨\u00061"}, d2 = {"Lcom/nick/myrecoverytracker/UsageCaptureWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "zone", "Ljava/time/ZoneId;", "tsFmt", "Ljava/text/SimpleDateFormat;", "categories", "", "", "excludedPkgs", "", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureHeader", "", "name", "header", "ensureHeaderExact", "expectedHeader", "appendBulk", "text", "upsertDailyCount", "day", "count", "", "upsertDailySwitching", "starts", "", "upsertDailyMinutes", "perPkgMillis", "", "writeOtherBreakdown", "otherAgg", "", "mapPackageToCategory", "pkg", "upsertByFirstColumn", "key", "line", "readLines", "writeAll", "content", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UsageCaptureWorker extends CoroutineWorker {
    private final List<String> categories;
    private final Set<String> excludedPkgs;
    private final SimpleDateFormat tsFmt;
    private final ZoneId zone;

    /* compiled from: UsageCaptureWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UsageCaptureWorker", f = "UsageCaptureWorker.kt", i = {}, l = {LockFreeTaskQueueCore.CLOSED_SHIFT}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UsageCaptureWorker$doWork$1, reason: invalid class name */
    static final class AnonymousClass1 extends ContinuationImpl {
        int label;
        /* synthetic */ Object result;

        AnonymousClass1(Continuation<? super AnonymousClass1> continuation) {
            super(continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return UsageCaptureWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public UsageCaptureWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
        Intrinsics.checkNotNullExpressionValue(zoneIdSystemDefault, "systemDefault(...)");
        this.zone = zoneIdSystemDefault;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        this.tsFmt = simpleDateFormat;
        this.categories = CollectionsKt.listOf((Object[]) new String[]{"app_min_social", "app_min_dating", "app_min_productivity", "app_min_music_audio", "app_min_image", "app_min_maps", "app_min_video", "app_min_travel_local", "app_min_shopping", "app_min_news", "app_min_game", "app_min_health", "app_min_finance", "app_min_browser", "app_min_comm", "app_min_other", "app_min_total"});
        this.excludedPkgs = SetsKt.setOf((Object[]) new String[]{"android", "com.android.systemui", "androidx.work.impl.foreground", "com.google.android.odad", BuildConfig.APPLICATION_ID});
    }

    /* compiled from: UsageCaptureWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UsageCaptureWorker$doWork$2", f = "UsageCaptureWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UsageCaptureWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return UsageCaptureWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            String typeStr;
            Object $result;
            UsageEvents ev;
            String pkg;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    Object $result2 = obj;
                    Object systemService = UsageCaptureWorker.this.getApplicationContext().getSystemService("usagestats");
                    Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.usage.UsageStatsManager");
                    UsageStatsManager usm = (UsageStatsManager) systemService;
                    ZonedDateTime now = ZonedDateTime.now(UsageCaptureWorker.this.zone);
                    long start = LocalDate.now(UsageCaptureWorker.this.zone).atStartOfDay(UsageCaptureWorker.this.zone).toInstant().toEpochMilli();
                    long end = now.toInstant().toEpochMilli();
                    UsageEvents ev2 = usm.queryEvents(start, end);
                    if (ev2.hasNextEvent()) {
                        UsageCaptureWorker.this.ensureHeader("usage_events.csv", "timestamp,package,event");
                        String lastFgPkg = null;
                        long lastFgStart = 0;
                        HashMap perPkgMillis = new HashMap();
                        HashMap perPkgStarts = new HashMap();
                        int totalEvents = 0;
                        UsageEvents.Event e = new UsageEvents.Event();
                        StringBuilder lines = new StringBuilder();
                        while (true) {
                            long end2 = end;
                            if (ev2.getNextEvent(e)) {
                                totalEvents++;
                                String tsLocal = UsageCaptureWorker.this.tsFmt.format(Boxing.boxLong(e.getTimeStamp()));
                                String pkg2 = e.getPackageName();
                                if (pkg2 == null) {
                                    pkg2 = "";
                                }
                                switch (e.getEventType()) {
                                    case 1:
                                        typeStr = "FOREGROUND";
                                        $result = $result2;
                                        ev = ev2;
                                        break;
                                    case 2:
                                        typeStr = "BACKGROUND";
                                        $result = $result2;
                                        ev = ev2;
                                        break;
                                    case 8:
                                        typeStr = "SHORTCUT_INVOCATION";
                                        $result = $result2;
                                        ev = ev2;
                                        break;
                                    case 12:
                                        typeStr = "NOTIFICATION_INTERRUPTION";
                                        $result = $result2;
                                        ev = ev2;
                                        break;
                                    case 15:
                                        typeStr = "SCREEN_INTERACTIVE";
                                        $result = $result2;
                                        ev = ev2;
                                        break;
                                    case 16:
                                        typeStr = "SCREEN_NON_INTERACTIVE";
                                        $result = $result2;
                                        ev = ev2;
                                        break;
                                    case 23:
                                        typeStr = "ACTIVITY_STOPPED";
                                        $result = $result2;
                                        ev = ev2;
                                        break;
                                    default:
                                        $result = $result2;
                                        ev = ev2;
                                        typeStr = "EVENT_" + e.getEventType();
                                        break;
                                }
                                lines.append(tsLocal).append(',').append(pkg2).append(',').append(typeStr).append('\n');
                                switch (e.getEventType()) {
                                    case 1:
                                        if (!(pkg2.length() > 0)) {
                                            end = end2;
                                            $result2 = $result;
                                            ev2 = ev;
                                            break;
                                        } else {
                                            HashMap map = perPkgStarts;
                                            Integer num = (Integer) perPkgStarts.get(pkg2);
                                            map.put(pkg2, Boxing.boxInt((num != null ? num.intValue() : 0) + 1));
                                            if (lastFgPkg == null || lastFgStart <= 0) {
                                                pkg = pkg2;
                                            } else {
                                                pkg = pkg2;
                                                long dur = Math.max(0L, e.getTimeStamp() - lastFgStart);
                                                HashMap map2 = perPkgMillis;
                                                Long l = (Long) perPkgMillis.get(lastFgPkg);
                                                map2.put(lastFgPkg, Boxing.boxLong((l != null ? l.longValue() : 0L) + dur));
                                            }
                                            lastFgPkg = pkg;
                                            lastFgStart = e.getTimeStamp();
                                            end = end2;
                                            $result2 = $result;
                                            ev2 = ev;
                                            break;
                                        }
                                    case 2:
                                    case 23:
                                        if (lastFgPkg == null || lastFgStart <= 0) {
                                            end = end2;
                                            $result2 = $result;
                                            ev2 = ev;
                                            break;
                                        } else {
                                            long dur2 = Math.max(0L, e.getTimeStamp() - lastFgStart);
                                            HashMap map3 = perPkgMillis;
                                            Long l2 = (Long) perPkgMillis.get(lastFgPkg);
                                            map3.put(lastFgPkg, Boxing.boxLong((l2 != null ? l2.longValue() : 0L) + dur2));
                                            lastFgPkg = null;
                                            lastFgStart = 0;
                                            end = end2;
                                            $result2 = $result;
                                            ev2 = ev;
                                            break;
                                        }
                                    default:
                                        end = end2;
                                        $result2 = $result;
                                        ev2 = ev;
                                        break;
                                }
                            } else {
                                if (lastFgPkg != null) {
                                    if (lastFgStart > 0) {
                                        long dur3 = Math.max(0L, end2 - lastFgStart);
                                        HashMap map4 = perPkgMillis;
                                        Long l3 = (Long) perPkgMillis.get(lastFgPkg);
                                        map4.put(lastFgPkg, Boxing.boxLong((l3 != null ? l3.longValue() : 0L) + dur3));
                                    }
                                }
                                UsageCaptureWorker usageCaptureWorker = UsageCaptureWorker.this;
                                String string = lines.toString();
                                Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
                                usageCaptureWorker.appendBulk("usage_events.csv", string);
                                UsageCaptureWorker usageCaptureWorker2 = UsageCaptureWorker.this;
                                String string2 = LocalDate.now(UsageCaptureWorker.this.zone).toString();
                                Intrinsics.checkNotNullExpressionValue(string2, "toString(...)");
                                usageCaptureWorker2.upsertDailyCount("daily_usage_events.csv", string2, totalEvents);
                                UsageCaptureWorker.this.upsertDailySwitching(perPkgStarts);
                                UsageCaptureWorker.this.upsertDailyMinutes(perPkgMillis);
                                return ListenableWorker.Result.success();
                            }
                        }
                    } else {
                        UsageCaptureWorker usageCaptureWorker3 = UsageCaptureWorker.this;
                        String string3 = LocalDate.now(UsageCaptureWorker.this.zone).toString();
                        Intrinsics.checkNotNullExpressionValue(string3, "toString(...)");
                        usageCaptureWorker3.upsertDailyCount("daily_usage_events.csv", string3, 0);
                        UsageCaptureWorker.this.upsertDailySwitching(MapsKt.emptyMap());
                        UsageCaptureWorker.this.upsertDailyMinutes(MapsKt.emptyMap());
                        return ListenableWorker.Result.success();
                    }
                    break;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:7:0x0014  */
    @Override // androidx.work.CoroutineWorker
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.Object doWork(kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> r8) throws java.lang.Throwable {
        /*
            r7 = this;
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.UsageCaptureWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.UsageCaptureWorker$doWork$1 r0 = (com.nick.myrecoverytracker.UsageCaptureWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.UsageCaptureWorker$doWork$1 r0 = new com.nick.myrecoverytracker.UsageCaptureWorker$doWork$1
            r0.<init>(r8)
        L19:
            java.lang.Object r1 = r0.result
            java.lang.Object r2 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r3 = r0.label
            switch(r3) {
                case 0: goto L31;
                case 1: goto L2c;
                default: goto L24;
            }
        L24:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "call to 'resume' before 'invoke' with coroutine"
            r0.<init>(r1)
            throw r0
        L2c:
            kotlin.ResultKt.throwOnFailure(r1)
            r3 = r1
            goto L4d
        L31:
            kotlin.ResultKt.throwOnFailure(r1)
            r3 = r7
            kotlinx.coroutines.CoroutineDispatcher r4 = kotlinx.coroutines.Dispatchers.getIO()
            kotlin.coroutines.CoroutineContext r4 = (kotlin.coroutines.CoroutineContext) r4
            com.nick.myrecoverytracker.UsageCaptureWorker$doWork$2 r5 = new com.nick.myrecoverytracker.UsageCaptureWorker$doWork$2
            r6 = 0
            r5.<init>(r6)
            kotlin.jvm.functions.Function2 r5 = (kotlin.jvm.functions.Function2) r5
            r6 = 1
            r0.label = r6
            java.lang.Object r3 = kotlinx.coroutines.BuildersKt.withContext(r4, r5, r0)
            if (r3 != r2) goto L4d
            return r2
        L4d:
            java.lang.String r2 = "withContext(...)"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r3, r2)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.UsageCaptureWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void ensureHeader(String name, String header) {
        File f = new File(getApplicationContext().getFilesDir(), name);
        if (!f.exists() || f.length() == 0) {
            FileOutputStream fileOutputStream = new FileOutputStream(f, false);
            try {
                byte[] bytes = (header + "\n").getBytes(Charsets.UTF_8);
                Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
                fileOutputStream.write(bytes);
                Unit unit = Unit.INSTANCE;
                CloseableKt.closeFinally(fileOutputStream, null);
            } catch (Throwable th) {
                try {
                    throw th;
                } catch (Throwable th2) {
                    CloseableKt.closeFinally(fileOutputStream, th);
                    throw th2;
                }
            }
        }
    }

    private final void ensureHeaderExact(String name, String expectedHeader) {
        FileOutputStream bufferedReader;
        String string;
        File f = new File(getApplicationContext().getFilesDir(), name);
        if (f.exists()) {
            Reader inputStreamReader = new InputStreamReader(new FileInputStream(f), Charsets.UTF_8);
            bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
            try {
                String line = bufferedReader.readLine();
                CloseableKt.closeFinally(bufferedReader, null);
                if (line == null || (string = StringsKt.trim((CharSequence) line).toString()) == null) {
                    string = "";
                }
                String current = string;
                if (Intrinsics.areEqual(current, expectedHeader)) {
                    return;
                }
                File backup = new File(getApplicationContext().getFilesDir(), name + ".legacy");
                try {
                    Result.Companion companion = Result.INSTANCE;
                    UsageCaptureWorker usageCaptureWorker = this;
                    Result.m212constructorimpl(FilesKt.copyTo$default(f, backup, true, 0, 4, null));
                } catch (Throwable th) {
                    Result.Companion companion2 = Result.INSTANCE;
                    Result.m212constructorimpl(ResultKt.createFailure(th));
                }
                bufferedReader = new FileOutputStream(f, false);
                try {
                    byte[] bytes = (expectedHeader + "\n").getBytes(Charsets.UTF_8);
                    Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
                    bufferedReader.write(bytes);
                    Unit unit = Unit.INSTANCE;
                    CloseableKt.closeFinally(bufferedReader, null);
                    return;
                } finally {
                    try {
                        throw th;
                    } finally {
                    }
                }
            } finally {
                try {
                    throw th;
                } finally {
                }
            }
        }
        bufferedReader = new FileOutputStream(f, false);
        try {
            byte[] bytes2 = (expectedHeader + "\n").getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes2, "getBytes(...)");
            bufferedReader.write(bytes2);
            Unit unit2 = Unit.INSTANCE;
            CloseableKt.closeFinally(bufferedReader, null);
        } finally {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void appendBulk(String name, String text) {
        if (text.length() == 0) {
            return;
        }
        File f = new File(getApplicationContext().getFilesDir(), name);
        FileOutputStream fileOutputStream = new FileOutputStream(f, true);
        try {
            byte[] bytes = text.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            fileOutputStream.write(bytes);
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(fileOutputStream, null);
        } finally {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void upsertDailyCount(String name, String day, int count) {
        ensureHeader(name, "date,count");
        upsertByFirstColumn(name, day, day + "," + count);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void upsertDailySwitching(Map<String, Integer> starts) {
        String str = "date,package,starts";
        ensureHeader("daily_app_switching.csv", "date,package,starts");
        String day = LocalDate.now(this.zone).toString();
        Intrinsics.checkNotNullExpressionValue(day, "toString(...)");
        Iterable lines = readLines("daily_app_switching.csv");
        Collection arrayList = new ArrayList();
        for (Object obj : lines) {
            String str2 = (String) obj;
            boolean z = false;
            if (StringsKt.startsWith$default(str2, day, false, 2, (Object) null) && !Intrinsics.areEqual(str2, "date,package,starts")) {
                z = true;
            }
            if (!z) {
                arrayList.add(obj);
            }
        }
        List all = (List) arrayList;
        Iterable<Map.Entry> iterableSortedWith = CollectionsKt.sortedWith(starts.entrySet(), new Comparator() { // from class: com.nick.myrecoverytracker.UsageCaptureWorker$upsertDailySwitching$$inlined$sortedByDescending$1
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return ComparisonsKt.compareValues((Integer) ((Map.Entry) t2).getValue(), (Integer) ((Map.Entry) t).getValue());
            }
        });
        Collection arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(iterableSortedWith, 10));
        for (Map.Entry entry : iterableSortedWith) {
            arrayList2.add(day + "," + ((String) entry.getKey()) + "," + ((Number) entry.getValue()).intValue());
            str = str;
        }
        String str3 = str;
        List newRows = (List) arrayList2;
        writeAll("daily_app_switching.csv", CollectionsKt.joinToString$default(CollectionsKt.plus((Collection) CollectionsKt.plus((Collection) CollectionsKt.listOf(str3), (Iterable) CollectionsKt.drop(all, 1)), (Iterable) newRows), "\n", null, null, 0, null, null, 62, null) + "\n");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void upsertDailyMinutes(Map<String, Long> perPkgMillis) {
        String name = "daily_app_usage_minutes.csv";
        String str = ",";
        String header = "date," + CollectionsKt.joinToString$default(this.categories, ",", null, null, 0, null, null, 62, null);
        ensureHeaderExact("daily_app_usage_minutes.csv", header);
        String day = LocalDate.now(this.zone).toString();
        Intrinsics.checkNotNullExpressionValue(day, "toString(...)");
        Map perCategoryRaw = new LinkedHashMap();
        for (String str2 : this.categories) {
            if (!Intrinsics.areEqual(str2, "app_min_total")) {
                perCategoryRaw.put(str2, Double.valueOf(0.0d));
            }
        }
        LinkedHashMap otherAggRaw = new LinkedHashMap();
        for (Map.Entry<String, Long> entry : perPkgMillis.entrySet()) {
            String rawPkg = entry.getKey();
            long millis = entry.getValue().longValue();
            if (millis > 0) {
                String pkg = StringsKt.trim((CharSequence) rawPkg).toString();
                if (!(pkg.length() == 0) && !this.excludedPkgs.contains(pkg)) {
                    String cat = mapPackageToCategory(pkg);
                    String name2 = name;
                    String str3 = str;
                    double minutes = millis / 60000.0d;
                    if (Intrinsics.areEqual(cat, "app_min_other")) {
                        LinkedHashMap linkedHashMap = otherAggRaw;
                        Double d = (Double) otherAggRaw.get(pkg);
                        linkedHashMap.put(pkg, Double.valueOf((d != null ? d.doubleValue() : 0.0d) + minutes));
                        name = name2;
                        str = str3;
                    } else {
                        Double d2 = (Double) perCategoryRaw.get(cat);
                        perCategoryRaw.put(cat, Double.valueOf((d2 != null ? d2.doubleValue() : 0.0d) + minutes));
                        name = name2;
                        str = str3;
                    }
                }
            }
        }
        String name3 = name;
        String str4 = str;
        Map perCategoryRounded = new LinkedHashMap();
        for (Map.Entry entry2 : perCategoryRaw.entrySet()) {
            String k = (String) entry2.getKey();
            double v = ((Number) entry2.getValue()).doubleValue();
            if (!Intrinsics.areEqual(k, "app_min_total") && !Intrinsics.areEqual(k, "app_min_other")) {
                perCategoryRounded.put(k, Double.valueOf(upsertDailyMinutes$round2(v)));
            }
        }
        LinkedHashMap otherAggRounded = new LinkedHashMap();
        for (Map.Entry entry3 : otherAggRaw.entrySet()) {
            String pkg2 = (String) entry3.getKey();
            double v2 = ((Number) entry3.getValue()).doubleValue();
            otherAggRounded.put(pkg2, Double.valueOf(upsertDailyMinutes$round2(v2)));
        }
        Iterable<Double> iterableValues = otherAggRounded.values();
        Intrinsics.checkNotNullExpressionValue(iterableValues, "<get-values>(...)");
        double otherMinutesRounded = 0.0d;
        for (Double d3 : iterableValues) {
            Intrinsics.checkNotNull(d3);
            otherMinutesRounded = upsertDailyMinutes$round2(otherMinutesRounded + d3.doubleValue());
        }
        perCategoryRounded.put("app_min_other", Double.valueOf(otherMinutesRounded));
        Iterable iterable = this.categories;
        Collection arrayList = new ArrayList();
        for (Object obj : iterable) {
            LinkedHashMap otherAggRounded2 = otherAggRounded;
            if (!Intrinsics.areEqual((String) obj, "app_min_total")) {
                arrayList.add(obj);
            }
            otherAggRounded = otherAggRounded2;
        }
        LinkedHashMap otherAggRounded3 = otherAggRounded;
        Iterable iterable2 = (List) arrayList;
        Collection arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(iterable2, 10));
        Iterator it = iterable2.iterator();
        while (it.hasNext()) {
            Double d4 = (Double) perCategoryRounded.get((String) it.next());
            arrayList2.add(Double.valueOf(d4 != null ? d4.doubleValue() : 0.0d));
        }
        double totalRounded = 0.0d;
        Iterator it2 = ((List) arrayList2).iterator();
        while (it2.hasNext()) {
            totalRounded = upsertDailyMinutes$round2(totalRounded + ((Number) it2.next()).doubleValue());
        }
        perCategoryRounded.put("app_min_total", Double.valueOf(totalRounded));
        StringBuilder sb = new StringBuilder();
        sb.append(day);
        for (String str5 : this.categories) {
            StringBuilder sb2 = sb;
            String header2 = header;
            String header3 = str4;
            sb.append(header3);
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            str4 = header3;
            Locale locale = Locale.US;
            Double d5 = (Double) perCategoryRounded.get(str5);
            Map perCategoryRounded2 = perCategoryRounded;
            String str6 = String.format(locale, "%.2f", Arrays.copyOf(new Object[]{Double.valueOf(d5 != null ? d5.doubleValue() : 0.0d)}, 1));
            Intrinsics.checkNotNullExpressionValue(str6, "format(...)");
            sb.append(str6);
            sb = sb2;
            header = header2;
            perCategoryRounded = perCategoryRounded2;
        }
        String row = sb.toString();
        Intrinsics.checkNotNullExpressionValue(row, "toString(...)");
        upsertByFirstColumn(name3, day, row);
        writeOtherBreakdown(day, otherAggRounded3);
    }

    private static final double upsertDailyMinutes$round2(double v) {
        return Math.rint(v * 100.0d) / 100.0d;
    }

    private final void writeOtherBreakdown(String day, Map<String, Double> otherAgg) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbAppend = sb.append("date,package,minutes");
        Intrinsics.checkNotNullExpressionValue(sbAppend, "append(...)");
        Intrinsics.checkNotNullExpressionValue(sbAppend.append('\n'), "append(...)");
        for (Map.Entry entry : CollectionsKt.sortedWith(otherAgg.entrySet(), new Comparator() { // from class: com.nick.myrecoverytracker.UsageCaptureWorker$writeOtherBreakdown$lambda$19$$inlined$sortedByDescending$1
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                return ComparisonsKt.compareValues((Double) ((Map.Entry) t2).getValue(), (Double) ((Map.Entry) t).getValue());
            }
        })) {
            String str = (String) entry.getKey();
            String str2 = String.format(Locale.US, "%.2f", Arrays.copyOf(new Object[]{Double.valueOf(((Number) entry.getValue()).doubleValue())}, 1));
            Intrinsics.checkNotNullExpressionValue(str2, "format(...)");
            StringBuilder sbAppend2 = sb.append(day + "," + str + "," + str2);
            Intrinsics.checkNotNullExpressionValue(sbAppend2, "append(...)");
            Intrinsics.checkNotNullExpressionValue(sbAppend2.append('\n'), "append(...)");
        }
        String content = sb.toString();
        Intrinsics.checkNotNullExpressionValue(content, "toString(...)");
        File dir = getApplicationContext().getFilesDir();
        File tmp = new File(dir, "daily_app_usage_other.csv.tmp");
        FileOutputStream fileOutputStream = new FileOutputStream(tmp, false);
        try {
            byte[] bytes = content.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            fileOutputStream.write(bytes);
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(fileOutputStream, null);
            File f = new File(dir, "daily_app_usage_other.csv");
            if (f.exists()) {
                f.delete();
            }
            tmp.renameTo(f);
        } finally {
        }
    }

    private final String mapPackageToCategory(String pkg) {
        Locale US = Locale.US;
        Intrinsics.checkNotNullExpressionValue(US, "US");
        String p = pkg.toLowerCase(US);
        Intrinsics.checkNotNullExpressionValue(p, "toLowerCase(...)");
        if (Intrinsics.areEqual(p, "com.android.chrome") || StringsKt.startsWith$default(p, "org.chromium.webapk", false, 2, (Object) null)) {
            return "app_min_browser";
        }
        if (Intrinsics.areEqual(p, "com.google.android.gm") || Intrinsics.areEqual(p, "com.google.android.apps.nexuslauncher") || Intrinsics.areEqual(p, "com.google.android.deskclock")) {
            return "app_min_productivity";
        }
        if (Intrinsics.areEqual(p, "com.google.android.dialer") || Intrinsics.areEqual(p, "com.textra")) {
            return "app_min_comm";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "textra", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "dialer", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "messaging", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "contacts", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "phone", false, 2, (Object) null)) {
            return "app_min_comm";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "facebook", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "instagram", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "twitter", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "snapchat", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "tiktok", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "telegram", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "whatsapp", false, 2, (Object) null)) {
            return "app_min_social";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "tinder", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "bumble", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "hinge", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "grindr", false, 2, (Object) null)) {
            return "app_min_dating";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "docs", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "sheets", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "slides", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "office", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "notion", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "keep", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "calendar", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "gmail", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "gm", false, 2, (Object) null)) {
            return "app_min_productivity";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "spotify", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "music", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "podcast", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "soundcloud", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "pocketcasts", false, 2, (Object) null)) {
            return "app_min_music_audio";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "camera", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "gallery", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "photos", false, 2, (Object) null)) {
            return "app_min_image";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "maps", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "waze", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "map", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "uber", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "lyft", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "airbnb", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "booking", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "trip", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "transit", false, 2, (Object) null)) {
            return "app_min_travel_local";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "youtube", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "netflix", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "primevideo", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "disney", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "stan", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "twitch", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "iplayer", false, 2, (Object) null)) {
            return "app_min_video";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "amazon", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "ebay", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "shop", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "shopping", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "etsy", false, 2, (Object) null)) {
            return "app_min_shopping";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "news", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "bbc", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "cnn", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "guardian", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "nyt", false, 2, (Object) null)) {
            return "app_min_news";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "game", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "playgames", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "supercell", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "riot", false, 2, (Object) null)) {
            return "app_min_game";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "fit", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "health", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "run", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "strava", false, 2, (Object) null)) {
            return "app_min_health";
        }
        if (StringsKt.contains$default((CharSequence) p, (CharSequence) "bank", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "paypal", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "finance", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "revolut", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "wise", false, 2, (Object) null)) {
            return "app_min_finance";
        }
        return (StringsKt.contains$default((CharSequence) p, (CharSequence) "chrome", false, 2, (Object) null) || StringsKt.contains$default((CharSequence) p, (CharSequence) "browser", false, 2, (Object) null) || StringsKt.startsWith$default(p, "org.chromium", false, 2, (Object) null)) ? "app_min_browser" : "app_min_other";
    }

    private final void upsertByFirstColumn(String name, String key, String line) {
        List lines = CollectionsKt.toMutableList((Collection) readLines(name));
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
            String d = StringsKt.substringBefore$default((String) lines.get(i), ',', (String) null, 2, (Object) null);
            if (!Intrinsics.areEqual(d, key)) {
                i++;
            } else {
                lines.set(i, line);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lines.add(line);
        }
        writeAll(name, CollectionsKt.joinToString$default(CollectionsKt.plus((Collection) CollectionsKt.listOf(header), (Iterable) CollectionsKt.drop(lines, 1)), "\n", null, null, 0, null, null, 62, null) + "\n");
    }

    private final List<String> readLines(String name) {
        File f = new File(getApplicationContext().getFilesDir(), name);
        if (!f.exists()) {
            return CollectionsKt.emptyList();
        }
        Iterable lines = FilesKt.readLines(f, Charsets.UTF_8);
        Collection arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(lines, 10));
        Iterator it = lines.iterator();
        while (it.hasNext()) {
            arrayList.add(StringsKt.trimEnd((String) it.next(), '\n', '\r'));
        }
        return (List) arrayList;
    }

    private final void writeAll(String name, String content) {
        File f = new File(getApplicationContext().getFilesDir(), name);
        FileOutputStream fileOutputStream = new FileOutputStream(f, false);
        try {
            byte[] bytes = content.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            fileOutputStream.write(bytes);
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(fileOutputStream, null);
        } finally {
        }
    }
}
