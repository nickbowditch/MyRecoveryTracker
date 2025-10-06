package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.nick.myrecoverytracker.MovementIntensityDailyWorker;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: MovementIntensityDailyWorker.kt */
@Metadata(d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\b\u0018\u0000 *2\u00020\u0001:\u0001*B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u0018\u001a\u00020\u0019H\u0096@¢\u0006\u0002\u0010\u001aJ\"\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\u001d\u0012\u0004\u0012\u00020\u001e0\u001c2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020!0 H\u0002J \u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\u00102\u0006\u0010%\u001a\u00020\u001d2\u0006\u0010&\u001a\u00020\u001eH\u0002J\u0018\u0010'\u001a\u00020#2\u0006\u0010(\u001a\u00020\u00102\u0006\u0010)\u001a\u00020\u001dH\u0002R\u0018\u0010\b\u001a\n \n*\u0004\u0018\u00010\t0\tX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u000bR\u0018\u0010\f\u001a\n \n*\u0004\u0018\u00010\r0\rX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u000eR\u001b\u0010\u000f\u001a\u00020\u00108BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u0011\u0010\u0012R\u001b\u0010\u0015\u001a\u00020\u00108BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\u0017\u0010\u0014\u001a\u0004\b\u0016\u0010\u0012¨\u0006+"}, d2 = {"Lcom/nick/myrecoverytracker/MovementIntensityDailyWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "zone", "Ljava/time/ZoneId;", "kotlin.jvm.PlatformType", "Ljava/time/ZoneId;", "fmtDate", "Ljava/time/format/DateTimeFormatter;", "Ljava/time/format/DateTimeFormatter;", "outFile", "Ljava/io/File;", "getOutFile", "()Ljava/io/File;", "outFile$delegate", "Lkotlin/Lazy;", "unlockLog", "getUnlockLog", "unlockLog$delegate", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "countUnlocks", "", "", "", "days", "", "Ljava/time/LocalDate;", "writeOrReplaceRow", "", "file", "dateStr", "intensity", "ensureHeader", "f", "header", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class MovementIntensityDailyWorker extends CoroutineWorker {
    private static final String TAG = "MovementIntensityDaily";
    private final DateTimeFormatter fmtDate;

    /* renamed from: outFile$delegate, reason: from kotlin metadata */
    private final Lazy outFile;

    /* renamed from: unlockLog$delegate, reason: from kotlin metadata */
    private final Lazy unlockLog;
    private final ZoneId zone;

    /* compiled from: MovementIntensityDailyWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.MovementIntensityDailyWorker", f = "MovementIntensityDailyWorker.kt", i = {}, l = {36}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.MovementIntensityDailyWorker$doWork$1, reason: invalid class name */
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
            return MovementIntensityDailyWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovementIntensityDailyWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        this.zone = ZoneId.systemDefault();
        this.fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        this.outFile = LazyKt.lazy(new Function0() { // from class: com.nick.myrecoverytracker.MovementIntensityDailyWorker$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function0
            public final Object invoke() {
                return MovementIntensityDailyWorker.outFile_delegate$lambda$0(this.f$0);
            }
        });
        this.unlockLog = LazyKt.lazy(new Function0() { // from class: com.nick.myrecoverytracker.MovementIntensityDailyWorker$$ExternalSyntheticLambda1
            @Override // kotlin.jvm.functions.Function0
            public final Object invoke() {
                return MovementIntensityDailyWorker.unlockLog_delegate$lambda$1(this.f$0);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final File getOutFile() {
        return (File) this.outFile.getValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final File outFile_delegate$lambda$0(MovementIntensityDailyWorker this$0) {
        return new File(this$0.getApplicationContext().getFilesDir(), "daily_movement_intensity.csv");
    }

    private final File getUnlockLog() {
        return (File) this.unlockLog.getValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final File unlockLog_delegate$lambda$1(MovementIntensityDailyWorker this$0) {
        return new File(this$0.getApplicationContext().getFilesDir(), "unlock_log.csv");
    }

    /* compiled from: MovementIntensityDailyWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.MovementIntensityDailyWorker$doWork$2", f = "MovementIntensityDailyWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.MovementIntensityDailyWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return MovementIntensityDailyWorker.this.new AnonymousClass2(continuation);
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
                        MovementIntensityDailyWorker.this.ensureHeader(MovementIntensityDailyWorker.this.getOutFile(), "date,intensity");
                        LocalDate today = LocalDate.now(MovementIntensityDailyWorker.this.zone);
                        LocalDate yesterday = today.minusDays(1L);
                        List days = CollectionsKt.listOf((Object[]) new LocalDate[]{yesterday, today});
                        final Map intensities = MovementIntensityDailyWorker.this.countUnlocks(days);
                        MovementIntensityDailyWorker movementIntensityDailyWorker = MovementIntensityDailyWorker.this;
                        Iterator it = days.iterator();
                        while (it.hasNext()) {
                            String str = ((LocalDate) it.next()).format(movementIntensityDailyWorker.fmtDate);
                            File outFile = movementIntensityDailyWorker.getOutFile();
                            Intrinsics.checkNotNull(str);
                            Integer num = (Integer) intensities.get(str);
                            movementIntensityDailyWorker.writeOrReplaceRow(outFile, str, num != null ? num.intValue() : 0);
                        }
                        final MovementIntensityDailyWorker movementIntensityDailyWorker2 = MovementIntensityDailyWorker.this;
                        Log.i(MovementIntensityDailyWorker.TAG, "MovementIntensityDaily -> " + CollectionsKt.joinToString$default(days, null, null, null, 0, null, new Function1() { // from class: com.nick.myrecoverytracker.MovementIntensityDailyWorker$doWork$2$$ExternalSyntheticLambda0
                            @Override // kotlin.jvm.functions.Function1
                            public final Object invoke(Object obj2) {
                                return MovementIntensityDailyWorker.AnonymousClass2.invokeSuspend$lambda$1(movementIntensityDailyWorker2, intensities, (LocalDate) obj2);
                            }
                        }, 31, null));
                        return ListenableWorker.Result.success();
                    } catch (Throwable t) {
                        Log.e(MovementIntensityDailyWorker.TAG, "MovementIntensityDailyWorker failed", t);
                        return ListenableWorker.Result.retry();
                    }
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static final CharSequence invokeSuspend$lambda$1(MovementIntensityDailyWorker this$0, Map $intensities, LocalDate it) {
            String str = it.format(this$0.fmtDate);
            Integer num = (Integer) $intensities.get(it.format(this$0.fmtDate));
            return str + "=" + (num != null ? num.intValue() : 0);
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.MovementIntensityDailyWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.MovementIntensityDailyWorker$doWork$1 r0 = (com.nick.myrecoverytracker.MovementIntensityDailyWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.MovementIntensityDailyWorker$doWork$1 r0 = new com.nick.myrecoverytracker.MovementIntensityDailyWorker$doWork$1
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
            com.nick.myrecoverytracker.MovementIntensityDailyWorker$doWork$2 r5 = new com.nick.myrecoverytracker.MovementIntensityDailyWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.MovementIntensityDailyWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Map<String, Integer> countUnlocks(List<LocalDate> days) {
        List<LocalDate> list = days;
        Collection arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(list, 10));
        Iterator it = list.iterator();
        while (it.hasNext()) {
            arrayList.add(((LocalDate) it.next()).format(this.fmtDate));
        }
        final Set datesWanted = CollectionsKt.toSet((List) arrayList);
        if (getUnlockLog().exists()) {
            final HashMap map = new HashMap(datesWanted.size());
            FilesKt.forEachLine$default(getUnlockLog(), null, new Function1() { // from class: com.nick.myrecoverytracker.MovementIntensityDailyWorker$$ExternalSyntheticLambda2
                @Override // kotlin.jvm.functions.Function1
                public final Object invoke(Object obj) {
                    return MovementIntensityDailyWorker.countUnlocks$lambda$4(datesWanted, map, (String) obj);
                }
            }, 1, null);
            Iterator it2 = datesWanted.iterator();
            while (it2.hasNext()) {
                map.putIfAbsent((String) it2.next(), 0);
            }
            return map;
        }
        Set set = datesWanted;
        LinkedHashMap linkedHashMap = new LinkedHashMap(RangesKt.coerceAtLeast(MapsKt.mapCapacity(CollectionsKt.collectionSizeOrDefault(set, 10)), 16));
        for (Object obj : set) {
            linkedHashMap.put(obj, 0);
        }
        return linkedHashMap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit countUnlocks$lambda$4(Set $datesWanted, HashMap $map, String line) {
        Intrinsics.checkNotNullParameter(line, "line");
        if (line.length() < 10) {
            return Unit.INSTANCE;
        }
        String datePrefix = line.substring(0, 10);
        Intrinsics.checkNotNullExpressionValue(datePrefix, "substring(...)");
        if ($datesWanted.contains(datePrefix) && StringsKt.contains$default((CharSequence) line, (CharSequence) "UNLOCK", false, 2, (Object) null)) {
            HashMap map = $map;
            Integer num = (Integer) $map.get(datePrefix);
            map.put(datePrefix, Integer.valueOf((num != null ? num.intValue() : 0) + 1));
        }
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void writeOrReplaceRow(File file, String dateStr, int intensity) {
        String header;
        List existing;
        String header2;
        boolean z;
        String header3 = "date,intensity";
        List existing2 = file.exists() ? CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(file, null, 1, null)) : CollectionsKt.mutableListOf("date,intensity");
        Collection arrayList = new ArrayList();
        int i = 0;
        for (Object obj : existing2) {
            int i2 = i + 1;
            if (i < 0) {
                CollectionsKt.throwIndexOverflow();
            }
            String str = (String) obj;
            if (i == 0) {
                header = header3;
                existing = existing2;
                header2 = null;
                z = true;
            } else {
                header = header3;
                existing = existing2;
                header2 = null;
                z = !StringsKt.startsWith$default(str, new StringBuilder().append(dateStr).append(",").toString(), false, 2, (Object) null);
            }
            if (z) {
                arrayList.add(obj);
            }
            header3 = header;
            i = i2;
            existing2 = existing;
        }
        List kept = CollectionsKt.toMutableList(arrayList);
        kept.add(dateStr + "," + intensity);
        FilesKt.writeText$default(file, CollectionsKt.joinToString$default(kept, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
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
