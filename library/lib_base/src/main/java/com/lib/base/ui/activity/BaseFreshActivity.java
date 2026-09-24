package com.lib.base.ui.activity;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.ui.widget.HolderView;
import com.lib.base.util.FreshUtil;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

/**
 * 刷新列表基类（BaseActivity + FreshUtil + BaseQuickAdapter + item/childClick）。
 * <p>
 * 业务只需实现：
 * <ul>
 *   <li>{@link #provideRefreshLayout()} / {@link #provideRecyclerView()} / {@link #createAdapter()}</li>
 *   <li>{@link #onListRequest(boolean)} —— 刷新 / 加载更多统一入口（isFresh=true 为刷新）</li>
 *   <li>请求结束后调用 {@link #onRequestSuccess(List)} 或 {@link #onRequestFailure()}</li>
 * </ul>
 * 子 View 点击：在 Adapter 里 {@code addChildClickViewIds}，或重写 {@link #provideChildClickViewIds()}；
 * 回调 {@link #onListItemChildClick}。
 */
public abstract class BaseFreshActivity<VB extends ViewBinding, T, A extends BaseQuickAdapter<T, ? extends BaseViewHolder>>
        extends BaseActivity<VB> {

    public static final String TAG = "BaseFreshActivity";

    protected A mAdapter;
    protected SmartRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;

    protected int mPage = 1;

    // ======================== 必须实现 ========================

    @NonNull
    protected abstract SmartRefreshLayout provideRefreshLayout();

    @NonNull
    protected abstract RecyclerView provideRecyclerView();

    @NonNull
    protected abstract A createAdapter();

    /**
     * 列表数据请求入口。
     *
     * @param isFresh true=下拉刷新（此时 {@link #mPage}=={@link #startPage()}）；
     *                false=上拉加载（此时 {@link #mPage} 已 +1）
     */
    protected abstract void onListRequest(boolean isFresh);

    // ======================== 可选配置 ========================

    protected boolean enableRefresh() {
        return true;
    }

    protected boolean enableLoadMore() {
        return true;
    }

    protected boolean autoRefreshOnEnter() {
        return false;
    }

    protected int pageSize() {
        return 20;
    }

    protected int startPage() {
        return 1;
    }

    @NonNull
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(this);
    }

    /**
     * 需要子 View 点击时返回 id；也可在 Adapter 构造里自行 {@link BaseQuickAdapter#addChildClickViewIds}。
     */
    @IdRes
    @NonNull
    protected int[] provideChildClickViewIds() {
        return new int[0];
    }

    @IdRes
    @NonNull
    protected int[] provideChildLongClickViewIds() {
        return new int[0];
    }

    protected void onListItemClick(@NonNull A adapter, @NonNull View view, int position) {
    }

    protected boolean onListItemLongClick(@NonNull A adapter, @NonNull View view, int position) {
        return false;
    }

    protected void onListItemChildClick(@NonNull A adapter, @NonNull View view, int position) {
    }

    protected boolean onListItemChildLongClick(@NonNull A adapter, @NonNull View view, int position) {
        return false;
    }

    @Nullable
    @Override
    public HolderView getHolderView() {
        return null;
    }

    // ======================== 生命周期 ========================

    @Override
    public void initView() {
        setupFreshList();
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
        if (autoRefreshOnEnter() && enableRefresh()) {
            FreshUtil.autoRefresh(mRefreshLayout);
        }
    }

    @Override
    public void requestData(boolean isFresh) {
        if (isFresh) {
            triggerRefresh();
        } else {
            triggerLoadMore();
        }
    }

    @Override
    public void freshData() {
        triggerRefresh();
    }

    @Override
    public void loadData() {
        triggerLoadMore();
    }

    // ======================== 初始化 ========================

    protected void setupFreshList() {
        mRefreshLayout = provideRefreshLayout();
        mRecyclerView = provideRecyclerView();
        mAdapter = createAdapter();

        mRecyclerView.setLayoutManager(createLayoutManager());
        mRecyclerView.setAdapter(mAdapter);

        mRefreshLayout.setEnableRefresh(enableRefresh());
        mRefreshLayout.setEnableLoadMore(enableLoadMore());
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                triggerRefresh();
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                triggerLoadMore();
            }
        });

        int[] childClickIds = provideChildClickViewIds();
        if (childClickIds.length > 0) {
            mAdapter.addChildClickViewIds(childClickIds);
        }
        int[] childLongClickIds = provideChildLongClickViewIds();
        if (childLongClickIds.length > 0) {
            mAdapter.addChildLongClickViewIds(childLongClickIds);
        }

        mAdapter.setOnItemClickListener((adapter, view, position) ->
                onListItemClick(mAdapter, view, position));
        mAdapter.setOnItemLongClickListener((adapter, view, position) ->
                onListItemLongClick(mAdapter, view, position));
        mAdapter.setOnItemChildClickListener((adapter, view, position) ->
                onListItemChildClick(mAdapter, view, position));
        mAdapter.setOnItemChildLongClickListener((adapter, view, position) ->
                onListItemChildLongClick(mAdapter, view, position));
    }

    // ======================== 触发 ========================

    /** 下拉刷新（手势已由 SmartRefreshLayout 持有刷新态） */
    protected void triggerRefresh() {
        mPage = startPage();
        onListRequest(true);
    }

    /** 静默刷新（无手势动画，结束时按 {@link #mPage} 收尾） */
    protected void triggerRefreshSilent() {
        triggerRefresh();
    }

    /** 上拉加载更多 */
    protected void triggerLoadMore() {
        mPage++;
        onListRequest(false);
    }

    // ======================== 结果回填 ========================

    /**
     * 请求成功。按 {@link #pageSize()} 自动判断是否还有更多。
     */
    protected void onRequestSuccess(@Nullable List<T> data) {
        List<T> list = data == null ? Collections.emptyList() : data;
        boolean noMore = list.isEmpty() || list.size() < pageSize();
        applySuccess(list, noMore);
    }

    /**
     * 请求成功，并显式指定是否还有更多。
     */
    protected void onRequestSuccess(@Nullable List<T> data, boolean noMore) {
        applySuccess(data == null ? Collections.emptyList() : data, noMore);
    }

    private void applySuccess(@NonNull List<T> list, boolean noMore) {
        if (isFreshPage()) {
            mAdapter.setList(new ArrayList<>(list));
            if (mAdapter.getData().isEmpty()) {
                showNoDataView();
            } else {
                hideView();
            }
        } else {
            if (list.isEmpty()) {
                rollbackPage();
            } else {
                mAdapter.addData(list);
            }
            hideView();
        }
        finishRefreshUi(noMore);
    }

    /** 请求失败 */
    protected void onRequestFailure() {
        if (!isFreshPage()) {
            rollbackPage();
        }
        if (isFreshPage() && mAdapter.getData().isEmpty()) {
            showErrorView();
        }
        finishRefreshUi(false);
    }

    private boolean isFreshPage() {
        return mPage == startPage();
    }

    private void rollbackPage() {
        mPage = Math.max(startPage(), mPage - 1);
    }

    /**
     * 结束刷新 / 加载动画，并维护「没有更多」状态。
     * 有手势时读 SmartRefreshLayout；静默请求时只维护 noMore。
     */
    protected void finishRefreshUi(boolean noMore) {
        if (mRefreshLayout == null) {
            return;
        }
        if (mRefreshLayout.isRefreshing()) {
            if (noMore) {
                FreshUtil.finishFreshWithNoMoreData(mRefreshLayout);
            } else {
                FreshUtil.finishFresh(mRefreshLayout);
            }
        } else if (mRefreshLayout.isLoading()) {
            if (noMore) {
                FreshUtil.finishLoadWithNoMoreData(mRefreshLayout);
            } else {
                FreshUtil.finishLoad(mRefreshLayout);
            }
        } else if (noMore) {
            mRefreshLayout.setNoMoreData(true);
        }
        if (!noMore) {
            mRefreshLayout.setNoMoreData(false);
        }
    }

    // ======================== 便捷方法 ========================

    @Nullable
    protected T getItem(int position) {
        if (mAdapter == null || position < 0 || position >= mAdapter.getData().size()) {
            return null;
        }
        return mAdapter.getItem(position);
    }

    protected void clearList() {
        if (mAdapter != null) {
            mAdapter.setNewInstance(null);
        }
    }
}
