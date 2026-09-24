package com.lib.base.ui.activity;

import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.R;
import com.lib.base.databinding.BaseFreshListLayoutBinding;
import com.lib.base.ui.widget.HolderView;
import com.lib.base.ui.widget.ShimmerView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

/**
 * 固定「SmartRefreshLayout + RecyclerView」布局的刷新列表基类。
 * Adapter 初始化时把 {@link HolderView} 设为 BRVAH emptyView：
 * 默认 shimmer 骨架 loading，请求结束后按有数据 / 无数据 / 失败切换。
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

    /** loading：{@link HolderView#LOADING_SHIMMER}（默认）或 {@link HolderView#LOADING_PROGRESS} */
    protected int provideLoadingMode() {
        return HolderView.LOADING_SHIMMER;
    }

    /** shimmer 骨架布局，默认列表样式 */
    @LayoutRes
    protected int provideShimmerLayout() {
        return R.layout.viewholder_shimmer_list;
    }

    @Override
    protected void setupFreshList() {
        super.setupFreshList();
        mHolderView = new HolderView(this);
        mHolderView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mHolderView.setBlockTouch(false);
        mHolderView.setOnRetryClickListener(v -> onHolderRetryClick());
        int loadingMode = provideLoadingMode();
        mHolderView.setLoadingMode(loadingMode);
        if (loadingMode == HolderView.LOADING_SHIMMER) {
            int shimmer = provideShimmerLayout();
            if (shimmer != 0) {
                mHolderView.setShimmerLayout(shimmer);
            } else {
                mHolderView.setShimmerType(ShimmerView.TYPE_LIST);
            }
        }
        mAdapter.setEmptyView(mHolderView);
        showLoadingView();
    }

    @Override
    protected void triggerRefresh() {
        if (mAdapter != null && mAdapter.getData().isEmpty()) {
            showLoadingView();
        }
        super.triggerRefresh();
    }
}
