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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
import kotlin.jvm.internal.Ref;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: StepCountWorker.kt */
@Metadata(d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u0000 \u00192\u00020\u0001:\u0001\u0019B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\nJ\u0017\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u0003H\u0002¢\u0006\u0002\u0010\u000eJ \u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\fH\u0002J \u0010\u0015\u001a\u00020\u00162\u0006\u0010\r\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0017\u001a\u00020\u0018H\u0002¨\u0006\u001a"}, d2 = {"Lcom/nick/myrecoverytracker/StepCountWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "readCurrentStepCounter", "", "ctx", "(Landroid/content/Context;)Ljava/lang/Float;", "readOrInitBaseline", "file", "Ljava/io/File;", "day", "", "current", "writeDaily", "", "steps", "", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class StepCountWorker extends CoroutineWorker {
    private static final String TAG = "StepCountWorker";

    /* compiled from: StepCountWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.StepCountWorker", f = "StepCountWorker.kt", i = {}, l = {35}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.StepCountWorker$doWork$1, reason: invalid class name */
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
            return StepCountWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public StepCountWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: StepCountWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.StepCountWorker$doWork$2", f = "StepCountWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.StepCountWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return StepCountWorker.this.new AnonymousClass2(continuation);
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
                    Context ctx = StepCountWorker.this.getApplicationContext();
                    Intrinsics.checkNotNullExpressionValue(ctx, "getApplicationContext(...)");
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    Float current = StepCountWorker.this.readCurrentStepCounter(ctx);
                    if (current == null) {
                        Log.w(StepCountWorker.TAG, "No step counter reading (sensor unavailable or timeout).");
                        return ListenableWorker.Result.success();
                    }
                    File baseFile = new File(ctx.getFilesDir(), "step_counter_baseline.json");
                    StepCountWorker stepCountWorker = StepCountWorker.this;
                    Intrinsics.checkNotNull(today);
                    float baseline = stepCountWorker.readOrInitBaseline(baseFile, today, current.floatValue());
                    int stepsToday = (int) Math.max(0.0f, current.floatValue() - baseline);
                    StepCountWorker.this.writeDaily(ctx, today, stepsToday);
                    Log.i(StepCountWorker.TAG, "StepCount " + today + ": current=" + current + " baseline=" + baseline + " -> steps=" + stepsToday);
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.StepCountWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.StepCountWorker$doWork$1 r0 = (com.nick.myrecoverytracker.StepCountWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.StepCountWorker$doWork$1 r0 = new com.nick.myrecoverytracker.StepCountWorker$doWork$1
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
            com.nick.myrecoverytracker.StepCountWorker$doWork$2 r5 = new com.nick.myrecoverytracker.StepCountWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.StepCountWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public final Float readCurrentStepCounter(Context ctx) throws InterruptedException {
        Object systemService = ctx.getSystemService("sensor");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.hardware.SensorManager");
        final SensorManager sm = (SensorManager) systemService;
        Sensor sensor = sm.getDefaultSensor(19);
        if (sensor == null) {
            return null;
        }
        final Ref.ObjectRef value = new Ref.ObjectRef();
        final CountDownLatch latch = new CountDownLatch(1);
        SensorEventListener sensorEventListener = new SensorEventListener() { // from class: com.nick.myrecoverytracker.StepCountWorker$readCurrentStepCounter$listener$1
            /* JADX WARN: Type inference failed for: r1v4, types: [T, java.lang.Float] */
            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(SensorEvent event) {
                Intrinsics.checkNotNullParameter(event, "event");
                if (event.sensor.getType() == 19) {
                    float[] values = event.values;
                    Intrinsics.checkNotNullExpressionValue(values, "values");
                    if (!(values.length == 0)) {
                        value.element = Float.valueOf(event.values[0]);
                        sm.unregisterListener(this);
                        latch.countDown();
                    }
                }
            }

            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor2, int accuracy) {
            }
        };
        sm.registerListener(sensorEventListener, sensor, 0);
        latch.await(1500L, TimeUnit.MILLISECONDS);
        sm.unregisterListener(sensorEventListener);
        return (Float) value.element;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final float readOrInitBaseline(File file, String day, float current) throws JSONException {
        try {
            if (file.exists()) {
                JSONObject json = new JSONObject(FilesKt.readText$default(file, null, 1, null));
                String savedDay = json.optString("day", "");
                double base = json.optDouble("baseline", Double.NaN);
                if (Intrinsics.areEqual(savedDay, day)) {
                    if (!Double.isNaN(base)) {
                        return (float) base;
                    }
                }
            }
        } catch (Throwable th) {
        }
        JSONObject obj = new JSONObject().put("day", day).put("baseline", current);
        File parentFile = file.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        String string = obj.toString();
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        FilesKt.writeText$default(file, string, null, 2, null);
        return current;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void writeDaily(Context ctx, String day, int steps) {
        File out = new File(ctx.getFilesDir(), "daily_steps.csv");
        List lines = out.exists() ? CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(out, null, 1, null)) : CollectionsKt.mutableListOf("date,total_steps");
        Collection arrayList = new ArrayList();
        for (Object obj : lines) {
            if (!StringsKt.startsWith$default((String) obj, day + ",", false, 2, (Object) null)) {
                arrayList.add(obj);
            }
        }
        List filtered = CollectionsKt.toMutableList(arrayList);
        filtered.add(day + "," + steps);
        FilesKt.writeText$default(out, CollectionsKt.joinToString$default(filtered, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }
}
