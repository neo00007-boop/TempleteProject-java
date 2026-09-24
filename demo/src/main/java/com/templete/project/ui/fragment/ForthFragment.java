package com.templete.project.ui.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.fragment.BaseFragment;
import com.lib.base.util.IntentUtil;
import com.templete.project.databinding.ForthFragmentBinding;
import com.templete.project.ui.activity.CanvasActivity;
import com.templete.project.ui.activity.DataBusActivity;
import com.templete.project.ui.activity.DatabindingActivity;
import com.templete.project.ui.activity.FreshFragmentHostActivity;
import com.templete.project.ui.activity.FreshListDemoActivity;
import com.templete.project.ui.activity.FreshMultiListDemoActivity;
import com.templete.project.ui.activity.IndexActivity;
import com.templete.project.ui.activity.NavsActivity;
import com.templete.project.ui.activity.PinnedActivity;
import com.templete.project.ui.activity.ShimmerActivity;
import com.templete.project.ui.activity.SoftActivity;
import com.templete.project.ui.activity.TxtActivity;

/**
 * PackageName  com.templete.project.ui.fragment
 * ProjectName  TempleteProject
 * Date         10/10/21.
 *
 * @author xwchen
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
                    if (v.equals(mViewBinding.tv13)) {
                        Intent intent = new Intent(requireActivity(), FreshFragmentHostActivity.class);
                        intent.putExtra(FreshFragmentHostActivity.EXTRA_MULTI, false);
                        startActivity(intent);
                        return;
                    }
                    if (v.equals(mViewBinding.tv14)) {
                        Intent intent = new Intent(requireActivity(), FreshFragmentHostActivity.class);
                        intent.putExtra(FreshFragmentHostActivity.EXTRA_MULTI, true);
                        startActivity(intent);
                        return;
                    }
                    Class<? extends BaseActivity<?>> clazz = null;
                    if (v.equals(mViewBinding.tv1)) {
                        clazz = TxtActivity.class;
                    } else if (v.equals(mViewBinding.tv2)) {
                        clazz = IndexActivity.class;
                    } else if (v.equals(mViewBinding.tv3)) {
                        clazz = SoftActivity.class;
                    } else if (v.equals(mViewBinding.tv4)) {
                        clazz = CanvasActivity.class;
//                    } else if (v.equals(mViewBinding.tv5)) {
//                        clazz = WrapActivity.class;
                    } else if (v.equals(mViewBinding.tv6)) {
                        clazz = NavsActivity.class;
                    } else if (v.equals(mViewBinding.tv8)) {
                        clazz = PinnedActivity.class;
                    } else if (v.equals(mViewBinding.tv9)) {
                        clazz = DataBusActivity.class;
                    } else if (v.equals(mViewBinding.tv10)) {
                        clazz = ShimmerActivity.class;
                    } else if (v.equals(mViewBinding.tv11)) {
                        clazz = FreshListDemoActivity.class;
                    } else if (v.equals(mViewBinding.tv12)) {
                        clazz = FreshMultiListDemoActivity.class;
                    }
                    if (clazz != null) {
                        ((BaseActivity<?>) requireActivity()).startAty(requireActivity(), clazz);
                    }
                },
                mViewBinding.tv1, mViewBinding.tv2,
                mViewBinding.tv3, mViewBinding.tv4,
                /*mViewBinding.tv5,*/ mViewBinding.tv6,
                mViewBinding.tv8, mViewBinding.tv9,
                mViewBinding.tv10, mViewBinding.tv11, mViewBinding.tv12,
                mViewBinding.tv13, mViewBinding.tv14);

        mViewBinding.tv7.setOnClickListener(v -> IntentUtil.startActivity(requireActivity(), DatabindingActivity.class));
    }

    @Override
    public void initData() {

    }
}
