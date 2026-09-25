package com.lib.base.ui.pop;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.lib.base.R;
import com.lib.base.util.ContextUtil;
import com.lib.base.util.DebugUtil;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * 占满屏幕宽度的下拉菜单PopupWindow
 * <p>
 * PackageName  com.spot.ispot.view.widget
 * ProjectName  Spot1-26-yoga
 * @author      xwchen
 * Date         6/27/21.
 */
public class PopMenuView extends PopupWindow implements DefaultLifecycleObserver {
    public static final String TAG = "PopMenuView";
    private final View locationView;
    private LifecycleOwner lifecycleOwner;
    private boolean observed;

    public PopMenuView(View contentView, View locationView) {
        this(contentView, locationView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public PopMenuView(View contentView, View locationView, int width, int height) {
        super(contentView, width, height);
        this.locationView = locationView;
        setFocusable(true);
        setOutsideTouchable(false);
        setClippingEnabled(false);
        setAnimationStyle(R.style.draw_down_pw_style);
        //setBackgroundDrawable(new BitmapDrawable())；已过时
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //height 占满屏幕中locationView以下的剩余高度
        Rect rect = new Rect();
        locationView.getGlobalVisibleRect(rect);
        int h = locationView.getResources().getDisplayMetrics().heightPixels - rect.bottom;//屏幕高度获取可能会有异常,直接根据当前view整体布局来测量实际高度即可.
        setHeight(h);
        //life
        addObserver(contentView.getContext());
    }

    private void addObserver(Context context) {
        if (observed) {
            return;
        }
        if (lifecycleOwner == null) {
            lifecycleOwner = ContextUtil.getLifecycleOwnerByContext(context);
        }
        if (lifecycleOwner != null) {
            lifecycleOwner.getLifecycle().addObserver(this);
            observed = true;
        }
    }

    private void removeObserver() {
        if (lifecycleOwner != null && observed) {
            lifecycleOwner.getLifecycle().removeObserver(this);
            observed = false;
        }
    }

    public void show() {
        addObserver(locationView.getContext());
        showAsDropDown(locationView);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        removeObserver();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DebugUtil.logD(TAG, "pop onDestroy");
        if (isShowing()) {
            dismiss();
        } else {
            removeObserver();
        }
    }
}