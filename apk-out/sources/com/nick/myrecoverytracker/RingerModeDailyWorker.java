package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.nick.myrecoverytracker.RingerModeDailyWorker;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: RingerModeDailyWorker.kt */
@Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u000b2\u00020\u0001:\u0001\u000bB\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\n¨\u0006\f"}, d2 = {"Lcom/nick/myrecoverytracker/RingerModeDailyWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class RingerModeDailyWorker extends CoroutineWorker {
    private static final String TAG = "RingerModeDailyWorker";

    /* compiled from: RingerModeDailyWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.RingerModeDailyWorker", f = "RingerModeDailyWorker.kt", i = {}, l = {24}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.RingerModeDailyWorker$doWork$1, reason: invalid class name */
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
            return RingerModeDailyWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public RingerModeDailyWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: RingerModeDailyWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.RingerModeDailyWorker$doWork$2", f = "RingerModeDailyWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.RingerModeDailyWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return RingerModeDailyWorker.this.new AnonymousClass2(continuation);
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
                    Object $result = obj;
                    Context ctx = RingerModeDailyWorker.this.getApplicationContext();
                    Intrinsics.checkNotNullExpressionValue(ctx, "getApplicationContext(...)");
                    final String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                    File log = new File(ctx.getFilesDir(), "ringer_log.csv");
                    final Ref.IntRef normal = new Ref.IntRef();
                    final Ref.IntRef vibrate = new Ref.IntRef();
                    final Ref.IntRef silent = new Ref.IntRef();
                    if (log.exists()) {
                        FilesKt.forEachLine$default(log, null, new Function1() { // from class: com.nick.myrecoverytracker.RingerModeDailyWorker$doWork$2$$ExternalSyntheticLambda0
                            @Override // kotlin.jvm.functions.Function1
                            public final Object invoke(Object obj2) {
                                return RingerModeDailyWorker.AnonymousClass2.invokeSuspend$lambda$0(day, normal, vibrate, silent, (String) obj2);
                            }
                        }, 1, null);
                    }
                    int total = normal.element + vibrate.element + silent.element;
                    File out = new File(ctx.getFilesDir(), "daily_ringer_mode_changes.csv");
                    List lines = out.exists() ? CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(out, null, 1, null)) : CollectionsKt.mutableListOf("date,normal_changes,vibrate_changes,silent_changes,total_changes");
                    Collection arrayList = new ArrayList();
                    for (Object obj2 : lines) {
                        Object $result2 = $result;
                        if (!StringsKt.startsWith$default((String) obj2, day + ",", false, 2, (Object) null)) {
                            arrayList.add(obj2);
                        }
                        $result = $result2;
                    }
                    List filtered = CollectionsKt.toMutableList(arrayList);
                    filtered.add(day + "," + normal.element + "," + vibrate.element + "," + silent.element + "," + total);
                    FilesKt.writeText$default(out, CollectionsKt.joinToString$default(filtered, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
                    Log.i(RingerModeDailyWorker.TAG, "RingerModeDaily(" + day + "): normal=" + normal.element + " vibrate=" + vibrate.element + " silent=" + silent.element + " total=" + total);
                    return ListenableWorker.Result.success();
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static final Unit invokeSuspend$lambda$0(String $day, Ref.IntRef $normal, Ref.IntRef $vibrate, Ref.IntRef $silent, String line) {
            Intrinsics.checkNotNull($day);
            if (StringsKt.startsWith$default(line, $day, false, 2, (Object) null)) {
                String string = StringsKt.trim((CharSequence) StringsKt.substringAfter$default(line, ",", (String) null, 2, (Object) null)).toString();
                Locale US = Locale.US;
                Intrinsics.checkNotNullExpressionValue(US, "US");
                String mode = string.toLowerCase(US);
                Intrinsics.checkNotNullExpressionValue(mode, "toLowerCase(...)");
                if (StringsKt.startsWith$default(mode, "normal", false, 2, (Object) null)) {
                    $normal.element++;
                } else if (StringsKt.startsWith$default(mode, "vibrate", false, 2, (Object) null)) {
                    $vibrate.element++;
                } else if (StringsKt.startsWith$default(mode, NotificationCompat.GROUP_KEY_SILENT, false, 2, (Object) null)) {
                    $silent.element++;
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.RingerModeDailyWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.RingerModeDailyWorker$doWork$1 r0 = (com.nick.myrecoverytracker.RingerModeDailyWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.RingerModeDailyWorker$doWork$1 r0 = new com.nick.myrecoverytracker.RingerModeDailyWorker$doWork$1
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
            com.nick.myrecoverytracker.RingerModeDailyWorker$doWork$2 r5 = new com.nick.myrecoverytracker.RingerModeDailyWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.RingerModeDailyWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }
}
