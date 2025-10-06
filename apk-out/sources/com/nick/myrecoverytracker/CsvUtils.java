package com.nick.myrecoverytracker;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.SequencesKt;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;

/* compiled from: CsvUtils.kt */
@Metadata(d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\u000bJ\u000e\u0010\r\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u000bJ\u0014\u0010\u000f\u001a\u00020\u000b2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0011J\u0016\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u000bJ$\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00132\u0006\u0010\u0019\u001a\u00020\u000b2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0011J\u000e\u0010\u001b\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0013J\u0016\u0010\u001c\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00132\u0006\u0010\u001d\u001a\u00020\u001eJ\u0016\u0010\u001f\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00132\u0006\u0010\u001d\u001a\u00020\u001eJ\u000e\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\u000bJ\u0016\u0010#\u001a\u00020\u00172\u0006\u0010$\u001a\u00020\u00132\u0006\u0010%\u001a\u00020\u000bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u0018\u0010\u0006\u001a\n \b*\u0004\u0018\u00010\u00070\u0007X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\t¨\u0006&"}, d2 = {"Lcom/nick/myrecoverytracker/CsvUtils;", "", "<init>", "()V", "zone", "Ljava/time/ZoneId;", "fmtDate", "Ljava/time/format/DateTimeFormatter;", "kotlin.jvm.PlatformType", "Ljava/time/format/DateTimeFormatter;", "todayLocal", "", "todayUtc", "csvEscape", "s", "csvJoin", "cols", "", "ensureHeader", "Ljava/io/File;", "f", "header", "upsertByDate", "", "file", "dateStr", "tailCols", "healTodayUtcVsLocal", "rotateByDate", "keepDays", "", "rotateByTimestampPrefix", "looksLikeHeader", "", "firstLine", "writeAtomic", "dst", "content", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class CsvUtils {
    public static final CsvUtils INSTANCE = new CsvUtils();
    private static final DateTimeFormatter fmtDate;
    private static final ZoneId zone;

    private CsvUtils() {
    }

    static {
        ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
        Intrinsics.checkNotNullExpressionValue(zoneIdSystemDefault, "systemDefault(...)");
        zone = zoneIdSystemDefault;
        fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    }

    public final String todayLocal() {
        String str = LocalDate.now(zone).format(fmtDate);
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    public final String todayUtc() {
        String str = LocalDate.now(ZoneOffset.UTC).format(fmtDate);
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    public final String csvEscape(String s) {
        Intrinsics.checkNotNullParameter(s, "s");
        boolean needs = true;
        if (s.length() == 0) {
            return "";
        }
        String str = s;
        int i = 0;
        while (true) {
            if (i < str.length()) {
                char cCharAt = str.charAt(i);
                if (((cCharAt == ',' || cCharAt == '\"' || cCharAt == '\n' || cCharAt == '\r') ? (char) 1 : (char) 0) != 0) {
                    break;
                }
                i++;
            } else {
                needs = false;
                break;
            }
        }
        if (needs) {
            return "\"" + StringsKt.replace$default(s, "\"", "\"\"", false, 4, (Object) null) + "\"";
        }
        return s;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence csvJoin$lambda$1(String it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return INSTANCE.csvEscape(it);
    }

    public final String csvJoin(List<String> cols) {
        Intrinsics.checkNotNullParameter(cols, "cols");
        return CollectionsKt.joinToString$default(cols, ",", null, null, 0, null, new Function1() { // from class: com.nick.myrecoverytracker.CsvUtils$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return CsvUtils.csvJoin$lambda$1((String) obj);
            }
        }, 30, null);
    }

    public final File ensureHeader(File f, String header) {
        Intrinsics.checkNotNullParameter(f, "f");
        Intrinsics.checkNotNullParameter(header, "header");
        if (!f.exists() || f.length() == 0) {
            File parentFile = f.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            writeAtomic(f, header + "\n");
        }
        return f;
    }

    public final void upsertByDate(File file, String dateStr, List<String> tailCols) {
        Intrinsics.checkNotNullParameter(file, "file");
        Intrinsics.checkNotNullParameter(dateStr, "dateStr");
        Intrinsics.checkNotNullParameter(tailCols, "tailCols");
        ArrayList lines = file.exists() ? CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(file, null, 1, null)) : new ArrayList();
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
            int idx = StringsKt.indexOf$default((CharSequence) lines.get(i), ',', 0, false, 6, (Object) null);
            String key = (String) lines.get(i);
            if (idx >= 0) {
                key = key.substring(0, idx);
                Intrinsics.checkNotNullExpressionValue(key, "substring(...)");
            }
            if (!Intrinsics.areEqual(key, dateStr)) {
                i++;
            } else {
                lines.set(i, csvJoin(CollectionsKt.plus((Collection) CollectionsKt.listOf(dateStr), (Iterable) tailCols)));
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lines.add(csvJoin(CollectionsKt.plus((Collection) CollectionsKt.listOf(dateStr), (Iterable) tailCols)));
        }
        writeAtomic(file, CollectionsKt.joinToString$default(CollectionsKt.plus((Collection) CollectionsKt.listOf(header), (Iterable) CollectionsKt.drop(lines, 1)), "\n", null, null, 0, null, null, 62, null) + "\n");
    }

    public final void healTodayUtcVsLocal(File file) {
        String header;
        ArrayList finalKept;
        String key;
        Intrinsics.checkNotNullParameter(file, "file");
        if (file.exists()) {
            List<String> lines = CollectionsKt.toMutableList((Collection) FilesKt.readLines$default(file, null, 1, null));
            if (lines.isEmpty()) {
                return;
            }
            String header2 = (String) lines.remove(0);
            String local = todayLocal();
            String utc = todayUtc();
            ArrayList kept = new ArrayList(lines.size());
            HashSet seen = new HashSet();
            boolean hasLocalToday = false;
            for (String line : lines) {
                int idx = StringsKt.indexOf$default((CharSequence) line, ',', 0, false, 6, (Object) null);
                if (idx >= 0) {
                    key = line.substring(0, idx);
                    Intrinsics.checkNotNullExpressionValue(key, "substring(...)");
                } else {
                    key = line;
                }
                if (Intrinsics.areEqual(key, local)) {
                    hasLocalToday = true;
                }
                if (seen.add(key)) {
                    kept.add(line);
                }
            }
            if (!hasLocalToday || Intrinsics.areEqual(local, utc)) {
                header = header2;
                finalKept = kept;
            } else {
                Collection arrayList = new ArrayList();
                for (Object obj : kept) {
                    List lines2 = lines;
                    String header3 = header2;
                    String local2 = local;
                    if (!StringsKt.startsWith$default((String) obj, utc + ",", false, 2, (Object) null)) {
                        arrayList.add(obj);
                    }
                    local = local2;
                    lines = lines2;
                    header2 = header3;
                }
                header = header2;
                finalKept = (List) arrayList;
            }
            writeAtomic(file, SequencesKt.joinToString$default(SequencesKt.plus(SequencesKt.sequenceOf(header), CollectionsKt.asSequence(finalKept)), "\n", null, null, 0, null, null, 62, null) + "\n");
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:27:0x009e  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x00a1 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final void rotateByDate(java.io.File r24, int r25) {
        /*
            Method dump skipped, instructions count: 231
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.CsvUtils.rotateByDate(java.io.File, int):void");
    }

    /* JADX WARN: Removed duplicated region for block: B:35:0x00a5  */
    /* JADX WARN: Removed duplicated region for block: B:49:0x00a8 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final void rotateByTimestampPrefix(java.io.File r22, int r23) {
        /*
            Method dump skipped, instructions count: 251
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.CsvUtils.rotateByTimestampPrefix(java.io.File, int):void");
    }

    public final boolean looksLikeHeader(String firstLine) {
        Intrinsics.checkNotNullParameter(firstLine, "firstLine");
        Locale US = Locale.US;
        Intrinsics.checkNotNullExpressionValue(US, "US");
        String s = firstLine.toLowerCase(US);
        Intrinsics.checkNotNullExpressionValue(s, "toLowerCase(...)");
        return StringsKt.startsWith$default(s, "date,", false, 2, (Object) null) || StringsKt.startsWith$default(s, "timestamp,", false, 2, (Object) null);
    }

    public final void writeAtomic(File dst, String content) {
        Intrinsics.checkNotNullParameter(dst, "dst");
        Intrinsics.checkNotNullParameter(content, "content");
        File parentFile = dst.getParentFile();
        if (parentFile == null) {
            parentFile = dst.getParentFile();
        }
        File tmp = new File(parentFile, dst.getName() + ".tmp");
        FileChannel channel = new FileOutputStream(tmp).getChannel();
        try {
            FileChannel fileChannel = channel;
            fileChannel.truncate(0L);
            byte[] bytes = content.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            fileChannel.write(ByteBuffer.wrap(bytes));
            fileChannel.force(true);
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(channel, null);
            if (!tmp.renameTo(dst)) {
                dst.delete();
                tmp.renameTo(dst);
            }
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                CloseableKt.closeFinally(channel, th);
                throw th2;
            }
        }
    }
}
