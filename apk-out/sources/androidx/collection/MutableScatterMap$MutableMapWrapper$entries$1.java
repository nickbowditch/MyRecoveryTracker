package androidx.collection;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import kotlin.Metadata;
import kotlin.jvm.internal.CollectionToArray;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import kotlin.jvm.internal.markers.KMutableSet;

/* JADX INFO: Add missing generic type declarations: [V, K] */
/* compiled from: ScatterMap.kt */
@Metadata(d1 = {"\u00005\n\u0000\n\u0002\u0010#\n\u0002\u0010'\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u001e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010)\n\u0002\b\u0004*\u0001\u0000\b\n\u0018\u00002\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u00020\u0001J\u001c\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u0002H\u0016J\"\u0010\n\u001a\u00020\b2\u0018\u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u00020\fH\u0016J\b\u0010\r\u001a\u00020\u000eH\u0016J\u001d\u0010\u000f\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u0002H\u0096\u0002J\"\u0010\u0010\u001a\u00020\b2\u0018\u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u00020\fH\u0016J\b\u0010\u0011\u001a\u00020\bH\u0016J\u001b\u0010\u0012\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u00020\u0013H\u0096\u0002J\u001c\u0010\u0014\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u0002H\u0016J\"\u0010\u0015\u001a\u00020\b2\u0018\u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u00020\fH\u0016J\"\u0010\u0016\u001a\u00020\b2\u0018\u0010\u000b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00010\u00020\fH\u0016R\u0014\u0010\u0003\u001a\u00020\u00048VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006¨\u0006\u0017"}, d2 = {"androidx/collection/MutableScatterMap$MutableMapWrapper$entries$1", "", "", "size", "", "getSize", "()I", "add", "", "element", "addAll", "elements", "", "clear", "", "contains", "containsAll", "isEmpty", "iterator", "", "remove", "removeAll", "retainAll", "collection"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class MutableScatterMap$MutableMapWrapper$entries$1<K, V> implements Set<Map.Entry<K, V>>, KMutableSet {
    final /* synthetic */ MutableScatterMap<K, V> this$0;

    @Override // java.util.Set, java.util.Collection
    public Object[] toArray() {
        return CollectionToArray.toArray(this);
    }

    @Override // java.util.Set, java.util.Collection
    public <T> T[] toArray(T[] array) {
        Intrinsics.checkNotNullParameter(array, "array");
        return (T[]) CollectionToArray.toArray(this, array);
    }

    MutableScatterMap$MutableMapWrapper$entries$1(MutableScatterMap<K, V> mutableScatterMap) {
        this.this$0 = mutableScatterMap;
    }

    @Override // java.util.Set, java.util.Collection
    public final /* bridge */ boolean contains(Object element) {
        if (TypeIntrinsics.isMutableMapEntry(element)) {
            return contains((Map.Entry) element);
        }
        return false;
    }

    @Override // java.util.Set, java.util.Collection
    public final /* bridge */ boolean remove(Object element) {
        if (TypeIntrinsics.isMutableMapEntry(element)) {
            return remove((Map.Entry) element);
        }
        return false;
    }

    @Override // java.util.Set, java.util.Collection
    public final /* bridge */ int size() {
        return getSize();
    }

    public int getSize() {
        return this.this$0._size;
    }

    @Override // java.util.Set, java.util.Collection
    public boolean isEmpty() {
        return this.this$0.isEmpty();
    }

    @Override // java.util.Set, java.util.Collection, java.lang.Iterable
    public Iterator<Map.Entry<K, V>> iterator() {
        return new MutableScatterMap$MutableMapWrapper$entries$1$iterator$1(this.this$0);
    }

    @Override // java.util.Set, java.util.Collection
    public void clear() {
        this.this$0.clear();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Set, java.util.Collection
    public boolean containsAll(Collection<? extends Object> elements) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        Collection<? extends Object> collection = elements;
        MutableScatterMap<K, V> mutableScatterMap = this.this$0;
        if (collection.isEmpty()) {
            return true;
        }
        Iterator<T> it = collection.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (!Intrinsics.areEqual(mutableScatterMap.get(entry.getKey()), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(Map.Entry<K, V> element) {
        Intrinsics.checkNotNullParameter(element, "element");
        return Intrinsics.areEqual(this.this$0.get(element.getKey()), element.getValue());
    }

    @Override // java.util.Set, java.util.Collection
    public boolean addAll(Collection<? extends Map.Entry<K, V>> elements) {
        Intrinsics.checkNotNullParameter(elements, "elements");
        throw new UnsupportedOperationException();
    }

    @Override // java.util.Set, java.util.Collection
    public boolean add(Map.Entry<K, V> element) {
        Intrinsics.checkNotNullParameter(element, "element");
        throw new UnsupportedOperationException();
    }

    /* JADX WARN: Removed duplicated region for block: B:31:0x00aa A[PHI: r1
      0x00aa: PHI (r1v4 'changed' boolean) = (r1v3 'changed' boolean), (r1v5 'changed' boolean) binds: [B:5:0x002c, B:30:0x00a8] A[DONT_GENERATE, DONT_INLINE]] */
    @Override // java.util.Set, java.util.Collection
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean retainAll(java.util.Collection<? extends java.lang.Object> r22) {
        /*
            r21 = this;
            r0 = r21
            java.lang.String r1 = "elements"
            r2 = r22
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r2, r1)
            r1 = 0
            androidx.collection.MutableScatterMap<K, V> r3 = r0.this$0
            androidx.collection.ScatterMap r3 = (androidx.collection.ScatterMap) r3
            androidx.collection.MutableScatterMap<K, V> r4 = r0.this$0
            r5 = 0
            long[] r6 = r3.metadata
            int r7 = r6.length
            int r7 = r7 + (-2)
            r8 = 0
            if (r8 > r7) goto Lb2
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
            if (r11 == 0) goto Laa
            int r11 = r8 - r7
            int r11 = ~r11
            int r11 = r11 >>> 31
            r12 = 8
            int r11 = 8 - r11
            r13 = 0
        L38:
            if (r13 >= r11) goto La4
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
            if (r17 == 0) goto L99
            int r14 = r8 << 3
            int r14 = r14 + r13
            r15 = r14
            r16 = 0
            r17 = 0
            java.util.Iterator r18 = r2.iterator()
        L58:
            boolean r19 = r18.hasNext()
            if (r19 == 0) goto L8f
            java.lang.Object r19 = r18.next()
            java.util.Map$Entry r19 = (java.util.Map.Entry) r19
            r20 = r12
            java.lang.Object r12 = r19.getKey()
            java.lang.Object[] r0 = r4.keys
            r0 = r0[r15]
            boolean r0 = kotlin.jvm.internal.Intrinsics.areEqual(r12, r0)
            if (r0 == 0) goto L8a
            java.lang.Object r0 = r19.getValue()
            java.lang.Object[] r12 = r4.values
            r12 = r12[r15]
            boolean r0 = kotlin.jvm.internal.Intrinsics.areEqual(r0, r12)
            if (r0 == 0) goto L85
            r17 = 1
            goto L91
        L85:
            r0 = r21
            r12 = r20
            goto L58
        L8a:
            r0 = r21
            r12 = r20
            goto L58
        L8f:
            r20 = r12
        L91:
            if (r17 != 0) goto L97
            r4.removeValueAt(r15)
            r1 = 1
        L97:
            goto L9b
        L99:
            r20 = r12
        L9b:
            long r9 = r9 >> r20
            int r13 = r13 + 1
            r0 = r21
            r12 = r20
            goto L38
        La4:
            r20 = r12
            r0 = r20
            if (r11 != r0) goto Lb3
        Laa:
            if (r8 == r7) goto Lb2
            int r8 = r8 + 1
            r0 = r21
            goto L19
        Lb2:
        Lb3:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.MutableScatterMap$MutableMapWrapper$entries$1.retainAll(java.util.Collection):boolean");
    }

    /* JADX WARN: Removed duplicated region for block: B:29:0x00a4 A[PHI: r1
      0x00a4: PHI (r1v4 'changed' boolean) = (r1v3 'changed' boolean), (r1v5 'changed' boolean) binds: [B:5:0x002c, B:28:0x00a2] A[DONT_GENERATE, DONT_INLINE]] */
    @Override // java.util.Set, java.util.Collection
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean removeAll(java.util.Collection<? extends java.lang.Object> r21) {
        /*
            r20 = this;
            r0 = r20
            java.lang.String r1 = "elements"
            r2 = r21
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r2, r1)
            r1 = 0
            androidx.collection.MutableScatterMap<K, V> r3 = r0.this$0
            androidx.collection.ScatterMap r3 = (androidx.collection.ScatterMap) r3
            androidx.collection.MutableScatterMap<K, V> r4 = r0.this$0
            r5 = 0
            long[] r6 = r3.metadata
            int r7 = r6.length
            int r7 = r7 + (-2)
            r8 = 0
            if (r8 > r7) goto Lac
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
            if (r11 == 0) goto La4
            int r11 = r8 - r7
            int r11 = ~r11
            int r11 = r11 >>> 31
            r12 = 8
            int r11 = 8 - r11
            r13 = 0
        L38:
            if (r13 >= r11) goto L9e
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
            if (r17 == 0) goto L93
            int r14 = r8 << 3
            int r14 = r14 + r13
            r15 = r14
            r16 = 0
            java.util.Iterator r17 = r2.iterator()
        L56:
            boolean r18 = r17.hasNext()
            if (r18 == 0) goto L8f
            java.lang.Object r18 = r17.next()
            java.util.Map$Entry r18 = (java.util.Map.Entry) r18
            r19 = r12
            java.lang.Object r12 = r18.getKey()
            java.lang.Object[] r0 = r4.keys
            r0 = r0[r15]
            boolean r0 = kotlin.jvm.internal.Intrinsics.areEqual(r12, r0)
            if (r0 == 0) goto L8a
            java.lang.Object r0 = r18.getValue()
            java.lang.Object[] r12 = r4.values
            r12 = r12[r15]
            boolean r0 = kotlin.jvm.internal.Intrinsics.areEqual(r0, r12)
            if (r0 == 0) goto L85
            r4.removeValueAt(r15)
            r1 = 1
            goto L91
        L85:
            r0 = r20
            r12 = r19
            goto L56
        L8a:
            r0 = r20
            r12 = r19
            goto L56
        L8f:
            r19 = r12
        L91:
            goto L95
        L93:
            r19 = r12
        L95:
            long r9 = r9 >> r19
            int r13 = r13 + 1
            r0 = r20
            r12 = r19
            goto L38
        L9e:
            r19 = r12
            r0 = r19
            if (r11 != r0) goto Lad
        La4:
            if (r8 == r7) goto Lac
            int r8 = r8 + 1
            r0 = r20
            goto L19
        Lac:
        Lad:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.MutableScatterMap$MutableMapWrapper$entries$1.removeAll(java.util.Collection):boolean");
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x00a6, code lost:
    
        r6 = (((~r6) << 6) & r6) & (-9187201950435737472L);
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x00b5, code lost:
    
        if (r6 == 0) goto L27;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x00b8, code lost:
    
        r6 = -1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean remove(java.util.Map.Entry<K, V> r27) {
        /*
            Method dump skipped, instructions count: 224
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.collection.MutableScatterMap$MutableMapWrapper$entries$1.remove(java.util.Map$Entry):boolean");
    }
}
