package com.lib.base.http;

import com.lib.base.util.DebugUtil;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 重试拦截器
 * PackageName  com.lib.base.http
 * ProjectName  TempleteProject-java
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
        boolean idempotent = "GET".equals(request.method()) || "HEAD".equals(request.method());
        //假如设置为2次重试的话，则最大可能请求3次（默认1次+2次重试）
        int retryNum = 0;
        Response response = chain.proceed(request);
        DebugUtil.logD(TAG, "retryNum=" + retryNum + ",maxRetry=" + maxRetry);
        while (idempotent && !response.isSuccessful() && retryNum < maxRetry) {
            retryNum++;
            DebugUtil.logD(TAG, "retryNum=" + retryNum + ",maxRetry=" + maxRetry);
            response.close();
            response = chain.proceed(request);
        }
        return response;
    }
}
