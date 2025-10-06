package com.nick.myrecoverytracker;

import android.app.Application;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.io.File;
import java.lang.Thread;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.collections.ArraysKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: CrashReporter.kt */
@Metadata(d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0016¨\u0006\u0006"}, d2 = {"Lcom/nick/myrecoverytracker/CrashReporter;", "Landroid/app/Application;", "<init>", "()V", "onCreate", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class CrashReporter extends Application {
    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() { // from class: com.nick.myrecoverytracker.CrashReporter$$ExternalSyntheticLambda0
            @Override // java.lang.Thread.UncaughtExceptionHandler
            public final void uncaughtException(Thread thread, Throwable th) {
                CrashReporter.onCreate$lambda$0(this.f$0, thread, th);
            }
        });
        PeriodicWorkRequest entropyReq = new PeriodicWorkRequest.Builder((Class<? extends ListenableWorker>) UsageEntropyDailyWorker.class, 1L, TimeUnit.DAYS).addTag("UsageEntropyDaily").build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("daily-usage-entropy", ExistingPeriodicWorkPolicy.UPDATE, entropyReq);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$0(CrashReporter this$0, Thread t, Throwable e) {
        try {
            String ts = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
            File f = new File(this$0.getFilesDir(), "crash_" + ts + ".txt");
            String name = e.getClass().getName();
            String message = e.getMessage();
            StackTraceElement[] stackTrace = e.getStackTrace();
            Intrinsics.checkNotNullExpressionValue(stackTrace, "getStackTrace(...)");
            FilesKt.writeText$default(f, name + ": " + message + "\n" + ArraysKt.joinToString$default(stackTrace, "\n", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null), null, 2, null);
        } catch (Throwable th) {
        }
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (defaultUncaughtExceptionHandler != null) {
            defaultUncaughtExceptionHandler.uncaughtException(t, e);
        }
    }
}
