package com.templete.project;

import android.annotation.SuppressLint;
import android.content.Context;

import com.lib.base.config.App;

import androidx.multidex.MultiDex;

/**
 * @author xwchen
 * Date         2021/10/11.
 */
public class DemoMainApp extends App {
    public static final String TAG = "MainApp";
    @SuppressLint("StaticFieldLeak")
    private static DemoMainApp mainApp;

    @Override
    public void onCreate() {
        mainApp = this;
        super.onCreate();
        // 如果不用百分比布局这一套，可以使用修改density的方案（二选一不要同时使用）
        // 参考375设计稿，直接使用测量值的dp，sp
        // ScreenAdapter.init(this, 375f);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static DemoMainApp getMainApp() {
        return mainApp;
    }

}
