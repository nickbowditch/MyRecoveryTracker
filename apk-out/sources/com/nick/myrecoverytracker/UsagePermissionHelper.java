package com.nick.myrecoverytracker;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Process;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: UsagePermissionHelper.kt */
@Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\u0010\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\u0011\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\u0012\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007J\u000e\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0006\u001a\u00020\u0007R\u0019\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b¢\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\r\u0010\u000e¨\u0006\u0015"}, d2 = {"Lcom/nick/myrecoverytracker/UsagePermissionHelper;", "", "<init>", "()V", "isGranted", "", "context", "Landroid/content/Context;", "openSettings", "", "RESTRICTED_PERMS", "", "", "getRESTRICTED_PERMS", "()[Ljava/lang/String;", "[Ljava/lang/String;", "hasSms", "hasCallLog", "needsRestricted", "appSettingsIntent", "Landroid/content/Intent;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UsagePermissionHelper {
    public static final UsagePermissionHelper INSTANCE = new UsagePermissionHelper();
    private static final String[] RESTRICTED_PERMS = {"android.permission.READ_SMS", "android.permission.READ_CALL_LOG"};

    private UsagePermissionHelper() {
    }

    public final boolean isGranted(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        Object systemService = context.getSystemService("appops");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.AppOpsManager");
        AppOpsManager appOps = (AppOpsManager) systemService;
        int mode = appOps.checkOpNoThrow("android:get_usage_stats", Process.myUid(), context.getPackageName());
        return mode == 0;
    }

    public final void openSettings(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        context.startActivity(new Intent("android.settings.USAGE_ACCESS_SETTINGS").addFlags(268435456));
    }

    public final String[] getRESTRICTED_PERMS() {
        return RESTRICTED_PERMS;
    }

    public final boolean hasSms(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        return ContextCompat.checkSelfPermission(context, "android.permission.READ_SMS") == 0;
    }

    public final boolean hasCallLog(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        return ContextCompat.checkSelfPermission(context, "android.permission.READ_CALL_LOG") == 0;
    }

    public final boolean needsRestricted(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        return (hasSms(context) && hasCallLog(context)) ? false : true;
    }

    public final Intent appSettingsIntent(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        intent.addFlags(268435456);
        return intent;
    }
}
