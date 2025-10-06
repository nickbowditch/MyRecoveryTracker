package androidx.core.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import androidx.collection.LruCache;
import androidx.core.content.res.FontResourcesParserCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.provider.FontRequest;
import androidx.core.provider.FontsContractCompat;
import androidx.core.util.Preconditions;
import androidx.tracing.Trace;
import java.util.List;

/* loaded from: classes.dex */
public class TypefaceCompat {
    public static final boolean DOWNLOADABLE_FALLBACK_DEBUG = false;
    public static final boolean DOWNLOADABLE_FONT_TRACING = true;
    private static final LruCache<String, Typeface> sTypefaceCache;
    private static final TypefaceCompatBaseImpl sTypefaceCompatImpl;

    static {
        Trace.beginSection("TypefaceCompat static init");
        if (Build.VERSION.SDK_INT >= 29) {
            sTypefaceCompatImpl = new TypefaceCompatApi29Impl();
        } else if (Build.VERSION.SDK_INT >= 28) {
            sTypefaceCompatImpl = new TypefaceCompatApi28Impl();
        } else if (Build.VERSION.SDK_INT >= 26) {
            sTypefaceCompatImpl = new TypefaceCompatApi26Impl();
        } else if (TypefaceCompatApi24Impl.isUsable()) {
            sTypefaceCompatImpl = new TypefaceCompatApi24Impl();
        } else {
            sTypefaceCompatImpl = new TypefaceCompatApi21Impl();
        }
        sTypefaceCache = new LruCache<>(16);
        Trace.endSection();
    }

    private TypefaceCompat() {
    }

    public static Typeface findFromCache(Resources resources, int id, String path, int cookie, int style) {
        return sTypefaceCache.get(createResourceUid(resources, id, path, cookie, style));
    }

    @Deprecated
    public static Typeface findFromCache(Resources resources, int id, int style) {
        return findFromCache(resources, id, null, 0, style);
    }

    private static String createResourceUid(Resources resources, int id, String path, int cookie, int style) {
        return resources.getResourcePackageName(id) + '-' + path + '-' + cookie + '-' + id + '-' + style;
    }

    private static Typeface getSystemFontFamily(String familyName) {
        if (familyName == null || familyName.isEmpty()) {
            return null;
        }
        Typeface typeface = Typeface.create(familyName, 0);
        Typeface defaultTypeface = Typeface.create(Typeface.DEFAULT, 0);
        if (typeface == null || typeface.equals(defaultTypeface)) {
            return null;
        }
        return typeface;
    }

    public static Typeface createFromResourcesFamilyXml(Context context, FontResourcesParserCompat.FamilyResourceEntry entry, Resources resources, int id, String path, int cookie, int style, ResourcesCompat.FontCallback fontCallback, Handler handler, boolean isRequestFromLayoutInflator) throws NoSuchFieldException {
        Typeface typeface;
        if (entry instanceof FontResourcesParserCompat.ProviderResourceEntry) {
            FontResourcesParserCompat.ProviderResourceEntry providerEntry = (FontResourcesParserCompat.ProviderResourceEntry) entry;
            Typeface fontFamilyTypeface = getSystemFontFamily(providerEntry.getSystemFontFamilyName());
            if (fontFamilyTypeface != null) {
                if (fontCallback != null) {
                    fontCallback.callbackSuccessAsync(fontFamilyTypeface, handler);
                }
                return fontFamilyTypeface;
            }
            boolean z = true;
            if (isRequestFromLayoutInflator) {
                if (providerEntry.getFetchStrategy() != 0) {
                    z = false;
                }
            } else if (fontCallback != null) {
                z = false;
            }
            boolean isBlocking = z;
            int timeout = isRequestFromLayoutInflator ? providerEntry.getTimeout() : -1;
            Handler newHandler = ResourcesCompat.FontCallback.getHandler(handler);
            ResourcesCallbackAdapter newCallback = new ResourcesCallbackAdapter(fontCallback);
            List<FontRequest> requests = providerEntry.getFallbackRequest() != null ? TypefaceCompat$$ExternalSyntheticBackport2.m(new Object[]{providerEntry.getRequest(), providerEntry.getFallbackRequest()}) : TypefaceCompat$$ExternalSyntheticBackport2.m(new Object[]{providerEntry.getRequest()});
            typeface = FontsContractCompat.requestFont(context, requests, style, isBlocking, timeout, newHandler, newCallback);
        } else {
            typeface = sTypefaceCompatImpl.createFromFontFamilyFilesResourceEntry(context, (FontResourcesParserCompat.FontFamilyFilesResourceEntry) entry, resources, style);
            if (fontCallback != null) {
                if (typeface == null) {
                    fontCallback.callbackFailAsync(-3, handler);
                } else {
                    fontCallback.callbackSuccessAsync(typeface, handler);
                }
            }
        }
        if (typeface != null) {
            sTypefaceCache.put(createResourceUid(resources, id, path, cookie, style), typeface);
        }
        return typeface;
    }

    @Deprecated
    public static Typeface createFromResourcesFamilyXml(Context context, FontResourcesParserCompat.FamilyResourceEntry entry, Resources resources, int id, int style, ResourcesCompat.FontCallback fontCallback, Handler handler, boolean isRequestFromLayoutInflator) {
        return createFromResourcesFamilyXml(context, entry, resources, id, null, 0, style, fontCallback, handler, isRequestFromLayoutInflator);
    }

    public static Typeface createFromResourcesFontFile(Context context, Resources resources, int id, String path, int cookie, int style) {
        Typeface typeface = sTypefaceCompatImpl.createFromResourcesFontFile(context, resources, id, path, style);
        if (typeface != null) {
            String resourceUid = createResourceUid(resources, id, path, cookie, style);
            sTypefaceCache.put(resourceUid, typeface);
        }
        return typeface;
    }

    @Deprecated
    public static Typeface createFromResourcesFontFile(Context context, Resources resources, int id, String path, int style) {
        return createFromResourcesFontFile(context, resources, id, path, 0, style);
    }

    public static Typeface createFromFontInfo(Context context, CancellationSignal cancellationSignal, FontsContractCompat.FontInfo[] fonts, int style) {
        Trace.beginSection("TypefaceCompat.createFromFontInfo");
        try {
            return sTypefaceCompatImpl.createFromFontInfo(context, cancellationSignal, fonts, style);
        } finally {
            Trace.endSection();
        }
    }

    public static Typeface createFromFontInfoWithFallback(Context context, CancellationSignal cancellationSignal, List<FontsContractCompat.FontInfo[]> fonts, int style) {
        Trace.beginSection("TypefaceCompat.createFromFontInfoWithFallback");
        try {
            return sTypefaceCompatImpl.createFromFontInfoWithFallback(context, cancellationSignal, fonts, style);
        } finally {
            Trace.endSection();
        }
    }

    private static Typeface getBestFontFromFamily(Context context, Typeface typeface, int style) throws NoSuchFieldException {
        FontResourcesParserCompat.FontFamilyFilesResourceEntry families = sTypefaceCompatImpl.getFontFamily(typeface);
        if (families == null) {
            return null;
        }
        return sTypefaceCompatImpl.createFromFontFamilyFilesResourceEntry(context, families, context.getResources(), style);
    }

    public static Typeface create(Context context, Typeface family, int style) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        return Typeface.create(family, style);
    }

    public static Typeface create(Context context, Typeface family, int weight, boolean italic) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        Preconditions.checkArgumentInRange(weight, 1, 1000, "weight");
        if (family == null) {
            family = Typeface.DEFAULT;
        }
        return sTypefaceCompatImpl.createWeightStyle(context, family, weight, italic);
    }

    public static void clearCache() {
        sTypefaceCache.evictAll();
    }

    public static class ResourcesCallbackAdapter extends FontsContractCompat.FontRequestCallback {
        private ResourcesCompat.FontCallback mFontCallback;

        public ResourcesCallbackAdapter(ResourcesCompat.FontCallback fontCallback) {
            this.mFontCallback = fontCallback;
        }

        @Override // androidx.core.provider.FontsContractCompat.FontRequestCallback
        public void onTypefaceRetrieved(Typeface typeface) {
            if (this.mFontCallback != null) {
                this.mFontCallback.m39x46c88379(typeface);
            }
        }

        @Override // androidx.core.provider.FontsContractCompat.FontRequestCallback
        public void onTypefaceRequestFailed(int reason) {
            if (this.mFontCallback != null) {
                this.mFontCallback.m38xb24343b7(reason);
            }
        }
    }
}
