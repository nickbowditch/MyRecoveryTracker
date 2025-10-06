package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkRequest;
import androidx.work.WorkerParameters;
import com.nick.myrecoverytracker.MovementRollupWorker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: MovementRollupWorker.kt */
@Metadata(d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000e\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u0014\u001a\u00020\u0015H\u0096@¢\u0006\u0002\u0010\u0016J0\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\r2\u0006\u0010\u001c\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\r2\u0006\u0010\u001e\u001a\u00020\rH\u0002J\u0018\u0010\u001f\u001a\u00020\u00182\u0006\u0010 \u001a\u00020\u001a2\u0006\u0010!\u001a\u00020\u001aH\u0002J \u0010\"\u001a\u00020\u00182\u0006\u0010 \u001a\u00020\u001a2\u0006\u0010#\u001a\u00020\u001a2\u0006\u0010$\u001a\u00020\u001aH\u0002J\u0010\u0010%\u001a\u00020\r2\u0006\u0010&\u001a\u00020\rH\u0002J\u0010\u0010'\u001a\u00020\u001a2\u0006\u0010&\u001a\u00020\rH\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000fX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\rX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\rX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\rX\u0082D¢\u0006\u0002\n\u0000¨\u0006("}, d2 = {"Lcom/nick/myrecoverytracker/MovementRollupWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "zone", "Ljava/time/ZoneId;", "tsFmt", "Ljava/time/format/DateTimeFormatter;", "STRIDE_METERS_PER_STEP", "", "DEFAULT_SAMPLE_WINDOW_MS", "", "MAX_GAP_CAP_MS", "THRESH_IGNORE", "THRESH_LOW", "THRESH_MOD", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsertDailyMovement", "", "day", "", "km", "low", "mod", "high", "ensureHeaderExact", "name", "expectedHeader", "upsertByFirstColumn", "key", "line", "round2", "v", "fmt2", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class MovementRollupWorker extends CoroutineWorker {
    private final long DEFAULT_SAMPLE_WINDOW_MS;
    private final long MAX_GAP_CAP_MS;
    private final double STRIDE_METERS_PER_STEP;
    private final double THRESH_IGNORE;
    private final double THRESH_LOW;
    private final double THRESH_MOD;
    private final DateTimeFormatter tsFmt;
    private final ZoneId zone;

    /* compiled from: MovementRollupWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.MovementRollupWorker", f = "MovementRollupWorker.kt", i = {}, l = {39}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.MovementRollupWorker$doWork$1, reason: invalid class name */
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
            return MovementRollupWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MovementRollupWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
        Intrinsics.checkNotNullExpressionValue(zoneIdSystemDefault, "systemDefault(...)");
        this.zone = zoneIdSystemDefault;
        DateTimeFormatter dateTimeFormatterOfPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);
        Intrinsics.checkNotNullExpressionValue(dateTimeFormatterOfPattern, "ofPattern(...)");
        this.tsFmt = dateTimeFormatterOfPattern;
        this.STRIDE_METERS_PER_STEP = 0.75d;
        this.DEFAULT_SAMPLE_WINDOW_MS = WorkRequest.MIN_BACKOFF_MILLIS;
        this.MAX_GAP_CAP_MS = 20000L;
        this.THRESH_IGNORE = 0.02d;
        this.THRESH_LOW = 0.15d;
        this.THRESH_MOD = 0.35d;
    }

    /* compiled from: MovementRollupWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.MovementRollupWorker$doWork$2", f = "MovementRollupWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.MovementRollupWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return MovementRollupWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            String str;
            int i;
            long nextT;
            String str2;
            long end;
            int i2;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    Object $result = obj;
                    LocalDate today = LocalDate.now(MovementRollupWorker.this.zone);
                    final long start = today.atStartOfDay(MovementRollupWorker.this.zone).toInstant().toEpochMilli();
                    final long end2 = ZonedDateTime.now(MovementRollupWorker.this.zone).toInstant().toEpochMilli();
                    File logFile = new File(MovementRollupWorker.this.getApplicationContext().getFilesDir(), "movement_log.csv");
                    String str3 = "MovementRollupWorker";
                    if (!logFile.exists()) {
                        Log.w("MovementRollupWorker", "movement_log.csv missing; writing zeros");
                        MovementRollupWorker movementRollupWorker = MovementRollupWorker.this;
                        String string = today.toString();
                        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
                        movementRollupWorker.upsertDailyMovement(string, 0.0d, 0.0d, 0.0d, 0.0d);
                        return ListenableWorker.Result.success();
                    }
                    final Ref.ObjectRef firstStep = new Ref.ObjectRef();
                    final Ref.ObjectRef lastStep = new Ref.ObjectRef();
                    final ArrayList samples = new ArrayList(1024);
                    final MovementRollupWorker movementRollupWorker2 = MovementRollupWorker.this;
                    FilesKt.forEachLine$default(logFile, null, new Function1() { // from class: com.nick.myrecoverytracker.MovementRollupWorker$doWork$2$$ExternalSyntheticLambda0
                        @Override // kotlin.jvm.functions.Function1
                        public final Object invoke(Object obj2) {
                            return MovementRollupWorker.AnonymousClass2.invokeSuspend$lambda$0(movementRollupWorker2, start, end2, firstStep, lastStep, samples, (String) obj2);
                        }
                    }, 1, null);
                    double steps = 0.0d;
                    if (firstStep.element != 0 && lastStep.element != 0) {
                        T t = lastStep.element;
                        Intrinsics.checkNotNull(t);
                        double dDoubleValue = ((Number) t).doubleValue();
                        T t2 = firstStep.element;
                        Intrinsics.checkNotNull(t2);
                        steps = Math.max(0.0d, dDoubleValue - ((Number) t2).doubleValue());
                    }
                    double km = MovementRollupWorker.this.round2((MovementRollupWorker.this.STRIDE_METERS_PER_STEP * steps) / 1000.0d);
                    ArrayList arrayList = samples;
                    if (arrayList.size() > 1) {
                        CollectionsKt.sortWith(arrayList, new Comparator() { // from class: com.nick.myrecoverytracker.MovementRollupWorker$doWork$2$invokeSuspend$$inlined$sortBy$1
                            /* JADX WARN: Multi-variable type inference failed */
                            @Override // java.util.Comparator
                            public final int compare(T t3, T t4) {
                                return ComparisonsKt.compareValues(Long.valueOf(((MovementRollupWorker.AnonymousClass2.Sample) t3).getT()), Long.valueOf(((MovementRollupWorker.AnonymousClass2.Sample) t4).getT()));
                            }
                        });
                    }
                    long lowMs = 0;
                    long modMs = 0;
                    long highMs = 0;
                    int i3 = 0;
                    int size = samples.size();
                    while (i3 < size) {
                        Object $result2 = $result;
                        Object $result3 = samples.get(i3);
                        int i4 = i3;
                        Intrinsics.checkNotNullExpressionValue($result3, "get(...)");
                        Sample cur = (Sample) $result3;
                        if (i4 + 1 < samples.size()) {
                            nextT = ((Sample) samples.get(i4 + 1)).getT();
                            str = str3;
                            i = size;
                        } else {
                            str = str3;
                            i = size;
                            nextT = Math.min(end2, cur.getT() + MovementRollupWorker.this.DEFAULT_SAMPLE_WINDOW_MS);
                        }
                        long dur = nextT - cur.getT();
                        if (dur <= 0) {
                            str2 = str;
                            end = end2;
                            i2 = i;
                        } else {
                            str2 = str;
                            end = end2;
                            i2 = i;
                            long dur2 = Math.min(dur, MovementRollupWorker.this.MAX_GAP_CAP_MS);
                            double mag = cur.getMag();
                            if (mag >= MovementRollupWorker.this.THRESH_IGNORE) {
                                if (mag < MovementRollupWorker.this.THRESH_LOW) {
                                    lowMs += dur2;
                                } else if (mag < MovementRollupWorker.this.THRESH_MOD) {
                                    modMs += dur2;
                                } else {
                                    highMs += dur2;
                                }
                            }
                        }
                        i3 = i4 + 1;
                        $result = $result2;
                        size = i2;
                        str3 = str2;
                        end2 = end;
                    }
                    double lowMin = MovementRollupWorker.this.round2(lowMs / 60000.0d);
                    double modMin = MovementRollupWorker.this.round2(modMs / 60000.0d);
                    double highMin = MovementRollupWorker.this.round2(highMs / 60000.0d);
                    MovementRollupWorker movementRollupWorker3 = MovementRollupWorker.this;
                    String string2 = today.toString();
                    Intrinsics.checkNotNullExpressionValue(string2, "toString(...)");
                    movementRollupWorker3.upsertDailyMovement(string2, km, lowMin, modMin, highMin);
                    Log.i(str3, "wrote daily: day=" + today + " km=" + km + " low=" + lowMin + " mod=" + modMin + " high=" + highMin);
                    return ListenableWorker.Result.success();
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }

        /* compiled from: MovementRollupWorker.kt */
        @Metadata(d1 = {"\u0000-\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000*\u0001\u0000\b\u008a\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0005HÆ\u0003J\"\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005HÆ\u0001¢\u0006\u0002\u0010\u000fJ\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0013\u001a\u00020\u0014HÖ\u0001J\t\u0010\u0015\u001a\u00020\u0016HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b¨\u0006\u0017"}, d2 = {"com/nick/myrecoverytracker/MovementRollupWorker$doWork$2$Sample", "", "t", "", "mag", "", "<init>", "(JD)V", "getT", "()J", "getMag", "()D", "component1", "component2", "copy", "(JD)Lcom/nick/myrecoverytracker/MovementRollupWorker$doWork$2$Sample;", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
        /* renamed from: com.nick.myrecoverytracker.MovementRollupWorker$doWork$2$Sample, reason: from toString */
        public static final /* data */ class Sample {
            private final double mag;
            private final long t;

            public static /* synthetic */ Sample copy$default(Sample sample, long j, double d, int i, Object obj) {
                if ((i & 1) != 0) {
                    j = sample.t;
                }
                if ((i & 2) != 0) {
                    d = sample.mag;
                }
                return sample.copy(j, d);
            }

            /* renamed from: component1, reason: from getter */
            public final long getT() {
                return this.t;
            }

            /* renamed from: component2, reason: from getter */
            public final double getMag() {
                return this.mag;
            }

            public final Sample copy(long t, double mag) {
                return new Sample(t, mag);
            }

            public boolean equals(Object other) {
                if (this == other) {
                    return true;
                }
                if (!(other instanceof Sample)) {
                    return false;
                }
                Sample sample = (Sample) other;
                return this.t == sample.t && Double.compare(this.mag, sample.mag) == 0;
            }

            public int hashCode() {
                return (Long.hashCode(this.t) * 31) + Double.hashCode(this.mag);
            }

            public String toString() {
                return "Sample(t=" + this.t + ", mag=" + this.mag + ")";
            }

            public Sample(long t, double mag) {
                this.t = t;
                this.mag = mag;
            }

            public final double getMag() {
                return this.mag;
            }

            public final long getT() {
                return this.t;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX WARN: Multi-variable type inference failed */
        public static final Unit invokeSuspend$lambda$0(MovementRollupWorker movementRollupWorker, long j, long j2, Ref.ObjectRef objectRef, Ref.ObjectRef objectRef2, ArrayList arrayList, String str) {
            int iIndexOf$default;
            String string = StringsKt.trim((CharSequence) str).toString();
            if (!(string.length() == 0) && (iIndexOf$default = StringsKt.indexOf$default((CharSequence) string, ',', 0, false, 6, (Object) null)) > 0) {
                String strSubstring = string.substring(0, iIndexOf$default);
                Intrinsics.checkNotNullExpressionValue(strSubstring, "substring(...)");
                String strSubstring2 = string.substring(iIndexOf$default + 1);
                Intrinsics.checkNotNullExpressionValue(strSubstring2, "substring(...)");
                try {
                    long epochMilli = ZonedDateTime.parse(strSubstring, movementRollupWorker.tsFmt.withZone(movementRollupWorker.zone)).toInstant().toEpochMilli();
                    if (epochMilli < j || epochMilli > j2) {
                        return Unit.INSTANCE;
                    }
                    if (StringsKt.startsWith$default(strSubstring2, "step,", false, 2, (Object) null)) {
                        List listSplit$default = StringsKt.split$default((CharSequence) strSubstring2, new char[]{','}, false, 0, 6, (Object) null);
                        if (listSplit$default.size() >= 2) {
                            String str2 = (String) CollectionsKt.getOrNull(listSplit$default, 1);
                            T doubleOrNull = str2 != null ? StringsKt.toDoubleOrNull(str2) : 0;
                            if (doubleOrNull != 0) {
                                if (objectRef.element == 0) {
                                    objectRef.element = doubleOrNull;
                                }
                                objectRef2.element = doubleOrNull;
                            }
                        }
                        return Unit.INSTANCE;
                    }
                    Double doubleOrNull2 = StringsKt.toDoubleOrNull(strSubstring2);
                    if (doubleOrNull2 != null) {
                        arrayList.add(new Sample(epochMilli, doubleOrNull2.doubleValue()));
                    }
                    return Unit.INSTANCE;
                } catch (Throwable th) {
                    return Unit.INSTANCE;
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.MovementRollupWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.MovementRollupWorker$doWork$1 r0 = (com.nick.myrecoverytracker.MovementRollupWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.MovementRollupWorker$doWork$1 r0 = new com.nick.myrecoverytracker.MovementRollupWorker$doWork$1
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
            com.nick.myrecoverytracker.MovementRollupWorker$doWork$2 r5 = new com.nick.myrecoverytracker.MovementRollupWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.MovementRollupWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void upsertDailyMovement(String day, double km, double low, double mod, double high) {
        ensureHeaderExact("daily_movement.csv", "date,km,low_min,mod_min,high_min,total_min");
        double total = round2(low + mod + high);
        String row = day + "," + fmt2(km) + "," + fmt2(low) + "," + fmt2(mod) + "," + fmt2(high) + "," + fmt2(total);
        upsertByFirstColumn("daily_movement.csv", day, row);
    }

    private final void ensureHeaderExact(String name, String expectedHeader) {
        FileOutputStream bufferedReader;
        String string;
        File f = new File(getApplicationContext().getFilesDir(), name);
        if (f.exists()) {
            Reader inputStreamReader = new InputStreamReader(new FileInputStream(f), Charsets.UTF_8);
            bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
            try {
                String line = bufferedReader.readLine();
                CloseableKt.closeFinally(bufferedReader, null);
                if (line == null || (string = StringsKt.trim((CharSequence) line).toString()) == null) {
                    string = "";
                }
                String current = string;
                if (Intrinsics.areEqual(current, expectedHeader)) {
                    return;
                }
                File backup = new File(getApplicationContext().getFilesDir(), name + ".legacy");
                try {
                    Result.Companion companion = Result.INSTANCE;
                    MovementRollupWorker movementRollupWorker = this;
                    Result.m212constructorimpl(FilesKt.copyTo$default(f, backup, true, 0, 4, null));
                } catch (Throwable th) {
                    Result.Companion companion2 = Result.INSTANCE;
                    Result.m212constructorimpl(ResultKt.createFailure(th));
                }
                bufferedReader = new FileOutputStream(f, false);
                try {
                    byte[] bytes = (expectedHeader + "\n").getBytes(Charsets.UTF_8);
                    Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
                    bufferedReader.write(bytes);
                    Unit unit = Unit.INSTANCE;
                    CloseableKt.closeFinally(bufferedReader, null);
                    return;
                } finally {
                    try {
                        throw th;
                    } finally {
                    }
                }
            } finally {
                try {
                    throw th;
                } finally {
                }
            }
        }
        bufferedReader = new FileOutputStream(f, false);
        try {
            byte[] bytes2 = (expectedHeader + "\n").getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes2, "getBytes(...)");
            bufferedReader.write(bytes2);
            Unit unit2 = Unit.INSTANCE;
            CloseableKt.closeFinally(bufferedReader, null);
        } finally {
        }
    }

    private final void upsertByFirstColumn(String name, String key, String line) {
        File f = new File(getApplicationContext().getFilesDir(), name);
        if (!f.exists()) {
            return;
        }
        List lines = CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(f, null, 1, null));
        if (lines.isEmpty()) {
            return;
        }
        String header = (String) CollectionsKt.first(lines);
        boolean replaced = false;
        int i = 1;
        int size = lines.size();
        while (true) {
            if (i >= size) {
                break;
            }
            String d = StringsKt.substringBefore$default((String) lines.get(i), ',', (String) null, 2, (Object) null);
            if (!Intrinsics.areEqual(d, key)) {
                i++;
            } else {
                lines.set(i, line);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lines.add(line);
        }
        FileOutputStream fileOutputStream = new FileOutputStream(f, false);
        try {
            byte[] bytes = (CollectionsKt.joinToString$default(CollectionsKt.plus((Collection) CollectionsKt.listOf(header), (Iterable) CollectionsKt.drop(lines, 1)), "\n", null, null, 0, null, null, 62, null) + "\n").getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            fileOutputStream.write(bytes);
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(fileOutputStream, null);
        } finally {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final double round2(double v) {
        return Math.rint(v * 100.0d) / 100.0d;
    }

    private final String fmt2(double v) {
        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
        String str = String.format(Locale.US, "%.2f", Arrays.copyOf(new Object[]{Double.valueOf(v)}, 1));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }
}
