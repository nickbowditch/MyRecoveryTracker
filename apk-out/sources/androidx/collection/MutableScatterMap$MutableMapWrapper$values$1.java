package androidx.collection;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Collection;
import java.util.Iterator;
import kotlin.Metadata;
import kotlin.jvm.internal.CollectionToArray;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.markers.KMutableCollection;

/* JADX INFO: Add missing generic type declarations: [V] */
/* compiled from: ScatterMap.kt */
@Metadata(d1 = {"\u00001\n\u0000\n\u0002\u0010\u001f\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u001e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010)\n\u0002\b\u0004*\u0001\u0000\b\n\u0018\u00002\b\u0012\u0004\u0012\u00028\u00000\u0001J\u0015\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00028\u0000H\u0016¢\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00028\u00000\fH\u0016J\b\u0010\r\u001a\u00020\u000eH\u0016J\u0016\u0010\u000f\u001a\u00020\u00072\u0006\u0010\b\u001a\u00028\u0000H\u0096\u0002¢\u0006\u0002\u0010\tJ\u0016\u0010\u0010\u001a\u00020\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00028\u00000\fH\u0016J\b\u0010\u0011\u001a\u00020\u0007H\u0016J\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00028\u00000\u0013H\u0096\u0002J\u0015\u0010\u0014\u001a\u00020\u00072\u0006\u0010\b\u001a\u00028\u0000H\u0016¢\u0006\u0002\u0010\tJ\u0016\u0010\u0015\u001a\u00020\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00028\u00000\fH\u0016J\u0016\u0010\u0016\u001a\u00020\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00028\u00000\fH\u0016R\u0014\u0010\u0002\u001a\u00020\u00038VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005¨\u0006\u0017"}, d2 = {"androidx/collection/MutableScatterMap$MutableMapWrapper$values$1", "", "size", "", "getSize", "()I", "add", "", "element", "(Ljava/lang/Object;)Z", "addAll", "elements", "", "clear", "", "contains", "containsAll", "isEmpty", "iterator", "", "remove", "removeAll", "retainAll", "collection"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class MutableScatterMap$MutableMapWrapper$values$1<V> implements Collection<V>, KMutableCollection {
    final /* synthetic */ MutableScatterMap<K, V> this$0;

    @Override // java.util.Collection
    public Object[] toArray() {
        return CollectionToArray.toArray(this);
    }

    @Override // java.util.Collection
    public <T> T[] toArray(T[] array) {
        Intrinsics.checkNotNullParameter(array, "array");
        return (T[]) CollectionToArray.toArray(this, array);
    }

    MutableScatterMap$MutableMapWrapper$values$1(MutableScatterMap<K, V> mutableScatterMap) {
        this.this$0 = mutableScatterMap;
    }

    @Override // java.util.Collection
    public final /* bridge */ int size() {
        return getSize();
    }

    public int getSize() {
        return this.this$0._size;
    }

    @Override // java.util.Collection
    public boolean isEmpty() {
        return this.this$0.isEmpty();
    }

    @Override // java.util.Collection, java.lang.Iterable
    public Iterator<V> iterator() {
        return new MutableScatterMap$MutableMapWrapper$values$1$iterator$1(this.this$0);
    }

    @Override // java.util.Collection
    public void clear() {
        this.this$0.clear();
    }

    @Override // java.util.Collection
    public boolean addAll(Collection<? extends V> elements) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Collection
    public boolean add(V element) {
        throw new UnsupportedOperationException();
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x0078 A[PHI: r2
      0x0078: PHI (r2v4 'changed' boolean) = (r2v3 'changed' boolean), (r2v5 'changed' boolean) binds: [B:5:0x002c, B:20:0x0076] A[DONT_GENERATE, DONT_INLINE]] */
    @Override // java.util.Collection
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean retainAll(java.util.Collection<? extends java.lang.Object> r20) {
        /*
            r19 = this;
            r0 = r19
            r1 = r20
            java.lang.String r2 = "elements"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r1, r2)
            r2 = 0
            androidx.collection.MutableScatterMap<K, V> r3 = r0.this$0
            androidx.collection.ScatterMap r3 = (androidx.collection.ScatterMap) r3
            androidx.collection.MutableScatterMap<K, V> r4 = r0.this$0
            r5 = 0
            long[] r6 = r3.metadata
            int r7 = r6.length
            int r7 = r7 + (-2)
            r8 = 0
            if (r8 > r7) goto L7f
        L19:
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
            if (r11 == 0) goto L78
            int r11 = r8 - r7
            int r11 = ~r11
            int r11 = r11 >>> 31
            r12 = 8
            int r11 = 8 - r11
            r13 = 0
        L38:
            if (r13 >= r11) goto L72
            r14 = 255(0xff, double:1.26E-321)
            long r14 = r14 & r9
            r16 = 0
            r17 = 128(0x80, double:6.32E-322)
            int r17 = (r14 > r17 ? 1 : (r14 == r17 ? 0 : -1))
            if (r17 >= 0) goto L48
            r17 = 1
            goto L4a
        L48:
            r17 = 0
        L4a:
            if (r17 == 0) goto L67
            int r14 = r8 << 3
            int r14 = r14 + r13
            r15 = r14
            r16 = 0
            r17 = r12
            r12 = r1
            java.lang.Iterable r12 = (java.lang.Iterable) r12
            java.lang.Object[] r0 = r4.values
            r0 = r0[r15]
            boolean r0 = kotlin.collections.CollectionsKt.contains(r12, r0)
            if (r0 != 0) goto L65
            r4.removeValueAt(r15)
            r2 = 1
        L65:
            goto L69
        L67:
            r17 = r12
        L69:
            long r9 = r9 >> r17
            int r13 = r13 + 1
            r0 = r19
            r12 = r17
            goto L38
        L72:
            r17 = r12
            r0 = r17
            if (r11 != r0) goto L80
        L78:
            if (r8 == r7) goto L7f
            int r8 = r8 + 1
            r0 = r19
            goto L19
        L7f:
        L80:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.MutableScatterMap$MutableMapWrapper$values$1.retainAll(java.util.Collection):boolean");
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x0078 A[PHI: r2
      0x0078: PHI (r2v4 'changed' boolean) = (r2v3 'changed' boolean), (r2v5 'changed' boolean) binds: [B:5:0x002c, B:20:0x0076] A[DONT_GENERATE, DONT_INLINE]] */
    @Override // java.util.Collection
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean removeAll(java.util.Collection<? extends java.lang.Object> r20) {
        /*
            r19 = this;
            r0 = r19
            r1 = r20
            java.lang.String r2 = "elements"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r1, r2)
            r2 = 0
            androidx.collection.MutableScatterMap<K, V> r3 = r0.this$0
            androidx.collection.ScatterMap r3 = (androidx.collection.ScatterMap) r3
            androidx.collection.MutableScatterMap<K, V> r4 = r0.this$0
            r5 = 0
            long[] r6 = r3.metadata
            int r7 = r6.length
            int r7 = r7 + (-2)
            r8 = 0
            if (r8 > r7) goto L7f
        L19:
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
            if (r11 == 0) goto L78
            int r11 = r8 - r7
            int r11 = ~r11
            int r11 = r11 >>> 31
            r12 = 8
            int r11 = 8 - r11
            r13 = 0
        L38:
            if (r13 >= r11) goto L72
            r14 = 255(0xff, double:1.26E-321)
            long r14 = r14 & r9
            r16 = 0
            r17 = 128(0x80, double:6.32E-322)
            int r17 = (r14 > r17 ? 1 : (r14 == r17 ? 0 : -1))
            if (r17 >= 0) goto L48
            r17 = 1
            goto L4a
        L48:
            r17 = 0
        L4a:
            if (r17 == 0) goto L67
            int r14 = r8 << 3
            int r14 = r14 + r13
            r15 = r14
            r16 = 0
            r17 = r12
            r12 = r1
            java.lang.Iterable r12 = (java.lang.Iterable) r12
            java.lang.Object[] r0 = r4.values
            r0 = r0[r15]
            boolean r0 = kotlin.collections.CollectionsKt.contains(r12, r0)
            if (r0 == 0) goto L65
            r4.removeValueAt(r15)
            r2 = 1
        L65:
            goto L69
        L67:
            r17 = r12
        L69:
            long r9 = r9 >> r17
            int r13 = r13 + 1
            r0 = r19
            r12 = r17
            goto L38
        L72:
            r17 = r12
            r0 = r17
            if (r11 != r0) goto L80
        L78:
            if (r8 == r7) goto L7f
            int r8 = r8 + 1
            r0 = r19
            goto L19
        L7f:
        L80:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.MutableScatterMap$MutableMapWrapper$values$1.removeAll(java.util.Collection):boolean");
    }

    @Override // java.util.Collection
    public boolean remove(Object element) {
        boolean z;
        int i;
        ScatterMap this_$iv = this.this$0;
        MutableScatterMap<K, V> mutableScatterMap = this.this$0;
        long[] m$iv = this_$iv.metadata;
        int lastIndex$iv = m$iv.length - 2;
        int i$iv = 0;
        if (0 > lastIndex$iv) {
            return false;
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
                        if (Intrinsics.areEqual(mutableScatterMap.values[index$iv], element)) {
                            mutableScatterMap.removeValueAt(index$iv);
                            return true;
                        }
                    }
                    slot$iv >>= i;
                    j$iv++;
                    i2 = i;
                }
                z = false;
                if (bitCount$iv != i2) {
                    return false;
                }
            } else {
                z = false;
            }
            if (i$iv == lastIndex$iv) {
                return z;
            }
            i$iv++;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Collection
    public boolean containsAll(Collection<? extends Object> elements) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        Collection<? extends Object> $this$all$iv = elements;
        MutableScatterMap<K, V> mutableScatterMap = this.this$0;
        if ($this$all$iv.isEmpty()) {
            return true;
        }
        for (Object element$iv : $this$all$iv) {
            if (!mutableScatterMap.containsValue(element$iv)) {
                return false;
            }
        }
        return true;
    }

    @Override // java.util.Collection
    public boolean contains(Object element) {
        return this.this$0.containsValue(element);
    }
}
