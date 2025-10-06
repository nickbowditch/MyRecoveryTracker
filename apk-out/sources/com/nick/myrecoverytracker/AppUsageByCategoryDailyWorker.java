package com.nick.myrecoverytracker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.work.CoroutineWorker;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import kotlinx.coroutines.CoroutineScope;

/* compiled from: AppUsageByCategoryDailyWorker.kt */
@Metadata(d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0003\u0018\u0000 \u00162\u00020\u0001:\u0001\u0016B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\b\u001a\u00020\tH\u0096@¢\u0006\u0002\u0010\nJ\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u001a\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0002J\u0014\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00140\u0013H\u0002J\b\u0010\u0015\u001a\u00020\u000eH\u0002¨\u0006\u0017"}, d2 = {"Lcom/nick/myrecoverytracker/AppUsageByCategoryDailyWorker;", "Landroidx/work/CoroutineWorker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "shouldSkip", "", "pkg", "", "classify", "ai", "Landroid/content/pm/ApplicationInfo;", "dayBoundsMillis", "Lkotlin/Pair;", "", "today", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class AppUsageByCategoryDailyWorker extends CoroutineWorker {
    private static final String TAG = "AppUsageByCategoryDaily";
    private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /* compiled from: AppUsageByCategoryDailyWorker.kt */
    @Metadata(k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker", f = "AppUsageByCategoryDailyWorker.kt", i = {}, l = {21}, m = "doWork", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker$doWork$1, reason: invalid class name */
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
            return AppUsageByCategoryDailyWorker.this.doWork(this);
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AppUsageByCategoryDailyWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* compiled from: AppUsageByCategoryDailyWorker.kt */
    @Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00070\u0001¢\u0006\u0002\b\u0002*\u00020\u0003H\n"}, d2 = {"<anonymous>", "Landroidx/work/ListenableWorker$Result;", "Lkotlin/jvm/internal/EnhancedNullability;", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    @DebugMetadata(c = "com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker$doWork$2", f = "AppUsageByCategoryDailyWorker.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker$doWork$2, reason: invalid class name */
    static final class AnonymousClass2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super ListenableWorker.Result>, Object> {
        private /* synthetic */ Object L$0;
        int label;

        AnonymousClass2(Continuation<? super AnonymousClass2> continuation) {
            super(2, continuation);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            AnonymousClass2 anonymousClass2 = AppUsageByCategoryDailyWorker.this.new AnonymousClass2(continuation);
            anonymousClass2.L$0 = obj;
            return anonymousClass2;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super ListenableWorker.Result> continuation) {
            return ((AnonymousClass2) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        /* JADX WARN: Removed duplicated region for block: B:134:0x0390 A[EXC_TOP_SPLITTER, SYNTHETIC] */
        /* JADX WARN: Removed duplicated region for block: B:164:0x03fb A[SYNTHETIC] */
        /* JADX WARN: Removed duplicated region for block: B:94:0x030a A[Catch: all -> 0x04fb, TryCatch #5 {all -> 0x04fb, blocks: (B:72:0x01dd, B:74:0x01e3, B:78:0x01f7, B:79:0x0231, B:82:0x023e, B:83:0x0286, B:86:0x029a, B:90:0x02ae, B:91:0x02b4, B:92:0x0304, B:94:0x030a, B:96:0x033c, B:98:0x034a, B:99:0x0384), top: B:140:0x01dd }] */
        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public final java.lang.Object invokeSuspend(java.lang.Object r42) throws java.lang.Throwable {
            /*
                Method dump skipped, instructions count: 1300
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker.AnonymousClass2.invokeSuspend(java.lang.Object):java.lang.Object");
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
            boolean r0 = r8 instanceof com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker.AnonymousClass1
            if (r0 == 0) goto L14
            r0 = r8
            com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker$doWork$1 r0 = (com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker.AnonymousClass1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker$doWork$1 r0 = new com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker$doWork$1
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
            com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker$doWork$2 r5 = new com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker$doWork$2
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
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.AppUsageByCategoryDailyWorker.doWork(kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean shouldSkip(String pkg) {
        Locale US = Locale.US;
        Intrinsics.checkNotNullExpressionValue(US, "US");
        String p = pkg.toLowerCase(US);
        Intrinsics.checkNotNullExpressionValue(p, "toLowerCase(...)");
        return Intrinsics.areEqual(p, "android") || Intrinsics.areEqual(p, BuildConfig.APPLICATION_ID) || Intrinsics.areEqual(p, "com.android.intentresolver") || Intrinsics.areEqual(p, "com.google.android.apps.nexuslauncher");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String classify(String pkg, ApplicationInfo ai) {
        switch (ai != null ? ai.category : -1) {
            case 0:
                return "game";
            case 1:
                return "music_and_audio";
            case 2:
                return "video";
            case 3:
                return "image";
            case 4:
                return NotificationCompat.CATEGORY_SOCIAL;
            case 5:
                return "news";
            case 6:
                return "maps";
            case 7:
                return "productivity";
            default:
                Locale US = Locale.US;
                Intrinsics.checkNotNullExpressionValue(US, "US");
                String p = pkg.toLowerCase(US);
                Intrinsics.checkNotNullExpressionValue(p, "toLowerCase(...)");
                if (StringsKt.startsWith$default(p, "com.tinder", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.bumble", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.hinge", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.okcupid", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.pof", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.match", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.happn", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.grindr", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.scruff", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.herapp", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.coffeemeetsbagel", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.feeld", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.zoosk", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.badoo", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.hily", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.rayalabs.raya", false, 2, (Object) null)) {
                    return "dating";
                }
                if (StringsKt.startsWith$default(p, "com.whatsapp", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.instagram", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.facebook.katana", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.facebook.orca", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.twitter.android", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.snapchat", false, 2, (Object) null) || StringsKt.startsWith$default(p, "org.telegram", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.discord", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.tiktok", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.gm", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.microsoft.office.outlook", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.textra", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.dialer", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.contacts", false, 2, (Object) null)) {
                    return NotificationCompat.CATEGORY_SOCIAL;
                }
                if (StringsKt.startsWith$default(p, "com.google.android.apps.docs", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.keep", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.microsoft.office", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.calendar", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.apps.tasks", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.chrome", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.android.chrome", false, 2, (Object) null) || StringsKt.startsWith$default(p, "org.mozilla.firefox", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.microsoft.emmx", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.android.vending", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.gms", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.deskclock", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.documentsui", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.googlequicksearchbox", false, 2, (Object) null) || StringsKt.startsWith$default(p, "org.chromium.webapk", false, 2, (Object) null)) {
                    return "productivity";
                }
                if (StringsKt.startsWith$default(p, "com.google.android.youtube", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.netflix", false, 2, (Object) null)) {
                    return "video";
                }
                if (StringsKt.startsWith$default(p, "com.spotify", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.apps.youtube.music", false, 2, (Object) null) || StringsKt.startsWith$default(p, "au.com.shiftyjelly.pocketcasts", false, 2, (Object) null)) {
                    return "music_and_audio";
                }
                if (StringsKt.startsWith$default(p, "com.amazon", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.ebay", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.apps.wallet", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.apps.walletnfcrel", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.apps.nbu.paisa.user", false, 2, (Object) null)) {
                    return "shopping";
                }
                if (StringsKt.startsWith$default(p, "com.google.android.apps.maps", false, 2, (Object) null)) {
                    return "maps";
                }
                if (StringsKt.startsWith$default(p, "com.ubercab", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.grab", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.lyft", false, 2, (Object) null)) {
                    return "travel_and_local";
                }
                if (StringsKt.startsWith$default(p, "com.google.android.apps.magazines", false, 2, (Object) null)) {
                    return "news";
                }
                if (StringsKt.startsWith$default(p, "com.strava", false, 2, (Object) null)) {
                    return "health";
                }
                return (StringsKt.startsWith$default(p, "com.google.android.apps.photos", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.photopicker", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.googlecamera", false, 2, (Object) null) || StringsKt.startsWith$default(p, "com.google.android.GoogleCamera", false, 2, (Object) null)) ? "image" : "other";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final Pair<Long, Long> dayBoundsMillis() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        long start = cal.getTimeInMillis();
        long end = 86400000 + start;
        return new Pair<>(Long.valueOf(start), Long.valueOf(end));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final String today() {
        String str = TS.format(new Date());
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }
}
