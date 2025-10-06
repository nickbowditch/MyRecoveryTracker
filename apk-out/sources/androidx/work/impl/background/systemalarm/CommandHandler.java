package androidx.work.impl.background.systemalarm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.work.Clock;
import androidx.work.Logger;
import androidx.work.impl.ExecutionListener;
import androidx.work.impl.StartStopToken;
import androidx.work.impl.StartStopTokens;
import androidx.work.impl.WorkDatabase;
import androidx.work.impl.WorkManagerImpl;
import androidx.work.impl.background.systemalarm.SystemAlarmDispatcher;
import androidx.work.impl.model.WorkGenerationalId;
import androidx.work.impl.model.WorkSpec;
import androidx.work.impl.model.WorkSpecDao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class CommandHandler implements ExecutionListener {
    static final String ACTION_CONSTRAINTS_CHANGED = "ACTION_CONSTRAINTS_CHANGED";
    static final String ACTION_DELAY_MET = "ACTION_DELAY_MET";
    static final String ACTION_EXECUTION_COMPLETED = "ACTION_EXECUTION_COMPLETED";
    static final String ACTION_RESCHEDULE = "ACTION_RESCHEDULE";
    static final String ACTION_SCHEDULE_WORK = "ACTION_SCHEDULE_WORK";
    static final String ACTION_STOP_WORK = "ACTION_STOP_WORK";
    private static final String KEY_NEEDS_RESCHEDULE = "KEY_NEEDS_RESCHEDULE";
    private static final String KEY_WORKSPEC_GENERATION = "KEY_WORKSPEC_GENERATION";
    private static final String KEY_WORKSPEC_ID = "KEY_WORKSPEC_ID";
    private static final String TAG = Logger.tagWithPrefix("CommandHandler");
    static final long WORK_PROCESSING_TIME_IN_MS = 600000;
    private final Clock mClock;
    private final Context mContext;
    private final StartStopTokens mStartStopTokens;
    private final Map<WorkGenerationalId, DelayMetCommandHandler> mPendingDelayMet = new HashMap();
    private final Object mLock = new Object();

    static Intent createScheduleWorkIntent(Context context, WorkGenerationalId id) {
        Intent intent = new Intent(context, (Class<?>) SystemAlarmService.class);
        intent.setAction(ACTION_SCHEDULE_WORK);
        return writeWorkGenerationalId(intent, id);
    }

    private static Intent writeWorkGenerationalId(Intent intent, WorkGenerationalId id) {
        intent.putExtra(KEY_WORKSPEC_ID, id.getWorkSpecId());
        intent.putExtra(KEY_WORKSPEC_GENERATION, id.getGeneration());
        return intent;
    }

    static WorkGenerationalId readWorkGenerationalId(Intent intent) {
        return new WorkGenerationalId(intent.getStringExtra(KEY_WORKSPEC_ID), intent.getIntExtra(KEY_WORKSPEC_GENERATION, 0));
    }

    static Intent createDelayMetIntent(Context context, WorkGenerationalId id) {
        Intent intent = new Intent(context, (Class<?>) SystemAlarmService.class);
        intent.setAction(ACTION_DELAY_MET);
        return writeWorkGenerationalId(intent, id);
    }

    static Intent createStopWorkIntent(Context context, String workSpecId) {
        Intent intent = new Intent(context, (Class<?>) SystemAlarmService.class);
        intent.setAction(ACTION_STOP_WORK);
        intent.putExtra(KEY_WORKSPEC_ID, workSpecId);
        return intent;
    }

    static Intent createStopWorkIntent(Context context, WorkGenerationalId id) {
        Intent intent = new Intent(context, (Class<?>) SystemAlarmService.class);
        intent.setAction(ACTION_STOP_WORK);
        return writeWorkGenerationalId(intent, id);
    }

    static Intent createConstraintsChangedIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) SystemAlarmService.class);
        intent.setAction(ACTION_CONSTRAINTS_CHANGED);
        return intent;
    }

    static Intent createRescheduleIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) SystemAlarmService.class);
        intent.setAction(ACTION_RESCHEDULE);
        return intent;
    }

    static Intent createExecutionCompletedIntent(Context context, WorkGenerationalId id, boolean needsReschedule) {
        Intent intent = new Intent(context, (Class<?>) SystemAlarmService.class);
        intent.setAction(ACTION_EXECUTION_COMPLETED);
        intent.putExtra(KEY_NEEDS_RESCHEDULE, needsReschedule);
        return writeWorkGenerationalId(intent, id);
    }

    CommandHandler(Context context, Clock clock, StartStopTokens startStopTokens) {
        this.mContext = context;
        this.mClock = clock;
        this.mStartStopTokens = startStopTokens;
    }

    @Override // androidx.work.impl.ExecutionListener
    public void onExecuted(WorkGenerationalId id, boolean needsReschedule) {
        synchronized (this.mLock) {
            DelayMetCommandHandler listener = this.mPendingDelayMet.remove(id);
            this.mStartStopTokens.remove(id);
            if (listener != null) {
                listener.onExecuted(needsReschedule);
            }
        }
    }

    boolean hasPendingCommands() {
        boolean z;
        synchronized (this.mLock) {
            z = !this.mPendingDelayMet.isEmpty();
        }
        return z;
    }

    void onHandleIntent(Intent intent, int startId, SystemAlarmDispatcher dispatcher) {
        String action = intent.getAction();
        if (ACTION_CONSTRAINTS_CHANGED.equals(action)) {
            handleConstraintsChanged(intent, startId, dispatcher);
            return;
        }
        if (ACTION_RESCHEDULE.equals(action)) {
            handleReschedule(intent, startId, dispatcher);
            return;
        }
        Bundle extras = intent.getExtras();
        if (!hasKeys(extras, KEY_WORKSPEC_ID)) {
            Logger.get().error(TAG, "Invalid request for " + action + " , requires " + KEY_WORKSPEC_ID + " .");
            return;
        }
        if (ACTION_SCHEDULE_WORK.equals(action)) {
            handleScheduleWorkIntent(intent, startId, dispatcher);
            return;
        }
        if (ACTION_DELAY_MET.equals(action)) {
            handleDelayMet(intent, startId, dispatcher);
            return;
        }
        if (ACTION_STOP_WORK.equals(action)) {
            handleStopWork(intent, dispatcher);
        } else if (ACTION_EXECUTION_COMPLETED.equals(action)) {
            handleExecutionCompleted(intent, startId);
        } else {
            Logger.get().warning(TAG, "Ignoring intent " + intent);
        }
    }

    private void handleScheduleWorkIntent(Intent intent, int startId, SystemAlarmDispatcher dispatcher) {
        WorkGenerationalId id = readWorkGenerationalId(intent);
        Logger.get().debug(TAG, "Handling schedule work for " + id);
        WorkManagerImpl workManager = dispatcher.getWorkManager();
        WorkDatabase workDatabase = workManager.getWorkDatabase();
        workDatabase.beginTransaction();
        try {
            WorkSpecDao workSpecDao = workDatabase.workSpecDao();
            WorkSpec workSpec = workSpecDao.getWorkSpec(id.getWorkSpecId());
            if (workSpec == null) {
                Logger.get().warning(TAG, "Skipping scheduling " + id + " because it's no longer in the DB");
                return;
            }
            if (workSpec.state.isFinished()) {
                Logger.get().warning(TAG, "Skipping scheduling " + id + "because it is finished.");
                return;
            }
            long triggerAt = workSpec.calculateNextRunTime();
            if (workSpec.hasConstraints()) {
                Logger.get().debug(TAG, "Opportunistically setting an alarm for " + id + "at " + triggerAt);
                Alarms.setAlarm(this.mContext, workDatabase, id, triggerAt);
                Intent constraintsUpdate = createConstraintsChangedIntent(this.mContext);
                dispatcher.getTaskExecutor().getMainThreadExecutor().execute(new SystemAlarmDispatcher.AddRunnable(dispatcher, constraintsUpdate, startId));
            } else {
                Logger.get().debug(TAG, "Setting up Alarms for " + id + "at " + triggerAt);
                Alarms.setAlarm(this.mContext, workDatabase, id, triggerAt);
            }
            workDatabase.setTransactionSuccessful();
        } finally {
            workDatabase.endTransaction();
        }
    }

    private void handleDelayMet(Intent intent, int startId, SystemAlarmDispatcher dispatcher) {
        synchronized (this.mLock) {
            WorkGenerationalId id = readWorkGenerationalId(intent);
            Logger.get().debug(TAG, "Handing delay met for " + id);
            if (!this.mPendingDelayMet.containsKey(id)) {
                DelayMetCommandHandler delayMetCommandHandler = new DelayMetCommandHandler(this.mContext, startId, dispatcher, this.mStartStopTokens.tokenFor(id));
                this.mPendingDelayMet.put(id, delayMetCommandHandler);
                delayMetCommandHandler.handleProcessWork();
            } else {
                Logger.get().debug(TAG, "WorkSpec " + id + " is is already being handled for ACTION_DELAY_MET");
            }
        }
    }

    private void handleStopWork(Intent intent, SystemAlarmDispatcher dispatcher) {
        List<StartStopToken> tokens;
        Bundle extras = intent.getExtras();
        String workSpecId = extras.getString(KEY_WORKSPEC_ID);
        if (extras.containsKey(KEY_WORKSPEC_GENERATION)) {
            int generation = extras.getInt(KEY_WORKSPEC_GENERATION);
            tokens = new ArrayList<>(1);
            StartStopToken id = this.mStartStopTokens.remove(new WorkGenerationalId(workSpecId, generation));
            if (id != null) {
                tokens.add(id);
            }
        } else {
            tokens = this.mStartStopTokens.remove(workSpecId);
        }
        for (StartStopToken token : tokens) {
            Logger.get().debug(TAG, "Handing stopWork work for " + workSpecId);
            dispatcher.getWorkerLauncher().stopWork(token);
            Alarms.cancelAlarm(this.mContext, dispatcher.getWorkManager().getWorkDatabase(), token.getId());
            dispatcher.onExecuted(token.getId(), false);
        }
    }

    private void handleConstraintsChanged(Intent intent, int startId, SystemAlarmDispatcher dispatcher) {
        Logger.get().debug(TAG, "Handling constraints changed " + intent);
        ConstraintsCommandHandler changedCommandHandler = new ConstraintsCommandHandler(this.mContext, this.mClock, startId, dispatcher);
        changedCommandHandler.handleConstraintsChanged();
    }

    private void handleReschedule(Intent intent, int startId, SystemAlarmDispatcher dispatcher) {
        Logger.get().debug(TAG, "Handling reschedule " + intent + ", " + startId);
        dispatcher.getWorkManager().rescheduleEligibleWork();
    }

    private void handleExecutionCompleted(Intent intent, int startId) {
        WorkGenerationalId id = readWorkGenerationalId(intent);
        boolean needsReschedule = intent.getExtras().getBoolean(KEY_NEEDS_RESCHEDULE);
        Logger.get().debug(TAG, "Handling onExecutionCompleted " + intent + ", " + startId);
        onExecuted(id, needsReschedule);
    }

    private static boolean hasKeys(Bundle bundle, String... keys) {
        if (bundle == null || bundle.isEmpty()) {
            return false;
        }
        for (String key : keys) {
            if (bundle.get(key) == null) {
                return false;
            }
        }
        return true;
    }
}
