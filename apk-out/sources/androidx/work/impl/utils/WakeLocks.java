package androidx.work.impl.utils;

import android.content.Context;
import android.os.PowerManager;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Logger;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: WakeLocks.kt */
@Metadata(d1 = {"\u0000 \n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u001c\u0010\u0004\u001a\u00060\u0005R\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0001H\u0000\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\n"}, d2 = {"TAG", "", "checkWakeLocks", "", "newWakeLock", "Landroid/os/PowerManager$WakeLock;", "Landroid/os/PowerManager;", "context", "Landroid/content/Context;", "tag", "work-runtime_release"}, k = 2, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class WakeLocks {
    private static final String TAG;

    static {
        String strTagWithPrefix = Logger.tagWithPrefix("WakeLocks");
        Intrinsics.checkNotNullExpressionValue(strTagWithPrefix, "tagWithPrefix(\"WakeLocks\")");
        TAG = strTagWithPrefix;
    }

    public static final PowerManager.WakeLock newWakeLock(Context context, String tag) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(tag, "tag");
        Object systemService = context.getApplicationContext().getSystemService("power");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.os.PowerManager");
        PowerManager powerManager = (PowerManager) systemService;
        String tagWithPrefix = "WorkManager: " + tag;
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(1, tagWithPrefix);
        synchronized (WakeLocksHolder.INSTANCE) {
            WakeLocksHolder.INSTANCE.getWakeLocks().put(wakeLock, tagWithPrefix);
        }
        Intrinsics.checkNotNullExpressionValue(wakeLock, "wakeLock");
        return wakeLock;
    }

    public static final void checkWakeLocks() {
        Map wakeLocksCopy = new LinkedHashMap();
        synchronized (WakeLocksHolder.INSTANCE) {
            wakeLocksCopy.putAll(WakeLocksHolder.INSTANCE.getWakeLocks());
            Unit unit = Unit.INSTANCE;
        }
        for (Map.Entry element$iv : wakeLocksCopy.entrySet()) {
            PowerManager.WakeLock wakeLock = (PowerManager.WakeLock) element$iv.getKey();
            String tag = (String) element$iv.getValue();
            boolean z = false;
            if (wakeLock != null && wakeLock.isHeld()) {
                z = true;
            }
            if (z) {
                Logger.get().warning(TAG, "WakeLock held for " + tag);
            }
        }
    }
}
