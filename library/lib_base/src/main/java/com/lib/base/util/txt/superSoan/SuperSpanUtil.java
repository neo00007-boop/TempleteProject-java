package com.lib.base.util.txt.superSoan;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.lib.base.util.OUtil;

import androidx.annotation.NonNull;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.util.txt
 * @author      xwchen
 * Date         2022/2/16.
 */

public class SuperSpanUtil {

    /**
     * @param tv
     * @param colorStr  注意以#开头,六位或者八位
     * @param size
     * @param clickable
     * @param bold
     * @param contents  将整个文本切割list(文本+label+文本+label+...)传入
     */
    public static void setSuperLabel(@NonNull TextView tv, @NonNull String colorStr, int size, boolean clickable, boolean bold,
                                     SpanClickListener listener, @NonNull SpanData... contents) {
        SpannableString span = new SpannableString(getTotalContent(contents));
        for (int i = 0; i < contents.length; i++) {
            SpanData spanData = contents[i];
            //不是标签直接跳过
            if (!spanData.isLabel) {
                continue;
            }
            int lenBefore = getCurrentLenBefore(i, contents);
            int lenEnd = lenBefore + spanData.contentLen;
            //color
            if (colorStr.length() == 7 || colorStr.length() == 9) {
                span.setSpan(new ForegroundColorSpan(Color.parseColor(colorStr)), lenBefore, lenEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            //click
            if (clickable) {
                int finalI = i;
                span.setSpan(new ClickableSpan() {
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        ds.setUnderlineText(false);    //去除超链接的下划线
                    }

                    @Override
                    public void onClick(View widget) {
                        if (listener != null) {
                            listener.click(getSpanIndex(finalI, contents));
                        }
                    }
                }, lenBefore, lenEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            //size
            if (size > 0) {
                span.setSpan(new AbsoluteSizeSpan(size), lenBefore, lenEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            //粗体
            if (bold) {
                span.setSpan(new StyleSpan(Typeface.BOLD), lenBefore, lenEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        if (clickable) {
            tv.setMovementMethod(LinkMovementMethod.getInstance());//不设置无法点击
            tv.setHighlightColor(tv.getContext().getResources().getColor(android.R.color.transparent));//不设置该属性，点击后会有背景色
        }
        tv.setText(span);
    }

    /**
     * 判断点击的标签是第几个标签
     *
     * @param contents
     * @param finalI
     * @return
     */
    private static int getSpanIndex(int finalI, SpanData... contents) {
        int index = 0;
        for (SpanData content : contents) {
            if (content.isLabel) {
                if (indexOf(contents, content) < finalI) {
                    index++;
                } else {
                    break;
                }
            }
        }
        return index + 1;
    }

    private static int indexOf(SpanData[] contents, SpanData content) {
        for (int i = 0; i < contents.length; i++) {
            // 按这一段本身找位置。两段文案相同（两次 @ 同一个人）时，按文字比会永远命中第一段。
            if (contents[i] == content) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 获取当前标签起始位置=之前文本长度+标签长度
     *
     * @param contents
     * @param i
     * @return
     */
    private static int getCurrentLenBefore(int i, SpanData... contents) {
        int len = 0;
        for (int j = 0; j < i; j++) {
            len += contents[j].contentLen;
        }
        return len;
    }

    /**
     * 总文本=文本+标签
     *
     * @param contents
     * @return
     */
    @NonNull
    private static String getTotalContent(@NonNull SpanData... contents) {
        StringBuilder builder = new StringBuilder();
        for (SpanData spanData : contents) {
            if (OUtil.isNull(spanData.content)) {
                throw new RuntimeException("content is null?");
            }
            builder.append(spanData.content);

        }
        return builder.toString();
    }

}
