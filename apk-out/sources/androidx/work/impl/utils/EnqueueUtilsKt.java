package androidx.work.impl.utils;

import android.os.Build;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Configuration;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.WorkRequest;
import androidx.work.impl.Scheduler;
import androidx.work.impl.WorkContinuationImpl;
import androidx.work.impl.WorkDatabase;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.workers.ConstraintTrackingWorker;
import androidx.work.impl.workers.ConstraintTrackingWorkerKt;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: EnqueueUtils.kt */
@Metadata(d1 = {"\u0000:\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u001a \u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0000\u001a\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0000\u001a\u001e\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002\u001a\u001e\u0010\u0012\u001a\u00020\t2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\u0006\u0010\n\u001a\u00020\tH\u0000¨\u0006\u0013"}, d2 = {"checkContentUriTriggerWorkerLimits", "", "workDatabase", "Landroidx/work/impl/WorkDatabase;", "configuration", "Landroidx/work/Configuration;", "continuation", "Landroidx/work/impl/WorkContinuationImpl;", "tryDelegateConstrainedWorkSpec", "Landroidx/work/impl/model/WorkSpec;", "workSpec", "usesScheduler", "", "schedulers", "", "Landroidx/work/impl/Scheduler;", "className", "", "wrapInConstraintTrackingWorkerIfNeeded", "work-runtime_release"}, k = 2, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class EnqueueUtilsKt {
    public static final void checkContentUriTriggerWorkerLimits(WorkDatabase workDatabase, Configuration configuration, WorkContinuationImpl continuation) {
        int count$iv;
        Intrinsics.checkNotNullParameter(workDatabase, "workDatabase");
        Intrinsics.checkNotNullParameter(configuration, "configuration");
        Intrinsics.checkNotNullParameter(continuation, "continuation");
        List continuations = CollectionsKt.mutableListOf(continuation);
        int newCount = 0;
        while (!continuations.isEmpty()) {
            WorkContinuationImpl current = (WorkContinuationImpl) CollectionsKt.removeLast(continuations);
            Iterable work = current.getWork();
            Intrinsics.checkNotNullExpressionValue(work, "current.work");
            Iterable $this$count$iv = work;
            if (($this$count$iv instanceof Collection) && ((Collection) $this$count$iv).isEmpty()) {
                count$iv = 0;
            } else {
                count$iv = 0;
                for (Object element$iv : $this$count$iv) {
                    if (((WorkRequest) element$iv).getWorkSpec().constraints.hasContentUriTriggers() && (count$iv = count$iv + 1) < 0) {
                        CollectionsKt.throwCountOverflow();
                    }
                }
            }
            newCount += count$iv;
            List it = current.getParents();
            if (it != null) {
                continuations.addAll(it);
            }
        }
        if (newCount == 0) {
            return;
        }
        int alreadyEnqueuedCount = workDatabase.workSpecDao().countNonFinishedContentUriTriggerWorkers();
        int limit = configuration.getContentUriTriggerWorkersLimit();
        if (alreadyEnqueuedCount + newCount > limit) {
            throw new IllegalArgumentException("Too many workers with contentUriTriggers are enqueued:\ncontentUriTrigger workers limit: " + limit + ";\nalready enqueued count: " + alreadyEnqueuedCount + ";\ncurrent enqueue operation count: " + newCount + ".\nTo address this issue you can: \n1. enqueue less workers or batch some of workers with content uri triggers together;\n2. increase limit via Configuration.Builder.setContentUriTriggerWorkersLimit;\nPlease beware that workers with content uri triggers immediately occupy slots in JobScheduler so no updates to content uris are missed.");
        }
    }

    public static final WorkSpec tryDelegateConstrainedWorkSpec(WorkSpec workSpec) throws IOException {
        Intrinsics.checkNotNullParameter(workSpec, "workSpec");
        Constraints constraints = workSpec.constraints;
        String workerClassName = workSpec.workerClassName;
        boolean isConstraintTrackingWorker = Intrinsics.areEqual(workerClassName, ConstraintTrackingWorker.class.getName());
        if (!isConstraintTrackingWorker && (constraints.getRequiresBatteryNotLow() || constraints.getRequiresStorageNotLow())) {
            Data newInputData = new Data.Builder().putAll(workSpec.input).putString(ConstraintTrackingWorkerKt.ARGUMENT_CLASS_NAME, workerClassName).build();
            Intrinsics.checkNotNullExpressionValue(newInputData, "Builder().putAll(workSpe…ame)\n            .build()");
            String name = ConstraintTrackingWorker.class.getName();
            Intrinsics.checkNotNullExpressionValue(name, "name");
            return WorkSpec.copy$default(workSpec, null, null, name, null, newInputData, null, 0L, 0L, 0L, null, 0, null, 0L, 0L, 0L, 0L, false, null, 0, 0, 0L, 0, 0, 8388587, null);
        }
        return workSpec;
    }

    public static final WorkSpec wrapInConstraintTrackingWorkerIfNeeded(List<? extends Scheduler> schedulers, WorkSpec workSpec) {
        Intrinsics.checkNotNullParameter(schedulers, "schedulers");
        Intrinsics.checkNotNullParameter(workSpec, "workSpec");
        if (Build.VERSION.SDK_INT < 26) {
            return tryDelegateConstrainedWorkSpec(workSpec);
        }
        return workSpec;
    }

    private static final boolean usesScheduler(List<? extends Scheduler> list, String className) throws ClassNotFoundException {
        try {
            Class klass = Class.forName(className);
            List<? extends Scheduler> $this$any$iv = list;
            if (($this$any$iv instanceof Collection) && $this$any$iv.isEmpty()) {
                return false;
            }
            for (Object element$iv : $this$any$iv) {
                Scheduler scheduler = (Scheduler) element$iv;
                if (klass.isAssignableFrom(scheduler.getClass())) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
