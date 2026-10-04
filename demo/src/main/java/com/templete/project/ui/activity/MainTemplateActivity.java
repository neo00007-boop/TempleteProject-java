package com.templete.project.ui.activity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.greendao.db.helper.CityBeanDao;
import com.greendao.db.util.LocalRepository;
import com.lib.base.aroute.ArouteConfig;
import com.lib.base.ui.activity.BaseMvvmActivity;
import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.util.LoginUtil;
import com.templete.project.R;
import com.templete.project.databinding.MainTempleteActivityBinding;
import com.templete.project.mvvm.MainViewModel;
import com.templete.project.ui.fragment.FirstFragment;
import com.templete.project.ui.fragment.ForthFragment;
import com.templete.project.ui.fragment.SecondFragment;
import com.templete.project.ui.fragment.ThirdFragment;
import com.templete.project.ui.widget.TabHost;

/**
 * @author xwchen
 */
@Route(path = ArouteConfig.ACTIVITY_MAIN)
public class MainTemplateActivity extends BaseMvvmActivity<MainTempleteActivityBinding, MainViewModel> {
    public static final String TAG = "MainActivity";

    @Override
    protected Class<MainViewModel> getViewModelClass() {
        return MainViewModel.class;
    }

    @Override
    public int getTitleTheme() {
        return TitleBarTheme.THEME_NONE;
    }

    @Override
    public boolean darkStatusBarFont() {
        return true;
    }

    @Override
    protected MainTempleteActivityBinding viewBinding() {
        return MainTempleteActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public int finishAnimType() {
        return ANIM_NO;
    }

    @Override
    public void inits() {
        //使用正常主题覆盖闪屏主题
        setTheme(R.style.AppTheme);
        setImmersionBar();
    }

    @Override
    public void initView() {
        TabHost
                .Builder
                .newBuilder()
                .setManager(getSupportFragmentManager())
                .setContainerId(R.id.fl_container)
                .setFragments(FirstFragment.class, SecondFragment.class, ThirdFragment.class, ForthFragment.class)
                .build(mViewBinding.tabHost)
                .setOnLoginListener(false, 3, LoginUtil::toLogin)
                .setOnTabChangedListener(tabIndex -> logD(TAG, "tabIndex=" + tabIndex))
                .over();
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
//        LocalRepository.getInstance().getDaoSession().getDemoBeanDao().insert(new DemoBean(null,"",1));
        CityBeanDao cityBeanDao = LocalRepository.getInstance().getCityBeanDao();
        logD("cityBeanDao", "cityBeanDao1=" + cityBeanDao);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logD("startTime", "startTime2=" + System.currentTimeMillis());
    }
}