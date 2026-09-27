package com.lib.base.util.txt.label;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.lib.base.R;
import com.lib.base.util.txt.boldSpan.FakeBoldSpan;
import com.lib.base.util.txt.boldSpan.Spanny;
import com.lib.base.util.txt.superSoan.SpanClickListener;
import com.lib.base.util.txt.superSoan.SpanData;
import com.lib.base.util.txt.superSoan.SuperSpanUtil;

import java.util.ArrayList;
import java.util.Collections;

/**
 * txt工具类
 * ProjectName  XSCat
 * PackageName  com.bigheadhorse.xscat.utils
 *
 * @author xwchen
 * Date         2021/6/11.
 */
public class TxtUtil {
    private static final int BG_COLOR = R.color.cl_yellow;
    private static final int TV_COLOR = R.color.cl_333333;

    /**
     * 修改多个标签颜色,只支持同一个颜色
     *
     * @param tv
     * @param color
     * @param content
     * @param label
     */
    public static void setMultipleLabelColor(TextView tv, String color, String content, @NonNull String... label) {
        applyLabelHtml(tv, color, false, content, label);
    }

    /**
     * 修改多个标签颜色,并用 html 加粗。
     *
     * @param tv
     * @param color
     * @param bold
     * @param content
     * @param label
     */
    public static void setMultipleLabelColorBold(TextView tv, String color, boolean bold, String content, @NonNull String... label) {
        applyLabelHtml(tv, color, bold, content, label);
    }

    /**
     * 给正文里的多个标签上色,可选加粗。只拼 html,不扫文本加 span。
     * 从左往右看正文:当前位置是标签就套上 font,不是就原样留下。
     * 长标签排前面,避免“标签”把“标签1”从中间拆开。
     */
    private static void applyLabelHtml(TextView tv, String color, boolean bold, String content, String... labels) {
        if (tv == null) {
            return;
        }
        if (content == null) {
            content = "";
        }
        // 丢掉空标签和重复标签
        ArrayList<String> list = new ArrayList<>();
        if (labels != null) {
            for (String s : labels) {
                if (!TextUtils.isEmpty(s) && !list.contains(s)) {
                    list.add(s);
                }
            }
        }
        if (list.isEmpty()) {
            tv.setText(content);
            return;
        }
        // 字数多的排前面,同一个位置先配更长的词
        Collections.sort(list, (a, b) -> b.length() - a.length());
        // 颜色和正文都转义,避免引号、尖括号把 font 标签截断
        String safeColor = TextUtils.htmlEncode(color == null ? "" : color);
        String open = bold ? "<b>" : "";
        String close = bold ? "</b>" : "";
        StringBuilder html = new StringBuilder();
        int i = 0;
        while (i < content.length()) {
            String hit = null;
            for (String label : list) {
                if (content.startsWith(label, i)) {
                    hit = label;
                    break;
                }
            }
            // 普通文字原样拼上
            if (hit == null) {
                html.append(TextUtils.htmlEncode(content.substring(i, i + 1)));
                i++;
                continue;
            }
            // 标签整段包起来,然后跳过这几个字,不再往里面配短标签
            html.append("<font color='").append(safeColor).append("'>")
                    .append(open)
                    .append(TextUtils.htmlEncode(hit))
                    .append(close)
                    .append("</font>");
            i += hit.length();
        }
        tv.setText(Html.fromHtml(html.toString()));
    }

    /**
     * 动态调整字体的宽度,默认0.8f
     *
     * @param textView
     * @param text
     */
    public static void setTextBold(@NonNull TextView textView, String text) {
        textView.setText(new Spanny().append(text, new FakeBoldSpan()));
    }

    /**
     * 动态调整字体的宽度
     *
     * @param textView
     * @param text
     * @param size
     */
    public static void setTextBold(@NonNull TextView textView, String text, float size) {
        textView.setText(new Spanny().append(text, new FakeBoldSpan(size)));
    }

    /**
     * 文本开头添加多个标签(带背景色圆角)，标签之间留一个空格。
     *
     * @param context
     * @param tv
     * @param content
     * @param size
     * @param labels
     */
    public static void addStartLabels(Context context, @NonNull TextView tv, String content, int size, String... labels) {
        if (context == null || size <= 0 || labels == null || labels.length == 0) {
            tv.setText(content);
            return;
        }
        StringBuilder prefix = new StringBuilder();
        int index = 0;
        SpannableString spanText = null;
        for (String label : labels) {
            if (TextUtils.isEmpty(label)) {
                continue;
            }
            prefix.append("  ");
        }
        if (prefix.length() == 0) {
            tv.setText(content);
            return;
        }
        spanText = new SpannableString(prefix + (content == null ? "" : content));
        for (String label : labels) {
            if (TextUtils.isEmpty(label)) {
                continue;
            }
            int start = index * 2;
            spanText.setSpan(new VerticalImageSpan(getDrawable(context, size, label)),
                    start, start + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            index++;
        }
        tv.setText(spanText);
    }

    /**
     * 获取标签Drawable
     *
     * @param context
     * @param size
     * @param lable1
     * @return
     */
    @NonNull
    private static Drawable getDrawable(@NonNull Context context, int size, @NonNull String lable1) {
        int width = Math.max(1, (int) ((lable1.length() - 0.7f) * size));
        int height = Math.max(1, size);
        Drawable drawable = TextDrawable.builder()
                .beginConfig()
                .width(width)
                .height(height)
                .textColor(context.getResources().getColor(TV_COLOR))
                .fontSize(Math.max(1, (int) (size * 0.6f)))
                .bold()
                .endConfig()
                .buildRoundRect(lable1, context.getResources().getColor(BG_COLOR), Math.max(0, size / 6));
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        return drawable;
    }

    /**
     * 超级富文本,可以修改多个标签,包括颜色,字号,粗体,可点击
     *
     * @param tv        TextView
     * @param colorStr  注意以#开头,六位或者八位,不支持三位
     * @param size      标签字号
     * @param clickable 标签可点击
     * @param bold      标签粗体
     * @param listener  点击回调
     * @param contents  将整个文本切割list(文本+label+文本+label+...)传入
     */
    public static void setSuperLabel(@NonNull TextView tv, @NonNull String colorStr, int size, boolean clickable, boolean bold, SpanClickListener listener, @NonNull SpanData... contents) {
        SuperSpanUtil.setSuperLabel(tv, colorStr, size, clickable, bold, listener, contents);
    }

    /**
     * 文本开头添加多个图片,图片之间留一个空格。
     *
     * @param tv
     * @param title
     * @param resIds
     */
    public static void setImageSpan(TextView tv, String title, int... resIds) {
        if (tv == null || resIds == null || resIds.length == 0) {
            return;
        }
        Drawable[] drawables = new Drawable[resIds.length];
        int count = 0;
        for (int resId : resIds) {
            Drawable drawable;
            try {
                drawable = ResourcesCompat.getDrawable(tv.getContext().getResources(), resId, null);
            } catch (Resources.NotFoundException e) {
                continue;
            }
            if (drawable == null) {
                continue;
            }
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            if (width <= 0 || height <= 0) {
                continue;
            }
            drawable.setBounds(0, 0, width, height);
            drawables[count++] = drawable;
        }
        if (count == 0) {
            return;
        }
        if (TextUtils.isEmpty(title)) {
            CharSequence text = tv.getText();
            title = text == null ? "" : text.toString();
        }
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < count; i++) {
            prefix.append("  ");
        }
        SpannableString spanString = new SpannableString(prefix + title);
        for (int i = 0; i < count; i++) {
            int start = i * 2;
            spanString.setSpan(new VerticalImageSpan(drawables[i]), start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(spanString);
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    public static String getTwoPoint(double value) {
        return String.format("%.2f", value);
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    public static String getTwoPoint(@NonNull String value) {
        try {
            return String.format("%.2f", Float.parseFloat(value));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    public static String getPoint(double value) {
        return String.format("%.1f", value);
    }

    @NonNull
    @SuppressLint("DefaultLocale")
    public static String getPoint(@NonNull String value) {
        try {
            return String.format("%.1f", Float.parseFloat(value));
        } catch (NumberFormatException e) {
            return "";
        }
    }

    @SuppressLint("DefaultLocale")
    public static String getPointNo0(double value) {
        String format = String.format("%.1f", value);
        return format.endsWith(".0") ? format.replace(".0", "") : format;
    }

    @SuppressLint("DefaultLocale")
    public static String getPointNo0(@NonNull String value) {
        try {
            String format = String.format("%.1f", Float.parseFloat(value));
            return format.endsWith(".0") ? format.replace(".0", "") : format;
        } catch (NumberFormatException e) {
            return "";
        }
    }
}
