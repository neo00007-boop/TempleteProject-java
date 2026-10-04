package com.lib.base.ui.action;

import android.content.Context;
import android.text.TextUtils;

import com.lib.base.bean.BtnBean;
import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.ui.widget.TitleBar;

/**
 * 处理TitleBar{@link TitleBar}
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.ui.action
 *
 * @author xwchen
 * Date         2022/1/27.
 */

public interface TitleBarAction extends ImmersionAvtion {
    /**
     * 获取TitleBar,结果可能为空
     *
     * @return
     */
    TitleBar getTitleBar();

    /**
     * 获取TitleBar主题
     *
     * @return
     */
    default int getTitleTheme() {
        return TitleBarTheme.THEME_BLUE;
    }

    /**
     * 初始化TitleBar
     *
     * @param context
     * @param theme
     */
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
     * 返回键或标题栏返回按钮事件
     *
     * @param keyBack true返回键返回,false标题返回
     * @return 注意return true时候, 如果是返回键返回会消费返回事件;标题返回无影响
     */
    default boolean actionTitleBackClick(boolean keyBack) {
        return false;
    }

    /**
     * 关闭activity事件
     */
    void actionFinish();

    /**
     * 默认不拦截标题栏返回事件
     *
     * @return
     */
    default boolean backClickIntercept() {
        return false;
    }

    /**
     * 设置返回文字
     *
     * @param str
     */
    default void setBackStr(String str) {
        hasTitle();
        getTitleBar().setBackStr(str);
    }

    /**
     * 是否使用了TitleBar
     */
    default void hasTitle() {
        if (getTitleBar() == null) {
            throw new RuntimeException("no title");
        }
    }

    /**
     * 隐藏箭头
     *
     * @param strBack 返回str
     */
    default void setLeftStrWithNoArrow(String strBack) {
        hasTitle();
        getTitleBar().setLeftWithNoArrow(strBack);
    }

    /**
     * 隐藏返回按钮
     */
    default void hideBackView() {
        hasTitle();
        getTitleBar().hideBackView();
    }

    /**
     * 设置标题名字
     *
     * @param titleName 标题str
     */
    default void setTitleStr(String titleName) {
        hasTitle();
        if (!TextUtils.isEmpty(titleName)) {
            getTitleBar().setTitleName(titleName, false);
        }
    }

    /**
     * 设置标题名字+加粗
     *
     * @param titleName 标题str
     */
    default void setTitleStrWithBold(String titleName) {
        hasTitle();
        if (!TextUtils.isEmpty(titleName)) {
            getTitleBar().setTitleName(titleName, true);
        }
    }

    /**
     * 标题栏右侧添加点击view和点击事件回调:
     * 1.两个以下时直接添加按钮;
     * 2.超过两个时直接添加PopView.
     *
     * @param onRightClickViewsListener 事件监听
     * @param hasSelect                 勾选
     * @param rightClickViews           资源,可以传R.drawble.xxx,string,view
     */
    default void setRightClickViews(TitleBar.OnRightViewsClickListener onRightClickViewsListener, boolean hasSelect, BtnBean... rightClickViews) {
        hasTitle();
        if (null != rightClickViews && rightClickViews.length > 0 && null != onRightClickViewsListener) {
            getTitleBar().setRightClickViews(onRightClickViewsListener, hasSelect, rightClickViews);
        }
    }

    /**
     * 清除右上角按钮
     */
    default void clearRightBtns() {
        hasTitle();
        getTitleBar().clearRightBtns();
    }

    /**
     * 标题栏设置背景图
     *
     * @param backGroundResource
     */
    default void setTitleBg(int backGroundResource) {
        hasTitle();
        specialImmersion();
        getTitleBar().setTitleBGResource(backGroundResource);
    }
}
