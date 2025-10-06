package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter;
import java.util.Comparator;
import kotlinx.coroutines.debug.internal.DebugCoroutineInfoImplKt;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public class DetectedActivity extends AbstractSafeParcelable {
    public static final int IN_VEHICLE = 0;
    public static final int ON_BICYCLE = 1;
    public static final int ON_FOOT = 2;
    public static final int RUNNING = 8;
    public static final int STILL = 3;
    public static final int TILTING = 5;
    public static final int UNKNOWN = 4;
    public static final int WALKING = 7;
    int zzb;
    int zzc;
    public static final Comparator zza = new zzk();
    public static final Parcelable.Creator<DetectedActivity> CREATOR = new zzl();

    public DetectedActivity(int activityType, int confidence) {
        this.zzb = activityType;
        this.zzc = confidence;
    }

    public final boolean equals(Object obj) {
        if (obj instanceof DetectedActivity) {
            DetectedActivity detectedActivity = (DetectedActivity) obj;
            if (this.zzb == detectedActivity.zzb && this.zzc == detectedActivity.zzc) {
                return true;
            }
        }
        return false;
    }

    public int getConfidence() {
        return this.zzc;
    }

    public int getType() {
        int i = this.zzb;
        if (i > 22 || i < 0) {
            return 4;
        }
        return i;
    }

    public final int hashCode() {
        return Objects.hashCode(Integer.valueOf(this.zzb), Integer.valueOf(this.zzc));
    }

    public String toString() {
        String string;
        int type = getType();
        switch (type) {
            case 0:
                string = "IN_VEHICLE";
                break;
            case 1:
                string = "ON_BICYCLE";
                break;
            case 2:
                string = "ON_FOOT";
                break;
            case 3:
                string = "STILL";
                break;
            case 4:
                string = "UNKNOWN";
                break;
            case 5:
                string = "TILTING";
                break;
            case 6:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            default:
                string = Integer.toString(type);
                break;
            case 7:
                string = "WALKING";
                break;
            case 8:
                string = DebugCoroutineInfoImplKt.RUNNING;
                break;
            case 16:
                string = "IN_ROAD_VEHICLE";
                break;
            case 17:
                string = "IN_RAIL_VEHICLE";
                break;
        }
        return "DetectedActivity [type=" + string + ", confidence=" + this.zzc + "]";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int i) {
        Preconditions.checkNotNull(out);
        int iBeginObjectHeader = SafeParcelWriter.beginObjectHeader(out);
        SafeParcelWriter.writeInt(out, 1, this.zzb);
        SafeParcelWriter.writeInt(out, 2, this.zzc);
        SafeParcelWriter.finishObjectHeader(out, iBeginObjectHeader);
    }
}
