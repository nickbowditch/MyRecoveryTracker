package androidx.work.impl.utils;

import android.content.Context;
import android.content.Intent;
import androidx.work.ForegroundInfo;
import androidx.work.ForegroundUpdater;
import androidx.work.Logger;
import androidx.work.impl.WorkDatabase;
import androidx.work.impl.foreground.ForegroundProcessor;
import androidx.work.impl.foreground.SystemForegroundDispatcher;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.model.WorkSpecDao;
import androidx.work.impl.model.WorkSpecKt;
import androidx.work.impl.utils.futures.SettableFuture;
import androidx.work.impl.utils.taskexecutor.TaskExecutor;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.UUID;

/* loaded from: classes.dex */
public class WorkForegroundUpdater implements ForegroundUpdater {
    private static final String TAG = Logger.tagWithPrefix("WMFgUpdater");
    final ForegroundProcessor mForegroundProcessor;
    private final TaskExecutor mTaskExecutor;
    final WorkSpecDao mWorkSpecDao;

    public WorkForegroundUpdater(WorkDatabase workDatabase, ForegroundProcessor foregroundProcessor, TaskExecutor taskExecutor) {
        this.mForegroundProcessor = foregroundProcessor;
        this.mTaskExecutor = taskExecutor;
        this.mWorkSpecDao = workDatabase.workSpecDao();
    }

    @Override // androidx.work.ForegroundUpdater
    public ListenableFuture<Void> setForegroundAsync(final Context context, final UUID id, final ForegroundInfo foregroundInfo) {
        final SettableFuture<Void> future = SettableFuture.create();
        this.mTaskExecutor.executeOnTaskThread(new Runnable() { // from class: androidx.work.impl.utils.WorkForegroundUpdater.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (!future.isCancelled()) {
                        String workSpecId = id.toString();
                        WorkSpec workSpec = WorkForegroundUpdater.this.mWorkSpecDao.getWorkSpec(workSpecId);
                        if (workSpec == null || workSpec.state.isFinished()) {
                            throw new IllegalStateException("Calls to setForegroundAsync() must complete before a ListenableWorker signals completion of work by returning an instance of Result.");
                        }
                        WorkForegroundUpdater.this.mForegroundProcessor.startForeground(workSpecId, foregroundInfo);
                        Intent intent = SystemForegroundDispatcher.createNotifyIntent(context, WorkSpecKt.generationalId(workSpec), foregroundInfo);
                        context.startService(intent);
                    }
                    future.set(null);
                } catch (Throwable throwable) {
                    future.setException(throwable);
                }
            }
        });
        return future;
    }
}
