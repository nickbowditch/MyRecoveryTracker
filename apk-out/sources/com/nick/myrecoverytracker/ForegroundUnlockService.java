package com.nick.myrecoverytracker;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import kotlinx.coroutines.DebugKt;

/* compiled from: ForegroundUnlockService.kt */
@Metadata(d1 = {"\u0000~\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u000f\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002*\u0002\u001a2\u0018\u0000 @2\u00020\u0001:\u0001@B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u001f\u0010\u001c\u001a\n \u001d*\u0004\u0018\u00010\r0\r2\b\b\u0002\u0010\u001e\u001a\u00020\u001fH\u0002¢\u0006\u0002\u0010 J\u0018\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\r2\u0006\u0010$\u001a\u00020\rH\u0002J\u0018\u0010%\u001a\u00020\"2\u0006\u0010#\u001a\u00020\r2\u0006\u0010&\u001a\u00020\rH\u0002J\u0010\u0010'\u001a\u00020\"2\u0006\u0010(\u001a\u00020\rH\u0002J\u0018\u0010'\u001a\u00020\"2\u0006\u0010(\u001a\u00020\r2\u0006\u0010)\u001a\u00020\rH\u0002J\u0010\u0010*\u001a\u00020\"2\u0006\u0010+\u001a\u00020\u0012H\u0002J\u0010\u0010,\u001a\u00020\"2\u0006\u0010-\u001a\u00020\rH\u0002J\b\u0010.\u001a\u00020\"H\u0002J\b\u0010/\u001a\u00020\"H\u0002J\b\u00100\u001a\u00020\"H\u0002J\b\u00104\u001a\u00020\"H\u0016J\b\u00105\u001a\u00020\"H\u0016J\"\u00106\u001a\u00020\u00102\b\u00107\u001a\u0004\u0018\u0001082\u0006\u00109\u001a\u00020\u00102\u0006\u0010:\u001a\u00020\u0010H\u0016J\u0014\u0010;\u001a\u0004\u0018\u00010<2\b\u00107\u001a\u0004\u0018\u000108H\u0016J\b\u0010=\u001a\u00020\"H\u0002J\b\u0010>\u001a\u00020?H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e¢\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\tX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u0019\u001a\u00020\u001aX\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u001bR\u0010\u00101\u001a\u000202X\u0082\u0004¢\u0006\u0004\n\u0002\u00103¨\u0006A"}, d2 = {"Lcom/nick/myrecoverytracker/ForegroundUnlockService;", "Landroid/app/Service;", "<init>", "()V", "km", "Landroid/app/KeyguardManager;", "pm", "Landroid/os/PowerManager;", "mainHandler", "Landroid/os/Handler;", "tsMinFmt", "Ljava/text/SimpleDateFormat;", "lastHeartbeat", "", "tsLogFmt", "currentSessionId", "", "currentSessionLogged", "", "pendingChecks", "", "Ljava/lang/Runnable;", "hbThread", "Landroid/os/HandlerThread;", "hbHandler", "hbRunnable", "com/nick/myrecoverytracker/ForegroundUnlockService$hbRunnable$1", "Lcom/nick/myrecoverytracker/ForegroundUnlockService$hbRunnable$1;", "nowStr", "kotlin.jvm.PlatformType", "ms", "", "(J)Ljava/lang/String;", "ensureHeader", "", "name", "header", "appendLine", "line", "logDiag", "tag", "extra", "logScreen", DebugKt.DEBUG_PROPERTY_VALUE_ON, "logUnlockForSession", "reason", "startNewSession", "cancelPendingChecks", "scheduleChecksForCurrentSession", "screenReceiver", "com/nick/myrecoverytracker/ForegroundUnlockService$screenReceiver$1", "Lcom/nick/myrecoverytracker/ForegroundUnlockService$screenReceiver$1;", "onCreate", "onDestroy", "onStartCommand", "intent", "Landroid/content/Intent;", "flags", "startId", "onBind", "Landroid/os/IBinder;", "createChannel", "buildNotification", "Landroid/app/Notification;", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class ForegroundUnlockService extends Service {
    private static final String CHANNEL_ID = "mrt_fg_channel_v4";
    private static final int NOTIF_ID = 1001;
    private int currentSessionId;
    private boolean currentSessionLogged;
    private Handler hbHandler;
    private final ForegroundUnlockService$hbRunnable$1 hbRunnable;
    private HandlerThread hbThread;
    private KeyguardManager km;
    private String lastHeartbeat;
    private Handler mainHandler;
    private final List<Runnable> pendingChecks;
    private PowerManager pm;
    private final ForegroundUnlockService$screenReceiver$1 screenReceiver;
    private final SimpleDateFormat tsLogFmt;
    private final SimpleDateFormat tsMinFmt;

    /* JADX WARN: Type inference failed for: r0v5, types: [com.nick.myrecoverytracker.ForegroundUnlockService$hbRunnable$1] */
    /* JADX WARN: Type inference failed for: r0v6, types: [com.nick.myrecoverytracker.ForegroundUnlockService$screenReceiver$1] */
    public ForegroundUnlockService() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        this.tsMinFmt = simpleDateFormat;
        this.lastHeartbeat = "";
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        simpleDateFormat2.setTimeZone(TimeZone.getDefault());
        this.tsLogFmt = simpleDateFormat2;
        this.pendingChecks = new ArrayList();
        this.hbRunnable = new Runnable() { // from class: com.nick.myrecoverytracker.ForegroundUnlockService$hbRunnable$1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    this.this$0.ensureHeader("heartbeat.csv", "ts");
                    String ts = this.this$0.tsMinFmt.format(Long.valueOf(System.currentTimeMillis()));
                    if (!Intrinsics.areEqual(ts, this.this$0.lastHeartbeat)) {
                        this.this$0.appendLine("heartbeat.csv", ts + "\n");
                        this.this$0.lastHeartbeat = ts;
                    }
                } catch (Throwable th) {
                }
                Handler handler = this.this$0.hbHandler;
                if (handler != null) {
                    handler.postDelayed(this, 60000L);
                }
            }
        };
        this.screenReceiver = new BroadcastReceiver() { // from class: com.nick.myrecoverytracker.ForegroundUnlockService$screenReceiver$1
            /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context ctx, Intent intent) {
                Intrinsics.checkNotNullParameter(ctx, "ctx");
                Intrinsics.checkNotNullParameter(intent, "intent");
                String action = intent.getAction();
                if (action != null) {
                    switch (action.hashCode()) {
                        case -2128145023:
                            if (action.equals("android.intent.action.SCREEN_OFF")) {
                                this.this$0.logScreen(false);
                                this.this$0.cancelPendingChecks();
                                this.this$0.currentSessionLogged = false;
                                break;
                            }
                            break;
                        case -1454123155:
                            if (action.equals("android.intent.action.SCREEN_ON")) {
                                this.this$0.logScreen(true);
                                this.this$0.startNewSession();
                                PowerManager powerManager = this.this$0.pm;
                                KeyguardManager keyguardManager = null;
                                if (powerManager == null) {
                                    Intrinsics.throwUninitializedPropertyAccessException("pm");
                                    powerManager = null;
                                }
                                boolean interactive = powerManager.isInteractive();
                                KeyguardManager keyguardManager2 = this.this$0.km;
                                if (keyguardManager2 == null) {
                                    Intrinsics.throwUninitializedPropertyAccessException("km");
                                } else {
                                    keyguardManager = keyguardManager2;
                                }
                                boolean locked = keyguardManager.isKeyguardLocked();
                                this.this$0.logDiag("SCREEN_ON", "locked=" + locked);
                                if (interactive && !locked) {
                                    this.this$0.logUnlockForSession("SCREEN_ON_immediate");
                                }
                                this.this$0.scheduleChecksForCurrentSession();
                                break;
                            }
                            break;
                        case 823795052:
                            if (action.equals("android.intent.action.USER_PRESENT")) {
                                this.this$0.logUnlockForSession("USER_PRESENT");
                                break;
                            }
                            break;
                    }
                }
            }
        };
    }

    private final String nowStr(long ms) {
        return this.tsLogFmt.format(Long.valueOf(ms));
    }

    static /* synthetic */ String nowStr$default(ForegroundUnlockService foregroundUnlockService, long j, int i, Object obj) {
        if ((i & 1) != 0) {
            j = System.currentTimeMillis();
        }
        return foregroundUnlockService.nowStr(j);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final synchronized void ensureHeader(String name, String header) {
        File f = new File(getFilesDir(), name);
        if (!f.exists() || f.length() == 0) {
            FileChannel channel = new FileOutputStream(f, false).getChannel();
            try {
                FileChannel fileChannel = channel;
                byte[] bytes = (header + "\n").getBytes(Charsets.UTF_8);
                Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
                fileChannel.write(ByteBuffer.wrap(bytes));
                fileChannel.force(true);
                Unit unit = Unit.INSTANCE;
                CloseableKt.closeFinally(channel, null);
            } finally {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final synchronized void appendLine(String name, String line) {
        File f = new File(getFilesDir(), name);
        FileChannel channel = new FileOutputStream(f, true).getChannel();
        try {
            FileChannel fileChannel = channel;
            byte[] bytes = line.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            fileChannel.write(ByteBuffer.wrap(bytes));
            fileChannel.force(true);
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(channel, null);
        } finally {
        }
    }

    private final void logDiag(String tag) {
        ensureHeader("unlock_diag.csv", "ts,tag,extra");
        appendLine("unlock_diag.csv", nowStr$default(this, 0L, 1, null) + "," + tag + ",\n");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void logDiag(String tag, String extra) {
        ensureHeader("unlock_diag.csv", "ts,tag,extra");
        String t = StringsKt.replace$default(tag, "\"", "\"\"", false, 4, (Object) null);
        String e = StringsKt.replace$default(extra, "\"", "\"\"", false, 4, (Object) null);
        appendLine("unlock_diag.csv", nowStr$default(this, 0L, 1, null) + ",\"" + t + "\",\"" + e + "\"\n");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void logScreen(boolean on) {
        ensureHeader("screen_log.csv", "ts,state");
        appendLine("screen_log.csv", nowStr$default(this, 0L, 1, null) + "," + (on ? "ON" : "OFF") + "\n");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void logUnlockForSession(String reason) {
        if (this.currentSessionLogged) {
            return;
        }
        this.currentSessionLogged = true;
        ensureHeader("unlock_log.csv", "ts,event");
        appendLine("unlock_log.csv", nowStr$default(this, 0L, 1, null) + ",UNLOCK\n");
        logDiag("UNLOCK", reason);
        cancelPendingChecks();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void startNewSession() {
        cancelPendingChecks();
        this.currentSessionId++;
        this.currentSessionLogged = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void cancelPendingChecks() {
        if (this.pendingChecks.isEmpty()) {
            return;
        }
        for (Runnable runnable : this.pendingChecks) {
            Handler handler = this.mainHandler;
            if (handler == null) {
                Intrinsics.throwUninitializedPropertyAccessException("mainHandler");
                handler = null;
            }
            handler.removeCallbacks(runnable);
        }
        this.pendingChecks.clear();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void scheduleChecksForCurrentSession() {
        int session = this.currentSessionId;
        scheduleChecksForCurrentSession$schedule(this, session, 0L, "0", true);
        scheduleChecksForCurrentSession$schedule$default(this, session, 120L, "120", false, 16, null);
        scheduleChecksForCurrentSession$schedule$default(this, session, 300L, "300", false, 16, null);
        scheduleChecksForCurrentSession$schedule$default(this, session, 800L, "800", false, 16, null);
        scheduleChecksForCurrentSession$schedule$default(this, session, 1200L, "1200", false, 16, null);
        scheduleChecksForCurrentSession$schedule$default(this, session, 1500L, "1500", false, 16, null);
        scheduleChecksForCurrentSession$schedule$default(this, session, 3000L, "3000", false, 16, null);
    }

    static /* synthetic */ void scheduleChecksForCurrentSession$schedule$default(ForegroundUnlockService foregroundUnlockService, int i, long j, String str, boolean z, int i2, Object obj) {
        if ((i2 & 16) != 0) {
            z = false;
        }
        scheduleChecksForCurrentSession$schedule(foregroundUnlockService, i, j, str, z);
    }

    private static final void scheduleChecksForCurrentSession$schedule(final ForegroundUnlockService this$0, final int session, long delayMs, final String label, final boolean immediate) {
        Runnable r = new Runnable() { // from class: com.nick.myrecoverytracker.ForegroundUnlockService$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                ForegroundUnlockService.scheduleChecksForCurrentSession$schedule$lambda$7(session, this$0, immediate, label);
            }
        };
        this$0.pendingChecks.add(r);
        Handler handler = this$0.mainHandler;
        if (handler == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mainHandler");
            handler = null;
        }
        handler.postDelayed(r, delayMs);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void scheduleChecksForCurrentSession$schedule$lambda$7(int $session, ForegroundUnlockService this$0, boolean $immediate, String $label) {
        if ($session != this$0.currentSessionId || this$0.currentSessionLogged) {
            return;
        }
        PowerManager powerManager = this$0.pm;
        KeyguardManager keyguardManager = null;
        if (powerManager == null) {
            Intrinsics.throwUninitializedPropertyAccessException("pm");
            powerManager = null;
        }
        boolean interactive = powerManager.isInteractive();
        KeyguardManager keyguardManager2 = this$0.km;
        if (keyguardManager2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("km");
        } else {
            keyguardManager = keyguardManager2;
        }
        boolean locked = keyguardManager.isKeyguardLocked();
        String note = "interactive=" + interactive + ",locked=" + locked;
        if ($immediate) {
            this$0.logDiag("CHECK,immediate", note);
        } else {
            this$0.logDiag("CHECK,t+" + $label, note);
        }
        if (interactive && !locked) {
            this$0.logUnlockForSession($immediate ? "CHECK,immediate" : "CHECK,t+" + $label);
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Object systemService = getSystemService("keyguard");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.app.KeyguardManager");
        this.km = (KeyguardManager) systemService;
        Object systemService2 = getSystemService("power");
        Intrinsics.checkNotNull(systemService2, "null cannot be cast to non-null type android.os.PowerManager");
        this.pm = (PowerManager) systemService2;
        this.mainHandler = new Handler(Looper.getMainLooper());
        MovementCapture.INSTANCE.attach(this);
        createChannel();
        startForeground(1001, buildNotification());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.registerReceiver(this, this.screenReceiver, filter, 4);
        } else {
            registerReceiver(this.screenReceiver, filter);
        }
        HandlerThread handlerThread = new HandlerThread("mrt-heartbeat");
        handlerThread.start();
        this.hbThread = handlerThread;
        HandlerThread handlerThread2 = this.hbThread;
        Intrinsics.checkNotNull(handlerThread2);
        this.hbHandler = new Handler(handlerThread2.getLooper());
        Handler handler = this.hbHandler;
        if (handler != null) {
            handler.post(this.hbRunnable);
        }
        ensureHeader("unlock_log.csv", "ts,event");
        ensureHeader("screen_log.csv", "ts,state");
        ensureHeader("unlock_diag.csv", "ts,tag,extra");
        ensureHeader("heartbeat.csv", "ts");
    }

    @Override // android.app.Service
    public void onDestroy() {
        try {
            Result.Companion companion = Result.INSTANCE;
            ForegroundUnlockService foregroundUnlockService = this;
            foregroundUnlockService.unregisterReceiver(foregroundUnlockService.screenReceiver);
            Result.m212constructorimpl(Unit.INSTANCE);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            Result.m212constructorimpl(ResultKt.createFailure(th));
        }
        cancelPendingChecks();
        Handler handler = this.hbHandler;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        HandlerThread handlerThread = this.hbThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
        this.hbHandler = null;
        this.hbThread = null;
        MovementCapture.INSTANCE.detach();
        super.onDestroy();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                int importance = BuildConfig.DEBUG ? 4 : 3;
                NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "MyRecovery Tracker (Foreground)", importance);
                nm.createNotificationChannel(ch);
            }
        }
    }

    private final Notification buildNotification() {
        Notification notificationBuild = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("MyRecoveryAssistant").setContentText("University study is running").setSmallIcon(R.mipmap.ic_launcher).setCategory(NotificationCompat.CATEGORY_SERVICE).setOngoing(true).setOnlyAlertOnce(true).setPriority(BuildConfig.DEBUG ? 1 : 0).setVisibility(1).setForegroundServiceBehavior(1).build();
        Intrinsics.checkNotNullExpressionValue(notificationBuild, "build(...)");
        return notificationBuild;
    }
}
