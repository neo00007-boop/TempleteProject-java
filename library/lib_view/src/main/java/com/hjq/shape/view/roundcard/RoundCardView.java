package com.hjq.shape.view.roundcard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import com.hjq.shape.R;

/**
 * CardView 风格容器（平行实现，非 {@link androidx.cardview.widget.CardView} 子类）。
 * <p>
 * <b>扩展</b>：四角圆角、{@code rcvShadowColor}。<br>
 * <b>阴影</b>：四周均匀的软阴影（Bitmap 缓存，View 保持硬件加速，可进列表），不走系统 elevation 的偏下投影。<br>
 * <b>注意</b>：{@code android:padding} 无效，请用 {@code rcvContentPadding*}。
 */
public class RoundCardView extends FrameLayout {

    private static final int[] COLOR_BACKGROUND_ATTR = {android.R.attr.colorBackground};
    private static final RoundCardViewImpl IMPL;

    static {
        if (Build.VERSION.SDK_INT >= 21) {
            IMPL = new RoundCardViewApi21Impl();
        } else if (Build.VERSION.SDK_INT >= 17) {
            IMPL = new RoundCardViewApi17Impl();
        } else {
            IMPL = new RoundCardViewBaseImpl();
        }
        IMPL.initStatic();
    }

    private boolean mCompatPadding;
    private boolean mPreventCornerOverlap;
    @Nullable
    private ColorStateList mShadowColor;
    int mUserSetMinWidth, mUserSetMinHeight;
    final Rect mContentPadding = new Rect();
    final Rect mShadowBounds = new Rect();

    public RoundCardView(@NonNull Context context) {
        this(context, null);
    }

    public RoundCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("ResourceType")
    public RoundCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundCardView, defStyleAttr,
                0);
        ColorStateList backgroundColor;
        if (a.hasValue(R.styleable.RoundCardView_rcvBackgroundColor)) {
            backgroundColor = a.getColorStateList(R.styleable.RoundCardView_rcvBackgroundColor);
        } else {
            final TypedArray aa = getContext().obtainStyledAttributes(COLOR_BACKGROUND_ATTR);
            final int themeColorBackground = aa.getColor(0, 0);
            aa.recycle();
            final float[] hsv = new float[3];
            Color.colorToHSV(themeColorBackground, hsv);
            backgroundColor = ColorStateList.valueOf(hsv[2] > 0.5f
                    ? getResources().getColor(R.color.rcv_light_background)
                    : getResources().getColor(R.color.rcv_dark_background));
        }
        float radius = a.getDimension(R.styleable.RoundCardView_rcvCornerRadius,
                getResources().getDimension(R.dimen.rcv_default_radius));
        float topLeft = a.getDimension(R.styleable.RoundCardView_rcvTopLeftRadius, radius);
        float topRight = a.getDimension(R.styleable.RoundCardView_rcvTopRightRadius, radius);
        float bottomRight = a.getDimension(R.styleable.RoundCardView_rcvBottomRightRadius, radius);
        float bottomLeft = a.getDimension(R.styleable.RoundCardView_rcvBottomLeftRadius, radius);
        float elevation = a.getDimension(R.styleable.RoundCardView_rcvElevation,
                getResources().getDimension(R.dimen.rcv_default_elevation));
        float maxElevation = a.getDimension(R.styleable.RoundCardView_rcvMaxElevation, elevation);
        final boolean hasShadowColor = a.hasValue(R.styleable.RoundCardView_rcvShadowColor);
        final ColorStateList shadowColor = hasShadowColor
                ? a.getColorStateList(R.styleable.RoundCardView_rcvShadowColor) : null;
        mCompatPadding = a.getBoolean(R.styleable.RoundCardView_rcvUseCompatPadding, false);
        mPreventCornerOverlap = a.getBoolean(R.styleable.RoundCardView_rcvPreventCornerOverlap,
                true);
        int defaultPadding = a.getDimensionPixelSize(R.styleable.RoundCardView_rcvContentPadding, 0);
        mContentPadding.left = a.getDimensionPixelSize(
                R.styleable.RoundCardView_rcvContentPaddingLeft, defaultPadding);
        mContentPadding.top = a.getDimensionPixelSize(
                R.styleable.RoundCardView_rcvContentPaddingTop, defaultPadding);
        mContentPadding.right = a.getDimensionPixelSize(
                R.styleable.RoundCardView_rcvContentPaddingRight, defaultPadding);
        mContentPadding.bottom = a.getDimensionPixelSize(
                R.styleable.RoundCardView_rcvContentPaddingBottom, defaultPadding);
        if (elevation > maxElevation) {
            maxElevation = elevation;
        }
        mUserSetMinWidth = a.getDimensionPixelSize(R.styleable.RoundCardView_android_minWidth, 0);
        mUserSetMinHeight = a.getDimensionPixelSize(R.styleable.RoundCardView_android_minHeight, 0);
        a.recycle();

        IMPL.initialize(mCardViewDelegate, context, backgroundColor, radius, elevation,
                maxElevation);
        if (topLeft != radius || topRight != radius || bottomRight != radius
                || bottomLeft != radius) {
            setCornerRadius(topLeft, topRight, bottomRight, bottomLeft);
        }
        if (hasShadowColor) {
            setCardShadowColor(shadowColor);
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        // 与 CardView 相同：禁止外部 padding，避免破坏阴影预留边距；请用 setContentPadding / rcvContentPadding*
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        // NO OP — 同上
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Drawable background = getBackground();
        if (!(background instanceof RoundRectDrawable)) {
            super.dispatchDraw(canvas);
            return;
        }
        // 软阴影不能用 clipToOutline（会裁掉四周光晕），子 View 在这里按圆角裁切
        RoundRectDrawable rd = (RoundRectDrawable) background;
        int save = canvas.save();
        canvas.clipPath(rd.getClipPath());
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    public boolean getUseCompatPadding() {
        return mCompatPadding;
    }

    public void setUseCompatPadding(boolean useCompatPadding) {
        if (mCompatPadding != useCompatPadding) {
            mCompatPadding = useCompatPadding;
            IMPL.onCompatPaddingChanged(mCardViewDelegate);
        }
    }

    public void setContentPadding(@Px int left, @Px int top, @Px int right, @Px int bottom) {
        mContentPadding.set(left, top, right, bottom);
        IMPL.updatePadding(mCardViewDelegate);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!(IMPL instanceof RoundCardViewApi21Impl)) {
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            switch (widthMode) {
                case MeasureSpec.EXACTLY:
                case MeasureSpec.AT_MOST:
                    final int minWidth = (int) Math.ceil(IMPL.getMinWidth(mCardViewDelegate));
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            Math.max(minWidth, MeasureSpec.getSize(widthMeasureSpec)), widthMode);
                    break;
                case MeasureSpec.UNSPECIFIED:
                    break;
            }
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            switch (heightMode) {
                case MeasureSpec.EXACTLY:
                case MeasureSpec.AT_MOST:
                    final int minHeight = (int) Math.ceil(IMPL.getMinHeight(mCardViewDelegate));
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            Math.max(minHeight, MeasureSpec.getSize(heightMeasureSpec)),
                            heightMode);
                    break;
                case MeasureSpec.UNSPECIFIED:
                    break;
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void setMinimumWidth(int minWidth) {
        mUserSetMinWidth = minWidth;
        super.setMinimumWidth(minWidth);
    }

    @Override
    public void setMinimumHeight(int minHeight) {
        mUserSetMinHeight = minHeight;
        super.setMinimumHeight(minHeight);
    }

    public void setCardBackgroundColor(@ColorInt int color) {
        IMPL.setBackgroundColor(mCardViewDelegate, ColorStateList.valueOf(color));
    }

    public void setCardBackgroundColor(@Nullable ColorStateList color) {
        IMPL.setBackgroundColor(mCardViewDelegate, color);
    }

    @NonNull
    public ColorStateList getCardBackgroundColor() {
        return IMPL.getBackgroundColor(mCardViewDelegate);
    }

    @Px
    public int getContentPaddingLeft() {
        return mContentPadding.left;
    }

    @Px
    public int getContentPaddingRight() {
        return mContentPadding.right;
    }

    @Px
    public int getContentPaddingTop() {
        return mContentPadding.top;
    }

    @Px
    public int getContentPaddingBottom() {
        return mContentPadding.bottom;
    }

    public void setRadius(float radius) {
        IMPL.setRadius(mCardViewDelegate, radius);
    }

    public float getRadius() {
        return IMPL.getRadius(mCardViewDelegate);
    }

    public void setCornerRadius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        // API 21+ 四角独立；API < 21 见 RoundCardViewBaseImpl（退化为 max 统一圆角）
        IMPL.setCornerRadii(mCardViewDelegate, new float[]{
                topLeft, topLeft, topRight, topRight,
                bottomRight, bottomRight, bottomLeft, bottomLeft
        });
    }

    public void setCornerRadii(@NonNull float[] radii) {
        if (radii.length < 8) {
            throw new IllegalArgumentException("radii must contain at least 8 values");
        }
        IMPL.setCornerRadii(mCardViewDelegate, radii);
    }

    @NonNull
    public float[] getCornerRadii() {
        return IMPL.getCornerRadii(mCardViewDelegate);
    }

    public void setTopLeftRadius(float radius) {
        float[] radii = getCornerRadii();
        radii[0] = radii[1] = radius;
        setCornerRadii(radii);
    }

    public void setTopRightRadius(float radius) {
        float[] radii = getCornerRadii();
        radii[2] = radii[3] = radius;
        setCornerRadii(radii);
    }

    public void setBottomRightRadius(float radius) {
        float[] radii = getCornerRadii();
        radii[4] = radii[5] = radius;
        setCornerRadii(radii);
    }

    public void setBottomLeftRadius(float radius) {
        float[] radii = getCornerRadii();
        radii[6] = radii[7] = radius;
        setCornerRadii(radii);
    }

    public float getTopLeftRadius() {
        return getCornerRadii()[0];
    }

    public float getTopRightRadius() {
        return getCornerRadii()[2];
    }

    public float getBottomRightRadius() {
        return getCornerRadii()[4];
    }

    public float getBottomLeftRadius() {
        return getCornerRadii()[6];
    }

    public void setCardElevation(float elevation) {
        IMPL.setElevation(mCardViewDelegate, elevation);
    }

    public float getCardElevation() {
        return IMPL.getElevation(mCardViewDelegate);
    }

    /**
     * 自定义阴影色。API 28+ 改 outline；更低版本作用在软阴影 Bitmap 上。
     */
    public void setCardShadowColor(@Nullable ColorStateList color) {
        mShadowColor = color;
        IMPL.setShadowColor(mCardViewDelegate, color);
    }

    public void setCardShadowColor(@ColorInt int color) {
        setCardShadowColor(ColorStateList.valueOf(color));
    }

    @Nullable
    public ColorStateList getCardShadowColor() {
        return mShadowColor;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mShadowColor != null && mShadowColor.isStateful()) {
            IMPL.setShadowColor(mCardViewDelegate, mShadowColor);
        }
    }

    public void setMaxCardElevation(float maxElevation) {
        IMPL.setMaxElevation(mCardViewDelegate, maxElevation);
    }

    public float getMaxCardElevation() {
        return IMPL.getMaxElevation(mCardViewDelegate);
    }

    public boolean getPreventCornerOverlap() {
        return mPreventCornerOverlap;
    }

    public void setPreventCornerOverlap(boolean preventCornerOverlap) {
        if (preventCornerOverlap != mPreventCornerOverlap) {
            mPreventCornerOverlap = preventCornerOverlap;
            IMPL.onPreventCornerOverlapChanged(mCardViewDelegate);
        }
    }

    private final RoundCardViewDelegate mCardViewDelegate = new RoundCardViewDelegate() {
        private Drawable mCardBackground;

        @Override
        public void setCardBackground(Drawable drawable) {
            mCardBackground = drawable;
            setBackgroundDrawable(drawable);
        }

        @Override
        public boolean getUseCompatPadding() {
            return RoundCardView.this.getUseCompatPadding();
        }

        @Override
        public boolean getPreventCornerOverlap() {
            return RoundCardView.this.getPreventCornerOverlap();
        }

        @Override
        public ColorStateList getShadowColor() {
            return RoundCardView.this.getCardShadowColor();
        }

        @Override
        public void setShadowPadding(int left, int top, int right, int bottom) {
            mShadowBounds.set(left, top, right, bottom);
            RoundCardView.super.setPadding(left + mContentPadding.left, top + mContentPadding.top,
                    right + mContentPadding.right, bottom + mContentPadding.bottom);
        }

        @Override
        public void setMinWidthHeightInternal(int width, int height) {
            if (width > mUserSetMinWidth) {
                RoundCardView.super.setMinimumWidth(width);
            }
            if (height > mUserSetMinHeight) {
                RoundCardView.super.setMinimumHeight(height);
            }
        }

        @Override
        public Drawable getCardBackground() {
            return mCardBackground;
        }

        @Override
        public View getCardView() {
            return RoundCardView.this;
        }
    };
}
