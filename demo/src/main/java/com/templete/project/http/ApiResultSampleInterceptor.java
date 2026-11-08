package com.templete.project.http;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 只拦截样例路径，其它请求照常发出。
 */
public final class ApiResultSampleInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String json = ApiResultSamples.body(chain.request().url().encodedPath());
        if (json == null) {
            return chain.proceed(chain.request());
        }
        return new Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(json, MediaType.get("application/json; charset=utf-8")))
                .build();
    }
}
