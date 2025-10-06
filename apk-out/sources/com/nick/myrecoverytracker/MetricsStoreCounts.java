package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/* compiled from: MetricsStoreCounts.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003JV\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u0014"}, d2 = {"Lcom/nick/myrecoverytracker/MetricsStoreCounts;", "", "<init>", "()V", "FILE_NAME", "", "writeDailyCounts", "", "ctx", "Landroid/content/Context;", "day", "smsOutWhitelist", "", "smsInWhitelist", "smsOutTotal", "smsInTotal", "callOutWhitelist", "callInWhitelist", "callOutTotal", "callInTotal", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class MetricsStoreCounts {
    private static final String FILE_NAME = "support_counts.csv";
    public static final MetricsStoreCounts INSTANCE = new MetricsStoreCounts();

    private MetricsStoreCounts() {
    }

    public final void writeDailyCounts(Context ctx, String day, int smsOutWhitelist, int smsInWhitelist, int smsOutTotal, int smsInTotal, int callOutWhitelist, int callInWhitelist, int callOutTotal, int callInTotal) {
        Iterable lines;
        String day2 = day;
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(day2, "day");
        File file = new File(ctx.getFilesDir(), FILE_NAME);
        String str = ",";
        String newLine = CollectionsKt.joinToString$default(CollectionsKt.listOf(day2, Integer.valueOf(smsOutWhitelist), Integer.valueOf(smsInWhitelist), Integer.valueOf(smsOutTotal), Integer.valueOf(smsInTotal), Integer.valueOf(callOutWhitelist), Integer.valueOf(callInWhitelist), Integer.valueOf(callOutTotal), Integer.valueOf(callInTotal)), ",", null, null, 0, null, null, 62, null);
        if (file.exists()) {
            Iterable lines$default = FilesKt.readLines$default(file, null, 1, null);
            Collection arrayList = new ArrayList();
            for (Object obj : lines$default) {
                String str2 = str;
                if (!StringsKt.startsWith$default((String) obj, day2 + str, false, 2, (Object) null)) {
                    arrayList.add(obj);
                }
                day2 = day;
                str = str2;
            }
            lines = CollectionsKt.toMutableList(arrayList);
        } else {
            lines = (List) new ArrayList();
        }
        ((Collection) lines).add(newLine);
        FileWriter fileWriter = new FileWriter(file, false);
        try {
            FileWriter fileWriter2 = fileWriter;
            Iterator it = lines.iterator();
            while (it.hasNext()) {
                Appendable appendableAppend = fileWriter2.append((CharSequence) it.next());
                Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
            }
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(fileWriter, null);
        } finally {
        }
    }
}
