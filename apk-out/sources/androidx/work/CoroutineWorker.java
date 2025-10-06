package androidx.work;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ListenableWorker;
import androidx.work.impl.utils.futures.SettableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import kotlin.Deprecated;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.CancellableContinuationImpl;
import kotlinx.coroutines.CompletableJob;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.JobKt__JobKt;

/* compiled from: CoroutineWorker.kt */
@Metadata(d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\b&\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0002\u0010\u0006J\u0011\u0010\u0016\u001a\u00020\u000fH¦@ø\u0001\u0000¢\u0006\u0002\u0010\u0017J\u0011\u0010\u0018\u001a\u00020\u0019H\u0096@ø\u0001\u0000¢\u0006\u0002\u0010\u0017J\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00190\u001bJ\u0006\u0010\u001c\u001a\u00020\u001dJ\u0019\u0010\u001e\u001a\u00020\u001d2\u0006\u0010\u001f\u001a\u00020\u0019H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010 J\u0019\u0010!\u001a\u00020\u001d2\u0006\u0010\"\u001a\u00020#H\u0086@ø\u0001\u0000¢\u0006\u0002\u0010$J\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u000f0\u001bR\u001c\u0010\u0007\u001a\u00020\b8\u0016X\u0097\u0004¢\u0006\u000e\n\u0000\u0012\u0004\b\t\u0010\n\u001a\u0004\b\u000b\u0010\fR\u001a\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0080\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0014\u0010\u0012\u001a\u00020\u0013X\u0080\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006&"}, d2 = {"Landroidx/work/CoroutineWorker;", "Landroidx/work/ListenableWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "coroutineContext", "Lkotlinx/coroutines/CoroutineDispatcher;", "getCoroutineContext$annotations", "()V", "getCoroutineContext", "()Lkotlinx/coroutines/CoroutineDispatcher;", "future", "Landroidx/work/impl/utils/futures/SettableFuture;", "Landroidx/work/ListenableWorker$Result;", "getFuture$work_runtime_release", "()Landroidx/work/impl/utils/futures/SettableFuture;", "job", "Lkotlinx/coroutines/CompletableJob;", "getJob$work_runtime_release", "()Lkotlinx/coroutines/CompletableJob;", "doWork", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getForegroundInfo", "Landroidx/work/ForegroundInfo;", "getForegroundInfoAsync", "Lcom/google/common/util/concurrent/ListenableFuture;", "onStopped", "", "setForeground", "foregroundInfo", "(Landroidx/work/ForegroundInfo;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setProgress", "data", "Landroidx/work/Data;", "(Landroidx/work/Data;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "startWork", "work-runtime_release"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public abstract class CoroutineWorker extends ListenableWorker {
    private final CoroutineDispatcher coroutineContext;
    private final SettableFuture<ListenableWorker.Result> future;
    private final CompletableJob job;

    @Deprecated(message = "use withContext(...) inside doWork() instead.")
    public static /* synthetic */ void getCoroutineContext$annotations() {
    }

    public abstract Object doWork(Continuation<? super ListenableWorker.Result> continuation);

    public Object getForegroundInfo(Continuation<? super ForegroundInfo> continuation) {
        return getForegroundInfo$suspendImpl(this, continuation);
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public CoroutineWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        this.job = JobKt__JobKt.Job$default((Job) null, 1, (Object) null);
        SettableFuture<ListenableWorker.Result> settableFutureCreate = SettableFuture.create();
        Intrinsics.checkNotNullExpressionValue(settableFutureCreate, "create()");
        this.future = settableFutureCreate;
        this.future.addListener(new Runnable() { // from class: androidx.work.CoroutineWorker$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                CoroutineWorker._init_$lambda$0(this.f$0);
            }
        }, getTaskExecutor().getSerialTaskExecutor());
        this.coroutineContext = Dispatchers.getDefault();
    }

    /* renamed from: getJob$work_runtime_release, reason: from getter */
    public final CompletableJob getJob() {
        return this.job;
    }

    public final SettableFuture<ListenableWorker.Result> getFuture$work_runtime_release() {
        return this.future;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void _init_$lambda$0(CoroutineWorker this$0) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        if (this$0.future.isCancelled()) {
            Job.DefaultImpls.cancel$default((Job) this$0.job, (CancellationException) null, 1, (Object) null);
        }
    }

    public CoroutineDispatcher getCoroutineContext() {
        return this.coroutineContext;
    }

    /* compiled from: CoroutineWorker.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "androidx.work.CoroutineWorker$startWork$1", f = "CoroutineWorker.kt", i = {}, l = {68}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: androidx.work.CoroutineWorker$startWork$1, reason: invalid class name and case insensitive filesystem */
    static final class C00341 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        int label;

        C00341(Continuation<? super C00341> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return CoroutineWorker.this.new C00341(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((C00341) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object $result) throws Throwable {
            C00341 c00341;
            Throwable t;
            C00341 c003412;
            Object objDoWork;
            Object $result2;
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    c00341 = this;
                    try {
                        c00341.label = 1;
                        objDoWork = CoroutineWorker.this.doWork(c00341);
                    } catch (Throwable th) {
                        t = th;
                        c003412 = c00341;
                        CoroutineWorker.this.getFuture$work_runtime_release().setException(t);
                        return Unit.INSTANCE;
                    }
                    if (objDoWork == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                    $result2 = $result;
                    $result = objDoWork;
                    try {
                        ListenableWorker.Result result = (ListenableWorker.Result) $result;
                        CoroutineWorker.this.getFuture$work_runtime_release().set(result);
                    } catch (Throwable th2) {
                        C00341 c003413 = c00341;
                        t = th2;
                        $result = $result2;
                        c003412 = c003413;
                        CoroutineWorker.this.getFuture$work_runtime_release().setException(t);
                        return Unit.INSTANCE;
                    }
                    return Unit.INSTANCE;
                case 1:
                    c003412 = this;
                    try {
                        ResultKt.throwOnFailure($result);
                        c00341 = c003412;
                        $result2 = $result;
                        ListenableWorker.Result result2 = (ListenableWorker.Result) $result;
                        CoroutineWorker.this.getFuture$work_runtime_release().set(result2);
                    } catch (Throwable th3) {
                        t = th3;
                        CoroutineWorker.this.getFuture$work_runtime_release().setException(t);
                        return Unit.INSTANCE;
                    }
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    @Override // androidx.work.ListenableWorker
    public final ListenableFuture<ListenableWorker.Result> startWork() {
        CoroutineScope coroutineScope = CoroutineScopeKt.CoroutineScope(getCoroutineContext().plus(this.job));
        BuildersKt__Builders_commonKt.launch$default(coroutineScope, null, null, new C00341(null), 3, null);
        return this.future;
    }

    static /* synthetic */ Object getForegroundInfo$suspendImpl(CoroutineWorker $this, Continuation<? super ForegroundInfo> continuation) {
        throw new IllegalStateException("Not implemented");
    }

    public final Object setProgress(Data data, Continuation<? super Unit> continuation) throws Throwable {
        ListenableFuture $this$await$iv = setProgressAsync(data);
        Intrinsics.checkNotNullExpressionValue($this$await$iv, "setProgressAsync(data)");
        if ($this$await$iv.isDone()) {
            try {
                $this$await$iv.get();
            } catch (ExecutionException e$iv) {
                Throwable cause = e$iv.getCause();
                if (cause == null) {
                    throw e$iv;
                }
                throw cause;
            }
        } else {
            CancellableContinuationImpl cancellable$iv$iv = new CancellableContinuationImpl(IntrinsicsKt.intercepted(continuation), 1);
            cancellable$iv$iv.initCancellability();
            CancellableContinuationImpl cancellableContinuation$iv = cancellable$iv$iv;
            $this$await$iv.addListener(new ListenableFutureKt$await$2$1(cancellableContinuation$iv, $this$await$iv), DirectExecutor.INSTANCE);
            cancellableContinuation$iv.invokeOnCancellation(new ListenableFutureKt$await$2$2($this$await$iv));
            Object result = cancellable$iv$iv.getResult();
            if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                DebugProbesKt.probeCoroutineSuspended(continuation);
            }
            if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                return result;
            }
        }
        return Unit.INSTANCE;
    }

    public final Object setForeground(ForegroundInfo foregroundInfo, Continuation<? super Unit> continuation) throws Throwable {
        ListenableFuture $this$await$iv = setForegroundAsync(foregroundInfo);
        Intrinsics.checkNotNullExpressionValue($this$await$iv, "setForegroundAsync(foregroundInfo)");
        if ($this$await$iv.isDone()) {
            try {
                $this$await$iv.get();
            } catch (ExecutionException e$iv) {
                Throwable cause = e$iv.getCause();
                if (cause == null) {
                    throw e$iv;
                }
                throw cause;
            }
        } else {
            CancellableContinuationImpl cancellable$iv$iv = new CancellableContinuationImpl(IntrinsicsKt.intercepted(continuation), 1);
            cancellable$iv$iv.initCancellability();
            CancellableContinuationImpl cancellableContinuation$iv = cancellable$iv$iv;
            $this$await$iv.addListener(new ListenableFutureKt$await$2$1(cancellableContinuation$iv, $this$await$iv), DirectExecutor.INSTANCE);
            cancellableContinuation$iv.invokeOnCancellation(new ListenableFutureKt$await$2$2($this$await$iv));
            Object result = cancellable$iv$iv.getResult();
            if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                DebugProbesKt.probeCoroutineSuspended(continuation);
            }
            if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                return result;
            }
        }
        return Unit.INSTANCE;
    }

    @Override // androidx.work.ListenableWorker
    public final ListenableFuture<ForegroundInfo> getForegroundInfoAsync() {
        CompletableJob job = JobKt__JobKt.Job$default((Job) null, 1, (Object) null);
        CoroutineScope scope = CoroutineScopeKt.CoroutineScope(getCoroutineContext().plus(job));
        JobListenableFuture jobFuture = new JobListenableFuture(job, null, 2, null);
        BuildersKt__Builders_commonKt.launch$default(scope, null, null, new AnonymousClass1(jobFuture, this, null), 3, null);
        return jobFuture;
    }

    /* compiled from: CoroutineWorker.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "androidx.work.CoroutineWorker$getForegroundInfoAsync$1", f = "CoroutineWorker.kt", i = {}, l = {134}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: androidx.work.CoroutineWorker$getForegroundInfoAsync$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ JobListenableFuture<ForegroundInfo> $jobFuture;
        Object L$0;
        int label;
        final /* synthetic */ CoroutineWorker this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(JobListenableFuture<ForegroundInfo> jobListenableFuture, CoroutineWorker coroutineWorker, Continuation<? super AnonymousClass1> continuation) {
            super(2, continuation);
            this.$jobFuture = jobListenableFuture;
            this.this$0 = coroutineWorker;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return new AnonymousClass1(this.$jobFuture, this.this$0, continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object $result) throws Throwable {
            JobListenableFuture jobListenableFuture;
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    jobListenableFuture = this.$jobFuture;
                    this.L$0 = jobListenableFuture;
                    this.label = 1;
                    Object foregroundInfo = this.this$0.getForegroundInfo(this);
                    if (foregroundInfo != coroutine_suspended) {
                        $result = foregroundInfo;
                        break;
                    } else {
                        return coroutine_suspended;
                    }
                case 1:
                    JobListenableFuture jobListenableFuture2 = (JobListenableFuture) this.L$0;
                    ResultKt.throwOnFailure($result);
                    jobListenableFuture = jobListenableFuture2;
                    break;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            jobListenableFuture.complete($result);
            return Unit.INSTANCE;
        }
    }

    @Override // androidx.work.ListenableWorker
    public final void onStopped() {
        super.onStopped();
        this.future.cancel(false);
    }
}
