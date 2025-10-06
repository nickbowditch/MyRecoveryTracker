package androidx.work.impl.background.systemalarm;

import android.content.Context;
import android.content.Intent;
import androidx.work.Logger;
import androidx.work.impl.Scheduler;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.model.WorkSpecKt;

/* loaded from: classes.dex */
public class SystemAlarmScheduler implements Scheduler {
    private static final String TAG = Logger.tagWithPrefix("SystemAlarmScheduler");
    private final Context mContext;

    public SystemAlarmScheduler(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Override // androidx.work.impl.Scheduler
    public void schedule(WorkSpec... workSpecs) {
        for (WorkSpec workSpec : workSpecs) {
            scheduleWorkSpec(workSpec);
        }
    }

    @Override // androidx.work.impl.Scheduler
    public void cancel(String workSpecId) {
        Intent cancelIntent = CommandHandler.createStopWorkIntent(this.mContext, workSpecId);
        this.mContext.startService(cancelIntent);
    }

    @Override // androidx.work.impl.Scheduler
    public boolean hasLimitedSchedulingSlots() {
        return true;
    }

    private void scheduleWorkSpec(WorkSpec workSpec) {
        Logger.get().debug(TAG, "Scheduling work with workSpecId " + workSpec.id);
        Intent scheduleIntent = CommandHandler.createScheduleWorkIntent(this.mContext, WorkSpecKt.generationalId(workSpec));
        this.mContext.startService(scheduleIntent);
    }
}
