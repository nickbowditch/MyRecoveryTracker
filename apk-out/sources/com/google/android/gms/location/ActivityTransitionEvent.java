package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public class ActivityTransitionEvent extends AbstractSafeParcelable {
    public static final Parcelable.Creator<ActivityTransitionEvent> CREATOR = new zzf();
    private final int zza;
    private final int zzb;
    private final long zzc;

    public ActivityTransitionEvent(int activityType, int transitionType, long elapsedRealtimeNanos) {
        ActivityTransition.zza(transitionType);
        this.zza = activityType;
        this.zzb = transitionType;
        this.zzc = elapsedRealtimeNanos;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ActivityTransitionEvent)) {
            return false;
        }
        ActivityTransitionEvent activityTransitionEvent = (ActivityTransitionEvent) object;
        return this.zza == activityTransitionEvent.zza && this.zzb == activityTransitionEvent.zzb && this.zzc == activityTransitionEvent.zzc;
    }

    public int getActivityType() {
        return this.zza;
    }

    public long getElapsedRealTimeNanos() {
        return this.zzc;
    }

    public int getTransitionType() {
        return this.zzb;
    }

    public int hashCode() {
        return Objects.hashCode(Integer.valueOf(this.zza), Integer.valueOf(this.zzb), Long.valueOf(this.zzc));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ActivityType " + this.zza);
        sb.append(" ");
        sb.append("TransitionType " + this.zzb);
        sb.append(" ");
        sb.append("ElapsedRealTimeNanos " + this.zzc);
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int i) {
        Preconditions.checkNotNull(dest);
        int iBeginObjectHeader = SafeParcelWriter.beginObjectHeader(dest);
        SafeParcelWriter.writeInt(dest, 1, getActivityType());
        SafeParcelWriter.writeInt(dest, 2, getTransitionType());
        SafeParcelWriter.writeLong(dest, 3, getElapsedRealTimeNanos());
        SafeParcelWriter.finishObjectHeader(dest, iBeginObjectHeader);
    }
}
