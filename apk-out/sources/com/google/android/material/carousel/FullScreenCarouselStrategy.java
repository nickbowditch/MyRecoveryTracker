package com.google.android.material.carousel;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

/* loaded from: classes.dex */
public class FullScreenCarouselStrategy extends CarouselStrategy {
    @Override // com.google.android.material.carousel.CarouselStrategy
    KeylineState onFirstChildMeasuredWithMargins(Carousel carousel, View child) {
        float childMargins;
        float availableSpace;
        RecyclerView.LayoutParams childLayoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
        if (carousel.isHorizontal()) {
            float availableSpace2 = carousel.getContainerWidth();
            childMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;
            availableSpace = availableSpace2;
        } else {
            float availableSpace3 = carousel.getContainerHeight();
            childMargins = childLayoutParams.topMargin + childLayoutParams.bottomMargin;
            availableSpace = availableSpace3;
        }
        float targetChildSize = Math.min(availableSpace + childMargins, availableSpace);
        Arrangement arrangement = new Arrangement(0, 0.0f, 0.0f, 0.0f, 0, 0.0f, 0, targetChildSize, 1, availableSpace);
        return CarouselStrategyHelper.createLeftAlignedKeylineState(child.getContext(), childMargins, availableSpace, arrangement);
    }
}
