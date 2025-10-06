package com.nick.myrecoverytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.os.EnvironmentCompat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: RingerChangeReceiver.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016J\u0018\u0010\n\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\rH\u0002¨\u0006\u000f"}, d2 = {"Lcom/nick/myrecoverytracker/RingerChangeReceiver;", "Landroid/content/BroadcastReceiver;", "<init>", "()V", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "write", "ctx", "mode", "", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class RingerChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "RingerChangeReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        int m;
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(intent, "intent");
        String a = intent.getAction();
        if (a == null) {
            return;
        }
        switch (a.hashCode()) {
            case -1988830022:
                if (!a.equals("com.nick.myrecoverytracker.TEST_RINGER_LOG") || (m = intent.getIntExtra("mode", -1)) == -1) {
                    return;
                }
                write(context, m);
                return;
            case -841039066:
                if (!a.equals("android.media.EXTERNAL_RINGER_MODE_CHANGED_ACTION")) {
                    return;
                }
                break;
            case 100931828:
                if (!a.equals("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")) {
                    return;
                }
                break;
            case 2070024785:
                if (!a.equals("android.media.RINGER_MODE_CHANGED")) {
                    return;
                }
                break;
            default:
                return;
        }
        Object systemService = context.getSystemService("audio");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.media.AudioManager");
        AudioManager am = (AudioManager) systemService;
        write(context, am.getRingerMode());
    }

    private final void write(Context ctx, int mode) {
        String name;
        switch (mode) {
            case 0:
                name = NotificationCompat.GROUP_KEY_SILENT;
                break;
            case 1:
                name = "vibrate";
                break;
            case 2:
                name = "normal";
                break;
            default:
                name = EnvironmentCompat.MEDIA_UNKNOWN;
                break;
        }
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        try {
            File f = new File(ctx.getFilesDir(), "ringer_log.csv");
            FilesKt.appendText$default(f, ts + "," + name + "\n", null, 2, null);
            Log.i(TAG, "ringer: " + ts + "," + name);
        } catch (Throwable t) {
            Log.e(TAG, "write failed", t);
        }
    }
}
