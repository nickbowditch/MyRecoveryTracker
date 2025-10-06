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
public final class WorkNameDao_Impl implements WorkNameDao {
    private final RoomDatabase __db;
    private final EntityInsertionAdapter<WorkName> __insertionAdapterOfWorkName;

    public WorkNameDao_Impl(RoomDatabase __db) {
        this.__db = __db;
        this.__insertionAdapterOfWorkName = new EntityInsertionAdapter<WorkName>(__db) { // from class: androidx.work.impl.model.WorkNameDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR IGNORE INTO `WorkName` (`name`,`work_spec_id`) VALUES (?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, WorkName value) {
                if (value.getName() == null) {
                    stmt.bindNull(1);
                } else {
                    stmt.bindString(1, value.getName());
                }
                if (value.getWorkSpecId() == null) {
                    stmt.bindNull(2);
                } else {
                    stmt.bindString(2, value.getWorkSpecId());
                }
            }
        };
    }

    @Override // androidx.work.impl.model.WorkNameDao
    public void insert(final WorkName workName) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfWorkName.insert((EntityInsertionAdapter<WorkName>) workName);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // androidx.work.impl.model.WorkNameDao
    public List<String> getWorkSpecIdsWithName(final String name) {
        String _item;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT work_spec_id FROM workname WHERE name=?", 1);
        if (name == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, name);
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

    @Override // androidx.work.impl.model.WorkNameDao
    public List<String> getNamesForWorkSpecId(final String workSpecId) {
        String _item;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT name FROM workname WHERE work_spec_id=?", 1);
        if (workSpecId == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, workSpecId);
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

    public static List<Class<?>> getRequiredConverters() {
        return Collections.emptyList();
    }
}
