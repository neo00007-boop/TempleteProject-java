package com.lib.base.ui.action;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * PackageName  com.lib.base.ui.action
 * ProjectName  TempleteProject-java
 * Date         2022/1/28.
 *
 * @author xwchen
 */

public interface ContextAction extends View.OnClickListener {
    /**
     * 获取当前Context
     *
     * @return
     */
    @NonNull
    Context getCurrentContext();

    /**
     * 获取当前Activity
     *
     * @return
     */
    @NonNull
    Activity getCurrentActivity();

    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    default void onClick(View v) {
    }
}
