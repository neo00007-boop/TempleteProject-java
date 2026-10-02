package com.templete.project.ui.activity;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 高度按条目内容包裹，超出 maxHeight 后在列表内部滚动。
 */
public class MentionMemberList extends RecyclerView {

    private int maxHeight;

    public MentionMemberList(Context context) {
        this(context, null);
    }

    public MentionMemberList(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MentionMemberList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.maxHeight});
        maxHeight = array.getDimensionPixelSize(0, 0);
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (maxHeight > 0) {
            int mode = View.MeasureSpec.getMode(heightSpec);
            int size = View.MeasureSpec.getSize(heightSpec);
            int cap = maxHeight;
            if ((mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) && size > 0) {
                cap = Math.min(maxHeight, size);
            }
            heightSpec = View.MeasureSpec.makeMeasureSpec(cap, View.MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}
