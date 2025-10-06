package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.ByteStreamsKt;
import kotlin.io.CloseableKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;

/* compiled from: Csv.kt */
@Metadata(d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u000f\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J6\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0013\u001a\u00020\u000b2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u0005J0\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\u000b2\b\b\u0002\u0010\u0016\u001a\u00020\u0005J\u0018\u0010\u0019\u001a\u00020\u000f2\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0013\u001a\u00020\u000bH\u0002J\u0018\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001d\u001a\u00020\u000bH\u0002J\u0018\u0010\u001e\u001a\u00020\u000f2\u0006\u0010\u001f\u001a\u00020\u001b2\u0006\u0010 \u001a\u00020\u000bH\u0002J\u0018\u0010!\u001a\u00020\u000f2\u0006\u0010\"\u001a\u00020\u001b2\u0006\u0010\u001f\u001a\u00020\u001bH\u0002J\u0016\u0010#\u001a\u00020\u000b2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015H\u0002J\u0012\u0010$\u001a\u00020\u000b2\b\u0010%\u001a\u0004\u0018\u00010\u000bH\u0002J(\u0010&\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010'\u001a\u00020\u0005H\u0002J\u0017\u0010(\u001a\u0004\u0018\u00010\f2\u0006\u0010%\u001a\u00020\u000bH\u0002¢\u0006\u0002\u0010)R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\f0\nX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082T¢\u0006\u0002\n\u0000¨\u0006*"}, d2 = {"Lcom/nick/myrecoverytracker/Csv;", "", "<init>", "()V", "RETENTION_DAYS", "", "tsSecondFmt", "Ljava/text/SimpleDateFormat;", "tsMinuteFmt", "lastPruneAtMs", "Ljava/util/concurrent/ConcurrentHashMap;", "", "", "PRUNE_MIN_INTERVAL_MS", "append", "", "ctx", "Landroid/content/Context;", "name", "header", "cells", "", "timestampColumnIndex", "appendRawLine", "rawLine", "ensureHeader", "file", "Ljava/io/File;", "atomicAppendLine", "line", "atomicWrite", "dst", "content", "replaceFile", "tmp", "toCsvLine", "escape", "s", "maybePrune", "tsCol", "parseTs", "(Ljava/lang/String;)Ljava/lang/Long;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class Csv {
    private static final long PRUNE_MIN_INTERVAL_MS = 60000;
    private static final int RETENTION_DAYS = 30;
    public static final Csv INSTANCE = new Csv();
    private static final SimpleDateFormat tsSecondFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat tsMinuteFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    private static final ConcurrentHashMap<String, Long> lastPruneAtMs = new ConcurrentHashMap<>();

    private Csv() {
    }

    public static /* synthetic */ void append$default(Csv csv, Context context, String str, String str2, List list, int i, int i2, Object obj) {
        int i3;
        if ((i2 & 16) == 0) {
            i3 = i;
        } else {
            i3 = 0;
        }
        csv.append(context, str, str2, list, i3);
    }

    public final void append(Context ctx, String name, String header, List<String> cells, int timestampColumnIndex) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(name, "name");
        Intrinsics.checkNotNullParameter(header, "header");
        Intrinsics.checkNotNullParameter(cells, "cells");
        File file = new File(ctx.getFilesDir(), name);
        ensureHeader(file, header);
        atomicAppendLine(file, toCsvLine(cells));
        maybePrune(ctx, file, header, timestampColumnIndex);
    }

    public static /* synthetic */ void appendRawLine$default(Csv csv, Context context, String str, String str2, String str3, int i, int i2, Object obj) {
        int i3;
        if ((i2 & 16) == 0) {
            i3 = i;
        } else {
            i3 = 0;
        }
        csv.appendRawLine(context, str, str2, str3, i3);
    }

    public final void appendRawLine(Context ctx, String name, String header, String rawLine, int timestampColumnIndex) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(name, "name");
        Intrinsics.checkNotNullParameter(header, "header");
        Intrinsics.checkNotNullParameter(rawLine, "rawLine");
        File file = new File(ctx.getFilesDir(), name);
        ensureHeader(file, header);
        atomicAppendLine(file, rawLine);
        maybePrune(ctx, file, header, timestampColumnIndex);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private final void ensureHeader(File file, String header) {
        if (!file.exists()) {
            atomicWrite(file, header + "\n");
            return;
        }
        if (file.length() == 0) {
            atomicWrite(file, header + "\n");
            return;
        }
        Reader inputStreamReader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
        FileInputStream bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
        try {
            String line = bufferedReader.readLine();
            if (line == null) {
                line = "";
            }
            if (!Intrinsics.areEqual(line, header)) {
                File file2 = new File(file.getParentFile(), file.getName() + ".tmp.reheader");
                OutputStream fileOutputStream = new FileOutputStream(file2);
                bufferedReader = fileOutputStream instanceof BufferedOutputStream ? (BufferedOutputStream) fileOutputStream : new BufferedOutputStream(fileOutputStream, 8192);
                try {
                    BufferedOutputStream bufferedOutputStream = bufferedReader;
                    byte[] bytes = (header + "\n").getBytes(Charsets.UTF_8);
                    Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
                    bufferedOutputStream.write(bytes);
                    bufferedReader = new FileInputStream(file);
                    try {
                        ByteStreamsKt.copyTo$default(bufferedReader, bufferedOutputStream, 0, 2, null);
                        CloseableKt.closeFinally(bufferedReader, null);
                        bufferedOutputStream.flush();
                        ((FileOutputStream) bufferedOutputStream).getFD().sync();
                        Unit unit = Unit.INSTANCE;
                        CloseableKt.closeFinally(bufferedReader, null);
                        INSTANCE.replaceFile(file2, file);
                    } finally {
                    }
                } finally {
                }
            }
            Unit unit2 = Unit.INSTANCE;
            CloseableKt.closeFinally(bufferedReader, null);
        } catch (Throwable th) {
            try {
                throw th;
            } finally {
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private final void atomicAppendLine(File file, String line) {
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp.append");
        BufferedOutputStream fileInputStream = new FileInputStream(file);
        try {
            FileInputStream fileInputStream2 = fileInputStream;
            OutputStream fileOutputStream = new FileOutputStream(tmp);
            fileInputStream = fileOutputStream instanceof BufferedOutputStream ? (BufferedOutputStream) fileOutputStream : new BufferedOutputStream(fileOutputStream, 8192);
            try {
                BufferedOutputStream bufferedOutputStream = fileInputStream;
                ByteStreamsKt.copyTo$default(fileInputStream2, bufferedOutputStream, 0, 2, null);
                byte[] bytes = (StringsKt.endsWith$default(line, "\n", false, 2, (Object) null) ? line : line + "\n").getBytes(Charsets.UTF_8);
                Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
                bufferedOutputStream.write(bytes);
                bufferedOutputStream.flush();
                ((FileOutputStream) bufferedOutputStream).getFD().sync();
                Unit unit = Unit.INSTANCE;
                CloseableKt.closeFinally(fileInputStream, null);
                Unit unit2 = Unit.INSTANCE;
                CloseableKt.closeFinally(fileInputStream, null);
                replaceFile(tmp, file);
            } finally {
            }
        } finally {
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private final void atomicWrite(File dst, String content) {
        File tmp = new File(dst.getParentFile(), dst.getName() + ".tmp.write");
        OutputStream fileOutputStream = new FileOutputStream(tmp);
        BufferedOutputStream bufferedOutputStream = fileOutputStream instanceof BufferedOutputStream ? (BufferedOutputStream) fileOutputStream : new BufferedOutputStream(fileOutputStream, 8192);
        try {
            BufferedOutputStream bufferedOutputStream2 = bufferedOutputStream;
            byte[] bytes = content.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            bufferedOutputStream2.write(bytes);
            bufferedOutputStream2.flush();
            ((FileOutputStream) bufferedOutputStream2).getFD().sync();
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(bufferedOutputStream, null);
            replaceFile(tmp, dst);
        } finally {
        }
    }

    private final void replaceFile(File tmp, File dst) {
        if (dst.exists()) {
            File bak = new File(dst.getParentFile(), dst.getName() + ".bak");
            if (bak.exists()) {
                bak.delete();
            }
            if (!dst.renameTo(bak)) {
                bak.delete();
                dst.renameTo(bak);
            }
            tmp.renameTo(dst);
            bak.delete();
            return;
        }
        tmp.renameTo(dst);
    }

    private final String toCsvLine(List<String> cells) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object obj : cells) {
            int i2 = i + 1;
            if (i < 0) {
                CollectionsKt.throwIndexOverflow();
            }
            String str = (String) obj;
            if (i > 0) {
                sb.append(',');
            }
            sb.append(INSTANCE.escape(str));
            i = i2;
        }
        sb.append('\n');
        String string = sb.toString();
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        return string;
    }

    private final String escape(String s) {
        String v = s == null ? "" : s;
        String str = v;
        CharSequence charSequence = null;
        int i = 0;
        while (true) {
            if (i >= str.length()) {
                break;
            }
            char cCharAt = str.charAt(i);
            if (((cCharAt == '\"' || cCharAt == ',' || cCharAt == '\n' || cCharAt == '\r') ? (char) 1 : (char) 0) != 0) {
                charSequence = 1;
                break;
            }
            i++;
        }
        if (charSequence == null) {
            return v;
        }
        return "\"" + StringsKt.replace$default(v, "\"", "\"\"", false, 4, (Object) null) + "\"";
    }

    private final void maybePrune(Context ctx, File file, String header, int tsCol) {
        Throwable th;
        Throwable th2;
        String str;
        Sequence sequence;
        int i = tsCol;
        long now = System.currentTimeMillis();
        Long l = lastPruneAtMs.get(file.getPath());
        long last = l != null ? l.longValue() : 0L;
        if (now - last < PRUNE_MIN_INTERVAL_MS) {
            return;
        }
        lastPruneAtMs.put(file.getPath(), Long.valueOf(now));
        long cutoff = now - 2592000000L;
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp.prune");
        boolean wrote = false;
        Reader inputStreamReader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
        Reader bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
        BufferedReader bufferedReader2 = bufferedReader instanceof BufferedReader ? (BufferedReader) bufferedReader : new BufferedReader(bufferedReader, 8192);
        try {
            Sequence sequenceLineSequence = TextStreamsKt.lineSequence(bufferedReader2);
            try {
                Writer outputStreamWriter = new OutputStreamWriter(new FileOutputStream(tmp), Charsets.UTF_8);
                BufferedWriter bufferedWriter = outputStreamWriter instanceof BufferedWriter ? (BufferedWriter) outputStreamWriter : new BufferedWriter(outputStreamWriter, 8192);
                try {
                    BufferedWriter bufferedWriter2 = bufferedWriter;
                    bufferedWriter2.write(header);
                    bufferedWriter2.newLine();
                    int i2 = 1;
                    Sequence sequence2 = sequenceLineSequence;
                    try {
                        for (String str2 : SequencesKt.drop(sequence2, 1)) {
                            try {
                                str = str2;
                                sequence = sequence2;
                            } catch (Throwable th3) {
                                th = th3;
                                th2 = th;
                                try {
                                    throw th2;
                                } catch (Throwable th4) {
                                    try {
                                        CloseableKt.closeFinally(bufferedWriter, th2);
                                        throw th4;
                                    } catch (Throwable th5) {
                                        th = th5;
                                        th = th;
                                        try {
                                            throw th;
                                        } catch (Throwable th6) {
                                            CloseableKt.closeFinally(bufferedReader2, th);
                                            throw th6;
                                        }
                                    }
                                }
                            }
                            try {
                                String[] strArr = new String[i2];
                                strArr[0] = ",";
                                List listSplit$default = StringsKt.split$default((CharSequence) str, strArr, false, 0, 6, (Object) null);
                                if (listSplit$default.size() > i) {
                                    Long ts = INSTANCE.parseTs((String) listSplit$default.get(i));
                                    if (ts != null && ts.longValue() >= cutoff) {
                                        bufferedWriter2.write(str2);
                                        bufferedWriter2.newLine();
                                        wrote = true;
                                    }
                                }
                                i = tsCol;
                                sequence2 = sequence;
                                i2 = 1;
                            } catch (Throwable th7) {
                                th2 = th7;
                                throw th2;
                            }
                        }
                        bufferedWriter2.flush();
                        bufferedWriter2.flush();
                        Unit unit = Unit.INSTANCE;
                        CloseableKt.closeFinally(bufferedWriter, null);
                        Unit unit2 = Unit.INSTANCE;
                        CloseableKt.closeFinally(bufferedReader2, null);
                        if (wrote) {
                            replaceFile(tmp, file);
                        } else {
                            tmp.delete();
                        }
                    } catch (Throwable th8) {
                        th = th8;
                    }
                } catch (Throwable th9) {
                    th2 = th9;
                }
            } catch (Throwable th10) {
                th = th10;
            }
        } catch (Throwable th11) {
            th = th11;
        }
    }

    private final Long parseTs(String s) {
        ParsePosition pos = new ParsePosition(0);
        Date d1 = tsSecondFmt.parse(s, pos);
        if (d1 != null && pos.getIndex() == s.length()) {
            return Long.valueOf(d1.getTime());
        }
        ParsePosition pos2 = new ParsePosition(0);
        Date d2 = tsMinuteFmt.parse(s, pos2);
        if (d2 == null || pos2.getIndex() != s.length()) {
            return null;
        }
        return Long.valueOf(d2.getTime());
    }
}
