package com.templete.project.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flyco.tablayoutsamples.ui.SimpleHomeActivity;
import com.lib.base.bean.BtnBean;
import com.lib.base.rxjava.RxUtils;
import com.lib.base.ui.fragment.BaseFragment;
import com.lib.base.util.LoginUtil;
import com.templete.project.databinding.FirstFragmentBinding;
import com.templete.project.ui.activity.CanvasActivity;
import com.templete.project.ui.activity.DialogActivity;
import com.templete.project.ui.activity.FlexActivity;
import com.templete.project.ui.activity.FlexBoxActivity;
import com.templete.project.ui.activity.GlideActivity;
import com.templete.project.ui.activity.ImmersionActivity;
import com.templete.project.ui.activity.NineGridActivity;
import com.templete.project.ui.activity.PopActivity;
import com.templete.project.ui.activity.RatioActivity;
import com.templete.project.ui.activity.ShapeActivity;
import com.templete.project.ui.activity.SheetActivity;
import com.templete.project.ui.activity.ShimmerActivity;
import com.templete.project.ui.activity.TxtActivity;
import com.templete.project.ui.activity.ViewActivity;

/**
 * Demo 第一页：常用控件。
 */
public class FirstFragment extends BaseFragment<FirstFragmentBinding> {
    public static final String TAG = "FirstFragment";

    @Override
    public FirstFragmentBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return FirstFragmentBinding.inflate(getLayoutInflater(), container, false);
    }

    @Override
    public void inits() {
        mViewBinding.titleBar.setRightClickViews((position, view) -> toast("right click"),
                false, new BtnBean("按钮1"), new BtnBean("按钮1"), new BtnBean("按钮1"), new BtnBean("按钮1"));
    }

    @Override
    public void initView() {
        TextView tvTitle = mViewBinding.titleBar.getTvTitle();
        if (tvTitle != null) {
            RxUtils.setClickCountAtLeast(tvTitle, 10, view -> LoginUtil.restartApplication());
        }
    }

    @Override
    public void initEvent() {
        setOnClickListener(v -> {
                    Class<?> clazz = null;
                    if (v.equals(mViewBinding.tv1)) {
                        clazz = PopActivity.class;
                    } else if (v.equals(mViewBinding.tv2)) {
                        clazz = ShapeActivity.class;
                    } else if (v.equals(mViewBinding.tv3)) {
                        clazz = DialogActivity.class;
                    } else if (v.equals(mViewBinding.tv4)) {
                        clazz = SheetActivity.class;
                    } else if (v.equals(mViewBinding.tv5)) {
                        clazz = SimpleHomeActivity.class;
                    } else if (v.equals(mViewBinding.tv6)) {
                        clazz = ViewActivity.class;
                    } else if (v.equals(mViewBinding.tv7)) {
                        clazz = RatioActivity.class;
                    } else if (v.equals(mViewBinding.tv8)) {
                        clazz = GlideActivity.class;
                    } else if (v.equals(mViewBinding.tv9)) {
                        clazz = ImmersionActivity.class;
                    } else if (v.equals(mViewBinding.tv10)) {
                        clazz = TxtActivity.class;
                    } else if (v.equals(mViewBinding.tv11)) {
                        clazz = ShimmerActivity.class;
                    } else if (v.equals(mViewBinding.tv12)) {
                        clazz = FlexBoxActivity.class;
                    } else if (v.equals(mViewBinding.tv13)) {
                        clazz = FlexActivity.class;
                    } else if (v.equals(mViewBinding.tv14)) {
                        clazz = CanvasActivity.class;
                    } else if (v.equals(mViewBinding.tv15)) {
                        clazz = NineGridActivity.class;
                    }
                    if (clazz != null) {
                        getBaseActivity().startAty(requireActivity(), clazz);
                    }
                },
                mViewBinding.tv1, mViewBinding.tv2, mViewBinding.tv3,
                mViewBinding.tv4, mViewBinding.tv5, mViewBinding.tv6,
                mViewBinding.tv7, mViewBinding.tv8, mViewBinding.tv9,
                mViewBinding.tv10, mViewBinding.tv11, mViewBinding.tv12,
                mViewBinding.tv13, mViewBinding.tv14, mViewBinding.tv15);
    }

    @Override
    public void initData() {
    }

    @Override
    public void onClick(View v) {
    }
}
