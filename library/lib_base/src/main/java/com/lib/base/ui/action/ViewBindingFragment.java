package com.lib.base.ui.action;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.viewbinding.ViewBinding;

/**
 * ViewBinding抽象类
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.ui.action
 *
 * @author xwchen
 * Date         2022/2/8.
 */

public abstract class ViewBindingFragment<T extends ViewBinding> extends RxjavaFragment {
    protected T mViewBinding;

    protected abstract T viewBinding(LayoutInflater inflater, ViewGroup container);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewBinding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewBinding = null;
    }
}
