package com.lib.common.init;

import android.app.Application;

/**
 * 模块初始化入口。模块需要在启动时做事，就实现这个接口，
 * 并在 src/main/resources/META-INF/services/com.lib.common.init.ModuleInitializer 里写上实现类全名。
 * 壳工程不需要知道有哪些模块，增删模块不用改壳工程代码。
 */
public interface ModuleInitializer {

    int PRIORITY_DATA = -200;
    int PRIORITY_ROUTER = -100;
    int PRIORITY_DEFAULT = 0;

    /**
     * 数值小的先执行。依赖路由的模块不要小于 {@link #PRIORITY_ROUTER}。
     */
    default int priority() {
        return PRIORITY_DEFAULT;
    }

    /**
     * 在主线程、Application.onCreate 里调用，耗时任务自己切线程。
     */
    void onCreate(Application application);
}
