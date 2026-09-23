package com.templete.project.ui.activity;

import android.annotation.SuppressLint;
import android.view.MotionEvent;

import com.hjq.shape.view.NavigationBar;
import com.lib.base.adapter.FloatingNewAdapter;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.FreshUtil;
import com.lib.base.util.ScreenUtil;
import com.lib.base.util.ViewUtil;
import com.templete.project.R;
import com.templete.project.databinding.FloatingActivityNewBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 比较较理想的悬浮刷新解决方案,推荐使用:
 * 顶部和底部各有一个导航容器,但是共用一个导航,避免了同步问题.
 * <p>
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui
 * @author      xwchen
 * Date         2022/1/13.
 */

public class FloatingNewActivity extends BaseActivity<FloatingActivityNewBinding> {
    public static final String TAG = "FloatingActivity";
    private int type = -1;
    private float dimensiony1;
    private float dimensiony2;
    private FloatingNewAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected FloatingActivityNewBinding viewBinding() {
        return FloatingActivityNewBinding.inflate(getLayoutInflater());
    }


    @Override
    public void inits() {
        setTitleStr("tablayout(悬浮) + 刷新列表");
        dimensiony1 = getDimen(R.dimen.x130) + ScreenUtil.getStatusBarSize();
        dimensiony2 = getDimen(R.dimen.x600);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initView() {
        mViewBinding.refreshLayout.setOnRefreshListener(refreshLayout -> FreshUtil.finishFresh(mViewBinding.refreshLayout));
        mViewBinding.refreshLayout.setOnLoadMoreListener(refreshLayout -> FreshUtil.finishLoad(mViewBinding.refreshLayout));

        NavigationBar bar = new NavigationBar(this);
        List<NavigationBar.Data> list = Arrays.asList(
                new NavigationBar.Data("导航按钮1"), new NavigationBar.Data("导航按钮2"),
                new NavigationBar.Data("导航按钮3"), new NavigationBar.Data("导航按钮4"),
                new NavigationBar.Data("导航按钮5"), new NavigationBar.Data("导航按钮6"),
                new NavigationBar.Data("导航按钮7"), new NavigationBar.Data("导航按钮8"),
                new NavigationBar.Data("导航按钮2"), new NavigationBar.Data("导航按钮3"),
                new NavigationBar.Data("导航按钮4"), new NavigationBar.Data("导航按钮5"),
                new NavigationBar.Data("导航按钮6"), new NavigationBar.Data("导航按钮7"),
                new NavigationBar.Data("导航按钮8"));
        bar.setData(list, 0, null);

        linearLayoutManager = new LinearLayoutManager(this);
        mViewBinding.recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new FloatingNewAdapter(this, position -> {
        });
        adapter.submitList(getList());
        mViewBinding.recyclerView.setAdapter(adapter);
        // scrollListener
        mViewBinding.recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                bar.stopScroll();
            }
            return false;
        });
        mViewBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int tabHeight = adapter.getheight();
                logD(TAG, "tabHeight=" + tabHeight);
                if (tabHeight <= dimensiony1) {
                    if (type != 0) {
                        type = 0;
                        ViewUtil.addView(mViewBinding.container, bar);
                    }
                } else {
                    if (type != 1) {
                        type = 1;
                        adapter.addView(bar);
                    }
                }
            }
        });
    }

    private List<String> getList() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            list.add("item" + i);
        }
        return list;
    }

    private void scrollView() {
       /* if (showType == 0) {
            logD("scrolly", "y=" + adapter.getheight());
            //mViewBinding.getRoot().postDelayed(() -> mViewBinding.recyclerView.smoothScrollBy(0, -(int) dimensiony2), 100);//滚动一段距离
            mViewBinding.getRoot().postDelayed(() -> linearLayoutManager.scrollToPositionWithOffset(0, -(int) dimensiony2 - 20), 50);//pos+距离
        }*/
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        mViewBinding.getRoot().postDelayed(runnable, 1000);
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            adapter.itemMove(3, 5);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewBinding.getRoot().removeCallbacks(runnable);
    }
}
