package com.nick.myrecoverytracker;

import android.content.Context;
import android.location.Location;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.Triple;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.sequences.SequencesKt;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: DistanceSummaryWorker.kt */
@Metadata(d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u0000 \u001b2\u00020\u0001:\u0001\u001bB\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\nJ\u0018\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0002J2\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\u00102\u0018\u0010\u0015\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\f0\u00170\u0016H\u0002J\u0010\u0010\u0018\u001a\u00020\u00102\u0006\u0010\u0019\u001a\u00020\u001aH\u0002¨\u0006\u001c"}, d2 = {"Lcom/nick/myrecoverytracker/DistanceSummaryWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "computeKmForDay", "", "locFile", "Ljava/io/File;", "day", "", "applyUpdates", "", "outFile", "header", "updates", "", "Lkotlin/Pair;", "dayString", "offsetDays", "", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class DistanceSummaryWorker extends CoroutineWorker {
    private static final String TAG = "DistanceSummaryWorker";

    /* compiled from: DistanceSummaryWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.DistanceSummaryWorker", f = "DistanceSummaryWorker.kt", i = {}, l = {17}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.DistanceSummaryWorker$doWork$1, reason: invalid class name */
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
            return DistanceSummaryWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DistanceSummaryWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: DistanceSummaryWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.DistanceSummaryWorker$doWork$2", f = "DistanceSummaryWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.DistanceSummaryWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return DistanceSummaryWorker.this.new AnonymousClass2(continuation);
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
                        File dir = DistanceSummaryWorker.this.getApplicationContext().getFilesDir();
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File locFile = new File(dir, "location_log.csv");
                        File outFile = new File(dir, "daily_distance_log.csv");
                        String today = DistanceSummaryWorker.this.dayString(0);
                        String yesterday = DistanceSummaryWorker.this.dayString(-1);
                        if (!locFile.exists()) {
                            DistanceSummaryWorker.this.applyUpdates(outFile, "date,distance_km\n", CollectionsKt.listOf((Object[]) new Pair[]{TuplesKt.to(yesterday, Boxing.boxFloat(0.0f)), TuplesKt.to(today, Boxing.boxFloat(0.0f))}));
                            Log.w(DistanceSummaryWorker.TAG, "location_log.csv missing; wrote zeros for " + yesterday + " and " + today);
                            return ListenableWorker.Result.success();
                        }
                        Iterable days = CollectionsKt.listOf((Object[]) new String[]{yesterday, today});
                        Iterable<String> iterable = days;
                        DistanceSummaryWorker distanceSummaryWorker = DistanceSummaryWorker.this;
                        Collection arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(iterable, 10));
                        for (String str : iterable) {
                            arrayList.add(TuplesKt.to(str, Boxing.boxFloat(distanceSummaryWorker.computeKmForDay(locFile, str))));
                        }
                        List<Pair> updates = (List) arrayList;
                        DistanceSummaryWorker.this.applyUpdates(outFile, "date,distance_km\n", updates);
                        for (Pair pair : updates) {
                            String str2 = (String) pair.component1();
                            String str3 = String.format("%.2f", Arrays.copyOf(new Object[]{Boxing.boxFloat(((Number) pair.component2()).floatValue())}, 1));
                            Intrinsics.checkNotNullExpressionValue(str3, "format(...)");
                            Log.i(DistanceSummaryWorker.TAG, "Distance(" + str2 + ") = " + str3 + " km");
                        }
                        return ListenableWorker.Result.success();
                    } catch (Throwable t) {
                        Log.e(DistanceSummaryWorker.TAG, "DistanceSummaryWorker error", t);
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.DistanceSummaryWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.DistanceSummaryWorker$doWork$1 r0 = (com.nick.myrecoverytracker.DistanceSummaryWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.DistanceSummaryWorker$doWork$1 r0 = new com.nick.myrecoverytracker.DistanceSummaryWorker$doWork$1
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
            com.nick.myrecoverytracker.DistanceSummaryWorker$doWork$2 r5 = new com.nick.myrecoverytracker.DistanceSummaryWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.DistanceSummaryWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final float computeKmForDay(File locFile, final String day) {
        Reader inputStreamReader = new InputStreamReader(new FileInputStream(locFile), Charsets.UTF_8);
        BufferedReader bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
        try {
            try {
                List points = SequencesKt.toList(SequencesKt.sortedWith(SequencesKt.mapNotNull(TextStreamsKt.lineSequence(bufferedReader), new Function1() { // from class: com.nick.myrecoverytracker.DistanceSummaryWorker$$ExternalSyntheticLambda0
                    @Override // kotlin.jvm.functions.Function1
                    public final Object invoke(Object obj) {
                        return DistanceSummaryWorker.computeKmForDay$lambda$3$lambda$1(day, (String) obj);
                    }
                }), new Comparator() { // from class: com.nick.myrecoverytracker.DistanceSummaryWorker$computeKmForDay$lambda$3$$inlined$sortedBy$1
                    /* JADX WARN: Multi-variable type inference failed */
                    @Override // java.util.Comparator
                    public final int compare(T t, T t2) {
                        return ComparisonsKt.compareValues((String) ((Triple) t).getFirst(), (String) ((Triple) t2).getFirst());
                    }
                }));
                CloseableKt.closeFinally(bufferedReader, null);
                float f = 0.0f;
                if (points.size() < 2) {
                    return 0.0f;
                }
                float meters = 0.0f;
                Location prevLoc = (Location) ((Triple) CollectionsKt.first(points)).getSecond();
                float prevAcc = ((Number) ((Triple) CollectionsKt.first(points)).getThird()).floatValue();
                int i = 1;
                int size = points.size();
                while (i < size) {
                    Triple triple = (Triple) points.get(i);
                    String ts = (String) triple.component1();
                    Location loc = (Location) triple.component2();
                    float acc = ((Number) triple.component3()).floatValue();
                    float d = prevLoc.distanceTo(loc);
                    boolean accOk = false;
                    boolean hopOk = (!Float.isInfinite(d) && !Float.isNaN(d)) && d >= f && d <= 30000.0f;
                    if ((acc == f) || acc <= 100.0f) {
                        if ((prevAcc == f) || prevAcc <= 100.0f) {
                            accOk = true;
                        }
                    }
                    if (hopOk && accOk) {
                        meters += d;
                        prevLoc = loc;
                        prevAcc = acc;
                    } else {
                        String str = String.format("%.1f", Arrays.copyOf(new Object[]{Float.valueOf(d)}, 1));
                        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
                        Log.d(TAG, "Skip hop @" + ts + " d=" + str + "m acc=" + acc + " prevAcc=" + prevAcc);
                    }
                    i++;
                    f = 0.0f;
                }
                return meters / 1000.0f;
            } catch (Throwable th) {
                th = th;
                Throwable th2 = th;
                try {
                    throw th2;
                } catch (Throwable th3) {
                    CloseableKt.closeFinally(bufferedReader, th2);
                    throw th3;
                }
            }
        } catch (Throwable th4) {
            th = th4;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Triple computeKmForDay$lambda$3$lambda$1(String $day, String line) {
        Float floatOrNull;
        Intrinsics.checkNotNullParameter(line, "line");
        List parts = StringsKt.split$default((CharSequence) line, new char[]{','}, false, 0, 6, (Object) null);
        if (parts.size() < 3 || !StringsKt.startsWith$default((String) parts.get(0), $day, false, 2, (Object) null)) {
            return null;
        }
        Double lat = StringsKt.toDoubleOrNull((String) parts.get(1));
        Double lon = StringsKt.toDoubleOrNull((String) parts.get(2));
        if (lat == null || lon == null) {
            return null;
        }
        String str = (String) CollectionsKt.getOrNull(parts, 3);
        float acc = (str == null || (floatOrNull = StringsKt.toFloatOrNull(str)) == null) ? 0.0f : floatOrNull.floatValue();
        Object obj = parts.get(0);
        Location location = new Location("");
        location.setLatitude(lat.doubleValue());
        location.setLongitude(lon.doubleValue());
        return new Triple(obj, location, Float.valueOf(acc));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v0 */
    /* JADX WARN: Type inference failed for: r12v1 */
    /* JADX WARN: Type inference failed for: r12v16 */
    public final void applyUpdates(File outFile, String header, List<Pair<String, Float>> updates) {
        ?? r12;
        List list;
        List list2;
        Set set;
        List list3;
        boolean z;
        File parentFile = outFile.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        List lines$default = outFile.exists() ? FilesKt.readLines$default(outFile, null, 1, null) : CollectionsKt.emptyList();
        ArrayList arrayList = new ArrayList();
        Iterator it = lines$default.iterator();
        while (true) {
            r12 = 0;
            if (!it.hasNext()) {
                break;
            }
            Object next = it.next();
            if (!StringsKt.startsWith$default((String) next, "date,", false, 2, (Object) null)) {
                arrayList.add(next);
            }
        }
        List mutableList = CollectionsKt.toMutableList((Collection) arrayList);
        List<Pair<String, Float>> list4 = updates;
        ArrayList arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(list4, 10));
        Iterator it2 = list4.iterator();
        while (it2.hasNext()) {
            arrayList2.add((String) ((Pair) it2.next()).getFirst());
        }
        Set set2 = CollectionsKt.toSet(arrayList2);
        ArrayList arrayList3 = new ArrayList();
        for (Object obj : mutableList) {
            String str = (String) obj;
            Set set3 = set2;
            if ((set3 instanceof Collection) && set3.isEmpty()) {
                list = lines$default;
                list2 = mutableList;
                set = set2;
                list3 = r12 == true ? 1 : 0;
                z = r12;
            } else {
                Iterator it3 = set3.iterator();
                boolean z2 = r12;
                while (true) {
                    if (!it3.hasNext()) {
                        list = lines$default;
                        list2 = mutableList;
                        set = set2;
                        list3 = z2 ? 1 : 0;
                        z = z2;
                        break;
                    }
                    list = lines$default;
                    Set set4 = set3;
                    list2 = mutableList;
                    set = set2;
                    list3 = null;
                    if (StringsKt.startsWith$default(str, ((String) it3.next()) + ",", false, 2, (Object) null)) {
                        z = true;
                        break;
                    }
                    z2 = false;
                    mutableList = list2;
                    lines$default = list;
                    set3 = set4;
                    set2 = set;
                }
            }
            if (!z) {
                arrayList3.add(obj);
            }
            r12 = list3;
            mutableList = list2;
            lines$default = list;
            set2 = set;
        }
        List mutableList2 = CollectionsKt.toMutableList((Collection) arrayList3);
        Iterator it4 = updates.iterator();
        while (it4.hasNext()) {
            Pair pair = (Pair) it4.next();
            String str2 = (String) pair.component1();
            float fFloatValue = ((Number) pair.component2()).floatValue();
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            String str3 = String.format(Locale.US, "%.2f", Arrays.copyOf(new Object[]{Float.valueOf(fFloatValue)}, 1));
            Intrinsics.checkNotNullExpressionValue(str3, "format(...)");
            mutableList2.add(str2 + "," + str3);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        Iterator it5 = mutableList2.iterator();
        while (it5.hasNext()) {
            sb.append((String) it5.next()).append('\n');
        }
        String string = sb.toString();
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        FilesKt.writeText$default(outFile, string, null, 2, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String dayString(int offsetDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(6, offsetDays);
        String str = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }
}
