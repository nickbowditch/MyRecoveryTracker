package com.google.android.gms.common.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.HandlerThread;
import java.util.concurrent.Executor;

/* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
/* loaded from: classes.dex */
public abstract class GmsClientSupervisor {
    static HandlerThread zza;
    private static zzr zzd;
    private static int zzb = 4225;
    private static final Object zzc = new Object();
    private static boolean zze = false;

    public static int getDefaultBindFlags() {
        return zzb;
    }

    public static GmsClientSupervisor getInstance(Context context) {
        synchronized (zzc) {
            if (zzd == null) {
                zzd = new zzr(context.getApplicationContext(), zze ? getOrStartHandlerThread().getLooper() : context.getMainLooper());
            }
        }
        return zzd;
    }

    public static HandlerThread getOrStartHandlerThread() {
        synchronized (zzc) {
            HandlerThread handlerThread = zza;
            if (handlerThread != null) {
                return handlerThread;
            }
            zza = new HandlerThread("GoogleApiHandler", 9);
            zza.start();
            return zza;
        }
    }

    public static void setUseHandlerThreadForCallbacks() {
        synchronized (zzc) {
            zzr zzrVar = zzd;
            if (zzrVar != null && !zze) {
                zzrVar.zzi(getOrStartHandlerThread().getLooper());
            }
            zze = true;
        }
    }

    public boolean bindService(ComponentName componentName, ServiceConnection connection, String realClientName) {
        return zzc(new zzn(componentName, getDefaultBindFlags()), connection, realClientName, null);
    }

    public void unbindService(ComponentName componentName, ServiceConnection connection, String realClientName) {
        zza(new zzn(componentName, getDefaultBindFlags()), connection, realClientName);
    }

    protected abstract void zza(zzn zznVar, ServiceConnection serviceConnection, String str);

    public final void zzb(String str, String str2, int i, ServiceConnection serviceConnection, String str3, boolean z) {
        zza(new zzn(str, str2, i, z), serviceConnection, str3);
    }

    protected abstract boolean zzc(zzn zznVar, ServiceConnection serviceConnection, String str, Executor executor);

    public boolean bindService(String startServiceAction, ServiceConnection connection, String realClientName) {
        return zzc(new zzn(startServiceAction, getDefaultBindFlags(), false), connection, realClientName, null);
    }

    public void unbindService(String startServiceAction, ServiceConnection connection, String realClientName) {
        zza(new zzn(startServiceAction, getDefaultBindFlags(), false), connection, realClientName);
    }
}
