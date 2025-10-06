package com.google.android.gms.internal.location;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;
import android.os.RemoteException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.internal.BaseImplementation;
import com.google.android.gms.common.api.internal.ListenerHolders;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public final class zzau implements FusedLocationProviderApi {
    static /* bridge */ /* synthetic */ TaskCompletionSource zza(final BaseImplementation.ResultHolder resultHolder) {
        TaskCompletionSource taskCompletionSource = new TaskCompletionSource();
        taskCompletionSource.getTask().addOnCompleteListener(new OnCompleteListener() { // from class: com.google.android.gms.internal.location.zzah
            @Override // com.google.android.gms.tasks.OnCompleteListener
            public final void onComplete(Task task) {
                BaseImplementation.ResultHolder resultHolder2 = resultHolder;
                if (task.isSuccessful()) {
                    resultHolder2.setResult(Status.RESULT_SUCCESS);
                    return;
                }
                if (task.isCanceled()) {
                    resultHolder2.setFailedResult(Status.RESULT_CANCELED);
                    return;
                }
                Exception exception = task.getException();
                if (exception instanceof ApiException) {
                    resultHolder2.setFailedResult(((ApiException) exception).getStatus());
                } else {
                    resultHolder2.setFailedResult(Status.RESULT_INTERNAL_ERROR);
                }
            }
        });
        return taskCompletionSource;
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> flushLocations(GoogleApiClient googleApiClient) {
        return googleApiClient.execute(new zzaj(this, googleApiClient));
    }

    /* JADX WARN: Removed duplicated region for block: B:27:0x0073  */
    @Override // com.google.android.gms.location.FusedLocationProviderApi
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final android.location.Location getLastLocation(com.google.android.gms.common.api.GoogleApiClient r11) throws java.lang.Throwable {
        /*
            r10 = this;
            r0 = 0
            r1 = 1
            if (r11 == 0) goto L6
            r2 = r1
            goto L7
        L6:
            r2 = r0
        L7:
            java.lang.String r3 = "GoogleApiClient parameter is required."
            com.google.android.gms.common.internal.Preconditions.checkArgument(r2, r3)
            com.google.android.gms.common.api.Api$ClientKey r2 = com.google.android.gms.internal.location.zzbp.zza
            com.google.android.gms.common.api.Api$Client r11 = r11.getClient(r2)
            com.google.android.gms.internal.location.zzda r11 = (com.google.android.gms.internal.location.zzda) r11
            java.util.concurrent.atomic.AtomicReference r2 = new java.util.concurrent.atomic.AtomicReference
            r2.<init>()
            java.util.concurrent.CountDownLatch r3 = new java.util.concurrent.CountDownLatch
            r3.<init>(r1)
            com.google.android.gms.tasks.TaskCompletionSource r4 = new com.google.android.gms.tasks.TaskCompletionSource
            r4.<init>()
            r5 = 0
            com.google.android.gms.location.LastLocationRequest$Builder r6 = new com.google.android.gms.location.LastLocationRequest$Builder     // Catch: java.lang.Exception -> L7b
            r6.<init>()     // Catch: java.lang.Exception -> L7b
            com.google.android.gms.location.LastLocationRequest r6 = r6.build()     // Catch: java.lang.Exception -> L7b
            r11.zzt(r6, r4)     // Catch: java.lang.Exception -> L7b
            com.google.android.gms.tasks.Task r11 = r4.getTask()
            com.google.android.gms.internal.location.zzai r4 = new com.google.android.gms.internal.location.zzai
            r4.<init>()
            r11.addOnCompleteListener(r4)
            java.util.concurrent.TimeUnit r11 = java.util.concurrent.TimeUnit.SECONDS
            r6 = 30
            long r6 = r11.toNanos(r6)     // Catch: java.lang.Throwable -> L70
            long r8 = java.lang.System.nanoTime()     // Catch: java.lang.Throwable -> L70
            long r8 = r8 + r6
        L49:
            java.util.concurrent.TimeUnit r11 = java.util.concurrent.TimeUnit.NANOSECONDS     // Catch: java.lang.Throwable -> L62 java.lang.InterruptedException -> L64
            boolean r11 = r3.await(r6, r11)     // Catch: java.lang.Throwable -> L62 java.lang.InterruptedException -> L64
            if (r0 == 0) goto L58
            java.lang.Thread r0 = java.lang.Thread.currentThread()
            r0.interrupt()
        L58:
            if (r11 == 0) goto L61
            java.lang.Object r11 = r2.get()
            android.location.Location r11 = (android.location.Location) r11
            return r11
        L61:
            return r5
        L62:
            r11 = move-exception
            goto L71
        L64:
            r11 = move-exception
            long r6 = java.lang.System.nanoTime()     // Catch: java.lang.Throwable -> L6d
            long r6 = r8 - r6
            r0 = r1
            goto L49
        L6d:
            r11 = move-exception
            r0 = r1
            goto L71
        L70:
            r11 = move-exception
        L71:
            if (r0 == 0) goto L7a
            java.lang.Thread r0 = java.lang.Thread.currentThread()
            r0.interrupt()
        L7a:
            throw r11
        L7b:
            r11 = move-exception
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.location.zzau.getLastLocation(com.google.android.gms.common.api.GoogleApiClient):android.location.Location");
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final LocationAvailability getLocationAvailability(GoogleApiClient googleApiClient) {
        Preconditions.checkArgument(googleApiClient != null, "GoogleApiClient parameter is required.");
        try {
            return ((zzda) googleApiClient.getClient(zzbp.zza)).zzp();
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> removeLocationUpdates(GoogleApiClient googleApiClient, PendingIntent pendingIntent) {
        return googleApiClient.execute(new zzao(this, googleApiClient, pendingIntent));
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> requestLocationUpdates(GoogleApiClient googleApiClient, LocationRequest locationRequest, PendingIntent pendingIntent) {
        return googleApiClient.execute(new zzam(this, googleApiClient, pendingIntent, locationRequest));
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> setMockLocation(GoogleApiClient googleApiClient, Location location) {
        return googleApiClient.execute(new zzar(this, googleApiClient, location));
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> setMockMode(GoogleApiClient googleApiClient, boolean z) {
        return googleApiClient.execute(new zzaq(this, googleApiClient, z));
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> removeLocationUpdates(GoogleApiClient googleApiClient, LocationCallback locationCallback) {
        return googleApiClient.execute(new zzap(this, googleApiClient, locationCallback));
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> requestLocationUpdates(GoogleApiClient googleApiClient, LocationRequest locationRequest, LocationCallback locationCallback, Looper looper) {
        if (looper == null) {
            looper = Looper.myLooper();
            Preconditions.checkNotNull(looper, "invalid null looper");
        }
        return googleApiClient.execute(new zzal(this, googleApiClient, ListenerHolders.createListenerHolder(locationCallback, looper, LocationCallback.class.getSimpleName()), locationRequest));
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> removeLocationUpdates(GoogleApiClient googleApiClient, LocationListener locationListener) {
        return googleApiClient.execute(new zzan(this, googleApiClient, locationListener));
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> requestLocationUpdates(GoogleApiClient googleApiClient, LocationRequest locationRequest, LocationListener locationListener) {
        Looper looperMyLooper = Looper.myLooper();
        Preconditions.checkNotNull(looperMyLooper, "invalid null looper");
        return googleApiClient.execute(new zzak(this, googleApiClient, ListenerHolders.createListenerHolder(locationListener, looperMyLooper, LocationListener.class.getSimpleName()), locationRequest));
    }

    @Override // com.google.android.gms.location.FusedLocationProviderApi
    public final PendingResult<Status> requestLocationUpdates(GoogleApiClient googleApiClient, LocationRequest locationRequest, LocationListener locationListener, Looper looper) {
        if (looper == null) {
            looper = Looper.myLooper();
            Preconditions.checkNotNull(looper, "invalid null looper");
        }
        return googleApiClient.execute(new zzak(this, googleApiClient, ListenerHolders.createListenerHolder(locationListener, looper, LocationListener.class.getSimpleName()), locationRequest));
    }
}
