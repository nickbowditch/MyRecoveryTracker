package com.nick.myrecoverytracker;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.io.CloseableKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/* compiled from: NotificationListener.kt */
@Metadata(d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\f\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u0007¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0016J\"\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0016J\u0010\u0010\u0013\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u0012H\u0002J8\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\b2\u0006\u0010\u0017\u001a\u00020\b2\u0006\u0010\u0018\u001a\u00020\b2\u0006\u0010\u0019\u001a\u00020\b2\u0006\u0010\u001a\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\bH\u0002J\u0010\u0010\u001b\u001a\u00020\b2\u0006\u0010\u001c\u001a\u00020\bH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001e"}, d2 = {"Lcom/nick/myrecoverytracker/NotificationListener;", "Landroid/service/notification/NotificationListenerService;", "<init>", "()V", "df", "Ljava/text/SimpleDateFormat;", "postedAt", "Ljava/util/concurrent/ConcurrentHashMap;", "", "", "onNotificationPosted", "", "sbn", "Landroid/service/notification/StatusBarNotification;", "onNotificationRemoved", "rankingMap", "Landroid/service/notification/NotificationListenerService$RankingMap;", "reason", "", "reasonToString", "code", "appendCsv", "ts", "pkg", "title", "text", NotificationCompat.CATEGORY_EVENT, "quote", "s", "Companion", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class NotificationListener extends NotificationListenerService {
    private static final String FILE = "notification_log.csv";
    private static final String TAG = "NotificationListener";
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private final ConcurrentHashMap<String, Long> postedAt = new ConcurrentHashMap<>();

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(StatusBarNotification sbn) {
        Intrinsics.checkNotNullParameter(sbn, "sbn");
        try {
            this.postedAt.put(sbn.getKey(), Long.valueOf(sbn.getPostTime()));
            String str = this.df.format(new Date(sbn.getPostTime()));
            Intrinsics.checkNotNullExpressionValue(str, "format(...)");
            String packageName = sbn.getPackageName();
            String str2 = packageName == null ? "" : packageName;
            String charSequence = sbn.getNotification().extras.getCharSequence(NotificationCompat.EXTRA_TITLE);
            if (charSequence == null) {
            }
            String string = charSequence.toString();
            String charSequence2 = sbn.getNotification().extras.getCharSequence(NotificationCompat.EXTRA_TEXT);
            if (charSequence2 == null) {
            }
            appendCsv(str, str2, string, charSequence2.toString(), "posted", "");
        } catch (Throwable t) {
            Log.e(TAG, "onNotificationPosted failed", t);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(StatusBarNotification sbn, NotificationListenerService.RankingMap rankingMap, int reason) {
        Intrinsics.checkNotNullParameter(sbn, "sbn");
        try {
            long now = System.currentTimeMillis();
            String r = reasonToString(reason);
            String str = this.df.format(new Date(now));
            Intrinsics.checkNotNullExpressionValue(str, "format(...)");
            String packageName = sbn.getPackageName();
            String str2 = packageName == null ? "" : packageName;
            String charSequence = sbn.getNotification().extras.getCharSequence(NotificationCompat.EXTRA_TITLE);
            if (charSequence == null) {
            }
            String string = charSequence.toString();
            String charSequence2 = sbn.getNotification().extras.getCharSequence(NotificationCompat.EXTRA_TEXT);
            if (charSequence2 == null) {
            }
            try {
                appendCsv(str, str2, string, charSequence2.toString(), "removed", r);
                this.postedAt.remove(sbn.getKey());
            } catch (Throwable th) {
                t = th;
                Log.e(TAG, "onNotificationRemoved failed", t);
            }
        } catch (Throwable th2) {
            t = th2;
        }
    }

    private final String reasonToString(int code) {
        switch (code) {
            case 1:
                return "CLICK";
            case 2:
                return "CANCEL";
            case 3:
                return "CANCEL_ALL";
            case 4:
            case 13:
            case 14:
            case 16:
            default:
                return "UNKNOWN";
            case 5:
                return "PACKAGE_CHANGED";
            case 6:
                return "USER_STOPPED";
            case 7:
                return "PACKAGE_BANNED";
            case 8:
                return "APP_CANCEL";
            case 9:
                return "APP_CANCEL_ALL";
            case 10:
                return "LISTENER_CANCEL";
            case 11:
                return "LISTENER_CANCEL_ALL";
            case 12:
                return "GROUP_SUMMARY_CANCELED";
            case 15:
                return "PROFILE_TURNED_OFF";
            case 17:
                return "CHANNEL_BANNED";
            case 18:
                return "SNOOZED";
            case 19:
                return "TIMEOUT";
        }
    }

    private final void appendCsv(String ts, String pkg, String title, String text, String event, String reason) {
        Throwable th;
        File f = new File(getApplicationContext().getFilesDir(), FILE);
        if (!f.exists() || f.length() == 0) {
            File parentFile = f.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            FilesKt.writeText$default(f, "timestamp,package,title,text,event,reason\n", null, 2, null);
        }
        FileWriter fileWriter = new FileWriter(f, true);
        try {
            FileWriter fileWriter2 = fileWriter;
            String[] strArr = new String[6];
            strArr[0] = ts;
            strArr[1] = pkg;
            try {
                strArr[2] = quote(title);
                try {
                    strArr[3] = quote(text);
                    strArr[4] = event;
                    strArr[5] = reason;
                    Appendable appendableAppend = fileWriter2.append((CharSequence) CollectionsKt.joinToString$default(CollectionsKt.listOf((Object[]) strArr), ",", null, null, 0, null, null, 62, null));
                    Intrinsics.checkNotNullExpressionValue(appendableAppend, "append(...)");
                    Intrinsics.checkNotNullExpressionValue(appendableAppend.append('\n'), "append(...)");
                    CloseableKt.closeFinally(fileWriter, null);
                } catch (Throwable th2) {
                    th = th2;
                    th = th;
                    try {
                        throw th;
                    } catch (Throwable th3) {
                        CloseableKt.closeFinally(fileWriter, th);
                        throw th3;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                th = th;
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
        }
    }

    private final String quote(String s) {
        String cleaned = StringsKt.replace$default(StringsKt.replace$default(s, "\r", " ", false, 4, (Object) null), "\n", " ", false, 4, (Object) null);
        String esc = StringsKt.replace$default(cleaned, "\"", "\"\"", false, 4, (Object) null);
        return "\"" + esc + "\"";
    }
}
