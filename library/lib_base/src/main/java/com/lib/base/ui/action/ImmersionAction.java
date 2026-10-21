package com.lib.base.ui.action;

import com.gyf.immersionbar.ImmersionBar;
import com.lib.base.R;

/**
 * 状态栏 / 沉浸式统一入口，避免多处重复拼 ImmersionBar 链式调用。
 */
public interface ImmersionAction extends ContextAction {

    /**
     * 无标题栏主题时的默认：深色状态栏图标。
     * 有标题栏时请在 {@link TitleBarAction} 中按主题覆盖。
     */
    default boolean darkStatusBarFont() {
        return true;
    }

    /**
     * 透明状态栏，内容顶到状态栏下方（全屏宿主页常用）。
     */
    default void setImmersionBar() {
        applyImmersionBar(R.color.cl_no_color, false, darkStatusBarFont());
    }

    /**
     * 着色状态栏。
     */
    default void setImmersionBar(int statusBarColor) {
        applyImmersionBar(statusBarColor, false, darkStatusBarFont());
    }

    /**
     * 完整参数入口。
     *
     * @param statusBarColor    状态栏颜色
     * @param fitsSystemWindows true：内容在状态栏下；false：内容延伸到状态栏
     */
    default void setImmersionBar(int statusBarColor, boolean fitsSystemWindows) {
        applyImmersionBar(statusBarColor, fitsSystemWindows, darkStatusBarFont());
    }

    /**
     * 只改状态栏图标深浅（保留当前其它 Immersion 配置会被重新 init）。
     */
    default void setStatueBarTextColorDark(boolean dark) {
        applyImmersionBar(R.color.cl_no_color, false, dark);
    }

    /**
     * 标题栏铺背景图时：透明状态栏 + 浅色图标，并露出 TitleBar 内部占位条。
     */
    default void specialImmersion() {
        applyImmersionBar(R.color.cl_no_color, false, false);
    }

    /**
     * ImmersionBar 唯一拼装点。
     */
    default void applyImmersionBar(int statusBarColor, boolean fitsSystemWindows, boolean darkFont) {
        ImmersionBar
                .with(getCurrentActivity())
                .statusBarColor(statusBarColor)
                .fitsSystemWindows(fitsSystemWindows)
                .statusBarDarkFont(darkFont)
                .keyboardEnable(true)
                .setOnKeyboardListener(this::togoSoftKeyboard)
                .init();
    }

    /**
     * 软键盘弹起 / 收起。
     */
    default void togoSoftKeyboard(boolean open, int keyboardHeight) {
    }
}
