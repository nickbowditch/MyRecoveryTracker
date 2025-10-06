package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelReader;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public final class zzv implements Parcelable.Creator {
    @Override // android.os.Parcelable.Creator
    public final /* bridge */ /* synthetic */ Object createFromParcel(Parcel parcel) {
        int iValidateObjectHeader = SafeParcelReader.validateObjectHeader(parcel);
        long j = Long.MAX_VALUE;
        int i = 0;
        boolean z = false;
        String strCreateString = null;
        com.google.android.gms.internal.location.zzd zzdVar = null;
        while (parcel.dataPosition() < iValidateObjectHeader) {
            int header = SafeParcelReader.readHeader(parcel);
            switch (SafeParcelReader.getFieldId(header)) {
                case 1:
                    j = SafeParcelReader.readLong(parcel, header);
                    break;
                case 2:
                    i = SafeParcelReader.readInt(parcel, header);
                    break;
                case 3:
                    z = SafeParcelReader.readBoolean(parcel, header);
                    break;
                case 4:
                    strCreateString = SafeParcelReader.createString(parcel, header);
                    break;
                case 5:
                    zzdVar = (com.google.android.gms.internal.location.zzd) SafeParcelReader.createParcelable(parcel, header, com.google.android.gms.internal.location.zzd.CREATOR);
                    break;
                default:
                    SafeParcelReader.skipUnknownField(parcel, header);
                    break;
            }
        }
        SafeParcelReader.ensureAtEnd(parcel, iValidateObjectHeader);
        return new LastLocationRequest(j, i, z, strCreateString, zzdVar);
    }

    @Override // android.os.Parcelable.Creator
    public final /* synthetic */ Object[] newArray(int i) {
        return new LastLocationRequest[i];
    }
}
