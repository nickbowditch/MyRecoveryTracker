package com.nick.myrecoverytracker;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.io.CloseableKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;

/* compiled from: RedcapClient.kt */
@Metadata(d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001:\u0001\fB\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0003J\u0010\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\u0003H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\r"}, d2 = {"Lcom/nick/myrecoverytracker/RedcapClient;", "", "apiUrl", "", "token", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "postRecords", "Lcom/nick/myrecoverytracker/RedcapClient$Response;", "jsonArrayPayload", "enc", "s", "Response", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes3.dex */
public final class RedcapClient {
    private final String apiUrl;
    private final String token;

    public RedcapClient(String apiUrl, String token) {
        Intrinsics.checkNotNullParameter(apiUrl, "apiUrl");
        Intrinsics.checkNotNullParameter(token, "token");
        this.apiUrl = apiUrl;
        this.token = token;
    }

    /* compiled from: RedcapClient.kt */
    @Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0011\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007¢\u0006\u0004\b\b\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0011\u001a\u00020\u0005HÆ\u0003J\t\u0010\u0012\u001a\u00020\u0007HÆ\u0003J'\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007HÆ\u0001J\u0013\u0010\u0014\u001a\u00020\u00052\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0016\u001a\u00020\u0003HÖ\u0001J\t\u0010\u0017\u001a\u00020\u0007HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f¨\u0006\u0018"}, d2 = {"Lcom/nick/myrecoverytracker/RedcapClient$Response;", "", "code", "", "ok", "", "body", "", "<init>", "(IZLjava/lang/String;)V", "getCode", "()I", "getOk", "()Z", "getBody", "()Ljava/lang/String;", "component1", "component2", "component3", "copy", "equals", "other", "hashCode", "toString", "app_debug"}, k = 1, mv = {2, 0, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
    public static final /* data */ class Response {
        private final String body;
        private final int code;
        private final boolean ok;

        public static /* synthetic */ Response copy$default(Response response, int i, boolean z, String str, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                i = response.code;
            }
            if ((i2 & 2) != 0) {
                z = response.ok;
            }
            if ((i2 & 4) != 0) {
                str = response.body;
            }
            return response.copy(i, z, str);
        }

        /* renamed from: component1, reason: from getter */
        public final int getCode() {
            return this.code;
        }

        /* renamed from: component2, reason: from getter */
        public final boolean getOk() {
            return this.ok;
        }

        /* renamed from: component3, reason: from getter */
        public final String getBody() {
            return this.body;
        }

        public final Response copy(int code, boolean ok, String body) {
            Intrinsics.checkNotNullParameter(body, "body");
            return new Response(code, ok, body);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Response)) {
                return false;
            }
            Response response = (Response) other;
            return this.code == response.code && this.ok == response.ok && Intrinsics.areEqual(this.body, response.body);
        }

        public int hashCode() {
            return (((Integer.hashCode(this.code) * 31) + Boolean.hashCode(this.ok)) * 31) + this.body.hashCode();
        }

        public String toString() {
            return "Response(code=" + this.code + ", ok=" + this.ok + ", body=" + this.body + ")";
        }

        public Response(int code, boolean ok, String body) {
            Intrinsics.checkNotNullParameter(body, "body");
            this.code = code;
            this.ok = ok;
            this.body = body;
        }

        public final String getBody() {
            return this.body;
        }

        public final int getCode() {
            return this.code;
        }

        public final boolean getOk() {
            return this.ok;
        }
    }

    public final Response postRecords(String jsonArrayPayload) throws IOException {
        Response response;
        boolean z;
        String body;
        Intrinsics.checkNotNullParameter(jsonArrayPayload, "jsonArrayPayload");
        StringBuilder sb = new StringBuilder();
        sb.append("token=").append(enc(this.token));
        sb.append("&content=record");
        sb.append("&format=json");
        sb.append("&type=flat");
        sb.append("&overwriteBehavior=normal");
        sb.append("&data=").append(enc(jsonArrayPayload));
        String params = sb.toString();
        Intrinsics.checkNotNullExpressionValue(params, "toString(...)");
        URL url = new URL(this.apiUrl);
        URLConnection uRLConnectionOpenConnection = url.openConnection();
        Intrinsics.checkNotNull(uRLConnectionOpenConnection, "null cannot be cast to non-null type java.net.HttpURLConnection");
        HttpURLConnection conn = (HttpURLConnection) uRLConnectionOpenConnection;
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        boolean z2 = true;
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        int code = -1;
        try {
            BufferedReader dataOutputStream = new DataOutputStream(conn.getOutputStream());
            try {
                dataOutputStream.writeBytes(params);
                Unit unit = Unit.INSTANCE;
                CloseableKt.closeFinally(dataOutputStream, null);
                code = conn.getResponseCode();
                if (200 <= code && code < 300) {
                    z = true;
                } else {
                    z = false;
                }
                if (z) {
                    dataOutputStream = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    try {
                        body = TextStreamsKt.readText(dataOutputStream);
                        CloseableKt.closeFinally(dataOutputStream, null);
                    } finally {
                    }
                } else {
                    InputStream errorStream = conn.getErrorStream();
                    if (errorStream == null) {
                        errorStream = conn.getInputStream();
                    }
                    dataOutputStream = new BufferedReader(new InputStreamReader(errorStream));
                    try {
                        body = TextStreamsKt.readText(dataOutputStream);
                        CloseableKt.closeFinally(dataOutputStream, null);
                    } finally {
                        try {
                            throw th;
                        } finally {
                        }
                    }
                }
                if (200 > code || code >= 300) {
                    z2 = false;
                }
                response = new Response(code, z2, body);
            } finally {
                try {
                    throw th;
                } finally {
                }
            }
        } finally {
            try {
                return response;
            } finally {
            }
        }
        return response;
    }

    private final String enc(String s) throws UnsupportedEncodingException {
        String strEncode = URLEncoder.encode(s, Charsets.UTF_8.name());
        Intrinsics.checkNotNullExpressionValue(strEncode, "encode(...)");
        return strEncode;
    }
}
