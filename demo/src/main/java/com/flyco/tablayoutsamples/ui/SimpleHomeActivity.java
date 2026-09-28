package com.flyco.tablayoutsamples.ui;

import android.view.View;

import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.ActivitySimpleHomeBinding;

public class SimpleHomeActivity extends BaseActivity<ActivitySimpleHomeBinding> {

    @Override
    public void inits() {
        setTitleStr("标签");
    }

    @Override
    public void initView() {
    }

    @Override
    public void initEvent() {
        setOnClickListener(this::openDemo,
                mViewBinding.tvSliding, mViewBinding.tvCommon, mViewBinding.tvSegment,
                mViewBinding.tvGradient, mViewBinding.tvDrawable, mViewBinding.tvCenter,
                mViewBinding.tvDesign);
    }

    @Override
    public void initData() {
    }

    @Override
    protected ActivitySimpleHomeBinding viewBinding() {
        return ActivitySimpleHomeBinding.inflate(getLayoutInflater());
    }

    private void openDemo(View view) {
        Class<?> clazz;
        if (view.equals(mViewBinding.tvSliding)) {
            clazz = SlidingTabActivity.class;
        } else if (view.equals(mViewBinding.tvCommon)) {
            clazz = CommonTabActivity.class;
        } else if (view.equals(mViewBinding.tvSegment)) {
            clazz = SegmentTabActivity.class;
        } else if (view.equals(mViewBinding.tvGradient)) {
            clazz = GradientTabActivity.class;
        } else if (view.equals(mViewBinding.tvDrawable)) {
            clazz = DrawableTabActivity.class;
        } else if (view.equals(mViewBinding.tvCenter)) {
            clazz = CenterBlockTabActivity.class;
        } else {
            clazz = DesignTabActivity.class;
        }
        startAty(this, clazz);
    }
}
