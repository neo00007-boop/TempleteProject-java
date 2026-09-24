package com.lib.base.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.hjq.shape.view.progress.ProgressDrawable;
import com.lib.base.R;
import com.lib.base.databinding.HolderViewBinding;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 列表/页面占位：loading（转圈或骨架）、无数据、错误。
 * 空/错态点击可触发 {@link #setOnRetryClickListener(OnClickListener)} 重试。
 */
public class HolderView extends FrameLayout {

    public static final int LOADING_PROGRESS = 0;
    public static final int LOADING_SHIMMER = 1;
    /** 不展示 loading UI（静默等数据 / 仅靠下拉 Header） */
    public static final int LOADING_NONE = 2;

    private static final String NO_DATA_STR = "没有找到相关的内容~";
    private static final String ERROR_STR = "网络不太给力，请稍后再试~";

    private final HolderViewBinding binding;
    private final int srcNoDataResource;
    private final int srcErrorResource;
    private final String strNoData;
    private final String strError;
    private int loadingMode;
    private boolean cancelFresh;
    /**
     * 为 true 时，loading/空/错态会拦截触摸（适合盖在内容上的容器模式）。
     * 作为 BRVAH emptyView 时应设为 false，否则空态下无法下拉刷新。
     */
    private boolean blockTouch = true;
    /** 空数据 / 错误时可点击重试 */
    private boolean retryEnabled;
    private OnClickListener retryClickListener;

    private ProgressDrawable progressDrawable;

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
        srcNoDataResource = a.getResourceId(R.styleable.HolderView_srcNoDataResource, R.drawable.ic_no_data);
        srcErrorResource = a.getResourceId(R.styleable.HolderView_srcErrorResource, R.drawable.ic_error);
        strNoData = a.getString(R.styleable.HolderView_strNoData);
        strError = a.getString(R.styleable.HolderView_strError);
        loadingMode = a.getInt(R.styleable.HolderView_holderLoadingMode, LOADING_PROGRESS);
        int shimmerLayout = a.getResourceId(R.styleable.HolderView_holderShimmerLayout, 0);
        a.recycle();

        if (shimmerLayout != 0) {
            binding.shimmerView.setSkeletonLayout(shimmerLayout);
        }

        View.OnClickListener click = v -> {
            if (retryEnabled && retryClickListener != null) {
                retryClickListener.onClick(v);
            }
        };
        binding.getRoot().setOnClickListener(click);
        binding.statePanel.setOnClickListener(click);
        binding.iv.setOnClickListener(click);
        binding.tv.setOnClickListener(click);
    }

    public void setBlockTouch(boolean blockTouch) {
        this.blockTouch = blockTouch;
    }

    public void setOnRetryClickListener(@Nullable OnClickListener listener) {
        this.retryClickListener = listener;
    }

    public void setLoadingMode(int loadingMode) {
        this.loadingMode = loadingMode;
    }

    public int getLoadingMode() {
        return loadingMode;
    }

    /** 自定义骨架布局；配合 {@link #LOADING_SHIMMER} */
    public void setShimmerLayout(@LayoutRes int layoutRes) {
        binding.shimmerView.setSkeletonLayout(layoutRes);
    }

    public void setShimmerType(int type) {
        binding.shimmerView.setType(type);
    }

    public void showLoadingView() {
        retryEnabled = false;
        hideEmptyContent();
        if (loadingMode == LOADING_NONE) {
            binding.shimmerView.hide();
            binding.statePanel.setVisibility(GONE);
            changeState(GONE);
            cancelFresh(true);
            return;
        }
        if (loadingMode == LOADING_SHIMMER) {
            binding.statePanel.setVisibility(GONE);
            if (binding.shimmerView.getSkeletonLayout() == 0) {
                binding.shimmerView.setType(ShimmerView.TYPE_LIST);
            }
            binding.shimmerView.show();
        } else {
            binding.shimmerView.hide();
            binding.statePanel.setVisibility(VISIBLE);
            binding.holderLoading.setVisibility(VISIBLE);
            binding.loadingDialog.getRoot().setVisibility(VISIBLE);
            play();
        }
        changeState(VISIBLE);
        cancelFresh(false);
    }

    public void showNoDataView() {
        stopLoadingUi();
        binding.shimmerView.hide();
        binding.statePanel.setVisibility(VISIBLE);
        binding.iv.setVisibility(VISIBLE);
        binding.iv.setImageResource(srcNoDataResource);
        binding.tv.setVisibility(VISIBLE);
        binding.tv.setText(getTxt(strNoData, NO_DATA_STR));
        retryEnabled = true;
        changeState(VISIBLE);
        cancelFresh(false);
    }

    public void showErrorView() {
        stopLoadingUi();
        binding.shimmerView.hide();
        binding.statePanel.setVisibility(VISIBLE);
        binding.iv.setVisibility(VISIBLE);
        binding.iv.setImageResource(srcErrorResource);
        binding.tv.setVisibility(VISIBLE);
        binding.tv.setText(getTxt(strError, ERROR_STR));
        retryEnabled = true;
        changeState(VISIBLE);
        cancelFresh(false);
    }

    public void hideView() {
        retryEnabled = false;
        stopLoadingUi();
        binding.shimmerView.hide();
        changeState(GONE);
        cancelFresh(true);
    }

    private void hideEmptyContent() {
        binding.iv.setVisibility(GONE);
        binding.tv.setVisibility(GONE);
        binding.loadingDialog.getRoot().setVisibility(GONE);
        binding.holderLoading.setVisibility(GONE);
        stopProgress();
    }

    private void stopLoadingUi() {
        binding.loadingDialog.getRoot().setVisibility(GONE);
        binding.holderLoading.setVisibility(GONE);
        stopProgress();
    }

    private void play() {
        if (progressDrawable == null) {
            progressDrawable = new ProgressDrawable();
            progressDrawable.setColor(Color.parseColor("#ffffff"));
        }
        binding.loadingDialog.ivPro.setImageDrawable(progressDrawable);
        progressDrawable.start();
    }

    private void stopProgress() {
        if (progressDrawable != null && progressDrawable.isRunning()) {
            progressDrawable.stop();
        }
    }

    private String getTxt(String custom, String fallback) {
        return !TextUtils.isEmpty(custom) ? custom : fallback;
    }

    private void changeState(int visible) {
        if (visible != binding.getRoot().getVisibility()) {
            binding.getRoot().setVisibility(visible);
        }
        if (visible == VISIBLE) {
            binding.getRoot().bringToFront();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (retryEnabled) {
            return false;
        }
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
        retryEnabled = false;
        retryClickListener = null;
        stopLoadingUi();
        binding.shimmerView.hide();
        if (binding.loadingDialog.ivPro != null) {
            binding.loadingDialog.ivPro.setImageDrawable(null);
        }
        progressDrawable = null;
    }
}
