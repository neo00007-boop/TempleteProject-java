package com.hjq.shape.view.roundcard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * API 21+：四周均匀的软阴影（Bitmap + BlurMaskFilter.OUTER）。
 * 不用系统 elevation，避免 Material 阴影偏下。View 保持硬件加速，可进列表。
 */
@RequiresApi(21)
class RoundCardViewApi21Impl implements RoundCardViewImpl {
    private static final ColorStateList DEFAULT_SOFT_SHADOW =
            ColorStateList.valueOf(Color.BLACK);

    @Override
    public void initialize(RoundCardViewDelegate cardView, Context context,
            ColorStateList backgroundColor, float radius, float elevation, float maxElevation) {
        final RoundRectDrawable background = new RoundRectDrawable(backgroundColor, radius);
        background.setShadowSize(elevation);
        cardView.setCardBackground(background);
        applyShadowMode(cardView);
        setMaxElevation(cardView, maxElevation);
    }

    @Override
    public void setRadius(RoundCardViewDelegate cardView, float radius) {
        getCardBackground(cardView).setRadius(radius);
        if (needsShadowPadding(cardView)) {
            updatePadding(cardView);
        }
        invalidateOutline(cardView);
    }

    @Override
    public void setCornerRadii(RoundCardViewDelegate cardView, float[] radii) {
        getCardBackground(cardView).setCornerRadii(radii);
        setMaxElevation(cardView, getMaxElevation(cardView));
        invalidateOutline(cardView);
    }

    @Override
    public float[] getCornerRadii(RoundCardViewDelegate cardView) {
        return getCardBackground(cardView).getCornerRadii();
    }

    @Override
    public void initStatic() {
    }

    @Override
    public void setMaxElevation(RoundCardViewDelegate cardView, float maxElevation) {
        RoundRectDrawable background = getCardBackground(cardView);
        background.setPadding(maxElevation, needsShadowPadding(cardView),
                cardView.getPreventCornerOverlap());
        updatePadding(cardView);
    }

    @Override
    public float getMaxElevation(RoundCardViewDelegate cardView) {
        return getCardBackground(cardView).getPadding();
    }

    @Override
    public float getMinWidth(RoundCardViewDelegate cardView) {
        return getRadius(cardView) * 2;
    }

    @Override
    public float getMinHeight(RoundCardViewDelegate cardView) {
        return getRadius(cardView) * 2;
    }

    @Override
    public float getRadius(RoundCardViewDelegate cardView) {
        return getCardBackground(cardView).getRadius();
    }

    @Override
    public void setElevation(RoundCardViewDelegate cardView, float elevation) {
        getCardBackground(cardView).setShadowSize(elevation);
        applyShadowMode(cardView);
    }

    @Override
    public float getElevation(RoundCardViewDelegate cardView) {
        return getCardBackground(cardView).getShadowSize();
    }

    @Override
    public void updatePadding(RoundCardViewDelegate cardView) {
        if (!needsShadowPadding(cardView)) {
            cardView.setShadowPadding(0, 0, 0, 0);
            return;
        }
        RoundRectDrawable background = getCardBackground(cardView);
        float size = Math.max(getMaxElevation(cardView), background.getShadowSize());
        int pad = (int) Math.ceil(size * 1.5f);
        cardView.setShadowPadding(pad, pad, pad, pad);
    }

    @Override
    public void onCompatPaddingChanged(RoundCardViewDelegate cardView) {
        setMaxElevation(cardView, getMaxElevation(cardView));
    }

    @Override
    public void onPreventCornerOverlapChanged(RoundCardViewDelegate cardView) {
        setMaxElevation(cardView, getMaxElevation(cardView));
    }

    @Override
    public void setBackgroundColor(RoundCardViewDelegate cardView, @Nullable ColorStateList color) {
        getCardBackground(cardView).setColor(color);
    }

    @Override
    public ColorStateList getBackgroundColor(RoundCardViewDelegate cardView) {
        return getCardBackground(cardView).getColor();
    }

    @Override
    public void setShadowColor(RoundCardViewDelegate cardView, @Nullable ColorStateList color) {
        applyShadowMode(cardView);
    }

    private void applyShadowMode(RoundCardViewDelegate cardView) {
        final View view = cardView.getCardView();
        final RoundRectDrawable background = getCardBackground(cardView);
        final float logicalElevation = background.getShadowSize();
        final ColorStateList shadowColor = cardView.getShadowColor();

        // 始终保持硬件加速：软阴影在 Drawable 内用 Bitmap 缓存，不 setLayerType(SOFTWARE)
        view.setLayerType(View.LAYER_TYPE_NONE, null);

        ColorStateList soft = shadowColor != null ? shadowColor : DEFAULT_SOFT_SHADOW;
        background.setCompatShadowColor(soft);
        // 阴影自己画，关掉系统 elevation，避免偏下的 spot shadow
        view.setElevation(0f);
        // 不能 clipToOutline：outline 是内容区，会把四周软阴影裁掉；子 View 圆角由 RoundCardView.dispatchDraw clip
        view.setClipToOutline(false);
        setMaxElevation(cardView, Math.max(getMaxElevation(cardView), logicalElevation));
        invalidateOutline(cardView);
    }

    private boolean needsShadowPadding(RoundCardViewDelegate cardView) {
        // 四周软阴影需要预留等宽边距，否则会被裁掉
        return true;
    }

    private void invalidateOutline(RoundCardViewDelegate cardView) {
        cardView.getCardView().invalidateOutline();
    }

    private RoundRectDrawable getCardBackground(RoundCardViewDelegate cardView) {
        return (RoundRectDrawable) cardView.getCardBackground();
    }
}
