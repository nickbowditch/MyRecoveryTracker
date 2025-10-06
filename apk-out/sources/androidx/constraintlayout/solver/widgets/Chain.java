package androidx.constraintlayout.solver.widgets;

import androidx.constraintlayout.solver.LinearSystem;

/* loaded from: classes.dex */
class Chain {
    private static final boolean DEBUG = false;

    Chain() {
    }

    static void applyChainConstraints(ConstraintWidgetContainer constraintWidgetContainer, LinearSystem system, int orientation) {
        int offset;
        int chainsSize;
        ChainHead[] chainsArray;
        if (orientation == 0) {
            offset = 0;
            chainsSize = constraintWidgetContainer.mHorizontalChainsSize;
            chainsArray = constraintWidgetContainer.mHorizontalChainsArray;
        } else {
            offset = 2;
            chainsSize = constraintWidgetContainer.mVerticalChainsSize;
            chainsArray = constraintWidgetContainer.mVerticalChainsArray;
        }
        for (int i = 0; i < chainsSize; i++) {
            ChainHead first = chainsArray[i];
            first.define();
            applyChainConstraints(constraintWidgetContainer, system, orientation, offset, first);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:305:0x0612  */
    /* JADX WARN: Removed duplicated region for block: B:306:0x0617  */
    /* JADX WARN: Removed duplicated region for block: B:309:0x061c  */
    /* JADX WARN: Removed duplicated region for block: B:310:0x0621  */
    /* JADX WARN: Removed duplicated region for block: B:312:0x0624  */
    /* JADX WARN: Removed duplicated region for block: B:317:0x063b  */
    /* JADX WARN: Removed duplicated region for block: B:319:0x063e  */
    /* JADX WARN: Removed duplicated region for block: B:320:0x064a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    static void applyChainConstraints(androidx.constraintlayout.solver.widgets.ConstraintWidgetContainer r40, androidx.constraintlayout.solver.LinearSystem r41, int r42, int r43, androidx.constraintlayout.solver.widgets.ChainHead r44) {
        /*
            Method dump skipped, instructions count: 1644
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.constraintlayout.solver.widgets.Chain.applyChainConstraints(androidx.constraintlayout.solver.widgets.ConstraintWidgetContainer, androidx.constraintlayout.solver.LinearSystem, int, int, androidx.constraintlayout.solver.widgets.ChainHead):void");
    }
}
