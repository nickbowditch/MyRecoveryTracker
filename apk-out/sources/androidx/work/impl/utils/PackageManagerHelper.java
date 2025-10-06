package androidx.work.impl.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.work.Logger;

/* loaded from: classes.dex */
public class PackageManagerHelper {
    private static final String TAG = Logger.tagWithPrefix("PackageManagerHelper");

    private PackageManagerHelper() {
    }

    public static void setComponentEnabled(Context context, Class<?> klazz, boolean enabled) {
        int i;
        try {
            boolean current = isComponentEnabled(getComponentEnabledSetting(context, klazz.getName()), false);
            if (enabled == current) {
                Logger.get().debug(TAG, "Skipping component enablement for " + klazz.getName());
                return;
            }
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, klazz.getName());
            if (enabled) {
                i = 1;
            } else {
                i = 2;
            }
            packageManager.setComponentEnabledSetting(componentName, i, 1);
            Logger.get().debug(TAG, klazz.getName() + " " + (enabled ? "enabled" : "disabled"));
        } catch (Exception exception) {
            Logger.get().debug(TAG, klazz.getName() + "could not be " + (enabled ? "enabled" : "disabled"), exception);
        }
    }

    public static boolean isComponentExplicitlyEnabled(Context context, Class<?> klazz) {
        int setting = getComponentEnabledSetting(context, klazz.getName());
        return isComponentEnabled(setting, false);
    }

    public static boolean isComponentExplicitlyEnabled(Context context, String className) {
        int state = getComponentEnabledSetting(context, className);
        return state == 1;
    }

    private static int getComponentEnabledSetting(Context context, String className) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, className);
        return packageManager.getComponentEnabledSetting(componentName);
    }

    private static boolean isComponentEnabled(int setting, boolean defaults) {
        if (setting == 0) {
            return defaults;
        }
        return setting == 1;
    }
}
