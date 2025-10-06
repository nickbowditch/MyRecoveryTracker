package com.google.android.gms.location;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public abstract class zzq extends com.google.android.gms.internal.location.zzb implements zzr {
    public zzq() {
        super("com.google.android.gms.location.ILocationCallback");
    }

    public static zzr zzb(IBinder iBinder) {
        IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.location.ILocationCallback");
        return iInterfaceQueryLocalInterface instanceof zzr ? (zzr) iInterfaceQueryLocalInterface : new zzp(iBinder);
    }

    @Override // com.google.android.gms.internal.location.zzb
    protected final boolean zza(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        switch (i) {
            case 1:
                LocationResult locationResult = (LocationResult) com.google.android.gms.internal.location.zzc.zza(parcel, LocationResult.CREATOR);
                com.google.android.gms.internal.location.zzc.zzb(parcel);
                zze(locationResult);
                return true;
            case 2:
                LocationAvailability locationAvailability = (LocationAvailability) com.google.android.gms.internal.location.zzc.zza(parcel, LocationAvailability.CREATOR);
                com.google.android.gms.internal.location.zzc.zzb(parcel);
                zzd(locationAvailability);
                return true;
            case 3:
                zzf();
                return true;
            default:
                return false;
        }
    }
}
