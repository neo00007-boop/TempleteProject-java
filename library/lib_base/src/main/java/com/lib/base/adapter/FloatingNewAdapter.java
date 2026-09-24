package com.lib.base.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.shape.view.NavigationBar;
import com.lib.base.R;
import com.lib.base.adapter.FloatingAdapter.FloatingItem;
import com.lib.base.databinding.DemoLayoutBinding;
import com.lib.base.databinding.HomeHeaderLayoutNewBinding;
import com.lib.base.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 悬浮导航新版（BaseMultiItemQuickAdapter）
 */
public class FloatingNewAdapter extends BaseMultiItemQuickAdapter<FloatingItem, BaseViewHolder> {
    public static final String TAG = "FloatingNewAdapter";
    public static final int TYPE_HEADER = FloatingItem.TYPE_HEADER;
    public static final int TYPE_ITEM = FloatingItem.TYPE_ITEM;

    private FrameLayout container;

    public FloatingNewAdapter(@NonNull Context context, OnItemClickListener onItemClickListener) {
        super();
        addItemType(TYPE_HEADER, R.layout.home_header_layout_new);
        addItemType(TYPE_ITEM, R.layout.demo_layout);
        if (onItemClickListener != null) {
            setOnItemClickListener((adapter, view, position) -> onItemClickListener.itemClick(position));
        }
    }

    @NonNull
    @Override
    protected BaseViewHolder onCreateDefViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(HomeHeaderLayoutNewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        return new ItemHolder(DemoLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, FloatingItem item) {
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind();
        } else if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind(item != null ? item.text : null);
        }
    }

    public void addView(NavigationBar bar) {
        ViewUtil.addView(container, bar);
    }

    public void itemMove(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        int from = Math.min(fromPosition, toPosition);
        int to = Math.max(fromPosition, toPosition);
        List<FloatingItem> list = new ArrayList<>(getData());
        FloatingItem removeFrom = list.get(from);
        FloatingItem removeTo = list.get(to);
        list.set(from, removeTo);
        list.set(to, removeFrom);
        setList(list);
    }

    public int getheight() {
        final int[] locations = new int[2];
        container.getLocationOnScreen(locations);
        return locations[1];
    }

    private final class HeaderHolder extends BaseViewHolder {

        private final HomeHeaderLayoutNewBinding binding;

        private HeaderHolder(@NonNull HomeHeaderLayoutNewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind() {
            container = binding.container;
        }
    }

    private static final class ItemHolder extends BaseViewHolder {

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
        void itemClick(int position);
    }
}
