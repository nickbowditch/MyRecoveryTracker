package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.nick.myrecoverytracker.NotificationLatencyWorker;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.ArrayDeque;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: NotificationLatencyWorker.kt */
@Metadata(d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\b\u0018\u0000 (2\u00020\u0001:\u0002'(B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\nJ\u0012\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0018\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u000e\u0018\u00010\u00102\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0010\u0010\u0011\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\u000eH\u0002J\u0017\u0010\u0013\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0015\u001a\u00020\u000eH\u0002¢\u0006\u0002\u0010\u0016J8\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001d2\u0006\u0010\u001f\u001a\u00020\u001d2\u0006\u0010 \u001a\u00020!H\u0002J\u001e\u0010\"\u001a\u00020\u001d2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u001d0\u00102\u0006\u0010$\u001a\u00020\u001dH\u0002J\u0016\u0010%\u001a\u00020\u001d2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u001d0\u0010H\u0002J\b\u0010&\u001a\u00020\u000eH\u0002¨\u0006)"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationLatencyWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "parseLine", "Lcom/nick/myrecoverytracker/NotificationLatencyWorker$Entry;", "line", "", "smartSplit", "", "unquote", "s", "parseTs", "", "ts", "(Ljava/lang/String;)Ljava/lang/Long;", "writeRow", "", "out", "Ljava/io/File;", "day", "p50ms", "", "p90ms", "p99ms", "n", "", "percentile", "values", "p", "median", "today", "Entry", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class NotificationLatencyWorker extends CoroutineWorker {
    private static final String FEATURE_SCHEMA_VERSION = "v6.0";
    private static final String IN_FILE = "notification_log.csv";
    private static final String OUT_FILE = "daily_notification_latency.csv";
    private static final String TAG = "NotificationLatencyWorker";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final String TOK_POSTED = StringsKt.concatToString(new char[]{'p', 'o', 's', 't', 'e', 'd'});
    private static final String TOK_REMOVED = StringsKt.concatToString(new char[]{'r', 'e', 'm', 'o', 'v', 'e', 'd'});
    private static final String TOK_CLICK = StringsKt.concatToString(new char[]{'C', 'L', 'I', 'C', 'K'});
    private static final String TOK_CANCEL = StringsKt.concatToString(new char[]{'C', 'A', 'N', 'C', 'E', 'L'});
    private static final String TOK_CANCEL_ALL = StringsKt.concatToString(new char[]{'C', 'A', 'N', 'C', 'E', 'L', '_', 'A', 'L', 'L'});
    private static final String TOK_GROUP_SUMMARY_CANCELED = StringsKt.concatToString(new char[]{'G', 'R', 'O', 'U', 'P', '_', 'S', 'U', 'M', 'M', 'A', 'R', 'Y', '_', 'C', 'A', 'N', 'C', 'E', 'L', 'E', 'D'});

    /* compiled from: NotificationLatencyWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.NotificationLatencyWorker", f = "NotificationLatencyWorker.kt", i = {}, l = {23}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$1, reason: invalid class name */
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
            return NotificationLatencyWorker.this.doWork(this);
        }
    }

    public static final /* synthetic */ Entry access$parseLine(NotificationLatencyWorker $this, String line) {
        return $this.parseLine(line);
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public NotificationLatencyWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: NotificationLatencyWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$2", f = "NotificationLatencyWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return NotificationLatencyWorker.this.new AnonymousClass2(continuation);
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
                    Context ctx = NotificationLatencyWorker.this.getApplicationContext();
                    Intrinsics.checkNotNullExpressionValue(ctx, "getApplicationContext(...)");
                    File inFile = new File(ctx.getFilesDir(), NotificationLatencyWorker.IN_FILE);
                    File outFile = new File(ctx.getFilesDir(), NotificationLatencyWorker.OUT_FILE);
                    final String day = NotificationLatencyWorker.this.today();
                    if (!inFile.exists()) {
                        NotificationLatencyWorker.this.writeRow(outFile, day, 0.0d, 0.0d, 0.0d, 0);
                        Log.i(NotificationLatencyWorker.TAG, "No notification_log.csv; wrote empty latency row for " + day);
                        return ListenableWorker.Result.success();
                    }
                    try {
                        Sequence sequenceAsSequence = CollectionsKt.asSequence(FilesKt.readLines$default(inFile, null, 1, null));
                        final NotificationLatencyWorker notificationLatencyWorker = NotificationLatencyWorker.this;
                        List<Entry> entries = SequencesKt.toList(SequencesKt.filter(SequencesKt.mapNotNull(sequenceAsSequence, new Function1() { // from class: com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$2$$ExternalSyntheticLambda0
                            @Override // kotlin.jvm.functions.Function1
                            public final Object invoke(Object obj2) {
                                return NotificationLatencyWorker.access$parseLine(notificationLatencyWorker, (String) obj2);
                            }
                        }), new Function1() { // from class: com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$2$$ExternalSyntheticLambda1
                            @Override // kotlin.jvm.functions.Function1
                            public final Object invoke(Object obj2) {
                                return Boolean.valueOf(NotificationLatencyWorker.AnonymousClass2.invokeSuspend$lambda$1(day, (NotificationLatencyWorker.Entry) obj2));
                            }
                        }));
                        if (entries.isEmpty()) {
                            NotificationLatencyWorker.this.writeRow(outFile, day, 0.0d, 0.0d, 0.0d, 0);
                            Log.i(NotificationLatencyWorker.TAG, "No today entries; wrote empty latency row for " + day);
                            return ListenableWorker.Result.success();
                        }
                        HashMap postedStacks = new HashMap();
                        for (Entry e : entries) {
                            if (Intrinsics.areEqual(e.getEvent(), NotificationLatencyWorker.TOK_POSTED)) {
                                HashMap map = postedStacks;
                                String strInvokeSuspend$keyOf = invokeSuspend$keyOf(e);
                                Object arrayDeque = map.get(strInvokeSuspend$keyOf);
                                if (arrayDeque == null) {
                                    arrayDeque = new ArrayDeque();
                                    map.put(strInvokeSuspend$keyOf, arrayDeque);
                                }
                                ((ArrayDeque) arrayDeque).addLast(e);
                            }
                        }
                        Set engagedReasons = SetsKt.setOf((Object[]) new String[]{NotificationLatencyWorker.TOK_CLICK, NotificationLatencyWorker.TOK_CANCEL, NotificationLatencyWorker.TOK_CANCEL_ALL, NotificationLatencyWorker.TOK_GROUP_SUMMARY_CANCELED});
                        ArrayList latenciesSec = new ArrayList();
                        for (Entry e2 : entries) {
                            if (Intrinsics.areEqual(e2.getEvent(), NotificationLatencyWorker.TOK_REMOVED)) {
                                String reason = e2.getReason();
                                if (reason == null) {
                                    reason = "";
                                }
                                Locale US = Locale.US;
                                Intrinsics.checkNotNullExpressionValue(US, "US");
                                String upperCase = reason.toUpperCase(US);
                                Intrinsics.checkNotNullExpressionValue(upperCase, "toUpperCase(...)");
                                if (engagedReasons.contains(upperCase)) {
                                    ArrayDeque stack = (ArrayDeque) postedStacks.get(invokeSuspend$keyOf(e2));
                                    if (stack != null) {
                                        Entry candidate = null;
                                        while (true) {
                                            if (!stack.isEmpty()) {
                                                Entry last = (Entry) stack.removeLast();
                                                if (last.getEpochMs() <= e2.getEpochMs()) {
                                                    candidate = last;
                                                }
                                            }
                                        }
                                        if (candidate != null) {
                                            Set engagedReasons2 = engagedReasons;
                                            double epochMs = (e2.getEpochMs() - candidate.getEpochMs()) / 1000.0d;
                                            if (epochMs >= 0.0d) {
                                                latenciesSec.add(Boxing.boxDouble(epochMs));
                                            }
                                            engagedReasons = engagedReasons2;
                                        }
                                    }
                                }
                            }
                        }
                        if (latenciesSec.isEmpty()) {
                            NotificationLatencyWorker.this.writeRow(outFile, day, 0.0d, 0.0d, 0.0d, 0);
                            Log.i(NotificationLatencyWorker.TAG, "NotificationLatency(" + day + "): no paired engagements");
                        } else {
                            ArrayList arrayList = latenciesSec;
                            Collection arrayList2 = new ArrayList(CollectionsKt.collectionSizeOrDefault(arrayList, 10));
                            Iterator it = arrayList.iterator();
                            while (it.hasNext()) {
                                arrayList2.add(Boxing.boxDouble(((Number) it.next()).doubleValue() * 1000.0d));
                            }
                            List ms = (List) arrayList2;
                            double p50 = NotificationLatencyWorker.this.percentile(ms, 50.0d);
                            double p90 = NotificationLatencyWorker.this.percentile(ms, 90.0d);
                            double p99 = NotificationLatencyWorker.this.percentile(ms, 99.0d);
                            NotificationLatencyWorker.this.writeRow(outFile, day, p50, p90, p99, ms.size());
                            String str = String.format("%.0f", Arrays.copyOf(new Object[]{Boxing.boxDouble(p50)}, 1));
                            Intrinsics.checkNotNullExpressionValue(str, "format(...)");
                            String str2 = String.format("%.0f", Arrays.copyOf(new Object[]{Boxing.boxDouble(p90)}, 1));
                            Intrinsics.checkNotNullExpressionValue(str2, "format(...)");
                            String str3 = String.format("%.0f", Arrays.copyOf(new Object[]{Boxing.boxDouble(p99)}, 1));
                            Intrinsics.checkNotNullExpressionValue(str3, "format(...)");
                            Log.i(NotificationLatencyWorker.TAG, "NotificationLatency(" + day + "): p50=" + str + "ms p90=" + str2 + "ms p99=" + str3 + "ms n=" + ms.size());
                        }
                        return ListenableWorker.Result.success();
                    } catch (Throwable t) {
                        Log.e(NotificationLatencyWorker.TAG, "NotificationLatencyWorker failed", t);
                        return ListenableWorker.Result.retry();
                    }
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static final boolean invokeSuspend$lambda$1(String $day, Entry it) {
            return StringsKt.startsWith$default(it.getDate(), $day, false, 2, (Object) null);
        }

        private static final String invokeSuspend$keyOf$norm(String s) {
            String string = StringsKt.trim((CharSequence) s).toString();
            Locale US = Locale.US;
            Intrinsics.checkNotNullExpressionValue(US, "US");
            String lowerCase = string.toLowerCase(US);
            Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
            return lowerCase;
        }

        private static final String invokeSuspend$keyOf(Entry e) {
            return invokeSuspend$keyOf$norm(e.getPkg()) + "|" + invokeSuspend$keyOf$norm(e.getTitle()) + "|" + invokeSuspend$keyOf$norm(e.getText());
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.NotificationLatencyWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$1 r0 = (com.nick.myrecoverytracker.NotificationLatencyWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$1 r0 = new com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$1
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
            com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$2 r5 = new com.nick.myrecoverytracker.NotificationLatencyWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.NotificationLatencyWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Entry parseLine(String line) {
        String event;
        String reason;
        List<String> groupValues;
        try {
            List parts = smartSplit(line);
            if (parts == null || parts.size() < 5) {
                return null;
            }
            String tsStr = parts.get(0);
            String pkg = parts.get(1);
            String title = unquote(parts.get(2));
            String text = unquote(parts.get(3));
            String tail = StringsKt.trim((CharSequence) CollectionsKt.joinToString$default(CollectionsKt.drop(parts, 4), ",", null, null, 0, null, null, 62, null)).toString();
            Locale US = Locale.US;
            Intrinsics.checkNotNullExpressionValue(US, "US");
            String lowerTail = tail.toLowerCase(US);
            Intrinsics.checkNotNullExpressionValue(lowerTail, "toLowerCase(...)");
            if (!StringsKt.startsWith$default(lowerTail, "event=" + TOK_POSTED, false, 2, (Object) null) && !Intrinsics.areEqual(lowerTail, TOK_POSTED)) {
                if (StringsKt.startsWith$default(lowerTail, "event=" + TOK_REMOVED, false, 2, (Object) null)) {
                    String event2 = TOK_REMOVED;
                    Regex rx = new Regex("reason=([A-Za-z_]+)");
                    MatchResult matchResultFind$default = Regex.find$default(rx, tail, 0, 2, null);
                    String reason2 = (matchResultFind$default == null || (groupValues = matchResultFind$default.getGroupValues()) == null) ? null : (String) CollectionsKt.getOrNull(groupValues, 1);
                    event = event2;
                    reason = reason2;
                } else {
                    String event3 = TOK_REMOVED;
                    if (StringsKt.startsWith$default(lowerTail, event3, false, 2, (Object) null)) {
                        String event4 = TOK_REMOVED;
                        List after = StringsKt.split$default((CharSequence) tail, new String[]{","}, false, 2, 2, (Object) null);
                        if (after.size() >= 2) {
                            String reason3 = StringsKt.trim((CharSequence) after.get(1)).toString();
                            event = event4;
                            reason = reason3;
                        } else {
                            event = event4;
                            reason = null;
                        }
                    } else {
                        String event5 = TOK_POSTED;
                        if (StringsKt.startsWith$default(lowerTail, event5, false, 2, (Object) null)) {
                            event = TOK_POSTED;
                            reason = null;
                        } else {
                            return null;
                        }
                    }
                }
            } else {
                event = TOK_POSTED;
                reason = null;
            }
            Long ts = parseTs(tsStr);
            if (ts != null) {
                long epoch = ts.longValue();
                String strSubstring = tsStr.substring(0, 10);
                Intrinsics.checkNotNullExpressionValue(strSubstring, "substring(...)");
                return new Entry(strSubstring, epoch, pkg, title, text, event, reason);
            }
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    private final List<String> smartSplit(String line) {
        ArrayList out = new ArrayList();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        int length = line.length();
        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);
            switch (c) {
                case '\"':
                    boolean inQuotes2 = !inQuotes;
                    sb.append(c);
                    inQuotes = inQuotes2;
                    break;
                case ',':
                    if (inQuotes) {
                        sb.append(c);
                        break;
                    } else {
                        out.add(sb.toString());
                        sb.setLength(0);
                        Unit unit = Unit.INSTANCE;
                        break;
                    }
                default:
                    sb.append(c);
                    break;
            }
        }
        out.add(sb.toString());
        return out;
    }

    private final String unquote(String s) {
        if (s.length() < 2 || StringsKt.first(s) != '\"' || StringsKt.last(s) != '\"') {
            return s;
        }
        String strSubstring = s.substring(1, s.length() - 1);
        Intrinsics.checkNotNullExpressionValue(strSubstring, "substring(...)");
        return strSubstring;
    }

    private final Long parseTs(String ts) {
        try {
            Date date = SDF.parse(ts);
            if (date != null) {
                return Long.valueOf(date.getTime());
            }
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void writeRow(File out, final String day, double p50ms, double p90ms, double p99ms, int n) {
        String header = CollectionsKt.joinToString$default(CollectionsKt.listOf((Object[]) new String[]{"date", "feature_schema_version", "p50_ms", "p90_ms", "p99_ms", "count"}), ",", null, null, 0, null, null, 62, null);
        ArrayList lines = out.exists() ? CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(out, null, 1, null)) : new ArrayList();
        if (lines.isEmpty()) {
            lines.add(header);
        } else if (!Intrinsics.areEqual(CollectionsKt.first(lines), header)) {
            lines.clear();
            lines.add(header);
        }
        CollectionsKt.removeAll(lines, new Function1() { // from class: com.nick.myrecoverytracker.NotificationLatencyWorker$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return Boolean.valueOf(NotificationLatencyWorker.writeRow$lambda$0(day, (String) obj));
            }
        });
        String row = CollectionsKt.joinToString$default(CollectionsKt.listOf((Object[]) new String[]{day, FEATURE_SCHEMA_VERSION, String.valueOf((long) writeRow$r0(p50ms)), String.valueOf((long) writeRow$r0(p90ms)), String.valueOf((long) writeRow$r0(p99ms)), String.valueOf(n)}), ",", null, null, 0, null, null, 62, null);
        lines.add(row);
        FilesKt.writeText$default(out, CollectionsKt.joinToString$default(lines, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean writeRow$lambda$0(String $day, String it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return StringsKt.startsWith$default(it, $day + ",", false, 2, (Object) null);
    }

    private static final double writeRow$r0(double x) {
        return Math.floor(0.5d + x);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final double percentile(List<Double> values, double p) {
        if (values.isEmpty()) {
            return 0.0d;
        }
        List s = CollectionsKt.sorted(values);
        double clampedP = Math.min(100.0d, Math.max(0.0d, p));
        double rank = (clampedP / 100.0d) * (s.size() - 1);
        int lo = (int) Math.floor(rank);
        int hi = Math.min(s.size() - 1, lo + 1);
        double frac = rank - lo;
        return (((Number) s.get(lo)).doubleValue() * (1 - frac)) + (((Number) s.get(hi)).doubleValue() * frac);
    }

    private final double median(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0d;
        }
        List s = CollectionsKt.sorted(values);
        int n = s.size();
        return n % 2 == 1 ? ((Number) s.get(n / 2)).doubleValue() : (((Number) s.get((n / 2) - 1)).doubleValue() + ((Number) s.get(n / 2)).doubleValue()) / 2.0d;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String today() {
        String str = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    /* compiled from: NotificationLatencyWorker.kt */
    @Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0019\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BC\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\b\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\n\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u000b\u0010\fJ\t\u0010\u0016\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0017\u001a\u00020\u0005HÆ\u0003J\t\u0010\u0018\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0019\u001a\u00020\u0003HÆ\u0003J\t\u0010\u001a\u001a\u00020\u0003HÆ\u0003J\u000b\u0010\u001b\u001a\u0004\u0018\u00010\u0003HÆ\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\u0003HÆ\u0003JS\u0010\u001d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0003HÆ\u0001J\u0013\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010!\u001a\u00020\"HÖ\u0001J\t\u0010#\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0006\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000eR\u0011\u0010\b\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000eR\u0013\u0010\t\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000eR\u0013\u0010\n\u001a\u0004\u0018\u00010\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u000e¨\u0006$"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationLatencyWorker$Entry;", "", "date", "", "epochMs", "", "pkg", "title", "text", NotificationCompat.CATEGORY_EVENT, "reason", "<init>", "(Ljava/lang/String;JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getDate", "()Ljava/lang/String;", "getEpochMs", "()J", "getPkg", "getTitle", "getText", "getEvent", "getReason", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final /* data */ class Entry {
        private final String date;
        private final long epochMs;
        private final String event;
        private final String pkg;
        private final String reason;
        private final String text;
        private final String title;

        public static /* synthetic */ Entry copy$default(Entry entry, String str, long j, String str2, String str3, String str4, String str5, String str6, int i, Object obj) {
            if ((i & 1) != 0) {
                str = entry.date;
            }
            if ((i & 2) != 0) {
                j = entry.epochMs;
            }
            if ((i & 4) != 0) {
                str2 = entry.pkg;
            }
            if ((i & 8) != 0) {
                str3 = entry.title;
            }
            if ((i & 16) != 0) {
                str4 = entry.text;
            }
            if ((i & 32) != 0) {
                str5 = entry.event;
            }
            if ((i & 64) != 0) {
                str6 = entry.reason;
            }
            String str7 = str6;
            String str8 = str4;
            String str9 = str2;
            return entry.copy(str, j, str9, str3, str8, str5, str7);
        }

        /* renamed from: component1, reason: from getter */
        public final String getDate() {
            return this.date;
        }

        /* renamed from: component2, reason: from getter */
        public final long getEpochMs() {
            return this.epochMs;
        }

        /* renamed from: component3, reason: from getter */
        public final String getPkg() {
            return this.pkg;
        }

        /* renamed from: component4, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        /* renamed from: component5, reason: from getter */
        public final String getText() {
            return this.text;
        }

        /* renamed from: component6, reason: from getter */
        public final String getEvent() {
            return this.event;
        }

        /* renamed from: component7, reason: from getter */
        public final String getReason() {
            return this.reason;
        }

        public final Entry copy(String date, long epochMs, String pkg, String title, String text, String event, String reason) {
            Intrinsics.checkNotNullParameter(date, "date");
            Intrinsics.checkNotNullParameter(pkg, "pkg");
            Intrinsics.checkNotNullParameter(title, "title");
            Intrinsics.checkNotNullParameter(text, "text");
            return new Entry(date, epochMs, pkg, title, text, event, reason);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Entry)) {
                return false;
            }
            Entry entry = (Entry) other;
            return Intrinsics.areEqual(this.date, entry.date) && this.epochMs == entry.epochMs && Intrinsics.areEqual(this.pkg, entry.pkg) && Intrinsics.areEqual(this.title, entry.title) && Intrinsics.areEqual(this.text, entry.text) && Intrinsics.areEqual(this.event, entry.event) && Intrinsics.areEqual(this.reason, entry.reason);
        }

        public int hashCode() {
            return (((((((((((this.date.hashCode() * 31) + Long.hashCode(this.epochMs)) * 31) + this.pkg.hashCode()) * 31) + this.title.hashCode()) * 31) + this.text.hashCode()) * 31) + (this.event == null ? 0 : this.event.hashCode())) * 31) + (this.reason != null ? this.reason.hashCode() : 0);
        }

        public String toString() {
            return "Entry(date=" + this.date + ", epochMs=" + this.epochMs + ", pkg=" + this.pkg + ", title=" + this.title + ", text=" + this.text + ", event=" + this.event + ", reason=" + this.reason + ")";
        }

        public Entry(String date, long epochMs, String pkg, String title, String text, String event, String reason) {
            Intrinsics.checkNotNullParameter(date, "date");
            Intrinsics.checkNotNullParameter(pkg, "pkg");
            Intrinsics.checkNotNullParameter(title, "title");
            Intrinsics.checkNotNullParameter(text, "text");
            this.date = date;
            this.epochMs = epochMs;
            this.pkg = pkg;
            this.title = title;
            this.text = text;
            this.event = event;
            this.reason = reason;
        }

        public final String getDate() {
            return this.date;
        }

        public final long getEpochMs() {
            return this.epochMs;
        }

        public final String getPkg() {
            return this.pkg;
        }

        public final String getTitle() {
            return this.title;
        }

        public final String getText() {
            return this.text;
        }

        public final String getEvent() {
            return this.event;
        }

        public final String getReason() {
            return this.reason;
        }
    }
}
