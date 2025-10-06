package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.nick.myrecoverytracker.UsageEntropyDailyWorker;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
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

/* compiled from: UsageEntropyDailyWorker.kt */
@Metadata(d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u001e\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\n\u001a\u00020\u000bH\u0096@¢\u0006\u0002\u0010\fJ\u001e\u0010\r\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0012\u001a\u00020\u0011H\u0002J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J \u0010\u0017\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u000eH\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001b"}, d2 = {"Lcom/nick/myrecoverytracker/UsageEntropyDailyWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "dayFmt", "Ljava/text/SimpleDateFormat;", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "shannonEntropy", "", "counts", "", "", "total", "ensureHeader", "", "dir", "Ljava/io/File;", "writeOut", "day", "", "entropy", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UsageEntropyDailyWorker extends CoroutineWorker {
    private final SimpleDateFormat dayFmt;

    /* compiled from: UsageEntropyDailyWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UsageEntropyDailyWorker", f = "UsageEntropyDailyWorker.kt", i = {}, l = {19}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$1, reason: invalid class name */
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
            return UsageEntropyDailyWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public UsageEntropyDailyWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        this.dayFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    /* compiled from: UsageEntropyDailyWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$2", f = "UsageEntropyDailyWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return UsageEntropyDailyWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    try {
                        File dir = UsageEntropyDailyWorker.this.getApplicationContext().getFilesDir();
                        final String today = UsageEntropyDailyWorker.this.dayFmt.format(Boxing.boxLong(System.currentTimeMillis()));
                        UsageEntropyDailyWorker usageEntropyDailyWorker = UsageEntropyDailyWorker.this;
                        Intrinsics.checkNotNull(dir);
                        usageEntropyDailyWorker.ensureHeader(dir);
                        File switching = new File(dir, "daily_app_switching.csv");
                        File usageEvents = new File(dir, "usage_events.csv");
                        final Ref.ObjectRef entropyFromSwitching = new Ref.ObjectRef();
                        if (switching.exists()) {
                            FilesKt.forEachLine$default(switching, null, new Function1() { // from class: com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$2$$ExternalSyntheticLambda0
                                @Override // kotlin.jvm.functions.Function1
                                public final Object invoke(Object obj2) {
                                    return UsageEntropyDailyWorker.AnonymousClass2.invokeSuspend$lambda$0(today, entropyFromSwitching, (String) obj2);
                                }
                            }, 1, null);
                        }
                        if (entropyFromSwitching.element != 0) {
                            UsageEntropyDailyWorker usageEntropyDailyWorker2 = UsageEntropyDailyWorker.this;
                            Intrinsics.checkNotNull(today);
                            T t = entropyFromSwitching.element;
                            Intrinsics.checkNotNull(t);
                            usageEntropyDailyWorker2.writeOut(dir, today, ((Number) t).doubleValue());
                            return ListenableWorker.Result.success();
                        }
                        final Map freq = new LinkedHashMap();
                        final Ref.IntRef total = new Ref.IntRef();
                        if (usageEvents.exists()) {
                            FilesKt.forEachLine$default(usageEvents, null, new Function1() { // from class: com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$2$$ExternalSyntheticLambda1
                                @Override // kotlin.jvm.functions.Function1
                                public final Object invoke(Object obj2) {
                                    return UsageEntropyDailyWorker.AnonymousClass2.invokeSuspend$lambda$1(today, freq, total, (String) obj2);
                                }
                            }, 1, null);
                        }
                        if (total.element != 0) {
                            double entropyBits = UsageEntropyDailyWorker.this.shannonEntropy(freq.values(), total.element);
                            UsageEntropyDailyWorker usageEntropyDailyWorker3 = UsageEntropyDailyWorker.this;
                            Intrinsics.checkNotNull(today);
                            usageEntropyDailyWorker3.writeOut(dir, today, entropyBits);
                            return ListenableWorker.Result.success();
                        }
                        return ListenableWorker.Result.success();
                    } catch (Throwable th) {
                        return ListenableWorker.Result.retry();
                    }
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX WARN: Type inference failed for: r1v8, types: [T, java.lang.Double] */
        public static final Unit invokeSuspend$lambda$0(String $today, Ref.ObjectRef $entropyFromSwitching, String line) {
            ?? doubleOrNull;
            if (StringsKt.startsWith$default(line, $today + ",", false, 2, (Object) null)) {
                List parts = StringsKt.split$default((CharSequence) line, new char[]{','}, false, 0, 6, (Object) null);
                if (parts.size() >= 3 && (doubleOrNull = StringsKt.toDoubleOrNull(StringsKt.trim((CharSequence) parts.get(2)).toString())) != 0) {
                    $entropyFromSwitching.element = doubleOrNull;
                }
            }
            return Unit.INSTANCE;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static final Unit invokeSuspend$lambda$1(String $today, Map $freq, Ref.IntRef $total, String line) {
            if (!StringsKt.startsWith$default(line, $today + ",", false, 2, (Object) null)) {
                return Unit.INSTANCE;
            }
            List parts = StringsKt.split$default((CharSequence) line, new char[]{','}, false, 0, 6, (Object) null);
            if (parts.size() >= 2) {
                String pkg = StringsKt.trim((CharSequence) parts.get(1)).toString();
                if (pkg.length() > 0) {
                    Integer num = (Integer) $freq.get(pkg);
                    $freq.put(pkg, Integer.valueOf((num != null ? num.intValue() : 0) + 1));
                    $total.element++;
                }
            }
            return Unit.INSTANCE;
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.UsageEntropyDailyWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$1 r0 = (com.nick.myrecoverytracker.UsageEntropyDailyWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$1 r0 = new com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$1
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
            com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$2 r5 = new com.nick.myrecoverytracker.UsageEntropyDailyWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.UsageEntropyDailyWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final double shannonEntropy(Collection<Integer> counts, int total) {
        double h = 0.0d;
        Iterator<Integer> it = counts.iterator();
        while (it.hasNext()) {
            int c = it.next().intValue();
            double p = c / total;
            h -= Math.log(p) * p;
        }
        return h / Math.log(2.0d);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void ensureHeader(File dir) {
        File f = new File(dir, "daily_usage_entropy.csv");
        if (!f.exists()) {
            FilesKt.writeText$default(f, "date,entropy_bits\n", null, 2, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void writeOut(File dir, String day, double entropy) {
        File f = new File(dir, "daily_usage_entropy.csv");
        if (!f.exists()) {
            FilesKt.writeText$default(f, "date,entropy_bits\n", null, 2, null);
        }
        List lines = CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(f, null, 1, null));
        String row = String.format(Locale.US, "%s,%.4f", Arrays.copyOf(new Object[]{day, Double.valueOf(entropy)}, 2));
        Intrinsics.checkNotNullExpressionValue(row, "format(...)");
        int idx = 0;
        Iterator it = lines.iterator();
        while (true) {
            if (it.hasNext()) {
                if (StringsKt.startsWith$default((String) it.next(), day + ",", false, 2, (Object) null)) {
                    break;
                } else {
                    idx++;
                }
            } else {
                idx = -1;
                break;
            }
        }
        if (idx >= 0) {
            lines.set(idx, row);
        } else {
            lines.add(row);
        }
        FilesKt.writeText$default(f, CollectionsKt.joinToString$default(lines, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }
}
