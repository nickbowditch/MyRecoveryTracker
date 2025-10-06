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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.enums.EnumEntries;
import kotlin.enums.EnumEntriesKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: UsageEventsDumpWorker.kt */
@Metadata(d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u0000 \u00182\u00020\u0001:\u0002\u0017\u0018B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u000b\u001a\u00020\fH\u0096@¢\u0006\u0002\u0010\rJ\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u0010\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0019"}, d2 = {"Lcom/nick/myrecoverytracker/UsageEventsDumpWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "dateFmt", "Ljava/text/SimpleDateFormat;", "timeFmt", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fmtDate", "", "ts", "", "fmtTime", "normalizeEventType", "Lcom/nick/myrecoverytracker/UsageEventsDumpWorker$NormalizedEventType;", "eventType", "", "NormalizedEventType", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UsageEventsDumpWorker extends CoroutineWorker {
    private static final int EVENT_ACTIVITY_PAUSED = 8;
    private static final int EVENT_ACTIVITY_RESUMED = 7;
    private static final String TAG = "UsageEventsDumpWorker";
    private final SimpleDateFormat dateFmt;
    private final SimpleDateFormat timeFmt;

    /* compiled from: UsageEventsDumpWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UsageEventsDumpWorker", f = "UsageEventsDumpWorker.kt", i = {}, l = {34}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UsageEventsDumpWorker$doWork$1, reason: invalid class name */
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
            return UsageEventsDumpWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public UsageEventsDumpWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        this.dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        this.timeFmt = new SimpleDateFormat("HH:mm:ss", Locale.US);
    }

    /* compiled from: UsageEventsDumpWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UsageEventsDumpWorker$doWork$2", f = "UsageEventsDumpWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UsageEventsDumpWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        /* compiled from: UsageEventsDumpWorker.kt */
        @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
        /* renamed from: com.nick.myrecoverytracker.UsageEventsDumpWorker$doWork$2$WhenMappings */
        public /* synthetic */ class WhenMappings {
            public static final /* synthetic */ int[] $EnumSwitchMapping$0;

            static {
                int[] iArr = new int[NormalizedEventType.values().length];
                try {
                    iArr[NormalizedEventType.MOVE_TO_FOREGROUND.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[NormalizedEventType.MOVE_TO_BACKGROUND.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                $EnumSwitchMapping$0 = iArr;
            }
        }

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return UsageEventsDumpWorker.this.new AnonymousClass2(continuation);
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
                    try {
                        UsagePermissionHelper usagePermissionHelper = UsagePermissionHelper.INSTANCE;
                        Context applicationContext = UsageEventsDumpWorker.this.getApplicationContext();
                        Intrinsics.checkNotNullExpressionValue(applicationContext, "getApplicationContext(...)");
                        if (!usagePermissionHelper.isGranted(applicationContext)) {
                            Log.w(UsageEventsDumpWorker.TAG, "Usage access not granted — skipping dump");
                            return ListenableWorker.Result.success();
                        }
                        Object systemService = UsageEventsDumpWorker.this.getApplicationContext().getSystemService("usagestats");
                        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.usage.UsageStatsManager");
                        UsageStatsManager usm = (UsageStatsManager) systemService;
                        long endTime = System.currentTimeMillis();
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(endTime);
                        cal.set(11, 0);
                        cal.set(12, 0);
                        cal.set(13, 0);
                        cal.set(14, 0);
                        long startTime = cal.getTimeInMillis();
                        UsageEvents events = usm.queryEvents(startTime, endTime);
                        UsageEvents.Event e = new UsageEvents.Event();
                        File outFile = new File(UsageEventsDumpWorker.this.getApplicationContext().getFilesDir(), "usage_events.csv");
                        if (!outFile.exists()) {
                            FilesKt.writeText$default(outFile, "date,time,event_type,package\n", null, 2, null);
                        }
                        StringBuilder sb = new StringBuilder();
                        while (events.hasNextEvent()) {
                            events.getNextEvent(e);
                            String pkg = e.getPackageName();
                            if (pkg != null) {
                                switch (WhenMappings.$EnumSwitchMapping$0[UsageEventsDumpWorker.this.normalizeEventType(e.getEventType()).ordinal()]) {
                                    case 1:
                                        sb.append(UsageEventsDumpWorker.this.fmtDate(e.getTimeStamp())).append(',').append(UsageEventsDumpWorker.this.fmtTime(e.getTimeStamp())).append(',').append("FOREGROUND,").append(pkg).append('\n');
                                        break;
                                    case 2:
                                        sb.append(UsageEventsDumpWorker.this.fmtDate(e.getTimeStamp())).append(',').append(UsageEventsDumpWorker.this.fmtTime(e.getTimeStamp())).append(',').append("BACKGROUND,").append(pkg).append('\n');
                                        break;
                                    default:
                                        Unit unit = Unit.INSTANCE;
                                        break;
                                }
                            }
                        }
                        if (sb.length() > 0) {
                            String string = sb.toString();
                            Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
                            FilesKt.appendText$default(outFile, string, null, 2, null);
                        }
                        Log.i(UsageEventsDumpWorker.TAG, "UsageEventsDumpWorker wrote events to " + outFile.getName());
                        return ListenableWorker.Result.success();
                    } catch (Throwable t) {
                        Log.e(UsageEventsDumpWorker.TAG, "UsageEventsDumpWorker failed", t);
                        return ListenableWorker.Result.retry();
                    }
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.UsageEventsDumpWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.UsageEventsDumpWorker$doWork$1 r0 = (com.nick.myrecoverytracker.UsageEventsDumpWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.UsageEventsDumpWorker$doWork$1 r0 = new com.nick.myrecoverytracker.UsageEventsDumpWorker$doWork$1
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
            com.nick.myrecoverytracker.UsageEventsDumpWorker$doWork$2 r5 = new com.nick.myrecoverytracker.UsageEventsDumpWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.UsageEventsDumpWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String fmtDate(long ts) {
        String str = this.dateFmt.format(new Date(ts));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String fmtTime(long ts) {
        String str = this.timeFmt.format(new Date(ts));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final NormalizedEventType normalizeEventType(int eventType) {
        if (eventType == 7) {
            return NormalizedEventType.MOVE_TO_FOREGROUND;
        }
        if (eventType == 8) {
            return NormalizedEventType.MOVE_TO_BACKGROUND;
        }
        switch (eventType) {
            case 1:
                return NormalizedEventType.MOVE_TO_FOREGROUND;
            case 2:
                return NormalizedEventType.MOVE_TO_BACKGROUND;
            default:
                return NormalizedEventType.IGNORED;
        }
    }

    /* compiled from: UsageEventsDumpWorker.kt */
    @Metadata(d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006¨\u0006\u0007"}, d2 = {"Lcom/nick/myrecoverytracker/UsageEventsDumpWorker$NormalizedEventType;", "", "<init>", "(Ljava/lang/String;I)V", "MOVE_TO_FOREGROUND", "MOVE_TO_BACKGROUND", "IGNORED", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public enum NormalizedEventType {
        MOVE_TO_FOREGROUND,
        MOVE_TO_BACKGROUND,
        IGNORED;

        private static final /* synthetic */ EnumEntries $ENTRIES = EnumEntriesKt.enumEntries($VALUES);

        public static EnumEntries<NormalizedEventType> getEntries() {
            return $ENTRIES;
        }
    }
}
