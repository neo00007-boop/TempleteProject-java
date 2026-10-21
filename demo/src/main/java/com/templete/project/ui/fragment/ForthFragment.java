package com.templete.project.ui.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.fragment.BaseFragment;
import com.templete.project.databinding.ForthFragmentBinding;
import com.templete.project.ui.activity.MultipleElementActivity;
import com.templete.project.ui.activity.NavsActivity;
import com.templete.project.ui.activity.OneElementActivity;
import com.templete.project.ui.activity.SoftActivity;

/**
 * Demo 第四页：页面交互（导航 / 转场 / 输入法）。
 */
public class ForthFragment extends BaseFragment<ForthFragmentBinding> {
    @Override
    public ForthFragmentBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return ForthFragmentBinding.inflate(getLayoutInflater(), container, false);
    }

    @Override
    public void inits() {
    }

    @Override
    public void initView() {
    }

    @Override
    public void initEvent() {
        setOnClickListener(v -> {
                    Class<? extends BaseActivity<?>> clazz = null;
                    if (v.equals(mViewBinding.tv1)) {
                        clazz = NavsActivity.class;
                    } else if (v.equals(mViewBinding.tv2)) {
                        clazz = OneElementActivity.class;
                    } else if (v.equals(mViewBinding.tv3)) {
                        clazz = MultipleElementActivity.class;
                    } else if (v.equals(mViewBinding.tv4)) {
                        clazz = SoftActivity.class;
                    }
                    if (clazz != null) {
                        ((BaseActivity<?>) requireActivity()).startAty(requireActivity(), clazz);
                    }
                },
                mViewBinding.tv1, mViewBinding.tv2,
                mViewBinding.tv3, mViewBinding.tv4);
    }

    @Override
    public void initData() {
    }
}
