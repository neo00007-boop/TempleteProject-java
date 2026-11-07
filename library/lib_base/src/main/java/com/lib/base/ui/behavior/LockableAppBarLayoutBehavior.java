package com.lib.base.ui.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

/**
 * 可锁定的 AppBar Behavior：关闭后头部不再跟手/嵌套滚动折叠，列表现滚动不受影响。
 */
public class LockableAppBarLayoutBehavior extends AppBarLayout.Behavior {

    private boolean scrollEnabled = true;

    public LockableAppBarLayoutBehavior() {
        initDragLock();
    }

    public LockableAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDragLock();
    }

    private void initDragLock() {
        setDragCallback(new DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return scrollEnabled;
            }
        });
    }

    public void setScrollEnabled(boolean enabled) {
        scrollEnabled = enabled;
    }

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout parent,
                                       @NonNull AppBarLayout child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int nestedScrollAxes,
                                       int type) {
        return scrollEnabled
                && super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type);
    }

    /**
     * 锁头后若仍有进行中的嵌套滚动，吞掉后续位移，避免半锁半滚。
     */
    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                  @NonNull AppBarLayout child,
                                  @NonNull View target,
                                  int dx, int dy, @NonNull int[] consumed, int type) {
        if (!scrollEnabled) {
            return;
        }
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull AppBarLayout child,
                               @NonNull View target,
                               int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed,
                               int type, @NonNull int[] consumed) {
        if (!scrollEnabled) {
            return;
        }
        super.onNestedScroll(coordinatorLayout, child, target,
                dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);
    }
}
