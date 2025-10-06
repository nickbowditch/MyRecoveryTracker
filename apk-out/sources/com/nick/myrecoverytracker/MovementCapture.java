package com.nick.myrecoverytracker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.WorkRequest;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;

/* compiled from: MovementCapture.kt */
@Metadata(d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0003\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u0005J\u0006\u0010\u001e\u001a\u00020\u001cJ\u0010\u0010\u001f\u001a\u00020\u001c2\u0006\u0010 \u001a\u00020!H\u0016J\u001a\u0010\"\u001a\u00020\u001c2\b\u0010#\u001a\u0004\u0018\u00010\t2\u0006\u0010$\u001a\u00020%H\u0016J\u0010\u0010&\u001a\u00020\u001c2\u0006\u0010'\u001a\u00020(H\u0002J\u0006\u0010)\u001a\u00020\u001cJ\u0006\u0010*\u001a\u00020\u001cR\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000fX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u000fX\u0082\u000e¢\u0006\u0002\n\u0000R\u0018\u0010\u0012\u001a\n \u0014*\u0004\u0018\u00010\u00130\u0013X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0015R\u0018\u0010\u0016\u001a\n \u0014*\u0004\u0018\u00010\u00170\u0017X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0018R\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u001aX\u0082\u000e¢\u0006\u0002\n\u0000¨\u0006+"}, d2 = {"Lcom/nick/myrecoverytracker/MovementCapture;", "Landroid/hardware/SensorEventListener;", "<init>", "()V", "ctx", "Landroid/content/Context;", "sm", "Landroid/hardware/SensorManager;", "acc", "Landroid/hardware/Sensor;", "lastAccTs", "", "baselineSet", "", "baseX", "", "baseY", "baseZ", "zone", "Ljava/time/ZoneId;", "kotlin.jvm.PlatformType", "Ljava/time/ZoneId;", "fmtTs", "Ljava/time/format/DateTimeFormatter;", "Ljava/time/format/DateTimeFormatter;", "arPi", "Landroid/app/PendingIntent;", "attach", "", "c", "detach", "onSensorChanged", "e", "Landroid/hardware/SensorEvent;", "onAccuracyChanged", "sensor", "accuracy", "", "writeMagnitude", "mag", "", "writeActive", "writeStill", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class MovementCapture implements SensorEventListener {
    private static Sensor acc;
    private static PendingIntent arPi;
    private static float baseX;
    private static float baseY;
    private static float baseZ;
    private static boolean baselineSet;
    private static Context ctx;
    private static long lastAccTs;
    private static SensorManager sm;
    public static final MovementCapture INSTANCE = new MovementCapture();
    private static final ZoneId zone = ZoneId.systemDefault();
    private static final DateTimeFormatter fmtTs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

    private MovementCapture() {
    }

    public final void attach(Context c) {
        Intrinsics.checkNotNullParameter(c, "c");
        if (ctx != null) {
            return;
        }
        ctx = c.getApplicationContext();
        Context context = ctx;
        Intrinsics.checkNotNull(context);
        Object systemService = context.getSystemService("sensor");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.hardware.SensorManager");
        sm = (SensorManager) systemService;
        SensorManager sensorManager = sm;
        Intrinsics.checkNotNull(sensorManager);
        acc = sensorManager.getDefaultSensor(1);
        Sensor sensor = acc;
        if (sensor != null) {
            SensorManager sensorManager2 = sm;
            Intrinsics.checkNotNull(sensorManager2);
            sensorManager2.registerListener(INSTANCE, sensor, 3);
        }
        Intent intent = new Intent(ctx, (Class<?>) MovementARReceiver.class).setAction("mrt.AR");
        Intrinsics.checkNotNullExpressionValue(intent, "setAction(...)");
        arPi = PendingIntent.getBroadcast(ctx, 2101, intent, 201326592);
        Context context2 = ctx;
        Intrinsics.checkNotNull(context2);
        ActivityRecognitionClient client = ActivityRecognition.getClient(context2);
        Intrinsics.checkNotNullExpressionValue(client, "getClient(...)");
        ActivityTransitionRequest req = new ActivityTransitionRequest(CollectionsKt.listOf((Object[]) new ActivityTransition[]{new ActivityTransition.Builder().setActivityType(3).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(7).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(8).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(2).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(1).setActivityTransition(0).build(), new ActivityTransition.Builder().setActivityType(0).setActivityTransition(0).build()}));
        PendingIntent pendingIntent = arPi;
        Intrinsics.checkNotNull(pendingIntent);
        client.requestActivityTransitionUpdates(req, pendingIntent);
    }

    public final void detach() {
        try {
            SensorManager sensorManager = sm;
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        } catch (Throwable th) {
        }
        try {
            Context context = ctx;
            ActivityRecognitionClient client = context != null ? ActivityRecognition.getClient(context) : null;
            PendingIntent pendingIntent = arPi;
            if (pendingIntent != null && client != null) {
                client.removeActivityTransitionUpdates(pendingIntent);
            }
        } catch (Throwable th2) {
        }
        ctx = null;
        sm = null;
        acc = null;
        arPi = null;
        baselineSet = false;
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent e) {
        Intrinsics.checkNotNullParameter(e, "e");
        if (e.sensor.getType() != 1) {
            return;
        }
        if (!baselineSet) {
            baseX = e.values[0];
            baseY = e.values[1];
            baseZ = e.values[2];
            baselineSet = true;
            lastAccTs = 0L;
            return;
        }
        float dx = e.values[0] - baseX;
        float dy = e.values[1] - baseY;
        float dz = e.values[2] - baseZ;
        double mag = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastAccTs >= WorkRequest.MIN_BACKOFF_MILLIS) {
            lastAccTs = nowMs;
            writeMagnitude(mag);
        }
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private final void writeMagnitude(double mag) {
        Context context = ctx;
        Intrinsics.checkNotNull(context);
        File f = new File(context.getFilesDir(), "movement_log.csv");
        String ts = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zone).format(fmtTs);
        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
        String v = String.format(Locale.US, "%.6f", Arrays.copyOf(new Object[]{Double.valueOf(mag)}, 1));
        Intrinsics.checkNotNullExpressionValue(v, "format(...)");
        FilesKt.appendText$default(f, ts + "," + v + "\n", null, 2, null);
    }

    public final void writeActive() {
        Context context = ctx;
        Intrinsics.checkNotNull(context);
        File f = new File(context.getFilesDir(), "movement_log.csv");
        String ts = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zone).format(fmtTs);
        FilesKt.appendText$default(f, ts + ",1.000000\n", null, 2, null);
    }

    public final void writeStill() {
        Context context = ctx;
        Intrinsics.checkNotNull(context);
        File f = new File(context.getFilesDir(), "movement_log.csv");
        String ts = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zone).format(fmtTs);
        FilesKt.appendText$default(f, ts + ",0.000000\n", null, 2, null);
    }
}
