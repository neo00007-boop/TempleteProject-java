package com.example.app;

import android.annotation.SuppressLint;
import android.content.Context;

import com.lib.base.config.App;

import androidx.multidex.MultiDex;

/**
 * 壳工程 Application。各模块的初始化由 {@link com.lib.startup.ModuleInitializer} 自动收集，这里不用逐个登记。
 *
 * @author xwchen
 * Date         2021/10/11.
 */
public class MainApp extends App {
    public static final String TAG = "MainApp";
    @SuppressLint("StaticFieldLeak")
    private static MainApp mainApp;

    @Override
    public void onCreate() {
        mainApp = this;
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static MainApp getMainApp() {
        return mainApp;
    }

}
