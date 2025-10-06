package androidx.constraintlayout.solver.widgets.analyzer;

import androidx.constraintlayout.solver.LinearSystem;
import androidx.constraintlayout.solver.widgets.ConstraintAnchor;
import androidx.constraintlayout.solver.widgets.ConstraintWidget;
import androidx.constraintlayout.solver.widgets.ConstraintWidgetContainer;
import androidx.constraintlayout.solver.widgets.Guideline;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.constraintlayout.solver.widgets.Optimizer;
import androidx.constraintlayout.solver.widgets.VirtualLayout;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class BasicMeasure {
    public static final int AT_MOST = Integer.MIN_VALUE;
    private static final boolean DEBUG = false;
    public static final int EXACTLY = 1073741824;
    public static final int FIXED = -3;
    public static final int MATCH_PARENT = -1;
    private static final int MODE_SHIFT = 30;
    public static final int UNSPECIFIED = 0;
    public static final int WRAP_CONTENT = -2;
    private ConstraintWidgetContainer constraintWidgetContainer;
    private final ArrayList<ConstraintWidget> mVariableDimensionsWidgets = new ArrayList<>();
    private Measure mMeasure = new Measure();

    public static class Measure {
        public ConstraintWidget.DimensionBehaviour horizontalBehavior;
        public int horizontalDimension;
        public int measuredBaseline;
        public boolean measuredHasBaseline;
        public int measuredHeight;
        public boolean measuredNeedsSolverPass;
        public int measuredWidth;
        public boolean useCurrentDimensions;
        public ConstraintWidget.DimensionBehaviour verticalBehavior;
        public int verticalDimension;
    }

    public enum MeasureType {
    }

    public interface Measurer {
        void didMeasures();

        void measure(ConstraintWidget constraintWidget, Measure measure);
    }

    public void updateHierarchy(ConstraintWidgetContainer layout) {
        this.mVariableDimensionsWidgets.clear();
        int childCount = layout.mChildren.size();
        for (int i = 0; i < childCount; i++) {
            ConstraintWidget widget = layout.mChildren.get(i);
            if (widget.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT || widget.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_PARENT || widget.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT || widget.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_PARENT) {
                this.mVariableDimensionsWidgets.add(widget);
            }
        }
        layout.invalidateGraph();
    }

    public BasicMeasure(ConstraintWidgetContainer constraintWidgetContainer) {
        this.constraintWidgetContainer = constraintWidgetContainer;
    }

    private void measureChildren(ConstraintWidgetContainer layout) {
        int childCount = layout.mChildren.size();
        Measurer measurer = layout.getMeasurer();
        for (int i = 0; i < childCount; i++) {
            ConstraintWidget child = layout.mChildren.get(i);
            if (!(child instanceof Guideline) && (!child.horizontalRun.dimension.resolved || !child.verticalRun.dimension.resolved)) {
                ConstraintWidget.DimensionBehaviour widthBehavior = child.getDimensionBehaviour(0);
                boolean skip = true;
                ConstraintWidget.DimensionBehaviour heightBehavior = child.getDimensionBehaviour(1);
                if (widthBehavior != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT || child.mMatchConstraintDefaultWidth == 1 || heightBehavior != ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT || child.mMatchConstraintDefaultHeight == 1) {
                    skip = false;
                }
                if (!skip) {
                    measure(measurer, child, false);
                    if (layout.mMetrics != null) {
                        layout.mMetrics.measuredWidgets++;
                    }
                }
            }
        }
        measurer.didMeasures();
    }

    private void solveLinearSystem(ConstraintWidgetContainer layout, String reason, int w, int h) {
        int minWidth = layout.getMinWidth();
        int minHeight = layout.getMinHeight();
        layout.setMinWidth(0);
        layout.setMinHeight(0);
        layout.setWidth(w);
        layout.setHeight(h);
        layout.setMinWidth(minWidth);
        layout.setMinHeight(minHeight);
        this.constraintWidgetContainer.layout();
    }

    public long solverMeasure(ConstraintWidgetContainer layout, int optimizationLevel, int paddingX, int paddingY, int widthMode, int widthSize, int heightMode, int heightSize, int lastMeasureWidth, int lastMeasureHeight) {
        long j;
        int widthSize2;
        boolean allSolved;
        int heightSize2;
        long layoutTime;
        int optimizations;
        int sizeDependentWidgetsCount;
        int maxIterations;
        int computations;
        int optimizations2;
        int childCount;
        boolean optimizeWrap;
        int widthSize3;
        boolean optimize;
        boolean needSolverPass;
        int minWidth;
        boolean needSolverPass2;
        int heightSize3;
        boolean allSolved2;
        boolean allSolved3;
        Measurer measurer = layout.getMeasurer();
        int childCount2 = layout.mChildren.size();
        int startingWidth = layout.getWidth();
        int startingHeight = layout.getHeight();
        boolean optimizeWrap2 = Optimizer.enabled(optimizationLevel, 128);
        boolean optimize2 = optimizeWrap2 || Optimizer.enabled(optimizationLevel, 64);
        if (optimize2) {
            int i = 0;
            while (true) {
                if (i >= childCount2) {
                    break;
                }
                ConstraintWidget child = layout.mChildren.get(i);
                boolean matchWidth = child.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
                boolean matchWidth2 = matchWidth;
                boolean matchHeight = child.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
                boolean ratio = matchWidth2 && matchHeight && child.getDimensionRatio() > 0.0f;
                if (child.isInHorizontalChain() && ratio) {
                    optimize2 = false;
                    break;
                }
                if (child.isInVerticalChain() && ratio) {
                    optimize2 = false;
                    break;
                }
                boolean matchHeight2 = child instanceof VirtualLayout;
                if (matchHeight2) {
                    optimize2 = false;
                    break;
                }
                if (child.isInHorizontalChain() || child.isInVerticalChain()) {
                    break;
                }
                i++;
            }
            optimize2 = false;
        }
        if (!optimize2 || LinearSystem.sMetrics == null) {
            j = 1;
        } else {
            j = 1;
            LinearSystem.sMetrics.measures++;
        }
        boolean optimize3 = ((widthMode == 1073741824 && heightMode == 1073741824) || optimizeWrap2) & optimize2;
        int computations2 = 0;
        if (!optimize3) {
            widthSize2 = widthSize;
            allSolved = false;
            heightSize2 = heightSize;
        } else {
            widthSize2 = Math.min(layout.getMaxWidth(), widthSize);
            int heightSize4 = Math.min(layout.getMaxHeight(), heightSize);
            if (widthMode == 1073741824 && layout.getWidth() != widthSize2) {
                layout.setWidth(widthSize2);
                layout.invalidateGraph();
            }
            if (heightMode == 1073741824 && layout.getHeight() != heightSize4) {
                layout.setHeight(heightSize4);
                layout.invalidateGraph();
            }
            if (widthMode == 1073741824 && heightMode == 1073741824) {
                boolean allSolved4 = layout.directMeasure(optimizeWrap2);
                computations2 = 2;
                heightSize3 = heightSize4;
                allSolved2 = allSolved4;
            } else {
                boolean allSolved5 = layout.directMeasureSetup(optimizeWrap2);
                heightSize3 = heightSize4;
                if (widthMode == 1073741824) {
                    allSolved5 &= layout.directMeasureWithOrientation(optimizeWrap2, 0);
                    computations2 = 0 + 1;
                }
                if (heightMode != 1073741824) {
                    allSolved2 = allSolved5;
                } else {
                    allSolved2 = allSolved5 & layout.directMeasureWithOrientation(optimizeWrap2, 1);
                    computations2++;
                }
            }
            if (!allSolved2) {
                allSolved3 = allSolved2;
            } else {
                allSolved3 = allSolved2;
                boolean allSolved6 = widthMode == 1073741824;
                layout.updateFromRuns(allSolved6, heightMode == 1073741824);
            }
            heightSize2 = heightSize3;
            allSolved = allSolved3;
        }
        if (allSolved && computations2 == 2) {
            return 0L;
        }
        if (childCount2 > 0) {
            measureChildren(layout);
        }
        int optimizations3 = layout.getOptimizationLevel();
        int sizeDependentWidgetsCount2 = this.mVariableDimensionsWidgets.size();
        if (childCount2 > 0) {
            solveLinearSystem(layout, "First pass", startingWidth, startingHeight);
        }
        if (sizeDependentWidgetsCount2 <= 0) {
            layoutTime = 0;
            optimizations = optimizations3;
        } else {
            boolean needSolverPass3 = false;
            boolean containerWrapWidth = layout.getHorizontalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
            boolean containerWrapHeight = layout.getVerticalDimensionBehaviour() == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT;
            int minWidth2 = Math.max(layout.getWidth(), this.constraintWidgetContainer.getMinWidth());
            int minHeight = Math.max(layout.getHeight(), this.constraintWidgetContainer.getMinHeight());
            int i2 = 0;
            int minWidth3 = minWidth2;
            layoutTime = 0;
            while (i2 < sizeDependentWidgetsCount2) {
                ConstraintWidget widget = this.mVariableDimensionsWidgets.get(i2);
                int i3 = i2;
                if (!(widget instanceof VirtualLayout)) {
                    needSolverPass2 = needSolverPass3;
                    childCount = childCount2;
                    optimizeWrap = optimizeWrap2;
                    widthSize3 = widthSize2;
                    optimize = optimize3;
                } else {
                    int preWidth = widget.getWidth();
                    childCount = childCount2;
                    int preHeight = widget.getHeight();
                    optimizeWrap = optimizeWrap2;
                    boolean needSolverPass4 = needSolverPass3 | measure(measurer, widget, true);
                    if (layout.mMetrics == null) {
                        widthSize3 = widthSize2;
                        optimize = optimize3;
                    } else {
                        widthSize3 = widthSize2;
                        optimize = optimize3;
                        layout.mMetrics.measuredMatchWidgets += j;
                    }
                    int measuredWidth = widget.getWidth();
                    int measuredHeight = widget.getHeight();
                    if (measuredWidth == preWidth) {
                        needSolverPass = needSolverPass4;
                    } else {
                        widget.setWidth(measuredWidth);
                        if (containerWrapWidth && widget.getRight() > minWidth3) {
                            int w = widget.getRight() + widget.getAnchor(ConstraintAnchor.Type.RIGHT).getMargin();
                            minWidth3 = Math.max(minWidth3, w);
                        }
                        needSolverPass = true;
                    }
                    if (measuredHeight == preHeight) {
                        minWidth = minWidth3;
                    } else {
                        widget.setHeight(measuredHeight);
                        if (!containerWrapHeight || widget.getBottom() <= minHeight) {
                            minWidth = minWidth3;
                        } else {
                            minWidth = minWidth3;
                            int h = widget.getBottom() + widget.getAnchor(ConstraintAnchor.Type.BOTTOM).getMargin();
                            minHeight = Math.max(minHeight, h);
                        }
                        needSolverPass = true;
                    }
                    VirtualLayout virtualLayout = (VirtualLayout) widget;
                    needSolverPass2 = needSolverPass | virtualLayout.needSolverPass();
                    minWidth3 = minWidth;
                }
                needSolverPass3 = needSolverPass2;
                i2 = i3 + 1;
                childCount2 = childCount;
                optimizeWrap2 = optimizeWrap;
                optimize3 = optimize;
                widthSize2 = widthSize3;
            }
            int maxIterations2 = 2;
            int j2 = 0;
            int minWidth4 = minWidth3;
            int minHeight2 = minHeight;
            boolean needSolverPass5 = needSolverPass3;
            while (j2 < maxIterations2) {
                int i4 = 0;
                while (i4 < sizeDependentWidgetsCount2) {
                    ConstraintWidget widget2 = this.mVariableDimensionsWidgets.get(i4);
                    if (((widget2 instanceof Helper) && !(widget2 instanceof VirtualLayout)) || (widget2 instanceof Guideline)) {
                        sizeDependentWidgetsCount = sizeDependentWidgetsCount2;
                    } else {
                        sizeDependentWidgetsCount = sizeDependentWidgetsCount2;
                        if (widget2.getVisibility() != 8 && ((!widget2.horizontalRun.dimension.resolved || !widget2.verticalRun.dimension.resolved) && !(widget2 instanceof VirtualLayout))) {
                            int preWidth2 = widget2.getWidth();
                            int preHeight2 = widget2.getHeight();
                            boolean needSolverPass6 = needSolverPass5;
                            int preBaselineDistance = widget2.getBaselineDistance();
                            maxIterations = maxIterations2;
                            boolean needSolverPass7 = needSolverPass6 | measure(measurer, widget2, true);
                            if (layout.mMetrics == null) {
                                computations = computations2;
                                optimizations2 = optimizations3;
                            } else {
                                computations = computations2;
                                optimizations2 = optimizations3;
                                layout.mMetrics.measuredMatchWidgets += j;
                            }
                            int measuredWidth2 = widget2.getWidth();
                            int measuredHeight2 = widget2.getHeight();
                            if (measuredWidth2 != preWidth2) {
                                widget2.setWidth(measuredWidth2);
                                if (containerWrapWidth && widget2.getRight() > minWidth4) {
                                    int w2 = widget2.getRight() + widget2.getAnchor(ConstraintAnchor.Type.RIGHT).getMargin();
                                    minWidth4 = Math.max(minWidth4, w2);
                                }
                                needSolverPass7 = true;
                            }
                            if (measuredHeight2 != preHeight2) {
                                widget2.setHeight(measuredHeight2);
                                if (containerWrapHeight && widget2.getBottom() > minHeight2) {
                                    int h2 = widget2.getBottom() + widget2.getAnchor(ConstraintAnchor.Type.BOTTOM).getMargin();
                                    minHeight2 = Math.max(minHeight2, h2);
                                }
                                needSolverPass7 = true;
                            }
                            if (!widget2.hasBaseline() || preBaselineDistance == widget2.getBaselineDistance()) {
                                needSolverPass5 = needSolverPass7;
                            } else {
                                needSolverPass5 = true;
                            }
                        }
                        i4++;
                        sizeDependentWidgetsCount2 = sizeDependentWidgetsCount;
                        maxIterations2 = maxIterations;
                        computations2 = computations;
                        optimizations3 = optimizations2;
                    }
                    maxIterations = maxIterations2;
                    computations = computations2;
                    optimizations2 = optimizations3;
                    i4++;
                    sizeDependentWidgetsCount2 = sizeDependentWidgetsCount;
                    maxIterations2 = maxIterations;
                    computations2 = computations;
                    optimizations3 = optimizations2;
                }
                int sizeDependentWidgetsCount3 = sizeDependentWidgetsCount2;
                boolean needSolverPass8 = needSolverPass5;
                int maxIterations3 = maxIterations2;
                int computations3 = computations2;
                int optimizations4 = optimizations3;
                if (needSolverPass8) {
                    solveLinearSystem(layout, "intermediate pass", startingWidth, startingHeight);
                    needSolverPass5 = false;
                } else {
                    needSolverPass5 = needSolverPass8;
                }
                j2++;
                sizeDependentWidgetsCount2 = sizeDependentWidgetsCount3;
                maxIterations2 = maxIterations3;
                computations2 = computations3;
                optimizations3 = optimizations4;
            }
            optimizations = optimizations3;
            if (needSolverPass5) {
                solveLinearSystem(layout, "2nd pass", startingWidth, startingHeight);
                boolean needSolverPass9 = false;
                if (layout.getWidth() < minWidth4) {
                    layout.setWidth(minWidth4);
                    needSolverPass9 = true;
                }
                if (layout.getHeight() < minHeight2) {
                    layout.setHeight(minHeight2);
                    needSolverPass9 = true;
                }
                if (needSolverPass9) {
                    solveLinearSystem(layout, "3rd pass", startingWidth, startingHeight);
                }
            }
        }
        layout.setOptimizationLevel(optimizations);
        return layoutTime;
    }

    private boolean measure(Measurer measurer, ConstraintWidget widget, boolean useCurrentDimensions) {
        this.mMeasure.horizontalBehavior = widget.getHorizontalDimensionBehaviour();
        this.mMeasure.verticalBehavior = widget.getVerticalDimensionBehaviour();
        this.mMeasure.horizontalDimension = widget.getWidth();
        this.mMeasure.verticalDimension = widget.getHeight();
        this.mMeasure.measuredNeedsSolverPass = false;
        this.mMeasure.useCurrentDimensions = useCurrentDimensions;
        boolean horizontalMatchConstraints = this.mMeasure.horizontalBehavior == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        boolean verticalMatchConstraints = this.mMeasure.verticalBehavior == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT;
        boolean horizontalUseRatio = horizontalMatchConstraints && widget.mDimensionRatio > 0.0f;
        boolean verticalUseRatio = verticalMatchConstraints && widget.mDimensionRatio > 0.0f;
        if (horizontalUseRatio && widget.mResolvedMatchConstraintDefault[0] == 4) {
            this.mMeasure.horizontalBehavior = ConstraintWidget.DimensionBehaviour.FIXED;
        }
        if (verticalUseRatio && widget.mResolvedMatchConstraintDefault[1] == 4) {
            this.mMeasure.verticalBehavior = ConstraintWidget.DimensionBehaviour.FIXED;
        }
        measurer.measure(widget, this.mMeasure);
        widget.setWidth(this.mMeasure.measuredWidth);
        widget.setHeight(this.mMeasure.measuredHeight);
        widget.setHasBaseline(this.mMeasure.measuredHasBaseline);
        widget.setBaselineDistance(this.mMeasure.measuredBaseline);
        this.mMeasure.useCurrentDimensions = false;
        return this.mMeasure.measuredNeedsSolverPass;
    }
}
