package com.nick.myrecoverytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;

/* compiled from: UnlockReceiver.kt */
@Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\f"}, d2 = {"Lcom/nick/myrecoverytracker/UnlockReceiver;", "Landroid/content/BroadcastReceiver;", "<init>", "()V", "ts", "Ljava/text/SimpleDateFormat;", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class UnlockReceiver extends BroadcastReceiver {
    private final SimpleDateFormat ts;

    public UnlockReceiver() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        this.ts = simpleDateFormat;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(intent, "intent");
        if (Intrinsics.areEqual(intent.getAction(), "android.intent.action.USER_PRESENT")) {
            String line = this.ts.format(Long.valueOf(System.currentTimeMillis())) + ",UNLOCK\n";
            try {
                Result.Companion companion = Result.INSTANCE;
                UnlockReceiver unlockReceiver = this;
                FileOutputStream fileOutputStreamOpenFileOutput = context.openFileOutput("unlock_log.csv", 32768);
                try {
                    byte[] bytes = line.getBytes(Charsets.UTF_8);
                    Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
                    fileOutputStreamOpenFileOutput.write(bytes);
                    Unit unit = Unit.INSTANCE;
                    CloseableKt.closeFinally(fileOutputStreamOpenFileOutput, null);
                    Result.m212constructorimpl(Unit.INSTANCE);
                } finally {
                }
            } catch (Throwable th) {
                Result.Companion companion2 = Result.INSTANCE;
                Result.m212constructorimpl(ResultKt.createFailure(th));
            }
            Log.i("UnlockReceiver", "USER_PRESENT → UNLOCK row appended");
        }
    }
}
