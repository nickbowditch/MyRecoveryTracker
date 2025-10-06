package androidx.work.impl.foreground;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import androidx.work.ForegroundInfo;
import androidx.work.Logger;
import androidx.work.impl.ExecutionListener;
import androidx.work.impl.WorkManagerImpl;
import androidx.work.impl.constraints.ConstraintsState;
import androidx.work.impl.constraints.OnConstraintsStateChangedListener;
import androidx.work.impl.constraints.WorkConstraintsTracker;
import androidx.work.impl.constraints.WorkConstraintsTrackerKt;
import androidx.work.impl.model.WorkGenerationalId;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.model.WorkSpecKt;
import androidx.work.impl.utils.taskexecutor.TaskExecutor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import kotlinx.coroutines.Job;

/* loaded from: classes.dex */
public class SystemForegroundDispatcher implements OnConstraintsStateChangedListener, ExecutionListener {
    private static final String ACTION_CANCEL_WORK = "ACTION_CANCEL_WORK";
    private static final String ACTION_NOTIFY = "ACTION_NOTIFY";
    private static final String ACTION_START_FOREGROUND = "ACTION_START_FOREGROUND";
    private static final String ACTION_STOP_FOREGROUND = "ACTION_STOP_FOREGROUND";
    private static final String KEY_FOREGROUND_SERVICE_TYPE = "KEY_FOREGROUND_SERVICE_TYPE";
    private static final String KEY_GENERATION = "KEY_GENERATION";
    private static final String KEY_NOTIFICATION = "KEY_NOTIFICATION";
    private static final String KEY_NOTIFICATION_ID = "KEY_NOTIFICATION_ID";
    private static final String KEY_WORKSPEC_ID = "KEY_WORKSPEC_ID";
    static final String TAG = Logger.tagWithPrefix("SystemFgDispatcher");
    private Callback mCallback;
    final WorkConstraintsTracker mConstraintsTracker;
    private Context mContext;
    private final TaskExecutor mTaskExecutor;
    private WorkManagerImpl mWorkManagerImpl;
    final Object mLock = new Object();
    WorkGenerationalId mCurrentForegroundId = null;
    final Map<WorkGenerationalId, ForegroundInfo> mForegroundInfoById = new LinkedHashMap();
    final Map<WorkGenerationalId, Job> mTrackedWorkSpecs = new HashMap();
    final Map<WorkGenerationalId, WorkSpec> mWorkSpecById = new HashMap();

    interface Callback {
        void cancelNotification(int notificationId);

        void notify(int notificationId, Notification notification);

        void startForeground(int notificationId, int notificationType, Notification notification);

        void stop();
    }

    SystemForegroundDispatcher(Context context) {
        this.mContext = context;
        this.mWorkManagerImpl = WorkManagerImpl.getInstance(this.mContext);
        this.mTaskExecutor = this.mWorkManagerImpl.getWorkTaskExecutor();
        this.mConstraintsTracker = new WorkConstraintsTracker(this.mWorkManagerImpl.getTrackers());
        this.mWorkManagerImpl.getProcessor().addExecutionListener(this);
    }

    SystemForegroundDispatcher(Context context, WorkManagerImpl workManagerImpl, WorkConstraintsTracker tracker) {
        this.mContext = context;
        this.mWorkManagerImpl = workManagerImpl;
        this.mTaskExecutor = this.mWorkManagerImpl.getWorkTaskExecutor();
        this.mConstraintsTracker = tracker;
        this.mWorkManagerImpl.getProcessor().addExecutionListener(this);
    }

    @Override // androidx.work.impl.ExecutionListener
    public void onExecuted(WorkGenerationalId id, boolean needsReschedule) {
        Map.Entry<WorkGenerationalId, ForegroundInfo> entry;
        Job removed = null;
        synchronized (this.mLock) {
            WorkSpec workSpec = this.mWorkSpecById.remove(id);
            if (workSpec != null) {
                removed = this.mTrackedWorkSpecs.remove(id);
            }
            if (removed != null) {
                removed.cancel((CancellationException) null);
            }
        }
        ForegroundInfo removedInfo = this.mForegroundInfoById.remove(id);
        if (id.equals(this.mCurrentForegroundId)) {
            if (this.mForegroundInfoById.size() > 0) {
                Iterator<Map.Entry<WorkGenerationalId, ForegroundInfo>> iterator = this.mForegroundInfoById.entrySet().iterator();
                Map.Entry<WorkGenerationalId, ForegroundInfo> entry2 = iterator.next();
                while (true) {
                    entry = entry2;
                    if (!iterator.hasNext()) {
                        break;
                    } else {
                        entry2 = iterator.next();
                    }
                }
                this.mCurrentForegroundId = entry.getKey();
                if (this.mCallback != null) {
                    ForegroundInfo info = entry.getValue();
                    this.mCallback.startForeground(info.getNotificationId(), info.getForegroundServiceType(), info.getNotification());
                    this.mCallback.cancelNotification(info.getNotificationId());
                }
            } else {
                this.mCurrentForegroundId = null;
            }
        }
        Callback callback = this.mCallback;
        if (removedInfo != null && callback != null) {
            Logger.get().debug(TAG, "Removing Notification (id: " + removedInfo.getNotificationId() + ", workSpecId: " + id + ", notificationType: " + removedInfo.getForegroundServiceType());
            callback.cancelNotification(removedInfo.getNotificationId());
        }
    }

    void setCallback(Callback callback) {
        if (this.mCallback != null) {
            Logger.get().error(TAG, "A callback already exists.");
        } else {
            this.mCallback = callback;
        }
    }

    void onStartCommand(Intent intent) {
        String action = intent.getAction();
        if (ACTION_START_FOREGROUND.equals(action)) {
            handleStartForeground(intent);
            handleNotify(intent);
        } else if (ACTION_NOTIFY.equals(action)) {
            handleNotify(intent);
        } else if (ACTION_CANCEL_WORK.equals(action)) {
            handleCancelWork(intent);
        } else if (ACTION_STOP_FOREGROUND.equals(action)) {
            handleStop(intent);
        }
    }

    void onDestroy() {
        this.mCallback = null;
        synchronized (this.mLock) {
            for (Job job : this.mTrackedWorkSpecs.values()) {
                job.cancel((CancellationException) null);
            }
        }
        this.mWorkManagerImpl.getProcessor().removeExecutionListener(this);
    }

    private void handleStartForeground(Intent intent) {
        Logger.get().info(TAG, "Started foreground service " + intent);
        final String workSpecId = intent.getStringExtra(KEY_WORKSPEC_ID);
        this.mTaskExecutor.executeOnTaskThread(new Runnable() { // from class: androidx.work.impl.foreground.SystemForegroundDispatcher.1
            @Override // java.lang.Runnable
            public void run() {
                WorkSpec workSpec = SystemForegroundDispatcher.this.mWorkManagerImpl.getProcessor().getRunningWorkSpec(workSpecId);
                if (workSpec != null && workSpec.hasConstraints()) {
                    synchronized (SystemForegroundDispatcher.this.mLock) {
                        SystemForegroundDispatcher.this.mWorkSpecById.put(WorkSpecKt.generationalId(workSpec), workSpec);
                        Job job = WorkConstraintsTrackerKt.listen(SystemForegroundDispatcher.this.mConstraintsTracker, workSpec, SystemForegroundDispatcher.this.mTaskExecutor.getTaskCoroutineDispatcher(), SystemForegroundDispatcher.this);
                        SystemForegroundDispatcher.this.mTrackedWorkSpecs.put(WorkSpecKt.generationalId(workSpec), job);
                    }
                }
            }
        });
    }

    private void handleNotify(Intent intent) {
        int notificationId = intent.getIntExtra(KEY_NOTIFICATION_ID, 0);
        int notificationType = intent.getIntExtra(KEY_FOREGROUND_SERVICE_TYPE, 0);
        String workSpecId = intent.getStringExtra(KEY_WORKSPEC_ID);
        int generation = intent.getIntExtra(KEY_GENERATION, 0);
        WorkGenerationalId workId = new WorkGenerationalId(workSpecId, generation);
        Notification notification = (Notification) intent.getParcelableExtra(KEY_NOTIFICATION);
        Logger.get().debug(TAG, "Notifying with (id:" + notificationId + ", workSpecId: " + workSpecId + ", notificationType :" + notificationType + ")");
        if (notification != null && this.mCallback != null) {
            ForegroundInfo info = new ForegroundInfo(notificationId, notification, notificationType);
            this.mForegroundInfoById.put(workId, info);
            if (this.mCurrentForegroundId == null) {
                this.mCurrentForegroundId = workId;
                this.mCallback.startForeground(notificationId, notificationType, notification);
                return;
            }
            this.mCallback.notify(notificationId, notification);
            if (notificationType != 0 && Build.VERSION.SDK_INT >= 29) {
                int foregroundServiceType = 0;
                for (Map.Entry<WorkGenerationalId, ForegroundInfo> entry : this.mForegroundInfoById.entrySet()) {
                    ForegroundInfo foregroundInfo = entry.getValue();
                    foregroundServiceType |= foregroundInfo.getForegroundServiceType();
                }
                ForegroundInfo currentInfo = this.mForegroundInfoById.get(this.mCurrentForegroundId);
                if (currentInfo != null) {
                    this.mCallback.startForeground(currentInfo.getNotificationId(), foregroundServiceType, currentInfo.getNotification());
                }
            }
        }
    }

    void handleStop(Intent intent) {
        Logger.get().info(TAG, "Stopping foreground service");
        if (this.mCallback != null) {
            this.mCallback.stop();
        }
    }

    private void handleCancelWork(Intent intent) {
        Logger.get().info(TAG, "Stopping foreground work for " + intent);
        String workSpecId = intent.getStringExtra(KEY_WORKSPEC_ID);
        if (workSpecId != null && !TextUtils.isEmpty(workSpecId)) {
            this.mWorkManagerImpl.cancelWorkById(UUID.fromString(workSpecId));
        }
    }

    @Override // androidx.work.impl.constraints.OnConstraintsStateChangedListener
    public void onConstraintsStateChanged(WorkSpec workSpec, ConstraintsState state) {
        if (state instanceof ConstraintsState.ConstraintsNotMet) {
            String workSpecId = workSpec.id;
            Logger.get().debug(TAG, "Constraints unmet for WorkSpec " + workSpecId);
            this.mWorkManagerImpl.stopForegroundWork(WorkSpecKt.generationalId(workSpec));
        }
    }

    public static Intent createStartForegroundIntent(Context context, WorkGenerationalId id, ForegroundInfo info) {
        Intent intent = new Intent(context, (Class<?>) SystemForegroundService.class);
        intent.setAction(ACTION_START_FOREGROUND);
        intent.putExtra(KEY_WORKSPEC_ID, id.getWorkSpecId());
        intent.putExtra(KEY_GENERATION, id.getGeneration());
        intent.putExtra(KEY_NOTIFICATION_ID, info.getNotificationId());
        intent.putExtra(KEY_FOREGROUND_SERVICE_TYPE, info.getForegroundServiceType());
        intent.putExtra(KEY_NOTIFICATION, info.getNotification());
        return intent;
    }

    public static Intent createCancelWorkIntent(Context context, String workSpecId) {
        Intent intent = new Intent(context, (Class<?>) SystemForegroundService.class);
        intent.setAction(ACTION_CANCEL_WORK);
        intent.setData(Uri.parse("workspec://" + workSpecId));
        intent.putExtra(KEY_WORKSPEC_ID, workSpecId);
        return intent;
    }

    public static Intent createNotifyIntent(Context context, WorkGenerationalId id, ForegroundInfo info) {
        Intent intent = new Intent(context, (Class<?>) SystemForegroundService.class);
        intent.setAction(ACTION_NOTIFY);
        intent.putExtra(KEY_NOTIFICATION_ID, info.getNotificationId());
        intent.putExtra(KEY_FOREGROUND_SERVICE_TYPE, info.getForegroundServiceType());
        intent.putExtra(KEY_NOTIFICATION, info.getNotification());
        intent.putExtra(KEY_WORKSPEC_ID, id.getWorkSpecId());
        intent.putExtra(KEY_GENERATION, id.getGeneration());
        return intent;
    }

    public static Intent createStopForegroundIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) SystemForegroundService.class);
        intent.setAction(ACTION_STOP_FOREGROUND);
        return intent;
    }
}
