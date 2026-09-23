package com.lib.base.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lib.base.databinding.DemoLayoutBinding;
import com.lib.base.util.DebugUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.adapter
 *
 * @author xwchen
 * Date         2021/12/30.
 */
public class DemoAdapter extends BaseQuickAdapter<String, DemoAdapter.ViewHolder> {
    private boolean show;

    public void setShow(boolean show) {
        this.show = show;
    }

    public DemoAdapter() {
        super();
    }

    public DemoAdapter(@NonNull Context context) {
        super();
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DemoLayoutBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @Nullable String item) {
        holder.bind(item);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {

        private final DemoLayoutBinding binding;

        private ViewHolder(@NonNull DemoLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        void bind(@Nullable String item) {
            binding.tv1.setOnClickListener(v -> DebugUtil.toast("影藏item点击"));
            binding.tv1.setVisibility(show ? View.VISIBLE : View.GONE);

            binding.tv.setText(item);
            if (!show) {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.tv.getLayoutParams();
                params.matchConstraintPercentWidth = 1;
                binding.tv.setLayoutParams(params);
            }
        }
    }
}
