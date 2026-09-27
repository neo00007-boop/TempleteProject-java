package com.lib.base.util.txt.label;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

/**
 * 图片和文字垂直居中,并且不把行间隙撑乱。
 * <p>
 * 行高由字体的 ascent/descent 算出来,TextView 的 lineSpacingExtra 再加在这个盒子外面。
 * 默认 ImageSpan 会按图片边界重写这组度量,行与行之间的缝会被图片带着变。
 * 这里把图片高出文字的部分,上下平均补进 ascent 和 descent,并且让 top=ascent、bottom=descent。
 * 这样行盒刚好包住图片,行间隙仍按原来的 lineSpacing 走。
 * <p>
 * ProjectName  XSCat
 * PackageName  com.bigheadhorse.xscat.view.widget
 * @author      xwchen
 * Date         2021/6/15.
 */
public class VerticalImageSpan extends ImageSpan {
    public VerticalImageSpan(Drawable drawable) {
        super(drawable);
    }

    /**
     * 用图片高度重算本行的字体度量,让行盒居中包住图片,不额外拉开行间隙。
     */
    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fontMetricsInt) {
        Drawable drawable = getDrawable();
        Rect rect = drawable.getBounds();
        if (fontMetricsInt != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.descent - fmPaint.ascent;
            int drHeight = rect.bottom - rect.top;
            // 文字盒子的中线,图片围绕这条线上下展开
            int centerY = fmPaint.ascent + fontHeight / 2;

            fontMetricsInt.ascent = centerY - drHeight / 2;
            fontMetricsInt.descent = centerY + drHeight / 2;
            // top、bottom 不再留字体自带的内部留白,行距只剩 TextView 自己的 lineSpacing
            fontMetricsInt.top = fontMetricsInt.ascent;
            fontMetricsInt.bottom = fontMetricsInt.descent;
        }
        return rect.right;
    }

    /**
     * 画的时候同样对齐文字中线,和 getSize 里撑开的行盒是同一条线。
     */
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {

        Drawable drawable = getDrawable();
        canvas.save();
        Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
        int fontHeight = fmPaint.descent - fmPaint.ascent;
        // y 是基线。文字中线 = 基线 + descent - 半个字高
        int centerY = y + fmPaint.descent - fontHeight / 2;
        int transY = centerY - (drawable.getBounds().bottom - drawable.getBounds().top) / 2;
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }
}
