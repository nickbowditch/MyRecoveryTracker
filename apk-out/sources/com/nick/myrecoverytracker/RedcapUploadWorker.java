package com.nick.myrecoverytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Triple;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.text.Charsets;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/* compiled from: RedcapUploadWorker.kt */
@Metadata(d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\f\u0018\u0000 (2\u00020\u0001:\u0001(B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\t\u001a\u00020\nH\u0096@¢\u0006\u0002\u0010\u000bJ\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J8\u0010\u0012\u001a\u00020\r2\u0006\u0010\u0013\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u00112\u001e\u0010\u0015\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00180\u00170\u0016H\u0002J\u0018\u0010\u0019\u001a\u00020\r2\u0006\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\u0011H\u0002J\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0011H\u0002J\u0012\u0010\u001f\u001a\u0004\u0018\u00010\u00112\u0006\u0010 \u001a\u00020\u0011H\u0002J\u0010\u0010!\u001a\u00020\u00112\u0006\u0010\"\u001a\u00020\u0011H\u0002J \u0010#\u001a\u00020\r2\u0006\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u0015\u001a\u00020\u00182\u0006\u0010$\u001a\u00020\u0011H\u0002J\u0018\u0010%\u001a\u00020\r2\u0006\u0010&\u001a\u00020\u00112\u0006\u0010'\u001a\u00020\u0018H\u0002R\u000e\u0010\b\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006)"}, d2 = {"Lcom/nick/myrecoverytracker/RedcapUploadWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "ctx", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureQueueHeader", "", "f", "Ljava/io/File;", "header", "", "upsertUnlocksQueue", "queue", "participantId", "rows", "", "Lkotlin/Triple;", "", "writeEncrypted", "file", "plain", "looksLikeHeader", "", "firstLine", "normalizeDate", "raw", "sha256", "s", "writeReceipt", "serverBody", "logSchemaVersion", "feature", "version", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class RedcapUploadWorker extends CoroutineWorker {
    private static final String DEVICE_ID = "TEST-DEVICE";
    private static final String INSTR_UNLOCKS = "daily_unlocks";
    private static final String QUEUE_FILE = "redcap_queue.csv";
    private static final String QUEUE_HEADER = "instrument,participant_id,date,schema_version,unlocks";
    private static final String RECEIPTS_FILE = "redcap_receipts.csv";
    private static final String SCHEMA_LOG_FILE = "schema_versions.csv";
    private static final int SCHEMA_VERSION_UNLOCKS = 1;
    private final Context ctx;
    private static final Regex DATE_PREFIX = new Regex("^(\\d{4}-\\d{2}-\\d{2})");
    private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    /* compiled from: RedcapUploadWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.RedcapUploadWorker", f = "RedcapUploadWorker.kt", i = {}, l = {ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_WIDTH_DEFAULT}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.RedcapUploadWorker$doWork$1, reason: invalid class name */
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
            return RedcapUploadWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public RedcapUploadWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        Context applicationContext = getApplicationContext();
        Intrinsics.checkNotNullExpressionValue(applicationContext, "getApplicationContext(...)");
        this.ctx = applicationContext;
    }

    /* compiled from: RedcapUploadWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.RedcapUploadWorker$doWork$2", f = "RedcapUploadWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.RedcapUploadWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return RedcapUploadWorker.this.new AnonymousClass2(continuation);
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Object invokeSuspend(Object obj) throws Throwable {
            String host;
            String str = "";
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure(obj);
                    File filesDir = RedcapUploadWorker.this.ctx.getFilesDir();
                    if (filesDir == null) {
                        return ListenableWorker.Result.retry();
                    }
                    int i = 0;
                    SharedPreferences prefs = RedcapUploadWorker.this.ctx.getSharedPreferences("redcap_upload", 0);
                    try {
                        RedcapUploadWorker.this.ensureQueueHeader(new File(filesDir, RedcapUploadWorker.QUEUE_FILE), RedcapUploadWorker.QUEUE_HEADER);
                    } catch (Throwable th) {
                    }
                    File rollup = new File(filesDir, "daily_unlocks.csv");
                    if (!rollup.exists()) {
                        return ListenableWorker.Result.success();
                    }
                    int i2 = 1;
                    List<String> lines = FilesKt.readLines$default(rollup, null, 1, null);
                    if (lines.isEmpty()) {
                        return ListenableWorker.Result.success();
                    }
                    if (RedcapUploadWorker.this.looksLikeHeader((String) CollectionsKt.first(lines))) {
                        lines = CollectionsKt.drop(lines, 1);
                    }
                    if (lines.isEmpty()) {
                        return ListenableWorker.Result.success();
                    }
                    List rows = new ArrayList();
                    for (String line : lines) {
                        if (StringsKt.isBlank(line)) {
                            i = 0;
                            i2 = 1;
                        } else {
                            String[] strArr = new String[i2];
                            strArr[i] = ",";
                            List parts = StringsKt.split$default((CharSequence) line, strArr, false, 0, 6, (Object) null);
                            if (parts.size() >= 2) {
                                String date = RedcapUploadWorker.this.normalizeDate(StringsKt.trim((CharSequence) parts.get(i)).toString());
                                if (date != null) {
                                    Integer intOrNull = StringsKt.toIntOrNull(StringsKt.trim((CharSequence) parts.get(i2)).toString());
                                    if (intOrNull == null) {
                                        i = 0;
                                        i2 = 1;
                                    } else {
                                        int unlocks = intOrNull.intValue();
                                        String recordId = "TEST-DEVICE-" + date + "-unlocks";
                                        rows.add(new Triple(recordId, date, Boxing.boxInt(unlocks)));
                                        i = 0;
                                        i2 = 1;
                                    }
                                }
                            } else {
                                i = 0;
                                i2 = 1;
                            }
                        }
                    }
                    if (rows.isEmpty()) {
                        return ListenableWorker.Result.success();
                    }
                    StringBuilder csvSb = new StringBuilder("record_id,participant_id,date,feature_schema_version,daily_unlocks\n");
                    for (Triple triple : CollectionsKt.sortedWith(rows, new Comparator() { // from class: com.nick.myrecoverytracker.RedcapUploadWorker$doWork$2$invokeSuspend$$inlined$sortedBy$1
                        /* JADX WARN: Multi-variable type inference failed */
                        @Override // java.util.Comparator
                        public final int compare(T t, T t2) {
                            return ComparisonsKt.compareValues((String) ((Triple) t).getSecond(), (String) ((Triple) t2).getSecond());
                        }
                    })) {
                        csvSb.append((String) triple.component1()).append(",").append(RedcapUploadWorker.DEVICE_ID).append(",").append((String) triple.component2()).append(",").append(1).append(",").append(((Number) triple.component3()).intValue()).append("\n");
                        str = str;
                    }
                    String str2 = str;
                    String csv = csvSb.toString();
                    Intrinsics.checkNotNullExpressionValue(csv, "toString(...)");
                    RedcapUploadWorker.this.logSchemaVersion(RedcapUploadWorker.INSTR_UNLOCKS, 1);
                    RedcapUploadWorker.this.writeEncrypted(new File(filesDir, "daily_metrics_upload.csv"), csv);
                    RollupValidator.INSTANCE.validateUnlocks(RedcapUploadWorker.this.ctx);
                    try {
                        File qf = new File(filesDir, RedcapUploadWorker.QUEUE_FILE);
                        RedcapUploadWorker.this.ensureQueueHeader(qf, RedcapUploadWorker.QUEUE_HEADER);
                        RedcapUploadWorker.this.upsertUnlocksQueue(qf, RedcapUploadWorker.DEVICE_ID, rows);
                    } catch (Throwable th2) {
                    }
                    String urlRaw = "";
                    if (!StringsKt.isBlank("") && !StringsKt.isBlank("")) {
                        if (!StringsKt.endsWith$default("", "/", false, 2, (Object) null)) {
                            urlRaw = "/";
                        }
                        String url = urlRaw;
                        try {
                            host = new URI(url).getHost();
                            if (host == null) {
                                host = str2;
                            }
                        } catch (Throwable th3) {
                            host = str2;
                        }
                        try {
                            InetAddress.getByName(host);
                            String hash = RedcapUploadWorker.this.sha256(csv);
                            String lastHash = prefs.getString("hash:daily_unlocks", null);
                            if (lastHash != null && Intrinsics.areEqual(lastHash, hash)) {
                                return ListenableWorker.Result.success();
                            }
                            FormBody form = new FormBody.Builder(null, 1, null).add("token", "").add("content", "record").add("action", "import").add("format", "csv").add("type", "flat").add("overwriteBehavior", "overwrite").add("returnContent", "count").add("returnFormat", "json").add("dateFormat", "YMD").add("data", csv).build();
                            Request req = new Request.Builder().url(url).header("Accept", "application/json").header("User-Agent", "MyRecoveryTracker/1.0 (Android)").post(form).build();
                            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(20L, TimeUnit.SECONDS).readTimeout(60L, TimeUnit.SECONDS).writeTimeout(60L, TimeUnit.SECONDS).build();
                            Ref.BooleanRef ok = new Ref.BooleanRef();
                            try {
                                Response responseExecute = client.newCall(req).execute();
                                RedcapUploadWorker redcapUploadWorker = RedcapUploadWorker.this;
                                try {
                                    Response response = responseExecute;
                                    ok.element = response.isSuccessful();
                                    ResponseBody responseBodyBody = response.body();
                                    String strString = responseBodyBody != null ? responseBodyBody.string() : null;
                                    if (strString != null) {
                                        str2 = strString;
                                    }
                                    String str3 = str2;
                                    if (ok.element) {
                                        redcapUploadWorker.writeReceipt(new File(filesDir, RedcapUploadWorker.RECEIPTS_FILE), rows.size(), str3);
                                        prefs.edit().putString("hash:daily_unlocks", hash).apply();
                                    }
                                    Unit unit = Unit.INSTANCE;
                                    CloseableKt.closeFinally(responseExecute, null);
                                } finally {
                                }
                            } catch (Throwable th4) {
                            }
                            return ok.element ? ListenableWorker.Result.success() : ListenableWorker.Result.retry();
                        } catch (Throwable th5) {
                            return ListenableWorker.Result.retry();
                        }
                    }
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.RedcapUploadWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.RedcapUploadWorker$doWork$1 r0 = (com.nick.myrecoverytracker.RedcapUploadWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.RedcapUploadWorker$doWork$1 r0 = new com.nick.myrecoverytracker.RedcapUploadWorker$doWork$1
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
            com.nick.myrecoverytracker.RedcapUploadWorker$doWork$2 r5 = new com.nick.myrecoverytracker.RedcapUploadWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.RedcapUploadWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void ensureQueueHeader(File f, String header) {
        if (!f.exists() || f.length() == 0) {
            FilesKt.writeText$default(f, header + "\n", null, 2, null);
            return;
        }
        Reader inputStreamReader = new InputStreamReader(new FileInputStream(f), Charsets.UTF_8);
        BufferedReader bufferedReader = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, 8192);
        try {
            String first = bufferedReader.readLine();
            CloseableKt.closeFinally(bufferedReader, null);
            if (first == null) {
                first = "";
            }
            if (!Intrinsics.areEqual(first, header)) {
                String body = CollectionsKt.joinToString$default(CollectionsKt.drop(FilesKt.readLines$default(f, null, 1, null), 1), "\n", null, null, 0, null, null, 62, null);
                File tmp = new File(f.getParentFile(), f.getName() + ".tmp");
                FilesKt.writeText$default(tmp, header + "\n" + body + (body.length() > 0 ? "\n" : ""), null, 2, null);
                f.delete();
                tmp.renameTo(f);
            }
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                CloseableKt.closeFinally(bufferedReader, th);
                throw th2;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void upsertUnlocksQueue(File queue, String participantId, List<Triple<String, String, Integer>> rows) {
        int i;
        int i2;
        LinkedHashMap map = new LinkedHashMap();
        int i3 = 0;
        if (!queue.exists()) {
            i = 0;
        } else {
            for (String str : CollectionsKt.drop(FilesKt.readLines$default(queue, null, 1, null), 1)) {
                if (StringsKt.isBlank(str)) {
                    i2 = i3;
                } else {
                    char[] cArr = new char[1];
                    cArr[i3] = ',';
                    List listSplit$default = StringsKt.split$default((CharSequence) str, cArr, false, 0, 6, (Object) null);
                    if (listSplit$default.size() < 5) {
                        i2 = i3;
                    } else {
                        i2 = i3;
                        map.put(listSplit$default.get(i3) + "|" + listSplit$default.get(2), listSplit$default);
                    }
                }
                i3 = i2;
            }
            i = i3;
        }
        for (Triple<String, String, Integer> triple : rows) {
            String date = triple.component2();
            int unlocks = triple.component3().intValue();
            String[] strArr = new String[5];
            strArr[i] = INSTR_UNLOCKS;
            strArr[1] = participantId;
            strArr[2] = date;
            strArr[3] = "1";
            strArr[4] = String.valueOf(unlocks);
            List cols = CollectionsKt.listOf((Object[]) strArr);
            map.put("daily_unlocks|" + date, cols);
        }
        File tmp = new File(queue.getParentFile(), queue.getName() + ".tmp");
        FilesKt.writeText$default(tmp, "instrument,participant_id,date,schema_version,unlocks\n", null, 2, null);
        Collection collectionValues = map.values();
        Intrinsics.checkNotNullExpressionValue(collectionValues, "<get-values>(...)");
        Function1[] function1Arr = new Function1[2];
        function1Arr[i] = new Function1() { // from class: com.nick.myrecoverytracker.RedcapUploadWorker$$ExternalSyntheticLambda1
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RedcapUploadWorker.upsertUnlocksQueue$lambda$2((List) obj);
            }
        };
        function1Arr[1] = new Function1() { // from class: com.nick.myrecoverytracker.RedcapUploadWorker$$ExternalSyntheticLambda2
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RedcapUploadWorker.upsertUnlocksQueue$lambda$3((List) obj);
            }
        };
        for (List list : CollectionsKt.sortedWith(collectionValues, ComparisonsKt.compareBy(function1Arr))) {
            Intrinsics.checkNotNull(list);
            FilesKt.appendText$default(tmp, CollectionsKt.joinToString$default(list, ",", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
        }
        if (queue.exists()) {
            queue.delete();
        }
        tmp.renameTo(queue);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Comparable upsertUnlocksQueue$lambda$2(List it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return (Comparable) it.get(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Comparable upsertUnlocksQueue$lambda$3(List it) {
        Intrinsics.checkNotNullParameter(it, "it");
        return (Comparable) it.get(2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void writeEncrypted(File file, String plain) {
        try {
            byte[] salt = "MyRecoverySalt".getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(salt, "getBytes(...)");
            char[] charArray = "local-pass".toCharArray();
            Intrinsics.checkNotNullExpressionValue(charArray, "toCharArray(...)");
            PBEKeySpec spec = new PBEKeySpec(charArray, salt, 10000, 256);
            SecretKey key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec);
            SecretKeySpec secret = new SecretKeySpec(key.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, secret);
            byte[] iv = cipher.getIV();
            byte[] bytes = plain.getBytes(Charsets.UTF_8);
            Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
            byte[] enc = cipher.doFinal(bytes);
            Intrinsics.checkNotNull(iv);
            Intrinsics.checkNotNull(enc);
            String blob = Base64.encodeToString(ArraysKt.plus(iv, enc), 2);
            Intrinsics.checkNotNull(blob);
            FilesKt.writeText$default(file, blob, null, 2, null);
        } catch (Throwable th) {
            FilesKt.writeText$default(file, plain, null, 2, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean looksLikeHeader(String firstLine) {
        Locale US = Locale.US;
        Intrinsics.checkNotNullExpressionValue(US, "US");
        String s = firstLine.toLowerCase(US);
        Intrinsics.checkNotNullExpressionValue(s, "toLowerCase(...)");
        return StringsKt.startsWith$default(s, "date,", false, 2, (Object) null) || StringsKt.startsWith$default(s, "ts,", false, 2, (Object) null) || StringsKt.startsWith$default(s, "day,", false, 2, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String normalizeDate(String raw) {
        MatchResult m = Regex.find$default(DATE_PREFIX, raw, 0, 2, null);
        if (m == null) {
            return null;
        }
        return m.getGroupValues().get(1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String sha256(String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = s.getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(bytes, "getBytes(...)");
        byte[] bArrDigest = md.digest(bytes);
        Intrinsics.checkNotNullExpressionValue(bArrDigest, "digest(...)");
        return ArraysKt.joinToString$default(bArrDigest, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, new Function1() { // from class: com.nick.myrecoverytracker.RedcapUploadWorker$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RedcapUploadWorker.sha256$lambda$5(((Byte) obj).byteValue());
            }
        }, 30, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final CharSequence sha256$lambda$5(byte it) {
        String str = String.format("%02x", Arrays.copyOf(new Object[]{Byte.valueOf(it)}, 1));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void writeReceipt(File file, int rows, String serverBody) {
        try {
            if (!file.exists()) {
                FilesKt.writeText$default(file, "ts,endpoint,rows,http_code,note\n", null, 2, null);
            }
            String ts = TS.format(Long.valueOf(System.currentTimeMillis()));
            String safe = StringsKt.take(StringsKt.replace$default(StringsKt.replace$default(serverBody, ",", " ", false, 4, (Object) null), "\n", " ", false, 4, (Object) null), 200);
            try {
                FilesKt.appendText$default(file, ts + ",record_import," + rows + ",200," + safe + "\n", null, 2, null);
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void logSchemaVersion(String feature, int version) {
        try {
            File f = new File(this.ctx.getFilesDir(), SCHEMA_LOG_FILE);
            if (!f.exists()) {
                FilesKt.writeText$default(f, "ts,feature,version\n", null, 2, null);
            }
            SharedPreferences prefs = this.ctx.getSharedPreferences("schema_versions", 0);
            String key = "v:" + feature;
            int last = prefs.getInt(key, -1);
            if (last != version) {
                String ts = TS.format(Long.valueOf(System.currentTimeMillis()));
                FilesKt.appendText$default(f, ts + "," + feature + "," + version + "\n", null, 2, null);
                prefs.edit().putInt(key, version).apply();
            }
        } catch (Throwable th) {
        }
    }
}
