package com.lib.base.ui.action;

import com.gyf.immersionbar.ImmersionBar;
import com.lib.base.R;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.ui.action
 * @author      xwchen
 * Date         2022/1/27.
 */

public interface ImmersionAvtion extends ContextAction {

    /**
     * 默认标题栏字体黑色
     *
     * @return
     */
    default boolean darkStatusBarFont() {
        return true;
    }

    /**
     * 默认无色,ContentView直接占用状态栏位置
     */
    default void setImmersionBar() {
        setImmersionBar(R.color.cl_no_color, false);
    }

    /**
     * 设置颜色后直接,ContentView从状态栏下方开始布局
     *
     * @param statusBarColor 状态栏颜色
     */
    default void setImmersionBar(int statusBarColor) {
        setImmersionBar(statusBarColor, true);
    }

    /**
     * 设置了autoDarkModeEnable后setStatueBarTextColorDark会失效
     *
     * @param statusBarColor    状态栏颜色
     * @param fitsSystemWindows 是否从状态栏下方开始布局
     */
    default void setImmersionBar(int statusBarColor, boolean fitsSystemWindows) {
        ImmersionBar
                .with(getCurrentActivity())
                .statusBarColor(statusBarColor)
                .fitsSystemWindows(fitsSystemWindows)
                .statusBarDarkFont(darkStatusBarFont())
                //.navigationBarColor(R.color.white)//可自定义navigationBar颜色
                /*.autoDarkModeEnable(true, 0.2f)*/
                //软键盘监听回调，keyboardEnable为true才会回调此方法
                //open为true，软键盘弹出，为false，软键盘关闭
                .keyboardEnable(true)
                .setOnKeyboardListener(this::togoSoftKeyboard)
                .init();
    }

    /**
     * 监听键盘状态:弹起,收回
     *
     * @param open
     * @param keyboardHeight
     */
    default void togoSoftKeyboard(boolean open, int keyboardHeight) {

    }

    /**
     * 只设置状态栏字体是否是黑色,其他不管
     *
     * @param dark 是否黑色
     */
    default void setStatueBarTextColorDark(boolean dark) {
        ImmersionBar
                .with(getCurrentActivity())
                .statusBarDarkFont(dark)
                //软键盘监听回调，keyboardEnable为true才会回调此方法
                //open为true，软键盘弹出，为false，软键盘关闭
                .keyboardEnable(true)
                .setOnKeyboardListener(this::togoSoftKeyboard)
                .init();
    }

    /**
     * 设置背景后单独处理状态栏属性
     */
    default void specialImmersion() {
        ImmersionBar
                .with(getCurrentActivity())
                .statusBarColor(R.color.cl_no_color)
                .fitsSystemWindows(false)
                .statusBarDarkFont(false)
                //软键盘监听回调，keyboardEnable为true才会回调此方法
                //open为true，软键盘弹出，为false，软键盘关闭
                .keyboardEnable(true)
                .setOnKeyboardListener(this::togoSoftKeyboard)
                .init();
    }
}
