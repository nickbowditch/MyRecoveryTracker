package com.google.android.gms.common;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.android.gms.internal.base.zau;

/* compiled from: com.google.android.gms:play-services-base@@18.1.0 */
/* loaded from: classes.dex */
final class zac extends zau {
    final /* synthetic */ GoogleApiAvailability zaa;
    private final Context zab;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public zac(GoogleApiAvailability googleApiAvailability, Context context) {
        super(Looper.myLooper() == null ? Looper.getMainLooper() : Looper.myLooper());
        this.zaa = googleApiAvailability;
        this.zab = context.getApplicationContext();
    }

    @Override // android.os.Handler
    public final void handleMessage(Message message) {
        switch (message.what) {
            case 1:
                int iIsGooglePlayServicesAvailable = this.zaa.isGooglePlayServicesAvailable(this.zab);
                if (this.zaa.isUserResolvableError(iIsGooglePlayServicesAvailable)) {
                    this.zaa.showErrorNotification(this.zab, iIsGooglePlayServicesAvailable);
                    break;
                }
                break;
            default:
                Log.w("GoogleApiAvailability", "Don't know how to handle this message: " + message.what);
                break;
        }
    }
}
