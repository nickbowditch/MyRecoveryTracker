package androidx.work;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public final class Data {
    public static final int MAX_DATA_BYTES = 10240;
    Map<String, Object> mValues;
    private static final String TAG = Logger.tagWithPrefix("Data");
    public static final Data EMPTY = new Builder().build();

    Data() {
    }

    public Data(Data other) {
        this.mValues = new HashMap(other.mValues);
    }

    public Data(Map<String, ?> values) {
        this.mValues = new HashMap(values);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = this.mValues.get(key);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return defaultValue;
    }

    public boolean[] getBooleanArray(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof Boolean[]) {
            Boolean[] array = (Boolean[]) value;
            return convertToPrimitiveArray(array);
        }
        return null;
    }

    public byte getByte(String key, byte defaultValue) {
        Object value = this.mValues.get(key);
        if (value instanceof Byte) {
            return ((Byte) value).byteValue();
        }
        return defaultValue;
    }

    public byte[] getByteArray(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof Byte[]) {
            Byte[] array = (Byte[]) value;
            return convertToPrimitiveArray(array);
        }
        return null;
    }

    public int getInt(String key, int defaultValue) {
        Object value = this.mValues.get(key);
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        return defaultValue;
    }

    public int[] getIntArray(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof Integer[]) {
            Integer[] array = (Integer[]) value;
            return convertToPrimitiveArray(array);
        }
        return null;
    }

    public long getLong(String key, long defaultValue) {
        Object value = this.mValues.get(key);
        if (value instanceof Long) {
            return ((Long) value).longValue();
        }
        return defaultValue;
    }

    public long[] getLongArray(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof Long[]) {
            Long[] array = (Long[]) value;
            return convertToPrimitiveArray(array);
        }
        return null;
    }

    public float getFloat(String key, float defaultValue) {
        Object value = this.mValues.get(key);
        if (value instanceof Float) {
            return ((Float) value).floatValue();
        }
        return defaultValue;
    }

    public float[] getFloatArray(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof Float[]) {
            Float[] array = (Float[]) value;
            return convertToPrimitiveArray(array);
        }
        return null;
    }

    public double getDouble(String key, double defaultValue) {
        Object value = this.mValues.get(key);
        if (value instanceof Double) {
            return ((Double) value).doubleValue();
        }
        return defaultValue;
    }

    public double[] getDoubleArray(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof Double[]) {
            Double[] array = (Double[]) value;
            return convertToPrimitiveArray(array);
        }
        return null;
    }

    public String getString(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    public String[] getStringArray(String key) {
        Object value = this.mValues.get(key);
        if (value instanceof String[]) {
            return (String[]) value;
        }
        return null;
    }

    public Map<String, Object> getKeyValueMap() {
        return Collections.unmodifiableMap(this.mValues);
    }

    public byte[] toByteArray() {
        return toByteArrayInternal(this);
    }

    public <T> boolean hasKeyWithValueOfType(String key, Class<T> klass) {
        Object value = this.mValues.get(key);
        return value != null && klass.isAssignableFrom(value.getClass());
    }

    public int size() {
        return this.mValues.size();
    }

    public static byte[] toByteArrayInternal(Data data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            try {
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeInt(data.size());
                for (Map.Entry<String, Object> entry : data.mValues.entrySet()) {
                    objectOutputStream.writeUTF(entry.getKey());
                    objectOutputStream.writeObject(entry.getValue());
                }
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error in Data#toByteArray: ", e);
                }
                try {
                    outputStream.close();
                } catch (IOException e2) {
                    Log.e(TAG, "Error in Data#toByteArray: ", e2);
                }
                if (outputStream.size() <= 10240) {
                    return outputStream.toByteArray();
                }
                throw new IllegalStateException("Data cannot occupy more than 10240 bytes when serialized");
            } catch (IOException e3) {
                Log.e(TAG, "Error in Data#toByteArray: ", e3);
                byte[] byteArray = outputStream.toByteArray();
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "Error in Data#toByteArray: ", e4);
                    }
                }
                try {
                    outputStream.close();
                } catch (IOException e5) {
                    Log.e(TAG, "Error in Data#toByteArray: ", e5);
                }
                return byteArray;
            }
        } finally {
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r0v3, types: [androidx.work.Data] */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:17:0x003c -> B:31:0x005d). Please report as a decompilation issue!!! */
    public static Data fromByteArray(byte[] bytes) throws IOException {
        String data = "Error in Data#fromByteArray: ";
        if (bytes.length > 10240) {
            throw new IllegalStateException("Data cannot occupy more than 10240 bytes when serialized");
        }
        Map<String, Object> map = new HashMap<>();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = null;
        try {
            try {
                try {
                    objectInputStream = new ObjectInputStream(inputStream);
                    for (int i = objectInputStream.readInt(); i > 0; i--) {
                        map.put(objectInputStream.readUTF(), objectInputStream.readObject());
                    }
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error in Data#fromByteArray: ", e);
                    }
                    inputStream.close();
                } finally {
                }
            } catch (IOException | ClassNotFoundException e2) {
                Log.e(TAG, "Error in Data#fromByteArray: ", e2);
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "Error in Data#fromByteArray: ", e3);
                    }
                }
                inputStream.close();
            }
        } catch (IOException e4) {
            Log.e(TAG, data, e4);
        }
        data = new Data((Map<String, ?>) map);
        return data;
    }

    public boolean equals(Object o) {
        boolean equal;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Data other = (Data) o;
        Set<String> keys = this.mValues.keySet();
        if (!keys.equals(other.mValues.keySet())) {
            return false;
        }
        for (String key : keys) {
            Object value = this.mValues.get(key);
            Object otherValue = other.mValues.get(key);
            if (value == null || otherValue == null) {
                if (value == otherValue) {
                    equal = true;
                } else {
                    equal = false;
                }
            } else if ((value instanceof Object[]) && (otherValue instanceof Object[])) {
                equal = Arrays.deepEquals((Object[]) value, (Object[]) otherValue);
            } else {
                equal = value.equals(otherValue);
            }
            if (!equal) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return this.mValues.hashCode() * 31;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Data {");
        if (!this.mValues.isEmpty()) {
            for (String key : this.mValues.keySet()) {
                sb.append(key).append(" : ");
                Object value = this.mValues.get(key);
                if (value instanceof Object[]) {
                    sb.append(Arrays.toString((Object[]) value));
                } else {
                    sb.append(value);
                }
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public static Boolean[] convertPrimitiveBooleanArray(boolean[] value) {
        Boolean[] returnValue = new Boolean[value.length];
        for (int i = 0; i < value.length; i++) {
            returnValue[i] = Boolean.valueOf(value[i]);
        }
        return returnValue;
    }

    public static Byte[] convertPrimitiveByteArray(byte[] value) {
        Byte[] returnValue = new Byte[value.length];
        for (int i = 0; i < value.length; i++) {
            returnValue[i] = Byte.valueOf(value[i]);
        }
        return returnValue;
    }

    public static Integer[] convertPrimitiveIntArray(int[] value) {
        Integer[] returnValue = new Integer[value.length];
        for (int i = 0; i < value.length; i++) {
            returnValue[i] = Integer.valueOf(value[i]);
        }
        return returnValue;
    }

    public static Long[] convertPrimitiveLongArray(long[] value) {
        Long[] returnValue = new Long[value.length];
        for (int i = 0; i < value.length; i++) {
            returnValue[i] = Long.valueOf(value[i]);
        }
        return returnValue;
    }

    public static Float[] convertPrimitiveFloatArray(float[] value) {
        Float[] returnValue = new Float[value.length];
        for (int i = 0; i < value.length; i++) {
            returnValue[i] = Float.valueOf(value[i]);
        }
        return returnValue;
    }

    public static Double[] convertPrimitiveDoubleArray(double[] value) {
        Double[] returnValue = new Double[value.length];
        for (int i = 0; i < value.length; i++) {
            returnValue[i] = Double.valueOf(value[i]);
        }
        return returnValue;
    }

    public static boolean[] convertToPrimitiveArray(Boolean[] array) {
        boolean[] returnArray = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            returnArray[i] = array[i].booleanValue();
        }
        return returnArray;
    }

    public static byte[] convertToPrimitiveArray(Byte[] array) {
        byte[] returnArray = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            returnArray[i] = array[i].byteValue();
        }
        return returnArray;
    }

    public static int[] convertToPrimitiveArray(Integer[] array) {
        int[] returnArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            returnArray[i] = array[i].intValue();
        }
        return returnArray;
    }

    public static long[] convertToPrimitiveArray(Long[] array) {
        long[] returnArray = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            returnArray[i] = array[i].longValue();
        }
        return returnArray;
    }

    public static float[] convertToPrimitiveArray(Float[] array) {
        float[] returnArray = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            returnArray[i] = array[i].floatValue();
        }
        return returnArray;
    }

    public static double[] convertToPrimitiveArray(Double[] array) {
        double[] returnArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            returnArray[i] = array[i].doubleValue();
        }
        return returnArray;
    }

    public static final class Builder {
        private Map<String, Object> mValues = new HashMap();

        public Builder putBoolean(String key, boolean value) {
            this.mValues.put(key, Boolean.valueOf(value));
            return this;
        }

        public Builder putBooleanArray(String key, boolean[] value) {
            this.mValues.put(key, Data.convertPrimitiveBooleanArray(value));
            return this;
        }

        public Builder putByte(String key, byte value) {
            this.mValues.put(key, Byte.valueOf(value));
            return this;
        }

        public Builder putByteArray(String key, byte[] value) {
            this.mValues.put(key, Data.convertPrimitiveByteArray(value));
            return this;
        }

        public Builder putInt(String key, int value) {
            this.mValues.put(key, Integer.valueOf(value));
            return this;
        }

        public Builder putIntArray(String key, int[] value) {
            this.mValues.put(key, Data.convertPrimitiveIntArray(value));
            return this;
        }

        public Builder putLong(String key, long value) {
            this.mValues.put(key, Long.valueOf(value));
            return this;
        }

        public Builder putLongArray(String key, long[] value) {
            this.mValues.put(key, Data.convertPrimitiveLongArray(value));
            return this;
        }

        public Builder putFloat(String key, float value) {
            this.mValues.put(key, Float.valueOf(value));
            return this;
        }

        public Builder putFloatArray(String key, float[] value) {
            this.mValues.put(key, Data.convertPrimitiveFloatArray(value));
            return this;
        }

        public Builder putDouble(String key, double value) {
            this.mValues.put(key, Double.valueOf(value));
            return this;
        }

        public Builder putDoubleArray(String key, double[] value) {
            this.mValues.put(key, Data.convertPrimitiveDoubleArray(value));
            return this;
        }

        public Builder putString(String key, String value) {
            this.mValues.put(key, value);
            return this;
        }

        public Builder putStringArray(String key, String[] value) {
            this.mValues.put(key, value);
            return this;
        }

        public Builder putAll(Data data) {
            putAll(data.mValues);
            return this;
        }

        public Builder putAll(Map<String, Object> values) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                put(key, value);
            }
            return this;
        }

        public Builder put(String key, Object value) {
            if (value == null) {
                this.mValues.put(key, null);
            } else {
                Class<?> valueType = value.getClass();
                if (valueType == Boolean.class || valueType == Byte.class || valueType == Integer.class || valueType == Long.class || valueType == Float.class || valueType == Double.class || valueType == String.class || valueType == Boolean[].class || valueType == Byte[].class || valueType == Integer[].class || valueType == Long[].class || valueType == Float[].class || valueType == Double[].class || valueType == String[].class) {
                    this.mValues.put(key, value);
                } else if (valueType == boolean[].class) {
                    this.mValues.put(key, Data.convertPrimitiveBooleanArray((boolean[]) value));
                } else if (valueType == byte[].class) {
                    this.mValues.put(key, Data.convertPrimitiveByteArray((byte[]) value));
                } else if (valueType == int[].class) {
                    this.mValues.put(key, Data.convertPrimitiveIntArray((int[]) value));
                } else if (valueType == long[].class) {
                    this.mValues.put(key, Data.convertPrimitiveLongArray((long[]) value));
                } else if (valueType == float[].class) {
                    this.mValues.put(key, Data.convertPrimitiveFloatArray((float[]) value));
                } else if (valueType == double[].class) {
                    this.mValues.put(key, Data.convertPrimitiveDoubleArray((double[]) value));
                } else {
                    throw new IllegalArgumentException("Key " + key + " has invalid type " + valueType);
                }
            }
            return this;
        }

        public Data build() throws IOException {
            Data data = new Data((Map<String, ?>) this.mValues);
            Data.toByteArrayInternal(data);
            return data;
        }
    }
}
