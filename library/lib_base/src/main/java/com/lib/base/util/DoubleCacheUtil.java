package com.lib.base.util;

import android.util.LruCache;

import com.google.gson.annotations.SerializedName;
import com.greendao.db.util.GsonUtil;
import com.lib.base.util.encrypt.base.TextUtils;

import java.lang.reflect.Type;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 双缓存工具类(内存LruCache+sp),可以设置缓存时长
 * <p>
 * PackageName  com.lib.base.util
 * ProjectName  TempleteProject-java
 * Date         2023/2/28.
 *
 * @author dave
 */

public class DoubleCacheUtil {
    public static final int MAX_CACHE_SIZE = 20 * 1024 * 1024;//LruCache最大内存缓存大小20MB
    public static LruCache<String, CacheBean> mMemoryCache;

    static {
        mMemoryCache = new LruCache<String, CacheBean>(MAX_CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, CacheBean value) {
                if (value == null) {
                    return 1;
                }
                String json = value.content /*GsonUtil.toJson(value)*/;
                return !TextUtils.isEmpty(json) ? json.getBytes().length : 1;
            }
        };
    }

    /**
     * 保存无缓存数据
     *
     * @param key
     * @param obj
     * @return true success/false fail
     */
    public static boolean put(String key, Object obj) {
        return put(key, obj, 0);
    }

    /**
     * 保存缓存数据
     *
     * @param key
     * @param obj
     * @param cacheTimeSeconds 秒
     * @return
     */
    public static boolean put(String key, Object obj, long cacheTimeSeconds) {
        if (TextUtils.isEmpty(key) || obj == null) {
            return false;
        }
        CacheBean bean = new CacheBean(obj, cacheTimeSeconds);
        if (!(obj instanceof String) && TextUtils.isEmpty(bean.content)) {
            return false;
        }
        String json = GsonUtil.toJson(bean);
        if (TextUtils.isEmpty(json)) {
            return false;
        }
        mMemoryCache.put(key, bean);
        SPUtil.put(key, json);
        return true;
    }

    /**
     * 获取未超时数据,包括基本数据和结构对象数据:
     * 1.基本类型数据最好使用包装类接收进行判空操作;
     * 2.保存的list和map数据使用下面方法获取.
     *
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T get(String key, Class<T> clazz) {
        return get(key, (Type) clazz);
    }

    /**
     * 每次都从 content 重新解析出新对象，调用方修改返回值不会影响缓存
     */
    public static <T> T get(String key, Type type) {
        try {
            if (TextUtils.isEmpty(key) || type == null) {
                return null;
            }
            CacheBean bean = getCacheBean(key);
            if (bean == null) {
                return null;
            }
            if (String.class.equals(type)) {
                return (T) bean.content;
            }
            return GsonUtil.fromJson(bean.content, type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 先查内存再查 sp，过期则两边都清掉
     */
    @Nullable
    private static CacheBean getCacheBean(@NonNull String key) {
        CacheBean bean = mMemoryCache.get(key);
        if (bean == null) {
            String json = SPUtil.getString(key);
            if (TextUtils.isEmpty(json)) {
                return null;
            }
            bean = GsonUtil.fromJsonObj(json, CacheBean.class);
            if (bean == null) {
                return null;
            }
            mMemoryCache.put(key, bean);
        }
        if (bean.cacheTimeSeconds > 0
                && bean.timeWhenCache + bean.cacheTimeSeconds * 1000 < System.currentTimeMillis()) {
            mMemoryCache.remove(key);
            SPUtil.put(key, "");
            return null;
        }
        return bean;
    }

    public static class CacheBean {

        @SerializedName("timeWhenCache")
        public long timeWhenCache;
        @SerializedName("cacheTimeSeconds")
        public long cacheTimeSeconds;
        @SerializedName("content")
        public String content;

        private CacheBean() {
        }

        public CacheBean(Object data, long cacheTimeSeconds) {
            this.content = data instanceof String ? (String) data : GsonUtil.toJson(data);
            this.cacheTimeSeconds = cacheTimeSeconds > 0 ? cacheTimeSeconds : -1;
            this.timeWhenCache = cacheTimeSeconds > 0 ? System.currentTimeMillis() : -1;
        }

        public CacheBean(Object data) {
            this(data, -1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheBean cacheBean = (CacheBean) o;
            return Objects.equals(content, cacheBean.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }
    }
}
