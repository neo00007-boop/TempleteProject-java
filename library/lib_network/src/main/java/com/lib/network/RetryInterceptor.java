package com.lib.network;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 重试拦截器
 * Date         2022/1/7.
 *
 * @author xwchen
 */
public class RetryInterceptor implements Interceptor {
    public static final String TAG = "RetryInterceptor";
    public int maxRetry = 2;//重试次数,一共2+1=3次

    public RetryInterceptor() {
    }

    public RetryInterceptor(int maxRetry) {
        if (maxRetry < 0) {
            return;
        }
        this.maxRetry = maxRetry;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        //只重试 GET/HEAD，POST 等重复提交可能产生副作用
        boolean idempotent = "GET".equals(request.method()) || "HEAD".equals(request.method());
        if (!idempotent) {
            return chain.proceed(request);
        }
        //假如设置为2次重试的话，则最大可能请求3次（默认1次+2次重试）
        for (int retryNum = 0; ; retryNum++) {
            Response response;
            try {
                response = chain.proceed(request);
            } catch (IOException e) {
                if (retryNum >= maxRetry || chain.call().isCanceled()) {
                    throw e;
                }
                HttpLog.d(TAG, "retry " + (retryNum + 1) + " after " + e);
                continue;
            }
            if (!shouldRetry(response.code()) || retryNum >= maxRetry) {
                return response;
            }
            HttpLog.d(TAG, "retry " + (retryNum + 1) + " after code " + response.code());
            response.close();
        }
    }

    /** 只重试临时性错误：超时、限流、服务端错误；400/401/404 等重试也不会成功 */
    private static boolean shouldRetry(int code) {
        return code == 408 || code == 429 || code >= 500;
    }
}
