package androidx.work.impl.utils;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.work.WorkInfo;
import androidx.work.WorkQuery;
import androidx.work.impl.model.WorkTypeConverters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: RawQueries.kt */
@Metadata(d1 = {"\u0000\"\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\u001a\u001c\u0010\u0000\u001a\u00020\u00012\n\u0010\u0002\u001a\u00060\u0003j\u0002`\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002\u001a\n\u0010\u0007\u001a\u00020\b*\u00020\t¨\u0006\n"}, d2 = {"bindings", "", "builder", "Ljava/lang/StringBuilder;", "Lkotlin/text/StringBuilder;", "count", "", "toRawQuery", "Landroidx/sqlite/db/SupportSQLiteQuery;", "Landroidx/work/WorkQuery;", "work-runtime_release"}, k = 2, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class RawQueries {
    public static final SupportSQLiteQuery toRawQuery(WorkQuery $this$toRawQuery) {
        Intrinsics.checkNotNullParameter($this$toRawQuery, "<this>");
        List arguments = new ArrayList();
        StringBuilder builder = new StringBuilder("SELECT * FROM workspec");
        String conjunction = " WHERE";
        List<WorkInfo.State> states = $this$toRawQuery.getStates();
        Intrinsics.checkNotNullExpressionValue(states, "states");
        if (!states.isEmpty()) {
            Iterable states2 = $this$toRawQuery.getStates();
            Intrinsics.checkNotNullExpressionValue(states2, "states");
            Iterable $this$map$iv = states2;
            Collection destination$iv$iv = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
            for (Object item$iv$iv : $this$map$iv) {
                WorkInfo.State it = (WorkInfo.State) item$iv$iv;
                Intrinsics.checkNotNull(it);
                destination$iv$iv.add(Integer.valueOf(WorkTypeConverters.stateToInt(it)));
            }
            List stateIds = (List) destination$iv$iv;
            builder.append(" WHERE state IN (");
            bindings(builder, stateIds.size());
            builder.append(")");
            arguments.addAll(stateIds);
            conjunction = " AND";
        }
        List<UUID> ids = $this$toRawQuery.getIds();
        Intrinsics.checkNotNullExpressionValue(ids, "ids");
        if (!ids.isEmpty()) {
            Iterable ids2 = $this$toRawQuery.getIds();
            Intrinsics.checkNotNullExpressionValue(ids2, "ids");
            Iterable $this$map$iv2 = ids2;
            Collection destination$iv$iv2 = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv2, 10));
            for (Object item$iv$iv2 : $this$map$iv2) {
                destination$iv$iv2.add(((UUID) item$iv$iv2).toString());
            }
            List workSpecIds = (List) destination$iv$iv2;
            builder.append(conjunction + " id IN (");
            bindings(builder, $this$toRawQuery.getIds().size());
            builder.append(")");
            arguments.addAll(workSpecIds);
            conjunction = " AND";
        }
        List<String> tags = $this$toRawQuery.getTags();
        Intrinsics.checkNotNullExpressionValue(tags, "tags");
        if (!tags.isEmpty()) {
            builder.append(conjunction + " id IN (SELECT work_spec_id FROM worktag WHERE tag IN (");
            bindings(builder, $this$toRawQuery.getTags().size());
            builder.append("))");
            List<String> tags2 = $this$toRawQuery.getTags();
            Intrinsics.checkNotNullExpressionValue(tags2, "tags");
            arguments.addAll(tags2);
            conjunction = " AND";
        }
        List<String> uniqueWorkNames = $this$toRawQuery.getUniqueWorkNames();
        Intrinsics.checkNotNullExpressionValue(uniqueWorkNames, "uniqueWorkNames");
        if (!uniqueWorkNames.isEmpty()) {
            builder.append(conjunction + " id IN (SELECT work_spec_id FROM workname WHERE name IN (");
            bindings(builder, $this$toRawQuery.getUniqueWorkNames().size());
            builder.append("))");
            List<String> uniqueWorkNames2 = $this$toRawQuery.getUniqueWorkNames();
            Intrinsics.checkNotNullExpressionValue(uniqueWorkNames2, "uniqueWorkNames");
            arguments.addAll(uniqueWorkNames2);
        }
        builder.append(";");
        String string = builder.toString();
        Intrinsics.checkNotNullExpressionValue(string, "builder.toString()");
        List $this$toTypedArray$iv = arguments;
        return new SimpleSQLiteQuery(string, $this$toTypedArray$iv.toArray(new Object[0]));
    }

    private static final void bindings(StringBuilder builder, int count) {
        if (count <= 0) {
            return;
        }
        ArrayList arrayList = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            arrayList.add("?");
        }
        builder.append(CollectionsKt.joinToString$default(arrayList, ",", null, null, 0, null, null, 62, null));
    }
}
