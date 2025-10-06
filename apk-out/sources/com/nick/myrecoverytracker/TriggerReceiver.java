package com.nick.myrecoverytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: TriggerReceiver.kt */
@Metadata(d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \n2\u00020\u0001:\u0001\nB\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u001a\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016¨\u0006\u000b"}, d2 = {"Lcom/nick/myrecoverytracker/TriggerReceiver;", "Landroid/content/BroadcastReceiver;", "<init>", "()V", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class TriggerReceiver extends BroadcastReceiver {
    public static final String ACTION_RUN_ALL_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ALL_ROLLUPS";
    public static final String ACTION_RUN_ENGAGEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_ENGAGEMENT_ROLLUP";
    public static final String ACTION_RUN_HEALTH_SNAPSHOT = "com.nick.myrecoverytracker.ACTION_RUN_HEALTH_SNAPSHOT";
    public static final String ACTION_RUN_LNS_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_LNS_ROLLUP";
    public static final String ACTION_RUN_MOVEMENT_INTENSITY = "com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_INTENSITY";
    public static final String ACTION_RUN_MOVEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_MOVEMENT_ROLLUP";
    public static final String ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP";
    public static final String ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP";
    public static final String ACTION_RUN_NOTIFICATION_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_ROLLUP";
    public static final String ACTION_RUN_NOTIFICATION_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_NOTIFICATION_VALIDATION";
    public static final String ACTION_RUN_NOTIF_ENGAGEMENT_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_NOTIF_ENGAGEMENT_ROLLUP";
    public static final String ACTION_RUN_REDCAP_UPLOAD = "com.nick.myrecoverytracker.ACTION_RUN_REDCAP_UPLOAD";
    public static final String ACTION_RUN_ROLLUPS = "com.nick.myrecoverytracker.ACTION_RUN_ROLLUPS";
    public static final String ACTION_RUN_SLEEP_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_SLEEP_ROLLUP";
    public static final String ACTION_RUN_SLEEP_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_SLEEP_VALIDATION";
    public static final String ACTION_RUN_UNLOCK_ROLLUP = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_ROLLUP";
    public static final String ACTION_RUN_UNLOCK_SCAN = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_SCAN";
    public static final String ACTION_RUN_UNLOCK_VALIDATION = "com.nick.myrecoverytracker.ACTION_RUN_UNLOCK_VALIDATION";
    public static final String ACTION_RUN_USAGE_CAPTURE = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_CAPTURE";
    public static final String ACTION_RUN_USAGE_ENTROPY = "com.nick.myrecoverytracker.ACTION_RUN_USAGE_ENTROPY";
    public static final String ACTION_VERIFY_SLEEP_RESCHEDULE = "com.nick.myrecoverytracker.ACTION_VERIFY_SLEEP_RESCHEDULE";

    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue
    java.lang.NullPointerException: Cannot invoke "java.util.List.iterator()" because the return value of "jadx.core.dex.visitors.regions.SwitchOverStringVisitor$SwitchData.getNewCases()" is null
    	at jadx.core.dex.visitors.regions.SwitchOverStringVisitor.restoreSwitchOverString(SwitchOverStringVisitor.java:109)
    	at jadx.core.dex.visitors.regions.SwitchOverStringVisitor.visitRegion(SwitchOverStringVisitor.java:66)
    	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:77)
    	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:82)
     */
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action;
        Intrinsics.checkNotNullParameter(context, "context");
        if (intent == null || (action = intent.getAction()) == null) {
            return;
        }
        Log.i("TriggerReceiver", "onReceive action=" + action);
        switch (action.hashCode()) {
            case -1984268308:
                if (action.equals(ACTION_RUN_UNLOCK_VALIDATION)) {
                    OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UnlockValidationWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("UnlockValidation").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-UnlockValidation", ExistingWorkPolicy.REPLACE, req);
                    break;
                }
                break;
            case -1699441428:
                if (!action.equals(ACTION_RUN_NOTIFICATION_ENGAGEMENT_ROLLUP)) {
                }
                Log.i("TriggerReceiver", "Enqueue NotificationEngagementWorker (once-EngagementRollup)");
                OneTimeWorkRequest engReq = new OneTimeWorkRequest.Builder(NotificationEngagementWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationEngagement").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-EngagementRollup", ExistingWorkPolicy.REPLACE, engReq);
                Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)");
                OneTimeWorkRequest latencyReq = new OneTimeWorkRequest.Builder(NotificationLatencyWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationLatency").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, latencyReq);
                break;
            case -1550154965:
                if (action.equals(ACTION_RUN_USAGE_ENTROPY)) {
                    OneTimeWorkRequest req2 = new OneTimeWorkRequest.Builder(UsageEntropyDailyWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("UsageEntropy").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-UsageEntropy", ExistingWorkPolicy.REPLACE, req2);
                    break;
                }
                break;
            case -1525855029:
                if (action.equals(ACTION_RUN_UNLOCK_ROLLUP)) {
                    Log.i("TriggerReceiver", "Enqueue UnlockRollupWorker (once-UnlockRollup)");
                    OneTimeWorkRequest req3 = new OneTimeWorkRequest.Builder(UnlockRollupWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("UnlockRollup").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-UnlockRollup", ExistingWorkPolicy.REPLACE, req3);
                    break;
                }
                break;
            case -1450094736:
                if (!action.equals(ACTION_RUN_ENGAGEMENT_ROLLUP)) {
                }
                Log.i("TriggerReceiver", "Enqueue NotificationEngagementWorker (once-EngagementRollup)");
                OneTimeWorkRequest engReq2 = new OneTimeWorkRequest.Builder(NotificationEngagementWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationEngagement").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-EngagementRollup", ExistingWorkPolicy.REPLACE, engReq2);
                Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)");
                OneTimeWorkRequest latencyReq2 = new OneTimeWorkRequest.Builder(NotificationLatencyWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationLatency").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, latencyReq2);
                break;
            case -1261155132:
                if (action.equals(ACTION_VERIFY_SLEEP_RESCHEDULE)) {
                    OneTimeWorkRequest req4 = new OneTimeWorkRequest.Builder(SleepValidationWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("SleepValidation").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("SleepRollup", ExistingWorkPolicy.KEEP, req4);
                    break;
                }
                break;
            case -656156159:
                if (!action.equals(ACTION_RUN_NOTIF_ENGAGEMENT_ROLLUP)) {
                }
                Log.i("TriggerReceiver", "Enqueue NotificationEngagementWorker (once-EngagementRollup)");
                OneTimeWorkRequest engReq22 = new OneTimeWorkRequest.Builder(NotificationEngagementWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationEngagement").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-EngagementRollup", ExistingWorkPolicy.REPLACE, engReq22);
                Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)");
                OneTimeWorkRequest latencyReq22 = new OneTimeWorkRequest.Builder(NotificationLatencyWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationLatency").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, latencyReq22);
                break;
            case -534756540:
                if (!action.equals(ACTION_RUN_NOTIFICATION_ROLLUP)) {
                }
                Log.i("TriggerReceiver", "Enqueue NotificationEngagementWorker (once-EngagementRollup)");
                OneTimeWorkRequest engReq222 = new OneTimeWorkRequest.Builder(NotificationEngagementWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationEngagement").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-EngagementRollup", ExistingWorkPolicy.REPLACE, engReq222);
                Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)");
                OneTimeWorkRequest latencyReq222 = new OneTimeWorkRequest.Builder(NotificationLatencyWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationLatency").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, latencyReq222);
                break;
            case -435740193:
                if (action.equals(ACTION_RUN_HEALTH_SNAPSHOT)) {
                    OneTimeWorkRequest req5 = new OneTimeWorkRequest.Builder(HealthSnapshotWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("HealthSnapshot").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-HealthSnapshot", ExistingWorkPolicy.REPLACE, req5);
                    break;
                }
                break;
            case -169016341:
                if (action.equals(ACTION_RUN_MOVEMENT_INTENSITY)) {
                    Log.i("TriggerReceiver", "Enqueue MovementIntensityDailyWorker (once-MovementIntensity)");
                    OneTimeWorkRequest req6 = new OneTimeWorkRequest.Builder(MovementIntensityDailyWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("MovementIntensity").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-MovementIntensity", ExistingWorkPolicy.REPLACE, req6);
                    break;
                }
                break;
            case -101636667:
                if (action.equals(ACTION_RUN_NOTIFICATION_LATENCY_ROLLUP)) {
                    Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)");
                    OneTimeWorkRequest req7 = new OneTimeWorkRequest.Builder(NotificationLatencyWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationLatency").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, req7);
                    break;
                }
                break;
            case -86485968:
                if (action.equals(ACTION_RUN_UNLOCK_SCAN)) {
                    UnlockMigrations.INSTANCE.run(context);
                    OneTimeWorkRequest req8 = new OneTimeWorkRequest.Builder(UnlockWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("UnlockScan").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-UnlockScan", ExistingWorkPolicy.REPLACE, req8);
                    break;
                }
                break;
            case 6283208:
                if (action.equals(ACTION_RUN_SLEEP_ROLLUP)) {
                    OneTimeWorkRequest req9 = new OneTimeWorkRequest.Builder(SleepRollupWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("SleepRollup").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-SleepRollup", ExistingWorkPolicy.REPLACE, req9);
                    break;
                }
                break;
            case 56706153:
                if (action.equals(ACTION_RUN_SLEEP_VALIDATION)) {
                    OneTimeWorkRequest req10 = new OneTimeWorkRequest.Builder(SleepValidationWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("SleepValidation").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-SleepValidation", ExistingWorkPolicy.REPLACE, req10);
                    break;
                }
                break;
            case 230456405:
                if (!action.equals(ACTION_RUN_ALL_ROLLUPS)) {
                }
                UnlockMigrations.INSTANCE.run(context);
                OneTimeWorkRequest unlockReq = new OneTimeWorkRequest.Builder(UnlockWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("UnlockScan").build();
                OneTimeWorkRequest sleepReq = new OneTimeWorkRequest.Builder(SleepRollupWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("SleepRollup").build();
                OneTimeWorkRequest uploadReq = new OneTimeWorkRequest.Builder(RedcapUploadWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("RedcapUpload").build();
                WorkManager.getInstance(context).beginUniqueWork("rollups-and-upload", ExistingWorkPolicy.REPLACE, unlockReq).then(sleepReq).then(uploadReq).enqueue();
                Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)");
                OneTimeWorkRequest latencyReq3 = new OneTimeWorkRequest.Builder(NotificationLatencyWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationLatency").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, latencyReq3);
                break;
            case 357681920:
                if (action.equals(ACTION_RUN_MOVEMENT_ROLLUP)) {
                    Log.i("TriggerReceiver", "Enqueue MovementRollupWorker (once-MovementRollup)");
                    OneTimeWorkRequest req11 = new OneTimeWorkRequest.Builder(MovementRollupWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("MovementRollup").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-MovementRollup", ExistingWorkPolicy.REPLACE, req11);
                    break;
                }
                break;
            case 376403219:
                if (!action.equals(ACTION_RUN_ROLLUPS)) {
                }
                UnlockMigrations.INSTANCE.run(context);
                OneTimeWorkRequest unlockReq2 = new OneTimeWorkRequest.Builder(UnlockWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("UnlockScan").build();
                OneTimeWorkRequest sleepReq2 = new OneTimeWorkRequest.Builder(SleepRollupWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("SleepRollup").build();
                OneTimeWorkRequest uploadReq2 = new OneTimeWorkRequest.Builder(RedcapUploadWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("RedcapUpload").build();
                WorkManager.getInstance(context).beginUniqueWork("rollups-and-upload", ExistingWorkPolicy.REPLACE, unlockReq2).then(sleepReq2).then(uploadReq2).enqueue();
                Log.i("TriggerReceiver", "Enqueue NotificationLatencyWorker (once-NotificationLatencyRollup)");
                OneTimeWorkRequest latencyReq32 = new OneTimeWorkRequest.Builder(NotificationLatencyWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationLatency").build();
                WorkManager.getInstance(context).enqueueUniqueWork("once-NotificationLatencyRollup", ExistingWorkPolicy.REPLACE, latencyReq32);
                break;
            case 593997312:
                if (action.equals(ACTION_RUN_USAGE_CAPTURE)) {
                    Log.i("TriggerReceiver", "Enqueue UsageCaptureWorker (once-UsageCapture)");
                    OneTimeWorkRequest req12 = new OneTimeWorkRequest.Builder(UsageCaptureWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("UsageCapture").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-UsageCapture", ExistingWorkPolicy.REPLACE, req12);
                    break;
                }
                break;
            case 1165750318:
                if (action.equals(ACTION_RUN_LNS_ROLLUP)) {
                    Log.i("TriggerReceiver", "Enqueue LateNightScreenRollupWorker (once-LateNightRollup)");
                    OneTimeWorkRequest req13 = new OneTimeWorkRequest.Builder(LateNightScreenRollupWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("LateNightRollup").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-LateNightRollup", ExistingWorkPolicy.REPLACE, req13);
                    break;
                }
                break;
            case 1572352503:
                if (action.equals(ACTION_RUN_REDCAP_UPLOAD)) {
                    Log.i("TriggerReceiver", "Enqueue RedcapUploadWorker (once-RedcapUpload)");
                    OneTimeWorkRequest req14 = new OneTimeWorkRequest.Builder(RedcapUploadWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("RedcapUpload").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-RedcapUpload", ExistingWorkPolicy.REPLACE, req14);
                    break;
                }
                break;
            case 2097908197:
                if (action.equals(ACTION_RUN_NOTIFICATION_VALIDATION)) {
                    Log.i("TriggerReceiver", "Enqueue NotificationValidationWorker (once-NotificationValidation)");
                    OneTimeWorkRequest req15 = new OneTimeWorkRequest.Builder(NotificationValidationWorker.class).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag("NotificationValidation").build();
                    WorkManager.getInstance(context).enqueueUniqueWork("once-NotificationValidation", ExistingWorkPolicy.REPLACE, req15);
                    break;
                }
                break;
        }
    }
}
