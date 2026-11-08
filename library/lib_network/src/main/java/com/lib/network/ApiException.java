package com.lib.network;

/**
 * 业务失败。{@link ApiResults#unwrap()} 在 code 不成功或 data 为空时抛出，走 RxJava 的 onError。
 */
public class ApiException extends RuntimeException {

    private final int code;

    public ApiException(int code, String msg) {
        super(msg == null || msg.length() == 0 ? ("请求失败 " + code) : msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
