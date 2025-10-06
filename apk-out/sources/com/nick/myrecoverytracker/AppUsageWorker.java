package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.StringsKt;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: AppUsageWorker.kt */
@Metadata(d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u001e\n\u0002\b\u0003\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 #2\u00020\u0001:\u0002\"#B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\b\u001a\u00020\tH\u0016J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0003H\u0002J@\u0010\r\u001a\u00020\u000e2\u0006\u0010\f\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u0012H\u0002J\u0010\u0010\u0017\u001a\u00020\u00102\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\u0016\u0010\u001a\u001a\u00020\u00122\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00190\u001cH\u0002J\u0010\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u0012H\u0002J\u0012\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00100 *\u00020!H\u0002¨\u0006$"}, d2 = {"Lcom/nick/myrecoverytracker/AppUsageWorker;", "Landroidx/work/Worker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "loadCategories", "Lcom/nick/myrecoverytracker/AppUsageWorker$Categories;", "ctx", "writeDailyCategories", "", "day", "", "totalMin", "", "recMin", "socMin", "entMin", "othMin", "dayStamp", "ts", "", "computeEntropy", "usages", "", "fmt", "v", "toSet", "", "Lorg/json/JSONArray;", "Categories", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class AppUsageWorker extends Worker {
    private static final String TAG = "AppUsageWorker";
    private static final Set<String> defaultSocial = SetsKt.setOf((Object[]) new String[]{"com.whatsapp", "com.facebook.katana", "com.instagram.android", "com.snapchat.android", "org.telegram.messenger", "com.twitter.android", "com.reddit.frontpage", "com.discord"});
    private static final Set<String> defaultEntertainment = SetsKt.setOf((Object[]) new String[]{"com.google.android.youtube", "com.netflix.mediaclient", "com.spotify.music", "com.amazon.avod.thirdpartyclient", "com.tiktok.android"});

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AppUsageWorker(Context context, WorkerParameters params) {
        super(context, params);
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(params, "params");
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x006a  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0086  */
    @Override // androidx.work.Worker
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public androidx.work.ListenableWorker.Result doWork() {
        /*
            Method dump skipped, instructions count: 747
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nick.myrecoverytracker.AppUsageWorker.doWork():androidx.work.ListenableWorker$Result");
    }

    /* compiled from: AppUsageWorker.kt */
    @Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B1\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\u0004\b\u0007\u0010\bJ\u000f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u000f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u000f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J9\u0010\u0010\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0014\u001a\u00020\u0015HÖ\u0001J\t\u0010\u0016\u001a\u00020\u0004HÖ\u0001R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\n¨\u0006\u0017"}, d2 = {"Lcom/nick/myrecoverytracker/AppUsageWorker$Categories;", "", "recovery", "", "", NotificationCompat.CATEGORY_SOCIAL, "entertainment", "<init>", "(Ljava/util/Set;Ljava/util/Set;Ljava/util/Set;)V", "getRecovery", "()Ljava/util/Set;", "getSocial", "getEntertainment", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    private static final /* data */ class Categories {
        private final Set<String> entertainment;
        private final Set<String> recovery;
        private final Set<String> social;

        /* JADX WARN: Multi-variable type inference failed */
        public static /* synthetic */ Categories copy$default(Categories categories, Set set, Set set2, Set set3, int i, Object obj) {
            if ((i & 1) != 0) {
                set = categories.recovery;
            }
            if ((i & 2) != 0) {
                set2 = categories.social;
            }
            if ((i & 4) != 0) {
                set3 = categories.entertainment;
            }
            return categories.copy(set, set2, set3);
        }

        public final Set<String> component1() {
            return this.recovery;
        }

        public final Set<String> component2() {
            return this.social;
        }

        public final Set<String> component3() {
            return this.entertainment;
        }

        public final Categories copy(Set<String> recovery, Set<String> social, Set<String> entertainment) {
            Intrinsics.checkNotNullParameter(recovery, "recovery");
            Intrinsics.checkNotNullParameter(social, "social");
            Intrinsics.checkNotNullParameter(entertainment, "entertainment");
            return new Categories(recovery, social, entertainment);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Categories)) {
                return false;
            }
            Categories categories = (Categories) other;
            return Intrinsics.areEqual(this.recovery, categories.recovery) && Intrinsics.areEqual(this.social, categories.social) && Intrinsics.areEqual(this.entertainment, categories.entertainment);
        }

        public int hashCode() {
            return (((this.recovery.hashCode() * 31) + this.social.hashCode()) * 31) + this.entertainment.hashCode();
        }

        public String toString() {
            return "Categories(recovery=" + this.recovery + ", social=" + this.social + ", entertainment=" + this.entertainment + ")";
        }

        public Categories(Set<String> recovery, Set<String> social, Set<String> entertainment) {
            Intrinsics.checkNotNullParameter(recovery, "recovery");
            Intrinsics.checkNotNullParameter(social, "social");
            Intrinsics.checkNotNullParameter(entertainment, "entertainment");
            this.recovery = recovery;
            this.social = social;
            this.entertainment = entertainment;
        }

        public final Set<String> getRecovery() {
            return this.recovery;
        }

        public final Set<String> getSocial() {
            return this.social;
        }

        public final Set<String> getEntertainment() {
            return this.entertainment;
        }
    }

    private final Categories loadCategories(Context ctx) {
        Set<String> setEmptySet;
        Set<String> set;
        Set<String> set2;
        File f = new File(ctx.getFilesDir(), "app_categories.json");
        if (!f.exists()) {
            Log.w(TAG, "app_categories.json missing; using empty sets");
            return new Categories(SetsKt.emptySet(), defaultSocial, defaultEntertainment);
        }
        try {
            JSONObject obj = new JSONObject(FilesKt.readText$default(f, null, 1, null));
            JSONArray jSONArrayOptJSONArray = obj.optJSONArray("recovery");
            if (jSONArrayOptJSONArray == null || (setEmptySet = toSet(jSONArrayOptJSONArray)) == null) {
                setEmptySet = SetsKt.emptySet();
            }
            JSONArray jSONArrayOptJSONArray2 = obj.optJSONArray(NotificationCompat.CATEGORY_SOCIAL);
            if (jSONArrayOptJSONArray2 == null || (set = toSet(jSONArrayOptJSONArray2)) == null) {
                set = defaultSocial;
            }
            JSONArray jSONArrayOptJSONArray3 = obj.optJSONArray("entertainment");
            if (jSONArrayOptJSONArray3 == null || (set2 = toSet(jSONArrayOptJSONArray3)) == null) {
                set2 = defaultEntertainment;
            }
            return new Categories(setEmptySet, set, set2);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to parse app_categories.json; using defaults", t);
            return new Categories(SetsKt.emptySet(), defaultSocial, defaultEntertainment);
        }
    }

    private final void writeDailyCategories(Context ctx, String day, double totalMin, double recMin, double socMin, double entMin, double othMin) {
        ArrayList lines;
        String str = day;
        File out = new File(ctx.getFilesDir(), "daily_app_usage_minutes.csv");
        String str2 = ",";
        String row = CollectionsKt.joinToString$default(CollectionsKt.listOf((Object[]) new String[]{str, fmt(totalMin), fmt(recMin), fmt(socMin), fmt(entMin), fmt(othMin)}), ",", null, null, 0, null, null, 62, null);
        if (out.exists()) {
            Iterable lines$default = FilesKt.readLines$default(out, null, 1, null);
            Collection arrayList = new ArrayList();
            for (Object obj : lines$default) {
                String str3 = str2;
                if (!StringsKt.startsWith$default((String) obj, str + str2, false, 2, (Object) null)) {
                    arrayList.add(obj);
                }
                str = day;
                str2 = str3;
            }
            lines = CollectionsKt.toMutableList(arrayList);
        } else {
            lines = new ArrayList();
        }
        if (lines.isEmpty() || !Intrinsics.areEqual(CollectionsKt.first(lines), "date,total_min,recovery_min,social_min,entertainment_min,other_min")) {
            lines.add(0, "date,total_min,recovery_min,social_min,entertainment_min,other_min");
        }
        lines.add(row);
        FilesKt.writeText$default(out, CollectionsKt.joinToString$default(lines, "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }

    private final String dayStamp(long ts) {
        String str = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(ts));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    private final double computeEntropy(Collection<Long> usages) {
        long total = CollectionsKt.sumOfLong(usages);
        if (total == 0) {
            return 0.0d;
        }
        double h = 0.0d;
        Iterator<Long> it = usages.iterator();
        while (it.hasNext()) {
            long t = it.next().longValue();
            double p = t / total;
            h += (-p) * Math.log(p);
        }
        return h;
    }

    private final String fmt(double v) {
        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
        String str = String.format(Locale.US, "%.3f", Arrays.copyOf(new Object[]{Double.valueOf(v)}, 1));
        Intrinsics.checkNotNullExpressionValue(str, "format(...)");
        return str;
    }

    private final Set<String> toSet(JSONArray $this$toSet) {
        HashSet s = new HashSet($this$toSet.length());
        int length = $this$toSet.length();
        for (int i = 0; i < length; i++) {
            String v = $this$toSet.optString(i, null);
            String str = v;
            if (!(str == null || StringsKt.isBlank(str))) {
                s.add(v);
            }
        }
        return s;
    }
}
