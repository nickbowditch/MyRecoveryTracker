package kotlin.text;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.FunctionReferenceImpl;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequenceScope;
import kotlin.sequences.SequencesKt;

/* compiled from: Regex.kt */
@Metadata(d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\r\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0010\u0000\n\u0002\b\u0003\u0018\u0000 02\u00060\u0001j\u0002`\u0002:\u000201B\u000f\b\u0016\u0012\u0006\u0010\u0003\u001a\u00020\u0004¢\u0006\u0002\u0010\u0005B\u0017\b\u0016\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0006\u001a\u00020\u0007¢\u0006\u0002\u0010\bB\u001d\b\u0016\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00070\n¢\u0006\u0002\u0010\u000bB\u000f\b\u0001\u0012\u0006\u0010\f\u001a\u00020\r¢\u0006\u0002\u0010\u000eJ\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\u001a\u0010\u0018\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010\u001a\u001a\u00020\u001bJ\u001e\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00190\u001d2\u0006\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010\u001a\u001a\u00020\u001bJ\u001a\u0010\u001e\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020\u001bH\u0007J\u0010\u0010 \u001a\u0004\u0018\u00010\u00192\u0006\u0010\u0016\u001a\u00020\u0017J\u0011\u0010!\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\u0086\u0004J\u0018\u0010\"\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020\u001bH\u0007J\"\u0010#\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u00172\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00170%J\u0016\u0010#\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010&\u001a\u00020\u0004J\u0016\u0010'\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010&\u001a\u00020\u0004J\u001e\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00040)2\u0006\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010*\u001a\u00020\u001bJ \u0010+\u001a\b\u0012\u0004\u0012\u00020\u00040\u001d2\u0006\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010*\u001a\u00020\u001bH\u0007J\u0006\u0010,\u001a\u00020\rJ\b\u0010-\u001a\u00020\u0004H\u0016J\b\u0010.\u001a\u00020/H\u0002R\u0016\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\nX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004¢\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00070\n8F¢\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0003\u001a\u00020\u00048F¢\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013¨\u00062"}, d2 = {"Lkotlin/text/Regex;", "Ljava/io/Serializable;", "Lkotlin/io/Serializable;", "pattern", "", "(Ljava/lang/String;)V", "option", "Lkotlin/text/RegexOption;", "(Ljava/lang/String;Lkotlin/text/RegexOption;)V", "options", "", "(Ljava/lang/String;Ljava/util/Set;)V", "nativePattern", "Ljava/util/regex/Pattern;", "(Ljava/util/regex/Pattern;)V", "_options", "getOptions", "()Ljava/util/Set;", "getPattern", "()Ljava/lang/String;", "containsMatchIn", "", "input", "", "find", "Lkotlin/text/MatchResult;", "startIndex", "", "findAll", "Lkotlin/sequences/Sequence;", "matchAt", "index", "matchEntire", "matches", "matchesAt", "replace", "transform", "Lkotlin/Function1;", "replacement", "replaceFirst", "split", "", "limit", "splitToSequence", "toPattern", "toString", "writeReplace", "", "Companion", "Serialized", "kotlin-stdlib"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes4.dex */
public final class Regex implements Serializable {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private Set<? extends RegexOption> _options;
    private final Pattern nativePattern;

    public Regex(Pattern nativePattern) {
        Intrinsics.checkNotNullParameter(nativePattern, "nativePattern");
        this.nativePattern = nativePattern;
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public Regex(String pattern) {
        Intrinsics.checkNotNullParameter(pattern, "pattern");
        Pattern patternCompile = Pattern.compile(pattern);
        Intrinsics.checkNotNullExpressionValue(patternCompile, "compile(...)");
        this(patternCompile);
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public Regex(String pattern, RegexOption option) {
        Intrinsics.checkNotNullParameter(pattern, "pattern");
        Intrinsics.checkNotNullParameter(option, "option");
        Pattern patternCompile = Pattern.compile(pattern, INSTANCE.ensureUnicodeCase(option.getValue()));
        Intrinsics.checkNotNullExpressionValue(patternCompile, "compile(...)");
        this(patternCompile);
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public Regex(String pattern, Set<? extends RegexOption> options) {
        Intrinsics.checkNotNullParameter(pattern, "pattern");
        Intrinsics.checkNotNullParameter(options, "options");
        Pattern patternCompile = Pattern.compile(pattern, INSTANCE.ensureUnicodeCase(RegexKt.toInt(options)));
        Intrinsics.checkNotNullExpressionValue(patternCompile, "compile(...)");
        this(patternCompile);
    }

    public final String getPattern() {
        String strPattern = this.nativePattern.pattern();
        Intrinsics.checkNotNullExpressionValue(strPattern, "pattern(...)");
        return strPattern;
    }

    public final Set<RegexOption> getOptions() {
        Set it = this._options;
        if (it != null) {
            return it;
        }
        final int value$iv = this.nativePattern.flags();
        EnumSet $this$fromInt_u24lambda_u241$iv = EnumSet.allOf(RegexOption.class);
        Intrinsics.checkNotNull($this$fromInt_u24lambda_u241$iv);
        CollectionsKt.retainAll($this$fromInt_u24lambda_u241$iv, new Function1<RegexOption, Boolean>() { // from class: kotlin.text.Regex$special$$inlined$fromInt$1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(1);
            }

            @Override // kotlin.jvm.functions.Function1
            public final Boolean invoke(RegexOption regexOption) {
                return Boolean.valueOf((value$iv & regexOption.getMask()) == regexOption.getValue());
            }
        });
        Set it2 = Collections.unmodifiableSet($this$fromInt_u24lambda_u241$iv);
        Intrinsics.checkNotNullExpressionValue(it2, "unmodifiableSet(...)");
        this._options = it2;
        return it2;
    }

    public final boolean matches(CharSequence input) {
        Intrinsics.checkNotNullParameter(input, "input");
        return this.nativePattern.matcher(input).matches();
    }

    public final boolean containsMatchIn(CharSequence input) {
        Intrinsics.checkNotNullParameter(input, "input");
        return this.nativePattern.matcher(input).find();
    }

    public static /* synthetic */ MatchResult find$default(Regex regex, CharSequence charSequence, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            i = 0;
        }
        return regex.find(charSequence, i);
    }

    public final MatchResult find(CharSequence input, int startIndex) {
        Intrinsics.checkNotNullParameter(input, "input");
        Matcher matcher = this.nativePattern.matcher(input);
        Intrinsics.checkNotNullExpressionValue(matcher, "matcher(...)");
        return RegexKt.findNext(matcher, startIndex, input);
    }

    public static /* synthetic */ Sequence findAll$default(Regex regex, CharSequence charSequence, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            i = 0;
        }
        return regex.findAll(charSequence, i);
    }

    public final Sequence<MatchResult> findAll(final CharSequence input, final int startIndex) {
        Intrinsics.checkNotNullParameter(input, "input");
        if (startIndex < 0 || startIndex > input.length()) {
            throw new IndexOutOfBoundsException("Start index out of bounds: " + startIndex + ", input length: " + input.length());
        }
        return SequencesKt.generateSequence((Function0) new Function0<MatchResult>() { // from class: kotlin.text.Regex.findAll.1
            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            {
                super(0);
            }

            @Override // kotlin.jvm.functions.Function0
            public final MatchResult invoke() {
                return Regex.this.find(input, startIndex);
            }
        }, (Function1) AnonymousClass2.INSTANCE);
    }

    /* compiled from: Regex.kt */
    @Metadata(k = 3, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    /* renamed from: kotlin.text.Regex$findAll$2, reason: invalid class name */
    /* synthetic */ class AnonymousClass2 extends FunctionReferenceImpl implements Function1<MatchResult, MatchResult> {
        public static final AnonymousClass2 INSTANCE = new AnonymousClass2();

        AnonymousClass2() {
            super(1, MatchResult.class, "next", "next()Lkotlin/text/MatchResult;", 0);
        }

        @Override // kotlin.jvm.functions.Function1
        public final MatchResult invoke(MatchResult p0) {
            Intrinsics.checkNotNullParameter(p0, "p0");
            return p0.next();
        }
    }

    public final MatchResult matchEntire(CharSequence input) {
        Intrinsics.checkNotNullParameter(input, "input");
        Matcher matcher = this.nativePattern.matcher(input);
        Intrinsics.checkNotNullExpressionValue(matcher, "matcher(...)");
        return RegexKt.matchEntire(matcher, input);
    }

    public final MatchResult matchAt(CharSequence input, int index) {
        MatcherMatchResult matcherMatchResult;
        Intrinsics.checkNotNullParameter(input, "input");
        Matcher $this$matchAt_u24lambda_u241 = this.nativePattern.matcher(input).useAnchoringBounds(false).useTransparentBounds(true).region(index, input.length());
        if ($this$matchAt_u24lambda_u241.lookingAt()) {
            Intrinsics.checkNotNull($this$matchAt_u24lambda_u241);
            matcherMatchResult = new MatcherMatchResult($this$matchAt_u24lambda_u241, input);
        } else {
            matcherMatchResult = null;
        }
        return matcherMatchResult;
    }

    public final boolean matchesAt(CharSequence input, int index) {
        Intrinsics.checkNotNullParameter(input, "input");
        return this.nativePattern.matcher(input).useAnchoringBounds(false).useTransparentBounds(true).region(index, input.length()).lookingAt();
    }

    public final String replace(CharSequence input, String replacement) {
        Intrinsics.checkNotNullParameter(input, "input");
        Intrinsics.checkNotNullParameter(replacement, "replacement");
        String strReplaceAll = this.nativePattern.matcher(input).replaceAll(replacement);
        Intrinsics.checkNotNullExpressionValue(strReplaceAll, "replaceAll(...)");
        return strReplaceAll;
    }

    public final String replace(CharSequence input, Function1<? super MatchResult, ? extends CharSequence> transform) {
        Intrinsics.checkNotNullParameter(input, "input");
        Intrinsics.checkNotNullParameter(transform, "transform");
        MatchResult match = find$default(this, input, 0, 2, null);
        if (match == null) {
            return input.toString();
        }
        int lastStart = 0;
        int length = input.length();
        StringBuilder sb = new StringBuilder(length);
        do {
            MatchResult foundMatch = match;
            sb.append(input, lastStart, foundMatch.getRange().getStart().intValue());
            sb.append(transform.invoke(foundMatch));
            lastStart = foundMatch.getRange().getEndInclusive().intValue() + 1;
            match = foundMatch.next();
            if (lastStart >= length) {
                break;
            }
        } while (match != null);
        if (lastStart < length) {
            sb.append(input, lastStart, length);
        }
        String string = sb.toString();
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        return string;
    }

    public final String replaceFirst(CharSequence input, String replacement) {
        Intrinsics.checkNotNullParameter(input, "input");
        Intrinsics.checkNotNullParameter(replacement, "replacement");
        String strReplaceFirst = this.nativePattern.matcher(input).replaceFirst(replacement);
        Intrinsics.checkNotNullExpressionValue(strReplaceFirst, "replaceFirst(...)");
        return strReplaceFirst;
    }

    public static /* synthetic */ List split$default(Regex regex, CharSequence charSequence, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            i = 0;
        }
        return regex.split(charSequence, i);
    }

    public final List<String> split(CharSequence input, int limit) {
        Intrinsics.checkNotNullParameter(input, "input");
        StringsKt.requireNonNegativeLimit(limit);
        Matcher matcher = this.nativePattern.matcher(input);
        if (limit == 1 || !matcher.find()) {
            return CollectionsKt.listOf(input.toString());
        }
        ArrayList result = new ArrayList(limit > 0 ? RangesKt.coerceAtMost(limit, 10) : 10);
        int lastStart = 0;
        int lastSplit = limit - 1;
        do {
            result.add(input.subSequence(lastStart, matcher.start()).toString());
            lastStart = matcher.end();
            if (lastSplit >= 0 && result.size() == lastSplit) {
                break;
            }
        } while (matcher.find());
        result.add(input.subSequence(lastStart, input.length()).toString());
        return result;
    }

    public static /* synthetic */ Sequence splitToSequence$default(Regex regex, CharSequence charSequence, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            i = 0;
        }
        return regex.splitToSequence(charSequence, i);
    }

    public final Sequence<String> splitToSequence(CharSequence input, int limit) {
        Intrinsics.checkNotNullParameter(input, "input");
        StringsKt.requireNonNegativeLimit(limit);
        return SequencesKt.sequence(new C00751(input, limit, null));
    }

    /* compiled from: Regex.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\u0010\u0000\u001a\u00020\u0001*\b\u0012\u0004\u0012\u00020\u00030\u0002H\u008a@"}, d2 = {"<anonymous>", "", "Lkotlin/sequences/SequenceScope;", ""}, k = 3, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "kotlin.text.Regex$splitToSequence$1", f = "Regex.kt", i = {1, 1, 1}, l = {275, 283, 287}, m = "invokeSuspend", n = {"$this$sequence", "matcher", "splitCount"}, s = {"L$0", "L$1", "I$0"})
    /* renamed from: kotlin.text.Regex$splitToSequence$1, reason: invalid class name and case insensitive filesystem */
    static final class C00751 extends RestrictedSuspendLambda implements Function2<SequenceScope<? super String>, Continuation<? super Unit>, Object> {
        final /* synthetic */ CharSequence $input;
        final /* synthetic */ int $limit;
        int I$0;
        private /* synthetic */ Object L$0;
        Object L$1;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        C00751(CharSequence charSequence, int i, Continuation<? super C00751> continuation) {
            super(2, continuation);
            this.$input = charSequence;
            this.$limit = i;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            C00751 c00751 = Regex.this.new C00751(this.$input, this.$limit, continuation);
            c00751.L$0 = obj;
            return c00751;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(SequenceScope<? super String> sequenceScope, Continuation<? super Unit> continuation) {
            return ((C00751) create(sequenceScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Removed duplicated region for block: B:17:0x0071 A[RETURN] */
        /* JADX WARN: Removed duplicated region for block: B:20:0x007c  */
        /* JADX WARN: Removed duplicated region for block: B:25:0x00a4 A[RETURN] */
        /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:16:0x006f -> B:18:0x0072). Please report as a decompilation issue!!! */
        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public final java.lang.Object invokeSuspend(java.lang.Object r10) throws java.lang.Throwable {
            /*
                r9 = this;
                java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
                int r1 = r9.label
                r2 = 1
                switch(r1) {
                    case 0: goto L2c;
                    case 1: goto L27;
                    case 2: goto L18;
                    case 3: goto L12;
                    default: goto La;
                }
            La:
                java.lang.IllegalStateException r10 = new java.lang.IllegalStateException
                java.lang.String r0 = "call to 'resume' before 'invoke' with coroutine"
                r10.<init>(r0)
                throw r10
            L12:
                kotlin.ResultKt.throwOnFailure(r10)
                r6 = r9
                goto La5
            L18:
                int r1 = r9.I$0
                java.lang.Object r3 = r9.L$1
                java.util.regex.Matcher r3 = (java.util.regex.Matcher) r3
                java.lang.Object r4 = r9.L$0
                kotlin.sequences.SequenceScope r4 = (kotlin.sequences.SequenceScope) r4
                kotlin.ResultKt.throwOnFailure(r10)
                r6 = r9
                goto L72
            L27:
                kotlin.ResultKt.throwOnFailure(r10)
                goto Lba
            L2c:
                kotlin.ResultKt.throwOnFailure(r10)
                java.lang.Object r1 = r9.L$0
                kotlin.sequences.SequenceScope r1 = (kotlin.sequences.SequenceScope) r1
                kotlin.text.Regex r3 = kotlin.text.Regex.this
                java.util.regex.Pattern r3 = kotlin.text.Regex.access$getNativePattern$p(r3)
                java.lang.CharSequence r4 = r9.$input
                java.util.regex.Matcher r3 = r3.matcher(r4)
                int r4 = r9.$limit
                if (r4 == r2) goto La8
                boolean r4 = r3.find()
                if (r4 != 0) goto L4a
                goto La8
            L4a:
                r4 = 0
                r5 = 0
                r6 = r4
                r4 = r1
                r1 = r5
                r5 = r6
                r6 = r9
            L51:
                java.lang.CharSequence r7 = r6.$input
                int r8 = r3.start()
                java.lang.CharSequence r7 = r7.subSequence(r5, r8)
                java.lang.String r5 = r7.toString()
                r7 = r6
                kotlin.coroutines.Continuation r7 = (kotlin.coroutines.Continuation) r7
                r6.L$0 = r4
                r6.L$1 = r3
                r6.I$0 = r1
                r8 = 2
                r6.label = r8
                java.lang.Object r5 = r4.yield(r5, r7)
                if (r5 != r0) goto L72
                return r0
            L72:
                int r5 = r3.end()
                int r1 = r1 + r2
                int r7 = r6.$limit
                int r7 = r7 - r2
                if (r1 == r7) goto L82
                boolean r7 = r3.find()
                if (r7 != 0) goto L51
            L82:
            L83:
                java.lang.CharSequence r1 = r6.$input
                java.lang.CharSequence r2 = r6.$input
                int r2 = r2.length()
                java.lang.CharSequence r1 = r1.subSequence(r5, r2)
                java.lang.String r1 = r1.toString()
                r2 = r6
                kotlin.coroutines.Continuation r2 = (kotlin.coroutines.Continuation) r2
                r3 = 0
                r6.L$0 = r3
                r6.L$1 = r3
                r3 = 3
                r6.label = r3
                java.lang.Object r1 = r4.yield(r1, r2)
                if (r1 != r0) goto La5
                return r0
            La5:
                kotlin.Unit r0 = kotlin.Unit.INSTANCE
                return r0
            La8:
                java.lang.CharSequence r3 = r9.$input
                java.lang.String r3 = r3.toString()
                r4 = r9
                kotlin.coroutines.Continuation r4 = (kotlin.coroutines.Continuation) r4
                r9.label = r2
                java.lang.Object r1 = r1.yield(r3, r4)
                if (r1 != r0) goto Lba
                return r0
            Lba:
                kotlin.Unit r0 = kotlin.Unit.INSTANCE
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: kotlin.text.Regex.C00751.invokeSuspend(java.lang.Object):java.lang.Object");
        }
    }

    public String toString() {
        String string = this.nativePattern.toString();
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        return string;
    }

    /* renamed from: toPattern, reason: from getter */
    public final Pattern getNativePattern() {
        return this.nativePattern;
    }

    private final Object writeReplace() {
        String strPattern = this.nativePattern.pattern();
        Intrinsics.checkNotNullExpressionValue(strPattern, "pattern(...)");
        return new Serialized(strPattern, this.nativePattern.flags());
    }

    /* compiled from: Regex.kt */
    @Metadata(d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u0000\n\u0002\b\u0002\b\u0002\u0018\u0000 \u000e2\u00060\u0001j\u0002`\u0002:\u0001\u000eB\u0015\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006¢\u0006\u0002\u0010\u0007J\b\u0010\f\u001a\u00020\rH\u0002R\u0011\u0010\u0005\u001a\u00020\u0006¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0003\u001a\u00020\u0004¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b¨\u0006\u000f"}, d2 = {"Lkotlin/text/Regex$Serialized;", "Ljava/io/Serializable;", "Lkotlin/io/Serializable;", "pattern", "", "flags", "", "(Ljava/lang/String;I)V", "getFlags", "()I", "getPattern", "()Ljava/lang/String;", "readResolve", "", "Companion", "kotlin-stdlib"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    private static final class Serialized implements Serializable {
        private static final long serialVersionUID = 0;
        private final int flags;
        private final String pattern;

        public Serialized(String pattern, int flags) {
            Intrinsics.checkNotNullParameter(pattern, "pattern");
            this.pattern = pattern;
            this.flags = flags;
        }

        public final int getFlags() {
            return this.flags;
        }

        public final String getPattern() {
            return this.pattern;
        }

        private final Object readResolve() {
            Pattern patternCompile = Pattern.compile(this.pattern, this.flags);
            Intrinsics.checkNotNullExpressionValue(patternCompile, "compile(...)");
            return new Regex(patternCompile);
        }
    }

    /* compiled from: Regex.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004H\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007J\u000e\u0010\t\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\u0007¨\u0006\f"}, d2 = {"Lkotlin/text/Regex$Companion;", "", "()V", "ensureUnicodeCase", "", "flags", "escape", "", "literal", "escapeReplacement", "fromLiteral", "Lkotlin/text/Regex;", "kotlin-stdlib"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final Regex fromLiteral(String literal) {
            Intrinsics.checkNotNullParameter(literal, "literal");
            return new Regex(literal, RegexOption.LITERAL);
        }

        public final String escape(String literal) {
            Intrinsics.checkNotNullParameter(literal, "literal");
            String strQuote = Pattern.quote(literal);
            Intrinsics.checkNotNullExpressionValue(strQuote, "quote(...)");
            return strQuote;
        }

        public final String escapeReplacement(String literal) {
            Intrinsics.checkNotNullParameter(literal, "literal");
            String strQuoteReplacement = Matcher.quoteReplacement(literal);
            Intrinsics.checkNotNullExpressionValue(strQuoteReplacement, "quoteReplacement(...)");
            return strQuoteReplacement;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final int ensureUnicodeCase(int flags) {
            return (flags & 2) != 0 ? flags | 64 : flags;
        }
    }
}
