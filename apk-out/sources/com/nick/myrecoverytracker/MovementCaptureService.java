package com.nick.myrecoverytracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MovementCaptureService.kt */
@Metadata(d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u00012\u00020\u0002B\u0007¢\u0006\u0004\b\u0003\u0010\u0004J\b\u0010\u0014\u001a\u00020\u0015H\u0016J\b\u0010\u0016\u001a\u00020\u0015H\u0016J\u0014\u0010\u0017\u001a\u0004\u0018\u00010\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u001aH\u0016J\u0010\u0010\u001b\u001a\u00020\u00152\u0006\u0010\u001c\u001a\u00020\u001dH\u0016J\u001a\u0010\u001e\u001a\u00020\u00152\b\u0010\u001f\u001a\u0004\u0018\u00010\b2\u0006\u0010 \u001a\u00020!H\u0016J\u0018\u0010\"\u001a\u00020\u00152\u0006\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020$H\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.¢\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.¢\u0006\u0002\n\u0000R\u0018\u0010\r\u001a\n \u000f*\u0004\u0018\u00010\u000e0\u000eX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0010R\u0018\u0010\u0011\u001a\n \u000f*\u0004\u0018\u00010\u00120\u0012X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0013¨\u0006&"}, d2 = {"Lcom/nick/myrecoverytracker/MovementCaptureService;", "Landroid/app/Service;", "Landroid/hardware/SensorEventListener;", "<init>", "()V", "sm", "Landroid/hardware/SensorManager;", "stepSensor", "Landroid/hardware/Sensor;", "arClient", "Lcom/google/android/gms/location/ActivityRecognitionClient;", "arPi", "Landroid/app/PendingIntent;", "zone", "Ljava/time/ZoneId;", "kotlin.jvm.PlatformType", "Ljava/time/ZoneId;", "fmtTs", "Ljava/time/format/DateTimeFormatter;", "Ljava/time/format/DateTimeFormatter;", "onCreate", "", "onDestroy", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onSensorChanged", NotificationCompat.CATEGORY_EVENT, "Landroid/hardware/SensorEvent;", "onAccuracyChanged", "sensor", "accuracy", "", "append", "name", "", "line", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class MovementCaptureService extends Service implements SensorEventListener {
    private ActivityRecognitionClient arClient;
    private PendingIntent arPi;
    private SensorManager sm;
    private Sensor stepSensor;
    private final ZoneId zone = ZoneId.systemDefault();
    private final DateTimeFormatter fmtTs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Object systemService = getSystemService("notification");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.NotificationManager");
        NotificationManager nm = (NotificationManager) systemService;
        if (nm.getNotificationChannel("movement_fg") == null) {
            nm.createNotificationChannel(new NotificationChannel("movement_fg", "Movement", 1));
        }
        Notification n = new Notification.Builder(this, "movement_fg").setContentTitle("Movement capture active").setSmallIcon(android.R.drawable.stat_notify_sync_noanim).setOngoing(true).build();
        Intrinsics.checkNotNullExpressionValue(n, "build(...)");
        startForeground(Notifier.ID_ONBOARDING, n);
        Object systemService2 = getSystemService("sensor");
        Intrinsics.checkNotNull(systemService2, "null cannot be cast to non-null type android.hardware.SensorManager");
        this.sm = (SensorManager) systemService2;
        SensorManager sensorManager = this.sm;
        PendingIntent pendingIntent = null;
        if (sensorManager == null) {
            Intrinsics.throwUninitializedPropertyAccessException("sm");
            sensorManager = null;
        }
        this.stepSensor = sensorManager.getDefaultSensor(19);
        Sensor sensor = this.stepSensor;
        if (sensor != null) {
            SensorManager sensorManager2 = this.sm;
            if (sensorManager2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("sm");
                sensorManager2 = null;
            }
            sensorManager2.registerListener(this, sensor, 3);
        }
        this.arClient = ActivityRecognition.getClient(this);
        Intent intent = new Intent(this, (Class<?>) MovementARReceiver.class).setAction("mrt.AR");
        Intrinsics.checkNotNullExpressionValue(intent, "setAction(...)");
        this.arPi = PendingIntent.getBroadcast(this, 100, intent, 201326592);
        ActivityTransitionRequest req = new ActivityTransitionRequest(CollectionsKt.listOf((Object[]) new ActivityTransition[]{new ActivityTransition.Builder().setActivityType(3).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(7).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(2).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(8).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(0).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(1).setActivityTransition(0).build()}));
        ActivityRecognitionClient activityRecognitionClient = this.arClient;
        if (activityRecognitionClient == null) {
            Intrinsics.throwUninitializedPropertyAccessException("arClient");
            activityRecognitionClient = null;
        }
        PendingIntent pendingIntent2 = this.arPi;
        if (pendingIntent2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("arPi");
        } else {
            pendingIntent = pendingIntent2;
        }
        activityRecognitionClient.requestActivityTransitionUpdates(req, pendingIntent);
    }

    @Override // android.app.Service
    public void onDestroy() {
        PendingIntent pendingIntent = null;
        try {
            SensorManager sensorManager = this.sm;
            if (sensorManager == null) {
                Intrinsics.throwUninitializedPropertyAccessException("sm");
                sensorManager = null;
            }
            sensorManager.unregisterListener(this);
        } catch (Throwable th) {
        }
        try {
            ActivityRecognitionClient activityRecognitionClient = this.arClient;
            if (activityRecognitionClient == null) {
                Intrinsics.throwUninitializedPropertyAccessException("arClient");
                activityRecognitionClient = null;
            }
            PendingIntent pendingIntent2 = this.arPi;
            if (pendingIntent2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("arPi");
            } else {
                pendingIntent = pendingIntent2;
            }
            activityRecognitionClient.removeActivityTransitionUpdates(pendingIntent);
        } catch (Throwable th2) {
        }
        super.onDestroy();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent event) {
        Intrinsics.checkNotNullParameter(event, "event");
        if (event.sensor.getType() != 19) {
            return;
        }
        String ts = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(this.zone).format(this.fmtTs);
        String line = ts + ",step," + event.values[0];
        append("movement_log.csv", line);
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private final void append(String name, String line) {
        File f = new File(getFilesDir(), name);
        File parentFile = f.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        FilesKt.appendText$default(f, line + "\n", null, 2, null);
    }
}
