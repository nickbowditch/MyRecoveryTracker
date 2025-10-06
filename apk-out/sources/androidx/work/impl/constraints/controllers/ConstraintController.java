package androidx.work.impl.constraints.controllers;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.impl.constraints.ConstraintListener;
import androidx.work.impl.constraints.ConstraintsState;
import androidx.work.impl.constraints.trackers.ConstraintTracker;
import androidx.work.impl.model.WorkSpec;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.channels.ProduceKt;
import kotlinx.coroutines.channels.ProducerScope;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowKt;

/* compiled from: ContraintControllers.kt */
@Metadata(d1 = {"\u00004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b&\u0018\u0000*\u0004\b\u0000\u0010\u00012\u00020\u0002B\u0013\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00028\u00000\u0004¢\u0006\u0002\u0010\u0005J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH&J\u0015\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00028\u0000H&¢\u0006\u0002\u0010\u0012J\u000e\u0010\u0010\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014R\u0018\u0010\u0006\u001a\u00020\u0007X¦\u0004¢\u0006\f\u0012\u0004\b\b\u0010\t\u001a\u0004\b\n\u0010\u000bR\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00028\u00000\u0004X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0016"}, d2 = {"Landroidx/work/impl/constraints/controllers/ConstraintController;", "T", "", "tracker", "Landroidx/work/impl/constraints/trackers/ConstraintTracker;", "(Landroidx/work/impl/constraints/trackers/ConstraintTracker;)V", "reason", "", "getReason$annotations", "()V", "getReason", "()I", "hasConstraint", "", "workSpec", "Landroidx/work/impl/model/WorkSpec;", "isConstrained", "value", "(Ljava/lang/Object;)Z", "track", "Lkotlinx/coroutines/flow/Flow;", "Landroidx/work/impl/constraints/ConstraintsState;", "work-runtime_release"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public abstract class ConstraintController<T> {
    private final ConstraintTracker<T> tracker;

    public static /* synthetic */ void getReason$annotations() {
    }

    public abstract int getReason();

    public abstract boolean hasConstraint(WorkSpec workSpec);

    public abstract boolean isConstrained(T value);

    public ConstraintController(ConstraintTracker<T> tracker) {
        Intrinsics.checkNotNullParameter(tracker, "tracker");
        this.tracker = tracker;
    }

    /* compiled from: ContraintControllers.kt */
    @Metadata(d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u00020\u00040\u0003H\u008a@"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/channels/ProducerScope;", "Landroidx/work/impl/constraints/ConstraintsState;"}, k = 3, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "androidx.work.impl.constraints.controllers.ConstraintController$track$1", f = "ContraintControllers.kt", i = {}, l = {55}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: androidx.work.impl.constraints.controllers.ConstraintController$track$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<ProducerScope<? super ConstraintsState>, Continuation<? super Unit>, Object> {
        private /* synthetic */ Object L$0;
        int label;
        final /* synthetic */ ConstraintController<T> this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(ConstraintController<T> constraintController, Continuation<? super AnonymousClass1> continuation) {
            super(2, continuation);
            this.this$0 = constraintController;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            AnonymousClass1 anonymousClass1 = new AnonymousClass1(this.this$0, continuation);
            anonymousClass1.L$0 = obj;
            return anonymousClass1;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(ProducerScope<? super ConstraintsState> producerScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(producerScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    final ProducerScope producerScope = (ProducerScope) this.L$0;
                    final ConstraintController<T> constraintController = this.this$0;
                    final ConstraintListener<T> constraintListener = new ConstraintListener<T>() { // from class: androidx.work.impl.constraints.controllers.ConstraintController$track$1$listener$1
                        @Override // androidx.work.impl.constraints.ConstraintListener
                        public void onConstraintChanged(T newValue) {
                            ConstraintsState value = constraintController.isConstrained((ConstraintController<T>) newValue) ? new ConstraintsState.ConstraintsNotMet(constraintController.getReason()) : ConstraintsState.ConstraintsMet.INSTANCE;
                            producerScope.getChannel().mo1715trySendJP2dKIU(value);
                        }
                    };
                    ((ConstraintController) this.this$0).tracker.addListener(constraintListener);
                    final ConstraintController<T> constraintController2 = this.this$0;
                    this.label = 1;
                    if (ProduceKt.awaitClose(producerScope, new Function0<Unit>() { // from class: androidx.work.impl.constraints.controllers.ConstraintController.track.1.1
                        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
                        {
                            super(0);
                        }

                        @Override // kotlin.jvm.functions.Function0
                        public /* bridge */ /* synthetic */ Unit invoke() {
                            invoke2();
                            return Unit.INSTANCE;
                        }

                        /* renamed from: invoke, reason: avoid collision after fix types in other method */
                        public final void invoke2() {
                            ((ConstraintController) constraintController2).tracker.removeListener(constraintListener);
                        }
                    }, this) != coroutine_suspended) {
                        break;
                    } else {
                        return coroutine_suspended;
                    }
                case 1:
                    ResultKt.throwOnFailure(obj);
                    break;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            return Unit.INSTANCE;
        }
    }

    public final Flow<ConstraintsState> track() {
        return FlowKt.callbackFlow(new AnonymousClass1(this, null));
    }

    public final boolean isConstrained(WorkSpec workSpec) {
        Intrinsics.checkNotNullParameter(workSpec, "workSpec");
        return hasConstraint(workSpec) && isConstrained((ConstraintController<T>) this.tracker.readSystemState());
    }
}
