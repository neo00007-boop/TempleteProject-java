package com.lib.network;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLException;

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
            Response response = null; // 【必要修改：初始化置空】
            try {
                response = chain.proceed(request);
            } catch (IOException e) {
                // 【必要修改：加入 !isRecoverable(e) 校验，过滤掉无意义的证书和协议错误】
                if (retryNum >= maxRetry || !isRecoverable(e) || chain.call().isCanceled()) {
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

    /** 【必要新增：过滤致命网络异常，避免无意义的重试等待】 */
    private boolean isRecoverable(IOException e) {
        if (e instanceof ProtocolException) return false; // 协议错误
        if (e instanceof InterruptedIOException) return e instanceof java.net.SocketTimeoutException; // 仅允许超时重试
        if (e instanceof SSLException && e.getCause() instanceof CertificateException) return false; // 证书错误
        return true;
    }
}
