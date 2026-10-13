package com.greendao.db.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.hjq.gson.factory.GsonFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by Dave on 2020/6/8.
 */
public class GsonUtil {
    public static final String TAG = "GSON_LOG";
    private static volatile Gson gson;
    private static volatile Gson gsonNoCrash;

    /**
     * 常规gson
     *
     * @return
     */
    public static Gson getCommonInstance() {
        if (gson == null) {
            synchronized (GsonUtil.class.getName()) {
                if (gson == null) {
                    gson = new GsonBuilder().serializeNulls().create();
                    useMapTypeAdapter(gson);
                }
            }
        }
        return gson;
    }

    /**
     * 解析容错gson
     *
     * @return
     */
    public static Gson getNoCrashInstance() {
        //return GsonFactory.getSingletonGson();
        if (gsonNoCrash == null) {
            synchronized (GsonFactory.class) {
                if (gsonNoCrash == null) {
                    gsonNoCrash = GsonFactory.newGsonBuilder().create();
                    useMapTypeAdapter(gsonNoCrash);
                }
            }
        }
        return gsonNoCrash;
    }

    /**
     * 解决 Gson 解析成 Object / Map 时把 int 转成 double 的问题。
     * Gson 2.8.6 没有公开接口替换 Object 的解析，这里往 Gson 自己的适配器缓存里预先放入 MapTypeAdapter，
     * 不能反射 Collections 内部字段，Android 9 起属于非公开接口，会抛 NoSuchFieldException
     */
    @SuppressWarnings("unchecked")
    private static void useMapTypeAdapter(Gson target) {
        try {
            Field cacheField = Gson.class.getDeclaredField("typeTokenCache");
            cacheField.setAccessible(true);
            Map<TypeToken<?>, TypeAdapter<?>> cache = (Map<TypeToken<?>, TypeAdapter<?>>) cacheField.get(target);
            TypeToken<Object> objectType = TypeToken.get(Object.class);
            cache.put(objectType, MapTypeAdapter.FACTORY.create(target, objectType));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 默认解析容错gson
     *
     * @return
     */
    public static Gson getInstance() {
        return getNoCrashInstance();
    }

    /**
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        try {
            if (obj == null) {
                return "";
            }
            return getInstance().toJson(obj);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * @param content
     * @param obj
     * @return
     */
    public static <T> T fromJsonObj(String content, Class<T> obj) {
        try {
            if (TextUtils.isEmpty(content) || obj == null) {
                return null;
            }
            return getInstance().fromJson(content, obj);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * @param content
     * @param obj
     * @return
     */
    public static <T> List<T> fromJsonList(String content, Class<T> obj) {
        try {
            if (TextUtils.isEmpty(content) || obj == null) {
                return null;
            }
            return getInstance().fromJson(content, TypeToken.getParameterized(List.class, obj).getType());
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * @param content
     * @param key
     * @param value
     * @return
     */
    public static <T, S> Map<T, S> fromJsonMap(String content, Class<T> key, Class<S> value) {
        try {
            if (TextUtils.isEmpty(content) || key == null || value == null) {
                return null;
            }
            return getInstance().fromJson(content, TypeToken.getParameterized(Map.class, key, value).getType());
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * 如：
     * 1、Type type1 = new TypeToken<List<User>>(){}.getType();
     * 2、Type type2 = new TypeToken<Map<String,User>>(){}.getType();
     *
     * @param content
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String content, Type type) {
        try {
            if (TextUtils.isEmpty(content) || type == null) {
                return null;
            }
            return getInstance().fromJson(content, type);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
