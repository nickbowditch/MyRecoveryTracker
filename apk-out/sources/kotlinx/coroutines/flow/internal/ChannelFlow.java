package kotlinx.coroutines.flow.internal;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.DebugKt;
import kotlinx.coroutines.DebugStringsKt;
import kotlinx.coroutines.channels.BufferOverflow;
import kotlinx.coroutines.channels.ProduceKt;
import kotlinx.coroutines.channels.ProducerScope;
import kotlinx.coroutines.channels.ReceiveChannel;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.flow.FlowKt;

/* compiled from: ChannelFlow.kt */
@Metadata(d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\u0010\u0000\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b'\u0018\u0000*\u0004\b\u0000\u0010\u00012\b\u0012\u0004\u0012\u0002H\u00010\u0002B\u001d\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b¢\u0006\u0002\u0010\tJ\n\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0014J\u001f\u0010\u0017\u001a\u00020\u000e2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00028\u00000\u0019H\u0096@ø\u0001\u0000¢\u0006\u0002\u0010\u001aJ\u001f\u0010\u001b\u001a\u00020\u000e2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00028\u00000\fH¤@ø\u0001\u0000¢\u0006\u0002\u0010\u001dJ&\u0010\u001e\u001a\b\u0012\u0004\u0012\u00028\u00000\u00002\u0006\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH$J\u0010\u0010\u001f\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010 H\u0016J&\u0010!\u001a\b\u0012\u0004\u0012\u00028\u00000 2\u0006\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0016J\u0016\u0010\"\u001a\b\u0012\u0004\u0012\u00028\u00000#2\u0006\u0010\u001c\u001a\u00020$H\u0016J\b\u0010%\u001a\u00020\u0016H\u0016R\u0010\u0010\u0005\u001a\u00020\u00068\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R9\u0010\n\u001a$\b\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00028\u00000\f\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u000b8@X\u0080\u0004ø\u0001\u0000¢\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011R\u0010\u0010\u0003\u001a\u00020\u00048\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u00020\b8\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\u00020\u00068@X\u0080\u0004¢\u0006\u0006\u001a\u0004\b\u0013\u0010\u0014\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006&"}, d2 = {"Lkotlinx/coroutines/flow/internal/ChannelFlow;", "T", "Lkotlinx/coroutines/flow/internal/FusibleFlow;", "context", "Lkotlin/coroutines/CoroutineContext;", "capacity", "", "onBufferOverflow", "Lkotlinx/coroutines/channels/BufferOverflow;", "(Lkotlin/coroutines/CoroutineContext;ILkotlinx/coroutines/channels/BufferOverflow;)V", "collectToFun", "Lkotlin/Function2;", "Lkotlinx/coroutines/channels/ProducerScope;", "Lkotlin/coroutines/Continuation;", "", "", "getCollectToFun$kotlinx_coroutines_core", "()Lkotlin/jvm/functions/Function2;", "produceCapacity", "getProduceCapacity$kotlinx_coroutines_core", "()I", "additionalToStringProps", "", "collect", "collector", "Lkotlinx/coroutines/flow/FlowCollector;", "(Lkotlinx/coroutines/flow/FlowCollector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "collectTo", "scope", "(Lkotlinx/coroutines/channels/ProducerScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "create", "dropChannelOperators", "Lkotlinx/coroutines/flow/Flow;", "fuse", "produceImpl", "Lkotlinx/coroutines/channels/ReceiveChannel;", "Lkotlinx/coroutines/CoroutineScope;", "toString", "kotlinx-coroutines-core"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes4.dex */
public abstract class ChannelFlow<T> implements FusibleFlow<T> {
    public final int capacity;
    public final CoroutineContext context;
    public final BufferOverflow onBufferOverflow;

    @Override // kotlinx.coroutines.flow.Flow
    public Object collect(FlowCollector<? super T> flowCollector, Continuation<? super Unit> continuation) {
        return collect$suspendImpl(this, flowCollector, continuation);
    }

    protected abstract Object collectTo(ProducerScope<? super T> producerScope, Continuation<? super Unit> continuation);

    protected abstract ChannelFlow<T> create(CoroutineContext context, int capacity, BufferOverflow onBufferOverflow);

    public ChannelFlow(CoroutineContext context, int capacity, BufferOverflow onBufferOverflow) {
        this.context = context;
        this.capacity = capacity;
        this.onBufferOverflow = onBufferOverflow;
        if (!DebugKt.getASSERTIONS_ENABLED()) {
            return;
        }
        if (!(this.capacity != -1)) {
            throw new AssertionError();
        }
    }

    public final Function2<ProducerScope<? super T>, Continuation<? super Unit>, Object> getCollectToFun$kotlinx_coroutines_core() {
        return new ChannelFlow$collectToFun$1(this, null);
    }

    public final int getProduceCapacity$kotlinx_coroutines_core() {
        if (this.capacity == -3) {
            return -2;
        }
        return this.capacity;
    }

    public Flow<T> dropChannelOperators() {
        return null;
    }

    /* JADX WARN: Removed duplicated region for block: B:22:0x0038  */
    @Override // kotlinx.coroutines.flow.internal.FusibleFlow
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public kotlinx.coroutines.flow.Flow<T> fuse(kotlin.coroutines.CoroutineContext r8, int r9, kotlinx.coroutines.channels.BufferOverflow r10) {
        /*
            r7 = this;
            boolean r0 = kotlinx.coroutines.DebugKt.getASSERTIONS_ENABLED()
            r1 = 1
            r2 = 0
            if (r0 == 0) goto L18
            r0 = 0
            r3 = -1
            if (r9 == r3) goto Le
            r0 = r1
            goto Lf
        Le:
            r0 = r2
        Lf:
            if (r0 == 0) goto L12
            goto L18
        L12:
            java.lang.AssertionError r0 = new java.lang.AssertionError
            r0.<init>()
            throw r0
        L18:
            kotlin.coroutines.CoroutineContext r0 = r7.context
            kotlin.coroutines.CoroutineContext r0 = r8.plus(r0)
            r3 = 0
            r4 = 0
            kotlinx.coroutines.channels.BufferOverflow r5 = kotlinx.coroutines.channels.BufferOverflow.SUSPEND
            if (r10 == r5) goto L27
            r1 = r9
            r2 = r10
            goto L77
        L27:
            int r5 = r7.capacity
            r6 = -3
            if (r5 != r6) goto L2e
            goto L38
        L2e:
            if (r9 != r6) goto L33
            int r1 = r7.capacity
            goto L74
        L33:
            int r5 = r7.capacity
            r6 = -2
            if (r5 != r6) goto L3a
        L38:
            r1 = r9
            goto L74
        L3a:
            if (r9 != r6) goto L3f
            int r1 = r7.capacity
            goto L74
        L3f:
            boolean r5 = kotlinx.coroutines.DebugKt.getASSERTIONS_ENABLED()
            if (r5 == 0) goto L56
            r5 = 0
            int r6 = r7.capacity
            if (r6 < 0) goto L4c
            r5 = r1
            goto L4d
        L4c:
            r5 = r2
        L4d:
            if (r5 == 0) goto L50
            goto L56
        L50:
            java.lang.AssertionError r1 = new java.lang.AssertionError
            r1.<init>()
            throw r1
        L56:
            boolean r5 = kotlinx.coroutines.DebugKt.getASSERTIONS_ENABLED()
            if (r5 == 0) goto L6a
            r5 = 0
            if (r9 < 0) goto L60
            goto L61
        L60:
            r1 = r2
        L61:
            if (r1 == 0) goto L64
            goto L6a
        L64:
            java.lang.AssertionError r1 = new java.lang.AssertionError
            r1.<init>()
            throw r1
        L6a:
            int r1 = r7.capacity
            int r1 = r1 + r9
            if (r1 < 0) goto L70
            goto L74
        L70:
            r2 = 2147483647(0x7fffffff, float:NaN)
            r1 = r2
        L74:
            kotlinx.coroutines.channels.BufferOverflow r2 = r7.onBufferOverflow
        L77:
            kotlin.coroutines.CoroutineContext r3 = r7.context
            boolean r3 = kotlin.jvm.internal.Intrinsics.areEqual(r0, r3)
            if (r3 == 0) goto L8b
            int r3 = r7.capacity
            if (r1 != r3) goto L8b
            kotlinx.coroutines.channels.BufferOverflow r3 = r7.onBufferOverflow
            if (r2 != r3) goto L8b
            r3 = r7
            kotlinx.coroutines.flow.Flow r3 = (kotlinx.coroutines.flow.Flow) r3
            return r3
        L8b:
            kotlinx.coroutines.flow.internal.ChannelFlow r3 = r7.create(r0, r1, r2)
            kotlinx.coroutines.flow.Flow r3 = (kotlinx.coroutines.flow.Flow) r3
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.flow.internal.ChannelFlow.fuse(kotlin.coroutines.CoroutineContext, int, kotlinx.coroutines.channels.BufferOverflow):kotlinx.coroutines.flow.Flow");
    }

    public ReceiveChannel<T> produceImpl(CoroutineScope scope) {
        return ProduceKt.produce$default(scope, this.context, getProduceCapacity$kotlinx_coroutines_core(), this.onBufferOverflow, CoroutineStart.ATOMIC, null, getCollectToFun$kotlinx_coroutines_core(), 16, null);
    }

    /* compiled from: ChannelFlow.kt */
    @Metadata(d1 = {"\u0000\f\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u0003H\u008a@"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "kotlinx.coroutines.flow.internal.ChannelFlow$collect$2", f = "ChannelFlow.kt", i = {}, l = {123}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: kotlinx.coroutines.flow.internal.ChannelFlow$collect$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ FlowCollector<T> $collector;
        private /* synthetic */ Object L$0;
        int label;
        final /* synthetic */ ChannelFlow<T> this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        AnonymousClass2(FlowCollector<? super T> flowCollector, ChannelFlow<T> channelFlow, Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
            this.$collector = flowCollector;
            this.this$0 = channelFlow;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            AnonymousClass2 anonymousClass2 = new AnonymousClass2(this.$collector, this.this$0, continuation);
            anonymousClass2.L$0 = obj;
            return anonymousClass2;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object $result) throws Throwable {
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    CoroutineScope $this$coroutineScope = (CoroutineScope) this.L$0;
                    this.label = 1;
                    if (FlowKt.emitAll(this.$collector, this.this$0.produceImpl($this$coroutineScope), this) != coroutine_suspended) {
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

    static /* synthetic */ <T> Object collect$suspendImpl(ChannelFlow<T> channelFlow, FlowCollector<? super T> flowCollector, Continuation<? super Unit> continuation) {
        Object objCoroutineScope = CoroutineScopeKt.coroutineScope(new AnonymousClass2(flowCollector, channelFlow, null), continuation);
        return objCoroutineScope == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? objCoroutineScope : Unit.INSTANCE;
    }

    protected String additionalToStringProps() {
        return null;
    }

    public String toString() {
        ArrayList props = new ArrayList(4);
        String it = additionalToStringProps();
        if (it != null) {
            props.add(it);
        }
        if (this.context != EmptyCoroutineContext.INSTANCE) {
            props.add("context=" + this.context);
        }
        if (this.capacity != -3) {
            props.add("capacity=" + this.capacity);
        }
        if (this.onBufferOverflow != BufferOverflow.SUSPEND) {
            props.add("onBufferOverflow=" + this.onBufferOverflow);
        }
        return DebugStringsKt.getClassSimpleName(this) + '[' + CollectionsKt.joinToString$default(props, ", ", null, null, 0, null, null, 62, null) + ']';
    }
}
