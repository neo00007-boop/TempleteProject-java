package com.templete.project.ui.activity;

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
import com.templete.project.R;
import com.templete.project.databinding.BoxItemBinding;
import com.templete.project.databinding.FlexBoxActivityBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        myAdapter.setList(list);
    }


    static class MyAdapter extends BaseQuickAdapter<String, MyAdapter.VH> {

        public MyAdapter() {
            super(R.layout.box_item);
        }

        @NonNull
        @Override
        protected VH onCreateDefViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(BoxItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        protected void convert(@NonNull VH holder, String item) {
            holder.bind(item);
        }

        static class VH extends com.chad.library.adapter.base.viewholder.BaseViewHolder {

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
