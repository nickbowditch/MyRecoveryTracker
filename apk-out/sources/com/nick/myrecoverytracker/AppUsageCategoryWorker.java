package com.nick.myrecoverytracker;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: AppUsageCategoryWorker.kt */
@Metadata(d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\n\u001a\u00020\u000bH\u0096@¢\u0006\u0002\u0010\fJ\u0010\u0010\r\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u001c\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f0\u00112\u0006\u0010\u0012\u001a\u00020\u000fH\u0002J\u001c\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t0\u00142\u0006\u0010\u0015\u001a\u00020\u0003H\u0002J\b\u0010\u0016\u001a\u00020\tH\u0002J$\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\t2\u0012\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u001b0\u0014H\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082D¢\u0006\u0002\n\u0000¨\u0006\u001c"}, d2 = {"Lcom/nick/myrecoverytracker/AppUsageCategoryWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "TAG", "", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "dayString", "ts", "", "dayBounds", "Lkotlin/Pair;", "now", "loadCategoryMap", "", "ctx", "defaultCategoriesJson", "writeCategoryDaily", "", "day", "buckets", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class AppUsageCategoryWorker extends CoroutineWorker {
    private final String TAG;

    /* compiled from: AppUsageCategoryWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AppUsageCategoryWorker", f = "AppUsageCategoryWorker.kt", i = {}, l = {24}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AppUsageCategoryWorker$doWork$1, reason: invalid class name */
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
            return AppUsageCategoryWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AppUsageCategoryWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        this.TAG = "AppUsageCategoryWorker";
    }

    /* compiled from: AppUsageCategoryWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AppUsageCategoryWorker$doWork$2", f = "AppUsageCategoryWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AppUsageCategoryWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return AppUsageCategoryWorker.this.new AnonymousClass2(continuation);
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
                    try {
                        long now = System.currentTimeMillis();
                        String day = AppUsageCategoryWorker.this.dayString(now);
                        Pair pairDayBounds = AppUsageCategoryWorker.this.dayBounds(now);
                        long startOfDay = ((Number) pairDayBounds.component1()).longValue();
                        long endOfDay = ((Number) pairDayBounds.component2()).longValue();
                        UsageStatsManager usm = (UsageStatsManager) AppUsageCategoryWorker.this.getApplicationContext().getSystemService(UsageStatsManager.class);
                        List stats = usm.queryUsageStats(0, startOfDay, endOfDay);
                        if (stats == null) {
                            stats = CollectionsKt.emptyList();
                        }
                        AppUsageCategoryWorker appUsageCategoryWorker = AppUsageCategoryWorker.this;
                        Context applicationContext = AppUsageCategoryWorker.this.getApplicationContext();
                        Intrinsics.checkNotNullExpressionValue(applicationContext, "getApplicationContext(...)");
                        Map pkgToCat = appUsageCategoryWorker.loadCategoryMap(applicationContext);
                        Map buckets = MapsKt.mutableMapOf(TuplesKt.to("recovery", Boxing.boxFloat(0.0f)), TuplesKt.to(NotificationCompat.CATEGORY_SOCIAL, Boxing.boxFloat(0.0f)), TuplesKt.to("entertainment", Boxing.boxFloat(0.0f)), TuplesKt.to("dating", Boxing.boxFloat(0.0f)), TuplesKt.to("other", Boxing.boxFloat(0.0f)));
                        for (UsageStats s : stats) {
                            String packageName = s.getPackageName();
                            if (packageName != null) {
                                float mins = s.getTotalTimeInForeground() / 60000.0f;
                                if (mins > 0.0f) {
                                    Locale ROOT = Locale.ROOT;
                                    Intrinsics.checkNotNullExpressionValue(ROOT, "ROOT");
                                    String lowerCase = packageName.toLowerCase(ROOT);
                                    Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
                                    String cat = (String) pkgToCat.get(lowerCase);
                                    if (cat == null) {
                                        cat = "other";
                                    }
                                    buckets.put(cat, Boxing.boxFloat(((Number) MapsKt.getValue(buckets, cat)).floatValue() + mins));
                                }
                            }
                        }
                        AppUsageCategoryWorker.this.writeCategoryDaily(day, buckets);
                        return ListenableWorker.Result.success();
                    } catch (Throwable t) {
                        Log.e(AppUsageCategoryWorker.this.TAG, "Failed: " + t.getMessage(), t);
                        return ListenableWorker.Result.retry();
                    }
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.AppUsageCategoryWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.AppUsageCategoryWorker$doWork$1 r0 = (com.nick.myrecoverytracker.AppUsageCategoryWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.AppUsageCategoryWorker$doWork$1 r0 = new com.nick.myrecoverytracker.AppUsageCategoryWorker$doWork$1
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
            kotlinx.coroutines.CoroutineDispatcher r4 = kotlinx.coroutines.Dispatchers.getDefault()
            kotlin.coroutines.CoroutineContext r4 = (kotlin.coroutines.CoroutineContext) r4
            com.nick.myrecoverytracker.AppUsageCategoryWorker$doWork$2 r5 = new com.nick.myrecoverytracker.AppUsageCategoryWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.AppUsageCategoryWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String dayString(long ts) {
        String str = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(ts));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Pair<Long, Long> dayBounds(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        long start = cal.getTimeInMillis();
        long end = 86400000 + start;
        return TuplesKt.to(Long.valueOf(start), Long.valueOf(end));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Map<String, String> loadCategoryMap(Context ctx) {
        File f = new File(ctx.getFilesDir(), "app_categories.json");
        String text = null;
        try {
            if (f.exists()) {
                text = FilesKt.readText$default(f, null, 1, null);
            }
        } catch (Throwable th) {
        }
        String json = text == null ? defaultCategoriesJson() : text;
        Map map = new LinkedHashMap();
        try {
            JSONObject obj = new JSONObject(json);
            loadCategoryMap$addAll(obj, map, "recovery");
            loadCategoryMap$addAll(obj, map, NotificationCompat.CATEGORY_SOCIAL);
            loadCategoryMap$addAll(obj, map, "entertainment");
            loadCategoryMap$addAll(obj, map, "dating");
            List<Pair> extras = CollectionsKt.listOf((Object[]) new Pair[]{TuplesKt.to("com.facebook.orca", NotificationCompat.CATEGORY_SOCIAL), TuplesKt.to("com.facebook.lite", NotificationCompat.CATEGORY_SOCIAL), TuplesKt.to("com.whatsapp.w4b", NotificationCompat.CATEGORY_SOCIAL)});
            for (Pair pair : extras) {
                String pkg = (String) pair.component1();
                String cat = (String) pair.component2();
                map.putIfAbsent(pkg, cat);
            }
            return map;
        } catch (Throwable th2) {
            return MapsKt.emptyMap();
        }
    }

    private static final void loadCategoryMap$addAll(JSONObject obj, Map<String, String> map, String cat) throws JSONException {
        String pkg;
        String string;
        if (obj.has(cat)) {
            JSONArray arr = obj.getJSONArray(cat);
            int length = arr.length();
            for (int i = 0; i < length; i++) {
                String strOptString = arr.optString(i);
                if (strOptString == null || (string = StringsKt.trim((CharSequence) strOptString).toString()) == null) {
                    pkg = null;
                } else {
                    Locale ROOT = Locale.ROOT;
                    Intrinsics.checkNotNullExpressionValue(ROOT, "ROOT");
                    pkg = string.toLowerCase(ROOT);
                    Intrinsics.checkNotNullExpressionValue(pkg, "toLowerCase(...)");
                }
                String str = pkg;
                if (!(str == null || str.length() == 0)) {
                    map.put(pkg, cat);
                }
            }
        }
    }

    private final String defaultCategoriesJson() {
        return "{\n  \"recovery\": [\n    \"au.org.aa.meetings\",\n    \"com.sobergrid\",\n    \"com.aa.bigbook\",\n    \"com.ias.recoverybox\",\n    \"com.addicaid.app\",\n    \"org.intherooms.intherooms\"\n  ],\n  \"social\": [\n    \"com.whatsapp\",\n    \"com.facebook.katana\",\n    \"com.instagram.android\",\n    \"com.snapchat.android\",\n    \"org.telegram.messenger\",\n    \"com.twitter.android\",\n    \"com.reddit.frontpage\",\n    \"com.discord\"\n  ],\n  \"entertainment\": [\n    \"com.google.android.youtube\",\n    \"com.netflix.mediaclient\",\n    \"com.spotify.music\",\n    \"com.amazon.avod.thirdpartyclient\",\n    \"com.tiktok.android\"\n  ],\n  \"dating\": [\n    \"com.tinder\",\n    \"com.bumble.app\",\n    \"co.hinge.app\",\n    \"com.grindrapp.android\",\n    \"com.okcupid.okcupid\",\n    \"com.pof.android\",\n    \"com.ftw_and_co.happn\",\n    \"com.match.android\",\n    \"com.badoo.mobile\"\n  ]\n}";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void writeCategoryDaily(String day, Map<String, Float> buckets) {
        File file = new File(getApplicationContext().getFilesDir(), "app_category_daily.csv");
        StringBuilder sb = new StringBuilder();
        for (String cat : CollectionsKt.listOf((Object[]) new String[]{"recovery", NotificationCompat.CATEGORY_SOCIAL, "entertainment", "dating", "other"})) {
            Float f = buckets.get(cat);
            float mins = f != null ? f.floatValue() : 0.0f;
            String str = String.format("%.1f", Arrays.copyOf(new Object[]{Float.valueOf(mins)}, 1));
            Intrinsics.checkNotNullExpressionValue(str, "format(...)");
            sb.append(day + "," + cat + "," + str + "\n");
        }
        String string = sb.toString();
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        FilesKt.appendText$default(file, string, null, 2, null);
    }
}
