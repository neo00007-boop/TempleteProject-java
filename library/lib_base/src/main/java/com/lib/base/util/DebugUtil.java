package com.lib.base.util;


import android.util.Log;

import com.hjq.toast.Toaster;


/**
 * 调试工具
 * Created by Dave on 2017/1/6.
 */
public class DebugUtil {
    public static boolean isDebug = true;
    public static String LOG_TAG = "_LOG_TAG";
    //private static Toasts toasts;

    public static void logD(String tag, String str) {
        if (isDebug) {
            BaseLog.d(tag + LOG_TAG, str);
            Log.d(tag + LOG_TAG, str);
        }
    }

    public static void logE(String tag, String str) {
        if (isDebug) {
            BaseLog.e(tag + LOG_TAG, str);
            Log.e(tag + LOG_TAG, str);
        }
    }

    public static void toast(String str) {
        Toaster.show(str);
    }

    public static void debugToast(String str) {
        Toaster.debugShow(str);
    }

}
