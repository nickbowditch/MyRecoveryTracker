package com.google.android.gms.location;

import android.app.Activity;
import android.content.Context;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.internal.location.zzau;
import com.google.android.gms.internal.location.zzbp;
import com.google.android.gms.internal.location.zzbv;
import com.google.android.gms.internal.location.zzbz;
import com.google.android.gms.internal.location.zzcc;
import com.google.android.gms.internal.location.zzce;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public class LocationServices {

    @Deprecated
    public static final Api<Api.ApiOptions.NoOptions> API = zzbp.zzb;

    @Deprecated
    public static final FusedLocationProviderApi FusedLocationApi = new zzau();

    @Deprecated
    public static final GeofencingApi GeofencingApi = new zzbv();

    @Deprecated
    public static final SettingsApi SettingsApi = new zzcc();

    private LocationServices() {
    }

    public static FusedLocationProviderClient getFusedLocationProviderClient(Activity activity) {
        return new zzbp(activity);
    }

    public static GeofencingClient getGeofencingClient(Activity activity) {
        return new zzbz(activity);
    }

    public static SettingsClient getSettingsClient(Activity activity) {
        return new zzce(activity);
    }

    public static FusedLocationProviderClient getFusedLocationProviderClient(Context context) {
        return new zzbp(context);
    }

    public static GeofencingClient getGeofencingClient(Context context) {
        return new zzbz(context);
    }

    public static SettingsClient getSettingsClient(Context context) {
        return new zzce(context);
    }
}
