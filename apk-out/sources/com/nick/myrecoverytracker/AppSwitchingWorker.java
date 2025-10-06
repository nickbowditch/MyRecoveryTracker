package com.nick.myrecoverytracker;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: AppSwitchingWorker.kt */
@Metadata(d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010 \n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u00192\u00020\u0001:\u0001\u0019B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0002J \u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\fH\u0002J\b\u0010\u0016\u001a\u00020\u0013H\u0002J\u0014\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f0\u0018H\u0002¨\u0006\u001a"}, d2 = {"Lcom/nick/myrecoverytracker/AppSwitchingWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "medianSeconds", "", "durationsMs", "", "", "writeRow", "", "day", "", "switchCount", "", "today", "todayRange", "Lkotlin/Pair;", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class AppSwitchingWorker extends CoroutineWorker {
    private static final String TAG = "AppSwitchingWorker";

    /* compiled from: AppSwitchingWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AppSwitchingWorker", f = "AppSwitchingWorker.kt", i = {}, l = {28}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AppSwitchingWorker$doWork$1, reason: invalid class name */
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
            return AppSwitchingWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AppSwitchingWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: AppSwitchingWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AppSwitchingWorker$doWork$2", f = "AppSwitchingWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AppSwitchingWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return AppSwitchingWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    Context ctx = AppSwitchingWorker.this.getApplicationContext();
                    Intrinsics.checkNotNullExpressionValue(ctx, "getApplicationContext(...)");
                    if (!UsagePermissionHelper.INSTANCE.isGranted(ctx)) {
                        AppSwitchingWorker.this.writeRow(AppSwitchingWorker.this.today(), 0, 0.0d);
                        return ListenableWorker.Result.success();
                    }
                    Pair pair = AppSwitchingWorker.this.todayRange();
                    long currentStart = ((Number) pair.component1()).longValue();
                    long end = ((Number) pair.component2()).longValue();
                    Object systemService = ctx.getSystemService("usagestats");
                    Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.usage.UsageStatsManager");
                    UsageStatsManager usm = (UsageStatsManager) systemService;
                    UsageEvents events = usm.queryEvents(currentStart, end);
                    String currentPkg = null;
                    int switchCount = 0;
                    ArrayList sessions = new ArrayList();
                    UsageEvents.Event ev = new UsageEvents.Event();
                    while (events.hasNextEvent()) {
                        events.getNextEvent(ev);
                        String pkg = ev.getPackageName();
                        if (pkg != null) {
                            switch (ev.getEventType()) {
                                case 1:
                                    if (currentPkg != null && currentStart > 0) {
                                        long dur = ev.getTimeStamp() - currentStart;
                                        if (dur > 0) {
                                            sessions.add(Boxing.boxLong(dur));
                                        }
                                    }
                                    if (currentPkg != null && !Intrinsics.areEqual(currentPkg, pkg)) {
                                        switchCount++;
                                    }
                                    currentPkg = pkg;
                                    currentStart = ev.getTimeStamp();
                                    break;
                                case 2:
                                case 23:
                                    if (Intrinsics.areEqual(currentPkg, pkg) && currentStart > 0) {
                                        long dur2 = ev.getTimeStamp() - currentStart;
                                        if (dur2 > 0) {
                                            sessions.add(Boxing.boxLong(dur2));
                                        }
                                        currentPkg = null;
                                        currentStart = 0;
                                        break;
                                    } else {
                                        break;
                                    }
                                    break;
                            }
                        }
                    }
                    if (currentPkg != null && currentStart > 0) {
                        long dur3 = end - currentStart;
                        if (dur3 > 0) {
                            sessions.add(Boxing.boxLong(dur3));
                        }
                    }
                    double medianSec = sessions.isEmpty() ? 0.0d : AppSwitchingWorker.this.medianSeconds(sessions);
                    String day = AppSwitchingWorker.this.today();
                    AppSwitchingWorker.this.writeRow(day, switchCount, medianSec);
                    Log.i(AppSwitchingWorker.TAG, "AppSwitching " + day + ": switch_count=" + switchCount + " median_session_seconds=" + medianSec + " (nSessions=" + sessions.size() + ")");
                    return ListenableWorker.Result.success();
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.AppSwitchingWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.AppSwitchingWorker$doWork$1 r0 = (com.nick.myrecoverytracker.AppSwitchingWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.AppSwitchingWorker$doWork$1 r0 = new com.nick.myrecoverytracker.AppSwitchingWorker$doWork$1
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
            com.nick.myrecoverytracker.AppSwitchingWorker$doWork$2 r5 = new com.nick.myrecoverytracker.AppSwitchingWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.AppSwitchingWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final double medianSeconds(List<Long> durationsMs) {
        if (durationsMs.isEmpty()) {
            return 0.0d;
        }
        List sorted = CollectionsKt.sorted(durationsMs);
        int n = sorted.size();
        if (n % 2 == 1) {
            return ((Number) sorted.get(n / 2)).doubleValue() / 1000.0d;
        }
        return ((((Number) sorted.get((n / 2) - 1)).doubleValue() / 1000.0d) + (((Number) sorted.get(n / 2)).doubleValue() / 1000.0d)) / 2.0d;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void writeRow(String day, int switchCount, double medianSeconds) {
        File out = new File(getApplicationContext().getFilesDir(), "daily_app_switching.csv");
        List lines = out.exists() ? CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(out, null, 1, null)) : CollectionsKt.mutableListOf("date,switch_count,median_session_seconds");
        Collection arrayList = new ArrayList();
        for (Object obj : lines) {
            if (!StringsKt.startsWith$default((String) obj, day + ",", false, 2, (Object) null)) {
                arrayList.add(obj);
            }
        }
        List filtered = CollectionsKt.toMutableList(arrayList);
        double medianRounded = Math.floor(medianSeconds * 10.0d) / 10.0d;
        filtered.add(day + "," + switchCount + "," + medianRounded);
        FilesKt.writeText$default(out, CollectionsKt.joinToString$default(filtered, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String today() {
        String str = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Pair<Long, Long> todayRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        long start = cal.getTimeInMillis();
        long end = System.currentTimeMillis();
        return TuplesKt.to(Long.valueOf(start), Long.valueOf(end));
    }
}
