package com.google.android.gms.common.internal;

import java.util.concurrent.ConcurrentHashMap;

/* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
@Deprecated
/* loaded from: classes.dex */
public class LibraryVersion {
    private static final GmsLogger zza = new GmsLogger("LibraryVersion", "");
    private static LibraryVersion zzb = new LibraryVersion();
    private ConcurrentHashMap zzc = new ConcurrentHashMap();

    protected LibraryVersion() {
    }

    public static LibraryVersion getInstance() {
        return zzb;
    }

    /* JADX WARN: Removed duplicated region for block: B:27:0x009a  */
    /* JADX WARN: Removed duplicated region for block: B:32:0x00ac  */
    @java.lang.Deprecated
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.String getVersion(java.lang.String r9) throws java.lang.Throwable {
        /*
            r8 = this;
            java.lang.String r0 = "Failed to get app version for libraryName: "
            java.lang.String r1 = "LibraryVersion"
            java.lang.String r2 = "Please provide a valid libraryName"
            com.google.android.gms.common.internal.Preconditions.checkNotEmpty(r9, r2)
            java.util.concurrent.ConcurrentHashMap r2 = r8.zzc
            boolean r2 = r2.containsKey(r9)
            if (r2 == 0) goto L1a
            java.util.concurrent.ConcurrentHashMap r0 = r8.zzc
            java.lang.Object r9 = r0.get(r9)
            java.lang.String r9 = (java.lang.String) r9
            return r9
        L1a:
            java.util.Properties r2 = new java.util.Properties
            r2.<init>()
            r3 = 0
            java.lang.Object[] r4 = new java.lang.Object[]{r9}     // Catch: java.lang.Throwable -> L78 java.io.IOException -> L7a
            java.lang.Class<com.google.android.gms.common.internal.LibraryVersion> r5 = com.google.android.gms.common.internal.LibraryVersion.class
            java.lang.String r6 = "/%s.properties"
            java.lang.String r4 = java.lang.String.format(r6, r4)     // Catch: java.lang.Throwable -> L78 java.io.IOException -> L7a
            java.io.InputStream r4 = r5.getResourceAsStream(r4)     // Catch: java.lang.Throwable -> L78 java.io.IOException -> L7a
            if (r4 == 0) goto L56
            r2.load(r4)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            java.lang.String r5 = "version"
            java.lang.String r3 = r2.getProperty(r5, r3)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            com.google.android.gms.common.internal.GmsLogger r2 = com.google.android.gms.common.internal.LibraryVersion.zza     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            r5.<init>()     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            r5.append(r9)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            java.lang.String r6 = " version is "
            r5.append(r6)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            r5.append(r3)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            java.lang.String r5 = r5.toString()     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            r2.v(r1, r5)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            goto L6a
        L56:
            com.google.android.gms.common.internal.GmsLogger r2 = com.google.android.gms.common.internal.LibraryVersion.zza     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            r5.<init>()     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            r5.append(r0)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            r5.append(r9)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            java.lang.String r5 = r5.toString()     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
            r2.w(r1, r5)     // Catch: java.lang.Throwable -> L70 java.io.IOException -> L73
        L6a:
            if (r4 == 0) goto L98
            com.google.android.gms.common.util.IOUtils.closeQuietly(r4)
            goto L98
        L70:
            r9 = move-exception
            r3 = r4
            goto Laa
        L73:
            r2 = move-exception
            r7 = r4
            r4 = r3
            r3 = r7
            goto L7c
        L78:
            r9 = move-exception
            goto Laa
        L7a:
            r2 = move-exception
            r4 = r3
        L7c:
            com.google.android.gms.common.internal.GmsLogger r5 = com.google.android.gms.common.internal.LibraryVersion.zza     // Catch: java.lang.Throwable -> La9
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> La9
            r6.<init>()     // Catch: java.lang.Throwable -> La9
            r6.append(r0)     // Catch: java.lang.Throwable -> La9
            r6.append(r9)     // Catch: java.lang.Throwable -> La9
            java.lang.String r0 = r6.toString()     // Catch: java.lang.Throwable -> La9
            r5.e(r1, r0, r2)     // Catch: java.lang.Throwable -> La9
            if (r3 == 0) goto L96
            com.google.android.gms.common.util.IOUtils.closeQuietly(r3)
            goto L97
        L96:
        L97:
            r3 = r4
        L98:
            if (r3 != 0) goto La3
            com.google.android.gms.common.internal.GmsLogger r0 = com.google.android.gms.common.internal.LibraryVersion.zza
            java.lang.String r2 = ".properties file is dropped during release process. Failure to read app version is expected during Google internal testing where locally-built libraries are used"
            r0.d(r1, r2)
            java.lang.String r3 = "UNKNOWN"
        La3:
            java.util.concurrent.ConcurrentHashMap r0 = r8.zzc
            r0.put(r9, r3)
            return r3
        La9:
            r9 = move-exception
        Laa:
            if (r3 == 0) goto Laf
            com.google.android.gms.common.util.IOUtils.closeQuietly(r3)
        Laf:
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.internal.LibraryVersion.getVersion(java.lang.String):java.lang.String");
    }
}
