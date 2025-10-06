package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.nick.myrecoverytracker.NotificationEngagementWorker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kotlin.Metadata;
import kotlin.NoWhenBranchMatchedException;
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
import kotlin.enums.EnumEntries;
import kotlin.enums.EnumEntriesKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.ranges.RangesKt;
import kotlin.sequences.SequencesKt;
import kotlin.text.Charsets;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import kotlin.text.Typography;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: NotificationEngagementWorker.kt */
@Metadata(d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0004\u0018\u0000 \u001c2\u00020\u0001:\u0003\u001a\u001b\u001cB\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\nJ\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0018\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\fH\u0002J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\fH\u0002J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0015\u001a\u00020\fH\u0002J\u0016\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\f0\u00192\u0006\u0010\u0015\u001a\u00020\fH\u0002¨\u0006\u001d"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationEngagementWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "readHeaderLock", "", "lock", "Ljava/io/File;", "ensureHeader", "", "out", "header", "isHeaderLike", "", "line", "classify", "Lcom/nick/myrecoverytracker/NotificationEngagementWorker$EventKind;", "readCols", "", "Counts", "EventKind", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class NotificationEngagementWorker extends CoroutineWorker {
    private static final Regex DATE_RE = new Regex("^\\d{4}-\\d{2}-\\d{2}$");
    private static final String DEFAULT_HEADER = "date,feature_schema_version,delivered,opened,open_rate";
    private static final String FEATURE_SCHEMA_VERSION = "1";
    private static final String IN_FILE = "notification_log.csv";
    private static final String LOCK_FILE = "app/locks/daily_notif_engagement.head";
    private static final String OUT_FILE = "daily_notification_engagement.csv";
    private static final String TAG = "NotificationEngagementWorker";

    /* compiled from: NotificationEngagementWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.NotificationEngagementWorker", f = "NotificationEngagementWorker.kt", i = {}, l = {18}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$1, reason: invalid class name */
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
            return NotificationEngagementWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public NotificationEngagementWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: NotificationEngagementWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$2", f = "NotificationEngagementWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        /* compiled from: NotificationEngagementWorker.kt */
        @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
        /* renamed from: com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$2$WhenMappings */
        public /* synthetic */ class WhenMappings {
            public static final /* synthetic */ int[] $EnumSwitchMapping$0;

            static {
                int[] iArr = new int[EventKind.values().length];
                try {
                    iArr[EventKind.DELIVERED.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[EventKind.OPENED.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[EventKind.OTHER.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                $EnumSwitchMapping$0 = iArr;
            }
        }

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return NotificationEngagementWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            int i;
            ArrayList lines;
            Iterator it;
            double opened;
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    Object $result = obj;
                    Context ctx = NotificationEngagementWorker.this.getApplicationContext();
                    Intrinsics.checkNotNullExpressionValue(ctx, "getApplicationContext(...)");
                    File files = ctx.getFilesDir();
                    File inFile = new File(files, NotificationEngagementWorker.IN_FILE);
                    File outFile = new File(files, NotificationEngagementWorker.OUT_FILE);
                    File lockFile = new File(ctx.getFilesDir().getParentFile(), NotificationEngagementWorker.LOCK_FILE);
                    String expectedHeader = NotificationEngagementWorker.this.readHeaderLock(lockFile);
                    String str = expectedHeader;
                    if (str.length() == 0) {
                        str = NotificationEngagementWorker.DEFAULT_HEADER;
                    }
                    String header = str;
                    if (!inFile.exists()) {
                        NotificationEngagementWorker.this.ensureHeader(outFile, header);
                        Log.i(NotificationEngagementWorker.TAG, "No notification_log.csv; wrote header only.");
                        return ListenableWorker.Result.success();
                    }
                    final Map agg = new LinkedHashMap();
                    try {
                        final NotificationEngagementWorker notificationEngagementWorker = NotificationEngagementWorker.this;
                        Function1 function1 = new Function1() { // from class: com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$2$$ExternalSyntheticLambda0
                            @Override // kotlin.jvm.functions.Function1
                            public final Object invoke(Object obj2) {
                                return NotificationEngagementWorker.AnonymousClass2.invokeSuspend$lambda$3(notificationEngagementWorker, agg, (String) obj2);
                            }
                        };
                        DefaultConstructorMarker defaultConstructorMarker = null;
                        FilesKt.forEachLine$default(inFile, null, function1, 1, null);
                        if (outFile.exists()) {
                            Iterable lines$default = FilesKt.readLines$default(outFile, null, 1, null);
                            Collection arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(lines$default, 10));
                            Iterator it2 = lines$default.iterator();
                            while (it2.hasNext()) {
                                arrayList.add(StringsKt.trimEnd((String) it2.next(), '\r'));
                            }
                            i = 0;
                            lines = CollectionsKt.toMutableList(arrayList);
                        } else {
                            i = 0;
                            lines = new ArrayList();
                        }
                        if (lines.isEmpty() || !Intrinsics.areEqual(CollectionsKt.first(lines), header)) {
                            lines.clear();
                            lines.add(header);
                        }
                        Iterable iterableDrop = CollectionsKt.drop(lines, 1);
                        Map linkedHashMap = new LinkedHashMap(RangesKt.coerceAtLeast(MapsKt.mapCapacity(CollectionsKt.collectionSizeOrDefault(iterableDrop, 10)), 16));
                        for (Object obj2 : iterableDrop) {
                            linkedHashMap.put(StringsKt.substringBefore$default((String) obj2, ',', (String) null, 2, (Object) null), (String) obj2);
                        }
                        Map byDate = MapsKt.toMutableMap(linkedHashMap);
                        Iterator it3 = CollectionsKt.sorted(agg.keySet()).iterator();
                        while (it3.hasNext()) {
                            String str2 = (String) it3.next();
                            Counts counts = (Counts) agg.get(str2);
                            if (counts == null) {
                                int i2 = i;
                                counts = new Counts(i2, i2, 3, defaultConstructorMarker);
                            }
                            if (counts.getDelivered() > 0) {
                                it = it3;
                                opened = counts.getOpened() / counts.getDelivered();
                            } else {
                                it = it3;
                                opened = 0.0d;
                            }
                            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
                            Map agg2 = agg;
                            String str3 = String.format(Locale.US, "%s,%s,%d,%d,%.6f", Arrays.copyOf(new Object[]{str2, NotificationEngagementWorker.FEATURE_SCHEMA_VERSION, Boxing.boxInt(counts.getDelivered()), Boxing.boxInt(counts.getOpened()), Boxing.boxDouble(opened)}, 5));
                            Intrinsics.checkNotNullExpressionValue(str3, "format(...)");
                            byDate.put(str2, str3);
                            it3 = it;
                            agg = agg2;
                            $result = $result;
                            i = 0;
                            defaultConstructorMarker = null;
                        }
                        Map agg3 = agg;
                        StringBuilder sb = new StringBuilder();
                        sb.append(header).append('\n');
                        Iterator it4 = CollectionsKt.sorted(byDate.keySet()).iterator();
                        while (it4.hasNext()) {
                            sb.append((String) byDate.get((String) it4.next())).append('\n');
                        }
                        String rebuilt = sb.toString();
                        Intrinsics.checkNotNullExpressionValue(rebuilt, "toString(...)");
                        FilesKt.writeText$default(outFile, rebuilt, null, 2, null);
                        Log.i(NotificationEngagementWorker.TAG, "Engagement rollup wrote " + agg3.size() + " date(s) to " + outFile.getName());
                        return ListenableWorker.Result.success();
                    } catch (Throwable t) {
                        Log.e(NotificationEngagementWorker.TAG, "Failed parsing notification_log.csv", t);
                        NotificationEngagementWorker.this.ensureHeader(outFile, header);
                        return ListenableWorker.Result.success();
                    }
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static final Unit invokeSuspend$lambda$3(NotificationEngagementWorker this$0, Map $agg, String raw) {
            Object counts;
            Object counts2;
            int i = 0;
            String line = StringsKt.trim((CharSequence) StringsKt.trimStart(raw, 65279)).toString();
            if (!(line.length() == 0) && !this$0.isHeaderLike(line)) {
                String date = StringsKt.substringBefore(line, ',', "");
                if (!NotificationEngagementWorker.DATE_RE.matches(date)) {
                    return Unit.INSTANCE;
                }
                DefaultConstructorMarker defaultConstructorMarker = null;
                int i2 = 3;
                switch (WhenMappings.$EnumSwitchMapping$0[this$0.classify(line).ordinal()]) {
                    case 1:
                        Object obj = $agg.get(date);
                        if (obj == null) {
                            counts = new Counts(i, i, i2, defaultConstructorMarker);
                            $agg.put(date, counts);
                        } else {
                            counts = obj;
                        }
                        Counts counts3 = (Counts) counts;
                        counts3.setDelivered(counts3.getDelivered() + 1);
                        break;
                    case 2:
                        Object obj2 = $agg.get(date);
                        if (obj2 == null) {
                            counts2 = new Counts(i, i, i2, defaultConstructorMarker);
                            $agg.put(date, counts2);
                        } else {
                            counts2 = obj2;
                        }
                        Counts counts4 = (Counts) counts2;
                        counts4.setOpened(counts4.getOpened() + 1);
                        break;
                    case 3:
                        break;
                    default:
                        throw new NoWhenBranchMatchedException();
                }
                return Unit.INSTANCE;
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.NotificationEngagementWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$1 r0 = (com.nick.myrecoverytracker.NotificationEngagementWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$1 r0 = new com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$1
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
            com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$2 r5 = new com.nick.myrecoverytracker.NotificationEngagementWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.NotificationEngagementWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String readHeaderLock(File lock) {
        try {
            return lock.exists() ? StringsKt.replace$default(StringsKt.trim((CharSequence) FilesKt.readText$default(lock, null, 1, null)).toString(), "\r", "", false, 4, (Object) null) : "";
        } catch (Throwable th) {
            return "";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void ensureHeader(File out, String header) {
        if (!out.exists() || out.length() == 0) {
            File parentFile = out.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FilesKt.writeText$default(out, header + "\n", null, 2, null);
            return;
        }
        Reader inputStreamReader = new InputStreamReader(new FileInputStream(out), Charsets.UTF_8);
        BufferedReader bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
        try {
            String str = (String) SequencesKt.firstOrNull(TextStreamsKt.lineSequence(bufferedReader));
            if (str == null) {
                str = "";
            }
            CloseableKt.closeFinally(bufferedReader, null);
            String cur = StringsKt.replace$default(StringsKt.trim((CharSequence) str).toString(), "\r", "", false, 4, (Object) null);
            if (Intrinsics.areEqual(cur, header)) {
                return;
            }
            String rest = CollectionsKt.joinToString$default(CollectionsKt.drop(FilesKt.readLines$default(out, null, 1, null), 1), "\n", null, null, 0, null, null, 62, null);
            StringBuilder sb = new StringBuilder();
            sb.append(header).append('\n');
            if (rest.length() > 0) {
                sb.append(rest).append('\n');
            }
            String string = sb.toString();
            Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
            FilesKt.writeText$default(out, string, null, 2, null);
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                CloseableKt.closeFinally(bufferedReader, th);
                throw th2;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean isHeaderLike(String line) {
        Locale US = Locale.US;
        Intrinsics.checkNotNullExpressionValue(US, "US");
        String l = line.toLowerCase(US);
        Intrinsics.checkNotNullExpressionValue(l, "toLowerCase(...)");
        return StringsKt.startsWith$default(l, "timestamp,", false, 2, (Object) null) || StringsKt.startsWith$default(l, "ts,", false, 2, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final EventKind classify(String line) {
        String reason;
        String string;
        List cols = readCols(line);
        if (cols.isEmpty()) {
            return EventKind.OTHER;
        }
        String eventEqPosted = "event=posted";
        String clicked = "clicked";
        if (cols.size() >= 5) {
            String ev = StringsKt.trim((CharSequence) cols.get(4)).toString();
            if (StringsKt.equals(ev, "posted", true) || StringsKt.equals(ev, eventEqPosted, true)) {
                return EventKind.DELIVERED;
            }
            String str = (String) CollectionsKt.getOrNull(cols, 5);
            if (str == null || (string = StringsKt.trim((CharSequence) str).toString()) == null) {
                reason = null;
            } else {
                Locale US = Locale.US;
                Intrinsics.checkNotNullExpressionValue(US, "US");
                reason = string.toUpperCase(US);
                Intrinsics.checkNotNullExpressionValue(reason, "toUpperCase(...)");
            }
            if (StringsKt.equals(ev, "removed", true) && Intrinsics.areEqual(reason, "CLICK")) {
                return EventKind.OPENED;
            }
        }
        if (cols.size() >= 2) {
            String ev2 = StringsKt.trim((CharSequence) cols.get(1)).toString();
            if (StringsKt.equals(ev2, "posted", true)) {
                return EventKind.DELIVERED;
            }
            if (StringsKt.equals(ev2, clicked, true) || StringsKt.equals(ev2, "click", true)) {
                return EventKind.OPENED;
            }
        }
        return EventKind.OTHER;
    }

    private final List<String> readCols(String line) {
        ArrayList out = new ArrayList(8);
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            switch (c) {
                case '\r':
                    break;
                case '\"':
                    if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                        sb.append(Typography.quote);
                        i++;
                        break;
                    } else {
                        inQuotes = inQuotes ? false : true;
                        break;
                    }
                    break;
                case ',':
                    if (inQuotes) {
                        sb.append(c);
                        break;
                    } else {
                        out.add(sb.toString());
                        sb.setLength(0);
                        break;
                    }
                default:
                    sb.append(c);
                    break;
            }
            i++;
        }
        out.add(sb.toString());
        return out;
    }

    /* compiled from: NotificationEngagementWorker.kt */
    @Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u00002\u00020\u0001B\u001b\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\r\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000e\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0013\u001a\u00020\u0003HÖ\u0001J\t\u0010\u0014\u001a\u00020\u0015HÖ\u0001R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\b\"\u0004\b\f\u0010\n¨\u0006\u0016"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationEngagementWorker$Counts;", "", "delivered", "", "opened", "<init>", "(II)V", "getDelivered", "()I", "setDelivered", "(I)V", "getOpened", "setOpened", "component1", "component2", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    private static final /* data */ class Counts {
        private int delivered;
        private int opened;

        /* JADX WARN: Illegal instructions before constructor call */
        public Counts() {
            int i = 0;
            this(i, i, 3, null);
        }

        public static /* synthetic */ Counts copy$default(Counts counts, int i, int i2, int i3, Object obj) {
            if ((i3 & 1) != 0) {
                i = counts.delivered;
            }
            if ((i3 & 2) != 0) {
                i2 = counts.opened;
            }
            return counts.copy(i, i2);
        }

        /* renamed from: component1, reason: from getter */
        public final int getDelivered() {
            return this.delivered;
        }

        /* renamed from: component2, reason: from getter */
        public final int getOpened() {
            return this.opened;
        }

        public final Counts copy(int delivered, int opened) {
            return new Counts(delivered, opened);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Counts)) {
                return false;
            }
            Counts counts = (Counts) other;
            return this.delivered == counts.delivered && this.opened == counts.opened;
        }

        public int hashCode() {
            return (Integer.hashCode(this.delivered) * 31) + Integer.hashCode(this.opened);
        }

        public String toString() {
            return "Counts(delivered=" + this.delivered + ", opened=" + this.opened + ")";
        }

        public Counts(int delivered, int opened) {
            this.delivered = delivered;
            this.opened = opened;
        }

        public /* synthetic */ Counts(int i, int i2, int i3, DefaultConstructorMarker defaultConstructorMarker) {
            this((i3 & 1) != 0 ? 0 : i, (i3 & 2) != 0 ? 0 : i2);
        }

        public final int getDelivered() {
            return this.delivered;
        }

        public final int getOpened() {
            return this.opened;
        }

        public final void setDelivered(int i) {
            this.delivered = i;
        }

        public final void setOpened(int i) {
            this.opened = i;
        }
    }

    /* compiled from: NotificationEngagementWorker.kt */
    @Metadata(d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0082\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006¨\u0006\u0007"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationEngagementWorker$EventKind;", "", "<init>", "(Ljava/lang/String;I)V", "DELIVERED", "OPENED", "OTHER", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    private enum EventKind {
        DELIVERED,
        OPENED,
        OTHER;

        private static final /* synthetic */ EnumEntries $ENTRIES = EnumEntriesKt.enumEntries($VALUES);

        public static EnumEntries<EventKind> getEntries() {
            return $ENTRIES;
        }
    }
}
