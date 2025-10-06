package androidx.work.impl.model;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public final class DependencyDao_Impl implements DependencyDao {
    private final RoomDatabase __db;
    private final EntityInsertionAdapter<Dependency> __insertionAdapterOfDependency;

    public DependencyDao_Impl(RoomDatabase __db) {
        this.__db = __db;
        this.__insertionAdapterOfDependency = new EntityInsertionAdapter<Dependency>(__db) { // from class: androidx.work.impl.model.DependencyDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR IGNORE INTO `Dependency` (`work_spec_id`,`prerequisite_id`) VALUES (?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, Dependency value) {
                if (value.getWorkSpecId() == null) {
                    stmt.bindNull(1);
                } else {
                    stmt.bindString(1, value.getWorkSpecId());
                }
                if (value.getPrerequisiteId() == null) {
                    stmt.bindNull(2);
                } else {
                    stmt.bindString(2, value.getPrerequisiteId());
                }
            }
        };
    }

    @Override // androidx.work.impl.model.DependencyDao
    public void insertDependency(final Dependency dependency) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfDependency.insert((EntityInsertionAdapter<Dependency>) dependency);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // androidx.work.impl.model.DependencyDao
    public boolean hasCompletedAllPrerequisites(final String id) {
        boolean _result = true;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT COUNT(*)=0 FROM dependency WHERE work_spec_id=? AND prerequisite_id IN (SELECT id FROM workspec WHERE state!=2)", 1);
        if (id == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, id);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            if (_cursor.moveToFirst()) {
                int _tmp = _cursor.getInt(0);
                if (_tmp == 0) {
                    _result = false;
                }
            } else {
                _result = false;
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    @Override // androidx.work.impl.model.DependencyDao
    public List<String> getPrerequisites(final String id) {
        String _item;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT prerequisite_id FROM dependency WHERE work_spec_id=?", 1);
        if (id == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, id);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            List<String> _result = new ArrayList<>(_cursor.getCount());
            while (_cursor.moveToNext()) {
                if (_cursor.isNull(0)) {
                    _item = null;
                } else {
                    _item = _cursor.getString(0);
                }
                _result.add(_item);
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    @Override // androidx.work.impl.model.DependencyDao
    public List<String> getDependentWorkIds(final String id) {
        String _item;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT work_spec_id FROM dependency WHERE prerequisite_id=?", 1);
        if (id == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, id);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            List<String> _result = new ArrayList<>(_cursor.getCount());
            while (_cursor.moveToNext()) {
                if (_cursor.isNull(0)) {
                    _item = null;
                } else {
                    _item = _cursor.getString(0);
                }
                _result.add(_item);
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    @Override // androidx.work.impl.model.DependencyDao
    public boolean hasDependents(final String id) {
        boolean _result = true;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT COUNT(*)>0 FROM dependency WHERE prerequisite_id=?", 1);
        if (id == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, id);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            if (_cursor.moveToFirst()) {
                int _tmp = _cursor.getInt(0);
                if (_tmp == 0) {
                    _result = false;
                }
            } else {
                _result = false;
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    public static List<Class<?>> getRequiredConverters() {
        return Collections.emptyList();
    }
}
