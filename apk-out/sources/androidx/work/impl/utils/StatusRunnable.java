package androidx.work.impl.utils;

import androidx.work.WorkInfo;
import androidx.work.WorkQuery;
import androidx.work.impl.WorkDatabase;
import androidx.work.impl.WorkManagerImpl;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.utils.futures.SettableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.UUID;

/* loaded from: classes.dex */
public abstract class StatusRunnable<T> implements Runnable {
    private final SettableFuture<T> mFuture = SettableFuture.create();

    abstract T runInternal();

    @Override // java.lang.Runnable
    public void run() {
        try {
            T value = runInternal();
            this.mFuture.set(value);
        } catch (Throwable throwable) {
            this.mFuture.setException(throwable);
        }
    }

    public ListenableFuture<T> getFuture() {
        return this.mFuture;
    }

    public static StatusRunnable<List<WorkInfo>> forStringIds(final WorkManagerImpl workManager, final List<String> ids) {
        return new StatusRunnable<List<WorkInfo>>() { // from class: androidx.work.impl.utils.StatusRunnable.1
            @Override // androidx.work.impl.utils.StatusRunnable
            public List<WorkInfo> runInternal() {
                WorkDatabase workDatabase = workManager.getWorkDatabase();
                List<WorkSpec.WorkInfoPojo> workInfoPojos = workDatabase.workSpecDao().getWorkStatusPojoForIds(ids);
                return WorkSpec.WORK_INFO_MAPPER.apply(workInfoPojos);
            }
        };
    }

    public static StatusRunnable<WorkInfo> forUUID(final WorkManagerImpl workManager, final UUID id) {
        return new StatusRunnable<WorkInfo>() { // from class: androidx.work.impl.utils.StatusRunnable.2
            /* JADX INFO: Access modifiers changed from: package-private */
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // androidx.work.impl.utils.StatusRunnable
            public WorkInfo runInternal() {
                WorkDatabase workDatabase = workManager.getWorkDatabase();
                WorkSpec.WorkInfoPojo workInfoPojo = workDatabase.workSpecDao().getWorkStatusPojoForId(id.toString());
                if (workInfoPojo != null) {
                    return workInfoPojo.toWorkInfo();
                }
                return null;
            }
        };
    }

    public static StatusRunnable<List<WorkInfo>> forTag(final WorkManagerImpl workManager, final String tag) {
        return new StatusRunnable<List<WorkInfo>>() { // from class: androidx.work.impl.utils.StatusRunnable.3
            /* JADX INFO: Access modifiers changed from: package-private */
            @Override // androidx.work.impl.utils.StatusRunnable
            public List<WorkInfo> runInternal() {
                WorkDatabase workDatabase = workManager.getWorkDatabase();
                List<WorkSpec.WorkInfoPojo> workInfoPojos = workDatabase.workSpecDao().getWorkStatusPojoForTag(tag);
                return WorkSpec.WORK_INFO_MAPPER.apply(workInfoPojos);
            }
        };
    }

    public static StatusRunnable<List<WorkInfo>> forUniqueWork(final WorkManagerImpl workManager, final String name) {
        return new StatusRunnable<List<WorkInfo>>() { // from class: androidx.work.impl.utils.StatusRunnable.4
            /* JADX INFO: Access modifiers changed from: package-private */
            @Override // androidx.work.impl.utils.StatusRunnable
            public List<WorkInfo> runInternal() {
                WorkDatabase workDatabase = workManager.getWorkDatabase();
                List<WorkSpec.WorkInfoPojo> workInfoPojos = workDatabase.workSpecDao().getWorkStatusPojoForName(name);
                return WorkSpec.WORK_INFO_MAPPER.apply(workInfoPojos);
            }
        };
    }

    public static StatusRunnable<List<WorkInfo>> forWorkQuerySpec(final WorkManagerImpl workManager, final WorkQuery querySpec) {
        return new StatusRunnable<List<WorkInfo>>() { // from class: androidx.work.impl.utils.StatusRunnable.5
            /* JADX INFO: Access modifiers changed from: package-private */
            @Override // androidx.work.impl.utils.StatusRunnable
            public List<WorkInfo> runInternal() {
                WorkDatabase workDatabase = workManager.getWorkDatabase();
                List<WorkSpec.WorkInfoPojo> workInfoPojos = workDatabase.rawWorkInfoDao().getWorkInfoPojos(RawQueries.toRawQuery(querySpec));
                return WorkSpec.WORK_INFO_MAPPER.apply(workInfoPojos);
            }
        };
    }
}
