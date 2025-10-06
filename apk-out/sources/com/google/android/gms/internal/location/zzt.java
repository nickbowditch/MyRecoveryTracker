package com.google.android.gms.internal.location;

import android.app.PendingIntent;
import android.os.RemoteException;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.Preconditions;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
final class zzt extends zzv {
    final /* synthetic */ long zza;
    final /* synthetic */ PendingIntent zzb;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    zzt(zzw zzwVar, GoogleApiClient googleApiClient, long j, PendingIntent pendingIntent) {
        super(googleApiClient);
        this.zza = j;
        this.zzb = pendingIntent;
    }

    @Override // com.google.android.gms.common.api.internal.BaseImplementation.ApiMethodImpl
    protected final /* bridge */ /* synthetic */ void doExecute(Api.AnyClient anyClient) throws RemoteException {
        zzf zzfVar = (zzf) anyClient;
        long j = this.zza;
        PendingIntent pendingIntent = this.zzb;
        Preconditions.checkNotNull(pendingIntent);
        Preconditions.checkArgument(j >= 0, "detectionIntervalMillis must be >= 0");
        ((zzo) zzfVar.getService()).zzr(j, true, pendingIntent);
        setResult((zzt) Status.RESULT_SUCCESS);
    }
}
