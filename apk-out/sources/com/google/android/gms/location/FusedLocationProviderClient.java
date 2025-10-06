package com.google.android.gms.location;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.HasApiKey;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.Task;
import java.util.concurrent.Executor;

/* compiled from: com.google.android.gms:play-services-location@@21.0.1 */
/* loaded from: classes.dex */
public interface FusedLocationProviderClient extends HasApiKey<Api.ApiOptions.NoOptions> {

    @Deprecated
    public static final String KEY_MOCK_LOCATION = "mockLocation";

    @Deprecated
    public static final String KEY_VERTICAL_ACCURACY = "verticalAccuracy";

    Task<Void> flushLocations();

    Task<Location> getCurrentLocation(int i, CancellationToken cancellationToken);

    Task<Location> getCurrentLocation(CurrentLocationRequest currentLocationRequest, CancellationToken cancellationToken);

    Task<Location> getLastLocation();

    Task<Location> getLastLocation(LastLocationRequest lastLocationRequest);

    Task<LocationAvailability> getLocationAvailability();

    Task<Void> removeLocationUpdates(PendingIntent pendingIntent);

    Task<Void> removeLocationUpdates(LocationCallback locationCallback);

    Task<Void> removeLocationUpdates(LocationListener locationListener);

    Task<Void> requestLocationUpdates(LocationRequest locationRequest, PendingIntent pendingIntent);

    Task<Void> requestLocationUpdates(LocationRequest locationRequest, LocationCallback locationCallback, Looper looper);

    Task<Void> requestLocationUpdates(LocationRequest locationRequest, LocationListener locationListener, Looper looper);

    Task<Void> requestLocationUpdates(LocationRequest locationRequest, Executor executor, LocationCallback locationCallback);

    Task<Void> requestLocationUpdates(LocationRequest locationRequest, Executor executor, LocationListener locationListener);

    Task<Void> setMockLocation(Location location);

    Task<Void> setMockMode(boolean z);
}
