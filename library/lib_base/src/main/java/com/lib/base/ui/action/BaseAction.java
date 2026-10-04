package com.lib.base.ui.action;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

/**
 * context工具类
 *
 * @author : xwchen
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/09/15
 * desc   : 根据Context处理Resources
 */
public interface BaseAction extends ContextAction {

    /**
     * LayoutInflater
     *
     * @return
     */
    default LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(getCurrentContext());
    }

    /**
     * Resources
     *
     * @return
     */
    default Resources getResourcess() {
        return getCurrentContext().getResources();
    }

    /**
     * getDimen
     *
     * @param id
     * @return
     */
    default float getDimen(int id) {
        return getResourcess().getDimension(id);
    }

    /**
     * getString
     *
     * @param id
     * @return
     */
    default String getString(@StringRes int id) {
        return getCurrentContext().getString(id);
    }

    /**
     * getString
     *
     * @param id
     * @param formatArgs
     * @return
     */
    default String getString(@StringRes int id, Object... formatArgs) {
        return getResourcess().getString(id, formatArgs);
    }

    /**
     * getDrawable
     *
     * @param id
     * @return
     */
    default Drawable getDrawable(@DrawableRes int id) {
        return ContextCompat.getDrawable(getCurrentContext(), id);
    }

    /**
     * getColor
     *
     * @param id
     * @return
     */
    @ColorInt
    default int getColor(@ColorRes int id) {
        return ContextCompat.getColor(getCurrentContext(), id);
    }

    /**
     * getSystemService
     *
     * @param serviceClass
     * @param <S>
     * @return
     */
    default <S> S getSystemService(@NonNull Class<S> serviceClass) {
        return ContextCompat.getSystemService(getCurrentContext(), serviceClass);
    }

}