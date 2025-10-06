package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
import kotlin.collections.MapsKt;
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

/* compiled from: UnlockRollupWorker.kt */
@Metadata(d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u000e\u001a\u00020\u000fH\u0096@¢\u0006\u0002\u0010\u0010J\u001c\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\t0\u00122\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\rH\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082D¢\u0006\u0002\n\u0000¨\u0006\u0018"}, d2 = {"Lcom/nick/myrecoverytracker/UnlockRollupWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "recentDays", "", "zone", "Ljava/time/ZoneId;", "schemaVersion", "", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "readUnlockCounts", "", "dir", "Ljava/io/File;", "looksLikeHeader", "", "first", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UnlockRollupWorker extends CoroutineWorker {
    private final int recentDays;
    private final String schemaVersion;
    private final ZoneId zone;

    /* compiled from: UnlockRollupWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UnlockRollupWorker", f = "UnlockRollupWorker.kt", i = {}, l = {22}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UnlockRollupWorker$doWork$1, reason: invalid class name */
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
            return UnlockRollupWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public UnlockRollupWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        this.recentDays = 7;
        ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
        Intrinsics.checkNotNullExpressionValue(zoneIdSystemDefault, "systemDefault(...)");
        this.zone = zoneIdSystemDefault;
        this.schemaVersion = "v6.0";
    }

    /* compiled from: UnlockRollupWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.UnlockRollupWorker$doWork$2", f = "UnlockRollupWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.UnlockRollupWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return UnlockRollupWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            String string;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    try {
                        File dir = UnlockRollupWorker.this.getApplicationContext().getFilesDir();
                        if (dir == null) {
                            return ListenableWorker.Result.success();
                        }
                        File out = new File(dir, "daily_unlocks.csv");
                        Map counts = UnlockRollupWorker.this.readUnlockCounts(dir);
                        Map existing = new LinkedHashMap();
                        if (out.exists() && out.length() > 0) {
                            List<String> lines = FilesKt.readLines$default(out, null, 1, null);
                            if (!lines.isEmpty() && UnlockRollupWorker.this.looksLikeHeader((String) lines.get(0))) {
                                lines = CollectionsKt.drop(lines, 1);
                            }
                            for (String line : lines) {
                                List parts = StringsKt.split$default((CharSequence) line, new char[]{','}, false, 0, 6, (Object) null);
                                String str = (String) CollectionsKt.getOrNull(parts, 0);
                                String d = str != null ? StringsKt.trim((CharSequence) str).toString() : null;
                                if (d == null) {
                                    d = "";
                                }
                                String str2 = (String) CollectionsKt.getOrNull(parts, 2);
                                Integer v = (str2 == null || (string = StringsKt.trim((CharSequence) str2).toString()) == null) ? null : StringsKt.toIntOrNull(string);
                                if (d.length() == 10 && v != null) {
                                    existing.put(d, v);
                                }
                            }
                        }
                        Map merged = MapsKt.toMutableMap(existing);
                        for (Map.Entry entry : counts.entrySet()) {
                            merged.put((String) entry.getKey(), Boxing.boxInt(((Number) entry.getValue()).intValue()));
                        }
                        LocalDate todayLocal = LocalDate.now(UnlockRollupWorker.this.zone);
                        int i = UnlockRollupWorker.this.recentDays;
                        for (int i2 = 0; i2 < i; i2++) {
                            String d2 = todayLocal.minusDays(i2).toString();
                            Intrinsics.checkNotNullExpressionValue(d2, "toString(...)");
                            if (!merged.containsKey(d2)) {
                                merged.put(d2, Boxing.boxInt(0));
                            }
                        }
                        String localToday = todayLocal.toString();
                        Intrinsics.checkNotNullExpressionValue(localToday, "toString(...)");
                        String utcToday = LocalDate.now(ZoneOffset.UTC).toString();
                        Intrinsics.checkNotNullExpressionValue(utcToday, "toString(...)");
                        if (!Intrinsics.areEqual(localToday, utcToday) && merged.containsKey(localToday) && merged.containsKey(utcToday)) {
                            merged.remove(utcToday);
                        }
                        File tmp = new File(dir, "daily_unlocks.csv.tmp");
                        StringBuilder sb = new StringBuilder();
                        UnlockRollupWorker unlockRollupWorker = UnlockRollupWorker.this;
                        sb.append("date,feature_schema_version,daily_unlocks\n");
                        for (String str3 : CollectionsKt.sorted(merged.keySet())) {
                            sb.append(str3).append(',').append(unlockRollupWorker.schemaVersion).append(',').append(merged.get(str3)).append('\n');
                        }
                        String string2 = sb.toString();
                        Intrinsics.checkNotNullExpressionValue(string2, "toString(...)");
                        FilesKt.writeText$default(tmp, string2, null, 2, null);
                        if (out.exists()) {
                            out.delete();
                        }
                        tmp.renameTo(out);
                        return ListenableWorker.Result.success();
                    } catch (Throwable th) {
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.UnlockRollupWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.UnlockRollupWorker$doWork$1 r0 = (com.nick.myrecoverytracker.UnlockRollupWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.UnlockRollupWorker$doWork$1 r0 = new com.nick.myrecoverytracker.UnlockRollupWorker$doWork$1
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
            com.nick.myrecoverytracker.UnlockRollupWorker$doWork$2 r5 = new com.nick.myrecoverytracker.UnlockRollupWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.UnlockRollupWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Map<String, Integer> readUnlockCounts(File dir) {
        int i = 2;
        int i2 = 0;
        int i3 = 1;
        List files = CollectionsKt.listOf((Object[]) new File[]{new File(dir, "unlock_log.csv"), new File(dir, "unlocks_log.csv")});
        Map counts = new LinkedHashMap();
        Collection arrayList = new ArrayList();
        for (Object obj : files) {
            if (((File) obj).exists()) {
                arrayList.add(obj);
            }
        }
        Iterator it = ((List) arrayList).iterator();
        while (it.hasNext()) {
            List lines$default = FilesKt.readLines$default((File) it.next(), null, i3, null);
            Iterator it2 = ((lines$default.isEmpty() || !looksLikeHeader((String) lines$default.get(i2))) ? lines$default : CollectionsKt.drop(lines$default, i3)).iterator();
            while (it2.hasNext()) {
                String string = StringsKt.trim((CharSequence) it2.next()).toString();
                if ((string.length() == 0 ? i3 : i2) == 0) {
                    int i4 = i2;
                    char[] cArr = new char[i3];
                    cArr[i4] = ',';
                    List listSplit$default = StringsKt.split$default((CharSequence) string, cArr, false, 0, 6, (Object) null);
                    if (listSplit$default.size() >= i) {
                        String str = (String) listSplit$default.get(i4);
                        String string2 = StringsKt.trim((CharSequence) listSplit$default.get(1)).toString();
                        Locale US = Locale.US;
                        List files2 = files;
                        Intrinsics.checkNotNullExpressionValue(US, "US");
                        String upperCase = string2.toUpperCase(US);
                        Intrinsics.checkNotNullExpressionValue(upperCase, "toUpperCase(...)");
                        if (str.length() < 10 || str.charAt(4) != '-' || str.charAt(7) != '-' || !Intrinsics.areEqual(upperCase, "UNLOCK")) {
                            i3 = 1;
                            files = files2;
                            i = 2;
                            i2 = 0;
                        } else {
                            String strSubstring = str.substring(0, 10);
                            Intrinsics.checkNotNullExpressionValue(strSubstring, "substring(...)");
                            Integer num = (Integer) counts.get(strSubstring);
                            counts.put(strSubstring, Integer.valueOf((num != null ? num.intValue() : 0) + 1));
                            i3 = 1;
                            files = files2;
                            i = 2;
                            i2 = 0;
                        }
                    } else {
                        i3 = 1;
                        i = 2;
                        i2 = 0;
                    }
                } else {
                    i = 2;
                    i2 = 0;
                }
            }
            i = 2;
            i2 = 0;
        }
        return counts;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean looksLikeHeader(String first) {
        Locale US = Locale.US;
        Intrinsics.checkNotNullExpressionValue(US, "US");
        String l = first.toLowerCase(US);
        Intrinsics.checkNotNullExpressionValue(l, "toLowerCase(...)");
        return StringsKt.startsWith$default(l, "ts,", false, 2, (Object) null) || StringsKt.startsWith$default(l, "date,", false, 2, (Object) null);
    }
}
