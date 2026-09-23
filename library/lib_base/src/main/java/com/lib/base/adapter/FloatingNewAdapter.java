package com.lib.base.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chad.library.adapter.base.BaseMultiItemAdapter;
import com.hjq.shape.view.NavigationBar;
import com.lib.base.databinding.DemoLayoutBinding;
import com.lib.base.databinding.HomeHeaderLayoutNewBinding;
import com.lib.base.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 悬浮导航新版（多类型用 BRVAH BaseMultiItemAdapter）
 */
public class FloatingNewAdapter extends BaseMultiItemAdapter<String> {
    public static final String TAG = "FloatingNewAdapter";
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private FrameLayout container;

    public FloatingNewAdapter(@NonNull Context context, OnItemClickListener onItemClickListener) {
        super();
        addItemType(TYPE_HEADER, new OnMultiItemAdapterListener<String, HeaderHolder>() {
            @NonNull
            @Override
            public HeaderHolder onCreate(@NonNull Context ctx, @NonNull ViewGroup parent, int viewType) {
                return new HeaderHolder(HomeHeaderLayoutNewBinding.inflate(LayoutInflater.from(ctx), parent, false));
            }

            @Override
            public void onBind(@NonNull HeaderHolder holder, int position, @Nullable String item) {
                holder.bind();
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
        if (onItemClickListener != null) {
            setOnItemClickListener((adapter, view, position) -> onItemClickListener.itemClick(position));
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
        List<String> list = new ArrayList<>(getItems());
        String removeFrom = list.get(from);
        String removeTo = list.get(to);
        list.set(from, removeTo);
        list.set(to, removeFrom);
        submitList(list);
    }

    public int getheight() {
        final int[] locations = new int[2];
        container.getLocationOnScreen(locations);
        return locations[1];
    }

    private final class HeaderHolder extends RecyclerView.ViewHolder {

        private final HomeHeaderLayoutNewBinding binding;

        private HeaderHolder(@NonNull HomeHeaderLayoutNewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind() {
            container = binding.container;
        }
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
        void itemClick(int position);
    }
}
