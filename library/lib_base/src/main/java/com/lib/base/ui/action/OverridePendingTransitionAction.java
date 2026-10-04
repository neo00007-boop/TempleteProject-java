package com.lib.base.ui.action;

import android.app.Activity;

import com.lib.base.R;

/**
 * 常规转场动画,包括常规跳转转场/login上拉动画/finish转场/
 * PackageName  com.lib.base.ui.action
 * ProjectName  TempleteProject-java
 * Date         2022/1/28.
 *
 * @author xwchen
 */

public interface OverridePendingTransitionAction extends ContextAction {
    int ANIM_NO = -1;
    int ANIM_NORMAL = 0;
    int ANIM_LOGIN = 1;

    /**
     * 默认不拦截finish转场动画
     *
     * @return
     */
    default int startAnimType() {
        return ANIM_NORMAL;
    }

    /**
     * 默认不拦截finish转场动画
     *
     * @return
     */
    default int finishAnimType() {
        return ANIM_NORMAL;
    }

    /**
     * 展示finish转场动画
     */
    default void finishAnim() {
        int type = finishAnimType();
        if (type == ANIM_LOGIN) {
            overridePendingTransitionFinishLogin();
        } else if (type == ANIM_NORMAL) {
            overridePendingTransitionFinishNormal();
        } else if (type == ANIM_NO) {
            //else no anim
            //overridePendingTransitionFinishNone();
        }
    }

    /**
     * 常规跳转activity转场动画
     */
    default void overridePendingTransitionStartNormal() {
        Activity activity = getCurrentActivity();
        activity.overridePendingTransition(R.anim.trans_pre_in, R.anim.trans_pre_out);
    }

    /**
     * 无跳转activity转场动画
     */
    default void overridePendingTransitionStartNone() {
        Activity activity = getCurrentActivity();
        activity.overridePendingTransition(0, 0);
    }

    /**
     * 常规finish activity转场动画
     */
    default void overridePendingTransitionFinishNormal() {
        Activity activity = getCurrentActivity();
        activity.overridePendingTransition(R.anim.trans_pre_in_back, R.anim.trans_pre_out_back);
    }

    /**
     * 无转场动画
     */
    default void overridePendingTransitionFinishNone() {
        Activity activity = getCurrentActivity();
        activity.overridePendingTransition(0, 0);
    }

    /**
     * 登录跳转activity转场动画
     */
    default void overridePendingTransitionStartLogin() {
        Activity activity = getCurrentActivity();
        activity.overridePendingTransition(R.anim.bottom_in_anim, R.anim.no_anim);
    }

    /**
     * 登录finish activity转场动画
     */
    default void overridePendingTransitionFinishLogin() {
        Activity activity = getCurrentActivity();
        activity.overridePendingTransition(R.anim.no_anim, R.anim.bottom_out_anim);
    }
}
