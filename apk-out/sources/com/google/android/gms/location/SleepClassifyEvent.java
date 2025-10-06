package com.google.android.gms.location;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter;
import com.google.android.gms.common.internal.safeparcel.SafeParcelableSerializer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public class SleepClassifyEvent extends AbstractSafeParcelable {
    public static final Parcelable.Creator<SleepClassifyEvent> CREATOR = new zzaf();
    private final int zza;
    private final int zzb;
    private final int zzc;
    private final int zzd;
    private final int zze;
    private final int zzf;
    private final int zzg;
    private final boolean zzh;
    private final int zzi;

    public SleepClassifyEvent(int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8) {
        this.zza = i;
        this.zzb = i2;
        this.zzc = i3;
        this.zzd = i4;
        this.zze = i5;
        this.zzf = i6;
        this.zzg = i7;
        this.zzh = z;
        this.zzi = i8;
    }

    public static List<SleepClassifyEvent> extractEvents(Intent intent) {
        Preconditions.checkNotNull(intent);
        if (!hasEvents(intent)) {
            return Collections.emptyList();
        }
        ArrayList arrayList = (ArrayList) intent.getSerializableExtra("com.google.android.location.internal.EXTRA_SLEEP_CLASSIFY_RESULT");
        if (arrayList == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList2 = new ArrayList(arrayList.size());
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            byte[] bArr = (byte[]) arrayList.get(i);
            Preconditions.checkNotNull(bArr);
            arrayList2.add((SleepClassifyEvent) SafeParcelableSerializer.deserializeFromBytes(bArr, CREATOR));
        }
        return Collections.unmodifiableList(arrayList2);
    }

    public static boolean hasEvents(Intent intent) {
        if (intent == null) {
            return false;
        }
        return intent.hasExtra("com.google.android.location.internal.EXTRA_SLEEP_CLASSIFY_RESULT");
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SleepClassifyEvent)) {
            return false;
        }
        SleepClassifyEvent sleepClassifyEvent = (SleepClassifyEvent) o;
        return this.zza == sleepClassifyEvent.zza && this.zzb == sleepClassifyEvent.zzb;
    }

    public int getConfidence() {
        return this.zzb;
    }

    public int getLight() {
        return this.zzd;
    }

    public int getMotion() {
        return this.zzc;
    }

    public long getTimestampMillis() {
        return this.zza * 1000;
    }

    public int hashCode() {
        return Objects.hashCode(Integer.valueOf(this.zza), Integer.valueOf(this.zzb));
    }

    public String toString() {
        return this.zza + " Conf:" + this.zzb + " Motion:" + this.zzc + " Light:" + this.zzd;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int i) {
        Preconditions.checkNotNull(out);
        int iBeginObjectHeader = SafeParcelWriter.beginObjectHeader(out);
        SafeParcelWriter.writeInt(out, 1, this.zza);
        SafeParcelWriter.writeInt(out, 2, getConfidence());
        SafeParcelWriter.writeInt(out, 3, getMotion());
        SafeParcelWriter.writeInt(out, 4, getLight());
        SafeParcelWriter.writeInt(out, 5, this.zze);
        SafeParcelWriter.writeInt(out, 6, this.zzf);
        SafeParcelWriter.writeInt(out, 7, this.zzg);
        SafeParcelWriter.writeBoolean(out, 8, this.zzh);
        SafeParcelWriter.writeInt(out, 9, this.zzi);
        SafeParcelWriter.finishObjectHeader(out, iBeginObjectHeader);
    }
}
