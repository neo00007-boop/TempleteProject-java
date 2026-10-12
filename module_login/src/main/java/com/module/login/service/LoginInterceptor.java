package com.module.login.service;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Interceptor;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.lib.base.util.LoginUtil;
import com.lib.router.RouteExtras;

/**
 * 页面路由带 {@link RouteExtras#NEED_LOGIN} 且未登录时，拦下并打开登录页。
 * 用法：{@code @Route(path = ..., extras = RouteExtras.NEED_LOGIN)}
 */
@Interceptor(priority = 1, name = "login")
public class LoginInterceptor implements IInterceptor {

    @Override
    public void process(Postcard postcard, InterceptorCallback callback) {
        if (RouteExtras.has(postcard.getExtra(), RouteExtras.NEED_LOGIN) && !LoginUtil.isLogin()) {
            LoginUtil.toLogin();
            callback.onInterrupt(new IllegalStateException("need login: " + postcard.getPath()));
            return;
        }
        callback.onContinue(postcard);
    }

    @Override
    public void init(Context context) {
    }
}
