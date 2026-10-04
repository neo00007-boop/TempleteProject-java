package com.templete.project.mvvm;

import android.app.Application;

import androidx.annotation.NonNull;

import com.kunminx.architecture.domain.message.MutableResult;
import com.lib.base.mvvm.BaseViewModel;
import com.templete.project.bean.Demo;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.mvvm
 *
 * @author xwchen
 * Date         2021/12/28.
 */

public class DemoViewModel extends BaseViewModel {
    public MutableResult<Demo> darks = new MutableResult<>();

    public MutableResult<Demo> getDarks() {
        if (darks.getValue() == null) {
            Demo demo = new Demo(0);
            darks.setValue(demo);
        }
        return darks;
    }

    public void setDarks(int type) {
        getDarks().getValue().setType(type);
    }

    /**
     * 注意:ViewModel构造函数不要写任务业务逻辑,否则有报错的可能
     *
     * @param application app application
     */
    public DemoViewModel(@NonNull Application application) {
        super(application);
    }

    public void test(int tag, String data) {
        setBaseData(tag, data);
    }
}
