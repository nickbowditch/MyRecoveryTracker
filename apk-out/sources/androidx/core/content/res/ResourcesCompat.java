package androidx.core.content.res;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import androidx.core.util.ObjectsCompat;
import androidx.core.util.Preconditions;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.WeakHashMap;
import org.xmlpull.v1.XmlPullParser;

/* loaded from: classes.dex */
public final class ResourcesCompat {
    public static final int ID_NULL = 0;
    private static final String TAG = "ResourcesCompat";
    private static final ThreadLocal<TypedValue> sTempTypedValue = new ThreadLocal<>();
    private static final WeakHashMap<ColorStateListCacheKey, SparseArray<ColorStateListCacheEntry>> sColorStateCaches = new WeakHashMap<>(0);
    private static final Object sColorStateCacheLock = new Object();

    public static void clearCachesForTheme(Resources.Theme theme) {
        synchronized (sColorStateCacheLock) {
            Iterator<ColorStateListCacheKey> keys = sColorStateCaches.keySet().iterator();
            while (keys.hasNext()) {
                ColorStateListCacheKey key = keys.next();
                if (key != null && theme.equals(key.mTheme)) {
                    keys.remove();
                }
            }
        }
    }

    public static Drawable getDrawable(Resources res, int id, Resources.Theme theme) throws Resources.NotFoundException {
        return Api21Impl.getDrawable(res, id, theme);
    }

    public static Drawable getDrawableForDensity(Resources res, int id, int density, Resources.Theme theme) throws Resources.NotFoundException {
        return Api21Impl.getDrawableForDensity(res, id, density, theme);
    }

    public static int getColor(Resources res, int id, Resources.Theme theme) throws Resources.NotFoundException {
        return Api23Impl.getColor(res, id, theme);
    }

    public static ColorStateList getColorStateList(Resources res, int id, Resources.Theme theme) throws Resources.NotFoundException {
        ColorStateListCacheKey key = new ColorStateListCacheKey(res, theme);
        ColorStateList csl = getCachedColorStateList(key, id);
        if (csl != null) {
            return csl;
        }
        ColorStateList csl2 = inflateColorStateList(res, id, theme);
        if (csl2 != null) {
            addColorStateListToCache(key, id, csl2, theme);
            return csl2;
        }
        return Api23Impl.getColorStateList(res, id, theme);
    }

    private static ColorStateList inflateColorStateList(Resources resources, int resId, Resources.Theme theme) throws Resources.NotFoundException {
        if (isColorInt(resources, resId)) {
            return null;
        }
        XmlPullParser xml = resources.getXml(resId);
        try {
            return ColorStateListInflaterCompat.createFromXml(resources, xml, theme);
        } catch (Exception e) {
            Log.w(TAG, "Failed to inflate ColorStateList, leaving it to the framework", e);
            return null;
        }
    }

    private static ColorStateList getCachedColorStateList(ColorStateListCacheKey key, int resId) {
        ColorStateListCacheEntry entry;
        synchronized (sColorStateCacheLock) {
            SparseArray<ColorStateListCacheEntry> entries = sColorStateCaches.get(key);
            if (entries != null && entries.size() > 0 && (entry = entries.get(resId)) != null) {
                if (entry.mConfiguration.equals(key.mResources.getConfiguration()) && ((key.mTheme == null && entry.mThemeHash == 0) || (key.mTheme != null && entry.mThemeHash == key.mTheme.hashCode()))) {
                    return entry.mValue;
                }
                entries.remove(resId);
            }
            return null;
        }
    }

    private static void addColorStateListToCache(ColorStateListCacheKey key, int resId, ColorStateList value, Resources.Theme theme) {
        synchronized (sColorStateCacheLock) {
            SparseArray<ColorStateListCacheEntry> entries = sColorStateCaches.get(key);
            if (entries == null) {
                entries = new SparseArray<>();
                sColorStateCaches.put(key, entries);
            }
            entries.append(resId, new ColorStateListCacheEntry(value, key.mResources.getConfiguration(), theme));
        }
    }

    private static boolean isColorInt(Resources resources, int resId) throws Resources.NotFoundException {
        TypedValue value = getTypedValue();
        resources.getValue(resId, value, true);
        return value.type >= 28 && value.type <= 31;
    }

    private static TypedValue getTypedValue() {
        TypedValue tv = sTempTypedValue.get();
        if (tv == null) {
            TypedValue tv2 = new TypedValue();
            sTempTypedValue.set(tv2);
            return tv2;
        }
        return tv;
    }

    private static final class ColorStateListCacheKey {
        final Resources mResources;
        final Resources.Theme mTheme;

        ColorStateListCacheKey(Resources resources, Resources.Theme theme) {
            this.mResources = resources;
            this.mTheme = theme;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ColorStateListCacheKey that = (ColorStateListCacheKey) o;
            if (this.mResources.equals(that.mResources) && ObjectsCompat.equals(this.mTheme, that.mTheme)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return ObjectsCompat.hash(this.mResources, this.mTheme);
        }
    }

    private static class ColorStateListCacheEntry {
        final Configuration mConfiguration;
        final int mThemeHash;
        final ColorStateList mValue;

        ColorStateListCacheEntry(ColorStateList value, Configuration configuration, Resources.Theme theme) {
            this.mValue = value;
            this.mConfiguration = configuration;
            this.mThemeHash = theme == null ? 0 : theme.hashCode();
        }
    }

    public static float getFloat(Resources res, int id) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= 29) {
            return Api29Impl.getFloat(res, id);
        }
        TypedValue value = getTypedValue();
        res.getValue(id, value, true);
        if (value.type == 4) {
            return value.getFloat();
        }
        throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
    }

    public static Typeface getFont(Context context, int id) throws Resources.NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, id, new TypedValue(), 0, null, null, false, false);
    }

    public static Typeface getCachedFont(Context context, int id) throws Resources.NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, id, new TypedValue(), 0, null, null, false, true);
    }

    public static abstract class FontCallback {
        /* renamed from: onFontRetrievalFailed, reason: merged with bridge method [inline-methods] */
        public abstract void m38xb24343b7(int i);

        /* renamed from: onFontRetrieved, reason: merged with bridge method [inline-methods] */
        public abstract void m39x46c88379(Typeface typeface);

        public final void callbackSuccessAsync(final Typeface typeface, Handler handler) {
            getHandler(handler).post(new Runnable() { // from class: androidx.core.content.res.ResourcesCompat$FontCallback$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m39x46c88379(typeface);
                }
            });
        }

        public final void callbackFailAsync(final int reason, Handler handler) {
            getHandler(handler).post(new Runnable() { // from class: androidx.core.content.res.ResourcesCompat$FontCallback$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m38xb24343b7(reason);
                }
            });
        }

        public static Handler getHandler(Handler handler) {
            return handler == null ? new Handler(Looper.getMainLooper()) : handler;
        }
    }

    public static void getFont(Context context, int id, FontCallback fontCallback, Handler handler) throws Resources.NotFoundException {
        Preconditions.checkNotNull(fontCallback);
        if (context.isRestricted()) {
            fontCallback.callbackFailAsync(-4, handler);
        } else {
            loadFont(context, id, new TypedValue(), 0, fontCallback, handler, false, false);
        }
    }

    public static Typeface getFont(Context context, int id, TypedValue value, int style, FontCallback fontCallback) throws Resources.NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, id, value, style, fontCallback, null, true, false);
    }

    private static Typeface loadFont(Context context, int id, TypedValue value, int style, FontCallback fontCallback, Handler handler, boolean isRequestFromLayoutInflator, boolean isCachedOnly) throws Resources.NotFoundException {
        Resources resources = context.getResources();
        resources.getValue(id, value, true);
        Typeface typeface = loadFont(context, resources, value, id, style, fontCallback, handler, isRequestFromLayoutInflator, isCachedOnly);
        if (typeface == null && fontCallback == null && !isCachedOnly) {
            throw new Resources.NotFoundException("Font resource ID #0x" + Integer.toHexString(id) + " could not be retrieved.");
        }
        return typeface;
    }

    /* JADX WARN: Removed duplicated region for block: B:50:0x00d2  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private static android.graphics.Typeface loadFont(android.content.Context r16, android.content.res.Resources r17, android.util.TypedValue r18, int r19, int r20, androidx.core.content.res.ResourcesCompat.FontCallback r21, android.os.Handler r22, boolean r23, boolean r24) throws android.content.res.Resources.NotFoundException {
        /*
            Method dump skipped, instructions count: 267
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.core.content.res.ResourcesCompat.loadFont(android.content.Context, android.content.res.Resources, android.util.TypedValue, int, int, androidx.core.content.res.ResourcesCompat$FontCallback, android.os.Handler, boolean, boolean):android.graphics.Typeface");
    }

    static class Api29Impl {
        private Api29Impl() {
        }

        static float getFloat(Resources res, int id) {
            return res.getFloat(id);
        }
    }

    static class Api23Impl {
        private Api23Impl() {
        }

        static ColorStateList getColorStateList(Resources res, int id, Resources.Theme theme) {
            return res.getColorStateList(id, theme);
        }

        static int getColor(Resources resources, int id, Resources.Theme theme) {
            return resources.getColor(id, theme);
        }
    }

    static class Api21Impl {
        private Api21Impl() {
        }

        static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
            return resources.getDrawable(id, theme);
        }

        static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
            return resources.getDrawableForDensity(id, density, theme);
        }
    }

    private ResourcesCompat() {
    }

    public static final class ThemeCompat {
        private ThemeCompat() {
        }

        public static void rebase(Resources.Theme theme) {
            if (Build.VERSION.SDK_INT >= 29) {
                Api29Impl.rebase(theme);
            } else {
                Api23Impl.rebase(theme);
            }
        }

        static class Api29Impl {
            private Api29Impl() {
            }

            static void rebase(Resources.Theme theme) {
                theme.rebase();
            }
        }

        static class Api23Impl {
            private static Method sRebaseMethod;
            private static boolean sRebaseMethodFetched;
            private static final Object sRebaseMethodLock = new Object();

            private Api23Impl() {
            }

            /* JADX WARN: Removed duplicated region for block: B:30:0x0029 A[EXC_TOP_SPLITTER, SYNTHETIC] */
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct add '--show-bad-code' argument
            */
            static void rebase(android.content.res.Resources.Theme r6) {
                /*
                    java.lang.Object r0 = androidx.core.content.res.ResourcesCompat.ThemeCompat.Api23Impl.sRebaseMethodLock
                    monitor-enter(r0)
                    boolean r1 = androidx.core.content.res.ResourcesCompat.ThemeCompat.Api23Impl.sRebaseMethodFetched     // Catch: java.lang.Throwable -> L40
                    r2 = 0
                    if (r1 != 0) goto L25
                    r1 = 1
                    java.lang.Class<android.content.res.Resources$Theme> r3 = android.content.res.Resources.Theme.class
                    java.lang.String r4 = "rebase"
                    java.lang.Class[] r5 = new java.lang.Class[r2]     // Catch: java.lang.NoSuchMethodException -> L1b java.lang.Throwable -> L40
                    java.lang.reflect.Method r3 = r3.getDeclaredMethod(r4, r5)     // Catch: java.lang.NoSuchMethodException -> L1b java.lang.Throwable -> L40
                    androidx.core.content.res.ResourcesCompat.ThemeCompat.Api23Impl.sRebaseMethod = r3     // Catch: java.lang.NoSuchMethodException -> L1b java.lang.Throwable -> L40
                    java.lang.reflect.Method r3 = androidx.core.content.res.ResourcesCompat.ThemeCompat.Api23Impl.sRebaseMethod     // Catch: java.lang.NoSuchMethodException -> L1b java.lang.Throwable -> L40
                    r3.setAccessible(r1)     // Catch: java.lang.NoSuchMethodException -> L1b java.lang.Throwable -> L40
                    goto L23
                L1b:
                    r3 = move-exception
                    java.lang.String r4 = "ResourcesCompat"
                    java.lang.String r5 = "Failed to retrieve rebase() method"
                    android.util.Log.i(r4, r5, r3)     // Catch: java.lang.Throwable -> L40
                L23:
                    androidx.core.content.res.ResourcesCompat.ThemeCompat.Api23Impl.sRebaseMethodFetched = r1     // Catch: java.lang.Throwable -> L40
                L25:
                    java.lang.reflect.Method r1 = androidx.core.content.res.ResourcesCompat.ThemeCompat.Api23Impl.sRebaseMethod     // Catch: java.lang.Throwable -> L40
                    if (r1 == 0) goto L3e
                    java.lang.reflect.Method r1 = androidx.core.content.res.ResourcesCompat.ThemeCompat.Api23Impl.sRebaseMethod     // Catch: java.lang.reflect.InvocationTargetException -> L31 java.lang.IllegalAccessException -> L33 java.lang.Throwable -> L40
                    java.lang.Object[] r2 = new java.lang.Object[r2]     // Catch: java.lang.reflect.InvocationTargetException -> L31 java.lang.IllegalAccessException -> L33 java.lang.Throwable -> L40
                    r1.invoke(r6, r2)     // Catch: java.lang.reflect.InvocationTargetException -> L31 java.lang.IllegalAccessException -> L33 java.lang.Throwable -> L40
                    goto L3e
                L31:
                    r1 = move-exception
                    goto L34
                L33:
                    r1 = move-exception
                L34:
                    java.lang.String r2 = "ResourcesCompat"
                    java.lang.String r3 = "Failed to invoke rebase() method via reflection"
                    android.util.Log.i(r2, r3, r1)     // Catch: java.lang.Throwable -> L40
                    r2 = 0
                    androidx.core.content.res.ResourcesCompat.ThemeCompat.Api23Impl.sRebaseMethod = r2     // Catch: java.lang.Throwable -> L40
                L3e:
                    monitor-exit(r0)     // Catch: java.lang.Throwable -> L40
                    return
                L40:
                    r1 = move-exception
                    monitor-exit(r0)     // Catch: java.lang.Throwable -> L40
                    throw r1
                */
                throw new UnsupportedOperationException("Method not decompiled: androidx.core.content.res.ResourcesCompat.ThemeCompat.Api23Impl.rebase(android.content.res.Resources$Theme):void");
            }
        }
    }
}
