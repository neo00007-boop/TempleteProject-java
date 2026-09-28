package com.lib.base.ui.fragment;

import android.annotation.SuppressLint;

import androidx.viewbinding.ViewBinding;

import com.lib.base.mvp.BaseContract;

/**
 * fragment基类增加mvp模式。
 * mvp和mvvm{@link BaseMvvmFragment}可选择性使用。
 *
 * @author xwchen
 * Date          2019/11/28.
 */
@SuppressLint("Registered")
public abstract class BaseMvpFragment<S extends ViewBinding, T extends BaseContract.BasePresenter> extends BaseFragment<S> {
    protected T mPresenter;

    /**
     * bind presenter
     *
     * @return
     */
    protected abstract T bindPresenter();

    @Override
    public void initDevelopmentMode() {
        mPresenter = bindPresenter();
        mPresenter.attachView((BaseContract.BaseView) this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        mPresenter = null;
    }
}
