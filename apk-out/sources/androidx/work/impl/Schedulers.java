package androidx.work.impl;

import android.content.Context;
import androidx.work.Clock;
import androidx.work.Configuration;
import androidx.work.Logger;
import androidx.work.impl.background.systemjob.SystemJobScheduler;
import androidx.work.impl.background.systemjob.SystemJobService;
import androidx.work.impl.model.WorkGenerationalId;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.model.WorkSpecDao;
import androidx.work.impl.utils.PackageManagerHelper;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class Schedulers {
    public static final String GCM_SCHEDULER = "androidx.work.impl.background.gcm.GcmScheduler";
    private static final String TAG = Logger.tagWithPrefix("Schedulers");

    public static void registerRescheduling(final List<Scheduler> schedulers, Processor processor, final Executor executor, final WorkDatabase workDatabase, final Configuration configuration) {
        processor.addExecutionListener(new ExecutionListener() { // from class: androidx.work.impl.Schedulers$$ExternalSyntheticLambda0
            @Override // androidx.work.impl.ExecutionListener
            public final void onExecuted(WorkGenerationalId workGenerationalId, boolean z) {
                executor.execute(new Runnable() { // from class: androidx.work.impl.Schedulers$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        Schedulers.lambda$registerRescheduling$0(list, workGenerationalId, configuration, workDatabase);
                    }
                });
            }
        });
    }

    static /* synthetic */ void lambda$registerRescheduling$0(List schedulers, WorkGenerationalId id, Configuration configuration, WorkDatabase workDatabase) {
        Iterator it = schedulers.iterator();
        while (it.hasNext()) {
            Scheduler scheduler = (Scheduler) it.next();
            scheduler.cancel(id.getWorkSpecId());
        }
        schedule(configuration, workDatabase, schedulers);
    }

    public static void schedule(Configuration configuration, WorkDatabase workDatabase, List<Scheduler> schedulers) {
        if (schedulers == null || schedulers.size() == 0) {
            return;
        }
        WorkSpecDao workSpecDao = workDatabase.workSpecDao();
        workDatabase.beginTransaction();
        try {
            List<WorkSpec> contentUriWorkSpecs = workSpecDao.getEligibleWorkForSchedulingWithContentUris();
            markScheduled(workSpecDao, configuration.getClock(), contentUriWorkSpecs);
            List<WorkSpec> eligibleWorkSpecsForLimitedSlots = workSpecDao.getEligibleWorkForScheduling(configuration.getMaxSchedulerLimit());
            markScheduled(workSpecDao, configuration.getClock(), eligibleWorkSpecsForLimitedSlots);
            if (contentUriWorkSpecs != null) {
                eligibleWorkSpecsForLimitedSlots.addAll(contentUriWorkSpecs);
            }
            List<WorkSpec> allEligibleWorkSpecs = workSpecDao.getAllEligibleWorkSpecsForScheduling(200);
            workDatabase.setTransactionSuccessful();
            workDatabase.endTransaction();
            if (eligibleWorkSpecsForLimitedSlots.size() > 0) {
                WorkSpec[] eligibleWorkSpecsArray = new WorkSpec[eligibleWorkSpecsForLimitedSlots.size()];
                WorkSpec[] eligibleWorkSpecsArray2 = (WorkSpec[]) eligibleWorkSpecsForLimitedSlots.toArray(eligibleWorkSpecsArray);
                for (Scheduler scheduler : schedulers) {
                    if (scheduler.hasLimitedSchedulingSlots()) {
                        scheduler.schedule(eligibleWorkSpecsArray2);
                    }
                }
            }
            if (allEligibleWorkSpecs.size() > 0) {
                WorkSpec[] enqueuedWorkSpecsArray = new WorkSpec[allEligibleWorkSpecs.size()];
                WorkSpec[] enqueuedWorkSpecsArray2 = (WorkSpec[]) allEligibleWorkSpecs.toArray(enqueuedWorkSpecsArray);
                for (Scheduler scheduler2 : schedulers) {
                    if (!scheduler2.hasLimitedSchedulingSlots()) {
                        scheduler2.schedule(enqueuedWorkSpecsArray2);
                    }
                }
            }
        } catch (Throwable th) {
            workDatabase.endTransaction();
            throw th;
        }
    }

    static Scheduler createBestAvailableBackgroundScheduler(Context context, WorkDatabase workDatabase, Configuration configuration) {
        Scheduler scheduler = new SystemJobScheduler(context, workDatabase, configuration);
        PackageManagerHelper.setComponentEnabled(context, SystemJobService.class, true);
        Logger.get().debug(TAG, "Created SystemJobScheduler and enabled SystemJobService");
        return scheduler;
    }

    private static Scheduler tryCreateGcmBasedScheduler(Context context, Clock clock) {
        try {
            Class<?> klass = Class.forName(GCM_SCHEDULER);
            Scheduler scheduler = (Scheduler) klass.getConstructor(Context.class, Clock.class).newInstance(context, clock);
            Logger.get().debug(TAG, "Created androidx.work.impl.background.gcm.GcmScheduler");
            return scheduler;
        } catch (Throwable throwable) {
            Logger.get().debug(TAG, "Unable to create GCM Scheduler", throwable);
            return null;
        }
    }

    private Schedulers() {
    }

    private static void markScheduled(WorkSpecDao dao, Clock clock, List<WorkSpec> workSpecs) {
        if (workSpecs.size() > 0) {
            long now = clock.currentTimeMillis();
            for (WorkSpec workSpec : workSpecs) {
                dao.markWorkSpecScheduled(workSpec.id, now);
            }
        }
    }
}
