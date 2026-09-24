package com.lib.base.ui.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.databinding.BaseFreshListLayoutBinding;
import com.lib.base.ui.fresh.FreshListEmptySupport;
import com.lib.base.ui.widget.HolderView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

/**
 * 固定「SmartRefreshLayout + RecyclerView」的刷新列表 Fragment，
 * 布局与 {@link com.lib.base.ui.activity.BaseFreshListActivity} 共用。
 */
public abstract class BaseFreshListFragment<T, A extends BaseQuickAdapter<T, ? extends BaseViewHolder>>
        extends BaseFreshFragment<BaseFreshListLayoutBinding, T, A> {

    private HolderView mHolderView;

    @Override
    protected BaseFreshListLayoutBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return BaseFreshListLayoutBinding.inflate(inflater, container, false);
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

    protected int provideLoadingMode() {
        return HolderView.LOADING_SHIMMER;
    }

    @LayoutRes
    protected int provideShimmerLayout() {
        return FreshListEmptySupport.defaultShimmerLayoutGrid();
    }

    @Override
    protected void setupFreshList() {
        super.setupFreshList();
        mHolderView = FreshListEmptySupport.attach(
                requireContext(), getAdapter(), provideLoadingMode(), provideShimmerLayout());
        mHolderView.setOnRetryClickListener(v -> onHolderRetryClick());
        showLoadingView();
    }

    @Override
    protected void triggerRefresh() {
        A adapter = getAdapter();
        if (adapter != null && adapter.getData().isEmpty()) {
            showLoadingView();
        }
        super.triggerRefresh();
    }
}
