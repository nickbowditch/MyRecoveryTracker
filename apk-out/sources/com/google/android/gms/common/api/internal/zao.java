package com.google.android.gms.common.api.internal;

import android.app.Dialog;
import android.app.PendingIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.internal.Preconditions;

/* compiled from: com.google.android.gms:play-services-base@@18.1.0 */
/* loaded from: classes.dex */
final class zao implements Runnable {
    final /* synthetic */ zap zaa;
    private final zam zab;

    zao(zap zapVar, zam zamVar) {
        this.zaa = zapVar;
        this.zab = zamVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        if (this.zaa.zaa) {
            ConnectionResult connectionResultZab = this.zab.zab();
            if (connectionResultZab.hasResolution()) {
                this.zaa.mLifecycleFragment.startActivityForResult(GoogleApiActivity.zaa(this.zaa.getActivity(), (PendingIntent) Preconditions.checkNotNull(connectionResultZab.getResolution()), this.zab.zaa(), false), 1);
                return;
            }
            zap zapVar = this.zaa;
            if (zapVar.zac.getErrorResolutionIntent(zapVar.getActivity(), connectionResultZab.getErrorCode(), null) != null) {
                zap zapVar2 = this.zaa;
                zapVar2.zac.zag(zapVar2.getActivity(), this.zaa.mLifecycleFragment, connectionResultZab.getErrorCode(), 2, this.zaa);
            } else {
                if (connectionResultZab.getErrorCode() != 18) {
                    this.zaa.zaa(connectionResultZab, this.zab.zaa());
                    return;
                }
                zap zapVar3 = this.zaa;
                Dialog dialogZab = zapVar3.zac.zab(zapVar3.getActivity(), this.zaa);
                zap zapVar4 = this.zaa;
                zapVar4.zac.zac(zapVar4.getActivity().getApplicationContext(), new zan(this, dialogZab));
            }
        }
    }
}
