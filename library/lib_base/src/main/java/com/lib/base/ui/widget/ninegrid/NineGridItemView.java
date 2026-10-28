package com.lib.base.ui.widget.ninegrid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 九宫格单格：圆角图 + 可选蒙层 + 右下角「n+」角标（样式均可配置）。
 */
public class NineGridItemView extends FrameLayout {

    private final NineGridRoundImageView imageView;
    private final TextView moreTextView;
    private final GradientDrawable moreBgDrawable;

    private int moreTextColor = Color.WHITE;
    private int moreTextBgColor = Color.parseColor("#99000000");
    private float moreTextCornerRadiusPx;
    private float moreTextSizePx;
    private int moreMarginEndPx;
    private int moreMarginBottomPx;
    private int morePaddingHorizontalPx;
    private int morePaddingVerticalPx;

    public NineGridItemView(Context context) {
        super(context);
        float density = context.getResources().getDisplayMetrics().density;
        moreTextCornerRadiusPx = 4 * density;
        moreTextSizePx = 16 * density;
        moreMarginEndPx = Math.round(6 * density);
        moreMarginBottomPx = moreMarginEndPx;
        morePaddingHorizontalPx = Math.round(6 * density);
        morePaddingVerticalPx = Math.round(2 * density);

        imageView = new NineGridRoundImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(imageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        moreBgDrawable = new GradientDrawable();
        moreBgDrawable.setShape(GradientDrawable.RECTANGLE);
        moreBgDrawable.setColor(moreTextBgColor);
        moreBgDrawable.setCornerRadius(moreTextCornerRadiusPx);

        moreTextView = new TextView(context);
        moreTextView.setTextColor(moreTextColor);
        moreTextView.setTypeface(Typeface.DEFAULT_BOLD);
        moreTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, moreTextSizePx);
        moreTextView.setGravity(Gravity.CENTER);
        moreTextView.setVisibility(GONE);
        moreTextView.setIncludeFontPadding(false);
        moreTextView.setPadding(morePaddingHorizontalPx, morePaddingVerticalPx,
                morePaddingHorizontalPx, morePaddingVerticalPx);
        moreTextView.setBackground(moreBgDrawable);

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM | Gravity.END;
        lp.setMargins(0, 0, moreMarginEndPx, moreMarginBottomPx);
        addView(moreTextView, lp);
    }

    public ImageView getImageView() {
        return imageView;
    }

    /** 图片圆角（px）、蒙层色（ARGB，alpha=0 表示无蒙层）。 */
    public void applyImageStyle(float cornerRadiusPx, int maskColor) {
        imageView.setCornerRadiusPx(cornerRadiusPx);
        imageView.setMaskColor(maskColor);
    }

    /**
     * 「n+」角标样式（均为 px）。
     */
    public void applyMoreBadgeStyle(int textColor, int bgColor, float cornerRadiusPx, float textSizePx,
                                    int marginEndPx, int marginBottomPx,
                                    int paddingHorizontalPx, int paddingVerticalPx) {
        moreTextColor = textColor;
        moreTextBgColor = bgColor;
        moreTextCornerRadiusPx = Math.max(0f, cornerRadiusPx);
        if (textSizePx > 0f) {
            moreTextSizePx = textSizePx;
        }
        moreMarginEndPx = Math.max(0, marginEndPx);
        moreMarginBottomPx = Math.max(0, marginBottomPx);
        morePaddingHorizontalPx = Math.max(0, paddingHorizontalPx);
        morePaddingVerticalPx = Math.max(0, paddingVerticalPx);
        moreTextView.setTextColor(moreTextColor);
        moreTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, moreTextSizePx);
        moreTextView.setPadding(morePaddingHorizontalPx, morePaddingVerticalPx,
                morePaddingHorizontalPx, morePaddingVerticalPx);
        moreBgDrawable.setColor(moreTextBgColor);
        moreBgDrawable.setCornerRadius(moreTextCornerRadiusPx);
        moreTextView.setBackground(moreBgDrawable);
        LayoutParams lp = (LayoutParams) moreTextView.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM | Gravity.END;
        }
        lp.setMargins(0, 0, moreMarginEndPx, moreMarginBottomPx);
        moreTextView.setLayoutParams(lp);
    }

    /**
     * 超过 maxSize 时在最后一格显示剩余张数，如共 12 张、展示 9 张 →「3+」；可叠加末格蒙层色。
     *
     * @param remainCount 未展示的剩余张数（total - maxSize）
     */
    public void setMoreCount(int remainCount, boolean show, int moreMaskColor) {
        if (show && remainCount > 0) {
            moreTextView.setText(remainCount + "+");
            moreTextView.setVisibility(VISIBLE);
            if ((moreMaskColor >>> 24) != 0) {
                imageView.setMaskColor(moreMaskColor);
            }
        } else {
            moreTextView.setVisibility(GONE);
            moreTextView.setText(null);
        }
    }
}
