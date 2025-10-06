package com.google.android.gms.common.api;

import android.os.Looper;
import com.google.android.gms.common.api.internal.OptionalPendingResultImpl;
import com.google.android.gms.common.api.internal.StatusPendingResult;
import com.google.android.gms.common.internal.Preconditions;

/* compiled from: com.google.android.gms:play-services-base@@18.1.0 */
/* loaded from: classes.dex */
public final class PendingResults {
    private PendingResults() {
    }

    public static PendingResult<Status> canceledPendingResult() {
        StatusPendingResult statusPendingResult = new StatusPendingResult(Looper.getMainLooper());
        statusPendingResult.cancel();
        return statusPendingResult;
    }

    public static <R extends Result> PendingResult<R> immediateFailedResult(R r, GoogleApiClient apiClient) {
        Preconditions.checkNotNull(r, "Result must not be null");
        Preconditions.checkArgument(!r.getStatus().isSuccess(), "Status code must not be SUCCESS");
        zag zagVar = new zag(apiClient, r);
        zagVar.setResult(r);
        return zagVar;
    }

    public static <R extends Result> OptionalPendingResult<R> immediatePendingResult(R r) {
        Preconditions.checkNotNull(r, "Result must not be null");
        zah zahVar = new zah(null);
        zahVar.setResult(r);
        return new OptionalPendingResultImpl(zahVar);
    }

    public static <R extends Result> PendingResult<R> canceledPendingResult(R r) {
        Preconditions.checkNotNull(r, "Result must not be null");
        Preconditions.checkArgument(r.getStatus().getStatusCode() == 16, "Status code must be CommonStatusCodes.CANCELED");
        zaf zafVar = new zaf(r);
        zafVar.cancel();
        return zafVar;
    }

    public static <R extends Result> OptionalPendingResult<R> immediatePendingResult(R r, GoogleApiClient apiClient) {
        Preconditions.checkNotNull(r, "Result must not be null");
        zah zahVar = new zah(apiClient);
        zahVar.setResult(r);
        return new OptionalPendingResultImpl(zahVar);
    }

    public static PendingResult<Status> immediatePendingResult(Status result) {
        Preconditions.checkNotNull(result, "Result must not be null");
        StatusPendingResult statusPendingResult = new StatusPendingResult(Looper.getMainLooper());
        statusPendingResult.setResult(result);
        return statusPendingResult;
    }

    public static PendingResult<Status> immediatePendingResult(Status result, GoogleApiClient apiClient) {
        Preconditions.checkNotNull(result, "Result must not be null");
        StatusPendingResult statusPendingResult = new StatusPendingResult(apiClient);
        statusPendingResult.setResult(result);
        return statusPendingResult;
    }
}
