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
 * time : 2019/08/23
 * optimization: 2026 健壮性优化版本
 */
public final class StatusBar extends View { // 1. 修正拼写：StatuesBar -> StatusBar

    private int mInsetTop;

    public StatusBar(Context context) {
        this(context, null);
    }

    public StatusBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化时，先用系统默认的 dimen 保底，防止首次测量前变量为 0
        mInsetTop = getStatusBarSizeFromDimen();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 2. 尝试从当前 Window 获取最新的 Insets 并更新
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowInsets insets = getRootWindowInsets();
            if (insets != null) {
                applyInsets(insets);
            }
        }
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        applyInsets(insets);
        // 保持向下分发 Insets，不要在这里消费掉它，以免影响兄弟 View
        return super.onApplyWindowInsets(insets);
    }

    private void applyInsets(WindowInsets insets) {
        int top = resolveStatusBarHeight(insets);
        // 如果计算出的高度大于 0 且与当前高度不一致，则更新并重新布局
        if (top > 0 && top != mInsetTop) {
            mInsetTop = top;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 3. 极简原则：测量高度严格遵循 mInsetTop，不在 Measure 期间做复杂的 Insets 运算
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mInsetTop, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int resolveStatusBarHeight(WindowInsets insets) {
        if (insets != null) {
            // Android 11 (API 30) 及以上推荐做法
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                int top = insets.getInsets(WindowInsets.Type.statusBars()).top;
                if (top > 0) {
                    return top;
                }
            }
            // Android 10 及以下老版本做法
            int top = insets.getSystemWindowInsetTop();
            if (top > 0) {
                return top;
            }
        }
        return getStatusBarSizeFromDimen();
    }

    /**
     * 从系统资源反射/获取默认状态栏高度（兜底方案）
     */
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
