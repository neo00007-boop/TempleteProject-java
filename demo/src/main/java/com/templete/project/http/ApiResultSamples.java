package com.templete.project.http;

import androidx.annotation.Nullable;

/**
 * 统一返回体的几段样例 JSON。Demo 拦截器按路径原样返回，不访问服务器。
 * {@code code == -1} 会被网络层当成登录失效，样例里的业务失败用 400。
 */
public final class ApiResultSamples {

    public static final String DETAIL = "/demo/api/user/detail";
    public static final String LIST = "/demo/api/user/list";
    public static final String DELETE = "/demo/api/user/delete";
    public static final String EMPTY = "/demo/api/user/empty";
    public static final String FAIL = "/demo/api/user/fail";

    public static final String DETAIL_JSON = "{\"code\":0,\"msg\":\"ok\",\"data\":{\"id\":7,\"name\":\"ann\"}}";
    public static final String LIST_JSON = "{\"code\":\"200\",\"message\":\"done\",\"data\":[{\"id\":1,\"name\":\"a\"},{\"id\":2,\"name\":\"b\"}]}";
    public static final String DELETE_JSON = "{\"code\":0,\"msg\":\"已删除\"}";
    public static final String EMPTY_JSON = "{\"code\":0,\"msg\":\"ok\",\"data\":\"\"}";
    public static final String FAIL_JSON = "{\"code\":400,\"errorMsg\":\"用户不存在\"}";

    private ApiResultSamples() {
    }

    @Nullable
    public static String body(@Nullable String path) {
        if (DETAIL.equals(path)) {
            return DETAIL_JSON;
        }
        if (LIST.equals(path)) {
            return LIST_JSON;
        }
        if (DELETE.equals(path)) {
            return DELETE_JSON;
        }
        if (EMPTY.equals(path)) {
            return EMPTY_JSON;
        }
        if (FAIL.equals(path)) {
            return FAIL_JSON;
        }
        return null;
    }
}
