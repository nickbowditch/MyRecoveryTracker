package com.nick.myrecoverytracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: NotificationUtils.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u000e\u0010\r\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082T¢\u0006\u0002\n\u0000¨\u0006\u000e"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationUtils;", "", "<init>", "()V", "CHANNEL_ID", "", "CHANNEL_NAME", "NID_RESTRICTED", "", "ensureChannel", "", "context", "Landroid/content/Context;", "postRestrictedPermsReminder", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class NotificationUtils {
    private static final String CHANNEL_ID = "perm_reminders";
    private static final String CHANNEL_NAME = "Permission Reminders";
    public static final NotificationUtils INSTANCE = new NotificationUtils();
    private static final int NID_RESTRICTED = 2001;

    private NotificationUtils() {
    }

    private final void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            Object systemService = context.getSystemService("notification");
            Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.NotificationManager");
            NotificationManager nm = (NotificationManager) systemService;
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, 2));
            }
        }
    }

    public final void postRestrictedPermsReminder(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        if (NotificationManagerCompat.from(context).areNotificationsEnabled() && UsagePermissionHelper.INSTANCE.needsRestricted(context)) {
            ensureChannel(context);
            PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, (Class<?>) OnboardingActivity.class), 201326592);
            Notification notif = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Enable SMS & Call Logs").setContentText("Tap to grant access. You can do it later anytime.").setContentIntent(pi).setAutoCancel(true).build();
            NotificationManagerCompat.from(context).notify(2001, notif);
        }
    }
}
