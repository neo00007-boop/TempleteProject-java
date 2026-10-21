package com.lib.base.ui.action;

import android.content.Context;
import android.text.TextUtils;

import com.lib.base.bean.BtnBean;
import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.ui.widget.TitleBar;

/**
 * 处理 {@link TitleBar}：主题、返回、标题、右侧按钮。
 * <p>
 * 状态栏图标深浅默认跟随 {@link #getTitleTheme()}，无需再手写蓝/白两套。
 */
public interface TitleBarAction extends ImmersionAction {

    TitleBar getTitleBar();

    /**
     * 标题栏主题，默认蓝底。无标题栏返回 {@link TitleBarTheme#THEME_NONE}。
     */
    default int getTitleTheme() {
        return TitleBarTheme.THEME_BLUE;
    }

    /**
     * 状态栏图标是否深色：跟标题栏主题走（白底深色 / 蓝底浅色）。
     */
    @Override
    default boolean darkStatusBarFont() {
        return TitleBarTheme.darkStatusBarFont(getTitleTheme());
    }

    default void initTitleBar(Context context, int theme) {
        TitleBar titleBar = getTitleBar();
        titleBar.initView(context, theme);
        titleBar.setBackClickListener(v -> {
            actionTitleBackClick(false);
            if (!backClickIntercept()) {
                actionFinish();
            }
        });
    }

    /**
     * @param keyBack true 系统返回键，false 标题栏返回
     * @return true 消费事件（仅对返回键有意义）
     */
    default boolean actionTitleBackClick(boolean keyBack) {
        return false;
    }

    void actionFinish();

    default boolean backClickIntercept() {
        return false;
    }

    default void setBackStr(String str) {
        hasTitle();
        getTitleBar().setBackStr(str);
    }

    default void hasTitle() {
        if (getTitleBar() == null) {
            throw new RuntimeException("no title");
        }
    }

    default void setLeftStrWithNoArrow(String strBack) {
        hasTitle();
        getTitleBar().setLeftWithNoArrow(strBack);
    }

    default void hideBackView() {
        hasTitle();
        getTitleBar().hideBackView();
    }

    default void setTitleStr(String titleName) {
        hasTitle();
        if (!TextUtils.isEmpty(titleName)) {
            getTitleBar().setTitleName(titleName, false);
        }
    }

    default void setTitleStrWithBold(String titleName) {
        hasTitle();
        if (!TextUtils.isEmpty(titleName)) {
            getTitleBar().setTitleName(titleName, true);
        }
    }

    /**
     * 右侧按钮：≤2 直接展示，&gt;2 收进 Pop。
     */
    default void setRightClickViews(TitleBar.OnRightViewsClickListener onRightClickViewsListener,
                                    boolean hasSelect, BtnBean... rightClickViews) {
        hasTitle();
        if (null != rightClickViews && rightClickViews.length > 0 && null != onRightClickViewsListener) {
            getTitleBar().setRightClickViews(onRightClickViewsListener, hasSelect, rightClickViews);
        }
    }

    default void clearRightBtns() {
        hasTitle();
        getTitleBar().clearRightBtns();
    }

    /**
     * 标题栏铺背景图：透明状态栏 + 内部 StatuesBar 占位。
     */
    default void setTitleBg(int backGroundResource) {
        hasTitle();
        specialImmersion();
        getTitleBar().setTitleBGResource(backGroundResource);
    }
}
