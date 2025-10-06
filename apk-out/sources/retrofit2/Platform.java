package retrofit2;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;

/* loaded from: classes4.dex */
final class Platform {
    static final BuiltInFactories builtInFactories;

    @Nullable
    static final Executor callbackExecutor;
    static final Reflection reflection;

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0022  */
    static {
        /*
            java.lang.String r0 = "java.vm.name"
            java.lang.String r0 = java.lang.System.getProperty(r0)
            int r1 = r0.hashCode()
            switch(r1) {
                case -1841837151: goto L18;
                case 2039697993: goto Le;
                default: goto Ld;
            }
        Ld:
            goto L22
        Le:
            java.lang.String r1 = "Dalvik"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto Ld
            r0 = 0
            goto L23
        L18:
            java.lang.String r1 = "RoboVM"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto Ld
            r0 = 1
            goto L23
        L22:
            r0 = -1
        L23:
            r1 = 0
            switch(r0) {
                case 0: goto L49;
                case 1: goto L38;
                default: goto L27;
            }
        L27:
            retrofit2.Platform.callbackExecutor = r1
            retrofit2.Reflection$Java8 r0 = new retrofit2.Reflection$Java8
            r0.<init>()
            retrofit2.Platform.reflection = r0
            retrofit2.BuiltInFactories$Java8 r0 = new retrofit2.BuiltInFactories$Java8
            r0.<init>()
            retrofit2.Platform.builtInFactories = r0
            goto L5f
        L38:
            retrofit2.Platform.callbackExecutor = r1
            retrofit2.Reflection r0 = new retrofit2.Reflection
            r0.<init>()
            retrofit2.Platform.reflection = r0
            retrofit2.BuiltInFactories r0 = new retrofit2.BuiltInFactories
            r0.<init>()
            retrofit2.Platform.builtInFactories = r0
            goto L5f
        L49:
            retrofit2.AndroidMainExecutor r0 = new retrofit2.AndroidMainExecutor
            r0.<init>()
            retrofit2.Platform.callbackExecutor = r0
            retrofit2.Reflection$Android24 r0 = new retrofit2.Reflection$Android24
            r0.<init>()
            retrofit2.Platform.reflection = r0
            retrofit2.BuiltInFactories$Java8 r0 = new retrofit2.BuiltInFactories$Java8
            r0.<init>()
            retrofit2.Platform.builtInFactories = r0
        L5f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: retrofit2.Platform.<clinit>():void");
    }

    private Platform() {
    }
}
