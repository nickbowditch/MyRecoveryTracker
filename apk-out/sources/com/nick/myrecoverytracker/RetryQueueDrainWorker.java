package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.android.gms.common.internal.ImagesContract;
import com.nick.myrecoverytracker.RedcapClient;
import java.io.File;
import java.util.List;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;
import okhttp3.HttpUrl;
import org.json.JSONObject;

/* compiled from: RetryQueueDrainWorker.kt */
@Metadata(d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001:\u0001\u0012B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u000b\u001a\u00020\fH\u0096@¢\u0006\u0002\u0010\rJ\u0012\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002R\u000e\u0010\b\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0013"}, d2 = {"Lcom/nick/myrecoverytracker/RetryQueueDrainWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "ctx", "queue", "Lcom/nick/myrecoverytracker/RetryQueue;", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "readConfig", "Lcom/nick/myrecoverytracker/RetryQueueDrainWorker$Config;", "file", "Ljava/io/File;", "Config", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class RetryQueueDrainWorker extends CoroutineWorker {
    private final Context ctx;
    private final RetryQueue queue;

    /* compiled from: RetryQueueDrainWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.RetryQueueDrainWorker", f = "RetryQueueDrainWorker.kt", i = {}, l = {18}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.RetryQueueDrainWorker$doWork$1, reason: invalid class name */
    static final class AnonymousClass1 extends ContinuationImpl {
        int label;
        /* synthetic */ Object result;

        AnonymousClass1(Continuation<? super AnonymousClass1> continuation) {
            super(continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) {
            this.result = obj;
            this.label |= Integer.MIN_VALUE;
            return RetryQueueDrainWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public RetryQueueDrainWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        Context applicationContext = getApplicationContext();
        Intrinsics.checkNotNullExpressionValue(applicationContext, "getApplicationContext(...)");
        this.ctx = applicationContext;
        this.queue = new RetryQueue(this.ctx);
    }

    /* compiled from: RetryQueueDrainWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.RetryQueueDrainWorker$doWork$2", f = "RetryQueueDrainWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.RetryQueueDrainWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return RetryQueueDrainWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    if (RetryQueue.INSTANCE.netOk(RetryQueueDrainWorker.this.ctx)) {
                        Config cfg = RetryQueueDrainWorker.this.readConfig(new File(RetryQueueDrainWorker.this.ctx.getFilesDir(), "redcap_config.json"));
                        if (cfg != null) {
                            List items = RetryQueueDrainWorker.this.queue.peekReady(10);
                            if (items.isEmpty()) {
                                Log.i("RetryQueueDrainWorker", "Queue empty.");
                                return ListenableWorker.Result.success();
                            }
                            boolean allOk = true;
                            RedcapClient client = new RedcapClient(cfg.getUrl(), cfg.getToken());
                            for (JSONObject item : items) {
                                String id = item.optString("id");
                                String payloadStr = item.optString("payload", HttpUrl.PATH_SEGMENT_ENCODE_SET_URI);
                                try {
                                    Intrinsics.checkNotNull(payloadStr);
                                    RedcapClient.Response resp = client.postRecords(payloadStr);
                                    RetryQueue.INSTANCE.logUpload(RetryQueueDrainWorker.this.ctx, resp.getCode(), resp.getOk(), resp.getBody());
                                    RetryQueue retryQueue = RetryQueueDrainWorker.this.queue;
                                    Intrinsics.checkNotNull(id);
                                    retryQueue.markResult(id, resp.getOk());
                                    if (!resp.getOk()) {
                                        allOk = false;
                                    }
                                } catch (Throwable t) {
                                    RetryQueue.INSTANCE.logUpload(RetryQueueDrainWorker.this.ctx, 0, false, "exception: " + t.getMessage());
                                    RetryQueue retryQueue2 = RetryQueueDrainWorker.this.queue;
                                    Intrinsics.checkNotNull(id);
                                    retryQueue2.markResult(id, false);
                                    allOk = false;
                                }
                            }
                            return allOk ? ListenableWorker.Result.success() : ListenableWorker.Result.retry();
                        }
                        Log.i("RetryQueueDrainWorker", "No redcap_config.json; nothing to send.");
                        return ListenableWorker.Result.success();
                    }
                    Log.i("RetryQueueDrainWorker", "No network; retry later.");
                    return ListenableWorker.Result.retry();
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:7:0x0014  */
    @Override // androidx.work.CoroutineWorker
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.Object doWork(kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> r8) throws java.lang.Throwable {
        /*
            r7 = this;
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.RetryQueueDrainWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.RetryQueueDrainWorker$doWork$1 r0 = (com.nick.myrecoverytracker.RetryQueueDrainWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.RetryQueueDrainWorker$doWork$1 r0 = new com.nick.myrecoverytracker.RetryQueueDrainWorker$doWork$1
            r0.<init>(r8)
        L19:
            java.lang.Object r1 = r0.result
            java.lang.Object r2 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r3 = r0.label
            switch(r3) {
                case 0: goto L31;
                case 1: goto L2c;
                default: goto L24;
            }
        L24:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "call to 'resume' before 'invoke' with coroutine"
            r0.<init>(r1)
            throw r0
        L2c:
            kotlin.ResultKt.throwOnFailure(r1)
            r3 = r1
            goto L4d
        L31:
            kotlin.ResultKt.throwOnFailure(r1)
            r3 = r7
            kotlinx.coroutines.CoroutineDispatcher r4 = kotlinx.coroutines.Dispatchers.getIO()
            kotlin.coroutines.CoroutineContext r4 = (kotlin.coroutines.CoroutineContext) r4
            com.nick.myrecoverytracker.RetryQueueDrainWorker$doWork$2 r5 = new com.nick.myrecoverytracker.RetryQueueDrainWorker$doWork$2
            r6 = 0
            r5.<init>(r6)
            kotlin.jvm.functions.Function2 r5 = (kotlin.jvm.functions.Function2) r5
            r6 = 1
            r0.label = r6
            java.lang.Object r3 = kotlinx.coroutines.BuildersKt.withContext(r4, r5, r0)
            if (r3 != r2) goto L4d
            return r2
        L4d:
            java.lang.String r2 = "withContext(...)"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r3, r2)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.RetryQueueDrainWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* compiled from: RetryQueueDrainWorker.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003¢\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000e\u001a\u00020\u0003HÆ\u0003J'\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0013\u001a\u00020\u0014HÖ\u0001J\t\u0010\u0015\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t¨\u0006\u0016"}, d2 = {"Lcom/nick/myrecoverytracker/RetryQueueDrainWorker$Config;", "", ImagesContract.URL, "", "token", "recordId", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getUrl", "()Ljava/lang/String;", "getToken", "getRecordId", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    private static final /* data */ class Config {
        private final String recordId;
        private final String token;
        private final String url;

        public static /* synthetic */ Config copy$default(Config config, String str, String str2, String str3, int i, Object obj) {
            if ((i & 1) != 0) {
                str = config.url;
            }
            if ((i & 2) != 0) {
                str2 = config.token;
            }
            if ((i & 4) != 0) {
                str3 = config.recordId;
            }
            return config.copy(str, str2, str3);
        }

        /* renamed from: component1, reason: from getter */
        public final String getUrl() {
            return this.url;
        }

        /* renamed from: component2, reason: from getter */
        public final String getToken() {
            return this.token;
        }

        /* renamed from: component3, reason: from getter */
        public final String getRecordId() {
            return this.recordId;
        }

        public final Config copy(String url, String token, String recordId) {
            Intrinsics.checkNotNullParameter(url, "url");
            Intrinsics.checkNotNullParameter(token, "token");
            Intrinsics.checkNotNullParameter(recordId, "recordId");
            return new Config(url, token, recordId);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Config)) {
                return false;
            }
            Config config = (Config) other;
            return Intrinsics.areEqual(this.url, config.url) && Intrinsics.areEqual(this.token, config.token) && Intrinsics.areEqual(this.recordId, config.recordId);
        }

        public int hashCode() {
            return (((this.url.hashCode() * 31) + this.token.hashCode()) * 31) + this.recordId.hashCode();
        }

        public String toString() {
            return "Config(url=" + this.url + ", token=" + this.token + ", recordId=" + this.recordId + ")";
        }

        public Config(String url, String token, String recordId) {
            Intrinsics.checkNotNullParameter(url, "url");
            Intrinsics.checkNotNullParameter(token, "token");
            Intrinsics.checkNotNullParameter(recordId, "recordId");
            this.url = url;
            this.token = token;
            this.recordId = recordId;
        }

        public final String getRecordId() {
            return this.recordId;
        }

        public final String getToken() {
            return this.token;
        }

        public final String getUrl() {
            return this.url;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Config readConfig(File file) {
        Object objM212constructorimpl;
        Config config;
        if (!file.exists()) {
            return null;
        }
        try {
            Result.Companion companion = Result.INSTANCE;
            RetryQueueDrainWorker retryQueueDrainWorker = this;
            boolean z = true;
            JSONObject jSONObject = new JSONObject(FilesKt.readText$default(file, null, 1, null));
            String strOptString = jSONObject.optString(ImagesContract.URL, "");
            Intrinsics.checkNotNullExpressionValue(strOptString, "optString(...)");
            String string = StringsKt.trim((CharSequence) strOptString).toString();
            String strOptString2 = jSONObject.optString("token", "");
            Intrinsics.checkNotNullExpressionValue(strOptString2, "optString(...)");
            String string2 = StringsKt.trim((CharSequence) strOptString2).toString();
            String strOptString3 = jSONObject.optString("record_id", "");
            Intrinsics.checkNotNullExpressionValue(strOptString3, "optString(...)");
            String string3 = StringsKt.trim((CharSequence) strOptString3).toString();
            if (string.length() == 0) {
                config = null;
                objM212constructorimpl = Result.m212constructorimpl(config);
            } else {
                if (string2.length() != 0) {
                    z = false;
                }
                if (z) {
                    config = null;
                    objM212constructorimpl = Result.m212constructorimpl(config);
                } else {
                    config = new Config(string, string2, string3);
                    objM212constructorimpl = Result.m212constructorimpl(config);
                }
            }
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            objM212constructorimpl = Result.m212constructorimpl(ResultKt.createFailure(th));
        }
        return (Config) (Result.m218isFailureimpl(objM212constructorimpl) ? null : objM212constructorimpl);
    }
}
