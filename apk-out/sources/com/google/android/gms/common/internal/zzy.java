package com.google.android.gms.common.internal;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.dynamic.IObjectWrapper;

/* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
/* loaded from: classes.dex */
public abstract class zzy extends com.google.android.gms.internal.common.zzb implements zzz {
    public zzy() {
        super("com.google.android.gms.common.internal.ICertData");
    }

    public static zzz zzg(IBinder iBinder) {
        IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.common.internal.ICertData");
        return iInterfaceQueryLocalInterface instanceof zzz ? (zzz) iInterfaceQueryLocalInterface : new zzx(iBinder);
    }

    @Override // com.google.android.gms.internal.common.zzb
    protected final boolean zza(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        switch (i) {
            case 1:
                IObjectWrapper iObjectWrapperZzd = zzd();
                parcel2.writeNoException();
                com.google.android.gms.internal.common.zzc.zzf(parcel2, iObjectWrapperZzd);
                return true;
            case 2:
                int iZzc = zzc();
                parcel2.writeNoException();
                parcel2.writeInt(iZzc);
                return true;
            default:
                return false;
        }
    }
}
