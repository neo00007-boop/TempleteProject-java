package com.lib.network;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 网络层需要的外部信息。在 Application 里构建一次，交给 {@link RetrofitHelper#init}。
 */
public final class HttpConfig {

    public interface TokenProvider {
        @Nullable
        String token();
    }

    final String baseUrl;
    final Map<String, String> hosts;
    final Map<String, String> headers;
    final TokenProvider tokenProvider;
    final Runnable onTokenExpired;
    final Gson gson;
    final String cookiePath;
    final boolean debug;

    private HttpConfig(Builder builder) {
        baseUrl = builder.baseUrl;
        hosts = Collections.unmodifiableMap(new LinkedHashMap<>(builder.hosts));
        headers = Collections.unmodifiableMap(new LinkedHashMap<>(builder.headers));
        tokenProvider = builder.tokenProvider;
        onTokenExpired = builder.onTokenExpired;
        gson = builder.gson != null ? builder.gson : new Gson();
        cookiePath = builder.cookiePath;
        debug = builder.debug;
    }

    public static final class Builder {
        private final String baseUrl;
        private final Map<String, String> hosts = new LinkedHashMap<>();
        private final Map<String, String> headers = new LinkedHashMap<>();
        private TokenProvider tokenProvider;
        private Runnable onTokenExpired;
        private Gson gson;
        private String cookiePath = "";
        private boolean debug;

        public Builder(@NonNull String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         * 登记多域名，key 对应 {@link Hosts} 里的 KEY1、KEY2。token 只会带给 baseUrl 和这里登记的域名。
         */
        public Builder host(@NonNull String key, @NonNull String url) {
            hosts.put(key, url);
            return this;
        }

        /**
         * 每个请求都带的公共请求头，如版本号。
         */
        public Builder header(@NonNull String name, @NonNull String value) {
            headers.put(name, value);
            return this;
        }

        public Builder tokenProvider(TokenProvider tokenProvider) {
            this.tokenProvider = tokenProvider;
            return this;
        }

        /**
         * 接口返回登录失效时回调，在 OkHttp 线程上执行。
         */
        public Builder onTokenExpired(Runnable onTokenExpired) {
            this.onTokenExpired = onTokenExpired;
            return this;
        }

        /**
         * Retrofit 解析用的 Gson，不传用默认的。
         */
        public Builder gson(Gson gson) {
            this.gson = gson;
            return this;
        }

        /**
         * 只保存这个 path 返回的 cookie，空表示不保存。
         */
        public Builder cookiePath(String cookiePath) {
            this.cookiePath = cookiePath;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public HttpConfig build() {
            return new HttpConfig(this);
        }
    }
}
