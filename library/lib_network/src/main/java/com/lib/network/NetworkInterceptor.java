package com.lib.network;

import android.text.TextUtils;


import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
/* 常见的媒体格式类型如下：

    text/html ： HTML格式
    text/plain ：纯文本格式
    text/xml ：  XML格式
    image/gif ：gif图片格式
    image/jpeg ：jpg图片格式
    image/png：png图片格式

   以application开头的媒体格式类型：

   application/xhtml+xml ：XHTML格式
   application/xml     ： XML数据格式
   application/atom+xml  ：Atom XML聚合格式
   application/json    ： JSON数据格式
   application/pdf       ：pdf格式
   application/msword  ： Word文档格式
   application/octet-stream ： 二进制流数据（如常见的文件下载）
   application/x-www-form-urlencoded ： <form encType=””>中默认的encType，form表单数据被编码为key/value格式发送到服务器（表单默认的提交数据的格式）

   另外一种常见的媒体格式是上传文件之时使用的：

    multipart/form-data ： 需要在表单中进行文件上传时，就需要使用该格式*/

/**
 * ok拦截器:
 * 1.统一添加header;
 * 2.过率图片加载;
 * 3.判断登录态.
 *
 * @author xwchen
 * Date         2021/9/14.
 */

public class NetworkInterceptor implements Interceptor {

    //登录态失效
    private static final String LOGIN_STATE_KEY = "code";
    private static final int LOGIN_STATE_FAIL = -1;
    private final HttpConfig config;

    NetworkInterceptor(HttpConfig config) {
        this.config = config;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        //①添加Header参数
        Request request = buildRequest(chain);
        Response response = chain.proceed(request);
        //②只看 json 响应；图片、文件下载、没有 Content-Type 的响应不读，避免整个读进内存
        ResponseBody responseBody = response.body();
        MediaType mediaType = responseBody != null ? responseBody.contentType() : null;
        if (mediaType == null || !"json".equals(mediaType.subtype())) {
            return response;
        }
        //③peekBody 只读一份副本，原响应原样交给 Retrofit；读失败直接抛出，按网络错误处理
        String str = response.peekBody(Long.MAX_VALUE).string();
        if (!TextUtils.isEmpty(str)) {
            checkLoginCode(str);
        }
        return response;
    }

    @NotNull
    private Request buildRequest(@NotNull Chain chain) {
        Request request = chain.request();
        Request.Builder builder = request
                .newBuilder()
                .cacheControl(new CacheControl.Builder().noCache().build());
        for (Map.Entry<String, String> header : config.headers.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        if (isOwnApi(request.url()) && config.tokenProvider != null) {
            String token = config.tokenProvider.token();
            if (!TextUtils.isEmpty(token)) {
                builder.addHeader("token", token);
            }
        }
        return builder.build();
    }

    /** 只给自己的接口带 token，图片和第三方域名不带 */
    private boolean isOwnApi(HttpUrl url) {
        if (sameHost(url, config.baseUrl)) {
            return true;
        }
        for (String base : config.hosts.values()) {
            if (sameHost(url, base)) {
                return true;
            }
        }
        return false;
    }

    private boolean sameHost(HttpUrl url, String base) {
        HttpUrl api = HttpUrl.parse(base);
        return api != null && url.host().equals(api.host());
    }

    /**
     * 跳转登录
     *
     * @param str
     */
    private void checkLoginCode(String str) {
        try {
            if (!TextUtils.isEmpty(str) && str.contains(LOGIN_STATE_KEY)) {
                JSONObject object = new JSONObject(str);
                if (!object.isNull(LOGIN_STATE_KEY)) {
                    if (LOGIN_STATE_FAIL == object.getInt(LOGIN_STATE_KEY)) {
                        if (config.onTokenExpired != null) {
                            config.onTokenExpired.run();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
