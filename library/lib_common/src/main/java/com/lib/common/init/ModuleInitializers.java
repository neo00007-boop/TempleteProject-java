package com.lib.common.init;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 收集并按优先级执行所有模块的 {@link ModuleInitializer}。只在 Application.onCreate 调用一次。
 */
public final class ModuleInitializers {
    private static final String TAG = "ModuleInitializers";
    private static boolean initialized;

    private ModuleInitializers() {
    }

    public static synchronized void init(Application application) {
        if (initialized) {
            return;
        }
        initialized = true;
        List<ModuleInitializer> list = new ArrayList<>();
        for (ModuleInitializer initializer : ServiceLoader.load(ModuleInitializer.class, ModuleInitializer.class.getClassLoader())) {
            list.add(initializer);
        }
        Collections.sort(list, (a, b) -> Integer.compare(a.priority(), b.priority()));
        for (ModuleInitializer initializer : list) {
            long start = System.currentTimeMillis();
            try {
                initializer.onCreate(application);
            } catch (RuntimeException e) {
                // 一个模块初始化失败不拖垮整个 App 启动
                Log.e(TAG, initializer.getClass().getName() + " init failed", e);
            }
            Log.d(TAG, initializer.getClass().getName() + " cost " + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
