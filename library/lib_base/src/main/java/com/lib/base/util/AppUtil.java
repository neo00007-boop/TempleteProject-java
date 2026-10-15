package com.lib.base.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.lib.base.config.App;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

/**
 * PackageName  com.hycg.company.utils
 * ProjectName  HYCGCompanyProject
 * @author      xwchen
 * Date         2019/4/16.
 */
public class AppUtil {
    private static PackageInfo getPackageInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    public static int getVersionCode(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        return packageInfo != null ? packageInfo.versionCode : 0;
    }

    public static String getVersionName(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        return packageInfo != null ? packageInfo.versionName : "0";
    }

    /**
     * 判断是否平板设备
     *
     * @param context
     * @return true:平板,false:手机
     */
    public static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isForeground(String className1, String className2) {
        boolean result = false;
        if (TextUtils.isEmpty(className1) && TextUtils.isEmpty(className2)) {
            result = false;
        } else {
            ActivityManager am = (ActivityManager) App.getContext().getSystemService(ACTIVITY_SERVICE);
            if (am == null) {
                result = false;
            } else {
                List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
                for (ActivityManager.RunningTaskInfo taskInfo : list) {
                    String className = taskInfo.topActivity.getShortClassName();
                    if (className.contains(className1) ||
                            className.contains(className2)) { // 说明它已经启动了
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static boolean isForegroundOne(String className) {
        boolean result = false;
        if (TextUtils.isEmpty(className)) {
            result = false;
        } else {
            ActivityManager am = (ActivityManager) App.getContext().getSystemService(ACTIVITY_SERVICE);
            if (am == null) {
                result = false;
            } else {
                List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
                for (ActivityManager.RunningTaskInfo taskInfo : list) {
                    String name = taskInfo.topActivity.getShortClassName();
                    if (name.contains(className)) { // 说明它已经启动了
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取 IMEI，拿不到返回 null。
     * Android 10 起普通应用无权获取，直接返回 null；Android 9 及以下需要 READ_PHONE_STATE 权限，且要在用户同意隐私政策后再调用
     */
    @Nullable
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getImei() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return null;
        }
        try {
            TelephonyManager tm = (TelephonyManager) App.getContext().getSystemService(TELEPHONY_SERVICE);
            return tm != null ? tm.getDeviceId() : null;
        } catch (SecurityException e) {
            return null;
        }
    }
}
