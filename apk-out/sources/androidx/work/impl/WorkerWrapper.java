package androidx.work.impl;

import android.content.Context;
import androidx.work.Clock;
import androidx.work.Configuration;
import androidx.work.Data;
import androidx.work.InputMerger;
import androidx.work.InputMergerFactory;
import androidx.work.ListenableWorker;
import androidx.work.Logger;
import androidx.work.WorkInfo;
import androidx.work.WorkerParameters;
import androidx.work.impl.background.systemalarm.RescheduleReceiver;
import androidx.work.impl.foreground.ForegroundProcessor;
import androidx.work.impl.model.DependencyDao;
import androidx.work.impl.model.WorkGenerationalId;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.model.WorkSpecDao;
import androidx.work.impl.model.WorkSpecKt;
import androidx.work.impl.utils.PackageManagerHelper;
import androidx.work.impl.utils.SynchronousExecutor;
import androidx.work.impl.utils.WorkForegroundRunnable;
import androidx.work.impl.utils.WorkForegroundUpdater;
import androidx.work.impl.utils.WorkProgressUpdater;
import androidx.work.impl.utils.futures.SettableFuture;
import androidx.work.impl.utils.taskexecutor.TaskExecutor;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/* loaded from: classes.dex */
public class WorkerWrapper implements Runnable {
    static final String TAG = Logger.tagWithPrefix("WorkerWrapper");
    Context mAppContext;
    private Clock mClock;
    private Configuration mConfiguration;
    private DependencyDao mDependencyDao;
    private ForegroundProcessor mForegroundProcessor;
    private WorkerParameters.RuntimeExtras mRuntimeExtras;
    private List<String> mTags;
    private WorkDatabase mWorkDatabase;
    private String mWorkDescription;
    WorkSpec mWorkSpec;
    private WorkSpecDao mWorkSpecDao;
    private final String mWorkSpecId;
    TaskExecutor mWorkTaskExecutor;
    ListenableWorker mWorker;
    ListenableWorker.Result mResult = ListenableWorker.Result.failure();
    SettableFuture<Boolean> mFuture = SettableFuture.create();
    final SettableFuture<ListenableWorker.Result> mWorkerResultFuture = SettableFuture.create();
    private volatile int mInterrupted = -256;

    WorkerWrapper(Builder builder) {
        this.mAppContext = builder.mAppContext;
        this.mWorkTaskExecutor = builder.mWorkTaskExecutor;
        this.mForegroundProcessor = builder.mForegroundProcessor;
        this.mWorkSpec = builder.mWorkSpec;
        this.mWorkSpecId = this.mWorkSpec.id;
        this.mRuntimeExtras = builder.mRuntimeExtras;
        this.mWorker = builder.mWorker;
        this.mConfiguration = builder.mConfiguration;
        this.mClock = builder.mConfiguration.getClock();
        this.mWorkDatabase = builder.mWorkDatabase;
        this.mWorkSpecDao = this.mWorkDatabase.workSpecDao();
        this.mDependencyDao = this.mWorkDatabase.dependencyDao();
        this.mTags = builder.mTags;
    }

    public WorkGenerationalId getWorkGenerationalId() {
        return WorkSpecKt.generationalId(this.mWorkSpec);
    }

    public ListenableFuture<Boolean> getFuture() {
        return this.mFuture;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mWorkDescription = createWorkDescription(this.mTags);
        runWorker();
    }

    public WorkSpec getWorkSpec() {
        return this.mWorkSpec;
    }

    private void runWorker() {
        Data input;
        if (tryCheckForInterruptionAndResolve()) {
            return;
        }
        this.mWorkDatabase.beginTransaction();
        try {
            if (this.mWorkSpec.state != WorkInfo.State.ENQUEUED) {
                resolveIncorrectStatus();
                this.mWorkDatabase.setTransactionSuccessful();
                Logger.get().debug(TAG, this.mWorkSpec.workerClassName + " is not in ENQUEUED state. Nothing more to do");
                return;
            }
            if (this.mWorkSpec.isPeriodic() || this.mWorkSpec.isBackedOff()) {
                long now = this.mClock.currentTimeMillis();
                if (now < this.mWorkSpec.calculateNextRunTime()) {
                    Logger.get().debug(TAG, String.format("Delaying execution for %s because it is being executed before schedule.", this.mWorkSpec.workerClassName));
                    resolve(true);
                    this.mWorkDatabase.setTransactionSuccessful();
                    return;
                }
            }
            this.mWorkDatabase.setTransactionSuccessful();
            this.mWorkDatabase.endTransaction();
            if (this.mWorkSpec.isPeriodic()) {
                input = this.mWorkSpec.input;
            } else {
                InputMergerFactory inputMergerFactory = this.mConfiguration.getInputMergerFactory();
                String inputMergerClassName = this.mWorkSpec.inputMergerClassName;
                InputMerger inputMerger = inputMergerFactory.createInputMergerWithDefaultFallback(inputMergerClassName);
                if (inputMerger == null) {
                    Logger.get().error(TAG, "Could not create Input Merger " + this.mWorkSpec.inputMergerClassName);
                    setFailedAndResolve();
                    return;
                } else {
                    List<Data> inputs = new ArrayList<>();
                    inputs.add(this.mWorkSpec.input);
                    inputs.addAll(this.mWorkSpecDao.getInputsFromPrerequisites(this.mWorkSpecId));
                    input = inputMerger.merge(inputs);
                }
            }
            WorkerParameters params = new WorkerParameters(UUID.fromString(this.mWorkSpecId), input, this.mTags, this.mRuntimeExtras, this.mWorkSpec.runAttemptCount, this.mWorkSpec.getGeneration(), this.mConfiguration.getExecutor(), this.mWorkTaskExecutor, this.mConfiguration.getWorkerFactory(), new WorkProgressUpdater(this.mWorkDatabase, this.mWorkTaskExecutor), new WorkForegroundUpdater(this.mWorkDatabase, this.mForegroundProcessor, this.mWorkTaskExecutor));
            if (this.mWorker == null) {
                this.mWorker = this.mConfiguration.getWorkerFactory().createWorkerWithDefaultFallback(this.mAppContext, this.mWorkSpec.workerClassName, params);
            }
            if (this.mWorker == null) {
                Logger.get().error(TAG, "Could not create Worker " + this.mWorkSpec.workerClassName);
                setFailedAndResolve();
                return;
            }
            if (this.mWorker.isUsed()) {
                Logger.get().error(TAG, "Received an already-used Worker " + this.mWorkSpec.workerClassName + "; Worker Factory should return new instances");
                setFailedAndResolve();
                return;
            }
            this.mWorker.setUsed();
            if (!trySetRunning()) {
                resolveIncorrectStatus();
                return;
            }
            if (tryCheckForInterruptionAndResolve()) {
                return;
            }
            WorkForegroundRunnable foregroundRunnable = new WorkForegroundRunnable(this.mAppContext, this.mWorkSpec, this.mWorker, params.getForegroundUpdater(), this.mWorkTaskExecutor);
            this.mWorkTaskExecutor.getMainThreadExecutor().execute(foregroundRunnable);
            final ListenableFuture<Void> runExpedited = foregroundRunnable.getFuture();
            this.mWorkerResultFuture.addListener(new Runnable() { // from class: androidx.work.impl.WorkerWrapper$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m105lambda$runWorker$0$androidxworkimplWorkerWrapper(runExpedited);
                }
            }, new SynchronousExecutor());
            runExpedited.addListener(new Runnable() { // from class: androidx.work.impl.WorkerWrapper.1
                @Override // java.lang.Runnable
                public void run() {
                    if (WorkerWrapper.this.mWorkerResultFuture.isCancelled()) {
                        return;
                    }
                    try {
                        runExpedited.get();
                        Logger.get().debug(WorkerWrapper.TAG, "Starting work for " + WorkerWrapper.this.mWorkSpec.workerClassName);
                        WorkerWrapper.this.mWorkerResultFuture.setFuture(WorkerWrapper.this.mWorker.startWork());
                    } catch (Throwable e) {
                        WorkerWrapper.this.mWorkerResultFuture.setException(e);
                    }
                }
            }, this.mWorkTaskExecutor.getMainThreadExecutor());
            final String workDescription = this.mWorkDescription;
            this.mWorkerResultFuture.addListener(new Runnable() { // from class: androidx.work.impl.WorkerWrapper.2
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        try {
                            ListenableWorker.Result result = WorkerWrapper.this.mWorkerResultFuture.get();
                            if (result == null) {
                                Logger.get().error(WorkerWrapper.TAG, WorkerWrapper.this.mWorkSpec.workerClassName + " returned a null result. Treating it as a failure.");
                            } else {
                                Logger.get().debug(WorkerWrapper.TAG, WorkerWrapper.this.mWorkSpec.workerClassName + " returned a " + result + ".");
                                WorkerWrapper.this.mResult = result;
                            }
                        } catch (InterruptedException e) {
                            exception = e;
                            Logger.get().error(WorkerWrapper.TAG, workDescription + " failed because it threw an exception/error", exception);
                        } catch (CancellationException exception) {
                            Logger.get().info(WorkerWrapper.TAG, workDescription + " was cancelled", exception);
                        } catch (ExecutionException e2) {
                            exception = e2;
                            Logger.get().error(WorkerWrapper.TAG, workDescription + " failed because it threw an exception/error", exception);
                        }
                    } finally {
                        WorkerWrapper.this.onWorkFinished();
                    }
                }
            }, this.mWorkTaskExecutor.getSerialTaskExecutor());
        } finally {
            this.mWorkDatabase.endTransaction();
        }
    }

    /* renamed from: lambda$runWorker$0$androidx-work-impl-WorkerWrapper, reason: not valid java name */
    /* synthetic */ void m105lambda$runWorker$0$androidxworkimplWorkerWrapper(ListenableFuture runExpedited) {
        if (this.mWorkerResultFuture.isCancelled()) {
            runExpedited.cancel(true);
        }
    }

    void onWorkFinished() {
        if (!tryCheckForInterruptionAndResolve()) {
            this.mWorkDatabase.beginTransaction();
            try {
                WorkInfo.State state = this.mWorkSpecDao.getState(this.mWorkSpecId);
                this.mWorkDatabase.workProgressDao().delete(this.mWorkSpecId);
                if (state == null) {
                    resolve(false);
                } else if (state == WorkInfo.State.RUNNING) {
                    handleResult(this.mResult);
                } else if (!state.isFinished()) {
                    this.mInterrupted = WorkInfo.STOP_REASON_UNKNOWN;
                    rescheduleAndResolve();
                }
                this.mWorkDatabase.setTransactionSuccessful();
            } finally {
                this.mWorkDatabase.endTransaction();
            }
        }
    }

    public void interrupt(int stopReason) {
        this.mInterrupted = stopReason;
        tryCheckForInterruptionAndResolve();
        this.mWorkerResultFuture.cancel(true);
        if (this.mWorker != null && this.mWorkerResultFuture.isCancelled()) {
            this.mWorker.stop(stopReason);
        } else {
            String message = "WorkSpec " + this.mWorkSpec + " is already done. Not interrupting.";
            Logger.get().debug(TAG, message);
        }
    }

    private void resolveIncorrectStatus() {
        WorkInfo.State status = this.mWorkSpecDao.getState(this.mWorkSpecId);
        if (status == WorkInfo.State.RUNNING) {
            Logger.get().debug(TAG, "Status for " + this.mWorkSpecId + " is RUNNING; not doing any work and rescheduling for later execution");
            resolve(true);
        } else {
            Logger.get().debug(TAG, "Status for " + this.mWorkSpecId + " is " + status + " ; not doing any work");
            resolve(false);
        }
    }

    private boolean tryCheckForInterruptionAndResolve() {
        if (this.mInterrupted == -256) {
            return false;
        }
        Logger.get().debug(TAG, "Work interrupted for " + this.mWorkDescription);
        WorkInfo.State currentState = this.mWorkSpecDao.getState(this.mWorkSpecId);
        if (currentState != null) {
            resolve(!currentState.isFinished());
        } else {
            resolve(false);
        }
        return true;
    }

    private void resolve(final boolean needsReschedule) {
        this.mWorkDatabase.beginTransaction();
        try {
            boolean hasUnfinishedWork = this.mWorkDatabase.workSpecDao().hasUnfinishedWork();
            if (!hasUnfinishedWork) {
                PackageManagerHelper.setComponentEnabled(this.mAppContext, RescheduleReceiver.class, false);
            }
            if (needsReschedule) {
                this.mWorkSpecDao.setState(WorkInfo.State.ENQUEUED, this.mWorkSpecId);
                this.mWorkSpecDao.setStopReason(this.mWorkSpecId, this.mInterrupted);
                this.mWorkSpecDao.markWorkSpecScheduled(this.mWorkSpecId, -1L);
            }
            this.mWorkDatabase.setTransactionSuccessful();
            this.mWorkDatabase.endTransaction();
            this.mFuture.set(Boolean.valueOf(needsReschedule));
        } catch (Throwable th) {
            this.mWorkDatabase.endTransaction();
            throw th;
        }
    }

    private void handleResult(ListenableWorker.Result result) {
        if (result instanceof ListenableWorker.Result.Success) {
            Logger.get().info(TAG, "Worker result SUCCESS for " + this.mWorkDescription);
            if (this.mWorkSpec.isPeriodic()) {
                resetPeriodicAndResolve();
                return;
            } else {
                setSucceededAndResolve();
                return;
            }
        }
        if (result instanceof ListenableWorker.Result.Retry) {
            Logger.get().info(TAG, "Worker result RETRY for " + this.mWorkDescription);
            rescheduleAndResolve();
            return;
        }
        Logger.get().info(TAG, "Worker result FAILURE for " + this.mWorkDescription);
        if (this.mWorkSpec.isPeriodic()) {
            resetPeriodicAndResolve();
        } else {
            setFailedAndResolve();
        }
    }

    private boolean trySetRunning() {
        boolean setToRunning = false;
        this.mWorkDatabase.beginTransaction();
        try {
            WorkInfo.State currentState = this.mWorkSpecDao.getState(this.mWorkSpecId);
            if (currentState == WorkInfo.State.ENQUEUED) {
                this.mWorkSpecDao.setState(WorkInfo.State.RUNNING, this.mWorkSpecId);
                this.mWorkSpecDao.incrementWorkSpecRunAttemptCount(this.mWorkSpecId);
                this.mWorkSpecDao.setStopReason(this.mWorkSpecId, -256);
                setToRunning = true;
            }
            this.mWorkDatabase.setTransactionSuccessful();
            return setToRunning;
        } finally {
            this.mWorkDatabase.endTransaction();
        }
    }

    void setFailedAndResolve() {
        this.mWorkDatabase.beginTransaction();
        try {
            iterativelyFailWorkAndDependents(this.mWorkSpecId);
            ListenableWorker.Result.Failure failure = (ListenableWorker.Result.Failure) this.mResult;
            Data output = failure.getOutputData();
            this.mWorkSpecDao.resetWorkSpecNextScheduleTimeOverride(this.mWorkSpecId, this.mWorkSpec.getNextScheduleTimeOverrideGeneration());
            this.mWorkSpecDao.setOutput(this.mWorkSpecId, output);
            this.mWorkDatabase.setTransactionSuccessful();
        } finally {
            this.mWorkDatabase.endTransaction();
            resolve(false);
        }
    }

    private void iterativelyFailWorkAndDependents(String workSpecId) {
        LinkedList<String> idsToProcess = new LinkedList<>();
        idsToProcess.add(workSpecId);
        while (!idsToProcess.isEmpty()) {
            String id = idsToProcess.remove();
            if (this.mWorkSpecDao.getState(id) != WorkInfo.State.CANCELLED) {
                this.mWorkSpecDao.setState(WorkInfo.State.FAILED, id);
            }
            idsToProcess.addAll(this.mDependencyDao.getDependentWorkIds(id));
        }
    }

    private void rescheduleAndResolve() {
        this.mWorkDatabase.beginTransaction();
        try {
            this.mWorkSpecDao.setState(WorkInfo.State.ENQUEUED, this.mWorkSpecId);
            this.mWorkSpecDao.setLastEnqueueTime(this.mWorkSpecId, this.mClock.currentTimeMillis());
            this.mWorkSpecDao.resetWorkSpecNextScheduleTimeOverride(this.mWorkSpecId, this.mWorkSpec.getNextScheduleTimeOverrideGeneration());
            this.mWorkSpecDao.markWorkSpecScheduled(this.mWorkSpecId, -1L);
            this.mWorkDatabase.setTransactionSuccessful();
        } finally {
            this.mWorkDatabase.endTransaction();
            resolve(true);
        }
    }

    private void resetPeriodicAndResolve() {
        this.mWorkDatabase.beginTransaction();
        try {
            this.mWorkSpecDao.setLastEnqueueTime(this.mWorkSpecId, this.mClock.currentTimeMillis());
            this.mWorkSpecDao.setState(WorkInfo.State.ENQUEUED, this.mWorkSpecId);
            this.mWorkSpecDao.resetWorkSpecRunAttemptCount(this.mWorkSpecId);
            this.mWorkSpecDao.resetWorkSpecNextScheduleTimeOverride(this.mWorkSpecId, this.mWorkSpec.getNextScheduleTimeOverrideGeneration());
            this.mWorkSpecDao.incrementPeriodCount(this.mWorkSpecId);
            this.mWorkSpecDao.markWorkSpecScheduled(this.mWorkSpecId, -1L);
            this.mWorkDatabase.setTransactionSuccessful();
        } finally {
            this.mWorkDatabase.endTransaction();
            resolve(false);
        }
    }

    private void setSucceededAndResolve() {
        this.mWorkDatabase.beginTransaction();
        try {
            this.mWorkSpecDao.setState(WorkInfo.State.SUCCEEDED, this.mWorkSpecId);
            ListenableWorker.Result.Success success = (ListenableWorker.Result.Success) this.mResult;
            Data output = success.getOutputData();
            this.mWorkSpecDao.setOutput(this.mWorkSpecId, output);
            long currentTimeMillis = this.mClock.currentTimeMillis();
            List<String> dependentWorkIds = this.mDependencyDao.getDependentWorkIds(this.mWorkSpecId);
            for (String dependentWorkId : dependentWorkIds) {
                if (this.mWorkSpecDao.getState(dependentWorkId) == WorkInfo.State.BLOCKED && this.mDependencyDao.hasCompletedAllPrerequisites(dependentWorkId)) {
                    Logger.get().info(TAG, "Setting status to enqueued for " + dependentWorkId);
                    this.mWorkSpecDao.setState(WorkInfo.State.ENQUEUED, dependentWorkId);
                    this.mWorkSpecDao.setLastEnqueueTime(dependentWorkId, currentTimeMillis);
                }
            }
            this.mWorkDatabase.setTransactionSuccessful();
        } finally {
            this.mWorkDatabase.endTransaction();
            resolve(false);
        }
    }

    private String createWorkDescription(List<String> tags) {
        StringBuilder sb = new StringBuilder("Work [ id=").append(this.mWorkSpecId).append(", tags={ ");
        boolean first = true;
        for (String tag : tags) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(tag);
        }
        sb.append(" } ]");
        return sb.toString();
    }

    public static class Builder {
        Context mAppContext;
        Configuration mConfiguration;
        ForegroundProcessor mForegroundProcessor;
        WorkerParameters.RuntimeExtras mRuntimeExtras = new WorkerParameters.RuntimeExtras();
        private final List<String> mTags;
        WorkDatabase mWorkDatabase;
        WorkSpec mWorkSpec;
        TaskExecutor mWorkTaskExecutor;
        ListenableWorker mWorker;

        public Builder(Context context, Configuration configuration, TaskExecutor workTaskExecutor, ForegroundProcessor foregroundProcessor, WorkDatabase database, WorkSpec workSpec, List<String> tags) {
            this.mAppContext = context.getApplicationContext();
            this.mWorkTaskExecutor = workTaskExecutor;
            this.mForegroundProcessor = foregroundProcessor;
            this.mConfiguration = configuration;
            this.mWorkDatabase = database;
            this.mWorkSpec = workSpec;
            this.mTags = tags;
        }

        public Builder withRuntimeExtras(WorkerParameters.RuntimeExtras runtimeExtras) {
            if (runtimeExtras != null) {
                this.mRuntimeExtras = runtimeExtras;
            }
            return this;
        }

        public Builder withWorker(ListenableWorker worker) {
            this.mWorker = worker;
            return this;
        }

        public WorkerWrapper build() {
            return new WorkerWrapper(this);
        }
    }
}
