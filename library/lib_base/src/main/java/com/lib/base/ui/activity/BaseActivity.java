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

import com.lib.base.R;
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
 * 1.activity基类,可以选择标题栏主题样式;
 * 2.使用ViewBinding;
 * 3.沉浸式标题栏,可设置多个属性;
 * 4.自带默认转场动画,可禁用;
 * 5.销毁时自动取消RxJava执行的任务;
 * 6.log,toast,intent...
 *
 * @author xwchen
 * Date on 2019/11/28.
 */
@SuppressLint("Registered")
public abstract class BaseActivity<T extends ViewBinding> extends ViewBindingActivity<T>
        implements InitAction, TitleBarAction, /*ImmersionAvtion,*/ LogAction, ToastAction,
        KeyboardAction, ClickAction, StatusAction, IntentAction/*, OverridePendingTransitionAction*/ {
    public static final String TAG = "BaseActivity";
    protected Bundle  bundle;
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
        int activityTheme = getTitleTheme();
        View innerView = mViewBinding.getRoot();
        if (TitleBarTheme.THEME_NONE != activityTheme) {
            //setContentView
            BaseTitleLayoutBinding binding = getContentView(innerView);
            //初始化
            titleBar = binding.titleBar;
            initTitleBar(this, activityTheme);
            //沉浸式
            setImmersionBar(TitleBarTheme.THEME_WHITE == activityTheme ? R.color.cl_white : R.color.common_accent_color);
        } else {
            setContentView(innerView);
            setStatueBarTextColorDark(darkStatusBarFont());
        }
        //提供一个供子类使用的ViewBinding对象
        super.mViewBinding = mViewBinding;
    }

    public BaseTitleLayoutBinding getContentView(View innerView) {
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
