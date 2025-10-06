package androidx.room.migration;

import androidx.sqlite.db.SupportSQLiteDatabase;

/* loaded from: classes.dex */
public interface AutoMigrationSpec {
    default void onPostMigrate(SupportSQLiteDatabase db) {
    }
}
