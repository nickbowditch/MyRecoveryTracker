package com.google.android.gms.location;

import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.util.DefaultClock;
import com.google.android.gms.internal.location.zzdh;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public interface Geofence {
    public static final int GEOFENCE_TRANSITION_DWELL = 4;
    public static final int GEOFENCE_TRANSITION_ENTER = 1;
    public static final int GEOFENCE_TRANSITION_EXIT = 2;
    public static final long NEVER_EXPIRE = -1;

    /* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
    public static final class Builder {
        private double zze;
        private double zzf;
        private float zzg;
        private String zza = null;
        private int zzb = 0;
        private long zzc = Long.MIN_VALUE;
        private short zzd = -1;
        private int zzh = 0;
        private int zzi = -1;

        public Geofence build() {
            if (this.zza == null) {
                throw new IllegalArgumentException("Request ID not set.");
            }
            int i = this.zzb;
            if (i == 0) {
                throw new IllegalArgumentException("Transitions types not set.");
            }
            if ((i & 4) != 0 && this.zzi < 0) {
                throw new IllegalArgumentException("Non-negative loitering delay needs to be set when transition types include GEOFENCE_TRANSITION_DWELL.");
            }
            if (this.zzc == Long.MIN_VALUE) {
                throw new IllegalArgumentException("Expiration not set.");
            }
            if (this.zzd == -1) {
                throw new IllegalArgumentException("Geofence region not set.");
            }
            if (this.zzh >= 0) {
                return new zzdh(this.zza, this.zzb, (short) 1, this.zze, this.zzf, this.zzg, this.zzc, this.zzh, this.zzi);
            }
            throw new IllegalArgumentException("Notification responsiveness should be nonnegative.");
        }

        public Builder setCircularRegion(double latitude, double longitude, float radius) {
            Preconditions.checkArgument(latitude >= -90.0d && latitude <= 90.0d, "Invalid latitude: " + latitude);
            Preconditions.checkArgument(longitude >= -180.0d && longitude <= 180.0d, "Invalid longitude: " + longitude);
            Preconditions.checkArgument(radius > 0.0f, "Invalid radius: " + radius);
            this.zzd = (short) 1;
            this.zze = latitude;
            this.zzf = longitude;
            this.zzg = radius;
            return this;
        }

        public Builder setExpirationDuration(long durationMillis) {
            if (durationMillis < 0) {
                this.zzc = -1L;
            } else {
                this.zzc = DefaultClock.getInstance().elapsedRealtime() + durationMillis;
            }
            return this;
        }

        public Builder setLoiteringDelay(int i) {
            this.zzi = i;
            return this;
        }

        public Builder setNotificationResponsiveness(int i) {
            this.zzh = i;
            return this;
        }

        public Builder setRequestId(String requestId) {
            this.zza = (String) Preconditions.checkNotNull(requestId, "Request ID can't be set to null");
            return this;
        }

        public Builder setTransitionTypes(int i) {
            this.zzb = i;
            return this;
        }
    }

    /* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
    public @interface GeofenceTransition {
    }

    /* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
    public @interface TransitionTypes {
    }

    long getExpirationTime();

    double getLatitude();

    int getLoiteringDelay();

    double getLongitude();

    int getNotificationResponsiveness();

    float getRadius();

    String getRequestId();

    int getTransitionTypes();
}
