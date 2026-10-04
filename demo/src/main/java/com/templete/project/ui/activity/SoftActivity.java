package com.templete.project.ui.activity;

import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.databinding.SoftActivityBinding;

/**
 * PackageName  com.templete.project.ui.activity
 * ProjectName  TempleteProject-java
 * Date         2022/2/20.
 *
 * @author xwchen
 */

public class SoftActivity extends BaseActivity<SoftActivityBinding> {

    @Override
    public int getTitleTheme() {
        return TitleBarTheme.THEME_BLUE;
    }

    @Override
    public void inits() {
        setTitleStr("输入法");
    }

    /**
     * 不需要再使用.getViewTreeObserver()..addOnGlobalLayoutListener...来判断
     *
     * @param open
     * @param keyboardHeight
     */
    @Override
    public void togoSoftKeyboard(boolean open, int keyboardHeight) {
        logD("softKeyboard", "open=" + open + ",keyboardHeight=" + keyboardHeight);
        toast((open ? "键盘弹出" : "键盘关闭") + ",键盘高度=" + keyboardHeight);
    }

    @Override
    public void initView() {
        //标题栏支持沉浸式图片背景,输入法正常使用
        //setTitleBg(R.drawable.ic_top_bg);

//        mViewBinding.getRoot().postDelayed(runnable, 2000);
//        mViewBinding.getRoot().postDelayed(runnable, 4000);
    }

    private final Runnable runnable = () -> toggleSoftInput(mViewBinding.getRoot());

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {

    }

    @Override
    protected SoftActivityBinding viewBinding() {
        return SoftActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewBinding.getRoot().removeCallbacks(runnable);
    }
}
