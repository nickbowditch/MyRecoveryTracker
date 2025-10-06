package com.google.android.gms.common;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.common.internal.Preconditions;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.RestrictedInheritance;

/* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
@CheckReturnValue
@RestrictedInheritance(allowedOnPath = ".*javatests.*/com/google/android/gms/common/.*", explanation = "Sub classing of GMS Core's APIs are restricted to testing fakes.", link = "go/gmscore-restrictedinheritance")
/* loaded from: classes.dex */
public class PackageSignatureVerifier {
    private static zzad zza;
    private volatile zzac zzb;

    private static zzad zza() {
        zzad zzadVar;
        synchronized (zzad.class) {
            if (zza == null) {
                zza = new zzad();
            }
            zzadVar = zza;
        }
        return zzadVar;
    }

    public PackageVerificationResult queryPackageSignatureVerified(Context context, String callingPackage) {
        boolean zHonorsDebugCertificates = GooglePlayServicesUtilLight.honorsDebugCertificates(context);
        zza();
        if (!zzn.zzf()) {
            throw new zzae();
        }
        String strConcat = String.valueOf(callingPackage).concat(true != zHonorsDebugCertificates ? "-0" : "-1");
        if (this.zzb != null && this.zzb.zza.equals(strConcat)) {
            return this.zzb.zzb;
        }
        zza();
        zzx zzxVarZzc = zzn.zzc(callingPackage, zHonorsDebugCertificates, false, false);
        if (zzxVarZzc.zza) {
            this.zzb = new zzac(strConcat, PackageVerificationResult.zzd(callingPackage, zzxVarZzc.zzd));
            return this.zzb.zzb;
        }
        Preconditions.checkNotNull(zzxVarZzc.zzb);
        return PackageVerificationResult.zza(callingPackage, zzxVarZzc.zzb, zzxVarZzc.zzc);
    }

    public PackageVerificationResult queryPackageSignatureVerifiedWithRetry(Context context, String callingPackage) {
        try {
            PackageVerificationResult packageVerificationResultQueryPackageSignatureVerified = queryPackageSignatureVerified(context, callingPackage);
            packageVerificationResultQueryPackageSignatureVerified.zzb();
            return packageVerificationResultQueryPackageSignatureVerified;
        } catch (SecurityException e) {
            PackageVerificationResult packageVerificationResultQueryPackageSignatureVerified2 = queryPackageSignatureVerified(context, callingPackage);
            if (!packageVerificationResultQueryPackageSignatureVerified2.zzc()) {
                return packageVerificationResultQueryPackageSignatureVerified2;
            }
            Log.e("PkgSignatureVerifier", "Got flaky result during package signature verification", e);
            return packageVerificationResultQueryPackageSignatureVerified2;
        }
    }
}
