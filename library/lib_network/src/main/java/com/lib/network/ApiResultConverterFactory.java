package com.lib.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 只接管 {@link ApiResult}。对象、列表都按接口上的泛型解析 data，其它类型仍交给 Gson。
 * 挂在 {@link RetrofitHelper} 里，业务不用自己注册。
 */
final class ApiResultConverterFactory extends Converter.Factory {

    private final Gson gson;

    ApiResultConverterFactory(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(type) != ApiResult.class) {
            return null;
        }
        final Type dataType = type instanceof java.lang.reflect.ParameterizedType
                ? getParameterUpperBound(0, (java.lang.reflect.ParameterizedType) type)
                : Object.class;
        return value -> {
            try {
                String json = value.string();
                return ApiResults.parseData(gson, json, dataType);
            } catch (JsonSyntaxException e) {
                throw new IOException(e.getMessage() == null ? "data 解析失败" : e.getMessage(), e);
            } finally {
                value.close();
            }
        };
    }
}
