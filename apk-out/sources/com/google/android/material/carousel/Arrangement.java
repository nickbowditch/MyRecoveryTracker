package com.google.android.material.carousel;

import androidx.core.math.MathUtils;

/* loaded from: classes.dex */
final class Arrangement {
    private static final float MEDIUM_ITEM_FLEX_PERCENTAGE = 0.1f;
    final float cost;
    final int largeCount;
    float largeSize;
    int mediumCount;
    float mediumSize;
    final int priority;
    int smallCount;
    float smallSize;

    Arrangement(int priority, float targetSmallSize, float minSmallSize, float maxSmallSize, int smallCount, float targetMediumSize, int mediumCount, float targetLargeSize, int largeCount, float availableSpace) {
        this.priority = priority;
        this.smallSize = MathUtils.clamp(targetSmallSize, minSmallSize, maxSmallSize);
        this.smallCount = smallCount;
        this.mediumSize = targetMediumSize;
        this.mediumCount = mediumCount;
        this.largeSize = targetLargeSize;
        this.largeCount = largeCount;
        fit(availableSpace, minSmallSize, maxSmallSize, targetLargeSize);
        this.cost = cost(targetLargeSize);
    }

    public String toString() {
        return "Arrangement [priority=" + this.priority + ", smallCount=" + this.smallCount + ", smallSize=" + this.smallSize + ", mediumCount=" + this.mediumCount + ", mediumSize=" + this.mediumSize + ", largeCount=" + this.largeCount + ", largeSize=" + this.largeSize + ", cost=" + this.cost + "]";
    }

    private float getSpace() {
        return (this.largeSize * this.largeCount) + (this.mediumSize * this.mediumCount) + (this.smallSize * this.smallCount);
    }

    private void fit(float availableSpace, float minSmallSize, float maxSmallSize, float targetLargeSize) {
        float delta = availableSpace - getSpace();
        if (this.smallCount > 0 && delta > 0.0f) {
            this.smallSize += Math.min(delta / this.smallCount, maxSmallSize - this.smallSize);
        } else if (this.smallCount > 0 && delta < 0.0f) {
            this.smallSize += Math.max(delta / this.smallCount, minSmallSize - this.smallSize);
        }
        this.smallSize = this.smallCount > 0 ? this.smallSize : 0.0f;
        this.largeSize = calculateLargeSize(availableSpace, this.smallCount, this.smallSize, this.mediumCount, this.largeCount);
        this.mediumSize = (this.largeSize + this.smallSize) / 2.0f;
        if (this.mediumCount > 0 && this.largeSize != targetLargeSize) {
            float targetAdjustment = (targetLargeSize - this.largeSize) * this.largeCount;
            float availableMediumFlex = this.mediumSize * 0.1f * this.mediumCount;
            float distribute = Math.min(Math.abs(targetAdjustment), availableMediumFlex);
            if (targetAdjustment > 0.0f) {
                this.mediumSize -= distribute / this.mediumCount;
                this.largeSize += distribute / this.largeCount;
            } else {
                this.mediumSize += distribute / this.mediumCount;
                this.largeSize -= distribute / this.largeCount;
            }
        }
    }

    private float calculateLargeSize(float availableSpace, int smallCount, float smallSize, int mediumCount, int largeCount) {
        float smallSize2 = smallCount > 0 ? smallSize : 0.0f;
        float smallSize3 = smallCount;
        return (availableSpace - ((smallSize3 + (mediumCount / 2.0f)) * smallSize2)) / (largeCount + (mediumCount / 2.0f));
    }

    private boolean isValid() {
        return (this.largeCount <= 0 || this.smallCount <= 0 || this.mediumCount <= 0) ? this.largeCount <= 0 || this.smallCount <= 0 || this.largeSize > this.smallSize : this.largeSize > this.mediumSize && this.mediumSize > this.smallSize;
    }

    private float cost(float targetLargeSize) {
        if (!isValid()) {
            return Float.MAX_VALUE;
        }
        return Math.abs(targetLargeSize - this.largeSize) * this.priority;
    }

    static Arrangement findLowestCostArrangement(float availableSpace, float targetSmallSize, float minSmallSize, float maxSmallSize, int[] smallCounts, float targetMediumSize, int[] mediumCounts, float targetLargeSize, int[] largeCounts) {
        Arrangement lowestCostArrangement = null;
        int priority = 1;
        for (int largeCount : largeCounts) {
            int priority2 = mediumCounts.length;
            int priority3 = 0;
            while (priority3 < priority2) {
                int mediumCount = mediumCounts[priority3];
                int length = smallCounts.length;
                int i = 0;
                while (i < length) {
                    int smallCount = smallCounts[i];
                    int i2 = priority2;
                    int i3 = priority3;
                    int priority4 = priority;
                    int i4 = length;
                    int i5 = i;
                    Arrangement arrangement = new Arrangement(priority4, targetSmallSize, minSmallSize, maxSmallSize, smallCount, targetMediumSize, mediumCount, targetLargeSize, largeCount, availableSpace);
                    if (lowestCostArrangement == null || arrangement.cost < lowestCostArrangement.cost) {
                        lowestCostArrangement = arrangement;
                        if (lowestCostArrangement.cost == 0.0f) {
                            return lowestCostArrangement;
                        }
                    }
                    int priority5 = priority4 + 1;
                    i = i5 + 1;
                    priority3 = i3;
                    priority = priority5;
                    priority2 = i2;
                    length = i4;
                }
                priority3++;
                priority = priority;
                priority2 = priority2;
            }
        }
        return lowestCostArrangement;
    }

    int getItemCount() {
        return this.smallCount + this.mediumCount + this.largeCount;
    }
}
