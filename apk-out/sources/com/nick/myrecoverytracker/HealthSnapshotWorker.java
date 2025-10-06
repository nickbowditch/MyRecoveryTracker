package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
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
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: HealthSnapshotWorker.kt */
@Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u000b\u001a\u00020\fH\u0096@¢\u0006\u0002\u0010\rR\u000e\u0010\b\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u000e"}, d2 = {"Lcom/nick/myrecoverytracker/HealthSnapshotWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "ctx", "tsFmt", "Ljava/text/SimpleDateFormat;", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class HealthSnapshotWorker extends CoroutineWorker {
    private final Context ctx;
    private final SimpleDateFormat tsFmt;

    /* compiled from: HealthSnapshotWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.HealthSnapshotWorker", f = "HealthSnapshotWorker.kt", i = {}, l = {17}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.HealthSnapshotWorker$doWork$1, reason: invalid class name */
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
            return HealthSnapshotWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public HealthSnapshotWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        Context applicationContext = getApplicationContext();
        Intrinsics.checkNotNullExpressionValue(applicationContext, "getApplicationContext(...)");
        this.ctx = applicationContext;
        this.tsFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    /* compiled from: HealthSnapshotWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.HealthSnapshotWorker$doWork$2", f = "HealthSnapshotWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.HealthSnapshotWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return HealthSnapshotWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            int rawRows;
            int rollRows;
            Object objPrevious;
            List listSplit$default;
            String str;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    Object $result = obj;
                    try {
                        File filesDir = HealthSnapshotWorker.this.ctx.getFilesDir();
                        try {
                            if (filesDir == null) {
                                return ListenableWorker.Result.success();
                            }
                            File unlockRaw = new File(filesDir, "unlock_log.csv");
                            File unlockRoll = new File(filesDir, "daily_unlocks.csv");
                            File uploadLog = new File(filesDir, "redcap_upload_log.csv");
                            File out = new File(filesDir, "daily_health.csv");
                            if (!unlockRaw.exists()) {
                                rawRows = 0;
                            } else {
                                Iterable<String> lines$default = FilesKt.readLines$default(unlockRaw, null, 1, null);
                                if ((lines$default instanceof Collection) && ((Collection) lines$default).isEmpty()) {
                                    rawRows = 0;
                                } else {
                                    rawRows = 0;
                                    for (String str2 : lines$default) {
                                        if (((StringsKt.isBlank(str2) || StringsKt.startsWith$default(str2, "ts,", false, 2, (Object) null)) ? 0 : 1) != 0 && (rawRows = rawRows + 1) < 0) {
                                            CollectionsKt.throwCountOverflow();
                                        }
                                    }
                                }
                            }
                            if (!unlockRoll.exists()) {
                                rollRows = 0;
                            } else {
                                Iterable<String> lines$default2 = FilesKt.readLines$default(unlockRoll, null, 1, null);
                                if ((lines$default2 instanceof Collection) && ((Collection) lines$default2).isEmpty()) {
                                    rollRows = 0;
                                } else {
                                    rollRows = 0;
                                    for (String str3 : lines$default2) {
                                        if (((StringsKt.isBlank(str3) || StringsKt.startsWith$default(str3, "date,", false, 2, (Object) null)) ? 0 : 1) != 0 && (rollRows = rollRows + 1) < 0) {
                                            CollectionsKt.throwCountOverflow();
                                        }
                                    }
                                }
                            }
                            String lastUpload = "none";
                            if (uploadLog.exists()) {
                                List lines$default3 = FilesKt.readLines$default(uploadLog, null, 1, null);
                                ListIterator listIterator = lines$default3.listIterator(lines$default3.size());
                                while (true) {
                                    if (listIterator.hasPrevious()) {
                                        objPrevious = listIterator.previous();
                                        Object $result2 = $result;
                                        try {
                                            if (!StringsKt.contains$default((CharSequence) objPrevious, (CharSequence) "daily_metrics.csv", false, 2, (Object) null)) {
                                                $result = $result2;
                                            }
                                        } catch (Throwable th) {
                                            return ListenableWorker.Result.retry();
                                        }
                                    } else {
                                        objPrevious = null;
                                    }
                                }
                                String str4 = (String) objPrevious;
                                if (str4 != null && (listSplit$default = StringsKt.split$default((CharSequence) str4, new char[]{','}, false, 0, 6, (Object) null)) != null && (str = (String) CollectionsKt.getOrNull(listSplit$default, 2)) != null) {
                                    lastUpload = str;
                                }
                            }
                            if (!out.exists()) {
                                FilesKt.writeText$default(out, "ts,unlocks_raw_rows,unlocks_rollup_rows,last_upload_status\n", null, 2, null);
                            }
                            String line = HealthSnapshotWorker.this.tsFmt.format(Boxing.boxLong(System.currentTimeMillis())) + "," + rawRows + "," + rollRows + "," + lastUpload + "\n";
                            FilesKt.appendText$default(out, line, null, 2, null);
                            return ListenableWorker.Result.success();
                        } catch (Throwable th2) {
                        }
                    } catch (Throwable th3) {
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.HealthSnapshotWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.HealthSnapshotWorker$doWork$1 r0 = (com.nick.myrecoverytracker.HealthSnapshotWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.HealthSnapshotWorker$doWork$1 r0 = new com.nick.myrecoverytracker.HealthSnapshotWorker$doWork$1
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
            com.nick.myrecoverytracker.HealthSnapshotWorker$doWork$2 r5 = new com.nick.myrecoverytracker.HealthSnapshotWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.HealthSnapshotWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }
}
