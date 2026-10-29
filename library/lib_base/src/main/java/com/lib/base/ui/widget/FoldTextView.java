package com.lib.base.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.lib.base.R;

/**
 * 仿哔站标题折叠：左文字右箭头；默认收起；不足一行隐藏箭头；超一行省略号；
 * 箭头位置固定；展开/收起带高度过渡 + 箭头旋转动画。
 * 未设置 ctv_foldIconExpandSrc 时用单图旋转；设置了则淡入淡出切图。
 */
public class FoldTextView extends FrameLayout {

    private static final long DEFAULT_ANIM_DURATION = 220L;

    private final TextView textView;
    private final ImageView foldIcon;

    private CharSequence rawText = "";
    private boolean expanded;
    private boolean needFold;
    private float textSizePx;
    private int textColor = Color.parseColor("#212121");
    private int gapHeight;
    private int foldIconSize;
    private int iconTextGap;
    private Drawable foldIconSrc;
    private Drawable foldIconExpandSrc;
    private long animDuration = DEFAULT_ANIM_DURATION;

    private boolean animating;
    private int animHeight = -1;
    @Nullable
    private ValueAnimator heightAnimator;

    private OnFoldChangeListener foldChangeListener;

    public interface OnFoldChangeListener {
        void onFoldChanged(boolean expanded);
    }

    public FoldTextView(Context context) {
        this(context, null);
    }

    public FoldTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoldTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = context.getResources().getDisplayMetrics().density;
        textSizePx = 14f * density;
        gapHeight = Math.round(40f * density);
        foldIconSize = Math.round(16f * density);
        iconTextGap = Math.round(4f * density);
        foldIconSrc = ContextCompat.getDrawable(context, R.drawable.ic_fold_arrow_down);
        foldIconExpandSrc = null;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FoldTextView);
            rawText = ta.getText(R.styleable.FoldTextView_ctv_text);
            if (rawText == null) {
                rawText = "";
            }
            textSizePx = ta.getDimension(R.styleable.FoldTextView_ctv_textSize, textSizePx);
            textColor = ta.getColor(R.styleable.FoldTextView_ctv_textColor, textColor);
            gapHeight = ta.getDimensionPixelSize(R.styleable.FoldTextView_ctv_gapHeight, gapHeight);
            foldIconSize = ta.getDimensionPixelSize(R.styleable.FoldTextView_ctv_foldIconSize, foldIconSize);
            iconTextGap = ta.getDimensionPixelSize(R.styleable.FoldTextView_ctv_iconTextGap, iconTextGap);
            Drawable src = ta.getDrawable(R.styleable.FoldTextView_ctv_foldIconSrc);
            if (src != null) {
                foldIconSrc = src;
            }
            foldIconExpandSrc = ta.getDrawable(R.styleable.FoldTextView_ctv_foldIconExpandSrc);
            expanded = ta.getBoolean(R.styleable.FoldTextView_ctv_expanded, false);
            ta.recycle();
        }

        textView = new AppCompatTextView(context);
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
        textView.setIncludeFontPadding(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        // 始终 WRAP_CONTENT，避免被塞进固定高度后裁字
        LayoutParams textLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textLp.gravity = Gravity.TOP;
        addView(textView, textLp);

        foldIcon = new AppCompatImageView(context);
        foldIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        foldIcon.setVisibility(GONE);
        LayoutParams iconLp = new LayoutParams(foldIconSize, foldIconSize);
        iconLp.gravity = Gravity.END | Gravity.TOP;
        addView(foldIcon, iconLp);

        setClipChildren(false);
        setClipToPadding(false);

        applyIconState(false);
        textView.setText(rawText);

        OnClickListener toggle = v -> {
            if (!needFold || animating) {
                return;
            }
            setExpanded(!expanded, true);
        };
        setOnClickListener(toggle);
        foldIcon.setOnClickListener(toggle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int contentW = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        updateFoldNeed(contentW);
        if (!animating) {
            applyDisplayState();
        }

        if (animating && animHeight >= 0) {
            int heightSpec = MeasureSpec.makeMeasureSpec(animHeight, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightSpec);
            setMeasuredDimension(getMeasuredWidth(), animHeight);
            return;
        }

        if (!expanded) {
            int h = collapsedHeight(width);
            int heightSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightSpec);
            setMeasuredDimension(getMeasuredWidth(), h);
        } else {
            setMinimumHeight(0);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelHeightAnim();
        super.onDetachedFromWindow();
    }

    private void updateFoldNeed(int contentWidth) {
        if (contentWidth <= 0) {
            needFold = false;
            return;
        }
        CharSequence text = rawText == null ? "" : rawText;
        float textW = textView.getPaint().measureText(text.toString());
        needFold = textW > contentWidth;
    }

    private int iconReserve() {
        return needFold ? foldIconSize + iconTextGap : 0;
    }

    /** 实测单行文字自然高度（不会裁字）。 */
    private int measureSingleLineHeight(int viewWidth) {
        int endPad = iconReserve();
        int contentW = Math.max(1, viewWidth - getPaddingLeft() - getPaddingRight());
        int wSpec = MeasureSpec.makeMeasureSpec(contentW, MeasureSpec.EXACTLY);
        int hSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        int oldMax = textView.getMaxLines();
        TextUtils.TruncateAt oldEllipsize = textView.getEllipsize();
        int oldPadL = textView.getPaddingLeft();
        int oldPadT = textView.getPaddingTop();
        int oldPadR = textView.getPaddingRight();
        int oldPadB = textView.getPaddingBottom();

        textView.setMaxLines(1);
        textView.setEllipsize(needFold ? TextUtils.TruncateAt.END : null);
        textView.setPadding(0, 0, endPad, 0);
        textView.measure(wSpec, hSpec);
        int h = textView.getMeasuredHeight();

        textView.setMaxLines(oldMax);
        textView.setEllipsize(oldEllipsize);
        textView.setPadding(oldPadL, oldPadT, oldPadR, oldPadB);
        return Math.max(h, Math.round(textSizePx));
    }

    private int effectiveGapHeight(int viewWidth, int singleLineH) {
        return Math.max(gapHeight, singleLineH);
    }

    private int verticalInset(int gapH, int singleLineH) {
        return Math.max(0, (gapH - singleLineH) / 2);
    }

    private int collapsedHeight(int viewWidth) {
        int w = viewWidth > 0 ? viewWidth : Math.max(getWidth(), getMeasuredWidth());
        if (w <= 0) {
            return gapHeight + getPaddingTop() + getPaddingBottom();
        }
        return effectiveGapHeight(w, measureSingleLineHeight(w)) + getPaddingTop() + getPaddingBottom();
    }

    /** 按展开态测量完整高度。 */
    private int measureExpandedHeight(int width) {
        int endPad = iconReserve();
        int singleLineH = measureSingleLineHeight(width);
        int gapH = effectiveGapHeight(width, singleLineH);
        int vInset = verticalInset(gapH, singleLineH);
        int contentW = Math.max(1, width - getPaddingLeft() - getPaddingRight());
        int wSpec = MeasureSpec.makeMeasureSpec(contentW, MeasureSpec.EXACTLY);
        int hSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        int oldMax = textView.getMaxLines();
        TextUtils.TruncateAt oldEllipsize = textView.getEllipsize();
        int oldPadL = textView.getPaddingLeft();
        int oldPadT = textView.getPaddingTop();
        int oldPadR = textView.getPaddingRight();
        int oldPadB = textView.getPaddingBottom();

        textView.setMaxLines(Integer.MAX_VALUE);
        textView.setEllipsize(null);
        textView.setPadding(0, vInset, endPad, vInset);
        textView.measure(wSpec, hSpec);
        int textH = textView.getMeasuredHeight();

        textView.setMaxLines(oldMax);
        textView.setEllipsize(oldEllipsize);
        textView.setPadding(oldPadL, oldPadT, oldPadR, oldPadB);

        return textH + getPaddingTop() + getPaddingBottom();
    }

    private void applyDisplayState() {
        LayoutParams textLp = (LayoutParams) textView.getLayoutParams();
        if (textLp == null) {
            textLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        LayoutParams iconLp = (LayoutParams) foldIcon.getLayoutParams();
        if (iconLp == null) {
            iconLp = new LayoutParams(foldIconSize, foldIconSize);
        }

        int endPad = iconReserve();
        int width = getMeasuredWidth() > 0 ? getMeasuredWidth() : getWidth();
        int singleLineH = width > 0 ? measureSingleLineHeight(width) : Math.round(textSizePx);
        int gapH = width > 0 ? effectiveGapHeight(width, singleLineH) : gapHeight;
        int vInset = verticalInset(gapH, singleLineH);

        // 箭头位置始终固定（相对收起行高垂直居中），展开/收起/动画都不改
        iconLp.width = foldIconSize;
        iconLp.height = foldIconSize;
        iconLp.gravity = Gravity.END | Gravity.TOP;
        iconLp.topMargin = Math.max(0, (gapH - foldIconSize) / 2);
        iconLp.bottomMargin = 0;
        iconLp.setMarginStart(0);
        iconLp.setMarginEnd(0);

        textLp.width = LayoutParams.MATCH_PARENT;
        textLp.height = LayoutParams.WRAP_CONTENT;
        // 始终 TOP + vInset：收起动画开始时不会因切 CENTER_VERTICAL 而整行下跳
        textLp.gravity = Gravity.TOP;
        textView.setGravity(Gravity.START | Gravity.TOP);

        if (!needFold) {
            foldIcon.setVisibility(GONE);
            textView.setMaxLines(Integer.MAX_VALUE);
            textView.setEllipsize(null);
            textView.setPadding(0, vInset, 0, vInset);
            textView.setLayoutParams(textLp);
            foldIcon.setLayoutParams(iconLp);
            textView.setText(rawText);
            return;
        }

        foldIcon.setVisibility(VISIBLE);
        textView.setPadding(0, vInset, endPad, vInset);
        if (expanded) {
            textView.setMaxLines(Integer.MAX_VALUE);
            textView.setEllipsize(null);
        } else {
            textView.setMaxLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.END);
        }
        textView.setLayoutParams(textLp);
        foldIcon.setLayoutParams(iconLp);
        textView.setText(rawText);
    }

    private void applyIconState(boolean animate) {
        foldIcon.animate().cancel();
        // 配置了展开图：淡入淡出切图；否则单图 0°↔180° 旋转
        if (foldIconExpandSrc != null) {
            Drawable target = expanded ? foldIconExpandSrc : foldIconSrc;
            if (target == null) {
                return;
            }
            if (animate) {
                foldIcon.animate().alpha(0f).setDuration(animDuration / 2).withEndAction(() -> {
                    foldIcon.setImageDrawable(target);
                    foldIcon.setRotation(0f);
                    foldIcon.animate().alpha(1f).setDuration(animDuration / 2).start();
                }).start();
            } else {
                foldIcon.setImageDrawable(target);
                foldIcon.setAlpha(1f);
                foldIcon.setRotation(0f);
            }
            return;
        }
        if (foldIconSrc == null) {
            return;
        }
        foldIcon.setImageDrawable(foldIconSrc);
        foldIcon.setAlpha(1f);
        float targetRot = expanded ? 180f : 0f;
        if (animate) {
            foldIcon.animate()
                    .rotation(targetRot)
                    .setDuration(animDuration)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            foldIcon.setRotation(targetRot);
        }
    }

    private void cancelHeightAnim() {
        if (heightAnimator != null) {
            heightAnimator.removeAllListeners();
            heightAnimator.cancel();
            heightAnimator = null;
        }
        foldIcon.animate().cancel();
        foldIcon.setAlpha(1f);
        animating = false;
        animHeight = -1;
        setClipChildren(false);
        setClipToPadding(false);
    }

    private void animateHeight(int from, int to, final boolean targetExpanded) {
        cancelHeightAnim();
        animating = true;
        animHeight = from;
        expanded = targetExpanded;
        // 先切到目标文案态，再只动画高度；高度动画期间临时裁剪子 View
        setClipChildren(true);
        setClipToPadding(true);
        applyDisplayState();
        applyIconState(true);

        heightAnimator = ValueAnimator.ofInt(from, to);
        heightAnimator.setDuration(animDuration);
        heightAnimator.setInterpolator(new DecelerateInterpolator());
        heightAnimator.addUpdateListener(animation -> {
            animHeight = (int) animation.getAnimatedValue();
            requestLayout();
        });
        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishHeightAnim(targetExpanded, true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                finishHeightAnim(targetExpanded, false);
            }
        });
        heightAnimator.start();
    }

    private void finishHeightAnim(boolean targetExpanded, boolean notify) {
        animating = false;
        animHeight = -1;
        heightAnimator = null;
        expanded = targetExpanded;
        setClipChildren(false);
        setClipToPadding(false);
        applyDisplayState();
        requestLayout();
        if (notify && foldChangeListener != null) {
            foldChangeListener.onFoldChanged(targetExpanded);
        }
    }

    public void setText(@Nullable CharSequence text) {
        rawText = text == null ? "" : text;
        textView.setText(rawText);
        requestLayout();
    }

    public CharSequence getText() {
        return rawText;
    }

    public void setExpanded(boolean expanded) {
        setExpanded(expanded, false);
    }

    public void setExpanded(boolean expanded, boolean animate) {
        if (this.expanded == expanded && !animating) {
            return;
        }
        if (!needFold) {
            this.expanded = expanded;
            applyIconState(false);
            requestLayout();
            return;
        }

        if (!animate || getWidth() <= 0) {
            cancelHeightAnim();
            this.expanded = expanded;
            applyIconState(false);
            applyDisplayState();
            requestLayout();
            if (foldChangeListener != null) {
                foldChangeListener.onFoldChanged(expanded);
            }
            return;
        }

        int w = getWidth();
        int from = getHeight() > 0 ? getHeight() : (this.expanded ? measureExpandedHeight(w) : collapsedHeight(w));
        int to = expanded ? measureExpandedHeight(w) : collapsedHeight(w);
        if (from == to) {
            this.expanded = expanded;
            applyIconState(true);
            applyDisplayState();
            requestLayout();
            if (foldChangeListener != null) {
                foldChangeListener.onFoldChanged(expanded);
            }
            return;
        }
        animateHeight(from, to, expanded);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isNeedFold() {
        return needFold;
    }

    public void setOnFoldChangeListener(@Nullable OnFoldChangeListener listener) {
        foldChangeListener = listener;
    }

    public void setAnimDuration(long durationMs) {
        animDuration = Math.max(0L, durationMs);
    }

    public void setFoldTextSize(float sizePx) {
        textSizePx = sizePx;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
        requestLayout();
    }

    public void setFoldTextColor(@ColorInt int color) {
        textColor = color;
        textView.setTextColor(color);
    }

    public void setGapHeight(int gapHeightPx) {
        gapHeight = Math.max(0, gapHeightPx);
        requestLayout();
    }

    public void setFoldIconSize(int sizePx) {
        foldIconSize = Math.max(0, sizePx);
        requestLayout();
    }

    public void setFoldIconSrc(@DrawableRes int resId) {
        foldIconSrc = ContextCompat.getDrawable(getContext(), resId);
        applyIconState(false);
    }

    public void setFoldIconSrc(@Nullable Drawable drawable) {
        foldIconSrc = drawable;
        applyIconState(false);
    }

    public void setFoldIconExpandSrc(@DrawableRes int resId) {
        foldIconExpandSrc = ContextCompat.getDrawable(getContext(), resId);
        applyIconState(false);
    }

    public void setFoldIconExpandSrc(@Nullable Drawable drawable) {
        foldIconExpandSrc = drawable;
        applyIconState(false);
    }

    public void setIconTextGap(int gapPx) {
        iconTextGap = Math.max(0, gapPx);
        requestLayout();
    }

    public TextView getTextView() {
        return textView;
    }

    public ImageView getFoldIcon() {
        return foldIcon;
    }
}
