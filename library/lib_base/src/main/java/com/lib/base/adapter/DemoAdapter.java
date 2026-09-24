package com.lib.base.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lib.base.R;
import com.lib.base.databinding.DemoLayoutBinding;
import com.lib.base.util.DebugUtil;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

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
        super(R.layout.demo_layout);
    }

    public DemoAdapter(@NonNull Context context) {
        super(R.layout.demo_layout);
    }

    @NonNull
    @Override
    protected ViewHolder onCreateDefViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DemoLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    protected void convert(@NonNull ViewHolder holder, String item) {
        holder.bind(item);
    }

    final class ViewHolder extends BaseViewHolder {

        private final DemoLayoutBinding binding;

        private ViewHolder(@NonNull DemoLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        void bind(String item) {
            binding.tv1.setOnClickListener(v -> DebugUtil.toast("影藏item点击"));
            binding.tv1.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);

            binding.tv.setText(item);
            if (!show) {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.tv.getLayoutParams();
                params.matchConstraintPercentWidth = 1;
                binding.tv.setLayoutParams(params);
            }
        }
    }
}
