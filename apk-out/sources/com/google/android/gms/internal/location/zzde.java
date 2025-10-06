package com.google.android.gms.internal.location;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.ClientIdentity;
import com.google.android.gms.common.internal.safeparcel.SafeParcelReader;
import com.google.android.gms.location.LocationRequest;
import java.util.ArrayList;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public final class zzde implements Parcelable.Creator {
    @Override // android.os.Parcelable.Creator
    public final /* bridge */ /* synthetic */ Object createFromParcel(Parcel parcel) {
        int iValidateObjectHeader = SafeParcelReader.validateObjectHeader(parcel);
        LocationRequest locationRequest = null;
        ArrayList arrayListCreateTypedList = null;
        String strCreateString = null;
        String strCreateString2 = null;
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        long j = Long.MAX_VALUE;
        while (parcel.dataPosition() < iValidateObjectHeader) {
            int header = SafeParcelReader.readHeader(parcel);
            switch (SafeParcelReader.getFieldId(header)) {
                case 1:
                    locationRequest = (LocationRequest) SafeParcelReader.createParcelable(parcel, header, LocationRequest.CREATOR);
                    break;
                case 2:
                case 3:
                case 4:
                case 6:
                case 7:
                default:
                    SafeParcelReader.skipUnknownField(parcel, header);
                    break;
                case 5:
                    arrayListCreateTypedList = SafeParcelReader.createTypedList(parcel, header, ClientIdentity.CREATOR);
                    break;
                case 8:
                    z = SafeParcelReader.readBoolean(parcel, header);
                    break;
                case 9:
                    z2 = SafeParcelReader.readBoolean(parcel, header);
                    break;
                case 10:
                    strCreateString = SafeParcelReader.createString(parcel, header);
                    break;
                case 11:
                    z3 = SafeParcelReader.readBoolean(parcel, header);
                    break;
                case 12:
                    z4 = SafeParcelReader.readBoolean(parcel, header);
                    break;
                case 13:
                    strCreateString2 = SafeParcelReader.createString(parcel, header);
                    break;
                case 14:
                    j = SafeParcelReader.readLong(parcel, header);
                    break;
            }
        }
        SafeParcelReader.ensureAtEnd(parcel, iValidateObjectHeader);
        return new zzdd(locationRequest, arrayListCreateTypedList, z, z2, strCreateString, z3, z4, strCreateString2, j);
    }

    @Override // android.os.Parcelable.Creator
    public final /* synthetic */ Object[] newArray(int i) {
        return new zzdd[i];
    }
}
