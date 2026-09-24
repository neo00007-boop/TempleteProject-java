package com.templete.project.ui.activity;

import android.annotation.SuppressLint;
import android.view.View;

import com.lib.base.adapter.DemoAdapter;
import com.lib.base.config.AppConfig;
import com.lib.base.databinding.IndexActivityBinding;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.FreshUtil;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui.activity
 * @author      xwchen
 * Date         2022/2/16.
 */

public class IndexActivity extends BaseActivity<IndexActivityBinding> {
    public static final String TAG = "IndexActivity";
    private DemoAdapter demoAdapter;

    @Override
    public void inits() {
        setTitleStr("索引");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        mViewBinding.bar.setLetters("#ABCFGHIJMNOPQRSTWXYZ");
        mViewBinding.bar.setOnCharSelectedListener((touchIndex, c) -> {
            logD("索引", "索引 index=" + touchIndex);
            mViewBinding.bigTv.setText(c + "");
            mViewBinding.bigTv.setVisibility(View.VISIBLE);
            mViewBinding.recyclerView.smoothScrollToPosition(200 / 20 * (touchIndex));
//            linearLayoutManager.scrollToPositionWithOffset(200 / 20 * (touchIndex), 0);
//            linearLayoutManager.smoothScrollToPosition(mViewBinding.recyclerView, null, 200 / 20 * (touchIndex));
            mViewBinding.getRoot().removeCallbacks(dismissRunnable);
            mViewBinding.getRoot().postDelayed(dismissRunnable, AppConfig.BIG_TV_SHOW_TIME);
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mViewBinding.recyclerView.setLayoutManager(linearLayoutManager);
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

    private final Runnable dismissRunnable = () -> mViewBinding.bigTv.setVisibility(View.GONE);

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            list.add("条目"+i);
        }
        demoAdapter.setList(list);
    }

    @Override
    protected IndexActivityBinding viewBinding() {
        return IndexActivityBinding.inflate(getLayoutInflater());
    }
}
