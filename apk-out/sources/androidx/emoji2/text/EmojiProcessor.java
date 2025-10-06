package androidx.emoji2.text;

import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import androidx.emoji2.text.EmojiCompat;
import androidx.emoji2.text.MetadataRepo;
import java.util.Arrays;
import java.util.Set;

/* loaded from: classes.dex */
final class EmojiProcessor {
    private static final int ACTION_ADVANCE_BOTH = 1;
    private static final int ACTION_ADVANCE_END = 2;
    private static final int ACTION_FLUSH = 3;
    private static final int MAX_LOOK_AROUND_CHARACTER = 16;
    private final int[] mEmojiAsDefaultStyleExceptions;
    private EmojiCompat.GlyphChecker mGlyphChecker;
    private final MetadataRepo mMetadataRepo;
    private final EmojiCompat.SpanFactory mSpanFactory;
    private final boolean mUseEmojiAsDefaultStyle;

    private interface EmojiProcessCallback<T> {
        T getResult();

        boolean handleEmoji(CharSequence charSequence, int i, int i2, TypefaceEmojiRasterizer typefaceEmojiRasterizer);
    }

    EmojiProcessor(MetadataRepo metadataRepo, EmojiCompat.SpanFactory spanFactory, EmojiCompat.GlyphChecker glyphChecker, boolean useEmojiAsDefaultStyle, int[] emojiAsDefaultStyleExceptions, Set<int[]> emojiExclusions) {
        this.mSpanFactory = spanFactory;
        this.mMetadataRepo = metadataRepo;
        this.mGlyphChecker = glyphChecker;
        this.mUseEmojiAsDefaultStyle = useEmojiAsDefaultStyle;
        this.mEmojiAsDefaultStyleExceptions = emojiAsDefaultStyleExceptions;
        initExclusions(emojiExclusions);
    }

    private void initExclusions(Set<int[]> emojiExclusions) {
        if (emojiExclusions.isEmpty()) {
            return;
        }
        for (int[] codepoints : emojiExclusions) {
            String emoji = new String(codepoints, 0, codepoints.length);
            MarkExclusionCallback callback = new MarkExclusionCallback(emoji);
            process(emoji, 0, emoji.length(), 1, true, callback);
        }
    }

    int getEmojiMatch(CharSequence charSequence) {
        return getEmojiMatch(charSequence, this.mMetadataRepo.getMetadataVersion());
    }

    int getEmojiMatch(CharSequence charSequence, int metadataVersion) {
        ProcessorSm sm = new ProcessorSm(this.mMetadataRepo.getRootNode(), this.mUseEmojiAsDefaultStyle, this.mEmojiAsDefaultStyleExceptions);
        int end = charSequence.length();
        int currentOffset = 0;
        int potentialSubsequenceMatch = 0;
        int subsequenceMatch = 0;
        while (currentOffset < end) {
            int codePoint = Character.codePointAt(charSequence, currentOffset);
            int action = sm.check(codePoint);
            TypefaceEmojiRasterizer currentNode = sm.getCurrentMetadata();
            switch (action) {
                case 1:
                    currentOffset += Character.charCount(codePoint);
                    potentialSubsequenceMatch = 0;
                    break;
                case 2:
                    currentOffset += Character.charCount(codePoint);
                    break;
                case 3:
                    currentNode = sm.getFlushMetadata();
                    if (currentNode.getCompatAdded() <= metadataVersion) {
                        subsequenceMatch++;
                        break;
                    }
                    break;
            }
            if (currentNode != null && currentNode.getCompatAdded() <= metadataVersion) {
                potentialSubsequenceMatch++;
            }
        }
        if (subsequenceMatch != 0) {
            return 2;
        }
        if (sm.isInFlushableState()) {
            TypefaceEmojiRasterizer exactMatch = sm.getCurrentMetadata();
            if (exactMatch.getCompatAdded() <= metadataVersion) {
                return 1;
            }
        }
        if (potentialSubsequenceMatch != 0) {
            return 2;
        }
        return 0;
    }

    int getEmojiStart(CharSequence charSequence, int offset) {
        if (offset >= 0 && offset < charSequence.length()) {
            if (charSequence instanceof Spanned) {
                Spanned spanned = (Spanned) charSequence;
                EmojiSpan[] spans = (EmojiSpan[]) spanned.getSpans(offset, offset + 1, EmojiSpan.class);
                if (spans.length > 0) {
                    return spanned.getSpanStart(spans[0]);
                }
            }
            int start = Math.max(0, offset - 16);
            int end = Math.min(charSequence.length(), offset + 16);
            return ((EmojiProcessLookupCallback) process(charSequence, start, end, Integer.MAX_VALUE, true, new EmojiProcessLookupCallback(offset))).start;
        }
        return -1;
    }

    int getEmojiEnd(CharSequence charSequence, int offset) {
        if (offset >= 0 && offset < charSequence.length()) {
            if (charSequence instanceof Spanned) {
                Spanned spanned = (Spanned) charSequence;
                EmojiSpan[] spans = (EmojiSpan[]) spanned.getSpans(offset, offset + 1, EmojiSpan.class);
                if (spans.length > 0) {
                    return spanned.getSpanEnd(spans[0]);
                }
            }
            int start = Math.max(0, offset - 16);
            int end = Math.min(charSequence.length(), offset + 16);
            return ((EmojiProcessLookupCallback) process(charSequence, start, end, Integer.MAX_VALUE, true, new EmojiProcessLookupCallback(offset))).end;
        }
        return -1;
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x003c A[Catch: all -> 0x002c, TRY_ENTER, TryCatch #4 {all -> 0x002c, blocks: (B:7:0x000d, B:10:0x0012, B:12:0x0016, B:14:0x0025, B:21:0x003c, B:23:0x0046, B:25:0x0049, B:27:0x004d, B:29:0x0059, B:30:0x005c), top: B:82:0x000d }] */
    /* JADX WARN: Removed duplicated region for block: B:33:0x006c  */
    /* JADX WARN: Removed duplicated region for block: B:64:0x00d5  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x00da  */
    /* JADX WARN: Removed duplicated region for block: B:84:0x0070 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    java.lang.CharSequence process(java.lang.CharSequence r10, int r11, int r12, int r13, boolean r14) throws java.lang.Throwable {
        /*
            Method dump skipped, instructions count: 237
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.emoji2.text.EmojiProcessor.process(java.lang.CharSequence, int, int, int, boolean):java.lang.CharSequence");
    }

    private <T> T process(CharSequence charSequence, int start, int end, int maxEmojiCount, boolean processAll, EmojiProcessCallback<T> emojiProcessCallback) {
        int addedCount = 0;
        ProcessorSm sm = new ProcessorSm(this.mMetadataRepo.getRootNode(), this.mUseEmojiAsDefaultStyle, this.mEmojiAsDefaultStyleExceptions);
        int currentOffset = start;
        int codePoint = Character.codePointAt(charSequence, currentOffset);
        boolean keepProcessing = true;
        while (currentOffset < end && addedCount < maxEmojiCount && keepProcessing) {
            int action = sm.check(codePoint);
            switch (action) {
                case 1:
                    start += Character.charCount(Character.codePointAt(charSequence, start));
                    currentOffset = start;
                    if (currentOffset >= end) {
                        break;
                    } else {
                        codePoint = Character.codePointAt(charSequence, currentOffset);
                        break;
                    }
                case 2:
                    currentOffset += Character.charCount(codePoint);
                    if (currentOffset >= end) {
                        break;
                    } else {
                        codePoint = Character.codePointAt(charSequence, currentOffset);
                        break;
                    }
                case 3:
                    if (processAll || !hasGlyph(charSequence, start, currentOffset, sm.getFlushMetadata())) {
                        keepProcessing = emojiProcessCallback.handleEmoji(charSequence, start, currentOffset, sm.getFlushMetadata());
                        addedCount++;
                    }
                    start = currentOffset;
                    break;
            }
        }
        if (sm.isInFlushableState() && addedCount < maxEmojiCount && keepProcessing && (processAll || !hasGlyph(charSequence, start, currentOffset, sm.getCurrentMetadata()))) {
            emojiProcessCallback.handleEmoji(charSequence, start, currentOffset, sm.getCurrentMetadata());
            int i = addedCount + 1;
        }
        return emojiProcessCallback.getResult();
    }

    static boolean handleOnKeyDown(Editable editable, int keyCode, KeyEvent event) {
        boolean handled;
        switch (keyCode) {
            case 67:
                handled = delete(editable, event, false);
                break;
            case 112:
                handled = delete(editable, event, true);
                break;
            default:
                handled = false;
                break;
        }
        if (!handled) {
            return false;
        }
        MetaKeyKeyListener.adjustMetaAfterKeypress(editable);
        return true;
    }

    private static boolean delete(Editable content, KeyEvent event, boolean forwardDelete) {
        EmojiSpan[] spans;
        if (hasModifiers(event)) {
            return false;
        }
        int start = Selection.getSelectionStart(content);
        int end = Selection.getSelectionEnd(content);
        if (!hasInvalidSelection(start, end) && (spans = (EmojiSpan[]) content.getSpans(start, end, EmojiSpan.class)) != null && spans.length > 0) {
            for (EmojiSpan span : spans) {
                int spanStart = content.getSpanStart(span);
                int spanEnd = content.getSpanEnd(span);
                if ((forwardDelete && spanStart == start) || ((!forwardDelete && spanEnd == start) || (start > spanStart && start < spanEnd))) {
                    content.delete(spanStart, spanEnd);
                    return true;
                }
            }
        }
        return false;
    }

    static boolean handleDeleteSurroundingText(InputConnection inputConnection, Editable editable, int beforeLength, int afterLength, boolean inCodePoints) {
        int start;
        int end;
        if (editable == null || inputConnection == null || beforeLength < 0 || afterLength < 0) {
            return false;
        }
        int selectionStart = Selection.getSelectionStart(editable);
        int selectionEnd = Selection.getSelectionEnd(editable);
        if (hasInvalidSelection(selectionStart, selectionEnd)) {
            return false;
        }
        if (inCodePoints) {
            start = CodepointIndexFinder.findIndexBackward(editable, selectionStart, Math.max(beforeLength, 0));
            end = CodepointIndexFinder.findIndexForward(editable, selectionEnd, Math.max(afterLength, 0));
            if (start == -1 || end == -1) {
                return false;
            }
        } else {
            int start2 = selectionStart - beforeLength;
            start = Math.max(start2, 0);
            end = Math.min(selectionEnd + afterLength, editable.length());
        }
        EmojiSpan[] spans = (EmojiSpan[]) editable.getSpans(start, end, EmojiSpan.class);
        if (spans == null || spans.length <= 0) {
            return false;
        }
        for (EmojiSpan span : spans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            start = Math.min(spanStart, start);
            end = Math.max(spanEnd, end);
        }
        int start3 = Math.max(start, 0);
        int start4 = editable.length();
        int end2 = Math.min(end, start4);
        inputConnection.beginBatchEdit();
        editable.delete(start3, end2);
        inputConnection.endBatchEdit();
        return true;
    }

    private static boolean hasInvalidSelection(int start, int end) {
        return start == -1 || end == -1 || start != end;
    }

    private static boolean hasModifiers(KeyEvent event) {
        return !KeyEvent.metaStateHasNoModifiers(event.getMetaState());
    }

    private boolean hasGlyph(CharSequence charSequence, int start, int end, TypefaceEmojiRasterizer rasterizer) {
        if (rasterizer.getHasGlyph() == 0) {
            boolean hasGlyph = this.mGlyphChecker.hasGlyph(charSequence, start, end, rasterizer.getSdkAdded());
            rasterizer.setHasGlyph(hasGlyph);
        }
        return rasterizer.getHasGlyph() == 2;
    }

    static final class ProcessorSm {
        private static final int STATE_DEFAULT = 1;
        private static final int STATE_WALKING = 2;
        private int mCurrentDepth;
        private MetadataRepo.Node mCurrentNode;
        private final int[] mEmojiAsDefaultStyleExceptions;
        private MetadataRepo.Node mFlushNode;
        private int mLastCodepoint;
        private final MetadataRepo.Node mRootNode;
        private int mState = 1;
        private final boolean mUseEmojiAsDefaultStyle;

        ProcessorSm(MetadataRepo.Node rootNode, boolean useEmojiAsDefaultStyle, int[] emojiAsDefaultStyleExceptions) {
            this.mRootNode = rootNode;
            this.mCurrentNode = rootNode;
            this.mUseEmojiAsDefaultStyle = useEmojiAsDefaultStyle;
            this.mEmojiAsDefaultStyleExceptions = emojiAsDefaultStyleExceptions;
        }

        int check(int codePoint) {
            int action;
            MetadataRepo.Node node = this.mCurrentNode.get(codePoint);
            switch (this.mState) {
                case 2:
                    if (node != null) {
                        this.mCurrentNode = node;
                        this.mCurrentDepth++;
                        action = 2;
                        break;
                    } else if (isTextStyle(codePoint)) {
                        action = reset();
                        break;
                    } else if (isEmojiStyle(codePoint)) {
                        action = 2;
                        break;
                    } else if (this.mCurrentNode.getData() != null) {
                        if (this.mCurrentDepth != 1 || shouldUseEmojiPresentationStyleForSingleCodepoint()) {
                            this.mFlushNode = this.mCurrentNode;
                            action = 3;
                            reset();
                            break;
                        } else {
                            action = reset();
                            break;
                        }
                    } else {
                        action = reset();
                        break;
                    }
                    break;
                default:
                    if (node == null) {
                        action = reset();
                        break;
                    } else {
                        this.mState = 2;
                        this.mCurrentNode = node;
                        this.mCurrentDepth = 1;
                        action = 2;
                        break;
                    }
            }
            this.mLastCodepoint = codePoint;
            return action;
        }

        private int reset() {
            this.mState = 1;
            this.mCurrentNode = this.mRootNode;
            this.mCurrentDepth = 0;
            return 1;
        }

        TypefaceEmojiRasterizer getFlushMetadata() {
            return this.mFlushNode.getData();
        }

        TypefaceEmojiRasterizer getCurrentMetadata() {
            return this.mCurrentNode.getData();
        }

        boolean isInFlushableState() {
            return this.mState == 2 && this.mCurrentNode.getData() != null && (this.mCurrentDepth > 1 || shouldUseEmojiPresentationStyleForSingleCodepoint());
        }

        private boolean shouldUseEmojiPresentationStyleForSingleCodepoint() {
            if (this.mCurrentNode.getData().isDefaultEmoji() || isEmojiStyle(this.mLastCodepoint)) {
                return true;
            }
            if (this.mUseEmojiAsDefaultStyle) {
                if (this.mEmojiAsDefaultStyleExceptions == null) {
                    return true;
                }
                int codepoint = this.mCurrentNode.getData().getCodepointAt(0);
                int index = Arrays.binarySearch(this.mEmojiAsDefaultStyleExceptions, codepoint);
                if (index < 0) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isEmojiStyle(int codePoint) {
            return codePoint == 65039;
        }

        private static boolean isTextStyle(int codePoint) {
            return codePoint == 65038;
        }
    }

    private static final class CodepointIndexFinder {
        private static final int INVALID_INDEX = -1;

        private CodepointIndexFinder() {
        }

        static int findIndexBackward(CharSequence cs, int from, int numCodePoints) {
            int currentIndex = from;
            boolean waitingHighSurrogate = false;
            int length = cs.length();
            if (currentIndex < 0 || length < currentIndex || numCodePoints < 0) {
                return -1;
            }
            int remainingCodePoints = numCodePoints;
            while (remainingCodePoints != 0) {
                currentIndex--;
                if (currentIndex < 0) {
                    if (waitingHighSurrogate) {
                        return -1;
                    }
                    return 0;
                }
                char c = cs.charAt(currentIndex);
                if (waitingHighSurrogate) {
                    if (!Character.isHighSurrogate(c)) {
                        return -1;
                    }
                    waitingHighSurrogate = false;
                    remainingCodePoints--;
                } else if (!Character.isSurrogate(c)) {
                    remainingCodePoints--;
                } else {
                    if (Character.isHighSurrogate(c)) {
                        return -1;
                    }
                    waitingHighSurrogate = true;
                }
            }
            return currentIndex;
        }

        static int findIndexForward(CharSequence cs, int from, int numCodePoints) {
            int currentIndex = from;
            boolean waitingLowSurrogate = false;
            int length = cs.length();
            if (currentIndex < 0 || length < currentIndex || numCodePoints < 0) {
                return -1;
            }
            int remainingCodePoints = numCodePoints;
            while (remainingCodePoints != 0) {
                if (currentIndex >= length) {
                    if (waitingLowSurrogate) {
                        return -1;
                    }
                    return length;
                }
                char c = cs.charAt(currentIndex);
                if (waitingLowSurrogate) {
                    if (!Character.isLowSurrogate(c)) {
                        return -1;
                    }
                    remainingCodePoints--;
                    waitingLowSurrogate = false;
                    currentIndex++;
                } else if (!Character.isSurrogate(c)) {
                    remainingCodePoints--;
                    currentIndex++;
                } else {
                    if (Character.isLowSurrogate(c)) {
                        return -1;
                    }
                    waitingLowSurrogate = true;
                    currentIndex++;
                }
            }
            return currentIndex;
        }
    }

    private static class EmojiProcessAddSpanCallback implements EmojiProcessCallback<UnprecomputeTextOnModificationSpannable> {
        private final EmojiCompat.SpanFactory mSpanFactory;
        public UnprecomputeTextOnModificationSpannable spannable;

        EmojiProcessAddSpanCallback(UnprecomputeTextOnModificationSpannable spannable, EmojiCompat.SpanFactory spanFactory) {
            this.spannable = spannable;
            this.mSpanFactory = spanFactory;
        }

        @Override // androidx.emoji2.text.EmojiProcessor.EmojiProcessCallback
        public boolean handleEmoji(CharSequence charSequence, int start, int end, TypefaceEmojiRasterizer metadata) {
            Spannable spannableString;
            if (metadata.isPreferredSystemRender()) {
                return true;
            }
            if (this.spannable == null) {
                if (charSequence instanceof Spannable) {
                    spannableString = (Spannable) charSequence;
                } else {
                    spannableString = new SpannableString(charSequence);
                }
                this.spannable = new UnprecomputeTextOnModificationSpannable(spannableString);
            }
            EmojiSpan span = this.mSpanFactory.createSpan(metadata);
            this.spannable.setSpan(span, start, end, 33);
            return true;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // androidx.emoji2.text.EmojiProcessor.EmojiProcessCallback
        public UnprecomputeTextOnModificationSpannable getResult() {
            return this.spannable;
        }
    }

    private static class EmojiProcessLookupCallback implements EmojiProcessCallback<EmojiProcessLookupCallback> {
        private final int mOffset;
        public int start = -1;
        public int end = -1;

        EmojiProcessLookupCallback(int offset) {
            this.mOffset = offset;
        }

        @Override // androidx.emoji2.text.EmojiProcessor.EmojiProcessCallback
        public boolean handleEmoji(CharSequence charSequence, int start, int end, TypefaceEmojiRasterizer metadata) {
            if (start > this.mOffset || this.mOffset >= end) {
                return end <= this.mOffset;
            }
            this.start = start;
            this.end = end;
            return false;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // androidx.emoji2.text.EmojiProcessor.EmojiProcessCallback
        public EmojiProcessLookupCallback getResult() {
            return this;
        }
    }

    private static class MarkExclusionCallback implements EmojiProcessCallback<MarkExclusionCallback> {
        private final String mExclusion;

        MarkExclusionCallback(String emoji) {
            this.mExclusion = emoji;
        }

        @Override // androidx.emoji2.text.EmojiProcessor.EmojiProcessCallback
        public boolean handleEmoji(CharSequence charSequence, int start, int end, TypefaceEmojiRasterizer metadata) {
            if (!TextUtils.equals(charSequence.subSequence(start, end), this.mExclusion)) {
                return true;
            }
            metadata.setExclusion(true);
            return false;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // androidx.emoji2.text.EmojiProcessor.EmojiProcessCallback
        public MarkExclusionCallback getResult() {
            return this;
        }
    }
}
