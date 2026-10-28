package com.lib.base.ui.widget.ninegrid;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * 以 URL 字符串为数据源的默认九宫格 Adapter。
 */
public class UrlNineGridAdapter extends NineGridImageViewAdapter<String> {

    @Override
    protected void onDisplayImage(Context context, ImageView imageView, String url) {
        Glide.with(imageView).load(url).centerCrop().into(imageView);
    }
}
