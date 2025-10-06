package androidx.appcompat.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.DataSetObservable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlSerializer;

/* loaded from: classes.dex */
class ActivityChooserModel extends DataSetObservable {
    static final String ATTRIBUTE_ACTIVITY = "activity";
    static final String ATTRIBUTE_TIME = "time";
    static final String ATTRIBUTE_WEIGHT = "weight";
    static final boolean DEBUG = false;
    private static final int DEFAULT_ACTIVITY_INFLATION = 5;
    private static final float DEFAULT_HISTORICAL_RECORD_WEIGHT = 1.0f;
    public static final String DEFAULT_HISTORY_FILE_NAME = "activity_choser_model_history.xml";
    public static final int DEFAULT_HISTORY_MAX_LENGTH = 50;
    private static final String HISTORY_FILE_EXTENSION = ".xml";
    private static final int INVALID_INDEX = -1;
    static final String TAG_HISTORICAL_RECORD = "historical-record";
    static final String TAG_HISTORICAL_RECORDS = "historical-records";
    private OnChooseActivityListener mActivityChoserModelPolicy;
    final Context mContext;
    final String mHistoryFileName;
    private Intent mIntent;
    static final String LOG_TAG = ActivityChooserModel.class.getSimpleName();
    private static final Object sRegistryLock = new Object();
    private static final Map<String, ActivityChooserModel> sDataModelRegistry = new HashMap();
    private final Object mInstanceLock = new Object();
    private final List<ActivityResolveInfo> mActivities = new ArrayList();
    private final List<HistoricalRecord> mHistoricalRecords = new ArrayList();
    private ActivitySorter mActivitySorter = new DefaultSorter();
    private int mHistoryMaxSize = 50;
    boolean mCanReadHistoricalData = true;
    private boolean mReadShareHistoryCalled = false;
    private boolean mHistoricalRecordsChanged = true;
    private boolean mReloadActivities = false;

    public interface ActivityChooserModelClient {
        void setActivityChooserModel(ActivityChooserModel activityChooserModel);
    }

    public interface ActivitySorter {
        void sort(Intent intent, List<ActivityResolveInfo> list, List<HistoricalRecord> list2);
    }

    public interface OnChooseActivityListener {
        boolean onChooseActivity(ActivityChooserModel activityChooserModel, Intent intent);
    }

    public static ActivityChooserModel get(Context context, String historyFileName) {
        ActivityChooserModel dataModel;
        synchronized (sRegistryLock) {
            dataModel = sDataModelRegistry.get(historyFileName);
            if (dataModel == null) {
                dataModel = new ActivityChooserModel(context, historyFileName);
                sDataModelRegistry.put(historyFileName, dataModel);
            }
        }
        return dataModel;
    }

    private ActivityChooserModel(Context context, String historyFileName) {
        this.mContext = context.getApplicationContext();
        if (!TextUtils.isEmpty(historyFileName) && !historyFileName.endsWith(HISTORY_FILE_EXTENSION)) {
            this.mHistoryFileName = historyFileName + HISTORY_FILE_EXTENSION;
        } else {
            this.mHistoryFileName = historyFileName;
        }
    }

    public void setIntent(Intent intent) {
        synchronized (this.mInstanceLock) {
            if (this.mIntent == intent) {
                return;
            }
            this.mIntent = intent;
            this.mReloadActivities = true;
            ensureConsistentState();
        }
    }

    public Intent getIntent() {
        Intent intent;
        synchronized (this.mInstanceLock) {
            intent = this.mIntent;
        }
        return intent;
    }

    public int getActivityCount() {
        int size;
        synchronized (this.mInstanceLock) {
            ensureConsistentState();
            size = this.mActivities.size();
        }
        return size;
    }

    public ResolveInfo getActivity(int index) {
        ResolveInfo resolveInfo;
        synchronized (this.mInstanceLock) {
            ensureConsistentState();
            resolveInfo = this.mActivities.get(index).resolveInfo;
        }
        return resolveInfo;
    }

    public int getActivityIndex(ResolveInfo activity) {
        synchronized (this.mInstanceLock) {
            ensureConsistentState();
            List<ActivityResolveInfo> activities = this.mActivities;
            int activityCount = activities.size();
            for (int i = 0; i < activityCount; i++) {
                ActivityResolveInfo currentActivity = activities.get(i);
                if (currentActivity.resolveInfo == activity) {
                    return i;
                }
            }
            return -1;
        }
    }

    public Intent chooseActivity(int index) {
        synchronized (this.mInstanceLock) {
            if (this.mIntent == null) {
                return null;
            }
            ensureConsistentState();
            ActivityResolveInfo chosenActivity = this.mActivities.get(index);
            ComponentName chosenName = new ComponentName(chosenActivity.resolveInfo.activityInfo.packageName, chosenActivity.resolveInfo.activityInfo.name);
            Intent choiceIntent = new Intent(this.mIntent);
            choiceIntent.setComponent(chosenName);
            if (this.mActivityChoserModelPolicy != null) {
                Intent choiceIntentCopy = new Intent(choiceIntent);
                boolean handled = this.mActivityChoserModelPolicy.onChooseActivity(this, choiceIntentCopy);
                if (handled) {
                    return null;
                }
            }
            HistoricalRecord historicalRecord = new HistoricalRecord(chosenName, System.currentTimeMillis(), 1.0f);
            addHistoricalRecord(historicalRecord);
            return choiceIntent;
        }
    }

    public void setOnChooseActivityListener(OnChooseActivityListener listener) {
        synchronized (this.mInstanceLock) {
            this.mActivityChoserModelPolicy = listener;
        }
    }

    public ResolveInfo getDefaultActivity() {
        synchronized (this.mInstanceLock) {
            ensureConsistentState();
            if (!this.mActivities.isEmpty()) {
                return this.mActivities.get(0).resolveInfo;
            }
            return null;
        }
    }

    public void setDefaultActivity(int index) {
        float weight;
        synchronized (this.mInstanceLock) {
            ensureConsistentState();
            ActivityResolveInfo newDefaultActivity = this.mActivities.get(index);
            ActivityResolveInfo oldDefaultActivity = this.mActivities.get(0);
            if (oldDefaultActivity != null) {
                weight = (oldDefaultActivity.weight - newDefaultActivity.weight) + 5.0f;
            } else {
                weight = 1.0f;
            }
            ComponentName defaultName = new ComponentName(newDefaultActivity.resolveInfo.activityInfo.packageName, newDefaultActivity.resolveInfo.activityInfo.name);
            HistoricalRecord historicalRecord = new HistoricalRecord(defaultName, System.currentTimeMillis(), weight);
            addHistoricalRecord(historicalRecord);
        }
    }

    private void persistHistoricalDataIfNeeded() {
        if (!this.mReadShareHistoryCalled) {
            throw new IllegalStateException("No preceding call to #readHistoricalData");
        }
        if (!this.mHistoricalRecordsChanged) {
            return;
        }
        this.mHistoricalRecordsChanged = false;
        if (!TextUtils.isEmpty(this.mHistoryFileName)) {
            new PersistHistoryAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new ArrayList(this.mHistoricalRecords), this.mHistoryFileName);
        }
    }

    public void setActivitySorter(ActivitySorter activitySorter) {
        synchronized (this.mInstanceLock) {
            if (this.mActivitySorter == activitySorter) {
                return;
            }
            this.mActivitySorter = activitySorter;
            if (sortActivitiesIfNeeded()) {
                notifyChanged();
            }
        }
    }

    public void setHistoryMaxSize(int historyMaxSize) {
        synchronized (this.mInstanceLock) {
            if (this.mHistoryMaxSize == historyMaxSize) {
                return;
            }
            this.mHistoryMaxSize = historyMaxSize;
            pruneExcessiveHistoricalRecordsIfNeeded();
            if (sortActivitiesIfNeeded()) {
                notifyChanged();
            }
        }
    }

    public int getHistoryMaxSize() {
        int i;
        synchronized (this.mInstanceLock) {
            i = this.mHistoryMaxSize;
        }
        return i;
    }

    public int getHistorySize() {
        int size;
        synchronized (this.mInstanceLock) {
            ensureConsistentState();
            size = this.mHistoricalRecords.size();
        }
        return size;
    }

    private void ensureConsistentState() {
        boolean stateChanged = loadActivitiesIfNeeded();
        boolean stateChanged2 = stateChanged | readHistoricalDataIfNeeded();
        pruneExcessiveHistoricalRecordsIfNeeded();
        if (stateChanged2) {
            sortActivitiesIfNeeded();
            notifyChanged();
        }
    }

    private boolean sortActivitiesIfNeeded() {
        if (this.mActivitySorter != null && this.mIntent != null && !this.mActivities.isEmpty() && !this.mHistoricalRecords.isEmpty()) {
            this.mActivitySorter.sort(this.mIntent, this.mActivities, Collections.unmodifiableList(this.mHistoricalRecords));
            return true;
        }
        return false;
    }

    private boolean loadActivitiesIfNeeded() {
        if (!this.mReloadActivities || this.mIntent == null) {
            return false;
        }
        this.mReloadActivities = false;
        this.mActivities.clear();
        List<ResolveInfo> resolveInfos = this.mContext.getPackageManager().queryIntentActivities(this.mIntent, 0);
        int resolveInfoCount = resolveInfos.size();
        for (int i = 0; i < resolveInfoCount; i++) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            this.mActivities.add(new ActivityResolveInfo(resolveInfo));
        }
        return true;
    }

    private boolean readHistoricalDataIfNeeded() throws IOException {
        if (!this.mCanReadHistoricalData || !this.mHistoricalRecordsChanged || TextUtils.isEmpty(this.mHistoryFileName)) {
            return false;
        }
        this.mCanReadHistoricalData = false;
        this.mReadShareHistoryCalled = true;
        readHistoricalDataImpl();
        return true;
    }

    private boolean addHistoricalRecord(HistoricalRecord historicalRecord) {
        boolean added = this.mHistoricalRecords.add(historicalRecord);
        if (added) {
            this.mHistoricalRecordsChanged = true;
            pruneExcessiveHistoricalRecordsIfNeeded();
            persistHistoricalDataIfNeeded();
            sortActivitiesIfNeeded();
            notifyChanged();
        }
        return added;
    }

    private void pruneExcessiveHistoricalRecordsIfNeeded() {
        int pruneCount = this.mHistoricalRecords.size() - this.mHistoryMaxSize;
        if (pruneCount <= 0) {
            return;
        }
        this.mHistoricalRecordsChanged = true;
        for (int i = 0; i < pruneCount; i++) {
            this.mHistoricalRecords.remove(0);
        }
    }

    public static final class HistoricalRecord {
        public final ComponentName activity;
        public final long time;
        public final float weight;

        public HistoricalRecord(String activityName, long time, float weight) {
            this(ComponentName.unflattenFromString(activityName), time, weight);
        }

        public HistoricalRecord(ComponentName activityName, long time, float weight) {
            this.activity = activityName;
            this.time = time;
            this.weight = weight;
        }

        public int hashCode() {
            int result = (1 * 31) + (this.activity == null ? 0 : this.activity.hashCode());
            return (((result * 31) + ((int) (this.time ^ (this.time >>> 32)))) * 31) + Float.floatToIntBits(this.weight);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            HistoricalRecord other = (HistoricalRecord) obj;
            if (this.activity == null) {
                if (other.activity != null) {
                    return false;
                }
            } else if (!this.activity.equals(other.activity)) {
                return false;
            }
            if (this.time == other.time && Float.floatToIntBits(this.weight) == Float.floatToIntBits(other.weight)) {
                return true;
            }
            return false;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            builder.append("; activity:").append(this.activity);
            builder.append("; time:").append(this.time);
            builder.append("; weight:").append(new BigDecimal(this.weight));
            builder.append("]");
            return builder.toString();
        }
    }

    public static final class ActivityResolveInfo implements Comparable<ActivityResolveInfo> {
        public final ResolveInfo resolveInfo;
        public float weight;

        public ActivityResolveInfo(ResolveInfo resolveInfo) {
            this.resolveInfo = resolveInfo;
        }

        public int hashCode() {
            return Float.floatToIntBits(this.weight) + 31;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ActivityResolveInfo other = (ActivityResolveInfo) obj;
            if (Float.floatToIntBits(this.weight) == Float.floatToIntBits(other.weight)) {
                return true;
            }
            return false;
        }

        @Override // java.lang.Comparable
        public int compareTo(ActivityResolveInfo another) {
            return Float.floatToIntBits(another.weight) - Float.floatToIntBits(this.weight);
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            builder.append("resolveInfo:").append(this.resolveInfo.toString());
            builder.append("; weight:").append(new BigDecimal(this.weight));
            builder.append("]");
            return builder.toString();
        }
    }

    private static final class DefaultSorter implements ActivitySorter {
        private static final float WEIGHT_DECAY_COEFFICIENT = 0.95f;
        private final Map<ComponentName, ActivityResolveInfo> mPackageNameToActivityMap = new HashMap();

        DefaultSorter() {
        }

        @Override // androidx.appcompat.widget.ActivityChooserModel.ActivitySorter
        public void sort(Intent intent, List<ActivityResolveInfo> activities, List<HistoricalRecord> historicalRecords) {
            Map<ComponentName, ActivityResolveInfo> componentNameToActivityMap = this.mPackageNameToActivityMap;
            componentNameToActivityMap.clear();
            int activityCount = activities.size();
            for (int i = 0; i < activityCount; i++) {
                ActivityResolveInfo activity = activities.get(i);
                activity.weight = 0.0f;
                ComponentName componentName = new ComponentName(activity.resolveInfo.activityInfo.packageName, activity.resolveInfo.activityInfo.name);
                componentNameToActivityMap.put(componentName, activity);
            }
            int i2 = historicalRecords.size();
            int lastShareIndex = i2 - 1;
            float nextRecordWeight = 1.0f;
            for (int i3 = lastShareIndex; i3 >= 0; i3--) {
                HistoricalRecord historicalRecord = historicalRecords.get(i3);
                ComponentName componentName2 = historicalRecord.activity;
                ActivityResolveInfo activity2 = componentNameToActivityMap.get(componentName2);
                if (activity2 != null) {
                    activity2.weight += historicalRecord.weight * nextRecordWeight;
                    nextRecordWeight *= WEIGHT_DECAY_COEFFICIENT;
                }
            }
            Collections.sort(activities);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:45:0x00d0, code lost:
    
        return;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void readHistoricalDataImpl() throws java.io.IOException {
        /*
            r12 = this;
            java.lang.String r0 = "Error reading historical recrod file: "
            r1 = 0
            android.content.Context r2 = r12.mContext     // Catch: java.io.FileNotFoundException -> Ld9
            java.lang.String r3 = r12.mHistoryFileName     // Catch: java.io.FileNotFoundException -> Ld9
            java.io.FileInputStream r2 = r2.openFileInput(r3)     // Catch: java.io.FileNotFoundException -> Ld9
            org.xmlpull.v1.XmlPullParser r1 = android.util.Xml.newPullParser()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            java.lang.String r3 = "UTF-8"
            r1.setInput(r2, r3)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            r3 = 0
        L16:
            r4 = 1
            if (r3 == r4) goto L22
            r5 = 2
            if (r3 == r5) goto L22
            int r4 = r1.next()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            r3 = r4
            goto L16
        L22:
            java.lang.String r5 = "historical-records"
            java.lang.String r6 = r1.getName()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            boolean r5 = r5.equals(r6)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            if (r5 == 0) goto L84
            java.util.List<androidx.appcompat.widget.ActivityChooserModel$HistoricalRecord> r5 = r12.mHistoricalRecords     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            r5.clear()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
        L33:
            int r6 = r1.next()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            r3 = r6
            if (r3 != r4) goto L42
        L3b:
            if (r2 == 0) goto Ld0
            r2.close()     // Catch: java.io.IOException -> Lce
            goto Lcd
        L42:
            r6 = 3
            if (r3 == r6) goto L33
            r6 = 4
            if (r3 != r6) goto L49
            goto L33
        L49:
            java.lang.String r6 = r1.getName()     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            java.lang.String r7 = "historical-record"
            boolean r7 = r7.equals(r6)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            if (r7 == 0) goto L7c
            java.lang.String r7 = "activity"
            r8 = 0
            java.lang.String r7 = r1.getAttributeValue(r8, r7)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            java.lang.String r9 = "time"
            java.lang.String r9 = r1.getAttributeValue(r8, r9)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            long r9 = java.lang.Long.parseLong(r9)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            java.lang.String r11 = "weight"
            java.lang.String r8 = r1.getAttributeValue(r8, r11)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            float r8 = java.lang.Float.parseFloat(r8)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            androidx.appcompat.widget.ActivityChooserModel$HistoricalRecord r11 = new androidx.appcompat.widget.ActivityChooserModel$HistoricalRecord     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            r11.<init>(r7, r9, r8)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            r5.add(r11)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            goto L33
        L7c:
            org.xmlpull.v1.XmlPullParserException r4 = new org.xmlpull.v1.XmlPullParserException     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            java.lang.String r7 = "Share records file not well-formed."
            r4.<init>(r7)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            throw r4     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
        L84:
            org.xmlpull.v1.XmlPullParserException r4 = new org.xmlpull.v1.XmlPullParserException     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            java.lang.String r5 = "Share records file does not start with historical-records tag."
            r4.<init>(r5)     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
            throw r4     // Catch: java.lang.Throwable -> L8c java.io.IOException -> L8e org.xmlpull.v1.XmlPullParserException -> Lae
        L8c:
            r0 = move-exception
            goto Ld1
        L8e:
            r1 = move-exception
            java.lang.String r3 = androidx.appcompat.widget.ActivityChooserModel.LOG_TAG     // Catch: java.lang.Throwable -> L8c
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L8c
            r4.<init>()     // Catch: java.lang.Throwable -> L8c
            java.lang.StringBuilder r0 = r4.append(r0)     // Catch: java.lang.Throwable -> L8c
            java.lang.String r4 = r12.mHistoryFileName     // Catch: java.lang.Throwable -> L8c
            java.lang.StringBuilder r0 = r0.append(r4)     // Catch: java.lang.Throwable -> L8c
            java.lang.String r0 = r0.toString()     // Catch: java.lang.Throwable -> L8c
            android.util.Log.e(r3, r0, r1)     // Catch: java.lang.Throwable -> L8c
            if (r2 == 0) goto Ld0
            r2.close()     // Catch: java.io.IOException -> Lce
            goto Lcd
        Lae:
            r1 = move-exception
            java.lang.String r3 = androidx.appcompat.widget.ActivityChooserModel.LOG_TAG     // Catch: java.lang.Throwable -> L8c
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L8c
            r4.<init>()     // Catch: java.lang.Throwable -> L8c
            java.lang.StringBuilder r0 = r4.append(r0)     // Catch: java.lang.Throwable -> L8c
            java.lang.String r4 = r12.mHistoryFileName     // Catch: java.lang.Throwable -> L8c
            java.lang.StringBuilder r0 = r0.append(r4)     // Catch: java.lang.Throwable -> L8c
            java.lang.String r0 = r0.toString()     // Catch: java.lang.Throwable -> L8c
            android.util.Log.e(r3, r0, r1)     // Catch: java.lang.Throwable -> L8c
            if (r2 == 0) goto Ld0
            r2.close()     // Catch: java.io.IOException -> Lce
        Lcd:
            goto Ld0
        Lce:
            r0 = move-exception
            goto Lcd
        Ld0:
            return
        Ld1:
            if (r2 == 0) goto Ld8
            r2.close()     // Catch: java.io.IOException -> Ld7
            goto Ld8
        Ld7:
            r1 = move-exception
        Ld8:
            throw r0
        Ld9:
            r0 = move-exception
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.appcompat.widget.ActivityChooserModel.readHistoricalDataImpl():void");
    }

    private final class PersistHistoryAsyncTask extends AsyncTask<Object, Void, Void> {
        PersistHistoryAsyncTask() {
        }

        @Override // android.os.AsyncTask
        public Void doInBackground(Object... args) throws Throwable {
            Throwable th;
            int i = 0;
            List<HistoricalRecord> historicalRecords = (List) args[0];
            String historyFileName = (String) args[1];
            try {
                FileOutputStream fos = ActivityChooserModel.this.mContext.openFileOutput(historyFileName, 0);
                XmlSerializer serializer = Xml.newSerializer();
                try {
                } catch (Throwable th2) {
                    th = th2;
                }
                try {
                    try {
                        serializer.setOutput(fos, null);
                        serializer.startDocument("UTF-8", true);
                        serializer.startTag(null, ActivityChooserModel.TAG_HISTORICAL_RECORDS);
                        int recordCount = historicalRecords.size();
                        int i2 = 0;
                        while (i2 < recordCount) {
                            HistoricalRecord record = historicalRecords.remove(i);
                            serializer.startTag(null, ActivityChooserModel.TAG_HISTORICAL_RECORD);
                            serializer.attribute(null, ActivityChooserModel.ATTRIBUTE_ACTIVITY, record.activity.flattenToString());
                            List<HistoricalRecord> historicalRecords2 = historicalRecords;
                            try {
                                serializer.attribute(null, ActivityChooserModel.ATTRIBUTE_TIME, String.valueOf(record.time));
                                serializer.attribute(null, ActivityChooserModel.ATTRIBUTE_WEIGHT, String.valueOf(record.weight));
                                serializer.endTag(null, ActivityChooserModel.TAG_HISTORICAL_RECORD);
                                i2++;
                                historicalRecords = historicalRecords2;
                                i = 0;
                            } catch (IOException e) {
                                ioe = e;
                                Log.e(ActivityChooserModel.LOG_TAG, "Error writing historical record file: " + ActivityChooserModel.this.mHistoryFileName, ioe);
                                ActivityChooserModel.this.mCanReadHistoricalData = true;
                                if (fos != null) {
                                    fos.close();
                                }
                                return null;
                            } catch (IllegalArgumentException e2) {
                                iae = e2;
                                Log.e(ActivityChooserModel.LOG_TAG, "Error writing historical record file: " + ActivityChooserModel.this.mHistoryFileName, iae);
                                ActivityChooserModel.this.mCanReadHistoricalData = true;
                                if (fos != null) {
                                    fos.close();
                                }
                                return null;
                            } catch (IllegalStateException e3) {
                                ise = e3;
                                Log.e(ActivityChooserModel.LOG_TAG, "Error writing historical record file: " + ActivityChooserModel.this.mHistoryFileName, ise);
                                ActivityChooserModel.this.mCanReadHistoricalData = true;
                                if (fos != null) {
                                    fos.close();
                                }
                                return null;
                            }
                        }
                        serializer.endTag(null, ActivityChooserModel.TAG_HISTORICAL_RECORDS);
                        serializer.endDocument();
                        ActivityChooserModel.this.mCanReadHistoricalData = true;
                    } catch (IOException e4) {
                    }
                } catch (IOException e5) {
                    ioe = e5;
                } catch (IllegalArgumentException e6) {
                    iae = e6;
                } catch (IllegalStateException e7) {
                    ise = e7;
                } catch (Throwable th3) {
                    th = th3;
                    ActivityChooserModel.this.mCanReadHistoricalData = true;
                    if (fos == null) {
                        throw th;
                    }
                    try {
                        fos.close();
                        throw th;
                    } catch (IOException e8) {
                        throw th;
                    }
                }
                if (fos != null) {
                    fos.close();
                }
                return null;
            } catch (FileNotFoundException fnfe) {
                Log.e(ActivityChooserModel.LOG_TAG, "Error writing historical record file: " + historyFileName, fnfe);
                return null;
            }
        }
    }
}
