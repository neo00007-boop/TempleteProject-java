
package com.templete.project.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.lib.base.ui.fragment.BaseFragment;
import com.lib.base.util.DebugUtil;
import com.lib.base.util.LoginUtil;
import com.lib.base.util.inject.MyException;
import com.templete.project.databinding.TabContainerBinding;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * @author Dave
 * @date 2017/1/18
 */

public class TabHost extends LinearLayout {
    public static final String TAG = "TabHost";
    private int lastTab = -1;
    private int containerId;
    private int currentTab = 0;
    private FragmentManager manager;
    private BaseFragment<?> currentFragment;
    private View[] tabViewList;
    private BaseFragment<?>[] fragmentList;
    private boolean intercept;
    private int interceptIndex;

    public TabHost(Context context) {
        this(context, null);
    }

    public TabHost(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabHost(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @SuppressLint("FindViewByIdCast")
    private void initView(Context context) {
        TabContainerBinding binding = TabContainerBinding.inflate(LayoutInflater.from(context), this, true);
        tabViewList = new View[]{binding.tab1, binding.tab2, binding.tab3, binding.tab4, binding.tab5};
    }

    private void setTabs() {
        checkData();
        tabClick();
        showFragment();
    }

    private void checkData() {
        if (tabViewList.length != fragmentList.length) {
            throw new RuntimeException("are you ok?");
        }
    }

    private void showFragment() {
        if (lastTab != -1) {
            currentFragment = fragmentList[lastTab];
            setTabSelected(lastTab);
            currentTab = lastTab;
        } else {
            BaseFragment<?> fragment = fragmentList[0];
            manager
                    .beginTransaction()
                    .add(containerId, fragment, getClassTag(fragment))
                    .commit();
            currentFragment = fragment;
            setTabSelected(0);
            currentTab = 0;
        }
        callBack();
    }

    private String getClassTag(BaseFragment<?> fragment) {
        return fragment.getClass().getSimpleName();
    }

    private void callBack() {
        if (null != onTabChangedListener) {
            //回调到MainActivity
            onTabChangedListener.tabChange(currentTab);
        }
        currentFragment.tabClick();//直接回调到Fragment
    }

    //此处有判断登录的逻辑
    private void tabClick() {
        for (int i = 0; i < tabViewList.length; i++) {
            int finalI = i;
            tabViewList[i].setOnClickListener(v -> changeFragment(finalI));
        }
    }

    private void changeFragment(int finalI) {
        if (intercept && finalI >= interceptIndex && !LoginUtil.isLogin()) {
            if (onUserStateListener != null) {
                onUserStateListener.login();
            }
            return;
        }
        //
        BaseFragment<?> fragment = fragmentList[finalI];
        //是当前的tab,再次点击的话无效
        if (currentFragment.equals(fragment)) {
            return;
        }
        if (fragment.isAdded()) {
            DebugUtil.logD("sss", "isAdded");
            manager
                    .beginTransaction()
                    .hide(currentFragment)
                    .show(fragment)
                    .commit();
        } else {
            DebugUtil.logD("sss", "notAdded");
            manager
                    .beginTransaction()
                    .hide(currentFragment)
                    .add(containerId, fragment, getClassTag(fragment))
                    .commit();
        }
        //状态
        currentFragment = fragment;
        setTabSelected(finalI);
        currentTab = finalI;
        //以下两种都可以,根据需求选择即可
        callBack();
    }

    //设置选中状态
    private void setTabSelected(int position) {
        for (int i = 0; i < tabViewList.length; i++) {
            tabViewList[i].setSelected(position == i);
        }
    }

    //自定义切换tab栏
    public void setCurrentTab(int tabPosition) {
        if (tabPosition >= 0 && tabPosition < tabViewList.length) {
            changeFragment(tabPosition);
        }
    }

    //获取当前tab栏
    public int getCurrentTab() {
        return currentTab;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }


    /*public void showPop(int unReadCount) {
        if (unReadCount < 99) {
            tv_pop.setText(String.valueOf(unReadCount));
        } else {
            tv_pop.setText(R.string.str_99);
        }
        fl_pop.setVisibility(0 == unReadCount ? View.GONE : View.VISIBLE);
    }*/

    private OnTabChangedListener onTabChangedListener;

    /**
     * tab切换后在MainActivity里面的回调,参数为下标
     */
    public TabHost setOnTabChangedListener(OnTabChangedListener onTabChangedListener) {
        this.onTabChangedListener = onTabChangedListener;
        return this;
    }

    public void over() {
        DebugUtil.logD(TAG, "TabHost init over!lastTab=" + lastTab);
    }

    public interface OnTabChangedListener {
        void tabChange(int tabIndex);
    }

    private OnUserStateListener onUserStateListener;

    /**
     * 登录拦截
     *
     * @param intercept           是否拦截
     * @param interceptIndex      从第几个角标开始拦截
     * @param onUserStateListener 拦截回调
     * @return
     */
    public TabHost setOnLoginListener(boolean intercept, int interceptIndex, OnUserStateListener onUserStateListener) {
        this.intercept = intercept;
        this.interceptIndex = interceptIndex;
        this.onUserStateListener = onUserStateListener;
        return this;
    }

    /**
     * 登录状态回调接口
     */
    public interface OnUserStateListener {
        /**
         * login事件
         */
        void login();
    }

    public static class Builder {
        FragmentManager manager;
        int containerId;
        BaseFragment<?>[] fragmentList;
        int lastTab;

        public Builder() {
            manager = null;
            containerId = -1;
            fragmentList = new BaseFragment[0];
            lastTab = -1;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder setManager(FragmentManager manager) {
            this.manager = manager;
            return this;
        }

        public Builder setContainerId(int containerId) {
            this.containerId = containerId;
            return this;
        }

        /**
         * 1.利用反射生成BaseFragment对象;
         * 2.Fragment内存不足销毁后重新创建时,可以使用findFragmentByTag找到之前销毁的Fragment,
         * 前提是add()时候添加了tag.
         *
         * @return
         */
        @SafeVarargs
        public final Builder setFragments(Class<? extends BaseFragment<?>>... clazz) {
            if (clazz == null || clazz.length == 0) {
                throw new RuntimeException("fragments is empty!");
            }
            fragmentList = new BaseFragment[clazz.length];
            for (int i = 0; i < clazz.length; i++) {
                Class<?> aClass = clazz[i];
                BaseFragment<?> fragment = (BaseFragment<?>) manager.findFragmentByTag(aClass.getSimpleName());
                if (fragment != null) {
                    //记录之前选中的position
                    if (!fragment.isHidden()) {
                        lastTab = i;
                    }
                } else {
                    //直接new 一个Fragment
                    Object o = null;
                    try {
                        o = aClass.newInstance();
                    } catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                    if (!(o instanceof BaseFragment)) {
                        throw new RuntimeException("clazz must be BaseFragment!");
                    } else {
                        fragment = (BaseFragment<?>) o;
                    }
                }
                fragmentList[i] = fragment;
            }
            return this;
        }

        public TabHost build(TabHost tabHost) {
            if (manager == null) {
                throw new MyException("manager 为空！");
            }
            if (containerId == 0) {
                throw new MyException("containerId 为空！");
            }
            if (fragmentList == null || fragmentList.length == 0) {
                throw new MyException("fragmentList 为空！");
            }
            tabHost.manager = manager;
            tabHost.containerId = containerId;
            tabHost.fragmentList = fragmentList;
            tabHost.lastTab = lastTab;
            tabHost.setTabs();
            return tabHost;
        }
    }
}
