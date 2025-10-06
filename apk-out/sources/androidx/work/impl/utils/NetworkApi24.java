package androidx.work.impl.utils;

import android.net.ConnectivityManager;
import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: NetworkApi24.kt */
@Metadata(d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u0014\u0010\u0000\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u0007¨\u0006\u0005"}, d2 = {"registerDefaultNetworkCallbackCompat", "", "Landroid/net/ConnectivityManager;", "networkCallback", "Landroid/net/ConnectivityManager$NetworkCallback;", "work-runtime_release"}, k = 2, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class NetworkApi24 {
    public static final void registerDefaultNetworkCallbackCompat(ConnectivityManager $this$registerDefaultNetworkCallbackCompat, ConnectivityManager.NetworkCallback networkCallback) {
        Intrinsics.checkNotNullParameter($this$registerDefaultNetworkCallbackCompat, "<this>");
        Intrinsics.checkNotNullParameter(networkCallback, "networkCallback");
        $this$registerDefaultNetworkCallbackCompat.registerDefaultNetworkCallback(networkCallback);
    }
}
