package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter;
import com.google.android.gms.internal.location.zzdh;
import java.util.ArrayList;
import java.util.List;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public class GeofencingRequest extends AbstractSafeParcelable {
    public static final Parcelable.Creator<GeofencingRequest> CREATOR = new zzn();
    public static final int INITIAL_TRIGGER_DWELL = 4;
    public static final int INITIAL_TRIGGER_ENTER = 1;
    public static final int INITIAL_TRIGGER_EXIT = 2;
    private final List zza;
    private final int zzb;
    private final String zzc;
    private final String zzd;

    /* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
    public static final class Builder {
        private final List zza = new ArrayList();
        private int zzb = 5;
        private String zzc = "";

        public Builder addGeofence(Geofence geofence) {
            Preconditions.checkNotNull(geofence, "geofence can't be null.");
            Preconditions.checkArgument(geofence instanceof zzdh, "Geofence must be created using Geofence.Builder.");
            this.zza.add((zzdh) geofence);
            return this;
        }

        public Builder addGeofences(List<Geofence> list) {
            if (list == null || list.isEmpty()) {
                return this;
            }
            for (Geofence geofence : list) {
                if (geofence != null) {
                    addGeofence(geofence);
                }
            }
            return this;
        }

        public GeofencingRequest build() {
            Preconditions.checkArgument(!this.zza.isEmpty(), "No geofence has been added to this request.");
            return new GeofencingRequest(this.zza, this.zzb, this.zzc, null);
        }

        public Builder setInitialTrigger(int i) {
            this.zzb = i & 7;
            return this;
        }
    }

    /* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
    public @interface InitialTrigger {
    }

    GeofencingRequest(List list, int i, String str, String str2) {
        this.zza = list;
        this.zzb = i;
        this.zzc = str;
        this.zzd = str2;
    }

    public List<Geofence> getGeofences() {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(this.zza);
        return arrayList;
    }

    public int getInitialTrigger() {
        return this.zzb;
    }

    public String toString() {
        return "GeofencingRequest[geofences=" + this.zza + ", initialTrigger=" + this.zzb + ", tag=" + this.zzc + ", attributionTag=" + this.zzd + "]";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int i) {
        int iBeginObjectHeader = SafeParcelWriter.beginObjectHeader(dest);
        SafeParcelWriter.writeTypedList(dest, 1, this.zza, false);
        SafeParcelWriter.writeInt(dest, 2, getInitialTrigger());
        SafeParcelWriter.writeString(dest, 3, this.zzc, false);
        SafeParcelWriter.writeString(dest, 4, this.zzd, false);
        SafeParcelWriter.finishObjectHeader(dest, iBeginObjectHeader);
    }

    public final GeofencingRequest zza(String str) {
        return new GeofencingRequest(this.zza, this.zzb, this.zzc, str);
    }
}
