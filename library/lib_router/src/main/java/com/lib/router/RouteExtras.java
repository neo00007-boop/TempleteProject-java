package com.lib.router;

/**
 * 写在 {@code @Route(extras = ...)} 上的标记位，可以按位或组合。
 */
public final class RouteExtras {
    /**
     * 未登录时被登录拦截器拦下并跳转登录页，拦截器在 module_login 里。
     */
    public static final int NEED_LOGIN = 1;

    private RouteExtras() {
    }

    public static boolean has(int extras, int flag) {
        return (extras & flag) == flag;
    }
}
