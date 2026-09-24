package com.lib.base.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.lib.base.config.App;
import com.lib.base.mvvm.GlobalViewModel;
import com.lib.base.ui.action.BaseAction;
import com.lib.base.ui.action.BundleAction;
import com.lib.base.ui.action.ClickAction;
import com.lib.base.ui.action.InitAction;
import com.lib.base.ui.action.KeyboardAction;
import com.lib.base.ui.action.LogAction;
import com.lib.base.ui.action.StatusAction;
import com.lib.base.ui.action.ToastAction;
import com.lib.base.ui.action.ViewBindingFragment;
import com.lib.base.ui.activity.BaseActivity;


/**
 * 1.fragment基类,内存不足回收时自动保存状态,防止多fragment多层展示;
 * 2.使用ViewBinding;
 * 3.销毁时自动取消RxJava执行的任务.
 * Created by Dave on 2017/1/11.
 */
public abstract class BaseFragment<T extends ViewBinding> extends ViewBindingFragment<T>
        implements InitAction, LogAction, ToastAction, KeyboardAction, BaseAction, ClickAction,
        BundleAction, StatusAction {
    public static final String TAG = "BaseFragment";
    protected Bundle bundle;

    /**
     * tab切换后fragment回调
     */
    public void tabClick() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        T mViewBinding = viewBinding(inflater, container);
        if (mViewBinding != null) {
            super.mViewBinding = mViewBinding;
            return mViewBinding.getRoot();
        } else {
            return super.onCreateView(inflater, container, bundle);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initBundle(bundle);
        initDevelopmentMode();
        inits();
        initView();
        initEvent();
        initData();
    }

    private void initBundle(@Nullable Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * 注意绑定fragment的activity的类型
     *
     * @return
     */
    protected BaseActivity<?> getBaseActivity() {
        Activity activity = requireActivity();
        if (!(activity instanceof BaseActivity)) {
            throw new RuntimeException("attach activity is not BaseActivity!");
        }
        return (BaseActivity<?>) activity;
    }

    @Nullable
    @Override
    public Bundle getBundle() {
        return getArguments();
    }

    @NonNull
    @Override
    public Context getCurCtx() {
        return requireContext();
    }

    @NonNull
    @Override
    public Activity getCurAty() {
        return requireActivity();
    }

    @Override
    public <S extends View> S findViewByIds(int id) {
        return requireView().findViewById(id);
    }

    protected GlobalViewModel getGlobalUserStateViewModel() {
        return ((App) requireActivity().getApplication()).getGlobalViewModel();
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        bundle = null;
//    }
}
