package androidx.transition;

import java.util.Arrays;

/* loaded from: classes.dex */
class VelocityTracker1D {
    private static final int ASSUME_POINTER_MOVE_STOPPED_MILLIS = 40;
    private static final int HISTORY_SIZE = 20;
    private static final int HORIZON_MILLIS = 100;
    private long[] mTimeSamples = new long[20];
    private float[] mDataSamples = new float[20];
    private int mIndex = 0;

    VelocityTracker1D() {
        Arrays.fill(this.mTimeSamples, Long.MIN_VALUE);
    }

    public void addDataPoint(long timeMillis, float data) {
        this.mIndex = (this.mIndex + 1) % 20;
        this.mTimeSamples[this.mIndex] = timeMillis;
        this.mDataSamples[this.mIndex] = data;
    }

    public void resetTracking() {
        this.mIndex = 0;
        Arrays.fill(this.mTimeSamples, Long.MIN_VALUE);
        Arrays.fill(this.mDataSamples, 0.0f);
    }

    float calculateVelocity() {
        char c;
        int sampleCount;
        int sampleCount2 = 0;
        int index = this.mIndex;
        float f = 0.0f;
        if (index == 0 && this.mTimeSamples[index] == Long.MIN_VALUE) {
            return 0.0f;
        }
        long newestTime = this.mTimeSamples[index];
        long previousTime = newestTime;
        do {
            long sampleTime = this.mTimeSamples[index];
            c = 20;
            if (sampleTime == Long.MIN_VALUE) {
                break;
            }
            float age = newestTime - sampleTime;
            float delta = Math.abs(sampleTime - previousTime);
            previousTime = sampleTime;
            if (age > 100.0f || delta > 40.0f) {
                break;
            }
            index = (index == 0 ? 20 : index) - 1;
            sampleCount2++;
        } while (sampleCount2 < 20);
        if (sampleCount2 < 2) {
            return 0.0f;
        }
        float f2 = 1000.0f;
        if (sampleCount2 == 2) {
            int prevIndex = this.mIndex == 0 ? 19 : this.mIndex - 1;
            float timeDiff = this.mTimeSamples[this.mIndex] - this.mTimeSamples[prevIndex];
            if (timeDiff == 0.0f) {
                return 0.0f;
            }
            float dataDiff = this.mDataSamples[this.mIndex] - this.mDataSamples[prevIndex];
            return (dataDiff / timeDiff) * 1000.0f;
        }
        float work = 0.0f;
        int startIndex = (((this.mIndex - sampleCount2) + 20) + 1) % 20;
        int endIndex = ((this.mIndex + 1) + 20) % 20;
        long previousTime2 = this.mTimeSamples[startIndex];
        float previousData = this.mDataSamples[startIndex];
        int i = (startIndex + 1) % 20;
        while (i != endIndex) {
            long time = this.mTimeSamples[i];
            float f3 = f2;
            long timeDelta = time - previousTime2;
            char c2 = c;
            if (timeDelta == f) {
                sampleCount = sampleCount2;
            } else {
                float data = this.mDataSamples[i];
                float vPrev = kineticEnergyToVelocity(work);
                float dataPointsDelta = data - previousData;
                float vCurr = dataPointsDelta / timeDelta;
                work += (vCurr - vPrev) * Math.abs(vCurr);
                sampleCount = sampleCount2;
                int sampleCount3 = startIndex + 1;
                if (i == sampleCount3) {
                    work *= 0.5f;
                }
                previousTime2 = time;
                previousData = data;
            }
            i = (i + 1) % 20;
            f2 = f3;
            c = c2;
            sampleCount2 = sampleCount;
            f = 0.0f;
        }
        return kineticEnergyToVelocity(work) * f2;
    }

    private float kineticEnergyToVelocity(float kineticEnergy) {
        return (float) (Math.signum(kineticEnergy) * Math.sqrt(Math.abs(kineticEnergy) * 2.0f));
    }
}
