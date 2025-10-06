package androidx.work;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.lang.reflect.InvocationTargetException;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: InputMerger.kt */
@Metadata(d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0012\u0010\u0002\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0004\u001a\u00020\u0001H\u0000\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0005"}, d2 = {"TAG", "", "fromClassName", "Landroidx/work/InputMerger;", "className", "work-runtime_release"}, k = 2, mv = {1, 8, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes.dex */
public final class InputMergerKt {
    private static final String TAG;

    static {
        String strTagWithPrefix = Logger.tagWithPrefix("InputMerger");
        Intrinsics.checkNotNullExpressionValue(strTagWithPrefix, "tagWithPrefix(\"InputMerger\")");
        TAG = strTagWithPrefix;
    }

    public static final InputMerger fromClassName(String className) throws IllegalAccessException, InstantiationException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException {
        Intrinsics.checkNotNullParameter(className, "className");
        try {
            Class clazz = Class.forName(className);
            Object objNewInstance = clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            Intrinsics.checkNotNull(objNewInstance, "null cannot be cast to non-null type androidx.work.InputMerger");
            return (InputMerger) objNewInstance;
        } catch (Exception e) {
            Logger.get().error(TAG, "Trouble instantiating " + className, e);
            return null;
        }
    }
}
