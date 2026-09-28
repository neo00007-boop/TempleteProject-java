package com.lib.base.ui.action;

import androidx.viewbinding.ViewBinding;

/**
 * ViewBinding抽象类
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.ui.action
 *
 * @author xwchen
 * Date         2022/2/8.
 */

public abstract class ViewBindingActivity<T extends ViewBinding> extends RxjavaActivity {
    protected T mViewBinding;

    /**
     * 获取ViewBinding实例
     *
     * @return
     */
    protected abstract T viewBinding();

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewBinding = null;
    }
}
