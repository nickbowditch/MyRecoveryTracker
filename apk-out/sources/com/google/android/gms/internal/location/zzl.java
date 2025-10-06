package com.google.android.gms.internal.location;

import android.app.PendingIntent;
import android.os.Parcel;
import android.os.RemoteException;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public abstract class zzl extends zzb implements zzm {
    public zzl() {
        super("com.google.android.gms.location.internal.IGeofencerCallbacks");
    }

    @Override // com.google.android.gms.internal.location.zzb
    protected final boolean zza(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        switch (i) {
            case 1:
                int i3 = parcel.readInt();
                String[] strArrCreateStringArray = parcel.createStringArray();
                zzc.zzb(parcel);
                zzb(i3, strArrCreateStringArray);
                return true;
            case 2:
                int i4 = parcel.readInt();
                String[] strArrCreateStringArray2 = parcel.createStringArray();
                zzc.zzb(parcel);
                zzd(i4, strArrCreateStringArray2);
                return true;
            case 3:
                int i5 = parcel.readInt();
                PendingIntent pendingIntent = (PendingIntent) zzc.zza(parcel, PendingIntent.CREATOR);
                zzc.zzb(parcel);
                zzc(i5, pendingIntent);
                return true;
            default:
                return false;
        }
    }
}
