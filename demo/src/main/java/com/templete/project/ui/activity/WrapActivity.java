package com.templete.project.ui.activity;

import android.widget.TextView;

import com.lib.base.adapter.DemoAdapter;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.R;
import com.templete.project.databinding.WrapActivityBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * 可以添加多个header,footer,结合刷新==
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui.activity
 * Author       Administrator
 * Date         2022/3/9.
 */

public class WrapActivity extends BaseActivity<WrapActivityBinding> {
    public static final String TAG = "WrapActivity";
    private DemoAdapter demoAdapter;

    @Override
    public void inits() {
        setTitleStr("WrapRecyclerView");
    }

    @Override
    public void initView() {
        mViewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        demoAdapter = new DemoAdapter(this);
        demoAdapter.setOnItemClickListener((adapter, view, position) -> {
            toast("我是item" + position);
//            demoAdapter.removeAt(position);
            demoAdapter.add(position, "添加");
        });
        mViewBinding.recyclerView.setAdapter(demoAdapter);

        TextView headerView = mViewBinding.recyclerView.addHeaderView(R.layout.picker_item);
        headerView.setText("我是头部");
        headerView.setOnClickListener(v -> toast("点击了头部"));

        TextView footerView = mViewBinding.recyclerView.addFooterView(R.layout.picker_item);
        footerView.setText("我是尾部");
        footerView.setOnClickListener(v -> toast("点击了尾部"));
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
        demoAdapter.setShow(false);
        demoAdapter.submitList(list);
    }

    @Override
    protected WrapActivityBinding viewBinding() {
        return WrapActivityBinding.inflate(getLayoutInflater());
    }
}
