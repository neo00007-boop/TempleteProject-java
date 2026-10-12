package com.lib.base.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavCallback;
import com.lib.base.R;
import com.lib.base.config.App;
import com.lib.base.config.AppConfig;
import com.lib.router.RoutePath;
import com.lib.router.Router;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 登录态和登录页跳转的公共入口，按 {@link RoutePath#LOGIN_ACTIVITY} 打开登录页，不依赖登录模块。
 *
 * @author xwchen
 * Date         2019-12-02.
 */
public class LoginUtil {

    /**
     * 登录页正在打开或已经打开。登录页 onCreate 置为 true，finish 时置回 false。
     */
    private static final AtomicBoolean sLoginShowing = new AtomicBoolean(false);
    private static final Handler sMain = new Handler(Looper.getMainLooper());

    /**
     * 多个接口同时 token 失效、或已经在登录页时，只会有一个登录页。
     */
    public static void toLogin() {
        if (!sLoginShowing.compareAndSet(false, true)) {
            return;
        }
        sMain.post(LoginUtil::openLogin);
    }

    /**
     * 登录页 onCreate 调用。登录页不一定经过 toLogin 打开（进程被回收后系统重建、直接走路由打开），
     * 这时标记还是 false，不补上会叠出第二个登录页
     */
    public static void onLoginShown() {
        sLoginShowing.set(true);
    }

    /** 登录页 finish 后调用，避免下次失效再也进不了登录页 */
    public static void onLoginFinished() {
        sLoginShowing.set(false);
    }

    private static void openLogin() {
        try {
            Activity topActivity = App.getContext().getTopActivity();
            if (!ContextUtil.isActivitySurvive(topActivity)) {
                sLoginShowing.set(false);
                return;
            }
            Router.build(RoutePath.LOGIN_ACTIVITY)
                    .withTransition(R.anim.bottom_in_anim, R.anim.no_anim)
                    .withString(RoutePath.LOGIN_KEY_DATA, "1000")
                    .navigation(topActivity, new NavCallback() {
                        @Override
                        public void onArrival(Postcard postcard) {
                        }

                        @Override
                        public void onLost(Postcard postcard) {
                            sLoginShowing.set(false);
                        }

                        @Override
                        public void onInterrupt(Postcard postcard) {
                            sLoginShowing.set(false);
                        }
                    });
        } catch (Exception e) {
            sLoginShowing.set(false);
            e.printStackTrace();
        }
    }

    public static boolean isLogin() {
        return !TextUtils.isEmpty(SPUtil.getString(AppConfig.USER_PHONE_NUM));
    }

    public static String getUserNum() {
        return SPUtil.getString(AppConfig.USER_PHONE_NUM);
    }

    public static void clear() {
        SPUtil.clear();
    }

    public static void restartApplication() {
        try {
            Activity activity = App.getContext().getTopActivity();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
            if (intent == null) {
                return;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            activity.finish();
            //exit
            ActivityManager mActivityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
                if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
                    android.os.Process.killProcess(runningAppProcessInfo.pid);
                }
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
