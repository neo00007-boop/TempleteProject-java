package com.lib.base.util.txt.superSoan;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.util.txt.superSoan
 *
 * @author xwchen
 * Date         2022/2/16.
 */

public class SpanData {
    //每一段文本
    public String content;
    //当前文本是不是标签
    public boolean isLabel;
    //文本长度
    public int contentLen;

    public SpanData(String content, boolean isLabel) {
        this.content = content;
        this.isLabel = isLabel;
        this.contentLen = content.length();
    }

    public static SpanData build(String content, boolean isLabel) {
        return new SpanData(content, isLabel);
    }
}
