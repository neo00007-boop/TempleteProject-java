package com.lib.network;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ok拦截器:项目中多域名切换,一个host对应一个url，见 {@link Hosts}
 *
 * @author xwchen
 * Date         1/30/21.
 */
public class UrlManagerInterceptor implements Interceptor {
    public static final String TAG = "UrlManagerInterceptor";
    private final HttpConfig config;

    UrlManagerInterceptor(HttpConfig config) {
        this.config = config;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        List<String> headerValues = request.headers(Hosts.hostKey);
        if (headerValues.isEmpty()) {
            return chain.proceed(request);
        }
        Request.Builder builder = request.newBuilder().removeHeader(Hosts.hostKey);
        String key = headerValues.get(0);
        HttpLog.d(TAG, "headerValue=" + key);
        String url = config.hosts.get(key);
        HttpUrl newBaseUrl = url != null ? HttpUrl.parse(url) : null;
        if (newBaseUrl == null) {
            return chain.proceed(builder.build());
        }
        HttpUrl newFullUrl = request.url().newBuilder()
                .scheme(newBaseUrl.scheme())//更换网络协议
                .host(newBaseUrl.host())//更换主机名
                .port(newBaseUrl.port())//更换端口
                .build();
        HttpLog.d(TAG, "intercept: " + newFullUrl);
        return chain.proceed(builder.url(newFullUrl).build());
    }
}
