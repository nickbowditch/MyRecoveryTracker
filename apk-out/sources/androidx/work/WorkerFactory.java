package androidx.work;

import android.content.Context;
import java.lang.reflect.Constructor;

/* loaded from: classes.dex */
public abstract class WorkerFactory {
    private static final String TAG = Logger.tagWithPrefix("WorkerFactory");

    public abstract ListenableWorker createWorker(Context appContext, String workerClassName, WorkerParameters workerParameters);

    public final ListenableWorker createWorkerWithDefaultFallback(Context appContext, String workerClassName, WorkerParameters workerParameters) {
        ListenableWorker worker = createWorker(appContext, workerClassName, workerParameters);
        if (worker == null) {
            Class clsAsSubclass = null;
            try {
                clsAsSubclass = Class.forName(workerClassName).asSubclass(ListenableWorker.class);
            } catch (Throwable throwable) {
                Logger.get().error(TAG, "Invalid class: " + workerClassName, throwable);
            }
            if (clsAsSubclass != null) {
                try {
                    Constructor<? extends ListenableWorker> constructor = clsAsSubclass.getDeclaredConstructor(Context.class, WorkerParameters.class);
                    worker = (ListenableWorker) constructor.newInstance(appContext, workerParameters);
                } catch (Throwable e) {
                    Logger.get().error(TAG, "Could not instantiate " + workerClassName, e);
                }
            }
        }
        if (worker != null && worker.isUsed()) {
            String factoryName = getClass().getName();
            String message = "WorkerFactory (" + factoryName + ") returned an instance of a ListenableWorker (" + workerClassName + ") which has already been invoked. createWorker() must always return a new instance of a ListenableWorker.";
            throw new IllegalStateException(message);
        }
        return worker;
    }

    public static WorkerFactory getDefaultWorkerFactory() {
        return new WorkerFactory() { // from class: androidx.work.WorkerFactory.1
            @Override // androidx.work.WorkerFactory
            public ListenableWorker createWorker(Context appContext, String workerClassName, WorkerParameters workerParameters) {
                return null;
            }
        };
    }
}
