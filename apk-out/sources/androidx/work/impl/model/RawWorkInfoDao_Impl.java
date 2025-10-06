package androidx.work.impl.model;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.WorkInfo;
import androidx.work.impl.model.WorkSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import kotlinx.coroutines.flow.Flow;

/* loaded from: classes.dex */
public final class RawWorkInfoDao_Impl implements RawWorkInfoDao {
    private final RoomDatabase __db;

    public RawWorkInfoDao_Impl(RoomDatabase __db) {
        this.__db = __db;
    }

    @Override // androidx.work.impl.model.RawWorkInfoDao
    public List<WorkSpec.WorkInfoPojo> getWorkInfoPojos(final SupportSQLiteQuery query) throws Throwable {
        String _tmpId;
        WorkInfo.State _tmpState;
        byte[] _tmp_1;
        Data _tmpOutput;
        long _tmpInitialDelay;
        long _tmpIntervalDuration;
        long _tmpFlexDuration;
        int _tmpRunAttemptCount;
        BackoffPolicy _tmpBackoffPolicy;
        long _tmpBackoffDelayDuration;
        long _tmpLastEnqueueTime;
        int _tmpPeriodCount;
        int _tmpGeneration;
        long _tmpNextScheduleTimeOverride;
        int _tmpStopReason;
        NetworkType _tmpRequiredNetworkType;
        boolean _tmpRequiresCharging;
        boolean _tmpRequiresDeviceIdle;
        boolean _tmpRequiresBatteryNotLow;
        boolean _tmpRequiresStorageNotLow;
        long _tmpContentTriggerUpdateDelayMillis;
        long _tmpContentTriggerMaxDelayMillis;
        byte[] _tmp_8;
        Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers;
        ArrayList<String> _tmpTagsCollection_1;
        ArrayList<Data> _tmpProgressCollection_1;
        int _cursorIndexOfGeneration;
        int _cursorIndexOfPeriodCount;
        this.__db.assertNotSuspendingTransaction();
        Cursor _cursor = DBUtil.query(this.__db, query, true, null);
        try {
            int _cursorIndexOfId = CursorUtil.getColumnIndex(_cursor, "id");
            int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndex(_cursor, "state");
            int _cursorIndexOfOutput = CursorUtil.getColumnIndex(_cursor, "output");
            int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndex(_cursor, "initial_delay");
            int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndex(_cursor, "interval_duration");
            int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndex(_cursor, "flex_duration");
            int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndex(_cursor, "run_attempt_count");
            int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndex(_cursor, "backoff_policy");
            int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndex(_cursor, "backoff_delay_duration");
            int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndex(_cursor, "last_enqueue_time");
            int _cursorIndexOfPeriodCount2 = CursorUtil.getColumnIndex(_cursor, "period_count");
            int _cursorIndexOfGeneration2 = CursorUtil.getColumnIndex(_cursor, "generation");
            int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndex(_cursor, "next_schedule_time_override");
            try {
                int _cursorIndexOfStopReason = CursorUtil.getColumnIndex(_cursor, "stop_reason");
                int _cursorIndexOfStopReason2 = _cursorIndexOfStopReason;
                int _cursorIndexOfRequiredNetworkType = CursorUtil.getColumnIndex(_cursor, "required_network_type");
                int _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfRequiredNetworkType;
                int _cursorIndexOfRequiresCharging = CursorUtil.getColumnIndex(_cursor, "requires_charging");
                int _cursorIndexOfRequiredNetworkType2 = _cursorIndexOfRequiresCharging;
                int _cursorIndexOfRequiresDeviceIdle = CursorUtil.getColumnIndex(_cursor, "requires_device_idle");
                int _cursorIndexOfState = _cursorIndexOfRequiresDeviceIdle;
                int _cursorIndexOfRequiresBatteryNotLow = CursorUtil.getColumnIndex(_cursor, "requires_battery_not_low");
                int _cursorIndexOfRequiresDeviceIdle2 = _cursorIndexOfRequiresBatteryNotLow;
                int _cursorIndexOfRequiresStorageNotLow2 = CursorUtil.getColumnIndex(_cursor, "requires_storage_not_low");
                int _cursorIndexOfRequiresBatteryNotLow2 = _cursorIndexOfRequiresStorageNotLow2;
                int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndex(_cursor, "trigger_content_update_delay");
                int _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis;
                int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndex(_cursor, "trigger_max_content_delay");
                int _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerMaxDelayMillis;
                int _cursorIndexOfContentUriTriggers2 = CursorUtil.getColumnIndex(_cursor, "content_uri_triggers");
                HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                while (_cursor.moveToNext()) {
                    String _tmpKey = _cursor.getString(_cursorIndexOfId);
                    int _cursorIndexOfContentUriTriggers3 = _cursorIndexOfContentUriTriggers2;
                    HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                    int _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfNextScheduleTimeOverride;
                    ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                    if (_tmpTagsCollection != null) {
                        _cursorIndexOfGeneration = _cursorIndexOfGeneration2;
                    } else {
                        ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                        _cursorIndexOfGeneration = _cursorIndexOfGeneration2;
                        _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                    }
                    String _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
                    HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                    ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                    if (_tmpProgressCollection != null) {
                        _cursorIndexOfPeriodCount = _cursorIndexOfPeriodCount2;
                    } else {
                        ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                        _cursorIndexOfPeriodCount = _cursorIndexOfPeriodCount2;
                        _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                    }
                    _collectionProgress = _collectionProgress2;
                    _cursorIndexOfNextScheduleTimeOverride = _cursorIndexOfNextScheduleTimeOverride2;
                    _cursorIndexOfGeneration2 = _cursorIndexOfGeneration;
                    _cursorIndexOfPeriodCount2 = _cursorIndexOfPeriodCount;
                    _collectionTags = _collectionTags2;
                    _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentUriTriggers3;
                }
                int _cursorIndexOfContentUriTriggers4 = _cursorIndexOfContentUriTriggers2;
                int _cursorIndexOfPeriodCount3 = _cursorIndexOfPeriodCount2;
                int _cursorIndexOfGeneration3 = _cursorIndexOfGeneration2;
                HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                int _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfNextScheduleTimeOverride;
                HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                _cursor.moveToPosition(-1);
                __fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                __fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                while (_cursor.moveToNext()) {
                    if (_cursorIndexOfId == -1 || _cursor.isNull(_cursorIndexOfId)) {
                        _tmpId = null;
                    } else {
                        String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                        _tmpId = _tmpId2;
                    }
                    if (_cursorIndexOfContentUriTriggers == -1) {
                        _tmpState = null;
                    } else {
                        int _tmp = _cursor.getInt(_cursorIndexOfContentUriTriggers);
                        WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                        _tmpState = WorkTypeConverters.intToState(_tmp);
                    }
                    if (_cursorIndexOfOutput == -1) {
                        _tmpOutput = null;
                    } else {
                        if (_cursor.isNull(_cursorIndexOfOutput)) {
                            _tmp_1 = null;
                        } else {
                            _tmp_1 = _cursor.getBlob(_cursorIndexOfOutput);
                        }
                        _tmpOutput = Data.fromByteArray(_tmp_1);
                    }
                    if (_cursorIndexOfInitialDelay == -1) {
                        _tmpInitialDelay = 0;
                    } else {
                        long _tmpInitialDelay2 = _cursor.getLong(_cursorIndexOfInitialDelay);
                        _tmpInitialDelay = _tmpInitialDelay2;
                    }
                    if (_cursorIndexOfIntervalDuration == -1) {
                        _tmpIntervalDuration = 0;
                    } else {
                        long _tmpIntervalDuration2 = _cursor.getLong(_cursorIndexOfIntervalDuration);
                        _tmpIntervalDuration = _tmpIntervalDuration2;
                    }
                    if (_cursorIndexOfFlexDuration == -1) {
                        _tmpFlexDuration = 0;
                    } else {
                        long _tmpFlexDuration2 = _cursor.getLong(_cursorIndexOfFlexDuration);
                        _tmpFlexDuration = _tmpFlexDuration2;
                    }
                    if (_cursorIndexOfRunAttemptCount == -1) {
                        _tmpRunAttemptCount = 0;
                    } else {
                        int _tmpRunAttemptCount2 = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                        _tmpRunAttemptCount = _tmpRunAttemptCount2;
                    }
                    if (_cursorIndexOfBackoffPolicy == -1) {
                        _tmpBackoffPolicy = null;
                    } else {
                        int _tmp_2 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                        WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                        _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                    }
                    if (_cursorIndexOfBackoffDelayDuration == -1) {
                        _tmpBackoffDelayDuration = 0;
                    } else {
                        long _tmpBackoffDelayDuration2 = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                        _tmpBackoffDelayDuration = _tmpBackoffDelayDuration2;
                    }
                    if (_cursorIndexOfLastEnqueueTime == -1) {
                        _tmpLastEnqueueTime = 0;
                    } else {
                        long _tmpLastEnqueueTime2 = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                        _tmpLastEnqueueTime = _tmpLastEnqueueTime2;
                    }
                    int _cursorIndexOfPeriodCount4 = _cursorIndexOfPeriodCount3;
                    if (_cursorIndexOfPeriodCount4 == -1) {
                        _tmpPeriodCount = 0;
                    } else {
                        int _tmpPeriodCount2 = _cursor.getInt(_cursorIndexOfPeriodCount4);
                        _tmpPeriodCount = _tmpPeriodCount2;
                    }
                    _cursorIndexOfPeriodCount3 = _cursorIndexOfPeriodCount4;
                    int _cursorIndexOfPeriodCount5 = _cursorIndexOfGeneration3;
                    if (_cursorIndexOfPeriodCount5 == -1) {
                        _tmpGeneration = 0;
                    } else {
                        int _tmpGeneration2 = _cursor.getInt(_cursorIndexOfPeriodCount5);
                        _tmpGeneration = _tmpGeneration2;
                    }
                    _cursorIndexOfGeneration3 = _cursorIndexOfPeriodCount5;
                    int _cursorIndexOfGeneration4 = _cursorIndexOfNextScheduleTimeOverride3;
                    if (_cursorIndexOfGeneration4 == -1) {
                        _tmpNextScheduleTimeOverride = 0;
                    } else {
                        long _tmpNextScheduleTimeOverride2 = _cursor.getLong(_cursorIndexOfGeneration4);
                        _tmpNextScheduleTimeOverride = _tmpNextScheduleTimeOverride2;
                    }
                    _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfGeneration4;
                    int _cursorIndexOfNextScheduleTimeOverride4 = _cursorIndexOfStopReason2;
                    if (_cursorIndexOfNextScheduleTimeOverride4 == -1) {
                        _tmpStopReason = 0;
                    } else {
                        int _tmpStopReason2 = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverride4);
                        _tmpStopReason = _tmpStopReason2;
                    }
                    _cursorIndexOfStopReason2 = _cursorIndexOfNextScheduleTimeOverride4;
                    int _cursorIndexOfStopReason3 = _cursorIndexOfRequiresStorageNotLow;
                    if (_cursorIndexOfStopReason3 == -1) {
                        _tmpRequiredNetworkType = null;
                    } else {
                        int _tmp_3 = _cursor.getInt(_cursorIndexOfStopReason3);
                        WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                        _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                    }
                    int _tmp_32 = _cursorIndexOfRequiredNetworkType2;
                    if (_tmp_32 == -1) {
                        _tmpRequiresCharging = false;
                    } else {
                        _tmpRequiresCharging = _cursor.getInt(_tmp_32) != 0;
                    }
                    int _tmp_4 = _cursorIndexOfState;
                    int _cursorIndexOfRequiresDeviceIdle3 = _cursorIndexOfContentUriTriggers;
                    if (_tmp_4 == -1) {
                        _tmpRequiresDeviceIdle = false;
                    } else {
                        _tmpRequiresDeviceIdle = _cursor.getInt(_tmp_4) != 0;
                    }
                    int _tmp_5 = _cursorIndexOfRequiresDeviceIdle2;
                    if (_tmp_5 == -1) {
                        _tmpRequiresBatteryNotLow = false;
                    } else {
                        _tmpRequiresBatteryNotLow = _cursor.getInt(_tmp_5) != 0;
                    }
                    int _tmp_6 = _cursorIndexOfRequiresBatteryNotLow2;
                    if (_tmp_6 == -1) {
                        _tmpRequiresStorageNotLow = false;
                    } else {
                        int _tmp_7 = _cursor.getInt(_tmp_6);
                        _tmpRequiresStorageNotLow = _tmp_7 != 0;
                    }
                    int _cursorIndexOfContentTriggerUpdateDelayMillis3 = _cursorIndexOfContentTriggerUpdateDelayMillis2;
                    if (_cursorIndexOfContentTriggerUpdateDelayMillis3 == -1) {
                        _tmpContentTriggerUpdateDelayMillis = 0;
                    } else {
                        long _tmpContentTriggerUpdateDelayMillis2 = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis3);
                        _tmpContentTriggerUpdateDelayMillis = _tmpContentTriggerUpdateDelayMillis2;
                    }
                    _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis3;
                    int _cursorIndexOfContentTriggerUpdateDelayMillis4 = _cursorIndexOfContentTriggerMaxDelayMillis2;
                    if (_cursorIndexOfContentTriggerUpdateDelayMillis4 == -1) {
                        _tmpContentTriggerMaxDelayMillis = 0;
                    } else {
                        long _tmpContentTriggerMaxDelayMillis2 = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis4);
                        _tmpContentTriggerMaxDelayMillis = _tmpContentTriggerMaxDelayMillis2;
                    }
                    _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis4;
                    int _cursorIndexOfContentTriggerMaxDelayMillis3 = _cursorIndexOfContentUriTriggers4;
                    if (_cursorIndexOfContentTriggerMaxDelayMillis3 == -1) {
                        _tmpContentUriTriggers = null;
                    } else {
                        if (_cursor.isNull(_cursorIndexOfContentTriggerMaxDelayMillis3)) {
                            _tmp_8 = null;
                        } else {
                            _tmp_8 = _cursor.getBlob(_cursorIndexOfContentTriggerMaxDelayMillis3);
                        }
                        WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                        _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                    }
                    Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                    String _tmpKey_2 = _cursor.getString(_cursorIndexOfId);
                    ArrayList<String> _tmpTagsCollection_12 = _collectionTags3.get(_tmpKey_2);
                    if (_tmpTagsCollection_12 != null) {
                        _tmpTagsCollection_1 = _tmpTagsCollection_12;
                    } else {
                        _tmpTagsCollection_1 = new ArrayList<>();
                    }
                    String _tmpKey_3 = _cursor.getString(_cursorIndexOfId);
                    int _cursorIndexOfId2 = _cursorIndexOfId;
                    ArrayList<Data> _tmpProgressCollection_12 = _collectionProgress3.get(_tmpKey_3);
                    if (_tmpProgressCollection_12 != null) {
                        _tmpProgressCollection_1 = _tmpProgressCollection_12;
                    } else {
                        _tmpProgressCollection_1 = new ArrayList<>();
                    }
                    WorkSpec.WorkInfoPojo _item = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                    _result.add(_item);
                    _cursorIndexOfContentUriTriggers4 = _cursorIndexOfContentTriggerMaxDelayMillis3;
                    _cursorIndexOfContentUriTriggers = _cursorIndexOfRequiresDeviceIdle3;
                    _cursorIndexOfState = _tmp_4;
                    _cursorIndexOfRequiresDeviceIdle2 = _tmp_5;
                    _cursorIndexOfId = _cursorIndexOfId2;
                    _cursorIndexOfRequiresBatteryNotLow2 = _tmp_6;
                    _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfStopReason3;
                    _cursorIndexOfRequiredNetworkType2 = _tmp_32;
                }
                _cursor.close();
                return _result;
            } catch (Throwable th) {
                th = th;
                _cursor.close();
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    @Override // androidx.work.impl.model.RawWorkInfoDao
    public LiveData<List<WorkSpec.WorkInfoPojo>> getWorkInfoPojosLiveData(final SupportSQLiteQuery query) {
        return this.__db.getInvalidationTracker().createLiveData(new String[]{"WorkTag", "WorkProgress", "WorkSpec"}, false, new Callable<List<WorkSpec.WorkInfoPojo>>() { // from class: androidx.work.impl.model.RawWorkInfoDao_Impl.1
            @Override // java.util.concurrent.Callable
            public List<WorkSpec.WorkInfoPojo> call() throws Exception {
                String _tmpId;
                WorkInfo.State _tmpState;
                byte[] _tmp_1;
                Data _tmpOutput;
                long _tmpInitialDelay;
                long _tmpIntervalDuration;
                long _tmpFlexDuration;
                int _tmpRunAttemptCount;
                BackoffPolicy _tmpBackoffPolicy;
                long _tmpBackoffDelayDuration;
                long _tmpLastEnqueueTime;
                int _tmpPeriodCount;
                int _tmpGeneration;
                long _tmpNextScheduleTimeOverride;
                int _tmpStopReason;
                NetworkType _tmpRequiredNetworkType;
                boolean _tmpRequiresCharging;
                boolean _tmpRequiresDeviceIdle;
                boolean _tmpRequiresBatteryNotLow;
                boolean _tmpRequiresStorageNotLow;
                long _tmpContentTriggerUpdateDelayMillis;
                long _tmpContentTriggerMaxDelayMillis;
                byte[] _tmp_8;
                Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers;
                ArrayList<String> _tmpTagsCollection_1;
                ArrayList<Data> _tmpProgressCollection_1;
                int _cursorIndexOfGeneration;
                int _cursorIndexOfPeriodCount;
                Cursor _cursor = DBUtil.query(RawWorkInfoDao_Impl.this.__db, query, true, null);
                try {
                    int _cursorIndexOfId = CursorUtil.getColumnIndex(_cursor, "id");
                    int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndex(_cursor, "state");
                    int _cursorIndexOfOutput = CursorUtil.getColumnIndex(_cursor, "output");
                    int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndex(_cursor, "initial_delay");
                    int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndex(_cursor, "interval_duration");
                    int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndex(_cursor, "flex_duration");
                    int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndex(_cursor, "run_attempt_count");
                    int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndex(_cursor, "backoff_policy");
                    int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndex(_cursor, "backoff_delay_duration");
                    int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndex(_cursor, "last_enqueue_time");
                    int _cursorIndexOfPeriodCount2 = CursorUtil.getColumnIndex(_cursor, "period_count");
                    int _cursorIndexOfGeneration2 = CursorUtil.getColumnIndex(_cursor, "generation");
                    int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndex(_cursor, "next_schedule_time_override");
                    int _cursorIndexOfStopReason = CursorUtil.getColumnIndex(_cursor, "stop_reason");
                    int _cursorIndexOfStopReason2 = _cursorIndexOfStopReason;
                    int _cursorIndexOfRequiredNetworkType = CursorUtil.getColumnIndex(_cursor, "required_network_type");
                    int _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfRequiredNetworkType;
                    int _cursorIndexOfRequiresCharging = CursorUtil.getColumnIndex(_cursor, "requires_charging");
                    int _cursorIndexOfRequiredNetworkType2 = _cursorIndexOfRequiresCharging;
                    int _cursorIndexOfRequiresDeviceIdle = CursorUtil.getColumnIndex(_cursor, "requires_device_idle");
                    int _cursorIndexOfState = _cursorIndexOfRequiresDeviceIdle;
                    int _cursorIndexOfRequiresBatteryNotLow = CursorUtil.getColumnIndex(_cursor, "requires_battery_not_low");
                    int _cursorIndexOfRequiresDeviceIdle2 = _cursorIndexOfRequiresBatteryNotLow;
                    int _cursorIndexOfRequiresStorageNotLow2 = CursorUtil.getColumnIndex(_cursor, "requires_storage_not_low");
                    int _cursorIndexOfRequiresBatteryNotLow2 = _cursorIndexOfRequiresStorageNotLow2;
                    int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndex(_cursor, "trigger_content_update_delay");
                    int _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis;
                    int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndex(_cursor, "trigger_max_content_delay");
                    int _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerMaxDelayMillis;
                    int _cursorIndexOfContentUriTriggers2 = CursorUtil.getColumnIndex(_cursor, "content_uri_triggers");
                    HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                    HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                    while (_cursor.moveToNext()) {
                        String _tmpKey = _cursor.getString(_cursorIndexOfId);
                        int _cursorIndexOfContentUriTriggers3 = _cursorIndexOfContentUriTriggers2;
                        HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                        int _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfNextScheduleTimeOverride;
                        ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                        if (_tmpTagsCollection != null) {
                            _cursorIndexOfGeneration = _cursorIndexOfGeneration2;
                        } else {
                            ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                            _cursorIndexOfGeneration = _cursorIndexOfGeneration2;
                            _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                        }
                        String _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
                        HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                        ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                        if (_tmpProgressCollection != null) {
                            _cursorIndexOfPeriodCount = _cursorIndexOfPeriodCount2;
                        } else {
                            ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                            _cursorIndexOfPeriodCount = _cursorIndexOfPeriodCount2;
                            _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                        }
                        _collectionProgress = _collectionProgress2;
                        _cursorIndexOfNextScheduleTimeOverride = _cursorIndexOfNextScheduleTimeOverride2;
                        _cursorIndexOfGeneration2 = _cursorIndexOfGeneration;
                        _cursorIndexOfPeriodCount2 = _cursorIndexOfPeriodCount;
                        _collectionTags = _collectionTags2;
                        _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentUriTriggers3;
                    }
                    int _cursorIndexOfContentUriTriggers4 = _cursorIndexOfContentUriTriggers2;
                    int _cursorIndexOfPeriodCount3 = _cursorIndexOfPeriodCount2;
                    int _cursorIndexOfGeneration3 = _cursorIndexOfGeneration2;
                    HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                    int _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfNextScheduleTimeOverride;
                    HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                    _cursor.moveToPosition(-1);
                    RawWorkInfoDao_Impl.this.__fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                    RawWorkInfoDao_Impl.this.__fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                    List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                    while (_cursor.moveToNext()) {
                        if (_cursorIndexOfId == -1 || _cursor.isNull(_cursorIndexOfId)) {
                            _tmpId = null;
                        } else {
                            String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                            _tmpId = _tmpId2;
                        }
                        if (_cursorIndexOfContentUriTriggers == -1) {
                            _tmpState = null;
                        } else {
                            int _tmp = _cursor.getInt(_cursorIndexOfContentUriTriggers);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            _tmpState = WorkTypeConverters.intToState(_tmp);
                        }
                        if (_cursorIndexOfOutput == -1) {
                            _tmpOutput = null;
                        } else {
                            if (_cursor.isNull(_cursorIndexOfOutput)) {
                                _tmp_1 = null;
                            } else {
                                _tmp_1 = _cursor.getBlob(_cursorIndexOfOutput);
                            }
                            _tmpOutput = Data.fromByteArray(_tmp_1);
                        }
                        if (_cursorIndexOfInitialDelay == -1) {
                            _tmpInitialDelay = 0;
                        } else {
                            long _tmpInitialDelay2 = _cursor.getLong(_cursorIndexOfInitialDelay);
                            _tmpInitialDelay = _tmpInitialDelay2;
                        }
                        if (_cursorIndexOfIntervalDuration == -1) {
                            _tmpIntervalDuration = 0;
                        } else {
                            long _tmpIntervalDuration2 = _cursor.getLong(_cursorIndexOfIntervalDuration);
                            _tmpIntervalDuration = _tmpIntervalDuration2;
                        }
                        if (_cursorIndexOfFlexDuration == -1) {
                            _tmpFlexDuration = 0;
                        } else {
                            long _tmpFlexDuration2 = _cursor.getLong(_cursorIndexOfFlexDuration);
                            _tmpFlexDuration = _tmpFlexDuration2;
                        }
                        if (_cursorIndexOfRunAttemptCount == -1) {
                            _tmpRunAttemptCount = 0;
                        } else {
                            int _tmpRunAttemptCount2 = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                            _tmpRunAttemptCount = _tmpRunAttemptCount2;
                        }
                        if (_cursorIndexOfBackoffPolicy == -1) {
                            _tmpBackoffPolicy = null;
                        } else {
                            int _tmp_2 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                        }
                        if (_cursorIndexOfBackoffDelayDuration == -1) {
                            _tmpBackoffDelayDuration = 0;
                        } else {
                            long _tmpBackoffDelayDuration2 = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                            _tmpBackoffDelayDuration = _tmpBackoffDelayDuration2;
                        }
                        if (_cursorIndexOfLastEnqueueTime == -1) {
                            _tmpLastEnqueueTime = 0;
                        } else {
                            long _tmpLastEnqueueTime2 = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                            _tmpLastEnqueueTime = _tmpLastEnqueueTime2;
                        }
                        int _cursorIndexOfPeriodCount4 = _cursorIndexOfPeriodCount3;
                        if (_cursorIndexOfPeriodCount4 == -1) {
                            _tmpPeriodCount = 0;
                        } else {
                            int _tmpPeriodCount2 = _cursor.getInt(_cursorIndexOfPeriodCount4);
                            _tmpPeriodCount = _tmpPeriodCount2;
                        }
                        _cursorIndexOfPeriodCount3 = _cursorIndexOfPeriodCount4;
                        int _cursorIndexOfPeriodCount5 = _cursorIndexOfGeneration3;
                        if (_cursorIndexOfPeriodCount5 == -1) {
                            _tmpGeneration = 0;
                        } else {
                            int _tmpGeneration2 = _cursor.getInt(_cursorIndexOfPeriodCount5);
                            _tmpGeneration = _tmpGeneration2;
                        }
                        _cursorIndexOfGeneration3 = _cursorIndexOfPeriodCount5;
                        int _cursorIndexOfGeneration4 = _cursorIndexOfNextScheduleTimeOverride3;
                        if (_cursorIndexOfGeneration4 == -1) {
                            _tmpNextScheduleTimeOverride = 0;
                        } else {
                            long _tmpNextScheduleTimeOverride2 = _cursor.getLong(_cursorIndexOfGeneration4);
                            _tmpNextScheduleTimeOverride = _tmpNextScheduleTimeOverride2;
                        }
                        _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfGeneration4;
                        int _cursorIndexOfNextScheduleTimeOverride4 = _cursorIndexOfStopReason2;
                        if (_cursorIndexOfNextScheduleTimeOverride4 == -1) {
                            _tmpStopReason = 0;
                        } else {
                            int _tmpStopReason2 = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverride4);
                            _tmpStopReason = _tmpStopReason2;
                        }
                        _cursorIndexOfStopReason2 = _cursorIndexOfNextScheduleTimeOverride4;
                        int _cursorIndexOfStopReason3 = _cursorIndexOfRequiresStorageNotLow;
                        if (_cursorIndexOfStopReason3 == -1) {
                            _tmpRequiredNetworkType = null;
                        } else {
                            int _tmp_3 = _cursor.getInt(_cursorIndexOfStopReason3);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                        }
                        int _tmp_32 = _cursorIndexOfRequiredNetworkType2;
                        if (_tmp_32 == -1) {
                            _tmpRequiresCharging = false;
                        } else {
                            _tmpRequiresCharging = _cursor.getInt(_tmp_32) != 0;
                        }
                        int _tmp_4 = _cursorIndexOfState;
                        int _cursorIndexOfRequiresDeviceIdle3 = _cursorIndexOfContentUriTriggers;
                        if (_tmp_4 == -1) {
                            _tmpRequiresDeviceIdle = false;
                        } else {
                            _tmpRequiresDeviceIdle = _cursor.getInt(_tmp_4) != 0;
                        }
                        int _tmp_5 = _cursorIndexOfRequiresDeviceIdle2;
                        if (_tmp_5 == -1) {
                            _tmpRequiresBatteryNotLow = false;
                        } else {
                            _tmpRequiresBatteryNotLow = _cursor.getInt(_tmp_5) != 0;
                        }
                        int _tmp_6 = _cursorIndexOfRequiresBatteryNotLow2;
                        if (_tmp_6 == -1) {
                            _tmpRequiresStorageNotLow = false;
                        } else {
                            int _tmp_7 = _cursor.getInt(_tmp_6);
                            _tmpRequiresStorageNotLow = _tmp_7 != 0;
                        }
                        int _cursorIndexOfContentTriggerUpdateDelayMillis3 = _cursorIndexOfContentTriggerUpdateDelayMillis2;
                        if (_cursorIndexOfContentTriggerUpdateDelayMillis3 == -1) {
                            _tmpContentTriggerUpdateDelayMillis = 0;
                        } else {
                            long _tmpContentTriggerUpdateDelayMillis2 = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis3);
                            _tmpContentTriggerUpdateDelayMillis = _tmpContentTriggerUpdateDelayMillis2;
                        }
                        _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis3;
                        int _cursorIndexOfContentTriggerUpdateDelayMillis4 = _cursorIndexOfContentTriggerMaxDelayMillis2;
                        if (_cursorIndexOfContentTriggerUpdateDelayMillis4 == -1) {
                            _tmpContentTriggerMaxDelayMillis = 0;
                        } else {
                            long _tmpContentTriggerMaxDelayMillis2 = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis4);
                            _tmpContentTriggerMaxDelayMillis = _tmpContentTriggerMaxDelayMillis2;
                        }
                        _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis4;
                        int _cursorIndexOfContentTriggerMaxDelayMillis3 = _cursorIndexOfContentUriTriggers4;
                        if (_cursorIndexOfContentTriggerMaxDelayMillis3 == -1) {
                            _tmpContentUriTriggers = null;
                        } else {
                            if (_cursor.isNull(_cursorIndexOfContentTriggerMaxDelayMillis3)) {
                                _tmp_8 = null;
                            } else {
                                _tmp_8 = _cursor.getBlob(_cursorIndexOfContentTriggerMaxDelayMillis3);
                            }
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                        }
                        Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                        String _tmpKey_2 = _cursor.getString(_cursorIndexOfId);
                        ArrayList<String> _tmpTagsCollection_12 = _collectionTags3.get(_tmpKey_2);
                        if (_tmpTagsCollection_12 != null) {
                            _tmpTagsCollection_1 = _tmpTagsCollection_12;
                        } else {
                            _tmpTagsCollection_1 = new ArrayList<>();
                        }
                        String _tmpKey_3 = _cursor.getString(_cursorIndexOfId);
                        int _cursorIndexOfId2 = _cursorIndexOfId;
                        ArrayList<Data> _tmpProgressCollection_12 = _collectionProgress3.get(_tmpKey_3);
                        if (_tmpProgressCollection_12 != null) {
                            _tmpProgressCollection_1 = _tmpProgressCollection_12;
                        } else {
                            _tmpProgressCollection_1 = new ArrayList<>();
                        }
                        WorkSpec.WorkInfoPojo _item = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                        _result.add(_item);
                        _cursorIndexOfContentUriTriggers4 = _cursorIndexOfContentTriggerMaxDelayMillis3;
                        _cursorIndexOfContentUriTriggers = _cursorIndexOfRequiresDeviceIdle3;
                        _cursorIndexOfState = _tmp_4;
                        _cursorIndexOfRequiresDeviceIdle2 = _tmp_5;
                        _cursorIndexOfId = _cursorIndexOfId2;
                        _cursorIndexOfRequiresBatteryNotLow2 = _tmp_6;
                        _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfStopReason3;
                        _cursorIndexOfRequiredNetworkType2 = _tmp_32;
                    }
                    return _result;
                } finally {
                    _cursor.close();
                }
            }
        });
    }

    @Override // androidx.work.impl.model.RawWorkInfoDao
    public Flow<List<WorkSpec.WorkInfoPojo>> getWorkInfoPojosFlow(final SupportSQLiteQuery query) {
        return CoroutinesRoom.createFlow(this.__db, false, new String[]{"WorkTag", "WorkProgress", "WorkSpec"}, new Callable<List<WorkSpec.WorkInfoPojo>>() { // from class: androidx.work.impl.model.RawWorkInfoDao_Impl.2
            @Override // java.util.concurrent.Callable
            public List<WorkSpec.WorkInfoPojo> call() throws Exception {
                String _tmpId;
                WorkInfo.State _tmpState;
                byte[] _tmp_1;
                Data _tmpOutput;
                long _tmpInitialDelay;
                long _tmpIntervalDuration;
                long _tmpFlexDuration;
                int _tmpRunAttemptCount;
                BackoffPolicy _tmpBackoffPolicy;
                long _tmpBackoffDelayDuration;
                long _tmpLastEnqueueTime;
                int _tmpPeriodCount;
                int _tmpGeneration;
                long _tmpNextScheduleTimeOverride;
                int _tmpStopReason;
                NetworkType _tmpRequiredNetworkType;
                boolean _tmpRequiresCharging;
                boolean _tmpRequiresDeviceIdle;
                boolean _tmpRequiresBatteryNotLow;
                boolean _tmpRequiresStorageNotLow;
                long _tmpContentTriggerUpdateDelayMillis;
                long _tmpContentTriggerMaxDelayMillis;
                byte[] _tmp_8;
                Set<Constraints.ContentUriTrigger> _tmpContentUriTriggers;
                ArrayList<String> _tmpTagsCollection_1;
                ArrayList<Data> _tmpProgressCollection_1;
                int _cursorIndexOfGeneration;
                int _cursorIndexOfPeriodCount;
                Cursor _cursor = DBUtil.query(RawWorkInfoDao_Impl.this.__db, query, true, null);
                try {
                    int _cursorIndexOfId = CursorUtil.getColumnIndex(_cursor, "id");
                    int _cursorIndexOfContentUriTriggers = CursorUtil.getColumnIndex(_cursor, "state");
                    int _cursorIndexOfOutput = CursorUtil.getColumnIndex(_cursor, "output");
                    int _cursorIndexOfInitialDelay = CursorUtil.getColumnIndex(_cursor, "initial_delay");
                    int _cursorIndexOfIntervalDuration = CursorUtil.getColumnIndex(_cursor, "interval_duration");
                    int _cursorIndexOfFlexDuration = CursorUtil.getColumnIndex(_cursor, "flex_duration");
                    int _cursorIndexOfRunAttemptCount = CursorUtil.getColumnIndex(_cursor, "run_attempt_count");
                    int _cursorIndexOfBackoffPolicy = CursorUtil.getColumnIndex(_cursor, "backoff_policy");
                    int _cursorIndexOfBackoffDelayDuration = CursorUtil.getColumnIndex(_cursor, "backoff_delay_duration");
                    int _cursorIndexOfLastEnqueueTime = CursorUtil.getColumnIndex(_cursor, "last_enqueue_time");
                    int _cursorIndexOfPeriodCount2 = CursorUtil.getColumnIndex(_cursor, "period_count");
                    int _cursorIndexOfGeneration2 = CursorUtil.getColumnIndex(_cursor, "generation");
                    int _cursorIndexOfNextScheduleTimeOverride = CursorUtil.getColumnIndex(_cursor, "next_schedule_time_override");
                    int _cursorIndexOfStopReason = CursorUtil.getColumnIndex(_cursor, "stop_reason");
                    int _cursorIndexOfStopReason2 = _cursorIndexOfStopReason;
                    int _cursorIndexOfRequiredNetworkType = CursorUtil.getColumnIndex(_cursor, "required_network_type");
                    int _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfRequiredNetworkType;
                    int _cursorIndexOfRequiresCharging = CursorUtil.getColumnIndex(_cursor, "requires_charging");
                    int _cursorIndexOfRequiredNetworkType2 = _cursorIndexOfRequiresCharging;
                    int _cursorIndexOfRequiresDeviceIdle = CursorUtil.getColumnIndex(_cursor, "requires_device_idle");
                    int _cursorIndexOfState = _cursorIndexOfRequiresDeviceIdle;
                    int _cursorIndexOfRequiresBatteryNotLow = CursorUtil.getColumnIndex(_cursor, "requires_battery_not_low");
                    int _cursorIndexOfRequiresDeviceIdle2 = _cursorIndexOfRequiresBatteryNotLow;
                    int _cursorIndexOfRequiresStorageNotLow2 = CursorUtil.getColumnIndex(_cursor, "requires_storage_not_low");
                    int _cursorIndexOfRequiresBatteryNotLow2 = _cursorIndexOfRequiresStorageNotLow2;
                    int _cursorIndexOfContentTriggerUpdateDelayMillis = CursorUtil.getColumnIndex(_cursor, "trigger_content_update_delay");
                    int _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis;
                    int _cursorIndexOfContentTriggerMaxDelayMillis = CursorUtil.getColumnIndex(_cursor, "trigger_max_content_delay");
                    int _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerMaxDelayMillis;
                    int _cursorIndexOfContentUriTriggers2 = CursorUtil.getColumnIndex(_cursor, "content_uri_triggers");
                    HashMap<String, ArrayList<String>> _collectionTags = new HashMap<>();
                    HashMap<String, ArrayList<Data>> _collectionProgress = new HashMap<>();
                    while (_cursor.moveToNext()) {
                        String _tmpKey = _cursor.getString(_cursorIndexOfId);
                        int _cursorIndexOfContentUriTriggers3 = _cursorIndexOfContentUriTriggers2;
                        HashMap<String, ArrayList<String>> _collectionTags2 = _collectionTags;
                        int _cursorIndexOfNextScheduleTimeOverride2 = _cursorIndexOfNextScheduleTimeOverride;
                        ArrayList<String> _tmpTagsCollection = _collectionTags2.get(_tmpKey);
                        if (_tmpTagsCollection != null) {
                            _cursorIndexOfGeneration = _cursorIndexOfGeneration2;
                        } else {
                            ArrayList<String> _tmpTagsCollection2 = new ArrayList<>();
                            _cursorIndexOfGeneration = _cursorIndexOfGeneration2;
                            _collectionTags2.put(_tmpKey, _tmpTagsCollection2);
                        }
                        String _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
                        HashMap<String, ArrayList<Data>> _collectionProgress2 = _collectionProgress;
                        ArrayList<Data> _tmpProgressCollection = _collectionProgress2.get(_tmpKey_1);
                        if (_tmpProgressCollection != null) {
                            _cursorIndexOfPeriodCount = _cursorIndexOfPeriodCount2;
                        } else {
                            ArrayList<Data> _tmpProgressCollection2 = new ArrayList<>();
                            _cursorIndexOfPeriodCount = _cursorIndexOfPeriodCount2;
                            _collectionProgress2.put(_tmpKey_1, _tmpProgressCollection2);
                        }
                        _collectionProgress = _collectionProgress2;
                        _cursorIndexOfNextScheduleTimeOverride = _cursorIndexOfNextScheduleTimeOverride2;
                        _cursorIndexOfGeneration2 = _cursorIndexOfGeneration;
                        _cursorIndexOfPeriodCount2 = _cursorIndexOfPeriodCount;
                        _collectionTags = _collectionTags2;
                        _cursorIndexOfContentUriTriggers2 = _cursorIndexOfContentUriTriggers3;
                    }
                    int _cursorIndexOfContentUriTriggers4 = _cursorIndexOfContentUriTriggers2;
                    int _cursorIndexOfPeriodCount3 = _cursorIndexOfPeriodCount2;
                    int _cursorIndexOfGeneration3 = _cursorIndexOfGeneration2;
                    HashMap<String, ArrayList<String>> _collectionTags3 = _collectionTags;
                    int _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfNextScheduleTimeOverride;
                    HashMap<String, ArrayList<Data>> _collectionProgress3 = _collectionProgress;
                    _cursor.moveToPosition(-1);
                    RawWorkInfoDao_Impl.this.__fetchRelationshipWorkTagAsjavaLangString(_collectionTags3);
                    RawWorkInfoDao_Impl.this.__fetchRelationshipWorkProgressAsandroidxWorkData(_collectionProgress3);
                    List<WorkSpec.WorkInfoPojo> _result = new ArrayList<>(_cursor.getCount());
                    while (_cursor.moveToNext()) {
                        if (_cursorIndexOfId == -1 || _cursor.isNull(_cursorIndexOfId)) {
                            _tmpId = null;
                        } else {
                            String _tmpId2 = _cursor.getString(_cursorIndexOfId);
                            _tmpId = _tmpId2;
                        }
                        if (_cursorIndexOfContentUriTriggers == -1) {
                            _tmpState = null;
                        } else {
                            int _tmp = _cursor.getInt(_cursorIndexOfContentUriTriggers);
                            WorkTypeConverters workTypeConverters = WorkTypeConverters.INSTANCE;
                            _tmpState = WorkTypeConverters.intToState(_tmp);
                        }
                        if (_cursorIndexOfOutput == -1) {
                            _tmpOutput = null;
                        } else {
                            if (_cursor.isNull(_cursorIndexOfOutput)) {
                                _tmp_1 = null;
                            } else {
                                _tmp_1 = _cursor.getBlob(_cursorIndexOfOutput);
                            }
                            _tmpOutput = Data.fromByteArray(_tmp_1);
                        }
                        if (_cursorIndexOfInitialDelay == -1) {
                            _tmpInitialDelay = 0;
                        } else {
                            long _tmpInitialDelay2 = _cursor.getLong(_cursorIndexOfInitialDelay);
                            _tmpInitialDelay = _tmpInitialDelay2;
                        }
                        if (_cursorIndexOfIntervalDuration == -1) {
                            _tmpIntervalDuration = 0;
                        } else {
                            long _tmpIntervalDuration2 = _cursor.getLong(_cursorIndexOfIntervalDuration);
                            _tmpIntervalDuration = _tmpIntervalDuration2;
                        }
                        if (_cursorIndexOfFlexDuration == -1) {
                            _tmpFlexDuration = 0;
                        } else {
                            long _tmpFlexDuration2 = _cursor.getLong(_cursorIndexOfFlexDuration);
                            _tmpFlexDuration = _tmpFlexDuration2;
                        }
                        if (_cursorIndexOfRunAttemptCount == -1) {
                            _tmpRunAttemptCount = 0;
                        } else {
                            int _tmpRunAttemptCount2 = _cursor.getInt(_cursorIndexOfRunAttemptCount);
                            _tmpRunAttemptCount = _tmpRunAttemptCount2;
                        }
                        if (_cursorIndexOfBackoffPolicy == -1) {
                            _tmpBackoffPolicy = null;
                        } else {
                            int _tmp_2 = _cursor.getInt(_cursorIndexOfBackoffPolicy);
                            WorkTypeConverters workTypeConverters2 = WorkTypeConverters.INSTANCE;
                            _tmpBackoffPolicy = WorkTypeConverters.intToBackoffPolicy(_tmp_2);
                        }
                        if (_cursorIndexOfBackoffDelayDuration == -1) {
                            _tmpBackoffDelayDuration = 0;
                        } else {
                            long _tmpBackoffDelayDuration2 = _cursor.getLong(_cursorIndexOfBackoffDelayDuration);
                            _tmpBackoffDelayDuration = _tmpBackoffDelayDuration2;
                        }
                        if (_cursorIndexOfLastEnqueueTime == -1) {
                            _tmpLastEnqueueTime = 0;
                        } else {
                            long _tmpLastEnqueueTime2 = _cursor.getLong(_cursorIndexOfLastEnqueueTime);
                            _tmpLastEnqueueTime = _tmpLastEnqueueTime2;
                        }
                        int _cursorIndexOfPeriodCount4 = _cursorIndexOfPeriodCount3;
                        if (_cursorIndexOfPeriodCount4 == -1) {
                            _tmpPeriodCount = 0;
                        } else {
                            int _tmpPeriodCount2 = _cursor.getInt(_cursorIndexOfPeriodCount4);
                            _tmpPeriodCount = _tmpPeriodCount2;
                        }
                        _cursorIndexOfPeriodCount3 = _cursorIndexOfPeriodCount4;
                        int _cursorIndexOfPeriodCount5 = _cursorIndexOfGeneration3;
                        if (_cursorIndexOfPeriodCount5 == -1) {
                            _tmpGeneration = 0;
                        } else {
                            int _tmpGeneration2 = _cursor.getInt(_cursorIndexOfPeriodCount5);
                            _tmpGeneration = _tmpGeneration2;
                        }
                        _cursorIndexOfGeneration3 = _cursorIndexOfPeriodCount5;
                        int _cursorIndexOfGeneration4 = _cursorIndexOfNextScheduleTimeOverride3;
                        if (_cursorIndexOfGeneration4 == -1) {
                            _tmpNextScheduleTimeOverride = 0;
                        } else {
                            long _tmpNextScheduleTimeOverride2 = _cursor.getLong(_cursorIndexOfGeneration4);
                            _tmpNextScheduleTimeOverride = _tmpNextScheduleTimeOverride2;
                        }
                        _cursorIndexOfNextScheduleTimeOverride3 = _cursorIndexOfGeneration4;
                        int _cursorIndexOfNextScheduleTimeOverride4 = _cursorIndexOfStopReason2;
                        if (_cursorIndexOfNextScheduleTimeOverride4 == -1) {
                            _tmpStopReason = 0;
                        } else {
                            int _tmpStopReason2 = _cursor.getInt(_cursorIndexOfNextScheduleTimeOverride4);
                            _tmpStopReason = _tmpStopReason2;
                        }
                        _cursorIndexOfStopReason2 = _cursorIndexOfNextScheduleTimeOverride4;
                        int _cursorIndexOfStopReason3 = _cursorIndexOfRequiresStorageNotLow;
                        if (_cursorIndexOfStopReason3 == -1) {
                            _tmpRequiredNetworkType = null;
                        } else {
                            int _tmp_3 = _cursor.getInt(_cursorIndexOfStopReason3);
                            WorkTypeConverters workTypeConverters3 = WorkTypeConverters.INSTANCE;
                            _tmpRequiredNetworkType = WorkTypeConverters.intToNetworkType(_tmp_3);
                        }
                        int _tmp_32 = _cursorIndexOfRequiredNetworkType2;
                        if (_tmp_32 == -1) {
                            _tmpRequiresCharging = false;
                        } else {
                            _tmpRequiresCharging = _cursor.getInt(_tmp_32) != 0;
                        }
                        int _tmp_4 = _cursorIndexOfState;
                        int _cursorIndexOfRequiresDeviceIdle3 = _cursorIndexOfContentUriTriggers;
                        if (_tmp_4 == -1) {
                            _tmpRequiresDeviceIdle = false;
                        } else {
                            _tmpRequiresDeviceIdle = _cursor.getInt(_tmp_4) != 0;
                        }
                        int _tmp_5 = _cursorIndexOfRequiresDeviceIdle2;
                        if (_tmp_5 == -1) {
                            _tmpRequiresBatteryNotLow = false;
                        } else {
                            _tmpRequiresBatteryNotLow = _cursor.getInt(_tmp_5) != 0;
                        }
                        int _tmp_6 = _cursorIndexOfRequiresBatteryNotLow2;
                        if (_tmp_6 == -1) {
                            _tmpRequiresStorageNotLow = false;
                        } else {
                            int _tmp_7 = _cursor.getInt(_tmp_6);
                            _tmpRequiresStorageNotLow = _tmp_7 != 0;
                        }
                        int _cursorIndexOfContentTriggerUpdateDelayMillis3 = _cursorIndexOfContentTriggerUpdateDelayMillis2;
                        if (_cursorIndexOfContentTriggerUpdateDelayMillis3 == -1) {
                            _tmpContentTriggerUpdateDelayMillis = 0;
                        } else {
                            long _tmpContentTriggerUpdateDelayMillis2 = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis3);
                            _tmpContentTriggerUpdateDelayMillis = _tmpContentTriggerUpdateDelayMillis2;
                        }
                        _cursorIndexOfContentTriggerUpdateDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis3;
                        int _cursorIndexOfContentTriggerUpdateDelayMillis4 = _cursorIndexOfContentTriggerMaxDelayMillis2;
                        if (_cursorIndexOfContentTriggerUpdateDelayMillis4 == -1) {
                            _tmpContentTriggerMaxDelayMillis = 0;
                        } else {
                            long _tmpContentTriggerMaxDelayMillis2 = _cursor.getLong(_cursorIndexOfContentTriggerUpdateDelayMillis4);
                            _tmpContentTriggerMaxDelayMillis = _tmpContentTriggerMaxDelayMillis2;
                        }
                        _cursorIndexOfContentTriggerMaxDelayMillis2 = _cursorIndexOfContentTriggerUpdateDelayMillis4;
                        int _cursorIndexOfContentTriggerMaxDelayMillis3 = _cursorIndexOfContentUriTriggers4;
                        if (_cursorIndexOfContentTriggerMaxDelayMillis3 == -1) {
                            _tmpContentUriTriggers = null;
                        } else {
                            if (_cursor.isNull(_cursorIndexOfContentTriggerMaxDelayMillis3)) {
                                _tmp_8 = null;
                            } else {
                                _tmp_8 = _cursor.getBlob(_cursorIndexOfContentTriggerMaxDelayMillis3);
                            }
                            WorkTypeConverters workTypeConverters4 = WorkTypeConverters.INSTANCE;
                            _tmpContentUriTriggers = WorkTypeConverters.byteArrayToSetOfTriggers(_tmp_8);
                        }
                        Constraints _tmpConstraints = new Constraints(_tmpRequiredNetworkType, _tmpRequiresCharging, _tmpRequiresDeviceIdle, _tmpRequiresBatteryNotLow, _tmpRequiresStorageNotLow, _tmpContentTriggerUpdateDelayMillis, _tmpContentTriggerMaxDelayMillis, _tmpContentUriTriggers);
                        String _tmpKey_2 = _cursor.getString(_cursorIndexOfId);
                        ArrayList<String> _tmpTagsCollection_12 = _collectionTags3.get(_tmpKey_2);
                        if (_tmpTagsCollection_12 != null) {
                            _tmpTagsCollection_1 = _tmpTagsCollection_12;
                        } else {
                            _tmpTagsCollection_1 = new ArrayList<>();
                        }
                        String _tmpKey_3 = _cursor.getString(_cursorIndexOfId);
                        int _cursorIndexOfId2 = _cursorIndexOfId;
                        ArrayList<Data> _tmpProgressCollection_12 = _collectionProgress3.get(_tmpKey_3);
                        if (_tmpProgressCollection_12 != null) {
                            _tmpProgressCollection_1 = _tmpProgressCollection_12;
                        } else {
                            _tmpProgressCollection_1 = new ArrayList<>();
                        }
                        WorkSpec.WorkInfoPojo _item = new WorkSpec.WorkInfoPojo(_tmpId, _tmpState, _tmpOutput, _tmpInitialDelay, _tmpIntervalDuration, _tmpFlexDuration, _tmpConstraints, _tmpRunAttemptCount, _tmpBackoffPolicy, _tmpBackoffDelayDuration, _tmpLastEnqueueTime, _tmpPeriodCount, _tmpGeneration, _tmpNextScheduleTimeOverride, _tmpStopReason, _tmpTagsCollection_1, _tmpProgressCollection_1);
                        _result.add(_item);
                        _cursorIndexOfContentUriTriggers4 = _cursorIndexOfContentTriggerMaxDelayMillis3;
                        _cursorIndexOfContentUriTriggers = _cursorIndexOfRequiresDeviceIdle3;
                        _cursorIndexOfState = _tmp_4;
                        _cursorIndexOfRequiresDeviceIdle2 = _tmp_5;
                        _cursorIndexOfId = _cursorIndexOfId2;
                        _cursorIndexOfRequiresBatteryNotLow2 = _tmp_6;
                        _cursorIndexOfRequiresStorageNotLow = _cursorIndexOfStopReason3;
                        _cursorIndexOfRequiredNetworkType2 = _tmp_32;
                    }
                    return _result;
                } finally {
                    _cursor.close();
                }
            }
        });
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
