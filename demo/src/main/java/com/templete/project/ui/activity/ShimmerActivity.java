package com.templete.project.ui.activity;

import com.lib.base.bean.BtnBean;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.widget.ShimmerView;
import com.templete.project.databinding.ShimmerActivityBinding;

/**
 * 骨架屏 Demo
 */
public class ShimmerActivity extends BaseActivity<ShimmerActivityBinding> {
    @Override
    public void inits() {
        setRightClickViews((position, view) -> {
            mViewBinding.shimmerView.setType(position == 0 ? ShimmerView.TYPE_GRID : ShimmerView.TYPE_LIST);
            mViewBinding.shimmerView.show();
        }, false, new BtnBean("grid"), new BtnBean("list"));
    }

    @Override
    public void initView() {
        mViewBinding.shimmerView.setType(ShimmerView.TYPE_GRID);
        mViewBinding.shimmerView.show();
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @Override
    protected ShimmerActivityBinding viewBinding() {
        return ShimmerActivityBinding.inflate(getLayoutInflater());
    }
}
