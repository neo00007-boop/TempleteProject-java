package com.lib.base.ui.activity;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.appbar.AppBarLayout;
import com.lib.base.ui.behavior.LockableAppBarLayoutBehavior;

/**
 * 折叠布局 Activity 基类（Coordinator + AppBar）。
 * <p>
 * 方法1 {@link #setCollapsingScrollEnabled(boolean)}：禁用后头部不再跟手折叠，列表仍可滚；
 * 也可 {@link #lockCollapsingHeaderExpanded(boolean)} / {@link #lockCollapsingHeaderCollapsed(boolean)}
 * 先展开或折叠再锁头。<br>
 * 方法2 {@link #expandCollapsingHeader(boolean)} / {@link #collapseCollapsingHeader(boolean)}：
 * 在允许滚动时一键展开 / 折叠（已锁头时调用无效）。<br>
 * 方法3 {@link #getCollapsingHeaderState()}：判断当前是展开、收起还是过渡态。
 */
public abstract class BaseCollapsingActivity<T extends ViewBinding> extends BaseActivity<T> {

    /** 折叠头当前状态 */
    public enum CollapsingHeaderState {
        /** 完全展开 */
        EXPANDED,
        /** 完全收起 */
        COLLAPSED,
        /** 展开与收起之间的过渡 */
        SETTLING
    }

    @Nullable
    private AppBarLayout collapsingAppBar;
    @Nullable
    private LockableAppBarLayoutBehavior collapsingBehavior;
    @Nullable
    private AppBarLayout.OnOffsetChangedListener pendingLockListener;

    /**
     * 绑定页面里的 {@link AppBarLayout}，并挂上可锁定 Behavior。
     * 子类在 {@link #initView()} 拿到 AppBar 后调用一次即可。
     */
    protected void bindCollapsingAppBar(@NonNull AppBarLayout appBar) {
        clearPendingLockListener();
        collapsingAppBar = appBar;
        collapsingBehavior = ensureLockableBehavior(appBar);
    }

    @Nullable
    protected AppBarLayout getCollapsingAppBar() {
        return collapsingAppBar;
    }

    @Nullable
    protected LockableAppBarLayoutBehavior getCollapsingBehavior() {
        return collapsingBehavior;
    }

    /**
     * 方法1：是否允许头部跟手 / 嵌套滚动折叠。
     * {@code false} 时头部锁死在<strong>当前</strong>展开比例，列表仍可滚动。
     */
    public void setCollapsingScrollEnabled(boolean enabled) {
        if (collapsingBehavior == null) {
            return;
        }
        if (enabled) {
            clearPendingLockListener();
        }
        collapsingBehavior.setScrollEnabled(enabled);
    }

    public boolean isCollapsingScrollEnabled() {
        return collapsingBehavior == null || collapsingBehavior.isScrollEnabled();
    }

    /** 先展开到顶，再锁头（列表仍可滚）。 */
    public void lockCollapsingHeaderExpanded(boolean animate) {
        lockCollapsingHeader(true, animate);
    }

    /** 先折叠收起，再锁头（列表仍可滚）。 */
    public void lockCollapsingHeaderCollapsed(boolean animate) {
        lockCollapsingHeader(false, animate);
    }

    /** 解锁头部滚动。 */
    public void unlockCollapsingHeader() {
        setCollapsingScrollEnabled(true);
    }

    /**
     * 方法2：一键展开头部。仅在允许滚动时生效。
     */
    public void expandCollapsingHeader(boolean animate) {
        setCollapsingExpanded(true, animate);
    }

    /**
     * 方法2：一键折叠头部。仅在允许滚动时生效。
     */
    public void collapseCollapsingHeader(boolean animate) {
        setCollapsingExpanded(false, animate);
    }

    /** 展开 / 折叠切换。仅在允许滚动时生效。 */
    public void toggleCollapsingHeader(boolean animate) {
        setCollapsingExpanded(!isCollapsingExpanded(), animate);
    }

    public boolean isCollapsingExpanded() {
        return getCollapsingHeaderState() == CollapsingHeaderState.EXPANDED;
    }

    public boolean isCollapsingCollapsed() {
        return getCollapsingHeaderState() == CollapsingHeaderState.COLLAPSED;
    }

    /**
     * 方法3：当前折叠头状态。
     * <ul>
     *   <li>{@link CollapsingHeaderState#EXPANDED}：完全展开</li>
     *   <li>{@link CollapsingHeaderState#COLLAPSED}：完全收起</li>
     *   <li>{@link CollapsingHeaderState#SETTLING}：中间过渡态</li>
     * </ul>
     */
    @NonNull
    public CollapsingHeaderState getCollapsingHeaderState() {
        if (collapsingAppBar == null) {
            return CollapsingHeaderState.EXPANDED;
        }
        int range = collapsingAppBar.getTotalScrollRange();
        if (range <= 0) {
            return CollapsingHeaderState.EXPANDED;
        }
        int abs = Math.abs(collapsingAppBar.getTop());
        if (abs <= 1) {
            return CollapsingHeaderState.EXPANDED;
        }
        if (abs >= range - 1) {
            return CollapsingHeaderState.COLLAPSED;
        }
        return CollapsingHeaderState.SETTLING;
    }

    /** 折叠进度 0=展开 … 1=收起；无 AppBar 时返回 0。 */
    public float getCollapsingHeaderProgress() {
        if (collapsingAppBar == null) {
            return 0f;
        }
        int range = collapsingAppBar.getTotalScrollRange();
        if (range <= 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, Math.abs(collapsingAppBar.getTop()) / (float) range));
    }

    /**
     * 先落到展开或折叠态，再禁用头部滚动。
     * 动画过程中临时允许偏移，到位后锁死。
     */
    private void lockCollapsingHeader(boolean expanded, boolean animate) {
        if (collapsingAppBar == null || collapsingBehavior == null) {
            return;
        }
        clearPendingLockListener();
        if (!animate) {
            collapsingAppBar.setExpanded(expanded, false);
            collapsingBehavior.setScrollEnabled(false);
            return;
        }
        if ((expanded && isCollapsingExpanded()) || (!expanded && isCollapsingCollapsed())) {
            collapsingBehavior.setScrollEnabled(false);
            return;
        }
        // 动画需要 Behavior 能改 offset；先解锁，到位后再锁
        collapsingBehavior.setScrollEnabled(true);
        final AppBarLayout appBar = collapsingAppBar;
        final LockableAppBarLayoutBehavior behavior = collapsingBehavior;
        pendingLockListener = (abl, offset) -> {
            int range = abl.getTotalScrollRange();
            boolean settled = expanded
                    ? offset >= 0
                    : range <= 0 || Math.abs(offset) >= range - 1;
            if (!settled) {
                return;
            }
            abl.removeOnOffsetChangedListener(pendingLockListener);
            pendingLockListener = null;
            behavior.setScrollEnabled(false);
        };
        appBar.addOnOffsetChangedListener(pendingLockListener);
        appBar.setExpanded(expanded, true);
    }

    private void setCollapsingExpanded(boolean expanded, boolean animate) {
        if (collapsingAppBar == null || collapsingBehavior == null) {
            return;
        }
        // 方法2：禁用滚动时不响应一键折叠 / 展开
        if (!collapsingBehavior.isScrollEnabled()) {
            return;
        }
        collapsingAppBar.setExpanded(expanded, animate);
    }

    private void clearPendingLockListener() {
        if (collapsingAppBar != null && pendingLockListener != null) {
            collapsingAppBar.removeOnOffsetChangedListener(pendingLockListener);
        }
        pendingLockListener = null;
    }

    @Override
    public void onDestroy() {
        clearPendingLockListener();
        super.onDestroy();
    }

    @NonNull
    private static LockableAppBarLayoutBehavior ensureLockableBehavior(@NonNull AppBarLayout appBar) {
        ViewGroup.LayoutParams lp = appBar.getLayoutParams();
        if (!(lp instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalStateException("AppBarLayout must be a child of CoordinatorLayout");
        }
        CoordinatorLayout.LayoutParams clp = (CoordinatorLayout.LayoutParams) lp;
        CoordinatorLayout.Behavior<?> existing = clp.getBehavior();
        if (existing instanceof LockableAppBarLayoutBehavior) {
            return (LockableAppBarLayoutBehavior) existing;
        }
        LockableAppBarLayoutBehavior behavior = new LockableAppBarLayoutBehavior();
        clp.setBehavior(behavior);
        appBar.setLayoutParams(clp);
        return behavior;
    }
}
