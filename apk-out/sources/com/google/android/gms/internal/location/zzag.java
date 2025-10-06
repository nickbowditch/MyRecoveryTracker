package com.google.android.gms.internal.location;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.os.RemoteException;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.internal.RemoteCall;
import com.google.android.gms.common.api.internal.StatusCallback;
import com.google.android.gms.common.api.internal.TaskApiCall;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.SleepSegmentRequest;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public final class zzag extends GoogleApi implements ActivityRecognitionClient {
    static final Api.ClientKey zza = new Api.ClientKey();
    public static final Api zzb = new Api("ActivityRecognition.API", new zzad(), zza);

    public zzag(Activity activity) {
        super(activity, (Api<Api.ApiOptions.NoOptions>) zzb, Api.ApiOptions.NO_OPTIONS, GoogleApi.Settings.DEFAULT_SETTINGS);
    }

    @Override // com.google.android.gms.location.ActivityRecognitionClient
    public final Task<Void> removeActivityTransitionUpdates(final PendingIntent pendingIntent) {
        return doWrite(TaskApiCall.builder().run(new RemoteCall() { // from class: com.google.android.gms.internal.location.zzx
            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) throws RemoteException {
                PendingIntent pendingIntent2 = pendingIntent;
                Api api = zzag.zzb;
                zzaf zzafVar = new zzaf((TaskCompletionSource) obj2);
                Preconditions.checkNotNull(zzafVar, "ResultHolder not provided.");
                ((zzo) ((zzf) obj).getService()).zzl(pendingIntent2, new StatusCallback(zzafVar));
            }
        }).setMethodKey(2406).build());
    }

    @Override // com.google.android.gms.location.ActivityRecognitionClient
    public final Task<Void> removeActivityUpdates(final PendingIntent pendingIntent) {
        return doWrite(TaskApiCall.builder().run(new RemoteCall() { // from class: com.google.android.gms.internal.location.zzy
            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) throws RemoteException {
                PendingIntent pendingIntent2 = pendingIntent;
                Api api = zzag.zzb;
                ((zzf) obj).zzp(pendingIntent2);
                ((TaskCompletionSource) obj2).setResult(null);
            }
        }).setMethodKey(2402).build());
    }

    @Override // com.google.android.gms.location.ActivityRecognitionClient
    public final Task<Void> removeSleepSegmentUpdates(final PendingIntent pendingIntent) {
        return doWrite(TaskApiCall.builder().run(new RemoteCall() { // from class: com.google.android.gms.internal.location.zzab
            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) throws RemoteException {
                PendingIntent pendingIntent2 = pendingIntent;
                Api api = zzag.zzb;
                zzaf zzafVar = new zzaf((TaskCompletionSource) obj2);
                Preconditions.checkNotNull(pendingIntent2, "PendingIntent must be specified.");
                Preconditions.checkNotNull(zzafVar, "ResultHolder not provided.");
                ((zzo) ((zzf) obj).getService()).zzp(pendingIntent2, new StatusCallback(zzafVar));
            }
        }).setMethodKey(2411).build());
    }

    @Override // com.google.android.gms.location.ActivityRecognitionClient
    public final Task<Void> requestActivityTransitionUpdates(final ActivityTransitionRequest activityTransitionRequest, final PendingIntent pendingIntent) {
        activityTransitionRequest.zza(getContextAttributionTag());
        return doWrite(TaskApiCall.builder().run(new RemoteCall() { // from class: com.google.android.gms.internal.location.zzaa
            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) throws RemoteException {
                ActivityTransitionRequest activityTransitionRequest2 = activityTransitionRequest;
                PendingIntent pendingIntent2 = pendingIntent;
                Api api = zzag.zzb;
                zzaf zzafVar = new zzaf((TaskCompletionSource) obj2);
                Preconditions.checkNotNull(activityTransitionRequest2, "activityTransitionRequest must be specified.");
                Preconditions.checkNotNull(pendingIntent2, "PendingIntent must be specified.");
                Preconditions.checkNotNull(zzafVar, "ResultHolder not provided.");
                ((zzo) ((zzf) obj).getService()).zzq(activityTransitionRequest2, pendingIntent2, new StatusCallback(zzafVar));
            }
        }).setMethodKey(2405).build());
    }

    @Override // com.google.android.gms.location.ActivityRecognitionClient
    public final Task<Void> requestActivityUpdates(long j, final PendingIntent pendingIntent) {
        com.google.android.gms.location.zza zzaVar = new com.google.android.gms.location.zza();
        zzaVar.zza(j);
        final com.google.android.gms.location.zzb zzbVarZzb = zzaVar.zzb();
        zzbVarZzb.zza(getContextAttributionTag());
        return doWrite(TaskApiCall.builder().run(new RemoteCall() { // from class: com.google.android.gms.internal.location.zzz
            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) throws RemoteException {
                com.google.android.gms.location.zzb zzbVar = zzbVarZzb;
                PendingIntent pendingIntent2 = pendingIntent;
                Api api = zzag.zzb;
                zzaf zzafVar = new zzaf((TaskCompletionSource) obj2);
                Preconditions.checkNotNull(zzbVar, "ActivityRecognitionRequest can't be null.");
                Preconditions.checkNotNull(pendingIntent2, "PendingIntent must be specified.");
                Preconditions.checkNotNull(zzafVar, "ResultHolder not provided.");
                ((zzo) ((zzf) obj).getService()).zzs(zzbVar, pendingIntent2, new StatusCallback(zzafVar));
            }
        }).setMethodKey(2401).build());
    }

    @Override // com.google.android.gms.location.ActivityRecognitionClient
    public final Task<Void> requestSleepSegmentUpdates(final PendingIntent pendingIntent, final SleepSegmentRequest sleepSegmentRequest) {
        Preconditions.checkNotNull(pendingIntent, "PendingIntent must be specified.");
        return doRead(TaskApiCall.builder().run(new RemoteCall() { // from class: com.google.android.gms.internal.location.zzac
            @Override // com.google.android.gms.common.api.internal.RemoteCall
            public final void accept(Object obj, Object obj2) throws RemoteException {
                zzag zzagVar = this.zza;
                ((zzo) ((zzf) obj).getService()).zzt(pendingIntent, sleepSegmentRequest, new zzae(zzagVar, (TaskCompletionSource) obj2));
            }
        }).setFeatures(com.google.android.gms.location.zzm.zzb).setMethodKey(2410).build());
    }

    public zzag(Context context) {
        super(context, (Api<Api.ApiOptions.NoOptions>) zzb, Api.ApiOptions.NO_OPTIONS, GoogleApi.Settings.DEFAULT_SETTINGS);
    }
}
