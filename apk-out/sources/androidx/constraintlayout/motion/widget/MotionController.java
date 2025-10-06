package androidx.constraintlayout.motion.widget;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import androidx.constraintlayout.motion.utils.CurveFit;
import androidx.constraintlayout.motion.utils.Easing;
import androidx.constraintlayout.motion.utils.VelocityMatrix;
import androidx.constraintlayout.solver.widgets.ConstraintWidget;
import androidx.constraintlayout.widget.ConstraintAttribute;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/* loaded from: classes.dex */
public class MotionController {
    private static final boolean DEBUG = false;
    public static final int DRAW_PATH_AS_CONFIGURED = 4;
    public static final int DRAW_PATH_BASIC = 1;
    public static final int DRAW_PATH_CARTESIAN = 3;
    public static final int DRAW_PATH_NONE = 0;
    public static final int DRAW_PATH_RECTANGLE = 5;
    public static final int DRAW_PATH_RELATIVE = 2;
    public static final int DRAW_PATH_SCREEN = 6;
    private static final boolean FAVOR_FIXED_SIZE_VIEWS = false;
    public static final int HORIZONTAL_PATH_X = 2;
    public static final int HORIZONTAL_PATH_Y = 3;
    public static final int PATH_PERCENT = 0;
    public static final int PATH_PERPENDICULAR = 1;
    private static final String TAG = "MotionController";
    public static final int VERTICAL_PATH_X = 4;
    public static final int VERTICAL_PATH_Y = 5;
    String[] attributeTable;
    private CurveFit mArcSpline;
    private int[] mAttributeInterpCount;
    private String[] mAttributeNames;
    private HashMap<String, SplineSet> mAttributesMap;
    String mConstraintTag;
    private HashMap<String, KeyCycleOscillator> mCycleMap;
    int mId;
    private double[] mInterpolateData;
    private int[] mInterpolateVariables;
    private double[] mInterpolateVelocity;
    private KeyTrigger[] mKeyTriggers;
    private CurveFit[] mSpline;
    private HashMap<String, TimeCycleSplineSet> mTimeCycleAttributesMap;
    View mView;
    private int mCurveFitType = -1;
    private MotionPaths mStartMotionPath = new MotionPaths();
    private MotionPaths mEndMotionPath = new MotionPaths();
    private MotionConstrainedPoint mStartPoint = new MotionConstrainedPoint();
    private MotionConstrainedPoint mEndPoint = new MotionConstrainedPoint();
    float mMotionStagger = Float.NaN;
    float mStaggerOffset = 0.0f;
    float mStaggerScale = 1.0f;
    private int MAX_DIMENSION = 4;
    private float[] mValuesBuff = new float[this.MAX_DIMENSION];
    private ArrayList<MotionPaths> mMotionPaths = new ArrayList<>();
    private float[] mVelocity = new float[1];
    private ArrayList<Key> mKeyList = new ArrayList<>();
    private int mPathMotionArc = Key.UNSET;

    MotionPaths getKeyFrame(int i) {
        return this.mMotionPaths.get(i);
    }

    MotionController(View view) {
        setView(view);
    }

    float getStartX() {
        return this.mStartMotionPath.x;
    }

    float getStartY() {
        return this.mStartMotionPath.y;
    }

    float getFinalX() {
        return this.mEndMotionPath.x;
    }

    float getFinalY() {
        return this.mEndMotionPath.y;
    }

    void buildPath(float[] points, int pointCount) {
        MotionController motionController = this;
        int i = pointCount;
        float f = 1.0f;
        float mils = 1.0f / (i - 1);
        SplineSet trans_x = motionController.mAttributesMap == null ? null : motionController.mAttributesMap.get("translationX");
        SplineSet trans_y = motionController.mAttributesMap == null ? null : motionController.mAttributesMap.get("translationY");
        KeyCycleOscillator osc_x = motionController.mCycleMap == null ? null : motionController.mCycleMap.get("translationX");
        KeyCycleOscillator osc_y = motionController.mCycleMap != null ? motionController.mCycleMap.get("translationY") : null;
        int i2 = 0;
        while (i2 < i) {
            float position = i2 * mils;
            if (motionController.mStaggerScale != f) {
                if (position < motionController.mStaggerOffset) {
                    position = 0.0f;
                }
                if (position > motionController.mStaggerOffset && position < 1.0d) {
                    position = (position - motionController.mStaggerOffset) * motionController.mStaggerScale;
                }
            }
            double p = position;
            Easing easing = motionController.mStartMotionPath.mKeyFrameEasing;
            float start = 0.0f;
            float end = Float.NaN;
            Iterator<MotionPaths> it = motionController.mMotionPaths.iterator();
            while (it.hasNext()) {
                MotionPaths frame = it.next();
                float mils2 = mils;
                if (frame.mKeyFrameEasing != null) {
                    if (frame.time < position) {
                        Easing easing2 = frame.mKeyFrameEasing;
                        start = frame.time;
                        easing = easing2;
                    } else if (Float.isNaN(end)) {
                        end = frame.time;
                    }
                }
                mils = mils2;
            }
            float mils3 = mils;
            if (easing != null) {
                if (Float.isNaN(end)) {
                    end = 1.0f;
                }
                float offset = (position - start) / (end - start);
                p = ((end - start) * ((float) easing.get(offset))) + start;
            }
            motionController.mSpline[0].getPos(p, motionController.mInterpolateData);
            if (motionController.mArcSpline != null && motionController.mInterpolateData.length > 0) {
                motionController.mArcSpline.getPos(p, motionController.mInterpolateData);
            }
            motionController.mStartMotionPath.getCenter(motionController.mInterpolateVariables, motionController.mInterpolateData, points, i2 * 2);
            if (osc_x != null) {
                int i3 = i2 * 2;
                points[i3] = points[i3] + osc_x.get(position);
            } else if (trans_x != null) {
                int i4 = i2 * 2;
                points[i4] = points[i4] + trans_x.get(position);
            }
            if (osc_y != null) {
                int i5 = (i2 * 2) + 1;
                points[i5] = points[i5] + osc_y.get(position);
            } else if (trans_y != null) {
                int i6 = (i2 * 2) + 1;
                points[i6] = points[i6] + trans_y.get(position);
            }
            i2++;
            motionController = this;
            i = pointCount;
            mils = mils3;
            f = 1.0f;
        }
    }

    void buildBounds(float[] bounds, int pointCount) {
        float mils;
        MotionController motionController = this;
        int i = pointCount;
        float f = 1.0f;
        float mils2 = 1.0f / (i - 1);
        SplineSet trans_x = motionController.mAttributesMap == null ? null : motionController.mAttributesMap.get("translationX");
        if (motionController.mAttributesMap != null) {
            motionController.mAttributesMap.get("translationY");
        }
        if (motionController.mCycleMap != null) {
            motionController.mCycleMap.get("translationX");
        }
        if (motionController.mCycleMap != null) {
            motionController.mCycleMap.get("translationY");
        }
        int i2 = 0;
        while (i2 < i) {
            float position = i2 * mils2;
            if (motionController.mStaggerScale != f) {
                if (position < motionController.mStaggerOffset) {
                    position = 0.0f;
                }
                if (position > motionController.mStaggerOffset && position < 1.0d) {
                    position = (position - motionController.mStaggerOffset) * motionController.mStaggerScale;
                }
            }
            double p = position;
            Easing easing = motionController.mStartMotionPath.mKeyFrameEasing;
            float start = 0.0f;
            float end = Float.NaN;
            Iterator<MotionPaths> it = motionController.mMotionPaths.iterator();
            while (it.hasNext()) {
                MotionPaths frame = it.next();
                if (frame.mKeyFrameEasing != null) {
                    if (frame.time < position) {
                        Easing easing2 = frame.mKeyFrameEasing;
                        start = frame.time;
                        easing = easing2;
                    } else if (Float.isNaN(end)) {
                        end = frame.time;
                    }
                }
            }
            if (easing == null) {
                mils = mils2;
            } else {
                if (Float.isNaN(end)) {
                    end = 1.0f;
                }
                float offset = (position - start) / (end - start);
                mils = mils2;
                p = ((end - start) * ((float) easing.get(offset))) + start;
            }
            motionController.mSpline[0].getPos(p, motionController.mInterpolateData);
            if (motionController.mArcSpline != null && motionController.mInterpolateData.length > 0) {
                motionController.mArcSpline.getPos(p, motionController.mInterpolateData);
            }
            motionController.mStartMotionPath.getBounds(motionController.mInterpolateVariables, motionController.mInterpolateData, bounds, i2 * 2);
            i2++;
            motionController = this;
            i = pointCount;
            mils2 = mils;
            trans_x = trans_x;
            f = 1.0f;
        }
    }

    private float getPreCycleDistance() {
        float mils;
        char c;
        float[] points;
        char c2;
        int pointCount = 100;
        float[] points2 = new float[2];
        float sum = 0.0f;
        float mils2 = 1.0f / (100 - 1);
        double x = 0.0d;
        double y = 0.0d;
        int i = 0;
        while (i < pointCount) {
            float position = i * mils2;
            double p = position;
            Easing easing = this.mStartMotionPath.mKeyFrameEasing;
            float start = 0.0f;
            float end = Float.NaN;
            int pointCount2 = pointCount;
            Iterator<MotionPaths> it = this.mMotionPaths.iterator();
            while (it.hasNext()) {
                Iterator<MotionPaths> it2 = it;
                MotionPaths frame = it.next();
                int i2 = i;
                if (frame.mKeyFrameEasing != null) {
                    if (frame.time < position) {
                        Easing easing2 = frame.mKeyFrameEasing;
                        start = frame.time;
                        easing = easing2;
                    } else if (Float.isNaN(end)) {
                        end = frame.time;
                    }
                }
                i = i2;
                it = it2;
            }
            int i3 = i;
            if (easing == null) {
                mils = mils2;
            } else {
                if (Float.isNaN(end)) {
                    end = 1.0f;
                }
                float offset = (position - start) / (end - start);
                mils = mils2;
                p = ((end - start) * ((float) easing.get(offset))) + start;
            }
            this.mSpline[0].getPos(p, this.mInterpolateData);
            double x2 = x;
            this.mStartMotionPath.getCenter(this.mInterpolateVariables, this.mInterpolateData, points2, 0);
            if (i3 > 0) {
                c = 1;
                points = points2;
                c2 = 0;
                sum = (float) (sum + Math.hypot(y - points2[1], x2 - points[0]));
            } else {
                c = 1;
                points = points2;
                c2 = 0;
            }
            x = points[c2];
            y = points[c];
            i = i3 + 1;
            pointCount = pointCount2;
            mils2 = mils;
            points2 = points;
        }
        return sum;
    }

    KeyPositionBase getPositionKeyframe(int layoutWidth, int layoutHeight, float x, float y) {
        int layoutWidth2;
        int layoutHeight2;
        float x2;
        float y2;
        RectF start = new RectF();
        start.left = this.mStartMotionPath.x;
        start.top = this.mStartMotionPath.y;
        start.right = start.left + this.mStartMotionPath.width;
        start.bottom = start.top + this.mStartMotionPath.height;
        RectF end = new RectF();
        end.left = this.mEndMotionPath.x;
        end.top = this.mEndMotionPath.y;
        end.right = end.left + this.mEndMotionPath.width;
        end.bottom = end.top + this.mEndMotionPath.height;
        Iterator<Key> it = this.mKeyList.iterator();
        while (it.hasNext()) {
            Key key = it.next();
            if (!(key instanceof KeyPositionBase)) {
                layoutWidth2 = layoutWidth;
                layoutHeight2 = layoutHeight;
                x2 = x;
                y2 = y;
            } else {
                layoutWidth2 = layoutWidth;
                layoutHeight2 = layoutHeight;
                x2 = x;
                y2 = y;
                if (((KeyPositionBase) key).intersects(layoutWidth2, layoutHeight2, start, end, x2, y2)) {
                    return (KeyPositionBase) key;
                }
            }
            layoutWidth = layoutWidth2;
            layoutHeight = layoutHeight2;
            x = x2;
            y = y2;
        }
        return null;
    }

    int buildKeyFrames(float[] keyFrames, int[] mode) {
        if (keyFrames == null) {
            return 0;
        }
        int count = 0;
        double[] time = this.mSpline[0].getTimePoints();
        if (mode != null) {
            Iterator<MotionPaths> it = this.mMotionPaths.iterator();
            while (it.hasNext()) {
                MotionPaths keyFrame = it.next();
                mode[count] = keyFrame.mMode;
                count++;
            }
            count = 0;
        }
        for (double d : time) {
            this.mSpline[0].getPos(d, this.mInterpolateData);
            this.mStartMotionPath.getCenter(this.mInterpolateVariables, this.mInterpolateData, keyFrames, count);
            count += 2;
        }
        return count / 2;
    }

    int buildKeyBounds(float[] keyBounds, int[] mode) {
        if (keyBounds == null) {
            return 0;
        }
        int count = 0;
        double[] time = this.mSpline[0].getTimePoints();
        if (mode != null) {
            Iterator<MotionPaths> it = this.mMotionPaths.iterator();
            while (it.hasNext()) {
                MotionPaths keyFrame = it.next();
                mode[count] = keyFrame.mMode;
                count++;
            }
            count = 0;
        }
        for (double d : time) {
            this.mSpline[0].getPos(d, this.mInterpolateData);
            this.mStartMotionPath.getBounds(this.mInterpolateVariables, this.mInterpolateData, keyBounds, count);
            count += 2;
        }
        return count / 2;
    }

    int getAttributeValues(String attributeType, float[] points, int pointCount) {
        float f = 1.0f / (pointCount - 1);
        SplineSet spline = this.mAttributesMap.get(attributeType);
        if (spline == null) {
            return -1;
        }
        for (int j = 0; j < points.length; j++) {
            points[j] = spline.get(j / (points.length - 1));
        }
        int j2 = points.length;
        return j2;
    }

    void buildRect(float p, float[] path, int offset) {
        this.mSpline[0].getPos(getAdjustedPosition(p, null), this.mInterpolateData);
        this.mStartMotionPath.getRect(this.mInterpolateVariables, this.mInterpolateData, path, offset);
    }

    void buildRectangles(float[] path, int pointCount) {
        float mils = 1.0f / (pointCount - 1);
        for (int i = 0; i < pointCount; i++) {
            float position = i * mils;
            this.mSpline[0].getPos(getAdjustedPosition(position, null), this.mInterpolateData);
            this.mStartMotionPath.getRect(this.mInterpolateVariables, this.mInterpolateData, path, i * 8);
        }
    }

    float getKeyFrameParameter(int type, float x, float y) {
        float dx = this.mEndMotionPath.x - this.mStartMotionPath.x;
        float dy = this.mEndMotionPath.y - this.mStartMotionPath.y;
        float startCenterX = this.mStartMotionPath.x + (this.mStartMotionPath.width / 2.0f);
        float startCenterY = this.mStartMotionPath.y + (this.mStartMotionPath.height / 2.0f);
        float hypot = (float) Math.hypot(dx, dy);
        if (hypot < 1.0E-7d) {
            return Float.NaN;
        }
        float vx = x - startCenterX;
        float vy = y - startCenterY;
        float distFromStart = (float) Math.hypot(vx, vy);
        if (distFromStart == 0.0f) {
            return 0.0f;
        }
        float pathDistance = (vx * dx) + (vy * dy);
        switch (type) {
        }
        return 0.0f;
    }

    private void insertKey(MotionPaths point) {
        int pos = Collections.binarySearch(this.mMotionPaths, point);
        if (pos == 0) {
            Log.e(TAG, " KeyPath positon \"" + point.position + "\" outside of range");
        }
        this.mMotionPaths.add((-pos) - 1, point);
    }

    void addKeys(ArrayList<Key> list) {
        this.mKeyList.addAll(list);
    }

    void addKey(Key key) {
        this.mKeyList.add(key);
    }

    public void setPathMotionArc(int arc) {
        this.mPathMotionArc = arc;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r17v0 */
    /* JADX WARN: Type inference failed for: r17v1 */
    /* JADX WARN: Type inference failed for: r17v2 */
    public void setup(int parentWidth, int parentHeight, float transitionDuration, long currentTime) {
        ?? r17;
        HashMap<String, Integer> interpolation;
        int j;
        ArrayList<KeyTrigger> triggerList;
        int j2;
        HashSet<String> timeCycleAttributes;
        Iterator<String> it;
        TimeCycleSplineSet splineSets;
        Iterator<String> it2;
        HashSet<String> springAttributes;
        char c;
        SplineSet splineSets2;
        HashSet<String> springAttributes2;
        HashSet<String> springAttributes3 = new HashSet<>();
        HashSet<String> timeCycleAttributes2 = new HashSet<>();
        HashSet<String> splineAttributes = new HashSet<>();
        HashSet<String> cycleAttributes = new HashSet<>();
        HashMap<String, Integer> interpolation2 = new HashMap<>();
        ArrayList<KeyTrigger> triggerList2 = null;
        if (this.mPathMotionArc != Key.UNSET) {
            this.mStartMotionPath.mPathMotionArc = this.mPathMotionArc;
        }
        this.mStartPoint.different(this.mEndPoint, splineAttributes);
        if (this.mKeyList != null) {
            Iterator<Key> it3 = this.mKeyList.iterator();
            while (it3.hasNext()) {
                Key key = it3.next();
                if (key instanceof KeyPosition) {
                    KeyPosition keyPath = (KeyPosition) key;
                    insertKey(new MotionPaths(parentWidth, parentHeight, keyPath, this.mStartMotionPath, this.mEndMotionPath));
                    if (keyPath.mCurveFit != Key.UNSET) {
                        this.mCurveFitType = keyPath.mCurveFit;
                    }
                } else if (key instanceof KeyCycle) {
                    key.getAttributeNames(cycleAttributes);
                } else if (key instanceof KeyTimeCycle) {
                    key.getAttributeNames(timeCycleAttributes2);
                } else if (key instanceof KeyTrigger) {
                    if (triggerList2 == null) {
                        triggerList2 = new ArrayList<>();
                    }
                    triggerList2.add((KeyTrigger) key);
                } else {
                    key.setInterpolation(interpolation2);
                    key.getAttributeNames(splineAttributes);
                }
            }
        }
        if (triggerList2 != null) {
            this.mKeyTriggers = (KeyTrigger[]) triggerList2.toArray(new KeyTrigger[0]);
        }
        char c2 = 1;
        if (splineAttributes.isEmpty()) {
            r17 = 1;
        } else {
            this.mAttributesMap = new HashMap<>();
            Iterator<String> it4 = splineAttributes.iterator();
            while (it4.hasNext()) {
                String attribute = it4.next();
                if (attribute.startsWith("CUSTOM,")) {
                    SparseArray<ConstraintAttribute> attrList = new SparseArray<>();
                    String customAttributeName = attribute.split(",")[c2];
                    Iterator<Key> it5 = this.mKeyList.iterator();
                    while (it5.hasNext()) {
                        char c3 = c2;
                        Key key2 = it5.next();
                        if (key2.mCustomConstraints == null) {
                            c2 = c3;
                        } else {
                            ConstraintAttribute customAttribute = key2.mCustomConstraints.get(customAttributeName);
                            if (customAttribute == null) {
                                springAttributes2 = springAttributes3;
                            } else {
                                springAttributes2 = springAttributes3;
                                attrList.append(key2.mFramePosition, customAttribute);
                            }
                            c2 = c3;
                            springAttributes3 = springAttributes2;
                        }
                    }
                    springAttributes = springAttributes3;
                    c = c2;
                    splineSets2 = SplineSet.makeCustomSpline(attribute, attrList);
                } else {
                    springAttributes = springAttributes3;
                    c = c2;
                    splineSets2 = SplineSet.makeSpline(attribute);
                }
                if (splineSets2 == null) {
                    c2 = c;
                    springAttributes3 = springAttributes;
                } else {
                    splineSets2.setType(attribute);
                    this.mAttributesMap.put(attribute, splineSets2);
                    c2 = c;
                    springAttributes3 = springAttributes;
                }
            }
            r17 = c2;
            if (this.mKeyList != null) {
                Iterator<Key> it6 = this.mKeyList.iterator();
                while (it6.hasNext()) {
                    Key key3 = it6.next();
                    if (key3 instanceof KeyAttributes) {
                        key3.addValues(this.mAttributesMap);
                    }
                }
            }
            this.mStartPoint.addValues(this.mAttributesMap, 0);
            this.mEndPoint.addValues(this.mAttributesMap, 100);
            for (String spline : this.mAttributesMap.keySet()) {
                int curve = 0;
                if (interpolation2.containsKey(spline)) {
                    curve = interpolation2.get(spline).intValue();
                }
                this.mAttributesMap.get(spline).setup(curve);
            }
        }
        if (!timeCycleAttributes2.isEmpty()) {
            if (this.mTimeCycleAttributesMap == null) {
                this.mTimeCycleAttributesMap = new HashMap<>();
            }
            Iterator<String> it7 = timeCycleAttributes2.iterator();
            while (it7.hasNext()) {
                String attribute2 = it7.next();
                if (!this.mTimeCycleAttributesMap.containsKey(attribute2)) {
                    if (attribute2.startsWith("CUSTOM,")) {
                        SparseArray<ConstraintAttribute> attrList2 = new SparseArray<>();
                        String customAttributeName2 = attribute2.split(",")[r17];
                        Iterator<Key> it8 = this.mKeyList.iterator();
                        while (it8.hasNext()) {
                            Key key4 = it8.next();
                            if (key4.mCustomConstraints != null) {
                                ConstraintAttribute customAttribute2 = key4.mCustomConstraints.get(customAttributeName2);
                                if (customAttribute2 == null) {
                                    it2 = it7;
                                } else {
                                    it2 = it7;
                                    attrList2.append(key4.mFramePosition, customAttribute2);
                                }
                                it7 = it2;
                            }
                        }
                        it = it7;
                        splineSets = TimeCycleSplineSet.makeCustomSpline(attribute2, attrList2);
                    } else {
                        it = it7;
                        splineSets = TimeCycleSplineSet.makeSpline(attribute2, currentTime);
                    }
                    if (splineSets == null) {
                        it7 = it;
                    } else {
                        splineSets.setType(attribute2);
                        this.mTimeCycleAttributesMap.put(attribute2, splineSets);
                        it7 = it;
                    }
                }
            }
            if (this.mKeyList != null) {
                Iterator<Key> it9 = this.mKeyList.iterator();
                while (it9.hasNext()) {
                    Key key5 = it9.next();
                    if (key5 instanceof KeyTimeCycle) {
                        ((KeyTimeCycle) key5).addTimeValues(this.mTimeCycleAttributesMap);
                    }
                }
            }
            for (String spline2 : this.mTimeCycleAttributesMap.keySet()) {
                int curve2 = 0;
                if (interpolation2.containsKey(spline2)) {
                    curve2 = interpolation2.get(spline2).intValue();
                }
                this.mTimeCycleAttributesMap.get(spline2).setup(curve2);
            }
        }
        MotionPaths[] points = new MotionPaths[this.mMotionPaths.size() + 2];
        int count = 1;
        points[0] = this.mStartMotionPath;
        points[points.length - 1] = this.mEndMotionPath;
        if (this.mMotionPaths.size() > 0 && this.mCurveFitType == -1) {
            this.mCurveFitType = 0;
        }
        Iterator<MotionPaths> it10 = this.mMotionPaths.iterator();
        while (it10.hasNext()) {
            MotionPaths point = it10.next();
            points[count] = point;
            count++;
        }
        HashSet<String> attributeNameSet = new HashSet<>();
        for (String s : this.mEndMotionPath.attributes.keySet()) {
            if (this.mStartMotionPath.attributes.containsKey(s) && !splineAttributes.contains("CUSTOM," + s)) {
                attributeNameSet.add(s);
            }
        }
        this.mAttributeNames = (String[]) attributeNameSet.toArray(new String[0]);
        this.mAttributeInterpCount = new int[this.mAttributeNames.length];
        int i = 0;
        while (i < this.mAttributeNames.length) {
            String attributeName = this.mAttributeNames[i];
            this.mAttributeInterpCount[i] = 0;
            int j3 = 0;
            while (true) {
                if (j3 >= points.length) {
                    timeCycleAttributes = timeCycleAttributes2;
                    break;
                } else {
                    if (points[j3].attributes.containsKey(attributeName)) {
                        int[] iArr = this.mAttributeInterpCount;
                        timeCycleAttributes = timeCycleAttributes2;
                        iArr[i] = iArr[i] + points[j3].attributes.get(attributeName).noOfInterpValues();
                        break;
                    }
                    j3++;
                }
            }
            i++;
            timeCycleAttributes2 = timeCycleAttributes;
        }
        boolean arcMode = points[0].mPathMotionArc != Key.UNSET ? r17 : false;
        boolean[] mask = new boolean[this.mAttributeNames.length + 18];
        int i2 = 1;
        while (i2 < points.length) {
            points[i2].different(points[i2 - 1], mask, this.mAttributeNames, arcMode);
            i2++;
            splineAttributes = splineAttributes;
        }
        int count2 = 0;
        for (int i3 = 1; i3 < mask.length; i3++) {
            if (mask[i3]) {
                count2++;
            }
        }
        this.mInterpolateVariables = new int[count2];
        this.mInterpolateData = new double[this.mInterpolateVariables.length];
        this.mInterpolateVelocity = new double[this.mInterpolateVariables.length];
        int count3 = 0;
        for (int i4 = 1; i4 < mask.length; i4++) {
            if (mask[i4]) {
                this.mInterpolateVariables[count3] = i4;
                count3++;
            }
        }
        int i5 = points.length;
        int[] iArr2 = new int[2];
        iArr2[r17] = this.mInterpolateVariables.length;
        iArr2[0] = i5;
        double[][] splineData = (double[][]) Array.newInstance((Class<?>) Double.TYPE, iArr2);
        double[] timePoint = new double[points.length];
        int i6 = 0;
        while (i6 < points.length) {
            points[i6].fillStandard(splineData[i6], this.mInterpolateVariables);
            timePoint[i6] = points[i6].time;
            i6++;
            arcMode = arcMode;
            count3 = count3;
        }
        int j4 = 0;
        while (j4 < this.mInterpolateVariables.length) {
            int interpolateVariable = this.mInterpolateVariables[j4];
            if (interpolateVariable < MotionPaths.names.length) {
                j2 = j4;
                String s2 = MotionPaths.names[this.mInterpolateVariables[j2]] + " [";
                int i7 = 0;
                while (i7 < points.length) {
                    s2 = s2 + splineData[i7][j2];
                    i7++;
                    interpolateVariable = interpolateVariable;
                }
            } else {
                j2 = j4;
            }
            j4 = j2 + 1;
        }
        this.mSpline = new CurveFit[this.mAttributeNames.length + 1];
        int i8 = 0;
        while (i8 < this.mAttributeNames.length) {
            int pointCount = 0;
            double[][] splinePoints = (double[][]) null;
            double[] timePoints = null;
            int i9 = i8;
            String name = this.mAttributeNames[i9];
            HashSet<String> cycleAttributes2 = cycleAttributes;
            int j5 = 0;
            while (true) {
                interpolation = interpolation2;
                if (j5 < points.length) {
                    if (!points[j5].hasCustomData(name)) {
                        j = j5;
                        triggerList = triggerList2;
                    } else {
                        if (splinePoints != null) {
                            j = j5;
                        } else {
                            timePoints = new double[points.length];
                            int length = points.length;
                            j = j5;
                            int[] iArr3 = new int[2];
                            iArr3[r17] = points[j].getCustomDataCount(name);
                            iArr3[0] = length;
                            splinePoints = (double[][]) Array.newInstance((Class<?>) Double.TYPE, iArr3);
                        }
                        timePoints[pointCount] = points[j].time;
                        triggerList = triggerList2;
                        points[j].getCustomData(name, splinePoints[pointCount], 0);
                        pointCount++;
                    }
                    j5 = j + 1;
                    interpolation2 = interpolation;
                    triggerList2 = triggerList;
                }
            }
            this.mSpline[i9 + 1] = CurveFit.get(this.mCurveFitType, Arrays.copyOf(timePoints, pointCount), (double[][]) Arrays.copyOf(splinePoints, pointCount));
            i8 = i9 + 1;
            cycleAttributes = cycleAttributes2;
            interpolation2 = interpolation;
            triggerList2 = triggerList2;
        }
        HashSet<String> cycleAttributes3 = cycleAttributes;
        this.mSpline[0] = CurveFit.get(this.mCurveFitType, timePoint, splineData);
        if (points[0].mPathMotionArc != Key.UNSET) {
            int size = points.length;
            int[] mode = new int[size];
            double[] time = new double[size];
            int[] iArr4 = new int[2];
            iArr4[r17] = 2;
            iArr4[0] = size;
            double[][] values = (double[][]) Array.newInstance((Class<?>) Double.TYPE, iArr4);
            int i10 = 0;
            while (i10 < size) {
                mode[i10] = points[i10].mPathMotionArc;
                time[i10] = points[i10].time;
                values[i10][0] = points[i10].x;
                values[i10][r17] = r19[i10].y;
                i10++;
                points = points;
                size = size;
            }
            this.mArcSpline = CurveFit.getArc(mode, time, values);
        }
        float distance = Float.NaN;
        this.mCycleMap = new HashMap<>();
        if (this.mKeyList != null) {
            Iterator<String> it11 = cycleAttributes3.iterator();
            while (it11.hasNext()) {
                String attribute3 = it11.next();
                KeyCycleOscillator cycle = KeyCycleOscillator.makeSpline(attribute3);
                if (cycle != null) {
                    if (cycle.variesByPath() && Float.isNaN(distance)) {
                        distance = getPreCycleDistance();
                    }
                    cycle.setType(attribute3);
                    this.mCycleMap.put(attribute3, cycle);
                }
            }
            Iterator<Key> it12 = this.mKeyList.iterator();
            while (it12.hasNext()) {
                Key key6 = it12.next();
                if (key6 instanceof KeyCycle) {
                    ((KeyCycle) key6).addCycleValues(this.mCycleMap);
                }
            }
            Iterator<KeyCycleOscillator> it13 = this.mCycleMap.values().iterator();
            while (it13.hasNext()) {
                it13.next().setup(distance);
            }
        }
    }

    public String toString() {
        return " start: x: " + this.mStartMotionPath.x + " y: " + this.mStartMotionPath.y + " end: x: " + this.mEndMotionPath.x + " y: " + this.mEndMotionPath.y;
    }

    private void readView(MotionPaths motionPaths) {
        motionPaths.setBounds((int) this.mView.getX(), (int) this.mView.getY(), this.mView.getWidth(), this.mView.getHeight());
    }

    public void setView(View view) {
        this.mView = view;
        this.mId = view.getId();
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof ConstraintLayout.LayoutParams) {
            this.mConstraintTag = ((ConstraintLayout.LayoutParams) lp).getConstraintTag();
        }
    }

    void setStartCurrentState(View v) {
        this.mStartMotionPath.time = 0.0f;
        this.mStartMotionPath.position = 0.0f;
        this.mStartMotionPath.setBounds(v.getX(), v.getY(), v.getWidth(), v.getHeight());
        this.mStartPoint.setState(v);
    }

    void setStartState(ConstraintWidget cw, ConstraintSet constraintSet) {
        this.mStartMotionPath.time = 0.0f;
        this.mStartMotionPath.position = 0.0f;
        readView(this.mStartMotionPath);
        this.mStartMotionPath.setBounds(cw.getX(), cw.getY(), cw.getWidth(), cw.getHeight());
        ConstraintSet.Constraint constraint = constraintSet.getParameters(this.mId);
        this.mStartMotionPath.applyParameters(constraint);
        this.mMotionStagger = constraint.motion.mMotionStagger;
        this.mStartPoint.setState(cw, constraintSet, this.mId);
    }

    void setEndState(ConstraintWidget cw, ConstraintSet constraintSet) {
        this.mEndMotionPath.time = 1.0f;
        this.mEndMotionPath.position = 1.0f;
        readView(this.mEndMotionPath);
        this.mEndMotionPath.setBounds(cw.getX(), cw.getY(), cw.getWidth(), cw.getHeight());
        this.mEndMotionPath.applyParameters(constraintSet.getParameters(this.mId));
        this.mEndPoint.setState(cw, constraintSet, this.mId);
    }

    private float getAdjustedPosition(float position, float[] velocity) {
        if (velocity != null) {
            velocity[0] = 1.0f;
        } else if (this.mStaggerScale != 1.0d) {
            if (position < this.mStaggerOffset) {
                position = 0.0f;
            }
            if (position > this.mStaggerOffset && position < 1.0d) {
                position = (position - this.mStaggerOffset) * this.mStaggerScale;
            }
        }
        float adjusted = position;
        Easing easing = this.mStartMotionPath.mKeyFrameEasing;
        float start = 0.0f;
        float end = Float.NaN;
        Iterator<MotionPaths> it = this.mMotionPaths.iterator();
        while (it.hasNext()) {
            MotionPaths frame = it.next();
            if (frame.mKeyFrameEasing != null) {
                if (frame.time < position) {
                    easing = frame.mKeyFrameEasing;
                    start = frame.time;
                } else if (Float.isNaN(end)) {
                    end = frame.time;
                }
            }
        }
        if (easing != null) {
            if (Float.isNaN(end)) {
                end = 1.0f;
            }
            float offset = (position - start) / (end - start);
            float new_offset = (float) easing.get(offset);
            adjusted = ((end - start) * new_offset) + start;
            if (velocity != null) {
                velocity[0] = (float) easing.getDiff(offset);
            }
        }
        return adjusted;
    }

    /* JADX WARN: Incorrect condition in loop: B:12:0x003f */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    boolean interpolate(android.view.View r21, float r22, long r23, androidx.constraintlayout.motion.widget.KeyCache r25) {
        /*
            Method dump skipped, instructions count: 563
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.constraintlayout.motion.widget.MotionController.interpolate(android.view.View, float, long, androidx.constraintlayout.motion.widget.KeyCache):boolean");
    }

    void getDpDt(float position, float locationX, float locationY, float[] mAnchorDpDt) {
        float position2 = getAdjustedPosition(position, this.mVelocity);
        if (this.mSpline != null) {
            this.mSpline[0].getSlope(position2, this.mInterpolateVelocity);
            this.mSpline[0].getPos(position2, this.mInterpolateData);
            float v = this.mVelocity[0];
            for (int i = 0; i < this.mInterpolateVelocity.length; i++) {
                double[] dArr = this.mInterpolateVelocity;
                dArr[i] = dArr[i] * v;
            }
            if (this.mArcSpline == null) {
                this.mStartMotionPath.setDpDt(locationX, locationY, mAnchorDpDt, this.mInterpolateVariables, this.mInterpolateVelocity, this.mInterpolateData);
                return;
            } else {
                if (this.mInterpolateData.length > 0) {
                    this.mArcSpline.getPos(position2, this.mInterpolateData);
                    this.mArcSpline.getSlope(position2, this.mInterpolateVelocity);
                    this.mStartMotionPath.setDpDt(locationX, locationY, mAnchorDpDt, this.mInterpolateVariables, this.mInterpolateVelocity, this.mInterpolateData);
                    return;
                }
                return;
            }
        }
        float dleft = this.mEndMotionPath.x - this.mStartMotionPath.x;
        float dTop = this.mEndMotionPath.y - this.mStartMotionPath.y;
        float dWidth = this.mEndMotionPath.width - this.mStartMotionPath.width;
        float dHeight = this.mEndMotionPath.height - this.mStartMotionPath.height;
        float dRight = dleft + dWidth;
        float dBottom = dTop + dHeight;
        mAnchorDpDt[0] = ((1.0f - locationX) * dleft) + (dRight * locationX);
        mAnchorDpDt[1] = ((1.0f - locationY) * dTop) + (dBottom * locationY);
    }

    void getPostLayoutDvDp(float position, int width, int height, float locationX, float locationY, float[] mAnchorDpDt) {
        VelocityMatrix vmat;
        float position2 = getAdjustedPosition(position, this.mVelocity);
        SplineSet trans_x = this.mAttributesMap == null ? null : this.mAttributesMap.get("translationX");
        SplineSet trans_y = this.mAttributesMap == null ? null : this.mAttributesMap.get("translationY");
        SplineSet rotation = this.mAttributesMap == null ? null : this.mAttributesMap.get("rotation");
        SplineSet scale_x = this.mAttributesMap == null ? null : this.mAttributesMap.get("scaleX");
        SplineSet scale_y = this.mAttributesMap == null ? null : this.mAttributesMap.get("scaleY");
        KeyCycleOscillator osc_x = this.mCycleMap == null ? null : this.mCycleMap.get("translationX");
        KeyCycleOscillator osc_y = this.mCycleMap == null ? null : this.mCycleMap.get("translationY");
        KeyCycleOscillator osc_r = this.mCycleMap == null ? null : this.mCycleMap.get("rotation");
        KeyCycleOscillator osc_sx = this.mCycleMap == null ? null : this.mCycleMap.get("scaleX");
        KeyCycleOscillator osc_sy = this.mCycleMap != null ? this.mCycleMap.get("scaleY") : null;
        VelocityMatrix vmat2 = new VelocityMatrix();
        vmat2.clear();
        vmat2.setRotationVelocity(rotation, position2);
        vmat2.setTranslationVelocity(trans_x, trans_y, position2);
        vmat2.setScaleVelocity(scale_x, scale_y, position2);
        vmat2.setRotationVelocity(osc_r, position2);
        vmat2.setTranslationVelocity(osc_x, osc_y, position2);
        vmat2.setScaleVelocity(osc_sx, osc_sy, position2);
        if (this.mArcSpline != null) {
            if (this.mInterpolateData.length > 0) {
                vmat = vmat2;
                this.mArcSpline.getPos(position2, this.mInterpolateData);
                this.mArcSpline.getSlope(position2, this.mInterpolateVelocity);
                this.mStartMotionPath.setDpDt(locationX, locationY, mAnchorDpDt, this.mInterpolateVariables, this.mInterpolateVelocity, this.mInterpolateData);
            } else {
                vmat = vmat2;
            }
            vmat.applyTransform(locationX, locationY, width, height, mAnchorDpDt);
            return;
        }
        if (this.mSpline != null) {
            float position3 = getAdjustedPosition(position2, this.mVelocity);
            this.mSpline[0].getSlope(position3, this.mInterpolateVelocity);
            this.mSpline[0].getPos(position3, this.mInterpolateData);
            float v = this.mVelocity[0];
            int i = 0;
            while (i < this.mInterpolateVelocity.length) {
                double[] dArr = this.mInterpolateVelocity;
                int i2 = i;
                dArr[i2] = dArr[i] * v;
                i = i2 + 1;
            }
            this.mStartMotionPath.setDpDt(locationX, locationY, mAnchorDpDt, this.mInterpolateVariables, this.mInterpolateVelocity, this.mInterpolateData);
            vmat2.applyTransform(locationX, locationY, width, height, mAnchorDpDt);
            return;
        }
        float dleft = this.mEndMotionPath.x - this.mStartMotionPath.x;
        float dTop = this.mEndMotionPath.y - this.mStartMotionPath.y;
        float dWidth = this.mEndMotionPath.width - this.mStartMotionPath.width;
        float dHeight = this.mEndMotionPath.height - this.mStartMotionPath.height;
        float dRight = dleft + dWidth;
        float dBottom = dTop + dHeight;
        mAnchorDpDt[0] = ((1.0f - locationX) * dleft) + (dRight * locationX);
        mAnchorDpDt[1] = ((1.0f - locationY) * dTop) + (dBottom * locationY);
        vmat2.clear();
        vmat2.setRotationVelocity(rotation, position2);
        vmat2.setTranslationVelocity(trans_x, trans_y, position2);
        vmat2.setScaleVelocity(scale_x, scale_y, position2);
        vmat2.setRotationVelocity(osc_r, position2);
        vmat2.setTranslationVelocity(osc_x, osc_y, position2);
        vmat2.setScaleVelocity(osc_sx, osc_sy, position2);
        vmat2.applyTransform(locationX, locationY, width, height, mAnchorDpDt);
    }

    public int getDrawPath() {
        int mode = this.mStartMotionPath.mDrawPath;
        Iterator<MotionPaths> it = this.mMotionPaths.iterator();
        while (it.hasNext()) {
            MotionPaths keyFrame = it.next();
            mode = Math.max(mode, keyFrame.mDrawPath);
        }
        return Math.max(mode, this.mEndMotionPath.mDrawPath);
    }

    public void setDrawPath(int debugMode) {
        this.mStartMotionPath.mDrawPath = debugMode;
    }

    String name() {
        Context context = this.mView.getContext();
        return context.getResources().getResourceEntryName(this.mView.getId());
    }

    void positionKeyframe(View view, KeyPositionBase key, float x, float y, String[] attribute, float[] value) {
        RectF start = new RectF();
        start.left = this.mStartMotionPath.x;
        start.top = this.mStartMotionPath.y;
        start.right = start.left + this.mStartMotionPath.width;
        start.bottom = start.top + this.mStartMotionPath.height;
        RectF end = new RectF();
        end.left = this.mEndMotionPath.x;
        end.top = this.mEndMotionPath.y;
        end.right = end.left + this.mEndMotionPath.width;
        end.bottom = end.top + this.mEndMotionPath.height;
        key.positionAttributes(view, start, end, x, y, attribute, value);
    }

    public int getkeyFramePositions(int[] type, float[] pos) {
        int i = 0;
        int count = 0;
        Iterator<Key> it = this.mKeyList.iterator();
        while (it.hasNext()) {
            Key key = it.next();
            int i2 = i + 1;
            type[i] = key.mFramePosition + (key.mType * 1000);
            float time = key.mFramePosition / 100.0f;
            this.mSpline[0].getPos(time, this.mInterpolateData);
            this.mStartMotionPath.getCenter(this.mInterpolateVariables, this.mInterpolateData, pos, count);
            count += 2;
            i = i2;
        }
        return i;
    }

    public int getKeyFrameInfo(int type, int[] info) {
        int count = 0;
        int cursor = 0;
        float[] pos = new float[2];
        Iterator<Key> it = this.mKeyList.iterator();
        while (it.hasNext()) {
            Key key = it.next();
            if (key.mType == type || type != -1) {
                int len = cursor;
                info[cursor] = 0;
                int cursor2 = cursor + 1;
                info[cursor2] = key.mType;
                int cursor3 = cursor2 + 1;
                info[cursor3] = key.mFramePosition;
                float time = key.mFramePosition / 100.0f;
                this.mSpline[0].getPos(time, this.mInterpolateData);
                this.mStartMotionPath.getCenter(this.mInterpolateVariables, this.mInterpolateData, pos, 0);
                int cursor4 = cursor3 + 1;
                info[cursor4] = Float.floatToIntBits(pos[0]);
                int cursor5 = cursor4 + 1;
                info[cursor5] = Float.floatToIntBits(pos[1]);
                if (key instanceof KeyPosition) {
                    KeyPosition kp = (KeyPosition) key;
                    int cursor6 = cursor5 + 1;
                    info[cursor6] = kp.mPositionType;
                    int cursor7 = cursor6 + 1;
                    info[cursor7] = Float.floatToIntBits(kp.mPercentX);
                    cursor5 = cursor7 + 1;
                    info[cursor5] = Float.floatToIntBits(kp.mPercentY);
                }
                cursor = cursor5 + 1;
                info[len] = cursor - len;
                count++;
            }
        }
        return count;
    }
}
