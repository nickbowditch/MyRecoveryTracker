package com.nick.myrecoverytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MovementARReceiver.kt */
@Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016¨\u0006\n"}, d2 = {"Lcom/nick/myrecoverytracker/MovementARReceiver;", "Landroid/content/BroadcastReceiver;", "<init>", "()V", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class MovementARReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        ActivityTransitionResult res;
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(intent, "intent");
        if (ActivityTransitionResult.hasResult(intent) && (res = ActivityTransitionResult.extractResult(intent)) != null) {
            for (ActivityTransitionEvent e : res.getTransitionEvents()) {
                switch (e.getActivityType()) {
                    case 0:
                    case 1:
                    case 2:
                    case 7:
                    case 8:
                        MovementCapture.INSTANCE.writeActive();
                        break;
                    case 3:
                        MovementCapture.INSTANCE.writeStill();
                        break;
                }
            }
        }
    }
}
