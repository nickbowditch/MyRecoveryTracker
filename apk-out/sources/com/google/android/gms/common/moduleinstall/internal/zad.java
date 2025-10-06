package com.google.android.gms.common.moduleinstall.internal;

import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.moduleinstall.ModuleAvailabilityResponse;
import com.google.android.gms.common.moduleinstall.ModuleInstallIntentResponse;
import com.google.android.gms.common.moduleinstall.ModuleInstallResponse;

/* compiled from: com.google.android.gms:play-services-base@@18.1.0 */
/* loaded from: classes.dex */
public abstract class zad extends com.google.android.gms.internal.base.zab implements zae {
    public zad() {
        super("com.google.android.gms.common.moduleinstall.internal.IModuleInstallCallbacks");
    }

    @Override // com.google.android.gms.internal.base.zab
    protected final boolean zaa(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        switch (i) {
            case 1:
                Status status = (Status) com.google.android.gms.internal.base.zac.zaa(parcel, Status.CREATOR);
                ModuleAvailabilityResponse moduleAvailabilityResponse = (ModuleAvailabilityResponse) com.google.android.gms.internal.base.zac.zaa(parcel, ModuleAvailabilityResponse.CREATOR);
                com.google.android.gms.internal.base.zac.zab(parcel);
                zae(status, moduleAvailabilityResponse);
                return true;
            case 2:
                Status status2 = (Status) com.google.android.gms.internal.base.zac.zaa(parcel, Status.CREATOR);
                ModuleInstallResponse moduleInstallResponse = (ModuleInstallResponse) com.google.android.gms.internal.base.zac.zaa(parcel, ModuleInstallResponse.CREATOR);
                com.google.android.gms.internal.base.zac.zab(parcel);
                zad(status2, moduleInstallResponse);
                return true;
            case 3:
                Status status3 = (Status) com.google.android.gms.internal.base.zac.zaa(parcel, Status.CREATOR);
                ModuleInstallIntentResponse moduleInstallIntentResponse = (ModuleInstallIntentResponse) com.google.android.gms.internal.base.zac.zaa(parcel, ModuleInstallIntentResponse.CREATOR);
                com.google.android.gms.internal.base.zac.zab(parcel);
                zac(status3, moduleInstallIntentResponse);
                return true;
            case 4:
                Status status4 = (Status) com.google.android.gms.internal.base.zac.zaa(parcel, Status.CREATOR);
                com.google.android.gms.internal.base.zac.zab(parcel);
                zab(status4);
                return true;
            default:
                return false;
        }
    }
}
