package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;

/* compiled from: UnlockMigrations.kt */
@Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0007\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\u0018\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0011H\u0002J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0011H\u0002J0\u0010\u0017\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u00052\u0006\u0010\u0019\u001a\u00020\u00052\u0006\u0010\u001a\u001a\u00020\u00052\u0006\u0010\u001b\u001a\u00020\u0015H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R \u0010\t\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u000b0\nX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001c"}, d2 = {"Lcom/nick/myrecoverytracker/UnlockMigrations;", "", "<init>", "()V", "TAG", "", "TS", "Ljava/text/SimpleDateFormat;", "MAPLOG", "LEGACY_FILES", "", "Lkotlin/Pair;", "run", "", "ctx", "Landroid/content/Context;", "chooseWinner", "Ljava/io/File;", "a", "b", "safeRowCount", "", "f", "writeMapping", "feature", "from", "to", "rows", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UnlockMigrations {
    private static final String MAPLOG = "migrations.log";
    private static final String TAG = "UnlockMigrations";
    public static final UnlockMigrations INSTANCE = new UnlockMigrations();
    private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final List<Pair<String, String>> LEGACY_FILES = CollectionsKt.listOf((Object[]) new Pair[]{TuplesKt.to("unlock_rollup.csv", "daily_unlocks.csv"), TuplesKt.to("daily_unlocks_count.csv", "daily_unlocks.csv"), TuplesKt.to("daily_unlocks_v0.csv", "daily_unlocks.csv")});

    private UnlockMigrations() {
    }

    public final void run(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        File filesDir = ctx.getFilesDir();
        if (filesDir == null) {
            return;
        }
        for (Pair<String, String> pair : LEGACY_FILES) {
            String legacy = pair.component1();
            String target = pair.component2();
            File from = new File(filesDir, legacy);
            File to = new File(filesDir, target);
            if (from.exists()) {
                File winner = chooseWinner(from, to);
                if (winner == from) {
                    if (to.exists()) {
                        to.delete();
                    }
                    boolean ok = from.renameTo(to);
                    if (!ok) {
                        Log.w(TAG, "rename failed: " + legacy + " → " + target);
                    }
                }
                Context ctx2 = ctx;
                writeMapping(ctx2, "unlocks", legacy, target, safeRowCount(to));
                try {
                    to.setReadable(true, true);
                    Boolean.valueOf(to.setWritable(true, true));
                    ctx = ctx2;
                } catch (Throwable th) {
                    Unit unit = Unit.INSTANCE;
                    ctx = ctx2;
                }
            }
        }
    }

    private final File chooseWinner(File a, File b) {
        if (!b.exists() || a.lastModified() > b.lastModified() || a.length() > b.length()) {
            return a;
        }
        return b;
    }

    private final int safeRowCount(File f) {
        try {
            Reader inputStreamReader = new InputStreamReader(new FileInputStream(f), Charsets.UTF_8);
            BufferedReader bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
            try {
                int i = 0;
                Iterator<String> it = TextStreamsKt.lineSequence(bufferedReader).iterator();
                while (it.hasNext()) {
                    if (!StringsKt.isBlank((String) it.next()) && (i = i + 1) < 0) {
                        CollectionsKt.throwCountOverflow();
                    }
                }
                CloseableKt.closeFinally(bufferedReader, null);
                return i;
            } finally {
            }
        } catch (Throwable th) {
            return 0;
        }
    }

    private final void writeMapping(Context ctx, String feature, String from, String to, int rows) {
        try {
            File f = new File(ctx.getFilesDir(), MAPLOG);
            if (!f.exists()) {
                FilesKt.writeText$default(f, "ts,feature,from,to,rows\n", null, 2, null);
            }
            String ts = TS.format(Long.valueOf(System.currentTimeMillis()));
            FilesKt.appendText$default(f, ts + "," + feature + "," + from + "," + to + "," + rows + "\n", null, 2, null);
        } catch (Throwable th) {
        }
    }
}
