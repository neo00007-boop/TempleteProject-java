package com.lib.base.ui.activity;

import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.databinding.BaseFreshListLayoutBinding;
import com.lib.base.ui.widget.HolderView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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
        // emptyView 模式：不拦截触摸，否则空态下 SmartRefreshLayout 无法下拉刷新
        mHolderView.setBlockTouch(false);
        mAdapter.setEmptyView(mHolderView);
        // holder_view 内部默认 GONE，不显式切状态会出现空白 emptyView
        showLoadingView();
    }

    @Override
    protected void triggerRefresh() {
        // 仍无数据时回到 loading，避免停留在上一帧的空/错图
        if (mAdapter != null && mAdapter.getData().isEmpty()) {
            showLoadingView();
        }
        super.triggerRefresh();
    }
}
