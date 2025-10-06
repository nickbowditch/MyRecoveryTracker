package okhttp3.internal.http;

import androidx.constraintlayout.widget.ConstraintLayout;
import kotlin.Metadata;
import okhttp3.Interceptor;

/* compiled from: CallServerInterceptor.kt */
@Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0016J\u0010\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\f"}, d2 = {"Lokhttp3/internal/http/CallServerInterceptor;", "Lokhttp3/Interceptor;", "forWebSocket", "", "(Z)V", "intercept", "Lokhttp3/Response;", "chain", "Lokhttp3/Interceptor$Chain;", "shouldIgnoreAndWaitForRealResponse", "code", "", "okhttp"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes4.dex */
public final class CallServerInterceptor implements Interceptor {
    private final boolean forWebSocket;

    public CallServerInterceptor(boolean forWebSocket) {
        this.forWebSocket = forWebSocket;
    }

    /* JADX WARN: Removed duplicated region for block: B:43:0x00ed A[Catch: IOException -> 0x01d8, TRY_LEAVE, TryCatch #1 {IOException -> 0x01d8, blocks: (B:41:0x00d6, B:43:0x00ed), top: B:97:0x00d6 }] */
    /* JADX WARN: Removed duplicated region for block: B:55:0x0137  */
    /* JADX WARN: Removed duplicated region for block: B:61:0x0158 A[Catch: IOException -> 0x01d5, TryCatch #3 {IOException -> 0x01d5, blocks: (B:52:0x011d, B:56:0x013e, B:60:0x0149, B:62:0x0168, B:64:0x0179, B:71:0x0190, B:73:0x0196, B:77:0x01a3, B:79:0x01c0, B:80:0x01c8, B:81:0x01d3, B:66:0x0185, B:61:0x0158), top: B:101:0x011d }] */
    /* JADX WARN: Removed duplicated region for block: B:66:0x0185 A[Catch: IOException -> 0x01d5, TryCatch #3 {IOException -> 0x01d5, blocks: (B:52:0x011d, B:56:0x013e, B:60:0x0149, B:62:0x0168, B:64:0x0179, B:71:0x0190, B:73:0x0196, B:77:0x01a3, B:79:0x01c0, B:80:0x01c8, B:81:0x01d3, B:66:0x0185, B:61:0x0158), top: B:101:0x011d }] */
    /* JADX WARN: Removed duplicated region for block: B:71:0x0190 A[Catch: IOException -> 0x01d5, TryCatch #3 {IOException -> 0x01d5, blocks: (B:52:0x011d, B:56:0x013e, B:60:0x0149, B:62:0x0168, B:64:0x0179, B:71:0x0190, B:73:0x0196, B:77:0x01a3, B:79:0x01c0, B:80:0x01c8, B:81:0x01d3, B:66:0x0185, B:61:0x0158), top: B:101:0x011d }] */
    /* JADX WARN: Removed duplicated region for block: B:73:0x0196 A[Catch: IOException -> 0x01d5, TryCatch #3 {IOException -> 0x01d5, blocks: (B:52:0x011d, B:56:0x013e, B:60:0x0149, B:62:0x0168, B:64:0x0179, B:71:0x0190, B:73:0x0196, B:77:0x01a3, B:79:0x01c0, B:80:0x01c8, B:81:0x01d3, B:66:0x0185, B:61:0x0158), top: B:101:0x011d }] */
    /* JADX WARN: Removed duplicated region for block: B:74:0x019b  */
    /* JADX WARN: Removed duplicated region for block: B:77:0x01a3 A[Catch: IOException -> 0x01d5, TryCatch #3 {IOException -> 0x01d5, blocks: (B:52:0x011d, B:56:0x013e, B:60:0x0149, B:62:0x0168, B:64:0x0179, B:71:0x0190, B:73:0x0196, B:77:0x01a3, B:79:0x01c0, B:80:0x01c8, B:81:0x01d3, B:66:0x0185, B:61:0x0158), top: B:101:0x011d }] */
    @Override // okhttp3.Interceptor
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public okhttp3.Response intercept(okhttp3.Interceptor.Chain r21) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 502
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http.CallServerInterceptor.intercept(okhttp3.Interceptor$Chain):okhttp3.Response");
    }

    private final boolean shouldIgnoreAndWaitForRealResponse(int code) {
        if (code == 100) {
            return true;
        }
        return 102 <= code && code < 200;
    }
}
