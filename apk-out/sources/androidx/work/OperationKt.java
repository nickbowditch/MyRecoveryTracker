package androidx.work;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Operation;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CancellableContinuationImpl;

/* compiled from: Operation.kt */
@Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0015\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u0086Hø\u0001\u0000¢\u0006\u0002\u0010\u0003\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u0004"}, d2 = {"await", "Landroidx/work/Operation$State$SUCCESS;", "Landroidx/work/Operation;", "(Landroidx/work/Operation;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "work-runtime_release"}, k = 2, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class OperationKt {

    /* compiled from: Operation.kt */
    @Metadata(k = 3, mv = {1, 8, 0}, xi = 176)
    @DebugMetadata(c = "androidx.work.OperationKt", f = "Operation.kt", i = {0}, l = {39}, m = "await", n = {"$this$await$iv"}, s = {"L$0"})
    /* renamed from: androidx.work.OperationKt$await$1, reason: invalid class name */
    static final class AnonymousClass1 extends ContinuationImpl {
        Object L$0;
        int label;
        /* synthetic */ Object result;

        AnonymousClass1(Continuation<? super AnonymousClass1> continuation) {
            super(continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return OperationKt.await(null, this);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:7:0x0014  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final java.lang.Object await(androidx.work.Operation r12, kotlin.coroutines.Continuation<? super androidx.work.Operation.State.SUCCESS> r13) throws java.lang.Throwable {
        /*
            boolean r0 = r13 instanceof androidx.work.OperationKt.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r13
            androidx.work.OperationKt$await$1 r0 = (androidx.work.OperationKt.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r13 = r0.label
            int r13 = r13 - r2
            r0.label = r13
            goto L19
        L14:
            androidx.work.OperationKt$await$1 r0 = new androidx.work.OperationKt$await$1
            r0.<init>(r13)
        L19:
            java.lang.Object r13 = r0.result
            java.lang.Object r1 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r2 = r0.label
            switch(r2) {
                case 0: goto L38;
                case 1: goto L2c;
                default: goto L24;
            }
        L24:
            java.lang.IllegalStateException r12 = new java.lang.IllegalStateException
            java.lang.String r13 = "call to 'resume' before 'invoke' with coroutine"
            r12.<init>(r13)
            throw r12
        L2c:
            r12 = 0
            r1 = 0
            r2 = 0
            java.lang.Object r3 = r0.L$0
            com.google.common.util.concurrent.ListenableFuture r3 = (com.google.common.util.concurrent.ListenableFuture) r3
            kotlin.ResultKt.throwOnFailure(r13)
            r3 = r13
            goto La7
        L38:
            kotlin.ResultKt.throwOnFailure(r13)
            r2 = 0
            com.google.common.util.concurrent.ListenableFuture r3 = r12.getResult()
            java.lang.String r12 = "result"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r3, r12)
            r12 = 0
            boolean r4 = r3.isDone()
            if (r4 == 0) goto L5d
        L4d:
            java.lang.Object r1 = r3.get()     // Catch: java.util.concurrent.ExecutionException -> L52
            goto Laa
        L52:
            r1 = move-exception
            java.lang.Throwable r3 = r1.getCause()
            if (r3 != 0) goto L5c
            r3 = r1
            java.lang.Throwable r3 = (java.lang.Throwable) r3
        L5c:
            throw r3
        L5d:
            r4 = 0
            r0.L$0 = r3
            r5 = 1
            r0.label = r5
            r6 = r0
            kotlin.coroutines.Continuation r6 = (kotlin.coroutines.Continuation) r6
            r7 = 0
            kotlinx.coroutines.CancellableContinuationImpl r8 = new kotlinx.coroutines.CancellableContinuationImpl
            kotlin.coroutines.Continuation r9 = kotlin.coroutines.intrinsics.IntrinsicsKt.intercepted(r6)
            r8.<init>(r9, r5)
            r8.initCancellability()
            r5 = r8
            kotlinx.coroutines.CancellableContinuation r5 = (kotlinx.coroutines.CancellableContinuation) r5
            r9 = 0
            androidx.work.ListenableFutureKt$await$2$1 r10 = new androidx.work.ListenableFutureKt$await$2$1
            r10.<init>(r5, r3)
            java.lang.Runnable r10 = (java.lang.Runnable) r10
            androidx.work.DirectExecutor r11 = androidx.work.DirectExecutor.INSTANCE
            java.util.concurrent.Executor r11 = (java.util.concurrent.Executor) r11
            r3.addListener(r10, r11)
            androidx.work.ListenableFutureKt$await$2$2 r10 = new androidx.work.ListenableFutureKt$await$2$2
            r10.<init>(r3)
            kotlin.jvm.functions.Function1 r10 = (kotlin.jvm.functions.Function1) r10
            r5.invokeOnCancellation(r10)
            java.lang.Object r3 = r8.getResult()
            java.lang.Object r5 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            if (r3 != r5) goto La1
            r5 = r0
            kotlin.coroutines.Continuation r5 = (kotlin.coroutines.Continuation) r5
            kotlin.coroutines.jvm.internal.DebugProbesKt.probeCoroutineSuspended(r5)
        La1:
            if (r3 != r1) goto La4
            return r1
        La4:
            r1 = r12
            r12 = r2
            r2 = r4
        La7:
            r2 = r12
            r1 = r3
        Laa:
            java.lang.String r12 = "result.await()"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r1, r12)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.work.OperationKt.await(androidx.work.Operation, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private static final Object await$$forInline(Operation $this$await, Continuation<? super Operation.State.SUCCESS> continuation) throws Throwable {
        Object obj;
        ListenableFuture result = $this$await.getResult();
        Intrinsics.checkNotNullExpressionValue(result, "result");
        ListenableFuture $this$await$iv = result;
        if ($this$await$iv.isDone()) {
            try {
                obj = $this$await$iv.get();
            } catch (ExecutionException e$iv) {
                Throwable cause = e$iv.getCause();
                if (cause != null) {
                    throw cause;
                }
                throw e$iv;
            }
        } else {
            Continuation<? super Operation.State.SUCCESS> uCont$iv$iv = continuation;
            CancellableContinuationImpl cancellable$iv$iv = new CancellableContinuationImpl(IntrinsicsKt.intercepted(uCont$iv$iv), 1);
            cancellable$iv$iv.initCancellability();
            CancellableContinuationImpl cancellableContinuation$iv = cancellable$iv$iv;
            $this$await$iv.addListener(new ListenableFutureKt$await$2$1(cancellableContinuation$iv, $this$await$iv), DirectExecutor.INSTANCE);
            cancellableContinuation$iv.invokeOnCancellation(new ListenableFutureKt$await$2$2($this$await$iv));
            Unit unit = Unit.INSTANCE;
            Object result2 = cancellable$iv$iv.getResult();
            if (result2 == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                DebugProbesKt.probeCoroutineSuspended(continuation);
            }
            obj = result2;
        }
        Intrinsics.checkNotNullExpressionValue(obj, "result.await()");
        return obj;
    }
}
