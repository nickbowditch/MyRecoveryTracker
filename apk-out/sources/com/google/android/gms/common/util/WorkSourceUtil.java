package com.google.android.gms.common.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.WorkSource;
import android.util.Log;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.wrappers.Wrappers;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
/* loaded from: classes.dex */
public class WorkSourceUtil {
    private static final int zza = Process.myUid();
    private static final Method zzb;
    private static final Method zzc;
    private static final Method zzd;
    private static final Method zze;
    private static final Method zzf;
    private static final Method zzg;
    private static final Method zzh;
    private static final Method zzi;

    static {
        Method method;
        Method method2;
        Method method3;
        Method method4;
        Method method5;
        Method method6;
        Method method7;
        Method method8 = null;
        try {
            method = WorkSource.class.getMethod("add", Integer.TYPE);
        } catch (Exception e) {
            method = null;
        }
        zzb = method;
        if (PlatformVersion.isAtLeastJellyBeanMR2()) {
            try {
                method2 = WorkSource.class.getMethod("add", Integer.TYPE, String.class);
            } catch (Exception e2) {
            }
        } else {
            method2 = null;
        }
        zzc = method2;
        try {
            method3 = WorkSource.class.getMethod("size", new Class[0]);
        } catch (Exception e3) {
            method3 = null;
        }
        zzd = method3;
        try {
            method4 = WorkSource.class.getMethod("get", Integer.TYPE);
        } catch (Exception e4) {
            method4 = null;
        }
        zze = method4;
        if (PlatformVersion.isAtLeastJellyBeanMR2()) {
            try {
                method5 = WorkSource.class.getMethod("getName", Integer.TYPE);
            } catch (Exception e5) {
            }
        } else {
            method5 = null;
        }
        zzf = method5;
        if (PlatformVersion.isAtLeastP()) {
            try {
                method6 = WorkSource.class.getMethod("createWorkChain", new Class[0]);
            } catch (Exception e6) {
                Log.w("WorkSourceUtil", "Missing WorkChain API createWorkChain", e6);
                method6 = null;
            }
        } else {
            method6 = null;
        }
        zzg = method6;
        if (PlatformVersion.isAtLeastP()) {
            try {
                method7 = Class.forName("android.os.WorkSource$WorkChain").getMethod("addNode", Integer.TYPE, String.class);
            } catch (Exception e7) {
                Log.w("WorkSourceUtil", "Missing WorkChain class", e7);
                method7 = null;
            }
        } else {
            method7 = null;
        }
        zzh = method7;
        if (PlatformVersion.isAtLeastP()) {
            try {
                method8 = WorkSource.class.getMethod("isEmpty", new Class[0]);
                method8.setAccessible(true);
            } catch (Exception e8) {
            }
        }
        zzi = method8;
    }

    private WorkSourceUtil() {
    }

    public static void add(WorkSource workSource, int uid, String packageName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = zzc;
        if (method != null) {
            if (packageName == null) {
                packageName = "";
            }
            try {
                method.invoke(workSource, Integer.valueOf(uid), packageName);
                return;
            } catch (Exception e) {
                Log.wtf("WorkSourceUtil", "Unable to assign blame through WorkSource", e);
                return;
            }
        }
        Method method2 = zzb;
        if (method2 != null) {
            try {
                method2.invoke(workSource, Integer.valueOf(uid));
            } catch (Exception e2) {
                Log.wtf("WorkSourceUtil", "Unable to assign blame through WorkSource", e2);
            }
        }
    }

    public static WorkSource fromPackage(Context context, String packageName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (context == null || context.getPackageManager() == null || packageName == null) {
            return null;
        }
        try {
            ApplicationInfo applicationInfo = Wrappers.packageManager(context).getApplicationInfo(packageName, 0);
            if (applicationInfo == null) {
                Log.e("WorkSourceUtil", "Could not get applicationInfo from package: ".concat(packageName));
                return null;
            }
            int i = applicationInfo.uid;
            WorkSource workSource = new WorkSource();
            add(workSource, i, packageName);
            return workSource;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("WorkSourceUtil", "Could not find package: ".concat(packageName));
            return null;
        }
    }

    public static WorkSource fromPackageAndModuleExperimentalPi(Context context, String packageName, String moduleName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (context == null || context.getPackageManager() == null || moduleName == null || packageName == null) {
            Log.w("WorkSourceUtil", "Unexpected null arguments");
            return null;
        }
        int i = -1;
        try {
            ApplicationInfo applicationInfo = Wrappers.packageManager(context).getApplicationInfo(packageName, 0);
            if (applicationInfo == null) {
                Log.e("WorkSourceUtil", "Could not get applicationInfo from package: ".concat(packageName));
            } else {
                i = applicationInfo.uid;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("WorkSourceUtil", "Could not find package: ".concat(packageName));
        }
        if (i < 0) {
            return null;
        }
        WorkSource workSource = new WorkSource();
        Method method = zzg;
        if (method == null || zzh == null) {
            add(workSource, i, packageName);
        } else {
            try {
                Object objInvoke = method.invoke(workSource, new Object[0]);
                if (i != zza) {
                    zzh.invoke(objInvoke, Integer.valueOf(i), packageName);
                }
                zzh.invoke(objInvoke, Integer.valueOf(zza), moduleName);
            } catch (Exception e2) {
                Log.w("WorkSourceUtil", "Unable to assign chained blame through WorkSource", e2);
            }
        }
        return workSource;
    }

    public static int get(WorkSource workSource, int i) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = zze;
        if (method == null) {
            return 0;
        }
        try {
            Object objInvoke = method.invoke(workSource, Integer.valueOf(i));
            Preconditions.checkNotNull(objInvoke);
            return ((Integer) objInvoke).intValue();
        } catch (Exception e) {
            Log.wtf("WorkSourceUtil", "Unable to assign blame through WorkSource", e);
            return 0;
        }
    }

    public static String getName(WorkSource workSource, int i) {
        Method method = zzf;
        if (method == null) {
            return null;
        }
        try {
            return (String) method.invoke(workSource, Integer.valueOf(i));
        } catch (Exception e) {
            Log.wtf("WorkSourceUtil", "Unable to assign blame through WorkSource", e);
            return null;
        }
    }

    public static List<String> getNames(WorkSource workSource) {
        ArrayList arrayList = new ArrayList();
        int size = workSource == null ? 0 : size(workSource);
        if (size != 0) {
            for (int i = 0; i < size; i++) {
                String name = getName(workSource, i);
                if (!Strings.isEmptyOrWhitespace(name)) {
                    Preconditions.checkNotNull(name);
                    arrayList.add(name);
                }
            }
        }
        return arrayList;
    }

    public static boolean hasWorkSourcePermission(Context context) {
        return (context == null || context.getPackageManager() == null || Wrappers.packageManager(context).checkPermission("android.permission.UPDATE_DEVICE_STATS", context.getPackageName()) != 0) ? false : true;
    }

    public static boolean isEmpty(WorkSource workSource) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = zzi;
        if (method != null) {
            try {
                Object objInvoke = method.invoke(workSource, new Object[0]);
                Preconditions.checkNotNull(objInvoke);
                return ((Boolean) objInvoke).booleanValue();
            } catch (Exception e) {
                Log.e("WorkSourceUtil", "Unable to check WorkSource emptiness", e);
            }
        }
        return size(workSource) == 0;
    }

    public static int size(WorkSource workSource) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = zzd;
        if (method != null) {
            try {
                Object objInvoke = method.invoke(workSource, new Object[0]);
                Preconditions.checkNotNull(objInvoke);
                return ((Integer) objInvoke).intValue();
            } catch (Exception e) {
                Log.wtf("WorkSourceUtil", "Unable to assign blame through WorkSource", e);
            }
        }
        return 0;
    }
}
