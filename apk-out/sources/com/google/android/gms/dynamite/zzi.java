package com.google.android.gms.dynamite;

import com.google.android.gms.dynamite.DynamiteModule;

/* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
/* loaded from: classes.dex */
final class zzi implements DynamiteModule.VersionPolicy {
    zzi() {
    }

    /* JADX WARN: Removed duplicated region for block: B:7:0x001c A[PHI: r4
      0x001c: PHI (r4v2 int) = (r4v1 int), (r4v3 int) binds: [B:3:0x0014, B:5:0x0017] A[DONT_GENERATE, DONT_INLINE]] */
    @Override // com.google.android.gms.dynamite.DynamiteModule.VersionPolicy
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final com.google.android.gms.dynamite.DynamiteModule.VersionPolicy.SelectionResult selectModule(android.content.Context r3, java.lang.String r4, com.google.android.gms.dynamite.DynamiteModule.VersionPolicy.IVersions r5) throws com.google.android.gms.dynamite.DynamiteModule.LoadingException {
        /*
            r2 = this;
            com.google.android.gms.dynamite.DynamiteModule$VersionPolicy$SelectionResult r0 = new com.google.android.gms.dynamite.DynamiteModule$VersionPolicy$SelectionResult
            r0.<init>()
            int r1 = r5.zza(r3, r4)
            r0.localVersion = r1
            r1 = 1
            int r3 = r5.zzb(r3, r4, r1)
            r0.remoteVersion = r3
            int r4 = r0.localVersion
            if (r4 != 0) goto L1c
            r4 = 0
            if (r3 != 0) goto L1c
            r0.selection = r4
            goto L24
        L1c:
            if (r4 < r3) goto L22
            r3 = -1
            r0.selection = r3
            goto L24
        L22:
            r0.selection = r1
        L24:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.dynamite.zzi.selectModule(android.content.Context, java.lang.String, com.google.android.gms.dynamite.DynamiteModule$VersionPolicy$IVersions):com.google.android.gms.dynamite.DynamiteModule$VersionPolicy$SelectionResult");
    }
}
