package androidx.work.impl.background.systemalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.work.Constraints;
import androidx.work.Logger;
import androidx.work.NetworkType;
import androidx.work.impl.model.WorkSpec;
import java.util.List;

/* loaded from: classes.dex */
abstract class ConstraintProxy extends BroadcastReceiver {
    private static final String TAG = Logger.tagWithPrefix("ConstraintProxy");

    ConstraintProxy() {
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Logger.get().debug(TAG, "onReceive : " + intent);
        Intent constraintChangedIntent = CommandHandler.createConstraintsChangedIntent(context);
        context.startService(constraintChangedIntent);
    }

    public static class BatteryNotLowProxy extends ConstraintProxy {
        @Override // androidx.work.impl.background.systemalarm.ConstraintProxy, android.content.BroadcastReceiver
        public /* bridge */ /* synthetic */ void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }

    public static class BatteryChargingProxy extends ConstraintProxy {
        @Override // androidx.work.impl.background.systemalarm.ConstraintProxy, android.content.BroadcastReceiver
        public /* bridge */ /* synthetic */ void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }

    public static class StorageNotLowProxy extends ConstraintProxy {
        @Override // androidx.work.impl.background.systemalarm.ConstraintProxy, android.content.BroadcastReceiver
        public /* bridge */ /* synthetic */ void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }

    public static class NetworkStateProxy extends ConstraintProxy {
        @Override // androidx.work.impl.background.systemalarm.ConstraintProxy, android.content.BroadcastReceiver
        public /* bridge */ /* synthetic */ void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }

    static void updateAll(Context context, List<WorkSpec> workSpecs) {
        boolean batteryNotLowProxyEnabled = false;
        boolean batteryChargingProxyEnabled = false;
        boolean storageNotLowProxyEnabled = false;
        boolean networkStateProxyEnabled = false;
        for (WorkSpec workSpec : workSpecs) {
            Constraints constraints = workSpec.constraints;
            batteryNotLowProxyEnabled |= constraints.getRequiresBatteryNotLow();
            batteryChargingProxyEnabled |= constraints.getRequiresCharging();
            storageNotLowProxyEnabled |= constraints.getRequiresStorageNotLow();
            networkStateProxyEnabled |= constraints.getRequiredNetworkType() != NetworkType.NOT_REQUIRED;
            if (batteryNotLowProxyEnabled && batteryChargingProxyEnabled && storageNotLowProxyEnabled && networkStateProxyEnabled) {
                break;
            }
        }
        Intent updateProxyIntent = ConstraintProxyUpdateReceiver.newConstraintProxyUpdateIntent(context, batteryNotLowProxyEnabled, batteryChargingProxyEnabled, storageNotLowProxyEnabled, networkStateProxyEnabled);
        context.sendBroadcast(updateProxyIntent);
    }
}
