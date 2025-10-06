package com.google.android.gms.common.api.internal;

import java.util.concurrent.locks.Lock;

/* compiled from: com.google.android.gms:play-services-base@@18.1.0 */
/* loaded from: classes.dex */
abstract class zabg {
    private final zabf zaa;

    protected zabg(zabf zabfVar) {
        this.zaa = zabfVar;
    }

    protected abstract void zaa();

    public final void zab(zabi zabiVar) {
        Lock lock;
        zabiVar.zai.lock();
        try {
            if (zabiVar.zan != this.zaa) {
                lock = zabiVar.zai;
            } else {
                zaa();
                lock = zabiVar.zai;
            }
            lock.unlock();
        } catch (Throwable th) {
            zabiVar.zai.unlock();
            throw th;
        }
    }
}
