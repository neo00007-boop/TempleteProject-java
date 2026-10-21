package com.lib.base.ui.widget.audio;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
 */
public class PostAudioView extends FrameLayout {

    private static final String TAG = "PostAudioView";
    private static final long PREPARE_TIMEOUT_MS = 15_000L;
    /** 同一条波形连点不同进度：合并为最后一次 seek，减轻 Exo 压力 */
    private static final long SEEK_COALESCE_MS = 80L;
    /** seek 后进度条按住目标，避免 ticker 短暂拉回旧位置 */
    private static final long SEEK_HOLD_MS = 900L;

    private View flPlayBtn;
    private ImageView ivPlay;
    private ImageView ivPause;
    private ProgressBar pbLoading;
    private WaveformSeekBar waveformSeekBar;
    private TextView tvDuration;

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
        super(context);
        init(context);
    }

    public PostAudioView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PostAudioView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_post_audio, this, true);
        flPlayBtn = findViewById(R.id.flPlayBtn);
        ivPlay = findViewById(R.id.ivPlay);
        ivPause = findViewById(R.id.ivPause);
        pbLoading = findViewById(R.id.pbLoading);
        waveformSeekBar = findViewById(R.id.waveformSeekBar);
        tvDuration = findViewById(R.id.tvDuration);

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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }
}
