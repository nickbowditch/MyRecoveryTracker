package com.nick.myrecoverytracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: BatteryLogWorker.kt */
@Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u0000 \u00192\u00020\u0001:\u0001\u0019B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\nJ\u0018\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u0012\u0010\u0010\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\r\u001a\u00020\fH\u0002J\u001a\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u0014\u001a\u00020\u000fH\u0002J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0003H\u0002J\b\u0010\u0018\u001a\u00020\u000fH\u0002¨\u0006\u001a"}, d2 = {"Lcom/nick/myrecoverytracker/BatteryLogWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureHeader", "Ljava/io/File;", "f", "header", "", "lastDataLine", "minutesBetween", "", "prev", "now", "currentBatteryPercent", "", "context", "nowTs", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class BatteryLogWorker extends CoroutineWorker {
    private static final String FILE = "battery_log.csv";
    private static final String HEADER = "timestamp,level_pct";
    private static final String TAG = "BatteryLogWorker";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    /* compiled from: BatteryLogWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.BatteryLogWorker", f = "BatteryLogWorker.kt", i = {}, l = {24}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.BatteryLogWorker$doWork$1, reason: invalid class name */
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
            return BatteryLogWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public BatteryLogWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: BatteryLogWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.BatteryLogWorker$doWork$2", f = "BatteryLogWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.BatteryLogWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return BatteryLogWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            String strRemoveSuffix;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    try {
                        Context ctx = BatteryLogWorker.this.getApplicationContext();
                        Intrinsics.checkNotNullExpressionValue(ctx, "getApplicationContext(...)");
                        int level = BatteryLogWorker.this.currentBatteryPercent(ctx);
                        if (level >= 0) {
                            String ts = BatteryLogWorker.this.nowTs();
                            File file = BatteryLogWorker.this.ensureHeader(new File(ctx.getFilesDir(), BatteryLogWorker.FILE), BatteryLogWorker.HEADER);
                            String last = BatteryLogWorker.this.lastDataLine(file);
                            boolean shouldWrite = true;
                            if (last != null) {
                                List parts = StringsKt.split$default((CharSequence) last, new String[]{","}, false, 0, 6, (Object) null);
                                String str = (String) CollectionsKt.getOrNull(parts, 1);
                                Integer lastPct = (str == null || (strRemoveSuffix = StringsKt.removeSuffix(str, (CharSequence) "%")) == null) ? null : StringsKt.toIntOrNull(strRemoveSuffix);
                                String lastTs = (String) CollectionsKt.getOrNull(parts, 0);
                                long minutesSince = BatteryLogWorker.this.minutesBetween(lastTs, ts);
                                if (lastPct != null && lastPct.intValue() == level && minutesSince < 30) {
                                    shouldWrite = false;
                                }
                            }
                            if (shouldWrite) {
                                FilesKt.appendText$default(file, ts + "," + level + "%\n", null, 2, null);
                                Log.i(BatteryLogWorker.TAG, "battery_log appended: " + ts + "," + level + "%");
                            } else {
                                Log.i(BatteryLogWorker.TAG, "battery_log skipped (no change & <30m)");
                            }
                            return ListenableWorker.Result.success();
                        }
                        Log.w(BatteryLogWorker.TAG, "Battery level unknown; skip");
                        return ListenableWorker.Result.success();
                    } catch (Throwable t) {
                        Log.e(BatteryLogWorker.TAG, "BatteryLogWorker failed", t);
                        return ListenableWorker.Result.failure();
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.BatteryLogWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.BatteryLogWorker$doWork$1 r0 = (com.nick.myrecoverytracker.BatteryLogWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.BatteryLogWorker$doWork$1 r0 = new com.nick.myrecoverytracker.BatteryLogWorker$doWork$1
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
            com.nick.myrecoverytracker.BatteryLogWorker$doWork$2 r5 = new com.nick.myrecoverytracker.BatteryLogWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.BatteryLogWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final File ensureHeader(File f, String header) {
        if (!f.exists() || f.length() == 0) {
            File parentFile = f.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FilesKt.writeText$default(f, header + "\n", null, 2, null);
        }
        return f;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String lastDataLine(File f) {
        if (!f.exists()) {
            return null;
        }
        List lines = FilesKt.readLines$default(f, null, 1, null);
        for (int i = lines.size() - 1; i > 0; i--) {
            String line = StringsKt.trim((CharSequence) lines.get(i)).toString();
            if (line.length() > 0) {
                return line;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final long minutesBetween(String prev, String now) {
        if (prev == null) {
            return Long.MAX_VALUE;
        }
        try {
            Date date = sdf.parse(prev);
            if (date == null) {
                return Long.MAX_VALUE;
            }
            long dPrev = date.getTime();
            Date date2 = sdf.parse(now);
            if (date2 == null) {
                return Long.MAX_VALUE;
            }
            long dNow = date2.getTime();
            return (dNow - dPrev) / 60000;
        } catch (Throwable th) {
            return Long.MAX_VALUE;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final int currentBatteryPercent(Context context) {
        IntentFilter ifilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        Intent status = context.registerReceiver(null, ifilter);
        int level = status != null ? status.getIntExtra("level", -1) : -1;
        int scale = status != null ? status.getIntExtra("scale", -1) : -1;
        if (level < 0 || scale <= 0) {
            return -1;
        }
        return (int) ((level * 100.0f) / scale);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String nowTs() {
        String str = sdf.format(new Date());
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }
}
