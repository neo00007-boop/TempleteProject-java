package com.lib.base.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.lib.base.R;
import com.lib.base.aroute.ArouteConfig;
import com.lib.base.config.App;
import com.lib.base.config.AppConfig;
import com.lib.base.http.RetrofitHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PackageName  com.bigheadhorse.xscat.utils
 * ProjectName  XSCat
 *
 * @author xwchen
 * Date         2019-12-02.
 */
public class LoginUtil {

    private static final String LOGIN_ACTIVITY = "com.module.login.ui.LoginActivity";
    private static final AtomicBoolean sOpeningLogin = new AtomicBoolean(false);
    private static final Handler sMain = new Handler(Looper.getMainLooper());

    /**
     * 多个接口同时 token 失效时只跳一次登录。
     * 登录页真正关闭后再允许下一次跳转，见 {@link #onLoginFinished()}。
     */
    public static void toLogin() {
        if (!sOpeningLogin.compareAndSet(false, true)) {
            return;
        }
        sMain.post(LoginUtil::openLogin);
    }

    /** 登录页 finish 后调用，避免下次失效再也进不了登录页 */
    public static void onLoginFinished() {
        sOpeningLogin.set(false);
    }

    private static void openLogin() {
        try {
            RetrofitHelper.getInstance().cancelAll();
            Activity topActivity = App.getContext().getTopActivity();
            if (!ContextUtil.isActivitySurvive(topActivity)) {
                sOpeningLogin.set(false);
                return;
            }
            if (LOGIN_ACTIVITY.equals(topActivity.getClass().getName())) {
                return;
            }
            ARouter.getInstance()
                    .build(ArouteConfig.ACTIVITY_LOGIN)
                    .withTransition(R.anim.bottom_in_anim, R.anim.no_anim)
                    .withString("data", "1000")
                    .navigation(topActivity);
        } catch (Exception e) {
            sOpeningLogin.set(false);
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

    /*public static boolean isOk(LoginRecord bean) {
        return bean.code == 200;
    }*/

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
