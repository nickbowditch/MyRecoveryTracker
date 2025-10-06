package com.google.android.gms.common;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.google.android.gms.common.util.DeviceProperties;
import com.google.android.gms.common.wrappers.Wrappers;

/* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
/* loaded from: classes.dex */
public class GoogleApiAvailabilityLight {
    public static final String GOOGLE_PLAY_SERVICES_PACKAGE = "com.google.android.gms";
    public static final String GOOGLE_PLAY_STORE_PACKAGE = "com.android.vending";
    static final String TRACKING_SOURCE_DIALOG = "d";
    static final String TRACKING_SOURCE_NOTIFICATION = "n";
    public static final int GOOGLE_PLAY_SERVICES_VERSION_CODE = GooglePlayServicesUtilLight.GOOGLE_PLAY_SERVICES_VERSION_CODE;
    private static final GoogleApiAvailabilityLight zza = new GoogleApiAvailabilityLight();

    GoogleApiAvailabilityLight() {
    }

    public static GoogleApiAvailabilityLight getInstance() {
        return zza;
    }

    public void cancelAvailabilityErrorNotifications(Context context) {
        GooglePlayServicesUtilLight.cancelAvailabilityErrorNotifications(context);
    }

    public int getApkVersion(Context context) {
        return GooglePlayServicesUtilLight.getApkVersion(context);
    }

    public int getClientVersion(Context context) {
        return GooglePlayServicesUtilLight.getClientVersion(context);
    }

    @Deprecated
    public Intent getErrorResolutionIntent(int errorCode) {
        return getErrorResolutionIntent(null, errorCode, null);
    }

    public PendingIntent getErrorResolutionPendingIntent(Context context, int errorCode, int requestCode) {
        return getErrorResolutionPendingIntent(context, errorCode, requestCode, null);
    }

    public String getErrorString(int errorCode) {
        return GooglePlayServicesUtilLight.getErrorString(errorCode);
    }

    public int isGooglePlayServicesAvailable(Context context) {
        return isGooglePlayServicesAvailable(context, GOOGLE_PLAY_SERVICES_VERSION_CODE);
    }

    public boolean isPlayServicesPossiblyUpdating(Context context, int errorCode) {
        return GooglePlayServicesUtilLight.isPlayServicesPossiblyUpdating(context, errorCode);
    }

    public boolean isPlayStorePossiblyUpdating(Context context, int errorCode) {
        return GooglePlayServicesUtilLight.isPlayStorePossiblyUpdating(context, errorCode);
    }

    public boolean isUninstalledAppPossiblyUpdating(Context context, String packageName) {
        return GooglePlayServicesUtilLight.zza(context, packageName);
    }

    public boolean isUserResolvableError(int errorCode) {
        return GooglePlayServicesUtilLight.isUserRecoverableError(errorCode);
    }

    public void verifyGooglePlayServicesIsAvailable(Context context, int minApkVersion) throws GooglePlayServicesRepairableException, GooglePlayServicesNotAvailableException {
        GooglePlayServicesUtilLight.ensurePlayServicesAvailable(context, minApkVersion);
    }

    public Intent getErrorResolutionIntent(Context context, int errorCode, String trackingSource) {
        switch (errorCode) {
            case 1:
            case 2:
                if (context != null && DeviceProperties.isWearableWithoutPlayStore(context)) {
                    return com.google.android.gms.common.internal.zzt.zza();
                }
                StringBuilder sb = new StringBuilder();
                sb.append("gcore_");
                sb.append(GOOGLE_PLAY_SERVICES_VERSION_CODE);
                sb.append("-");
                if (!TextUtils.isEmpty(trackingSource)) {
                    sb.append(trackingSource);
                }
                sb.append("-");
                if (context != null) {
                    sb.append(context.getPackageName());
                }
                sb.append("-");
                if (context != null) {
                    try {
                        sb.append(Wrappers.packageManager(context).getPackageInfo(context.getPackageName(), 0).versionCode);
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                }
                return com.google.android.gms.common.internal.zzt.zzb("com.google.android.gms", sb.toString());
            case 3:
                return com.google.android.gms.common.internal.zzt.zzc("com.google.android.gms");
            default:
                return null;
        }
    }

    public PendingIntent getErrorResolutionPendingIntent(Context context, int errorCode, int requestCode, String trackingSource) {
        Intent errorResolutionIntent = getErrorResolutionIntent(context, errorCode, trackingSource);
        if (errorResolutionIntent == null) {
            return null;
        }
        return PendingIntent.getActivity(context, requestCode, errorResolutionIntent, com.google.android.gms.internal.common.zzd.zza | 134217728);
    }

    public int isGooglePlayServicesAvailable(Context context, int minApkVersion) {
        int minApkVersion2 = GooglePlayServicesUtilLight.isGooglePlayServicesAvailable(context, minApkVersion);
        if (GooglePlayServicesUtilLight.isPlayServicesPossiblyUpdating(context, minApkVersion2)) {
            return 18;
        }
        return minApkVersion2;
    }
}
