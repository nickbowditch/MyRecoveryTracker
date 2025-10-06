package com.google.gson.internal.bind;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.C$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.internal.ReflectionAccessFilterHelper;
import com.google.gson.internal.reflect.ReflectionHelper;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;
    private final Excluder excluder;
    private final FieldNamingStrategy fieldNamingPolicy;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;
    private final List<ReflectionAccessFilter> reflectionFilters;

    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor, FieldNamingStrategy fieldNamingPolicy, Excluder excluder, JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory, List<ReflectionAccessFilter> reflectionFilters) {
        this.constructorConstructor = constructorConstructor;
        this.fieldNamingPolicy = fieldNamingPolicy;
        this.excluder = excluder;
        this.jsonAdapterFactory = jsonAdapterFactory;
        this.reflectionFilters = reflectionFilters;
    }

    private boolean includeField(Field f, boolean serialize) {
        return (this.excluder.excludeClass(f.getType(), serialize) || this.excluder.excludeField(f, serialize)) ? false : true;
    }

    private List<String> getFieldNames(Field f) {
        SerializedName annotation = (SerializedName) f.getAnnotation(SerializedName.class);
        if (annotation == null) {
            String name = this.fieldNamingPolicy.translateName(f);
            return Collections.singletonList(name);
        }
        String serializedName = annotation.value();
        String[] alternates = annotation.alternate();
        if (alternates.length == 0) {
            return Collections.singletonList(serializedName);
        }
        List<String> fieldNames = new ArrayList<>(alternates.length + 1);
        fieldNames.add(serializedName);
        Collections.addAll(fieldNames, alternates);
        return fieldNames;
    }

    @Override // com.google.gson.TypeAdapterFactory
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) throws NoSuchMethodException, SecurityException {
        Class<? super T> raw = type.getRawType();
        if (!Object.class.isAssignableFrom(raw)) {
            return null;
        }
        ReflectionAccessFilter.FilterResult filterResult = ReflectionAccessFilterHelper.getFilterResult(this.reflectionFilters, raw);
        if (filterResult == ReflectionAccessFilter.FilterResult.BLOCK_ALL) {
            throw new JsonIOException("ReflectionAccessFilter does not permit using reflection for " + raw + ". Register a TypeAdapter for this type or adjust the access filter.");
        }
        boolean blockInaccessible = filterResult == ReflectionAccessFilter.FilterResult.BLOCK_INACCESSIBLE;
        if (ReflectionHelper.isRecord(raw)) {
            TypeAdapter<T> adapter = new RecordAdapter<>(raw, getBoundFields(gson, type, raw, blockInaccessible, true), blockInaccessible);
            return adapter;
        }
        ObjectConstructor<T> constructor = this.constructorConstructor.get(type);
        return new FieldReflectionAdapter(constructor, getBoundFields(gson, type, raw, blockInaccessible, false));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <M extends AccessibleObject & Member> void checkAccessible(Object object, M member) {
        if (!ReflectionAccessFilterHelper.canAccess(member, Modifier.isStatic(member.getModifiers()) ? null : object)) {
            String memberDescription = ReflectionHelper.getAccessibleObjectDescription(member, true);
            throw new JsonIOException(memberDescription + " is not accessible and ReflectionAccessFilter does not permit making it accessible. Register a TypeAdapter for the declaring type, adjust the access filter or increase the visibility of the element and its declaring type.");
        }
    }

    private BoundField createBoundField(final Gson context, Field field, final Method accessor, String name, final TypeToken<?> fieldType, boolean serialize, boolean deserialize, final boolean blockInaccessible) {
        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
        int modifiers = field.getModifiers();
        final boolean isStaticFinalField = Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
        JsonAdapter annotation = (JsonAdapter) field.getAnnotation(JsonAdapter.class);
        TypeAdapter<?> mapped = null;
        if (annotation != null) {
            mapped = this.jsonAdapterFactory.getTypeAdapter(this.constructorConstructor, context, fieldType, annotation);
        }
        final boolean jsonAdapterPresent = mapped != null;
        if (mapped == null) {
            mapped = context.getAdapter(fieldType);
        }
        final TypeAdapter<?> mapped2 = mapped;
        return new BoundField(name, field, serialize, deserialize) { // from class: com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.1
            @Override // com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.BoundField
            void write(JsonWriter writer, Object source) throws IllegalAccessException, IOException, IllegalArgumentException, InvocationTargetException {
                Object fieldValue;
                if (this.serialized) {
                    if (blockInaccessible) {
                        if (accessor == null) {
                            ReflectiveTypeAdapterFactory.checkAccessible(source, this.field);
                        } else {
                            ReflectiveTypeAdapterFactory.checkAccessible(source, accessor);
                        }
                    }
                    if (accessor != null) {
                        try {
                            fieldValue = accessor.invoke(source, new Object[0]);
                        } catch (InvocationTargetException e) {
                            String accessorDescription = ReflectionHelper.getAccessibleObjectDescription(accessor, false);
                            throw new JsonIOException("Accessor " + accessorDescription + " threw exception", e.getCause());
                        }
                    } else {
                        fieldValue = this.field.get(source);
                    }
                    if (fieldValue == source) {
                        return;
                    }
                    writer.name(this.name);
                    TypeAdapter<Object> t = jsonAdapterPresent ? mapped2 : new TypeAdapterRuntimeTypeWrapper<>(context, mapped2, fieldType.getType());
                    t.write(writer, fieldValue);
                }
            }

            @Override // com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.BoundField
            void readIntoArray(JsonReader reader, int index, Object[] target) throws JsonParseException, IOException {
                Object fieldValue = mapped2.read(reader);
                if (fieldValue == null && isPrimitive) {
                    throw new JsonParseException("null is not allowed as value for record component '" + this.fieldName + "' of primitive type; at path " + reader.getPath());
                }
                target[index] = fieldValue;
            }

            @Override // com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.BoundField
            void readIntoField(JsonReader reader, Object target) throws IllegalAccessException, IOException, IllegalArgumentException {
                Object fieldValue = mapped2.read(reader);
                if (fieldValue != null || !isPrimitive) {
                    if (blockInaccessible) {
                        ReflectiveTypeAdapterFactory.checkAccessible(target, this.field);
                    } else if (isStaticFinalField) {
                        String fieldDescription = ReflectionHelper.getAccessibleObjectDescription(this.field, false);
                        throw new JsonIOException("Cannot set value of 'static final' " + fieldDescription);
                    }
                    this.field.set(target, fieldValue);
                }
            }
        };
    }

    private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw, boolean blockInaccessible, boolean isRecord) throws JsonIOException, SecurityException {
        boolean blockInaccessible2;
        boolean deserialize;
        Class<?> originalRaw;
        TypeToken<?> type2;
        int i;
        int i2;
        ReflectiveTypeAdapterFactory reflectiveTypeAdapterFactory = this;
        Map<String, BoundField> result = new LinkedHashMap<>();
        if (raw.isInterface()) {
            return result;
        }
        Class<?> originalRaw2 = raw;
        TypeToken<?> type3 = type;
        Class<?> raw2 = raw;
        boolean blockInaccessible3 = blockInaccessible;
        while (raw2 != Object.class) {
            Field[] fields = raw2.getDeclaredFields();
            boolean z = true;
            boolean z2 = false;
            if (raw2 != originalRaw2 && fields.length > 0) {
                ReflectionAccessFilter.FilterResult filterResult = ReflectionAccessFilterHelper.getFilterResult(reflectiveTypeAdapterFactory.reflectionFilters, raw2);
                if (filterResult == ReflectionAccessFilter.FilterResult.BLOCK_ALL) {
                    throw new JsonIOException("ReflectionAccessFilter does not permit using reflection for " + raw2 + " (supertype of " + originalRaw2 + "). Register a TypeAdapter for this type or adjust the access filter.");
                }
                blockInaccessible2 = filterResult == ReflectionAccessFilter.FilterResult.BLOCK_INACCESSIBLE;
            } else {
                blockInaccessible2 = blockInaccessible3;
            }
            int length = fields.length;
            int i3 = 0;
            while (i3 < length) {
                int i4 = i3;
                Field field = fields[i4];
                boolean serialize = reflectiveTypeAdapterFactory.includeField(field, z);
                boolean deserialize2 = reflectiveTypeAdapterFactory.includeField(field, z2);
                if (!serialize && !deserialize2) {
                    i = length;
                    i2 = i4;
                    originalRaw = originalRaw2;
                    type2 = type3;
                } else {
                    Method accessor = null;
                    if (!isRecord) {
                        deserialize = deserialize2;
                    } else if (Modifier.isStatic(field.getModifiers())) {
                        deserialize = false;
                    } else {
                        accessor = ReflectionHelper.getAccessor(raw2, field);
                        if (!blockInaccessible2) {
                            ReflectionHelper.makeAccessible(accessor);
                        }
                        if (accessor.getAnnotation(SerializedName.class) != null && field.getAnnotation(SerializedName.class) == null) {
                            String methodDescription = ReflectionHelper.getAccessibleObjectDescription(accessor, z2);
                            throw new JsonIOException("@SerializedName on " + methodDescription + " is not supported");
                        }
                        deserialize = deserialize2;
                    }
                    if (!blockInaccessible2 && accessor == null) {
                        ReflectionHelper.makeAccessible(field);
                    }
                    Type fieldType = C$Gson$Types.resolve(type3.getType(), raw2, field.getGenericType());
                    List<String> fieldNames = reflectiveTypeAdapterFactory.getFieldNames(field);
                    int size = fieldNames.size();
                    originalRaw = originalRaw2;
                    BoundField previous = null;
                    type2 = type3;
                    int i5 = 0;
                    while (i5 < size) {
                        String name = fieldNames.get(i5);
                        if (i5 != 0) {
                            serialize = false;
                        }
                        List<String> fieldNames2 = fieldNames;
                        int i6 = i4;
                        Method accessor2 = accessor;
                        boolean serialize2 = serialize;
                        int i7 = length;
                        BoundField boundField = reflectiveTypeAdapterFactory.createBoundField(context, field, accessor2, name, TypeToken.get(fieldType), serialize2, deserialize, blockInaccessible2);
                        BoundField replaced = result.put(name, boundField);
                        if (previous == null) {
                            previous = replaced;
                        }
                        i5++;
                        reflectiveTypeAdapterFactory = this;
                        serialize = serialize2;
                        length = i7;
                        fieldNames = fieldNames2;
                        accessor = accessor2;
                        i4 = i6;
                    }
                    i = length;
                    i2 = i4;
                    if (previous != null) {
                        throw new IllegalArgumentException("Class " + originalRaw.getName() + " declares multiple JSON fields named '" + previous.name + "'; conflict is caused by fields " + ReflectionHelper.fieldToString(previous.field) + " and " + ReflectionHelper.fieldToString(field));
                    }
                }
                i3 = i2 + 1;
                reflectiveTypeAdapterFactory = this;
                type3 = type2;
                originalRaw2 = originalRaw;
                length = i;
                z = true;
                z2 = false;
            }
            type3 = TypeToken.get(C$Gson$Types.resolve(type3.getType(), raw2, raw2.getGenericSuperclass()));
            raw2 = type3.getRawType();
            reflectiveTypeAdapterFactory = this;
            blockInaccessible3 = blockInaccessible2;
        }
        return result;
    }

    static abstract class BoundField {
        final boolean deserialized;
        final Field field;
        final String fieldName;
        final String name;
        final boolean serialized;

        abstract void readIntoArray(JsonReader jsonReader, int i, Object[] objArr) throws JsonParseException, IOException;

        abstract void readIntoField(JsonReader jsonReader, Object obj) throws IllegalAccessException, IOException;

        abstract void write(JsonWriter jsonWriter, Object obj) throws IllegalAccessException, IOException;

        protected BoundField(String name, Field field, boolean serialized, boolean deserialized) {
            this.name = name;
            this.field = field;
            this.fieldName = field.getName();
            this.serialized = serialized;
            this.deserialized = deserialized;
        }
    }

    public static abstract class Adapter<T, A> extends TypeAdapter<T> {
        final Map<String, BoundField> boundFields;

        abstract A createAccumulator();

        abstract T finalize(A a);

        abstract void readField(A a, JsonReader jsonReader, BoundField boundField) throws IllegalAccessException, IOException;

        Adapter(Map<String, BoundField> boundFields) {
            this.boundFields = boundFields;
        }

        @Override // com.google.gson.TypeAdapter
        public void write(JsonWriter out, T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            try {
                for (BoundField boundField : this.boundFields.values()) {
                    boundField.write(out, value);
                }
                out.endObject();
            } catch (IllegalAccessException e) {
                throw ReflectionHelper.createExceptionForUnexpectedIllegalAccess(e);
            }
        }

        @Override // com.google.gson.TypeAdapter
        public T read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            A accumulator = createAccumulator();
            try {
                in.beginObject();
                while (in.hasNext()) {
                    String name = in.nextName();
                    BoundField field = this.boundFields.get(name);
                    if (field == null || !field.deserialized) {
                        in.skipValue();
                    } else {
                        readField(accumulator, in, field);
                    }
                }
                in.endObject();
                return finalize(accumulator);
            } catch (IllegalAccessException e) {
                throw ReflectionHelper.createExceptionForUnexpectedIllegalAccess(e);
            } catch (IllegalStateException e2) {
                throw new JsonSyntaxException(e2);
            }
        }
    }

    private static final class FieldReflectionAdapter<T> extends Adapter<T, T> {
        private final ObjectConstructor<T> constructor;

        FieldReflectionAdapter(ObjectConstructor<T> constructor, Map<String, BoundField> boundFields) {
            super(boundFields);
            this.constructor = constructor;
        }

        @Override // com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter
        T createAccumulator() {
            return this.constructor.construct();
        }

        @Override // com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter
        void readField(T accumulator, JsonReader in, BoundField field) throws IllegalAccessException, IOException {
            field.readIntoField(in, accumulator);
        }

        @Override // com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter
        T finalize(T accumulator) {
            return accumulator;
        }
    }

    private static final class RecordAdapter<T> extends Adapter<T, Object[]> {
        static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = primitiveDefaults();
        private final Map<String, Integer> componentIndices;
        private final Constructor<T> constructor;
        private final Object[] constructorArgsDefaults;

        RecordAdapter(Class<T> raw, Map<String, BoundField> boundFields, boolean blockInaccessible) throws JsonIOException, SecurityException {
            super(boundFields);
            this.componentIndices = new HashMap();
            this.constructor = ReflectionHelper.getCanonicalRecordConstructor(raw);
            if (blockInaccessible) {
                ReflectiveTypeAdapterFactory.checkAccessible(null, this.constructor);
            } else {
                ReflectionHelper.makeAccessible(this.constructor);
            }
            String[] componentNames = ReflectionHelper.getRecordComponentNames(raw);
            for (int i = 0; i < componentNames.length; i++) {
                this.componentIndices.put(componentNames[i], Integer.valueOf(i));
            }
            Class<?>[] parameterTypes = this.constructor.getParameterTypes();
            this.constructorArgsDefaults = new Object[parameterTypes.length];
            for (int i2 = 0; i2 < parameterTypes.length; i2++) {
                this.constructorArgsDefaults[i2] = PRIMITIVE_DEFAULTS.get(parameterTypes[i2]);
            }
        }

        private static Map<Class<?>, Object> primitiveDefaults() {
            Map<Class<?>, Object> zeroes = new HashMap<>();
            zeroes.put(Byte.TYPE, (byte) 0);
            zeroes.put(Short.TYPE, (short) 0);
            zeroes.put(Integer.TYPE, 0);
            zeroes.put(Long.TYPE, 0L);
            zeroes.put(Float.TYPE, Float.valueOf(0.0f));
            zeroes.put(Double.TYPE, Double.valueOf(0.0d));
            zeroes.put(Character.TYPE, (char) 0);
            zeroes.put(Boolean.TYPE, false);
            return zeroes;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter
        public Object[] createAccumulator() {
            return (Object[]) this.constructorArgsDefaults.clone();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter
        public void readField(Object[] accumulator, JsonReader in, BoundField field) throws JsonParseException, IOException {
            Integer componentIndex = this.componentIndices.get(field.fieldName);
            if (componentIndex == null) {
                throw new IllegalStateException("Could not find the index in the constructor '" + ReflectionHelper.constructorToString(this.constructor) + "' for field with name '" + field.fieldName + "', unable to determine which argument in the constructor the field corresponds to. This is unexpected behavior, as we expect the RecordComponents to have the same names as the fields in the Java class, and that the order of the RecordComponents is the same as the order of the canonical constructor parameters.");
            }
            field.readIntoArray(in, componentIndex.intValue(), accumulator);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter
        public T finalize(Object[] accumulator) {
            try {
                return this.constructor.newInstance(accumulator);
            } catch (IllegalAccessException e) {
                throw ReflectionHelper.createExceptionForUnexpectedIllegalAccess(e);
            } catch (IllegalArgumentException e2) {
                e = e2;
                throw new RuntimeException("Failed to invoke constructor '" + ReflectionHelper.constructorToString(this.constructor) + "' with args " + Arrays.toString(accumulator), e);
            } catch (InstantiationException e3) {
                e = e3;
                throw new RuntimeException("Failed to invoke constructor '" + ReflectionHelper.constructorToString(this.constructor) + "' with args " + Arrays.toString(accumulator), e);
            } catch (InvocationTargetException e4) {
                throw new RuntimeException("Failed to invoke constructor '" + ReflectionHelper.constructorToString(this.constructor) + "' with args " + Arrays.toString(accumulator), e4.getCause());
            }
        }
    }
}
