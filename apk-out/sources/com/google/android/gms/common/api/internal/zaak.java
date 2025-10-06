package com.google.android.gms.common.api.internal;

/* compiled from: com.google.android.gms:play-services-base@@18.1.0 */
/* loaded from: classes.dex */
final class zaak implements Runnable {
    final /* synthetic */ zaaw zaa;

    zaak(zaaw zaawVar) {
        this.zaa = zaawVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        zaaw zaawVar = this.zaa;
        zaawVar.zad.cancelAvailabilityErrorNotifications(zaawVar.zac);
    }
}
