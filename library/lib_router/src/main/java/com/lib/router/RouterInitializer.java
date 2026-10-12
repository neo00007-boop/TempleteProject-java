package com.lib.router;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import com.alibaba.android.arouter.launcher.ARouter;
import com.lib.startup.ModuleInitializer;

public class RouterInitializer implements ModuleInitializer {

    @Override
    public int priority() {
        return PRIORITY_ROUTER;
    }

    @Override
    public void onCreate(Application application) {
        if ((application.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(application);
    }
}
