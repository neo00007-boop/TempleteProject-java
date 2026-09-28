package com.flyco.tablayoutsamples.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.flyco.tablayout.GradientSlidingTabLayout;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.ActivityGradientTabBinding;

import java.util.ArrayList;

public class GradientTabActivity extends BaseActivity<ActivityGradientTabBinding> {
    private final ArrayList<Fragment> mFragments = new ArrayList<>();
    private final String[] mTitles = {"赛况", "阵容", "数据", "指数"};

    @Override
    public void inits() {
        setTitleStr("GradientSlidingTabLayout");
    }

    @Override
    public void initView() {
        for (String title : mTitles) {
            mFragments.add(SimpleCardFragment.getInstance(title));
        }
        mViewBinding.vp.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        GradientSlidingTabLayout tabLayout = mViewBinding.xtablayoutInfo;
        tabLayout.setViewPager(mViewBinding.vp, mTitles);
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @Override
    protected ActivityGradientTabBinding viewBinding() {
        return ActivityGradientTabBinding.inflate(getLayoutInflater());
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }
}
