package com.lib.base.config;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.view.Gravity;

import com.greendao.db.util.GsonUtil;
import com.hjq.gson.factory.GsonFactory;
import com.hjq.shape.view.horizontal.SmartRefreshHorizontal;
import com.hjq.toast.CustomToast;
import com.hjq.toast.ToastStrategy;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.config.IToast;
import com.lib.base.BuildConfig;
import com.lib.base.R;
import com.lib.base.glide.GlideApp;
import com.lib.base.mvvm.GlobalViewModel;
import com.lib.base.ui.OverlayTextDrawableUtil;
import com.lib.base.ui.action.LogAction;
import com.lib.base.ui.widget.BlackToastStyle;
import com.lib.base.util.AppUtil;
import com.lib.base.util.ContextUtil;
import com.lib.base.util.DebugUtil;
import com.lib.base.util.LoginUtil;
import com.lib.base.util.SPUtil;
import com.lib.common.init.ModuleInitializers;
import com.lib.network.HttpConfig;
import com.lib.network.Hosts;
import com.lib.network.RetrofitHelper;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.wrapper.RefreshFooterWrapper;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

/**
 * application基类
 * PackageName  com.bigheadhorse.xscat.config
 * ProjectName  NumericalCodeProject
 *
 * @author xwchen
 * Date         2019-11-27.
 */
@SuppressLint("CheckResult")
public abstract class App extends Application implements ViewModelStoreOwner, LogAction, CameraXConfig.Provider {
    public static final String TAG = "BaseApplication";
    @SuppressLint("StaticFieldLeak")
    private static App mContext;
    private SoftReference<Activity> topActivity;
    private ViewModelStore globalViewModelStore;
    private GlobalViewModel globalViewModel;

    public static App getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        init();
        logD("startTime", "startTime1=" + System.currentTimeMillis());
    }

    private void init() {
        //处理RxJava中未处理异常
        setRxJavaErrorHandler();
        // 初始化 Toast 框架
        initToast();
        //网络，模块初始化里可能发请求，要放在前面
        initNetwork();
        //各模块初始化：数据库、路由、业务模块，按优先级执行
        ModuleInitializers.init(this);
        //json解析异常回调
        setGsonErrorHandler();
        //life
        setLifeObserve();
        //smart refresh
        setFreshHeaderOrFooter();
        //viewModel
        setViewModel();
        //注册网络状态变化监听
        setNetWorkListener();
        //debug
        if (BuildConfig.DEBUG) {
            OverlayTextDrawableUtil.setENABLE(true);
            OverlayTextDrawableUtil.debug(this);
        }
    }

    private void initNetwork() {
        RetrofitHelper.init(new HttpConfig.Builder(AppConfig.getUrl())
                .host(Hosts.KEY1, AppConfig.API_BASE_URL_FINAL1)
                .host(Hosts.KEY2, AppConfig.API_BASE_URL_FINAL2)
                .header("client_type", "android")
                .header("version_code", String.valueOf(AppUtil.getVersionCode(this)))
                .header("version_name", String.valueOf(AppUtil.getVersionName(this)))
                .tokenProvider(() -> SPUtil.getString(AppConfig.USER_TOKEN))
                .onTokenExpired(LoginUtil::toLogin)
                .gson(GsonUtil.getInstance())
                .cookiePath(AppConfig.COOKIE_URL_PATH)
                .debug(DebugUtil.isDebug)
                .build());
    }

    /**
     * toast
     */
    private void initToast() {
        ToastUtils.init(this, new ToastStrategy() {
            @Override
            public IToast createToast(Application application) {
                IToast toast = super.createToast(application);
                if (toast instanceof CustomToast) {
                    CustomToast customToast = ((CustomToast) toast);
                    // 设置 Toast 动画效果
                    //customToast.setAnimationsId(R.anim.xxx);
                    // 设置短 Toast 的显示时长（默认是 2000 毫秒）
                    customToast.setShortDuration(1000);
                    // 设置长 Toast 的显示时长（默认是 3500 毫秒）
                    customToast.setLongDuration(1000);
                }
                return toast;
            }
        });
        ToastUtils.setStyle(new BlackToastStyle());
        ToastUtils.setGravity(Gravity.CENTER);
    }

    @SuppressLint("MissingPermission")
    private void setNetWorkListener() {
        ConnectivityManager connectivityManager = ContextCompat.getSystemService(mContext, ConnectivityManager.class);
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    toastInfo(R.string.common_network_error);
                }

                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    toastInfo(R.string.common_network_ok);
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            DebugUtil.logD("NetWork", "wifi已经连接");
                        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            DebugUtil.logD("NetWork", "数据流量已经连接");
                        } else {
                            DebugUtil.logD("NetWork", "其他网络");
                        }
                    }
                }
            });
        }
    }

    private void toastInfo(int resId) {
        try {
            Activity topActivity = getTopActivity();
            if (ContextUtil.isActivityResume(topActivity)) {
                topActivity.runOnUiThread(() -> DebugUtil.debugToast(getResources().getString(resId)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Json解析容错监听
    private void setGsonErrorHandler() {
        GsonFactory.setJsonCallback((typeToken, fieldName, jsonToken) -> {
            try {
                String info = "类型解析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken;
                DebugUtil.logD(GsonUtil.TAG, info);
                Activity topActivity = getTopActivity();
                if (ContextUtil.isActivityResume(topActivity)) {
                    topActivity.runOnUiThread(() -> DebugUtil.debugToast(info));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setViewModel() {
        globalViewModelStore = new ViewModelStore();
        globalViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this)).get(GlobalViewModel.class);
        globalViewModel.init();
    }

    public GlobalViewModel getGlobalViewModel() {
        return globalViewModel;
    }

    private void setFreshHeaderOrFooter() {
        /*垂直刷新*/
        // 设置全局的 Header 构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> new ClassicsHeader(context));
        // 设置全局的 Footer 构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator((context, layout) -> {
             /*ClassicsFooter classicsFooter = new ClassicsFooter(context);
            classicsFooter.setSpinnerStyle(SpinnerStyle.Translate);
            return classicsFooter;*/
            return new ClassicsFooter(context);
        });
        // 设置全局初始化器
        SmartRefreshLayout.setDefaultRefreshInitializer((cx, layout) -> {
            // 刷新头部是否跟随内容偏移
            layout.setEnableHeaderTranslationContent(true)
                    // 刷新尾部是否跟随内容偏移
                    .setEnableFooterTranslationContent(true)
                    // 加载更多是否跟随内容偏移
                    .setEnableFooterFollowWhenNoMoreData(true)
                    // 内容不满一页时是否可以上拉加载更多
                    .setEnableLoadMoreWhenContentNotFull(false)
                    // 仿苹果越界效果开关
                    .setEnableOverScrollDrag(true);
        });

        /*水平刷新*/
        SmartRefreshHorizontal.setDefaultRefreshInitializer((context, layout) -> {
            layout.setEnableAutoLoadMore(true);
            //layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);
        });
        SmartRefreshHorizontal.setDefaultRefreshHeaderCreator((context, layout) -> {
            layout.setEnableHeaderTranslationContent(true);
            return new MaterialHeader(context);
        });
        SmartRefreshHorizontal.setDefaultRefreshFooterCreator((context, layout) -> {
            layout.setEnableFooterTranslationContent(true);
            return new RefreshFooterWrapper(new MaterialHeader(context));
        });
    }

    private void setLifeObserve() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallback() {
            @Override
            public void resumeActivity(Activity activity) {
                topActivity = new SoftReference<>(activity);
            }
        });
    }

    public Activity getTopActivity() {
        if (topActivity != null) {
            Activity activity = topActivity.get();
            return activity != null && !activity.isDestroyed() && !activity.isFinishing() ? activity : null;
        }
        return null;
    }

    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(throwable -> DebugUtil.logE("RxJavaError", "error msg=" + throwable.getMessage()));
    }

    @NotNull
    @Override
    public ViewModelStore getViewModelStore() {
        return globalViewModelStore;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // 清理所有图片内存缓存
        GlideApp.get(this).onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // 根据手机内存剩余情况清理图片内存缓存
        GlideApp.get(this).onTrimMemory(level);
    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
