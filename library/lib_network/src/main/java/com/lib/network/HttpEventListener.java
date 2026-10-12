package com.lib.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

public class HttpEventListener extends EventListener {

    private final String TAG = "OK_EVENT";

    /**
     * 自定义EventListener工厂
     */
    public static final Factory FACTORY = new Factory() {
        final AtomicLong nextCallId = new AtomicLong(1L);

        @NonNull
        @Override
        public EventListener create(Call call) {
            long callId = nextCallId.getAndIncrement();
            return new HttpEventListener(callId, call.request().url(), System.currentTimeMillis());
        }
    };

    public HttpEventListener(long callId, HttpUrl url, long callStart) {
        HttpLog.d(TAG, String.format("callId=%s,url=%s", callId, url));
    }

    @Override
    public void callStart(@NonNull Call call) {
        super.callStart(call);
    }

    @Override
    public void dnsStart(@NonNull Call call, @NonNull String domainName) {
        super.dnsStart(call, domainName);
        HttpLog.d(TAG, String.format("dnsStart:domainName=%s,call=%s", domainName, call));
    }

    @Override
    public void dnsEnd(@NonNull Call call, @NonNull String domainName, @NonNull List<InetAddress> inetAddressList) {
        super.dnsEnd(call, domainName, inetAddressList);
        HttpLog.d(TAG, String.format("dnsEnd:domainName=%s,inetAddressList=%s,call=%s", domainName, inetAddressList, call));
    }

    @Override
    public void connectStart(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy) {
        super.connectStart(call, inetSocketAddress, proxy);
    }

    @Override
    public void secureConnectStart(@NonNull Call call) {
        super.secureConnectStart(call);
    }

    @Override
    public void secureConnectEnd(@NonNull Call call, @Nullable Handshake handshake) {
        super.secureConnectEnd(call, handshake);
    }

    @Override
    public void connectEnd(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy, @Nullable Protocol protocol) {
        super.connectEnd(call, inetSocketAddress, proxy, protocol);
    }

    @Override
    public void connectFailed(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy, @Nullable Protocol protocol, @NonNull IOException ioe) {
        super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
        HttpLog.d(TAG, String.format("connectFailed:proxy=%s,inetSocketAddress=%s,call=%s", proxy, inetSocketAddress, call));
    }

    @Override
    public void connectionAcquired(@NonNull Call call, @NonNull Connection connection) {
        super.connectionAcquired(call, connection);
    }

    @Override
    public void connectionReleased(@NonNull Call call, @NonNull Connection connection) {
        super.connectionReleased(call, connection);
    }

    @Override
    public void requestHeadersStart(@NonNull Call call) {
        super.requestHeadersStart(call);
    }

    @Override
    public void requestHeadersEnd(@NonNull Call call, @NonNull Request request) {
        super.requestHeadersEnd(call, request);
    }

    @Override
    public void requestBodyStart(@NonNull Call call) {
        super.requestBodyStart(call);
    }

    @Override
    public void requestBodyEnd(@NonNull Call call, long byteCount) {
        super.requestBodyEnd(call, byteCount);
    }

    @Override
    public void responseHeadersStart(@NonNull Call call) {
        super.responseHeadersStart(call);
    }

    @Override
    public void responseHeadersEnd(@NonNull Call call, @NonNull Response response) {
        super.responseHeadersEnd(call, response);
    }

    @Override
    public void responseBodyStart(@NonNull Call call) {
        super.responseBodyStart(call);
    }

    @Override
    public void responseBodyEnd(@NonNull Call call, long byteCount) {
        super.responseBodyEnd(call, byteCount);
    }

    @Override
    public void callEnd(@NonNull Call call) {
        super.callEnd(call);
    }

    @Override
    public void callFailed(@NonNull Call call, @NonNull IOException ioe) {
        super.callFailed(call, ioe);
        if (call.isCanceled()) {
            return;
        }
        String request = call.request().toString();
        String url = call.request().url().toString();
        String encodedPath = call.request().url().encodedPath();
        HttpLog.d(TAG, "request=" + request);
        HttpLog.d(TAG, "url=" + url);
        HttpLog.d(TAG, "encodedPath=" + encodedPath);
    }
}
