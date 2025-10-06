package androidx.work.impl.background.systemalarm;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import androidx.work.Logger;
import androidx.work.impl.ExecutionListener;
import androidx.work.impl.Processor;
import androidx.work.impl.StartStopTokens;
import androidx.work.impl.WorkLauncher;
import androidx.work.impl.WorkLauncherImpl;
import androidx.work.impl.WorkManagerImpl;
import androidx.work.impl.model.WorkGenerationalId;
import androidx.work.impl.utils.WakeLocks;
import androidx.work.impl.utils.WorkTimer;
import androidx.work.impl.utils.taskexecutor.SerialExecutor;
import androidx.work.impl.utils.taskexecutor.TaskExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class SystemAlarmDispatcher implements ExecutionListener {
    private static final int DEFAULT_START_ID = 0;
    private static final String KEY_START_ID = "KEY_START_ID";
    private static final String PROCESS_COMMAND_TAG = "ProcessCommand";
    static final String TAG = Logger.tagWithPrefix("SystemAlarmDispatcher");
    final CommandHandler mCommandHandler;
    private CommandsCompletedListener mCompletedListener;
    final Context mContext;
    Intent mCurrentIntent;
    final List<Intent> mIntents;
    private final Processor mProcessor;
    private StartStopTokens mStartStopTokens;
    final TaskExecutor mTaskExecutor;
    private final WorkLauncher mWorkLauncher;
    private final WorkManagerImpl mWorkManager;
    private final WorkTimer mWorkTimer;

    interface CommandsCompletedListener {
        void onAllCommandsCompleted();
    }

    SystemAlarmDispatcher(Context context) {
        this(context, null, null, null);
    }

    SystemAlarmDispatcher(Context context, Processor processor, WorkManagerImpl workManager, WorkLauncher launcher) {
        this.mContext = context.getApplicationContext();
        this.mStartStopTokens = new StartStopTokens();
        this.mWorkManager = workManager != null ? workManager : WorkManagerImpl.getInstance(context);
        this.mCommandHandler = new CommandHandler(this.mContext, this.mWorkManager.getConfiguration().getClock(), this.mStartStopTokens);
        this.mWorkTimer = new WorkTimer(this.mWorkManager.getConfiguration().getRunnableScheduler());
        this.mProcessor = processor != null ? processor : this.mWorkManager.getProcessor();
        this.mTaskExecutor = this.mWorkManager.getWorkTaskExecutor();
        this.mWorkLauncher = launcher != null ? launcher : new WorkLauncherImpl(this.mProcessor, this.mTaskExecutor);
        this.mProcessor.addExecutionListener(this);
        this.mIntents = new ArrayList();
        this.mCurrentIntent = null;
    }

    void onDestroy() {
        Logger.get().debug(TAG, "Destroying SystemAlarmDispatcher");
        this.mProcessor.removeExecutionListener(this);
        this.mCompletedListener = null;
    }

    @Override // androidx.work.impl.ExecutionListener
    public void onExecuted(WorkGenerationalId id, boolean needsReschedule) {
        this.mTaskExecutor.getMainThreadExecutor().execute(new AddRunnable(this, CommandHandler.createExecutionCompletedIntent(this.mContext, id, needsReschedule), 0));
    }

    public boolean add(final Intent intent, final int startId) {
        Logger.get().debug(TAG, "Adding command " + intent + " (" + startId + ")");
        assertMainThread();
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            Logger.get().warning(TAG, "Unknown command. Ignoring");
            return false;
        }
        if ("ACTION_CONSTRAINTS_CHANGED".equals(action) && hasIntentWithAction("ACTION_CONSTRAINTS_CHANGED")) {
            return false;
        }
        intent.putExtra(KEY_START_ID, startId);
        synchronized (this.mIntents) {
            boolean hasCommands = this.mIntents.isEmpty() ? false : true;
            this.mIntents.add(intent);
            if (!hasCommands) {
                processCommand();
            }
        }
        return true;
    }

    void setCompletedListener(CommandsCompletedListener listener) {
        if (this.mCompletedListener != null) {
            Logger.get().error(TAG, "A completion listener for SystemAlarmDispatcher already exists.");
        } else {
            this.mCompletedListener = listener;
        }
    }

    Processor getProcessor() {
        return this.mProcessor;
    }

    WorkTimer getWorkTimer() {
        return this.mWorkTimer;
    }

    WorkManagerImpl getWorkManager() {
        return this.mWorkManager;
    }

    TaskExecutor getTaskExecutor() {
        return this.mTaskExecutor;
    }

    WorkLauncher getWorkerLauncher() {
        return this.mWorkLauncher;
    }

    void dequeueAndCheckForCompletion() {
        Logger.get().debug(TAG, "Checking if commands are complete.");
        assertMainThread();
        synchronized (this.mIntents) {
            if (this.mCurrentIntent != null) {
                Logger.get().debug(TAG, "Removing command " + this.mCurrentIntent);
                if (!this.mIntents.remove(0).equals(this.mCurrentIntent)) {
                    throw new IllegalStateException("Dequeue-d command is not the first.");
                }
                this.mCurrentIntent = null;
            }
            SerialExecutor serialExecutor = this.mTaskExecutor.getSerialTaskExecutor();
            if (!this.mCommandHandler.hasPendingCommands() && this.mIntents.isEmpty() && !serialExecutor.hasPendingTasks()) {
                Logger.get().debug(TAG, "No more commands & intents.");
                if (this.mCompletedListener != null) {
                    this.mCompletedListener.onAllCommandsCompleted();
                }
            } else if (!this.mIntents.isEmpty()) {
                processCommand();
            }
        }
    }

    private void processCommand() {
        assertMainThread();
        PowerManager.WakeLock processCommandLock = WakeLocks.newWakeLock(this.mContext, PROCESS_COMMAND_TAG);
        try {
            processCommandLock.acquire();
            this.mWorkManager.getWorkTaskExecutor().executeOnTaskThread(new Runnable() { // from class: androidx.work.impl.background.systemalarm.SystemAlarmDispatcher.1
                @Override // java.lang.Runnable
                public void run() {
                    Executor mainThreadExecutor;
                    DequeueAndCheckForCompletion dequeueAndCheckForCompletion;
                    synchronized (SystemAlarmDispatcher.this.mIntents) {
                        SystemAlarmDispatcher.this.mCurrentIntent = SystemAlarmDispatcher.this.mIntents.get(0);
                    }
                    if (SystemAlarmDispatcher.this.mCurrentIntent != null) {
                        String action = SystemAlarmDispatcher.this.mCurrentIntent.getAction();
                        int startId = SystemAlarmDispatcher.this.mCurrentIntent.getIntExtra(SystemAlarmDispatcher.KEY_START_ID, 0);
                        Logger.get().debug(SystemAlarmDispatcher.TAG, "Processing command " + SystemAlarmDispatcher.this.mCurrentIntent + ", " + startId);
                        PowerManager.WakeLock wakeLock = WakeLocks.newWakeLock(SystemAlarmDispatcher.this.mContext, action + " (" + startId + ")");
                        try {
                            Logger.get().debug(SystemAlarmDispatcher.TAG, "Acquiring operation wake lock (" + action + ") " + wakeLock);
                            wakeLock.acquire();
                            SystemAlarmDispatcher.this.mCommandHandler.onHandleIntent(SystemAlarmDispatcher.this.mCurrentIntent, startId, SystemAlarmDispatcher.this);
                            Logger.get().debug(SystemAlarmDispatcher.TAG, "Releasing operation wake lock (" + action + ") " + wakeLock);
                            wakeLock.release();
                            mainThreadExecutor = SystemAlarmDispatcher.this.mTaskExecutor.getMainThreadExecutor();
                            dequeueAndCheckForCompletion = new DequeueAndCheckForCompletion(SystemAlarmDispatcher.this);
                        } catch (Throwable throwable) {
                            try {
                                Logger.get().error(SystemAlarmDispatcher.TAG, "Unexpected error in onHandleIntent", throwable);
                                Logger.get().debug(SystemAlarmDispatcher.TAG, "Releasing operation wake lock (" + action + ") " + wakeLock);
                                wakeLock.release();
                                mainThreadExecutor = SystemAlarmDispatcher.this.mTaskExecutor.getMainThreadExecutor();
                                dequeueAndCheckForCompletion = new DequeueAndCheckForCompletion(SystemAlarmDispatcher.this);
                            } catch (Throwable th) {
                                Logger.get().debug(SystemAlarmDispatcher.TAG, "Releasing operation wake lock (" + action + ") " + wakeLock);
                                wakeLock.release();
                                SystemAlarmDispatcher.this.mTaskExecutor.getMainThreadExecutor().execute(new DequeueAndCheckForCompletion(SystemAlarmDispatcher.this));
                                throw th;
                            }
                        }
                        mainThreadExecutor.execute(dequeueAndCheckForCompletion);
                    }
                }
            });
        } finally {
            processCommandLock.release();
        }
    }

    private boolean hasIntentWithAction(String action) {
        assertMainThread();
        synchronized (this.mIntents) {
            for (Intent intent : this.mIntents) {
                if (action.equals(intent.getAction())) {
                    return true;
                }
            }
            return false;
        }
    }

    private void assertMainThread() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Needs to be invoked on the main thread.");
        }
    }

    static class DequeueAndCheckForCompletion implements Runnable {
        private final SystemAlarmDispatcher mDispatcher;

        DequeueAndCheckForCompletion(SystemAlarmDispatcher dispatcher) {
            this.mDispatcher = dispatcher;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mDispatcher.dequeueAndCheckForCompletion();
        }
    }

    static class AddRunnable implements Runnable {
        private final SystemAlarmDispatcher mDispatcher;
        private final Intent mIntent;
        private final int mStartId;

        AddRunnable(SystemAlarmDispatcher dispatcher, Intent intent, int startId) {
            this.mDispatcher = dispatcher;
            this.mIntent = intent;
            this.mStartId = startId;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mDispatcher.add(this.mIntent, this.mStartId);
        }
    }
}
