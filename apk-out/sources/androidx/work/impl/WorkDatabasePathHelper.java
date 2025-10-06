package androidx.work.impl;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Logger;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.MapsKt;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;

/* compiled from: WorkDatabasePathHelper.kt */
@Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010$\n\u0000\bÇ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0010\u0010\b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0003J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u001a\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\f2\u0006\u0010\u0005\u001a\u00020\u0006¨\u0006\r"}, d2 = {"Landroidx/work/impl/WorkDatabasePathHelper;", "", "()V", "getDatabasePath", "Ljava/io/File;", "context", "Landroid/content/Context;", "getDefaultDatabasePath", "getNoBackupPath", "migrateDatabase", "", "migrationPaths", "", "work-runtime_release"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class WorkDatabasePathHelper {
    public static final WorkDatabasePathHelper INSTANCE = new WorkDatabasePathHelper();

    private WorkDatabasePathHelper() {
    }

    @JvmStatic
    public static final void migrateDatabase(Context context) {
        String message;
        Intrinsics.checkNotNullParameter(context, "context");
        File defaultDatabasePath = INSTANCE.getDefaultDatabasePath(context);
        if (defaultDatabasePath.exists()) {
            Logger.get().debug(WorkDatabasePathHelperKt.TAG, "Migrating WorkDatabase to the no-backup directory");
            Map $this$forEach$iv = INSTANCE.migrationPaths(context);
            for (Map.Entry element$iv : $this$forEach$iv.entrySet()) {
                File source = element$iv.getKey();
                File destination = element$iv.getValue();
                if (source.exists()) {
                    if (destination.exists()) {
                        Logger.get().warning(WorkDatabasePathHelperKt.TAG, "Over-writing contents of " + destination);
                    }
                    boolean renamed = source.renameTo(destination);
                    if (renamed) {
                        message = "Migrated " + source + "to " + destination;
                    } else {
                        message = "Renaming " + source + " to " + destination + " failed";
                    }
                    Logger.get().debug(WorkDatabasePathHelperKt.TAG, message);
                }
            }
        }
    }

    public final Map<File, File> migrationPaths(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        File databasePath = getDefaultDatabasePath(context);
        File migratedPath = getDatabasePath(context);
        String[] strArr = WorkDatabasePathHelperKt.DATABASE_EXTRA_FILES;
        int capacity$iv = RangesKt.coerceAtLeast(MapsKt.mapCapacity(strArr.length), 16);
        Map map = new LinkedHashMap(capacity$iv);
        int length = strArr.length;
        int i = 0;
        while (i < length) {
            String str = strArr[i];
            Pair pair = TuplesKt.to(new File(databasePath.getPath() + str), new File(migratedPath.getPath() + str));
            map.put(pair.getFirst(), pair.getSecond());
            i++;
            strArr = strArr;
        }
        return MapsKt.plus(map, TuplesKt.to(databasePath, migratedPath));
    }

    public final File getDefaultDatabasePath(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        File databasePath = context.getDatabasePath(WorkDatabasePathHelperKt.WORK_DATABASE_NAME);
        Intrinsics.checkNotNullExpressionValue(databasePath, "context.getDatabasePath(WORK_DATABASE_NAME)");
        return databasePath;
    }

    public final File getDatabasePath(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        return getNoBackupPath(context);
    }

    private final File getNoBackupPath(Context context) {
        return new File(Api21Impl.INSTANCE.getNoBackupFilesDir(context), WorkDatabasePathHelperKt.WORK_DATABASE_NAME);
    }
}
