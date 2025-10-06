package androidx.work.impl.background.greedy;

import androidx.work.Clock;
import androidx.work.Logger;
import androidx.work.RunnableScheduler;
import androidx.work.impl.Scheduler;
import androidx.work.impl.model.WorkSpec;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class DelayedWorkTracker {
    static final String TAG = Logger.tagWithPrefix("DelayedWorkTracker");
    private final Clock mClock;
    final Scheduler mImmediateScheduler;
    private final RunnableScheduler mRunnableScheduler;
    private final Map<String, Runnable> mRunnables = new HashMap();

    public DelayedWorkTracker(Scheduler immediateScheduler, RunnableScheduler runnableScheduler, Clock clock) {
        this.mImmediateScheduler = immediateScheduler;
        this.mRunnableScheduler = runnableScheduler;
        this.mClock = clock;
    }

    public void schedule(final WorkSpec workSpec, long nextRunTime) {
        Runnable existing = this.mRunnables.remove(workSpec.id);
        if (existing != null) {
            this.mRunnableScheduler.cancel(existing);
        }
        Runnable runnable = new Runnable() { // from class: androidx.work.impl.background.greedy.DelayedWorkTracker.1
            @Override // java.lang.Runnable
            public void run() {
                Logger.get().debug(DelayedWorkTracker.TAG, "Scheduling work " + workSpec.id);
                DelayedWorkTracker.this.mImmediateScheduler.schedule(workSpec);
            }
        };
        this.mRunnables.put(workSpec.id, runnable);
        long now = this.mClock.currentTimeMillis();
        long delay = nextRunTime - now;
        this.mRunnableScheduler.scheduleWithDelay(delay, runnable);
    }

    public void unschedule(String workSpecId) {
        Runnable runnable = this.mRunnables.remove(workSpecId);
        if (runnable != null) {
            this.mRunnableScheduler.cancel(runnable);
        }
    }
}
