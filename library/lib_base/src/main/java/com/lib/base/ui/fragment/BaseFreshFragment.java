package com.lib.base.ui.fragment;

import android.content.Context;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.ui.fresh.FreshListKit;
import com.lib.base.ui.widget.HolderView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import java.util.List;

/**
 * 刷新列表 Fragment 基类，核心逻辑与 Activity 共用 {@link FreshListKit}。
 */
public abstract class BaseFreshFragment<VB extends ViewBinding, T, A extends BaseQuickAdapter<T, ? extends BaseViewHolder>>
        extends BaseFragment<VB> {

    public static final String TAG = "BaseFreshFragment";

    private final FreshListKit<T, A> fresh = new FreshListKit<>(host(), this);

    @NonNull
    private FreshListKit.Host<T, A> host() {
        return new FreshListKit.Host<T, A>() {
            @NonNull
            @Override
            public Context getFreshContext() {
                return requireContext();
            }

            @Override
            public boolean isFreshHostAlive() {
                return isAdded() && getView() != null && !isDetached();
            }

            @NonNull
            @Override
            public SmartRefreshLayout provideRefreshLayout() {
                return BaseFreshFragment.this.provideRefreshLayout();
            }

            @NonNull
            @Override
            public RecyclerView provideRecyclerView() {
                return BaseFreshFragment.this.provideRecyclerView();
            }

            @NonNull
            @Override
            public A createAdapter() {
                return BaseFreshFragment.this.createAdapter();
            }

            @Override
            public void onListRequest(boolean isFresh) {
                BaseFreshFragment.this.onListRequest(isFresh);
            }

            @Override
            public boolean enableRefresh() {
                return BaseFreshFragment.this.enableRefresh();
            }

            @Override
            public boolean enableLoadMore() {
                return BaseFreshFragment.this.enableLoadMore();
            }

            @Override
            public boolean autoRefreshOnEnter() {
                return BaseFreshFragment.this.autoRefreshOnEnter();
            }

            @Override
            public int pageSize() {
                return BaseFreshFragment.this.pageSize();
            }

            @Override
            public int startPage() {
                return BaseFreshFragment.this.startPage();
            }

            @NonNull
            @Override
            public RecyclerView.LayoutManager createLayoutManager() {
                return BaseFreshFragment.this.createLayoutManager();
            }

            @NonNull
            @Override
            public int[] provideChildClickViewIds() {
                return BaseFreshFragment.this.provideChildClickViewIds();
            }

            @NonNull
            @Override
            public int[] provideChildLongClickViewIds() {
                return BaseFreshFragment.this.provideChildLongClickViewIds();
            }

            @Override
            public void onListItemClick(@NonNull A adapter, @NonNull View view, int position) {
                BaseFreshFragment.this.onListItemClick(adapter, view, position);
            }

            @Override
            public boolean onListItemLongClick(@NonNull A adapter, @NonNull View view, int position) {
                return BaseFreshFragment.this.onListItemLongClick(adapter, view, position);
            }

            @Override
            public void onListItemChildClick(@NonNull A adapter, @NonNull View view, int position) {
                BaseFreshFragment.this.onListItemChildClick(adapter, view, position);
            }

            @Override
            public boolean onListItemChildLongClick(@NonNull A adapter, @NonNull View view, int position) {
                return BaseFreshFragment.this.onListItemChildLongClick(adapter, view, position);
            }
        };
    }

    @Nullable
    protected A getAdapter() {
        return fresh.getAdapter();
    }

    @Nullable
    protected SmartRefreshLayout getRefreshLayout() {
        return fresh.getRefreshLayout();
    }

    @Nullable
    protected RecyclerView getRecyclerView() {
        return fresh.getRecyclerView();
    }

    protected int getPage() {
        return fresh.getPage();
    }

    @NonNull
    protected abstract SmartRefreshLayout provideRefreshLayout();

    @NonNull
    protected abstract RecyclerView provideRecyclerView();

    @NonNull
    protected abstract A createAdapter();

    protected abstract void onListRequest(boolean isFresh);

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
        return new LinearLayoutManager(requireContext());
    }

    /**
     * item 内子 View 点击，对应 Adapter {@code addChildClickViewIds}。
     * <pre>
     * &#64;Override
     * protected int[] provideChildClickViewIds() {
     *     return new int[]{R.id.tv1, R.id.iv_avatar};
     * }
     * </pre>
     * 也可在 {@link #createAdapter()} 里自行 {@code adapter.addChildClickViewIds(...)}。
     */
    @IdRes
    @NonNull
    protected int[] provideChildClickViewIds() {
        return new int[0];
    }

    /**
     * item 内子 View 长按，对应 Adapter {@code addChildLongClickViewIds}。
     * <pre>
     * &#64;Override
     * protected int[] provideChildLongClickViewIds() {
     *     return new int[]{R.id.tv1};
     * }
     * </pre>
     */
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

    @Override
    public void onHolderRetryClick() {
        freshData();
    }

    @Override
    public void initView() {
        setupFreshList();
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
        fresh.enterLoad();
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

    protected void setupFreshList() {
        fresh.setup();
    }

    protected void triggerRefresh() {
        fresh.triggerRefresh();
    }

    protected void triggerLoadMore() {
        fresh.triggerLoadMore();
    }

    protected void onRequestSuccess(@Nullable List<T> data) {
        fresh.onRequestSuccess(data);
    }

    protected void onRequestSuccess(@Nullable List<T> data, boolean noMore) {
        fresh.onRequestSuccess(data, noMore);
    }

    protected void onRequestFailure() {
        fresh.onRequestFailure();
    }

    @Nullable
    protected T getItem(int position) {
        return fresh.getItem(position);
    }

    protected void clearList() {
        fresh.clearList();
    }
}
