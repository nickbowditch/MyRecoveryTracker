package com.google.android.gms.internal.location;

import android.app.PendingIntent;
import android.location.Location;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.common.api.internal.IStatusCallback;
import com.google.android.gms.common.internal.ICancelToken;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LastLocationRequest;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SleepSegmentRequest;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public final class zzn extends zza implements zzo {
    zzn(IBinder iBinder) {
        super(iBinder, "com.google.android.gms.location.internal.IGoogleLocationManagerService");
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final Location zzd() throws RemoteException {
        Parcel parcelZzb = zzb(7, zza());
        Location location = (Location) zzc.zza(parcelZzb, Location.CREATOR);
        parcelZzb.recycle();
        return location;
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final ICancelToken zze(CurrentLocationRequest currentLocationRequest, zzq zzqVar) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, currentLocationRequest);
        zzc.zze(parcelZza, zzqVar);
        Parcel parcelZzb = zzb(87, parcelZza);
        ICancelToken iCancelTokenAsInterface = ICancelToken.Stub.asInterface(parcelZzb.readStrongBinder());
        parcelZzb.recycle();
        return iCancelTokenAsInterface;
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final LocationAvailability zzf(String str) throws RemoteException {
        Parcel parcelZza = zza();
        parcelZza.writeString(str);
        Parcel parcelZzb = zzb(34, parcelZza);
        LocationAvailability locationAvailability = (LocationAvailability) zzc.zza(parcelZzb, LocationAvailability.CREATOR);
        parcelZzb.recycle();
        return locationAvailability;
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzg(GeofencingRequest geofencingRequest, PendingIntent pendingIntent, zzm zzmVar) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, geofencingRequest);
        zzc.zzd(parcelZza, pendingIntent);
        zzc.zze(parcelZza, zzmVar);
        zzc(57, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzh(LocationSettingsRequest locationSettingsRequest, zzs zzsVar, String str) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, locationSettingsRequest);
        zzc.zze(parcelZza, zzsVar);
        parcelZza.writeString(null);
        zzc(63, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzi(zzk zzkVar) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zze(parcelZza, zzkVar);
        zzc(67, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzj(LastLocationRequest lastLocationRequest, zzq zzqVar) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, lastLocationRequest);
        zzc.zze(parcelZza, zzqVar);
        zzc(82, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzk(zzdb zzdbVar, LocationRequest locationRequest, IStatusCallback iStatusCallback) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, zzdbVar);
        zzc.zzd(parcelZza, locationRequest);
        zzc.zze(parcelZza, iStatusCallback);
        zzc(88, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzl(PendingIntent pendingIntent, IStatusCallback iStatusCallback) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, pendingIntent);
        zzc.zze(parcelZza, iStatusCallback);
        zzc(73, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzm(PendingIntent pendingIntent) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, pendingIntent);
        zzc(6, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzn(PendingIntent pendingIntent, zzm zzmVar, String str) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, pendingIntent);
        zzc.zze(parcelZza, zzmVar);
        parcelZza.writeString(str);
        zzc(2, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzo(String[] strArr, zzm zzmVar, String str) throws RemoteException {
        Parcel parcelZza = zza();
        parcelZza.writeStringArray(strArr);
        zzc.zze(parcelZza, zzmVar);
        parcelZza.writeString(str);
        zzc(3, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzp(PendingIntent pendingIntent, IStatusCallback iStatusCallback) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, pendingIntent);
        zzc.zze(parcelZza, iStatusCallback);
        zzc(69, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzq(ActivityTransitionRequest activityTransitionRequest, PendingIntent pendingIntent, IStatusCallback iStatusCallback) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, activityTransitionRequest);
        zzc.zzd(parcelZza, pendingIntent);
        zzc.zze(parcelZza, iStatusCallback);
        zzc(72, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzr(long j, boolean z, PendingIntent pendingIntent) throws RemoteException {
        Parcel parcelZza = zza();
        parcelZza.writeLong(j);
        zzc.zzc(parcelZza, true);
        zzc.zzd(parcelZza, pendingIntent);
        zzc(5, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzs(com.google.android.gms.location.zzb zzbVar, PendingIntent pendingIntent, IStatusCallback iStatusCallback) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, zzbVar);
        zzc.zzd(parcelZza, pendingIntent);
        zzc.zze(parcelZza, iStatusCallback);
        zzc(70, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzt(PendingIntent pendingIntent, SleepSegmentRequest sleepSegmentRequest, IStatusCallback iStatusCallback) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, pendingIntent);
        zzc.zzd(parcelZza, sleepSegmentRequest);
        zzc.zze(parcelZza, iStatusCallback);
        zzc(79, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzu(Location location) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, location);
        zzc(13, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzv(Location location, IStatusCallback iStatusCallback) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, location);
        zzc.zze(parcelZza, iStatusCallback);
        zzc(85, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzw(boolean z) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzc(parcelZza, z);
        zzc(12, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzx(boolean z, IStatusCallback iStatusCallback) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzc(parcelZza, z);
        zzc.zze(parcelZza, iStatusCallback);
        zzc(84, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzy(zzdb zzdbVar, IStatusCallback iStatusCallback) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, zzdbVar);
        zzc.zze(parcelZza, iStatusCallback);
        zzc(89, parcelZza);
    }

    @Override // com.google.android.gms.internal.location.zzo
    public final void zzz(zzdf zzdfVar) throws RemoteException {
        Parcel parcelZza = zza();
        zzc.zzd(parcelZza, zzdfVar);
        zzc(59, parcelZza);
    }
}
