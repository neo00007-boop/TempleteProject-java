package com.greendao.db.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.hjq.gson.factory.GsonFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
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
                    // 解决gsonut默认将int转化为double问题
                    try {
                        Field factories = Gson.class.getDeclaredField("factories");
                        factories.setAccessible(true);
                        Object o = factories.get(gson);
                        Class<?>[] declaredClasses = Collections.class.getDeclaredClasses();
                        for (Class<?> c : declaredClasses) {
                            if ("java.util.Collections$UnmodifiableList".equals(c.getName())) {
                                Field listField = c.getDeclaredField("list");
                                listField.setAccessible(true);
                                List<TypeAdapterFactory> list = (List<TypeAdapterFactory>) listField.get(o);
                                if (list != null) {
                                    int i = list.indexOf(ObjectTypeAdapter.FACTORY);
                                    if (i >= 0) {
                                        list.set(i, MapTypeAdapter.FACTORY);
                                    }
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                    // 解决gsonut默认将int转化为double问题
                    try {
                        Field factories = Gson.class.getDeclaredField("factories");
                        factories.setAccessible(true);
                        Object o = factories.get(gsonNoCrash);
                        Class<?>[] declaredClasses = Collections.class.getDeclaredClasses();
                        for (Class<?> c : declaredClasses) {
                            if ("java.util.Collections$UnmodifiableList".equals(c.getName())) {
                                Field listField = c.getDeclaredField("list");
                                listField.setAccessible(true);
                                List<TypeAdapterFactory> list = (List<TypeAdapterFactory>) listField.get(o);
                                if (list != null) {
                                    int i = list.indexOf(ObjectTypeAdapter.FACTORY);
                                    if (i >= 0) {
                                        list.set(i, MapTypeAdapter.FACTORY);
                                    }
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return gsonNoCrash;
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
