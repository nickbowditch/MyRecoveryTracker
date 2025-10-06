package com.nick.myrecoverytracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.ComponentActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;

/* compiled from: MainActivity.kt */
@Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u0014¨\u0006\b"}, d2 = {"Lcom/nick/myrecoverytracker/MainActivity;", "Landroidx/activity/ComponentActivity;", "<init>", "()V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class MainActivity extends ComponentActivity {
    @Override // androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent fgIntent = new Intent(this, (Class<?>) ForegroundUnlockService.class);
        ContextCompat.startForegroundService(this, fgIntent);
        Log.i("MainActivity", "Requested ForegroundUnlockService start");
        HeartbeatWorker.INSTANCE.ensure(this);
        Log.i("MainActivity", "HeartbeatWorker.ensure invoked");
        PeriodicWorkRequest periodicUsage = new PeriodicWorkRequest.Builder((Class<? extends ListenableWorker>) UsageCaptureWorker.class, 24L, TimeUnit.HOURS).addTag("UsageCaptureDaily").build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("mrt_usage_daily", ExistingPeriodicWorkPolicy.UPDATE, periodicUsage);
        finish();
    }
}
