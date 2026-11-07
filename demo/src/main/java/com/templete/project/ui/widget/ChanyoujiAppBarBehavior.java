package com.templete.project.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.templete.project.R;

/**
 * 不拦截顶图触摸（交给 Activity 转发给列表，保证和列表同一套路）。
 * 仅处理 NestedScroll 未消费下拉：translationY + 封面 scale，松手回弹。
 */
public class ChanyoujiAppBarBehavior extends AppBarLayout.Behavior {

    private static final float PULL_DAMP = 0.45f;
    private static final float MAX_RATIO = 0.55f;

    private int baseHeight;
    private float pullExtra;
    private ValueAnimator anim;

    public ChanyoujiAppBarBehavior() {
    }

    public ChanyoujiAppBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBaseHeight(int px) {
        if (px > 0) {
            baseHeight = px;
        }
    }

    public float getPullExtra() {
        return pullExtra;
    }

    /** 禁止 Behavior 自己抢顶图拖拽，避免和列表两套路 */
    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent,
                                         @NonNull AppBarLayout child,
                                         @NonNull MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent,
                                @NonNull AppBarLayout child,
                                @NonNull MotionEvent ev) {
        return false;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                  @NonNull AppBarLayout child, @NonNull View target,
                                  int dx, int dy, @NonNull int[] consumed, int type) {
        if (pullExtra > 0f && dy > 0) {
            cancelAnim();
            float before = pullExtra;
            pullExtra = Math.max(0f, pullExtra - dy);
            applyVisual(child, coordinatorLayout);
            int used = Math.round(before - pullExtra);
            if (used > 0) {
                consumed[1] += used;
            }
            int left = dy - used;
            if (left > 0) {
                super.onNestedPreScroll(coordinatorLayout, child, target, dx, left, consumed, type);
            }
            return;
        }
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull AppBarLayout child, @NonNull View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                               int type, @NonNull int[] consumed) {
        super.onNestedScroll(coordinatorLayout, child, target,
                dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);
        if (type != ViewCompat.TYPE_TOUCH) {
            return;
        }
        if (getTopAndBottomOffset() >= 0 && dyUnconsumed < 0) {
            cancelAnim();
            ensureBase(child);
            float max = baseHeight * MAX_RATIO;
            pullExtra = Math.min(max, pullExtra + (-dyUnconsumed) * PULL_DAMP);
            applyVisual(child, coordinatorLayout);
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                   @NonNull AppBarLayout abl, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, abl, target, type);
        if (type == ViewCompat.TYPE_TOUCH && pullExtra > 0f) {
            bounce(abl, coordinatorLayout);
        }
    }

    /** 强制清掉下拉位移，防止残留把界面「炸」住 */
    public void resetPull(@NonNull AppBarLayout appBar, @NonNull ViewGroup parent) {
        cancelAnim();
        pullExtra = 0f;
        applyVisual(appBar, parent);
    }

    private void applyVisual(AppBarLayout appBar, ViewGroup parent) {
        ensureBase(appBar);
        // 列表整体下移；封面反向顶回屏幕顶部并放大铺满，避免顶部露白
        appBar.setTranslationY(pullExtra);
        RecyclerView rv = findRv(parent);
        if (rv != null) {
            rv.setTranslationY(pullExtra);
        }
        ImageView cover = appBar.findViewById(R.id.ivCover);
        if (cover != null) {
            if (cover.getWidth() > 0) {
                cover.setPivotX(cover.getWidth() / 2f);
            }
            cover.setPivotY(0f);
            // AppBar 下移了 pullExtra，封面先顶回去，再按总高放大铺满
            cover.setTranslationY(-pullExtra);
            float scale = (baseHeight + pullExtra) / Math.max(1f, (float) baseHeight);
            cover.setScaleX(scale);
            cover.setScaleY(scale);
        }
    }

    private RecyclerView findRv(ViewGroup parent) {
        if (parent == null) {
            return null;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            View c = parent.getChildAt(i);
            if (c instanceof RecyclerView) {
                return (RecyclerView) c;
            }
        }
        return null;
    }

    private void ensureBase(AppBarLayout appBar) {
        if (baseHeight > 0) {
            return;
        }
        baseHeight = Math.round(280 * appBar.getResources().getDisplayMetrics().density);
    }

    private void bounce(AppBarLayout appBar, ViewGroup parent) {
        cancelAnim();
        if (pullExtra <= 0f) {
            applyVisual(appBar, parent);
            return;
        }
        final float start = pullExtra;
        anim = ValueAnimator.ofFloat(start, 0f);
        anim.setDuration(280);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            pullExtra = (float) a.getAnimatedValue();
            applyVisual(appBar, parent);
        });
        anim.start();
    }

    private void cancelAnim() {
        if (anim != null) {
            anim.cancel();
            anim = null;
        }
    }
}
