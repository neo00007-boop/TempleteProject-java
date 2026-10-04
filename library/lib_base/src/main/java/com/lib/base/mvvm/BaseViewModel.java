package com.lib.base.mvvm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.kunminx.architecture.domain.message.MutableResult;
import com.lib.base.bean.BaseData;
import com.lib.base.config.App;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created on 2021/2/21.
 */

public class BaseViewModel extends AndroidViewModel {
    /**
     * 注意:ViewModel构造函数不要写任务业务逻辑,否则有报错的可能
     *
     * @param application app application
     */
    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    protected GlobalViewModel getGlobalViewModel() {
        return ((App) getApplication()).getGlobalViewModel();
    }

    /***********************BaseViewModelState操作******************************/
    private MutableResult<BaseData> baseData;

    public MutableResult<BaseData> getBaseData() {
        if (baseData == null) {
            baseData = new MutableResult<>();
        }
        return baseData;
    }

    public void setBaseData(int type) {
        setBaseData(type, null);
    }

    public void setBaseData(int type, String message) {
        setBaseData(type, message, null);
    }

    public void setBaseData(int type, Object obj) {
        setBaseData(type, null, obj);
    }

    /**
     * @param type
     * @param message
     * @param obj
     */
    public void setBaseData(int type, String message, Object obj) {
        MutableResult<BaseData> liveData = getBaseData();
        BaseData state = liveData.getValue();
        if (state == null) {
            state = new BaseData(type, message, obj);
        } else {
            state.type = type;
            state.message = message;
            state.obj = obj;
        }
        liveData.setValue(state);
    }

    /**
     * 所有的RxJava都需要在组件生命周期结束时进行取消操作,防止内存泄漏
     */
    private CompositeDisposable compositeDisposable;

    protected void addDisposable(Disposable disposable) {
        getCompositeDisposable().add(disposable);
    }

    protected CompositeDisposable getCompositeDisposable() {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        return compositeDisposable;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unSubscribe();
    }

    /**
     * 取消任务
     */
    private void unSubscribe() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }
}
