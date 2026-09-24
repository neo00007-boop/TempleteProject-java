package com.lib.base.ui.activity;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.databinding.BaseFreshListLayoutBinding;
import com.lib.base.ui.widget.HolderView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 固定「HolderView + SmartRefreshLayout + RecyclerView」布局的刷新列表基类。
 * 自定义布局请直接继承 {@link BaseFreshActivity}。
 *
 * @param <T> 列表数据类型
 * @param <A> Adapter
 */
public abstract class BaseFreshListActivity<T, A extends BaseQuickAdapter<T, ? extends BaseViewHolder>>
        extends BaseFreshActivity<BaseFreshListLayoutBinding, T, A> {

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
        return mViewBinding.holderView;
    }
}
