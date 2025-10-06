package com.google.android.gms.location;

import android.os.IBinder;
import android.os.RemoteException;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public final class zzp extends com.google.android.gms.internal.location.zza implements zzr {
    zzp(IBinder iBinder) {
        super(iBinder, "com.google.android.gms.location.ILocationCallback");
    }

    @Override // com.google.android.gms.location.zzr
    public final void zzd(LocationAvailability locationAvailability) throws RemoteException {
        throw null;
    }

    @Override // com.google.android.gms.location.zzr
    public final void zze(LocationResult locationResult) throws RemoteException {
        throw null;
    }

    @Override // com.google.android.gms.location.zzr
    public final void zzf() throws RemoteException {
        throw null;
    }
}
