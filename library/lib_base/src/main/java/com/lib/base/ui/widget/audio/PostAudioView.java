package com.lib.base.ui.widget.audio;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.lib.base.util.GlobalThreadPoolUtil;
import com.lib.base.R;

import java.util.HashMap;

/**
 * 帖子音频：确定性波形 + ExoPlayer + {@link AudioPlayManager} 互斥。
 * 支持 XML 自定义属性（{@code pav_*}）：整体背景/内边距、播放钮、时长、波形颜色与柱形。
 */
public class PostAudioView extends FrameLayout {

    private static final String TAG = "PostAudioView";
    private static final long PREPARE_TIMEOUT_MS = 15_000L;
    /** 同一条波形连点不同进度：合并为最后一次 seek，减轻 Exo 压力 */
    private static final long SEEK_COALESCE_MS = 80L;
    /** seek 后进度条按住目标，避免 ticker 短暂拉回旧位置 */
    private static final long SEEK_HOLD_MS = 900L;
    private static final int UNSET_COLOR = Color.TRANSPARENT;
    private static final int UNSET_DIMEN = Integer.MIN_VALUE;

    private View llRoot;
    private View flPlayBtn;
    private ImageView ivPlay;
    private ImageView ivPause;
    private ProgressBar pbLoading;
    private View llWaveArea;
    private WaveformSeekBar waveformSeekBar;
    private TextView tvDuration;

    // —— 外观（XML / setter）——
    @Nullable
    private Drawable styleBackground;
    private int styleBgColor = UNSET_COLOR;
    private int styleCornerRadius = UNSET_DIMEN;
    private int stylePadStart = UNSET_DIMEN;
    private int stylePadTop = UNSET_DIMEN;
    private int stylePadEnd = UNSET_DIMEN;
    private int stylePadBottom = UNSET_DIMEN;
    private int stylePlayBtnSize = UNSET_DIMEN;
    @Nullable
    private Drawable stylePlayBtnBackground;
    private int stylePlayBtnColor = UNSET_COLOR;
    @Nullable
    private Drawable stylePlayIcon;
    @Nullable
    private Drawable stylePauseIcon;
    private int stylePlayIconPadding = UNSET_DIMEN;
    private int styleLoadingTint = UNSET_COLOR;
    private int styleWaveAreaMarginStart = UNSET_DIMEN;
    private int styleWaveHeight = UNSET_DIMEN;
    private int styleWaveMarginTop = UNSET_DIMEN;
    private int styleWavePlayedColor = UNSET_COLOR;
    private int styleWaveBufferColor = UNSET_COLOR;
    private int styleWaveRemainColor = UNSET_COLOR;
    private int styleWaveMinBarHeight = UNSET_DIMEN;
    private int styleWaveBarCornerRadius = UNSET_DIMEN;
    private float styleWaveBarWidthRatio = -1f;
    private int styleDurationTextSize = UNSET_DIMEN;
    private int styleDurationTextColor = UNSET_COLOR;
    /** 0 start / 1 end / 2 center */
    private int styleDurationGravity = 1;

    private ExoPlayer player;
    private String audioUrl;
    private boolean prepared;
    private boolean preparing;
    private boolean buffering;
    private boolean seekDragging;
    private boolean playWhenReady;
    private int durationMs;
    private int bufferPercent;
    /** 待跳转 0~1；&lt;0 表示当前位置续播 */
    private float pendingSeekPercent = -1f;
    /** seek 过程中 UI 按住的目标进度 */
    private float seekHoldPercent = -1f;
    private long seekHoldUntilElapsed;
    private int durationToken;
    private MediaMetadataRetriever durationRetriever;

    /** 主线程 Handler；所有 delay 必须在 pause/release/detach 时 clearHandlerTasks() */
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable progressTicker = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) {
                return;
            }
            updateProgressUi();
            // 播放中或缓冲中都继续刷；仅复用本 Runnable，不会堆积实例
            if (isAttachedToWindow() && (isPlaying() || buffering || preparing)) {
                handler.postDelayed(this, 80);
            }
        }
    };
    private final Runnable prepareTimeout = () -> {
        if (!isAttachedToWindow() || !preparing) {
            return;
        }
        Log.e(TAG, "prepare timeout url=" + audioUrl);
        resetForError("音频加载超时");
    };
    private final Runnable coalesceSeekRunnable = () -> {
        if (!isAttachedToWindow() || !prepared || player == null || pendingSeekPercent < 0f) {
            return;
        }
        Log.d(TAG, "coalesceSeek apply pending=" + pendingSeekPercent);
        applyPendingSeekIfNeeded();
        player.setPlayWhenReady(true);
        if (player.isPlaying() && !buffering) {
            showPlaying();
        } else {
            showBuffering();
        }
        startProgressTicker();
    };

    /** 摘掉所有已 post 的 delay，避免 Handler 间接持有 View */
    private void clearHandlerTasks() {
        handler.removeCallbacksAndMessages(null);
    }

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int state) {
            if (player == null) {
                return;
            }
            Log.d(TAG, "state=" + stateName(state)
                    + " playWhenReady=" + player.getPlayWhenReady()
                    + " pos=" + player.getCurrentPosition()
                    + " buf=" + player.getBufferedPercentage() + "%"
                    + " current=" + isCurrentPlayer());
            switch (state) {
                case Player.STATE_BUFFERING:
                    buffering = true;
                    syncBufferProgress();
                    if (playWhenReady || player.getPlayWhenReady()) {
                        showBuffering();
                        // 未起播/卡顿时也要刷缓冲条
                        startProgressTicker();
                    }
                    refreshTimeText(uiPercent());
                    break;
                case Player.STATE_READY:
                    onPlayerReady();
                    break;
                case Player.STATE_ENDED:
                    onPlayerEnded();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onIsPlayingChanged(boolean playing) {
            if (player == null) {
                return;
            }
            Log.d(TAG, "isPlaying=" + playing + " pos=" + player.getCurrentPosition());
            syncBufferProgress();
            if (!playing) {
                return;
            }
            buffering = false;
            showPlaying();
            startProgressTicker();
        }

        @Override
        public void onPlayerError(@NonNull PlaybackException error) {
            Log.e(TAG, "onPlayerError code=" + error.errorCode + " " + error.getMessage()
                    + " url=" + audioUrl);
            resetForError("音频播放失败");
        }
    };

    public PostAudioView(@NonNull Context context) {
        this(context, null);
    }

    public PostAudioView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PostAudioView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_post_audio, this, true);
        llRoot = findViewById(R.id.llPostAudioRoot);
        flPlayBtn = findViewById(R.id.flPlayBtn);
        ivPlay = findViewById(R.id.ivPlay);
        ivPause = findViewById(R.id.ivPause);
        pbLoading = findViewById(R.id.pbLoading);
        llWaveArea = findViewById(R.id.llWaveArea);
        waveformSeekBar = findViewById(R.id.waveformSeekBar);
        tvDuration = findViewById(R.id.tvDuration);

        parseAttrs(context, attrs);
        applyAppearance();

        ivPlay.setClickable(false);
        ivPause.setClickable(false);
        flPlayBtn.setOnClickListener(v -> {
            if (preparing || buffering || isPlaying()) {
                pause();
            } else {
                play();
            }
        });
        waveformSeekBar.setOnSeekListener(new WaveformSeekBar.OnSeekListener() {
            @Override
            public void onSeekStart() {
                seekDragging = true;
            }

            @Override
            public void onSeekChanged(float percent) {
                refreshTimeText(percent);
            }

            @Override
            public void onSeekStop(float percent) {
                seekDragging = false;
                seekAndPlay(percent);
            }

            @Override
            public void onSeekCancel() {
                seekDragging = false;
                if (prepared && player != null) {
                    updateProgressUi();
                }
            }
        });
        showIdle();
    }

    private void parseAttrs(Context context, @Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PostAudioView);
        try {
            if (ta.hasValue(R.styleable.PostAudioView_pav_background)) {
                styleBackground = ta.getDrawable(R.styleable.PostAudioView_pav_background);
            }
            styleBgColor = ta.getColor(R.styleable.PostAudioView_pav_bgColor, styleBgColor);
            styleCornerRadius = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_cornerRadius, styleCornerRadius);
            stylePadStart = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_paddingStart, stylePadStart);
            stylePadTop = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_paddingTop, stylePadTop);
            stylePadEnd = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_paddingEnd, stylePadEnd);
            stylePadBottom = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_paddingBottom, stylePadBottom);

            stylePlayBtnSize = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_playBtnSize, stylePlayBtnSize);
            if (ta.hasValue(R.styleable.PostAudioView_pav_playBtnBackground)) {
                stylePlayBtnBackground = ta.getDrawable(
                        R.styleable.PostAudioView_pav_playBtnBackground);
            }
            stylePlayBtnColor = ta.getColor(
                    R.styleable.PostAudioView_pav_playBtnColor, stylePlayBtnColor);
            if (ta.hasValue(R.styleable.PostAudioView_pav_playIcon)) {
                stylePlayIcon = ta.getDrawable(R.styleable.PostAudioView_pav_playIcon);
            }
            if (ta.hasValue(R.styleable.PostAudioView_pav_pauseIcon)) {
                stylePauseIcon = ta.getDrawable(R.styleable.PostAudioView_pav_pauseIcon);
            }
            stylePlayIconPadding = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_playIconPadding, stylePlayIconPadding);
            styleLoadingTint = ta.getColor(
                    R.styleable.PostAudioView_pav_loadingTint, styleLoadingTint);

            styleWaveAreaMarginStart = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_waveAreaMarginStart, styleWaveAreaMarginStart);
            styleWaveHeight = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_waveHeight, styleWaveHeight);
            styleWaveMarginTop = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_waveMarginTop, styleWaveMarginTop);
            styleWavePlayedColor = ta.getColor(
                    R.styleable.PostAudioView_pav_wavePlayedColor, styleWavePlayedColor);
            styleWaveBufferColor = ta.getColor(
                    R.styleable.PostAudioView_pav_waveBufferColor, styleWaveBufferColor);
            styleWaveRemainColor = ta.getColor(
                    R.styleable.PostAudioView_pav_waveRemainColor, styleWaveRemainColor);
            styleWaveMinBarHeight = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_waveMinBarHeight, styleWaveMinBarHeight);
            styleWaveBarCornerRadius = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_waveBarCornerRadius, styleWaveBarCornerRadius);
            styleWaveBarWidthRatio = ta.getFloat(
                    R.styleable.PostAudioView_pav_waveBarWidthRatio, styleWaveBarWidthRatio);

            styleDurationTextSize = ta.getDimensionPixelSize(
                    R.styleable.PostAudioView_pav_durationTextSize, styleDurationTextSize);
            styleDurationTextColor = ta.getColor(
                    R.styleable.PostAudioView_pav_durationTextColor, styleDurationTextColor);
            styleDurationGravity = ta.getInt(
                    R.styleable.PostAudioView_pav_durationGravity, styleDurationGravity);
        } finally {
            ta.recycle();
        }
    }

    private void applyAppearance() {
        if (llRoot == null) {
            return;
        }
        // 背景：drawable 优先，否则纯色+圆角
        if (styleBackground != null) {
            llRoot.setBackground(styleBackground);
        } else if (styleBgColor != UNSET_COLOR || styleCornerRadius != UNSET_DIMEN) {
            int color = styleBgColor != UNSET_COLOR
                    ? styleBgColor
                    : ContextCompat.getColor(getContext(), R.color.white);
            float radius = styleCornerRadius != UNSET_DIMEN
                    ? styleCornerRadius
                    : getResources().getDimension(R.dimen.x24);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setColor(color);
            bg.setCornerRadius(radius);
            llRoot.setBackground(bg);
        }

        int padL = stylePadStart != UNSET_DIMEN ? stylePadStart : llRoot.getPaddingStart();
        int padT = stylePadTop != UNSET_DIMEN ? stylePadTop : llRoot.getPaddingTop();
        int padR = stylePadEnd != UNSET_DIMEN ? stylePadEnd : llRoot.getPaddingEnd();
        int padB = stylePadBottom != UNSET_DIMEN ? stylePadBottom : llRoot.getPaddingBottom();
        if (stylePadStart != UNSET_DIMEN || stylePadTop != UNSET_DIMEN
                || stylePadEnd != UNSET_DIMEN || stylePadBottom != UNSET_DIMEN) {
            llRoot.setPaddingRelative(padL, padT, padR, padB);
        }

        if (stylePlayBtnSize != UNSET_DIMEN) {
            ViewGroup.LayoutParams lp = flPlayBtn.getLayoutParams();
            lp.width = stylePlayBtnSize;
            lp.height = stylePlayBtnSize;
            flPlayBtn.setLayoutParams(lp);
        }
        if (stylePlayBtnBackground != null) {
            flPlayBtn.setBackground(stylePlayBtnBackground);
        } else if (stylePlayBtnColor != UNSET_COLOR) {
            GradientDrawable oval = new GradientDrawable();
            oval.setShape(GradientDrawable.OVAL);
            oval.setColor(stylePlayBtnColor);
            flPlayBtn.setBackground(oval);
        }
        if (stylePlayIcon != null) {
            ivPlay.setImageDrawable(stylePlayIcon);
        }
        if (stylePauseIcon != null) {
            ivPause.setImageDrawable(stylePauseIcon);
        }
        if (stylePlayIconPadding != UNSET_DIMEN) {
            ivPlay.setPadding(stylePlayIconPadding, stylePlayIconPadding,
                    stylePlayIconPadding, stylePlayIconPadding);
            ivPause.setPadding(stylePlayIconPadding, stylePlayIconPadding,
                    stylePlayIconPadding, stylePlayIconPadding);
            pbLoading.setPadding(stylePlayIconPadding, stylePlayIconPadding,
                    stylePlayIconPadding, stylePlayIconPadding);
        }
        if (styleLoadingTint != UNSET_COLOR) {
            pbLoading.setIndeterminateTintList(ColorStateList.valueOf(styleLoadingTint));
        }

        if (llWaveArea != null && styleWaveAreaMarginStart != UNSET_DIMEN) {
            ViewGroup.MarginLayoutParams mlp =
                    (ViewGroup.MarginLayoutParams) llWaveArea.getLayoutParams();
            mlp.setMarginStart(styleWaveAreaMarginStart);
            llWaveArea.setLayoutParams(mlp);
        }

        if (styleWaveHeight != UNSET_DIMEN || styleWaveMarginTop != UNSET_DIMEN) {
            ViewGroup.MarginLayoutParams wlp =
                    (ViewGroup.MarginLayoutParams) waveformSeekBar.getLayoutParams();
            if (styleWaveHeight != UNSET_DIMEN) {
                wlp.height = styleWaveHeight;
            }
            if (styleWaveMarginTop != UNSET_DIMEN) {
                wlp.topMargin = styleWaveMarginTop;
            }
            waveformSeekBar.setLayoutParams(wlp);
        }
        int played = styleWavePlayedColor != UNSET_COLOR
                ? styleWavePlayedColor
                : ContextCompat.getColor(getContext(), R.color.common_accent_color);
        int buffer = styleWaveBufferColor != UNSET_COLOR
                ? styleWaveBufferColor
                : 0x805A8DDF;
        int remain = styleWaveRemainColor != UNSET_COLOR
                ? styleWaveRemainColor
                : ContextCompat.getColor(getContext(), R.color.cl_ccc);
        if (styleWavePlayedColor != UNSET_COLOR
                || styleWaveBufferColor != UNSET_COLOR
                || styleWaveRemainColor != UNSET_COLOR) {
            waveformSeekBar.setColors(played, buffer, remain);
        }
        if (styleWaveMinBarHeight != UNSET_DIMEN) {
            waveformSeekBar.setMinBarHeightPx(styleWaveMinBarHeight);
        }
        if (styleWaveBarCornerRadius != UNSET_DIMEN) {
            waveformSeekBar.setBarCornerRadiusPx(styleWaveBarCornerRadius);
        }
        if (styleWaveBarWidthRatio > 0f) {
            waveformSeekBar.setBarWidthRatio(styleWaveBarWidthRatio);
        }

        if (styleDurationTextSize != UNSET_DIMEN) {
            tvDuration.setTextSize(TypedValue.COMPLEX_UNIT_PX, styleDurationTextSize);
        }
        if (styleDurationTextColor != UNSET_COLOR) {
            tvDuration.setTextColor(styleDurationTextColor);
        }
        int gravity;
        switch (styleDurationGravity) {
            case 0:
                gravity = Gravity.START;
                break;
            case 2:
                gravity = Gravity.CENTER_HORIZONTAL;
                break;
            case 1:
            default:
                gravity = Gravity.END;
                break;
        }
        if (tvDuration.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams tlp = (LinearLayout.LayoutParams) tvDuration.getLayoutParams();
            tlp.gravity = gravity;
            tvDuration.setLayoutParams(tlp);
        }
    }

    public void setAudioUrl(String url) {
        if (TextUtils.equals(audioUrl, url) && (prepared || preparing)) {
            return;
        }
        Log.d(TAG, "setAudioUrl=" + url);
        if (isCurrentPlayer()) {
            AudioPlayManager.get().onStop(this);
        }
        releasePlayer();
        audioUrl = url;
        durationMs = 0;
        bufferPercent = 0;
        pendingSeekPercent = -1f;
        playWhenReady = false;
        seekDragging = false;
        waveformSeekBar.setWaveFromUrl(url == null ? "" : url);
        waveformSeekBar.setProgressPercent(0f);
        waveformSeekBar.setBufferPercent(0f);
        tvDuration.setText("加载中...");
        showIdle();
        setWaveSeekEnabled(false);
        fetchDuration(url);
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void play() {
        playFrom(-1f);
    }

    private void seekAndPlay(float percent) {
        if (!isAttachedToWindow()) {
            return;
        }
        pendingSeekPercent = percent;
        waveformSeekBar.setProgressPercent(percent);
        refreshTimeText(percent);
        playFrom(percent);
    }

    private void playFrom(float seekPercent) {
        if (TextUtils.isEmpty(audioUrl) || !isAttachedToWindow()) {
            return;
        }
        // 已在播且只是点播放：忽略
        if (seekPercent < 0f && isPlaying() && isCurrentPlayer()) {
            Log.d(TAG, "playFrom ignore already playing");
            return;
        }
        Log.d(TAG, "playFrom seek=" + seekPercent
                + " prepared=" + prepared + " preparing=" + preparing
                + " url=" + audioUrl);
        AudioPlayManager.get().requestPlay(this);
        if (seekPercent >= 0f) {
            pendingSeekPercent = seekPercent;
        } else if (!preparing) {
            pendingSeekPercent = -1f;
        }
        playWhenReady = true;

        if (prepared && player != null) {
            if (seekPercent >= 0f) {
                // 连点不同进度：合并成最后一次再 seekTo
                waveformSeekBar.setProgressPercent(seekPercent);
                seekHoldPercent = seekPercent;
                seekHoldUntilElapsed = SystemClock.elapsedRealtime() + SEEK_HOLD_MS;
                handler.removeCallbacks(coalesceSeekRunnable);
                handler.postDelayed(coalesceSeekRunnable, SEEK_COALESCE_MS);
                Log.d(TAG, "playFrom coalesce seek=" + seekPercent);
            } else {
                player.setPlayWhenReady(true);
                showPlaying();
                startProgressTicker();
            }
            return;
        }
        if (preparing) {
            // 加载中又点了波形：只更新 pendingSeek，READY 后跳最后一次
            Log.d(TAG, "playFrom preparing, keep pending=" + pendingSeekPercent);
            return;
        }
        preparePlayer();
    }

    public void pause() {
        pauseInternal(true);
    }

    void pauseByManager() {
        pauseInternal(false);
    }

    private void pauseInternal(boolean notifyManager) {
        Log.d(TAG, "pause notifyManager=" + notifyManager
                + " preparing=" + preparing + " url=" + audioUrl);
        playWhenReady = false;
        pendingSeekPercent = -1f;
        seekHoldPercent = -1f;
        buffering = false;
        clearHandlerTasks();
        if (preparing) {
            preparing = false;
            releasePlayerOnly();
            prepared = false;
        } else if (player != null) {
            player.setPlayWhenReady(false);
        }
        if (notifyManager) {
            AudioPlayManager.get().onStop(this);
        }
        showIdle();
    }

    private void preparePlayer() {
        if (TextUtils.isEmpty(audioUrl)) {
            return;
        }
        // 避免和拉时长抢同一连接
        abortDurationFetch();
        preparing = true;
        prepared = false;
        showLoading();
        releasePlayerOnly();

        player = new ExoPlayer.Builder(getContext()).build();
        player.addListener(playerListener);
        player.setMediaItem(MediaItem.fromUri(Uri.parse(audioUrl)));
        player.setPlayWhenReady(playWhenReady);
        Log.d(TAG, "prepare playWhenReady=" + playWhenReady
                + " pendingSeek=" + pendingSeekPercent + " url=" + audioUrl);
        handler.removeCallbacks(prepareTimeout);
        handler.postDelayed(prepareTimeout, PREPARE_TIMEOUT_MS);
        player.prepare();
        startProgressTicker();
    }

    private void onPlayerReady() {
        preparing = false;
        prepared = true;
        handler.removeCallbacks(prepareTimeout);
        long d = player.getDuration();
        if (isDurationValid(d)) {
            durationMs = (int) d;
        }
        buffering = false;
        Log.d(TAG, "onReady duration=" + durationMs
                + " pendingSeek=" + pendingSeekPercent
                + " current=" + isCurrentPlayer());

        // 准备完成时已不是当前播放者（被其它条目抢播）
        if (!isCurrentPlayer()) {
            Log.d(TAG, "onReady lost focus, pause");
            playWhenReady = false;
            pendingSeekPercent = -1f;
            player.setPlayWhenReady(false);
            showIdle();
            return;
        }

        applyPendingSeekIfNeeded();
        if (player.getPlayWhenReady()) {
            showPlaying();
            startProgressTicker();
        } else {
            showIdle();
        }
        refreshTimeText(uiPercent());
        setWaveSeekEnabled(true);
    }

    private void onPlayerEnded() {
        Log.d(TAG, "onEnded url=" + audioUrl);
        handler.removeCallbacks(progressTicker);
        handler.removeCallbacks(coalesceSeekRunnable);
        // 保留 prepareTimeout 不在 ended 清：此时已 prepared，超时回调会因 !preparing 直接 return
        buffering = false;
        playWhenReady = false;
        pendingSeekPercent = -1f;
        seekHoldPercent = -1f;
        bufferPercent = 0;
        waveformSeekBar.setProgressPercent(0f);
        waveformSeekBar.setBufferPercent(0f);
        if (player != null) {
            player.seekTo(0);
            player.setPlayWhenReady(false);
        }
        AudioPlayManager.get().onStop(this);
        refreshTimeText(0f);
        showIdle();
    }

    private void applyPendingSeekIfNeeded() {
        if (player == null || pendingSeekPercent < 0f) {
            return;
        }
        long duration = player.getDuration();
        if (!isDurationValid(duration)) {
            duration = durationMs;
        }
        if (!isDurationValid(duration)) {
            return;
        }
        float seek = pendingSeekPercent;
        long position = (long) (duration * seek);
        position = Math.max(0L, Math.min(duration - 1, position));
        Log.d(TAG, "seekTo " + position + "/" + duration);
        waveformSeekBar.setProgressPercent(seek);
        seekHoldPercent = seek;
        seekHoldUntilElapsed = SystemClock.elapsedRealtime() + SEEK_HOLD_MS;
        player.seekTo(position);
        pendingSeekPercent = -1f;
    }

    private void resetForError(String toast) {
        clearHandlerTasks();
        preparing = false;
        prepared = false;
        buffering = false;
        playWhenReady = false;
        pendingSeekPercent = -1f;
        seekHoldPercent = -1f;
        releasePlayerOnly();
        showIdle();
        if (isCurrentPlayer()) {
            Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
            AudioPlayManager.get().onStop(this);
        }
    }

    private boolean isCurrentPlayer() {
        return AudioPlayManager.get().getCurrent() == this;
    }

    public boolean isPlaying() {
        try {
            return player != null && player.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public void release() {
        clearHandlerTasks();
        if (pbLoading != null) {
            pbLoading.animate().cancel();
        }
        if (isCurrentPlayer()) {
            AudioPlayManager.get().onStop(this);
        }
        releasePlayer();
    }

    private void fetchDuration(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        final String target = url;
        final int token = ++durationToken;
        GlobalThreadPoolUtil.postOnSubThread(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            synchronized (this) {
                abortDurationFetchLocked();
                durationRetriever = retriever;
            }
            int ms = 0;
            try {
                if (target.startsWith("http")) {
                    retriever.setDataSource(target, new HashMap<>());
                } else {
                    retriever.setDataSource(target);
                }
                String value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (!TextUtils.isEmpty(value)) {
                    ms = Integer.parseInt(value);
                }
            } catch (Exception e) {
                Log.e(TAG, "fetchDuration: " + e.getMessage());
            } finally {
                synchronized (this) {
                    if (durationRetriever == retriever) {
                        durationRetriever = null;
                    }
                }
                try {
                    retriever.release();
                } catch (Exception ignored) {
                }
            }
            final int duration = ms;
            GlobalThreadPoolUtil.postOnUiThread(() -> {
                if (token != durationToken || !TextUtils.equals(audioUrl, target)) {
                    return;
                }
                if (duration > 0 && !prepared) {
                    durationMs = duration;
                    if (!seekDragging) {
                        refreshTimeText(waveformSeekBar.getProgressPercent());
                    }
                } else if (durationMs <= 0) {
                    tvDuration.setText("00:00 / --:--");
                }
                if (!preparing) {
                    setWaveSeekEnabled(true);
                }
            });
        });
    }

    private synchronized void abortDurationFetchLocked() {
        if (durationRetriever != null) {
            try {
                durationRetriever.release();
            } catch (Exception ignored) {
            }
            durationRetriever = null;
        }
    }

    private void abortDurationFetch() {
        durationToken++;
        synchronized (this) {
            abortDurationFetchLocked();
        }
    }

    private void startProgressTicker() {
        handler.removeCallbacks(progressTicker);
        handler.post(progressTicker);
    }

    private float uiPercent() {
        if (player != null && prepared) {
            long d = player.getDuration();
            if (isDurationValid(d)) {
                return clamp01(player.getCurrentPosition() * 1f / d);
            }
        }
        return waveformSeekBar.getProgressPercent();
    }

    private void updateProgressUi() {
        if (player == null) {
            return;
        }
        syncBufferProgress();
        if (seekDragging || !prepared) {
            if (preparing || buffering) {
                refreshTimeText(seekHoldPercent >= 0f
                        ? seekHoldPercent
                        : waveformSeekBar.getProgressPercent());
            }
            return;
        }
        long duration = player.getDuration();
        if (!isDurationValid(duration)) {
            return;
        }
        durationMs = (int) duration;
        float actual = clamp01(player.getCurrentPosition() * 1f / duration);
        // 连点/seek 后：未落地前 UI 停在目标点，避免闪回旧进度
        if (seekHoldPercent >= 0f) {
            float target = seekHoldPercent;
            long slackMs = Math.max(1200L, (long) (duration * 0.03f));
            boolean landed = Math.abs(player.getCurrentPosition() - (long) (duration * target))
                    <= slackMs;
            if (!landed && SystemClock.elapsedRealtime() < seekHoldUntilElapsed) {
                waveformSeekBar.setProgressPercent(target);
                refreshTimeText(target);
                return;
            }
            seekHoldPercent = -1f;
        }
        waveformSeekBar.setProgressPercent(actual);
        refreshTimeText(actual);
    }

    /** 同步波形已缓冲段 + 文案百分比 */
    private void syncBufferProgress() {
        if (player == null) {
            return;
        }
        int percent = player.getBufferedPercentage();
        if (percent <= 0) {
            long duration = player.getDuration();
            long buffered = player.getBufferedPosition();
            if (isDurationValid(duration) && buffered > 0) {
                percent = (int) Math.max(0, Math.min(100, buffered * 100f / duration));
            }
        }
        bufferPercent = Math.max(0, Math.min(100, percent));
        waveformSeekBar.setBufferPercent(bufferPercent / 100f);
    }

    private void refreshTimeText(float percent) {
        int duration = durationMs;
        if (prepared && player != null) {
            long d = player.getDuration();
            if (isDurationValid(d)) {
                duration = (int) d;
                durationMs = duration;
            }
        }
        if (preparing) {
            tvDuration.setText(bufferPercent > 0 ? ("缓冲中 " + bufferPercent + "%") : "加载中...");
            return;
        }
        if (duration <= 0) {
            tvDuration.setText(buffering ? ("缓冲中 " + bufferPercent + "%") : "00:00 / --:--");
            return;
        }
        String time = formatTime((int) (duration * clamp01(percent))) + " / " + formatTime(duration);
        tvDuration.setText(buffering ? (time + "  缓冲" + bufferPercent + "%") : time);
    }

    private void showIdle() {
        buffering = false;
        pbLoading.animate().cancel();
        ivPlay.setVisibility(VISIBLE);
        ivPause.setVisibility(GONE);
        pbLoading.setVisibility(GONE);
        setWaveSeekEnabled(true);
    }

    private void showPlaying() {
        buffering = false;
        pbLoading.animate().cancel();
        ivPlay.setVisibility(GONE);
        ivPause.setVisibility(VISIBLE);
        pbLoading.setVisibility(GONE);
        setWaveSeekEnabled(true);
    }

    private void showLoading() {
        ivPlay.setVisibility(INVISIBLE);
        ivPause.setVisibility(GONE);
        pbLoading.animate().cancel();
        pbLoading.setAlpha(0f);
        pbLoading.setVisibility(VISIBLE);
        pbLoading.animate().alpha(1f).setDuration(160).start();
        setWaveSeekEnabled(false);
        refreshTimeText(waveformSeekBar.getProgressPercent());
    }

    private void showBuffering() {
        buffering = true;
        ivPlay.setVisibility(GONE);
        ivPause.setVisibility(INVISIBLE);
        pbLoading.animate().cancel();
        pbLoading.setAlpha(1f);
        pbLoading.setVisibility(VISIBLE);
        setWaveSeekEnabled(true);
    }

    private void setWaveSeekEnabled(boolean enabled) {
        if (waveformSeekBar != null) {
            waveformSeekBar.setSeekEnabled(enabled);
        }
    }

    private void releasePlayer() {
        playWhenReady = false;
        pendingSeekPercent = -1f;
        seekHoldPercent = -1f;
        buffering = false;
        clearHandlerTasks();
        abortDurationFetch();
        releasePlayerOnly();
        prepared = false;
        preparing = false;
        bufferPercent = 0;
        if (waveformSeekBar != null) {
            waveformSeekBar.setBufferPercent(0f);
        }
    }

    private void releasePlayerOnly() {
        if (player == null) {
            return;
        }
        try {
            player.removeListener(playerListener);
            player.release();
        } catch (Exception e) {
            Log.e(TAG, "release: " + e.getMessage());
        }
        player = null;
    }

    private static boolean isDurationValid(long duration) {
        return duration > 0 && duration != C.TIME_UNSET;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static String stateName(int state) {
        switch (state) {
            case Player.STATE_IDLE:
                return "IDLE";
            case Player.STATE_BUFFERING:
                return "BUFFERING";
            case Player.STATE_READY:
                return "READY";
            case Player.STATE_ENDED:
                return "ENDED";
            default:
                return String.valueOf(state);
        }
    }

    private static String formatTime(int millis) {
        int totalSec = Math.max(0, millis / 1000);
        return String.format("%02d:%02d", totalSec / 60, totalSec % 60);
    }

    // —— 外观 setter（与 pav_* 对应）——

    public void setAudioBackground(@Nullable Drawable background) {
        styleBackground = background;
        applyAppearance();
    }

    public void setAudioBackground(@DrawableRes int resId) {
        styleBackground = ContextCompat.getDrawable(getContext(), resId);
        applyAppearance();
    }

    public void setAudioBgColor(@ColorInt int color) {
        styleBackground = null;
        styleBgColor = color;
        applyAppearance();
    }

    public void setAudioCornerRadius(int px) {
        styleCornerRadius = Math.max(0, px);
        applyAppearance();
    }

    public void setAudioPadding(int startPx, int topPx, int endPx, int bottomPx) {
        stylePadStart = Math.max(0, startPx);
        stylePadTop = Math.max(0, topPx);
        stylePadEnd = Math.max(0, endPx);
        stylePadBottom = Math.max(0, bottomPx);
        applyAppearance();
    }

    public void setPlayBtnSize(int px) {
        stylePlayBtnSize = Math.max(0, px);
        applyAppearance();
    }

    public void setPlayBtnBackground(@Nullable Drawable background) {
        stylePlayBtnBackground = background;
        applyAppearance();
    }

    public void setPlayBtnColor(@ColorInt int color) {
        stylePlayBtnBackground = null;
        stylePlayBtnColor = color;
        applyAppearance();
    }

    public void setPlayIcon(@Nullable Drawable drawable) {
        stylePlayIcon = drawable;
        applyAppearance();
    }

    public void setPlayIcon(@DrawableRes int resId) {
        stylePlayIcon = ContextCompat.getDrawable(getContext(), resId);
        applyAppearance();
    }

    public void setPauseIcon(@Nullable Drawable drawable) {
        stylePauseIcon = drawable;
        applyAppearance();
    }

    public void setPauseIcon(@DrawableRes int resId) {
        stylePauseIcon = ContextCompat.getDrawable(getContext(), resId);
        applyAppearance();
    }

    public void setPlayIconPadding(int px) {
        stylePlayIconPadding = Math.max(0, px);
        applyAppearance();
    }

    public void setLoadingTint(@ColorInt int color) {
        styleLoadingTint = color;
        applyAppearance();
    }

    public void setWaveAreaMarginStart(int px) {
        styleWaveAreaMarginStart = Math.max(0, px);
        applyAppearance();
    }

    public void setWaveHeight(int px) {
        styleWaveHeight = Math.max(0, px);
        applyAppearance();
    }

    public void setWaveMarginTop(int px) {
        styleWaveMarginTop = Math.max(0, px);
        applyAppearance();
    }

    public void setWaveColors(@ColorInt int played, @ColorInt int buffer, @ColorInt int remain) {
        styleWavePlayedColor = played;
        styleWaveBufferColor = buffer;
        styleWaveRemainColor = remain;
        applyAppearance();
    }

    public void setWaveMinBarHeight(int px) {
        styleWaveMinBarHeight = Math.max(0, px);
        applyAppearance();
    }

    public void setWaveBarCornerRadius(int px) {
        styleWaveBarCornerRadius = Math.max(0, px);
        applyAppearance();
    }

    public void setWaveBarWidthRatio(float ratio) {
        styleWaveBarWidthRatio = ratio;
        applyAppearance();
    }

    public void setDurationTextSizePx(int px) {
        styleDurationTextSize = Math.max(0, px);
        applyAppearance();
    }

    public void setDurationTextColor(@ColorInt int color) {
        styleDurationTextColor = color;
        applyAppearance();
    }

    /** @param gravity 0 start / 1 end / 2 center */
    public void setDurationGravity(int gravity) {
        styleDurationGravity = gravity;
        applyAppearance();
    }

    public WaveformSeekBar getWaveformSeekBar() {
        return waveformSeekBar;
    }

    public TextView getDurationView() {
        return tvDuration;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }
}
