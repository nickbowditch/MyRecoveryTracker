package androidx.collection;

import androidx.collection.internal.ContainerHelpersKt;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.CollectionToArray;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.markers.KMappedMarker;
import kotlin.sequences.SequencesKt;

/* compiled from: ScatterSet.kt */
@Metadata(d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u0011\n\u0002\b\u0003\n\u0002\u0010\u0016\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\"\n\u0002\b\r\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\r\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\b6\u0018\u0000*\u0004\b\u0000\u0010\u00012\u00020\u0002:\u0001<B\u0007\b\u0004¢\u0006\u0002\u0010\u0003J:\u0010\u0013\u001a\u00020\u00142!\u0010\u0015\u001a\u001d\u0012\u0013\u0012\u00118\u0000¢\u0006\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\b(\u0019\u0012\u0004\u0012\u00020\u00140\u0016H\u0086\bø\u0001\u0000\u0082\u0002\b\n\u0006\b\u0001\u0012\u0002\u0010\u0001J\u0006\u0010\u001a\u001a\u00020\u0014J:\u0010\u001a\u001a\u00020\u00142!\u0010\u0015\u001a\u001d\u0012\u0013\u0012\u00118\u0000¢\u0006\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\b(\u0019\u0012\u0004\u0012\u00020\u00140\u0016H\u0086\bø\u0001\u0000\u0082\u0002\b\n\u0006\b\u0001\u0012\u0002\u0010\u0001J\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00028\u00000\u001cJ\u0016\u0010\u001d\u001a\u00020\u00142\u0006\u0010\u0019\u001a\u00028\u0000H\u0086\u0002¢\u0006\u0002\u0010\u001eJ\b\u0010\u001f\u001a\u00020\u0005H\u0007J:\u0010\u001f\u001a\u00020\u00052!\u0010\u0015\u001a\u001d\u0012\u0013\u0012\u00118\u0000¢\u0006\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\b(\u0019\u0012\u0004\u0012\u00020\u00140\u0016H\u0087\bø\u0001\u0000\u0082\u0002\b\n\u0006\b\u0001\u0012\u0002\u0010\u0001J\u0013\u0010 \u001a\u00020\u00142\b\u0010!\u001a\u0004\u0018\u00010\u0002H\u0096\u0002J\u0018\u0010\"\u001a\u00020\u00052\u0006\u0010\u0019\u001a\u00028\u0000H\u0080\b¢\u0006\u0004\b#\u0010$J\u000e\u0010%\u001a\u00028\u0000H\u0086\b¢\u0006\u0002\u0010&J?\u0010%\u001a\u00028\u00002!\u0010\u0015\u001a\u001d\u0012\u0013\u0012\u00118\u0000¢\u0006\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\b(\u0019\u0012\u0004\u0012\u00020\u00140\u0016H\u0086\bø\u0001\u0000\u0082\u0002\b\n\u0006\b\u0001\u0012\u0002\u0010\u0001¢\u0006\u0002\u0010'JA\u0010(\u001a\u0004\u0018\u00018\u00002!\u0010\u0015\u001a\u001d\u0012\u0013\u0012\u00118\u0000¢\u0006\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\b(\u0019\u0012\u0004\u0012\u00020\u00140\u0016H\u0086\bø\u0001\u0000\u0082\u0002\b\n\u0006\b\u0001\u0012\u0002\u0010\u0001¢\u0006\u0002\u0010'J:\u0010)\u001a\u00020*2!\u0010+\u001a\u001d\u0012\u0013\u0012\u00118\u0000¢\u0006\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\b(\u0019\u0012\u0004\u0012\u00020*0\u0016H\u0086\bø\u0001\u0000\u0082\u0002\b\n\u0006\b\u0001\u0012\u0002\u0010\u0001J:\u0010,\u001a\u00020*2!\u0010+\u001a\u001d\u0012\u0013\u0012\u00110\u0005¢\u0006\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\b(-\u0012\u0004\u0012\u00020*0\u0016H\u0081\bø\u0001\u0000\u0082\u0002\b\n\u0006\b\u0001\u0012\u0002\u0010\u0001J\b\u0010.\u001a\u00020\u0005H\u0016J\u0006\u0010/\u001a\u00020\u0014J\u0006\u00100\u001a\u00020\u0014JR\u00101\u001a\u0002022\b\b\u0002\u00103\u001a\u0002042\b\b\u0002\u00105\u001a\u0002042\b\b\u0002\u00106\u001a\u0002042\b\b\u0002\u00107\u001a\u00020\u00052\b\b\u0002\u00108\u001a\u0002042\u0016\b\u0002\u00109\u001a\u0010\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u000204\u0018\u00010\u0016H\u0007J\u0006\u0010:\u001a\u00020\u0014J\b\u0010;\u001a\u000202H\u0016R\u0012\u0010\u0004\u001a\u00020\u00058\u0000@\u0000X\u0081\u000e¢\u0006\u0002\n\u0000R\u0012\u0010\u0006\u001a\u00020\u00058\u0000@\u0000X\u0081\u000e¢\u0006\u0002\n\u0000R\u0011\u0010\u0007\u001a\u00020\u00058G¢\u0006\u0006\u001a\u0004\b\b\u0010\tR\"\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u000b8\u0000@\u0000X\u0081\u000e¢\u0006\n\n\u0002\u0010\r\u0012\u0004\b\f\u0010\u0003R\u0018\u0010\u000e\u001a\u00020\u000f8\u0000@\u0000X\u0081\u000e¢\u0006\b\n\u0000\u0012\u0004\b\u0010\u0010\u0003R\u0011\u0010\u0011\u001a\u00020\u00058G¢\u0006\u0006\u001a\u0004\b\u0012\u0010\t\u0082\u0001\u0001=\u0082\u0002\u0007\n\u0005\b\u009920\u0001¨\u0006>"}, d2 = {"Landroidx/collection/ScatterSet;", "E", "", "()V", "_capacity", "", "_size", "capacity", "getCapacity", "()I", "elements", "", "getElements$annotations", "[Ljava/lang/Object;", "metadata", "", "getMetadata$annotations", "size", "getSize", "all", "", "predicate", "Lkotlin/Function1;", "Lkotlin/ParameterName;", "name", "element", "any", "asSet", "", "contains", "(Ljava/lang/Object;)Z", "count", "equals", "other", "findElementIndex", "findElementIndex$collection", "(Ljava/lang/Object;)I", "first", "()Ljava/lang/Object;", "(Lkotlin/jvm/functions/Function1;)Ljava/lang/Object;", "firstOrNull", "forEach", "", "block", "forEachIndex", "index", "hashCode", "isEmpty", "isNotEmpty", "joinToString", "", "separator", "", "prefix", "postfix", "limit", "truncated", "transform", "none", "toString", "SetWrapper", "Landroidx/collection/MutableScatterSet;", "collection"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public abstract class ScatterSet<E> {
    public int _capacity;
    public int _size;
    public Object[] elements;
    public long[] metadata;

    public /* synthetic */ ScatterSet(DefaultConstructorMarker defaultConstructorMarker) {
        this();
    }

    public static /* synthetic */ void getElements$annotations() {
    }

    public static /* synthetic */ void getMetadata$annotations() {
    }

    public final String joinToString() {
        return joinToString$default(this, null, null, null, 0, null, null, 63, null);
    }

    public final String joinToString(CharSequence separator) {
        Intrinsics.checkNotNullParameter(separator, "separator");
        return joinToString$default(this, separator, null, null, 0, null, null, 62, null);
    }

    public final String joinToString(CharSequence separator, CharSequence prefix) {
        Intrinsics.checkNotNullParameter(separator, "separator");
        Intrinsics.checkNotNullParameter(prefix, "prefix");
        return joinToString$default(this, separator, prefix, null, 0, null, null, 60, null);
    }

    public final String joinToString(CharSequence separator, CharSequence prefix, CharSequence postfix) {
        Intrinsics.checkNotNullParameter(separator, "separator");
        Intrinsics.checkNotNullParameter(prefix, "prefix");
        Intrinsics.checkNotNullParameter(postfix, "postfix");
        return joinToString$default(this, separator, prefix, postfix, 0, null, null, 56, null);
    }

    public final String joinToString(CharSequence separator, CharSequence prefix, CharSequence postfix, int i) {
        Intrinsics.checkNotNullParameter(separator, "separator");
        Intrinsics.checkNotNullParameter(prefix, "prefix");
        Intrinsics.checkNotNullParameter(postfix, "postfix");
        return joinToString$default(this, separator, prefix, postfix, i, null, null, 48, null);
    }

    public final String joinToString(CharSequence separator, CharSequence prefix, CharSequence postfix, int i, CharSequence truncated) {
        Intrinsics.checkNotNullParameter(separator, "separator");
        Intrinsics.checkNotNullParameter(prefix, "prefix");
        Intrinsics.checkNotNullParameter(postfix, "postfix");
        Intrinsics.checkNotNullParameter(truncated, "truncated");
        return joinToString$default(this, separator, prefix, postfix, i, truncated, null, 32, null);
    }

    private ScatterSet() {
        this.metadata = ScatterMapKt.EmptyGroup;
        this.elements = ContainerHelpersKt.EMPTY_OBJECTS;
    }

    /* renamed from: getCapacity, reason: from getter */
    public final int get_capacity() {
        return this._capacity;
    }

    /* renamed from: getSize, reason: from getter */
    public final int get_size() {
        return this._size;
    }

    public final boolean any() {
        return this._size != 0;
    }

    public final boolean none() {
        return this._size == 0;
    }

    public final boolean isEmpty() {
        return this._size == 0;
    }

    public final boolean isNotEmpty() {
        return this._size != 0;
    }

    /* JADX WARN: Removed duplicated region for block: B:17:0x0055  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final E first() {
        /*
            r19 = this;
            r0 = 0
            r1 = r19
            r2 = 0
            java.lang.Object[] r3 = r1.elements
            r4 = r1
            r5 = 0
            long[] r6 = r4.metadata
            int r7 = r6.length
            int r7 = r7 + (-2)
            r8 = 0
            if (r8 > r7) goto L5a
        L12:
            r9 = r6[r8]
            r11 = r9
            r13 = 0
            long r14 = ~r11
            r16 = 7
            long r14 = r14 << r16
            long r14 = r14 & r11
            r16 = -9187201950435737472(0x8080808080808080, double:-2.937446524422997E-306)
            long r11 = r14 & r16
            int r11 = (r11 > r16 ? 1 : (r11 == r16 ? 0 : -1))
            if (r11 == 0) goto L55
            int r11 = r8 - r7
            int r11 = ~r11
            int r11 = r11 >>> 31
            r12 = 8
            int r11 = 8 - r11
            r13 = 0
        L31:
            if (r13 >= r11) goto L53
            r14 = 255(0xff, double:1.26E-321)
            long r14 = r14 & r9
            r16 = 0
            r17 = 128(0x80, double:6.32E-322)
            int r17 = (r14 > r17 ? 1 : (r14 == r17 ? 0 : -1))
            if (r17 >= 0) goto L41
            r17 = 1
            goto L43
        L41:
            r17 = 0
        L43:
            if (r17 == 0) goto L4f
            int r12 = r8 << 3
            int r12 = r12 + r13
            r14 = r12
            r15 = 0
            r16 = r3[r14]
            r17 = 0
            return r16
        L4f:
            long r9 = r9 >> r12
            int r13 = r13 + 1
            goto L31
        L53:
            if (r11 != r12) goto L5b
        L55:
            if (r8 == r7) goto L5a
            int r8 = r8 + 1
            goto L12
        L5a:
        L5b:
            java.util.NoSuchElementException r1 = new java.util.NoSuchElementException
            java.lang.String r2 = "The ScatterSet is empty"
            r1.<init>(r2)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.ScatterSet.first():java.lang.Object");
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x0079  */
    /* JADX WARN: Type inference failed for: r2v7, types: [E, java.lang.Object] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final E first(kotlin.jvm.functions.Function1<? super E, java.lang.Boolean> r22) {
        /*
            r21 = this;
            r0 = r22
            java.lang.String r1 = "predicate"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r0, r1)
            r1 = 0
            r2 = r21
            r3 = 0
            java.lang.Object[] r4 = r2.elements
            r5 = r2
            r6 = 0
            long[] r7 = r5.metadata
            int r8 = r7.length
            int r8 = r8 + (-2)
            r9 = 0
            if (r9 > r8) goto L81
        L1a:
            r10 = r7[r9]
            r12 = r10
            r14 = 0
            r15 = r1
            r16 = r2
            long r1 = ~r12
            r17 = 7
            long r1 = r1 << r17
            long r1 = r1 & r12
            r17 = -9187201950435737472(0x8080808080808080, double:-2.937446524422997E-306)
            long r1 = r1 & r17
            int r1 = (r1 > r17 ? 1 : (r1 == r17 ? 0 : -1))
            if (r1 == 0) goto L79
            int r1 = r9 - r8
            int r1 = ~r1
            int r1 = r1 >>> 31
            r2 = 8
            int r1 = 8 - r1
            r12 = 0
        L3c:
            if (r12 >= r1) goto L75
            r13 = 255(0xff, double:1.26E-321)
            long r13 = r13 & r10
            r17 = 0
            r18 = 128(0x80, double:6.32E-322)
            int r18 = (r13 > r18 ? 1 : (r13 == r18 ? 0 : -1))
            if (r18 >= 0) goto L4c
            r18 = 1
            goto L4e
        L4c:
            r18 = 0
        L4e:
            if (r18 == 0) goto L6c
            int r13 = r9 << 3
            int r13 = r13 + r12
            r14 = r13
            r17 = 0
            r18 = r2
            r2 = r4[r14]
            r19 = 0
            java.lang.Object r20 = r0.invoke(r2)
            java.lang.Boolean r20 = (java.lang.Boolean) r20
            boolean r20 = r20.booleanValue()
            if (r20 == 0) goto L69
            return r2
        L69:
            goto L6e
        L6c:
            r18 = r2
        L6e:
            long r10 = r10 >> r18
            int r12 = r12 + 1
            r2 = r18
            goto L3c
        L75:
            r18 = r2
            if (r1 != r2) goto L85
        L79:
            if (r9 == r8) goto L84
            int r9 = r9 + 1
            r1 = r15
            r2 = r16
            goto L1a
        L81:
            r15 = r1
            r16 = r2
        L84:
        L85:
            java.util.NoSuchElementException r1 = new java.util.NoSuchElementException
            java.lang.String r2 = "Could not find a match"
            r1.<init>(r2)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.ScatterSet.first(kotlin.jvm.functions.Function1):java.lang.Object");
    }

    /* JADX WARN: Type inference failed for: r2v6, types: [E, java.lang.Object] */
    public final E firstOrNull(Function1<? super E, Boolean> predicate) {
        int i;
        Intrinsics.checkNotNullParameter(predicate, "predicate");
        int i2 = 0;
        ScatterSet<E> scatterSet = this;
        Object[] objArr = scatterSet.elements;
        long[] jArr = scatterSet.metadata;
        int length = jArr.length - 2;
        int i3 = 0;
        if (0 > length) {
            return null;
        }
        while (true) {
            long j = jArr[i3];
            int i4 = i2;
            ScatterSet<E> scatterSet2 = scatterSet;
            if ((((~j) << 7) & j & (-9187201950435737472L)) != -9187201950435737472L) {
                int i5 = 8;
                int i6 = 8 - ((~(i3 - length)) >>> 31);
                int i7 = 0;
                while (i7 < i6) {
                    if (!((255 & j) < 128)) {
                        i = i5;
                    } else {
                        i = i5;
                        ?? r2 = (Object) objArr[(i3 << 3) + i7];
                        if (predicate.invoke(r2).booleanValue()) {
                            return r2;
                        }
                    }
                    j >>= i;
                    i7++;
                    i5 = i;
                }
                if (i6 != i5) {
                    return null;
                }
            }
            if (i3 == length) {
                return null;
            }
            i3++;
            i2 = i4;
            scatterSet = scatterSet2;
        }
    }

    public final void forEachIndex(Function1<? super Integer, Unit> block) {
        Intrinsics.checkNotNullParameter(block, "block");
        long[] m = this.metadata;
        int lastIndex = m.length - 2;
        int i = 0;
        if (0 > lastIndex) {
            return;
        }
        while (true) {
            long slot = m[i];
            long $this$maskEmptyOrDeleted$iv = ((~slot) << 7) & slot & (-9187201950435737472L);
            if ($this$maskEmptyOrDeleted$iv != -9187201950435737472L) {
                int bitCount = 8 - ((~(i - lastIndex)) >>> 31);
                for (int j = 0; j < bitCount; j++) {
                    long value$iv = 255 & slot;
                    if (value$iv < 128) {
                        int index = (i << 3) + j;
                        block.invoke(Integer.valueOf(index));
                    }
                    slot >>= 8;
                }
                if (bitCount != 8) {
                    return;
                }
            }
            if (i == lastIndex) {
                return;
            } else {
                i++;
            }
        }
    }

    public final void forEach(Function1<? super E, Unit> block) {
        int i;
        Intrinsics.checkNotNullParameter(block, "block");
        Object[] k = this.elements;
        long[] m$iv = this.metadata;
        int lastIndex$iv = m$iv.length - 2;
        int i$iv = 0;
        if (0 > lastIndex$iv) {
            return;
        }
        while (true) {
            long slot$iv = m$iv[i$iv];
            long $this$maskEmptyOrDeleted$iv$iv = ((~slot$iv) << 7) & slot$iv & (-9187201950435737472L);
            if ($this$maskEmptyOrDeleted$iv$iv != -9187201950435737472L) {
                int i2 = 8;
                int bitCount$iv = 8 - ((~(i$iv - lastIndex$iv)) >>> 31);
                int j$iv = 0;
                while (j$iv < bitCount$iv) {
                    long value$iv$iv = 255 & slot$iv;
                    if (!(value$iv$iv < 128)) {
                        i = i2;
                    } else {
                        int index$iv = (i$iv << 3) + j$iv;
                        i = i2;
                        block.invoke(k[index$iv]);
                    }
                    slot$iv >>= i;
                    j$iv++;
                    i2 = i;
                }
                if (bitCount$iv != i2) {
                    return;
                }
            }
            if (i$iv == lastIndex$iv) {
                return;
            } else {
                i$iv++;
            }
        }
    }

    public final boolean all(Function1<? super E, Boolean> predicate) {
        int i;
        Intrinsics.checkNotNullParameter(predicate, "predicate");
        Object[] k$iv = this.elements;
        long[] m$iv$iv = this.metadata;
        int lastIndex$iv$iv = m$iv$iv.length - 2;
        int i$iv$iv = 0;
        if (0 > lastIndex$iv$iv) {
            return true;
        }
        while (true) {
            long slot$iv$iv = m$iv$iv[i$iv$iv];
            long slot$iv$iv2 = slot$iv$iv;
            if ((((~slot$iv$iv) << 7) & slot$iv$iv & (-9187201950435737472L)) != -9187201950435737472L) {
                int i2 = 8;
                int bitCount$iv$iv = 8 - ((~(i$iv$iv - lastIndex$iv$iv)) >>> 31);
                int j$iv$iv = 0;
                while (j$iv$iv < bitCount$iv$iv) {
                    long value$iv$iv$iv = slot$iv$iv2 & 255;
                    if (!(value$iv$iv$iv < 128)) {
                        i = i2;
                    } else {
                        int index$iv$iv = (i$iv$iv << 3) + j$iv$iv;
                        i = i2;
                        Object element = k$iv[index$iv$iv];
                        if (!predicate.invoke(element).booleanValue()) {
                            return false;
                        }
                    }
                    slot$iv$iv2 >>= i;
                    j$iv$iv++;
                    i2 = i;
                }
                if (bitCount$iv$iv != i2) {
                    return true;
                }
            }
            if (i$iv$iv == lastIndex$iv$iv) {
                return true;
            }
            i$iv$iv++;
        }
    }

    public final boolean any(Function1<? super E, Boolean> predicate) {
        int i;
        Intrinsics.checkNotNullParameter(predicate, "predicate");
        Object[] k$iv = this.elements;
        long[] m$iv$iv = this.metadata;
        int lastIndex$iv$iv = m$iv$iv.length - 2;
        int i$iv$iv = 0;
        if (0 > lastIndex$iv$iv) {
            return false;
        }
        while (true) {
            long slot$iv$iv = m$iv$iv[i$iv$iv];
            long slot$iv$iv2 = slot$iv$iv;
            if ((((~slot$iv$iv) << 7) & slot$iv$iv & (-9187201950435737472L)) != -9187201950435737472L) {
                int i2 = 8;
                int bitCount$iv$iv = 8 - ((~(i$iv$iv - lastIndex$iv$iv)) >>> 31);
                int j$iv$iv = 0;
                while (j$iv$iv < bitCount$iv$iv) {
                    long value$iv$iv$iv = slot$iv$iv2 & 255;
                    if (!(value$iv$iv$iv < 128)) {
                        i = i2;
                    } else {
                        int index$iv$iv = (i$iv$iv << 3) + j$iv$iv;
                        i = i2;
                        Object element = k$iv[index$iv$iv];
                        if (predicate.invoke(element).booleanValue()) {
                            return true;
                        }
                    }
                    slot$iv$iv2 >>= i;
                    j$iv$iv++;
                    i2 = i;
                }
                if (bitCount$iv$iv != i2) {
                    return false;
                }
            }
            if (i$iv$iv == lastIndex$iv$iv) {
                return false;
            }
            i$iv$iv++;
        }
    }

    public final int count() {
        return get_size();
    }

    public final int count(Function1<? super E, Boolean> predicate) {
        int i;
        Intrinsics.checkNotNullParameter(predicate, "predicate");
        int $i$f$count = 0;
        int count = 0;
        Object[] k$iv = this.elements;
        long[] m$iv$iv = this.metadata;
        int lastIndex$iv$iv = m$iv$iv.length - 2;
        int i$iv$iv = 0;
        if (0 <= lastIndex$iv$iv) {
            while (true) {
                long slot$iv$iv = m$iv$iv[i$iv$iv];
                int $i$f$count2 = $i$f$count;
                int count2 = count;
                if ((((~slot$iv$iv) << 7) & slot$iv$iv & (-9187201950435737472L)) == -9187201950435737472L) {
                    count = count2;
                } else {
                    int i2 = 8;
                    int bitCount$iv$iv = 8 - ((~(i$iv$iv - lastIndex$iv$iv)) >>> 31);
                    int j$iv$iv = 0;
                    while (j$iv$iv < bitCount$iv$iv) {
                        long value$iv$iv$iv = 255 & slot$iv$iv;
                        if (!(value$iv$iv$iv < 128)) {
                            i = i2;
                        } else {
                            int index$iv$iv = (i$iv$iv << 3) + j$iv$iv;
                            i = i2;
                            Object element = k$iv[index$iv$iv];
                            if (predicate.invoke(element).booleanValue()) {
                                count2++;
                            }
                        }
                        slot$iv$iv >>= i;
                        j$iv$iv++;
                        i2 = i;
                    }
                    if (bitCount$iv$iv != i2) {
                        return count2;
                    }
                    count = count2;
                }
                if (i$iv$iv == lastIndex$iv$iv) {
                    break;
                }
                i$iv$iv++;
                $i$f$count = $i$f$count2;
            }
        }
        return count;
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x0091, code lost:
    
        r9 = (((~r4) << 6) & r4) & (-9187201950435737472L);
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x009f, code lost:
    
        if (r9 == 0) goto L24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x00a2, code lost:
    
        r9 = -1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final boolean contains(E r25) {
        /*
            Method dump skipped, instructions count: 184
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.ScatterSet.contains(java.lang.Object):boolean");
    }

    public static /* synthetic */ String joinToString$default(ScatterSet scatterSet, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, int i, CharSequence charSequence4, Function1 function1, int i2, Object obj) {
        if (obj != null) {
            throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: joinToString");
        }
        if ((i2 & 1) != 0) {
        }
        if ((i2 & 2) != 0) {
        }
        if ((i2 & 4) != 0) {
        }
        if ((i2 & 8) != 0) {
            i = -1;
        }
        if ((i2 & 16) != 0) {
        }
        Function1 function12 = (i2 & 32) != 0 ? null : function1;
        CharSequence charSequence5 = charSequence4;
        return scatterSet.joinToString(charSequence, charSequence2, charSequence3, i, charSequence5, function12);
    }

    public final String joinToString(CharSequence separator, CharSequence prefix, CharSequence postfix, int limit, CharSequence truncated, Function1<? super E, ? extends CharSequence> transform) {
        StringBuilder sb;
        int i;
        CharSequence separator2 = separator;
        Intrinsics.checkNotNullParameter(separator2, "separator");
        Intrinsics.checkNotNullParameter(prefix, "prefix");
        Intrinsics.checkNotNullParameter(postfix, "postfix");
        Intrinsics.checkNotNullParameter(truncated, "truncated");
        StringBuilder $this$joinToString_u24lambda_u2415 = new StringBuilder();
        int i2 = 0;
        $this$joinToString_u24lambda_u2415.append(prefix);
        int index = 0;
        ScatterSet this_$iv = this;
        int $i$f$forEach = 0;
        Object[] k$iv = this_$iv.elements;
        long[] m$iv$iv = this_$iv.metadata;
        int lastIndex$iv$iv = m$iv$iv.length - 2;
        int i$iv$iv = 0;
        if (0 <= lastIndex$iv$iv) {
            loop0: while (true) {
                long slot$iv$iv = m$iv$iv[i$iv$iv];
                int i3 = i2;
                int index2 = index;
                ScatterSet this_$iv2 = this_$iv;
                int $i$f$forEach2 = $i$f$forEach;
                long $this$maskEmptyOrDeleted$iv$iv$iv = ((~slot$iv$iv) << 7) & slot$iv$iv & (-9187201950435737472L);
                if ($this$maskEmptyOrDeleted$iv$iv$iv == -9187201950435737472L) {
                    sb = $this$joinToString_u24lambda_u2415;
                    index = index2;
                } else {
                    int i4 = 8;
                    int bitCount$iv$iv = 8 - ((~(i$iv$iv - lastIndex$iv$iv)) >>> 31);
                    int j$iv$iv = 0;
                    int index3 = index2;
                    while (j$iv$iv < bitCount$iv$iv) {
                        long value$iv$iv$iv = slot$iv$iv & 255;
                        if (!(value$iv$iv$iv < 128)) {
                            sb = $this$joinToString_u24lambda_u2415;
                            i = i4;
                        } else {
                            int index$iv$iv = (i$iv$iv << 3) + j$iv$iv;
                            i = i4;
                            Object element = k$iv[index$iv$iv];
                            sb = $this$joinToString_u24lambda_u2415;
                            if (index3 == limit) {
                                $this$joinToString_u24lambda_u2415.append(truncated);
                                break loop0;
                            }
                            if (index3 != 0) {
                                $this$joinToString_u24lambda_u2415.append(separator2);
                            }
                            if (transform == null) {
                                $this$joinToString_u24lambda_u2415.append(element);
                            } else {
                                $this$joinToString_u24lambda_u2415.append(transform.invoke(element));
                            }
                            index3++;
                        }
                        slot$iv$iv >>= i;
                        j$iv$iv++;
                        separator2 = separator;
                        i4 = i;
                        $this$joinToString_u24lambda_u2415 = sb;
                    }
                    sb = $this$joinToString_u24lambda_u2415;
                    if (bitCount$iv$iv != i4) {
                        break;
                    }
                    index = index3;
                }
                if (i$iv$iv == lastIndex$iv$iv) {
                    break;
                }
                i$iv$iv++;
                separator2 = separator;
                this_$iv = this_$iv2;
                $i$f$forEach = $i$f$forEach2;
                i2 = i3;
                $this$joinToString_u24lambda_u2415 = sb;
            }
        } else {
            sb = $this$joinToString_u24lambda_u2415;
        }
        $this$joinToString_u24lambda_u2415.append(postfix);
        String string = sb.toString();
        Intrinsics.checkNotNullExpressionValue(string, "StringBuilder().apply(builderAction).toString()");
        return string;
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x0063 A[PHI: r0
      0x0063: PHI (r0v3 'hash' int) = (r0v2 'hash' int), (r0v4 'hash' int) binds: [B:5:0x0025, B:18:0x0061] A[DONT_GENERATE, DONT_INLINE]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public int hashCode() {
        /*
            r20 = this;
            r0 = 0
            r1 = r20
            r2 = 0
            java.lang.Object[] r3 = r1.elements
            r4 = r1
            r5 = 0
            long[] r6 = r4.metadata
            int r7 = r6.length
            int r7 = r7 + (-2)
            r8 = 0
            if (r8 > r7) goto L68
        L12:
            r9 = r6[r8]
            r11 = r9
            r13 = 0
            long r14 = ~r11
            r16 = 7
            long r14 = r14 << r16
            long r14 = r14 & r11
            r16 = -9187201950435737472(0x8080808080808080, double:-2.937446524422997E-306)
            long r11 = r14 & r16
            int r11 = (r11 > r16 ? 1 : (r11 == r16 ? 0 : -1))
            if (r11 == 0) goto L63
            int r11 = r8 - r7
            int r11 = ~r11
            int r11 = r11 >>> 31
            r12 = 8
            int r11 = 8 - r11
            r13 = 0
        L31:
            if (r13 >= r11) goto L61
            r14 = 255(0xff, double:1.26E-321)
            long r14 = r14 & r9
            r16 = 0
            r17 = 128(0x80, double:6.32E-322)
            int r17 = (r14 > r17 ? 1 : (r14 == r17 ? 0 : -1))
            r18 = 0
            if (r17 >= 0) goto L43
            r17 = 1
            goto L45
        L43:
            r17 = r18
        L45:
            if (r17 == 0) goto L5d
            int r14 = r8 << 3
            int r14 = r14 + r13
            r15 = r14
            r16 = 0
            r17 = r3[r15]
            r19 = 0
            if (r17 == 0) goto L57
            int r18 = r17.hashCode()
        L57:
            int r0 = r0 + r18
        L5d:
            long r9 = r9 >> r12
            int r13 = r13 + 1
            goto L31
        L61:
            if (r11 != r12) goto L69
        L63:
            if (r8 == r7) goto L68
            int r8 = r8 + 1
            goto L12
        L68:
        L69:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.ScatterSet.hashCode():int");
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean equals(Object other) {
        boolean z;
        boolean z2;
        boolean z3 = true;
        if (other == this) {
            return true;
        }
        boolean z4 = false;
        if (!(other instanceof ScatterSet) || ((ScatterSet) other).get_size() != get_size()) {
            return false;
        }
        ScatterSet scatterSet = (ScatterSet) other;
        ScatterSet this_$iv = this;
        Object[] k$iv = this_$iv.elements;
        long[] m$iv$iv = this_$iv.metadata;
        int lastIndex$iv$iv = m$iv$iv.length - 2;
        int i$iv$iv = 0;
        if (0 > lastIndex$iv$iv) {
            return true;
        }
        while (true) {
            long slot$iv$iv = m$iv$iv[i$iv$iv];
            boolean z5 = z3;
            boolean z6 = z4;
            ScatterSet this_$iv2 = this_$iv;
            long $this$maskEmptyOrDeleted$iv$iv$iv = ((~slot$iv$iv) << 7) & slot$iv$iv & (-9187201950435737472L);
            if ($this$maskEmptyOrDeleted$iv$iv$iv == -9187201950435737472L) {
                z = z6;
            } else {
                int bitCount$iv$iv = 8 - ((~(i$iv$iv - lastIndex$iv$iv)) >>> 31);
                int j$iv$iv = 0;
                while (j$iv$iv < bitCount$iv$iv) {
                    long value$iv$iv$iv = slot$iv$iv & 255;
                    if (!(value$iv$iv$iv < 128 ? z5 : z6)) {
                        z2 = z6;
                    } else {
                        int index$iv$iv = (i$iv$iv << 3) + j$iv$iv;
                        z2 = z6;
                        Object element = k$iv[index$iv$iv];
                        if (!scatterSet.contains(element)) {
                            return z2;
                        }
                    }
                    slot$iv$iv >>= 8;
                    j$iv$iv++;
                    z6 = z2;
                }
                z = z6;
                if (bitCount$iv$iv != 8) {
                    return z5;
                }
            }
            if (i$iv$iv == lastIndex$iv$iv) {
                return z5;
            }
            i$iv$iv++;
            this_$iv = this_$iv2;
            z3 = z5;
            z4 = z;
        }
    }

    public String toString() {
        return joinToString$default(this, null, "[", "]", 0, null, new Function1<E, CharSequence>(this) { // from class: androidx.collection.ScatterSet.toString.1
            final /* synthetic */ ScatterSet<E> this$0;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(1);
                this.this$0 = this;
            }

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ CharSequence invoke(Object p1) {
                return invoke((AnonymousClass1) p1);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function1
            public final CharSequence invoke(E e) {
                if (e == this.this$0) {
                    return "(this)";
                }
                return String.valueOf(e);
            }
        }, 25, null);
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x0090, code lost:
    
        r9 = (((~r4) << 6) & r4) & (-9187201950435737472L);
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x009e, code lost:
    
        if (r9 == 0) goto L21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x00a1, code lost:
    
        return -1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final int findElementIndex$collection(E r24) {
        /*
            r23 = this;
            r0 = r23
            r1 = r24
            r2 = 0
            r3 = 0
            if (r1 == 0) goto Ld
            int r5 = r1.hashCode()
            goto Le
        Ld:
            r5 = 0
        Le:
            r6 = -862048943(0xffffffffcc9e2d51, float:-8.2930312E7)
            int r5 = r5 * r6
            int r6 = r5 << 16
            r3 = r5 ^ r6
            r5 = 0
            r5 = r3 & 127(0x7f, float:1.78E-43)
            int r6 = r0._capacity
            r7 = 0
            int r7 = r3 >>> 7
            r7 = r7 & r6
            r8 = 0
        L22:
            long[] r9 = r0.metadata
            r10 = 0
            int r11 = r7 >> 3
            r12 = r7 & 7
            int r12 = r12 << 3
            r13 = r9[r11]
            long r13 = r13 >>> r12
            int r15 = r11 + 1
            r15 = r9[r15]
            int r17 = 64 - r12
            long r15 = r15 << r17
            r18 = r5
            long r4 = (long) r12
            long r4 = -r4
            r19 = 63
            long r4 = r4 >> r19
            long r4 = r4 & r15
            long r4 = r4 | r13
            r9 = r4
            r11 = 0
            r12 = r18
            long r13 = (long) r12
            r15 = 72340172838076673(0x101010101010101, double:7.748604185489348E-304)
            long r13 = r13 * r15
            long r13 = r13 ^ r9
            long r15 = r13 - r15
            r18 = r2
            r19 = r3
            long r2 = ~r13
            long r2 = r2 & r15
            r15 = -9187201950435737472(0x8080808080808080, double:-2.937446524422997E-306)
            long r2 = r2 & r15
        L5d:
            r9 = r2
            r11 = 0
            r13 = 0
            int r20 = (r9 > r13 ? 1 : (r9 == r13 ? 0 : -1))
            if (r20 == 0) goto L68
            r20 = 1
            goto L6a
        L68:
            r20 = 0
        L6a:
            if (r20 == 0) goto L90
            r9 = r2
            r11 = 0
            r13 = r9
            r20 = 0
            int r21 = java.lang.Long.numberOfTrailingZeros(r13)
            int r13 = r21 >> 3
            int r13 = r13 + r7
            r9 = r13 & r6
            java.lang.Object[] r10 = r0.elements
            r10 = r10[r9]
            boolean r10 = kotlin.jvm.internal.Intrinsics.areEqual(r10, r1)
            if (r10 == 0) goto L86
            return r9
        L86:
            r10 = r2
            r13 = 0
            r20 = 1
            long r20 = r10 - r20
            long r10 = r10 & r20
            r2 = r10
            goto L5d
        L90:
            r9 = r4
            r11 = 0
            r20 = r13
            long r13 = ~r9
            r22 = 6
            long r13 = r13 << r22
            long r13 = r13 & r9
            long r9 = r13 & r15
            int r9 = (r9 > r20 ? 1 : (r9 == r20 ? 0 : -1))
            if (r9 == 0) goto La3
        La1:
            r2 = -1
            return r2
        La3:
            int r8 = r8 + 8
            int r9 = r7 + r8
            r7 = r9 & r6
            r5 = r12
            r2 = r18
            r3 = r19
            goto L22
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.ScatterSet.findElementIndex$collection(java.lang.Object):int");
    }

    public final Set<E> asSet() {
        return new SetWrapper();
    }

    /* compiled from: ScatterSet.kt */
    @Metadata(d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\"\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u001e\n\u0002\b\u0002\n\u0002\u0010(\n\u0000\b\u0090\u0004\u0018\u00002\b\u0012\u0004\u0012\u00028\u00000\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00028\u0000H\u0096\u0002¢\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00028\u00000\rH\u0016J\b\u0010\u000e\u001a\u00020\bH\u0016J\u000f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00028\u00000\u0010H\u0096\u0002R\u0014\u0010\u0003\u001a\u00020\u00048VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006¨\u0006\u0011"}, d2 = {"Landroidx/collection/ScatterSet$SetWrapper;", "", "(Landroidx/collection/ScatterSet;)V", "size", "", "getSize", "()I", "contains", "", "element", "(Ljava/lang/Object;)Z", "containsAll", "elements", "", "isEmpty", "iterator", "", "collection"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public class SetWrapper implements Set<E>, KMappedMarker {
        @Override // java.util.Set, java.util.Collection
        public boolean add(E e) {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
        }

        @Override // java.util.Set, java.util.Collection
        public boolean addAll(Collection<? extends E> collection) {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
        }

        @Override // java.util.Set, java.util.Collection
        public void clear() {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
        }

        @Override // java.util.Set, java.util.Collection
        public boolean remove(Object obj) {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
        }

        @Override // java.util.Set, java.util.Collection
        public boolean removeAll(Collection<? extends Object> collection) {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
        }

        @Override // java.util.Set, java.util.Collection
        public boolean retainAll(Collection<? extends Object> collection) {
            throw new UnsupportedOperationException("Operation is not supported for read-only collection");
        }

        @Override // java.util.Set, java.util.Collection
        public Object[] toArray() {
            return CollectionToArray.toArray(this);
        }

        @Override // java.util.Set, java.util.Collection
        public <T> T[] toArray(T[] array) {
            Intrinsics.checkNotNullParameter(array, "array");
            return (T[]) CollectionToArray.toArray(this, array);
        }

        public SetWrapper() {
        }

        @Override // java.util.Set, java.util.Collection
        public final /* bridge */ int size() {
            return getSize();
        }

        public int getSize() {
            return ScatterSet.this._size;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.util.Set, java.util.Collection
        public boolean containsAll(Collection<? extends Object> elements) {
            Intrinsics.checkNotNullParameter(elements, "elements");
            ScatterSet<E> scatterSet = ScatterSet.this;
            Iterator<T> it = elements.iterator();
            while (it.hasNext()) {
                if (!scatterSet.contains(it.next())) {
                    return false;
                }
            }
            return true;
        }

        @Override // java.util.Set, java.util.Collection
        public boolean contains(Object element) {
            return ScatterSet.this.contains(element);
        }

        @Override // java.util.Set, java.util.Collection
        public boolean isEmpty() {
            return ScatterSet.this.isEmpty();
        }

        @Override // java.util.Set, java.util.Collection, java.lang.Iterable
        public Iterator<E> iterator() {
            return SequencesKt.iterator(new ScatterSet$SetWrapper$iterator$1(ScatterSet.this, null));
        }
    }
}
