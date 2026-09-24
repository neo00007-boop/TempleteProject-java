package com.lib.base.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lib.base.R;
import com.skydoves.androidveil.VeilLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 骨架屏容器：inflate 任意含 {@link VeilLayout} 的布局，自动收集并控制 veil/unVeil。
 * <p>
 * 未指定 {@code shimmerLayout}/{@code shimmerType} 时不预加载骨架，首次 {@link #show()} /
 * {@link #setType(int)} / {@link #setSkeletonLayout(int)} 再 inflate（便于嵌在 HolderView 里按需使用）。
 */
public class ShimmerView extends FrameLayout {

    public static final int TYPE_LIST = 0;
    public static final int TYPE_GRID = 1;

    private final List<VeilLayout> veilLayouts = new ArrayList<>();
    @LayoutRes
    private int layoutRes;
    private boolean autoStart = true;

    public ShimmerView(@NonNull Context context) {
        this(context, null);
    }

    public ShimmerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShimmerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerView);
        autoStart = a.getBoolean(R.styleable.ShimmerView_shimmerAutoStart, true);
        boolean hasLayout = a.hasValue(R.styleable.ShimmerView_shimmerLayout);
        boolean hasType = a.hasValue(R.styleable.ShimmerView_shimmerType);
        if (hasLayout) {
            layoutRes = a.getResourceId(R.styleable.ShimmerView_shimmerLayout, 0);
        } else if (hasType) {
            layoutRes = resolveTypeLayout(a.getInt(R.styleable.ShimmerView_shimmerType, TYPE_LIST));
        }
        a.recycle();
        if (layoutRes != 0) {
            rebuild();
        }
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    /** list / grid 内置骨架 */
    public void setType(int type) {
        setSkeletonLayout(resolveTypeLayout(type));
    }

    /** 自定义骨架布局（需包含 VeilLayout） */
    public void setSkeletonLayout(@LayoutRes int layoutRes) {
        if (layoutRes == 0) {
            return;
        }
        if (this.layoutRes == layoutRes && getChildCount() > 0) {
            if (getVisibility() == VISIBLE) {
                start();
            }
            return;
        }
        this.layoutRes = layoutRes;
        rebuild();
    }

    @LayoutRes
    public int getSkeletonLayout() {
        return layoutRes;
    }

    public void start() {
        ensureLayout();
        for (VeilLayout veil : veilLayouts) {
            veil.veil();
        }
    }

    public void stop() {
        for (VeilLayout veil : veilLayouts) {
            veil.unVeil();
        }
    }

    public void show() {
        ensureLayout();
        setVisibility(VISIBLE);
        start();
    }

    public void hide() {
        stop();
        setVisibility(GONE);
    }

    /** 未配置布局时默认 list 骨架，避免空 show */
    private void ensureLayout() {
        if (layoutRes == 0) {
            layoutRes = resolveTypeLayout(TYPE_LIST);
        }
        if (getChildCount() == 0) {
            rebuild();
        }
    }

    private void rebuild() {
        stop();
        removeAllViews();
        veilLayouts.clear();
        if (layoutRes == 0) {
            return;
        }
        LayoutInflater.from(getContext()).inflate(layoutRes, this, true);
        collectVeils(this);
        // 不在此处再调 start()，避免与 ensureLayout↔rebuild 互相递归；由 show()/setVisibility 驱动
        if (autoStart && getVisibility() == VISIBLE) {
            for (VeilLayout veil : veilLayouts) {
                veil.veil();
            }
        }
    }

    private void collectVeils(@NonNull View view) {
        if (view instanceof VeilLayout) {
            veilLayouts.add((VeilLayout) view);
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                collectVeils(group.getChildAt(i));
            }
        }
    }

    @LayoutRes
    private static int resolveTypeLayout(int type) {
        return type == TYPE_GRID ? R.layout.viewholder_shimmer_grid : R.layout.viewholder_shimmer_list;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != VISIBLE) {
            stop();
        } else if (autoStart) {
            ensureLayout();
            if (!veilLayouts.isEmpty()) {
                start();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }
}
