package kotlinx.coroutines.internal;

import androidx.concurrent.futures.AbstractResolvableFuture$SafeAtomicHelper$$ExternalSyntheticBackportWithForwarding0;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.Volatile;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlinx.coroutines.DebugKt;

/* compiled from: LockFreeTaskQueue.kt */
@Metadata(d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\t\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\b\u0000\u0018\u0000 ,*\b\b\u0000\u0010\u0001*\u00020\u00022\u00020\u0002:\u0002,-B\u0015\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006¢\u0006\u0002\u0010\u0007J\u0013\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00028\u0000¢\u0006\u0002\u0010\u0017J \u0010\u0018\u001a\u0012\u0012\u0004\u0012\u00028\u00000\u0000j\b\u0012\u0004\u0012\u00028\u0000`\n2\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J \u0010\u001b\u001a\u0012\u0012\u0004\u0012\u00028\u00000\u0000j\b\u0012\u0004\u0012\u00028\u0000`\n2\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\u0006\u0010\u001c\u001a\u00020\u0006J1\u0010\u001d\u001a\u0016\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u0000j\n\u0012\u0004\u0012\u00028\u0000\u0018\u0001`\n2\u0006\u0010\u001e\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00028\u0000H\u0002¢\u0006\u0002\u0010\u001fJ\u0006\u0010 \u001a\u00020\u0006J&\u0010!\u001a\b\u0012\u0004\u0012\u0002H#0\"\"\u0004\b\u0001\u0010#2\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u0002H#0%J\b\u0010&\u001a\u00020\u001aH\u0002J\f\u0010'\u001a\b\u0012\u0004\u0012\u00028\u00000\u0000J\b\u0010(\u001a\u0004\u0018\u00010\u0002J,\u0010)\u001a\u0016\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u0000j\n\u0012\u0004\u0012\u00028\u0000\u0018\u0001`\n2\u0006\u0010*\u001a\u00020\u00042\u0006\u0010+\u001a\u00020\u0004H\u0002R#\u0010\b\u001a\u001c\u0012\u0018\u0012\u0016\u0012\u0004\u0012\u00028\u0000\u0018\u00010\u0000j\n\u0012\u0004\u0012\u00028\u0000\u0018\u0001`\n0\tX\u0082\u0004R\t\u0010\u000b\u001a\u00020\fX\u0082\u0004R\u0011\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u000eX\u0082\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R\u0011\u0010\u000f\u001a\u00020\u00068F¢\u0006\u0006\u001a\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0011\u001a\u00020\u0004X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004¢\u0006\u0002\n\u0000R\u0011\u0010\u0012\u001a\u00020\u00048F¢\u0006\u0006\u001a\u0004\b\u0013\u0010\u0014¨\u0006."}, d2 = {"Lkotlinx/coroutines/internal/LockFreeTaskQueueCore;", "E", "", "capacity", "", "singleConsumer", "", "(IZ)V", "_next", "Lkotlinx/atomicfu/AtomicRef;", "Lkotlinx/coroutines/internal/Core;", "_state", "Lkotlinx/atomicfu/AtomicLong;", "array", "Lkotlinx/atomicfu/AtomicArray;", "isEmpty", "()Z", "mask", "size", "getSize", "()I", "addLast", "element", "(Ljava/lang/Object;)I", "allocateNextCopy", "state", "", "allocateOrGetNextCopy", "close", "fillPlaceholder", "index", "(ILjava/lang/Object;)Lkotlinx/coroutines/internal/LockFreeTaskQueueCore;", "isClosed", "map", "", "R", "transform", "Lkotlin/Function1;", "markFrozen", "next", "removeFirstOrNull", "removeSlowPath", "oldHead", "newHead", "Companion", "Placeholder", "kotlinx-coroutines-core"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes4.dex */
public final class LockFreeTaskQueueCore<E> {
    public static final int ADD_CLOSED = 2;
    public static final int ADD_FROZEN = 1;
    public static final int ADD_SUCCESS = 0;
    public static final int CAPACITY_BITS = 30;
    public static final long CLOSED_MASK = 2305843009213693952L;
    public static final int CLOSED_SHIFT = 61;
    public static final long FROZEN_MASK = 1152921504606846976L;
    public static final int FROZEN_SHIFT = 60;
    public static final long HEAD_MASK = 1073741823;
    public static final int HEAD_SHIFT = 0;
    public static final int INITIAL_CAPACITY = 8;
    public static final int MAX_CAPACITY_MASK = 1073741823;
    public static final int MIN_ADD_SPIN_CAPACITY = 1024;
    public static final long TAIL_MASK = 1152921503533105152L;
    public static final int TAIL_SHIFT = 30;

    @Volatile
    private volatile Object _next;

    @Volatile
    private volatile long _state;
    private final AtomicReferenceArray array;
    private final int capacity;
    private final int mask;
    private final boolean singleConsumer;

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final AtomicReferenceFieldUpdater _next$FU = AtomicReferenceFieldUpdater.newUpdater(LockFreeTaskQueueCore.class, Object.class, "_next");
    private static final AtomicLongFieldUpdater _state$FU = AtomicLongFieldUpdater.newUpdater(LockFreeTaskQueueCore.class, "_state");
    public static final Symbol REMOVE_FROZEN = new Symbol("REMOVE_FROZEN");

    private final void loop$atomicfu(AtomicLongFieldUpdater atomicLongFieldUpdater, Function1<? super Long, Unit> function1, Object obj) {
        while (true) {
            function1.invoke(Long.valueOf(atomicLongFieldUpdater.get(obj)));
        }
    }

    private final void loop$atomicfu(AtomicReferenceFieldUpdater atomicReferenceFieldUpdater, Function1<Object, Unit> function1, Object obj) {
        while (true) {
            function1.invoke(atomicReferenceFieldUpdater.get(obj));
        }
    }

    private final void update$atomicfu(AtomicLongFieldUpdater atomicLongFieldUpdater, Function1<? super Long, Long> function1, Object obj) {
        while (true) {
            long j = atomicLongFieldUpdater.get(obj);
            AtomicLongFieldUpdater atomicLongFieldUpdater2 = atomicLongFieldUpdater;
            Object obj2 = obj;
            if (atomicLongFieldUpdater2.compareAndSet(obj2, j, function1.invoke(Long.valueOf(j)).longValue())) {
                return;
            }
            atomicLongFieldUpdater = atomicLongFieldUpdater2;
            obj = obj2;
        }
    }

    private final long updateAndGet$atomicfu(AtomicLongFieldUpdater atomicLongFieldUpdater, Function1<? super Long, Long> function1, Object obj) {
        while (true) {
            long j = atomicLongFieldUpdater.get(obj);
            Long lInvoke = function1.invoke(Long.valueOf(j));
            AtomicLongFieldUpdater atomicLongFieldUpdater2 = atomicLongFieldUpdater;
            Object obj2 = obj;
            if (atomicLongFieldUpdater2.compareAndSet(obj2, j, lInvoke.longValue())) {
                return lInvoke.longValue();
            }
            atomicLongFieldUpdater = atomicLongFieldUpdater2;
            obj = obj2;
        }
    }

    public LockFreeTaskQueueCore(int capacity, boolean singleConsumer) {
        this.capacity = capacity;
        this.singleConsumer = singleConsumer;
        this.mask = this.capacity - 1;
        this.array = new AtomicReferenceArray(this.capacity);
        if (!(this.mask <= 1073741823)) {
            throw new IllegalStateException("Check failed.".toString());
        }
        if ((this.capacity & this.mask) == 0) {
        } else {
            throw new IllegalStateException("Check failed.".toString());
        }
    }

    public final boolean isEmpty() {
        Companion companion = INSTANCE;
        long $this$withState$iv = _state$FU.get(this);
        int head$iv = (int) ((HEAD_MASK & $this$withState$iv) >> 0);
        int tail$iv = (int) ((TAIL_MASK & $this$withState$iv) >> 30);
        return head$iv == tail$iv;
    }

    public final int getSize() {
        Companion companion = INSTANCE;
        long $this$withState$iv = _state$FU.get(this);
        int head$iv = (int) ((HEAD_MASK & $this$withState$iv) >> 0);
        int tail$iv = (int) ((TAIL_MASK & $this$withState$iv) >> 30);
        int head = (tail$iv - head$iv) & MAX_CAPACITY_MASK;
        return head;
    }

    public final boolean close() {
        long state;
        AtomicLongFieldUpdater atomicfu$handler$iv = _state$FU;
        do {
            state = atomicfu$handler$iv.get(this);
            if ((state & CLOSED_MASK) != 0) {
                return true;
            }
            if ((FROZEN_MASK & state) != 0) {
                return false;
            }
        } while (!atomicfu$handler$iv.compareAndSet(this, state, state | CLOSED_MASK));
        return true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:20:0x0071, code lost:
    
        return 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final int addLast(E r29) {
        /*
            r28 = this;
            r1 = r28
            r6 = r29
            java.util.concurrent.atomic.AtomicLongFieldUpdater r0 = kotlinx.coroutines.internal.LockFreeTaskQueueCore._state$FU
            r7 = r28
            r8 = r0
            r9 = 0
        La:
            long r2 = r8.get(r1)
            r10 = 0
            r4 = 3458764513820540928(0x3000000000000000, double:1.727233711018889E-77)
            long r4 = r4 & r2
            r11 = 0
            int r0 = (r4 > r11 ? 1 : (r4 == r11 ? 0 : -1))
            if (r0 == 0) goto L1f
            kotlinx.coroutines.internal.LockFreeTaskQueueCore$Companion r0 = kotlinx.coroutines.internal.LockFreeTaskQueueCore.INSTANCE
            int r0 = r0.addFailReason(r2)
            return r0
        L1f:
            kotlinx.coroutines.internal.LockFreeTaskQueueCore$Companion r13 = kotlinx.coroutines.internal.LockFreeTaskQueueCore.INSTANCE
            r4 = r2
            r14 = r4
            r16 = 0
            r4 = 1073741823(0x3fffffff, double:5.304989472E-315)
            long r4 = r4 & r14
            r17 = 0
            long r4 = r4 >> r17
            int r0 = (int) r4
            r4 = 1152921503533105152(0xfffffffc0000000, double:1.2882296003504729E-231)
            long r4 = r4 & r14
            r18 = 30
            long r4 = r4 >> r18
            int r4 = (int) r4
            r18 = r0
            r5 = r4
            r19 = 0
            r20 = r11
            int r11 = r1.mask
            int r12 = r5 + 2
            r12 = r12 & r11
            r22 = r0
            r0 = r18 & r11
            r23 = 1
            if (r12 != r0) goto L4e
            return r23
        L4e:
            boolean r0 = r1.singleConsumer
            if (r0 != 0) goto L72
            java.util.concurrent.atomic.AtomicReferenceArray r0 = r1.array
            r24 = 1073741823(0x3fffffff, float:1.9999999)
            r12 = r5 & r11
            java.lang.Object r0 = r0.get(r12)
            if (r0 == 0) goto L75
            int r0 = r1.capacity
            r12 = 1024(0x400, float:1.435E-42)
            if (r0 < r12) goto L71
            int r0 = r5 - r18
            r0 = r0 & r24
            int r12 = r1.capacity
            int r12 = r12 >> 1
            if (r0 <= r12) goto L70
            goto L71
        L70:
            goto La
        L71:
            return r23
        L72:
            r24 = 1073741823(0x3fffffff, float:1.9999999)
        L75:
            int r0 = r5 + 1
            r12 = r0 & r24
            java.util.concurrent.atomic.AtomicLongFieldUpdater r0 = kotlinx.coroutines.internal.LockFreeTaskQueueCore._state$FU
            r23 = r0
            kotlinx.coroutines.internal.LockFreeTaskQueueCore$Companion r0 = kotlinx.coroutines.internal.LockFreeTaskQueueCore.INSTANCE
            long r24 = r0.updateTail(r2, r12)
            r0 = r23
            r23 = r4
            r27 = r7
            r7 = r5
            r4 = r24
            r24 = r27
            boolean r0 = r0.compareAndSet(r1, r2, r4)
            if (r0 == 0) goto Lba
            java.util.concurrent.atomic.AtomicReferenceArray r0 = r1.array
            r4 = r7 & r11
            r0.set(r4, r6)
            r0 = r28
        L9d:
            java.util.concurrent.atomic.AtomicLongFieldUpdater r4 = kotlinx.coroutines.internal.LockFreeTaskQueueCore._state$FU
            long r4 = r4.get(r0)
            r25 = 1152921504606846976(0x1000000000000000, double:1.2882297539194267E-231)
            long r4 = r4 & r25
            int r4 = (r4 > r20 ? 1 : (r4 == r20 ? 0 : -1))
            if (r4 == 0) goto Lb9
            kotlinx.coroutines.internal.LockFreeTaskQueueCore r4 = r0.next()
            kotlinx.coroutines.internal.LockFreeTaskQueueCore r4 = r4.fillPlaceholder(r7, r6)
            if (r4 != 0) goto Lb7
            goto Lb9
        Lb7:
            r0 = r4
            goto L9d
        Lb9:
            return r17
        Lba:
            r7 = r24
            goto La
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.internal.LockFreeTaskQueueCore.addLast(java.lang.Object):int");
    }

    private final LockFreeTaskQueueCore<E> fillPlaceholder(int index, E element) {
        Object old = this.array.get(this.mask & index);
        if ((old instanceof Placeholder) && ((Placeholder) old).index == index) {
            this.array.set(this.mask & index, element);
            return this;
        }
        return null;
    }

    public final Object removeFirstOrNull() {
        AtomicLongFieldUpdater atomicfu$handler$iv = _state$FU;
        LockFreeTaskQueueCore this_$iv = this;
        while (true) {
            long state = atomicfu$handler$iv.get(this);
            if ((FROZEN_MASK & state) != 0) {
                return REMOVE_FROZEN;
            }
            Companion companion = INSTANCE;
            int head$iv = (int) ((HEAD_MASK & state) >> 0);
            int tail$iv = (int) ((TAIL_MASK & state) >> 30);
            AtomicLongFieldUpdater atomicfu$handler$iv2 = atomicfu$handler$iv;
            if ((tail$iv & this.mask) == (this.mask & head$iv)) {
                return null;
            }
            Object element = this.array.get(this.mask & head$iv);
            if (element == null) {
                if (this.singleConsumer) {
                    return null;
                }
                atomicfu$handler$iv = atomicfu$handler$iv2;
            } else {
                if (element instanceof Placeholder) {
                    return null;
                }
                int newHead = (head$iv + 1) & MAX_CAPACITY_MASK;
                LockFreeTaskQueueCore this_$iv2 = this_$iv;
                if (_state$FU.compareAndSet(this, state, INSTANCE.updateHead(state, newHead))) {
                    this.array.set(this.mask & head$iv, null);
                    return element;
                }
                if (!this.singleConsumer) {
                    atomicfu$handler$iv = atomicfu$handler$iv2;
                    this_$iv = this_$iv2;
                } else {
                    LockFreeTaskQueueCore cur = this;
                    while (true) {
                        LockFreeTaskQueueCore lockFreeTaskQueueCoreRemoveSlowPath = cur.removeSlowPath(head$iv, newHead);
                        if (lockFreeTaskQueueCoreRemoveSlowPath == null) {
                            return element;
                        }
                        cur = lockFreeTaskQueueCoreRemoveSlowPath;
                    }
                }
            }
        }
    }

    private final LockFreeTaskQueueCore<E> removeSlowPath(int oldHead, int newHead) {
        AtomicLongFieldUpdater atomicfu$handler$iv;
        AtomicLongFieldUpdater atomicfu$handler$iv2 = _state$FU;
        while (true) {
            long state = atomicfu$handler$iv2.get(this);
            Companion companion = INSTANCE;
            int head$iv = (int) ((HEAD_MASK & state) >> 0);
            if (DebugKt.getASSERTIONS_ENABLED()) {
                atomicfu$handler$iv = atomicfu$handler$iv2;
                if (!(head$iv == oldHead)) {
                    throw new AssertionError();
                }
            } else {
                atomicfu$handler$iv = atomicfu$handler$iv2;
            }
            if ((state & FROZEN_MASK) != 0) {
                return next();
            }
            if (!_state$FU.compareAndSet(this, state, INSTANCE.updateHead(state, newHead))) {
                atomicfu$handler$iv2 = atomicfu$handler$iv;
            } else {
                this.array.set(head$iv & this.mask, null);
                return null;
            }
        }
    }

    public final LockFreeTaskQueueCore<E> next() {
        return allocateOrGetNextCopy(markFrozen());
    }

    private final long markFrozen() {
        long state;
        long state2;
        AtomicLongFieldUpdater atomicfu$handler$iv = _state$FU;
        do {
            state = atomicfu$handler$iv.get(this);
            if ((state & FROZEN_MASK) != 0) {
                return state;
            }
            state2 = state | FROZEN_MASK;
        } while (!atomicfu$handler$iv.compareAndSet(this, state, state2));
        return state2;
    }

    private final LockFreeTaskQueueCore<E> allocateOrGetNextCopy(long state) {
        AtomicReferenceFieldUpdater atomicfu$handler$iv = _next$FU;
        while (true) {
            LockFreeTaskQueueCore next = (LockFreeTaskQueueCore) atomicfu$handler$iv.get(this);
            if (next != null) {
                return next;
            }
            AbstractResolvableFuture$SafeAtomicHelper$$ExternalSyntheticBackportWithForwarding0.m(_next$FU, this, null, allocateNextCopy(state));
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private final LockFreeTaskQueueCore<E> allocateNextCopy(long state) {
        LockFreeTaskQueueCore next = new LockFreeTaskQueueCore(this.capacity * 2, this.singleConsumer);
        Companion companion = INSTANCE;
        int head$iv = (int) ((HEAD_MASK & state) >> 0);
        int tail$iv = (int) ((TAIL_MASK & state) >> 30);
        for (int index = head$iv; (this.mask & index) != (this.mask & tail$iv); index++) {
            Object value = this.array.get(this.mask & index);
            if (value == null) {
                value = new Placeholder(index);
            }
            next.array.set(next.mask & index, value);
        }
        _state$FU.set(next, INSTANCE.wo(state, FROZEN_MASK));
        return next;
    }

    public final <R> List<R> map(Function1<? super E, ? extends R> transform) {
        ArrayList res = new ArrayList(this.capacity);
        Companion companion = INSTANCE;
        long $this$withState$iv = _state$FU.get(this);
        int head$iv = (int) ((HEAD_MASK & $this$withState$iv) >> 0);
        int tail$iv = (int) ((TAIL_MASK & $this$withState$iv) >> 30);
        for (int index = head$iv; (this.mask & index) != (this.mask & tail$iv); index++) {
            Object element = this.array.get(this.mask & index);
            if (element != null && !(element instanceof Placeholder)) {
                res.add(transform.invoke(element));
            }
        }
        return res;
    }

    public final boolean isClosed() {
        return (_state$FU.get(this) & CLOSED_MASK) != 0;
    }

    /* compiled from: LockFreeTaskQueue.kt */
    @Metadata(d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004R\u0010\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\u0002\n\u0000¨\u0006\u0005"}, d2 = {"Lkotlinx/coroutines/internal/LockFreeTaskQueueCore$Placeholder;", "", "index", "", "(I)V", "kotlinx-coroutines-core"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final class Placeholder {
        public final int index;

        public Placeholder(int index) {
            this.index = index;
        }
    }

    /* compiled from: LockFreeTaskQueue.kt */
    @Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0080\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\n\u0010\u0016\u001a\u00020\u0004*\u00020\tJ\u0012\u0010\u0017\u001a\u00020\t*\u00020\t2\u0006\u0010\u0018\u001a\u00020\u0004J\u0012\u0010\u0019\u001a\u00020\t*\u00020\t2\u0006\u0010\u001a\u001a\u00020\u0004JP\u0010\u001b\u001a\u0002H\u001c\"\u0004\b\u0001\u0010\u001c*\u00020\t26\u0010\u001d\u001a2\u0012\u0013\u0012\u00110\u0004¢\u0006\f\b\u001f\u0012\b\b \u0012\u0004\b\b(!\u0012\u0013\u0012\u00110\u0004¢\u0006\f\b\u001f\u0012\b\b \u0012\u0004\b\b(\"\u0012\u0004\u0012\u0002H\u001c0\u001eH\u0086\b¢\u0006\u0002\u0010#J\u0015\u0010$\u001a\u00020\t*\u00020\t2\u0006\u0010%\u001a\u00020\tH\u0086\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\tX\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\tX\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u00020\u00138\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\tX\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0004X\u0086T¢\u0006\u0002\n\u0000¨\u0006&"}, d2 = {"Lkotlinx/coroutines/internal/LockFreeTaskQueueCore$Companion;", "", "()V", "ADD_CLOSED", "", "ADD_FROZEN", "ADD_SUCCESS", "CAPACITY_BITS", "CLOSED_MASK", "", "CLOSED_SHIFT", "FROZEN_MASK", "FROZEN_SHIFT", "HEAD_MASK", "HEAD_SHIFT", "INITIAL_CAPACITY", "MAX_CAPACITY_MASK", "MIN_ADD_SPIN_CAPACITY", "REMOVE_FROZEN", "Lkotlinx/coroutines/internal/Symbol;", "TAIL_MASK", "TAIL_SHIFT", "addFailReason", "updateHead", "newHead", "updateTail", "newTail", "withState", "T", "block", "Lkotlin/Function2;", "Lkotlin/ParameterName;", "name", "head", "tail", "(JLkotlin/jvm/functions/Function2;)Ljava/lang/Object;", "wo", "other", "kotlinx-coroutines-core"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final long wo(long $this$wo, long other) {
            return (~other) & $this$wo;
        }

        public final long updateHead(long $this$updateHead, int newHead) {
            return wo($this$updateHead, LockFreeTaskQueueCore.HEAD_MASK) | (newHead << 0);
        }

        public final long updateTail(long $this$updateTail, int newTail) {
            return wo($this$updateTail, LockFreeTaskQueueCore.TAIL_MASK) | (newTail << 30);
        }

        public final <T> T withState(long $this$withState, Function2<? super Integer, ? super Integer, ? extends T> function2) {
            int head = (int) ((LockFreeTaskQueueCore.HEAD_MASK & $this$withState) >> 0);
            int tail = (int) ((LockFreeTaskQueueCore.TAIL_MASK & $this$withState) >> 30);
            return function2.invoke(Integer.valueOf(head), Integer.valueOf(tail));
        }

        public final int addFailReason(long $this$addFailReason) {
            return (LockFreeTaskQueueCore.CLOSED_MASK & $this$addFailReason) != 0 ? 2 : 1;
        }
    }
}
