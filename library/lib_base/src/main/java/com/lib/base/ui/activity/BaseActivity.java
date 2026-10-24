package com.lib.base.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.lib.base.databinding.BaseTitleLayoutBinding;
import com.lib.base.ui.action.ClickAction;
import com.lib.base.ui.action.InitAction;
import com.lib.base.ui.action.IntentAction;
import com.lib.base.ui.action.KeyboardAction;
import com.lib.base.ui.action.LogAction;
import com.lib.base.ui.action.StatusAction;
import com.lib.base.ui.action.TitleBarAction;
import com.lib.base.ui.action.ToastAction;
import com.lib.base.ui.action.ViewBindingActivity;
import com.lib.base.ui.widget.TitleBar;

/**
 * Activity 基类。
 * <p>
 * 标题栏两种用法：
 * <ul>
 *   <li>{@link #getTitleTheme()} ≠ {@link TitleBarTheme#THEME_NONE}：Base 包裹 TitleBar，
 *   状态栏颜色/图标跟主题走（见 {@link TitleBarTheme}）。</li>
 *   <li>{@link TitleBarTheme#THEME_NONE}：内容区自管；Base 只做透明沉浸式，
 *   适合 Tab 宿主 / 布局内自带 TitleBar。</li>
 * </ul>
 *
 * @author xwchen
 */
@SuppressLint("Registered")
public abstract class BaseActivity<T extends ViewBinding> extends ViewBindingActivity<T>
        implements InitAction, TitleBarAction, LogAction, ToastAction,
        KeyboardAction, ClickAction, StatusAction, IntentAction {
    public static final String TAG = "BaseActivity";
    protected Bundle bundle;
    private TitleBar titleBar;

    private void initBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initBundle(bundle);
        initContentBefore();
        initContentLayout();
        initDevelopmentMode();
        inits();
        initView();
        initEvent();
        initData();
    }

    private void initContentLayout() {
        T mViewBinding = viewBinding();
        if (mViewBinding == null) {
            throw new RuntimeException("viewBinding() must not return null!");
        }
        int theme = getTitleTheme();
        View innerView = mViewBinding.getRoot();
        if (!TitleBarTheme.isNone(theme)) {
            BaseTitleLayoutBinding binding = wrapWithTitleBar(innerView);
            titleBar = binding.titleBar;
            initTitleBar(this, theme);
            // 状态栏颜色 + 图标深浅统一跟 TitleBarTheme
            setImmersionBar();
        } else {
            setContentView(innerView);
            // 透明沉浸，图标按 darkStatusBarFont()（默认深色）
            setImmersionBar();
        }
        super.mViewBinding = mViewBinding;
    }

    /**
     * 用带 TitleBar 的壳包住业务布局。
     */
    public BaseTitleLayoutBinding wrapWithTitleBar(View innerView) {
        BaseTitleLayoutBinding mBaseBinding = BaseTitleLayoutBinding.inflate(getLayoutInflater());
        LinearLayout rootView = mBaseBinding.getRoot();
        rootView.addView(innerView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        setContentView(rootView);
        return mBaseBinding;
    }

    @Override
    public TitleBar getTitleBar() {
        return titleBar;
    }

    @Override
    public void actionFinish() {
        onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        hideKeyboard(mViewBinding.getRoot());
        finishAnim();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (actionTitleBackClick(true)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        bundle = null;
//        titleBar = null;
    }

    /**
     * action
     *
     * @return
     */
    protected float getDimen(int id) {
        return getResources().getDimension(id);
    }

    @Override
    public <S extends View> S findViewByIds(int id) {
        return findViewById(id);
    }

    @NonNull
    @Override
    public Context getCurrentContext() {
        return this;
    }

    @NonNull
    @Override
    public Activity getCurrentActivity() {
        return this;
    }

    @Override
    public Intent getIntents() {
        return getIntent();
    }
}
