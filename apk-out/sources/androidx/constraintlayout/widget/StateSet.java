package androidx.constraintlayout.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* loaded from: classes.dex */
public class StateSet {
    private static final boolean DEBUG = false;
    public static final String TAG = "ConstraintLayoutStates";
    ConstraintSet mDefaultConstraintSet;
    int mDefaultState = -1;
    int mCurrentStateId = -1;
    int mCurrentConstraintNumber = -1;
    private SparseArray<State> mStateList = new SparseArray<>();
    private SparseArray<ConstraintSet> mConstraintSetMap = new SparseArray<>();
    private ConstraintsChangedListener mConstraintsChangedListener = null;

    public StateSet(Context context, XmlPullParser parser) throws XmlPullParserException, IOException {
        load(context, parser);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    /* JADX WARN: Removed duplicated region for block: B:21:0x004f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void load(android.content.Context r11, org.xmlpull.v1.XmlPullParser r12) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
            r10 = this;
            android.util.AttributeSet r0 = android.util.Xml.asAttributeSet(r12)
            int[] r1 = androidx.constraintlayout.widget.R.styleable.StateSet
            android.content.res.TypedArray r1 = r11.obtainStyledAttributes(r0, r1)
            int r2 = r1.getIndexCount()
            r3 = 0
        Lf:
            if (r3 >= r2) goto L24
            int r4 = r1.getIndex(r3)
            int r5 = androidx.constraintlayout.widget.R.styleable.StateSet_defaultState
            if (r4 != r5) goto L21
            int r5 = r10.mDefaultState
            int r5 = r1.getResourceId(r4, r5)
            r10.mDefaultState = r5
        L21:
            int r3 = r3 + 1
            goto Lf
        L24:
            r3 = 0
            r4 = 0
            r5 = 0
            int r6 = r12.getEventType()     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
        L2b:
            r7 = 1
            if (r6 == r7) goto Lbb
            java.lang.String r8 = "StateSet"
            switch(r6) {
                case 0: goto Laf;
                case 1: goto L33;
                case 2: goto L43;
                case 3: goto L35;
                default: goto L33;
            }
        L33:
            goto Lb4
        L35:
            java.lang.String r7 = r12.getName()     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            boolean r7 = r8.equals(r7)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            if (r7 == 0) goto L40
            return
        L40:
            r3 = 0
            goto Lb4
        L43:
            java.lang.String r9 = r12.getName()     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            r3 = r9
            int r9 = r3.hashCode()     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            switch(r9) {
                case 80204913: goto L6b;
                case 1301459538: goto L61;
                case 1382829617: goto L5a;
                case 1901439077: goto L50;
                default: goto L4f;
            }     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
        L4f:
            goto L75
        L50:
            java.lang.String r7 = "Variant"
            boolean r7 = r3.equals(r7)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            if (r7 == 0) goto L4f
            r7 = 3
            goto L76
        L5a:
            boolean r8 = r3.equals(r8)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            if (r8 == 0) goto L4f
            goto L76
        L61:
            java.lang.String r7 = "LayoutDescription"
            boolean r7 = r3.equals(r7)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            if (r7 == 0) goto L4f
            r7 = 0
            goto L76
        L6b:
            java.lang.String r7 = "State"
            boolean r7 = r3.equals(r7)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            if (r7 == 0) goto L4f
            r7 = 2
            goto L76
        L75:
            r7 = -1
        L76:
            switch(r7) {
                case 0: goto L96;
                case 1: goto L95;
                case 2: goto L87;
                case 3: goto L7c;
                default: goto L79;
            }     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
        L79:
            java.lang.String r7 = "ConstraintLayoutStates"
            goto L97
        L7c:
            androidx.constraintlayout.widget.StateSet$Variant r7 = new androidx.constraintlayout.widget.StateSet$Variant     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            r7.<init>(r11, r12)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            if (r5 == 0) goto Lae
            r5.add(r7)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            goto Lae
        L87:
            androidx.constraintlayout.widget.StateSet$State r7 = new androidx.constraintlayout.widget.StateSet$State     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            r7.<init>(r11, r12)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            r5 = r7
            android.util.SparseArray<androidx.constraintlayout.widget.StateSet$State> r7 = r10.mStateList     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            int r8 = r5.mId     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            r7.put(r8, r5)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            goto Lae
        L95:
            goto Lae
        L96:
            goto Lae
        L97:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            r8.<init>()     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            java.lang.String r9 = "unknown tag "
            java.lang.StringBuilder r8 = r8.append(r9)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            java.lang.StringBuilder r8 = r8.append(r3)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            java.lang.String r8 = r8.toString()     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            android.util.Log.v(r7, r8)     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
        Lae:
            goto Lb4
        Laf:
            java.lang.String r7 = r12.getName()     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            r4 = r7
        Lb4:
            int r7 = r12.next()     // Catch: java.io.IOException -> Lbc org.xmlpull.v1.XmlPullParserException -> Lc1
            r6 = r7
            goto L2b
        Lbb:
            goto Lc5
        Lbc:
            r4 = move-exception
            r4.printStackTrace()
            goto Lc6
        Lc1:
            r4 = move-exception
            r4.printStackTrace()
        Lc5:
        Lc6:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.constraintlayout.widget.StateSet.load(android.content.Context, org.xmlpull.v1.XmlPullParser):void");
    }

    public boolean needsToChange(int id, float width, float height) {
        if (this.mCurrentStateId != id) {
            return true;
        }
        SparseArray<State> sparseArray = this.mStateList;
        State state = id == -1 ? sparseArray.valueAt(0) : sparseArray.get(this.mCurrentStateId);
        return (this.mCurrentConstraintNumber == -1 || !state.mVariants.get(this.mCurrentConstraintNumber).match(width, height)) && this.mCurrentConstraintNumber != state.findMatch(width, height);
    }

    public void setOnConstraintsChanged(ConstraintsChangedListener constraintsChangedListener) {
        this.mConstraintsChangedListener = constraintsChangedListener;
    }

    public int stateGetConstraintID(int id, int width, int height) {
        return updateConstraints(-1, id, width, height);
    }

    public int convertToConstraintSet(int currentConstrainSettId, int stateId, float width, float height) {
        State state = this.mStateList.get(stateId);
        if (state == null) {
            return stateId;
        }
        if (width == -1.0f || height == -1.0f) {
            if (state.mConstraintID == currentConstrainSettId) {
                return currentConstrainSettId;
            }
            Iterator<Variant> it = state.mVariants.iterator();
            while (it.hasNext()) {
                if (currentConstrainSettId == it.next().mConstraintID) {
                    return currentConstrainSettId;
                }
            }
            return state.mConstraintID;
        }
        Variant match = null;
        Iterator<Variant> it2 = state.mVariants.iterator();
        while (it2.hasNext()) {
            Variant mVariant = it2.next();
            if (mVariant.match(width, height)) {
                if (currentConstrainSettId == mVariant.mConstraintID) {
                    return currentConstrainSettId;
                }
                match = mVariant;
            }
        }
        if (match != null) {
            return match.mConstraintID;
        }
        return state.mConstraintID;
    }

    public int updateConstraints(int currentid, int id, float width, float height) {
        State state;
        int match;
        if (currentid == id) {
            if (id == -1) {
                state = this.mStateList.valueAt(0);
            } else {
                state = this.mStateList.get(this.mCurrentStateId);
            }
            if (state == null) {
                return -1;
            }
            if ((this.mCurrentConstraintNumber == -1 || !state.mVariants.get(currentid).match(width, height)) && currentid != (match = state.findMatch(width, height))) {
                return match == -1 ? state.mConstraintID : state.mVariants.get(match).mConstraintID;
            }
            return currentid;
        }
        State state2 = this.mStateList.get(id);
        if (state2 == null) {
            return -1;
        }
        int match2 = state2.findMatch(width, height);
        return match2 == -1 ? state2.mConstraintID : state2.mVariants.get(match2).mConstraintID;
    }

    static class State {
        int mConstraintID;
        int mId;
        boolean mIsLayout;
        ArrayList<Variant> mVariants = new ArrayList<>();

        public State(Context context, XmlPullParser parser) throws Resources.NotFoundException {
            this.mConstraintID = -1;
            this.mIsLayout = false;
            AttributeSet attrs = Xml.asAttributeSet(parser);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.State);
            int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.State_android_id) {
                    this.mId = a.getResourceId(attr, this.mId);
                } else if (attr == R.styleable.State_constraints) {
                    this.mConstraintID = a.getResourceId(attr, this.mConstraintID);
                    String type = context.getResources().getResourceTypeName(this.mConstraintID);
                    context.getResources().getResourceName(this.mConstraintID);
                    if ("layout".equals(type)) {
                        this.mIsLayout = true;
                    }
                }
            }
            a.recycle();
        }

        void add(Variant size) {
            this.mVariants.add(size);
        }

        public int findMatch(float width, float height) {
            for (int i = 0; i < this.mVariants.size(); i++) {
                if (this.mVariants.get(i).match(width, height)) {
                    return i;
                }
            }
            return -1;
        }
    }

    static class Variant {
        int mConstraintID;
        int mId;
        boolean mIsLayout;
        float mMaxHeight;
        float mMaxWidth;
        float mMinHeight;
        float mMinWidth;

        public Variant(Context context, XmlPullParser parser) throws Resources.NotFoundException {
            this.mMinWidth = Float.NaN;
            this.mMinHeight = Float.NaN;
            this.mMaxWidth = Float.NaN;
            this.mMaxHeight = Float.NaN;
            this.mConstraintID = -1;
            this.mIsLayout = false;
            AttributeSet attrs = Xml.asAttributeSet(parser);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Variant);
            int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.Variant_constraints) {
                    this.mConstraintID = a.getResourceId(attr, this.mConstraintID);
                    String type = context.getResources().getResourceTypeName(this.mConstraintID);
                    context.getResources().getResourceName(this.mConstraintID);
                    if ("layout".equals(type)) {
                        this.mIsLayout = true;
                    }
                } else if (attr == R.styleable.Variant_region_heightLessThan) {
                    this.mMaxHeight = a.getDimension(attr, this.mMaxHeight);
                } else if (attr == R.styleable.Variant_region_heightMoreThan) {
                    this.mMinHeight = a.getDimension(attr, this.mMinHeight);
                } else if (attr == R.styleable.Variant_region_widthLessThan) {
                    this.mMaxWidth = a.getDimension(attr, this.mMaxWidth);
                } else if (attr == R.styleable.Variant_region_widthMoreThan) {
                    this.mMinWidth = a.getDimension(attr, this.mMinWidth);
                } else {
                    Log.v("ConstraintLayoutStates", "Unknown tag");
                }
            }
            a.recycle();
        }

        boolean match(float widthDp, float heightDp) {
            if (!Float.isNaN(this.mMinWidth) && widthDp < this.mMinWidth) {
                return false;
            }
            if (!Float.isNaN(this.mMinHeight) && heightDp < this.mMinHeight) {
                return false;
            }
            if (Float.isNaN(this.mMaxWidth) || widthDp <= this.mMaxWidth) {
                return Float.isNaN(this.mMaxHeight) || heightDp <= this.mMaxHeight;
            }
            return false;
        }
    }
}
