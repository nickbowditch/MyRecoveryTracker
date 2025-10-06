package com.nick.myrecoverytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/* compiled from: MetricsStore.kt */
@Metadata(d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0014\n\u0002\u0018\u0002\n\u0002\b\u0002\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u000e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u0011J\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00050\u00152\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0016\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0010\u001a\u00020\u0011J\u0010\u0010\u0019\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u0016\u0010\u001a\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u001b\u001a\u00020\u0005J\u000e\u0010\u001c\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010\u001d\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u0011J\u0016\u0010\u001e\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u001f\u001a\u00020 J\u000e\u0010!\u001a\u00020\"2\u0006\u0010\u0010\u001a\u00020\u0011J\u0018\u0010#\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010$\u001a\u00020\u0005J\u0014\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00050\u00152\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u0010&\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u0011J\u0016\u0010'\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010(\u001a\u00020\"J.\u0010)\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010*\u001a\u00020\u00052\u0006\u0010+\u001a\u00020\u00052\u0006\u0010,\u001a\u00020\u00052\u0006\u0010-\u001a\u00020\u0005J\u000e\u0010.\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u0011J&\u0010/\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u00100\u001a\u00020\u00052\u0006\u00101\u001a\u00020\u00052\u0006\u00102\u001a\u00020\u0018J\u001e\u00103\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u00104\u001a\u00020\u00052\u0006\u00105\u001a\u00020\"J \u00106\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\"070\u00152\u0006\u0010\u0010\u001a\u00020\u0011J\u000e\u00108\u001a\u00020\"2\u0006\u0010\u0010\u001a\u00020\u0011R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000¨\u00069"}, d2 = {"Lcom/nick/myrecoverytracker/MetricsStore;", "", "<init>", "()V", "UNLOCK_LOG_FILE", "", "UNLOCK_LOG_FILE_LEGACY", "SCREEN_LOG_FILE", "ENTROPY_LOG_FILE", "LOCATION_LOG_FILE", "LUX_LOG_FILE", "NOTIF_LOG_FILE", "WIFI_LOG_FILE", "DISTANCE_LOG_FILE", "unlockFile", "Ljava/io/File;", "context", "Landroid/content/Context;", "saveUnlock", "", "getUnlockLog", "", "clearUnlockLog", "summarizeDailyUnlocks", "", "screenFile", "saveScreenEvent", NotificationCompat.CATEGORY_EVENT, "saveScreenOn", "saveScreenOff", "saveAppUsageEntropy", "entropy", "", "getAppUsageEntropy", "", "exportFile", "filename", "getLocationLog", "clearLocationLog", "saveAmbientLux", "lux", "appendNotificationLog", "timestamp", "packageName", "title", "text", "clearNotificationLog", "saveWifiNetworksLog", "ssid", "bssid", "level", "saveDailyDistance", "date", "distanceKm", "getDailyDistanceLog", "Lkotlin/Pair;", "summarizeDailyDistance", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class MetricsStore {
    private static final String DISTANCE_LOG_FILE = "daily_distance_log.csv";
    private static final String ENTROPY_LOG_FILE = "usage_entropy.csv";
    public static final MetricsStore INSTANCE = new MetricsStore();
    private static final String LOCATION_LOG_FILE = "location_log.csv";
    private static final String LUX_LOG_FILE = "ambient_lux.csv";
    private static final String NOTIF_LOG_FILE = "notification_log.csv";
    private static final String SCREEN_LOG_FILE = "screen_log.csv";
    private static final String UNLOCK_LOG_FILE = "unlock_log.csv";
    private static final String UNLOCK_LOG_FILE_LEGACY = "unlocks_log.csv";
    private static final String WIFI_LOG_FILE = "wifi_log.csv";

    private MetricsStore() {
    }

    private final File unlockFile(Context context) {
        File dir = context.getFilesDir();
        File current = new File(dir, UNLOCK_LOG_FILE);
        File legacy = new File(dir, UNLOCK_LOG_FILE_LEGACY);
        if (legacy.exists()) {
            try {
                if (!current.exists()) {
                    FilesKt.copyTo$default(legacy, current, false, 0, 4, null);
                }
                legacy.delete();
            } catch (Exception e) {
                Log.e("MetricsStore", "Unlock log migration failed", e);
            }
        }
        return current;
    }

    public final void saveUnlock(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        try {
            File file = unlockFile(context);
            FileWriter fileWriter = new FileWriter(file, true);
            try {
                Appendable appendableAppend = fileWriter.append((CharSequence) (timestamp + ",UNLOCK"));
                Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
                CloseableKt.closeFinally(fileWriter, null);
                Log.i("MetricsStore", "Unlock logged: " + timestamp);
            } finally {
            }
        } catch (Exception e) {
            Log.e("MetricsStore", "Failed to log unlock", e);
        }
    }

    public final List<String> getUnlockLog(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        File file = unlockFile(context);
        return file.exists() ? FilesKt.readLines$default(file, null, 1, null) : CollectionsKt.emptyList();
    }

    public final void clearUnlockLog(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        File file = unlockFile(context);
        if (file.exists()) {
            file.delete();
        }
    }

    public final int summarizeDailyUnlocks(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        Iterable<String> unlockLog = getUnlockLog(context);
        if ((unlockLog instanceof Collection) && ((Collection) unlockLog).isEmpty()) {
            return 0;
        }
        int i = 0;
        for (String str : unlockLog) {
            Intrinsics.checkNotNull(today);
            if (StringsKt.startsWith$default(str, today, false, 2, (Object) null) && (i = i + 1) < 0) {
                CollectionsKt.throwCountOverflow();
            }
        }
        return i;
    }

    private final File screenFile(Context context) {
        return new File(context.getFilesDir(), SCREEN_LOG_FILE);
    }

    public final void saveScreenEvent(Context context, String event) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(event, "event");
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        File f = screenFile(context);
        try {
            if (!f.exists()) {
                File parentFile = f.getParentFile();
                if (parentFile != null) {
                    parentFile.mkdirs();
                }
                FilesKt.writeText$default(f, "timestamp,event\n", null, 2, null);
            }
            FileWriter fileWriter = new FileWriter(f, true);
            try {
                Appendable appendableAppend = fileWriter.append((CharSequence) (ts + "," + event));
                Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
                CloseableKt.closeFinally(fileWriter, null);
                Log.i("MetricsStore", "Screen logged: " + event + " at " + ts);
            } finally {
            }
        } catch (Exception e) {
            Log.e("MetricsStore", "Failed to write screen_log.csv", e);
        }
    }

    public final void saveScreenOn(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        saveScreenEvent(context, "ON");
    }

    public final void saveScreenOff(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        saveScreenEvent(context, "SCREEN_OFF");
    }

    public final void saveAppUsageEntropy(Context context, double entropy) {
        Intrinsics.checkNotNullParameter(context, "context");
        SharedPreferences prefs = context.getSharedPreferences("metrics", 0);
        prefs.edit().putFloat("entropy", (float) entropy).apply();
        Log.i("MetricsStore", "Saved app usage entropy: " + entropy);
        try {
            File file = new File(context.getFilesDir(), ENTROPY_LOG_FILE);
            FileWriter fileWriter = new FileWriter(file, true);
            try {
                Appendable appendableAppend = fileWriter.append((CharSequence) (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "," + entropy));
                Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
                CloseableKt.closeFinally(fileWriter, null);
            } finally {
            }
        } catch (Exception e) {
            Log.e("MetricsStore", "Failed to write entropy log", e);
        }
    }

    public final float getAppUsageEntropy(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        SharedPreferences prefs = context.getSharedPreferences("metrics", 0);
        return prefs.getFloat("entropy", -1.0f);
    }

    public final File exportFile(Context context, String filename) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(filename, "filename");
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public final List<String> getLocationLog(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        File file = new File(context.getFilesDir(), LOCATION_LOG_FILE);
        return file.exists() ? FilesKt.readLines$default(file, null, 1, null) : CollectionsKt.emptyList();
    }

    public final void clearLocationLog(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        File file = new File(context.getFilesDir(), LOCATION_LOG_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    public final void saveAmbientLux(Context context, float lux) {
        Intrinsics.checkNotNullParameter(context, "context");
        try {
            File file = new File(context.getFilesDir(), LUX_LOG_FILE);
            FileWriter fileWriter = new FileWriter(file, true);
            try {
                Appendable appendableAppend = fileWriter.append((CharSequence) (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "," + lux));
                Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
                CloseableKt.closeFinally(fileWriter, null);
                Log.i("MetricsStore", "Logged ambient lux: " + lux);
            } finally {
            }
        } catch (Exception e) {
            Log.e("MetricsStore", "Failed to log ambient lux", e);
        }
    }

    public final void appendNotificationLog(Context context, String timestamp, String packageName, String title, String text) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(timestamp, "timestamp");
        Intrinsics.checkNotNullParameter(packageName, "packageName");
        Intrinsics.checkNotNullParameter(title, "title");
        Intrinsics.checkNotNullParameter(text, "text");
        try {
            File file = new File(context.getFilesDir(), NOTIF_LOG_FILE);
            FileWriter fileWriter = new FileWriter(file, true);
            try {
                Appendable appendableAppend = fileWriter.append((CharSequence) (timestamp + "," + packageName + ",\"" + title + "\",\"" + text + "\""));
                Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
                CloseableKt.closeFinally(fileWriter, null);
                Log.i("MetricsStore", "Logged notification from " + packageName);
            } finally {
            }
        } catch (Exception e) {
            Log.e("MetricsStore", "Failed to log notification", e);
        }
    }

    public final void clearNotificationLog(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        File file = new File(context.getFilesDir(), NOTIF_LOG_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    public final void saveWifiNetworksLog(Context context, String ssid, String bssid, int level) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(ssid, "ssid");
        Intrinsics.checkNotNullParameter(bssid, "bssid");
        try {
            File file = new File(context.getFilesDir(), WIFI_LOG_FILE);
            FileWriter fileWriter = new FileWriter(file, true);
            try {
                Appendable appendableAppend = fileWriter.append((CharSequence) (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "," + ssid + "," + bssid + "," + level));
                Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
                CloseableKt.closeFinally(fileWriter, null);
            } finally {
            }
        } catch (Exception e) {
            Log.e("MetricsStore", "Failed to log WiFi network", e);
        }
    }

    public final void saveDailyDistance(Context context, String date, float distanceKm) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(date, "date");
        try {
            File file = new File(context.getFilesDir(), DISTANCE_LOG_FILE);
            FileWriter fileWriter = new FileWriter(file, true);
            try {
                Appendable appendableAppend = fileWriter.append((CharSequence) (date + "," + distanceKm));
                Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
                CloseableKt.closeFinally(fileWriter, null);
                Log.i("MetricsStore", "Logged distance: " + distanceKm + " km on " + date);
            } finally {
            }
        } catch (Exception e) {
            Log.e("MetricsStore", "Failed to log daily distance", e);
        }
    }

    public final List<Pair<String, Float>> getDailyDistanceLog(Context context) {
        Pair pair;
        Intrinsics.checkNotNullParameter(context, "context");
        File file = new File(context.getFilesDir(), DISTANCE_LOG_FILE);
        if (!file.exists()) {
            return CollectionsKt.emptyList();
        }
        int i = 1;
        Iterable<String> lines$default = FilesKt.readLines$default(file, null, 1, null);
        Collection arrayList = new ArrayList();
        for (String str : lines$default) {
            String[] strArr = new String[i];
            strArr[0] = ",";
            List listSplit$default = StringsKt.split$default((CharSequence) str, strArr, false, 0, 6, (Object) null);
            File file2 = file;
            if (listSplit$default.size() == 2) {
                String str2 = (String) listSplit$default.get(0);
                Float floatOrNull = StringsKt.toFloatOrNull((String) listSplit$default.get(1));
                pair = floatOrNull != null ? TuplesKt.to(str2, floatOrNull) : null;
            } else {
                pair = null;
            }
            if (pair != null) {
                arrayList.add(pair);
            }
            file = file2;
            i = 1;
        }
        return (List) arrayList;
    }

    /* JADX WARN: Removed duplicated region for block: B:33:0x00fc  */
    /* JADX WARN: Removed duplicated region for block: B:53:0x0100 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final float summarizeDailyDistance(android.content.Context r30) throws java.lang.NumberFormatException {
        /*
            Method dump skipped, instructions count: 373
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.MetricsStore.summarizeDailyDistance(android.content.Context):float");
    }
}
