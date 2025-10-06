package androidx.room;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.ContinuationInterceptor;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt__BuildersKt;
import kotlinx.coroutines.CancellableContinuation;
import kotlinx.coroutines.CancellableContinuationImpl;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Job;

/* compiled from: RoomDatabaseExt.kt */
@Metadata(d1 = {"\u00000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\u001a\u001d\u0010\u0000\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u0082@ø\u0001\u0000¢\u0006\u0002\u0010\u0005\u001a\u0015\u0010\u0006\u001a\u00020\u0007*\u00020\bH\u0082@ø\u0001\u0000¢\u0006\u0002\u0010\t\u001a9\u0010\n\u001a\u0002H\u000b\"\u0004\b\u0000\u0010\u000b*\u00020\b2\u001c\u0010\f\u001a\u0018\b\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u000b0\u000e\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\rH\u0086@ø\u0001\u0000¢\u0006\u0002\u0010\u0010\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\u0011"}, d2 = {"acquireTransactionThread", "Lkotlin/coroutines/ContinuationInterceptor;", "Ljava/util/concurrent/Executor;", "controlJob", "Lkotlinx/coroutines/Job;", "(Ljava/util/concurrent/Executor;Lkotlinx/coroutines/Job;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createTransactionContext", "Lkotlin/coroutines/CoroutineContext;", "Landroidx/room/RoomDatabase;", "(Landroidx/room/RoomDatabase;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "withTransaction", "R", "block", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "", "(Landroidx/room/RoomDatabase;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "room-ktx_release"}, k = 2, mv = {1, 7, 1}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class RoomDatabaseKt {

    /* compiled from: RoomDatabaseExt.kt */
    @Metadata(k = 3, mv = {1, 7, 1}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "androidx.room.RoomDatabaseKt", f = "RoomDatabaseExt.kt", i = {0, 0}, l = {100}, m = "createTransactionContext", n = {"$this$createTransactionContext", "controlJob"}, s = {"L$0", "L$1"})
    /* renamed from: androidx.room.RoomDatabaseKt$createTransactionContext$1, reason: invalid class name */
    static final class AnonymousClass1 extends ContinuationImpl {
        Object L$0;
        Object L$1;
        int label;
        /* synthetic */ Object result;

        AnonymousClass1(Continuation<? super AnonymousClass1> continuation) {
            super(continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return RoomDatabaseKt.createTransactionContext(null, this);
        }
    }

    /* compiled from: RoomDatabaseExt.kt */
    @Metadata(k = 3, mv = {1, 7, 1}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "androidx.room.RoomDatabaseKt", f = "RoomDatabaseExt.kt", i = {0, 0}, l = {ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_TAG, 52}, m = "withTransaction", n = {"$this$withTransaction", "block"}, s = {"L$0", "L$1"})
    /* renamed from: androidx.room.RoomDatabaseKt$withTransaction$1, reason: invalid class name and case insensitive filesystem */
    static final class C00321<R> extends ContinuationImpl {
        Object L$0;
        Object L$1;
        int label;
        /* synthetic */ Object result;

        C00321(Continuation<? super C00321> continuation) {
            super(continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return RoomDatabaseKt.withTransaction(null, null, this);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x0086 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:28:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0014  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final <R> java.lang.Object withTransaction(androidx.room.RoomDatabase r6, kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super R>, ? extends java.lang.Object> r7, kotlin.coroutines.Continuation<? super R> r8) throws java.lang.Throwable {
        /*
            boolean r0 = r8 instanceof androidx.room.RoomDatabaseKt.C00321
            if (r0 == 0) goto L14
            r0 = r8
            androidx.room.RoomDatabaseKt$withTransaction$1 r0 = (androidx.room.RoomDatabaseKt.C00321) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r8 = r0.label
            int r8 = r8 - r2
            r0.label = r8
            goto L19
        L14:
            androidx.room.RoomDatabaseKt$withTransaction$1 r0 = new androidx.room.RoomDatabaseKt$withTransaction$1
            r0.<init>(r8)
        L19:
            java.lang.Object r8 = r0.result
            java.lang.Object r1 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r2 = r0.label
            switch(r2) {
                case 0: goto L3e;
                case 1: goto L31;
                case 2: goto L2c;
                default: goto L24;
            }
        L24:
            java.lang.IllegalStateException r6 = new java.lang.IllegalStateException
            java.lang.String r7 = "call to 'resume' before 'invoke' with coroutine"
            r6.<init>(r7)
            throw r6
        L2c:
            kotlin.ResultKt.throwOnFailure(r8)
            r6 = r8
            goto L87
        L31:
            java.lang.Object r6 = r0.L$1
            kotlin.jvm.functions.Function1 r6 = (kotlin.jvm.functions.Function1) r6
            java.lang.Object r7 = r0.L$0
            androidx.room.RoomDatabase r7 = (androidx.room.RoomDatabase) r7
            kotlin.ResultKt.throwOnFailure(r8)
            r2 = r8
            goto L6b
        L3e:
            kotlin.ResultKt.throwOnFailure(r8)
            kotlin.coroutines.CoroutineContext r2 = r0.get$context()
            androidx.room.TransactionElement$Key r3 = androidx.room.TransactionElement.INSTANCE
            kotlin.coroutines.CoroutineContext$Key r3 = (kotlin.coroutines.CoroutineContext.Key) r3
            kotlin.coroutines.CoroutineContext$Element r2 = r2.get(r3)
            androidx.room.TransactionElement r2 = (androidx.room.TransactionElement) r2
            if (r2 == 0) goto L5a
            kotlin.coroutines.ContinuationInterceptor r2 = r2.getTransactionDispatcher()
            if (r2 == 0) goto L5a
            kotlin.coroutines.CoroutineContext r2 = (kotlin.coroutines.CoroutineContext) r2
            goto L70
        L5a:
            r0.L$0 = r6
            r0.L$1 = r7
            r2 = 1
            r0.label = r2
            java.lang.Object r2 = createTransactionContext(r6, r0)
            if (r2 != r1) goto L68
            return r1
        L68:
            r5 = r7
            r7 = r6
            r6 = r5
        L6b:
            kotlin.coroutines.CoroutineContext r2 = (kotlin.coroutines.CoroutineContext) r2
            r5 = r7
            r7 = r6
            r6 = r5
        L70:
            androidx.room.RoomDatabaseKt$withTransaction$2 r3 = new androidx.room.RoomDatabaseKt$withTransaction$2
            r4 = 0
            r3.<init>(r6, r7, r4)
            kotlin.jvm.functions.Function2 r3 = (kotlin.jvm.functions.Function2) r3
            r0.L$0 = r4
            r0.L$1 = r4
            r4 = 2
            r0.label = r4
            java.lang.Object r6 = kotlinx.coroutines.BuildersKt.withContext(r2, r3, r0)
            if (r6 != r1) goto L87
            return r1
        L87:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.room.RoomDatabaseKt.withTransaction(androidx.room.RoomDatabase, kotlin.jvm.functions.Function1, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Add missing generic type declarations: [R] */
    /* compiled from: RoomDatabaseExt.kt */
    @Metadata(d1 = {"\u0000\b\n\u0002\b\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u0002H\u0001\"\u0004\b\u0000\u0010\u0001*\u00020\u0002H\u008a@"}, d2 = {"<anonymous>", "R", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {1, 7, 1}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "androidx.room.RoomDatabaseKt$withTransaction$2", f = "RoomDatabaseExt.kt", i = {0}, l = {59}, m = "invokeSuspend", n = {"transactionElement"}, s = {"L$0"})
    /* renamed from: androidx.room.RoomDatabaseKt$withTransaction$2, reason: invalid class name and case insensitive filesystem */
    static final class C00332<R> extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super R>, Object> {
        final /* synthetic */ Function1<Continuation<? super R>, Object> $block;
        final /* synthetic */ RoomDatabase $this_withTransaction;
        private /* synthetic */ Object L$0;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        C00332(RoomDatabase roomDatabase, Function1<? super Continuation<? super R>, ? extends Object> function1, Continuation<? super C00332> continuation) {
            super(2, continuation);
            this.$this_withTransaction = roomDatabase;
            this.$block = function1;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            C00332 c00332 = new C00332(this.$this_withTransaction, this.$block, continuation);
            c00332.L$0 = obj;
            return c00332;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super R> continuation) {
            return ((C00332) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object $result) throws Throwable {
            C00332 c00332;
            TransactionElement transactionElement;
            Throwable th;
            Throwable th2;
            C00332 c003322;
            TransactionElement transactionElement2;
            Object $result2;
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    c00332 = this;
                    CoroutineScope $this$withContext = (CoroutineScope) c00332.L$0;
                    CoroutineContext.Element element = $this$withContext.getCoroutineContext().get(TransactionElement.INSTANCE);
                    Intrinsics.checkNotNull(element);
                    transactionElement = (TransactionElement) element;
                    transactionElement.acquire();
                    try {
                        c00332.$this_withTransaction.beginTransaction();
                        try {
                            Function1<Continuation<? super R>, Object> function1 = c00332.$block;
                            c00332.L$0 = transactionElement;
                            c00332.label = 1;
                            Object objInvoke = function1.invoke(c00332);
                            if (objInvoke == coroutine_suspended) {
                                return coroutine_suspended;
                            }
                            $result2 = $result;
                            $result = objInvoke;
                            try {
                                c00332.$this_withTransaction.setTransactionSuccessful();
                                try {
                                    c00332.$this_withTransaction.endTransaction();
                                    transactionElement.release();
                                    return $result;
                                } catch (Throwable th3) {
                                    th = th3;
                                    transactionElement.release();
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                TransactionElement transactionElement3 = transactionElement;
                                th2 = th4;
                                $result = $result2;
                                c003322 = c00332;
                                transactionElement2 = transactionElement3;
                                try {
                                    c003322.$this_withTransaction.endTransaction();
                                    throw th2;
                                } catch (Throwable th5) {
                                    th = th5;
                                    transactionElement = transactionElement2;
                                    transactionElement.release();
                                    throw th;
                                }
                            }
                        } catch (Throwable th6) {
                            th2 = th6;
                            c003322 = c00332;
                            transactionElement2 = transactionElement;
                            c003322.$this_withTransaction.endTransaction();
                            throw th2;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        transactionElement.release();
                        throw th;
                    }
                case 1:
                    c003322 = this;
                    transactionElement2 = (TransactionElement) c003322.L$0;
                    try {
                        ResultKt.throwOnFailure($result);
                        transactionElement = transactionElement2;
                        c00332 = c003322;
                        $result2 = $result;
                        c00332.$this_withTransaction.setTransactionSuccessful();
                        c00332.$this_withTransaction.endTransaction();
                        transactionElement.release();
                        return $result;
                    } catch (Throwable th8) {
                        th2 = th8;
                        c003322.$this_withTransaction.endTransaction();
                        throw th2;
                    }
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:7:0x0014  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final java.lang.Object createTransactionContext(androidx.room.RoomDatabase r7, kotlin.coroutines.Continuation<? super kotlin.coroutines.CoroutineContext> r8) throws java.lang.Throwable {
        /*
            boolean r0 = r8 instanceof androidx.room.RoomDatabaseKt.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            androidx.room.RoomDatabaseKt$createTransactionContext$1 r0 = (androidx.room.RoomDatabaseKt.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r8 = r0.label
            int r8 = r8 - r2
            r0.label = r8
            goto L19
        L14:
            androidx.room.RoomDatabaseKt$createTransactionContext$1 r0 = new androidx.room.RoomDatabaseKt$createTransactionContext$1
            r0.<init>(r8)
        L19:
            java.lang.Object r8 = r0.result
            java.lang.Object r1 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r2 = r0.label
            switch(r2) {
                case 0: goto L39;
                case 1: goto L2c;
                default: goto L24;
            }
        L24:
            java.lang.IllegalStateException r7 = new java.lang.IllegalStateException
            java.lang.String r8 = "call to 'resume' before 'invoke' with coroutine"
            r7.<init>(r8)
            throw r7
        L2c:
            java.lang.Object r7 = r0.L$1
            kotlinx.coroutines.CompletableJob r7 = (kotlinx.coroutines.CompletableJob) r7
            java.lang.Object r1 = r0.L$0
            androidx.room.RoomDatabase r1 = (androidx.room.RoomDatabase) r1
            kotlin.ResultKt.throwOnFailure(r8)
            r3 = r8
            goto L72
        L39:
            kotlin.ResultKt.throwOnFailure(r8)
            r2 = 0
            r3 = 1
            kotlinx.coroutines.CompletableJob r2 = kotlinx.coroutines.JobKt.Job$default(r2, r3, r2)
            kotlin.coroutines.CoroutineContext r4 = r0.get$context()
            kotlinx.coroutines.Job$Key r5 = kotlinx.coroutines.Job.INSTANCE
            kotlin.coroutines.CoroutineContext$Key r5 = (kotlin.coroutines.CoroutineContext.Key) r5
            kotlin.coroutines.CoroutineContext$Element r4 = r4.get(r5)
            kotlinx.coroutines.Job r4 = (kotlinx.coroutines.Job) r4
            if (r4 == 0) goto L5c
            androidx.room.RoomDatabaseKt$createTransactionContext$2 r5 = new androidx.room.RoomDatabaseKt$createTransactionContext$2
            r5.<init>()
            kotlin.jvm.functions.Function1 r5 = (kotlin.jvm.functions.Function1) r5
            r4.invokeOnCompletion(r5)
        L5c:
            java.util.concurrent.Executor r4 = r7.getTransactionExecutor()
            r5 = r2
            kotlinx.coroutines.Job r5 = (kotlinx.coroutines.Job) r5
            r0.L$0 = r7
            r0.L$1 = r2
            r0.label = r3
            java.lang.Object r3 = acquireTransactionThread(r4, r5, r0)
            if (r3 != r1) goto L70
            return r1
        L70:
            r1 = r7
            r7 = r2
        L72:
            r2 = r3
            kotlin.coroutines.ContinuationInterceptor r2 = (kotlin.coroutines.ContinuationInterceptor) r2
            androidx.room.TransactionElement r3 = new androidx.room.TransactionElement
            r4 = r7
            kotlinx.coroutines.Job r4 = (kotlinx.coroutines.Job) r4
            r3.<init>(r4, r2)
            java.lang.ThreadLocal r4 = r1.getSuspendingTransactionId()
            int r5 = java.lang.System.identityHashCode(r7)
            java.lang.Integer r5 = kotlin.coroutines.jvm.internal.Boxing.boxInt(r5)
            kotlinx.coroutines.ThreadContextElement r4 = kotlinx.coroutines.ThreadContextElementKt.asContextElement(r4, r5)
            r5 = r3
            kotlin.coroutines.CoroutineContext r5 = (kotlin.coroutines.CoroutineContext) r5
            kotlin.coroutines.CoroutineContext r5 = r2.plus(r5)
            r6 = r4
            kotlin.coroutines.CoroutineContext r6 = (kotlin.coroutines.CoroutineContext) r6
            kotlin.coroutines.CoroutineContext r5 = r5.plus(r6)
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.room.RoomDatabaseKt.createTransactionContext(androidx.room.RoomDatabase, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Object acquireTransactionThread(Executor $this$acquireTransactionThread, final Job controlJob, Continuation<? super ContinuationInterceptor> continuation) {
        CancellableContinuationImpl cancellable$iv = new CancellableContinuationImpl(IntrinsicsKt.intercepted(continuation), 1);
        cancellable$iv.initCancellability();
        final CancellableContinuationImpl continuation2 = cancellable$iv;
        continuation2.invokeOnCancellation(new Function1<Throwable, Unit>() { // from class: androidx.room.RoomDatabaseKt$acquireTransactionThread$2$1
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Throwable th) {
                invoke2(th);
                return Unit.INSTANCE;
            }

            /* renamed from: invoke, reason: avoid collision after fix types in other method */
            public final void invoke2(Throwable it) {
                Job.DefaultImpls.cancel$default(controlJob, (CancellationException) null, 1, (Object) null);
            }
        });
        try {
            $this$acquireTransactionThread.execute(new Runnable() { // from class: androidx.room.RoomDatabaseKt$acquireTransactionThread$2$2

                /* compiled from: RoomDatabaseExt.kt */
                @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u008a@"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {1, 7, 1}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
                @DebugMetadata(c = "androidx.room.RoomDatabaseKt$acquireTransactionThread$2$2$1", f = "RoomDatabaseExt.kt", i = {}, l = {125}, m = "invokeSuspend", n = {}, s = {})
                /* renamed from: androidx.room.RoomDatabaseKt$acquireTransactionThread$2$2$1, reason: invalid class name */
                static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
                    final /* synthetic */ CancellableContinuation<ContinuationInterceptor> $continuation;
                    final /* synthetic */ Job $controlJob;
                    private /* synthetic */ Object L$0;
                    int label;

                    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                    /* JADX WARN: Multi-variable type inference failed */
                    AnonymousClass1(CancellableContinuation<? super ContinuationInterceptor> cancellableContinuation, Job job, Continuation<? super AnonymousClass1> continuation) {
                        super(2, continuation);
                        this.$continuation = cancellableContinuation;
                        this.$controlJob = job;
                    }

                    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
                    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
                        AnonymousClass1 anonymousClass1 = new AnonymousClass1(this.$continuation, this.$controlJob, continuation);
                        anonymousClass1.L$0 = obj;
                        return anonymousClass1;
                    }

                    @Override // kotlin.jvm.functions.Function2
                    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
                        return ((AnonymousClass1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
                    }

                    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
                    public final Object invokeSuspend(Object $result) throws Throwable {
                        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                        switch (this.label) {
                            case 0:
                                ResultKt.throwOnFailure($result);
                                CoroutineScope coroutineScope = (CoroutineScope) this.L$0;
                                CancellableContinuation<ContinuationInterceptor> cancellableContinuation = this.$continuation;
                                Result.Companion companion = Result.INSTANCE;
                                CoroutineContext.Element element = coroutineScope.getCoroutineContext().get(ContinuationInterceptor.INSTANCE);
                                Intrinsics.checkNotNull(element);
                                cancellableContinuation.resumeWith(Result.m212constructorimpl(element));
                                this.label = 1;
                                if (this.$controlJob.join(this) != coroutine_suspended) {
                                    break;
                                } else {
                                    return coroutine_suspended;
                                }
                            case 1:
                                ResultKt.throwOnFailure($result);
                                break;
                            default:
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                        }
                        return Unit.INSTANCE;
                    }
                }

                @Override // java.lang.Runnable
                public final void run() throws InterruptedException {
                    BuildersKt__BuildersKt.runBlocking$default(null, new AnonymousClass1(continuation2, controlJob, null), 1, null);
                }
            });
        } catch (RejectedExecutionException ex) {
            continuation2.cancel(new IllegalStateException("Unable to acquire a thread to perform the database transaction.", ex));
        }
        Object result = cancellable$iv.getResult();
        if (result == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
            DebugProbesKt.probeCoroutineSuspended(continuation);
        }
        return result;
    }
}
