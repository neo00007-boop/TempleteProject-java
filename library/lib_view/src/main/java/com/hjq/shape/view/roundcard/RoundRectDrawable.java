package com.hjq.shape.view.roundcard;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * API 21+ 卡片背景。
 * <p>
 * 软阴影先画进 Bitmap 缓存（{@link BlurMaskFilter.Blur#OUTER}，无纵向偏移），再 blit 到硬件 Canvas，
 * 四周均匀扩散。<b>不需要</b>给整个 View 开 {@code LAYER_TYPE_SOFTWARE}，列表可流畅滚动。
 */
@RequiresApi(21)
class RoundRectDrawable extends Drawable {
    private static final float SHADOW_MULTIPLIER = 1.5f;
    private static final int SOFT_OPAQUE_SHADOW_ALPHA = 0x2A;
    private static final float TRANSLUCENT_ALPHA_SCALE = 0.45f;

    private float mRadius;
    private final float[] mRadii = new float[8];
    private final float[] mClampedRadii = new float[8];
    private final Paint mPaint;
    private final Paint mShadowPaint;
    private final RectF mBoundsF;
    private final Rect mBoundsI;
    private final Path mPath;
    private final Path mShadowPath;
    private float mPadding;
    private boolean mInsetForPadding = false;
    private boolean mInsetForRadius = true;
    private boolean mUniformRadius = true;
    private float mShadowSize;
    private float mCachedBlur = -1f;
    @Nullable
    private BlurMaskFilter mOuterBlur;
    @Nullable
    private ColorStateList mCompatShadowColor;
    private ColorStateList mBackground;
    private PorterDuffColorFilter mTintFilter;
    private ColorStateList mTint;
    private PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;

    @Nullable
    private Bitmap mShadowBitmap;
    private int mShadowBitmapKey;

    RoundRectDrawable(ColorStateList backgroundColor, float radius) {
        mRadius = Math.max(0f, radius);
        setRadiiInternal(mRadius, mRadius, mRadius, mRadius);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mShadowPaint.setStyle(Paint.Style.FILL);
        setBackground(backgroundColor);
        mBoundsF = new RectF();
        mBoundsI = new Rect();
        mPath = new Path();
        mShadowPath = new Path();
    }

    private void setBackground(ColorStateList color) {
        mBackground = (color == null) ? ColorStateList.valueOf(Color.TRANSPARENT) : color;
        mPaint.setColor(mBackground.getColorForState(getState(), mBackground.getDefaultColor()));
    }

    void setPadding(float padding, boolean insetForPadding, boolean insetForRadius) {
        if (padding == mPadding && mInsetForPadding == insetForPadding
                && mInsetForRadius == insetForRadius) {
            return;
        }
        mPadding = padding;
        mInsetForPadding = insetForPadding;
        mInsetForRadius = insetForRadius;
        updateBounds(null);
        invalidateShadowCache();
        invalidateSelf();
    }

    float getPadding() {
        return mPadding;
    }

    void setShadowSize(float shadowSize) {
        if (shadowSize < 0f) {
            shadowSize = 0f;
        }
        if (mShadowSize == shadowSize) {
            return;
        }
        mShadowSize = shadowSize;
        invalidateShadowCache();
        invalidateSelf();
    }

    float getShadowSize() {
        return mShadowSize;
    }

    void setCompatShadowColor(@Nullable ColorStateList color) {
        if (mCompatShadowColor == color) {
            return;
        }
        mCompatShadowColor = color;
        invalidateShadowCache();
        invalidateSelf();
    }

    @Nullable
    ColorStateList getCompatShadowColor() {
        return mCompatShadowColor;
    }

    boolean usesCompatShadow() {
        return mCompatShadowColor != null && mShadowSize > 0f;
    }

    boolean isUniformRadius() {
        return mUniformRadius;
    }

    @NonNull
    Path getClipPath() {
        buildPath();
        return mPath;
    }

    @Override
    public void draw(Canvas canvas) {
        final Paint paint = mPaint;
        final boolean clearColorFilter;
        if (mTintFilter != null && paint.getColorFilter() == null) {
            paint.setColorFilter(mTintFilter);
            clearColorFilter = true;
        } else {
            clearColorFilter = false;
        }

        if (usesCompatShadow()) {
            Bitmap shadow = getShadowBitmap();
            if (shadow != null) {
                final Rect bounds = getBounds();
                canvas.drawBitmap(shadow, bounds.left, bounds.top, null);
            }
        }

        if (mUniformRadius) {
            canvas.drawRoundRect(mBoundsF, mRadius, mRadius, paint);
        } else {
            buildPath();
            canvas.drawPath(mPath, paint);
        }

        if (clearColorFilter) {
            paint.setColorFilter(null);
        }
    }

    /**
     * 在软件 Canvas 上生成阴影 Bitmap。
     * 用 {@link BlurMaskFilter.Blur#OUTER} 只画外侧光晕，避免 setShadowLayer+DST_OUT
     * 抗锯齿对不齐留下灰色描边（彩色阴影在深色背景下尤其明显）。
     */
    @Nullable
    private Bitmap getShadowBitmap() {
        final Rect bounds = getBounds();
        final int w = bounds.width();
        final int h = bounds.height();
        if (w <= 0 || h <= 0) {
            return null;
        }
        final int shadowColor = softenShadowColor(mCompatShadowColor.getColorForState(
                getState(), mCompatShadowColor.getDefaultColor()));
        final int key = shadowCacheKey(w, h, shadowColor);
        if (mShadowBitmap != null && mShadowCacheValid(key, w, h)) {
            return mShadowBitmap;
        }
        invalidateShadowCache();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas soft = new Canvas(bitmap);

        final float blur = Math.max(mShadowSize * 0.55f, 2f);
        if (mOuterBlur == null || mCachedBlur != blur) {
            mCachedBlur = blur;
            mOuterBlur = new BlurMaskFilter(blur, BlurMaskFilter.Blur.OUTER);
        }
        final float ox = -bounds.left;
        final float oy = -bounds.top;
        final RectF local = new RectF(mBoundsF);
        local.offset(ox, oy);

        mShadowPaint.setMaskFilter(mOuterBlur);
        mShadowPaint.setColor(shadowColor);
        mShadowPaint.clearShadowLayer();
        if (mUniformRadius) {
            soft.drawRoundRect(local, mRadius, mRadius, mShadowPaint);
        } else {
            mShadowPath.reset();
            float[] clamped = new float[8];
            final float maxX = Math.max(0f, local.width() / 2f);
            final float maxY = Math.max(0f, local.height() / 2f);
            for (int i = 0; i < 8; i += 2) {
                clamped[i] = Math.min(Math.max(0f, mRadii[i]), maxX);
                clamped[i + 1] = Math.min(Math.max(0f, mRadii[i + 1]), maxY);
            }
            mShadowPath.addRoundRect(local, clamped, Path.Direction.CW);
            soft.drawPath(mShadowPath, mShadowPaint);
        }
        mShadowPaint.setMaskFilter(null);

        mShadowBitmap = bitmap;
        mShadowBitmapKey = key;
        return bitmap;
    }

    private int shadowCacheKey(int w, int h, int shadowColor) {
        int result = w;
        result = 31 * result + h;
        result = 31 * result + Float.floatToIntBits(mShadowSize);
        result = 31 * result + Float.floatToIntBits(mRadius);
        result = 31 * result + shadowColor;
        result = 31 * result + (mUniformRadius ? 1 : 0);
        for (float r : mRadii) {
            result = 31 * result + Float.floatToIntBits(r);
        }
        result = 31 * result + Float.floatToIntBits(mBoundsF.left);
        result = 31 * result + Float.floatToIntBits(mBoundsF.top);
        result = 31 * result + Float.floatToIntBits(mBoundsF.right);
        result = 31 * result + Float.floatToIntBits(mBoundsF.bottom);
        return result;
    }

    private boolean mShadowCacheValid(int key, int w, int h) {
        return mShadowBitmapKey == key
                && mShadowBitmap.getWidth() == w
                && mShadowBitmap.getHeight() == h
                && !mShadowBitmap.isRecycled();
    }

    private void invalidateShadowCache() {
        if (mShadowBitmap != null) {
            mShadowBitmap.recycle();
            mShadowBitmap = null;
        }
        mShadowBitmapKey = 0;
        mCachedBlur = -1f;
        mOuterBlur = null;
    }

    private void buildPath() {
        mPath.reset();
        clampRadiiInto(mClampedRadii);
        mPath.addRoundRect(mBoundsF, mClampedRadii, Path.Direction.CW);
    }

    private void clampRadiiInto(float[] out) {
        final float maxX = Math.max(0f, mBoundsF.width() / 2f);
        final float maxY = Math.max(0f, mBoundsF.height() / 2f);
        for (int i = 0; i < 8; i += 2) {
            out[i] = Math.min(Math.max(0f, mRadii[i]), maxX);
            out[i + 1] = Math.min(Math.max(0f, mRadii[i + 1]), maxY);
        }
    }

    private void updateBounds(Rect bounds) {
        if (bounds == null) {
            bounds = getBounds();
        }
        mBoundsF.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
        mBoundsI.set(bounds);
        if (mInsetForPadding) {
            if (mCompatShadowColor != null) {
                float inset = Math.max(mPadding, mShadowSize) * SHADOW_MULTIPLIER;
                int pad = (int) Math.ceil(inset);
                mBoundsI.inset(pad, pad);
            } else {
                float radiusForPadding = getMaxRadius();
                float vInset = RoundRectDrawableWithShadow.calculateVerticalPadding(mPadding,
                        radiusForPadding, mInsetForRadius);
                float hInset = RoundRectDrawableWithShadow.calculateHorizontalPadding(mPadding,
                        radiusForPadding, mInsetForRadius);
                mBoundsI.inset((int) Math.ceil(hInset), (int) Math.ceil(vInset));
            }
            mBoundsF.set(mBoundsI);
        }
        invalidateShadowCache();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateBounds(bounds);
    }

    @Override
    public void getOutline(Outline outline) {
        if (mUniformRadius) {
            outline.setRoundRect(mBoundsI, mRadius);
            return;
        }
        buildPath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            outline.setPath(mPath);
        } else {
            try {
                outline.setConvexPath(mPath);
            } catch (IllegalArgumentException ignored) {
                float r = Math.min(getMaxRadius(),
                        Math.min(mBoundsI.width(), mBoundsI.height()) / 2f);
                outline.setRoundRect(mBoundsI, r);
            }
        }
    }

    void setRadius(float radius) {
        if (mUniformRadius && radius == mRadius) {
            return;
        }
        mRadius = radius;
        setRadiiInternal(radius, radius, radius, radius);
        mUniformRadius = true;
        updateBounds(null);
        invalidateSelf();
    }

    void setCornerRadii(float[] radii) {
        if (radii == null || radii.length < 8) {
            throw new IllegalArgumentException("radii must contain 8 values");
        }
        for (int i = 0; i < 8; i++) {
            if (radii[i] < 0f) {
                throw new IllegalArgumentException("radii must be >= 0");
            }
        }
        System.arraycopy(radii, 0, mRadii, 0, 8);
        mUniformRadius = isUniform(mRadii);
        mRadius = mUniformRadius ? mRadii[0] : getMaxRadius();
        updateBounds(null);
        invalidateSelf();
    }

    float[] getCornerRadii() {
        return mRadii.clone();
    }

    private void setRadiiInternal(float topLeft, float topRight, float bottomRight,
            float bottomLeft) {
        mRadii[0] = mRadii[1] = topLeft;
        mRadii[2] = mRadii[3] = topRight;
        mRadii[4] = mRadii[5] = bottomRight;
        mRadii[6] = mRadii[7] = bottomLeft;
    }

    private static boolean isUniform(float[] radii) {
        float first = radii[0];
        for (int i = 1; i < 8; i++) {
            if (radii[i] != first) {
                return false;
            }
        }
        return true;
    }

    float getMaxRadius() {
        float max = mRadii[0];
        for (int i = 2; i < 8; i += 2) {
            if (mRadii[i] > max) {
                max = mRadii[i];
            }
        }
        return max;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setColor(@Nullable ColorStateList color) {
        setBackground(color);
        invalidateSelf();
    }

    public ColorStateList getColor() {
        return mBackground;
    }

    @Override
    public void setTintList(ColorStateList tint) {
        mTint = tint;
        mTintFilter = createTintFilter(mTint, mTintMode);
        invalidateSelf();
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        mTintMode = tintMode;
        mTintFilter = createTintFilter(mTint, mTintMode);
        invalidateSelf();
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        final int newColor = mBackground.getColorForState(stateSet, mBackground.getDefaultColor());
        final boolean colorChanged = newColor != mPaint.getColor();
        if (colorChanged) {
            mPaint.setColor(newColor);
        }
        boolean shadowStateful = mCompatShadowColor != null && mCompatShadowColor.isStateful();
        if (shadowStateful) {
            invalidateShadowCache();
        }
        if (mTint != null && mTintMode != null) {
            mTintFilter = createTintFilter(mTint, mTintMode);
            return true;
        }
        return colorChanged || shadowStateful;
    }

    @Override
    public boolean isStateful() {
        return (mTint != null && mTint.isStateful())
                || (mBackground != null && mBackground.isStateful())
                || (mCompatShadowColor != null && mCompatShadowColor.isStateful())
                || super.isStateful();
    }

    private PorterDuffColorFilter createTintFilter(ColorStateList tint, PorterDuff.Mode tintMode) {
        if (tint == null || tintMode == null) {
            return null;
        }
        final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
        return new PorterDuffColorFilter(color, tintMode);
    }

    private static int softenShadowColor(int color) {
        int alpha = Color.alpha(color);
        if (alpha >= 255) {
            alpha = SOFT_OPAQUE_SHADOW_ALPHA;
        } else {
            alpha = Math.max(1, Math.round(alpha * TRANSLUCENT_ALPHA_SCALE));
        }
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
