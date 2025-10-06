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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import kotlin.collections.SetsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: AppSwitchingDailyWorker.kt */
@Metadata(d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0005\u0018\u0000 (2\u00020\u0001:\u0001(B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u0015\u001a\u00020\u0016H\u0096@¢\u0006\u0002\u0010\u0017J\u0018\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\r2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J&\u0010\u001d\u001a\u00020\u00192\u0006\u0010\u001e\u001a\u00020\r2\u0006\u0010\u001f\u001a\u00020\u001c2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\"0!H\u0002J\u0010\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020$H\u0002J\u0010\u0010&\u001a\u00020\u001c2\u0006\u0010'\u001a\u00020$H\u0002R\u0018\u0010\b\u001a\n \n*\u0004\u0018\u00010\t0\tX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u000bR\u001b\u0010\f\u001a\u00020\r8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\u0010\u0010\u0011\u001a\u0004\b\u000e\u0010\u000fR\u001b\u0010\u0012\u001a\u00020\r8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\u0014\u0010\u0011\u001a\u0004\b\u0013\u0010\u000f¨\u0006)"}, d2 = {"Lcom/nick/myrecoverytracker/AppSwitchingDailyWorker;", "Landroidx/work/CoroutineWorker;", "ctx", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "zone", "Ljava/time/ZoneId;", "kotlin.jvm.PlatformType", "Ljava/time/ZoneId;", "inFile", "Ljava/io/File;", "getInFile", "()Ljava/io/File;", "inFile$delegate", "Lkotlin/Lazy;", "outFile", "getOutFile", "outFile$delegate", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureHeader", "", "f", "header", "", "upsert", "file", "dateStr", "cols", "", "", "log2", "", "x", "format1", "v", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class AppSwitchingDailyWorker extends CoroutineWorker {
    private static final double LN2 = Math.log(2.0d);
    private static final String TAG = "AppSwitchingDailyWorker";

    /* renamed from: inFile$delegate, reason: from kotlin metadata */
    private final Lazy inFile;

    /* renamed from: outFile$delegate, reason: from kotlin metadata */
    private final Lazy outFile;
    private final ZoneId zone;

    /* compiled from: AppSwitchingDailyWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AppSwitchingDailyWorker", f = "AppSwitchingDailyWorker.kt", i = {}, l = {37}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AppSwitchingDailyWorker$doWork$1, reason: invalid class name */
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
            return AppSwitchingDailyWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AppSwitchingDailyWorker(Context ctx, WorkerParameters params) {
        super(ctx, params);
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(params, "params");
        this.zone = ZoneId.systemDefault();
        this.inFile = LazyKt.lazy(new Function0() { // from class: com.nick.myrecoverytracker.AppSwitchingDailyWorker$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function0
            public final Object invoke() {
                return AppSwitchingDailyWorker.inFile_delegate$lambda$0(this.f$0);
            }
        });
        this.outFile = LazyKt.lazy(new Function0() { // from class: com.nick.myrecoverytracker.AppSwitchingDailyWorker$$ExternalSyntheticLambda1
            @Override // kotlin.jvm.functions.Function0
            public final Object invoke() {
                return AppSwitchingDailyWorker.outFile_delegate$lambda$1(this.f$0);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final File getInFile() {
        return (File) this.inFile.getValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final File inFile_delegate$lambda$0(AppSwitchingDailyWorker this$0) {
        return new File(this$0.getApplicationContext().getFilesDir(), "usage_events.csv");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final File getOutFile() {
        return (File) this.outFile.getValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final File outFile_delegate$lambda$1(AppSwitchingDailyWorker this$0) {
        return new File(this$0.getApplicationContext().getFilesDir(), "daily_app_switching.csv");
    }

    /* compiled from: AppSwitchingDailyWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AppSwitchingDailyWorker$doWork$2", f = "AppSwitchingDailyWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AppSwitchingDailyWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return AppSwitchingDailyWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r8v0 */
        /* JADX WARN: Type inference failed for: r8v1, types: [boolean, int] */
        /* JADX WARN: Type inference failed for: r8v5 */
        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            int i;
            Iterator it;
            LocalDate today;
            int i2;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    try {
                        if (!AppSwitchingDailyWorker.this.getInFile().exists()) {
                            AppSwitchingDailyWorker.this.ensureHeader(AppSwitchingDailyWorker.this.getOutFile(), "date,switches,entropy");
                            Log.i(AppSwitchingDailyWorker.TAG, "No usage_events.csv yet");
                            return ListenableWorker.Result.success();
                        }
                        AppSwitchingDailyWorker.this.ensureHeader(AppSwitchingDailyWorker.this.getOutFile(), "date,switches,entropy");
                        LocalDate today2 = LocalDate.now(AppSwitchingDailyWorker.this.zone);
                        LocalDate yesterday = today2.minusDays(1L);
                        ?? r8 = 0;
                        Set targets = SetsKt.setOf((Object[]) new String[]{yesterday.toString(), today2.toString()});
                        HashMap map = new HashMap();
                        Reader inputStreamReader = new InputStreamReader(new FileInputStream(AppSwitchingDailyWorker.this.getInFile()), Charsets.UTF_8);
                        BufferedReader bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
                        try {
                            for (String str : TextStreamsKt.lineSequence(bufferedReader)) {
                                if (StringsKt.isBlank(str)) {
                                    today = today2;
                                    i2 = r8;
                                } else {
                                    today = today2;
                                    if (StringsKt.startsWith$default(str, "date,", (boolean) r8, 2, (Object) null)) {
                                        i2 = r8;
                                    } else {
                                        char[] cArr = new char[1];
                                        cArr[r8] = ',';
                                        List listSplit$default = StringsKt.split$default((CharSequence) str, cArr, false, 4, 2, (Object) null);
                                        if (listSplit$default.size() >= 4) {
                                            String str2 = (String) listSplit$default.get(r8);
                                            if (!targets.contains(str2)) {
                                                i2 = r8;
                                            } else if (Intrinsics.areEqual((String) listSplit$default.get(2), "FOREGROUND")) {
                                                String str3 = (String) listSplit$default.get(3);
                                                HashMap map2 = map;
                                                Object obj2 = map2.get(str2);
                                                if (obj2 == null) {
                                                    DayStats dayStats = new DayStats(null, 0, null, 7, null);
                                                    i2 = r8;
                                                    map2.put(str2, dayStats);
                                                    obj2 = dayStats;
                                                } else {
                                                    i2 = r8;
                                                }
                                                DayStats dayStats2 = (DayStats) obj2;
                                                String lastPkg = dayStats2.getLastPkg();
                                                if (lastPkg == null) {
                                                    dayStats2.setLastPkg(str3);
                                                } else if (!Intrinsics.areEqual(str3, lastPkg)) {
                                                    dayStats2.setSwitches(dayStats2.getSwitches() + 1);
                                                    dayStats2.setLastPkg(str3);
                                                }
                                                Map<String, Integer> freq = dayStats2.getFreq();
                                                Integer num = dayStats2.getFreq().get(str3);
                                                freq.put(str3, Boxing.boxInt((num != null ? num.intValue() : i2) + 1));
                                            } else {
                                                i2 = r8;
                                            }
                                        } else {
                                            i2 = r8;
                                        }
                                    }
                                }
                                today2 = today;
                                r8 = i2;
                            }
                            LocalDate today3 = today2;
                            int i3 = r8;
                            Unit unit = Unit.INSTANCE;
                            CloseableKt.closeFinally(bufferedReader, null);
                            String[] strArr = new String[2];
                            strArr[i3] = yesterday.toString();
                            strArr[1] = today3.toString();
                            Iterable iterableListOf = CollectionsKt.listOf((Object[]) strArr);
                            AppSwitchingDailyWorker appSwitchingDailyWorker = AppSwitchingDailyWorker.this;
                            int i4 = 0;
                            Iterator it2 = iterableListOf.iterator();
                            while (it2.hasNext()) {
                                String str4 = (String) it2.next();
                                Object obj3 = null;
                                DayStats dayStats3 = (DayStats) map.get(str4);
                                double dLog2 = 0.0d;
                                if (dayStats3 == null || dayStats3.getFreq().isEmpty()) {
                                    i = i4;
                                    it = it2;
                                } else {
                                    double dSumOfInt = CollectionsKt.sumOfInt(dayStats3.getFreq().values());
                                    Iterator<T> it3 = dayStats3.getFreq().values().iterator();
                                    while (it3.hasNext()) {
                                        Object obj4 = obj3;
                                        double dIntValue = ((Number) it3.next()).intValue() / dSumOfInt;
                                        dLog2 += (-dIntValue) * appSwitchingDailyWorker.log2(dIntValue);
                                        obj3 = obj4;
                                        i4 = i4;
                                        it2 = it2;
                                    }
                                    i = i4;
                                    it = it2;
                                }
                                File outFile = appSwitchingDailyWorker.getOutFile();
                                Intrinsics.checkNotNull(str4);
                                appSwitchingDailyWorker.upsert(outFile, str4, CollectionsKt.listOf(Boxing.boxInt(dayStats3 != null ? dayStats3.getSwitches() : i3), appSwitchingDailyWorker.format1(dLog2)));
                                Log.i(AppSwitchingDailyWorker.TAG, "AppSwitchingDaily " + str4 + " -> switches=" + (dayStats3 != null ? dayStats3.getSwitches() : i3) + " entropy=" + appSwitchingDailyWorker.format1(dLog2));
                                i4 = i;
                                it2 = it;
                            }
                            return ListenableWorker.Result.success();
                        } finally {
                        }
                    } catch (Throwable t) {
                        Log.e(AppSwitchingDailyWorker.TAG, "AppSwitchingDailyWorker failed", t);
                        return ListenableWorker.Result.retry();
                    }
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }

        /* compiled from: AppSwitchingDailyWorker.kt */
        @Metadata(d1 = {"\u0000'\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010%\n\u0002\b\u0012\n\u0002\u0010\u000b\n\u0002\b\u0004*\u0001\u0000\b\u008a\b\u0018\u00002\u00020\u0001B3\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\u0014\b\u0002\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00050\u0007¢\u0006\u0004\b\b\u0010\tJ\u000b\u0010\u0014\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\t\u0010\u0015\u001a\u00020\u0005HÆ\u0003J\u0015\u0010\u0016\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00050\u0007HÆ\u0003J:\u0010\u0017\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u0014\b\u0002\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00050\u0007HÆ\u0001¢\u0006\u0002\u0010\u0018J\u0013\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u001c\u001a\u00020\u0005HÖ\u0001J\t\u0010\u001d\u001a\u00020\u0003HÖ\u0001R\u001c\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001d\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00050\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013¨\u0006\u001e"}, d2 = {"com/nick/myrecoverytracker/AppSwitchingDailyWorker$doWork$2$DayStats", "", "lastPkg", "", "switches", "", "freq", "", "<init>", "(Ljava/lang/String;ILjava/util/Map;)V", "getLastPkg", "()Ljava/lang/String;", "setLastPkg", "(Ljava/lang/String;)V", "getSwitches", "()I", "setSwitches", "(I)V", "getFreq", "()Ljava/util/Map;", "component1", "component2", "component3", "copy", "(Ljava/lang/String;ILjava/util/Map;)Lcom/nick/myrecoverytracker/AppSwitchingDailyWorker$doWork$2$DayStats;", "equals", "", "other", "hashCode", "toString", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
        /* renamed from: com.nick.myrecoverytracker.AppSwitchingDailyWorker$doWork$2$DayStats, reason: from toString */
        public static final /* data */ class DayStats {
            private final Map<String, Integer> freq;
            private String lastPkg;
            private int switches;

            /* JADX WARN: Multi-variable type inference failed */
            public static /* synthetic */ DayStats copy$default(DayStats dayStats, String str, int i, Map map, int i2, Object obj) {
                if ((i2 & 1) != 0) {
                    str = dayStats.lastPkg;
                }
                if ((i2 & 2) != 0) {
                    i = dayStats.switches;
                }
                if ((i2 & 4) != 0) {
                    map = dayStats.freq;
                }
                return dayStats.copy(str, i, map);
            }

            /* renamed from: component1, reason: from getter */
            public final String getLastPkg() {
                return this.lastPkg;
            }

            /* renamed from: component2, reason: from getter */
            public final int getSwitches() {
                return this.switches;
            }

            public final Map<String, Integer> component3() {
                return this.freq;
            }

            public final DayStats copy(String lastPkg, int switches, Map<String, Integer> freq) {
                Intrinsics.checkNotNullParameter(freq, "freq");
                return new DayStats(lastPkg, switches, freq);
            }

            public boolean equals(Object other) {
                if (this == other) {
                    return true;
                }
                if (!(other instanceof DayStats)) {
                    return false;
                }
                DayStats dayStats = (DayStats) other;
                return Intrinsics.areEqual(this.lastPkg, dayStats.lastPkg) && this.switches == dayStats.switches && Intrinsics.areEqual(this.freq, dayStats.freq);
            }

            public int hashCode() {
                return ((((this.lastPkg == null ? 0 : this.lastPkg.hashCode()) * 31) + Integer.hashCode(this.switches)) * 31) + this.freq.hashCode();
            }

            public String toString() {
                return "DayStats(lastPkg=" + this.lastPkg + ", switches=" + this.switches + ", freq=" + this.freq + ")";
            }

            public DayStats(String lastPkg, int switches, Map<String, Integer> freq) {
                Intrinsics.checkNotNullParameter(freq, "freq");
                this.lastPkg = lastPkg;
                this.switches = switches;
                this.freq = freq;
            }

            public /* synthetic */ DayStats(String str, int i, HashMap map, int i2, DefaultConstructorMarker defaultConstructorMarker) {
                this((i2 & 1) != 0 ? null : str, (i2 & 2) != 0 ? 0 : i, (i2 & 4) != 0 ? new HashMap() : map);
            }

            public final String getLastPkg() {
                return this.lastPkg;
            }

            public final void setLastPkg(String str) {
                this.lastPkg = str;
            }

            public final int getSwitches() {
                return this.switches;
            }

            public final void setSwitches(int i) {
                this.switches = i;
            }

            public final Map<String, Integer> getFreq() {
                return this.freq;
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.AppSwitchingDailyWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.AppSwitchingDailyWorker$doWork$1 r0 = (com.nick.myrecoverytracker.AppSwitchingDailyWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.AppSwitchingDailyWorker$doWork$1 r0 = new com.nick.myrecoverytracker.AppSwitchingDailyWorker$doWork$1
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
            com.nick.myrecoverytracker.AppSwitchingDailyWorker$doWork$2 r5 = new com.nick.myrecoverytracker.AppSwitchingDailyWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.AppSwitchingDailyWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
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

    /* JADX INFO: Access modifiers changed from: private */
    public final void upsert(File file, String dateStr, List<? extends Object> cols) {
        String header;
        if (file.exists() && file.length() > 0) {
            header = (String) CollectionsKt.first(FilesKt.readLines$default(file, null, 1, null));
        } else {
            header = "date,switches,entropy";
        }
        List lines = file.exists() ? CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(file, null, 1, null)) : CollectionsKt.mutableListOf(header);
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
        FilesKt.writeText$default(file, CollectionsKt.joinToString$default(lines, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final double log2(double x) {
        return Math.log(x) / LN2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String format1(double v) {
        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
        String str = String.format(Locale.US, "%.1f", Arrays.copyOf(new Object[]{Double.valueOf(v)}, 1));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }
}
