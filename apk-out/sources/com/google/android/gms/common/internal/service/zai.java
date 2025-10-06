package com.google.android.gms.common.internal.service;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.common.internal.TelemetryData;

/* compiled from: com.google.android.gms:play-services-base@@18.1.0 */
/* loaded from: classes.dex */
public final class zai extends com.google.android.gms.internal.base.zaa implements IInterface {
    zai(IBinder iBinder) {
        super(iBinder, "com.google.android.gms.common.internal.service.IClientTelemetryService");
    }

    public final void zae(TelemetryData telemetryData) throws RemoteException {
        Parcel parcelZaa = zaa();
        com.google.android.gms.internal.base.zac.zad(parcelZaa, telemetryData);
        zad(1, parcelZaa);
    }
}
