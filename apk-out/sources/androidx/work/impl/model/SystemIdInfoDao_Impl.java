package androidx.work.impl.model;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import androidx.work.impl.model.SystemIdInfoDao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public final class SystemIdInfoDao_Impl implements SystemIdInfoDao {
    private final RoomDatabase __db;
    private final EntityInsertionAdapter<SystemIdInfo> __insertionAdapterOfSystemIdInfo;
    private final SharedSQLiteStatement __preparedStmtOfRemoveSystemIdInfo;
    private final SharedSQLiteStatement __preparedStmtOfRemoveSystemIdInfo_1;

    public SystemIdInfoDao_Impl(RoomDatabase __db) {
        this.__db = __db;
        this.__insertionAdapterOfSystemIdInfo = new EntityInsertionAdapter<SystemIdInfo>(__db) { // from class: androidx.work.impl.model.SystemIdInfoDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR REPLACE INTO `SystemIdInfo` (`work_spec_id`,`generation`,`system_id`) VALUES (?,?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement stmt, SystemIdInfo value) {
                if (value.workSpecId == null) {
                    stmt.bindNull(1);
                } else {
                    stmt.bindString(1, value.workSpecId);
                }
                stmt.bindLong(2, value.getGeneration());
                stmt.bindLong(3, value.systemId);
            }
        };
        this.__preparedStmtOfRemoveSystemIdInfo = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.SystemIdInfoDao_Impl.2
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM SystemIdInfo where work_spec_id=? AND generation=?";
            }
        };
        this.__preparedStmtOfRemoveSystemIdInfo_1 = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.SystemIdInfoDao_Impl.3
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM SystemIdInfo where work_spec_id=?";
            }
        };
    }

    @Override // androidx.work.impl.model.SystemIdInfoDao
    public void insertSystemIdInfo(final SystemIdInfo systemIdInfo) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfSystemIdInfo.insert((EntityInsertionAdapter<SystemIdInfo>) systemIdInfo);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // androidx.work.impl.model.SystemIdInfoDao
    public void removeSystemIdInfo(final String workSpecId, final int generation) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfRemoveSystemIdInfo.acquire();
        if (workSpecId == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindString(1, workSpecId);
        }
        _stmt.bindLong(2, generation);
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfRemoveSystemIdInfo.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.SystemIdInfoDao
    public void removeSystemIdInfo(final String workSpecId) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfRemoveSystemIdInfo_1.acquire();
        if (workSpecId == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindString(1, workSpecId);
        }
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfRemoveSystemIdInfo_1.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.SystemIdInfoDao
    public SystemIdInfo getSystemIdInfo(final String workSpecId, final int generation) {
        SystemIdInfo _result;
        String _tmpWorkSpecId;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT * FROM SystemIdInfo WHERE work_spec_id=? AND generation=?", 2);
        if (workSpecId == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, workSpecId);
        }
        _statement.bindLong(2, generation);
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            int _cursorIndexOfWorkSpecId = CursorUtil.getColumnIndexOrThrow(_cursor, "work_spec_id");
            int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
            int _cursorIndexOfSystemId = CursorUtil.getColumnIndexOrThrow(_cursor, "system_id");
            if (_cursor.moveToFirst()) {
                if (_cursor.isNull(_cursorIndexOfWorkSpecId)) {
                    _tmpWorkSpecId = null;
                } else {
                    _tmpWorkSpecId = _cursor.getString(_cursorIndexOfWorkSpecId);
                }
                int _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
                int _tmpSystemId = _cursor.getInt(_cursorIndexOfSystemId);
                _result = new SystemIdInfo(_tmpWorkSpecId, _tmpGeneration, _tmpSystemId);
            } else {
                _result = null;
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    @Override // androidx.work.impl.model.SystemIdInfoDao
    public List<String> getWorkSpecIds() {
        String _item;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT DISTINCT work_spec_id FROM SystemIdInfo", 0);
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

    @Override // androidx.work.impl.model.SystemIdInfoDao
    public SystemIdInfo getSystemIdInfo(final WorkGenerationalId id) {
        return SystemIdInfoDao.DefaultImpls.getSystemIdInfo(this, id);
    }

    @Override // androidx.work.impl.model.SystemIdInfoDao
    public void removeSystemIdInfo(final WorkGenerationalId id) {
        SystemIdInfoDao.DefaultImpls.removeSystemIdInfo(this, id);
    }

    public static List<Class<?>> getRequiredConverters() {
        return Collections.emptyList();
    }
}
