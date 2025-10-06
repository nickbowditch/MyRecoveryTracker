package kotlin;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.jvm.JvmInline;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.UIntRange;
import kotlin.ranges.URangesKt;
import okhttp3.internal.ws.WebSocketProtocol;

/* compiled from: UShort.kt */
@Metadata(d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0010\u000f\n\u0000\n\u0002\u0010\n\n\u0002\b\t\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\u0010\u0000\n\u0002\b!\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u0005\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u000e\b\u0087@\u0018\u0000 v2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001vB\u0011\b\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\u0018\u0010\b\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0000H\u0087\f¢\u0006\u0004\b\n\u0010\u000bJ\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\u000eH\u0087\n¢\u0006\u0004\b\u000f\u0010\u0010J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\u0011H\u0087\n¢\u0006\u0004\b\u0012\u0010\u0013J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\u0014H\u0087\n¢\u0006\u0004\b\u0015\u0010\u0016J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\u0000H\u0097\n¢\u0006\u0004\b\u0017\u0010\u0018J\u0013\u0010\u0019\u001a\u00020\u0000H\u0087\nø\u0001\u0000¢\u0006\u0004\b\u001a\u0010\u0005J\u0018\u0010\u001b\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\u0087\n¢\u0006\u0004\b\u001c\u0010\u0010J\u0018\u0010\u001b\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\u0087\n¢\u0006\u0004\b\u001d\u0010\u0013J\u0018\u0010\u001b\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\u0087\n¢\u0006\u0004\b\u001e\u0010\u001fJ\u0018\u0010\u001b\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\u0087\n¢\u0006\u0004\b \u0010\u0018J\u001a\u0010!\u001a\u00020\"2\b\u0010\t\u001a\u0004\u0018\u00010#HÖ\u0003¢\u0006\u0004\b$\u0010%J\u0018\u0010&\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\u0087\b¢\u0006\u0004\b'\u0010\u0010J\u0018\u0010&\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\u0087\b¢\u0006\u0004\b(\u0010\u0013J\u0018\u0010&\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\u0087\b¢\u0006\u0004\b)\u0010\u001fJ\u0018\u0010&\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\u0087\b¢\u0006\u0004\b*\u0010\u0018J\u0010\u0010+\u001a\u00020\rHÖ\u0001¢\u0006\u0004\b,\u0010-J\u0013\u0010.\u001a\u00020\u0000H\u0087\nø\u0001\u0000¢\u0006\u0004\b/\u0010\u0005J\u0013\u00100\u001a\u00020\u0000H\u0087\bø\u0001\u0000¢\u0006\u0004\b1\u0010\u0005J\u0018\u00102\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\u0087\n¢\u0006\u0004\b3\u0010\u0010J\u0018\u00102\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\u0087\n¢\u0006\u0004\b4\u0010\u0013J\u0018\u00102\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\u0087\n¢\u0006\u0004\b5\u0010\u001fJ\u0018\u00102\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\u0087\n¢\u0006\u0004\b6\u0010\u0018J\u0018\u00107\u001a\u00020\u000e2\u0006\u0010\t\u001a\u00020\u000eH\u0087\b¢\u0006\u0004\b8\u00109J\u0018\u00107\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\u0087\b¢\u0006\u0004\b:\u0010\u0013J\u0018\u00107\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\u0087\b¢\u0006\u0004\b;\u0010\u001fJ\u0018\u00107\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0000H\u0087\b¢\u0006\u0004\b<\u0010\u000bJ\u0018\u0010=\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0000H\u0087\f¢\u0006\u0004\b>\u0010\u000bJ\u0018\u0010?\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\u0087\n¢\u0006\u0004\b@\u0010\u0010J\u0018\u0010?\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\u0087\n¢\u0006\u0004\bA\u0010\u0013J\u0018\u0010?\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\u0087\n¢\u0006\u0004\bB\u0010\u001fJ\u0018\u0010?\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\u0087\n¢\u0006\u0004\bC\u0010\u0018J\u0018\u0010D\u001a\u00020E2\u0006\u0010\t\u001a\u00020\u0000H\u0087\n¢\u0006\u0004\bF\u0010GJ\u0018\u0010H\u001a\u00020E2\u0006\u0010\t\u001a\u00020\u0000H\u0087\n¢\u0006\u0004\bI\u0010GJ\u0018\u0010J\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\u0087\n¢\u0006\u0004\bK\u0010\u0010J\u0018\u0010J\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\u0087\n¢\u0006\u0004\bL\u0010\u0013J\u0018\u0010J\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\u0087\n¢\u0006\u0004\bM\u0010\u001fJ\u0018\u0010J\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\u0087\n¢\u0006\u0004\bN\u0010\u0018J\u0018\u0010O\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\u0087\n¢\u0006\u0004\bP\u0010\u0010J\u0018\u0010O\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\u0087\n¢\u0006\u0004\bQ\u0010\u0013J\u0018\u0010O\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\u0087\n¢\u0006\u0004\bR\u0010\u001fJ\u0018\u0010O\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\u0087\n¢\u0006\u0004\bS\u0010\u0018J\u0010\u0010T\u001a\u00020UH\u0087\b¢\u0006\u0004\bV\u0010WJ\u0010\u0010X\u001a\u00020YH\u0087\b¢\u0006\u0004\bZ\u0010[J\u0010\u0010\\\u001a\u00020]H\u0087\b¢\u0006\u0004\b^\u0010_J\u0010\u0010`\u001a\u00020\rH\u0087\b¢\u0006\u0004\ba\u0010-J\u0010\u0010b\u001a\u00020cH\u0087\b¢\u0006\u0004\bd\u0010eJ\u0010\u0010f\u001a\u00020\u0003H\u0087\b¢\u0006\u0004\bg\u0010\u0005J\u000f\u0010h\u001a\u00020iH\u0016¢\u0006\u0004\bj\u0010kJ\u0013\u0010l\u001a\u00020\u000eH\u0087\bø\u0001\u0000¢\u0006\u0004\bm\u0010WJ\u0013\u0010n\u001a\u00020\u0011H\u0087\bø\u0001\u0000¢\u0006\u0004\bo\u0010-J\u0013\u0010p\u001a\u00020\u0014H\u0087\bø\u0001\u0000¢\u0006\u0004\bq\u0010eJ\u0013\u0010r\u001a\u00020\u0000H\u0087\bø\u0001\u0000¢\u0006\u0004\bs\u0010\u0005J\u0018\u0010t\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0000H\u0087\f¢\u0006\u0004\bu\u0010\u000bR\u0016\u0010\u0002\u001a\u00020\u00038\u0000X\u0081\u0004¢\u0006\b\n\u0000\u0012\u0004\b\u0006\u0010\u0007\u0088\u0001\u0002\u0092\u0001\u00020\u0003\u0082\u0002\u0004\n\u0002\b!¨\u0006w"}, d2 = {"Lkotlin/UShort;", "", "data", "", "constructor-impl", "(S)S", "getData$annotations", "()V", "and", "other", "and-xj2QHRw", "(SS)S", "compareTo", "", "Lkotlin/UByte;", "compareTo-7apg3OU", "(SB)I", "Lkotlin/UInt;", "compareTo-WZ4Q5Ns", "(SI)I", "Lkotlin/ULong;", "compareTo-VKZWuLQ", "(SJ)I", "compareTo-xj2QHRw", "(SS)I", "dec", "dec-Mh2AYeg", "div", "div-7apg3OU", "div-WZ4Q5Ns", "div-VKZWuLQ", "(SJ)J", "div-xj2QHRw", "equals", "", "", "equals-impl", "(SLjava/lang/Object;)Z", "floorDiv", "floorDiv-7apg3OU", "floorDiv-WZ4Q5Ns", "floorDiv-VKZWuLQ", "floorDiv-xj2QHRw", "hashCode", "hashCode-impl", "(S)I", "inc", "inc-Mh2AYeg", "inv", "inv-Mh2AYeg", "minus", "minus-7apg3OU", "minus-WZ4Q5Ns", "minus-VKZWuLQ", "minus-xj2QHRw", "mod", "mod-7apg3OU", "(SB)B", "mod-WZ4Q5Ns", "mod-VKZWuLQ", "mod-xj2QHRw", "or", "or-xj2QHRw", "plus", "plus-7apg3OU", "plus-WZ4Q5Ns", "plus-VKZWuLQ", "plus-xj2QHRw", "rangeTo", "Lkotlin/ranges/UIntRange;", "rangeTo-xj2QHRw", "(SS)Lkotlin/ranges/UIntRange;", "rangeUntil", "rangeUntil-xj2QHRw", "rem", "rem-7apg3OU", "rem-WZ4Q5Ns", "rem-VKZWuLQ", "rem-xj2QHRw", "times", "times-7apg3OU", "times-WZ4Q5Ns", "times-VKZWuLQ", "times-xj2QHRw", "toByte", "", "toByte-impl", "(S)B", "toDouble", "", "toDouble-impl", "(S)D", "toFloat", "", "toFloat-impl", "(S)F", "toInt", "toInt-impl", "toLong", "", "toLong-impl", "(S)J", "toShort", "toShort-impl", "toString", "", "toString-impl", "(S)Ljava/lang/String;", "toUByte", "toUByte-w2LRezQ", "toUInt", "toUInt-pVg5ArA", "toULong", "toULong-s-VKNKU", "toUShort", "toUShort-Mh2AYeg", "xor", "xor-xj2QHRw", "Companion", "kotlin-stdlib"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
@JvmInline
/* loaded from: classes.dex */
public final class UShort implements Comparable<UShort> {
    public static final short MAX_VALUE = -1;
    public static final short MIN_VALUE = 0;
    public static final int SIZE_BITS = 16;
    public static final int SIZE_BYTES = 2;
    private final short data;

    /* renamed from: box-impl, reason: not valid java name */
    public static final /* synthetic */ UShort m487boximpl(short s) {
        return new UShort(s);
    }

    /* renamed from: constructor-impl, reason: not valid java name */
    public static short m493constructorimpl(short s) {
        return s;
    }

    /* renamed from: equals-impl, reason: not valid java name */
    public static boolean m499equalsimpl(short s, Object obj) {
        return (obj instanceof UShort) && s == ((UShort) obj).getData();
    }

    /* renamed from: equals-impl0, reason: not valid java name */
    public static final boolean m500equalsimpl0(short s, short s2) {
        return s == s2;
    }

    public static /* synthetic */ void getData$annotations() {
    }

    /* renamed from: hashCode-impl, reason: not valid java name */
    public static int m505hashCodeimpl(short s) {
        return Short.hashCode(s);
    }

    public boolean equals(Object obj) {
        return m499equalsimpl(this.data, obj);
    }

    public int hashCode() {
        return m505hashCodeimpl(this.data);
    }

    /* renamed from: unbox-impl, reason: not valid java name and from getter */
    public final /* synthetic */ short getData() {
        return this.data;
    }

    @Override // java.lang.Comparable
    public /* bridge */ /* synthetic */ int compareTo(UShort uShort) {
        return Intrinsics.compare(getData() & MAX_VALUE, uShort.getData() & MAX_VALUE);
    }

    private /* synthetic */ UShort(short data) {
        this.data = data;
    }

    /* renamed from: compareTo-7apg3OU, reason: not valid java name */
    private static final int m488compareTo7apg3OU(short arg0, byte other) {
        return Intrinsics.compare(65535 & arg0, other & UByte.MAX_VALUE);
    }

    /* renamed from: compareTo-xj2QHRw, reason: not valid java name */
    private int m491compareToxj2QHRw(short other) {
        return Intrinsics.compare(getData() & MAX_VALUE, 65535 & other);
    }

    /* renamed from: compareTo-xj2QHRw, reason: not valid java name */
    private static int m492compareToxj2QHRw(short arg0, short other) {
        return Intrinsics.compare(arg0 & MAX_VALUE, 65535 & other);
    }

    /* renamed from: compareTo-WZ4Q5Ns, reason: not valid java name */
    private static final int m490compareToWZ4Q5Ns(short arg0, int other) {
        return Integer.compare(UInt.m307constructorimpl(65535 & arg0) ^ Integer.MIN_VALUE, other ^ Integer.MIN_VALUE);
    }

    /* renamed from: compareTo-VKZWuLQ, reason: not valid java name */
    private static final int m489compareToVKZWuLQ(short arg0, long other) {
        return Long.compare(ULong.m386constructorimpl(arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX) ^ Long.MIN_VALUE, other ^ Long.MIN_VALUE);
    }

    /* renamed from: plus-7apg3OU, reason: not valid java name */
    private static final int m517plus7apg3OU(short arg0, byte other) {
        return UInt.m307constructorimpl(UInt.m307constructorimpl(65535 & arg0) + UInt.m307constructorimpl(other & UByte.MAX_VALUE));
    }

    /* renamed from: plus-xj2QHRw, reason: not valid java name */
    private static final int m520plusxj2QHRw(short arg0, short other) {
        return UInt.m307constructorimpl(UInt.m307constructorimpl(arg0 & MAX_VALUE) + UInt.m307constructorimpl(65535 & other));
    }

    /* renamed from: plus-WZ4Q5Ns, reason: not valid java name */
    private static final int m519plusWZ4Q5Ns(short arg0, int other) {
        return UInt.m307constructorimpl(UInt.m307constructorimpl(65535 & arg0) + other);
    }

    /* renamed from: plus-VKZWuLQ, reason: not valid java name */
    private static final long m518plusVKZWuLQ(short arg0, long other) {
        return ULong.m386constructorimpl(ULong.m386constructorimpl(arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX) + other);
    }

    /* renamed from: minus-7apg3OU, reason: not valid java name */
    private static final int m508minus7apg3OU(short arg0, byte other) {
        return UInt.m307constructorimpl(UInt.m307constructorimpl(65535 & arg0) - UInt.m307constructorimpl(other & UByte.MAX_VALUE));
    }

    /* renamed from: minus-xj2QHRw, reason: not valid java name */
    private static final int m511minusxj2QHRw(short arg0, short other) {
        return UInt.m307constructorimpl(UInt.m307constructorimpl(arg0 & MAX_VALUE) - UInt.m307constructorimpl(65535 & other));
    }

    /* renamed from: minus-WZ4Q5Ns, reason: not valid java name */
    private static final int m510minusWZ4Q5Ns(short arg0, int other) {
        return UInt.m307constructorimpl(UInt.m307constructorimpl(65535 & arg0) - other);
    }

    /* renamed from: minus-VKZWuLQ, reason: not valid java name */
    private static final long m509minusVKZWuLQ(short arg0, long other) {
        return ULong.m386constructorimpl(ULong.m386constructorimpl(arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX) - other);
    }

    /* renamed from: times-7apg3OU, reason: not valid java name */
    private static final int m527times7apg3OU(short arg0, byte other) {
        return UInt.m307constructorimpl(UInt.m307constructorimpl(65535 & arg0) * UInt.m307constructorimpl(other & UByte.MAX_VALUE));
    }

    /* renamed from: times-xj2QHRw, reason: not valid java name */
    private static final int m530timesxj2QHRw(short arg0, short other) {
        return UInt.m307constructorimpl(UInt.m307constructorimpl(arg0 & MAX_VALUE) * UInt.m307constructorimpl(65535 & other));
    }

    /* renamed from: times-WZ4Q5Ns, reason: not valid java name */
    private static final int m529timesWZ4Q5Ns(short arg0, int other) {
        return UInt.m307constructorimpl(UInt.m307constructorimpl(65535 & arg0) * other);
    }

    /* renamed from: times-VKZWuLQ, reason: not valid java name */
    private static final long m528timesVKZWuLQ(short arg0, long other) {
        return ULong.m386constructorimpl(ULong.m386constructorimpl(arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX) * other);
    }

    /* renamed from: div-7apg3OU, reason: not valid java name */
    private static final int m495div7apg3OU(short arg0, byte other) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m307constructorimpl(65535 & arg0), UInt.m307constructorimpl(other & UByte.MAX_VALUE));
    }

    /* renamed from: div-xj2QHRw, reason: not valid java name */
    private static final int m498divxj2QHRw(short arg0, short other) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m307constructorimpl(arg0 & MAX_VALUE), UInt.m307constructorimpl(65535 & other));
    }

    /* renamed from: div-WZ4Q5Ns, reason: not valid java name */
    private static final int m497divWZ4Q5Ns(short arg0, int other) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m307constructorimpl(65535 & arg0), other);
    }

    /* renamed from: div-VKZWuLQ, reason: not valid java name */
    private static final long m496divVKZWuLQ(short arg0, long other) {
        return UByte$$ExternalSyntheticBackport3.m(ULong.m386constructorimpl(arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX), other);
    }

    /* renamed from: rem-7apg3OU, reason: not valid java name */
    private static final int m523rem7apg3OU(short arg0, byte other) {
        return UByte$$ExternalSyntheticBackport1.m(UInt.m307constructorimpl(65535 & arg0), UInt.m307constructorimpl(other & UByte.MAX_VALUE));
    }

    /* renamed from: rem-xj2QHRw, reason: not valid java name */
    private static final int m526remxj2QHRw(short arg0, short other) {
        return UByte$$ExternalSyntheticBackport1.m(UInt.m307constructorimpl(arg0 & MAX_VALUE), UInt.m307constructorimpl(65535 & other));
    }

    /* renamed from: rem-WZ4Q5Ns, reason: not valid java name */
    private static final int m525remWZ4Q5Ns(short arg0, int other) {
        return UByte$$ExternalSyntheticBackport1.m(UInt.m307constructorimpl(65535 & arg0), other);
    }

    /* renamed from: rem-VKZWuLQ, reason: not valid java name */
    private static final long m524remVKZWuLQ(short arg0, long other) {
        return UByte$$ExternalSyntheticBackport2.m(ULong.m386constructorimpl(arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX), other);
    }

    /* renamed from: floorDiv-7apg3OU, reason: not valid java name */
    private static final int m501floorDiv7apg3OU(short arg0, byte other) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m307constructorimpl(65535 & arg0), UInt.m307constructorimpl(other & UByte.MAX_VALUE));
    }

    /* renamed from: floorDiv-xj2QHRw, reason: not valid java name */
    private static final int m504floorDivxj2QHRw(short arg0, short other) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m307constructorimpl(arg0 & MAX_VALUE), UInt.m307constructorimpl(65535 & other));
    }

    /* renamed from: floorDiv-WZ4Q5Ns, reason: not valid java name */
    private static final int m503floorDivWZ4Q5Ns(short arg0, int other) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m307constructorimpl(65535 & arg0), other);
    }

    /* renamed from: floorDiv-VKZWuLQ, reason: not valid java name */
    private static final long m502floorDivVKZWuLQ(short arg0, long other) {
        return UByte$$ExternalSyntheticBackport3.m(ULong.m386constructorimpl(arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX), other);
    }

    /* renamed from: mod-7apg3OU, reason: not valid java name */
    private static final byte m512mod7apg3OU(short arg0, byte other) {
        return UByte.m230constructorimpl((byte) UByte$$ExternalSyntheticBackport1.m(UInt.m307constructorimpl(65535 & arg0), UInt.m307constructorimpl(other & UByte.MAX_VALUE)));
    }

    /* renamed from: mod-xj2QHRw, reason: not valid java name */
    private static final short m515modxj2QHRw(short arg0, short other) {
        return m493constructorimpl((short) UByte$$ExternalSyntheticBackport1.m(UInt.m307constructorimpl(arg0 & MAX_VALUE), UInt.m307constructorimpl(65535 & other)));
    }

    /* renamed from: mod-WZ4Q5Ns, reason: not valid java name */
    private static final int m514modWZ4Q5Ns(short arg0, int other) {
        return UByte$$ExternalSyntheticBackport1.m(UInt.m307constructorimpl(65535 & arg0), other);
    }

    /* renamed from: mod-VKZWuLQ, reason: not valid java name */
    private static final long m513modVKZWuLQ(short arg0, long other) {
        return UByte$$ExternalSyntheticBackport2.m(ULong.m386constructorimpl(arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX), other);
    }

    /* renamed from: inc-Mh2AYeg, reason: not valid java name */
    private static final short m506incMh2AYeg(short arg0) {
        return m493constructorimpl((short) (arg0 + 1));
    }

    /* renamed from: dec-Mh2AYeg, reason: not valid java name */
    private static final short m494decMh2AYeg(short arg0) {
        return m493constructorimpl((short) (arg0 - 1));
    }

    /* renamed from: rangeTo-xj2QHRw, reason: not valid java name */
    private static final UIntRange m521rangeToxj2QHRw(short arg0, short other) {
        return new UIntRange(UInt.m307constructorimpl(arg0 & MAX_VALUE), UInt.m307constructorimpl(65535 & other), null);
    }

    /* renamed from: rangeUntil-xj2QHRw, reason: not valid java name */
    private static final UIntRange m522rangeUntilxj2QHRw(short arg0, short other) {
        return URangesKt.m1480untilJ1ME1BU(UInt.m307constructorimpl(arg0 & MAX_VALUE), UInt.m307constructorimpl(65535 & other));
    }

    /* renamed from: and-xj2QHRw, reason: not valid java name */
    private static final short m486andxj2QHRw(short arg0, short other) {
        return m493constructorimpl((short) (arg0 & other));
    }

    /* renamed from: or-xj2QHRw, reason: not valid java name */
    private static final short m516orxj2QHRw(short arg0, short other) {
        return m493constructorimpl((short) (arg0 | other));
    }

    /* renamed from: xor-xj2QHRw, reason: not valid java name */
    private static final short m542xorxj2QHRw(short arg0, short other) {
        return m493constructorimpl((short) (arg0 ^ other));
    }

    /* renamed from: inv-Mh2AYeg, reason: not valid java name */
    private static final short m507invMh2AYeg(short arg0) {
        return m493constructorimpl((short) (~arg0));
    }

    /* renamed from: toByte-impl, reason: not valid java name */
    private static final byte m531toByteimpl(short arg0) {
        return (byte) arg0;
    }

    /* renamed from: toShort-impl, reason: not valid java name */
    private static final short m536toShortimpl(short arg0) {
        return arg0;
    }

    /* renamed from: toInt-impl, reason: not valid java name */
    private static final int m534toIntimpl(short arg0) {
        return 65535 & arg0;
    }

    /* renamed from: toLong-impl, reason: not valid java name */
    private static final long m535toLongimpl(short arg0) {
        return arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX;
    }

    /* renamed from: toUByte-w2LRezQ, reason: not valid java name */
    private static final byte m538toUBytew2LRezQ(short arg0) {
        return UByte.m230constructorimpl((byte) arg0);
    }

    /* renamed from: toUShort-Mh2AYeg, reason: not valid java name */
    private static final short m541toUShortMh2AYeg(short arg0) {
        return arg0;
    }

    /* renamed from: toUInt-pVg5ArA, reason: not valid java name */
    private static final int m539toUIntpVg5ArA(short arg0) {
        return UInt.m307constructorimpl(65535 & arg0);
    }

    /* renamed from: toULong-s-VKNKU, reason: not valid java name */
    private static final long m540toULongsVKNKU(short arg0) {
        return ULong.m386constructorimpl(arg0 & WebSocketProtocol.PAYLOAD_SHORT_MAX);
    }

    /* renamed from: toFloat-impl, reason: not valid java name */
    private static final float m533toFloatimpl(short arg0) {
        return (float) UnsignedKt.uintToDouble(65535 & arg0);
    }

    /* renamed from: toDouble-impl, reason: not valid java name */
    private static final double m532toDoubleimpl(short arg0) {
        return UnsignedKt.uintToDouble(65535 & arg0);
    }

    /* renamed from: toString-impl, reason: not valid java name */
    public static String m537toStringimpl(short arg0) {
        return String.valueOf(65535 & arg0);
    }

    public String toString() {
        return m537toStringimpl(this.data);
    }
}
