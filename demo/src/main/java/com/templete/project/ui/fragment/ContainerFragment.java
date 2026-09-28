package com.templete.project.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.greendao.db.util.GsonUtil;
import com.lib.base.ui.fragment.BaseFragment;
import com.lib.base.util.DebugUtil;
import com.templete.project.BuildConfig;
import com.templete.project.R;
import com.templete.project.bean.NavBean;
import com.templete.project.databinding.ContainerFragmentBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Date         2026/4/10.
 *
 * @author xxx
 */

public class ContainerFragment extends BaseFragment<ContainerFragmentBinding> {
    public static final String TAG = "ContainerFragment";
    public static final String USER_VIEWPAGER_2 = "userViewPager2";
    public static final String NAVS = "navs";
    public static final String NAV = "nav";
    private NavBean nav;
    private List<NavBean> navs;
    private boolean userViewPager2;
    private List<NavBean> currNavs;
    private boolean isInit;
    private TabLayoutMediator tabLayoutMediator;
    private ViewPager viewPager;
    private ViewPager2 viewPager2;

    /**
     * 第一次调用,可选择使用viewpager还是viewpager2
     *
     * @param navs
     * @return
     */
    public static ContainerFragment newInstance(List<NavBean> navs, boolean userViewPager2) {
        ContainerFragment fragment = new ContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(NAVS, new ArrayList<>(navs));
        bundle.putBoolean(USER_VIEWPAGER_2, userViewPager2);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * 嵌套调用
     *
     * @param nav
     * @return
     */
    public static ContainerFragment newInstance(NavBean nav, boolean userViewPager2) {
        ContainerFragment fragment = new ContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(NAV, nav);
        bundle.putBoolean(USER_VIEWPAGER_2, userViewPager2);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected ContainerFragmentBinding viewBinding(LayoutInflater inflater, ViewGroup container) {
        return ContainerFragmentBinding.inflate(inflater, container, false);
    }

    @Override
    public void inits() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            nav = arguments.getParcelable(NAV);
            navs = arguments.getParcelableArrayList(NAVS);
            userViewPager2 = arguments.getBoolean(USER_VIEWPAGER_2);
        }
    }

    @Override
    public void initView() {
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        isInit = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInit) {
            initTab();
            isInit = true;
        } else {
            DebugUtil.logD(TAG, "onResume,isInit = " + true);
        }
    }

    private void addViewPager(boolean isInner) {
        if (!userViewPager2) {
            mViewBinding.viewPager.setVisibility(View.VISIBLE);
            mViewBinding.viewPager21.setVisibility(View.GONE);
            mViewBinding.host.setVisibility(View.GONE);
            viewPager = mViewBinding.viewPager;
        } else {
            if (!isInner) {
                mViewBinding.viewPager.setVisibility(View.GONE);
                mViewBinding.viewPager21.setVisibility(View.VISIBLE);
                mViewBinding.host.setVisibility(View.GONE);
                viewPager2 = mViewBinding.viewPager21;
            } else {
                mViewBinding.viewPager.setVisibility(View.GONE);
                mViewBinding.viewPager21.setVisibility(View.GONE);
                mViewBinding.host.setVisibility(View.VISIBLE);
                viewPager2 = mViewBinding.viewPager22;
            }
        }
    }

    private void initTab() {
        if (navs != null && !navs.isEmpty()) {
            currNavs = navs;
            addViewPager(false);
        } else if (nav != null) {
            if (nav.navs != null && !nav.navs.isEmpty()) {
                currNavs = nav.navs;
                addViewPager(true);
            } else {
                showContent();
            }
        }
        if (currNavs != null && !currNavs.isEmpty()) {
            int selectIndex = -1;
            for (int i = 0; i < currNavs.size(); i++) {
                NavBean navBean = currNavs.get(i);
                if (navBean.isSelect && selectIndex == -1) {
                    selectIndex = i;
                }
            }
            if (selectIndex == -1) {
                selectIndex = 0;
            }
            int limit = Math.min(3, currNavs.size());
            if (!userViewPager2) {
                // viewpager
                viewPager.setOffscreenPageLimit(limit);
                PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
                viewPager.setAdapter(pagerAdapter);
                mViewBinding.tabLayout.setupWithViewPager(viewPager);
                viewPager.setCurrentItem(selectIndex, false);
                // 监听最外层
                if (navs != null && !navs.isEmpty()) {
                    viewPager.addOnPageChangeListener(listener);
                }
            } else {
                // viewpager2
                viewPager2.setOffscreenPageLimit(limit);
                PagerAdapter2 adapter2 = new PagerAdapter2(this);
                viewPager2.setAdapter(adapter2);
                tabLayoutMediator = new TabLayoutMediator(mViewBinding.tabLayout, viewPager2,
                        (tab, position) -> {
                            if (currNavs != null && position < currNavs.size()) {
                                tab.setText(currNavs.get(position).name);
                            }
                        }
                );
                tabLayoutMediator.attach();
                viewPager2.setCurrentItem(selectIndex, false);
                // 监听最外层
                if (navs != null && !navs.isEmpty()) {
                    viewPager2.registerOnPageChangeCallback(callback);
                }
            }
        }
        if (BuildConfig.DEBUG) {
            DebugUtil.logD(TAG, "initTab,currNavs = " + GsonUtil.toJson(currNavs));
        }
    }

    /**
     * 没有层级了，直接展示内容
     */
    private void showContent() {
        mViewBinding.llContent.setVisibility(View.INVISIBLE);
        mViewBinding.flContainer.setVisibility(View.VISIBLE);
        ContentFragment fragment = ContentFragment.newInstance(nav);
        getChildFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commitNow();
    }

    private final ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
        }
    };

    private final ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    @Override
    public void onDestroyView() {
        if (viewPager != null) {
            viewPager.removeOnPageChangeListener(listener);
            viewPager = null;
        }
        if (viewPager2 != null) {
            viewPager2.unregisterOnPageChangeCallback(callback);
            viewPager2 = null;
        }
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }
        super.onDestroyView();
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (currNavs != null && position < currNavs.size()) {
                return currNavs.get(position).name;
            }
            throw new IllegalStateException("Invalid position: " + position);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            ContainerFragment fragment = getContainerFragment(position);
            if (fragment != null) return fragment;
            throw new IllegalStateException("Invalid position: " + position);
        }

        @Override
        public int getCount() {
            return currNavs != null ? currNavs.size() : 0;
        }
    }

    private class PagerAdapter2 extends FragmentStateAdapter {
        public PagerAdapter2(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            ContainerFragment fragment = getContainerFragment(position);
            if (fragment != null) return fragment;
            throw new IllegalStateException("Invalid position: " + position);
        }

        @Override
        public int getItemCount() {
            return currNavs != null ? currNavs.size() : 0;
        }
    }

    @Nullable
    private ContainerFragment getContainerFragment(int position) {
        if (currNavs != null && position < currNavs.size()) {
            NavBean navBean = currNavs.get(position);
            return ContainerFragment.newInstance(navBean, userViewPager2);
        }
        return null;
    }
}