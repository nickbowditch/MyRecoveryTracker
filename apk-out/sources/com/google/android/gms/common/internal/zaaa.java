package com.google.android.gms.common.internal;

import android.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.gms.common.util.DeviceProperties;

/* compiled from: com.google.android.gms:play-services-base@@18.1.0 */
/* loaded from: classes.dex */
public final class zaaa extends Button {
    public zaaa(Context context, AttributeSet attributeSet) {
        super(context, null, R.attr.buttonStyle);
    }

    private static final int zab(int i, int i2, int i3, int i4) {
        switch (i) {
            case 0:
                return i2;
            case 1:
                return i3;
            case 2:
                return i4;
            default:
                throw new IllegalStateException("Unknown color scheme: " + i);
        }
    }

    public final void zaa(Resources resources, int i, int i2) {
        setTypeface(Typeface.DEFAULT_BOLD);
        setTextSize(14.0f);
        int i3 = (int) ((resources.getDisplayMetrics().density * 48.0f) + 0.5f);
        setMinHeight(i3);
        setMinWidth(i3);
        int iZab = zab(i2, com.google.android.gms.base.R.drawable.common_google_signin_btn_icon_dark, com.google.android.gms.base.R.drawable.common_google_signin_btn_icon_light, com.google.android.gms.base.R.drawable.common_google_signin_btn_icon_light);
        int iZab2 = zab(i2, com.google.android.gms.base.R.drawable.common_google_signin_btn_text_dark, com.google.android.gms.base.R.drawable.common_google_signin_btn_text_light, com.google.android.gms.base.R.drawable.common_google_signin_btn_text_light);
        switch (i) {
            case 0:
            case 1:
                iZab = iZab2;
                break;
            case 2:
                break;
            default:
                throw new IllegalStateException("Unknown button size: " + i);
        }
        Drawable drawableWrap = DrawableCompat.wrap(resources.getDrawable(iZab));
        DrawableCompat.setTintList(drawableWrap, resources.getColorStateList(com.google.android.gms.base.R.color.common_google_signin_btn_tint));
        DrawableCompat.setTintMode(drawableWrap, PorterDuff.Mode.SRC_ATOP);
        setBackgroundDrawable(drawableWrap);
        setTextColor((ColorStateList) Preconditions.checkNotNull(resources.getColorStateList(zab(i2, com.google.android.gms.base.R.color.common_google_signin_btn_text_dark, com.google.android.gms.base.R.color.common_google_signin_btn_text_light, com.google.android.gms.base.R.color.common_google_signin_btn_text_light))));
        switch (i) {
            case 0:
                setText(resources.getString(com.google.android.gms.base.R.string.common_signin_button_text));
                break;
            case 1:
                setText(resources.getString(com.google.android.gms.base.R.string.common_signin_button_text_long));
                break;
            case 2:
                setText((CharSequence) null);
                break;
            default:
                throw new IllegalStateException("Unknown button size: " + i);
        }
        setTransformationMethod(null);
        if (DeviceProperties.isWearable(getContext())) {
            setGravity(19);
        }
    }
}
