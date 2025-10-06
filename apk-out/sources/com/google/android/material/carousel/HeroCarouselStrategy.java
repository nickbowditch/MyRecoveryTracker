package com.google.android.material.carousel;

import android.view.View;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;

/* loaded from: classes.dex */
public class HeroCarouselStrategy extends CarouselStrategy {
    private int keylineCount = 0;
    private static final int[] SMALL_COUNTS = {1};
    private static final int[] MEDIUM_COUNTS = {0, 1};

    @Override // com.google.android.material.carousel.CarouselStrategy
    KeylineState onFirstChildMeasuredWithMargins(Carousel carousel, View child) {
        int[] iArrDoubleCounts;
        int[] iArrDoubleCounts2;
        Arrangement arrangement;
        int availableSpace = carousel.getContainerHeight();
        if (carousel.isHorizontal()) {
            availableSpace = carousel.getContainerWidth();
        }
        RecyclerView.LayoutParams childLayoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
        float childMargins = childLayoutParams.topMargin + childLayoutParams.bottomMargin;
        float measuredChildSize = child.getMeasuredWidth() * 2;
        if (carousel.isHorizontal()) {
            childMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;
            measuredChildSize = child.getMeasuredHeight() * 2;
        }
        float smallChildSizeMin = getSmallItemSizeMin() + childMargins;
        float smallChildSizeMax = Math.max(getSmallItemSizeMax() + childMargins, smallChildSizeMin);
        float targetLargeChildSize = Math.min(measuredChildSize + childMargins, availableSpace);
        float targetSmallChildSize = MathUtils.clamp((measuredChildSize / 3.0f) + childMargins, smallChildSizeMin + childMargins, smallChildSizeMax + childMargins);
        float targetMediumChildSize = (targetLargeChildSize + targetSmallChildSize) / 2.0f;
        int[] smallCounts = SMALL_COUNTS;
        if (availableSpace < 2.0f * smallChildSizeMin) {
            smallCounts = new int[]{0};
        }
        float minAvailableLargeSpace = availableSpace - (CarouselStrategyHelper.maxValue(SMALL_COUNTS) * smallChildSizeMax);
        int[] smallCounts2 = smallCounts;
        int largeCountMin = (int) Math.max(1.0d, Math.floor(minAvailableLargeSpace / targetLargeChildSize));
        int largeCountMax = (int) Math.ceil(availableSpace / targetLargeChildSize);
        int[] largeCounts = new int[(largeCountMax - largeCountMin) + 1];
        for (int i = 0; i < largeCounts.length; i++) {
            largeCounts[i] = largeCountMin + i;
        }
        boolean isCenterAligned = carousel.getCarouselAlignment() == 1;
        float f = availableSpace;
        if (isCenterAligned) {
            iArrDoubleCounts = doubleCounts(smallCounts2);
        } else {
            iArrDoubleCounts = smallCounts2;
        }
        if (isCenterAligned) {
            iArrDoubleCounts2 = doubleCounts(MEDIUM_COUNTS);
        } else {
            iArrDoubleCounts2 = MEDIUM_COUNTS;
        }
        Arrangement arrangement2 = Arrangement.findLowestCostArrangement(f, targetSmallChildSize, smallChildSizeMin, smallChildSizeMax, iArrDoubleCounts, targetMediumChildSize, iArrDoubleCounts2, targetLargeChildSize, largeCounts);
        this.keylineCount = arrangement2.getItemCount();
        if (arrangement2.getItemCount() <= carousel.getItemCount()) {
            arrangement = arrangement2;
        } else {
            isCenterAligned = false;
            arrangement = Arrangement.findLowestCostArrangement(availableSpace, targetSmallChildSize, smallChildSizeMin, smallChildSizeMax, smallCounts2, targetMediumChildSize, MEDIUM_COUNTS, targetLargeChildSize, largeCounts);
        }
        return CarouselStrategyHelper.createKeylineState(child.getContext(), childMargins, availableSpace, arrangement, isCenterAligned ? 1 : 0);
    }

    @Override // com.google.android.material.carousel.CarouselStrategy
    boolean shouldRefreshKeylineState(Carousel carousel, int oldItemCount) {
        if (carousel.getCarouselAlignment() == 1) {
            if (oldItemCount < this.keylineCount && carousel.getItemCount() >= this.keylineCount) {
                return true;
            }
            if (oldItemCount >= this.keylineCount && carousel.getItemCount() < this.keylineCount) {
                return true;
            }
        }
        return false;
    }
}
