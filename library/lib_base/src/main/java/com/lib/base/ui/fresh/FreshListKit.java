package com.lib.base.ui.fresh;

import android.content.Context;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.ui.action.StatusAction;
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

/**
 * Activity / Fragment 共用的刷新列表核心逻辑，避免两套基类复制粘贴。
 */
public final class FreshListKit<T, A extends BaseQuickAdapter<T, ? extends BaseViewHolder>> {

    public interface Host<T, A extends BaseQuickAdapter<T, ? extends BaseViewHolder>> {

        @NonNull
        Context getFreshContext();

        /** Activity：!finishing && !destroyed；Fragment：added 且 view 仍在 */
        boolean isFreshHostAlive();

        @NonNull
        SmartRefreshLayout provideRefreshLayout();

        @NonNull
        RecyclerView provideRecyclerView();

        @NonNull
        A createAdapter();

        void onListRequest(boolean isFresh);

        default boolean enableRefresh() {
            return true;
        }

        default boolean enableLoadMore() {
            return true;
        }

        default boolean autoRefreshOnEnter() {
            return false;
        }

        default int pageSize() {
            return 20;
        }

        default int startPage() {
            return 1;
        }

        @NonNull
        default RecyclerView.LayoutManager createLayoutManager() {
            return new LinearLayoutManager(getFreshContext());
        }

        /**
         * item 内子 View 点击 id，例：{@code return new int[]{R.id.tv1, R.id.iv_avatar};}
         * 也可在 {@link #createAdapter()} 里自行 {@code addChildClickViewIds}。
         */
        @IdRes
        @NonNull
        default int[] provideChildClickViewIds() {
            return new int[0];
        }

        /**
         * item 内子 View 长按 id，例：{@code return new int[]{R.id.tv1};}
         */
        @IdRes
        @NonNull
        default int[] provideChildLongClickViewIds() {
            return new int[0];
        }

        default void onListItemClick(@NonNull A adapter, @NonNull View view, int position) {
        }

        default boolean onListItemLongClick(@NonNull A adapter, @NonNull View view, int position) {
            return false;
        }

        default void onListItemChildClick(@NonNull A adapter, @NonNull View view, int position) {
        }

        default boolean onListItemChildLongClick(@NonNull A adapter, @NonNull View view, int position) {
            return false;
        }
    }

    private final Host<T, A> host;
    private final StatusAction status;

    private A adapter;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private int page = 1;

    public FreshListKit(@NonNull Host<T, A> host, @NonNull StatusAction status) {
        this.host = host;
        this.status = status;
    }

    @Nullable
    public A getAdapter() {
        return adapter;
    }

    @Nullable
    public SmartRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    @Nullable
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public int getPage() {
        return page;
    }

    public void setup() {
        refreshLayout = host.provideRefreshLayout();
        recyclerView = host.provideRecyclerView();
        adapter = host.createAdapter();
        page = host.startPage();

        recyclerView.setLayoutManager(host.createLayoutManager());
        recyclerView.setAdapter(adapter);

        refreshLayout.setEnableRefresh(host.enableRefresh());
        refreshLayout.setEnableLoadMore(host.enableLoadMore());
        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout layout) {
                if (!host.isFreshHostAlive()) {
                    return;
                }
                triggerRefresh();
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout layout) {
                if (!host.isFreshHostAlive()) {
                    return;
                }
                triggerLoadMore();
            }
        });

        int[] childClickIds = host.provideChildClickViewIds();
        if (childClickIds.length > 0) {
            adapter.addChildClickViewIds(childClickIds);
        }
        int[] childLongClickIds = host.provideChildLongClickViewIds();
        if (childLongClickIds.length > 0) {
            adapter.addChildLongClickViewIds(childLongClickIds);
        }

        adapter.setOnItemClickListener((a, view, position) ->
                host.onListItemClick(adapter, view, position));
        adapter.setOnItemLongClickListener((a, view, position) ->
                host.onListItemLongClick(adapter, view, position));
        adapter.setOnItemChildClickListener((a, view, position) ->
                host.onListItemChildClick(adapter, view, position));
        adapter.setOnItemChildLongClickListener((a, view, position) ->
                host.onListItemChildLongClick(adapter, view, position));
    }

    public void enterLoad() {
        if (host.autoRefreshOnEnter() && host.enableRefresh() && refreshLayout != null) {
            FreshUtil.autoRefresh(refreshLayout);
        } else {
            triggerRefresh();
        }
    }

    public void triggerRefresh() {
        page = host.startPage();
        host.onListRequest(true);
    }

    public void triggerLoadMore() {
        page++;
        host.onListRequest(false);
    }

    public void onRequestSuccess(@Nullable List<T> data) {
        List<T> list = data == null ? Collections.emptyList() : data;
        boolean noMore = list.isEmpty() || list.size() < host.pageSize();
        applySuccess(list, noMore);
    }

    public void onRequestSuccess(@Nullable List<T> data, boolean noMore) {
        applySuccess(data == null ? Collections.emptyList() : data, noMore);
    }

    public void onRequestFailure() {
        if (!host.isFreshHostAlive()) {
            return;
        }
        if (!isFreshPage()) {
            rollbackPage();
        }
        if (isFreshPage() && adapter != null && adapter.getData().isEmpty()) {
            status.showErrorView();
        }
        finishRefreshUi(false);
    }

    private void applySuccess(@NonNull List<T> list, boolean noMore) {
        if (!host.isFreshHostAlive() || adapter == null) {
            return;
        }
        if (isFreshPage()) {
            adapter.setList(new ArrayList<>(list));
            if (adapter.getData().isEmpty()) {
                status.showNoDataView();
            } else {
                status.hideView();
            }
        } else {
            if (list.isEmpty()) {
                rollbackPage();
            } else {
                adapter.addData(list);
            }
            status.hideView();
        }
        finishRefreshUi(noMore);
    }

    private boolean isFreshPage() {
        return page == host.startPage();
    }

    private void rollbackPage() {
        page = Math.max(host.startPage(), page - 1);
    }

    private void finishRefreshUi(boolean noMore) {
        if (refreshLayout == null) {
            return;
        }
        if (refreshLayout.isRefreshing()) {
            if (noMore) {
                FreshUtil.finishFreshWithNoMoreData(refreshLayout);
            } else {
                FreshUtil.finishFresh(refreshLayout);
            }
        } else if (refreshLayout.isLoading()) {
            if (noMore) {
                FreshUtil.finishLoadWithNoMoreData(refreshLayout);
            } else {
                FreshUtil.finishLoad(refreshLayout);
            }
        } else if (noMore) {
            refreshLayout.setNoMoreData(true);
        }
        if (!noMore) {
            refreshLayout.setNoMoreData(false);
        }
    }

    @Nullable
    public T getItem(int position) {
        if (adapter == null || position < 0 || position >= adapter.getData().size()) {
            return null;
        }
        return adapter.getItem(position);
    }

    public void clearList() {
        if (adapter != null) {
            adapter.setNewInstance(null);
        }
    }
}
