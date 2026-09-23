package com.templete.project.ui.activity;

import com.lib.base.adapter.DemoAdapter;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.FreshUtil;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.templete.project.databinding.SunFreshActivityBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui.activity
 *
 * @author xwchen
 * Date         2022/2/15.
 */

public class SunFreshActivity extends BaseActivity<SunFreshActivityBinding> {

    private DemoAdapter demoAdapter;

    @Override
    public void inits() {
        setTitleStr("烈日当空");
    }

    @Override
    public void initView() {
        mViewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        demoAdapter = new DemoAdapter(this);
        mViewBinding.recyclerView.setAdapter(demoAdapter);
        mViewBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                FreshUtil.finishSmart(mViewBinding.refreshLayout, false);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                FreshUtil.finishSmart(mViewBinding.refreshLayout, true);
            }
        });
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("条目"+i);
        }
        demoAdapter.submitList(list);
    }

    @Override
    protected SunFreshActivityBinding viewBinding() {
        return SunFreshActivityBinding.inflate(getLayoutInflater());
    }
}
