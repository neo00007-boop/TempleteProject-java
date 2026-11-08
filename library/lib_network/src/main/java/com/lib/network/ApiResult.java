package com.lib.network;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 统一响应体：{@code code}、{@code msg}、{@code data}。
 * {@code data} 用泛型，对象和列表都走这一个类。
 * <pre>
 * Single&lt;ApiResult&lt;UserBean&gt;&gt;           // data 是对象
 * Single&lt;ApiResult&lt;List&lt;UserBean&gt;&gt;&gt;    // data 是数组
 * </pre>
 * 成功码默认 {@code 0} 和 {@code 200}。接口字段不是这两个时，启动时调一次 {@link #setSuccessCodes(int...)}。
 */
public class ApiResult<T> {

    /** 响应里没有 code，或 code 不是数字。 */
    public static final int CODE_UNKNOWN = Integer.MIN_VALUE;

    private static volatile Set<Integer> successCodes = new HashSet<>(Arrays.asList(0, 200));

    private int code = CODE_UNKNOWN;
    private String msg;
    private T data;

    /**
     * 替换成功码。传空则没有任何码算成功。
     */
    public static void setSuccessCodes(int... codes) {
        Set<Integer> next = new HashSet<>();
        if (codes != null) {
            for (int code : codes) {
                next.add(code);
            }
        }
        successCodes = next;
    }

    public static Set<Integer> getSuccessCodes() {
        return Collections.unmodifiableSet(successCodes);
    }

    /**
     * 只看 code 是否在成功码里，不看 data。
     */
    public boolean isCodeSuccess() {
        return successCodes.contains(code);
    }

    /**
     * 同 {@link #isCodeSuccess()}。没有 data 的接口用这个。
     */
    public boolean isSuccess() {
        return isCodeSuccess();
    }

    /**
     * code 成功，并且 {@link #isDataEmpty()} 为 false。
     */
    public boolean hasData() {
        return isCodeSuccess() && !isDataEmpty();
    }

    /**
     * code 成功，并且 data 是非空 List（或数组）。
     */
    public boolean hasListData() {
        return isCodeSuccess() && !isListDataEmpty();
    }

    /**
     * data 为 null、空字符串、空 List、空数组、空 Map 时为空。对象实例不算空。
     */
    public boolean isDataEmpty() {
        return isEmptyValue(data);
    }

    /**
     * data 不是 List 或数组，或者里面没有元素。
     */
    public boolean isListDataEmpty() {
        if (data instanceof Collection) {
            return ((Collection<?>) data).isEmpty();
        }
        return !dataIsArray() || Array.getLength(data) == 0;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private boolean dataIsArray() {
        return data != null && data.getClass().isArray();
    }

    private static boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        return false;
    }
}
