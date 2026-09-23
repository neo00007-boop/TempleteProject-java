package com.templete.project.ui.activity;

import com.lib.base.adapter.DemoAdapter;
import com.lib.base.bean.BtnBean;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.TranslateActivityBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * PackageName  com.templete.project.ui
 * ProjectName  TempleteProject-java
 * Date         2022/1/21.
 *
 * @author xwchen
 */

public class TranslateActivity extends BaseActivity<TranslateActivityBinding> {
    public static final String TAG = "TranslateActivity";
    private DemoAdapter demoAdapter;

    @Override
    protected TranslateActivityBinding viewBinding() {
        return TranslateActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("列表横移");
        setRightClickViews((position, view) -> trans(), false, new BtnBean("平移"));
    }

    private void trans() {
        mViewBinding.recyclerView.trans();
    }

    @Override
    public void initView() {
        mViewBinding.recyclerView.setLayoutManager();
        demoAdapter = new DemoAdapter(this);
        mViewBinding.recyclerView.setAdapter(demoAdapter);
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("条目"+i);
        }
        demoAdapter.setShow(true);
        demoAdapter.submitList(list);
    }
}
