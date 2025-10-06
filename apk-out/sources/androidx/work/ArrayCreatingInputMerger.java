package androidx.work;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.work.Data;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ArrayCreatingInputMerger.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J&\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00042\b\u0010\u0006\u001a\u0004\u0018\u00010\u00042\n\u0010\u0007\u001a\u0006\u0012\u0002\b\u00030\bH\u0002J\u0018\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\u0004H\u0002J\u001e\u0010\f\u001a\u00020\u00042\b\u0010\u0006\u001a\u0004\u0018\u00010\u00042\n\u0010\u0007\u001a\u0006\u0012\u0002\b\u00030\bH\u0002J\u0016\u0010\r\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0010H\u0016¨\u0006\u0011"}, d2 = {"Landroidx/work/ArrayCreatingInputMerger;", "Landroidx/work/InputMerger;", "()V", "concatenateArrayAndNonArray", "", "array", "obj", "valueClass", "Ljava/lang/Class;", "concatenateArrays", "array1", "array2", "createArrayFor", "merge", "Landroidx/work/Data;", "inputs", "", "work-runtime_release"}, k = 1, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class ArrayCreatingInputMerger extends InputMerger {
    @Override // androidx.work.InputMerger
    public Data merge(List<Data> inputs) throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException, NegativeArraySizeException {
        Class valueClass;
        Object objConcatenateArrayAndNonArray;
        Intrinsics.checkNotNullParameter(inputs, "inputs");
        Data.Builder output = new Data.Builder();
        Map mergedValues = new HashMap();
        for (Data input : inputs) {
            Map<String, Object> keyValueMap = input.getKeyValueMap();
            Intrinsics.checkNotNullExpressionValue(keyValueMap, "input.keyValueMap");
            for (Map.Entry<String, Object> entry : keyValueMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value == null || (valueClass = value.getClass()) == null) {
                    valueClass = String.class;
                }
                Object existingValue = mergedValues.get(key);
                Intrinsics.checkNotNullExpressionValue(key, "key");
                if (existingValue == null) {
                    if (valueClass.isArray()) {
                        objConcatenateArrayAndNonArray = value;
                    } else {
                        objConcatenateArrayAndNonArray = createArrayFor(value, valueClass);
                    }
                } else {
                    Class existingValueClass = existingValue.getClass();
                    if (Intrinsics.areEqual(existingValueClass, valueClass)) {
                        Intrinsics.checkNotNullExpressionValue(value, "value");
                        objConcatenateArrayAndNonArray = concatenateArrays(existingValue, value);
                    } else if (Intrinsics.areEqual(existingValueClass.getComponentType(), valueClass)) {
                        objConcatenateArrayAndNonArray = concatenateArrayAndNonArray(existingValue, value, valueClass);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                Intrinsics.checkNotNullExpressionValue(objConcatenateArrayAndNonArray, "if (existingValue == nul…      }\n                }");
                mergedValues.put(key, objConcatenateArrayAndNonArray);
            }
        }
        output.putAll((Map<String, Object>) mergedValues);
        Data dataBuild = output.build();
        Intrinsics.checkNotNullExpressionValue(dataBuild, "output.build()");
        return dataBuild;
    }

    private final Object concatenateArrays(Object array1, Object array2) throws NegativeArraySizeException {
        int length1 = Array.getLength(array1);
        int length2 = Array.getLength(array2);
        Class<?> componentType = array1.getClass().getComponentType();
        Intrinsics.checkNotNull(componentType);
        Object newArray = Array.newInstance(componentType, length1 + length2);
        System.arraycopy(array1, 0, newArray, 0, length1);
        System.arraycopy(array2, 0, newArray, length1, length2);
        Intrinsics.checkNotNullExpressionValue(newArray, "newArray");
        return newArray;
    }

    private final Object concatenateArrayAndNonArray(Object array, Object obj, Class<?> valueClass) throws ArrayIndexOutOfBoundsException, IllegalArgumentException, NegativeArraySizeException {
        int arrayLength = Array.getLength(array);
        Object newArray = Array.newInstance(valueClass, arrayLength + 1);
        System.arraycopy(array, 0, newArray, 0, arrayLength);
        Array.set(newArray, arrayLength, obj);
        Intrinsics.checkNotNullExpressionValue(newArray, "newArray");
        return newArray;
    }

    private final Object createArrayFor(Object obj, Class<?> valueClass) throws ArrayIndexOutOfBoundsException, IllegalArgumentException, NegativeArraySizeException {
        Object newArray = Array.newInstance(valueClass, 1);
        Array.set(newArray, 0, obj);
        Intrinsics.checkNotNullExpressionValue(newArray, "newArray");
        return newArray;
    }
}
