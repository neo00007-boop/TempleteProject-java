package com.lib.base.ui.fresh;

import android.content.Context;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.R;
import com.lib.base.ui.widget.HolderView;
import com.lib.base.ui.widget.ShimmerView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

/**
 * 固定布局刷新列表：组装 emptyView（HolderView + shimmer）。
 */
public final class FreshListEmptySupport {

    private FreshListEmptySupport() {
    }

    @NonNull
    public static HolderView attach(@NonNull Context context,
                                    @NonNull BaseQuickAdapter<?, ? extends BaseViewHolder> adapter,
                                    int loadingMode,
                                    @LayoutRes int shimmerLayout) {
        HolderView holderView = new HolderView(context);
        holderView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        holderView.setBlockTouch(false);
        holderView.setLoadingMode(loadingMode);
        if (loadingMode == HolderView.LOADING_SHIMMER) {
            if (shimmerLayout != 0) {
                holderView.setShimmerLayout(shimmerLayout);
            } else {
                holderView.setShimmerType(ShimmerView.TYPE_LIST);
            }
        }
        adapter.setEmptyView(holderView);
        return holderView;
    }

    @LayoutRes
    public static int defaultShimmerLayout() {
        return R.layout.viewholder_shimmer_list;
    }
}
