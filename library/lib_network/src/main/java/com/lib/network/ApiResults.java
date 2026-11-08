package com.lib.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.SingleTransformer;

/**
 * 解析 {@link ApiResult}，以及从响应里取出 data。
 * Retrofit 已挂上 {@link ApiResultConverterFactory}，接口直接声明 {@code Single<ApiResult<T>>} 即可。
 */
public final class ApiResults {

    private static final String[] MSG_KEYS = {"msg", "message", "errorMsg", "errMsg"};

    private ApiResults() {
    }

    /**
     * code 成功且 data 非空时往下传 data，否则 onError 收到 {@link ApiException}。
     * 删改这类没有 data 的接口不要用这个，自己看 {@link ApiResult#isSuccess()}。
     */
    public static <T> SingleTransformer<ApiResult<T>, T> unwrap() {
        return upstream -> upstream.map(ApiResults::requireData);
    }

    public static <T> ObservableTransformer<ApiResult<T>, T> unwrapObservable() {
        return upstream -> upstream.map(ApiResults::requireData);
    }

    public static <T> FlowableTransformer<ApiResult<T>, T> unwrapFlowable() {
        return upstream -> upstream.map(ApiResults::requireData);
    }

    public static <T> T requireData(ApiResult<T> result) {
        if (result == null) {
            throw new ApiException(ApiResult.CODE_UNKNOWN, "响应为空");
        }
        if (!result.isSuccess()) {
            throw new ApiException(result.getCode(), result.getMsg());
        }
        T data = result.getData();
        if (data == null) {
            throw new ApiException(result.getCode(), "data 为空");
        }
        return data;
    }

    /**
     * @param type {@code new TypeToken<ApiResult<UserBean>>(){}.getType()}，列表同理换成 {@code ApiResult<List<UserBean>>}
     */
    public static <T> ApiResult<T> parse(Gson gson, String json, Type type) {
        Type dataType = Object.class;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            if (parameterized.getRawType() == ApiResult.class) {
                Type[] args = parameterized.getActualTypeArguments();
                if (args.length > 0 && args[0] != null) {
                    dataType = args[0];
                }
            }
        }
        return parseData(gson, json, dataType);
    }

    @SuppressWarnings("unchecked")
    static <T> ApiResult<T> parseData(Gson gson, String json, Type dataType) {
        ApiResult<T> result = new ApiResult<>();
        if (json == null || json.trim().length() == 0) {
            result.setMsg("响应为空");
            return result;
        }
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (JsonParseException e) {
            result.setMsg("响应不是 JSON");
            return result;
        }
        if (root == null || !root.isJsonObject()) {
            result.setMsg("响应不是 JSON 对象");
            return result;
        }
        JsonObject object = root.getAsJsonObject();
        result.setCode(readCode(object));
        result.setMsg(readMsg(object));
        if (!object.has("data") || object.get("data").isJsonNull()) {
            return result;
        }
        JsonElement dataElement = object.get("data");
        if (isEmptyString(dataElement) && dataType != String.class) {
            return result;
        }
        Gson parser = gson != null ? gson : new Gson();
        Type target = dataType != null ? dataType : Object.class;
        try {
            result.setData((T) parser.fromJson(dataElement, target));
        } catch (JsonSyntaxException e) {
            throw e;
        }
        return result;
    }

    private static int readCode(JsonObject object) {
        if (!object.has("code") || object.get("code").isJsonNull()) {
            return ApiResult.CODE_UNKNOWN;
        }
        JsonElement element = object.get("code");
        if (!element.isJsonPrimitive()) {
            return ApiResult.CODE_UNKNOWN;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        try {
            if (primitive.isNumber()) {
                return primitive.getAsInt();
            }
            if (primitive.isString()) {
                String text = primitive.getAsString().trim();
                if (text.length() == 0) {
                    return ApiResult.CODE_UNKNOWN;
                }
                return Integer.parseInt(text);
            }
        } catch (NumberFormatException ignored) {
            return ApiResult.CODE_UNKNOWN;
        }
        return ApiResult.CODE_UNKNOWN;
    }

    private static String readMsg(JsonObject object) {
        for (String key : MSG_KEYS) {
            if (!object.has(key) || object.get(key).isJsonNull()) {
                continue;
            }
            JsonElement element = object.get(key);
            if (element.isJsonPrimitive()) {
                return element.getAsString();
            }
        }
        return "";
    }

    private static boolean isEmptyString(JsonElement element) {
        return element.isJsonPrimitive()
                && element.getAsJsonPrimitive().isString()
                && element.getAsString().length() == 0;
    }
}
