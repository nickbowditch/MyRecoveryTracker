package androidx.work;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Data;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: OverwritingInputMerger.kt */
@Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006H\u0016¨\u0006\u0007"}, d2 = {"Landroidx/work/OverwritingInputMerger;", "Landroidx/work/InputMerger;", "()V", "merge", "Landroidx/work/Data;", "inputs", "", "work-runtime_release"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class OverwritingInputMerger extends InputMerger {
    @Override // androidx.work.InputMerger
    public Data merge(List<Data> inputs) throws IOException {
        Intrinsics.checkNotNullParameter(inputs, "inputs");
        Data.Builder output = new Data.Builder();
        Map mergedValues = new LinkedHashMap();
        for (Data input : inputs) {
            Map<String, Object> keyValueMap = input.getKeyValueMap();
            Intrinsics.checkNotNullExpressionValue(keyValueMap, "input.keyValueMap");
            mergedValues.putAll(keyValueMap);
        }
        output.putAll((Map<String, Object>) mergedValues);
        Data dataBuild = output.build();
        Intrinsics.checkNotNullExpressionValue(dataBuild, "output.build()");
        return dataBuild;
    }
}
