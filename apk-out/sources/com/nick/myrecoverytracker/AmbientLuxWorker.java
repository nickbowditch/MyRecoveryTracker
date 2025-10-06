package com.nick.myrecoverytracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CancellableContinuation;
import kotlinx.coroutines.CancellableContinuationImpl;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: AmbientLuxWorker.kt */
@Metadata(d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\f\u001a\u00020\rH\u0096@¢\u0006\u0002\u0010\u000eJ4\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019H\u0082@¢\u0006\u0002\u0010\u001aJ\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\tH\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006 "}, d2 = {"Lcom/nick/myrecoverytracker/AmbientLuxWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "tag", "", "tsFmt", "Ljava/text/SimpleDateFormat;", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sampleLux", "", "", "sm", "Landroid/hardware/SensorManager;", "sensor", "Landroid/hardware/Sensor;", "millis", "", "minCount", "", "(Landroid/hardware/SensorManager;Landroid/hardware/Sensor;JILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureHeader", "", "f", "Ljava/io/File;", "header", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class AmbientLuxWorker extends CoroutineWorker {
    private final String tag;
    private final SimpleDateFormat tsFmt;

    /* compiled from: AmbientLuxWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AmbientLuxWorker", f = "AmbientLuxWorker.kt", i = {}, l = {33}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AmbientLuxWorker$doWork$1, reason: invalid class name */
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
            return AmbientLuxWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AmbientLuxWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        this.tag = "AmbientLuxWorker";
        this.tsFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    /* compiled from: AmbientLuxWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AmbientLuxWorker$doWork$2", f = "AmbientLuxWorker.kt", i = {}, l = {41}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AmbientLuxWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return AmbientLuxWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object $result) throws Throwable {
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    Object systemService = AmbientLuxWorker.this.getApplicationContext().getSystemService("sensor");
                    Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.hardware.SensorManager");
                    SensorManager sm = (SensorManager) systemService;
                    Sensor sensor = sm.getDefaultSensor(5);
                    if (sensor == null) {
                        Log.w(AmbientLuxWorker.this.tag, "No TYPE_LIGHT sensor; skipping");
                        return ListenableWorker.Result.success();
                    }
                    this.label = 1;
                    Object objSampleLux = AmbientLuxWorker.this.sampleLux(sm, sensor, 2000L, 6, this);
                    if (objSampleLux != coroutine_suspended) {
                        $result = objSampleLux;
                        break;
                    } else {
                        return coroutine_suspended;
                    }
                case 1:
                    ResultKt.throwOnFailure($result);
                    break;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            List samples = (List) $result;
            if (samples.isEmpty()) {
                Log.w(AmbientLuxWorker.this.tag, "No lux samples captured");
                return ListenableWorker.Result.retry();
            }
            float avg = (float) CollectionsKt.averageOfFloat(samples);
            File file = new File(AmbientLuxWorker.this.getApplicationContext().getFilesDir(), "ambient_lux.csv");
            AmbientLuxWorker.this.ensureHeader(file, "timestamp,lux");
            FileWriter fileWriter = new FileWriter(file, true);
            try {
                Appendable appendableAppend = fileWriter.append((CharSequence) (AmbientLuxWorker.this.tsFmt.format(new Date()) + "," + avg));
                Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
                CloseableKt.closeFinally(fileWriter, null);
                Log.i(AmbientLuxWorker.this.tag, "Logged lux avg=" + avg + " (n=" + samples.size() + ")");
                return ListenableWorker.Result.success();
            } finally {
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.AmbientLuxWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.AmbientLuxWorker$doWork$1 r0 = (com.nick.myrecoverytracker.AmbientLuxWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.AmbientLuxWorker$doWork$1 r0 = new com.nick.myrecoverytracker.AmbientLuxWorker$doWork$1
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
            com.nick.myrecoverytracker.AmbientLuxWorker$doWork$2 r5 = new com.nick.myrecoverytracker.AmbientLuxWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.AmbientLuxWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r4v0, types: [com.nick.myrecoverytracker.AmbientLuxWorker$sampleLux$2$listener$1] */
    public final Object sampleLux(final SensorManager sm, Sensor sensor, final long millis, final int minCount, Continuation<? super List<Float>> continuation) {
        CancellableContinuationImpl cancellableContinuationImpl = new CancellableContinuationImpl(IntrinsicsKt.intercepted(continuation), 1);
        cancellableContinuationImpl.initCancellability();
        final CancellableContinuationImpl cancellableContinuationImpl2 = cancellableContinuationImpl;
        final ArrayList arrayList = new ArrayList(32);
        final ?? r4 = new SensorEventListener() { // from class: com.nick.myrecoverytracker.AmbientLuxWorker$sampleLux$2$listener$1
            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(SensorEvent event) {
                Intrinsics.checkNotNullParameter(event, "event");
                if (event.sensor.getType() == 5) {
                    arrayList.add(Float.valueOf(event.values[0]));
                    if (arrayList.size() >= minCount) {
                        try {
                            sm.unregisterListener(this);
                        } catch (Throwable th) {
                        }
                        if (!cancellableContinuationImpl2.isCompleted()) {
                            CancellableContinuation<List<Float>> cancellableContinuation = cancellableContinuationImpl2;
                            Result.Companion companion = Result.INSTANCE;
                            cancellableContinuation.resumeWith(Result.m212constructorimpl(CollectionsKt.toList(arrayList)));
                        }
                    }
                }
            }

            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor2, int accuracy) {
            }
        };
        sm.registerListener((SensorEventListener) r4, sensor, 3);
        Thread thread = new Thread(new Runnable() { // from class: com.nick.myrecoverytracker.AmbientLuxWorker$sampleLux$2$t$1
            @Override // java.lang.Runnable
            public final void run() throws InterruptedException {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                }
                try {
                    sm.unregisterListener(r4);
                } catch (Throwable th) {
                }
                if (!cancellableContinuationImpl2.isCompleted()) {
                    CancellableContinuation<List<Float>> cancellableContinuation = cancellableContinuationImpl2;
                    Result.Companion companion = Result.INSTANCE;
                    cancellableContinuation.resumeWith(Result.m212constructorimpl(CollectionsKt.toList(arrayList)));
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        cancellableContinuationImpl2.invokeOnCancellation(new Function1<Throwable, Unit>() { // from class: com.nick.myrecoverytracker.AmbientLuxWorker$sampleLux$2$1
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Throwable th) {
                invoke2(th);
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2(Throwable it) {
                try {
                    sm.unregisterListener(r4);
                } catch (Throwable th) {
                }
            }
        });
        Object result = cancellableContinuationImpl.getResult();
        if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
            DebugProbesKt.probeCoroutineSuspended(continuation);
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void ensureHeader(File f, String header) {
        if (!f.exists() || f.length() == 0) {
            File parentFile = f.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FilesKt.writeText$default(f, header + "\n", null, 2, null);
        }
    }
}
