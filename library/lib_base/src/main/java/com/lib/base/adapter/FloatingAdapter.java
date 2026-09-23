package com.lib.base.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseMultiItemAdapter;
import com.hjq.shape.view.recyclerList.CenterLayoutManager;
import com.lib.base.databinding.DemoLayoutBinding;
import com.lib.base.databinding.HomeHeaderLayoutBinding;
import com.lib.base.util.OUtil;
import com.lib.base.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 悬浮导航 + 列表（多类型用 BRVAH BaseMultiItemAdapter）
 */
public class FloatingAdapter extends BaseMultiItemAdapter<String> {
    public static final String TAG = "FloatingAdapter";
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_TOP = 0;
    public static final int TYPE_MIDDLE = 1;

    private final Context context;
    private final OnItemClickListener onItemClickListener;
    private RecyclerView recyclerView1, recyclerView2;
    private CenterLayoutManager layoutManager1, layoutManager2;
    private HomeTopBarAdapter adapter1, adapter2;
    private int scrollType, firstPosition, positionOffset;
    private boolean hasClick;

    public FloatingAdapter(@NonNull Context context, RecyclerView recyclerView1, OnItemClickListener onItemClickListener) {
        super();
        this.context = context;
        this.recyclerView1 = recyclerView1;
        this.onItemClickListener = onItemClickListener;

        addItemType(TYPE_HEADER, new OnMultiItemAdapterListener<String, HeaderHolder>() {
            @NonNull
            @Override
            public HeaderHolder onCreate(@NonNull Context ctx, @NonNull ViewGroup parent, int viewType) {
                return new HeaderHolder(HomeHeaderLayoutBinding.inflate(LayoutInflater.from(ctx), parent, false));
            }

            @Override
            public void onBind(@NonNull HeaderHolder holder, int position, @Nullable String item) {
                holder.bind(position);
            }
        });
        addItemType(TYPE_ITEM, new OnMultiItemAdapterListener<String, ItemHolder>() {
            @NonNull
            @Override
            public ItemHolder onCreate(@NonNull Context ctx, @NonNull ViewGroup parent, int viewType) {
                return new ItemHolder(DemoLayoutBinding.inflate(LayoutInflater.from(ctx), parent, false));
            }

            @Override
            public void onBind(@NonNull ItemHolder holder, int position, @Nullable String item) {
                holder.bind(item);
            }
        });
        onItemViewType((position, list) -> position == 0 ? TYPE_HEADER : TYPE_ITEM);
    }

    public void initRecyclerView1() {
        ViewUtil.stopFlingWhenTouchUp(recyclerView1);
        layoutManager1 = new CenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView1.setLayoutManager(layoutManager1);
        adapter1 = new HomeTopBarAdapter(0);
        recyclerView1.setAdapter(adapter1);
        adapter1.setOnBarItemClickListener((pos, itemStr) -> {
            adapter2.setSelect(adapter1.getPos());
            scrollType = TYPE_TOP;
            recyclerView1.smoothScrollToPosition(pos);
            hasClick = true;
        });
    }

    public void itemMove(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        int from = Math.min(fromPosition, toPosition);
        int to = Math.max(fromPosition, toPosition);
        List<String> list = new ArrayList<>(getItems());
        String removeFrom = list.get(from);
        String removeTo = list.get(to);
        list.set(from, removeTo);
        list.set(to, removeFrom);
        submitList(list);
    }

    private final class HeaderHolder extends RecyclerView.ViewHolder {

        private final HomeHeaderLayoutBinding binding;

        private HeaderHolder(@NonNull HomeHeaderLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(int position) {
            recyclerView2 = binding.recyclerView2;
            ViewUtil.stopFlingWhenTouchUp(recyclerView2);
            layoutManager2 = new CenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            recyclerView2.setLayoutManager(layoutManager2);
            adapter2 = new HomeTopBarAdapter(adapter1.getPos());
            recyclerView2.setAdapter(adapter2);
            adapter2.setOnBarItemClickListener((pos, itemStr) -> {
                adapter1.setSelect(adapter2.getPos());
                scrollType = TYPE_MIDDLE;
                recyclerView2.smoothScrollToPosition(pos);
                hasClick = true;
            });
            recyclerView1.clearOnScrollListeners();
            recyclerView2.clearOnScrollListeners();
            layoutManager1.scrollToPositionWithOffset(firstPosition, positionOffset);
            layoutManager2.scrollToPositionWithOffset(firstPosition, positionOffset);
            recyclerView1.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    endScroll(newState, true, position);
                }
            });
            recyclerView2.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    endScroll(newState, false, position);
                }
            });
        }
    }

    private void endScroll(int newState, boolean isTop, int position) {
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            scrollType = isTop ? TYPE_TOP : TYPE_MIDDLE;
        } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            boolean endScroll = (isTop && scrollType == TYPE_TOP) || (!isTop && scrollType == TYPE_MIDDLE);
            if (endScroll) {
                CenterLayoutManager layoutManager = isTop ? layoutManager1 : layoutManager2;
                int firstPos = layoutManager.findFirstVisibleItemPosition();
                if (firstPos < 0) {
                    return;
                }
                View view = layoutManager.findViewByPosition(firstPos);
                if (OUtil.isNull(view)) {
                    return;
                }
                int offset = view.getLeft();
                CenterLayoutManager scrollLayoutManager = isTop ? layoutManager2 : layoutManager1;
                scrollLayoutManager.scrollToPositionWithOffset(firstPos, offset);
                rememberPosition(firstPos, offset);
                if (hasClick) {
                    hasClick = false;
                    if (isTop) {
                        onItemClickListener.topBarClick(position);
                    } else {
                        onItemClickListener.middleBarClick(position);
                    }
                }
            }
        }
    }

    private void rememberPosition(int firstPosition, int positionOffset) {
        this.firstPosition = firstPosition;
        this.positionOffset = positionOffset;
    }

    public int getheight() {
        final int[] locations = new int[2];
        recyclerView2.getLocationOnScreen(locations);
        return locations[1];
    }

    private static final class ItemHolder extends RecyclerView.ViewHolder {

        private final DemoLayoutBinding binding;

        private ItemHolder(@NonNull DemoLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        void bind(@Nullable String item) {
            binding.tv.setText(item);
        }
    }

    public interface OnItemClickListener {
        void topBarClick(int position);

        void middleBarClick(int position);

        void itemClick(int position);
    }
}
