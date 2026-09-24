package com.templete.project.ui.activity;

import androidx.fragment.app.Fragment;

import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.FragmentHostActivityBinding;
import com.templete.project.ui.fragment.FreshListDemoFragment;
import com.templete.project.ui.fragment.FreshMultiListDemoFragment;

/**
 * 承载刷新列表 Fragment Demo 的壳 Activity。
 * {@link #EXTRA_MULTI} = true 时加载多类型 Demo。
 */
public class FreshFragmentHostActivity extends BaseActivity<FragmentHostActivityBinding> {

    public static final String EXTRA_MULTI = "extra_multi";

    @Override
    public void inits() {
        boolean multi = getIntent().getBooleanExtra(EXTRA_MULTI, false);
        setTitleStr(multi ? "多类型刷新列表 Fragment Demo" : "刷新列表 Fragment Demo");
    }

    @Override
    public void initView() {
        boolean multi = getIntent().getBooleanExtra(EXTRA_MULTI, false);
        Fragment fragment = multi ? new FreshMultiListDemoFragment() : new FreshListDemoFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(com.templete.project.R.id.fl_container, fragment)
                .commitNow();
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @Override
    protected FragmentHostActivityBinding viewBinding() {
        return FragmentHostActivityBinding.inflate(getLayoutInflater());
    }
}
