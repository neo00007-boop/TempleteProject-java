package com.lib.base.ui.widget.ninegrid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 正方形格子：填充底色 + 圆角虚线/实线描边（加号格用）。
 */
public class DashBorderSquareLayout extends SquareFrameLayout {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private int bgColor = Color.parseColor("#F5F5F5");
    private int borderColor = Color.parseColor("#CCCCCC");
    private float borderWidthPx;
    private float cornerRadiusPx;
    private float dashWidthPx;
    private float dashGapPx;
    private boolean dashed = true;

    public DashBorderSquareLayout(@NonNull Context context) {
        this(context, null);
    }

    public DashBorderSquareLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashBorderSquareLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = context.getResources().getDisplayMetrics().density;
        borderWidthPx = Math.max(1f, density);
        cornerRadiusPx = 6f * density;
        dashWidthPx = 6f * density;
        dashGapPx = 4f * density;
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        setWillNotDraw(false);
        // 部分机型虚线需关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        applyStrokeEffect();
    }

    public void setCellStyle(@ColorInt int bgColor,
                             @ColorInt int borderColor,
                             float borderWidthPx,
                             float cornerRadiusPx,
                             float dashWidthPx,
                             float dashGapPx,
                             boolean dashed) {
        this.bgColor = bgColor;
        this.borderColor = borderColor;
        this.borderWidthPx = Math.max(0f, borderWidthPx);
        this.cornerRadiusPx = Math.max(0f, cornerRadiusPx);
        this.dashWidthPx = Math.max(0f, dashWidthPx);
        this.dashGapPx = Math.max(0f, dashGapPx);
        this.dashed = dashed;
        applyStrokeEffect();
        invalidate();
    }

    private void applyStrokeEffect() {
        strokePaint.setStrokeWidth(borderWidthPx);
        PathEffect effect = null;
        if (dashed && dashWidthPx > 0f) {
            effect = new DashPathEffect(new float[]{dashWidthPx, dashGapPx}, 0f);
        }
        strokePaint.setPathEffect(effect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float inset = borderWidthPx / 2f;
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);
        fillPaint.setColor(bgColor);
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint);
        if (borderWidthPx > 0f && (borderColor >>> 24) != 0) {
            strokePaint.setColor(borderColor);
            canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, strokePaint);
        }
        super.onDraw(canvas);
    }
}
