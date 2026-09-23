package com.templete.project.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.hjq.shape.view.FlowLayout;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.BoxItemBinding;
import com.templete.project.databinding.FlexBoxActivityBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 谷歌官方流式布局,替代{@link FlowLayout}
 * PackageName  com.templete.project.ui
 * ProjectName  TempleteProject-java
 * Date         2022/1/23.
 *
 * @author xwchen
 */

public class FlexBoxActivity extends BaseActivity<FlexBoxActivityBinding> {

    private MyAdapter myAdapter;

    @Override
    public FlexBoxActivityBinding viewBinding() {
        return FlexBoxActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("官方流式布局解决方案");
    }

    @Override
    public void initView() {
        //设置LayoutManager
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
        //主轴为水平方向，起点在左端
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        //按正常方向换行
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        //定义项目在副轴轴上如何对齐
        flexboxLayoutManager.setAlignItems(AlignItems.FLEX_START);
        //多个轴对齐方式
        //SPACE_EVENLY 两边和中间间隙均分
        //SPACE_BETWEEN 两边靠边,中间间隙均分
        //SPACE_AROUND 看起来和SPACE_EVENLY一样
        flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_END);

        mViewBinding.getRoot().setLayoutManager(flexboxLayoutManager);
        myAdapter = new MyAdapter();
        mViewBinding.getRoot().setAdapter(myAdapter);

    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        String name = "我我我我我我我我我";
        List<String> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            int num = random.nextInt(10);
            num = num == 0 ? 1 : num;
            list.add(name.substring(0, num));
        }
        myAdapter.submitList(list);
    }


    static class MyAdapter extends BaseQuickAdapter<String, MyAdapter.VH> {

        public MyAdapter() {
            super();
        }

        @NonNull
        @Override
        protected VH onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
            return new VH(BoxItemBinding.inflate(LayoutInflater.from(context), parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull VH holder, int position, @Nullable String item) {
            holder.bind(item);
        }

        static class VH extends RecyclerView.ViewHolder {

            private final BoxItemBinding binding;

            public VH(BoxItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bind(@Nullable String item) {
                binding.tv.setText(item);
            }
        }
    }
}
