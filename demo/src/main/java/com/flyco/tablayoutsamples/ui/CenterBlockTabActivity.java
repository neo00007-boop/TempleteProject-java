package com.flyco.tablayoutsamples.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.flyco.tablayout.CenterBlockSlidingTabLayout;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.ActivityCenterBlockTabBinding;

import java.util.ArrayList;

public class CenterBlockTabActivity extends BaseActivity<ActivityCenterBlockTabBinding> {
    private final ArrayList<Fragment> mFragments = new ArrayList<>();
    private final String[] mTitles = {"英超", "西甲", "德甲", "意甲", "法甲"};

    @Override
    public void inits() {
        setTitleStr("CenterBlockSlidingTabLayout");
    }

    @Override
    public void initView() {
        for (String title : mTitles) {
            mFragments.add(SimpleCardFragment.getInstance(title));
        }
        mViewBinding.vp.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        CenterBlockSlidingTabLayout tabLayout = mViewBinding.sTabLayoutLeague;
        tabLayout.setViewPager(mViewBinding.vp, mTitles);
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @Override
    protected ActivityCenterBlockTabBinding viewBinding() {
        return ActivityCenterBlockTabBinding.inflate(getLayoutInflater());
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
