package com.google.android.gms.location;

import com.google.android.gms.common.internal.Preconditions;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public final class zzae {
    public static int zza(int i) {
        boolean z = true;
        if (i != 100 && i != 102 && i != 104) {
            if (i == 105) {
                i = 105;
            } else {
                z = false;
            }
        }
        Preconditions.checkArgument(z, "priority %d must be a Priority.PRIORITY_* constant", Integer.valueOf(i));
        return i;
    }

    public static String zzb(int i) {
        switch (i) {
            case 100:
                return "HIGH_ACCURACY";
            case 101:
            case 103:
            default:
                throw new IllegalArgumentException();
            case 102:
                return "BALANCED_POWER_ACCURACY";
            case 104:
                return "LOW_POWER";
            case 105:
                return "PASSIVE";
        }
    }
}
