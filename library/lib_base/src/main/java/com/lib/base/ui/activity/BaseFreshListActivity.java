package com.lib.base.ui.activity;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.databinding.BaseFreshListLayoutBinding;
import com.lib.base.ui.widget.HolderView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

/**
 * 固定「SmartRefreshLayout + RecyclerView」布局的刷新列表基类。
 * Adapter 初始化时把 {@link HolderView} 设为 BRVAH emptyView：
 * 先 loading，请求结束后按有数据 / 无数据 / 失败切换状态。
 * 自定义布局请直接继承 {@link BaseFreshActivity}。
 *
 * @param <T> 列表数据类型
 * @param <A> Adapter
 */
public abstract class BaseFreshListActivity<T, A extends BaseQuickAdapter<T, ? extends BaseViewHolder>>
        extends BaseFreshActivity<BaseFreshListLayoutBinding, T, A> {

    private HolderView mHolderView;

    @Override
    protected BaseFreshListLayoutBinding viewBinding() {
        return BaseFreshListLayoutBinding.inflate(getLayoutInflater());
    }

    @NonNull
    @Override
    protected SmartRefreshLayout provideRefreshLayout() {
        return mViewBinding.refreshLayout;
    }

    @NonNull
    @Override
    protected RecyclerView provideRecyclerView() {
        return mViewBinding.recyclerView;
    }

    @Nullable
    @Override
    public HolderView getHolderView() {
        return mHolderView;
    }

    @Override
    protected void setupFreshList() {
        super.setupFreshList();
        mHolderView = new HolderView(this);
        mHolderView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mAdapter.setEmptyView(mHolderView);
        if (needInitLoading()) {
            showLoadingView();
        }
    }

    protected boolean needInitLoading() {
        return false;
    }

    @Override
    protected void triggerRefresh() {
        if (mAdapter != null && mAdapter.getData().isEmpty()) {
            if (needInitLoading()) {
                showLoadingView();
            }
        }
        super.triggerRefresh();
    }
}
