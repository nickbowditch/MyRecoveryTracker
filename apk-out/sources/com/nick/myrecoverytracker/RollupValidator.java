package com.nick.myrecoverytracker;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.IntRange;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: RollupValidator.kt */
@Metadata(d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0000\n\u0002\u0010$\n\u0000\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007J\u0016\u0010\b\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\nJB\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\r\u001a\u00020\n2\u0006\u0010\u000e\u001a\u00020\u00052\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\u00102\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00010\u0012H\u0002¨\u0006\u0013"}, d2 = {"Lcom/nick/myrecoverytracker/RollupValidator;", "", "<init>", "()V", "validateUnlocks", "", "context", "Landroid/content/Context;", "validate", "fileName", "", "writeQa", "", "feature", "pass", "reasons", "", "stats", "", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class RollupValidator {
    public static final RollupValidator INSTANCE = new RollupValidator();

    private RollupValidator() {
    }

    public final boolean validateUnlocks(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        return validate(context, "daily_unlocks.csv");
    }

    public final boolean validate(Context context, String fileName) throws JSONException {
        int i;
        boolean pass;
        String str;
        HashMap uniq;
        int badCols;
        int badTypes;
        int badRange;
        int i2;
        List reasons;
        boolean pass2;
        Regex dateRe;
        boolean pass3;
        String str2;
        int i3;
        HashMap uniq2;
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(fileName, "fileName");
        File f = new File(context.getFilesDir(), fileName);
        if (!f.exists()) {
            return false;
        }
        List lines = FilesKt.readLines$default(f, null, 1, null);
        String str3 = ".csv";
        if (lines.isEmpty()) {
            writeQa(context, StringsKt.removeSuffix(fileName, (CharSequence) ".csv"), false, CollectionsKt.listOf("EMPTY_FILE"), MapsKt.emptyMap());
            return false;
        }
        String header = StringsKt.trim((CharSequence) CollectionsKt.first(lines)).toString();
        List cols = StringsKt.split$default((CharSequence) header, new char[]{','}, false, 0, 6, (Object) null);
        int dateIdx = cols.indexOf("date");
        int countIdx = cols.contains("daily_unlocks") ? cols.indexOf("daily_unlocks") : cols.contains("unlocks") ? cols.indexOf("unlocks") : -1;
        int schemaIdx = cols.indexOf("feature_schema_version");
        boolean pass4 = true;
        List reasons2 = new ArrayList();
        int rows = 0;
        int badCols2 = 0;
        int badTypes2 = 0;
        int badRange2 = 0;
        HashMap uniq3 = new HashMap();
        Regex dateRe2 = new Regex("^\\d{4}-\\d{2}-\\d{2}$");
        IntRange intRange = new IntRange(0, 2000);
        if (dateIdx < 0 || countIdx < 0) {
            i = 0;
            reasons2.add("BAD_HEADER(" + header + ")");
            pass4 = false;
        } else {
            i = 0;
        }
        if (pass4) {
            int i4 = 0;
            for (String str4 : CollectionsKt.drop(lines, 1)) {
                if (StringsKt.isBlank(str4)) {
                    dateRe = dateRe2;
                    pass3 = pass4;
                    str2 = str3;
                    i3 = i4;
                    uniq2 = uniq3;
                } else {
                    rows++;
                    pass3 = pass4;
                    char[] cArr = new char[1];
                    cArr[i] = ',';
                    List listSplit$default = StringsKt.split$default((CharSequence) str4, cArr, false, 0, 6, (Object) null);
                    str2 = str3;
                    Integer[] numArr = new Integer[2];
                    numArr[i] = Integer.valueOf(dateIdx);
                    numArr[1] = Integer.valueOf(countIdx);
                    Integer num = (Integer) CollectionsKt.maxOrNull((Iterable) CollectionsKt.listOf((Object[]) numArr));
                    if (listSplit$default.size() <= (num != null ? num.intValue() : -1)) {
                        badCols2++;
                        dateRe = dateRe2;
                        i3 = i4;
                        uniq2 = uniq3;
                    } else {
                        String string = StringsKt.trim((CharSequence) listSplit$default.get(dateIdx)).toString();
                        if (!dateRe2.matches(string)) {
                            badTypes2++;
                        }
                        Integer intOrNull = StringsKt.toIntOrNull(StringsKt.trim((CharSequence) listSplit$default.get(countIdx)).toString());
                        if (intOrNull == null) {
                            badTypes2++;
                            dateRe = dateRe2;
                        } else {
                            dateRe = dateRe2;
                            if (!intRange.contains(intOrNull.intValue())) {
                                badRange2++;
                            }
                        }
                        if (schemaIdx >= 0 && schemaIdx < listSplit$default.size()) {
                            StringsKt.trim((CharSequence) listSplit$default.get(schemaIdx)).toString();
                        }
                        HashMap map = uniq3;
                        i3 = i4;
                        uniq2 = uniq3;
                        Integer num2 = (Integer) uniq2.get(string);
                        map.put(string, Integer.valueOf((num2 != null ? num2.intValue() : i) + 1));
                    }
                }
                uniq3 = uniq2;
                str3 = str2;
                dateRe2 = dateRe;
                i4 = i3;
                pass4 = pass3;
            }
            pass = pass4;
            str = str3;
            uniq = uniq3;
            badCols = badCols2;
            badTypes = badTypes2;
            badRange = badRange2;
        } else {
            pass = pass4;
            str = ".csv";
            uniq = uniq3;
            badCols = 0;
            badTypes = 0;
            badRange = 0;
        }
        Iterable iterableValues = uniq.values();
        Intrinsics.checkNotNullExpressionValue(iterableValues, "<get-values>(...)");
        Iterable iterable = iterableValues;
        if (((Collection) iterable).isEmpty()) {
            i2 = i;
        } else {
            i2 = 0;
            Iterator it = iterable.iterator();
            while (it.hasNext()) {
                IntRange intRange2 = intRange;
                Iterable iterable2 = iterable;
                if ((((Integer) it.next()).intValue() > 1 ? 1 : i) != 0) {
                    i2++;
                    if (i2 < 0) {
                        CollectionsKt.throwCountOverflow();
                    }
                    intRange = intRange2;
                    iterable = iterable2;
                } else {
                    intRange = intRange2;
                    iterable = iterable2;
                }
            }
        }
        int dupCount = i2;
        if (dupCount > 0) {
            pass2 = false;
            reasons = reasons2;
            reasons2.add("DUP_KEYS=" + dupCount);
        } else {
            reasons = reasons2;
            pass2 = pass;
        }
        if (badCols > 0) {
            pass2 = false;
            reasons.add("BAD_COLS=" + badCols);
        }
        if (badTypes > 0) {
            pass2 = false;
            reasons.add("BAD_TYPES=" + badTypes);
        }
        if (badRange > 0) {
            pass2 = false;
            reasons.add("OUT_OF_RANGE=" + badRange);
        }
        boolean pass5 = pass2;
        String today = LocalDate.now(ZoneId.systemDefault()).toString();
        Intrinsics.checkNotNullExpressionValue(today, "toString(...)");
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Map.Entry entry : uniq.entrySet()) {
            int badCols3 = badCols;
            if (Intrinsics.areEqual((String) entry.getKey(), today)) {
                linkedHashMap.put(entry.getKey(), entry.getValue());
                badTypes = badTypes;
                badCols = badCols3;
                badRange = badRange;
            } else {
                badCols = badCols3;
                badRange = badRange;
            }
        }
        int badCols4 = badCols;
        int badTypes3 = badTypes;
        int todayRows = CollectionsKt.sumOfInt(linkedHashMap.values());
        Pair[] pairArr = new Pair[7];
        pairArr[i] = TuplesKt.to("rows", Integer.valueOf(rows));
        pairArr[1] = TuplesKt.to("dup_keys", Integer.valueOf(dupCount));
        pairArr[2] = TuplesKt.to("bad_cols", Integer.valueOf(badCols4));
        pairArr[3] = TuplesKt.to("bad_types", Integer.valueOf(badTypes3));
        pairArr[4] = TuplesKt.to("bad_range", Integer.valueOf(badRange));
        pairArr[5] = TuplesKt.to("today_rows", Integer.valueOf(todayRows));
        pairArr[6] = TuplesKt.to("header", header);
        Map stats = MapsKt.mapOf(pairArr);
        writeQa(context, StringsKt.removeSuffix(fileName, (CharSequence) str), pass5, reasons, stats);
        return pass5;
    }

    private final void writeQa(Context context, String feature, boolean pass, List<String> reasons, Map<String, ? extends Object> stats) throws JSONException {
        File dir = context.getFilesDir();
        if (dir == null) {
            return;
        }
        String day = LocalDate.now(ZoneId.systemDefault()).toString();
        Intrinsics.checkNotNullExpressionValue(day, "toString(...)");
        File file = new File(dir, "qa/" + day);
        file.mkdirs();
        File out = new File(file, feature + ".json");
        JSONObject obj = new JSONObject();
        obj.put("pass", pass);
        obj.put("reasons", new JSONArray((Collection) reasons));
        JSONObject jSONObject = new JSONObject();
        for (Map.Entry entry : stats.entrySet()) {
            jSONObject.put(entry.getKey(), entry.getValue().toString());
            dir = dir;
            day = day;
        }
        obj.put("stats", jSONObject);
        String string = obj.toString();
        Intrinsics.checkNotNullExpressionValue(string, "toString(...)");
        FilesKt.writeText$default(out, string, null, 2, null);
    }
}
