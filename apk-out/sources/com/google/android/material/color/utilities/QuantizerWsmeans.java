package com.google.android.material.color.utilities;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/* loaded from: classes.dex */
public final class QuantizerWsmeans {
    private static final int MAX_ITERATIONS = 10;
    private static final double MIN_MOVEMENT_DISTANCE = 3.0d;

    private QuantizerWsmeans() {
    }

    private static final class Distance implements Comparable<Distance> {
        int index = -1;
        double distance = -1.0d;

        Distance() {
        }

        @Override // java.lang.Comparable
        public int compareTo(Distance other) {
            return Double.valueOf(this.distance).compareTo(Double.valueOf(other.distance));
        }
    }

    public static Map<Integer, Integer> quantize(int[] iArr, int[] iArr2, int i) {
        int[] iArr3;
        double[] dArr;
        double[] dArr2;
        Random random = new Random(272008L);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        double[][] dArr3 = new double[iArr.length][];
        int[] iArr4 = new int[iArr.length];
        PointProviderLab pointProviderLab = new PointProviderLab();
        int i2 = 0;
        for (int i3 : iArr) {
            Integer num = (Integer) linkedHashMap.get(Integer.valueOf(i3));
            if (num == null) {
                dArr3[i2] = pointProviderLab.fromInt(i3);
                iArr4[i2] = i3;
                i2++;
                linkedHashMap.put(Integer.valueOf(i3), 1);
            } else {
                linkedHashMap.put(Integer.valueOf(i3), Integer.valueOf(num.intValue() + 1));
            }
        }
        int[] iArr5 = new int[i2];
        for (int i4 = 0; i4 < i2; i4++) {
            iArr5[i4] = ((Integer) linkedHashMap.get(Integer.valueOf(iArr4[i4]))).intValue();
        }
        int iMin = Math.min(i, i2);
        if (iArr2.length != 0) {
            iMin = Math.min(iMin, iArr2.length);
        }
        double[][] dArr4 = new double[iMin][];
        int i5 = 0;
        for (int i6 = 0; i6 < iArr2.length; i6++) {
            dArr4[i6] = pointProviderLab.fromInt(iArr2[i6]);
            i5++;
        }
        int i7 = iMin - i5;
        if (i7 > 0) {
            for (int i8 = 0; i8 < i7; i8++) {
            }
        }
        int[] iArr6 = new int[i2];
        for (int i9 = 0; i9 < i2; i9++) {
            iArr6[i9] = random.nextInt(iMin);
        }
        int[][] iArr7 = new int[iMin][];
        int i10 = 0;
        while (i10 < iMin) {
            int i11 = i10;
            iArr7[i11] = new int[iMin];
            i10 = i11 + 1;
        }
        Distance[][] distanceArr = new Distance[iMin][];
        int i12 = 0;
        while (i12 < iMin) {
            int i13 = i12;
            distanceArr[i13] = new Distance[iMin];
            for (int i14 = 0; i14 < iMin; i14++) {
                distanceArr[i13][i14] = new Distance();
            }
            i12 = i13 + 1;
        }
        int[] iArr8 = new int[iMin];
        int i15 = 0;
        while (true) {
            Random random2 = random;
            if (i15 >= 10) {
                iArr3 = iArr8;
                break;
            }
            int i16 = 0;
            while (i16 < iMin) {
                int i17 = i15;
                int i18 = i16 + 1;
                while (i18 < iMin) {
                    LinkedHashMap linkedHashMap2 = linkedHashMap;
                    double dDistance = pointProviderLab.distance(dArr4[i16], dArr4[i18]);
                    distanceArr[i18][i16].distance = dDistance;
                    distanceArr[i18][i16].index = i16;
                    distanceArr[i16][i18].distance = dDistance;
                    distanceArr[i16][i18].index = i18;
                    i18++;
                    linkedHashMap = linkedHashMap2;
                    dArr3 = dArr3;
                    iArr4 = iArr4;
                }
                LinkedHashMap linkedHashMap3 = linkedHashMap;
                double[][] dArr5 = dArr3;
                int[] iArr9 = iArr4;
                Arrays.sort(distanceArr[i16]);
                for (int i19 = 0; i19 < iMin; i19++) {
                    iArr7[i16][i19] = distanceArr[i16][i19].index;
                }
                i16++;
                linkedHashMap = linkedHashMap3;
                i15 = i17;
                dArr3 = dArr5;
                iArr4 = iArr9;
            }
            int i20 = i15;
            LinkedHashMap linkedHashMap4 = linkedHashMap;
            double[][] dArr6 = dArr3;
            int[] iArr10 = iArr4;
            int i21 = 0;
            int i22 = 0;
            while (i22 < i2) {
                double[] dArr7 = dArr6[i22];
                int i23 = iArr6[i22];
                double[] dArr8 = dArr4[i23];
                double dDistance2 = pointProviderLab.distance(dArr7, dArr8);
                double d = dDistance2;
                int i24 = i21;
                int i25 = -1;
                int i26 = i22;
                int i27 = 0;
                while (i27 < iMin) {
                    int i28 = i27;
                    int i29 = i23;
                    double[] dArr9 = dArr8;
                    if (distanceArr[i23][i28].distance < 4.0d * dDistance2) {
                        double dDistance3 = pointProviderLab.distance(dArr7, dArr4[i28]);
                        if (dDistance3 < d) {
                            d = dDistance3;
                            i25 = i28;
                        }
                    }
                    i27 = i28 + 1;
                    i23 = i29;
                    dArr8 = dArr9;
                }
                if (i25 == -1 || Math.abs(Math.sqrt(d) - Math.sqrt(dDistance2)) <= 3.0d) {
                    i21 = i24;
                } else {
                    iArr6[i26] = i25;
                    i21 = i24 + 1;
                }
                i22 = i26 + 1;
            }
            if (i21 == 0 && i20 != 0) {
                iArr3 = iArr8;
                break;
            }
            double[] dArr10 = new double[iMin];
            double[] dArr11 = new double[iMin];
            double[] dArr12 = new double[iMin];
            boolean z = false;
            Arrays.fill(iArr8, 0);
            int i30 = 0;
            while (i30 < i2) {
                int i31 = iArr6[i30];
                double[] dArr13 = dArr6[i30];
                boolean z2 = z;
                int i32 = iArr5[i30];
                iArr8[i31] = iArr8[i31] + i32;
                double[] dArr14 = dArr10;
                dArr14[i31] = dArr10[i31] + (dArr13[z2 ? 1 : 0] * i32);
                dArr11[i31] = dArr11[i31] + (dArr13[1] * i32);
                dArr12[i31] = dArr12[i31] + (dArr13[2] * i32);
                i30++;
                z = z2 ? 1 : 0;
                iArr8 = iArr8;
                dArr10 = dArr14;
            }
            int[] iArr11 = iArr8;
            double[] dArr15 = dArr10;
            boolean z3 = z;
            int i33 = 0;
            while (i33 < iMin) {
                int i34 = iArr11[i33];
                if (i34 == 0) {
                    dArr4[i33] = new double[]{0.0d, 0.0d, 0.0d};
                    dArr = dArr11;
                    dArr2 = dArr12;
                } else {
                    dArr = dArr11;
                    dArr2 = dArr12;
                    double d2 = dArr15[i33] / i34;
                    double d3 = dArr[i33] / i34;
                    double d4 = dArr2[i33] / i34;
                    dArr4[i33][z3 ? 1 : 0] = d2;
                    dArr4[i33][1] = d3;
                    dArr4[i33][2] = d4;
                }
                i33++;
                dArr11 = dArr;
                dArr12 = dArr2;
            }
            i15 = i20 + 1;
            random = random2;
            linkedHashMap = linkedHashMap4;
            dArr3 = dArr6;
            iArr4 = iArr10;
            iArr8 = iArr11;
        }
        LinkedHashMap linkedHashMap5 = new LinkedHashMap();
        for (int i35 = 0; i35 < iMin; i35++) {
            int i36 = iArr3[i35];
            if (i36 != 0) {
                int i37 = pointProviderLab.toInt(dArr4[i35]);
                if (!linkedHashMap5.containsKey(Integer.valueOf(i37))) {
                    linkedHashMap5.put(Integer.valueOf(i37), Integer.valueOf(i36));
                }
            }
        }
        return linkedHashMap5;
    }
}
