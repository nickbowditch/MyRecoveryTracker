package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.text.StringsKt;

/* compiled from: SleepValidationWorker.kt */
@Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u00102\u00020\u0001:\u0001\u0010B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\u000e\u001a\u00020\u000fH\u0016R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0018\u0010\n\u001a\n \f*\u0004\u0018\u00010\u000b0\u000bX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\r¨\u0006\u0011"}, d2 = {"Lcom/nick/myrecoverytracker/SleepValidationWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "zone", "Ljava/time/ZoneId;", "fmtDate", "Ljava/time/format/DateTimeFormatter;", "kotlin.jvm.PlatformType", "Ljava/time/format/DateTimeFormatter;", "doWork", "Landroidx/work/ListenableWorker$Result;", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class SleepValidationWorker extends Worker {
    private static final String TAG = "SleepValidationWorker";
    private final DateTimeFormatter fmtDate;
    private final ZoneId zone;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SleepValidationWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
        Intrinsics.checkNotNullExpressionValue(zoneIdSystemDefault, "systemDefault(...)");
        this.zone = zoneIdSystemDefault;
        this.fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    }

    /* JADX WARN: Removed duplicated region for block: B:100:0x02c6  */
    /* JADX WARN: Removed duplicated region for block: B:103:0x02d0  */
    /* JADX WARN: Removed duplicated region for block: B:104:0x02d3  */
    /* JADX WARN: Removed duplicated region for block: B:95:0x02b6  */
    /* JADX WARN: Removed duplicated region for block: B:96:0x02b9  */
    /* JADX WARN: Removed duplicated region for block: B:99:0x02c3  */
    @Override // androidx.work.Worker
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public androidx.work.ListenableWorker.Result doWork() throws org.json.JSONException {
        /*
            Method dump skipped, instructions count: 816
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.SleepValidationWorker.doWork():androidx.work.ListenableWorker$Result");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit doWork$lambda$0(Ref.BooleanRef $durPass, List $reasons, String line) {
        Double hrs;
        Intrinsics.checkNotNullParameter(line, "line");
        if (!StringsKt.startsWith$default(line, "date", false, 2, (Object) null) && !StringsKt.isBlank(line)) {
            List parts = StringsKt.split$default((CharSequence) line, new String[]{","}, false, 0, 6, (Object) null);
            if (parts.size() >= 2 && ((hrs = StringsKt.toDoubleOrNull((String) parts.get(1))) == null || hrs.doubleValue() < 0.0d || hrs.doubleValue() > 24.0d)) {
                $durPass.element = false;
                $reasons.add("duration out of range: " + line);
            }
        }
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit doWork$lambda$1(Ref.BooleanRef $qualPass, List $reasons, String line) {
        Intrinsics.checkNotNullParameter(line, "line");
        if (!StringsKt.startsWith$default(line, "date", false, 2, (Object) null) && !StringsKt.isBlank(line)) {
            List parts = StringsKt.split$default((CharSequence) line, new String[]{","}, false, 0, 6, (Object) null);
            if (parts.size() >= 2) {
                String q = (String) parts.get(1);
                if (StringsKt.isBlank(q)) {
                    $qualPass.element = false;
                    $reasons.add("empty quality: " + line);
                }
            }
        }
        return Unit.INSTANCE;
    }
}
