package com.lib.router;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.launcher.ARouter;

/**
 * 路由统一入口。业务代码只用这里，不直接调 ARouter，以后换路由框架只改这一层。
 */
public final class Router {

    private Router() {
    }

    public static Postcard build(String path) {
        return ARouter.getInstance().build(path);
    }

    public static void open(Context context, String path) {
        build(path).navigation(context);
    }

    /**
     * 按接口取跨模块服务。对应模块没打进包时返回 null，调用方要判空。
     */
    public static <T extends IProvider> T service(Class<T> service) {
        return ARouter.getInstance().navigation(service);
    }

    public static void inject(Object target) {
        ARouter.getInstance().inject(target);
    }
}
