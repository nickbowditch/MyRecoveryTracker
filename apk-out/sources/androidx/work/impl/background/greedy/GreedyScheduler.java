package androidx.work.impl.background.greedy;

import android.content.Context;
import android.text.TextUtils;
import androidx.work.Configuration;
import androidx.work.Logger;
import androidx.work.RunnableScheduler;
import androidx.work.WorkInfo;
import androidx.work.WorkRequest;
import androidx.work.impl.ExecutionListener;
import androidx.work.impl.Processor;
import androidx.work.impl.Scheduler;
import androidx.work.impl.StartStopToken;
import androidx.work.impl.StartStopTokens;
import androidx.work.impl.WorkLauncher;
import androidx.work.impl.constraints.ConstraintsState;
import androidx.work.impl.constraints.OnConstraintsStateChangedListener;
import androidx.work.impl.constraints.WorkConstraintsTracker;
import androidx.work.impl.constraints.WorkConstraintsTrackerKt;
import androidx.work.impl.constraints.trackers.Trackers;
import androidx.work.impl.model.WorkGenerationalId;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.model.WorkSpecKt;
import androidx.work.impl.utils.ProcessUtils;
import androidx.work.impl.utils.taskexecutor.TaskExecutor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import kotlinx.coroutines.Job;

/* loaded from: classes.dex */
public class GreedyScheduler implements Scheduler, OnConstraintsStateChangedListener, ExecutionListener {
    private static final int NON_THROTTLE_RUN_ATTEMPT_COUNT = 5;
    private static final String TAG = Logger.tagWithPrefix("GreedyScheduler");
    private final Configuration mConfiguration;
    private final WorkConstraintsTracker mConstraintsTracker;
    private final Context mContext;
    private DelayedWorkTracker mDelayedWorkTracker;
    Boolean mInDefaultProcess;
    private final Processor mProcessor;
    private boolean mRegisteredExecutionListener;
    private final TaskExecutor mTaskExecutor;
    private final TimeLimiter mTimeLimiter;
    private final WorkLauncher mWorkLauncher;
    private final Map<WorkGenerationalId, Job> mConstrainedWorkSpecs = new HashMap();
    private final Object mLock = new Object();
    private final StartStopTokens mStartStopTokens = new StartStopTokens();
    private final Map<WorkGenerationalId, AttemptData> mFirstRunAttempts = new HashMap();

    public GreedyScheduler(Context context, Configuration configuration, Trackers trackers, Processor processor, WorkLauncher workLauncher, TaskExecutor taskExecutor) {
        this.mContext = context;
        RunnableScheduler runnableScheduler = configuration.getRunnableScheduler();
        this.mDelayedWorkTracker = new DelayedWorkTracker(this, runnableScheduler, configuration.getClock());
        this.mTimeLimiter = new TimeLimiter(runnableScheduler, workLauncher);
        this.mTaskExecutor = taskExecutor;
        this.mConstraintsTracker = new WorkConstraintsTracker(trackers);
        this.mConfiguration = configuration;
        this.mProcessor = processor;
        this.mWorkLauncher = workLauncher;
    }

    public void setDelayedWorkTracker(DelayedWorkTracker delayedWorkTracker) {
        this.mDelayedWorkTracker = delayedWorkTracker;
    }

    @Override // androidx.work.impl.Scheduler
    public boolean hasLimitedSchedulingSlots() {
        return false;
    }

    @Override // androidx.work.impl.Scheduler
    public void schedule(WorkSpec... workSpecs) {
        int i;
        WorkSpec[] workSpecArr = workSpecs;
        if (this.mInDefaultProcess == null) {
            checkDefaultProcess();
        }
        if (!this.mInDefaultProcess.booleanValue()) {
            Logger.get().info(TAG, "Ignoring schedule request in a secondary process");
            return;
        }
        registerExecutionListenerIfNeeded();
        Set<WorkSpec> constrainedWorkSpecs = new HashSet<>();
        Set<String> constrainedWorkSpecIds = new HashSet<>();
        int length = workSpecArr.length;
        int i2 = 0;
        while (i2 < length) {
            WorkSpec workSpec = workSpecArr[i2];
            if (this.mStartStopTokens.contains(WorkSpecKt.generationalId(workSpec))) {
                i = length;
            } else {
                long throttled = throttleIfNeeded(workSpec);
                long nextRunTime = Math.max(workSpec.calculateNextRunTime(), throttled);
                long now = this.mConfiguration.getClock().currentTimeMillis();
                if (workSpec.state != WorkInfo.State.ENQUEUED) {
                    i = length;
                } else if (now < nextRunTime) {
                    if (this.mDelayedWorkTracker == null) {
                        i = length;
                    } else {
                        this.mDelayedWorkTracker.schedule(workSpec, nextRunTime);
                        i = length;
                    }
                } else if (workSpec.hasConstraints()) {
                    if (!workSpec.constraints.getRequiresDeviceIdle()) {
                        i = length;
                        if (workSpec.constraints.hasContentUriTriggers()) {
                            Logger.get().debug(TAG, "Ignoring " + workSpec + ". Requires ContentUri triggers.");
                        } else {
                            constrainedWorkSpecs.add(workSpec);
                            constrainedWorkSpecIds.add(workSpec.id);
                        }
                    } else {
                        i = length;
                        Logger.get().debug(TAG, "Ignoring " + workSpec + ". Requires device idle.");
                    }
                } else {
                    i = length;
                    if (!this.mStartStopTokens.contains(WorkSpecKt.generationalId(workSpec))) {
                        Logger.get().debug(TAG, "Starting work for " + workSpec.id);
                        StartStopToken token = this.mStartStopTokens.tokenFor(workSpec);
                        this.mTimeLimiter.track(token);
                        this.mWorkLauncher.startWork(token);
                    }
                }
            }
            i2++;
            workSpecArr = workSpecs;
            length = i;
        }
        synchronized (this.mLock) {
            if (!constrainedWorkSpecs.isEmpty()) {
                String formattedIds = TextUtils.join(",", constrainedWorkSpecIds);
                Logger.get().debug(TAG, "Starting tracking for " + formattedIds);
                for (WorkSpec spec : constrainedWorkSpecs) {
                    WorkGenerationalId id = WorkSpecKt.generationalId(spec);
                    if (!this.mConstrainedWorkSpecs.containsKey(id)) {
                        Job job = WorkConstraintsTrackerKt.listen(this.mConstraintsTracker, spec, this.mTaskExecutor.getTaskCoroutineDispatcher(), this);
                        this.mConstrainedWorkSpecs.put(id, job);
                    }
                }
            }
        }
    }

    private void checkDefaultProcess() {
        this.mInDefaultProcess = Boolean.valueOf(ProcessUtils.isDefaultProcess(this.mContext, this.mConfiguration));
    }

    @Override // androidx.work.impl.Scheduler
    public void cancel(String workSpecId) {
        if (this.mInDefaultProcess == null) {
            checkDefaultProcess();
        }
        if (!this.mInDefaultProcess.booleanValue()) {
            Logger.get().info(TAG, "Ignoring schedule request in non-main process");
            return;
        }
        registerExecutionListenerIfNeeded();
        Logger.get().debug(TAG, "Cancelling work ID " + workSpecId);
        if (this.mDelayedWorkTracker != null) {
            this.mDelayedWorkTracker.unschedule(workSpecId);
        }
        for (StartStopToken id : this.mStartStopTokens.remove(workSpecId)) {
            this.mTimeLimiter.cancel(id);
            this.mWorkLauncher.stopWork(id);
        }
    }

    @Override // androidx.work.impl.constraints.OnConstraintsStateChangedListener
    public void onConstraintsStateChanged(WorkSpec workSpec, ConstraintsState state) {
        WorkGenerationalId id = WorkSpecKt.generationalId(workSpec);
        if (state instanceof ConstraintsState.ConstraintsMet) {
            if (!this.mStartStopTokens.contains(id)) {
                Logger.get().debug(TAG, "Constraints met: Scheduling work ID " + id);
                StartStopToken token = this.mStartStopTokens.tokenFor(id);
                this.mTimeLimiter.track(token);
                this.mWorkLauncher.startWork(token);
                return;
            }
            return;
        }
        Logger.get().debug(TAG, "Constraints not met: Cancelling work ID " + id);
        StartStopToken runId = this.mStartStopTokens.remove(id);
        if (runId != null) {
            this.mTimeLimiter.cancel(runId);
            int reason = ((ConstraintsState.ConstraintsNotMet) state).getReason();
            this.mWorkLauncher.stopWorkWithReason(runId, reason);
        }
    }

    @Override // androidx.work.impl.ExecutionListener
    public void onExecuted(WorkGenerationalId id, boolean needsReschedule) {
        StartStopToken token = this.mStartStopTokens.remove(id);
        if (token != null) {
            this.mTimeLimiter.cancel(token);
        }
        removeConstraintTrackingFor(id);
        if (!needsReschedule) {
            synchronized (this.mLock) {
                this.mFirstRunAttempts.remove(id);
            }
        }
    }

    private void removeConstraintTrackingFor(WorkGenerationalId id) {
        Job job;
        synchronized (this.mLock) {
            job = this.mConstrainedWorkSpecs.remove(id);
        }
        if (job != null) {
            Logger.get().debug(TAG, "Stopping tracking for " + id);
            job.cancel((CancellationException) null);
        }
    }

    private void registerExecutionListenerIfNeeded() {
        if (!this.mRegisteredExecutionListener) {
            this.mProcessor.addExecutionListener(this);
            this.mRegisteredExecutionListener = true;
        }
    }

    private long throttleIfNeeded(WorkSpec workSpec) {
        long jMax;
        synchronized (this.mLock) {
            WorkGenerationalId id = WorkSpecKt.generationalId(workSpec);
            AttemptData firstRunAttempt = this.mFirstRunAttempts.get(id);
            if (firstRunAttempt == null) {
                firstRunAttempt = new AttemptData(workSpec.runAttemptCount, this.mConfiguration.getClock().currentTimeMillis());
                this.mFirstRunAttempts.put(id, firstRunAttempt);
            }
            jMax = firstRunAttempt.mTimeStamp + (Math.max((workSpec.runAttemptCount - firstRunAttempt.mRunAttemptCount) - 5, 0) * WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS);
        }
        return jMax;
    }

    private static class AttemptData {
        final int mRunAttemptCount;
        final long mTimeStamp;

        private AttemptData(int runAttemptCount, long timeStamp) {
            this.mRunAttemptCount = runAttemptCount;
            this.mTimeStamp = timeStamp;
        }
    }
}
