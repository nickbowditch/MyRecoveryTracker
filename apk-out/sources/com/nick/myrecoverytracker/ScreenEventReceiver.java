package com.nick.myrecoverytracker;

import android.content.BroadcastReceiver;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.text.SimpleDateFormat;
import java.util.Locale;
import kotlin.Metadata;

/* compiled from: ScreenEventReceiver.kt */
@Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\f"}, d2 = {"Lcom/nick/myrecoverytracker/ScreenEventReceiver;", "Landroid/content/BroadcastReceiver;", "<init>", "()V", "ts", "Ljava/text/SimpleDateFormat;", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class ScreenEventReceiver extends BroadcastReceiver {
    private final SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue
    java.lang.NullPointerException: Cannot invoke "java.util.List.iterator()" because the return value of "jadx.core.dex.visitors.regions.SwitchOverStringVisitor$SwitchData.getNewCases()" is null
    	at jadx.core.dex.visitors.regions.SwitchOverStringVisitor.restoreSwitchOverString(SwitchOverStringVisitor.java:109)
    	at jadx.core.dex.visitors.regions.SwitchOverStringVisitor.visitRegion(SwitchOverStringVisitor.java:66)
    	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:77)
    	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:82)
     */
    /* JADX WARN: Removed duplicated region for block: B:18:0x007d  */
    @Override // android.content.BroadcastReceiver
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void onReceive(android.content.Context r11, android.content.Intent r12) {
        /*
            r10 = this;
            java.lang.String r0 = "context"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r11, r0)
            java.lang.String r0 = "intent"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r12, r0)
            java.text.SimpleDateFormat r0 = r10.ts
            long r1 = java.lang.System.currentTimeMillis()
            java.lang.Long r1 = java.lang.Long.valueOf(r1)
            java.lang.String r0 = r0.format(r1)
            java.lang.String r1 = r12.getAction()
            r2 = 0
            if (r1 == 0) goto L7d
            int r3 = r1.hashCode()
            switch(r3) {
                case -2128145023: goto L60;
                case -1454123155: goto L44;
                case 823795052: goto L27;
                default: goto L26;
            }
        L26:
            goto L7d
        L27:
            java.lang.String r3 = "android.intent.action.USER_PRESENT"
            boolean r1 = r1.equals(r3)
            if (r1 != 0) goto L30
            goto L7d
        L30:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.StringBuilder r1 = r1.append(r0)
            java.lang.String r3 = ",UNLOCK\n"
            java.lang.StringBuilder r1 = r1.append(r3)
            java.lang.String r1 = r1.toString()
            goto L7e
        L44:
            java.lang.String r3 = "android.intent.action.SCREEN_ON"
            boolean r1 = r1.equals(r3)
            if (r1 == 0) goto L7d
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.StringBuilder r1 = r1.append(r0)
            java.lang.String r3 = ",ON\n"
            java.lang.StringBuilder r1 = r1.append(r3)
            java.lang.String r1 = r1.toString()
            goto L7e
        L60:
            java.lang.String r3 = "android.intent.action.SCREEN_OFF"
            boolean r1 = r1.equals(r3)
            if (r1 != 0) goto L69
            goto L7d
        L69:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.StringBuilder r1 = r1.append(r0)
            java.lang.String r3 = ",OFF\n"
            java.lang.StringBuilder r1 = r1.append(r3)
            java.lang.String r1 = r1.toString()
            goto L7e
        L7d:
            r1 = r2
        L7e:
            if (r1 == 0) goto Lde
            kotlin.Result$Companion r3 = kotlin.Result.INSTANCE     // Catch: java.lang.Throwable -> Lb8
            r3 = r10
            com.nick.myrecoverytracker.ScreenEventReceiver r3 = (com.nick.myrecoverytracker.ScreenEventReceiver) r3     // Catch: java.lang.Throwable -> Lb8
            r4 = 0
            java.lang.String r5 = "screen_log.csv"
            r6 = 32768(0x8000, float:4.5918E-41)
            java.io.FileOutputStream r5 = r11.openFileOutput(r5, r6)     // Catch: java.lang.Throwable -> Lb8
            java.io.Closeable r5 = (java.io.Closeable) r5     // Catch: java.lang.Throwable -> Lb8
            r6 = r5
            java.io.FileOutputStream r6 = (java.io.FileOutputStream) r6     // Catch: java.lang.Throwable -> Lb1
            r7 = 0
            java.nio.charset.Charset r8 = kotlin.text.Charsets.UTF_8     // Catch: java.lang.Throwable -> Lb1
            byte[] r8 = r1.getBytes(r8)     // Catch: java.lang.Throwable -> Lb1
            java.lang.String r9 = "getBytes(...)"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r8, r9)     // Catch: java.lang.Throwable -> Lb1
            r6.write(r8)     // Catch: java.lang.Throwable -> Lb1
            kotlin.Unit r6 = kotlin.Unit.INSTANCE     // Catch: java.lang.Throwable -> Lb1
            kotlin.io.CloseableKt.closeFinally(r5, r2)     // Catch: java.lang.Throwable -> Lb8
            kotlin.Unit r2 = kotlin.Unit.INSTANCE     // Catch: java.lang.Throwable -> Lb8
            kotlin.Result.m212constructorimpl(r2)     // Catch: java.lang.Throwable -> Lb8
            goto Lc2
        Lb1:
            r2 = move-exception
            throw r2     // Catch: java.lang.Throwable -> Lb3
        Lb3:
            r6 = move-exception
            kotlin.io.CloseableKt.closeFinally(r5, r2)     // Catch: java.lang.Throwable -> Lb8
            throw r6     // Catch: java.lang.Throwable -> Lb8
        Lb8:
            r2 = move-exception
            kotlin.Result$Companion r3 = kotlin.Result.INSTANCE
            java.lang.Object r2 = kotlin.ResultKt.createFailure(r2)
            kotlin.Result.m212constructorimpl(r2)
        Lc2:
            java.lang.String r2 = r12.getAction()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "screen event → "
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.StringBuilder r2 = r3.append(r2)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "ScreenEventReceiver"
            android.util.Log.i(r3, r2)
        Lde:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.ScreenEventReceiver.onReceive(android.content.Context, android.content.Intent):void");
    }
}
