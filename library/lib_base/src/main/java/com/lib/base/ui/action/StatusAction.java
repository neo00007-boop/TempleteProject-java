package com.lib.base.ui.action;

import com.lib.base.ui.widget.HolderView;

/**
 * 处理加载中/无数据/网络异常等状态,基于{@link HolderView}
 * <p>
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.ui.action
 *
 * @author xwchen
 * Date         2022/1/27.
 */

public interface StatusAction {
    /**
     * 绑定HolderView
     *
     * @return
     */
    default HolderView getHolderView() {
        return null;
    }

    /**
     * 空数据 / 错误态点击重试。默认空实现，列表基类里会走 {@code freshData()}。
     */
    default void onHolderRetryClick() {
    }

    /**
     * loading
     */
    default void showLoadingView() {
        HolderView holderView = getHolderView();
        if (holderView != null) {
            holderView.showLoadingView();
        }
    }

    /**
     * error view
     */
    default void showErrorView() {
        HolderView holderView = getHolderView();
        if (holderView != null) {
            holderView.setOnRetryClickListener(v -> onHolderRetryClick());
            holderView.showErrorView();
        }
    }

    /**
     * no data
     */
    default void showNoDataView() {
        HolderView holderView = getHolderView();
        if (holderView != null) {
            holderView.setOnRetryClickListener(v -> onHolderRetryClick());
            holderView.showNoDataView();
        }
    }

    /**
     * cancelFresh
     *
     * @param cancelFresh
     */
    default void cancelFresh(boolean cancelFresh) {
        HolderView holderView = getHolderView();
        if (holderView != null) {
            holderView.cancelFresh(cancelFresh);
        }
    }

    /**
     * hide loading View
     */
    default void hideView() {
        HolderView holderView = getHolderView();
        if (holderView != null) {
            holderView.hideView();
        }
    }
}
