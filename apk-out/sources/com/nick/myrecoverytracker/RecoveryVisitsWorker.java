package com.nick.myrecoverytracker;

import android.content.Context;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: RecoveryVisitsWorker.kt */
@Metadata(d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u00002\u00020\u0001:\u0002%&B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\f\u001a\u00020\rH\u0016J\u0018\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0002J(\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00152\u0006\u0010\u0017\u001a\u00020\u00152\u0006\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u0015H\u0002J\u0016\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00130\u001b2\u0006\u0010\u001c\u001a\u00020\u001dH\u0002J(\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u001d2\u0006\u0010!\u001a\u00020\t2\u0006\u0010\"\u001a\u00020\u000f2\u0006\u0010#\u001a\u00020$H\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006'"}, d2 = {"Lcom/nick/myrecoverytracker/RecoveryVisitsWorker;", "Landroidx/work/Worker;", "appContext", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "<init>", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "tag", "", "dayFmt", "Ljava/text/SimpleDateFormat;", "doWork", "Landroidx/work/ListenableWorker$Result;", "within", "", "pt", "Lcom/nick/myrecoverytracker/RecoveryVisitsWorker$Point;", "place", "Lcom/nick/myrecoverytracker/RecoveryVisitsWorker$Place;", "haversineMeters", "", "lat1", "lon1", "lat2", "lon2", "loadPlaces", "", "cfg", "Ljava/io/File;", "upsert", "", "out", "day", "visited", "distinct", "", "Point", "Place", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class RecoveryVisitsWorker extends Worker {
    private final SimpleDateFormat dayFmt;
    private final String tag;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public RecoveryVisitsWorker(Context appContext, WorkerParameters params) {
        super(appContext, params);
        Intrinsics.checkNotNullParameter(appContext, "appContext");
        Intrinsics.checkNotNullParameter(params, "params");
        this.tag = "RecoveryVisitsWorker";
        this.dayFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    /* compiled from: RecoveryVisitsWorker.kt */
    @Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0006\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\n\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000b\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0010\u001a\u00020\u0011HÖ\u0001J\t\u0010\u0012\u001a\u00020\u0013HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b¨\u0006\u0014"}, d2 = {"Lcom/nick/myrecoverytracker/RecoveryVisitsWorker$Point;", "", "lat", "", "lon", "<init>", "(DD)V", "getLat", "()D", "getLon", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final /* data */ class Point {
        private final double lat;
        private final double lon;

        public static /* synthetic */ Point copy$default(Point point, double d, double d2, int i, Object obj) {
            if ((i & 1) != 0) {
                d = point.lat;
            }
            if ((i & 2) != 0) {
                d2 = point.lon;
            }
            return point.copy(d, d2);
        }

        /* renamed from: component1, reason: from getter */
        public final double getLat() {
            return this.lat;
        }

        /* renamed from: component2, reason: from getter */
        public final double getLon() {
            return this.lon;
        }

        public final Point copy(double lat, double lon) {
            return new Point(lat, lon);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Point)) {
                return false;
            }
            Point point = (Point) other;
            return Double.compare(this.lat, point.lat) == 0 && Double.compare(this.lon, point.lon) == 0;
        }

        public int hashCode() {
            return (Double.hashCode(this.lat) * 31) + Double.hashCode(this.lon);
        }

        public String toString() {
            return "Point(lat=" + this.lat + ", lon=" + this.lon + ")";
        }

        public Point(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public final double getLat() {
            return this.lat;
        }

        public final double getLon() {
            return this.lon;
        }
    }

    /* compiled from: RecoveryVisitsWorker.kt */
    @Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B'\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0005¢\u0006\u0004\b\b\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0011\u001a\u00020\u0005HÆ\u0003J\t\u0010\u0012\u001a\u00020\u0005HÆ\u0003J\t\u0010\u0013\u001a\u00020\u0005HÆ\u0003J1\u0010\u0014\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u0005HÆ\u0001J\u0013\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0018\u001a\u00020\u0019HÖ\u0001J\t\u0010\u001a\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u0011\u0010\u0007\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\r¨\u0006\u001b"}, d2 = {"Lcom/nick/myrecoverytracker/RecoveryVisitsWorker$Place;", "", "name", "", "lat", "", "lon", "radiusM", "<init>", "(Ljava/lang/String;DDD)V", "getName", "()Ljava/lang/String;", "getLat", "()D", "getLon", "getRadiusM", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final /* data */ class Place {
        private final double lat;
        private final double lon;
        private final String name;
        private final double radiusM;

        public static /* synthetic */ Place copy$default(Place place, String str, double d, double d2, double d3, int i, Object obj) {
            if ((i & 1) != 0) {
                str = place.name;
            }
            if ((i & 2) != 0) {
                d = place.lat;
            }
            if ((i & 4) != 0) {
                d2 = place.lon;
            }
            if ((i & 8) != 0) {
                d3 = place.radiusM;
            }
            double d4 = d3;
            return place.copy(str, d, d2, d4);
        }

        /* renamed from: component1, reason: from getter */
        public final String getName() {
            return this.name;
        }

        /* renamed from: component2, reason: from getter */
        public final double getLat() {
            return this.lat;
        }

        /* renamed from: component3, reason: from getter */
        public final double getLon() {
            return this.lon;
        }

        /* renamed from: component4, reason: from getter */
        public final double getRadiusM() {
            return this.radiusM;
        }

        public final Place copy(String name, double lat, double lon, double radiusM) {
            Intrinsics.checkNotNullParameter(name, "name");
            return new Place(name, lat, lon, radiusM);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Place)) {
                return false;
            }
            Place place = (Place) other;
            return Intrinsics.areEqual(this.name, place.name) && Double.compare(this.lat, place.lat) == 0 && Double.compare(this.lon, place.lon) == 0 && Double.compare(this.radiusM, place.radiusM) == 0;
        }

        public int hashCode() {
            return (((((this.name.hashCode() * 31) + Double.hashCode(this.lat)) * 31) + Double.hashCode(this.lon)) * 31) + Double.hashCode(this.radiusM);
        }

        public String toString() {
            return "Place(name=" + this.name + ", lat=" + this.lat + ", lon=" + this.lon + ", radiusM=" + this.radiusM + ")";
        }

        public Place(String name, double lat, double lon, double radiusM) {
            Intrinsics.checkNotNullParameter(name, "name");
            this.name = name;
            this.lat = lat;
            this.lon = lon;
            this.radiusM = radiusM;
        }

        public final double getLat() {
            return this.lat;
        }

        public final double getLon() {
            return this.lon;
        }

        public final String getName() {
            return this.name;
        }

        public final double getRadiusM() {
            return this.radiusM;
        }
    }

    @Override // androidx.work.Worker
    public ListenableWorker.Result doWork() {
        boolean z;
        boolean z2;
        final String today = this.dayFmt.format(new Date());
        File filesDir = getApplicationContext().getFilesDir();
        File locFile = new File(filesDir, "location_log.csv");
        File outFile = new File(filesDir, "daily_recovery_visits.csv");
        File cfgFile = new File(filesDir, "participant_recovery_locations.json");
        List places = loadPlaces(cfgFile);
        if (places.isEmpty()) {
            Log.w(this.tag, "No recovery places configured (participant_recovery_locations.json missing or empty). Writing NO,0.");
            Intrinsics.checkNotNull(today);
            upsert(outFile, today, false, 0);
            ListenableWorker.Result resultSuccess = ListenableWorker.Result.success();
            Intrinsics.checkNotNullExpressionValue(resultSuccess, "success(...)");
            return resultSuccess;
        }
        if (!locFile.exists()) {
            Log.w(this.tag, "location_log.csv missing. Writing NO,0.");
            Intrinsics.checkNotNull(today);
            upsert(outFile, today, false, 0);
            ListenableWorker.Result resultSuccess2 = ListenableWorker.Result.success();
            Intrinsics.checkNotNullExpressionValue(resultSuccess2, "success(...)");
            return resultSuccess2;
        }
        final List todaysPoints = new ArrayList();
        boolean z3 = true;
        FilesKt.forEachLine$default(locFile, null, new Function1() { // from class: com.nick.myrecoverytracker.RecoveryVisitsWorker$$ExternalSyntheticLambda0
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return RecoveryVisitsWorker.doWork$lambda$0(today, todaysPoints, (String) obj);
            }
        }, 1, null);
        if (todaysPoints.isEmpty()) {
            Log.i(this.tag, "No points for " + today + ". Writing NO,0.");
            Intrinsics.checkNotNull(today);
            upsert(outFile, today, false, 0);
            ListenableWorker.Result resultSuccess3 = ListenableWorker.Result.success();
            Intrinsics.checkNotNullExpressionValue(resultSuccess3, "success(...)");
            return resultSuccess3;
        }
        Set visitedNames = new LinkedHashSet();
        for (Place p : places) {
            List list = todaysPoints;
            if ((list instanceof Collection) && list.isEmpty()) {
                z = z3;
                z2 = false;
            } else {
                Iterator it = list.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        z = z3;
                        z2 = false;
                        break;
                    }
                    z = z3;
                    if (within((Point) it.next(), p)) {
                        z2 = z;
                        break;
                    }
                    z3 = z;
                }
            }
            if (z2) {
                visitedNames.add(p.getName());
                z3 = z;
            } else {
                z3 = z;
            }
        }
        boolean visited = !visitedNames.isEmpty();
        int distinct = visitedNames.size();
        Log.i(this.tag, "Visited=" + visited + " distinct=" + distinct + " places=" + visitedNames);
        Intrinsics.checkNotNull(today);
        upsert(outFile, today, visited, distinct);
        ListenableWorker.Result resultSuccess4 = ListenableWorker.Result.success();
        Intrinsics.checkNotNullExpressionValue(resultSuccess4, "success(...)");
        return resultSuccess4;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit doWork$lambda$0(String $today, List $todaysPoints, String line) {
        Intrinsics.checkNotNullParameter(line, "line");
        Intrinsics.checkNotNull($today);
        if (StringsKt.startsWith$default(line, $today, false, 2, (Object) null)) {
            List parts = StringsKt.split$default((CharSequence) line, new char[]{','}, false, 0, 6, (Object) null);
            if (parts.size() >= 3) {
                Double lat = StringsKt.toDoubleOrNull((String) parts.get(1));
                Double lon = StringsKt.toDoubleOrNull((String) parts.get(2));
                if (lat != null && lon != null) {
                    $todaysPoints.add(new Point(lat.doubleValue(), lon.doubleValue()));
                }
            }
        }
        return Unit.INSTANCE;
    }

    private final boolean within(Point pt, Place place) {
        double d = haversineMeters(pt.getLat(), pt.getLon(), place.getLat(), place.getLon());
        return d <= place.getRadiusM();
    }

    private final double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double d = 2;
        double a = Math.pow(Math.sin(dLat / d), 2.0d) + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / d), 2.0d));
        double c = d * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371000.0d * c;
    }

    private final List<Place> loadPlaces(File cfg) {
        String str;
        String str2;
        String str3 = "radiusM";
        String str4 = "radius_m";
        if (!cfg.exists()) {
            return CollectionsKt.emptyList();
        }
        boolean z = true;
        try {
            String txt = StringsKt.trim((CharSequence) FilesKt.readText$default(cfg, null, 1, null)).toString();
            if (txt.length() != 0) {
                z = false;
            }
            if (z) {
                return CollectionsKt.emptyList();
            }
            JSONArray arr = new JSONArray(txt);
            List list = new ArrayList();
            int i = 0;
            int length = arr.length();
            while (i < length) {
                JSONObject o = arr.getJSONObject(i);
                Intrinsics.checkNotNullExpressionValue(o, "getJSONObject(...)");
                String name = o.optString("name", "place_" + i);
                double lat = o.optDouble("lat", Double.NaN);
                double lon = o.optDouble("lon", Double.NaN);
                String txt2 = txt;
                JSONArray arr2 = arr;
                double radius = 100.0d;
                if (o.has(str4)) {
                    radius = o.optDouble(str4, 100.0d);
                } else if (o.has(str3)) {
                    radius = o.optDouble(str3, 100.0d);
                }
                if (Double.isNaN(lat) || Double.isNaN(lon)) {
                    str = str3;
                    str2 = str4;
                } else {
                    Intrinsics.checkNotNull(name);
                    str = str3;
                    str2 = str4;
                    list.add(new Place(name, lat, lon, RangesKt.coerceAtLeast(radius, 10.0d)));
                }
                i++;
                txt = txt2;
                arr = arr2;
                str3 = str;
                str4 = str2;
            }
            return list;
        } catch (Throwable t) {
            Log.e(this.tag, "Failed to parse participant_recovery_locations.json", t);
            return CollectionsKt.emptyList();
        }
    }

    private final void upsert(File out, String day, boolean visited, int distinct) {
        List lines = out.exists() ? FilesKt.readLines$default(out, null, 1, null) : CollectionsKt.emptyList();
        Collection arrayList = new ArrayList();
        for (Object obj : lines) {
            if (!StringsKt.startsWith$default((String) obj, day + ",", false, 2, (Object) null)) {
                arrayList.add(obj);
            }
        }
        List keep = (List) arrayList;
        String yn = visited ? "YES" : "NO";
        String newLine = day + "," + yn + "," + distinct;
        FilesKt.writeText$default(out, CollectionsKt.joinToString$default(CollectionsKt.plus((Collection<? extends String>) keep, newLine), "\n", null, null, 0, null, null, 62, null) + "\n", null, 2, null);
    }
}
