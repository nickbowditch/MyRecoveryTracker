package com.nick.myrecoverytracker;

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
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: ScreenOnDailyWorker.kt */
@Metadata(d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0007\u0018\u0000 \"2\u00020\u0001:\u0001\"B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\nJ\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002J \u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\fH\u0002J\"\u0010\u0013\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00160\u00150\u00142\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J(\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001c2\u0006\u0010\u001e\u001a\u00020\u001c2\u0006\u0010\u001f\u001a\u00020\u001cH\u0002J\u0010\u0010 \u001a\u00020\u001a2\u0006\u0010!\u001a\u00020\u0018H\u0002¨\u0006#"}, d2 = {"Lcom/nick/myrecoverytracker/ScreenOnDailyWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "dayStartMillis", "", "calNow", "Ljava/util/Calendar;", "overlap", "start", "end", "winEnd", "parseEvents", "", "Lkotlin/Pair;", "", "file", "Ljava/io/File;", "appendOrReplaceToday", "", "early", "", "diurnal", "late", "total", "ensureHeader", "out", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class ScreenOnDailyWorker extends CoroutineWorker {
    private static final long DAY = 86400000;
    private static final long H06 = 21600000;
    private static final long H22 = 79200000;
    private static final String HEADER = "date,early_minutes,diurnal_minutes,late_minutes,total_minutes";
    private static final String OUT = "daily_screen_distribution.csv";
    private static final String TAG = "ScreenOnDailyWorker";

    /* compiled from: ScreenOnDailyWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.ScreenOnDailyWorker", f = "ScreenOnDailyWorker.kt", i = {}, l = {21}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.ScreenOnDailyWorker$doWork$1, reason: invalid class name */
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
            return ScreenOnDailyWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ScreenOnDailyWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: ScreenOnDailyWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.ScreenOnDailyWorker$doWork$2", f = "ScreenOnDailyWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.ScreenOnDailyWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        private /* synthetic */ Object L$0;
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            AnonymousClass2 anonymousClass2 = ScreenOnDailyWorker.this.new AnonymousClass2(continuation);
            anonymousClass2.L$0 = obj;
            return anonymousClass2;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            long earlyMs;
            long nextTs;
            long nextTs2;
            long end;
            int idx;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    try {
                        Context ctx = ScreenOnDailyWorker.this.getApplicationContext();
                        Intrinsics.checkNotNullExpressionValue(ctx, "getApplicationContext(...)");
                        File log = new File(ctx.getFilesDir(), "screen_log.csv");
                        ScreenOnDailyWorker.this.ensureHeader(new File(ctx.getFilesDir(), ScreenOnDailyWorker.OUT));
                        try {
                            if (!log.exists()) {
                                ScreenOnDailyWorker.this.appendOrReplaceToday(0, 0, 0, 0);
                                return ListenableWorker.Result.success();
                            }
                            long now = System.currentTimeMillis();
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(now);
                            ScreenOnDailyWorker screenOnDailyWorker = ScreenOnDailyWorker.this;
                            Intrinsics.checkNotNull(cal);
                            long start = screenOnDailyWorker.dayStartMillis(cal);
                            List events = ScreenOnDailyWorker.this.parseEvents(log);
                            Ref.BooleanRef onAtStart = new Ref.BooleanRef();
                            Pair pair = null;
                            Iterator it = events.iterator();
                            while (true) {
                                boolean z = true;
                                if (it.hasNext()) {
                                    Pair pair2 = (Pair) it.next();
                                    long jLongValue = ((Number) pair2.component1()).longValue();
                                    boolean zBooleanValue = ((Boolean) pair2.component2()).booleanValue();
                                    if (jLongValue <= start) {
                                        Long lBoxLong = Boxing.boxLong(jLongValue);
                                        if (!zBooleanValue) {
                                            z = false;
                                        }
                                        pair = TuplesKt.to(lBoxLong, Boxing.boxBoolean(z));
                                    }
                                }
                            }
                            onAtStart.element = pair != null ? ((Boolean) pair.getSecond()).booleanValue() : false;
                            long cursor = start;
                            boolean isOn = onAtStart.element;
                            int idx2 = 0;
                            Iterator it2 = events.iterator();
                            while (true) {
                                if (it2.hasNext()) {
                                    if ((((Number) ((Pair) it2.next()).getFirst()).longValue() >= start ? 1 : 0) == 0) {
                                        idx2++;
                                    }
                                } else {
                                    idx2 = -1;
                                }
                            }
                            if (idx2 < 0) {
                                idx2 = events.size();
                            }
                            long spanStart = 0;
                            long spanEnd = 0;
                            long lateMs = 0;
                            for (long end2 = ScreenOnDailyWorker.DAY + start; cursor < end2; end2 = end) {
                                boolean isOn2 = isOn;
                                if (idx2 < events.size()) {
                                    earlyMs = spanStart;
                                    long earlyMs2 = ((Number) ((Pair) events.get(idx2)).getFirst()).longValue();
                                    nextTs = Math.min(earlyMs2, end2);
                                } else {
                                    earlyMs = spanStart;
                                    nextTs = end2;
                                }
                                if (!isOn2 || nextTs <= cursor) {
                                    nextTs2 = nextTs;
                                    end = end2;
                                    idx = idx2;
                                    spanStart = earlyMs;
                                } else {
                                    long spanStart2 = cursor;
                                    long spanEnd2 = nextTs;
                                    nextTs2 = nextTs;
                                    long earlyMs3 = earlyMs + ScreenOnDailyWorker.this.overlap(spanStart2, spanEnd2, start + ScreenOnDailyWorker.H06);
                                    long diurnalMs = spanEnd + ScreenOnDailyWorker.this.overlap(Math.max(spanStart2, ScreenOnDailyWorker.H06 + start), spanEnd2, start + ScreenOnDailyWorker.H22);
                                    ScreenOnDailyWorker screenOnDailyWorker2 = ScreenOnDailyWorker.this;
                                    long spanEnd3 = Math.max(spanStart2, start + ScreenOnDailyWorker.H22);
                                    long end3 = end2;
                                    idx = idx2;
                                    end = end3;
                                    lateMs += screenOnDailyWorker2.overlap(spanEnd3, spanEnd2, end3);
                                    spanStart = earlyMs3;
                                    spanEnd = diurnalMs;
                                }
                                if (idx < events.size() && ((Number) ((Pair) events.get(idx)).getFirst()).longValue() == nextTs2) {
                                    boolean isOn3 = ((Boolean) ((Pair) events.get(idx)).getSecond()).booleanValue();
                                    idx2 = idx + 1;
                                    isOn = isOn3;
                                } else {
                                    idx2 = idx;
                                    isOn = isOn2;
                                }
                                cursor = nextTs2;
                            }
                            long earlyMs4 = spanStart;
                            int earlyMin = (int) (earlyMs4 / 60000);
                            int diurnalMin = (int) (spanEnd / 60000);
                            try {
                                int lateMin = (int) (lateMs / 60000);
                                int total = earlyMin + diurnalMin + lateMin;
                                ScreenOnDailyWorker.this.appendOrReplaceToday(earlyMin, diurnalMin, lateMin, total);
                                Log.i(ScreenOnDailyWorker.TAG, "ScreenOnDaily: early=" + earlyMin + " diurnal=" + diurnalMin + " late=" + lateMin + " total=" + total);
                                return ListenableWorker.Result.success();
                            } catch (Throwable th) {
                                t = th;
                                Log.e(ScreenOnDailyWorker.TAG, "ScreenOnDailyWorker failed", t);
                                return ListenableWorker.Result.failure();
                            }
                        } catch (Throwable th2) {
                            t = th2;
                        }
                    } catch (Throwable th3) {
                        t = th3;
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.ScreenOnDailyWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.ScreenOnDailyWorker$doWork$1 r0 = (com.nick.myrecoverytracker.ScreenOnDailyWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.ScreenOnDailyWorker$doWork$1 r0 = new com.nick.myrecoverytracker.ScreenOnDailyWorker$doWork$1
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
            com.nick.myrecoverytracker.ScreenOnDailyWorker$doWork$2 r5 = new com.nick.myrecoverytracker.ScreenOnDailyWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.ScreenOnDailyWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final long dayStartMillis(Calendar calNow) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calNow.getTimeInMillis());
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTimeInMillis();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:12:0x0023  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final long overlap(long r15, long r17, long r19) {
        /*
            r14 = this;
            r0 = r15
            r2 = 86400000(0x5265c00, double:4.2687272E-316)
            long r4 = r0 % r2
            long r4 = r0 - r4
            long r6 = r19 - r4
            r8 = 21600000(0x1499700, double:1.0671818E-316)
            int r10 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
            if (r10 != 0) goto L12
            goto L23
        L12:
            r10 = 79200000(0x4b87f00, double:3.9129999E-316)
            int r12 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1))
            if (r12 != 0) goto L1b
            long r8 = r8 + r4
            goto L24
        L1b:
            int r2 = (r6 > r2 ? 1 : (r6 == r2 ? 0 : -1))
            if (r2 != 0) goto L22
            long r8 = r4 + r10
            goto L24
        L22:
        L23:
            r8 = r4
        L24:
            long r2 = java.lang.Math.max(r0, r8)
            long r6 = java.lang.Math.min(r17, r19)
            r10 = 0
            long r12 = r6 - r2
            long r10 = java.lang.Math.max(r10, r12)
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.ScreenOnDailyWorker.overlap(long, long, long):long");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final List<Pair<Long, Boolean>> parseEvents(File file) {
        final List list = new ArrayList();
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        FilesKt.forEachLine$default(file, null, new Function1() { // from class: com.nick.myrecoverytracker.ScreenOnDailyWorker$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return ScreenOnDailyWorker.parseEvents$lambda$1(df, list, (String) obj);
            }
        }, 1, null);
        if (list.size() > 1) {
            CollectionsKt.sortWith(list, new Comparator() { // from class: com.nick.myrecoverytracker.ScreenOnDailyWorker$parseEvents$$inlined$sortBy$1
                /* JADX WARN: Multi-variable type inference failed */
                @Override // java.util.Comparator
                public final int compare(T t, T t2) {
                    return ComparisonsKt.compareValues((Long) ((Pair) t).getFirst(), (Long) ((Pair) t2).getFirst());
                }
            });
        }
        return list;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x0087, code lost:
    
        if (r0.equals("SCREEN_OFF") == false) goto L36;
     */
    /* JADX WARN: Code restructure failed: missing block: B:28:0x0090, code lost:
    
        if (r0.equals("OFF") == false) goto L36;
     */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x0093, code lost:
    
        r2 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x009b, code lost:
    
        if (r0.equals("ON") != false) goto L34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x009f, code lost:
    
        r11.add(kotlin.TuplesKt.to(java.lang.Long.valueOf(r7), java.lang.Boolean.valueOf(r2)));
     */
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue
    java.lang.NullPointerException: Cannot invoke "java.util.List.iterator()" because the return value of "jadx.core.dex.visitors.regions.SwitchOverStringVisitor$SwitchData.getNewCases()" is null
    	at jadx.core.dex.visitors.regions.SwitchOverStringVisitor.restoreSwitchOverString(SwitchOverStringVisitor.java:109)
    	at jadx.core.dex.visitors.regions.SwitchOverStringVisitor.visitRegion(SwitchOverStringVisitor.java:66)
    	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:77)
    	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:82)
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final kotlin.Unit parseEvents$lambda$1(java.text.SimpleDateFormat r10, java.util.List r11, java.lang.String r12) {
        /*
            java.lang.String r0 = "raw"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r12, r0)
            r0 = r12
            java.lang.CharSequence r0 = (java.lang.CharSequence) r0
            java.lang.CharSequence r0 = kotlin.text.StringsKt.trim(r0)
            java.lang.String r1 = r0.toString()
            r0 = r1
            java.lang.CharSequence r0 = (java.lang.CharSequence) r0
            int r0 = r0.length()
            r2 = 1
            r3 = 0
            if (r0 != 0) goto L1d
            r0 = r2
            goto L1e
        L1d:
            r0 = r3
        L1e:
            if (r0 == 0) goto L23
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        L23:
            r4 = r1
            java.lang.CharSequence r4 = (java.lang.CharSequence) r4
            r8 = 6
            r9 = 0
            r5 = 44
            r6 = 0
            r7 = 0
            int r4 = kotlin.text.StringsKt.lastIndexOf$default(r4, r5, r6, r7, r8, r9)
            if (r4 <= 0) goto Lb7
            r0 = r1
            java.lang.CharSequence r0 = (java.lang.CharSequence) r0
            int r0 = kotlin.text.StringsKt.getLastIndex(r0)
            if (r4 != r0) goto L3d
            goto Lb7
        L3d:
            java.lang.String r0 = r1.substring(r3, r4)
            java.lang.String r5 = "substring(...)"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r0, r5)
            r6 = r0
            int r0 = r4 + 1
            java.lang.String r0 = r1.substring(r0)
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r0, r5)
            java.util.Locale r5 = java.util.Locale.US
            java.lang.String r7 = "US"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r5, r7)
            java.lang.String r0 = r0.toUpperCase(r5)
            java.lang.String r5 = "toUpperCase(...)"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r0, r5)
            r5 = r0
            r7 = 0
            java.util.Date r0 = r10.parse(r6)     // Catch: java.lang.Throwable -> L72
            if (r0 == 0) goto L73
            long r8 = r0.getTime()     // Catch: java.lang.Throwable -> L72
            java.lang.Long r0 = java.lang.Long.valueOf(r8)     // Catch: java.lang.Throwable -> L72
            r7 = r0
            goto L73
        L72:
            r0 = move-exception
        L73:
            if (r7 == 0) goto Lb4
            long r7 = r7.longValue()
            int r0 = r5.hashCode()
            switch(r0) {
                case 2527: goto L95;
                case 78159: goto L8a;
                case 69009148: goto L81;
                default: goto L80;
            }
        L80:
            goto Lb1
        L81:
            java.lang.String r0 = "SCREEN_OFF"
            boolean r0 = r5.equals(r0)
            if (r0 != 0) goto L93
            goto L80
        L8a:
            java.lang.String r0 = "OFF"
            boolean r0 = r5.equals(r0)
            if (r0 != 0) goto L93
            goto L80
        L93:
            r2 = r3
            goto L9e
        L95:
            java.lang.String r0 = "ON"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L80
        L9e:
        L9f:
            java.lang.Long r0 = java.lang.Long.valueOf(r7)
            java.lang.Boolean r3 = java.lang.Boolean.valueOf(r2)
            kotlin.Pair r0 = kotlin.TuplesKt.to(r0, r3)
            r11.add(r0)
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        Lb1:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        Lb4:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        Lb7:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.ScreenOnDailyWorker.parseEvents$lambda$1(java.text.SimpleDateFormat, java.util.List, java.lang.String):kotlin.Unit");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void appendOrReplaceToday(int early, int diurnal, int late, int total) {
        File file = new File(getApplicationContext().getFilesDir(), OUT);
        String str = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        List lines$default = file.exists() ? FilesKt.readLines$default(file, null, 1, null) : CollectionsKt.emptyList();
        Object objFirstOrNull = CollectionsKt.firstOrNull((List<? extends Object>) lines$default);
        String str2 = (String) objFirstOrNull;
        if (str2 == null || StringsKt.isBlank(str2)) {
            objFirstOrNull = null;
        }
        String str3 = (String) objFirstOrNull;
        if (str3 == null) {
            str3 = HEADER;
        }
        List listDrop = CollectionsKt.drop(lines$default, 1 ^ (lines$default.isEmpty() ? 1 : 0));
        ArrayList arrayList = new ArrayList();
        for (Object obj : listDrop) {
            List list = lines$default;
            if (!StringsKt.startsWith$default((String) obj, str + ",", false, 2, (Object) null)) {
                arrayList.add(obj);
            }
            lines$default = list;
        }
        List mutableList = CollectionsKt.toMutableList((Collection) arrayList);
        mutableList.add(str + "," + early + "," + diurnal + "," + late + "," + total);
        FilesKt.writeText$default(file, str3 + "\n" + CollectionsKt.joinToString$default(mutableList, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void ensureHeader(File out) {
        if (!out.exists() || out.length() == 0) {
            File parentFile = out.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FilesKt.writeText$default(out, "date,early_minutes,diurnal_minutes,late_minutes,total_minutes\n", null, 2, null);
        }
    }
}
