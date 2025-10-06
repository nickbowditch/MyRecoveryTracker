package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kotlin.Metadata;
import kotlin.NoWhenBranchMatchedException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.enums.EnumEntries;
import kotlin.enums.EnumEntriesKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.ranges.RangesKt;
import kotlin.sequences.SequencesKt;
import kotlin.text.Charsets;
import kotlin.text.Regex;
import kotlin.text.StringsKt;

/* compiled from: NotificationRollupWorker.kt */
@Metadata(d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u00192\u00020\u0001:\u0003\u0017\u0018\u0019B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\b\u001a\u00020\tH\u0016J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0002J\u0018\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u000bH\u0002J\u0010\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u000bH\u0002J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0014\u001a\u00020\u000bH\u0002¨\u0006\u001a"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationRollupWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "readHeaderLock", "", "lock", "Ljava/io/File;", "ensureHeader", "", "out", "header", "isHeaderRow", "", "line", "normalizeEvent", "Lcom/nick/myrecoverytracker/NotificationRollupWorker$EventKind;", "Counts", "EventKind", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class NotificationRollupWorker extends Worker {
    private static final Regex DATE_RE = new Regex("^\\d{4}-\\d{2}-\\d{2}$");
    private static final String DEFAULT_HEADER = "date,feature_schema_version,delivered,opened,open_rate";
    private static final String FEATURE_SCHEMA_VERSION = "1";
    private static final String LOCK_FILE = "app/locks/daily_notif_engagement.head";
    private static final String LOG_FILE = "notification_log.csv";
    private static final String OUT_FILE = "daily_notification_engagement.csv";
    private static final String TAG = "NotificationRollupWorker";

    /* compiled from: NotificationRollupWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
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

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public NotificationRollupWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    @Override // androidx.work.Worker
    public ListenableWorker.Result doWork() {
        int i;
        Iterable iterable;
        int i2;
        File lockFile;
        File outFile;
        double opened;
        try {
            Context ctx = getApplicationContext();
            Intrinsics.checkNotNullExpressionValue(ctx, "getApplicationContext(...)");
            File files = ctx.getFilesDir();
            File inFile = new File(files, LOG_FILE);
            File outFile2 = new File(files, OUT_FILE);
            File lockFile2 = new File(ctx.getFilesDir().getParentFile(), LOCK_FILE);
            String headerLock = readHeaderLock(lockFile2);
            if (headerLock.length() == 0) {
                headerLock = DEFAULT_HEADER;
            }
            String header = headerLock;
            if (!inFile.exists()) {
                ensureHeader(outFile2, header);
                Log.i(TAG, "No notification_log.csv; wrote header only.");
                ListenableWorker.Result resultSuccess = ListenableWorker.Result.success();
                Intrinsics.checkNotNullExpressionValue(resultSuccess, "success(...)");
                return resultSuccess;
            }
            final Map agg = new LinkedHashMap();
            final Ref.IntRef lineNo = new Ref.IntRef();
            FilesKt.forEachLine$default(inFile, null, new Function1() { // from class: com.nick.myrecoverytracker.NotificationRollupWorker$$ExternalSyntheticLambda0
                @Override // kotlin.jvm.functions.Function1
                public final Object invoke(Object obj) {
                    return NotificationRollupWorker.doWork$lambda$3(lineNo, this, agg, (String) obj);
                }
            }, 1, null);
            List lines = new ArrayList();
            if (!outFile2.exists()) {
                i = 0;
            } else {
                Iterator it = FilesKt.readLines$default(outFile2, null, 1, null).iterator();
                while (it.hasNext()) {
                    lines.add(StringsKt.trimEnd((String) it.next(), '\r'));
                }
                i = 0;
            }
            if (lines.isEmpty() || !Intrinsics.areEqual(CollectionsKt.first(lines), header)) {
                lines.clear();
                lines.add(header);
            }
            Iterable iterableDrop = CollectionsKt.drop(lines, 1);
            Map linkedHashMap = new LinkedHashMap(RangesKt.coerceAtLeast(MapsKt.mapCapacity(CollectionsKt.collectionSizeOrDefault(iterableDrop, 10)), 16));
            for (Object obj : iterableDrop) {
                linkedHashMap.put(StringsKt.substringBefore$default((String) obj, ',', (String) null, 2, (Object) null), (String) obj);
                inFile = inFile;
                ctx = ctx;
                files = files;
            }
            Map byDate = MapsKt.toMutableMap(linkedHashMap);
            Iterable<String> iterableSorted = CollectionsKt.sorted(agg.keySet());
            int i3 = 0;
            for (String str : iterableSorted) {
                Counts counts = (Counts) agg.get(str);
                if (counts == null) {
                    iterable = iterableSorted;
                    i2 = i3;
                    lockFile = lockFile2;
                    int i4 = i;
                    counts = new Counts(i4, i4, 3, null);
                } else {
                    iterable = iterableSorted;
                    i2 = i3;
                    lockFile = lockFile2;
                }
                if (counts.getDelivered() > 0) {
                    outFile = outFile2;
                    opened = counts.getOpened() / counts.getDelivered();
                } else {
                    outFile = outFile2;
                    opened = 0.0d;
                }
                StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
                File outFile3 = outFile;
                String str2 = String.format(Locale.US, "%s,%s,%d,%d,%.6f", Arrays.copyOf(new Object[]{str, FEATURE_SCHEMA_VERSION, Integer.valueOf(counts.getDelivered()), Integer.valueOf(counts.getOpened()), Double.valueOf(opened)}, 5));
                Intrinsics.checkNotNullExpressionValue(str2, "format(...)");
                byDate.put(str, str2);
                iterableSorted = iterable;
                i3 = i2;
                lockFile2 = lockFile;
                agg = agg;
                outFile2 = outFile3;
                i = 0;
            }
            File outFile4 = outFile2;
            Map agg2 = agg;
            List listCreateListBuilder = CollectionsKt.createListBuilder();
            listCreateListBuilder.add(header);
            Iterator it2 = CollectionsKt.sorted(byDate.keySet()).iterator();
            while (it2.hasNext()) {
                Object obj2 = byDate.get((String) it2.next());
                Intrinsics.checkNotNull(obj2);
                listCreateListBuilder.add(obj2);
            }
            List rebuilt = CollectionsKt.build(listCreateListBuilder);
            FilesKt.writeText$default(outFile4, CollectionsKt.joinToString$default(rebuilt, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
            Log.i(TAG, "NotificationRollup → wrote " + agg2.size() + " date(s) to " + outFile4.getName());
            return ListenableWorker.Result.success();
        } catch (Throwable t) {
            Log.e(TAG, "NotificationRollup failed", t);
            return ListenableWorker.Result.failure();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit doWork$lambda$3(Ref.IntRef $lineNo, NotificationRollupWorker this$0, Map $agg, String raw) {
        int firstComma;
        Object counts;
        Object counts2;
        Intrinsics.checkNotNullParameter(raw, "raw");
        $lineNo.element++;
        String line = StringsKt.trim((CharSequence) raw).toString();
        int i = 0;
        if (line.length() == 0) {
            return Unit.INSTANCE;
        }
        if ($lineNo.element == 1 && this$0.isHeaderRow(line)) {
            return Unit.INSTANCE;
        }
        DefaultConstructorMarker defaultConstructorMarker = null;
        if (!StringsKt.startsWith$default(line, "timestamp,", false, 2, (Object) null) && !StringsKt.startsWith$default(line, "ts,", false, 2, (Object) null) && (firstComma = StringsKt.indexOf$default((CharSequence) line, ',', 0, false, 6, (Object) null)) > 0) {
            String tsField = line.substring(0, firstComma);
            Intrinsics.checkNotNullExpressionValue(tsField, "substring(...)");
            String date = StringsKt.take(tsField, 10);
            if (!DATE_RE.matches(date)) {
                return Unit.INSTANCE;
            }
            int i2 = 3;
            switch (WhenMappings.$EnumSwitchMapping$0[this$0.normalizeEvent(line).ordinal()]) {
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

    private final String readHeaderLock(File lock) {
        try {
            return lock.exists() ? StringsKt.replace$default(StringsKt.trim((CharSequence) FilesKt.readText$default(lock, null, 1, null)).toString(), "\r", "", false, 4, (Object) null) : "";
        } catch (Throwable th) {
            return "";
        }
    }

    private final void ensureHeader(File out, String header) {
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
            Iterable rest = CollectionsKt.drop(FilesKt.readLines$default(out, null, 1, null), 1);
            StringBuilder sb = new StringBuilder();
            sb.append(header).append('\n');
            Iterator it = rest.iterator();
            while (it.hasNext()) {
                sb.append(StringsKt.trimEnd((String) it.next(), '\r')).append('\n');
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

    private final boolean isHeaderRow(String line) {
        Locale US = Locale.US;
        Intrinsics.checkNotNullExpressionValue(US, "US");
        String l = line.toLowerCase(US);
        Intrinsics.checkNotNullExpressionValue(l, "toLowerCase(...)");
        return StringsKt.startsWith$default(l, "timestamp,", false, 2, (Object) null) || StringsKt.startsWith$default(l, "ts,", false, 2, (Object) null);
    }

    private final EventKind normalizeEvent(String line) {
        Locale US = Locale.US;
        Intrinsics.checkNotNullExpressionValue(US, "US");
        String lower = line.toLowerCase(US);
        Intrinsics.checkNotNullExpressionValue(lower, "toLowerCase(...)");
        String clicked = "clicked";
        if (StringsKt.contains$default((CharSequence) lower, (CharSequence) (",posted"), false, 2, (Object) null)) {
            return EventKind.DELIVERED;
        }
        if (StringsKt.contains$default((CharSequence) lower, (CharSequence) (",removed"), false, 2, (Object) null) && StringsKt.contains$default((CharSequence) lower, (CharSequence) (",click"), false, 2, (Object) null)) {
            return EventKind.OPENED;
        }
        if (StringsKt.contains$default((CharSequence) lower, (CharSequence) (",click"), false, 2, (Object) null) || StringsKt.contains$default((CharSequence) lower, (CharSequence) ("," + clicked), false, 2, (Object) null)) {
            return EventKind.OPENED;
        }
        return EventKind.OTHER;
    }

    /* compiled from: NotificationRollupWorker.kt */
    @Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u00002\u00020\u0001B\u001b\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\r\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000e\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0013\u001a\u00020\u0003HÖ\u0001J\t\u0010\u0014\u001a\u00020\u0015HÖ\u0001R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\b\"\u0004\b\f\u0010\n¨\u0006\u0016"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationRollupWorker$Counts;", "", "delivered", "", "opened", "<init>", "(II)V", "getDelivered", "()I", "setDelivered", "(I)V", "getOpened", "setOpened", "component1", "component2", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
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

    /* compiled from: NotificationRollupWorker.kt */
    @Metadata(d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0082\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006¨\u0006\u0007"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationRollupWorker$EventKind;", "", "<init>", "(Ljava/lang/String;I)V", "DELIVERED", "OPENED", "OTHER", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
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
