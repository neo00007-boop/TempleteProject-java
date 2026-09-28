package com.flyco.tablayoutsamples.ui;

import android.content.Context;
import android.content.Intent;
import android.widget.AdapterView;

import com.flyco.tablayoutsamples.adapter.SimpleHomeAdapter;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.ActivitySimpleHomeBinding;

public class SimpleHomeActivity extends BaseActivity<ActivitySimpleHomeBinding> {
    private final Context mContext = this;
    private final String[] mItems = {"SlidingTabLayout", "CommonTabLayout", "SegmentTabLayout",
            "GradientSlidingTabLayout", "DrawableIndicatorSlidingTabLayout",
            "CenterBlockSlidingTabLayout", "DesignSlidingTabLayout"};
    private final Class<?>[] mClasses = {SlidingTabActivity.class, CommonTabActivity.class,
            SegmentTabActivity.class, GradientTabActivity.class, DrawableTabActivity.class,
            CenterBlockTabActivity.class, DesignTabActivity.class};

    @Override
    public void inits() {
        setTitleStr("FlycoTabLayout");
    }

    @Override
    public void initView() {
        mViewBinding.lv.setCacheColorHint(android.graphics.Color.TRANSPARENT);
        mViewBinding.lv.setFadingEdgeLength(0);
        mViewBinding.lv.setAdapter(new SimpleHomeAdapter(mContext, mItems));
    }

    @Override
    public void initEvent() {
        mViewBinding.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                startActivity(new Intent(mContext, mClasses[position]));
            }
        });
    }

    @Override
    public void initData() {
    }

    @Override
    protected ActivitySimpleHomeBinding viewBinding() {
        return ActivitySimpleHomeBinding.inflate(getLayoutInflater());
    }
}
