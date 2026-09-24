package com.lib.base.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.hjq.shape.view.progress.ProgressDrawable;
import com.lib.base.R;
import com.lib.base.databinding.HolderViewBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ProjectName  TempleteProject-java
 * PackageName  com.lib.base.ui.widget
 * @author      xwchen
 * Date         2021/12/31.
 */

public class HolderView extends FrameLayout {
    //    private static final int TIME_STR = 360;
    private static final String LOADING_STR = "加载中...";
    private static final String NO_DATA_STR = "没有找到相关的内容~";
    private static final String ERROR_STR = "网络不太给力，请稍后再试~";

    private final HolderViewBinding binding;
    private final int srcLoadingResource;
    private final int srcNoDataResource;
    private final int srcErrorResource;
    private final String strLoading;
    private final String strNoData;
    private final String strError;
    private boolean cancelFresh;
    /**
     * 为 true 时，loading/空/错态会拦截触摸（适合盖在内容上的容器模式）。
     * 作为 BRVAH emptyView 时应设为 false，否则空态下无法下拉刷新。
     */
    private boolean blockTouch = true;

    public HolderView(@NonNull Context context) {
        this(context, null);
    }

    public HolderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HolderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = HolderViewBinding.inflate(LayoutInflater.from(context), this, true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HolderView);
        srcLoadingResource = a.getResourceId(R.styleable.HolderView_srcLoadingResource, R.drawable.loading_bg);
        srcNoDataResource = a.getResourceId(R.styleable.HolderView_srcNoDataResource, R.drawable.ic_no_data);
        srcErrorResource = a.getResourceId(R.styleable.HolderView_srcErrorResource, R.drawable.ic_error);
        strLoading = a.getString(R.styleable.HolderView_strLoading);
        strNoData = a.getString(R.styleable.HolderView_strNoData);
        strError = a.getString(R.styleable.HolderView_strError);
        a.recycle();
    }

    /**
     * @param blockTouch true=容器遮罩模式拦截触摸；false=emptyView 模式放行手势
     */
    public void setBlockTouch(boolean blockTouch) {
        this.blockTouch = blockTouch;
    }

    public void showLoadingView() {
        binding.tv.setVisibility(GONE);
        binding.iv.setVisibility(GONE);

//        binding.llLoading.setVisibility(VISIBLE);
        binding.holderLoading.setVisibility(VISIBLE);
//        binding.ivLoading.setAnimation(R.raw.progress);
        /*if (!binding.ivLoading.isAnimating()) {
            binding.ivLoading.playAnimation();
        }*/
        //binding.tvLoading.setText(getTxt(strLoading, LOADING_STR));
        binding.loadingDialog.getRoot().setVisibility(VISIBLE);
        play();
        changeState(VISIBLE);
        //binding.getRoot().setBackgroundResource(R.color.black10);
        cancelFresh(false);
    }

    private ProgressDrawable progressDrawable;

    private void play() {
        if (progressDrawable == null) {
            progressDrawable = new ProgressDrawable();
            progressDrawable.setColor(Color.parseColor("#ffffff"));
        }
        binding.loadingDialog.ivPro.setImageDrawable(progressDrawable);
        progressDrawable.start();
    }

    private String getTxt(String strLoading, String loadingStr) {
        return !TextUtils.isEmpty(strLoading) ? strLoading : loadingStr;
    }

    public void showNoDataView() {
        stopAnim();
        //binding.llLoading.setVisibility(GONE);
        binding.loadingDialog.getRoot().setVisibility(GONE);
        binding.holderLoading.setVisibility(GONE);

        binding.iv.setVisibility(VISIBLE);
        binding.iv.setImageResource(srcNoDataResource);
        binding.tv.setVisibility(VISIBLE);
        binding.tv.setText(getTxt(strNoData, NO_DATA_STR));
        changeState(VISIBLE);
        //binding.getRoot().setBackgroundResource(R.color.cl_white);
        cancelFresh(false);
    }

    public void showErrorView() {
        stopAnim();
//        binding.llLoading.setVisibility(GONE);
        binding.loadingDialog.getRoot().setVisibility(GONE);
        binding.holderLoading.setVisibility(GONE);

        binding.iv.setVisibility(VISIBLE);
        binding.iv.setImageResource(srcErrorResource);
        binding.tv.setVisibility(VISIBLE);
        binding.tv.setText(getTxt(strError, ERROR_STR));
        changeState(VISIBLE);
        //binding.getRoot().setBackgroundResource(R.color.cl_white);
        cancelFresh(false);
    }


    private void changeState(int visible) {
        if (visible != binding.getRoot().getVisibility()) {
            binding.getRoot().setVisibility(visible);
        }
    }

    public void hideView() {
        stopAnim();
        changeState(GONE);
        cancelFresh(true);
    }

    private void stopAnim() {
        /*if (binding.ivLoading.isAnimating()) {
            binding.ivLoading.cancelAnimation();
        }*/
        if (progressDrawable != null && progressDrawable.isRunning()) {
            progressDrawable.stop();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (blockTouch && !cancelFresh) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void cancelFresh(boolean cancelFresh) {
        this.cancelFresh = cancelFresh;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnim();
    }
}
