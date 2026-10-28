package com.lib.base.ui.widget.ninegrid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 九宫格图片：圆角、蒙层均按 px / ARGB 色值生效（与 XML dimension/color 一致）。
 */
public class NineGridRoundImageView extends AppCompatImageView {

    private float cornerRadiusPx;
    private int maskColor;

    private final Path path = new Path();
    private final RectF rectF = new RectF();
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public NineGridRoundImageView(Context context) {
        this(context, null);
    }

    public NineGridRoundImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NineGridRoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        maskPaint.setStyle(Paint.Style.FILL);
    }

    public void setCornerRadiusPx(float cornerRadiusPx) {
        if (this.cornerRadiusPx != cornerRadiusPx) {
            this.cornerRadiusPx = Math.max(0f, cornerRadiusPx);
            invalidate();
        }
    }

    public void setMaskColor(@ColorInt int maskColor) {
        if (this.maskColor != maskColor) {
            this.maskColor = maskColor;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            super.onDraw(canvas);
            return;
        }
        rectF.set(0, 0, w, h);
        path.reset();
        float r = cornerRadiusPx;
        if (r > 0f) {
            path.addRoundRect(rectF, r, r, Path.Direction.CW);
        } else {
            path.addRect(rectF, Path.Direction.CW);
        }
        int save = canvas.save();
        canvas.clipPath(path);
        super.onDraw(canvas);
        if ((maskColor >>> 24) != 0) {
            maskPaint.setColor(maskColor);
            canvas.drawPath(path, maskPaint);
        }
        canvas.restoreToCount(save);
    }
}
