package androidx.work.impl.model;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkInfo;
import androidx.work.impl.model.WorkSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import kotlinx.coroutines.flow.Flow;

/* loaded from: classes.dex */
public final class WorkSpecDao_Impl implements WorkSpecDao {
    private final RoomDatabase __db;
    private final EntityInsertionAdapter<WorkSpec> __insertionAdapterOfWorkSpec;
    private final SharedSQLiteStatement __preparedStmtOfDelete;
    private final SharedSQLiteStatement __preparedStmtOfIncrementGeneration;
    private final SharedSQLiteStatement __preparedStmtOfIncrementPeriodCount;
    private final SharedSQLiteStatement __preparedStmtOfIncrementWorkSpecRunAttemptCount;
    private final SharedSQLiteStatement __preparedStmtOfMarkWorkSpecScheduled;
    private final SharedSQLiteStatement __preparedStmtOfPruneFinishedWorkWithZeroDependentsIgnoringKeepForAtLeast;
    private final SharedSQLiteStatement __preparedStmtOfResetScheduledState;
    private final SharedSQLiteStatement __preparedStmtOfResetWorkSpecNextScheduleTimeOverride;
    private final SharedSQLiteStatement __preparedStmtOfResetWorkSpecRunAttemptCount;
    private final SharedSQLiteStatement __preparedStmtOfSetCancelledState;
    private final SharedSQLiteStatement __preparedStmtOfSetLastEnqueueTime;
    private final SharedSQLiteStatement __preparedStmtOfSetNextScheduleTimeOverride;
    private final SharedSQLiteStatement __preparedStmtOfSetOutput;
    private final SharedSQLiteStatement __preparedStmtOfSetState;
    private final SharedSQLiteStatement __preparedStmtOfSetStopReason;
    private final EntityDeletionOrUpdateAdapter<WorkSpec> __updateAdapterOfWorkSpec;

    public WorkSpecDao_Impl(RoomDatabase __db) {
        this.__db = __db;
        this.__insertionAdapterOfWorkSpec = new EntityInsertionAdapter<WorkSpec>(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.1
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "INSERT OR IGNORE INTO `WorkSpec` (`id`,`state`,`worker_class_name`,`input_merger_class_name`,`input`,`output`,`initial_delay`,`interval_duration`,`flex_duration`,`run_attempt_count`,`backoff_policy`,`backoff_delay_duration`,`last_enqueue_time`,`minimum_retention_duration`,`schedule_requested_at`,`run_in_foreground`,`out_of_quota_policy`,`period_count`,`generation`,`next_schedule_time_override`,`next_schedule_time_override_generation`,`stop_reason`,`required_network_type`,`requires_charging`,`requires_device_idle`,`requires_battery_not_low`,`requires_storage_not_low`,`trigger_content_update_delay`,`trigger_max_content_delay`,`content_uri_triggers`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            }

            @Override // androidx.room.EntityInsertionAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, WorkSpec workSpec) throws IOException {
                if (workSpec.id != null) {
                    supportSQLiteStatement.bindString(1, workSpec.id);
                } else {
                    supportSQLiteStatement.bindNull(1);
                }
                WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                supportSQLiteStatement.bindLong(2, WorkTypeConverters.stateToInt(workSpec.state));
                if (workSpec.workerClassName != null) {
                    supportSQLiteStatement.bindString(3, workSpec.workerClassName);
                } else {
                    supportSQLiteStatement.bindNull(3);
                }
                if (workSpec.inputMergerClassName != null) {
                    supportSQLiteStatement.bindString(4, workSpec.inputMergerClassName);
                } else {
                    supportSQLiteStatement.bindNull(4);
                }
                byte[] byteArrayInternal = Data.toByteArrayInternal(workSpec.input);
                if (byteArrayInternal == null) {
                    supportSQLiteStatement.bindNull(5);
                } else {
                    supportSQLiteStatement.bindBlob(5, byteArrayInternal);
                }
                byte[] byteArrayInternal2 = Data.toByteArrayInternal(workSpec.output);
                if (byteArrayInternal2 == null) {
                    supportSQLiteStatement.bindNull(6);
                } else {
                    supportSQLiteStatement.bindBlob(6, byteArrayInternal2);
                }
                supportSQLiteStatement.bindLong(7, workSpec.initialDelay);
                supportSQLiteStatement.bindLong(8, workSpec.intervalDuration);
                supportSQLiteStatement.bindLong(9, workSpec.flexDuration);
                supportSQLiteStatement.bindLong(10, workSpec.runAttemptCount);
                WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                supportSQLiteStatement.bindLong(11, WorkTypeConverters.backoffPolicyToInt(workSpec.backoffPolicy));
                supportSQLiteStatement.bindLong(12, workSpec.backoffDelayDuration);
                supportSQLiteStatement.bindLong(13, workSpec.lastEnqueueTime);
                supportSQLiteStatement.bindLong(14, workSpec.minimumRetentionDuration);
                supportSQLiteStatement.bindLong(15, workSpec.scheduleRequestedAt);
                supportSQLiteStatement.bindLong(16, workSpec.expedited ? 1L : 0L);
                WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                supportSQLiteStatement.bindLong(17, WorkTypeConverters.outOfQuotaPolicyToInt(workSpec.outOfQuotaPolicy));
                supportSQLiteStatement.bindLong(18, workSpec.getPeriodCount());
                supportSQLiteStatement.bindLong(19, workSpec.getGeneration());
                supportSQLiteStatement.bindLong(20, workSpec.getNextScheduleTimeOverride());
                supportSQLiteStatement.bindLong(21, workSpec.getNextScheduleTimeOverrideGeneration());
                supportSQLiteStatement.bindLong(22, workSpec.getStopReason());
                Constraints constraints = workSpec.constraints;
                if (constraints != null) {
                    WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                    supportSQLiteStatement.bindLong(23, WorkTypeConverters.networkTypeToInt(constraints.getRequiredNetworkType()));
                    supportSQLiteStatement.bindLong(24, constraints.getRequiresCharging() ? 1L : 0L);
                    supportSQLiteStatement.bindLong(25, constraints.getRequiresDeviceIdle() ? 1L : 0L);
                    supportSQLiteStatement.bindLong(26, constraints.getRequiresBatteryNotLow() ? 1L : 0L);
                    supportSQLiteStatement.bindLong(27, constraints.getRequiresStorageNotLow() ? 1L : 0L);
                    supportSQLiteStatement.bindLong(28, constraints.getContentTriggerUpdateDelayMillis());
                    supportSQLiteStatement.bindLong(29, constraints.getContentTriggerMaxDelayMillis());
                    WorkTypeConverters workTypeConverters5 = WorkTypeConverters.INSTANCE;
                    byte[] ofTriggersToByteArray = WorkTypeConverters.setOfTriggersToByteArray(constraints.getContentUriTriggers());
                    if (ofTriggersToByteArray == null) {
                        supportSQLiteStatement.bindNull(30);
                        return;
                    } else {
                        supportSQLiteStatement.bindBlob(30, ofTriggersToByteArray);
                        return;
                    }
                }
                supportSQLiteStatement.bindNull(23);
                supportSQLiteStatement.bindNull(24);
                supportSQLiteStatement.bindNull(25);
                supportSQLiteStatement.bindNull(26);
                supportSQLiteStatement.bindNull(27);
                supportSQLiteStatement.bindNull(28);
                supportSQLiteStatement.bindNull(29);
                supportSQLiteStatement.bindNull(30);
            }
        };
        this.__updateAdapterOfWorkSpec = new EntityDeletionOrUpdateAdapter<WorkSpec>(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.2
            @Override // androidx.room.EntityDeletionOrUpdateAdapter, androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE OR ABORT `WorkSpec` SET `id` = ?,`state` = ?,`worker_class_name` = ?,`input_merger_class_name` = ?,`input` = ?,`output` = ?,`initial_delay` = ?,`interval_duration` = ?,`flex_duration` = ?,`run_attempt_count` = ?,`backoff_policy` = ?,`backoff_delay_duration` = ?,`last_enqueue_time` = ?,`minimum_retention_duration` = ?,`schedule_requested_at` = ?,`run_in_foreground` = ?,`out_of_quota_policy` = ?,`period_count` = ?,`generation` = ?,`next_schedule_time_override` = ?,`next_schedule_time_override_generation` = ?,`stop_reason` = ?,`required_network_type` = ?,`requires_charging` = ?,`requires_device_idle` = ?,`requires_battery_not_low` = ?,`requires_storage_not_low` = ?,`trigger_content_update_delay` = ?,`trigger_max_content_delay` = ?,`content_uri_triggers` = ? WHERE `id` = ?";
            }

            @Override // androidx.room.EntityDeletionOrUpdateAdapter
            public void bind(SupportSQLiteStatement supportSQLiteStatement, WorkSpec workSpec) throws IOException {
                if (workSpec.id != null) {
                    supportSQLiteStatement.bindString(1, workSpec.id);
                } else {
                    supportSQLiteStatement.bindNull(1);
                }
                WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                supportSQLiteStatement.bindLong(2, WorkTypeConverters.stateToInt(workSpec.state));
                if (workSpec.workerClassName != null) {
                    supportSQLiteStatement.bindString(3, workSpec.workerClassName);
                } else {
                    supportSQLiteStatement.bindNull(3);
                }
                if (workSpec.inputMergerClassName != null) {
                    supportSQLiteStatement.bindString(4, workSpec.inputMergerClassName);
                } else {
                    supportSQLiteStatement.bindNull(4);
                }
                byte[] byteArrayInternal = Data.toByteArrayInternal(workSpec.input);
                if (byteArrayInternal == null) {
                    supportSQLiteStatement.bindNull(5);
                } else {
                    supportSQLiteStatement.bindBlob(5, byteArrayInternal);
                }
                byte[] byteArrayInternal2 = Data.toByteArrayInternal(workSpec.output);
                if (byteArrayInternal2 == null) {
                    supportSQLiteStatement.bindNull(6);
                } else {
                    supportSQLiteStatement.bindBlob(6, byteArrayInternal2);
                }
                supportSQLiteStatement.bindLong(7, workSpec.initialDelay);
                supportSQLiteStatement.bindLong(8, workSpec.intervalDuration);
                supportSQLiteStatement.bindLong(9, workSpec.flexDuration);
                supportSQLiteStatement.bindLong(10, workSpec.runAttemptCount);
                WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                supportSQLiteStatement.bindLong(11, WorkTypeConverters.backoffPolicyToInt(workSpec.backoffPolicy));
                supportSQLiteStatement.bindLong(12, workSpec.backoffDelayDuration);
                supportSQLiteStatement.bindLong(13, workSpec.lastEnqueueTime);
                supportSQLiteStatement.bindLong(14, workSpec.minimumRetentionDuration);
                supportSQLiteStatement.bindLong(15, workSpec.scheduleRequestedAt);
                supportSQLiteStatement.bindLong(16, workSpec.expedited ? 1L : 0L);
                WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                supportSQLiteStatement.bindLong(17, WorkTypeConverters.outOfQuotaPolicyToInt(workSpec.outOfQuotaPolicy));
                supportSQLiteStatement.bindLong(18, workSpec.getPeriodCount());
                supportSQLiteStatement.bindLong(19, workSpec.getGeneration());
                supportSQLiteStatement.bindLong(20, workSpec.getNextScheduleTimeOverride());
                supportSQLiteStatement.bindLong(21, workSpec.getNextScheduleTimeOverrideGeneration());
                supportSQLiteStatement.bindLong(22, workSpec.getStopReason());
                Constraints constraints = workSpec.constraints;
                if (constraints == null) {
                    supportSQLiteStatement.bindNull(23);
                    supportSQLiteStatement.bindNull(24);
                    supportSQLiteStatement.bindNull(25);
                    supportSQLiteStatement.bindNull(26);
                    supportSQLiteStatement.bindNull(27);
                    supportSQLiteStatement.bindNull(28);
                    supportSQLiteStatement.bindNull(29);
                    supportSQLiteStatement.bindNull(30);
                } else {
                    WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                    supportSQLiteStatement.bindLong(23, WorkTypeConverters.networkTypeToInt(constraints.getRequiredNetworkType()));
                    supportSQLiteStatement.bindLong(24, constraints.getRequiresCharging() ? 1L : 0L);
                    supportSQLiteStatement.bindLong(25, constraints.getRequiresDeviceIdle() ? 1L : 0L);
                    supportSQLiteStatement.bindLong(26, constraints.getRequiresBatteryNotLow() ? 1L : 0L);
                    supportSQLiteStatement.bindLong(27, constraints.getRequiresStorageNotLow() ? 1L : 0L);
                    supportSQLiteStatement.bindLong(28, constraints.getContentTriggerUpdateDelayMillis());
                    supportSQLiteStatement.bindLong(29, constraints.getContentTriggerMaxDelayMillis());
                    WorkTypeConverters workTypeConverters5 = WorkTypeConverters.INSTANCE;
                    byte[] ofTriggersToByteArray = WorkTypeConverters.setOfTriggersToByteArray(constraints.getContentUriTriggers());
                    if (ofTriggersToByteArray == null) {
                        supportSQLiteStatement.bindNull(30);
                    } else {
                        supportSQLiteStatement.bindBlob(30, ofTriggersToByteArray);
                    }
                }
                if (workSpec.id != null) {
                    supportSQLiteStatement.bindString(31, workSpec.id);
                } else {
                    supportSQLiteStatement.bindNull(31);
                }
            }
        };
        this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.3
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM workspec WHERE id=?";
            }
        };
        this.__preparedStmtOfSetState = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.4
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET state=? WHERE id=?";
            }
        };
        this.__preparedStmtOfSetCancelledState = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.5
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET stop_reason = CASE WHEN state=1 THEN 1 ELSE -256 END, state=5 WHERE id=?";
            }
        };
        this.__preparedStmtOfIncrementPeriodCount = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.6
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET period_count=period_count+1 WHERE id=?";
            }
        };
        this.__preparedStmtOfSetOutput = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.7
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET output=? WHERE id=?";
            }
        };
        this.__preparedStmtOfSetLastEnqueueTime = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.8
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET last_enqueue_time=? WHERE id=?";
            }
        };
        this.__preparedStmtOfIncrementWorkSpecRunAttemptCount = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.9
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET run_attempt_count=run_attempt_count+1 WHERE id=?";
            }
        };
        this.__preparedStmtOfResetWorkSpecRunAttemptCount = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.10
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET run_attempt_count=0 WHERE id=?";
            }
        };
        this.__preparedStmtOfSetNextScheduleTimeOverride = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.11
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET next_schedule_time_override=? WHERE id=?";
            }
        };
        this.__preparedStmtOfResetWorkSpecNextScheduleTimeOverride = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.12
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET next_schedule_time_override=9223372036854775807 WHERE (id=? AND next_schedule_time_override_generation=?)";
            }
        };
        this.__preparedStmtOfMarkWorkSpecScheduled = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.13
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET schedule_requested_at=? WHERE id=?";
            }
        };
        this.__preparedStmtOfResetScheduledState = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.14
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET schedule_requested_at=-1 WHERE state NOT IN (2, 3, 5)";
            }
        };
        this.__preparedStmtOfPruneFinishedWorkWithZeroDependentsIgnoringKeepForAtLeast = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.15
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "DELETE FROM workspec WHERE state IN (2, 3, 5) AND (SELECT COUNT(*)=0 FROM dependency WHERE     prerequisite_id=id AND     work_spec_id NOT IN         (SELECT id FROM workspec WHERE state IN (2, 3, 5)))";
            }
        };
        this.__preparedStmtOfIncrementGeneration = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.16
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET generation=generation+1 WHERE id=?";
            }
        };
        this.__preparedStmtOfSetStopReason = new SharedSQLiteStatement(__db) { // from class: androidx.work.impl.model.WorkSpecDao_Impl.17
            @Override // androidx.room.SharedSQLiteStatement
            public String createQuery() {
                return "UPDATE workspec SET stop_reason=? WHERE id=?";
            }
        };
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void insertWorkSpec(final WorkSpec workSpec) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfWorkSpec.insert((EntityInsertionAdapter<WorkSpec>) workSpec);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void updateWorkSpec(final WorkSpec workSpec) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfWorkSpec.handle(workSpec);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void delete(final String id) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfDelete.acquire();
        if (id == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindString(1, id);
        }
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDelete.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public int setState(final WorkInfo.State state, final String id) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfSetState.acquire();
        WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
        int _tmp = WorkTypeConverters.stateToInt(state);
        _stmt.bindLong(1, _tmp);
        if (id == null) {
            _stmt.bindNull(2);
        } else {
            _stmt.bindString(2, id);
        }
        this.__db.beginTransaction();
        try {
            int _result = _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
            return _result;
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfSetState.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public int setCancelledState(final String id) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfSetCancelledState.acquire();
        if (id == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindString(1, id);
        }
        this.__db.beginTransaction();
        try {
            int _result = _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
            return _result;
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfSetCancelledState.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void incrementPeriodCount(final String id) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfIncrementPeriodCount.acquire();
        if (id == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindString(1, id);
        }
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfIncrementPeriodCount.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void setOutput(final String id, final Data output) throws IOException {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfSetOutput.acquire();
        byte[] _tmp = Data.toByteArrayInternal(output);
        if (_tmp == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindBlob(1, _tmp);
        }
        if (id == null) {
            _stmt.bindNull(2);
        } else {
            _stmt.bindString(2, id);
        }
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfSetOutput.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void setLastEnqueueTime(final String id, final long enqueueTime) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfSetLastEnqueueTime.acquire();
        _stmt.bindLong(1, enqueueTime);
        if (id == null) {
            _stmt.bindNull(2);
        } else {
            _stmt.bindString(2, id);
        }
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfSetLastEnqueueTime.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public int incrementWorkSpecRunAttemptCount(final String id) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfIncrementWorkSpecRunAttemptCount.acquire();
        if (id == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindString(1, id);
        }
        this.__db.beginTransaction();
        try {
            int _result = _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
            return _result;
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfIncrementWorkSpecRunAttemptCount.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public int resetWorkSpecRunAttemptCount(final String id) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfResetWorkSpecRunAttemptCount.acquire();
        if (id == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindString(1, id);
        }
        this.__db.beginTransaction();
        try {
            int _result = _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
            return _result;
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfResetWorkSpecRunAttemptCount.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void setNextScheduleTimeOverride(final String id, final long nextScheduleTimeOverrideMillis) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfSetNextScheduleTimeOverride.acquire();
        _stmt.bindLong(1, nextScheduleTimeOverrideMillis);
        if (id == null) {
            _stmt.bindNull(2);
        } else {
            _stmt.bindString(2, id);
        }
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfSetNextScheduleTimeOverride.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void resetWorkSpecNextScheduleTimeOverride(final String id, final int overrideGeneration) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfResetWorkSpecNextScheduleTimeOverride.acquire();
        if (id == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindString(1, id);
        }
        _stmt.bindLong(2, overrideGeneration);
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfResetWorkSpecNextScheduleTimeOverride.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public int markWorkSpecScheduled(final String id, final long startTime) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfMarkWorkSpecScheduled.acquire();
        _stmt.bindLong(1, startTime);
        if (id == null) {
            _stmt.bindNull(2);
        } else {
            _stmt.bindString(2, id);
        }
        this.__db.beginTransaction();
        try {
            int _result = _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
            return _result;
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfMarkWorkSpecScheduled.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public int resetScheduledState() {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfResetScheduledState.acquire();
        this.__db.beginTransaction();
        try {
            int _result = _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
            return _result;
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfResetScheduledState.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void pruneFinishedWorkWithZeroDependentsIgnoringKeepForAtLeast() {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfPruneFinishedWorkWithZeroDependentsIgnoringKeepForAtLeast.acquire();
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfPruneFinishedWorkWithZeroDependentsIgnoringKeepForAtLeast.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void incrementGeneration(final String id) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfIncrementGeneration.acquire();
        if (id == null) {
            _stmt.bindNull(1);
        } else {
            _stmt.bindString(1, id);
        }
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfIncrementGeneration.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public void setStopReason(final String id, final int stopReason) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement _stmt = this.__preparedStmtOfSetStopReason.acquire();
        _stmt.bindLong(1, stopReason);
        if (id == null) {
            _stmt.bindNull(2);
        } else {
            _stmt.bindString(2, id);
        }
        this.__db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfSetStopReason.release(_stmt);
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public WorkSpec getWorkSpec(final String id) throws Throwable {
        RoomSQLiteQuery _statement;
        WorkSpec _result;
        String _tmpId;
        String _tmpWorkerClassName;
        String _tmpInputMergerClassName;
        byte[] _tmp_1;
        byte[] _tmp_2;
        byte[] _tmp_11;
        RoomSQLiteQuery _statement2 = RoomSQLiteQuery.acquire("SELECT * FROM workspec WHERE id=?", 1);
        if (id == null) {
            _statement2.bindNull(1);
        } else {
            _statement2.bindString(1, id);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement2, false, null);
        try {
            int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
            int _cursorIndexOfWorkerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "worker_class_name");
            int _cursorIndexOfInputMergerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "input_merger_class_name");
            int _cursorIndexOfInput = CursorUtil.getColumnIndexOrThrow(_cursor, "input");
            int _cursorIndexOfOutput = CursorUtil.getColumnIndexOrThrow(_cursor, "output");
            int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndexOrThrow(_cursor, "initial_delay");
            int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "interval_duration");
            int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "flex_duration");
            int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "run_attempt_count");
            int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_policy");
            int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_delay_duration");
            try {
                int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndexOrThrow(_cursor, "last_enqueue_time");
                _statement = _statement2;
                try {
                    int _cursorIndexOfMinimumRetentionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "minimum_retention_duration");
                    try {
                        int _cursorIndexOfScheduleRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_requested_at");
                        int _cursorIndexOfExpedited = CursorUtil.getColumnIndexOrThrow(_cursor, "run_in_foreground");
                        int _cursorIndexOfOutOfQuotaPolicy = CursorUtil.getColumnIndexOrThrow(_cursor, "out_of_quota_policy");
                        int _cursorIndexOfPeriodCount = CursorUtil.getColumnIndexOrThrow(_cursor, "period_count");
                        int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
                        int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override");
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override_generation");
                        int _cursorIndexOfStopReason = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_reason");
                        int _cursorIndexOfRequiredNetworkType = CursorUtil.getColumnIndexOrThrow(_cursor, "required_network_type");
                        int _cursorIndexOfRequiresCharging = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_charging");
                        int _cursorIndexOfRequiresDeviceIdle = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_device_idle");
                        int _cursorIndexOfRequiresBatteryNotLow = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_battery_not_low");
                        int _cursorIndexOfRequiresStorageNotLow = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_storage_not_low");
                        int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_content_update_delay");
                        int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_max_content_delay");
                        int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndexOrThrow(_cursor, "content_uri_triggers");
                        if (_cursor.moveToFirst()) {
                            if (_cursor.isNull(_cursorIndexOfId)) {
                                _tmpId = null;
                            } else {
                                String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                                _tmpId = _tmpId2;
                            }
                            int _tmp = _cursor.getInt(_cursorIndexOfState);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(_cursorIndexOfWorkerClassName)) {
                                _tmpWorkerClassName = null;
                            } else {
                                String _tmpWorkerClassName2 = _cursor.getString(_cursorIndexOfWorkerClassName);
                                _tmpWorkerClassName = _tmpWorkerClassName2;
                            }
                            if (_cursor.isNull(_cursorIndexOfInputMergerClassName)) {
                                _tmpInputMergerClassName = null;
                            } else {
                                String _tmpInputMergerClassName2 = _cursor.getString(_cursorIndexOfInputMergerClassName);
                                _tmpInputMergerClassName = _tmpInputMergerClassName2;
                            }
                            if (_cursor.isNull(_cursorIndexOfInput)) {
                                _tmp_1 = null;
                            } else {
                                byte[] _tmp_12 = _cursor.getBlob(_cursorIndexOfInput);
                                _tmp_1 = _tmp_12;
                            }
                            Data _tmpInput = Data.fromByteArray(_tmp_1);
                            if (_cursor.isNull(_cursorIndexOfOutput)) {
                                _tmp_2 = null;
                            } else {
                                byte[] _tmp_22 = _cursor.getBlob(_cursorIndexOfOutput);
                                _tmp_2 = _tmp_22;
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_2);
                            long _tmpInitialDelay = _cursor.getLong(_cursorIndexOfInitialDelay);
                            long _tmpIntervalDuration = _cursor.getLong(_cursorIndexOfIntervalDuration);
                            long _tmpFlexDuration = _cursor.getLong(_cursorIndexOfFlexDuration);
                            int _tmpRunAttemptCount = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                            int _tmp_3 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_3);
                            long _tmpBackoffDelayDuration = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                            long _tmpLastEnqueueTime = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                            long _tmpMinimumRetentionDuration = _cursor.getLong(_cursorIndexOfMinimumRetentionDuration);
                            long _tmpScheduleRequestedAt = _cursor.getLong(_cursorIndexOfScheduleRequestedAt);
                            int _tmp_4 = _cursor.getInt(_cursorIndexOfExpedited);
                            boolean _tmpExpedited = _tmp_4 != 0;
                            int _tmp_5 = _cursor.getInt(_cursorIndexOfOutOfQuotaPolicy);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            OutOfQuotaPolicy _tmpOutOfQuotaPolicy = WorkTypeConverters.intToOutOfQuotaPolicy(_tmp_5);
                            int _tmpPeriodCount = _cursor.getInt(_cursorIndexOfPeriodCount);
                            int _tmpGeneration = _cursor.getInt(_cursorIndexOfGeneration);
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(_cursorIndexOfNextScheduleTimeOverride);
                            int _tmpNextScheduleTimeOverrideGeneration = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverrideGeneration);
                            int _tmpStopReason = _cursor.getInt(_cursorIndexOfStopReason);
                            int _tmp_6 = _cursor.getInt(_cursorIndexOfRequiredNetworkType);
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_6);
                            int _tmp_7 = _cursor.getInt(_cursorIndexOfRequiresCharging);
                            boolean _tmpRequiresCharging = _tmp_7 != 0;
                            int _tmp_8 = _cursor.getInt(_cursorIndexOfRequiresDeviceIdle);
                            boolean _tmpRequiresDeviceIdle = _tmp_8 != 0;
                            int _tmp_9 = _cursor.getInt(_cursorIndexOfRequiresBatteryNotLow);
                            boolean _tmpRequiresBatteryNotLow = _tmp_9 != 0;
                            int _tmp_10 = _cursor.getInt(_cursorIndexOfRequiresStorageNotLow);
                            boolean _tmpRequiresStorageNotLow = _tmp_10 != 0;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis);
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(_cursorIndexOfContentTriggerMaxDelayMillis);
                            if (_cursor.isNull(_cursorIndexOfContentUriTriggers)) {
                                _tmp_11 = null;
                            } else {
                                _tmp_11 = _cursor.getBlob(_cursorIndexOfContentUriTriggers);
                            }
                            WorkTypeConverters workTypeConverters5 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_11);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            _result = new WorkSpec(_tmpId, _tmpState, _tmpWorkerClassName, _tmpInputMergerClassName, _tmpInput, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpMinimumRetentionDuration, _tmpScheduleRequestedAt, _tmpExpedited, _tmpOutOfQuotaPolicy, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpNextScheduleTimeOverrideGeneration, _tmpStopReason);
                        } else {
                            _result = null;
                        }
                        _cursor.close();
                        _statement.release();
                        return _result;
                    } catch (Throwable th) {
                        th = th;
                        _cursor.close();
                        _statement.release();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
                _statement = _statement2;
            }
        } catch (Throwable th4) {
            th = th4;
            _statement = _statement2;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec.IdAndState> getWorkSpecIdAndStatesForName(final String name) {
        String _tmpId;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id, state FROM workspec WHERE id IN (SELECT work_spec_id FROM workname WHERE name=?)", 1);
        if (name == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, name);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            List<WorkSpec.IdAndState> _result = new ArrayList<>(_cursor.getCount());
            while (_cursor.moveToNext()) {
                if (_cursor.isNull(0)) {
                    _tmpId = null;
                } else {
                    _tmpId = _cursor.getString(0);
                }
                int _tmp = _cursor.getInt(1);
                WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                WorkSpec.IdAndState _item = new WorkSpec.IdAndState(_tmpId, _tmpState);
                _result.add(_item);
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<String> getAllWorkSpecIds() {
        String _item;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id FROM workspec", 0);
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

    @Override // androidx.work.impl.model.WorkSpecDao
    public LiveData<List<String>> getAllWorkSpecIdsLiveData() {
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id FROM workspec", 0);
        return this.__db.getInvalidationTracker().createLiveData(new String[]{"workspec"}, true, new Callable<List<String>>() { // from class: androidx.work.impl.model.WorkSpecDao_Impl.18
            @Override // java.util.concurrent.Callable
            public List<String> call() throws Exception {
                String _item;
                WorkSpecDao_Impl.this.__db.beginTransaction();
                try {
                    Cursor _cursor = DBUtil.query(WorkSpecDao_Impl.this.__db, _statement, false, null);
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
                        WorkSpecDao_Impl.this.__db.setTransactionSuccessful();
                        return _result;
                    } finally {
                        _cursor.close();
                    }
                } finally {
                    WorkSpecDao_Impl.this.__db.endTransaction();
                }
            }

            protected void finalize() {
                _statement.release();
            }
        });
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public WorkInfo.State getState(final String id) {
        WorkInfo.State _result;
        Integer _tmp;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT state FROM workspec WHERE id=?", 1);
        if (id == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, id);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            if (_cursor.moveToFirst()) {
                if (_cursor.isNull(0)) {
                    _tmp = null;
                } else {
                    _tmp = Integer.valueOf(_cursor.getInt(0));
                }
                if (_tmp == null) {
                    _result = null;
                } else {
                    WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                    _result = WorkTypeConverters.intToState(_tmp.intValue());
                }
            } else {
                _result = null;
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public WorkSpec.WorkInfoPojo getWorkStatusPojoForId(final String id) throws Throwable {
        WorkSpec.WorkInfoPojo _result;
        String _tmpId;
        byte[] _tmp_1;
        byte[] _tmp_8;
        ArrayList<Data> _tmpProgressCollection_1;
        String _sql;
        String _sql2 = "SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id=?";
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id=?", 1);
        if (id == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, id);
        }
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            Cursor _cursor = DBUtil.query(this.__db, _statement, true, null);
            int _cursorIndexOfId = 0;
            try {
                try {
                    HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                    HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                    while (_cursor.moveToNext()) {
                        String _tmpKey = _cursor.getString(0);
                        HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                        int _cursorIndexOfId2 = _cursorIndexOfId;
                        ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                        if (_tmpTagsCollection == null) {
                            try {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                            } catch (Throwable th) {
                                th = th;
                                _cursor.close();
                                _statement.release();
                                throw th;
                            }
                        }
                        String _tmpKey_1 = _cursor.getString(0);
                        HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                        ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                        if (_tmpProgressCollection != null) {
                            _sql = _sql2;
                        } else {
                            ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                            _sql = _sql2;
                            try {
                                _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                            } catch (Throwable th2) {
                                th = th2;
                                _cursor.close();
                                _statement.release();
                                throw th;
                            }
                        }
                        _collectionProgress = _collectionProgress2;
                        _cursorIndexOfId = _cursorIndexOfId2;
                        _sql2 = _sql;
                        _collectionTags = _collectionTags2;
                    }
                    HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                    HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                    _cursor.moveToPosition(-1);
                    __fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                    __fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                    if (_cursor.moveToFirst()) {
                        if (_cursor.isNull(0)) {
                            _tmpId = null;
                        } else {
                            String _tmpId2 = _cursor.getString(0);
                            _tmpId = _tmpId2;
                        }
                        int _tmp = _cursor.getInt(1);
                        WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                        WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                        if (_cursor.isNull(2)) {
                            _tmp_1 = null;
                        } else {
                            _tmp_1 = _cursor.getBlob(2);
                        }
                        Data _tmpOutput = Data.fromByteArray(_tmp_1);
                        int _tmpRunAttemptCount = _cursor.getInt(3);
                        int _tmpGeneration = _cursor.getInt(4);
                        long _tmpInitialDelay = _cursor.getLong(13);
                        long _tmpIntervalDuration = _cursor.getLong(14);
                        long _tmpFlexDuration = _cursor.getLong(15);
                        int _tmp_2 = _cursor.getInt(16);
                        WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                        BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                        long _tmpBackoffDelayDuration = _cursor.getLong(17);
                        long _tmpLastEnqueueTime = _cursor.getLong(18);
                        int _tmpPeriodCount = _cursor.getInt(19);
                        long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                        int _tmpStopReason = _cursor.getInt(21);
                        int _tmp_3 = _cursor.getInt(5);
                        WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                        NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                        int _tmp_4 = _cursor.getInt(6);
                        boolean _tmpRequiresCharging = _tmp_4 != 0;
                        int _tmp_5 = _cursor.getInt(7);
                        boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                        int _tmp_6 = _cursor.getInt(8);
                        boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                        int _tmp_7 = _cursor.getInt(9);
                        boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                        long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                        long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                        if (_cursor.isNull(12)) {
                            _tmp_8 = null;
                        } else {
                            _tmp_8 = _cursor.getBlob(12);
                        }
                        WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                        Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                        Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                        String _tmpKey_2 = _cursor.getString(0);
                        ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                        if (_tmpTagsCollection_1 == null) {
                            _tmpTagsCollection_1 = new ArrayList<>();
                        }
                        String _tmpKey_3 = _cursor.getString(0);
                        ArrayList<Data> _tmpProgressCollection_12 = _collectionProgress3.get(_tmpKey_3);
                        if (_tmpProgressCollection_12 != null) {
                            _tmpProgressCollection_1 = _tmpProgressCollection_12;
                        } else {
                            _tmpProgressCollection_1 = new ArrayList<>();
                        }
                        _result = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                    } else {
                        _result = null;
                    }
                    this.__db.setTransactionSuccessful();
                    _cursor.close();
                    _statement.release();
                    this.__db.endTransaction();
                    return _result;
                } catch (Throwable th3) {
                    th = th3;
                    this.__db.endTransaction();
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
            }
        } catch (Throwable th5) {
            th = th5;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec.WorkInfoPojo> getWorkStatusPojoForIds(final List<String> ids) throws Throwable {
        StringBuilder _stringBuilder;
        int _inputSize;
        StringBuilder _stringBuilder2 = StringUtil.newStringBuilder();
        _stringBuilder2.append("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN (");
        int _inputSize2 = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder2, _inputSize2);
        _stringBuilder2.append(")");
        String _sql = _stringBuilder2.toString();
        int _argCount = _inputSize2 + 0;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
        int _argIndex = 1;
        for (String _item : ids) {
            if (_item == null) {
                _statement.bindNull(_argIndex);
            } else {
                _statement.bindString(_argIndex, _item);
            }
            _argIndex++;
        }
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            Cursor _cursor = DBUtil.query(this.__db, _statement, true, null);
            int _cursorIndexOfId = 0;
            try {
                try {
                    HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                    HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                    while (_cursor.moveToNext()) {
                        String _tmpKey = _cursor.getString(0);
                        HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                        int _cursorIndexOfId2 = _cursorIndexOfId;
                        ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                        if (_tmpTagsCollection == null) {
                            try {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _stringBuilder = _stringBuilder2;
                                try {
                                    _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                                } catch (Throwable th) {
                                    th = th;
                                    _cursor.close();
                                    _statement.release();
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        } else {
                            _stringBuilder = _stringBuilder2;
                        }
                        try {
                            String _tmpKey_1 = _cursor.getString(0);
                            HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                            ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                            if (_tmpProgressCollection == null) {
                                ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                                _inputSize = _inputSize2;
                                try {
                                    _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                                } catch (Throwable th3) {
                                    th = th3;
                                    _cursor.close();
                                    _statement.release();
                                    throw th;
                                }
                            } else {
                                _inputSize = _inputSize2;
                            }
                            _collectionProgress = _collectionProgress2;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _stringBuilder2 = _stringBuilder;
                            _inputSize2 = _inputSize;
                            _collectionTags = _collectionTags2;
                        } catch (Throwable th4) {
                            th = th4;
                            _cursor.close();
                            _statement.release();
                            throw th;
                        }
                    }
                    HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                    HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                    _cursor.moveToPosition(-1);
                    __fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                    __fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                    List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                    while (_cursor.moveToNext()) {
                        String _tmpId = _cursor.isNull(0) ? null : _cursor.getString(0);
                        int _tmp = _cursor.getInt(1);
                        WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                        WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                        byte[] _tmp_1 = _cursor.isNull(2) ? null : _cursor.getBlob(2);
                        Data _tmpOutput = Data.fromByteArray(_tmp_1);
                        int _tmpRunAttemptCount = _cursor.getInt(3);
                        int _tmpGeneration = _cursor.getInt(4);
                        long _tmpInitialDelay = _cursor.getLong(13);
                        long _tmpIntervalDuration = _cursor.getLong(14);
                        long _tmpFlexDuration = _cursor.getLong(15);
                        int _tmp_2 = _cursor.getInt(16);
                        WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                        BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                        long _tmpBackoffDelayDuration = _cursor.getLong(17);
                        long _tmpLastEnqueueTime = _cursor.getLong(18);
                        int _tmpPeriodCount = _cursor.getInt(19);
                        long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                        int _tmpStopReason = _cursor.getInt(21);
                        int _tmp_3 = _cursor.getInt(5);
                        WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                        NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                        int _tmp_4 = _cursor.getInt(6);
                        boolean _tmpRequiresCharging = _tmp_4 != 0;
                        int _tmp_5 = _cursor.getInt(7);
                        boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                        int _tmp_6 = _cursor.getInt(8);
                        boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                        int _tmp_7 = _cursor.getInt(9);
                        boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                        long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                        long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                        byte[] _tmp_8 = _cursor.isNull(12) ? null : _cursor.getBlob(12);
                        WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                        Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                        Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                        String _tmpKey_2 = _cursor.getString(0);
                        ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                        if (_tmpTagsCollection_1 == null) {
                            _tmpTagsCollection_1 = new ArrayList<>();
                        }
                        String _tmpKey_3 = _cursor.getString(0);
                        ArrayList<Data> _tmpProgressCollection_1 = _collectionProgress3.get(_tmpKey_3);
                        if (_tmpProgressCollection_1 == null) {
                            _tmpProgressCollection_1 = new ArrayList<>();
                        }
                        WorkSpec.WorkInfoPojo _item_1 = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                        _result.add(_item_1);
                        _collectionProgress3 = _collectionProgress3;
                    }
                    this.__db.setTransactionSuccessful();
                    _cursor.close();
                    _statement.release();
                    this.__db.endTransaction();
                    return _result;
                } catch (Throwable th5) {
                    th = th5;
                    this.__db.endTransaction();
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
            }
        } catch (Throwable th7) {
            th = th7;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public LiveData<List<WorkSpec.WorkInfoPojo>> getWorkStatusPojoLiveDataForIds(final List<String> ids) {
        StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN (");
        int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        String _sql = _stringBuilder.toString();
        int _argCount = _inputSize + 0;
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
        int _argIndex = 1;
        for (String _item : ids) {
            if (_item == null) {
                _statement.bindNull(_argIndex);
            } else {
                _statement.bindString(_argIndex, _item);
            }
            _argIndex++;
        }
        return this.__db.getInvalidationTracker().createLiveData(new String[]{"WorkTag", "WorkProgress", "workspec"}, true, new Callable<List<WorkSpec.WorkInfoPojo>>() { // from class: androidx.work.impl.model.WorkSpecDao_Impl.19
            @Override // java.util.concurrent.Callable
            public List<WorkSpec.WorkInfoPojo> call() throws Exception {
                String _tmpId;
                byte[] _tmp_1;
                byte[] _tmp_8;
                int _cursorIndexOfState;
                int _cursorIndexOfOutput;
                WorkSpecDao_Impl.this.__db.beginTransaction();
                try {
                    Cursor _cursor = DBUtil.query(WorkSpecDao_Impl.this.__db, _statement, true, null);
                    int _cursorIndexOfId = 0;
                    int _cursorIndexOfState2 = 1;
                    int _cursorIndexOfOutput2 = 2;
                    try {
                        HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                        HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                        while (_cursor.moveToNext()) {
                            String _tmpKey = _cursor.getString(0);
                            HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                            int _cursorIndexOfId2 = _cursorIndexOfId;
                            ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                            if (_tmpTagsCollection != null) {
                                _cursorIndexOfState = _cursorIndexOfState2;
                            } else {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _cursorIndexOfState = _cursorIndexOfState2;
                                _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                            }
                            String _tmpKey_1 = _cursor.getString(0);
                            HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                            ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                            if (_tmpProgressCollection != null) {
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                            } else {
                                ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                                _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                            }
                            _collectionProgress = _collectionProgress2;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _cursorIndexOfState2 = _cursorIndexOfState;
                            _cursorIndexOfOutput2 = _cursorIndexOfOutput;
                            _collectionTags = _collectionTags2;
                        }
                        HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                        HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                        _cursor.moveToPosition(-1);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                        List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                        while (_cursor.moveToNext()) {
                            if (_cursor.isNull(0)) {
                                _tmpId = null;
                            } else {
                                _tmpId = _cursor.getString(0);
                            }
                            int _tmp = _cursor.getInt(1);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(2)) {
                                _tmp_1 = null;
                            } else {
                                _tmp_1 = _cursor.getBlob(2);
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_1);
                            int _tmpRunAttemptCount = _cursor.getInt(3);
                            int _tmpGeneration = _cursor.getInt(4);
                            long _tmpInitialDelay = _cursor.getLong(13);
                            long _tmpIntervalDuration = _cursor.getLong(14);
                            long _tmpFlexDuration = _cursor.getLong(15);
                            int _tmp_2 = _cursor.getInt(16);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                            long _tmpBackoffDelayDuration = _cursor.getLong(17);
                            long _tmpLastEnqueueTime = _cursor.getLong(18);
                            int _tmpPeriodCount = _cursor.getInt(19);
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                            int _tmpStopReason = _cursor.getInt(21);
                            int _tmp_3 = _cursor.getInt(5);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                            int _tmp_4 = _cursor.getInt(6);
                            boolean _tmpRequiresCharging = _tmp_4 != 0;
                            int _tmp_5 = _cursor.getInt(7);
                            boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                            int _tmp_6 = _cursor.getInt(8);
                            boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                            int _tmp_7 = _cursor.getInt(9);
                            boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                            if (_cursor.isNull(12)) {
                                _tmp_8 = null;
                            } else {
                                _tmp_8 = _cursor.getBlob(12);
                            }
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            String _tmpKey_2 = _cursor.getString(0);
                            ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                            if (_tmpTagsCollection_1 == null) {
                                _tmpTagsCollection_1 = new ArrayList<>();
                            }
                            HashMap<String, ArrayList<String>> _collectionTags4 = _collectionTags3;
                            String _tmpKey_3 = _cursor.getString(0);
                            ArrayList<Data> _tmpProgressCollection_1 = _collectionProgress3.get(_tmpKey_3);
                            if (_tmpProgressCollection_1 == null) {
                                _tmpProgressCollection_1 = new ArrayList<>();
                            }
                            WorkSpec.WorkInfoPojo _item_1 = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                            _result.add(_item_1);
                            _collectionProgress3 = _collectionProgress3;
                            _collectionTags3 = _collectionTags4;
                        }
                        WorkSpecDao_Impl.this.__db.setTransactionSuccessful();
                        return _result;
                    } finally {
                        _cursor.close();
                    }
                } finally {
                    WorkSpecDao_Impl.this.__db.endTransaction();
                }
            }

            protected void finalize() {
                _statement.release();
            }
        });
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public Flow<List<WorkSpec.WorkInfoPojo>> getWorkStatusPojoFlowDataForIds(final List<String> ids) {
        StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN (");
        int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        String _sql = _stringBuilder.toString();
        int _argCount = _inputSize + 0;
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
        int _argIndex = 1;
        for (String _item : ids) {
            if (_item == null) {
                _statement.bindNull(_argIndex);
            } else {
                _statement.bindString(_argIndex, _item);
            }
            _argIndex++;
        }
        return CoroutinesRoom.createFlow(this.__db, true, new String[]{"WorkTag", "WorkProgress", "workspec"}, new Callable<List<WorkSpec.WorkInfoPojo>>() { // from class: androidx.work.impl.model.WorkSpecDao_Impl.20
            @Override // java.util.concurrent.Callable
            public List<WorkSpec.WorkInfoPojo> call() throws Exception {
                String _tmpId;
                byte[] _tmp_1;
                byte[] _tmp_8;
                int _cursorIndexOfState;
                int _cursorIndexOfOutput;
                WorkSpecDao_Impl.this.__db.beginTransaction();
                try {
                    Cursor _cursor = DBUtil.query(WorkSpecDao_Impl.this.__db, _statement, true, null);
                    int _cursorIndexOfId = 0;
                    int _cursorIndexOfState2 = 1;
                    int _cursorIndexOfOutput2 = 2;
                    try {
                        HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                        HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                        while (_cursor.moveToNext()) {
                            String _tmpKey = _cursor.getString(0);
                            HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                            int _cursorIndexOfId2 = _cursorIndexOfId;
                            ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                            if (_tmpTagsCollection != null) {
                                _cursorIndexOfState = _cursorIndexOfState2;
                            } else {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _cursorIndexOfState = _cursorIndexOfState2;
                                _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                            }
                            String _tmpKey_1 = _cursor.getString(0);
                            HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                            ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                            if (_tmpProgressCollection != null) {
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                            } else {
                                ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                                _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                            }
                            _collectionProgress = _collectionProgress2;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _cursorIndexOfState2 = _cursorIndexOfState;
                            _cursorIndexOfOutput2 = _cursorIndexOfOutput;
                            _collectionTags = _collectionTags2;
                        }
                        HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                        HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                        _cursor.moveToPosition(-1);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                        List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                        while (_cursor.moveToNext()) {
                            if (_cursor.isNull(0)) {
                                _tmpId = null;
                            } else {
                                _tmpId = _cursor.getString(0);
                            }
                            int _tmp = _cursor.getInt(1);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(2)) {
                                _tmp_1 = null;
                            } else {
                                _tmp_1 = _cursor.getBlob(2);
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_1);
                            int _tmpRunAttemptCount = _cursor.getInt(3);
                            int _tmpGeneration = _cursor.getInt(4);
                            long _tmpInitialDelay = _cursor.getLong(13);
                            long _tmpIntervalDuration = _cursor.getLong(14);
                            long _tmpFlexDuration = _cursor.getLong(15);
                            int _tmp_2 = _cursor.getInt(16);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                            long _tmpBackoffDelayDuration = _cursor.getLong(17);
                            long _tmpLastEnqueueTime = _cursor.getLong(18);
                            int _tmpPeriodCount = _cursor.getInt(19);
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                            int _tmpStopReason = _cursor.getInt(21);
                            int _tmp_3 = _cursor.getInt(5);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                            int _tmp_4 = _cursor.getInt(6);
                            boolean _tmpRequiresCharging = _tmp_4 != 0;
                            int _tmp_5 = _cursor.getInt(7);
                            boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                            int _tmp_6 = _cursor.getInt(8);
                            boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                            int _tmp_7 = _cursor.getInt(9);
                            boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                            if (_cursor.isNull(12)) {
                                _tmp_8 = null;
                            } else {
                                _tmp_8 = _cursor.getBlob(12);
                            }
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            String _tmpKey_2 = _cursor.getString(0);
                            ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                            if (_tmpTagsCollection_1 == null) {
                                _tmpTagsCollection_1 = new ArrayList<>();
                            }
                            HashMap<String, ArrayList<String>> _collectionTags4 = _collectionTags3;
                            String _tmpKey_3 = _cursor.getString(0);
                            ArrayList<Data> _tmpProgressCollection_1 = _collectionProgress3.get(_tmpKey_3);
                            if (_tmpProgressCollection_1 == null) {
                                _tmpProgressCollection_1 = new ArrayList<>();
                            }
                            WorkSpec.WorkInfoPojo _item_1 = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                            _result.add(_item_1);
                            _collectionProgress3 = _collectionProgress3;
                            _collectionTags3 = _collectionTags4;
                        }
                        WorkSpecDao_Impl.this.__db.setTransactionSuccessful();
                        return _result;
                    } finally {
                        _cursor.close();
                    }
                } finally {
                    WorkSpecDao_Impl.this.__db.endTransaction();
                }
            }

            protected void finalize() {
                _statement.release();
            }
        });
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec.WorkInfoPojo> getWorkStatusPojoForTag(final String tag) throws Throwable {
        String _tmpId;
        byte[] _tmp_1;
        byte[] _tmp_8;
        String _sql;
        String _sql2 = "SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN\n            (SELECT work_spec_id FROM worktag WHERE tag=?)";
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN\n            (SELECT work_spec_id FROM worktag WHERE tag=?)", 1);
        if (tag == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, tag);
        }
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            Cursor _cursor = DBUtil.query(this.__db, _statement, true, null);
            int _cursorIndexOfId = 0;
            try {
                try {
                    HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                    HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                    while (_cursor.moveToNext()) {
                        String _tmpKey = _cursor.getString(0);
                        HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                        int _cursorIndexOfId2 = _cursorIndexOfId;
                        ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                        if (_tmpTagsCollection == null) {
                            try {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                            } catch (Throwable th) {
                                th = th;
                                _cursor.close();
                                _statement.release();
                                throw th;
                            }
                        }
                        String _tmpKey_1 = _cursor.getString(0);
                        HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                        ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                        if (_tmpProgressCollection != null) {
                            _sql = _sql2;
                        } else {
                            ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                            _sql = _sql2;
                            try {
                                _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                            } catch (Throwable th2) {
                                th = th2;
                                _cursor.close();
                                _statement.release();
                                throw th;
                            }
                        }
                        _collectionProgress = _collectionProgress2;
                        _cursorIndexOfId = _cursorIndexOfId2;
                        _sql2 = _sql;
                        _collectionTags = _collectionTags2;
                    }
                    HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                    HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                    _cursor.moveToPosition(-1);
                    __fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                    __fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                    List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                    while (_cursor.moveToNext()) {
                        if (_cursor.isNull(0)) {
                            _tmpId = null;
                        } else {
                            _tmpId = _cursor.getString(0);
                        }
                        int _tmp = _cursor.getInt(1);
                        WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                        WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                        if (_cursor.isNull(2)) {
                            _tmp_1 = null;
                        } else {
                            _tmp_1 = _cursor.getBlob(2);
                        }
                        Data _tmpOutput = Data.fromByteArray(_tmp_1);
                        int _tmpRunAttemptCount = _cursor.getInt(3);
                        int _tmpGeneration = _cursor.getInt(4);
                        long _tmpInitialDelay = _cursor.getLong(13);
                        long _tmpIntervalDuration = _cursor.getLong(14);
                        long _tmpFlexDuration = _cursor.getLong(15);
                        int _tmp_2 = _cursor.getInt(16);
                        WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                        BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                        long _tmpBackoffDelayDuration = _cursor.getLong(17);
                        long _tmpLastEnqueueTime = _cursor.getLong(18);
                        int _tmpPeriodCount = _cursor.getInt(19);
                        long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                        int _tmpStopReason = _cursor.getInt(21);
                        int _tmp_3 = _cursor.getInt(5);
                        WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                        NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                        int _tmp_4 = _cursor.getInt(6);
                        boolean _tmpRequiresCharging = _tmp_4 != 0;
                        int _tmp_5 = _cursor.getInt(7);
                        boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                        int _tmp_6 = _cursor.getInt(8);
                        boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                        int _tmp_7 = _cursor.getInt(9);
                        boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                        long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                        long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                        if (_cursor.isNull(12)) {
                            _tmp_8 = null;
                        } else {
                            _tmp_8 = _cursor.getBlob(12);
                        }
                        WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                        Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                        Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                        String _tmpKey_2 = _cursor.getString(0);
                        ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                        if (_tmpTagsCollection_1 == null) {
                            _tmpTagsCollection_1 = new ArrayList<>();
                        }
                        String _tmpKey_3 = _cursor.getString(0);
                        ArrayList<Data> _tmpProgressCollection_1 = _collectionProgress3.get(_tmpKey_3);
                        if (_tmpProgressCollection_1 == null) {
                            _tmpProgressCollection_1 = new ArrayList<>();
                        }
                        WorkSpec.WorkInfoPojo _item = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                        _result.add(_item);
                        _collectionProgress3 = _collectionProgress3;
                    }
                    this.__db.setTransactionSuccessful();
                    _cursor.close();
                    _statement.release();
                    this.__db.endTransaction();
                    return _result;
                } catch (Throwable th3) {
                    th = th3;
                    this.__db.endTransaction();
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
            }
        } catch (Throwable th5) {
            th = th5;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public Flow<List<WorkSpec.WorkInfoPojo>> getWorkStatusPojoFlowForTag(final String tag) {
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN\n            (SELECT work_spec_id FROM worktag WHERE tag=?)", 1);
        if (tag == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, tag);
        }
        return CoroutinesRoom.createFlow(this.__db, true, new String[]{"WorkTag", "WorkProgress", "workspec", "worktag"}, new Callable<List<WorkSpec.WorkInfoPojo>>() { // from class: androidx.work.impl.model.WorkSpecDao_Impl.21
            @Override // java.util.concurrent.Callable
            public List<WorkSpec.WorkInfoPojo> call() throws Exception {
                String _tmpId;
                byte[] _tmp_1;
                byte[] _tmp_8;
                int _cursorIndexOfState;
                int _cursorIndexOfOutput;
                WorkSpecDao_Impl.this.__db.beginTransaction();
                try {
                    Cursor _cursor = DBUtil.query(WorkSpecDao_Impl.this.__db, _statement, true, null);
                    int _cursorIndexOfId = 0;
                    int _cursorIndexOfState2 = 1;
                    int _cursorIndexOfOutput2 = 2;
                    try {
                        HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                        HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                        while (_cursor.moveToNext()) {
                            String _tmpKey = _cursor.getString(0);
                            HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                            int _cursorIndexOfId2 = _cursorIndexOfId;
                            ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                            if (_tmpTagsCollection != null) {
                                _cursorIndexOfState = _cursorIndexOfState2;
                            } else {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _cursorIndexOfState = _cursorIndexOfState2;
                                _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                            }
                            String _tmpKey_1 = _cursor.getString(0);
                            HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                            ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                            if (_tmpProgressCollection != null) {
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                            } else {
                                ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                                _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                            }
                            _collectionProgress = _collectionProgress2;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _cursorIndexOfState2 = _cursorIndexOfState;
                            _cursorIndexOfOutput2 = _cursorIndexOfOutput;
                            _collectionTags = _collectionTags2;
                        }
                        HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                        HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                        _cursor.moveToPosition(-1);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                        List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                        while (_cursor.moveToNext()) {
                            if (_cursor.isNull(0)) {
                                _tmpId = null;
                            } else {
                                _tmpId = _cursor.getString(0);
                            }
                            int _tmp = _cursor.getInt(1);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(2)) {
                                _tmp_1 = null;
                            } else {
                                _tmp_1 = _cursor.getBlob(2);
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_1);
                            int _tmpRunAttemptCount = _cursor.getInt(3);
                            int _tmpGeneration = _cursor.getInt(4);
                            long _tmpInitialDelay = _cursor.getLong(13);
                            long _tmpIntervalDuration = _cursor.getLong(14);
                            long _tmpFlexDuration = _cursor.getLong(15);
                            int _tmp_2 = _cursor.getInt(16);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                            long _tmpBackoffDelayDuration = _cursor.getLong(17);
                            long _tmpLastEnqueueTime = _cursor.getLong(18);
                            int _tmpPeriodCount = _cursor.getInt(19);
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                            int _tmpStopReason = _cursor.getInt(21);
                            int _tmp_3 = _cursor.getInt(5);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                            int _tmp_4 = _cursor.getInt(6);
                            boolean _tmpRequiresCharging = _tmp_4 != 0;
                            int _tmp_5 = _cursor.getInt(7);
                            boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                            int _tmp_6 = _cursor.getInt(8);
                            boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                            int _tmp_7 = _cursor.getInt(9);
                            boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                            if (_cursor.isNull(12)) {
                                _tmp_8 = null;
                            } else {
                                _tmp_8 = _cursor.getBlob(12);
                            }
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            String _tmpKey_2 = _cursor.getString(0);
                            ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                            if (_tmpTagsCollection_1 == null) {
                                _tmpTagsCollection_1 = new ArrayList<>();
                            }
                            HashMap<String, ArrayList<String>> _collectionTags4 = _collectionTags3;
                            String _tmpKey_3 = _cursor.getString(0);
                            ArrayList<Data> _tmpProgressCollection_1 = _collectionProgress3.get(_tmpKey_3);
                            if (_tmpProgressCollection_1 == null) {
                                _tmpProgressCollection_1 = new ArrayList<>();
                            }
                            WorkSpec.WorkInfoPojo _item = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                            _result.add(_item);
                            _collectionProgress3 = _collectionProgress3;
                            _collectionTags3 = _collectionTags4;
                        }
                        WorkSpecDao_Impl.this.__db.setTransactionSuccessful();
                        return _result;
                    } finally {
                        _cursor.close();
                    }
                } finally {
                    WorkSpecDao_Impl.this.__db.endTransaction();
                }
            }

            protected void finalize() {
                _statement.release();
            }
        });
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public LiveData<List<WorkSpec.WorkInfoPojo>> getWorkStatusPojoLiveDataForTag(final String tag) {
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN\n            (SELECT work_spec_id FROM worktag WHERE tag=?)", 1);
        if (tag == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, tag);
        }
        return this.__db.getInvalidationTracker().createLiveData(new String[]{"WorkTag", "WorkProgress", "workspec", "worktag"}, true, new Callable<List<WorkSpec.WorkInfoPojo>>() { // from class: androidx.work.impl.model.WorkSpecDao_Impl.22
            @Override // java.util.concurrent.Callable
            public List<WorkSpec.WorkInfoPojo> call() throws Exception {
                String _tmpId;
                byte[] _tmp_1;
                byte[] _tmp_8;
                int _cursorIndexOfState;
                int _cursorIndexOfOutput;
                WorkSpecDao_Impl.this.__db.beginTransaction();
                try {
                    Cursor _cursor = DBUtil.query(WorkSpecDao_Impl.this.__db, _statement, true, null);
                    int _cursorIndexOfId = 0;
                    int _cursorIndexOfState2 = 1;
                    int _cursorIndexOfOutput2 = 2;
                    try {
                        HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                        HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                        while (_cursor.moveToNext()) {
                            String _tmpKey = _cursor.getString(0);
                            HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                            int _cursorIndexOfId2 = _cursorIndexOfId;
                            ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                            if (_tmpTagsCollection != null) {
                                _cursorIndexOfState = _cursorIndexOfState2;
                            } else {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _cursorIndexOfState = _cursorIndexOfState2;
                                _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                            }
                            String _tmpKey_1 = _cursor.getString(0);
                            HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                            ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                            if (_tmpProgressCollection != null) {
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                            } else {
                                ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                                _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                            }
                            _collectionProgress = _collectionProgress2;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _cursorIndexOfState2 = _cursorIndexOfState;
                            _cursorIndexOfOutput2 = _cursorIndexOfOutput;
                            _collectionTags = _collectionTags2;
                        }
                        HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                        HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                        _cursor.moveToPosition(-1);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                        List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                        while (_cursor.moveToNext()) {
                            if (_cursor.isNull(0)) {
                                _tmpId = null;
                            } else {
                                _tmpId = _cursor.getString(0);
                            }
                            int _tmp = _cursor.getInt(1);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(2)) {
                                _tmp_1 = null;
                            } else {
                                _tmp_1 = _cursor.getBlob(2);
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_1);
                            int _tmpRunAttemptCount = _cursor.getInt(3);
                            int _tmpGeneration = _cursor.getInt(4);
                            long _tmpInitialDelay = _cursor.getLong(13);
                            long _tmpIntervalDuration = _cursor.getLong(14);
                            long _tmpFlexDuration = _cursor.getLong(15);
                            int _tmp_2 = _cursor.getInt(16);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                            long _tmpBackoffDelayDuration = _cursor.getLong(17);
                            long _tmpLastEnqueueTime = _cursor.getLong(18);
                            int _tmpPeriodCount = _cursor.getInt(19);
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                            int _tmpStopReason = _cursor.getInt(21);
                            int _tmp_3 = _cursor.getInt(5);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                            int _tmp_4 = _cursor.getInt(6);
                            boolean _tmpRequiresCharging = _tmp_4 != 0;
                            int _tmp_5 = _cursor.getInt(7);
                            boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                            int _tmp_6 = _cursor.getInt(8);
                            boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                            int _tmp_7 = _cursor.getInt(9);
                            boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                            if (_cursor.isNull(12)) {
                                _tmp_8 = null;
                            } else {
                                _tmp_8 = _cursor.getBlob(12);
                            }
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            String _tmpKey_2 = _cursor.getString(0);
                            ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                            if (_tmpTagsCollection_1 == null) {
                                _tmpTagsCollection_1 = new ArrayList<>();
                            }
                            HashMap<String, ArrayList<String>> _collectionTags4 = _collectionTags3;
                            String _tmpKey_3 = _cursor.getString(0);
                            ArrayList<Data> _tmpProgressCollection_1 = _collectionProgress3.get(_tmpKey_3);
                            if (_tmpProgressCollection_1 == null) {
                                _tmpProgressCollection_1 = new ArrayList<>();
                            }
                            WorkSpec.WorkInfoPojo _item = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                            _result.add(_item);
                            _collectionProgress3 = _collectionProgress3;
                            _collectionTags3 = _collectionTags4;
                        }
                        WorkSpecDao_Impl.this.__db.setTransactionSuccessful();
                        return _result;
                    } finally {
                        _cursor.close();
                    }
                } finally {
                    WorkSpecDao_Impl.this.__db.endTransaction();
                }
            }

            protected void finalize() {
                _statement.release();
            }
        });
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec.WorkInfoPojo> getWorkStatusPojoForName(final String name) throws Throwable {
        String _tmpId;
        byte[] _tmp_1;
        byte[] _tmp_8;
        String _sql;
        String _sql2 = "SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN (SELECT work_spec_id FROM workname WHERE name=?)";
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN (SELECT work_spec_id FROM workname WHERE name=?)", 1);
        if (name == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, name);
        }
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            Cursor _cursor = DBUtil.query(this.__db, _statement, true, null);
            int _cursorIndexOfId = 0;
            try {
                try {
                    HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                    HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                    while (_cursor.moveToNext()) {
                        String _tmpKey = _cursor.getString(0);
                        HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                        int _cursorIndexOfId2 = _cursorIndexOfId;
                        ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                        if (_tmpTagsCollection == null) {
                            try {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                            } catch (Throwable th) {
                                th = th;
                                _cursor.close();
                                _statement.release();
                                throw th;
                            }
                        }
                        String _tmpKey_1 = _cursor.getString(0);
                        HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                        ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                        if (_tmpProgressCollection != null) {
                            _sql = _sql2;
                        } else {
                            ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                            _sql = _sql2;
                            try {
                                _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                            } catch (Throwable th2) {
                                th = th2;
                                _cursor.close();
                                _statement.release();
                                throw th;
                            }
                        }
                        _collectionProgress = _collectionProgress2;
                        _cursorIndexOfId = _cursorIndexOfId2;
                        _sql2 = _sql;
                        _collectionTags = _collectionTags2;
                    }
                    HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                    HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                    _cursor.moveToPosition(-1);
                    __fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                    __fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                    List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                    while (_cursor.moveToNext()) {
                        if (_cursor.isNull(0)) {
                            _tmpId = null;
                        } else {
                            _tmpId = _cursor.getString(0);
                        }
                        int _tmp = _cursor.getInt(1);
                        WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                        WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                        if (_cursor.isNull(2)) {
                            _tmp_1 = null;
                        } else {
                            _tmp_1 = _cursor.getBlob(2);
                        }
                        Data _tmpOutput = Data.fromByteArray(_tmp_1);
                        int _tmpRunAttemptCount = _cursor.getInt(3);
                        int _tmpGeneration = _cursor.getInt(4);
                        long _tmpInitialDelay = _cursor.getLong(13);
                        long _tmpIntervalDuration = _cursor.getLong(14);
                        long _tmpFlexDuration = _cursor.getLong(15);
                        int _tmp_2 = _cursor.getInt(16);
                        WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                        BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                        long _tmpBackoffDelayDuration = _cursor.getLong(17);
                        long _tmpLastEnqueueTime = _cursor.getLong(18);
                        int _tmpPeriodCount = _cursor.getInt(19);
                        long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                        int _tmpStopReason = _cursor.getInt(21);
                        int _tmp_3 = _cursor.getInt(5);
                        WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                        NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                        int _tmp_4 = _cursor.getInt(6);
                        boolean _tmpRequiresCharging = _tmp_4 != 0;
                        int _tmp_5 = _cursor.getInt(7);
                        boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                        int _tmp_6 = _cursor.getInt(8);
                        boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                        int _tmp_7 = _cursor.getInt(9);
                        boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                        long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                        long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                        if (_cursor.isNull(12)) {
                            _tmp_8 = null;
                        } else {
                            _tmp_8 = _cursor.getBlob(12);
                        }
                        WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                        Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                        Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                        String _tmpKey_2 = _cursor.getString(0);
                        ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                        if (_tmpTagsCollection_1 == null) {
                            _tmpTagsCollection_1 = new ArrayList<>();
                        }
                        String _tmpKey_3 = _cursor.getString(0);
                        ArrayList<Data> _tmpProgressCollection_1 = _collectionProgress3.get(_tmpKey_3);
                        if (_tmpProgressCollection_1 == null) {
                            _tmpProgressCollection_1 = new ArrayList<>();
                        }
                        WorkSpec.WorkInfoPojo _item = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                        _result.add(_item);
                        _collectionProgress3 = _collectionProgress3;
                    }
                    this.__db.setTransactionSuccessful();
                    _cursor.close();
                    _statement.release();
                    this.__db.endTransaction();
                    return _result;
                } catch (Throwable th3) {
                    th = th3;
                    this.__db.endTransaction();
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
            }
        } catch (Throwable th5) {
            th = th5;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public LiveData<List<WorkSpec.WorkInfoPojo>> getWorkStatusPojoLiveDataForName(final String name) {
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN (SELECT work_spec_id FROM workname WHERE name=?)", 1);
        if (name == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, name);
        }
        return this.__db.getInvalidationTracker().createLiveData(new String[]{"WorkTag", "WorkProgress", "workspec", "workname"}, true, new Callable<List<WorkSpec.WorkInfoPojo>>() { // from class: androidx.work.impl.model.WorkSpecDao_Impl.23
            @Override // java.util.concurrent.Callable
            public List<WorkSpec.WorkInfoPojo> call() throws Exception {
                String _tmpId;
                byte[] _tmp_1;
                byte[] _tmp_8;
                int _cursorIndexOfState;
                int _cursorIndexOfOutput;
                WorkSpecDao_Impl.this.__db.beginTransaction();
                try {
                    Cursor _cursor = DBUtil.query(WorkSpecDao_Impl.this.__db, _statement, true, null);
                    int _cursorIndexOfId = 0;
                    int _cursorIndexOfState2 = 1;
                    int _cursorIndexOfOutput2 = 2;
                    try {
                        HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                        HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                        while (_cursor.moveToNext()) {
                            String _tmpKey = _cursor.getString(0);
                            HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                            int _cursorIndexOfId2 = _cursorIndexOfId;
                            ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                            if (_tmpTagsCollection != null) {
                                _cursorIndexOfState = _cursorIndexOfState2;
                            } else {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _cursorIndexOfState = _cursorIndexOfState2;
                                _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                            }
                            String _tmpKey_1 = _cursor.getString(0);
                            HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                            ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                            if (_tmpProgressCollection != null) {
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                            } else {
                                ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                                _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                            }
                            _collectionProgress = _collectionProgress2;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _cursorIndexOfState2 = _cursorIndexOfState;
                            _cursorIndexOfOutput2 = _cursorIndexOfOutput;
                            _collectionTags = _collectionTags2;
                        }
                        HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                        HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                        _cursor.moveToPosition(-1);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                        List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                        while (_cursor.moveToNext()) {
                            if (_cursor.isNull(0)) {
                                _tmpId = null;
                            } else {
                                _tmpId = _cursor.getString(0);
                            }
                            int _tmp = _cursor.getInt(1);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(2)) {
                                _tmp_1 = null;
                            } else {
                                _tmp_1 = _cursor.getBlob(2);
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_1);
                            int _tmpRunAttemptCount = _cursor.getInt(3);
                            int _tmpGeneration = _cursor.getInt(4);
                            long _tmpInitialDelay = _cursor.getLong(13);
                            long _tmpIntervalDuration = _cursor.getLong(14);
                            long _tmpFlexDuration = _cursor.getLong(15);
                            int _tmp_2 = _cursor.getInt(16);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                            long _tmpBackoffDelayDuration = _cursor.getLong(17);
                            long _tmpLastEnqueueTime = _cursor.getLong(18);
                            int _tmpPeriodCount = _cursor.getInt(19);
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                            int _tmpStopReason = _cursor.getInt(21);
                            int _tmp_3 = _cursor.getInt(5);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                            int _tmp_4 = _cursor.getInt(6);
                            boolean _tmpRequiresCharging = _tmp_4 != 0;
                            int _tmp_5 = _cursor.getInt(7);
                            boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                            int _tmp_6 = _cursor.getInt(8);
                            boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                            int _tmp_7 = _cursor.getInt(9);
                            boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                            if (_cursor.isNull(12)) {
                                _tmp_8 = null;
                            } else {
                                _tmp_8 = _cursor.getBlob(12);
                            }
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            String _tmpKey_2 = _cursor.getString(0);
                            ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                            if (_tmpTagsCollection_1 == null) {
                                _tmpTagsCollection_1 = new ArrayList<>();
                            }
                            HashMap<String, ArrayList<String>> _collectionTags4 = _collectionTags3;
                            String _tmpKey_3 = _cursor.getString(0);
                            ArrayList<Data> _tmpProgressCollection_1 = _collectionProgress3.get(_tmpKey_3);
                            if (_tmpProgressCollection_1 == null) {
                                _tmpProgressCollection_1 = new ArrayList<>();
                            }
                            WorkSpec.WorkInfoPojo _item = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                            _result.add(_item);
                            _collectionProgress3 = _collectionProgress3;
                            _collectionTags3 = _collectionTags4;
                        }
                        WorkSpecDao_Impl.this.__db.setTransactionSuccessful();
                        return _result;
                    } finally {
                        _cursor.close();
                    }
                } finally {
                    WorkSpecDao_Impl.this.__db.endTransaction();
                }
            }

            protected void finalize() {
                _statement.release();
            }
        });
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public Flow<List<WorkSpec.WorkInfoPojo>> getWorkStatusPojoFlowForName(final String name) {
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id, state, output, run_attempt_count, generation, required_network_type, requires_charging,requires_device_idle, requires_battery_not_low, requires_storage_not_low, trigger_content_update_delay, trigger_max_content_delay, content_uri_triggers, initial_delay, interval_duration, flex_duration, backoff_policy, backoff_delay_duration, last_enqueue_time, period_count, next_schedule_time_override, stop_reason FROM workspec WHERE id IN (SELECT work_spec_id FROM workname WHERE name=?)", 1);
        if (name == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, name);
        }
        return CoroutinesRoom.createFlow(this.__db, true, new String[]{"WorkTag", "WorkProgress", "workspec", "workname"}, new Callable<List<WorkSpec.WorkInfoPojo>>() { // from class: androidx.work.impl.model.WorkSpecDao_Impl.24
            @Override // java.util.concurrent.Callable
            public List<WorkSpec.WorkInfoPojo> call() throws Exception {
                String _tmpId;
                byte[] _tmp_1;
                byte[] _tmp_8;
                int _cursorIndexOfState;
                int _cursorIndexOfOutput;
                WorkSpecDao_Impl.this.__db.beginTransaction();
                try {
                    Cursor _cursor = DBUtil.query(WorkSpecDao_Impl.this.__db, _statement, true, null);
                    int _cursorIndexOfId = 0;
                    int _cursorIndexOfState2 = 1;
                    int _cursorIndexOfOutput2 = 2;
                    try {
                        HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                        HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                        while (_cursor.moveToNext()) {
                            String _tmpKey = _cursor.getString(0);
                            HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                            int _cursorIndexOfId2 = _cursorIndexOfId;
                            ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                            if (_tmpTagsCollection != null) {
                                _cursorIndexOfState = _cursorIndexOfState2;
                            } else {
                                ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                                _cursorIndexOfState = _cursorIndexOfState2;
                                _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                            }
                            String _tmpKey_1 = _cursor.getString(0);
                            HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                            ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                            if (_tmpProgressCollection != null) {
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                            } else {
                                ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                                _cursorIndexOfOutput = _cursorIndexOfOutput2;
                                _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                            }
                            _collectionProgress = _collectionProgress2;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _cursorIndexOfState2 = _cursorIndexOfState;
                            _cursorIndexOfOutput2 = _cursorIndexOfOutput;
                            _collectionTags = _collectionTags2;
                        }
                        HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                        HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                        _cursor.moveToPosition(-1);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                        WorkSpecDao_Impl.this.__fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                        List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                        while (_cursor.moveToNext()) {
                            if (_cursor.isNull(0)) {
                                _tmpId = null;
                            } else {
                                _tmpId = _cursor.getString(0);
                            }
                            int _tmp = _cursor.getInt(1);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(2)) {
                                _tmp_1 = null;
                            } else {
                                _tmp_1 = _cursor.getBlob(2);
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_1);
                            int _tmpRunAttemptCount = _cursor.getInt(3);
                            int _tmpGeneration = _cursor.getInt(4);
                            long _tmpInitialDelay = _cursor.getLong(13);
                            long _tmpIntervalDuration = _cursor.getLong(14);
                            long _tmpFlexDuration = _cursor.getLong(15);
                            int _tmp_2 = _cursor.getInt(16);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                            long _tmpBackoffDelayDuration = _cursor.getLong(17);
                            long _tmpLastEnqueueTime = _cursor.getLong(18);
                            int _tmpPeriodCount = _cursor.getInt(19);
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(20);
                            int _tmpStopReason = _cursor.getInt(21);
                            int _tmp_3 = _cursor.getInt(5);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                            int _tmp_4 = _cursor.getInt(6);
                            boolean _tmpRequiresCharging = _tmp_4 != 0;
                            int _tmp_5 = _cursor.getInt(7);
                            boolean _tmpRequiresDeviceIdle = _tmp_5 != 0;
                            int _tmp_6 = _cursor.getInt(8);
                            boolean _tmpRequiresBatteryNotLow = _tmp_6 != 0;
                            int _tmp_7 = _cursor.getInt(9);
                            boolean _tmpRequiresStorageNotLow = _tmp_7 != 0;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(10);
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(11);
                            if (_cursor.isNull(12)) {
                                _tmp_8 = null;
                            } else {
                                _tmp_8 = _cursor.getBlob(12);
                            }
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            String _tmpKey_2 = _cursor.getString(0);
                            ArrayList<String> _tmpTagsCollection_1 = _collectionTags3.get(_tmpKey_2);
                            if (_tmpTagsCollection_1 == null) {
                                _tmpTagsCollection_1 = new ArrayList<>();
                            }
                            HashMap<String, ArrayList<String>> _collectionTags4 = _collectionTags3;
                            String _tmpKey_3 = _cursor.getString(0);
                            ArrayList<Data> _tmpProgressCollection_1 = _collectionProgress3.get(_tmpKey_3);
                            if (_tmpProgressCollection_1 == null) {
                                _tmpProgressCollection_1 = new ArrayList<>();
                            }
                            WorkSpec.WorkInfoPojo _item = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                            _result.add(_item);
                            _collectionProgress3 = _collectionProgress3;
                            _collectionTags3 = _collectionTags4;
                        }
                        WorkSpecDao_Impl.this.__db.setTransactionSuccessful();
                        return _result;
                    } finally {
                        _cursor.close();
                    }
                } finally {
                    WorkSpecDao_Impl.this.__db.endTransaction();
                }
            }

            protected void finalize() {
                _statement.release();
            }
        });
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<Data> getInputsFromPrerequisites(final String id) {
        byte[] _tmp;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT output FROM workspec WHERE id IN\n             (SELECT prerequisite_id FROM dependency WHERE work_spec_id=?)", 1);
        if (id == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, id);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            List<Data> _result = new ArrayList<>(_cursor.getCount());
            while (_cursor.moveToNext()) {
                if (_cursor.isNull(0)) {
                    _tmp = null;
                } else {
                    _tmp = _cursor.getBlob(0);
                }
                Data _item = Data.fromByteArray(_tmp);
                _result.add(_item);
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<String> getUnfinishedWorkWithTag(final String tag) {
        String _item;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id FROM workspec WHERE state NOT IN (2, 3, 5) AND id IN (SELECT work_spec_id FROM worktag WHERE tag=?)", 1);
        if (tag == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, tag);
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

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<String> getUnfinishedWorkWithName(final String name) {
        String _item;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id FROM workspec WHERE state NOT IN (2, 3, 5) AND id IN (SELECT work_spec_id FROM workname WHERE name=?)", 1);
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

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<String> getAllUnfinishedWork() {
        String _item;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT id FROM workspec WHERE state NOT IN (2, 3, 5)", 0);
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

    @Override // androidx.work.impl.model.WorkSpecDao
    public boolean hasUnfinishedWork() {
        boolean _result = false;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT COUNT(*) > 0 FROM workspec WHERE state NOT IN (2, 3, 5) LIMIT 1", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            if (_cursor.moveToFirst()) {
                int _tmp = _cursor.getInt(0);
                if (_tmp != 0) {
                    _result = true;
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

    @Override // androidx.work.impl.model.WorkSpecDao
    public LiveData<Long> getScheduleRequestedAtLiveData(final String id) {
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("SELECT schedule_requested_at FROM workspec WHERE id=?", 1);
        if (id == null) {
            _statement.bindNull(1);
        } else {
            _statement.bindString(1, id);
        }
        return this.__db.getInvalidationTracker().createLiveData(new String[]{"workspec"}, false, new Callable<Long>() { // from class: androidx.work.impl.model.WorkSpecDao_Impl.25
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public Long call() throws Exception {
                long _result;
                Cursor _cursor = DBUtil.query(WorkSpecDao_Impl.this.__db, _statement, false, null);
                try {
                    if (_cursor.moveToFirst()) {
                        _result = _cursor.getLong(0);
                    } else {
                        _result = 0;
                    }
                    return Long.valueOf(_result);
                } finally {
                    _cursor.close();
                }
            }

            protected void finalize() {
                _statement.release();
            }
        });
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec> getEligibleWorkForScheduling(final int schedulerLimit) throws Throwable {
        RoomSQLiteQuery _statement;
        String _tmpId;
        String _tmpWorkerClassName;
        String _tmpInputMergerClassName;
        byte[] _tmp_1;
        byte[] _tmp_2;
        byte[] _tmp_11;
        RoomSQLiteQuery _statement2 = RoomSQLiteQuery.acquire("SELECT * FROM workspec WHERE state=0 AND schedule_requested_at=-1 ORDER BY last_enqueue_time LIMIT (SELECT MAX(?-COUNT(*), 0) FROM workspec WHERE schedule_requested_at<>-1 AND LENGTH(content_uri_triggers)=0 AND state NOT IN (2, 3, 5))", 1);
        _statement2.bindLong(1, schedulerLimit);
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement2, false, null);
        try {
            int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
            int _cursorIndexOfWorkerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "worker_class_name");
            int _cursorIndexOfInputMergerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "input_merger_class_name");
            int _cursorIndexOfInput = CursorUtil.getColumnIndexOrThrow(_cursor, "input");
            int _cursorIndexOfOutput = CursorUtil.getColumnIndexOrThrow(_cursor, "output");
            int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndexOrThrow(_cursor, "initial_delay");
            int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "interval_duration");
            int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "flex_duration");
            int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "run_attempt_count");
            int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_policy");
            try {
                int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_delay_duration");
                _statement = _statement2;
                try {
                    int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndexOrThrow(_cursor, "last_enqueue_time");
                    try {
                        int _cursorIndexOfMinimumRetentionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "minimum_retention_duration");
                        int _cursorIndexOfScheduleRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_requested_at");
                        int _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfScheduleRequestedAt;
                        int _tmp_4 = CursorUtil.getColumnIndexOrThrow(_cursor, "run_in_foreground");
                        int _tmp_5 = CursorUtil.getColumnIndexOrThrow(_cursor, "out_of_quota_policy");
                        int _cursorIndexOfPeriodCount = CursorUtil.getColumnIndexOrThrow(_cursor, "period_count");
                        int _cursorIndexOfPeriodCount2 = _cursorIndexOfPeriodCount;
                        int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
                        int _cursorIndexOfGeneration2 = _cursorIndexOfGeneration;
                        int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override");
                        int _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfNextScheduleTimeOverride;
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override_generation");
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverrideGeneration;
                        int _cursorIndexOfStopReason = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_reason");
                        int _cursorIndexOfStopReason2 = _cursorIndexOfStopReason;
                        int _tmp_6 = CursorUtil.getColumnIndexOrThrow(_cursor, "required_network_type");
                        int _tmp_7 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_charging");
                        int _tmp_8 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_device_idle");
                        int _tmp_9 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_battery_not_low");
                        int _tmp_10 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_storage_not_low");
                        int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_content_update_delay");
                        int _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis;
                        int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_max_content_delay");
                        int _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerMaxDelayMillis;
                        int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndexOrThrow(_cursor, "content_uri_triggers");
                        int _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentUriTriggers;
                        int _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfMinimumRetentionDuration;
                        int _cursorIndexOfMinimumRetentionDuration3 = _cursor.getCount();
                        List<WorkSpec> _result = new ArrayList<>(_cursorIndexOfMinimumRetentionDuration3);
                        while (_cursor.moveToNext()) {
                            if (_cursor.isNull(_cursorIndexOfId)) {
                                _tmpId = null;
                            } else {
                                String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                                _tmpId = _tmpId2;
                            }
                            int _tmp = _cursor.getInt(_cursorIndexOfState);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(_cursorIndexOfWorkerClassName)) {
                                _tmpWorkerClassName = null;
                            } else {
                                String _tmpWorkerClassName2 = _cursor.getString(_cursorIndexOfWorkerClassName);
                                _tmpWorkerClassName = _tmpWorkerClassName2;
                            }
                            if (_cursor.isNull(_cursorIndexOfInputMergerClassName)) {
                                _tmpInputMergerClassName = null;
                            } else {
                                String _tmpInputMergerClassName2 = _cursor.getString(_cursorIndexOfInputMergerClassName);
                                _tmpInputMergerClassName = _tmpInputMergerClassName2;
                            }
                            if (_cursor.isNull(_cursorIndexOfInput)) {
                                _tmp_1 = null;
                            } else {
                                byte[] _tmp_12 = _cursor.getBlob(_cursorIndexOfInput);
                                _tmp_1 = _tmp_12;
                            }
                            Data _tmpInput = Data.fromByteArray(_tmp_1);
                            if (_cursor.isNull(_cursorIndexOfOutput)) {
                                _tmp_2 = null;
                            } else {
                                byte[] _tmp_22 = _cursor.getBlob(_cursorIndexOfOutput);
                                _tmp_2 = _tmp_22;
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_2);
                            long _tmpInitialDelay = _cursor.getLong(_cursorIndexOfInitialDelay);
                            long _tmpIntervalDuration = _cursor.getLong(_cursorIndexOfIntervalDuration);
                            long _tmpFlexDuration = _cursor.getLong(_cursorIndexOfFlexDuration);
                            int _tmpRunAttemptCount = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                            int _tmp_3 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_3);
                            long _tmpBackoffDelayDuration = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                            long _tmpLastEnqueueTime = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                            int _cursorIndexOfId2 = _cursorIndexOfId;
                            int _cursorIndexOfId3 = _cursorIndexOfMinimumRetentionDuration2;
                            long _tmpMinimumRetentionDuration = _cursor.getLong(_cursorIndexOfId3);
                            _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfId3;
                            int _cursorIndexOfMinimumRetentionDuration4 = _cursorIndexOfScheduleRequestedAt2;
                            long _tmpScheduleRequestedAt = _cursor.getLong(_cursorIndexOfMinimumRetentionDuration4);
                            _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfMinimumRetentionDuration4;
                            int _cursorIndexOfScheduleRequestedAt3 = _tmp_4;
                            int _tmp_42 = _cursor.getInt(_cursorIndexOfScheduleRequestedAt3);
                            boolean _tmpExpedited = _tmp_42 != 0;
                            int _cursorIndexOfExpedited = _tmp_5;
                            int _tmp_52 = _cursor.getInt(_cursorIndexOfExpedited);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            OutOfQuotaPolicy _tmpOutOfQuotaPolicy = WorkTypeConverters.intToOutOfQuotaPolicy(_tmp_52);
                            int _cursorIndexOfOutOfQuotaPolicy = _cursorIndexOfPeriodCount2;
                            int _tmpPeriodCount = _cursor.getInt(_cursorIndexOfOutOfQuotaPolicy);
                            _cursorIndexOfPeriodCount2 = _cursorIndexOfOutOfQuotaPolicy;
                            int _cursorIndexOfPeriodCount3 = _cursorIndexOfGeneration2;
                            int _tmpGeneration = _cursor.getInt(_cursorIndexOfPeriodCount3);
                            _cursorIndexOfGeneration2 = _cursorIndexOfPeriodCount3;
                            int _cursorIndexOfGeneration3 = _cursorIndexOfNextScheduleTimeOverride2;
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(_cursorIndexOfGeneration3);
                            _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfGeneration3;
                            int _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfNextScheduleTimeOverrideGeneration2;
                            int _tmpNextScheduleTimeOverrideGeneration = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverride3);
                            _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverride3;
                            int _cursorIndexOfNextScheduleTimeOverrideGeneration3 = _cursorIndexOfStopReason2;
                            int _tmpStopReason = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverrideGeneration3);
                            _cursorIndexOfStopReason2 = _cursorIndexOfNextScheduleTimeOverrideGeneration3;
                            int _cursorIndexOfStopReason3 = _tmp_6;
                            int _tmp_62 = _cursor.getInt(_cursorIndexOfStopReason3);
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_62);
                            int _cursorIndexOfRequiredNetworkType = _tmp_7;
                            int _tmp_72 = _cursor.getInt(_cursorIndexOfRequiredNetworkType);
                            boolean _tmpRequiresCharging = _tmp_72 != 0;
                            int _cursorIndexOfRequiresCharging = _tmp_8;
                            int _tmp_82 = _cursor.getInt(_cursorIndexOfRequiresCharging);
                            boolean _tmpRequiresDeviceIdle = _tmp_82 != 0;
                            int _cursorIndexOfRequiresDeviceIdle = _tmp_9;
                            int _tmp_92 = _cursor.getInt(_cursorIndexOfRequiresDeviceIdle);
                            boolean _tmpRequiresBatteryNotLow = _tmp_92 != 0;
                            int _cursorIndexOfRequiresBatteryNotLow = _tmp_10;
                            int _tmp_102 = _cursor.getInt(_cursorIndexOfRequiresBatteryNotLow);
                            boolean _tmpRequiresStorageNotLow = _tmp_102 != 0;
                            int _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfContentTriggerUpdateDelayMillis2;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(_cursorIndexOfRequiresStorageNotLow);
                            _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfRequiresStorageNotLow;
                            int _cursorIndexOfContentTriggerUpdateDelayMillis3 = _cursorIndexOfContentTriggerMaxDelayMillis2;
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis3);
                            _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis3;
                            int _cursorIndexOfContentTriggerMaxDelayMillis3 = _cursorIndexOfContentUriTriggers2;
                            if (_cursor.isNull(_cursorIndexOfContentTriggerMaxDelayMillis3)) {
                                _tmp_11 = null;
                            } else {
                                _tmp_11 = _cursor.getBlob(_cursorIndexOfContentTriggerMaxDelayMillis3);
                            }
                            WorkTypeConverters workTypeConverters5 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_11);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            WorkSpec _item = new WorkSpec(_tmpId, _tmpState, _tmpWorkerClassName, _tmpInputMergerClassName, _tmpInput, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpMinimumRetentionDuration, _tmpScheduleRequestedAt, _tmpExpedited, _tmpOutOfQuotaPolicy, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpNextScheduleTimeOverrideGeneration, _tmpStopReason);
                            _result.add(_item);
                            _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentTriggerMaxDelayMillis3;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _tmp_4 = _cursorIndexOfScheduleRequestedAt3;
                            _tmp_5 = _cursorIndexOfExpedited;
                            _tmp_6 = _cursorIndexOfStopReason3;
                            _tmp_7 = _cursorIndexOfRequiredNetworkType;
                            _tmp_8 = _cursorIndexOfRequiresCharging;
                            _tmp_9 = _cursorIndexOfRequiresDeviceIdle;
                            _tmp_10 = _cursorIndexOfRequiresBatteryNotLow;
                        }
                        _cursor.close();
                        _statement.release();
                        return _result;
                    } catch (Throwable th) {
                        th = th;
                        _cursor.close();
                        _statement.release();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
                _statement = _statement2;
            }
        } catch (Throwable th4) {
            th = th4;
            _statement = _statement2;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec> getEligibleWorkForSchedulingWithContentUris() throws Throwable {
        RoomSQLiteQuery _statement;
        String _tmpId;
        String _tmpWorkerClassName;
        String _tmpInputMergerClassName;
        byte[] _tmp_1;
        byte[] _tmp_2;
        byte[] _tmp_11;
        RoomSQLiteQuery _statement2 = RoomSQLiteQuery.acquire("SELECT * FROM workspec WHERE state=0 AND schedule_requested_at=-1 AND LENGTH(content_uri_triggers)<>0 ORDER BY last_enqueue_time", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement2, false, null);
        try {
            int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
            int _cursorIndexOfWorkerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "worker_class_name");
            int _cursorIndexOfInputMergerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "input_merger_class_name");
            int _cursorIndexOfInput = CursorUtil.getColumnIndexOrThrow(_cursor, "input");
            int _cursorIndexOfOutput = CursorUtil.getColumnIndexOrThrow(_cursor, "output");
            int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndexOrThrow(_cursor, "initial_delay");
            int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "interval_duration");
            int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "flex_duration");
            int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "run_attempt_count");
            int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_policy");
            int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_delay_duration");
            int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndexOrThrow(_cursor, "last_enqueue_time");
            try {
                int _cursorIndexOfMinimumRetentionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "minimum_retention_duration");
                _statement = _statement2;
                try {
                    int _cursorIndexOfScheduleRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_requested_at");
                    int _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfScheduleRequestedAt;
                    int _tmp_4 = CursorUtil.getColumnIndexOrThrow(_cursor, "run_in_foreground");
                    int _tmp_5 = CursorUtil.getColumnIndexOrThrow(_cursor, "out_of_quota_policy");
                    int _cursorIndexOfPeriodCount = CursorUtil.getColumnIndexOrThrow(_cursor, "period_count");
                    int _cursorIndexOfPeriodCount2 = _cursorIndexOfPeriodCount;
                    int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
                    int _cursorIndexOfGeneration2 = _cursorIndexOfGeneration;
                    int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override");
                    int _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfNextScheduleTimeOverride;
                    int _cursorIndexOfNextScheduleTimeOverrideGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override_generation");
                    int _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverrideGeneration;
                    int _cursorIndexOfStopReason = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_reason");
                    int _cursorIndexOfStopReason2 = _cursorIndexOfStopReason;
                    int _tmp_6 = CursorUtil.getColumnIndexOrThrow(_cursor, "required_network_type");
                    int _tmp_7 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_charging");
                    int _tmp_8 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_device_idle");
                    int _tmp_9 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_battery_not_low");
                    int _tmp_10 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_storage_not_low");
                    int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_content_update_delay");
                    int _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis;
                    int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_max_content_delay");
                    int _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerMaxDelayMillis;
                    int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndexOrThrow(_cursor, "content_uri_triggers");
                    int _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentUriTriggers;
                    int _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfMinimumRetentionDuration;
                    int _cursorIndexOfMinimumRetentionDuration3 = _cursor.getCount();
                    List<WorkSpec> _result = new ArrayList<>(_cursorIndexOfMinimumRetentionDuration3);
                    while (_cursor.moveToNext()) {
                        if (_cursor.isNull(_cursorIndexOfId)) {
                            _tmpId = null;
                        } else {
                            String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                            _tmpId = _tmpId2;
                        }
                        int _tmp = _cursor.getInt(_cursorIndexOfState);
                        WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                        WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                        if (_cursor.isNull(_cursorIndexOfWorkerClassName)) {
                            _tmpWorkerClassName = null;
                        } else {
                            String _tmpWorkerClassName2 = _cursor.getString(_cursorIndexOfWorkerClassName);
                            _tmpWorkerClassName = _tmpWorkerClassName2;
                        }
                        if (_cursor.isNull(_cursorIndexOfInputMergerClassName)) {
                            _tmpInputMergerClassName = null;
                        } else {
                            String _tmpInputMergerClassName2 = _cursor.getString(_cursorIndexOfInputMergerClassName);
                            _tmpInputMergerClassName = _tmpInputMergerClassName2;
                        }
                        if (_cursor.isNull(_cursorIndexOfInput)) {
                            _tmp_1 = null;
                        } else {
                            byte[] _tmp_12 = _cursor.getBlob(_cursorIndexOfInput);
                            _tmp_1 = _tmp_12;
                        }
                        Data _tmpInput = Data.fromByteArray(_tmp_1);
                        if (_cursor.isNull(_cursorIndexOfOutput)) {
                            _tmp_2 = null;
                        } else {
                            byte[] _tmp_22 = _cursor.getBlob(_cursorIndexOfOutput);
                            _tmp_2 = _tmp_22;
                        }
                        Data _tmpOutput = Data.fromByteArray(_tmp_2);
                        long _tmpInitialDelay = _cursor.getLong(_cursorIndexOfInitialDelay);
                        long _tmpIntervalDuration = _cursor.getLong(_cursorIndexOfIntervalDuration);
                        long _tmpFlexDuration = _cursor.getLong(_cursorIndexOfFlexDuration);
                        int _tmpRunAttemptCount = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                        int _tmp_3 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                        WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                        BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_3);
                        long _tmpBackoffDelayDuration = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                        long _tmpLastEnqueueTime = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                        int _cursorIndexOfId2 = _cursorIndexOfId;
                        int _cursorIndexOfId3 = _cursorIndexOfMinimumRetentionDuration2;
                        long _tmpMinimumRetentionDuration = _cursor.getLong(_cursorIndexOfId3);
                        _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfId3;
                        int _cursorIndexOfMinimumRetentionDuration4 = _cursorIndexOfScheduleRequestedAt2;
                        long _tmpScheduleRequestedAt = _cursor.getLong(_cursorIndexOfMinimumRetentionDuration4);
                        _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfMinimumRetentionDuration4;
                        int _cursorIndexOfScheduleRequestedAt3 = _tmp_4;
                        int _tmp_42 = _cursor.getInt(_cursorIndexOfScheduleRequestedAt3);
                        boolean _tmpExpedited = _tmp_42 != 0;
                        int _cursorIndexOfExpedited = _tmp_5;
                        int _tmp_52 = _cursor.getInt(_cursorIndexOfExpedited);
                        WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                        OutOfQuotaPolicy _tmpOutOfQuotaPolicy = WorkTypeConverters.intToOutOfQuotaPolicy(_tmp_52);
                        int _cursorIndexOfOutOfQuotaPolicy = _cursorIndexOfPeriodCount2;
                        int _tmpPeriodCount = _cursor.getInt(_cursorIndexOfOutOfQuotaPolicy);
                        _cursorIndexOfPeriodCount2 = _cursorIndexOfOutOfQuotaPolicy;
                        int _cursorIndexOfPeriodCount3 = _cursorIndexOfGeneration2;
                        int _tmpGeneration = _cursor.getInt(_cursorIndexOfPeriodCount3);
                        _cursorIndexOfGeneration2 = _cursorIndexOfPeriodCount3;
                        int _cursorIndexOfGeneration3 = _cursorIndexOfNextScheduleTimeOverride2;
                        long _tmpNextScheduleTimeOverride = _cursor.getLong(_cursorIndexOfGeneration3);
                        _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfGeneration3;
                        int _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfNextScheduleTimeOverrideGeneration2;
                        int _tmpNextScheduleTimeOverrideGeneration = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverride3);
                        _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverride3;
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration3 = _cursorIndexOfStopReason2;
                        int _tmpStopReason = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverrideGeneration3);
                        _cursorIndexOfStopReason2 = _cursorIndexOfNextScheduleTimeOverrideGeneration3;
                        int _cursorIndexOfStopReason3 = _tmp_6;
                        int _tmp_62 = _cursor.getInt(_cursorIndexOfStopReason3);
                        WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                        NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_62);
                        int _cursorIndexOfRequiredNetworkType = _tmp_7;
                        int _tmp_72 = _cursor.getInt(_cursorIndexOfRequiredNetworkType);
                        boolean _tmpRequiresCharging = _tmp_72 != 0;
                        int _cursorIndexOfRequiresCharging = _tmp_8;
                        int _tmp_82 = _cursor.getInt(_cursorIndexOfRequiresCharging);
                        boolean _tmpRequiresDeviceIdle = _tmp_82 != 0;
                        int _cursorIndexOfRequiresDeviceIdle = _tmp_9;
                        int _tmp_92 = _cursor.getInt(_cursorIndexOfRequiresDeviceIdle);
                        boolean _tmpRequiresBatteryNotLow = _tmp_92 != 0;
                        int _cursorIndexOfRequiresBatteryNotLow = _tmp_10;
                        int _tmp_102 = _cursor.getInt(_cursorIndexOfRequiresBatteryNotLow);
                        boolean _tmpRequiresStorageNotLow = _tmp_102 != 0;
                        int _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfContentTriggerUpdateDelayMillis2;
                        long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(_cursorIndexOfRequiresStorageNotLow);
                        _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfRequiresStorageNotLow;
                        int _cursorIndexOfContentTriggerUpdateDelayMillis3 = _cursorIndexOfContentTriggerMaxDelayMillis2;
                        long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis3);
                        _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis3;
                        int _cursorIndexOfContentTriggerMaxDelayMillis3 = _cursorIndexOfContentUriTriggers2;
                        if (_cursor.isNull(_cursorIndexOfContentTriggerMaxDelayMillis3)) {
                            _tmp_11 = null;
                        } else {
                            _tmp_11 = _cursor.getBlob(_cursorIndexOfContentTriggerMaxDelayMillis3);
                        }
                        WorkTypeConverters workTypeConverters5 = WorkTypeConverters.INSTANCE;
                        Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_11);
                        Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                        WorkSpec _item = new WorkSpec(_tmpId, _tmpState, _tmpWorkerClassName, _tmpInputMergerClassName, _tmpInput, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpMinimumRetentionDuration, _tmpScheduleRequestedAt, _tmpExpedited, _tmpOutOfQuotaPolicy, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpNextScheduleTimeOverrideGeneration, _tmpStopReason);
                        _result.add(_item);
                        _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentTriggerMaxDelayMillis3;
                        _cursorIndexOfId = _cursorIndexOfId2;
                        _tmp_4 = _cursorIndexOfScheduleRequestedAt3;
                        _tmp_5 = _cursorIndexOfExpedited;
                        _tmp_6 = _cursorIndexOfStopReason3;
                        _tmp_7 = _cursorIndexOfRequiredNetworkType;
                        _tmp_8 = _cursorIndexOfRequiresCharging;
                        _tmp_9 = _cursorIndexOfRequiresDeviceIdle;
                        _tmp_10 = _cursorIndexOfRequiresBatteryNotLow;
                    }
                    _cursor.close();
                    _statement.release();
                    return _result;
                } catch (Throwable th) {
                    th = th;
                    _cursor.close();
                    _statement.release();
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                _statement = _statement2;
            }
        } catch (Throwable th3) {
            th = th3;
            _statement = _statement2;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec> getAllEligibleWorkSpecsForScheduling(final int maxLimit) throws Throwable {
        RoomSQLiteQuery _statement;
        String _tmpId;
        String _tmpWorkerClassName;
        String _tmpInputMergerClassName;
        byte[] _tmp_1;
        byte[] _tmp_2;
        byte[] _tmp_11;
        RoomSQLiteQuery _statement2 = RoomSQLiteQuery.acquire("SELECT * FROM workspec WHERE state=0 ORDER BY last_enqueue_time LIMIT ?", 1);
        _statement2.bindLong(1, maxLimit);
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement2, false, null);
        try {
            int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
            int _cursorIndexOfWorkerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "worker_class_name");
            int _cursorIndexOfInputMergerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "input_merger_class_name");
            int _cursorIndexOfInput = CursorUtil.getColumnIndexOrThrow(_cursor, "input");
            int _cursorIndexOfOutput = CursorUtil.getColumnIndexOrThrow(_cursor, "output");
            int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndexOrThrow(_cursor, "initial_delay");
            int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "interval_duration");
            int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "flex_duration");
            int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "run_attempt_count");
            int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_policy");
            try {
                int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_delay_duration");
                _statement = _statement2;
                try {
                    int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndexOrThrow(_cursor, "last_enqueue_time");
                    try {
                        int _cursorIndexOfMinimumRetentionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "minimum_retention_duration");
                        int _cursorIndexOfScheduleRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_requested_at");
                        int _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfScheduleRequestedAt;
                        int _tmp_4 = CursorUtil.getColumnIndexOrThrow(_cursor, "run_in_foreground");
                        int _tmp_5 = CursorUtil.getColumnIndexOrThrow(_cursor, "out_of_quota_policy");
                        int _cursorIndexOfPeriodCount = CursorUtil.getColumnIndexOrThrow(_cursor, "period_count");
                        int _cursorIndexOfPeriodCount2 = _cursorIndexOfPeriodCount;
                        int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
                        int _cursorIndexOfGeneration2 = _cursorIndexOfGeneration;
                        int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override");
                        int _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfNextScheduleTimeOverride;
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override_generation");
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverrideGeneration;
                        int _cursorIndexOfStopReason = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_reason");
                        int _cursorIndexOfStopReason2 = _cursorIndexOfStopReason;
                        int _tmp_6 = CursorUtil.getColumnIndexOrThrow(_cursor, "required_network_type");
                        int _tmp_7 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_charging");
                        int _tmp_8 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_device_idle");
                        int _tmp_9 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_battery_not_low");
                        int _tmp_10 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_storage_not_low");
                        int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_content_update_delay");
                        int _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis;
                        int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_max_content_delay");
                        int _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerMaxDelayMillis;
                        int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndexOrThrow(_cursor, "content_uri_triggers");
                        int _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentUriTriggers;
                        int _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfMinimumRetentionDuration;
                        int _cursorIndexOfMinimumRetentionDuration3 = _cursor.getCount();
                        List<WorkSpec> _result = new ArrayList<>(_cursorIndexOfMinimumRetentionDuration3);
                        while (_cursor.moveToNext()) {
                            if (_cursor.isNull(_cursorIndexOfId)) {
                                _tmpId = null;
                            } else {
                                String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                                _tmpId = _tmpId2;
                            }
                            int _tmp = _cursor.getInt(_cursorIndexOfState);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(_cursorIndexOfWorkerClassName)) {
                                _tmpWorkerClassName = null;
                            } else {
                                String _tmpWorkerClassName2 = _cursor.getString(_cursorIndexOfWorkerClassName);
                                _tmpWorkerClassName = _tmpWorkerClassName2;
                            }
                            if (_cursor.isNull(_cursorIndexOfInputMergerClassName)) {
                                _tmpInputMergerClassName = null;
                            } else {
                                String _tmpInputMergerClassName2 = _cursor.getString(_cursorIndexOfInputMergerClassName);
                                _tmpInputMergerClassName = _tmpInputMergerClassName2;
                            }
                            if (_cursor.isNull(_cursorIndexOfInput)) {
                                _tmp_1 = null;
                            } else {
                                byte[] _tmp_12 = _cursor.getBlob(_cursorIndexOfInput);
                                _tmp_1 = _tmp_12;
                            }
                            Data _tmpInput = Data.fromByteArray(_tmp_1);
                            if (_cursor.isNull(_cursorIndexOfOutput)) {
                                _tmp_2 = null;
                            } else {
                                byte[] _tmp_22 = _cursor.getBlob(_cursorIndexOfOutput);
                                _tmp_2 = _tmp_22;
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_2);
                            long _tmpInitialDelay = _cursor.getLong(_cursorIndexOfInitialDelay);
                            long _tmpIntervalDuration = _cursor.getLong(_cursorIndexOfIntervalDuration);
                            long _tmpFlexDuration = _cursor.getLong(_cursorIndexOfFlexDuration);
                            int _tmpRunAttemptCount = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                            int _tmp_3 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_3);
                            long _tmpBackoffDelayDuration = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                            long _tmpLastEnqueueTime = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                            int _cursorIndexOfId2 = _cursorIndexOfId;
                            int _cursorIndexOfId3 = _cursorIndexOfMinimumRetentionDuration2;
                            long _tmpMinimumRetentionDuration = _cursor.getLong(_cursorIndexOfId3);
                            _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfId3;
                            int _cursorIndexOfMinimumRetentionDuration4 = _cursorIndexOfScheduleRequestedAt2;
                            long _tmpScheduleRequestedAt = _cursor.getLong(_cursorIndexOfMinimumRetentionDuration4);
                            _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfMinimumRetentionDuration4;
                            int _cursorIndexOfScheduleRequestedAt3 = _tmp_4;
                            int _tmp_42 = _cursor.getInt(_cursorIndexOfScheduleRequestedAt3);
                            boolean _tmpExpedited = _tmp_42 != 0;
                            int _cursorIndexOfExpedited = _tmp_5;
                            int _tmp_52 = _cursor.getInt(_cursorIndexOfExpedited);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            OutOfQuotaPolicy _tmpOutOfQuotaPolicy = WorkTypeConverters.intToOutOfQuotaPolicy(_tmp_52);
                            int _cursorIndexOfOutOfQuotaPolicy = _cursorIndexOfPeriodCount2;
                            int _tmpPeriodCount = _cursor.getInt(_cursorIndexOfOutOfQuotaPolicy);
                            _cursorIndexOfPeriodCount2 = _cursorIndexOfOutOfQuotaPolicy;
                            int _cursorIndexOfPeriodCount3 = _cursorIndexOfGeneration2;
                            int _tmpGeneration = _cursor.getInt(_cursorIndexOfPeriodCount3);
                            _cursorIndexOfGeneration2 = _cursorIndexOfPeriodCount3;
                            int _cursorIndexOfGeneration3 = _cursorIndexOfNextScheduleTimeOverride2;
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(_cursorIndexOfGeneration3);
                            _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfGeneration3;
                            int _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfNextScheduleTimeOverrideGeneration2;
                            int _tmpNextScheduleTimeOverrideGeneration = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverride3);
                            _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverride3;
                            int _cursorIndexOfNextScheduleTimeOverrideGeneration3 = _cursorIndexOfStopReason2;
                            int _tmpStopReason = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverrideGeneration3);
                            _cursorIndexOfStopReason2 = _cursorIndexOfNextScheduleTimeOverrideGeneration3;
                            int _cursorIndexOfStopReason3 = _tmp_6;
                            int _tmp_62 = _cursor.getInt(_cursorIndexOfStopReason3);
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_62);
                            int _cursorIndexOfRequiredNetworkType = _tmp_7;
                            int _tmp_72 = _cursor.getInt(_cursorIndexOfRequiredNetworkType);
                            boolean _tmpRequiresCharging = _tmp_72 != 0;
                            int _cursorIndexOfRequiresCharging = _tmp_8;
                            int _tmp_82 = _cursor.getInt(_cursorIndexOfRequiresCharging);
                            boolean _tmpRequiresDeviceIdle = _tmp_82 != 0;
                            int _cursorIndexOfRequiresDeviceIdle = _tmp_9;
                            int _tmp_92 = _cursor.getInt(_cursorIndexOfRequiresDeviceIdle);
                            boolean _tmpRequiresBatteryNotLow = _tmp_92 != 0;
                            int _cursorIndexOfRequiresBatteryNotLow = _tmp_10;
                            int _tmp_102 = _cursor.getInt(_cursorIndexOfRequiresBatteryNotLow);
                            boolean _tmpRequiresStorageNotLow = _tmp_102 != 0;
                            int _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfContentTriggerUpdateDelayMillis2;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(_cursorIndexOfRequiresStorageNotLow);
                            _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfRequiresStorageNotLow;
                            int _cursorIndexOfContentTriggerUpdateDelayMillis3 = _cursorIndexOfContentTriggerMaxDelayMillis2;
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis3);
                            _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis3;
                            int _cursorIndexOfContentTriggerMaxDelayMillis3 = _cursorIndexOfContentUriTriggers2;
                            if (_cursor.isNull(_cursorIndexOfContentTriggerMaxDelayMillis3)) {
                                _tmp_11 = null;
                            } else {
                                _tmp_11 = _cursor.getBlob(_cursorIndexOfContentTriggerMaxDelayMillis3);
                            }
                            WorkTypeConverters workTypeConverters5 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_11);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            WorkSpec _item = new WorkSpec(_tmpId, _tmpState, _tmpWorkerClassName, _tmpInputMergerClassName, _tmpInput, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpMinimumRetentionDuration, _tmpScheduleRequestedAt, _tmpExpedited, _tmpOutOfQuotaPolicy, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpNextScheduleTimeOverrideGeneration, _tmpStopReason);
                            _result.add(_item);
                            _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentTriggerMaxDelayMillis3;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _tmp_4 = _cursorIndexOfScheduleRequestedAt3;
                            _tmp_5 = _cursorIndexOfExpedited;
                            _tmp_6 = _cursorIndexOfStopReason3;
                            _tmp_7 = _cursorIndexOfRequiredNetworkType;
                            _tmp_8 = _cursorIndexOfRequiresCharging;
                            _tmp_9 = _cursorIndexOfRequiresDeviceIdle;
                            _tmp_10 = _cursorIndexOfRequiresBatteryNotLow;
                        }
                        _cursor.close();
                        _statement.release();
                        return _result;
                    } catch (Throwable th) {
                        th = th;
                        _cursor.close();
                        _statement.release();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
                _statement = _statement2;
            }
        } catch (Throwable th4) {
            th = th4;
            _statement = _statement2;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec> getScheduledWork() throws Throwable {
        RoomSQLiteQuery _statement;
        String _tmpId;
        String _tmpWorkerClassName;
        String _tmpInputMergerClassName;
        byte[] _tmp_1;
        byte[] _tmp_2;
        byte[] _tmp_11;
        RoomSQLiteQuery _statement2 = RoomSQLiteQuery.acquire("SELECT * FROM workspec WHERE state=0 AND schedule_requested_at<>-1", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement2, false, null);
        try {
            int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
            int _cursorIndexOfWorkerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "worker_class_name");
            int _cursorIndexOfInputMergerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "input_merger_class_name");
            int _cursorIndexOfInput = CursorUtil.getColumnIndexOrThrow(_cursor, "input");
            int _cursorIndexOfOutput = CursorUtil.getColumnIndexOrThrow(_cursor, "output");
            int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndexOrThrow(_cursor, "initial_delay");
            int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "interval_duration");
            int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "flex_duration");
            int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "run_attempt_count");
            int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_policy");
            int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_delay_duration");
            int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndexOrThrow(_cursor, "last_enqueue_time");
            try {
                int _cursorIndexOfMinimumRetentionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "minimum_retention_duration");
                _statement = _statement2;
                try {
                    int _cursorIndexOfScheduleRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_requested_at");
                    int _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfScheduleRequestedAt;
                    int _tmp_4 = CursorUtil.getColumnIndexOrThrow(_cursor, "run_in_foreground");
                    int _tmp_5 = CursorUtil.getColumnIndexOrThrow(_cursor, "out_of_quota_policy");
                    int _cursorIndexOfPeriodCount = CursorUtil.getColumnIndexOrThrow(_cursor, "period_count");
                    int _cursorIndexOfPeriodCount2 = _cursorIndexOfPeriodCount;
                    int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
                    int _cursorIndexOfGeneration2 = _cursorIndexOfGeneration;
                    int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override");
                    int _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfNextScheduleTimeOverride;
                    int _cursorIndexOfNextScheduleTimeOverrideGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override_generation");
                    int _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverrideGeneration;
                    int _cursorIndexOfStopReason = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_reason");
                    int _cursorIndexOfStopReason2 = _cursorIndexOfStopReason;
                    int _tmp_6 = CursorUtil.getColumnIndexOrThrow(_cursor, "required_network_type");
                    int _tmp_7 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_charging");
                    int _tmp_8 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_device_idle");
                    int _tmp_9 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_battery_not_low");
                    int _tmp_10 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_storage_not_low");
                    int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_content_update_delay");
                    int _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis;
                    int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_max_content_delay");
                    int _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerMaxDelayMillis;
                    int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndexOrThrow(_cursor, "content_uri_triggers");
                    int _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentUriTriggers;
                    int _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfMinimumRetentionDuration;
                    int _cursorIndexOfMinimumRetentionDuration3 = _cursor.getCount();
                    List<WorkSpec> _result = new ArrayList<>(_cursorIndexOfMinimumRetentionDuration3);
                    while (_cursor.moveToNext()) {
                        if (_cursor.isNull(_cursorIndexOfId)) {
                            _tmpId = null;
                        } else {
                            String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                            _tmpId = _tmpId2;
                        }
                        int _tmp = _cursor.getInt(_cursorIndexOfState);
                        WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                        WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                        if (_cursor.isNull(_cursorIndexOfWorkerClassName)) {
                            _tmpWorkerClassName = null;
                        } else {
                            String _tmpWorkerClassName2 = _cursor.getString(_cursorIndexOfWorkerClassName);
                            _tmpWorkerClassName = _tmpWorkerClassName2;
                        }
                        if (_cursor.isNull(_cursorIndexOfInputMergerClassName)) {
                            _tmpInputMergerClassName = null;
                        } else {
                            String _tmpInputMergerClassName2 = _cursor.getString(_cursorIndexOfInputMergerClassName);
                            _tmpInputMergerClassName = _tmpInputMergerClassName2;
                        }
                        if (_cursor.isNull(_cursorIndexOfInput)) {
                            _tmp_1 = null;
                        } else {
                            byte[] _tmp_12 = _cursor.getBlob(_cursorIndexOfInput);
                            _tmp_1 = _tmp_12;
                        }
                        Data _tmpInput = Data.fromByteArray(_tmp_1);
                        if (_cursor.isNull(_cursorIndexOfOutput)) {
                            _tmp_2 = null;
                        } else {
                            byte[] _tmp_22 = _cursor.getBlob(_cursorIndexOfOutput);
                            _tmp_2 = _tmp_22;
                        }
                        Data _tmpOutput = Data.fromByteArray(_tmp_2);
                        long _tmpInitialDelay = _cursor.getLong(_cursorIndexOfInitialDelay);
                        long _tmpIntervalDuration = _cursor.getLong(_cursorIndexOfIntervalDuration);
                        long _tmpFlexDuration = _cursor.getLong(_cursorIndexOfFlexDuration);
                        int _tmpRunAttemptCount = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                        int _tmp_3 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                        WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                        BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_3);
                        long _tmpBackoffDelayDuration = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                        long _tmpLastEnqueueTime = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                        int _cursorIndexOfId2 = _cursorIndexOfId;
                        int _cursorIndexOfId3 = _cursorIndexOfMinimumRetentionDuration2;
                        long _tmpMinimumRetentionDuration = _cursor.getLong(_cursorIndexOfId3);
                        _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfId3;
                        int _cursorIndexOfMinimumRetentionDuration4 = _cursorIndexOfScheduleRequestedAt2;
                        long _tmpScheduleRequestedAt = _cursor.getLong(_cursorIndexOfMinimumRetentionDuration4);
                        _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfMinimumRetentionDuration4;
                        int _cursorIndexOfScheduleRequestedAt3 = _tmp_4;
                        int _tmp_42 = _cursor.getInt(_cursorIndexOfScheduleRequestedAt3);
                        boolean _tmpExpedited = _tmp_42 != 0;
                        int _cursorIndexOfExpedited = _tmp_5;
                        int _tmp_52 = _cursor.getInt(_cursorIndexOfExpedited);
                        WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                        OutOfQuotaPolicy _tmpOutOfQuotaPolicy = WorkTypeConverters.intToOutOfQuotaPolicy(_tmp_52);
                        int _cursorIndexOfOutOfQuotaPolicy = _cursorIndexOfPeriodCount2;
                        int _tmpPeriodCount = _cursor.getInt(_cursorIndexOfOutOfQuotaPolicy);
                        _cursorIndexOfPeriodCount2 = _cursorIndexOfOutOfQuotaPolicy;
                        int _cursorIndexOfPeriodCount3 = _cursorIndexOfGeneration2;
                        int _tmpGeneration = _cursor.getInt(_cursorIndexOfPeriodCount3);
                        _cursorIndexOfGeneration2 = _cursorIndexOfPeriodCount3;
                        int _cursorIndexOfGeneration3 = _cursorIndexOfNextScheduleTimeOverride2;
                        long _tmpNextScheduleTimeOverride = _cursor.getLong(_cursorIndexOfGeneration3);
                        _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfGeneration3;
                        int _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfNextScheduleTimeOverrideGeneration2;
                        int _tmpNextScheduleTimeOverrideGeneration = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverride3);
                        _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverride3;
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration3 = _cursorIndexOfStopReason2;
                        int _tmpStopReason = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverrideGeneration3);
                        _cursorIndexOfStopReason2 = _cursorIndexOfNextScheduleTimeOverrideGeneration3;
                        int _cursorIndexOfStopReason3 = _tmp_6;
                        int _tmp_62 = _cursor.getInt(_cursorIndexOfStopReason3);
                        WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                        NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_62);
                        int _cursorIndexOfRequiredNetworkType = _tmp_7;
                        int _tmp_72 = _cursor.getInt(_cursorIndexOfRequiredNetworkType);
                        boolean _tmpRequiresCharging = _tmp_72 != 0;
                        int _cursorIndexOfRequiresCharging = _tmp_8;
                        int _tmp_82 = _cursor.getInt(_cursorIndexOfRequiresCharging);
                        boolean _tmpRequiresDeviceIdle = _tmp_82 != 0;
                        int _cursorIndexOfRequiresDeviceIdle = _tmp_9;
                        int _tmp_92 = _cursor.getInt(_cursorIndexOfRequiresDeviceIdle);
                        boolean _tmpRequiresBatteryNotLow = _tmp_92 != 0;
                        int _cursorIndexOfRequiresBatteryNotLow = _tmp_10;
                        int _tmp_102 = _cursor.getInt(_cursorIndexOfRequiresBatteryNotLow);
                        boolean _tmpRequiresStorageNotLow = _tmp_102 != 0;
                        int _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfContentTriggerUpdateDelayMillis2;
                        long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(_cursorIndexOfRequiresStorageNotLow);
                        _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfRequiresStorageNotLow;
                        int _cursorIndexOfContentTriggerUpdateDelayMillis3 = _cursorIndexOfContentTriggerMaxDelayMillis2;
                        long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis3);
                        _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis3;
                        int _cursorIndexOfContentTriggerMaxDelayMillis3 = _cursorIndexOfContentUriTriggers2;
                        if (_cursor.isNull(_cursorIndexOfContentTriggerMaxDelayMillis3)) {
                            _tmp_11 = null;
                        } else {
                            _tmp_11 = _cursor.getBlob(_cursorIndexOfContentTriggerMaxDelayMillis3);
                        }
                        WorkTypeConverters workTypeConverters5 = WorkTypeConverters.INSTANCE;
                        Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_11);
                        Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                        WorkSpec _item = new WorkSpec(_tmpId, _tmpState, _tmpWorkerClassName, _tmpInputMergerClassName, _tmpInput, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpMinimumRetentionDuration, _tmpScheduleRequestedAt, _tmpExpedited, _tmpOutOfQuotaPolicy, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpNextScheduleTimeOverrideGeneration, _tmpStopReason);
                        _result.add(_item);
                        _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentTriggerMaxDelayMillis3;
                        _cursorIndexOfId = _cursorIndexOfId2;
                        _tmp_4 = _cursorIndexOfScheduleRequestedAt3;
                        _tmp_5 = _cursorIndexOfExpedited;
                        _tmp_6 = _cursorIndexOfStopReason3;
                        _tmp_7 = _cursorIndexOfRequiredNetworkType;
                        _tmp_8 = _cursorIndexOfRequiresCharging;
                        _tmp_9 = _cursorIndexOfRequiresDeviceIdle;
                        _tmp_10 = _cursorIndexOfRequiresBatteryNotLow;
                    }
                    _cursor.close();
                    _statement.release();
                    return _result;
                } catch (Throwable th) {
                    th = th;
                    _cursor.close();
                    _statement.release();
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                _statement = _statement2;
            }
        } catch (Throwable th3) {
            th = th3;
            _statement = _statement2;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec> getRunningWork() throws Throwable {
        RoomSQLiteQuery _statement;
        String _tmpId;
        String _tmpWorkerClassName;
        String _tmpInputMergerClassName;
        byte[] _tmp_1;
        byte[] _tmp_2;
        byte[] _tmp_11;
        RoomSQLiteQuery _statement2 = RoomSQLiteQuery.acquire("SELECT * FROM workspec WHERE state=1", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement2, false, null);
        try {
            int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
            int _cursorIndexOfWorkerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "worker_class_name");
            int _cursorIndexOfInputMergerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "input_merger_class_name");
            int _cursorIndexOfInput = CursorUtil.getColumnIndexOrThrow(_cursor, "input");
            int _cursorIndexOfOutput = CursorUtil.getColumnIndexOrThrow(_cursor, "output");
            int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndexOrThrow(_cursor, "initial_delay");
            int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "interval_duration");
            int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "flex_duration");
            int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "run_attempt_count");
            int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_policy");
            int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_delay_duration");
            int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndexOrThrow(_cursor, "last_enqueue_time");
            try {
                int _cursorIndexOfMinimumRetentionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "minimum_retention_duration");
                _statement = _statement2;
                try {
                    int _cursorIndexOfScheduleRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_requested_at");
                    int _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfScheduleRequestedAt;
                    int _tmp_4 = CursorUtil.getColumnIndexOrThrow(_cursor, "run_in_foreground");
                    int _tmp_5 = CursorUtil.getColumnIndexOrThrow(_cursor, "out_of_quota_policy");
                    int _cursorIndexOfPeriodCount = CursorUtil.getColumnIndexOrThrow(_cursor, "period_count");
                    int _cursorIndexOfPeriodCount2 = _cursorIndexOfPeriodCount;
                    int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
                    int _cursorIndexOfGeneration2 = _cursorIndexOfGeneration;
                    int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override");
                    int _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfNextScheduleTimeOverride;
                    int _cursorIndexOfNextScheduleTimeOverrideGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override_generation");
                    int _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverrideGeneration;
                    int _cursorIndexOfStopReason = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_reason");
                    int _cursorIndexOfStopReason2 = _cursorIndexOfStopReason;
                    int _tmp_6 = CursorUtil.getColumnIndexOrThrow(_cursor, "required_network_type");
                    int _tmp_7 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_charging");
                    int _tmp_8 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_device_idle");
                    int _tmp_9 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_battery_not_low");
                    int _tmp_10 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_storage_not_low");
                    int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_content_update_delay");
                    int _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis;
                    int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_max_content_delay");
                    int _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerMaxDelayMillis;
                    int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndexOrThrow(_cursor, "content_uri_triggers");
                    int _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentUriTriggers;
                    int _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfMinimumRetentionDuration;
                    int _cursorIndexOfMinimumRetentionDuration3 = _cursor.getCount();
                    List<WorkSpec> _result = new ArrayList<>(_cursorIndexOfMinimumRetentionDuration3);
                    while (_cursor.moveToNext()) {
                        if (_cursor.isNull(_cursorIndexOfId)) {
                            _tmpId = null;
                        } else {
                            String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                            _tmpId = _tmpId2;
                        }
                        int _tmp = _cursor.getInt(_cursorIndexOfState);
                        WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                        WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                        if (_cursor.isNull(_cursorIndexOfWorkerClassName)) {
                            _tmpWorkerClassName = null;
                        } else {
                            String _tmpWorkerClassName2 = _cursor.getString(_cursorIndexOfWorkerClassName);
                            _tmpWorkerClassName = _tmpWorkerClassName2;
                        }
                        if (_cursor.isNull(_cursorIndexOfInputMergerClassName)) {
                            _tmpInputMergerClassName = null;
                        } else {
                            String _tmpInputMergerClassName2 = _cursor.getString(_cursorIndexOfInputMergerClassName);
                            _tmpInputMergerClassName = _tmpInputMergerClassName2;
                        }
                        if (_cursor.isNull(_cursorIndexOfInput)) {
                            _tmp_1 = null;
                        } else {
                            byte[] _tmp_12 = _cursor.getBlob(_cursorIndexOfInput);
                            _tmp_1 = _tmp_12;
                        }
                        Data _tmpInput = Data.fromByteArray(_tmp_1);
                        if (_cursor.isNull(_cursorIndexOfOutput)) {
                            _tmp_2 = null;
                        } else {
                            byte[] _tmp_22 = _cursor.getBlob(_cursorIndexOfOutput);
                            _tmp_2 = _tmp_22;
                        }
                        Data _tmpOutput = Data.fromByteArray(_tmp_2);
                        long _tmpInitialDelay = _cursor.getLong(_cursorIndexOfInitialDelay);
                        long _tmpIntervalDuration = _cursor.getLong(_cursorIndexOfIntervalDuration);
                        long _tmpFlexDuration = _cursor.getLong(_cursorIndexOfFlexDuration);
                        int _tmpRunAttemptCount = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                        int _tmp_3 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                        WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                        BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_3);
                        long _tmpBackoffDelayDuration = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                        long _tmpLastEnqueueTime = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                        int _cursorIndexOfId2 = _cursorIndexOfId;
                        int _cursorIndexOfId3 = _cursorIndexOfMinimumRetentionDuration2;
                        long _tmpMinimumRetentionDuration = _cursor.getLong(_cursorIndexOfId3);
                        _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfId3;
                        int _cursorIndexOfMinimumRetentionDuration4 = _cursorIndexOfScheduleRequestedAt2;
                        long _tmpScheduleRequestedAt = _cursor.getLong(_cursorIndexOfMinimumRetentionDuration4);
                        _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfMinimumRetentionDuration4;
                        int _cursorIndexOfScheduleRequestedAt3 = _tmp_4;
                        int _tmp_42 = _cursor.getInt(_cursorIndexOfScheduleRequestedAt3);
                        boolean _tmpExpedited = _tmp_42 != 0;
                        int _cursorIndexOfExpedited = _tmp_5;
                        int _tmp_52 = _cursor.getInt(_cursorIndexOfExpedited);
                        WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                        OutOfQuotaPolicy _tmpOutOfQuotaPolicy = WorkTypeConverters.intToOutOfQuotaPolicy(_tmp_52);
                        int _cursorIndexOfOutOfQuotaPolicy = _cursorIndexOfPeriodCount2;
                        int _tmpPeriodCount = _cursor.getInt(_cursorIndexOfOutOfQuotaPolicy);
                        _cursorIndexOfPeriodCount2 = _cursorIndexOfOutOfQuotaPolicy;
                        int _cursorIndexOfPeriodCount3 = _cursorIndexOfGeneration2;
                        int _tmpGeneration = _cursor.getInt(_cursorIndexOfPeriodCount3);
                        _cursorIndexOfGeneration2 = _cursorIndexOfPeriodCount3;
                        int _cursorIndexOfGeneration3 = _cursorIndexOfNextScheduleTimeOverride2;
                        long _tmpNextScheduleTimeOverride = _cursor.getLong(_cursorIndexOfGeneration3);
                        _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfGeneration3;
                        int _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfNextScheduleTimeOverrideGeneration2;
                        int _tmpNextScheduleTimeOverrideGeneration = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverride3);
                        _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverride3;
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration3 = _cursorIndexOfStopReason2;
                        int _tmpStopReason = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverrideGeneration3);
                        _cursorIndexOfStopReason2 = _cursorIndexOfNextScheduleTimeOverrideGeneration3;
                        int _cursorIndexOfStopReason3 = _tmp_6;
                        int _tmp_62 = _cursor.getInt(_cursorIndexOfStopReason3);
                        WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                        NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_62);
                        int _cursorIndexOfRequiredNetworkType = _tmp_7;
                        int _tmp_72 = _cursor.getInt(_cursorIndexOfRequiredNetworkType);
                        boolean _tmpRequiresCharging = _tmp_72 != 0;
                        int _cursorIndexOfRequiresCharging = _tmp_8;
                        int _tmp_82 = _cursor.getInt(_cursorIndexOfRequiresCharging);
                        boolean _tmpRequiresDeviceIdle = _tmp_82 != 0;
                        int _cursorIndexOfRequiresDeviceIdle = _tmp_9;
                        int _tmp_92 = _cursor.getInt(_cursorIndexOfRequiresDeviceIdle);
                        boolean _tmpRequiresBatteryNotLow = _tmp_92 != 0;
                        int _cursorIndexOfRequiresBatteryNotLow = _tmp_10;
                        int _tmp_102 = _cursor.getInt(_cursorIndexOfRequiresBatteryNotLow);
                        boolean _tmpRequiresStorageNotLow = _tmp_102 != 0;
                        int _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfContentTriggerUpdateDelayMillis2;
                        long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(_cursorIndexOfRequiresStorageNotLow);
                        _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfRequiresStorageNotLow;
                        int _cursorIndexOfContentTriggerUpdateDelayMillis3 = _cursorIndexOfContentTriggerMaxDelayMillis2;
                        long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis3);
                        _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis3;
                        int _cursorIndexOfContentTriggerMaxDelayMillis3 = _cursorIndexOfContentUriTriggers2;
                        if (_cursor.isNull(_cursorIndexOfContentTriggerMaxDelayMillis3)) {
                            _tmp_11 = null;
                        } else {
                            _tmp_11 = _cursor.getBlob(_cursorIndexOfContentTriggerMaxDelayMillis3);
                        }
                        WorkTypeConverters workTypeConverters5 = WorkTypeConverters.INSTANCE;
                        Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_11);
                        Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                        WorkSpec _item = new WorkSpec(_tmpId, _tmpState, _tmpWorkerClassName, _tmpInputMergerClassName, _tmpInput, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpMinimumRetentionDuration, _tmpScheduleRequestedAt, _tmpExpedited, _tmpOutOfQuotaPolicy, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpNextScheduleTimeOverrideGeneration, _tmpStopReason);
                        _result.add(_item);
                        _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentTriggerMaxDelayMillis3;
                        _cursorIndexOfId = _cursorIndexOfId2;
                        _tmp_4 = _cursorIndexOfScheduleRequestedAt3;
                        _tmp_5 = _cursorIndexOfExpedited;
                        _tmp_6 = _cursorIndexOfStopReason3;
                        _tmp_7 = _cursorIndexOfRequiredNetworkType;
                        _tmp_8 = _cursorIndexOfRequiresCharging;
                        _tmp_9 = _cursorIndexOfRequiresDeviceIdle;
                        _tmp_10 = _cursorIndexOfRequiresBatteryNotLow;
                    }
                    _cursor.close();
                    _statement.release();
                    return _result;
                } catch (Throwable th) {
                    th = th;
                    _cursor.close();
                    _statement.release();
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                _statement = _statement2;
            }
        } catch (Throwable th3) {
            th = th3;
            _statement = _statement2;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public List<WorkSpec> getRecentlyCompletedWork(final long startingAt) throws Throwable {
        RoomSQLiteQuery _statement;
        String _tmpId;
        String _tmpWorkerClassName;
        String _tmpInputMergerClassName;
        byte[] _tmp_1;
        byte[] _tmp_2;
        byte[] _tmp_11;
        RoomSQLiteQuery _statement2 = RoomSQLiteQuery.acquire("SELECT * FROM workspec WHERE last_enqueue_time >= ? AND state IN (2, 3, 5) ORDER BY last_enqueue_time DESC", 1);
        _statement2.bindLong(1, startingAt);
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement2, false, null);
        try {
            int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
            int _cursorIndexOfWorkerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "worker_class_name");
            int _cursorIndexOfInputMergerClassName = CursorUtil.getColumnIndexOrThrow(_cursor, "input_merger_class_name");
            int _cursorIndexOfInput = CursorUtil.getColumnIndexOrThrow(_cursor, "input");
            int _cursorIndexOfOutput = CursorUtil.getColumnIndexOrThrow(_cursor, "output");
            int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndexOrThrow(_cursor, "initial_delay");
            int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "interval_duration");
            int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "flex_duration");
            int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "run_attempt_count");
            try {
                int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_policy");
                _statement = _statement2;
                try {
                    int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "backoff_delay_duration");
                    try {
                        int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndexOrThrow(_cursor, "last_enqueue_time");
                        int _cursorIndexOfMinimumRetentionDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "minimum_retention_duration");
                        int _cursorIndexOfScheduleRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_requested_at");
                        int _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfScheduleRequestedAt;
                        int _tmp_4 = CursorUtil.getColumnIndexOrThrow(_cursor, "run_in_foreground");
                        int _tmp_5 = CursorUtil.getColumnIndexOrThrow(_cursor, "out_of_quota_policy");
                        int _cursorIndexOfPeriodCount = CursorUtil.getColumnIndexOrThrow(_cursor, "period_count");
                        int _cursorIndexOfPeriodCount2 = _cursorIndexOfPeriodCount;
                        int _cursorIndexOfGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "generation");
                        int _cursorIndexOfGeneration2 = _cursorIndexOfGeneration;
                        int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override");
                        int _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfNextScheduleTimeOverride;
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration = CursorUtil.getColumnIndexOrThrow(_cursor, "next_schedule_time_override_generation");
                        int _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverrideGeneration;
                        int _cursorIndexOfStopReason = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_reason");
                        int _cursorIndexOfStopReason2 = _cursorIndexOfStopReason;
                        int _tmp_6 = CursorUtil.getColumnIndexOrThrow(_cursor, "required_network_type");
                        int _tmp_7 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_charging");
                        int _tmp_8 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_device_idle");
                        int _tmp_9 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_battery_not_low");
                        int _tmp_10 = CursorUtil.getColumnIndexOrThrow(_cursor, "requires_storage_not_low");
                        int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_content_update_delay");
                        int _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis;
                        int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger_max_content_delay");
                        int _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerMaxDelayMillis;
                        int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndexOrThrow(_cursor, "content_uri_triggers");
                        int _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentUriTriggers;
                        int _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfMinimumRetentionDuration;
                        int _cursorIndexOfMinimumRetentionDuration3 = _cursor.getCount();
                        List<WorkSpec> _result = new ArrayList<>(_cursorIndexOfMinimumRetentionDuration3);
                        while (_cursor.moveToNext()) {
                            if (_cursor.isNull(_cursorIndexOfId)) {
                                _tmpId = null;
                            } else {
                                String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                                _tmpId = _tmpId2;
                            }
                            int _tmp = _cursor.getInt(_cursorIndexOfState);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            WorkInfo.State _tmpState = WorkTypeConverters.intToState(_tmp);
                            if (_cursor.isNull(_cursorIndexOfWorkerClassName)) {
                                _tmpWorkerClassName = null;
                            } else {
                                String _tmpWorkerClassName2 = _cursor.getString(_cursorIndexOfWorkerClassName);
                                _tmpWorkerClassName = _tmpWorkerClassName2;
                            }
                            if (_cursor.isNull(_cursorIndexOfInputMergerClassName)) {
                                _tmpInputMergerClassName = null;
                            } else {
                                String _tmpInputMergerClassName2 = _cursor.getString(_cursorIndexOfInputMergerClassName);
                                _tmpInputMergerClassName = _tmpInputMergerClassName2;
                            }
                            if (_cursor.isNull(_cursorIndexOfInput)) {
                                _tmp_1 = null;
                            } else {
                                byte[] _tmp_12 = _cursor.getBlob(_cursorIndexOfInput);
                                _tmp_1 = _tmp_12;
                            }
                            Data _tmpInput = Data.fromByteArray(_tmp_1);
                            if (_cursor.isNull(_cursorIndexOfOutput)) {
                                _tmp_2 = null;
                            } else {
                                byte[] _tmp_22 = _cursor.getBlob(_cursorIndexOfOutput);
                                _tmp_2 = _tmp_22;
                            }
                            Data _tmpOutput = Data.fromByteArray(_tmp_2);
                            long _tmpInitialDelay = _cursor.getLong(_cursorIndexOfInitialDelay);
                            long _tmpIntervalDuration = _cursor.getLong(_cursorIndexOfIntervalDuration);
                            long _tmpFlexDuration = _cursor.getLong(_cursorIndexOfFlexDuration);
                            int _tmpRunAttemptCount = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                            int _tmp_3 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            BackoffPolicy _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_3);
                            long _tmpBackoffDelayDuration = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                            long _tmpLastEnqueueTime = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                            int _cursorIndexOfId2 = _cursorIndexOfId;
                            int _cursorIndexOfId3 = _cursorIndexOfMinimumRetentionDuration2;
                            long _tmpMinimumRetentionDuration = _cursor.getLong(_cursorIndexOfId3);
                            _cursorIndexOfMinimumRetentionDuration2 = _cursorIndexOfId3;
                            int _cursorIndexOfMinimumRetentionDuration4 = _cursorIndexOfScheduleRequestedAt2;
                            long _tmpScheduleRequestedAt = _cursor.getLong(_cursorIndexOfMinimumRetentionDuration4);
                            _cursorIndexOfScheduleRequestedAt2 = _cursorIndexOfMinimumRetentionDuration4;
                            int _cursorIndexOfScheduleRequestedAt3 = _tmp_4;
                            int _tmp_42 = _cursor.getInt(_cursorIndexOfScheduleRequestedAt3);
                            boolean _tmpExpedited = _tmp_42 != 0;
                            int _cursorIndexOfExpedited = _tmp_5;
                            int _tmp_52 = _cursor.getInt(_cursorIndexOfExpedited);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            OutOfQuotaPolicy _tmpOutOfQuotaPolicy = WorkTypeConverters.intToOutOfQuotaPolicy(_tmp_52);
                            int _cursorIndexOfOutOfQuotaPolicy = _cursorIndexOfPeriodCount2;
                            int _tmpPeriodCount = _cursor.getInt(_cursorIndexOfOutOfQuotaPolicy);
                            _cursorIndexOfPeriodCount2 = _cursorIndexOfOutOfQuotaPolicy;
                            int _cursorIndexOfPeriodCount3 = _cursorIndexOfGeneration2;
                            int _tmpGeneration = _cursor.getInt(_cursorIndexOfPeriodCount3);
                            _cursorIndexOfGeneration2 = _cursorIndexOfPeriodCount3;
                            int _cursorIndexOfGeneration3 = _cursorIndexOfNextScheduleTimeOverride2;
                            long _tmpNextScheduleTimeOverride = _cursor.getLong(_cursorIndexOfGeneration3);
                            _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfGeneration3;
                            int _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfNextScheduleTimeOverrideGeneration2;
                            int _tmpNextScheduleTimeOverrideGeneration = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverride3);
                            _cursorIndexOfNextScheduleTimeOverrideGeneration2 = _cursorIndexOfNextScheduleTimeOverride3;
                            int _cursorIndexOfNextScheduleTimeOverrideGeneration3 = _cursorIndexOfStopReason2;
                            int _tmpStopReason = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverrideGeneration3);
                            _cursorIndexOfStopReason2 = _cursorIndexOfNextScheduleTimeOverrideGeneration3;
                            int _cursorIndexOfStopReason3 = _tmp_6;
                            int _tmp_62 = _cursor.getInt(_cursorIndexOfStopReason3);
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            NetworkType _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_62);
                            int _cursorIndexOfRequiredNetworkType = _tmp_7;
                            int _tmp_72 = _cursor.getInt(_cursorIndexOfRequiredNetworkType);
                            boolean _tmpRequiresCharging = _tmp_72 != 0;
                            int _cursorIndexOfRequiresCharging = _tmp_8;
                            int _tmp_82 = _cursor.getInt(_cursorIndexOfRequiresCharging);
                            boolean _tmpRequiresDeviceIdle = _tmp_82 != 0;
                            int _cursorIndexOfRequiresDeviceIdle = _tmp_9;
                            int _tmp_92 = _cursor.getInt(_cursorIndexOfRequiresDeviceIdle);
                            boolean _tmpRequiresBatteryNotLow = _tmp_92 != 0;
                            int _cursorIndexOfRequiresBatteryNotLow = _tmp_10;
                            int _tmp_102 = _cursor.getInt(_cursorIndexOfRequiresBatteryNotLow);
                            boolean _tmpRequiresStorageNotLow = _tmp_102 != 0;
                            int _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfContentTriggerUpdateDelayMillis2;
                            long _tmpContentTriggerUpdateDelayMillis = _cursor.getLong(_cursorIndexOfRequiresStorageNotLow);
                            _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfRequiresStorageNotLow;
                            int _cursorIndexOfContentTriggerUpdateDelayMillis3 = _cursorIndexOfContentTriggerMaxDelayMillis2;
                            long _tmpContentTriggerMaxDelayMillis = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis3);
                            _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis3;
                            int _cursorIndexOfContentTriggerMaxDelayMillis3 = _cursorIndexOfContentUriTriggers2;
                            if (_cursor.isNull(_cursorIndexOfContentTriggerMaxDelayMillis3)) {
                                _tmp_11 = null;
                            } else {
                                _tmp_11 = _cursor.getBlob(_cursorIndexOfContentTriggerMaxDelayMillis3);
                            }
                            WorkTypeConverters workTypeConverters5 = WorkTypeConverters.INSTANCE;
                            Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_11);
                            Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                            WorkSpec _item = new WorkSpec(_tmpId, _tmpState, _tmpWorkerClassName, _tmpInputMergerClassName, _tmpInput, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpMinimumRetentionDuration, _tmpScheduleRequestedAt, _tmpExpedited, _tmpOutOfQuotaPolicy, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpNextScheduleTimeOverrideGeneration, _tmpStopReason);
                            _result.add(_item);
                            _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentTriggerMaxDelayMillis3;
                            _cursorIndexOfId = _cursorIndexOfId2;
                            _tmp_4 = _cursorIndexOfScheduleRequestedAt3;
                            _tmp_5 = _cursorIndexOfExpedited;
                            _tmp_6 = _cursorIndexOfStopReason3;
                            _tmp_7 = _cursorIndexOfRequiredNetworkType;
                            _tmp_8 = _cursorIndexOfRequiresCharging;
                            _tmp_9 = _cursorIndexOfRequiresDeviceIdle;
                            _tmp_10 = _cursorIndexOfRequiresBatteryNotLow;
                        }
                        _cursor.close();
                        _statement.release();
                        return _result;
                    } catch (Throwable th) {
                        th = th;
                        _cursor.close();
                        _statement.release();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
                _statement = _statement2;
            }
        } catch (Throwable th4) {
            th = th4;
            _statement = _statement2;
        }
    }

    @Override // androidx.work.impl.model.WorkSpecDao
    public int countNonFinishedContentUriTriggerWorkers() {
        int _result;
        RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire("Select COUNT(*) FROM workspec WHERE LENGTH(content_uri_triggers)<>0 AND state NOT IN (2, 3, 5)", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, _statement, false, null);
        try {
            if (_cursor.moveToFirst()) {
                _result = _cursor.getInt(0);
            } else {
                _result = 0;
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

    /* JADX INFO: Access modifiers changed from: private */
    public void __fetchRelationshipWorkTagAsjavaLangString(final HashMap<String, ArrayList<String>> _map) {
        String _item_1;
        Set<String> __mapKeySet = _map.keySet();
        if (__mapKeySet.isEmpty()) {
            return;
        }
        if (_map.size() > 999) {
            HashMap<String, ArrayList<String>> _tmpInnerMap = new HashMap<>(RoomDatabase.MAX_BIND_PARAMETER_CNT);
            int _tmpIndex = 0;
            for (String _mapKey : __mapKeySet) {
                _tmpInnerMap.put(_mapKey, _map.get(_mapKey));
                _tmpIndex++;
                if (_tmpIndex == 999) {
                    __fetchRelationshipWorkTagAsjavaLangString(_tmpInnerMap);
                    HashMap<String, ArrayList<String>> _tmpInnerMap2 = new HashMap<>(RoomDatabase.MAX_BIND_PARAMETER_CNT);
                    _tmpIndex = 0;
                    _tmpInnerMap = _tmpInnerMap2;
                }
            }
            if (_tmpIndex > 0) {
                __fetchRelationshipWorkTagAsjavaLangString(_tmpInnerMap);
                return;
            }
            return;
        }
        StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("SELECT `tag`,`work_spec_id` FROM `WorkTag` WHERE `work_spec_id` IN (");
        int _inputSize = __mapKeySet.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        String _sql = _stringBuilder.toString();
        int _argCount = _inputSize + 0;
        RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
        int _argIndex = 1;
        for (String _item : __mapKeySet) {
            if (_item == null) {
                _stmt.bindNull(_argIndex);
            } else {
                _stmt.bindString(_argIndex, _item);
            }
            _argIndex++;
        }
        Cursor _cursor = DBUtil.query(this.__db, _stmt, false, null);
        try {
            int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "work_spec_id");
            if (_itemKeyIndex == -1) {
                return;
            }
            while (_cursor.moveToNext()) {
                String _tmpKey = _cursor.getString(_itemKeyIndex);
                ArrayList<String> _tmpRelation = _map.get(_tmpKey);
                if (_tmpRelation != null) {
                    if (_cursor.isNull(0)) {
                        _item_1 = null;
                    } else {
                        _item_1 = _cursor.getString(0);
                    }
                    _tmpRelation.add(_item_1);
                }
            }
        } finally {
            _cursor.close();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void __fetchRelationshipWorkProgressAsandroidxWorkData(final HashMap<String, ArrayList<Data>> _map) {
        byte[] _tmp;
        Set<String> __mapKeySet = _map.keySet();
        if (__mapKeySet.isEmpty()) {
            return;
        }
        if (_map.size() > 999) {
            HashMap<String, ArrayList<Data>> _tmpInnerMap = new HashMap<>(RoomDatabase.MAX_BIND_PARAMETER_CNT);
            int _tmpIndex = 0;
            for (String _mapKey : __mapKeySet) {
                _tmpInnerMap.put(_mapKey, _map.get(_mapKey));
                _tmpIndex++;
                if (_tmpIndex == 999) {
                    __fetchRelationshipWorkProgressAsandroidxWorkData(_tmpInnerMap);
                    HashMap<String, ArrayList<Data>> _tmpInnerMap2 = new HashMap<>(RoomDatabase.MAX_BIND_PARAMETER_CNT);
                    _tmpIndex = 0;
                    _tmpInnerMap = _tmpInnerMap2;
                }
            }
            if (_tmpIndex > 0) {
                __fetchRelationshipWorkProgressAsandroidxWorkData(_tmpInnerMap);
                return;
            }
            return;
        }
        StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("SELECT `progress`,`work_spec_id` FROM `WorkProgress` WHERE `work_spec_id` IN (");
        int _inputSize = __mapKeySet.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        String _sql = _stringBuilder.toString();
        int _argCount = _inputSize + 0;
        RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
        int _argIndex = 1;
        for (String _item : __mapKeySet) {
            if (_item == null) {
                _stmt.bindNull(_argIndex);
            } else {
                _stmt.bindString(_argIndex, _item);
            }
            _argIndex++;
        }
        Cursor _cursor = DBUtil.query(this.__db, _stmt, false, null);
        try {
            int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "work_spec_id");
            if (_itemKeyIndex == -1) {
                return;
            }
            while (_cursor.moveToNext()) {
                String _tmpKey = _cursor.getString(_itemKeyIndex);
                ArrayList<Data> _tmpRelation = _map.get(_tmpKey);
                if (_tmpRelation != null) {
                    if (_cursor.isNull(0)) {
                        _tmp = null;
                    } else {
                        _tmp = _cursor.getBlob(0);
                    }
                    Data _item_1 = Data.fromByteArray(_tmp);
                    _tmpRelation.add(_item_1);
                }
            }
        } finally {
            _cursor.close();
        }
    }
}
