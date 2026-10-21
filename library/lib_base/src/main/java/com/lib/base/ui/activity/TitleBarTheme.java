package com.lib.base.ui.activity;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;

import com.lib.base.R;

/**
 * 标题栏双色主题：蓝底 / 白底 / 无标题栏。
 * <p>
 * 状态栏颜色、图标深浅、布局资源统一从这里取，避免 BaseActivity / TitleBar / XML 各写一份。
 */
public final class TitleBarTheme {

    /** 不使用 BaseActivity 包裹的标题栏（内容区自管 TitleBar / 沉浸式） */
    public static final int THEME_NONE = -1;
    /** 蓝底白字 */
    public static final int THEME_BLUE = 0;
    /** 白底深色字 */
    public static final int THEME_WHITE = 1;

    private TitleBarTheme() {
    }

    public static boolean isNone(int theme) {
        return theme == THEME_NONE;
    }

    public static boolean isWhite(int theme) {
        return theme == THEME_WHITE;
    }

    public static boolean isBlue(int theme) {
        return theme == THEME_BLUE;
    }

    /**
     * 与标题栏背景一致的状态栏颜色（THEME_NONE 返回透明色）。
     */
    @ColorRes
    public static int statusBarColorRes(int theme) {
        if (theme == THEME_WHITE) {
            return R.color.cl_white;
        }
        if (theme == THEME_BLUE) {
            return R.color.common_accent_color;
        }
        return R.color.cl_no_color;
    }

    /**
     * 状态栏图标是否用深色：白底用深色，蓝底用浅色。
     */
    public static boolean darkStatusBarFont(int theme) {
        return theme == THEME_WHITE || theme == THEME_NONE;
    }

    /**
     * 标题栏布局。THEME_NONE 无布局，调用方勿使用。
     */
    @LayoutRes
    public static int titleLayoutRes(int theme) {
        return theme == THEME_WHITE ? R.layout.white_title_layout : R.layout.blue_title_layout;
    }

    /**
     * 右侧文字按钮颜色。
     */
    @ColorRes
    public static int rightTextColorRes(int theme) {
        return theme == THEME_WHITE ? R.color.cl_333333 : R.color.cl_white;
    }
}
