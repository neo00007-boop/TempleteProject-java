package com.lib.base.ui.widget.animLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lib.base.R;
import com.lib.base.databinding.DynamicLayoutBinding;

/**
 * 动态添加/删除View(添加View时候以动画方式调整高度,使用场景很多)
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.ui.widget.dynamicLayout
 * @author      xwchen
 * Date         2021/12/31.
 */

public class AnimView extends FrameLayout {

    private static final int DURATION_ADD = 300;
    private static final int ANIM_IDEL = 0;
    private static final int ANIM_ING = 1;

    public int animType = ANIM_IDEL;
    private final DynamicLayoutBinding binding;

    public AnimView(Context context) {
        this(context, null);
    }

    public AnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = DynamicLayoutBinding.inflate(LayoutInflater.from(context), this, true);
        binding.llContainer.setLayoutTransition(null);
        binding.llContainer.setClipChildren(true);
        init(context);
    }

    private void init(Context context) {
        binding.tvAddItem.setOnClickListener(v -> addView(context));
    }

    @SuppressLint("InflateParams")
    private void addView(Context context) {
        if (animType == ANIM_ING) {
            return;
        }
        animType = ANIM_ING;
        View viewChild = LayoutInflater.from(context).inflate(R.layout.dynamic_item, binding.llContainer, false);
        viewChild.setOnClickListener(v -> removeItem(viewChild));
        int itemWidth = itemContentWidth();
        int itemHeight = measureItemHeight(viewChild, itemWidth);
        int fromWidth = binding.llContainer.getWidth();
        int fromHeight = binding.llContainer.getHeight();
        rememberEmptyWidth(fromWidth);
        binding.llContainer.addView(viewChild);
        lockSize(fromWidth, fromHeight);
        animateSize(fromWidth, Math.max(fromWidth, itemWidth), fromHeight, fromHeight + itemHeight, true, viewChild);
    }

    private void removeItem(View viewChild) {
        if (animType == ANIM_ING || viewChild.getParent() != binding.llContainer) {
            return;
        }
        animType = ANIM_ING;
        int fromWidth = binding.llContainer.getWidth();
        int fromHeight = binding.llContainer.getHeight();
        int toWidth = binding.llContainer.getChildCount() == 1 ? emptyWidth() : fromWidth;
        int toHeight = Math.max(0, fromHeight - viewChild.getHeight());
        lockSize(fromWidth, fromHeight);
        animateSize(fromWidth, toWidth, fromHeight, toHeight, false, viewChild);
    }

    private void animateSize(int fromWidth, int toWidth, int fromHeight, int toHeight, boolean isAdd, View viewChild) {
        LinearLayout container = binding.llContainer;
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURATION_ADD);
        animator.setInterpolator(isAdd ? new DecelerateInterpolator() : new AccelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            lp.width = fromWidth + Math.round((toWidth - fromWidth) * fraction);
            lp.height = fromHeight + Math.round((toHeight - fromHeight) * fraction);
            container.setLayoutParams(lp);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isAdd) {
                    container.removeView(viewChild);
                }
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (container.getChildCount() == 0) {
                    lp.width = emptyWidth();
                }
                container.setLayoutParams(lp);
                notifyData();
                animType = ANIM_IDEL;
            }
        });
        animator.start();
    }

    private void lockSize(int width, int height) {
        ViewGroup.LayoutParams lp = binding.llContainer.getLayoutParams();
        lp.width = width;
        lp.height = height;
        binding.llContainer.setLayoutParams(lp);
    }

    private int emptyWidth = -1;

    private void rememberEmptyWidth(int width) {
        if (emptyWidth < 0 && width > 0 && binding.llContainer.getChildCount() == 0) {
            emptyWidth = width;
        }
    }

    private int emptyWidth() {
        if (emptyWidth < 0) {
            emptyWidth = getResources().getDimensionPixelSize(R.dimen.x50);
        }
        return emptyWidth;
    }

    private int itemContentWidth() {
        int button = getResources().getDimensionPixelSize(R.dimen.x150);
        int margin = getResources().getDimensionPixelSize(R.dimen.x60);
        return button + margin * 2;
    }

    private int measureItemHeight(View viewChild, int width) {
        viewChild.measure(
                View.MeasureSpec.makeMeasureSpec(Math.max(width, 0), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        return viewChild.getMeasuredHeight();
    }

    @SuppressLint("SetTextI18n")
    private void notifyData() {
        for (int i = 0; i < binding.llContainer.getChildCount(); i++) {
            TextView tv = binding.llContainer.getChildAt(i).findViewById(R.id.tv);
            if (tv != null) {
                tv.setText("条目" + (i + 1));
            }
        }
    }
}
