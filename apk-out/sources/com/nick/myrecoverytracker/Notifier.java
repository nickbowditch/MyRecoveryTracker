package com.nick.myrecoverytracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Notifier.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u000e\u0010\r\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0086T¢\u0006\u0002\n\u0000¨\u0006\u000f"}, d2 = {"Lcom/nick/myrecoverytracker/Notifier;", "", "<init>", "()V", "CHANNEL_ID", "", "CHANNEL_NAME", "ID_ONBOARDING", "", "ensureChannel", "", "ctx", "Landroid/content/Context;", "showPersistentOnboarding", "cancelOnboarding", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class Notifier {
    private static final String CHANNEL_ID = "onboarding_support_channel";
    private static final String CHANNEL_NAME = "Onboarding";
    public static final int ID_ONBOARDING = 2001;
    public static final Notifier INSTANCE = new Notifier();

    private Notifier() {
    }

    private final void ensureChannel(Context ctx) {
        Object systemService = ctx.getSystemService("notification");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.NotificationManager");
        NotificationManager nm = (NotificationManager) systemService;
        NotificationChannel existing = nm.getNotificationChannel(CHANNEL_ID);
        if (existing == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, 3);
            ch.setDescription("Prompts you to add your support contacts");
            ch.enableLights(true);
            ch.setLightColor(-16711681);
            ch.enableVibration(false);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
    }

    public final void showPersistentOnboarding(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        ensureChannel(ctx);
        Intent intent = new Intent(ctx, (Class<?>) OnboardingActivity.class);
        intent.addFlags(335544320);
        PendingIntent contentPi = PendingIntent.getActivity(ctx, 0, intent, 201326592);
        Notification notif = new NotificationCompat.Builder(ctx, CHANNEL_ID).setSmallIcon(android.R.drawable.stat_sys_warning).setContentTitle("Add your support contacts").setContentText("Tap to add 1–5 numbers you consider safe.").setOngoing(true).setAutoCancel(false).setContentIntent(contentPi).build();
        Intrinsics.checkNotNullExpressionValue(notif, "build(...)");
        Object systemService = ctx.getSystemService("notification");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.NotificationManager");
        NotificationManager nm = (NotificationManager) systemService;
        nm.notify(ID_ONBOARDING, notif);
    }

    public final void cancelOnboarding(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Object systemService = ctx.getSystemService("notification");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.NotificationManager");
        NotificationManager nm = (NotificationManager) systemService;
        nm.cancel(ID_ONBOARDING);
    }
}
