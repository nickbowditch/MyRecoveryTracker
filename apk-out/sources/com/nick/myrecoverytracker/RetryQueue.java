package com.nick.myrecoverytracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: RetryQueue.kt */
@Metadata(d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\b\u0010\n\u001a\u00020\u000bH\u0002J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000bH\u0002J\u0016\u0010\u000f\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011J\u0016\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u00142\b\b\u0002\u0010\u0016\u001a\u00020\u0017J\u0016\u0010\u0018\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\u001aJ\u0018\u0010\u001b\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u001c\u001a\u00020\u0017H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001e"}, d2 = {"Lcom/nick/myrecoverytracker/RetryQueue;", "", "context", "Landroid/content/Context;", "<init>", "(Landroid/content/Context;)V", "lock", "Ljava/util/concurrent/locks/ReentrantLock;", "file", "Ljava/io/File;", "read", "Lorg/json/JSONArray;", "write", "", "arr", "addOrReplace", "id", "", "payload", "peekReady", "", "Lorg/json/JSONObject;", "limit", "", "markResult", "ok", "", "trim", "max", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class RetryQueue {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private final Context context;
    private final File file;
    private final ReentrantLock lock;

    public RetryQueue(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        this.context = context;
        this.lock = new ReentrantLock();
        this.file = new File(this.context.getFilesDir(), "retry_queue.json");
    }

    private final JSONArray read() {
        Object objM212constructorimpl;
        Object objM212constructorimpl2;
        JSONArray jSONArray;
        ReentrantLock reentrantLock = this.lock;
        reentrantLock.lock();
        try {
            if (!this.file.exists()) {
                return new JSONArray();
            }
            Object obj = null;
            try {
                Result.Companion companion = Result.INSTANCE;
                objM212constructorimpl = Result.m212constructorimpl(FilesKt.readText$default(this.file, null, 1, null));
            } catch (Throwable th) {
                Result.Companion companion2 = Result.INSTANCE;
                objM212constructorimpl = Result.m212constructorimpl(ResultKt.createFailure(th));
            }
            if (!Result.m218isFailureimpl(objM212constructorimpl)) {
                obj = objM212constructorimpl;
            }
            String str = (String) obj;
            if (str == null) {
                str = "";
            }
            if (StringsKt.isBlank(str)) {
                jSONArray = new JSONArray();
            } else {
                try {
                    Result.Companion companion3 = Result.INSTANCE;
                    RetryQueue retryQueue = this;
                    objM212constructorimpl2 = Result.m212constructorimpl(new JSONArray(str));
                } catch (Throwable th2) {
                    Result.Companion companion4 = Result.INSTANCE;
                    objM212constructorimpl2 = Result.m212constructorimpl(ResultKt.createFailure(th2));
                }
                if (Result.m215exceptionOrNullimpl(objM212constructorimpl2) != null) {
                    objM212constructorimpl2 = new JSONArray();
                }
                jSONArray = (JSONArray) objM212constructorimpl2;
            }
            return jSONArray;
        } finally {
            reentrantLock.unlock();
        }
    }

    private final void write(JSONArray arr) {
        ReentrantLock reentrantLock = this.lock;
        reentrantLock.lock();
        try {
            File parentFile = this.file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            File file = this.file;
            String string = arr.toString();
            Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
            FilesKt.writeText$default(file, string, null, 2, null);
            Unit unit = Unit.INSTANCE;
        } finally {
            reentrantLock.unlock();
        }
    }

    public final void addOrReplace(String id, String payload) throws JSONException {
        Intrinsics.checkNotNullParameter(id, "id");
        Intrinsics.checkNotNullParameter(payload, "payload");
        JSONArray arr = read();
        boolean replaced = false;
        int i = 0;
        int length = arr.length();
        while (true) {
            if (i >= length) {
                break;
            }
            JSONObject o = arr.getJSONObject(i);
            if (!Intrinsics.areEqual(o.optString("id"), id)) {
                i++;
            } else {
                o.put("payload", payload);
                o.put("attempts", 0);
                o.put("next_at", 0);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            arr.put(new JSONObject().put("id", id).put("payload", payload).put("attempts", 0).put("next_at", 0));
        }
        trim(arr, 200);
        write(arr);
    }

    public static /* synthetic */ List peekReady$default(RetryQueue retryQueue, int i, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = 10;
        }
        return retryQueue.peekReady(i);
    }

    public final List<JSONObject> peekReady(int limit) throws JSONException {
        long now = System.currentTimeMillis();
        JSONArray arr = read();
        ArrayList out = new ArrayList(limit);
        int length = arr.length();
        for (int i = 0; i < length; i++) {
            JSONObject o = arr.getJSONObject(i);
            if (o.optLong("next_at", 0L) <= now) {
                out.add(new JSONObject(o.toString()));
                if (out.size() >= limit) {
                    break;
                }
            }
        }
        return out;
    }

    public final void markResult(String id, boolean ok) throws JSONException {
        int delayMin;
        Intrinsics.checkNotNullParameter(id, "id");
        JSONArray arr = read();
        int length = arr.length();
        for (int i = 0; i < length; i++) {
            JSONObject o = arr.getJSONObject(i);
            if (Intrinsics.areEqual(o.optString("id"), id)) {
                if (ok) {
                    JSONArray newArr = new JSONArray();
                    int length2 = arr.length();
                    for (int j = 0; j < length2; j++) {
                        if (j != i) {
                            newArr.put(arr.get(j));
                        }
                    }
                    write(newArr);
                    return;
                }
                int attempts = o.optInt("attempts", 0) + 1;
                switch (attempts) {
                    case 1:
                        delayMin = 15;
                        break;
                    case 2:
                        delayMin = 60;
                        break;
                    case 3:
                        delayMin = 180;
                        break;
                    case 4:
                        delayMin = 360;
                        break;
                    case 5:
                        delayMin = 720;
                        break;
                    default:
                        delayMin = 1440;
                        break;
                }
                o.put("attempts", attempts);
                o.put("next_at", System.currentTimeMillis() + (delayMin * 60000));
                write(arr);
                return;
            }
        }
    }

    private final void trim(JSONArray arr, int max) {
        if (arr.length() <= max) {
            return;
        }
        JSONArray newArr = new JSONArray();
        int start = arr.length() - max;
        int length = arr.length();
        for (int i = start; i < length; i++) {
            newArr.put(arr.get(i));
        }
        write(newArr);
    }

    /* compiled from: RetryQueue.kt */
    @Metadata(d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007J&\u0010\b\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u000e¨\u0006\u000f"}, d2 = {"Lcom/nick/myrecoverytracker/RetryQueue$Companion;", "", "<init>", "()V", "netOk", "", "ctx", "Landroid/content/Context;", "logUpload", "", "code", "", "ok", "bodySnippet", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final boolean netOk(Context ctx) {
            NetworkCapabilities caps;
            Intrinsics.checkNotNullParameter(ctx, "ctx");
            Object systemService = ctx.getSystemService("connectivity");
            Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.net.ConnectivityManager");
            ConnectivityManager cm = (ConnectivityManager) systemService;
            Network net = cm.getActiveNetwork();
            if (net == null || (caps = cm.getNetworkCapabilities(net)) == null || !caps.hasCapability(12)) {
                return false;
            }
            return caps.hasTransport(1) || caps.hasTransport(0) || caps.hasTransport(3);
        }

        public final void logUpload(Context ctx, int code, boolean ok, String bodySnippet) {
            Intrinsics.checkNotNullParameter(ctx, "ctx");
            Intrinsics.checkNotNullParameter(bodySnippet, "bodySnippet");
            File f = new File(ctx.getFilesDir(), "redcap_upload_log.csv");
            if (!f.exists()) {
                File parentFile = f.getParentFile();
                if (parentFile != null) {
                    parentFile.mkdirs();
                }
                FilesKt.writeText$default(f, "date,http_code,ok,body_snippet\n", null, 2, null);
            }
            String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            String snip = StringsKt.take(StringsKt.replace$default(bodySnippet, "\n", " ", false, 4, (Object) null), 200);
            FilesKt.appendText$default(f, day + "," + code + "," + ok + "," + snip + "\n", null, 2, null);
        }
    }
}
