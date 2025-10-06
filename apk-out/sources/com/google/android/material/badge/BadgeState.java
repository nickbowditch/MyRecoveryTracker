package com.google.android.material.badge;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import com.google.android.material.R;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import java.io.IOException;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParserException;

/* loaded from: classes.dex */
public final class BadgeState {
    private static final String BADGE_RESOURCE_TAG = "badge";
    final float badgeHeight;
    final float badgeRadius;
    final float badgeWidth;
    final float badgeWithTextHeight;
    final float badgeWithTextRadius;
    final float badgeWithTextWidth;
    private final State currentState = new State();
    final int horizontalInset;
    final int horizontalInsetWithText;
    int offsetAlignmentMode;
    private final State overridingState;

    BadgeState(Context context, int badgeResId, int defStyleAttr, int defStyleRes, State storedState) throws XmlPullParserException, Resources.NotFoundException, IOException {
        CharSequence string;
        int i;
        int i2;
        int i3;
        int i4;
        int iIntValue;
        int iIntValue2;
        int iIntValue3;
        int iIntValue4;
        int iIntValue5;
        int iIntValue6;
        int iIntValue7;
        int iIntValue8;
        int iIntValue9;
        int iIntValue10;
        int iIntValue11;
        int iIntValue12;
        int iIntValue13;
        int iIntValue14;
        boolean zBooleanValue;
        storedState = storedState == null ? new State() : storedState;
        if (badgeResId != 0) {
            storedState.badgeResId = badgeResId;
        }
        TypedArray a = generateTypedArray(context, storedState.badgeResId, defStyleAttr, defStyleRes);
        Resources res = context.getResources();
        this.badgeRadius = a.getDimensionPixelSize(R.styleable.Badge_badgeRadius, -1);
        this.horizontalInset = context.getResources().getDimensionPixelSize(R.dimen.mtrl_badge_horizontal_edge_offset);
        this.horizontalInsetWithText = context.getResources().getDimensionPixelSize(R.dimen.mtrl_badge_text_horizontal_edge_offset);
        this.badgeWithTextRadius = a.getDimensionPixelSize(R.styleable.Badge_badgeWithTextRadius, -1);
        this.badgeWidth = a.getDimension(R.styleable.Badge_badgeWidth, res.getDimension(R.dimen.m3_badge_size));
        this.badgeWithTextWidth = a.getDimension(R.styleable.Badge_badgeWithTextWidth, res.getDimension(R.dimen.m3_badge_with_text_size));
        this.badgeHeight = a.getDimension(R.styleable.Badge_badgeHeight, res.getDimension(R.dimen.m3_badge_size));
        this.badgeWithTextHeight = a.getDimension(R.styleable.Badge_badgeWithTextHeight, res.getDimension(R.dimen.m3_badge_with_text_size));
        boolean z = true;
        this.offsetAlignmentMode = a.getInt(R.styleable.Badge_offsetAlignmentMode, 1);
        this.currentState.alpha = storedState.alpha == -2 ? 255 : storedState.alpha;
        if (storedState.number == -2) {
            if (a.hasValue(R.styleable.Badge_number)) {
                this.currentState.number = a.getInt(R.styleable.Badge_number, 0);
            } else {
                this.currentState.number = -1;
            }
        } else {
            this.currentState.number = storedState.number;
        }
        if (storedState.text == null) {
            if (a.hasValue(R.styleable.Badge_badgeText)) {
                this.currentState.text = a.getString(R.styleable.Badge_badgeText);
            }
        } else {
            this.currentState.text = storedState.text;
        }
        this.currentState.contentDescriptionForText = storedState.contentDescriptionForText;
        State state = this.currentState;
        if (storedState.contentDescriptionNumberless == null) {
            string = context.getString(R.string.mtrl_badge_numberless_content_description);
        } else {
            string = storedState.contentDescriptionNumberless;
        }
        state.contentDescriptionNumberless = string;
        State state2 = this.currentState;
        if (storedState.contentDescriptionQuantityStrings == 0) {
            i = R.plurals.mtrl_badge_content_description;
        } else {
            i = storedState.contentDescriptionQuantityStrings;
        }
        state2.contentDescriptionQuantityStrings = i;
        State state3 = this.currentState;
        if (storedState.contentDescriptionExceedsMaxBadgeNumberRes == 0) {
            i2 = R.string.mtrl_exceed_max_badge_number_content_description;
        } else {
            i2 = storedState.contentDescriptionExceedsMaxBadgeNumberRes;
        }
        state3.contentDescriptionExceedsMaxBadgeNumberRes = i2;
        State state4 = this.currentState;
        if (storedState.isVisible != null && !storedState.isVisible.booleanValue()) {
            z = false;
        }
        state4.isVisible = Boolean.valueOf(z);
        State state5 = this.currentState;
        if (storedState.maxCharacterCount == -2) {
            i3 = a.getInt(R.styleable.Badge_maxCharacterCount, -2);
        } else {
            i3 = storedState.maxCharacterCount;
        }
        state5.maxCharacterCount = i3;
        State state6 = this.currentState;
        if (storedState.maxNumber == -2) {
            i4 = a.getInt(R.styleable.Badge_maxNumber, -2);
        } else {
            i4 = storedState.maxNumber;
        }
        state6.maxNumber = i4;
        State state7 = this.currentState;
        if (storedState.badgeShapeAppearanceResId == null) {
            iIntValue = a.getResourceId(R.styleable.Badge_badgeShapeAppearance, R.style.ShapeAppearance_M3_Sys_Shape_Corner_Full);
        } else {
            iIntValue = storedState.badgeShapeAppearanceResId.intValue();
        }
        state7.badgeShapeAppearanceResId = Integer.valueOf(iIntValue);
        State state8 = this.currentState;
        if (storedState.badgeShapeAppearanceOverlayResId == null) {
            iIntValue2 = a.getResourceId(R.styleable.Badge_badgeShapeAppearanceOverlay, 0);
        } else {
            iIntValue2 = storedState.badgeShapeAppearanceOverlayResId.intValue();
        }
        state8.badgeShapeAppearanceOverlayResId = Integer.valueOf(iIntValue2);
        State state9 = this.currentState;
        if (storedState.badgeWithTextShapeAppearanceResId == null) {
            iIntValue3 = a.getResourceId(R.styleable.Badge_badgeWithTextShapeAppearance, R.style.ShapeAppearance_M3_Sys_Shape_Corner_Full);
        } else {
            iIntValue3 = storedState.badgeWithTextShapeAppearanceResId.intValue();
        }
        state9.badgeWithTextShapeAppearanceResId = Integer.valueOf(iIntValue3);
        State state10 = this.currentState;
        if (storedState.badgeWithTextShapeAppearanceOverlayResId == null) {
            iIntValue4 = a.getResourceId(R.styleable.Badge_badgeWithTextShapeAppearanceOverlay, 0);
        } else {
            iIntValue4 = storedState.badgeWithTextShapeAppearanceOverlayResId.intValue();
        }
        state10.badgeWithTextShapeAppearanceOverlayResId = Integer.valueOf(iIntValue4);
        State state11 = this.currentState;
        if (storedState.backgroundColor == null) {
            iIntValue5 = readColorFromAttributes(context, a, R.styleable.Badge_backgroundColor);
        } else {
            iIntValue5 = storedState.backgroundColor.intValue();
        }
        state11.backgroundColor = Integer.valueOf(iIntValue5);
        State state12 = this.currentState;
        if (storedState.badgeTextAppearanceResId == null) {
            iIntValue6 = a.getResourceId(R.styleable.Badge_badgeTextAppearance, R.style.TextAppearance_MaterialComponents_Badge);
        } else {
            iIntValue6 = storedState.badgeTextAppearanceResId.intValue();
        }
        state12.badgeTextAppearanceResId = Integer.valueOf(iIntValue6);
        if (storedState.badgeTextColor == null) {
            if (!a.hasValue(R.styleable.Badge_badgeTextColor)) {
                TextAppearance textAppearance = new TextAppearance(context, this.currentState.badgeTextAppearanceResId.intValue());
                this.currentState.badgeTextColor = Integer.valueOf(textAppearance.getTextColor().getDefaultColor());
            } else {
                this.currentState.badgeTextColor = Integer.valueOf(readColorFromAttributes(context, a, R.styleable.Badge_badgeTextColor));
            }
        } else {
            this.currentState.badgeTextColor = storedState.badgeTextColor;
        }
        State state13 = this.currentState;
        if (storedState.badgeGravity == null) {
            iIntValue7 = a.getInt(R.styleable.Badge_badgeGravity, 8388661);
        } else {
            iIntValue7 = storedState.badgeGravity.intValue();
        }
        state13.badgeGravity = Integer.valueOf(iIntValue7);
        State state14 = this.currentState;
        if (storedState.badgeHorizontalPadding == null) {
            iIntValue8 = a.getDimensionPixelSize(R.styleable.Badge_badgeWidePadding, res.getDimensionPixelSize(R.dimen.mtrl_badge_long_text_horizontal_padding));
        } else {
            iIntValue8 = storedState.badgeHorizontalPadding.intValue();
        }
        state14.badgeHorizontalPadding = Integer.valueOf(iIntValue8);
        State state15 = this.currentState;
        if (storedState.badgeVerticalPadding == null) {
            iIntValue9 = a.getDimensionPixelSize(R.styleable.Badge_badgeVerticalPadding, res.getDimensionPixelSize(R.dimen.m3_badge_with_text_vertical_padding));
        } else {
            iIntValue9 = storedState.badgeVerticalPadding.intValue();
        }
        state15.badgeVerticalPadding = Integer.valueOf(iIntValue9);
        State state16 = this.currentState;
        if (storedState.horizontalOffsetWithoutText == null) {
            iIntValue10 = a.getDimensionPixelOffset(R.styleable.Badge_horizontalOffset, 0);
        } else {
            iIntValue10 = storedState.horizontalOffsetWithoutText.intValue();
        }
        state16.horizontalOffsetWithoutText = Integer.valueOf(iIntValue10);
        State state17 = this.currentState;
        if (storedState.verticalOffsetWithoutText == null) {
            iIntValue11 = a.getDimensionPixelOffset(R.styleable.Badge_verticalOffset, 0);
        } else {
            iIntValue11 = storedState.verticalOffsetWithoutText.intValue();
        }
        state17.verticalOffsetWithoutText = Integer.valueOf(iIntValue11);
        State state18 = this.currentState;
        if (storedState.horizontalOffsetWithText == null) {
            iIntValue12 = a.getDimensionPixelOffset(R.styleable.Badge_horizontalOffsetWithText, this.currentState.horizontalOffsetWithoutText.intValue());
        } else {
            iIntValue12 = storedState.horizontalOffsetWithText.intValue();
        }
        state18.horizontalOffsetWithText = Integer.valueOf(iIntValue12);
        State state19 = this.currentState;
        if (storedState.verticalOffsetWithText == null) {
            iIntValue13 = a.getDimensionPixelOffset(R.styleable.Badge_verticalOffsetWithText, this.currentState.verticalOffsetWithoutText.intValue());
        } else {
            iIntValue13 = storedState.verticalOffsetWithText.intValue();
        }
        state19.verticalOffsetWithText = Integer.valueOf(iIntValue13);
        State state20 = this.currentState;
        if (storedState.largeFontVerticalOffsetAdjustment == null) {
            iIntValue14 = a.getDimensionPixelOffset(R.styleable.Badge_largeFontVerticalOffsetAdjustment, 0);
        } else {
            iIntValue14 = storedState.largeFontVerticalOffsetAdjustment.intValue();
        }
        state20.largeFontVerticalOffsetAdjustment = Integer.valueOf(iIntValue14);
        this.currentState.additionalHorizontalOffset = Integer.valueOf(storedState.additionalHorizontalOffset == null ? 0 : storedState.additionalHorizontalOffset.intValue());
        this.currentState.additionalVerticalOffset = Integer.valueOf(storedState.additionalVerticalOffset == null ? 0 : storedState.additionalVerticalOffset.intValue());
        State state21 = this.currentState;
        if (storedState.autoAdjustToWithinGrandparentBounds == null) {
            zBooleanValue = a.getBoolean(R.styleable.Badge_autoAdjustToWithinGrandparentBounds, false);
        } else {
            zBooleanValue = storedState.autoAdjustToWithinGrandparentBounds.booleanValue();
        }
        state21.autoAdjustToWithinGrandparentBounds = Boolean.valueOf(zBooleanValue);
        a.recycle();
        if (storedState.numberLocale == null) {
            this.currentState.numberLocale = Locale.getDefault(Locale.Category.FORMAT);
        } else {
            this.currentState.numberLocale = storedState.numberLocale;
        }
        this.overridingState = storedState;
    }

    private TypedArray generateTypedArray(Context context, int badgeResId, int defStyleAttr, int defStyleRes) throws XmlPullParserException, Resources.NotFoundException, IOException {
        AttributeSet attrs;
        int style;
        int style2 = 0;
        if (badgeResId == 0) {
            attrs = null;
        } else {
            AttributeSet attrs2 = DrawableUtils.parseDrawableXml(context, badgeResId, BADGE_RESOURCE_TAG);
            style2 = attrs2.getStyleAttribute();
            attrs = attrs2;
        }
        if (style2 != 0) {
            style = style2;
        } else {
            style = defStyleRes;
        }
        return ThemeEnforcement.obtainStyledAttributes(context, attrs, R.styleable.Badge, defStyleAttr, style, new int[0]);
    }

    State getOverridingState() {
        return this.overridingState;
    }

    boolean isVisible() {
        return this.currentState.isVisible.booleanValue();
    }

    void setVisible(boolean visible) {
        this.overridingState.isVisible = Boolean.valueOf(visible);
        this.currentState.isVisible = Boolean.valueOf(visible);
    }

    boolean hasNumber() {
        return this.currentState.number != -1;
    }

    int getNumber() {
        return this.currentState.number;
    }

    void setNumber(int number) {
        this.overridingState.number = number;
        this.currentState.number = number;
    }

    void clearNumber() {
        setNumber(-1);
    }

    boolean hasText() {
        return this.currentState.text != null;
    }

    String getText() {
        return this.currentState.text;
    }

    void setText(String text) {
        this.overridingState.text = text;
        this.currentState.text = text;
    }

    void clearText() {
        setText(null);
    }

    int getAlpha() {
        return this.currentState.alpha;
    }

    void setAlpha(int alpha) {
        this.overridingState.alpha = alpha;
        this.currentState.alpha = alpha;
    }

    int getMaxCharacterCount() {
        return this.currentState.maxCharacterCount;
    }

    void setMaxCharacterCount(int maxCharacterCount) {
        this.overridingState.maxCharacterCount = maxCharacterCount;
        this.currentState.maxCharacterCount = maxCharacterCount;
    }

    int getMaxNumber() {
        return this.currentState.maxNumber;
    }

    void setMaxNumber(int maxNumber) {
        this.overridingState.maxNumber = maxNumber;
        this.currentState.maxNumber = maxNumber;
    }

    int getBackgroundColor() {
        return this.currentState.backgroundColor.intValue();
    }

    void setBackgroundColor(int backgroundColor) {
        this.overridingState.backgroundColor = Integer.valueOf(backgroundColor);
        this.currentState.backgroundColor = Integer.valueOf(backgroundColor);
    }

    int getBadgeTextColor() {
        return this.currentState.badgeTextColor.intValue();
    }

    void setBadgeTextColor(int badgeTextColor) {
        this.overridingState.badgeTextColor = Integer.valueOf(badgeTextColor);
        this.currentState.badgeTextColor = Integer.valueOf(badgeTextColor);
    }

    int getTextAppearanceResId() {
        return this.currentState.badgeTextAppearanceResId.intValue();
    }

    void setTextAppearanceResId(int textAppearanceResId) {
        this.overridingState.badgeTextAppearanceResId = Integer.valueOf(textAppearanceResId);
        this.currentState.badgeTextAppearanceResId = Integer.valueOf(textAppearanceResId);
    }

    int getBadgeShapeAppearanceResId() {
        return this.currentState.badgeShapeAppearanceResId.intValue();
    }

    void setBadgeShapeAppearanceResId(int shapeAppearanceResId) {
        this.overridingState.badgeShapeAppearanceResId = Integer.valueOf(shapeAppearanceResId);
        this.currentState.badgeShapeAppearanceResId = Integer.valueOf(shapeAppearanceResId);
    }

    int getBadgeShapeAppearanceOverlayResId() {
        return this.currentState.badgeShapeAppearanceOverlayResId.intValue();
    }

    void setBadgeShapeAppearanceOverlayResId(int shapeAppearanceOverlayResId) {
        this.overridingState.badgeShapeAppearanceOverlayResId = Integer.valueOf(shapeAppearanceOverlayResId);
        this.currentState.badgeShapeAppearanceOverlayResId = Integer.valueOf(shapeAppearanceOverlayResId);
    }

    int getBadgeWithTextShapeAppearanceResId() {
        return this.currentState.badgeWithTextShapeAppearanceResId.intValue();
    }

    void setBadgeWithTextShapeAppearanceResId(int shapeAppearanceResId) {
        this.overridingState.badgeWithTextShapeAppearanceResId = Integer.valueOf(shapeAppearanceResId);
        this.currentState.badgeWithTextShapeAppearanceResId = Integer.valueOf(shapeAppearanceResId);
    }

    int getBadgeWithTextShapeAppearanceOverlayResId() {
        return this.currentState.badgeWithTextShapeAppearanceOverlayResId.intValue();
    }

    void setBadgeWithTextShapeAppearanceOverlayResId(int shapeAppearanceOverlayResId) {
        this.overridingState.badgeWithTextShapeAppearanceOverlayResId = Integer.valueOf(shapeAppearanceOverlayResId);
        this.currentState.badgeWithTextShapeAppearanceOverlayResId = Integer.valueOf(shapeAppearanceOverlayResId);
    }

    int getBadgeGravity() {
        return this.currentState.badgeGravity.intValue();
    }

    void setBadgeGravity(int badgeGravity) {
        this.overridingState.badgeGravity = Integer.valueOf(badgeGravity);
        this.currentState.badgeGravity = Integer.valueOf(badgeGravity);
    }

    int getBadgeHorizontalPadding() {
        return this.currentState.badgeHorizontalPadding.intValue();
    }

    void setBadgeHorizontalPadding(int horizontalPadding) {
        this.overridingState.badgeHorizontalPadding = Integer.valueOf(horizontalPadding);
        this.currentState.badgeHorizontalPadding = Integer.valueOf(horizontalPadding);
    }

    int getBadgeVerticalPadding() {
        return this.currentState.badgeVerticalPadding.intValue();
    }

    void setBadgeVerticalPadding(int verticalPadding) {
        this.overridingState.badgeVerticalPadding = Integer.valueOf(verticalPadding);
        this.currentState.badgeVerticalPadding = Integer.valueOf(verticalPadding);
    }

    int getHorizontalOffsetWithoutText() {
        return this.currentState.horizontalOffsetWithoutText.intValue();
    }

    void setHorizontalOffsetWithoutText(int offset) {
        this.overridingState.horizontalOffsetWithoutText = Integer.valueOf(offset);
        this.currentState.horizontalOffsetWithoutText = Integer.valueOf(offset);
    }

    int getVerticalOffsetWithoutText() {
        return this.currentState.verticalOffsetWithoutText.intValue();
    }

    void setVerticalOffsetWithoutText(int offset) {
        this.overridingState.verticalOffsetWithoutText = Integer.valueOf(offset);
        this.currentState.verticalOffsetWithoutText = Integer.valueOf(offset);
    }

    int getHorizontalOffsetWithText() {
        return this.currentState.horizontalOffsetWithText.intValue();
    }

    void setHorizontalOffsetWithText(int offset) {
        this.overridingState.horizontalOffsetWithText = Integer.valueOf(offset);
        this.currentState.horizontalOffsetWithText = Integer.valueOf(offset);
    }

    int getVerticalOffsetWithText() {
        return this.currentState.verticalOffsetWithText.intValue();
    }

    void setVerticalOffsetWithText(int offset) {
        this.overridingState.verticalOffsetWithText = Integer.valueOf(offset);
        this.currentState.verticalOffsetWithText = Integer.valueOf(offset);
    }

    int getLargeFontVerticalOffsetAdjustment() {
        return this.currentState.largeFontVerticalOffsetAdjustment.intValue();
    }

    void setLargeFontVerticalOffsetAdjustment(int offsetAdjustment) {
        this.overridingState.largeFontVerticalOffsetAdjustment = Integer.valueOf(offsetAdjustment);
        this.currentState.largeFontVerticalOffsetAdjustment = Integer.valueOf(offsetAdjustment);
    }

    int getAdditionalHorizontalOffset() {
        return this.currentState.additionalHorizontalOffset.intValue();
    }

    void setAdditionalHorizontalOffset(int offset) {
        this.overridingState.additionalHorizontalOffset = Integer.valueOf(offset);
        this.currentState.additionalHorizontalOffset = Integer.valueOf(offset);
    }

    int getAdditionalVerticalOffset() {
        return this.currentState.additionalVerticalOffset.intValue();
    }

    void setAdditionalVerticalOffset(int offset) {
        this.overridingState.additionalVerticalOffset = Integer.valueOf(offset);
        this.currentState.additionalVerticalOffset = Integer.valueOf(offset);
    }

    CharSequence getContentDescriptionForText() {
        return this.currentState.contentDescriptionForText;
    }

    void setContentDescriptionForText(CharSequence contentDescription) {
        this.overridingState.contentDescriptionForText = contentDescription;
        this.currentState.contentDescriptionForText = contentDescription;
    }

    CharSequence getContentDescriptionNumberless() {
        return this.currentState.contentDescriptionNumberless;
    }

    void setContentDescriptionNumberless(CharSequence contentDescriptionNumberless) {
        this.overridingState.contentDescriptionNumberless = contentDescriptionNumberless;
        this.currentState.contentDescriptionNumberless = contentDescriptionNumberless;
    }

    int getContentDescriptionQuantityStrings() {
        return this.currentState.contentDescriptionQuantityStrings;
    }

    void setContentDescriptionQuantityStringsResource(int stringsResource) {
        this.overridingState.contentDescriptionQuantityStrings = stringsResource;
        this.currentState.contentDescriptionQuantityStrings = stringsResource;
    }

    int getContentDescriptionExceedsMaxBadgeNumberStringResource() {
        return this.currentState.contentDescriptionExceedsMaxBadgeNumberRes;
    }

    void setContentDescriptionExceedsMaxBadgeNumberStringResource(int stringsResource) {
        this.overridingState.contentDescriptionExceedsMaxBadgeNumberRes = stringsResource;
        this.currentState.contentDescriptionExceedsMaxBadgeNumberRes = stringsResource;
    }

    Locale getNumberLocale() {
        return this.currentState.numberLocale;
    }

    void setNumberLocale(Locale locale) {
        this.overridingState.numberLocale = locale;
        this.currentState.numberLocale = locale;
    }

    boolean isAutoAdjustedToGrandparentBounds() {
        return this.currentState.autoAdjustToWithinGrandparentBounds.booleanValue();
    }

    void setAutoAdjustToGrandparentBounds(boolean autoAdjustToGrandparentBounds) {
        this.overridingState.autoAdjustToWithinGrandparentBounds = Boolean.valueOf(autoAdjustToGrandparentBounds);
        this.currentState.autoAdjustToWithinGrandparentBounds = Boolean.valueOf(autoAdjustToGrandparentBounds);
    }

    private static int readColorFromAttributes(Context context, TypedArray a, int index) {
        return MaterialResources.getColorStateList(context, a, index).getDefaultColor();
    }

    public static final class State implements Parcelable {
        private static final int BADGE_NUMBER_NONE = -1;
        public static final Parcelable.Creator<State> CREATOR = new Parcelable.Creator<State>() { // from class: com.google.android.material.badge.BadgeState.State.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public State[] newArray(int size) {
                return new State[size];
            }
        };
        private static final int NOT_SET = -2;
        private Integer additionalHorizontalOffset;
        private Integer additionalVerticalOffset;
        private int alpha;
        private Boolean autoAdjustToWithinGrandparentBounds;
        private Integer backgroundColor;
        private Integer badgeGravity;
        private Integer badgeHorizontalPadding;
        private int badgeResId;
        private Integer badgeShapeAppearanceOverlayResId;
        private Integer badgeShapeAppearanceResId;
        private Integer badgeTextAppearanceResId;
        private Integer badgeTextColor;
        private Integer badgeVerticalPadding;
        private Integer badgeWithTextShapeAppearanceOverlayResId;
        private Integer badgeWithTextShapeAppearanceResId;
        private int contentDescriptionExceedsMaxBadgeNumberRes;
        private CharSequence contentDescriptionForText;
        private CharSequence contentDescriptionNumberless;
        private int contentDescriptionQuantityStrings;
        private Integer horizontalOffsetWithText;
        private Integer horizontalOffsetWithoutText;
        private Boolean isVisible;
        private Integer largeFontVerticalOffsetAdjustment;
        private int maxCharacterCount;
        private int maxNumber;
        private int number;
        private Locale numberLocale;
        private String text;
        private Integer verticalOffsetWithText;
        private Integer verticalOffsetWithoutText;

        public State() {
            this.alpha = 255;
            this.number = -2;
            this.maxCharacterCount = -2;
            this.maxNumber = -2;
            this.isVisible = true;
        }

        State(Parcel in) {
            this.alpha = 255;
            this.number = -2;
            this.maxCharacterCount = -2;
            this.maxNumber = -2;
            this.isVisible = true;
            this.badgeResId = in.readInt();
            this.backgroundColor = (Integer) in.readSerializable();
            this.badgeTextColor = (Integer) in.readSerializable();
            this.badgeTextAppearanceResId = (Integer) in.readSerializable();
            this.badgeShapeAppearanceResId = (Integer) in.readSerializable();
            this.badgeShapeAppearanceOverlayResId = (Integer) in.readSerializable();
            this.badgeWithTextShapeAppearanceResId = (Integer) in.readSerializable();
            this.badgeWithTextShapeAppearanceOverlayResId = (Integer) in.readSerializable();
            this.alpha = in.readInt();
            this.text = in.readString();
            this.number = in.readInt();
            this.maxCharacterCount = in.readInt();
            this.maxNumber = in.readInt();
            this.contentDescriptionForText = in.readString();
            this.contentDescriptionNumberless = in.readString();
            this.contentDescriptionQuantityStrings = in.readInt();
            this.badgeGravity = (Integer) in.readSerializable();
            this.badgeHorizontalPadding = (Integer) in.readSerializable();
            this.badgeVerticalPadding = (Integer) in.readSerializable();
            this.horizontalOffsetWithoutText = (Integer) in.readSerializable();
            this.verticalOffsetWithoutText = (Integer) in.readSerializable();
            this.horizontalOffsetWithText = (Integer) in.readSerializable();
            this.verticalOffsetWithText = (Integer) in.readSerializable();
            this.largeFontVerticalOffsetAdjustment = (Integer) in.readSerializable();
            this.additionalHorizontalOffset = (Integer) in.readSerializable();
            this.additionalVerticalOffset = (Integer) in.readSerializable();
            this.isVisible = (Boolean) in.readSerializable();
            this.numberLocale = (Locale) in.readSerializable();
            this.autoAdjustToWithinGrandparentBounds = (Boolean) in.readSerializable();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.badgeResId);
            dest.writeSerializable(this.backgroundColor);
            dest.writeSerializable(this.badgeTextColor);
            dest.writeSerializable(this.badgeTextAppearanceResId);
            dest.writeSerializable(this.badgeShapeAppearanceResId);
            dest.writeSerializable(this.badgeShapeAppearanceOverlayResId);
            dest.writeSerializable(this.badgeWithTextShapeAppearanceResId);
            dest.writeSerializable(this.badgeWithTextShapeAppearanceOverlayResId);
            dest.writeInt(this.alpha);
            dest.writeString(this.text);
            dest.writeInt(this.number);
            dest.writeInt(this.maxCharacterCount);
            dest.writeInt(this.maxNumber);
            dest.writeString(this.contentDescriptionForText != null ? this.contentDescriptionForText.toString() : null);
            dest.writeString(this.contentDescriptionNumberless != null ? this.contentDescriptionNumberless.toString() : null);
            dest.writeInt(this.contentDescriptionQuantityStrings);
            dest.writeSerializable(this.badgeGravity);
            dest.writeSerializable(this.badgeHorizontalPadding);
            dest.writeSerializable(this.badgeVerticalPadding);
            dest.writeSerializable(this.horizontalOffsetWithoutText);
            dest.writeSerializable(this.verticalOffsetWithoutText);
            dest.writeSerializable(this.horizontalOffsetWithText);
            dest.writeSerializable(this.verticalOffsetWithText);
            dest.writeSerializable(this.largeFontVerticalOffsetAdjustment);
            dest.writeSerializable(this.additionalHorizontalOffset);
            dest.writeSerializable(this.additionalVerticalOffset);
            dest.writeSerializable(this.isVisible);
            dest.writeSerializable(this.numberLocale);
            dest.writeSerializable(this.autoAdjustToWithinGrandparentBounds);
        }
    }
}
