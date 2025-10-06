package com.google.android.gms.internal.location;

import android.os.RemoteException;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
final class zzaq extends zzat {
    final /* synthetic */ boolean zza;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    zzaq(zzau zzauVar, GoogleApiClient googleApiClient, boolean z) {
        super(googleApiClient);
        this.zza = z;
    }

    @Override // com.google.android.gms.common.api.internal.BaseImplementation.ApiMethodImpl
    protected final /* bridge */ /* synthetic */ void doExecute(Api.AnyClient anyClient) throws RemoteException {
        ((zzda) anyClient).zzA(this.zza, zzau.zza(this));
    }
}
