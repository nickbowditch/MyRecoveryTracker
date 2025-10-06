package androidx.room.util;

import android.database.Cursor;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.io.CloseableKt;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/* compiled from: FtsTableInfo.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\"\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0007\u0018\u0000 \u00102\u00020\u0001:\u0001\u0010B%\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003¢\u0006\u0002\u0010\u0007B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u0012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005¢\u0006\u0002\u0010\tJ\u0013\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u0001H\u0096\u0002J\b\u0010\r\u001a\u00020\u000eH\u0016J\b\u0010\u000f\u001a\u00020\u0003H\u0016R\u0016\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u00058\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u0010\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004¢\u0006\u0002\n\u0000R\u0016\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00030\u00058\u0006X\u0087\u0004¢\u0006\u0002\n\u0000¨\u0006\u0011"}, d2 = {"Landroidx/room/util/FtsTableInfo;", "", "name", "", "columns", "", "createSql", "(Ljava/lang/String;Ljava/util/Set;Ljava/lang/String;)V", "options", "(Ljava/lang/String;Ljava/util/Set;Ljava/util/Set;)V", "equals", "", "other", "hashCode", "", "toString", "Companion", "room-runtime_release"}, k = 1, mv = {1, 7, 1}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class FtsTableInfo {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final String[] FTS_OPTIONS = {"tokenize=", "compress=", "content=", "languageid=", "matchinfo=", "notindexed=", "order=", "prefix=", "uncompress="};
    public final Set<String> columns;
    public final String name;
    public final Set<String> options;

    @JvmStatic
    public static final Set<String> parseOptions(String str) {
        return INSTANCE.parseOptions(str);
    }

    @JvmStatic
    public static final FtsTableInfo read(SupportSQLiteDatabase supportSQLiteDatabase, String str) {
        return INSTANCE.read(supportSQLiteDatabase, str);
    }

    public FtsTableInfo(String name, Set<String> columns, Set<String> options) {
        Intrinsics.checkNotNullParameter(name, "name");
        Intrinsics.checkNotNullParameter(columns, "columns");
        Intrinsics.checkNotNullParameter(options, "options");
        this.name = name;
        this.columns = columns;
        this.options = options;
    }

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public FtsTableInfo(String name, Set<String> columns, String createSql) {
        this(name, columns, INSTANCE.parseOptions(createSql));
        Intrinsics.checkNotNullParameter(name, "name");
        Intrinsics.checkNotNullParameter(columns, "columns");
        Intrinsics.checkNotNullParameter(createSql, "createSql");
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if ((other instanceof FtsTableInfo) && Intrinsics.areEqual(this.name, ((FtsTableInfo) other).name) && Intrinsics.areEqual(this.columns, ((FtsTableInfo) other).columns)) {
            return Intrinsics.areEqual(this.options, ((FtsTableInfo) other).options);
        }
        return false;
    }

    public int hashCode() {
        int result = this.name.hashCode();
        return (((result * 31) + this.columns.hashCode()) * 31) + this.options.hashCode();
    }

    public String toString() {
        return "FtsTableInfo{name='" + this.name + "', columns=" + this.columns + ", options=" + this.options + "'}";
    }

    /* compiled from: FtsTableInfo.kt */
    @Metadata(d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0016\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00050\b2\u0006\u0010\t\u001a\u00020\u0005H\u0007J\u0018\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u0005H\u0007J\u001e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00050\b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u0005H\u0002J\u001e\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00050\b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u0005H\u0002R\u0016\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0006¨\u0006\u0011"}, d2 = {"Landroidx/room/util/FtsTableInfo$Companion;", "", "()V", "FTS_OPTIONS", "", "", "[Ljava/lang/String;", "parseOptions", "", "createStatement", "read", "Landroidx/room/util/FtsTableInfo;", "database", "Landroidx/sqlite/db/SupportSQLiteDatabase;", "tableName", "readColumns", "readOptions", "room-runtime_release"}, k = 1, mv = {1, 7, 1}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @JvmStatic
        public final FtsTableInfo read(SupportSQLiteDatabase database, String tableName) {
            Intrinsics.checkNotNullParameter(database, "database");
            Intrinsics.checkNotNullParameter(tableName, "tableName");
            Set columns = readColumns(database, tableName);
            Set options = readOptions(database, tableName);
            return new FtsTableInfo(tableName, (Set<String>) columns, (Set<String>) options);
        }

        private final Set<String> readColumns(SupportSQLiteDatabase database, String tableName) {
            Set $this$readColumns_u24lambda_u241 = SetsKt.createSetBuilder();
            Cursor $this$useCursor$iv = database.query("PRAGMA table_info(`" + tableName + "`)");
            Cursor cursor = $this$useCursor$iv;
            try {
                Cursor cursor2 = cursor;
                if (cursor2.getColumnCount() > 0) {
                    int nameIndex = cursor2.getColumnIndex("name");
                    while (cursor2.moveToNext()) {
                        String string = cursor2.getString(nameIndex);
                        Intrinsics.checkNotNullExpressionValue(string, "cursor.getString(nameIndex)");
                        $this$readColumns_u24lambda_u241.add(string);
                    }
                }
                Unit unit = Unit.INSTANCE;
                CloseableKt.closeFinally(cursor, null);
                return SetsKt.build($this$readColumns_u24lambda_u241);
            } finally {
            }
        }

        private final Set<String> readOptions(SupportSQLiteDatabase database, String tableName) {
            String sql;
            Cursor $this$useCursor$iv = database.query("SELECT * FROM sqlite_master WHERE `name` = '" + tableName + '\'');
            Cursor cursor = $this$useCursor$iv;
            try {
                Cursor cursor2 = cursor;
                if (cursor2.moveToFirst()) {
                    sql = cursor2.getString(cursor2.getColumnIndexOrThrow("sql"));
                } else {
                    sql = "";
                }
                CloseableKt.closeFinally(cursor, null);
                Intrinsics.checkNotNullExpressionValue(sql, "sql");
                return parseOptions(sql);
            } finally {
            }
        }

        @JvmStatic
        public final Set<String> parseOptions(String createStatement) {
            String argsString;
            int $i$f$filter;
            Iterable $this$filterTo$iv$iv;
            boolean z;
            String str;
            int endIndex$iv$iv;
            Character ch;
            Intrinsics.checkNotNullParameter(createStatement, "createStatement");
            boolean z2 = true;
            if (createStatement.length() == 0) {
                return SetsKt.emptySet();
            }
            String argsString2 = createStatement.substring(StringsKt.indexOf$default((CharSequence) createStatement, '(', 0, false, 6, (Object) null) + 1, StringsKt.lastIndexOf$default((CharSequence) createStatement, ')', 0, false, 6, (Object) null));
            String str2 = "this as java.lang.String…ing(startIndex, endIndex)";
            Intrinsics.checkNotNullExpressionValue(argsString2, "this as java.lang.String…ing(startIndex, endIndex)");
            List args = new ArrayList();
            ArrayDeque quoteStack = new ArrayDeque();
            int lastDelimiterIndex = -1;
            String $this$forEachIndexed$iv = argsString2;
            int index$iv = 0;
            int i = 0;
            while (i < $this$forEachIndexed$iv.length()) {
                char item$iv = $this$forEachIndexed$iv.charAt(i);
                int index$iv2 = index$iv + 1;
                boolean z3 = z2;
                if ((((item$iv != '\'' && item$iv != '\"') ? false : z3) || item$iv == '`') ? z3 : false) {
                    if (quoteStack.isEmpty()) {
                        quoteStack.push(Character.valueOf(item$iv));
                        str = str2;
                    } else {
                        Character ch2 = (Character) quoteStack.peek();
                        if (ch2 == null || ch2.charValue() != item$iv) {
                            str = str2;
                        } else {
                            quoteStack.pop();
                            str = str2;
                        }
                    }
                } else if (item$iv == '[') {
                    if (!quoteStack.isEmpty()) {
                        str = str2;
                    } else {
                        quoteStack.push(Character.valueOf(item$iv));
                        str = str2;
                    }
                } else if (item$iv == ']') {
                    if (quoteStack.isEmpty() || (ch = (Character) quoteStack.peek()) == null || ch.charValue() != '[') {
                        str = str2;
                    } else {
                        quoteStack.pop();
                        str = str2;
                    }
                } else if (item$iv != ',' || !quoteStack.isEmpty()) {
                    str = str2;
                } else {
                    CharSequence $this$trim$iv = argsString2.substring(lastDelimiterIndex + 1, index$iv);
                    Intrinsics.checkNotNullExpressionValue($this$trim$iv, str2);
                    CharSequence $this$trim$iv$iv = $this$trim$iv;
                    int endIndex$iv$iv2 = $this$trim$iv$iv.length() - 1;
                    boolean startFound$iv$iv = false;
                    int startIndex$iv$iv = 0;
                    int $i$f$trim = endIndex$iv$iv2;
                    while (true) {
                        if (startIndex$iv$iv > $i$f$trim) {
                            endIndex$iv$iv = $i$f$trim;
                            str = str2;
                            break;
                        }
                        int index$iv$iv = !startFound$iv$iv ? startIndex$iv$iv : $i$f$trim;
                        endIndex$iv$iv = $i$f$trim;
                        str = str2;
                        char it = $this$trim$iv$iv.charAt(index$iv$iv);
                        boolean match$iv$iv = Intrinsics.compare((int) it, 32) <= 0 ? z3 : false;
                        if (!startFound$iv$iv) {
                            if (!match$iv$iv) {
                                startFound$iv$iv = true;
                                $i$f$trim = endIndex$iv$iv;
                                str2 = str;
                            } else {
                                startIndex$iv$iv++;
                                $i$f$trim = endIndex$iv$iv;
                                str2 = str;
                            }
                        } else {
                            if (!match$iv$iv) {
                                break;
                            }
                            $i$f$trim = endIndex$iv$iv - 1;
                            str2 = str;
                        }
                    }
                    args.add($this$trim$iv$iv.subSequence(startIndex$iv$iv, endIndex$iv$iv + 1).toString());
                    lastDelimiterIndex = index$iv;
                }
                i++;
                index$iv = index$iv2;
                z2 = z3;
                str2 = str;
            }
            boolean z4 = z2;
            String strSubstring = argsString2.substring(lastDelimiterIndex + 1);
            Intrinsics.checkNotNullExpressionValue(strSubstring, "this as java.lang.String).substring(startIndex)");
            args.add(StringsKt.trim((CharSequence) strSubstring).toString());
            List $this$filter$iv = args;
            int $i$f$filter2 = 0;
            Collection destination$iv$iv = new ArrayList();
            Iterable $this$filterTo$iv$iv2 = $this$filter$iv;
            for (Object element$iv$iv : $this$filterTo$iv$iv2) {
                String arg = (String) element$iv$iv;
                String[] strArr = FtsTableInfo.FTS_OPTIONS;
                int length = strArr.length;
                Iterable $this$filter$iv2 = $this$filter$iv;
                int i2 = 0;
                while (true) {
                    if (i2 < length) {
                        int i3 = i2;
                        argsString = argsString2;
                        $i$f$filter = $i$f$filter2;
                        $this$filterTo$iv$iv = $this$filterTo$iv$iv2;
                        if (StringsKt.startsWith$default(arg, strArr[i2], false, 2, (Object) null)) {
                            z = z4;
                            break;
                        }
                        i2 = i3 + 1;
                        $this$filterTo$iv$iv2 = $this$filterTo$iv$iv;
                        argsString2 = argsString;
                        $i$f$filter2 = $i$f$filter;
                    } else {
                        argsString = argsString2;
                        $i$f$filter = $i$f$filter2;
                        $this$filterTo$iv$iv = $this$filterTo$iv$iv2;
                        z = false;
                        break;
                    }
                }
                if (z) {
                    destination$iv$iv.add(element$iv$iv);
                }
                $this$filterTo$iv$iv2 = $this$filterTo$iv$iv;
                $this$filter$iv = $this$filter$iv2;
                argsString2 = argsString;
                $i$f$filter2 = $i$f$filter;
            }
            Set options = CollectionsKt.toSet((List) destination$iv$iv);
            return options;
        }
    }
}
