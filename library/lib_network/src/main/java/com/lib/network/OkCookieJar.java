package com.lib.network;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * 处理cookie
 *
 * @author xwchen
 * Date         2022/2/22.
 */
public class OkCookieJar implements CookieJar {
    public static final String TAG = "OkCookieJar";
    private final ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();
    private final String cookiePath;

    OkCookieJar(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    /**
     * 1.只保存指定path的cookie,防止被其他coookie覆盖;
     * 2.使用host关联,后续该host都是用该cookie.
     */
    @Override
    public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> cookies) {
        String host = httpUrl.host();
        String currPath = httpUrl.encodedPath();
        if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(cookiePath) && cookiePath.equals(currPath)) {
            cookieStore.put(host, cookies);
        }
        HttpLog.d(TAG, "cookies =" + cookies);
    }

    /**
     * 取出域名关联的cookie
     */
    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl) {
        List<Cookie> cookies = cookieStore.get(httpUrl.host());
        return cookies != null ? cookies : new ArrayList<>();
    }
}
