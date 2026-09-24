package com.templete.project.ui.activity;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lib.base.adapter.DemoAdapter;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.DebugUtil;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.simple.SimpleMultiListener;
import com.templete.project.databinding.ScrollActivityBBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * 官方嵌套布局,适用于无刷新悬浮UI,刷新的话存在问题
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui
 *
 * @author xwchen
 * Date         2022/1/12.
 */

public class ScrollActivity extends BaseActivity<ScrollActivityBBinding> {

    private DemoAdapter demoAdapter;

    @Override
    protected ScrollActivityBBinding viewBinding() {
        return ScrollActivityBBinding.inflate(getLayoutInflater());
    }


    @Override
    public void inits() {
        setTitleStr("嵌套滚动");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initView() {
        mViewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        demoAdapter = new DemoAdapter(this);
        demoAdapter.setOnItemClickListener((adapter, view, position) -> DebugUtil.toast("click position"));
        mViewBinding.recyclerView.setAdapter(demoAdapter);
        // demoAdapter.isStateViewEnable = true; // BRVAH empty/state view

        mViewBinding.refreshLayout.setOnMultiListener(new SimpleMultiListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mViewBinding.refreshLayout.finishRefresh();
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mViewBinding.refreshLayout.finishLoadMore();
            }
        });

        mViewBinding.appbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            logD("onScrolled", "verticalOffset=" + verticalOffset + ",transY=" + mViewBinding.appbar.getTop());
        });
        mViewBinding.recyclerView.setOnTouchListener(onTouchListener);
//        mViewBinding.ll.setOnTouchListener(onTouchListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        private float rawY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float rawY = event.getRawY();
                    // 只有下拉到顶时候才拦截事件处理下拉刷新,其他情况都不拦截
                    intercept(rawY > this.rawY && mViewBinding.appbar.getTop() == 0);
                    this.rawY = rawY;
                    break;
                case MotionEvent.ACTION_UP:
                    intercept(false);
                    break;
            }
            return false;
        }
    };

    /**
     * @param intercept 是否让mViewBinding.refreshLayout拦截事件:true 拦截事件,自己处理/false 不拦截,事件优先子view处理
     */
    private void intercept(boolean intercept) {
        mViewBinding.refreshLayout.requestDisallowInterceptTouchEvent(!intercept);
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("条目" + i);
        }
        mViewBinding.recyclerView.postDelayed(() -> demoAdapter.setList(list), 1000);
        /*mViewBinding.recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                demoAdapter.setNewInstance(null);
                // demoAdapter.setStateViewLayout(...); // BRVAH empty/state view
            }
        }, 2500);*/
    }
}
