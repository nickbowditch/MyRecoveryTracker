package androidx.work.impl.utils;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.ApplicationExitInfo;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteAccessPermException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteTableLockedException;
import android.os.Build;
import android.text.TextUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.os.UserManagerCompat;
import androidx.core.util.Consumer;
import androidx.work.Configuration;
import androidx.work.Logger;
import androidx.work.WorkInfo;
import androidx.work.impl.Schedulers;
import androidx.work.impl.WorkDatabase;
import androidx.work.impl.WorkDatabasePathHelper;
import androidx.work.impl.WorkManagerImpl;
import androidx.work.impl.background.systemjob.SystemJobScheduler;
import androidx.work.impl.model.WorkProgressDao;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.model.WorkSpecDao;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class ForceStopRunnable implements Runnable {
    static final String ACTION_FORCE_STOP_RESCHEDULE = "ACTION_FORCE_STOP_RESCHEDULE";
    private static final int ALARM_ID = -1;
    private static final long BACKOFF_DURATION_MS = 300;
    static final int MAX_ATTEMPTS = 3;
    private static final String TAG = Logger.tagWithPrefix("ForceStopRunnable");
    private static final long TEN_YEARS = TimeUnit.DAYS.toMillis(3650);
    private final Context mContext;
    private final PreferenceUtils mPreferenceUtils;
    private int mRetryCount = 0;
    private final WorkManagerImpl mWorkManager;

    public ForceStopRunnable(Context context, WorkManagerImpl workManager) {
        this.mContext = context.getApplicationContext();
        this.mWorkManager = workManager;
        this.mPreferenceUtils = workManager.getPreferenceUtils();
    }

    @Override // java.lang.Runnable
    public void run() {
        String message;
        try {
            if (!multiProcessChecks()) {
                return;
            }
            while (true) {
                try {
                    WorkDatabasePathHelper.migrateDatabase(this.mContext);
                    Logger.get().debug(TAG, "Performing cleanup operations.");
                    try {
                        forceStopRunnable();
                        break;
                    } catch (SQLiteAccessPermException | SQLiteCantOpenDatabaseException | SQLiteConstraintException | SQLiteDatabaseCorruptException | SQLiteDatabaseLockedException | SQLiteDiskIOException | SQLiteTableLockedException exception) {
                        this.mRetryCount++;
                        if (this.mRetryCount >= 3) {
                            if (UserManagerCompat.isUserUnlocked(this.mContext)) {
                                message = "The file system on the device is in a bad state. WorkManager cannot access the app's internal data store.";
                            } else {
                                message = "WorkManager can't be accessed from direct boot, because credential encrypted storage isn't accessible.\nDon't access or initialise WorkManager from directAware components. See https://developer.android.com/training/articles/direct-boot";
                            }
                            Logger.get().error(TAG, message, exception);
                            IllegalStateException throwable = new IllegalStateException(message, exception);
                            Consumer<Throwable> exceptionHandler = this.mWorkManager.getConfiguration().getInitializationExceptionHandler();
                            if (exceptionHandler != null) {
                                Logger.get().debug(TAG, "Routing exception to the specified exception handler", throwable);
                                exceptionHandler.accept(throwable);
                            } else {
                                throw throwable;
                            }
                        } else {
                            long duration = this.mRetryCount * BACKOFF_DURATION_MS;
                            Logger.get().debug(TAG, "Retrying after " + duration, exception);
                            sleep(this.mRetryCount * BACKOFF_DURATION_MS);
                        }
                    }
                    long duration2 = this.mRetryCount * BACKOFF_DURATION_MS;
                    Logger.get().debug(TAG, "Retrying after " + duration2, exception);
                    sleep(this.mRetryCount * BACKOFF_DURATION_MS);
                } catch (SQLiteException sqLiteException) {
                    Logger.get().error(TAG, "Unexpected SQLite exception during migrations");
                    IllegalStateException exception2 = new IllegalStateException("Unexpected SQLite exception during migrations", sqLiteException);
                    Consumer<Throwable> exceptionHandler2 = this.mWorkManager.getConfiguration().getInitializationExceptionHandler();
                    if (exceptionHandler2 != null) {
                        exceptionHandler2.accept(exception2);
                    } else {
                        throw exception2;
                    }
                }
            }
        } finally {
            this.mWorkManager.onForceStopRunnableCompleted();
        }
    }

    public boolean isForceStopped() {
        try {
            int flags = Build.VERSION.SDK_INT >= 31 ? 536870912 | 33554432 : 536870912;
            PendingIntent pendingIntent = getPendingIntent(this.mContext, flags);
            if (Build.VERSION.SDK_INT >= 30) {
                if (pendingIntent != null) {
                    pendingIntent.cancel();
                }
                ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
                List<ApplicationExitInfo> exitInfoList = activityManager.getHistoricalProcessExitReasons(null, 0, 0);
                if (exitInfoList != null && !exitInfoList.isEmpty()) {
                    long timestamp = this.mPreferenceUtils.getLastForceStopEventMillis();
                    for (int i = 0; i < exitInfoList.size(); i++) {
                        ApplicationExitInfo info = exitInfoList.get(i);
                        if (info.getReason() == 10 && info.getTimestamp() >= timestamp) {
                            return true;
                        }
                    }
                }
            } else if (pendingIntent == null) {
                setAlarm(this.mContext);
                return true;
            }
            return false;
        } catch (IllegalArgumentException | SecurityException exception) {
            Logger.get().warning(TAG, "Ignoring exception", exception);
            return true;
        }
    }

    public void forceStopRunnable() {
        boolean needsScheduling = cleanUp();
        if (shouldRescheduleWorkers()) {
            Logger.get().debug(TAG, "Rescheduling Workers.");
            this.mWorkManager.rescheduleEligibleWork();
            this.mWorkManager.getPreferenceUtils().setNeedsReschedule(false);
        } else if (isForceStopped()) {
            Logger.get().debug(TAG, "Application was force-stopped, rescheduling.");
            this.mWorkManager.rescheduleEligibleWork();
            this.mPreferenceUtils.setLastForceStopEventMillis(this.mWorkManager.getConfiguration().getClock().currentTimeMillis());
        } else if (needsScheduling) {
            Logger.get().debug(TAG, "Found unfinished work, scheduling it.");
            Schedulers.schedule(this.mWorkManager.getConfiguration(), this.mWorkManager.getWorkDatabase(), this.mWorkManager.getSchedulers());
        }
    }

    public boolean cleanUp() {
        boolean needsReconciling = SystemJobScheduler.reconcileJobs(this.mContext, this.mWorkManager.getWorkDatabase());
        WorkDatabase workDatabase = this.mWorkManager.getWorkDatabase();
        WorkSpecDao workSpecDao = workDatabase.workSpecDao();
        WorkProgressDao workProgressDao = workDatabase.workProgressDao();
        workDatabase.beginTransaction();
        try {
            List<WorkSpec> workSpecs = workSpecDao.getRunningWork();
            boolean needsScheduling = (workSpecs == null || workSpecs.isEmpty()) ? false : true;
            if (needsScheduling) {
                for (WorkSpec workSpec : workSpecs) {
                    workSpecDao.setState(WorkInfo.State.ENQUEUED, workSpec.id);
                    workSpecDao.setStopReason(workSpec.id, WorkInfo.STOP_REASON_UNKNOWN);
                    workSpecDao.markWorkSpecScheduled(workSpec.id, -1L);
                }
            }
            workProgressDao.deleteAll();
            workDatabase.setTransactionSuccessful();
            return needsScheduling || needsReconciling;
        } finally {
            workDatabase.endTransaction();
        }
    }

    public boolean shouldRescheduleWorkers() {
        return this.mWorkManager.getPreferenceUtils().getNeedsReschedule();
    }

    public boolean multiProcessChecks() {
        Configuration configuration = this.mWorkManager.getConfiguration();
        if (TextUtils.isEmpty(configuration.getDefaultProcessName())) {
            Logger.get().debug(TAG, "The default process name was not specified.");
            return true;
        }
        boolean isDefaultProcess = ProcessUtils.isDefaultProcess(this.mContext, configuration);
        Logger.get().debug(TAG, "Is default app process = " + isDefaultProcess);
        return isDefaultProcess;
    }

    public void sleep(long duration) throws InterruptedException {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
        }
    }

    private static PendingIntent getPendingIntent(Context context, int flags) {
        Intent intent = getIntent(context);
        return PendingIntent.getBroadcast(context, -1, intent, flags);
    }

    static Intent getIntent(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, (Class<?>) BroadcastReceiver.class));
        intent.setAction(ACTION_FORCE_STOP_RESCHEDULE);
        return intent;
    }

    static void setAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(NotificationCompat.CATEGORY_ALARM);
        int flags = Build.VERSION.SDK_INT >= 31 ? 134217728 | 33554432 : 134217728;
        PendingIntent pendingIntent = getPendingIntent(context, flags);
        long triggerAt = System.currentTimeMillis() + TEN_YEARS;
        if (alarmManager != null) {
            alarmManager.setExact(0, triggerAt, pendingIntent);
        }
    }

    public static class BroadcastReceiver extends android.content.BroadcastReceiver {
        private static final String TAG = Logger.tagWithPrefix("ForceStopRunnable$Rcvr");

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (ForceStopRunnable.ACTION_FORCE_STOP_RESCHEDULE.equals(action)) {
                    Logger.get().verbose(TAG, "Rescheduling alarm that keeps track of force-stops.");
                    ForceStopRunnable.setAlarm(context);
                }
            }
        }
    }
}
