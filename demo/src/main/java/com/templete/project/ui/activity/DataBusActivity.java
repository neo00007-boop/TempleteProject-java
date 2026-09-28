package com.templete.project.ui.activity;

import androidx.lifecycle.Observer;

import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.DebugUtil;
import com.lib.base.util.LiveDataBus;
import com.templete.project.databinding.DataBusActivityBinding;

/**
 * PackageName  com.templete.project.ui.activity
 * ProjectName  TempleteProject-java
 * Date         2022/11/12.
 *
 * @author xwchen
 */

public class DataBusActivity extends BaseActivity<DataBusActivityBinding> {
    public static final String TAG = "DataBusActivityTag";
    public static final String KEY_BUS1 = "KEY_BUS1";
    public static final String KEY_BUS2 = "KEY_BUS2";

    @Override
    public void inits() {
        setTitleStr("DataBus");
    }

    @Override
    public void initView() {

    }

    @Override
    public void initEvent() {
        LiveDataBus.get().with(KEY_BUS1, String.class).observe(this, s -> DebugUtil.logD(TAG, String.format("s=%s", s)));
        LiveDataBus.get().with(KEY_BUS2, String.class).observeForever(observer);

        LiveDataBus.get().with(KEY_BUS1).setValue("xumingling1");
        mViewBinding.getRoot().postDelayed(() -> LiveDataBus.get().with(KEY_BUS2).setValue("xumingling2"), 2000);
    }

    Observer<String> observer = s -> DebugUtil.logD(TAG, String.format("s=%s", s));

    @Override
    public void initData() {

    }

    @Override
    protected DataBusActivityBinding viewBinding() {
        return DataBusActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveDataBus.get().removeObserver(KEY_BUS2, observer);
    }
}
