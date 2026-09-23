package com.lib.base.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lib.base.bean.BtnBean;
import com.lib.base.databinding.PopItemLayoutBinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.adapter
 *
 * @author xwchen
 * Date         2021/12/30.
 */
public class PopAdapter extends BaseQuickAdapter<BtnBean, PopAdapter.ViewHolder> {
    private final boolean hasSelect;
    private final boolean hasHtml;
    private int posSelect = 0;
    private boolean hasLeftRes;

    public PopAdapter(@NonNull Context context, boolean hasSelect, boolean hasHtml) {
        super();
        this.hasSelect = hasSelect;
        this.hasHtml = hasHtml;
    }

    @Override
    public void submitList(@Nullable List<? extends BtnBean> list) {
        checkResId(list);
        super.submitList(list);
    }

    private void checkResId(@Nullable List<? extends BtnBean> data) {
        hasLeftRes = true;
        if (data == null) {
            hasLeftRes = false;
            return;
        }
        for (BtnBean titleBtnBean : data) {
            if (titleBtnBean.resId <= 0) {
                hasLeftRes = false;
                break;
            }
        }
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(PopItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @Nullable BtnBean item) {
        holder.bind(position, item);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {

        private final PopItemLayoutBinding binding;

        private ViewHolder(@NonNull PopItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(int position, @Nullable BtnBean popBean) {
            try {
                binding.line.setVisibility(position == getItems().size() - 1 ? View.INVISIBLE : View.VISIBLE);

                if (popBean == null) {
                    return;
                }
                binding.tv.setText(getTxt(popBean.str));
                if (hasLeftRes) {
                    binding.ivLeft.setVisibility(View.VISIBLE);
                    binding.ivLeft.setImageResource(popBean.resId);
                }
                if (hasSelect) {
                    binding.getRoot().setSelected(posSelect == position);
                    binding.ivRight.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private CharSequence getTxt(String str) {
            return !hasHtml ? str : Html.fromHtml(str);
        }
    }

    /**
     * 只刷新两个item状态
     */
    public void notifyData(int position) {
        if (hasSelect && posSelect != position) {
            int temp = posSelect;
            posSelect = position;
            notifyItemChanged(temp);
            notifyItemChanged(position);
        }
    }
}
