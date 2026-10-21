package com.templete.project.ui.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.fragment.BaseFragment;
import com.templete.project.databinding.ThirdFragmentBinding;
import com.templete.project.ui.activity.FreshFragmentHostActivity;
import com.templete.project.ui.activity.FreshListDemoActivity;
import com.templete.project.ui.activity.FreshMultiListDemoActivity;
import com.templete.project.ui.activity.GrassFreshActivity;
import com.templete.project.ui.activity.HorizontalFreshActivity;
import com.templete.project.ui.activity.IndexActivity;
import com.templete.project.ui.activity.PinnedActivity;
import com.templete.project.ui.activity.ScrollActivity;
import com.templete.project.ui.activity.SunFreshActivity;
import com.templete.project.ui.activity.TranslateActivity;
import com.templete.project.ui.activity.WrapActivity;

/**
 * Demo 第三页：列表刷新。
 */
public class ThirdFragment extends BaseFragment<ThirdFragmentBinding> {
    @Override
    public ThirdFragmentBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return ThirdFragmentBinding.inflate(getLayoutInflater(), container, false);
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
                    if (v.equals(mViewBinding.tv8)) {
                        Intent intent = new Intent(requireActivity(), FreshFragmentHostActivity.class);
                        intent.putExtra(FreshFragmentHostActivity.EXTRA_MULTI, false);
                        startActivity(intent);
                        return;
                    }
                    if (v.equals(mViewBinding.tv9)) {
                        Intent intent = new Intent(requireActivity(), FreshFragmentHostActivity.class);
                        intent.putExtra(FreshFragmentHostActivity.EXTRA_MULTI, true);
                        startActivity(intent);
                        return;
                    }
                    Class<? extends BaseActivity<?>> clazz = null;
                    if (v.equals(mViewBinding.tv1)) {
                        clazz = GrassFreshActivity.class;
                    } else if (v.equals(mViewBinding.tv2)) {
                        clazz = SunFreshActivity.class;
                    } else if (v.equals(mViewBinding.tv3)) {
                        clazz = HorizontalFreshActivity.class;
                    } else if (v.equals(mViewBinding.tv4)) {
                        clazz = ScrollActivity.class;
                    } else if (v.equals(mViewBinding.tv5)) {
                        clazz = TranslateActivity.class;
                    } else if (v.equals(mViewBinding.tv6)) {
                        clazz = FreshListDemoActivity.class;
                    } else if (v.equals(mViewBinding.tv7)) {
                        clazz = FreshMultiListDemoActivity.class;
                    } else if (v.equals(mViewBinding.tv10)) {
                        clazz = IndexActivity.class;
                    } else if (v.equals(mViewBinding.tv11)) {
                        clazz = PinnedActivity.class;
                    } else if (v.equals(mViewBinding.tv12)) {
                        clazz = WrapActivity.class;
                    }
                    if (clazz != null) {
                        ((BaseActivity<?>) requireActivity()).startAty(requireActivity(), clazz);
                    }
                },
                mViewBinding.tv1, mViewBinding.tv2, mViewBinding.tv3,
                mViewBinding.tv4, mViewBinding.tv5, mViewBinding.tv6,
                mViewBinding.tv7, mViewBinding.tv8, mViewBinding.tv9,
                mViewBinding.tv10, mViewBinding.tv11, mViewBinding.tv12);
    }

    @Override
    public void initData() {
    }
}
