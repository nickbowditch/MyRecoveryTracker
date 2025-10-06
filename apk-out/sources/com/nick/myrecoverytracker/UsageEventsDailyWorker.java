package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
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
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: UsageEventsDailyWorker.kt */
@Metadata(d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\u0018\u0000 \u001c2\u00020\u0001:\u0001\u001cB\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u000e\u001a\u00020\u000fH\u0096@¢\u0006\u0002\u0010\u0010J\u0018\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u0015H\u0002J&\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00122\u0006\u0010\u0019\u001a\u00020\u00152\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00150\u001bH\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0018\u0010\n\u001a\n \f*\u0004\u0018\u00010\u000b0\u000bX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\r¨\u0006\u001d"}, d2 = {"Lcom/nick/myrecoverytracker/UsageEventsDailyWorker;", "Landroidx/work/CoroutineWorker;", "ctx", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "zone", "Ljava/time/ZoneId;", "fmtDate", "Ljava/time/format/DateTimeFormatter;", "kotlin.jvm.PlatformType", "Ljava/time/format/DateTimeFormatter;", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureHeader", "Ljava/io/File;", "f", "header", "", "upsert", "", "file", "dateStr", "cols", "", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UsageEventsDailyWorker extends CoroutineWorker {
    private static final String TAG = "UsageEventsDailyWorker";
    private final DateTimeFormatter fmtDate;
    private final ZoneId zone;

    /* compiled from: UsageEventsDailyWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UsageEventsDailyWorker", f = "UsageEventsDailyWorker.kt", i = {}, l = {23}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UsageEventsDailyWorker$doWork$1, reason: invalid class name */
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
            return UsageEventsDailyWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public UsageEventsDailyWorker(Context ctx, WorkerParameters params) {
        super(ctx, params);
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(params, "params");
        ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
        Intrinsics.checkNotNullExpressionValue(zoneIdSystemDefault, "systemDefault(...)");
        this.zone = zoneIdSystemDefault;
        this.fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    }

    /* compiled from: UsageEventsDailyWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UsageEventsDailyWorker$doWork$2", f = "UsageEventsDailyWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UsageEventsDailyWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return UsageEventsDailyWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            File raw;
            File out;
            String tStr;
            String yStr;
            Ref.IntRef tCount;
            Ref.IntRef yCount;
            Reader bufferedReader;
            Throwable th;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    Object $result = obj;
                    try {
                        File dir = UsageEventsDailyWorker.this.getApplicationContext().getFilesDir();
                        raw = new File(dir, "usage_events.csv");
                        out = UsageEventsDailyWorker.this.ensureHeader(new File(dir, "daily_usage_events.csv"), "date,event_count");
                        LocalDate today = LocalDate.now(UsageEventsDailyWorker.this.zone);
                        LocalDate yesterday = today.minusDays(1L);
                        tStr = today.format(UsageEventsDailyWorker.this.fmtDate);
                        yStr = yesterday.format(UsageEventsDailyWorker.this.fmtDate);
                        tCount = new Ref.IntRef();
                        yCount = new Ref.IntRef();
                    } catch (Throwable th2) {
                        t = th2;
                    }
                    try {
                        if (raw.exists()) {
                            Reader inputStreamReader = new InputStreamReader(new FileInputStream(raw), Charsets.UTF_8);
                            if (inputStreamReader instanceof BufferedReader) {
                                try {
                                    bufferedReader = (BufferedReader) inputStreamReader;
                                } catch (Throwable th3) {
                                    t = th3;
                                    Log.e(UsageEventsDailyWorker.TAG, "UsageEventsDailyWorker failed", t);
                                    return ListenableWorker.Result.retry();
                                }
                            } else {
                                bufferedReader = new BufferedReader(inputStreamReader, 8192);
                            }
                            Reader reader = bufferedReader;
                            int i = 0;
                            BufferedReader bufferedReader2 = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, 8192);
                            try {
                                for (String str : TextStreamsKt.lineSequence(bufferedReader2)) {
                                    Object $result2 = $result;
                                    try {
                                        int i2 = i;
                                        if (str.length() >= 10) {
                                            try {
                                                String strSubstring = str.substring(0, 10);
                                                Intrinsics.checkNotNullExpressionValue(strSubstring, "substring(...)");
                                                if (Intrinsics.areEqual(strSubstring, tStr)) {
                                                    tCount.element++;
                                                } else if (Intrinsics.areEqual(strSubstring, yStr)) {
                                                    yCount.element++;
                                                }
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
                                        $result = $result2;
                                        i = i2;
                                    } catch (Throwable th6) {
                                        th = th6;
                                        throw th;
                                    }
                                }
                                Unit unit = Unit.INSTANCE;
                                CloseableKt.closeFinally(bufferedReader2, null);
                            } catch (Throwable th7) {
                                th = th7;
                            }
                        }
                        UsageEventsDailyWorker usageEventsDailyWorker = UsageEventsDailyWorker.this;
                        Intrinsics.checkNotNull(yStr);
                        usageEventsDailyWorker.upsert(out, yStr, CollectionsKt.listOf(String.valueOf(yCount.element)));
                        UsageEventsDailyWorker usageEventsDailyWorker2 = UsageEventsDailyWorker.this;
                        Intrinsics.checkNotNull(tStr);
                        usageEventsDailyWorker2.upsert(out, tStr, CollectionsKt.listOf(String.valueOf(tCount.element)));
                        Log.i(UsageEventsDailyWorker.TAG, "UsageEventsDaily -> " + yStr + "=" + yCount.element + ", " + tStr + "=" + tCount.element);
                        return ListenableWorker.Result.success();
                    } catch (Throwable th8) {
                        t = th8;
                        Log.e(UsageEventsDailyWorker.TAG, "UsageEventsDailyWorker failed", t);
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.UsageEventsDailyWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.UsageEventsDailyWorker$doWork$1 r0 = (com.nick.myrecoverytracker.UsageEventsDailyWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.UsageEventsDailyWorker$doWork$1 r0 = new com.nick.myrecoverytracker.UsageEventsDailyWorker$doWork$1
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
            com.nick.myrecoverytracker.UsageEventsDailyWorker$doWork$2 r5 = new com.nick.myrecoverytracker.UsageEventsDailyWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.UsageEventsDailyWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
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
    public final void upsert(File file, String dateStr, List<String> cols) {
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
                lines.set(i, dateStr + "," + CollectionsKt.joinToString$default(cols, ",", null, null, 0, null, null, 62, null));
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lines.add(dateStr + "," + CollectionsKt.joinToString$default(cols, ",", null, null, 0, null, null, 62, null));
        }
        FilesKt.writeText$default(file, CollectionsKt.joinToString$default(CollectionsKt.plus((Collection) CollectionsKt.listOf(header), (Iterable) CollectionsKt.drop(lines, 1)), "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }
}
