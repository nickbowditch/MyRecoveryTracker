package androidx.work;

import android.app.Notification;

/* loaded from: classes.dex */
public final class ForegroundInfo {
    private final int mForegroundServiceType;
    private final Notification mNotification;
    private final int mNotificationId;

    public ForegroundInfo(int notificationId, Notification notification) {
        this(notificationId, notification, 0);
    }

    public ForegroundInfo(int notificationId, Notification notification, int foregroundServiceType) {
        this.mNotificationId = notificationId;
        this.mNotification = notification;
        this.mForegroundServiceType = foregroundServiceType;
    }

    public int getNotificationId() {
        return this.mNotificationId;
    }

    public int getForegroundServiceType() {
        return this.mForegroundServiceType;
    }

    public Notification getNotification() {
        return this.mNotification;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ForegroundInfo that = (ForegroundInfo) o;
        if (this.mNotificationId != that.mNotificationId || this.mForegroundServiceType != that.mForegroundServiceType) {
            return false;
        }
        return this.mNotification.equals(that.mNotification);
    }

    public int hashCode() {
        int result = this.mNotificationId;
        return (((result * 31) + this.mForegroundServiceType) * 31) + this.mNotification.hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ForegroundInfo{");
        sb.append("mNotificationId=").append(this.mNotificationId);
        sb.append(", mForegroundServiceType=").append(this.mForegroundServiceType);
        sb.append(", mNotification=").append(this.mNotification);
        sb.append('}');
        return sb.toString();
    }
}
