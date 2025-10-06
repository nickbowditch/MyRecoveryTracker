package com.google.android.gms.common.images;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.ImagesContract;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: com.google.android.gms:play-services-base@@18.1.0 */
/* loaded from: classes.dex */
public final class WebImage extends AbstractSafeParcelable {
    public static final Parcelable.Creator<WebImage> CREATOR = new zah();
    final int zaa;
    private final Uri zab;
    private final int zac;
    private final int zad;

    WebImage(int i, Uri uri, int i2, int i3) {
        this.zaa = i;
        this.zab = uri;
        this.zac = i2;
        this.zad = i3;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !(other instanceof WebImage)) {
            return false;
        }
        WebImage webImage = (WebImage) other;
        return Objects.equal(this.zab, webImage.zab) && this.zac == webImage.zac && this.zad == webImage.zad;
    }

    public int getHeight() {
        return this.zad;
    }

    public Uri getUrl() {
        return this.zab;
    }

    public int getWidth() {
        return this.zac;
    }

    public int hashCode() {
        return Objects.hashCode(this.zab, Integer.valueOf(this.zac), Integer.valueOf(this.zad));
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(ImagesContract.URL, this.zab.toString());
            jSONObject.put("width", this.zac);
            jSONObject.put("height", this.zad);
        } catch (JSONException e) {
        }
        return jSONObject;
    }

    public String toString() {
        return String.format(Locale.US, "Image %dx%d %s", Integer.valueOf(this.zac), Integer.valueOf(this.zad), this.zab.toString());
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        int iBeginObjectHeader = SafeParcelWriter.beginObjectHeader(out);
        SafeParcelWriter.writeInt(out, 1, this.zaa);
        SafeParcelWriter.writeParcelable(out, 2, getUrl(), flags, false);
        SafeParcelWriter.writeInt(out, 3, getWidth());
        SafeParcelWriter.writeInt(out, 4, getHeight());
        SafeParcelWriter.finishObjectHeader(out, iBeginObjectHeader);
    }

    public WebImage(Uri url) throws IllegalArgumentException {
        this(url, 0, 0);
    }

    public WebImage(Uri url, int width, int height) throws IllegalArgumentException {
        this(1, url, width, height);
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("width and height must not be negative");
        }
    }

    /* JADX WARN: Illegal instructions before constructor call */
    public WebImage(JSONObject json) throws IllegalArgumentException {
        Uri uri = Uri.EMPTY;
        if (json.has(ImagesContract.URL)) {
            try {
                uri = Uri.parse(json.getString(ImagesContract.URL));
            } catch (JSONException e) {
            }
        }
        this(uri, json.optInt("width", 0), json.optInt("height", 0));
    }
}
