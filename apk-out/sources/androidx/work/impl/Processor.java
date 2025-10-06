package androidx.work.impl;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import androidx.core.content.ContextCompat;
import androidx.work.Configuration;
import androidx.work.ForegroundInfo;
import androidx.work.Logger;
import androidx.work.WorkerParameters;
import androidx.work.impl.WorkerWrapper;
import androidx.work.impl.foreground.ForegroundProcessor;
import androidx.work.impl.foreground.SystemForegroundDispatcher;
import androidx.work.impl.model.WorkGenerationalId;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.utils.WakeLocks;
import androidx.work.impl.utils.taskexecutor.TaskExecutor;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/* loaded from: classes.dex */
public class Processor implements ForegroundProcessor {
    private static final String FOREGROUND_WAKELOCK_TAG = "ProcessorForegroundLck";
    private static final String TAG = Logger.tagWithPrefix("Processor");
    private Context mAppContext;
    private Configuration mConfiguration;
    private WorkDatabase mWorkDatabase;
    private TaskExecutor mWorkTaskExecutor;
    private Map<String, WorkerWrapper> mEnqueuedWorkMap = new HashMap();
    private Map<String, WorkerWrapper> mForegroundWorkMap = new HashMap();
    private Set<String> mCancelledIds = new HashSet();
    private final List<ExecutionListener> mOuterListeners = new ArrayList();
    private PowerManager.WakeLock mForegroundLock = null;
    private final Object mLock = new Object();
    private Map<String, Set<StartStopToken>> mWorkRuns = new HashMap();

    public Processor(Context appContext, Configuration configuration, TaskExecutor workTaskExecutor, WorkDatabase workDatabase) {
        this.mAppContext = appContext;
        this.mConfiguration = configuration;
        this.mWorkTaskExecutor = workTaskExecutor;
        this.mWorkDatabase = workDatabase;
    }

    public boolean startWork(StartStopToken id) {
        return startWork(id, null);
    }

    public boolean startWork(StartStopToken startStopToken, WorkerParameters.RuntimeExtras runtimeExtras) throws Throwable {
        WorkGenerationalId id = startStopToken.getId();
        final String workSpecId = id.getWorkSpecId();
        final ArrayList<String> tags = new ArrayList<>();
        WorkSpec workSpec = (WorkSpec) this.mWorkDatabase.runInTransaction(new Callable() { // from class: androidx.work.impl.Processor$$ExternalSyntheticLambda1
            @Override // java.util.concurrent.Callable
            public final Object call() {
                return this.f$0.m102lambda$startWork$0$androidxworkimplProcessor(tags, workSpecId);
            }
        });
        if (workSpec == null) {
            Logger.get().warning(TAG, "Didn't find WorkSpec for id " + id);
            runOnExecuted(id, false);
            return false;
        }
        synchronized (this.mLock) {
            try {
                try {
                    if (isEnqueued(workSpecId)) {
                        Set<StartStopToken> tokens = this.mWorkRuns.get(workSpecId);
                        StartStopToken previousRun = tokens.iterator().next();
                        int previousRunGeneration = previousRun.getId().getGeneration();
                        if (previousRunGeneration == id.getGeneration()) {
                            tokens.add(startStopToken);
                            Logger.get().debug(TAG, "Work " + id + " is already enqueued for processing");
                        } else {
                            runOnExecuted(id, false);
                        }
                        return false;
                    }
                    if (workSpec.getGeneration() != id.getGeneration()) {
                        runOnExecuted(id, false);
                        return false;
                    }
                    final WorkerWrapper workWrapper = new WorkerWrapper.Builder(this.mAppContext, this.mConfiguration, this.mWorkTaskExecutor, this, this.mWorkDatabase, workSpec, tags).withRuntimeExtras(runtimeExtras).build();
                    final ListenableFuture<Boolean> future = workWrapper.getFuture();
                    future.addListener(new Runnable() { // from class: androidx.work.impl.Processor$$ExternalSyntheticLambda2
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m103lambda$startWork$1$androidxworkimplProcessor(future, workWrapper);
                        }
                    }, this.mWorkTaskExecutor.getMainThreadExecutor());
                    this.mEnqueuedWorkMap.put(workSpecId, workWrapper);
                    HashSet<StartStopToken> set = new HashSet<>();
                    set.add(startStopToken);
                    this.mWorkRuns.put(workSpecId, set);
                    this.mWorkTaskExecutor.getSerialTaskExecutor().execute(workWrapper);
                    Logger.get().debug(TAG, getClass().getSimpleName() + ": processing " + id);
                    return true;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    /* renamed from: lambda$startWork$0$androidx-work-impl-Processor, reason: not valid java name */
    /* synthetic */ WorkSpec m102lambda$startWork$0$androidxworkimplProcessor(ArrayList tags, String workSpecId) throws Exception {
        tags.addAll(this.mWorkDatabase.workTagDao().getTagsForWorkSpecId(workSpecId));
        return this.mWorkDatabase.workSpecDao().getWorkSpec(workSpecId);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* renamed from: lambda$startWork$1$androidx-work-impl-Processor, reason: not valid java name */
    /* synthetic */ void m103lambda$startWork$1$androidxworkimplProcessor(ListenableFuture future, WorkerWrapper workWrapper) {
        boolean needsReschedule;
        try {
            needsReschedule = ((Boolean) future.get()).booleanValue();
        } catch (InterruptedException | ExecutionException e) {
            needsReschedule = true;
        }
        onExecuted(workWrapper, needsReschedule);
    }

    @Override // androidx.work.impl.foreground.ForegroundProcessor
    public void startForeground(String workSpecId, ForegroundInfo foregroundInfo) {
        synchronized (this.mLock) {
            Logger.get().info(TAG, "Moving WorkSpec (" + workSpecId + ") to the foreground");
            WorkerWrapper wrapper = this.mEnqueuedWorkMap.remove(workSpecId);
            if (wrapper != null) {
                if (this.mForegroundLock == null) {
                    this.mForegroundLock = WakeLocks.newWakeLock(this.mAppContext, FOREGROUND_WAKELOCK_TAG);
                    this.mForegroundLock.acquire();
                }
                this.mForegroundWorkMap.put(workSpecId, wrapper);
                Intent intent = SystemForegroundDispatcher.createStartForegroundIntent(this.mAppContext, wrapper.getWorkGenerationalId(), foregroundInfo);
                ContextCompat.startForegroundService(this.mAppContext, intent);
            }
        }
    }

    public boolean stopForegroundWork(StartStopToken token, int reason) {
        WorkerWrapper wrapper;
        String id = token.getId().getWorkSpecId();
        synchronized (this.mLock) {
            wrapper = cleanUpWorkerUnsafe(id);
        }
        return interrupt(id, wrapper, reason);
    }

    public boolean stopWork(StartStopToken runId, int reason) {
        String id = runId.getId().getWorkSpecId();
        synchronized (this.mLock) {
            if (this.mForegroundWorkMap.get(id) != null) {
                Logger.get().debug(TAG, "Ignored stopWork. WorkerWrapper " + id + " is in foreground");
                return false;
            }
            Set<StartStopToken> runs = this.mWorkRuns.get(id);
            if (runs != null && runs.contains(runId)) {
                WorkerWrapper wrapper = cleanUpWorkerUnsafe(id);
                return interrupt(id, wrapper, reason);
            }
            return false;
        }
    }

    public boolean stopAndCancelWork(String id, int reason) {
        WorkerWrapper wrapper;
        synchronized (this.mLock) {
            Logger.get().debug(TAG, "Processor cancelling " + id);
            this.mCancelledIds.add(id);
            wrapper = cleanUpWorkerUnsafe(id);
        }
        return interrupt(id, wrapper, reason);
    }

    public boolean isCancelled(String id) {
        boolean zContains;
        synchronized (this.mLock) {
            zContains = this.mCancelledIds.contains(id);
        }
        return zContains;
    }

    public boolean hasWork() {
        boolean z;
        synchronized (this.mLock) {
            z = (this.mEnqueuedWorkMap.isEmpty() && this.mForegroundWorkMap.isEmpty()) ? false : true;
        }
        return z;
    }

    public boolean isEnqueued(String workSpecId) {
        boolean z;
        synchronized (this.mLock) {
            z = getWorkerWrapperUnsafe(workSpecId) != null;
        }
        return z;
    }

    public void addExecutionListener(ExecutionListener executionListener) {
        synchronized (this.mLock) {
            this.mOuterListeners.add(executionListener);
        }
    }

    public void removeExecutionListener(ExecutionListener executionListener) {
        synchronized (this.mLock) {
            this.mOuterListeners.remove(executionListener);
        }
    }

    private void onExecuted(WorkerWrapper wrapper, boolean needsReschedule) {
        synchronized (this.mLock) {
            WorkGenerationalId id = wrapper.getWorkGenerationalId();
            String workSpecId = id.getWorkSpecId();
            WorkerWrapper workerWrapper = getWorkerWrapperUnsafe(workSpecId);
            if (workerWrapper == wrapper) {
                cleanUpWorkerUnsafe(workSpecId);
            }
            Logger.get().debug(TAG, getClass().getSimpleName() + " " + workSpecId + " executed; reschedule = " + needsReschedule);
            for (ExecutionListener executionListener : this.mOuterListeners) {
                executionListener.onExecuted(id, needsReschedule);
            }
        }
    }

    private WorkerWrapper getWorkerWrapperUnsafe(String workSpecId) {
        WorkerWrapper workerWrapper = this.mForegroundWorkMap.get(workSpecId);
        if (workerWrapper == null) {
            return this.mEnqueuedWorkMap.get(workSpecId);
        }
        return workerWrapper;
    }

    public WorkSpec getRunningWorkSpec(String workSpecId) {
        synchronized (this.mLock) {
            WorkerWrapper workerWrapper = getWorkerWrapperUnsafe(workSpecId);
            if (workerWrapper == null) {
                return null;
            }
            return workerWrapper.getWorkSpec();
        }
    }

    private void runOnExecuted(final WorkGenerationalId id, final boolean needsReschedule) {
        this.mWorkTaskExecutor.getMainThreadExecutor().execute(new Runnable() { // from class: androidx.work.impl.Processor$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m101lambda$runOnExecuted$2$androidxworkimplProcessor(id, needsReschedule);
            }
        });
    }

    /* renamed from: lambda$runOnExecuted$2$androidx-work-impl-Processor, reason: not valid java name */
    /* synthetic */ void m101lambda$runOnExecuted$2$androidxworkimplProcessor(WorkGenerationalId id, boolean needsReschedule) {
        synchronized (this.mLock) {
            for (ExecutionListener executionListener : this.mOuterListeners) {
                executionListener.onExecuted(id, needsReschedule);
            }
        }
    }

    private void stopForegroundService() {
        synchronized (this.mLock) {
            boolean hasForegroundWork = !this.mForegroundWorkMap.isEmpty();
            if (!hasForegroundWork) {
                Intent intent = SystemForegroundDispatcher.createStopForegroundIntent(this.mAppContext);
                try {
                    this.mAppContext.startService(intent);
                } catch (Throwable throwable) {
                    Logger.get().error(TAG, "Unable to stop foreground service", throwable);
                }
                if (this.mForegroundLock != null) {
                    this.mForegroundLock.release();
                    this.mForegroundLock = null;
                }
            }
        }
    }

    private WorkerWrapper cleanUpWorkerUnsafe(String id) {
        WorkerWrapper wrapper = this.mForegroundWorkMap.remove(id);
        boolean wasForeground = wrapper != null;
        if (!wasForeground) {
            wrapper = this.mEnqueuedWorkMap.remove(id);
        }
        this.mWorkRuns.remove(id);
        if (wasForeground) {
            stopForegroundService();
        }
        return wrapper;
    }

    private static boolean interrupt(String id, WorkerWrapper wrapper, int stopReason) {
        if (wrapper != null) {
            wrapper.interrupt(stopReason);
            Logger.get().debug(TAG, "WorkerWrapper interrupted for " + id);
            return true;
        }
        Logger.get().debug(TAG, "WorkerWrapper could not be found for " + id);
        return false;
    }
}
