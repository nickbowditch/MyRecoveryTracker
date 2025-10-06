package androidx.work.impl.utils;

import android.content.Context;
import android.os.Build;
import androidx.work.ForegroundInfo;
import androidx.work.ForegroundUpdater;
import androidx.work.ListenableWorker;
import androidx.work.Logger;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.utils.futures.SettableFuture;
import androidx.work.impl.utils.taskexecutor.TaskExecutor;
import com.google.common.util.concurrent.ListenableFuture;

/* loaded from: classes.dex */
public class WorkForegroundRunnable implements Runnable {
    static final String TAG = Logger.tagWithPrefix("WorkForegroundRunnable");
    final Context mContext;
    final ForegroundUpdater mForegroundUpdater;
    final SettableFuture<Void> mFuture = SettableFuture.create();
    final TaskExecutor mTaskExecutor;
    final WorkSpec mWorkSpec;
    final ListenableWorker mWorker;

    public WorkForegroundRunnable(Context context, WorkSpec workSpec, ListenableWorker worker, ForegroundUpdater foregroundUpdater, TaskExecutor taskExecutor) {
        this.mContext = context;
        this.mWorkSpec = workSpec;
        this.mWorker = worker;
        this.mForegroundUpdater = foregroundUpdater;
        this.mTaskExecutor = taskExecutor;
    }

    public ListenableFuture<Void> getFuture() {
        return this.mFuture;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (!this.mWorkSpec.expedited || Build.VERSION.SDK_INT >= 31) {
            this.mFuture.set(null);
            return;
        }
        final SettableFuture<ForegroundInfo> foregroundFuture = SettableFuture.create();
        this.mTaskExecutor.getMainThreadExecutor().execute(new Runnable() { // from class: androidx.work.impl.utils.WorkForegroundRunnable$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m109lambda$run$0$androidxworkimplutilsWorkForegroundRunnable(foregroundFuture);
            }
        });
        foregroundFuture.addListener(new Runnable() { // from class: androidx.work.impl.utils.WorkForegroundRunnable.1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.lang.Runnable
            public void run() {
                if (WorkForegroundRunnable.this.mFuture.isCancelled()) {
                    return;
                }
                try {
                    ForegroundInfo foregroundInfo = (ForegroundInfo) foregroundFuture.get();
                    if (foregroundInfo == null) {
                        String message = "Worker was marked important (" + WorkForegroundRunnable.this.mWorkSpec.workerClassName + ") but did not provide ForegroundInfo";
                        throw new IllegalStateException(message);
                    }
                    Logger.get().debug(WorkForegroundRunnable.TAG, "Updating notification for " + WorkForegroundRunnable.this.mWorkSpec.workerClassName);
                    WorkForegroundRunnable.this.mFuture.setFuture(WorkForegroundRunnable.this.mForegroundUpdater.setForegroundAsync(WorkForegroundRunnable.this.mContext, WorkForegroundRunnable.this.mWorker.getId(), foregroundInfo));
                } catch (Throwable throwable) {
                    WorkForegroundRunnable.this.mFuture.setException(throwable);
                }
            }
        }, this.mTaskExecutor.getMainThreadExecutor());
    }

    /* renamed from: lambda$run$0$androidx-work-impl-utils-WorkForegroundRunnable, reason: not valid java name */
    /* synthetic */ void m109lambda$run$0$androidxworkimplutilsWorkForegroundRunnable(SettableFuture foregroundFuture) {
        if (!this.mFuture.isCancelled()) {
            foregroundFuture.setFuture(this.mWorker.getForegroundInfoAsync());
        } else {
            foregroundFuture.cancel(true);
        }
    }
}
