package androidx.work.impl.utils;

import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.impl.OperationImpl;
import androidx.work.impl.Processor;
import androidx.work.impl.Scheduler;
import androidx.work.impl.Schedulers;
import androidx.work.impl.WorkDatabase;
import androidx.work.impl.WorkManagerImpl;
import androidx.work.impl.model.DependencyDao;
import androidx.work.impl.model.WorkSpecDao;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/* loaded from: classes.dex */
public abstract class CancelWorkRunnable implements Runnable {
    private final OperationImpl mOperation = new OperationImpl();

    abstract void runInternal();

    public Operation getOperation() {
        return this.mOperation;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            runInternal();
            this.mOperation.markState(Operation.SUCCESS);
        } catch (Throwable throwable) {
            this.mOperation.markState(new Operation.State.FAILURE(throwable));
        }
    }

    void cancel(WorkManagerImpl workManagerImpl, String workSpecId) {
        iterativelyCancelWorkAndDependents(workManagerImpl.getWorkDatabase(), workSpecId);
        Processor processor = workManagerImpl.getProcessor();
        processor.stopAndCancelWork(workSpecId, 1);
        for (Scheduler scheduler : workManagerImpl.getSchedulers()) {
            scheduler.cancel(workSpecId);
        }
    }

    void reschedulePendingWorkers(WorkManagerImpl workManagerImpl) {
        Schedulers.schedule(workManagerImpl.getConfiguration(), workManagerImpl.getWorkDatabase(), workManagerImpl.getSchedulers());
    }

    private void iterativelyCancelWorkAndDependents(WorkDatabase workDatabase, String workSpecId) {
        WorkSpecDao workSpecDao = workDatabase.workSpecDao();
        DependencyDao dependencyDao = workDatabase.dependencyDao();
        LinkedList<String> idsToProcess = new LinkedList<>();
        idsToProcess.add(workSpecId);
        while (!idsToProcess.isEmpty()) {
            String id = idsToProcess.remove();
            WorkInfo.State state = workSpecDao.getState(id);
            if (state != WorkInfo.State.SUCCEEDED && state != WorkInfo.State.FAILED) {
                workSpecDao.setCancelledState(id);
            }
            idsToProcess.addAll(dependencyDao.getDependentWorkIds(id));
        }
    }

    public static CancelWorkRunnable forId(final UUID id, final WorkManagerImpl workManagerImpl) {
        return new CancelWorkRunnable() { // from class: androidx.work.impl.utils.CancelWorkRunnable.1
            @Override // androidx.work.impl.utils.CancelWorkRunnable
            void runInternal() {
                WorkDatabase workDatabase = workManagerImpl.getWorkDatabase();
                workDatabase.beginTransaction();
                try {
                    cancel(workManagerImpl, id.toString());
                    workDatabase.setTransactionSuccessful();
                    workDatabase.endTransaction();
                    reschedulePendingWorkers(workManagerImpl);
                } catch (Throwable th) {
                    workDatabase.endTransaction();
                    throw th;
                }
            }
        };
    }

    public static CancelWorkRunnable forTag(final String tag, final WorkManagerImpl workManagerImpl) {
        return new CancelWorkRunnable() { // from class: androidx.work.impl.utils.CancelWorkRunnable.2
            @Override // androidx.work.impl.utils.CancelWorkRunnable
            void runInternal() {
                WorkDatabase workDatabase = workManagerImpl.getWorkDatabase();
                workDatabase.beginTransaction();
                try {
                    WorkSpecDao workSpecDao = workDatabase.workSpecDao();
                    List<String> workSpecIds = workSpecDao.getUnfinishedWorkWithTag(tag);
                    for (String workSpecId : workSpecIds) {
                        cancel(workManagerImpl, workSpecId);
                    }
                    workDatabase.setTransactionSuccessful();
                    workDatabase.endTransaction();
                    reschedulePendingWorkers(workManagerImpl);
                } catch (Throwable th) {
                    workDatabase.endTransaction();
                    throw th;
                }
            }
        };
    }

    public static CancelWorkRunnable forName(final String name, final WorkManagerImpl workManagerImpl, final boolean allowReschedule) {
        return new CancelWorkRunnable() { // from class: androidx.work.impl.utils.CancelWorkRunnable.3
            @Override // androidx.work.impl.utils.CancelWorkRunnable
            void runInternal() {
                WorkDatabase workDatabase = workManagerImpl.getWorkDatabase();
                workDatabase.beginTransaction();
                try {
                    WorkSpecDao workSpecDao = workDatabase.workSpecDao();
                    List<String> workSpecIds = workSpecDao.getUnfinishedWorkWithName(name);
                    for (String workSpecId : workSpecIds) {
                        cancel(workManagerImpl, workSpecId);
                    }
                    workDatabase.setTransactionSuccessful();
                    workDatabase.endTransaction();
                    if (allowReschedule) {
                        reschedulePendingWorkers(workManagerImpl);
                    }
                } catch (Throwable th) {
                    workDatabase.endTransaction();
                    throw th;
                }
            }
        };
    }

    public static CancelWorkRunnable forAll(final WorkManagerImpl workManagerImpl) {
        return new CancelWorkRunnable() { // from class: androidx.work.impl.utils.CancelWorkRunnable.4
            @Override // androidx.work.impl.utils.CancelWorkRunnable
            void runInternal() {
                WorkDatabase workDatabase = workManagerImpl.getWorkDatabase();
                workDatabase.beginTransaction();
                try {
                    WorkSpecDao workSpecDao = workDatabase.workSpecDao();
                    List<String> workSpecIds = workSpecDao.getAllUnfinishedWork();
                    for (String workSpecId : workSpecIds) {
                        cancel(workManagerImpl, workSpecId);
                    }
                    new PreferenceUtils(workManagerImpl.getWorkDatabase()).setLastCancelAllTimeMillis(workManagerImpl.getConfiguration().getClock().currentTimeMillis());
                    workDatabase.setTransactionSuccessful();
                } finally {
                    workDatabase.endTransaction();
                }
            }
        };
    }
}
