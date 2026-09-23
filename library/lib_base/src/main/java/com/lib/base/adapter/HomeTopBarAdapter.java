package com.lib.base.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lib.base.R;
import com.lib.base.config.App;
import com.lib.base.databinding.HomeBarItemBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 顶部横向导航条 Adapter（BRVAH4）
 */
public class HomeTopBarAdapter extends BaseQuickAdapter<String, HomeTopBarAdapter.ViewHolder> {

    private int pos;
    private OnBarItemClickListener listener;

    public HomeTopBarAdapter(int pos) {
        super();
        this.pos = pos;
        submitList(buildDefaultTabs());
        setOnItemClickListener((adapter, view, position) -> {
            if (this.pos == position) {
                return;
            }
            int temp = this.pos;
            this.pos = position;
            notifyItemChanged(temp);
            notifyItemChanged(this.pos);
            if (listener != null) {
                listener.onItemClick(position, getItem(position));
            }
        });
    }

    private static List<String> buildDefaultTabs() {
        List<String> list = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            list.add("导航条目" + (i + 1));
        }
        return list;
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HomeBarItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @Nullable String item) {
        String str = item != null ? item : "";
        holder.binding.tv.setText(str);

        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) holder.binding.indicator.getLayoutParams();
        layoutParams.width = (int) (App.getContext().getResources().getDimension(R.dimen.x55)
                * (str.length() - 0.5));
        holder.binding.indicator.requestLayout();
        holder.binding.indicator.setSelected(pos == position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelect(int pos) {
        if (this.pos == pos) {
            return;
        }
        this.pos = pos;
        notifyDataSetChanged();
    }

    public int getPos() {
        return pos;
    }

    public void setOnBarItemClickListener(OnBarItemClickListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final HomeBarItemBinding binding;

        ViewHolder(@NonNull HomeBarItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnBarItemClickListener {
        void onItemClick(int pos, String itemStr);
    }
}
