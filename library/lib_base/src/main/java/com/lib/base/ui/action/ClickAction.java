package com.lib.base.ui.action;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

/**
 * view/click
 *
 * @author : xwchen
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/09/15
 * desc   : 点击事件意图
 */
public interface ClickAction extends ContextAction {

    /**
     * findViewByIds
     *
     * @param id
     * @param <V>
     * @return
     */
    <V extends View> V findViewByIds(@IdRes int id);

    /**
     * 快速setOnClickListener
     *
     * @param ids
     */
    default void setOnClickListener(@IdRes int... ids) {
        setOnClickListener(this, ids);
    }

    /**
     * 快速setOnClickListener
     *
     * @param listener
     * @param ids
     */
    default void setOnClickListener(@Nullable View.OnClickListener listener, @IdRes int... ids) {
        for (int id : ids) {
            findViewByIds(id).setOnClickListener(listener);
        }
    }

    /**
     * 快速setOnClickListener
     *
     * @param views
     */
    default void setOnClickListener(View... views) {
        setOnClickListener(this, views);
    }

    /**
     * 快速setOnClickListener
     *
     * @param listener
     * @param views
     */
    default void setOnClickListener(@Nullable View.OnClickListener listener, View... views) {
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }

    /**
     * getViewById
     *
     * @param layoutId
     * @return
     */
    default View getViewById(int layoutId) {
        return LayoutInflater.from(getCurrentContext()).inflate(layoutId, null);
    }

}