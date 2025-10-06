package com.google.android.gms.location;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.WorkSource;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter;
import com.google.android.gms.common.util.WorkSourceUtil;
import com.google.android.gms.internal.location.zzdj;
import org.checkerframework.dataflow.qual.Pure;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public final class CurrentLocationRequest extends AbstractSafeParcelable {
    public static final Parcelable.Creator<CurrentLocationRequest> CREATOR = new zzj();
    private final long zza;
    private final int zzb;
    private final int zzc;
    private final long zzd;
    private final boolean zze;
    private final int zzf;
    private final String zzg;
    private final WorkSource zzh;
    private final com.google.android.gms.internal.location.zzd zzi;

    /* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
    public static final class Builder {
        private long zza;
        private int zzb;
        private int zzc;
        private long zzd;
        private boolean zze;
        private int zzf;
        private String zzg;
        private WorkSource zzh;
        private com.google.android.gms.internal.location.zzd zzi;

        public Builder() {
            this.zza = 60000L;
            this.zzb = 0;
            this.zzc = 102;
            this.zzd = Long.MAX_VALUE;
            this.zze = false;
            this.zzf = 0;
            this.zzg = null;
            this.zzh = null;
            this.zzi = null;
        }

        public Builder(CurrentLocationRequest request) {
            this.zza = request.getMaxUpdateAgeMillis();
            this.zzb = request.getGranularity();
            this.zzc = request.getPriority();
            this.zzd = request.getDurationMillis();
            this.zze = request.zze();
            this.zzf = request.zza();
            this.zzg = request.zzd();
            this.zzh = new WorkSource(request.zzb());
            this.zzi = request.zzc();
        }

        public CurrentLocationRequest build() {
            return new CurrentLocationRequest(this.zza, this.zzb, this.zzc, this.zzd, this.zze, this.zzf, this.zzg, new WorkSource(this.zzh), this.zzi);
        }

        public Builder setDurationMillis(long durationMillis) {
            Preconditions.checkArgument(durationMillis > 0, "durationMillis must be greater than 0");
            this.zzd = durationMillis;
            return this;
        }

        public Builder setGranularity(int granularity) {
            zzo.zza(granularity);
            this.zzb = granularity;
            return this;
        }

        public Builder setMaxUpdateAgeMillis(long maxUpdateAgeMillis) {
            Preconditions.checkArgument(maxUpdateAgeMillis >= 0, "maxUpdateAgeMillis must be greater than or equal to 0");
            this.zza = maxUpdateAgeMillis;
            return this;
        }

        public Builder setPriority(int priority) {
            zzae.zza(priority);
            this.zzc = priority;
            return this;
        }
    }

    CurrentLocationRequest(long j, int i, int i2, long j2, boolean z, int i3, String str, WorkSource workSource, com.google.android.gms.internal.location.zzd zzdVar) {
        boolean z2 = true;
        if (Build.VERSION.SDK_INT >= 30 && str != null) {
            z2 = false;
        }
        Preconditions.checkArgument(z2);
        this.zza = j;
        this.zzb = i;
        this.zzc = i2;
        this.zzd = j2;
        this.zze = z;
        this.zzf = i3;
        this.zzg = str;
        this.zzh = workSource;
        this.zzi = zzdVar;
    }

    public boolean equals(Object object) {
        if (!(object instanceof CurrentLocationRequest)) {
            return false;
        }
        CurrentLocationRequest currentLocationRequest = (CurrentLocationRequest) object;
        return this.zza == currentLocationRequest.zza && this.zzb == currentLocationRequest.zzb && this.zzc == currentLocationRequest.zzc && this.zzd == currentLocationRequest.zzd && this.zze == currentLocationRequest.zze && this.zzf == currentLocationRequest.zzf && Objects.equal(this.zzg, currentLocationRequest.zzg) && Objects.equal(this.zzh, currentLocationRequest.zzh) && Objects.equal(this.zzi, currentLocationRequest.zzi);
    }

    @Pure
    public long getDurationMillis() {
        return this.zzd;
    }

    @Pure
    public int getGranularity() {
        return this.zzb;
    }

    @Pure
    public long getMaxUpdateAgeMillis() {
        return this.zza;
    }

    @Pure
    public int getPriority() {
        return this.zzc;
    }

    public int hashCode() {
        return Objects.hashCode(Long.valueOf(this.zza), Integer.valueOf(this.zzb), Integer.valueOf(this.zzc), Long.valueOf(this.zzd));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CurrentLocationRequest[");
        sb.append(zzae.zzb(this.zzc));
        if (this.zza != Long.MAX_VALUE) {
            sb.append(", maxAge=");
            zzdj.zzb(this.zza, sb);
        }
        if (this.zzd != Long.MAX_VALUE) {
            sb.append(", duration=");
            sb.append(this.zzd);
            sb.append("ms");
        }
        if (this.zzb != 0) {
            sb.append(", ");
            sb.append(zzo.zzb(this.zzb));
        }
        if (this.zze) {
            sb.append(", bypass");
        }
        if (this.zzf != 0) {
            sb.append(", ");
            sb.append(zzai.zza(this.zzf));
        }
        if (this.zzg != null) {
            sb.append(", moduleId=");
            sb.append(this.zzg);
        }
        if (!WorkSourceUtil.isEmpty(this.zzh)) {
            sb.append(", workSource=");
            sb.append(this.zzh);
        }
        if (this.zzi != null) {
            sb.append(", impersonation=");
            sb.append(this.zzi);
        }
        sb.append(']');
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        int iBeginObjectHeader = SafeParcelWriter.beginObjectHeader(parcel);
        SafeParcelWriter.writeLong(parcel, 1, getMaxUpdateAgeMillis());
        SafeParcelWriter.writeInt(parcel, 2, getGranularity());
        SafeParcelWriter.writeInt(parcel, 3, getPriority());
        SafeParcelWriter.writeLong(parcel, 4, getDurationMillis());
        SafeParcelWriter.writeBoolean(parcel, 5, this.zze);
        SafeParcelWriter.writeParcelable(parcel, 6, this.zzh, flags, false);
        SafeParcelWriter.writeInt(parcel, 7, this.zzf);
        SafeParcelWriter.writeString(parcel, 8, this.zzg, false);
        SafeParcelWriter.writeParcelable(parcel, 9, this.zzi, flags, false);
        SafeParcelWriter.finishObjectHeader(parcel, iBeginObjectHeader);
    }

    @Pure
    public final int zza() {
        return this.zzf;
    }

    @Pure
    public final WorkSource zzb() {
        return this.zzh;
    }

    @Pure
    public final com.google.android.gms.internal.location.zzd zzc() {
        return this.zzi;
    }

    @Deprecated
    @Pure
    public final String zzd() {
        return this.zzg;
    }

    @Pure
    public final boolean zze() {
        return this.zze;
    }
}
