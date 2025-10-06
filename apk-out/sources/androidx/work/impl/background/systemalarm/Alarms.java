package androidx.work.impl.background.systemalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.work.Logger;
import androidx.work.impl.WorkDatabase;
import androidx.work.impl.model.SystemIdInfo;
import androidx.work.impl.model.SystemIdInfoDao;
import androidx.work.impl.model.SystemIdInfoKt;
import androidx.work.impl.model.WorkGenerationalId;
import androidx.work.impl.utils.IdGenerator;

/* loaded from: classes.dex */
class Alarms {
    private static final String TAG = Logger.tagWithPrefix("Alarms");

    public static void setAlarm(Context context, WorkDatabase workDatabase, WorkGenerationalId id, long triggerAtMillis) {
        SystemIdInfoDao systemIdInfoDao = workDatabase.systemIdInfoDao();
        SystemIdInfo systemIdInfo = systemIdInfoDao.getSystemIdInfo(id);
        if (systemIdInfo != null) {
            cancelExactAlarm(context, id, systemIdInfo.systemId);
            setExactAlarm(context, id, systemIdInfo.systemId, triggerAtMillis);
            return;
        }
        IdGenerator idGenerator = new IdGenerator(workDatabase);
        int alarmId = idGenerator.nextAlarmManagerId();
        SystemIdInfo newSystemIdInfo = SystemIdInfoKt.systemIdInfo(id, alarmId);
        systemIdInfoDao.insertSystemIdInfo(newSystemIdInfo);
        setExactAlarm(context, id, alarmId, triggerAtMillis);
    }

    public static void cancelAlarm(Context context, WorkDatabase workDatabase, WorkGenerationalId id) {
        SystemIdInfoDao systemIdInfoDao = workDatabase.systemIdInfoDao();
        SystemIdInfo systemIdInfo = systemIdInfoDao.getSystemIdInfo(id);
        if (systemIdInfo != null) {
            cancelExactAlarm(context, id, systemIdInfo.systemId);
            Logger.get().debug(TAG, "Removing SystemIdInfo for workSpecId (" + id + ")");
            systemIdInfoDao.removeSystemIdInfo(id);
        }
    }

    private static void cancelExactAlarm(Context context, WorkGenerationalId id, int alarmId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(NotificationCompat.CATEGORY_ALARM);
        Intent delayMet = CommandHandler.createDelayMetIntent(context, id);
        int flags = 536870912 | AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL;
        PendingIntent pendingIntent = PendingIntent.getService(context, alarmId, delayMet, flags);
        if (pendingIntent != null && alarmManager != null) {
            Logger.get().debug(TAG, "Cancelling existing alarm with (workSpecId, systemId) (" + id + ", " + alarmId + ")");
            alarmManager.cancel(pendingIntent);
        }
    }

    private static void setExactAlarm(Context context, WorkGenerationalId id, int alarmId, long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(NotificationCompat.CATEGORY_ALARM);
        int flags = 134217728 | AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL;
        Intent delayMet = CommandHandler.createDelayMetIntent(context, id);
        PendingIntent pendingIntent = PendingIntent.getService(context, alarmId, delayMet, flags);
        if (alarmManager != null) {
            Api19Impl.setExact(alarmManager, 0, triggerAtMillis, pendingIntent);
        }
    }

    private Alarms() {
    }

    static class Api19Impl {
        private Api19Impl() {
        }

        static void setExact(AlarmManager alarmManager, int type, long triggerAtMillis, PendingIntent operation) {
            alarmManager.setExact(type, triggerAtMillis, operation);
        }
    }
}
