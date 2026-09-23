package com.lib.base.ui.fragment;

import com.lib.base.mvvm.BaseViewModel;
import com.lib.base.util.inject.MyException;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.viewbinding.ViewBinding;

/**
 * ProjectName  XSCat
 * PackageName  com.bigheadhorse.xscat.view.activity.common
 *
 * @author xwchen
 * Date         2021/6/16.
 */

public abstract class BaseMvvmFragment<T extends ViewBinding, S extends BaseViewModel> extends BaseFragment<T> {
    public static final String TAG = "BaseMvvmActivity";
    protected S mViewModel;

    /**
     * getViewModelClass
     *
     * @return
     */
    protected abstract Class<S> getViewModelClass();

    /**
     * 是否使用父activity的ViewModelStoreOwner:
     * 1)是:如果此时S类型{@link BaseMvvmFragment#mViewModel}和activity中S一样,则他们是同一个实例对象,即activity和fragment共享同一个ViewModel;
     * 2)否:创建的ViewModel跟activity没有任何关系,即使他们使用的是同一个S的class类型.
     *
     * @return
     */
    public abstract boolean userParentViewModelStoreOwner();

    @Override
    public void initDevelopmentMode() {
        Class<S> viewModelClass = getViewModelClass();
        if (viewModelClass == null) {
            throw new MyException("getViewModelClass() return mustn't be null!");
        }
        mViewModel = new ViewModelProvider(userParentViewModelStoreOwner() ? requireActivity() : this).get(viewModelClass);
    }


    /**
     * 实例{@link BaseMvvmFragment#mViewModel}以外的ViewModel
     *
     * @param clazz
     * @param userParentViewModelStoreOwner 此处参考{@link BaseMvvmFragment#userParentViewModelStoreOwner()}
     * @param <U>
     * @return
     */
    public <U extends BaseViewModel> U createViewModel(Class<U> clazz, boolean userParentViewModelStoreOwner) {
        if (clazz == null) {
            throw new MyException("clazz mustn't be null!");
        }
        ViewModelStoreOwner owner = userParentViewModelStoreOwner ? requireActivity() : this;
        return new ViewModelProvider(owner).get(clazz);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mViewModel = null;
    }
}
