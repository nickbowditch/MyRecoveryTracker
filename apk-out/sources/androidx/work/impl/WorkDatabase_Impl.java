package androidx.work.impl;

import android.database.SQLException;
import androidx.core.app.NotificationCompat;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomMasterTable;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.work.impl.model.DependencyDao;
import androidx.work.impl.model.DependencyDao_Impl;
import androidx.work.impl.model.PreferenceDao;
import androidx.work.impl.model.PreferenceDao_Impl;
import androidx.work.impl.model.RawWorkInfoDao;
import androidx.work.impl.model.RawWorkInfoDao_Impl;
import androidx.work.impl.model.SystemIdInfoDao;
import androidx.work.impl.model.SystemIdInfoDao_Impl;
import androidx.work.impl.model.WorkNameDao;
import androidx.work.impl.model.WorkNameDao_Impl;
import androidx.work.impl.model.WorkProgressDao;
import androidx.work.impl.model.WorkProgressDao_Impl;
import androidx.work.impl.model.WorkSpecDao;
import androidx.work.impl.model.WorkSpecDao_Impl;
import androidx.work.impl.model.WorkTagDao;
import androidx.work.impl.model.WorkTagDao_Impl;
import androidx.work.impl.utils.PreferenceUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public final class WorkDatabase_Impl extends WorkDatabase {
    private volatile DependencyDao _dependencyDao;
    private volatile PreferenceDao _preferenceDao;
    private volatile RawWorkInfoDao _rawWorkInfoDao;
    private volatile SystemIdInfoDao _systemIdInfoDao;
    private volatile WorkNameDao _workNameDao;
    private volatile WorkProgressDao _workProgressDao;
    private volatile WorkSpecDao _workSpecDao;
    private volatile WorkTagDao _workTagDao;

    @Override // androidx.room.RoomDatabase
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
        SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(20) { // from class: androidx.work.impl.WorkDatabase_Impl.1
            @Override // androidx.room.RoomOpenHelper.Delegate
            public void createAllTables(SupportSQLiteDatabase _db) throws SQLException {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `Dependency` (`work_spec_id` TEXT NOT NULL, `prerequisite_id` TEXT NOT NULL, PRIMARY KEY(`work_spec_id`, `prerequisite_id`), FOREIGN KEY(`work_spec_id`) REFERENCES `WorkSpec`(`id`) ON UPDATE CASCADE ON DELETE CASCADE , FOREIGN KEY(`prerequisite_id`) REFERENCES `WorkSpec`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )");
                _db.execSQL("CREATE INDEX IF NOT EXISTS `index_Dependency_work_spec_id` ON `Dependency` (`work_spec_id`)");
                _db.execSQL("CREATE INDEX IF NOT EXISTS `index_Dependency_prerequisite_id` ON `Dependency` (`prerequisite_id`)");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `WorkSpec` (`id` TEXT NOT NULL, `state` INTEGER NOT NULL, `worker_class_name` TEXT NOT NULL, `input_merger_class_name` TEXT NOT NULL, `input` BLOB NOT NULL, `output` BLOB NOT NULL, `initial_delay` INTEGER NOT NULL, `interval_duration` INTEGER NOT NULL, `flex_duration` INTEGER NOT NULL, `run_attempt_count` INTEGER NOT NULL, `backoff_policy` INTEGER NOT NULL, `backoff_delay_duration` INTEGER NOT NULL, `last_enqueue_time` INTEGER NOT NULL DEFAULT -1, `minimum_retention_duration` INTEGER NOT NULL, `schedule_requested_at` INTEGER NOT NULL, `run_in_foreground` INTEGER NOT NULL, `out_of_quota_policy` INTEGER NOT NULL, `period_count` INTEGER NOT NULL DEFAULT 0, `generation` INTEGER NOT NULL DEFAULT 0, `next_schedule_time_override` INTEGER NOT NULL DEFAULT 9223372036854775807, `next_schedule_time_override_generation` INTEGER NOT NULL DEFAULT 0, `stop_reason` INTEGER NOT NULL DEFAULT -256, `required_network_type` INTEGER NOT NULL, `requires_charging` INTEGER NOT NULL, `requires_device_idle` INTEGER NOT NULL, `requires_battery_not_low` INTEGER NOT NULL, `requires_storage_not_low` INTEGER NOT NULL, `trigger_content_update_delay` INTEGER NOT NULL, `trigger_max_content_delay` INTEGER NOT NULL, `content_uri_triggers` BLOB NOT NULL, PRIMARY KEY(`id`))");
                _db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkSpec_schedule_requested_at` ON `WorkSpec` (`schedule_requested_at`)");
                _db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkSpec_last_enqueue_time` ON `WorkSpec` (`last_enqueue_time`)");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `WorkTag` (`tag` TEXT NOT NULL, `work_spec_id` TEXT NOT NULL, PRIMARY KEY(`tag`, `work_spec_id`), FOREIGN KEY(`work_spec_id`) REFERENCES `WorkSpec`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )");
                _db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkTag_work_spec_id` ON `WorkTag` (`work_spec_id`)");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `SystemIdInfo` (`work_spec_id` TEXT NOT NULL, `generation` INTEGER NOT NULL DEFAULT 0, `system_id` INTEGER NOT NULL, PRIMARY KEY(`work_spec_id`, `generation`), FOREIGN KEY(`work_spec_id`) REFERENCES `WorkSpec`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `WorkName` (`name` TEXT NOT NULL, `work_spec_id` TEXT NOT NULL, PRIMARY KEY(`name`, `work_spec_id`), FOREIGN KEY(`work_spec_id`) REFERENCES `WorkSpec`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )");
                _db.execSQL("CREATE INDEX IF NOT EXISTS `index_WorkName_work_spec_id` ON `WorkName` (`work_spec_id`)");
                _db.execSQL("CREATE TABLE IF NOT EXISTS `WorkProgress` (`work_spec_id` TEXT NOT NULL, `progress` BLOB NOT NULL, PRIMARY KEY(`work_spec_id`), FOREIGN KEY(`work_spec_id`) REFERENCES `WorkSpec`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )");
                _db.execSQL(PreferenceUtils.CREATE_PREFERENCE);
                _db.execSQL(RoomMasterTable.CREATE_QUERY);
                _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7d73d21f1bd82c9e5268b6dcf9fde2cb')");
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void dropAllTables(SupportSQLiteDatabase _db) throws SQLException {
                _db.execSQL("DROP TABLE IF EXISTS `Dependency`");
                _db.execSQL("DROP TABLE IF EXISTS `WorkSpec`");
                _db.execSQL("DROP TABLE IF EXISTS `WorkTag`");
                _db.execSQL("DROP TABLE IF EXISTS `SystemIdInfo`");
                _db.execSQL("DROP TABLE IF EXISTS `WorkName`");
                _db.execSQL("DROP TABLE IF EXISTS `WorkProgress`");
                _db.execSQL("DROP TABLE IF EXISTS `Preference`");
                if (WorkDatabase_Impl.this.mCallbacks != null) {
                    int _size = WorkDatabase_Impl.this.mCallbacks.size();
                    for (int _i = 0; _i < _size; _i++) {
                        ((RoomDatabase.Callback) WorkDatabase_Impl.this.mCallbacks.get(_i)).onDestructiveMigration(_db);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onCreate(SupportSQLiteDatabase _db) {
                if (WorkDatabase_Impl.this.mCallbacks != null) {
                    int _size = WorkDatabase_Impl.this.mCallbacks.size();
                    for (int _i = 0; _i < _size; _i++) {
                        ((RoomDatabase.Callback) WorkDatabase_Impl.this.mCallbacks.get(_i)).onCreate(_db);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onOpen(SupportSQLiteDatabase _db) throws SQLException {
                WorkDatabase_Impl.this.mDatabase = _db;
                _db.execSQL("PRAGMA foreign_keys = ON");
                WorkDatabase_Impl.this.internalInitInvalidationTracker(_db);
                if (WorkDatabase_Impl.this.mCallbacks != null) {
                    int _size = WorkDatabase_Impl.this.mCallbacks.size();
                    for (int _i = 0; _i < _size; _i++) {
                        ((RoomDatabase.Callback) WorkDatabase_Impl.this.mCallbacks.get(_i)).onOpen(_db);
                    }
                }
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onPreMigrate(SupportSQLiteDatabase _db) throws SQLException {
                DBUtil.dropFtsSyncTriggers(_db);
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public void onPostMigrate(SupportSQLiteDatabase _db) {
            }

            @Override // androidx.room.RoomOpenHelper.Delegate
            public RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
                HashMap<String, TableInfo.Column> _columnsDependency = new HashMap<>(2);
                _columnsDependency.put("work_spec_id", new TableInfo.Column("work_spec_id", "TEXT", true, 1, null, 1));
                _columnsDependency.put("prerequisite_id", new TableInfo.Column("prerequisite_id", "TEXT", true, 2, null, 1));
                HashSet<TableInfo.ForeignKey> _foreignKeysDependency = new HashSet<>(2);
                _foreignKeysDependency.add(new TableInfo.ForeignKey("WorkSpec", "CASCADE", "CASCADE", Arrays.asList("work_spec_id"), Arrays.asList("id")));
                _foreignKeysDependency.add(new TableInfo.ForeignKey("WorkSpec", "CASCADE", "CASCADE", Arrays.asList("prerequisite_id"), Arrays.asList("id")));
                HashSet<TableInfo.Index> _indicesDependency = new HashSet<>(2);
                _indicesDependency.add(new TableInfo.Index("index_Dependency_work_spec_id", false, Arrays.asList("work_spec_id"), Arrays.asList("ASC")));
                _indicesDependency.add(new TableInfo.Index("index_Dependency_prerequisite_id", false, Arrays.asList("prerequisite_id"), Arrays.asList("ASC")));
                TableInfo _infoDependency = new TableInfo("Dependency", _columnsDependency, _foreignKeysDependency, _indicesDependency);
                TableInfo _existingDependency = TableInfo.read(_db, "Dependency");
                if (!_infoDependency.equals(_existingDependency)) {
                    return new RoomOpenHelper.ValidationResult(false, "Dependency(androidx.work.impl.model.Dependency).\n Expected:\n" + _infoDependency + "\n Found:\n" + _existingDependency);
                }
                HashMap<String, TableInfo.Column> _columnsWorkSpec = new HashMap<>(30);
                _columnsWorkSpec.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, 1));
                _columnsWorkSpec.put("state", new TableInfo.Column("state", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("worker_class_name", new TableInfo.Column("worker_class_name", "TEXT", true, 0, null, 1));
                _columnsWorkSpec.put("input_merger_class_name", new TableInfo.Column("input_merger_class_name", "TEXT", true, 0, null, 1));
                _columnsWorkSpec.put("input", new TableInfo.Column("input", "BLOB", true, 0, null, 1));
                _columnsWorkSpec.put("output", new TableInfo.Column("output", "BLOB", true, 0, null, 1));
                _columnsWorkSpec.put("initial_delay", new TableInfo.Column("initial_delay", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("interval_duration", new TableInfo.Column("interval_duration", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("flex_duration", new TableInfo.Column("flex_duration", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("run_attempt_count", new TableInfo.Column("run_attempt_count", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("backoff_policy", new TableInfo.Column("backoff_policy", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("backoff_delay_duration", new TableInfo.Column("backoff_delay_duration", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("last_enqueue_time", new TableInfo.Column("last_enqueue_time", "INTEGER", true, 0, "-1", 1));
                _columnsWorkSpec.put("minimum_retention_duration", new TableInfo.Column("minimum_retention_duration", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("schedule_requested_at", new TableInfo.Column("schedule_requested_at", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("run_in_foreground", new TableInfo.Column("run_in_foreground", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("out_of_quota_policy", new TableInfo.Column("out_of_quota_policy", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("period_count", new TableInfo.Column("period_count", "INTEGER", true, 0, "0", 1));
                _columnsWorkSpec.put("generation", new TableInfo.Column("generation", "INTEGER", true, 0, "0", 1));
                _columnsWorkSpec.put("next_schedule_time_override", new TableInfo.Column("next_schedule_time_override", "INTEGER", true, 0, "9223372036854775807", 1));
                _columnsWorkSpec.put("next_schedule_time_override_generation", new TableInfo.Column("next_schedule_time_override_generation", "INTEGER", true, 0, "0", 1));
                _columnsWorkSpec.put("stop_reason", new TableInfo.Column("stop_reason", "INTEGER", true, 0, "-256", 1));
                _columnsWorkSpec.put("required_network_type", new TableInfo.Column("required_network_type", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("requires_charging", new TableInfo.Column("requires_charging", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("requires_device_idle", new TableInfo.Column("requires_device_idle", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("requires_battery_not_low", new TableInfo.Column("requires_battery_not_low", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("requires_storage_not_low", new TableInfo.Column("requires_storage_not_low", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("trigger_content_update_delay", new TableInfo.Column("trigger_content_update_delay", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("trigger_max_content_delay", new TableInfo.Column("trigger_max_content_delay", "INTEGER", true, 0, null, 1));
                _columnsWorkSpec.put("content_uri_triggers", new TableInfo.Column("content_uri_triggers", "BLOB", true, 0, null, 1));
                HashSet<TableInfo.ForeignKey> _foreignKeysWorkSpec = new HashSet<>(0);
                HashSet<TableInfo.Index> _indicesWorkSpec = new HashSet<>(2);
                _indicesWorkSpec.add(new TableInfo.Index("index_WorkSpec_schedule_requested_at", false, Arrays.asList("schedule_requested_at"), Arrays.asList("ASC")));
                _indicesWorkSpec.add(new TableInfo.Index("index_WorkSpec_last_enqueue_time", false, Arrays.asList("last_enqueue_time"), Arrays.asList("ASC")));
                TableInfo _infoWorkSpec = new TableInfo("WorkSpec", _columnsWorkSpec, _foreignKeysWorkSpec, _indicesWorkSpec);
                TableInfo _existingWorkSpec = TableInfo.read(_db, "WorkSpec");
                if (!_infoWorkSpec.equals(_existingWorkSpec)) {
                    return new RoomOpenHelper.ValidationResult(false, "WorkSpec(androidx.work.impl.model.WorkSpec).\n Expected:\n" + _infoWorkSpec + "\n Found:\n" + _existingWorkSpec);
                }
                HashMap<String, TableInfo.Column> _columnsWorkTag = new HashMap<>(2);
                _columnsWorkTag.put("tag", new TableInfo.Column("tag", "TEXT", true, 1, null, 1));
                _columnsWorkTag.put("work_spec_id", new TableInfo.Column("work_spec_id", "TEXT", true, 2, null, 1));
                HashSet<TableInfo.ForeignKey> _foreignKeysWorkTag = new HashSet<>(1);
                _foreignKeysWorkTag.add(new TableInfo.ForeignKey("WorkSpec", "CASCADE", "CASCADE", Arrays.asList("work_spec_id"), Arrays.asList("id")));
                HashSet<TableInfo.Index> _indicesWorkTag = new HashSet<>(1);
                _indicesWorkTag.add(new TableInfo.Index("index_WorkTag_work_spec_id", false, Arrays.asList("work_spec_id"), Arrays.asList("ASC")));
                TableInfo _infoWorkTag = new TableInfo("WorkTag", _columnsWorkTag, _foreignKeysWorkTag, _indicesWorkTag);
                TableInfo _existingWorkTag = TableInfo.read(_db, "WorkTag");
                if (!_infoWorkTag.equals(_existingWorkTag)) {
                    return new RoomOpenHelper.ValidationResult(false, "WorkTag(androidx.work.impl.model.WorkTag).\n Expected:\n" + _infoWorkTag + "\n Found:\n" + _existingWorkTag);
                }
                HashMap<String, TableInfo.Column> _columnsSystemIdInfo = new HashMap<>(3);
                _columnsSystemIdInfo.put("work_spec_id", new TableInfo.Column("work_spec_id", "TEXT", true, 1, null, 1));
                _columnsSystemIdInfo.put("generation", new TableInfo.Column("generation", "INTEGER", true, 2, "0", 1));
                _columnsSystemIdInfo.put("system_id", new TableInfo.Column("system_id", "INTEGER", true, 0, null, 1));
                HashSet<TableInfo.ForeignKey> _foreignKeysSystemIdInfo = new HashSet<>(1);
                _foreignKeysSystemIdInfo.add(new TableInfo.ForeignKey("WorkSpec", "CASCADE", "CASCADE", Arrays.asList("work_spec_id"), Arrays.asList("id")));
                HashSet<TableInfo.Index> _indicesSystemIdInfo = new HashSet<>(0);
                TableInfo _infoSystemIdInfo = new TableInfo("SystemIdInfo", _columnsSystemIdInfo, _foreignKeysSystemIdInfo, _indicesSystemIdInfo);
                TableInfo _existingSystemIdInfo = TableInfo.read(_db, "SystemIdInfo");
                if (!_infoSystemIdInfo.equals(_existingSystemIdInfo)) {
                    return new RoomOpenHelper.ValidationResult(false, "SystemIdInfo(androidx.work.impl.model.SystemIdInfo).\n Expected:\n" + _infoSystemIdInfo + "\n Found:\n" + _existingSystemIdInfo);
                }
                HashMap<String, TableInfo.Column> _columnsWorkName = new HashMap<>(2);
                _columnsWorkName.put("name", new TableInfo.Column("name", "TEXT", true, 1, null, 1));
                _columnsWorkName.put("work_spec_id", new TableInfo.Column("work_spec_id", "TEXT", true, 2, null, 1));
                HashSet<TableInfo.ForeignKey> _foreignKeysWorkName = new HashSet<>(1);
                _foreignKeysWorkName.add(new TableInfo.ForeignKey("WorkSpec", "CASCADE", "CASCADE", Arrays.asList("work_spec_id"), Arrays.asList("id")));
                HashSet<TableInfo.Index> _indicesWorkName = new HashSet<>(1);
                _indicesWorkName.add(new TableInfo.Index("index_WorkName_work_spec_id", false, Arrays.asList("work_spec_id"), Arrays.asList("ASC")));
                TableInfo _infoWorkName = new TableInfo("WorkName", _columnsWorkName, _foreignKeysWorkName, _indicesWorkName);
                TableInfo _existingWorkName = TableInfo.read(_db, "WorkName");
                if (!_infoWorkName.equals(_existingWorkName)) {
                    return new RoomOpenHelper.ValidationResult(false, "WorkName(androidx.work.impl.model.WorkName).\n Expected:\n" + _infoWorkName + "\n Found:\n" + _existingWorkName);
                }
                HashMap<String, TableInfo.Column> _columnsWorkProgress = new HashMap<>(2);
                _columnsWorkProgress.put("work_spec_id", new TableInfo.Column("work_spec_id", "TEXT", true, 1, null, 1));
                _columnsWorkProgress.put(NotificationCompat.CATEGORY_PROGRESS, new TableInfo.Column(NotificationCompat.CATEGORY_PROGRESS, "BLOB", true, 0, null, 1));
                HashSet<TableInfo.ForeignKey> _foreignKeysWorkProgress = new HashSet<>(1);
                _foreignKeysWorkProgress.add(new TableInfo.ForeignKey("WorkSpec", "CASCADE", "CASCADE", Arrays.asList("work_spec_id"), Arrays.asList("id")));
                HashSet<TableInfo.Index> _indicesWorkProgress = new HashSet<>(0);
                TableInfo _infoWorkProgress = new TableInfo("WorkProgress", _columnsWorkProgress, _foreignKeysWorkProgress, _indicesWorkProgress);
                TableInfo _existingWorkProgress = TableInfo.read(_db, "WorkProgress");
                if (!_infoWorkProgress.equals(_existingWorkProgress)) {
                    return new RoomOpenHelper.ValidationResult(false, "WorkProgress(androidx.work.impl.model.WorkProgress).\n Expected:\n" + _infoWorkProgress + "\n Found:\n" + _existingWorkProgress);
                }
                HashMap<String, TableInfo.Column> _columnsPreference = new HashMap<>(2);
                _columnsPreference.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, 1));
                _columnsPreference.put("long_value", new TableInfo.Column("long_value", "INTEGER", false, 0, null, 1));
                HashSet<TableInfo.ForeignKey> _foreignKeysPreference = new HashSet<>(0);
                HashSet<TableInfo.Index> _indicesPreference = new HashSet<>(0);
                TableInfo _infoPreference = new TableInfo("Preference", _columnsPreference, _foreignKeysPreference, _indicesPreference);
                TableInfo _existingPreference = TableInfo.read(_db, "Preference");
                return !_infoPreference.equals(_existingPreference) ? new RoomOpenHelper.ValidationResult(false, "Preference(androidx.work.impl.model.Preference).\n Expected:\n" + _infoPreference + "\n Found:\n" + _existingPreference) : new RoomOpenHelper.ValidationResult(true, null);
            }
        }, "7d73d21f1bd82c9e5268b6dcf9fde2cb", "3071c8717539de5d5353f4c8cd59a032");
        SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context).name(configuration.name).callback(_openCallback).build();
        SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
        return _helper;
    }

    @Override // androidx.room.RoomDatabase
    protected InvalidationTracker createInvalidationTracker() {
        HashMap<String, String> _shadowTablesMap = new HashMap<>(0);
        HashMap<String, Set<String>> _viewTables = new HashMap<>(0);
        return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "Dependency", "WorkSpec", "WorkTag", "SystemIdInfo", "WorkName", "WorkProgress", "Preference");
    }

    @Override // androidx.room.RoomDatabase
    public void clearAllTables() throws SQLException {
        super.assertNotMainThread();
        SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
        if (1 == 0) {
            try {
                _db.execSQL("PRAGMA foreign_keys = FALSE");
            } finally {
                super.endTransaction();
                if (1 == 0) {
                    _db.execSQL("PRAGMA foreign_keys = TRUE");
                }
                _db.query("PRAGMA wal_checkpoint(FULL)").close();
                if (!_db.inTransaction()) {
                    _db.execSQL("VACUUM");
                }
            }
        }
        super.beginTransaction();
        if (1 != 0) {
            _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
        }
        _db.execSQL("DELETE FROM `Dependency`");
        _db.execSQL("DELETE FROM `WorkSpec`");
        _db.execSQL("DELETE FROM `WorkTag`");
        _db.execSQL("DELETE FROM `SystemIdInfo`");
        _db.execSQL("DELETE FROM `WorkName`");
        _db.execSQL("DELETE FROM `WorkProgress`");
        _db.execSQL("DELETE FROM `Preference`");
        super.setTransactionSuccessful();
    }

    @Override // androidx.room.RoomDatabase
    protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
        HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<>();
        _typeConvertersMap.put(WorkSpecDao.class, WorkSpecDao_Impl.getRequiredConverters());
        _typeConvertersMap.put(DependencyDao.class, DependencyDao_Impl.getRequiredConverters());
        _typeConvertersMap.put(WorkTagDao.class, WorkTagDao_Impl.getRequiredConverters());
        _typeConvertersMap.put(SystemIdInfoDao.class, SystemIdInfoDao_Impl.getRequiredConverters());
        _typeConvertersMap.put(WorkNameDao.class, WorkNameDao_Impl.getRequiredConverters());
        _typeConvertersMap.put(WorkProgressDao.class, WorkProgressDao_Impl.getRequiredConverters());
        _typeConvertersMap.put(PreferenceDao.class, PreferenceDao_Impl.getRequiredConverters());
        _typeConvertersMap.put(RawWorkInfoDao.class, RawWorkInfoDao_Impl.getRequiredConverters());
        return _typeConvertersMap;
    }

    @Override // androidx.room.RoomDatabase
    public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
        HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<>();
        return _autoMigrationSpecsSet;
    }

    @Override // androidx.room.RoomDatabase
    public List<Migration> getAutoMigrations(Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
        return Arrays.asList(new WorkDatabase_AutoMigration_13_14_Impl(), new WorkDatabase_AutoMigration_14_15_Impl(), new WorkDatabase_AutoMigration_16_17_Impl(), new WorkDatabase_AutoMigration_17_18_Impl(), new WorkDatabase_AutoMigration_18_19_Impl(), new WorkDatabase_AutoMigration_19_20_Impl());
    }

    @Override // androidx.work.impl.WorkDatabase
    public WorkSpecDao workSpecDao() {
        WorkSpecDao workSpecDao;
        if (this._workSpecDao != null) {
            return this._workSpecDao;
        }
        synchronized (this) {
            if (this._workSpecDao == null) {
                this._workSpecDao = new WorkSpecDao_Impl(this);
            }
            workSpecDao = this._workSpecDao;
        }
        return workSpecDao;
    }

    @Override // androidx.work.impl.WorkDatabase
    public DependencyDao dependencyDao() {
        DependencyDao dependencyDao;
        if (this._dependencyDao != null) {
            return this._dependencyDao;
        }
        synchronized (this) {
            if (this._dependencyDao == null) {
                this._dependencyDao = new DependencyDao_Impl(this);
            }
            dependencyDao = this._dependencyDao;
        }
        return dependencyDao;
    }

    @Override // androidx.work.impl.WorkDatabase
    public WorkTagDao workTagDao() {
        WorkTagDao workTagDao;
        if (this._workTagDao != null) {
            return this._workTagDao;
        }
        synchronized (this) {
            if (this._workTagDao == null) {
                this._workTagDao = new WorkTagDao_Impl(this);
            }
            workTagDao = this._workTagDao;
        }
        return workTagDao;
    }

    @Override // androidx.work.impl.WorkDatabase
    public SystemIdInfoDao systemIdInfoDao() {
        SystemIdInfoDao systemIdInfoDao;
        if (this._systemIdInfoDao != null) {
            return this._systemIdInfoDao;
        }
        synchronized (this) {
            if (this._systemIdInfoDao == null) {
                this._systemIdInfoDao = new SystemIdInfoDao_Impl(this);
            }
            systemIdInfoDao = this._systemIdInfoDao;
        }
        return systemIdInfoDao;
    }

    @Override // androidx.work.impl.WorkDatabase
    public WorkNameDao workNameDao() {
        WorkNameDao workNameDao;
        if (this._workNameDao != null) {
            return this._workNameDao;
        }
        synchronized (this) {
            if (this._workNameDao == null) {
                this._workNameDao = new WorkNameDao_Impl(this);
            }
            workNameDao = this._workNameDao;
        }
        return workNameDao;
    }

    @Override // androidx.work.impl.WorkDatabase
    public WorkProgressDao workProgressDao() {
        WorkProgressDao workProgressDao;
        if (this._workProgressDao != null) {
            return this._workProgressDao;
        }
        synchronized (this) {
            if (this._workProgressDao == null) {
                this._workProgressDao = new WorkProgressDao_Impl(this);
            }
            workProgressDao = this._workProgressDao;
        }
        return workProgressDao;
    }

    @Override // androidx.work.impl.WorkDatabase
    public PreferenceDao preferenceDao() {
        PreferenceDao preferenceDao;
        if (this._preferenceDao != null) {
            return this._preferenceDao;
        }
        synchronized (this) {
            if (this._preferenceDao == null) {
                this._preferenceDao = new PreferenceDao_Impl(this);
            }
            preferenceDao = this._preferenceDao;
        }
        return preferenceDao;
    }

    @Override // androidx.work.impl.WorkDatabase
    public RawWorkInfoDao rawWorkInfoDao() {
        RawWorkInfoDao rawWorkInfoDao;
        if (this._rawWorkInfoDao != null) {
            return this._rawWorkInfoDao;
        }
        synchronized (this) {
            if (this._rawWorkInfoDao == null) {
                this._rawWorkInfoDao = new RawWorkInfoDao_Impl(this);
            }
            rawWorkInfoDao = this._rawWorkInfoDao;
        }
        return rawWorkInfoDao;
    }
}
