package com.google.android.material.carousel;

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.carousel.KeylineState;

/* loaded from: classes.dex */
public final class UncontainedCarouselStrategy extends CarouselStrategy {
    private static final float MEDIUM_LARGE_ITEM_PERCENTAGE_THRESHOLD = 0.85f;

    @Override // com.google.android.material.carousel.CarouselStrategy
    KeylineState onFirstChildMeasuredWithMargins(Carousel carousel, View child) {
        float measuredChildSize;
        float measuredChildSize2;
        float availableSpace = carousel.isHorizontal() ? carousel.getContainerWidth() : carousel.getContainerHeight();
        RecyclerView.LayoutParams childLayoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
        float childMargins = childLayoutParams.topMargin + childLayoutParams.bottomMargin;
        float measuredChildSize3 = child.getMeasuredHeight();
        if (!carousel.isHorizontal()) {
            measuredChildSize = measuredChildSize3;
            measuredChildSize2 = childMargins;
        } else {
            float childMargins2 = childLayoutParams.leftMargin + childLayoutParams.rightMargin;
            float measuredChildSize4 = child.getMeasuredWidth();
            measuredChildSize = measuredChildSize4;
            measuredChildSize2 = childMargins2;
        }
        float largeChildSize = measuredChildSize + measuredChildSize2;
        float mediumChildSize = CarouselStrategyHelper.getExtraSmallSize(child.getContext()) + measuredChildSize2;
        float xSmallChildSize = CarouselStrategyHelper.getExtraSmallSize(child.getContext()) + measuredChildSize2;
        int largeCount = Math.max(1, (int) Math.floor(availableSpace / largeChildSize));
        float remainingSpace = availableSpace - (largeCount * largeChildSize);
        boolean isCenter = carousel.getCarouselAlignment() == 1;
        if (isCenter) {
            float remainingSpace2 = remainingSpace / 2.0f;
            float smallChildSizeMin = getSmallItemSizeMin() + measuredChildSize2;
            float mediumChildSize2 = Math.min(3.0f * remainingSpace2, largeChildSize);
            return createCenterAlignedKeylineState(availableSpace, measuredChildSize2, largeChildSize, largeCount, Math.max(mediumChildSize2, smallChildSizeMin), xSmallChildSize, remainingSpace2);
        }
        int mediumCount = remainingSpace > 0.0f ? 1 : 0;
        return createLeftAlignedKeylineState(child.getContext(), measuredChildSize2, availableSpace, largeChildSize, largeCount, calculateMediumChildSize(mediumChildSize, largeChildSize, remainingSpace), mediumCount, xSmallChildSize);
    }

    private float calculateMediumChildSize(float mediumChildSize, float largeChildSize, float remainingSpace) {
        float mediumChildSize2 = Math.max(1.5f * remainingSpace, mediumChildSize);
        float largeItemThreshold = MEDIUM_LARGE_ITEM_PERCENTAGE_THRESHOLD * largeChildSize;
        if (mediumChildSize2 > largeItemThreshold) {
            mediumChildSize2 = Math.max(largeItemThreshold, 1.2f * remainingSpace);
        }
        return Math.min(largeChildSize, mediumChildSize2);
    }

    private KeylineState createCenterAlignedKeylineState(float availableSpace, float childMargins, float largeSize, int largeCount, float mediumSize, float xSmallSize, float remainingSpace) {
        float xSmallSize2 = Math.min(xSmallSize, largeSize);
        float extraSmallMask = getChildMaskPercentage(xSmallSize2, largeSize, childMargins);
        float mediumMask = getChildMaskPercentage(mediumSize, largeSize, childMargins);
        float firstMediumCenterX = (0.0f + remainingSpace) - (mediumSize / 2.0f);
        float start = (mediumSize / 2.0f) + firstMediumCenterX;
        float extraSmallHeadCenterX = (firstMediumCenterX - (mediumSize / 2.0f)) - (xSmallSize2 / 2.0f);
        float largeStartCenterX = (largeSize / 2.0f) + start;
        float start2 = start + (largeCount * largeSize);
        KeylineState.Builder builder = new KeylineState.Builder(largeSize, availableSpace).addAnchorKeyline(extraSmallHeadCenterX, extraSmallMask, xSmallSize2).addKeyline(firstMediumCenterX, mediumMask, mediumSize, false).addKeylineRange(largeStartCenterX, 0.0f, largeSize, largeCount, true);
        float secondMediumCenterX = (mediumSize / 2.0f) + start2;
        builder.addKeyline(secondMediumCenterX, mediumMask, mediumSize, false);
        float xSmallCenterX = (xSmallSize2 / 2.0f) + start2 + mediumSize;
        builder.addAnchorKeyline(xSmallCenterX, extraSmallMask, xSmallSize2);
        return builder.build();
    }

    private KeylineState createLeftAlignedKeylineState(Context context, float childMargins, float availableSpace, float largeSize, int largeCount, float mediumSize, int mediumCount, float xSmallSize) {
        float xSmallSize2 = Math.min(xSmallSize, largeSize);
        float leftAnchorSize = Math.max(xSmallSize2, 0.5f * mediumSize);
        float leftAnchorMask = getChildMaskPercentage(leftAnchorSize, largeSize, childMargins);
        float extraSmallMask = getChildMaskPercentage(xSmallSize2, largeSize, childMargins);
        float mediumMask = getChildMaskPercentage(mediumSize, largeSize, childMargins);
        float leftAnchorCenterX = 0.0f - (leftAnchorSize / 2.0f);
        float largeStartCenterX = largeSize / 2.0f;
        float start = 0.0f + (largeCount * largeSize);
        KeylineState.Builder builder = new KeylineState.Builder(largeSize, availableSpace).addAnchorKeyline(leftAnchorCenterX, leftAnchorMask, leftAnchorSize).addKeylineRange(largeStartCenterX, 0.0f, largeSize, largeCount, true);
        if (mediumCount > 0) {
            float mediumCenterX = (mediumSize / 2.0f) + start;
            start += mediumSize;
            builder.addKeyline(mediumCenterX, mediumMask, mediumSize, false);
        }
        float mediumCenterX2 = CarouselStrategyHelper.getExtraSmallSize(context);
        float xSmallCenterX = (mediumCenterX2 / 2.0f) + start;
        builder.addAnchorKeyline(xSmallCenterX, extraSmallMask, xSmallSize2);
        return builder.build();
    }

    @Override // com.google.android.material.carousel.CarouselStrategy
    boolean isContained() {
        return false;
    }
}
