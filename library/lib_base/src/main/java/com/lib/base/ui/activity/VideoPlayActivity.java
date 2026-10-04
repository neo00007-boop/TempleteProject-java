package com.lib.base.ui.activity;

import android.os.Bundle;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.lib.base.bean.VideoBuilder;
import com.lib.base.databinding.VideoPlayActivityBinding;
import com.lib.base.ui.widget.paly.PlayerView;

import androidx.annotation.NonNull;

/**
 * @author: Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2020/02/16
 * desc   : 视频播放界面
 */
public class VideoPlayActivity extends BaseActivity<VideoPlayActivityBinding> implements PlayerView.OnPlayListener {

    private PlayerView mPlayerView;
    private VideoBuilder mBuilder;

    @Override
    public VideoPlayActivityBinding viewBinding() {
        return VideoPlayActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public int getTitleTheme() {
        return TitleBarTheme.THEME_NONE;
    }

    @Override
    public void inits() {
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_BAR).init();
    }

    @Override
    public void initView() {
        mPlayerView =mViewBinding.pvVideoPlayView;// findViewById(R.id.pv_video_play_view);
        mPlayerView.setLifecycleOwner(this);
        mPlayerView.setOnPlayListener(this);
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        mBuilder = /*getIntent().getExtras().*/getParcelableExtra(VideoBuilder.INTENT_KEY_PARAMETERS);
        if (mBuilder == null) {
            throw new IllegalArgumentException("are you ok?");
        }

        mPlayerView.setVideoTitle(mBuilder.getVideoTitle());
        mPlayerView.setVideoSource(mBuilder.getVideoSource());
        mPlayerView.setGestureEnabled(mBuilder.isGestureEnabled());

        if (mBuilder.isAutoPlay()) {
            mPlayerView.start();
        }
    }

    /**
     * {@link PlayerView.OnPlayListener}
     */
    @Override
    public void onClickBack(PlayerView view) {
        onBackPressed();
    }

    @Override
    public void onPlayStart(PlayerView view) {
        int progress = mBuilder.getPlayProgress();
        if (progress > 0) {
            mPlayerView.setProgress(progress);
        }
    }

    @Override
    public void onPlayProgress(PlayerView view) {
        // 记录播放进度
        mBuilder.setPlayProgress(view.getProgress());
    }

    @Override
    public void onPlayEnd(PlayerView view) {
        if (mBuilder.isLoopPlay()) {
            mPlayerView.setProgress(0);
            mPlayerView.start();
            return;
        }

        if (mBuilder.isAutoOver()) {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存播放进度
        outState.putParcelable(VideoBuilder.INTENT_KEY_PARAMETERS, mBuilder);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // 读取播放进度
        mBuilder = savedInstanceState.getParcelable(VideoBuilder.INTENT_KEY_PARAMETERS);
    }

    /**
     * 竖屏播放
     */
    public static final class Portrait extends VideoPlayActivity {
    }

    /**
     * 横屏播放
     */
    public static final class Landscape extends VideoPlayActivity {
    }

}