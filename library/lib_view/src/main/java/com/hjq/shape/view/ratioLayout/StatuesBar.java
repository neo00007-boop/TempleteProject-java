package com.hjq.shape.view.ratioLayout;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;


/**
 * 顶部状态栏占位图，高度取真实 WindowInsets，拿不到时再回退 dimen。
 * <p>
 * @author: Android xwchen
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/08/23
 */
public final class StatuesBar extends View {

    private int mInsetTop;

    public StatuesBar(Context context) {
        this(context, null);
    }

    public StatuesBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatuesBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInsetTop = resolveStatusBarHeight(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        WindowInsets insets = getRootWindowInsets();
        if (insets != null) {
            applyInsets(insets);
        }
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        applyInsets(insets);
        return super.onApplyWindowInsets(insets);
    }

    private void applyInsets(WindowInsets insets) {
        int top = resolveStatusBarHeight(insets);
        if (top != mInsetTop) {
            mInsetTop = top;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mInsetTop > 0 ? mInsetTop : resolveStatusBarHeight(getRootWindowInsets());
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int resolveStatusBarHeight(WindowInsets insets) {
        if (insets != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                int top = insets.getInsets(WindowInsets.Type.statusBars()).top;
                if (top > 0) {
                    return top;
                }
            }
            int top = insets.getSystemWindowInsetTop();
            if (top > 0) {
                return top;
            }
        }
        return getStatusBarSizeFromDimen();
    }

    private int getStatusBarSizeFromDimen() {
        int statusBarHeight = 0;
        Resources resources = getContext().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}
