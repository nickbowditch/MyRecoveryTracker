package com.nick.myrecoverytracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ForegroundWork.kt */
@Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J,\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u00052\b\b\u0002\u0010\r\u001a\u00020\u00052\b\b\u0002\u0010\u000e\u001a\u00020\u0007J,\u0010\u000f\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u00052\b\b\u0002\u0010\r\u001a\u00020\u00052\b\b\u0002\u0010\u000e\u001a\u00020\u0007J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\n\u001a\u00020\u000bH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u0012"}, d2 = {"Lcom/nick/myrecoverytracker/ForegroundWork;", "", "<init>", "()V", "CH_ID", "", "DEFAULT_ID", "", "info", "Landroidx/work/ForegroundInfo;", "ctx", "Landroid/content/Context;", "title", "text", "id", "createForegroundInfo", "ensureChannel", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class ForegroundWork {
    private static final String CH_ID = "mrt_wm_channel";
    private static final int DEFAULT_ID = 2002;
    public static final ForegroundWork INSTANCE = new ForegroundWork();

    private ForegroundWork() {
    }

    public static /* synthetic */ ForegroundInfo info$default(ForegroundWork foregroundWork, Context context, String str, String str2, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            str = "MyRecoveryAssistant";
        }
        if ((i2 & 4) != 0) {
            str2 = "Working…";
        }
        if ((i2 & 8) != 0) {
            i = DEFAULT_ID;
        }
        return foregroundWork.info(context, str, str2, i);
    }

    public final ForegroundInfo info(Context ctx, String title, String text, int id) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(title, "title");
        Intrinsics.checkNotNullParameter(text, "text");
        ensureChannel(ctx);
        Notification notif = new NotificationCompat.Builder(ctx, CH_ID).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(text).setOngoing(true).setOnlyAlertOnce(true).setForegroundServiceBehavior(1).build();
        int type = Build.VERSION.SDK_INT < 29 ? 0 : 1;
        if (Build.VERSION.SDK_INT >= 31) {
            return new ForegroundInfo(id, notif, type);
        }
        return new ForegroundInfo(id, notif);
    }

    public static /* synthetic */ ForegroundInfo createForegroundInfo$default(ForegroundWork foregroundWork, Context context, String str, String str2, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            str = "MyRecoveryAssistant";
        }
        if ((i2 & 4) != 0) {
            str2 = "Working…";
        }
        if ((i2 & 8) != 0) {
            i = DEFAULT_ID;
        }
        return foregroundWork.createForegroundInfo(context, str, str2, i);
    }

    public final ForegroundInfo createForegroundInfo(Context ctx, String title, String text, int id) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Intrinsics.checkNotNullParameter(title, "title");
        Intrinsics.checkNotNullParameter(text, "text");
        return info(ctx, title, text, id);
    }

    private final void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager nm = (NotificationManager) ctx.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CH_ID) == null) {
            nm.createNotificationChannel(new NotificationChannel(CH_ID, "MyRecovery Worker", 2));
        }
    }
}
