package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Feature;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Scope;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
/* loaded from: classes.dex */
public abstract class BaseGmsClient<T extends IInterface> {
    public static final int CONNECT_STATE_CONNECTED = 4;
    public static final int CONNECT_STATE_DISCONNECTED = 1;
    public static final int CONNECT_STATE_DISCONNECTING = 5;
    public static final String DEFAULT_ACCOUNT = "<<default account>>";
    public static final String KEY_PENDING_INTENT = "pendingIntent";
    private volatile String zzA;
    private ConnectionResult zzB;
    private boolean zzC;
    private volatile zzj zzD;
    zzu zza;
    final Handler zzb;
    protected ConnectionProgressReportCallbacks zzc;
    protected AtomicInteger zzd;
    private int zzf;
    private long zzg;
    private long zzh;
    private int zzi;
    private long zzj;
    private volatile String zzk;
    private final Context zzl;
    private final Looper zzm;
    private final GmsClientSupervisor zzn;
    private final GoogleApiAvailabilityLight zzo;
    private final Object zzp;
    private final Object zzq;
    private IGmsServiceBroker zzr;
    private IInterface zzs;
    private final ArrayList zzt;
    private zze zzu;
    private int zzv;
    private final BaseConnectionCallbacks zzw;
    private final BaseOnConnectionFailedListener zzx;
    private final int zzy;
    private final String zzz;
    private static final Feature[] zze = new Feature[0];
    public static final String[] GOOGLE_PLUS_REQUIRED_FEATURES = {"service_esmobile", "service_googleme"};

    /* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
    public interface BaseConnectionCallbacks {
        public static final int CAUSE_DEAD_OBJECT_EXCEPTION = 3;
        public static final int CAUSE_SERVICE_DISCONNECTED = 1;

        void onConnected(Bundle bundle);

        void onConnectionSuspended(int i);
    }

    /* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
    public interface BaseOnConnectionFailedListener {
        void onConnectionFailed(ConnectionResult connectionResult);
    }

    /* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
    public interface ConnectionProgressReportCallbacks {
        void onReportServiceBinding(ConnectionResult connectionResult);
    }

    /* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
    protected class LegacyClientCallbackAdapter implements ConnectionProgressReportCallbacks {
        public LegacyClientCallbackAdapter() {
        }

        @Override // com.google.android.gms.common.internal.BaseGmsClient.ConnectionProgressReportCallbacks
        public final void onReportServiceBinding(ConnectionResult connectionResult) {
            if (connectionResult.isSuccess()) {
                BaseGmsClient baseGmsClient = BaseGmsClient.this;
                baseGmsClient.getRemoteService(null, baseGmsClient.getScopes());
            } else if (BaseGmsClient.this.zzx != null) {
                BaseGmsClient.this.zzx.onConnectionFailed(connectionResult);
            }
        }
    }

    /* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
    public interface SignOutCallbacks {
        void onSignOutComplete();
    }

    protected BaseGmsClient(Context context, Handler handler, GmsClientSupervisor supervisor, GoogleApiAvailabilityLight apiAvailability, int gCoreServiceId, BaseConnectionCallbacks connectedListener, BaseOnConnectionFailedListener connectionFailedListener) {
        this.zzk = null;
        this.zzp = new Object();
        this.zzq = new Object();
        this.zzt = new ArrayList();
        this.zzv = 1;
        this.zzB = null;
        this.zzC = false;
        this.zzD = null;
        this.zzd = new AtomicInteger(0);
        Preconditions.checkNotNull(context, "Context must not be null");
        this.zzl = context;
        Preconditions.checkNotNull(handler, "Handler must not be null");
        this.zzb = handler;
        this.zzm = handler.getLooper();
        Preconditions.checkNotNull(supervisor, "Supervisor must not be null");
        this.zzn = supervisor;
        Preconditions.checkNotNull(apiAvailability, "API availability must not be null");
        this.zzo = apiAvailability;
        this.zzy = gCoreServiceId;
        this.zzw = connectedListener;
        this.zzx = connectionFailedListener;
        this.zzz = null;
    }

    static /* bridge */ /* synthetic */ void zzj(BaseGmsClient baseGmsClient, zzj zzjVar) {
        baseGmsClient.zzD = zzjVar;
        if (baseGmsClient.usesClientTelemetry()) {
            ConnectionTelemetryConfiguration connectionTelemetryConfiguration = zzjVar.zzd;
            RootTelemetryConfigManager.getInstance().zza(connectionTelemetryConfiguration == null ? null : connectionTelemetryConfiguration.zza());
        }
    }

    static /* bridge */ /* synthetic */ void zzk(BaseGmsClient baseGmsClient, int i) {
        int i2;
        int i3;
        synchronized (baseGmsClient.zzp) {
            i2 = baseGmsClient.zzv;
        }
        if (i2 == 3) {
            baseGmsClient.zzC = true;
            i3 = 5;
        } else {
            i3 = 4;
        }
        Handler handler = baseGmsClient.zzb;
        handler.sendMessage(handler.obtainMessage(i3, baseGmsClient.zzd.get(), 16));
    }

    static /* bridge */ /* synthetic */ boolean zzn(BaseGmsClient baseGmsClient, int i, int i2, IInterface iInterface) {
        synchronized (baseGmsClient.zzp) {
            if (baseGmsClient.zzv != i) {
                return false;
            }
            baseGmsClient.zzp(i2, iInterface);
            return true;
        }
    }

    static /* bridge */ /* synthetic */ boolean zzo(BaseGmsClient baseGmsClient) throws ClassNotFoundException {
        if (baseGmsClient.zzC || TextUtils.isEmpty(baseGmsClient.getServiceDescriptor()) || TextUtils.isEmpty(baseGmsClient.getLocalStartServiceAction())) {
            return false;
        }
        try {
            Class.forName(baseGmsClient.getServiceDescriptor());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void zzp(int i, IInterface iInterface) {
        zzu zzuVar;
        Preconditions.checkArgument((i == 4) == (iInterface != null));
        synchronized (this.zzp) {
            this.zzv = i;
            this.zzs = iInterface;
            switch (i) {
                case 1:
                    zze zzeVar = this.zzu;
                    if (zzeVar != null) {
                        GmsClientSupervisor gmsClientSupervisor = this.zzn;
                        String strZzc = this.zza.zzc();
                        Preconditions.checkNotNull(strZzc);
                        gmsClientSupervisor.zzb(strZzc, this.zza.zzb(), this.zza.zza(), zzeVar, zze(), this.zza.zzd());
                        this.zzu = null;
                        break;
                    }
                    break;
                case 2:
                case 3:
                    zze zzeVar2 = this.zzu;
                    if (zzeVar2 != null && (zzuVar = this.zza) != null) {
                        Log.e("GmsClient", "Calling connect() while still connected, missing disconnect() for " + zzuVar.zzc() + " on " + zzuVar.zzb());
                        GmsClientSupervisor gmsClientSupervisor2 = this.zzn;
                        String strZzc2 = this.zza.zzc();
                        Preconditions.checkNotNull(strZzc2);
                        gmsClientSupervisor2.zzb(strZzc2, this.zza.zzb(), this.zza.zza(), zzeVar2, zze(), this.zza.zzd());
                        this.zzd.incrementAndGet();
                    }
                    zze zzeVar3 = new zze(this, this.zzd.get());
                    this.zzu = zzeVar3;
                    this.zza = (this.zzv != 3 || getLocalStartServiceAction() == null) ? new zzu(getStartServicePackage(), getStartServiceAction(), false, GmsClientSupervisor.getDefaultBindFlags(), getUseDynamicLookup()) : new zzu(getContext().getPackageName(), getLocalStartServiceAction(), true, GmsClientSupervisor.getDefaultBindFlags(), false);
                    if (this.zza.zzd() && getMinApkVersion() < 17895000) {
                        throw new IllegalStateException("Internal Error, the minimum apk version of this BaseGmsClient is too low to support dynamic lookup. Start service action: ".concat(String.valueOf(this.zza.zzc())));
                    }
                    GmsClientSupervisor gmsClientSupervisor3 = this.zzn;
                    String strZzc3 = this.zza.zzc();
                    Preconditions.checkNotNull(strZzc3);
                    if (!gmsClientSupervisor3.zzc(new zzn(strZzc3, this.zza.zzb(), this.zza.zza(), this.zza.zzd()), zzeVar3, zze(), getBindServiceExecutor())) {
                        Log.w("GmsClient", "unable to connect to service: " + this.zza.zzc() + " on " + this.zza.zzb());
                        zzl(16, null, this.zzd.get());
                        break;
                    }
                    break;
                case 4:
                    Preconditions.checkNotNull(iInterface);
                    onConnectedLocked(iInterface);
                    break;
            }
        }
    }

    public void checkAvailabilityAndConnect() {
        int iIsGooglePlayServicesAvailable = this.zzo.isGooglePlayServicesAvailable(this.zzl, getMinApkVersion());
        if (iIsGooglePlayServicesAvailable == 0) {
            connect(new LegacyClientCallbackAdapter());
        } else {
            zzp(1, null);
            triggerNotAvailable(new LegacyClientCallbackAdapter(), iIsGooglePlayServicesAvailable, null);
        }
    }

    protected final void checkConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected. Call connect() and wait for onConnected() to be called.");
        }
    }

    public void connect(ConnectionProgressReportCallbacks callbacks) {
        Preconditions.checkNotNull(callbacks, "Connection progress callbacks cannot be null.");
        this.zzc = callbacks;
        zzp(2, null);
    }

    protected abstract T createServiceInterface(IBinder iBinder);

    public void disconnect() {
        this.zzd.incrementAndGet();
        synchronized (this.zzt) {
            int size = this.zzt.size();
            for (int i = 0; i < size; i++) {
                ((zzc) this.zzt.get(i)).zzf();
            }
            this.zzt.clear();
        }
        synchronized (this.zzq) {
            this.zzr = null;
        }
        zzp(1, null);
    }

    public void dump(String prefix, FileDescriptor fileDescriptor, PrintWriter writer, String[] strArr) {
        int i;
        IInterface iInterface;
        IGmsServiceBroker iGmsServiceBroker;
        synchronized (this.zzp) {
            i = this.zzv;
            iInterface = this.zzs;
        }
        synchronized (this.zzq) {
            iGmsServiceBroker = this.zzr;
        }
        writer.append((CharSequence) prefix).append("mConnectState=");
        switch (i) {
            case 1:
                writer.print("DISCONNECTED");
                break;
            case 2:
                writer.print("REMOTE_CONNECTING");
                break;
            case 3:
                writer.print("LOCAL_CONNECTING");
                break;
            case 4:
                writer.print("CONNECTED");
                break;
            case 5:
                writer.print("DISCONNECTING");
                break;
            default:
                writer.print("UNKNOWN");
                break;
        }
        writer.append(" mService=");
        if (iInterface == null) {
            writer.append("null");
        } else {
            writer.append((CharSequence) getServiceDescriptor()).append("@").append((CharSequence) Integer.toHexString(System.identityHashCode(iInterface.asBinder())));
        }
        writer.append(" mServiceBroker=");
        if (iGmsServiceBroker == null) {
            writer.println("null");
        } else {
            writer.append("IGmsServiceBroker@").println(Integer.toHexString(System.identityHashCode(iGmsServiceBroker.asBinder())));
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        if (this.zzh > 0) {
            PrintWriter printWriterAppend = writer.append((CharSequence) prefix).append("lastConnectedTime=");
            long j = this.zzh;
            printWriterAppend.println(j + " " + simpleDateFormat.format(new Date(j)));
        }
        if (this.zzg > 0) {
            writer.append((CharSequence) prefix).append("lastSuspendedCause=");
            int i2 = this.zzf;
            switch (i2) {
                case 1:
                    writer.append("CAUSE_SERVICE_DISCONNECTED");
                    break;
                case 2:
                    writer.append("CAUSE_NETWORK_LOST");
                    break;
                case 3:
                    writer.append("CAUSE_DEAD_OBJECT_EXCEPTION");
                    break;
                default:
                    writer.append((CharSequence) String.valueOf(i2));
                    break;
            }
            PrintWriter printWriterAppend2 = writer.append(" lastSuspendedTime=");
            long j2 = this.zzg;
            printWriterAppend2.println(j2 + " " + simpleDateFormat.format(new Date(j2)));
        }
        if (this.zzj > 0) {
            writer.append((CharSequence) prefix).append("lastFailedStatus=").append((CharSequence) CommonStatusCodes.getStatusCodeString(this.zzi));
            PrintWriter printWriterAppend3 = writer.append(" lastFailedTime=");
            long j3 = this.zzj;
            printWriterAppend3.println(j3 + " " + simpleDateFormat.format(new Date(j3)));
        }
    }

    protected boolean enableLocalFallback() {
        return false;
    }

    public Account getAccount() {
        return null;
    }

    public Feature[] getApiFeatures() {
        return zze;
    }

    public final Feature[] getAvailableFeatures() {
        zzj zzjVar = this.zzD;
        if (zzjVar == null) {
            return null;
        }
        return zzjVar.zzb;
    }

    protected Executor getBindServiceExecutor() {
        return null;
    }

    public Bundle getConnectionHint() {
        return null;
    }

    public final Context getContext() {
        return this.zzl;
    }

    public String getEndpointPackageName() {
        zzu zzuVar;
        if (!isConnected() || (zzuVar = this.zza) == null) {
            throw new RuntimeException("Failed to connect when checking package");
        }
        return zzuVar.zzb();
    }

    public int getGCoreServiceId() {
        return this.zzy;
    }

    protected Bundle getGetServiceRequestExtraArgs() {
        return new Bundle();
    }

    public String getLastDisconnectMessage() {
        return this.zzk;
    }

    protected String getLocalStartServiceAction() {
        return null;
    }

    public final Looper getLooper() {
        return this.zzm;
    }

    public int getMinApkVersion() {
        return GoogleApiAvailabilityLight.GOOGLE_PLAY_SERVICES_VERSION_CODE;
    }

    public void getRemoteService(IAccountAccessor authedAccountAccessor, Set<Scope> set) {
        Bundle getServiceRequestExtraArgs = getGetServiceRequestExtraArgs();
        int i = this.zzy;
        String str = this.zzA;
        int i2 = GoogleApiAvailabilityLight.GOOGLE_PLAY_SERVICES_VERSION_CODE;
        Scope[] scopeArr = GetServiceRequest.zza;
        Bundle bundle = new Bundle();
        Feature[] featureArr = GetServiceRequest.zzb;
        GetServiceRequest getServiceRequest = new GetServiceRequest(6, i, i2, null, null, scopeArr, bundle, null, featureArr, featureArr, true, 0, false, str);
        getServiceRequest.zzf = this.zzl.getPackageName();
        getServiceRequest.zzi = getServiceRequestExtraArgs;
        if (set != null) {
            getServiceRequest.zzh = (Scope[]) set.toArray(new Scope[0]);
        }
        if (requiresSignIn()) {
            Account account = getAccount();
            if (account == null) {
                account = new Account("<<default account>>", AccountType.GOOGLE);
            }
            getServiceRequest.zzj = account;
            if (authedAccountAccessor != null) {
                getServiceRequest.zzg = authedAccountAccessor.asBinder();
            }
        } else if (requiresAccount()) {
            getServiceRequest.zzj = getAccount();
        }
        getServiceRequest.zzk = zze;
        getServiceRequest.zzl = getApiFeatures();
        if (usesClientTelemetry()) {
            getServiceRequest.zzo = true;
        }
        try {
            synchronized (this.zzq) {
                IGmsServiceBroker iGmsServiceBroker = this.zzr;
                if (iGmsServiceBroker != null) {
                    iGmsServiceBroker.getService(new zzd(this, this.zzd.get()), getServiceRequest);
                } else {
                    Log.w("GmsClient", "mServiceBroker is null, client disconnected");
                }
            }
        } catch (DeadObjectException e) {
            Log.w("GmsClient", "IGmsServiceBroker.getService failed", e);
            triggerConnectionSuspended(3);
        } catch (RemoteException e2) {
            e = e2;
            Log.w("GmsClient", "IGmsServiceBroker.getService failed", e);
            onPostInitHandler(8, null, null, this.zzd.get());
        } catch (SecurityException e3) {
            throw e3;
        } catch (RuntimeException e4) {
            e = e4;
            Log.w("GmsClient", "IGmsServiceBroker.getService failed", e);
            onPostInitHandler(8, null, null, this.zzd.get());
        }
    }

    protected Set<Scope> getScopes() {
        return Collections.emptySet();
    }

    public final T getService() throws DeadObjectException {
        T t;
        synchronized (this.zzp) {
            if (this.zzv == 5) {
                throw new DeadObjectException();
            }
            checkConnected();
            t = (T) this.zzs;
            Preconditions.checkNotNull(t, "Client is connected but service is null");
        }
        return t;
    }

    protected abstract String getServiceDescriptor();

    public Intent getSignInIntent() {
        throw new UnsupportedOperationException("Not a sign in API");
    }

    protected abstract String getStartServiceAction();

    protected String getStartServicePackage() {
        return "com.google.android.gms";
    }

    public ConnectionTelemetryConfiguration getTelemetryConfiguration() {
        zzj zzjVar = this.zzD;
        if (zzjVar == null) {
            return null;
        }
        return zzjVar.zzd;
    }

    protected boolean getUseDynamicLookup() {
        return getMinApkVersion() >= 211700000;
    }

    public boolean hasConnectionInfo() {
        return this.zzD != null;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.zzp) {
            z = this.zzv == 4;
        }
        return z;
    }

    public boolean isConnecting() {
        boolean z;
        synchronized (this.zzp) {
            int i = this.zzv;
            z = true;
            if (i != 2 && i != 3) {
                z = false;
            }
        }
        return z;
    }

    protected void onConnectedLocked(T t) {
        this.zzh = System.currentTimeMillis();
    }

    protected void onConnectionFailed(ConnectionResult result) {
        this.zzi = result.getErrorCode();
        this.zzj = System.currentTimeMillis();
    }

    protected void onConnectionSuspended(int cause) {
        this.zzf = cause;
        this.zzg = System.currentTimeMillis();
    }

    protected void onPostInitHandler(int statusCode, IBinder service, Bundle resolution, int disconnectCount) {
        Handler handler = this.zzb;
        handler.sendMessage(handler.obtainMessage(1, disconnectCount, -1, new zzf(this, statusCode, service, resolution)));
    }

    public void onUserSignOut(SignOutCallbacks signOutCallbacks) {
        signOutCallbacks.onSignOutComplete();
    }

    public boolean providesSignIn() {
        return false;
    }

    public boolean requiresAccount() {
        return false;
    }

    public boolean requiresGooglePlayServices() {
        return true;
    }

    public boolean requiresSignIn() {
        return false;
    }

    public void setAttributionTag(String str) {
        this.zzA = str;
    }

    public void triggerConnectionSuspended(int cause) {
        Handler handler = this.zzb;
        handler.sendMessage(handler.obtainMessage(6, this.zzd.get(), cause));
    }

    protected void triggerNotAvailable(ConnectionProgressReportCallbacks callbacks, int errorCode, PendingIntent resolution) {
        Preconditions.checkNotNull(callbacks, "Connection progress callbacks cannot be null.");
        this.zzc = callbacks;
        Handler handler = this.zzb;
        handler.sendMessage(handler.obtainMessage(3, this.zzd.get(), errorCode, resolution));
    }

    public boolean usesClientTelemetry() {
        return false;
    }

    protected final String zze() {
        String str = this.zzz;
        return str == null ? this.zzl.getClass().getName() : str;
    }

    protected final void zzl(int i, Bundle bundle, int i2) {
        Handler handler = this.zzb;
        handler.sendMessage(handler.obtainMessage(7, i2, -1, new zzg(this, i, null)));
    }

    public IBinder getServiceBrokerBinder() {
        synchronized (this.zzq) {
            IGmsServiceBroker iGmsServiceBroker = this.zzr;
            if (iGmsServiceBroker == null) {
                return null;
            }
            return iGmsServiceBroker.asBinder();
        }
    }

    /* JADX WARN: Illegal instructions before constructor call */
    protected BaseGmsClient(Context context, Looper looper, int gCoreServiceId, BaseConnectionCallbacks connectedListener, BaseOnConnectionFailedListener connectionFailedListener, String realClientName) {
        GmsClientSupervisor gmsClientSupervisor = GmsClientSupervisor.getInstance(context);
        GoogleApiAvailabilityLight googleApiAvailabilityLight = GoogleApiAvailabilityLight.getInstance();
        Preconditions.checkNotNull(connectedListener);
        Preconditions.checkNotNull(connectionFailedListener);
        this(context, looper, gmsClientSupervisor, googleApiAvailabilityLight, gCoreServiceId, connectedListener, connectionFailedListener, realClientName);
    }

    public void disconnect(String message) {
        this.zzk = message;
        disconnect();
    }

    protected BaseGmsClient(Context context, Looper looper, GmsClientSupervisor supervisor, GoogleApiAvailabilityLight apiAvailability, int gCoreServiceId, BaseConnectionCallbacks connectedListener, BaseOnConnectionFailedListener connectionFailedListener, String realClientName) {
        this.zzk = null;
        this.zzp = new Object();
        this.zzq = new Object();
        this.zzt = new ArrayList();
        this.zzv = 1;
        this.zzB = null;
        this.zzC = false;
        this.zzD = null;
        this.zzd = new AtomicInteger(0);
        Preconditions.checkNotNull(context, "Context must not be null");
        this.zzl = context;
        Preconditions.checkNotNull(looper, "Looper must not be null");
        this.zzm = looper;
        Preconditions.checkNotNull(supervisor, "Supervisor must not be null");
        this.zzn = supervisor;
        Preconditions.checkNotNull(apiAvailability, "API availability must not be null");
        this.zzo = apiAvailability;
        this.zzb = new zzb(this, looper);
        this.zzy = gCoreServiceId;
        this.zzw = connectedListener;
        this.zzx = connectionFailedListener;
        this.zzz = realClientName;
    }
}
