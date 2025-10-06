package androidx.work.impl.background.systemjob;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.PersistableBundle;
import androidx.work.BackoffPolicy;
import androidx.work.Clock;
import androidx.work.Constraints;
import androidx.work.Logger;
import androidx.work.NetworkType;
import androidx.work.impl.model.WorkSpec;

/* loaded from: classes.dex */
class SystemJobInfoConverter {
    static final String EXTRA_IS_PERIODIC = "EXTRA_IS_PERIODIC";
    static final String EXTRA_WORK_SPEC_GENERATION = "EXTRA_WORK_SPEC_GENERATION";
    static final String EXTRA_WORK_SPEC_ID = "EXTRA_WORK_SPEC_ID";
    private static final String TAG = Logger.tagWithPrefix("SystemJobInfoConverter");
    private final Clock mClock;
    private final ComponentName mWorkServiceComponent;

    SystemJobInfoConverter(Context context, Clock clock) {
        this.mClock = clock;
        Context appContext = context.getApplicationContext();
        this.mWorkServiceComponent = new ComponentName(appContext, (Class<?>) SystemJobService.class);
    }

    JobInfo convert(WorkSpec workSpec, int jobId) {
        Constraints constraints = workSpec.constraints;
        PersistableBundle extras = new PersistableBundle();
        extras.putString(EXTRA_WORK_SPEC_ID, workSpec.id);
        extras.putInt(EXTRA_WORK_SPEC_GENERATION, workSpec.getGeneration());
        extras.putBoolean(EXTRA_IS_PERIODIC, workSpec.isPeriodic());
        JobInfo.Builder builder = new JobInfo.Builder(jobId, this.mWorkServiceComponent).setRequiresCharging(constraints.getRequiresCharging()).setRequiresDeviceIdle(constraints.getRequiresDeviceIdle()).setExtras(extras);
        setRequiredNetwork(builder, constraints.getRequiredNetworkType());
        if (!constraints.getRequiresDeviceIdle()) {
            int backoffPolicy = workSpec.backoffPolicy == BackoffPolicy.LINEAR ? 0 : 1;
            builder.setBackoffCriteria(workSpec.backoffDelayDuration, backoffPolicy);
        }
        long nextRunTime = workSpec.calculateNextRunTime();
        long now = this.mClock.currentTimeMillis();
        long offset = Math.max(nextRunTime - now, 0L);
        if (Build.VERSION.SDK_INT <= 28 || offset > 0) {
            builder.setMinimumLatency(offset);
        } else if (!workSpec.expedited) {
            builder.setImportantWhileForeground(true);
        }
        if (constraints.hasContentUriTriggers()) {
            for (Constraints.ContentUriTrigger trigger : constraints.getContentUriTriggers()) {
                builder.addTriggerContentUri(convertContentUriTrigger(trigger));
            }
            builder.setTriggerContentUpdateDelay(constraints.getContentTriggerUpdateDelayMillis());
            builder.setTriggerContentMaxDelay(constraints.getContentTriggerMaxDelayMillis());
        }
        builder.setPersisted(false);
        if (Build.VERSION.SDK_INT >= 26) {
            builder.setRequiresBatteryNotLow(constraints.getRequiresBatteryNotLow());
            builder.setRequiresStorageNotLow(constraints.getRequiresStorageNotLow());
        }
        boolean isRetry = workSpec.runAttemptCount > 0;
        boolean isDelayed = offset > 0;
        if (Build.VERSION.SDK_INT >= 31 && workSpec.expedited && !isRetry && !isDelayed) {
            builder.setExpedited(true);
        }
        return builder.build();
    }

    private static JobInfo.TriggerContentUri convertContentUriTrigger(Constraints.ContentUriTrigger trigger) {
        int flag = trigger.getIsTriggeredForDescendants() ? 1 : 0;
        return new JobInfo.TriggerContentUri(trigger.getUri(), flag);
    }

    static void setRequiredNetwork(JobInfo.Builder builder, NetworkType networkType) {
        if (Build.VERSION.SDK_INT >= 30 && networkType == NetworkType.TEMPORARILY_UNMETERED) {
            NetworkRequest networkRequest = new NetworkRequest.Builder().addCapability(25).build();
            builder.setRequiredNetwork(networkRequest);
        } else {
            builder.setRequiredNetworkType(convertNetworkType(networkType));
        }
    }

    static int convertNetworkType(NetworkType networkType) {
        switch (networkType) {
            case NOT_REQUIRED:
                return 0;
            case CONNECTED:
                return 1;
            case UNMETERED:
                return 2;
            case NOT_ROAMING:
                return 3;
            case METERED:
                if (Build.VERSION.SDK_INT >= 26) {
                    return 4;
                }
                break;
        }
        Logger.get().debug(TAG, "API version too low. Cannot convert network type value " + networkType);
        return 1;
    }
}
