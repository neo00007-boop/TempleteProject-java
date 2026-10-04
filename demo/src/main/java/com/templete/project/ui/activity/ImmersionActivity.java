package com.templete.project.ui.activity;

import com.lib.base.bean.BtnBean;
import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.R;
import com.templete.project.databinding.ImmersionActivityBinding;

/* <!--animateLayoutChanges,动态addView时添加默认的渐变动画,不能指定动画-->*/

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui
 *
 * @author xwchen
 * Date         2021/12/27.
 */

public class ImmersionActivity extends BaseActivity<ImmersionActivityBinding> {
    @Override
    protected ImmersionActivityBinding viewBinding() {
        return ImmersionActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("沉浸式UI");
    }

    @Override
    public int getTitleTheme() {
        return TitleBarTheme.THEME_BLUE;
    }

    @Override
    public boolean darkStatusBarFont() {
        return true;
    }

    @Override
    public void initView() {
        mViewBinding.tv1.setOnClickListener(v -> {
            setTitleStr("我是设置的标题");
            mViewBinding.tv1.setText("标题已设置");
        });
        mViewBinding.tv2.setOnClickListener(v -> setTitleStr("跑马灯标题跑马灯标题跑马灯标题"));
        mViewBinding.tv3.setOnClickListener(v -> setTitleStrWithBold("粗体跑马灯标题粗体跑马灯标题粗体跑马灯标题"));
        mViewBinding.tv4.setOnClickListener(v -> hideBackView());
        mViewBinding.tv5.setOnClickListener(v -> setStatueBarTextColorDark(true));
        mViewBinding.tv6.setOnClickListener(v -> setStatueBarTextColorDark(false));
        mViewBinding.tv7.setOnClickListener(v -> clearRightBtns());
        mViewBinding.tv8.setOnClickListener(v ->
                setRightClickViews((position, view) -> {
                            toast("按钮" + (position + 1) + "点击");
                            //todo
                        }, false,
                        new BtnBean("确定"),
                        new BtnBean(R.drawable.ic_home_search)));
        mViewBinding.tv9.setOnClickListener(v ->
                setRightClickViews((position, view) -> {
                            toast("按钮" + (position + 1) + "点击");
                            //todo
                        }, true,
                        new BtnBean("我是按钮1", R.drawable.ic_pop1),
                        new BtnBean("我是按钮2", R.drawable.ic_pop1),
                        new BtnBean("我是按钮3", R.drawable.ic_pop1),
                        new BtnBean("我是按钮4", R.drawable.ic_pop1),
                        new BtnBean("我是按钮5", R.drawable.ic_pop1)));
        mViewBinding.tv10.setOnClickListener(v -> backClickIntercept = true);
        mViewBinding.tv11.setOnClickListener(v -> setTitleBg(R.drawable.ic_top_bg));
        mViewBinding.tv12.setOnClickListener(v -> {
            animType = ANIM_NORMAL;
            finish();
        });
        mViewBinding.tv13.setOnClickListener(v -> {
            animType = ANIM_LOGIN;
            finish();
        });
        mViewBinding.tv14.setOnClickListener(v -> {
            animType = ANIM_NO;
            finish();
        });
    }

    boolean backClickIntercept = false;

    @Override
    public boolean backClickIntercept() {
        return backClickIntercept;
    }

    @Override
    public boolean actionTitleBackClick(boolean keyBack) {
        toast(keyBack ? "点击返回按钮" : "点击标题栏左侧返回按钮");
        return false;
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {

    }

    private int animType = ANIM_NORMAL;

    @Override
    public int finishAnimType() {
        return animType;
    }

}
