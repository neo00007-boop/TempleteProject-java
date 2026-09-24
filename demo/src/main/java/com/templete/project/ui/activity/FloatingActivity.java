package com.templete.project.ui.activity;

import android.view.View;

import com.lib.base.adapter.FloatingAdapter;
import com.lib.base.bean.FloatingItem;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.FreshUtil;
import com.lib.base.util.ScreenUtil;
import com.templete.project.R;
import com.templete.project.databinding.FloatingActivityBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 比较笨重的悬浮刷新解决方案,作为最初的解决思路,仅供参考:
 * 存在两个导航:顶部导航+底部导航,最麻烦的地方在于两个导航之间的同步操作,屎山代码.
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui
 * @author      xwchen
 * Date         2022/1/13.
 */

public class FloatingActivity extends BaseActivity<FloatingActivityBinding> {
    public static final String TAG = "FloatingActivity";
    private int showType = -1;//-1初始状态,0显示,1隐藏
    private float dimensiony1;
    private float dimensiony2;
    private FloatingAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected FloatingActivityBinding viewBinding() {
        return FloatingActivityBinding.inflate(getLayoutInflater());
    }


    @Override
    public void inits() {
        setTitleStr("tablayout(悬浮) + 刷新列表");
        dimensiony1 = getDimen(R.dimen.x130) + ScreenUtil.getStatusBarSize();
        dimensiony2 = getDimen(R.dimen.x600);
    }

    @Override
    public void initView() {
        mViewBinding.refreshLayout.setOnRefreshListener(refreshLayout -> FreshUtil.finishFresh(mViewBinding.refreshLayout));
        mViewBinding.refreshLayout.setOnLoadMoreListener(refreshLayout -> FreshUtil.finishLoad(mViewBinding.refreshLayout));

        linearLayoutManager = new LinearLayoutManager(this);
        mViewBinding.recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new FloatingAdapter(this, mViewBinding.recyclerView1, new FloatingAdapter.OnItemClickListener() {
            @Override
            public void topBarClick(int position) {
                //scrollView();
            }

            @Override
            public void middleBarClick(int position) {
                //scrollView();
            }

            @Override
            public void itemClick(int position) {
            }
        });
        adapter.initRecyclerView1();
        adapter.setList(getList());
        mViewBinding.recyclerView.setAdapter(adapter);
        // scrollListener
        mViewBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    homeContentAdapter.resetPos();
                }*/
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int tabHeight = adapter.getheight();
                logD(TAG, "tabHeight=" + tabHeight);
                if (tabHeight <= dimensiony1) {
                    if (showType != 0) {
                        showType = 0;
                        mViewBinding.recyclerView1.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (showType != 1) {
                        showType = 1;
                        mViewBinding.recyclerView1.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    private List<FloatingItem> getList() {
        List<FloatingItem> list = new ArrayList<>();
        list.add(FloatingItem.header());
        for (int i = 0; i < 200; i++) {
            list.add(FloatingItem.content("item" + i));
        }
        return list;
    }

    private void scrollView() {
        if (showType == 0) {
            logD("scrolly", "y=" + adapter.getheight());
            //mViewBinding.getRoot().postDelayed(() -> mViewBinding.recyclerView.smoothScrollBy(0, -(int) dimensiony2), 100);//滚动一段距离
            mViewBinding.getRoot().postDelayed(() -> linearLayoutManager.scrollToPositionWithOffset(0, -(int) dimensiony2 - 20), 50);//pos+距离
        }
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
