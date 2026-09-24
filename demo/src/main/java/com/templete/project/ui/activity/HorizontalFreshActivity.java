package com.templete.project.ui.activity;

import com.lib.base.adapter.DemoAdapter;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.FreshActivityBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui
 * @author      xwchen
 * Date         2022/1/11.
 */

public class HorizontalFreshActivity extends BaseActivity<FreshActivityBinding> {
    private DemoAdapter demoAdapter;

    @Override
    protected FreshActivityBinding viewBinding() {
        return FreshActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("横向刷新");
    }

    @Override
    public void initView() {
        /*mViewBinding.refreshLayout.setOnMultiListener(new SimpleMultiListener() {
            @Override
            public void onHeaderStartAnimator(RefreshHeader header, int footerHeight, int maxDragHeight) {
                super.onHeaderStartAnimator(header, footerHeight, maxDragHeight);
                ((Animatable) mViewBinding.header.getDrawable()).start();
            }

            @Override
            public void onHeaderFinish(RefreshHeader header, boolean success) {
                super.onHeaderFinish(header, success);
                ((Animatable) mViewBinding.header.getDrawable()).stop();
            }
        });*/
        mViewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        demoAdapter = new DemoAdapter(this);
        mViewBinding.recyclerView.setAdapter(demoAdapter);
        mViewBinding.refreshLayout.setOnRefreshListener(refreshLayout -> mViewBinding.refreshLayout.finishRefresh(1000));
        mViewBinding.refreshLayout.setOnLoadMoreListener(refreshLayout -> mViewBinding.refreshLayout.finishLoadMore(1000));
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            list.add("条目"+i);
        }
        demoAdapter.setList(list);
    }
}
