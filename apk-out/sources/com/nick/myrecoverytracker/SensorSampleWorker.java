package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.internal.ServiceSpecificExtraArgs;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: SensorSampleWorker.kt */
@Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\nJ\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002¨\u0006\u000f"}, d2 = {"Lcom/nick/myrecoverytracker/SensorSampleWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "writeIntensity", "", "value", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class SensorSampleWorker extends CoroutineWorker {

    /* compiled from: SensorSampleWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.SensorSampleWorker", f = "SensorSampleWorker.kt", i = {}, l = {ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_HORIZONTAL_BIAS}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.SensorSampleWorker$doWork$1, reason: invalid class name */
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
            return SensorSampleWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SensorSampleWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: SensorSampleWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.SensorSampleWorker$doWork$2", f = "SensorSampleWorker.kt", i = {0, 0, 0, 0}, l = {58}, m = "invokeSuspend", n = {"tag", "sm", "samples", ServiceSpecificExtraArgs.CastExtraArgs.LISTENER}, s = {"L$0", "L$1", "L$2", "L$3"})
    /* renamed from: com.nick.myrecoverytracker.SensorSampleWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        Object L$0;
        Object L$1;
        Object L$2;
        Object L$3;
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return SensorSampleWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Removed duplicated region for block: B:29:0x00a1  */
        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public final java.lang.Object invokeSuspend(java.lang.Object r12) throws java.lang.Throwable {
            /*
                r11 = this;
                java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
                int r1 = r11.label
                r2 = 0
                switch(r1) {
                    case 0: goto L29;
                    case 1: goto L13;
                    default: goto Lb;
                }
            Lb:
                java.lang.IllegalStateException r12 = new java.lang.IllegalStateException
                java.lang.String r0 = "call to 'resume' before 'invoke' with coroutine"
                r12.<init>(r0)
                throw r12
            L13:
                java.lang.Object r0 = r11.L$3
                com.nick.myrecoverytracker.SensorSampleWorker$doWork$2$listener$1 r0 = (com.nick.myrecoverytracker.SensorSampleWorker$doWork$2$listener$1) r0
                java.lang.Object r1 = r11.L$2
                java.util.ArrayList r1 = (java.util.ArrayList) r1
                java.lang.Object r4 = r11.L$1
                android.hardware.SensorManager r4 = (android.hardware.SensorManager) r4
                java.lang.Object r5 = r11.L$0
                java.lang.String r5 = (java.lang.String) r5
                kotlin.ResultKt.throwOnFailure(r12)     // Catch: java.lang.Throwable -> L27
                goto L88
            L27:
                r6 = move-exception
                goto L93
            L29:
                kotlin.ResultKt.throwOnFailure(r12)
                java.lang.String r5 = "SensorSampleWorker"
                com.nick.myrecoverytracker.SensorSampleWorker r1 = com.nick.myrecoverytracker.SensorSampleWorker.this
                android.content.Context r1 = r1.getApplicationContext()
                java.lang.String r4 = "getApplicationContext(...)"
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r1, r4)
                java.lang.String r4 = "sensor"
                java.lang.Object r4 = r1.getSystemService(r4)
                java.lang.String r1 = "null cannot be cast to non-null type android.hardware.SensorManager"
                kotlin.jvm.internal.Intrinsics.checkNotNull(r4, r1)
                android.hardware.SensorManager r4 = (android.hardware.SensorManager) r4
                r1 = 1
                android.hardware.Sensor r6 = r4.getDefaultSensor(r1)
                if (r6 != 0) goto L5c
                java.lang.String r0 = "No accelerometer; writing 0"
                android.util.Log.w(r5, r0)
                com.nick.myrecoverytracker.SensorSampleWorker r0 = com.nick.myrecoverytracker.SensorSampleWorker.this
                com.nick.myrecoverytracker.SensorSampleWorker.access$writeIntensity(r0, r2)
                androidx.work.ListenableWorker$Result r0 = androidx.work.ListenableWorker.Result.success()
                return r0
            L5c:
                java.util.ArrayList r7 = new java.util.ArrayList
                r8 = 512(0x200, float:7.175E-43)
                r7.<init>(r8)
                com.nick.myrecoverytracker.SensorSampleWorker$doWork$2$listener$1 r8 = new com.nick.myrecoverytracker.SensorSampleWorker$doWork$2$listener$1
                r8.<init>()
                r9 = r8
                android.hardware.SensorEventListener r9 = (android.hardware.SensorEventListener) r9     // Catch: java.lang.Throwable -> L90
                r4.registerListener(r9, r6, r1)     // Catch: java.lang.Throwable -> L90
                r6 = r11
                kotlin.coroutines.Continuation r6 = (kotlin.coroutines.Continuation) r6     // Catch: java.lang.Throwable -> L90
                r11.L$0 = r5     // Catch: java.lang.Throwable -> L90
                r11.L$1 = r4     // Catch: java.lang.Throwable -> L90
                r11.L$2 = r7     // Catch: java.lang.Throwable -> L90
                r11.L$3 = r8     // Catch: java.lang.Throwable -> L90
                r11.label = r1     // Catch: java.lang.Throwable -> L90
                r9 = 10000(0x2710, double:4.9407E-320)
                java.lang.Object r1 = kotlinx.coroutines.DelayKt.delay(r9, r6)     // Catch: java.lang.Throwable -> L90
                if (r1 != r0) goto L86
                return r0
            L86:
                r1 = r7
                r0 = r8
            L88:
            L89:
                r6 = r0
                android.hardware.SensorEventListener r6 = (android.hardware.SensorEventListener) r6
                r4.unregisterListener(r6)
                goto L9a
            L90:
                r6 = move-exception
                r1 = r7
                r0 = r8
            L93:
                java.lang.String r7 = "Sampling error"
                android.util.Log.e(r5, r7, r6)     // Catch: java.lang.Throwable -> Ld6
                goto L89
            L9a:
                boolean r0 = r1.isEmpty()
                if (r0 == 0) goto La1
                goto La8
            La1:
                r0 = r1
                java.lang.Iterable r0 = (java.lang.Iterable) r0
                double r2 = kotlin.collections.CollectionsKt.averageOfDouble(r0)
            La8:
                int r0 = r1.size()
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                java.lang.String r6 = "Sampled "
                java.lang.StringBuilder r4 = r4.append(r6)
                java.lang.StringBuilder r0 = r4.append(r0)
                java.lang.String r4 = " pts; intensity="
                java.lang.StringBuilder r0 = r0.append(r4)
                java.lang.StringBuilder r0 = r0.append(r2)
                java.lang.String r0 = r0.toString()
                android.util.Log.i(r5, r0)
                com.nick.myrecoverytracker.SensorSampleWorker r0 = com.nick.myrecoverytracker.SensorSampleWorker.this
                com.nick.myrecoverytracker.SensorSampleWorker.access$writeIntensity(r0, r2)
                androidx.work.ListenableWorker$Result r0 = androidx.work.ListenableWorker.Result.success()
                return r0
            Ld6:
                r1 = move-exception
                r2 = r0
                android.hardware.SensorEventListener r2 = (android.hardware.SensorEventListener) r2
                r4.unregisterListener(r2)
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.SensorSampleWorker.AnonymousClass2.invokeSuspend(java.lang.Object):java.lang.Object");
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.SensorSampleWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.SensorSampleWorker$doWork$1 r0 = (com.nick.myrecoverytracker.SensorSampleWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.SensorSampleWorker$doWork$1 r0 = new com.nick.myrecoverytracker.SensorSampleWorker$doWork$1
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
            kotlinx.coroutines.CoroutineDispatcher r4 = kotlinx.coroutines.Dispatchers.getDefault()
            kotlin.coroutines.CoroutineContext r4 = (kotlin.coroutines.CoroutineContext) r4
            com.nick.myrecoverytracker.SensorSampleWorker$doWork$2 r5 = new com.nick.myrecoverytracker.SensorSampleWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.SensorSampleWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void writeIntensity(double value) {
        File f = new File(getApplicationContext().getFilesDir(), "movement_log.csv");
        if (!f.exists()) {
            FilesKt.writeText$default(f, "timestamp,intensity\n", null, 2, null);
        }
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        FilesKt.appendText$default(f, ts + "," + value + "\n", null, 2, null);
    }
}
