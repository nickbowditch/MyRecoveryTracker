package androidx.constraintlayout.motion.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import androidx.constraintlayout.motion.utils.Easing;
import androidx.constraintlayout.widget.R;
import java.util.HashMap;

/* loaded from: classes.dex */
public class KeyPosition extends KeyPositionBase {
    static final int KEY_TYPE = 2;
    static final String NAME = "KeyPosition";
    private static final String PERCENT_X = "percentX";
    private static final String PERCENT_Y = "percentY";
    private static final String TAG = "KeyPosition";
    public static final int TYPE_CARTESIAN = 0;
    public static final int TYPE_PATH = 1;
    public static final int TYPE_SCREEN = 2;
    String mTransitionEasing = null;
    int mPathMotionArc = UNSET;
    int mDrawPath = 0;
    float mPercentWidth = Float.NaN;
    float mPercentHeight = Float.NaN;
    float mPercentX = Float.NaN;
    float mPercentY = Float.NaN;
    float mAltPercentX = Float.NaN;
    float mAltPercentY = Float.NaN;
    int mPositionType = 0;
    private float mCalculatedPositionX = Float.NaN;
    private float mCalculatedPositionY = Float.NaN;

    public KeyPosition() {
        this.mType = 2;
    }

    @Override // androidx.constraintlayout.motion.widget.Key
    public void load(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeyPosition);
        Loader.read(this, a);
    }

    @Override // androidx.constraintlayout.motion.widget.Key
    public void addValues(HashMap<String, SplineSet> splines) {
    }

    @Override // androidx.constraintlayout.motion.widget.KeyPositionBase
    void calcPosition(int layoutWidth, int layoutHeight, float start_x, float start_y, float end_x, float end_y) {
        switch (this.mPositionType) {
            case 1:
                calcPathPosition(start_x, start_y, end_x, end_y);
                break;
            case 2:
                calcScreenPosition(layoutWidth, layoutHeight);
                break;
            default:
                calcCartesianPosition(start_x, start_y, end_x, end_y);
                break;
        }
    }

    private void calcScreenPosition(int layoutWidth, int layoutHeight) {
        this.mCalculatedPositionX = ((layoutWidth - 0) * this.mPercentX) + (0 / 2);
        this.mCalculatedPositionY = ((layoutHeight - 0) * this.mPercentX) + (0 / 2);
    }

    private void calcPathPosition(float start_x, float start_y, float end_x, float end_y) {
        float pathVectorX = end_x - start_x;
        float pathVectorY = end_y - start_y;
        float perpendicularX = -pathVectorY;
        this.mCalculatedPositionX = (this.mPercentX * pathVectorX) + start_x + (this.mPercentY * perpendicularX);
        this.mCalculatedPositionY = (this.mPercentX * pathVectorY) + start_y + (this.mPercentY * pathVectorX);
    }

    private void calcCartesianPosition(float start_x, float start_y, float end_x, float end_y) {
        float pathVectorX = end_x - start_x;
        float pathVectorY = end_y - start_y;
        float dxdx = Float.isNaN(this.mPercentX) ? 0.0f : this.mPercentX;
        float dydx = Float.isNaN(this.mAltPercentY) ? 0.0f : this.mAltPercentY;
        float dydy = Float.isNaN(this.mPercentY) ? 0.0f : this.mPercentY;
        float dxdy = Float.isNaN(this.mAltPercentX) ? 0.0f : this.mAltPercentX;
        this.mCalculatedPositionX = (int) ((pathVectorX * dxdx) + start_x + (pathVectorY * dxdy));
        this.mCalculatedPositionY = (int) ((pathVectorX * dydx) + start_y + (pathVectorY * dydy));
    }

    @Override // androidx.constraintlayout.motion.widget.KeyPositionBase
    float getPositionX() {
        return this.mCalculatedPositionX;
    }

    @Override // androidx.constraintlayout.motion.widget.KeyPositionBase
    float getPositionY() {
        return this.mCalculatedPositionY;
    }

    @Override // androidx.constraintlayout.motion.widget.KeyPositionBase
    public void positionAttributes(View view, RectF start, RectF end, float x, float y, String[] attribute, float[] value) {
        switch (this.mPositionType) {
            case 1:
                positionPathAttributes(start, end, x, y, attribute, value);
                break;
            case 2:
                positionScreenAttributes(view, start, end, x, y, attribute, value);
                break;
            default:
                positionCartAttributes(start, end, x, y, attribute, value);
                break;
        }
    }

    void positionPathAttributes(RectF start, RectF end, float x, float y, String[] attribute, float[] value) {
        float startCenterX = start.centerX();
        float startCenterY = start.centerY();
        float endCenterX = end.centerX();
        float endCenterY = end.centerY();
        float pathVectorX = endCenterX - startCenterX;
        float pathVectorY = endCenterY - startCenterY;
        float distance = (float) Math.hypot(pathVectorX, pathVectorY);
        if (distance < 1.0E-4d) {
            System.out.println("distance ~ 0");
            value[0] = 0.0f;
            value[1] = 0.0f;
            return;
        }
        float dx = pathVectorX / distance;
        float dy = pathVectorY / distance;
        float perpendicular = (((y - startCenterY) * dx) - ((x - startCenterX) * dy)) / distance;
        float dist = (((x - startCenterX) * dx) + ((y - startCenterY) * dy)) / distance;
        if (attribute[0] != null) {
            if (PERCENT_X.equals(attribute[0])) {
                value[0] = dist;
                value[1] = perpendicular;
                return;
            }
            return;
        }
        attribute[0] = PERCENT_X;
        attribute[1] = PERCENT_Y;
        value[0] = dist;
        value[1] = perpendicular;
    }

    void positionScreenAttributes(View view, RectF start, RectF end, float x, float y, String[] attribute, float[] value) {
        float startCenterX = start.centerX();
        float startCenterY = start.centerY();
        float endCenterX = end.centerX();
        float endCenterY = end.centerY();
        float f = endCenterX - startCenterX;
        float f2 = endCenterY - startCenterY;
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        int width = viewGroup.getWidth();
        int height = viewGroup.getHeight();
        if (attribute[0] != null) {
            if (PERCENT_X.equals(attribute[0])) {
                value[0] = x / width;
                value[1] = y / height;
                return;
            } else {
                value[1] = x / width;
                value[0] = y / height;
                return;
            }
        }
        attribute[0] = PERCENT_X;
        value[0] = x / width;
        attribute[1] = PERCENT_Y;
        value[1] = y / height;
    }

    void positionCartAttributes(RectF start, RectF end, float x, float y, String[] attribute, float[] value) {
        float startCenterX = start.centerX();
        float startCenterY = start.centerY();
        float endCenterX = end.centerX();
        float endCenterY = end.centerY();
        float pathVectorX = endCenterX - startCenterX;
        float pathVectorY = endCenterY - startCenterY;
        if (attribute[0] != null) {
            if (PERCENT_X.equals(attribute[0])) {
                value[0] = (x - startCenterX) / pathVectorX;
                value[1] = (y - startCenterY) / pathVectorY;
                return;
            } else {
                value[1] = (x - startCenterX) / pathVectorX;
                value[0] = (y - startCenterY) / pathVectorY;
                return;
            }
        }
        attribute[0] = PERCENT_X;
        value[0] = (x - startCenterX) / pathVectorX;
        attribute[1] = PERCENT_Y;
        value[1] = (y - startCenterY) / pathVectorY;
    }

    @Override // androidx.constraintlayout.motion.widget.KeyPositionBase
    public boolean intersects(int layoutWidth, int layoutHeight, RectF start, RectF end, float x, float y) {
        calcPosition(layoutWidth, layoutHeight, start.centerX(), start.centerY(), end.centerX(), end.centerY());
        if (Math.abs(x - this.mCalculatedPositionX) < 20.0f && Math.abs(y - this.mCalculatedPositionY) < 20.0f) {
            return true;
        }
        return false;
    }

    private static class Loader {
        private static final int CURVE_FIT = 4;
        private static final int DRAW_PATH = 5;
        private static final int FRAME_POSITION = 2;
        private static final int PATH_MOTION_ARC = 10;
        private static final int PERCENT_HEIGHT = 12;
        private static final int PERCENT_WIDTH = 11;
        private static final int PERCENT_X = 6;
        private static final int PERCENT_Y = 7;
        private static final int SIZE_PERCENT = 8;
        private static final int TARGET_ID = 1;
        private static final int TRANSITION_EASING = 3;
        private static final int TYPE = 9;
        private static SparseIntArray mAttrMap = new SparseIntArray();

        private Loader() {
        }

        static {
            mAttrMap.append(R.styleable.KeyPosition_motionTarget, 1);
            mAttrMap.append(R.styleable.KeyPosition_framePosition, 2);
            mAttrMap.append(R.styleable.KeyPosition_transitionEasing, 3);
            mAttrMap.append(R.styleable.KeyPosition_curveFit, 4);
            mAttrMap.append(R.styleable.KeyPosition_drawPath, 5);
            mAttrMap.append(R.styleable.KeyPosition_percentX, 6);
            mAttrMap.append(R.styleable.KeyPosition_percentY, 7);
            mAttrMap.append(R.styleable.KeyPosition_keyPositionType, 9);
            mAttrMap.append(R.styleable.KeyPosition_sizePercent, 8);
            mAttrMap.append(R.styleable.KeyPosition_percentWidth, 11);
            mAttrMap.append(R.styleable.KeyPosition_percentHeight, 12);
            mAttrMap.append(R.styleable.KeyPosition_pathMotionArc, 10);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static void read(KeyPosition c, TypedArray a) {
            int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                switch (mAttrMap.get(attr)) {
                    case 1:
                        if (MotionLayout.IS_IN_EDIT_MODE) {
                            c.mTargetId = a.getResourceId(attr, c.mTargetId);
                            if (c.mTargetId == -1) {
                                c.mTargetString = a.getString(attr);
                                break;
                            } else {
                                break;
                            }
                        } else if (a.peekValue(attr).type == 3) {
                            c.mTargetString = a.getString(attr);
                            break;
                        } else {
                            c.mTargetId = a.getResourceId(attr, c.mTargetId);
                            break;
                        }
                    case 2:
                        c.mFramePosition = a.getInt(attr, c.mFramePosition);
                        break;
                    case 3:
                        if (a.peekValue(attr).type == 3) {
                            c.mTransitionEasing = a.getString(attr);
                            break;
                        } else {
                            c.mTransitionEasing = Easing.NAMED_EASING[a.getInteger(attr, 0)];
                            break;
                        }
                    case 4:
                        c.mCurveFit = a.getInteger(attr, c.mCurveFit);
                        break;
                    case 5:
                        c.mDrawPath = a.getInt(attr, c.mDrawPath);
                        break;
                    case 6:
                        c.mPercentX = a.getFloat(attr, c.mPercentX);
                        break;
                    case 7:
                        c.mPercentY = a.getFloat(attr, c.mPercentY);
                        break;
                    case 8:
                        float f = a.getFloat(attr, c.mPercentHeight);
                        c.mPercentWidth = f;
                        c.mPercentHeight = f;
                        break;
                    case 9:
                        c.mPositionType = a.getInt(attr, c.mPositionType);
                        break;
                    case 10:
                        c.mPathMotionArc = a.getInt(attr, c.mPathMotionArc);
                        break;
                    case 11:
                        c.mPercentWidth = a.getFloat(attr, c.mPercentWidth);
                        break;
                    case 12:
                        c.mPercentHeight = a.getFloat(attr, c.mPercentHeight);
                        break;
                    default:
                        Log.e("KeyPosition", "unused attribute 0x" + Integer.toHexString(attr) + "   " + mAttrMap.get(attr));
                        break;
                }
            }
            int i2 = c.mFramePosition;
            if (i2 == -1) {
                Log.e("KeyPosition", "no frame position");
            }
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0050  */
    @Override // androidx.constraintlayout.motion.widget.Key
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void setValue(java.lang.String r2, java.lang.Object r3) {
        /*
            r1 = this;
            int r0 = r2.hashCode()
            switch(r0) {
                case -1812823328: goto L45;
                case -1127236479: goto L3b;
                case -1017587252: goto L31;
                case -827014263: goto L27;
                case -200259324: goto L1c;
                case 428090547: goto L12;
                case 428090548: goto L8;
                default: goto L7;
            }
        L7:
            goto L50
        L8:
            java.lang.String r0 = "percentY"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L7
            r0 = 6
            goto L51
        L12:
            java.lang.String r0 = "percentX"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L7
            r0 = 5
            goto L51
        L1c:
            java.lang.String r0 = "sizePercent"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L7
            r0 = 4
            goto L51
        L27:
            java.lang.String r0 = "drawPath"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L7
            r0 = 1
            goto L51
        L31:
            java.lang.String r0 = "percentHeight"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L7
            r0 = 3
            goto L51
        L3b:
            java.lang.String r0 = "percentWidth"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L7
            r0 = 2
            goto L51
        L45:
            java.lang.String r0 = "transitionEasing"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L7
            r0 = 0
            goto L51
        L50:
            r0 = -1
        L51:
            switch(r0) {
                case 0: goto L81;
                case 1: goto L7a;
                case 2: goto L73;
                case 3: goto L6c;
                case 4: goto L63;
                case 5: goto L5c;
                case 6: goto L55;
                default: goto L54;
            }
        L54:
            goto L88
        L55:
            float r0 = r1.toFloat(r3)
            r1.mPercentY = r0
            goto L88
        L5c:
            float r0 = r1.toFloat(r3)
            r1.mPercentX = r0
            goto L88
        L63:
            float r0 = r1.toFloat(r3)
            r1.mPercentWidth = r0
            r1.mPercentHeight = r0
            goto L88
        L6c:
            float r0 = r1.toFloat(r3)
            r1.mPercentHeight = r0
            goto L88
        L73:
            float r0 = r1.toFloat(r3)
            r1.mPercentWidth = r0
            goto L88
        L7a:
            int r0 = r1.toInt(r3)
            r1.mDrawPath = r0
            goto L88
        L81:
            java.lang.String r0 = r3.toString()
            r1.mTransitionEasing = r0
        L88:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.constraintlayout.motion.widget.KeyPosition.setValue(java.lang.String, java.lang.Object):void");
    }
}
