package com.templete.project.ui.widget.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
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

import com.lib.base.util.GlobalThreadPoolUtil;
import com.templete.project.R;

import java.util.HashMap;

/**
 * 帖子音频组件：URL 确定性波形 + 全局互斥播放。
 */
public class PostAudioView extends FrameLayout {

    private static final String TAG = "PostAudioView";
    private static final int MAX_PREPARE_RETRY = 1;
    /** prepareAsync 超时兜底，避免一直转圈 */
    private static final long PREPARE_TIMEOUT_MS = 15_000L;

    private ImageView ivPlay;
    private ImageView ivPause;
    private ProgressBar pbLoading;
    private WaveformSeekBar waveformSeekBar;
    private TextView tvDuration;

    private MediaPlayer mediaPlayer;
    private String audioUrl;
    private boolean prepared;
    private boolean preparing;
    private boolean buffering;
    private boolean seekDragging;
    private int durationMs;
    /** 0~100，MediaPlayer 缓冲百分比 */
    private int bufferPercent;
    private float pendingSeekPercent = -1f;
    private boolean playAfterPrepare;
    private int prepareRetry;
    private int prepareToken;
    /** 每次暂停/取消播放 +1，作废未完成的 seekComplete / start */
    private int playSession;
    private int durationToken;
    private MediaMetadataRetriever durationRetriever;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable progressTicker = new Runnable() {
        @Override
        public void run() {
            updateProgressUi();
            if (isPlaying()) {
                handler.postDelayed(this, 50);
            }
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
        ivPlay = findViewById(R.id.ivPlay);
        ivPause = findViewById(R.id.ivPause);
        pbLoading = findViewById(R.id.pbLoading);
        waveformSeekBar = findViewById(R.id.waveformSeekBar);
        tvDuration = findViewById(R.id.tvDuration);

        ivPlay.setOnClickListener(v -> play());
        ivPause.setOnClickListener(v -> pause());
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
            }
        });
        showIdle();
    }

    /**
     * 绑定音频地址：立即生成波形，并异步拉时长（与播放互斥，避免抢连接）。
     */
    public void setAudioUrl(String url) {
        if (TextUtils.equals(audioUrl, url) && (prepared || preparing)) {
            return;
        }
        Log.d(TAG, "setAudioUrl=" + url);
        // 换绑前若自己是当前播放者，先从 Manager 摘掉，避免野指针
        if (isCurrentPlayer()) {
            AudioPlayManager.get().onStop(this);
        }
        releasePlayer();
        audioUrl = url;
        prepared = false;
        preparing = false;
        durationMs = 0;
        bufferPercent = 0;
        pendingSeekPercent = -1f;
        playAfterPrepare = false;
        prepareRetry = 0;
        buffering = false;
        seekDragging = false;
        waveformSeekBar.setWaveFromUrl(url == null ? "" : url);
        waveformSeekBar.setProgressPercent(0f);
        waveformSeekBar.setBufferPercent(0f);
        tvDuration.setText("加载中...");
        showIdle();
        // 初始化拉时长期间禁止拖波形；播放按钮仍可点
        setWaveSeekEnabled(false);
        fetchDuration(url);
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void play() {
        // 播放键：从当前位置续播，不带拖动 seek
        playFrom(-1f);
    }

    private void playFrom(float seekPercent) {
        if (TextUtils.isEmpty(audioUrl) || !isAttachedToWindow()) {
            return;
        }
        // 已在播且只是点播放键：忽略，避免快速连点反复 start
        if (seekPercent < 0f && isPlaying() && isCurrentPlayer()) {
            Log.d(TAG, "playFrom ignore already playing");
            return;
        }
        Log.d(TAG, "playFrom seek=" + seekPercent
                + " prepared=" + prepared + " preparing=" + preparing + " url=" + audioUrl);
        AudioPlayManager.get().requestPlay(this);
        if (seekPercent >= 0f) {
            pendingSeekPercent = seekPercent;
        } else {
            pendingSeekPercent = -1f;
        }
        if (prepared && mediaPlayer != null) {
            seekThenStart();
            return;
        }
        if (preparing) {
            playAfterPrepare = true;
            return;
        }
        playAfterPrepare = true;
        prepareRetry = 0;
        preparePlayer();
    }

    private void seekAndPlay(float percent) {
        if (!isAttachedToWindow()) {
            return;
        }
        pendingSeekPercent = percent;
        refreshTimeText(percent);
        playFrom(percent);
    }

    public void pause() {
        // 加载中用户点停：直接取消 prepare，释放网络
        if (preparing) {
            Log.d(TAG, "pause cancel preparing");
            abortPreparing(true);
            return;
        }
        pauseInternal(true);
    }

    void pauseByManager() {
        Log.d(TAG, "pauseByManager preparing=" + preparing + " url=" + audioUrl);
        if (preparing) {
            abortPreparing(false);
            return;
        }
        pauseInternal(false);
    }

    /** 取消进行中的 prepare，回到空闲 */
    private void abortPreparing(boolean notifyManager) {
        cancelPendingStart();
        preparing = false;
        prepareToken++;
        handler.removeCallbacks(prepareTimeout);
        releasePlayerOnly();
        prepared = false;
        if (notifyManager) {
            AudioPlayManager.get().onStop(this);
        }
        showIdle();
    }

    /** 取消未完成的 start/seek 意图，不释放已 prepare 的播放器 */
    private void cancelPendingStart() {
        playSession++;
        playAfterPrepare = false;
        pendingSeekPercent = -1f;
        buffering = false;
        handler.removeCallbacks(progressTicker);
        handler.removeCallbacks(prepareTimeout);
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setOnSeekCompleteListener(null);
            } catch (Exception ignored) {
            }
        }
        Log.d(TAG, "cancelPendingStart session=" + playSession);
    }

    private void pauseInternal(boolean notifyManager) {
        cancelPendingStart();
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (Exception e) {
            Log.e(TAG, "pause error: " + e.getMessage());
        }
        if (notifyManager) {
            AudioPlayManager.get().onStop(this);
        }
        showIdle();
    }

    private final Runnable prepareTimeout = () -> {
        if (!preparing) {
            return;
        }
        Log.e(TAG, "prepare timeout url=" + audioUrl);
        playSession++;
        preparing = false;
        playAfterPrepare = false;
        pendingSeekPercent = -1f;
        buffering = false;
        prepareToken++;
        releasePlayerOnly();
        prepared = false;
        showIdle();
        if (isCurrentPlayer()) {
            AudioPlayManager.get().onStop(this);
            Toast.makeText(getContext(), "音频加载超时", Toast.LENGTH_SHORT).show();
        }
    };

    private boolean isCurrentPlayer() {
        return AudioPlayManager.get().getCurrent() == this;
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public void release() {
        handler.removeCallbacksAndMessages(null);
        if (pbLoading != null) {
            pbLoading.animate().cancel();
        }
        pauseInternal(true);
        releasePlayer();
        AudioPlayManager.get().onStop(this);
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
                Log.d(TAG, "fetchDuration start token=" + token + " url=" + target);
                if (target.startsWith("http")) {
                    retriever.setDataSource(target, new HashMap<>());
                } else {
                    retriever.setDataSource(target);
                }
                String value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (!TextUtils.isEmpty(value)) {
                    ms = Integer.parseInt(value);
                }
                Log.d(TAG, "fetchDuration ok token=" + token + " duration=" + ms);
            } catch (Exception e) {
                Log.e(TAG, "fetchDuration fail token=" + token + " msg=" + e.getMessage());
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
                if (duration > 0 && !(prepared && mediaPlayer != null)) {
                    durationMs = duration;
                    if (!seekDragging) {
                        refreshTimeText(waveformSeekBar.getProgressPercent());
                    }
                } else if (durationMs <= 0) {
                    tvDuration.setText("00:00 / --:--");
                }
                // 时长拉完且未在 prepare，恢复波形拖动
                if (!preparing) {
                    setWaveSeekEnabled(true);
                }
            });
        });
    }

    private synchronized void abortDurationFetchLocked() {
        if (durationRetriever != null) {
            Log.d(TAG, "abortDurationFetch");
            try {
                durationRetriever.release();
            } catch (Exception e) {
                Log.e(TAG, "abortDurationFetch error: " + e.getMessage());
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

    private void preparePlayer() {
        if (TextUtils.isEmpty(audioUrl)) {
            return;
        }
        // 先掐掉拉时长，避免和 MediaPlayer 抢同一 url 连接（偶发 what=1 extra=-1004）
        abortDurationFetch();

        preparing = true;
        final int token = ++prepareToken;
        final String targetUrl = audioUrl;
        showLoading();
        releasePlayerOnly();
        mediaPlayer = new MediaPlayer();
        Log.d(TAG, "prepareAsync token=" + token + " playAfter=" + playAfterPrepare
                + " url=" + targetUrl);
        handler.removeCallbacks(prepareTimeout);
        handler.postDelayed(prepareTimeout, PREPARE_TIMEOUT_MS);
        try {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            mediaPlayer.setDataSource(targetUrl);
            mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                if (token != prepareToken) {
                    return;
                }
                bufferPercent = Math.max(0, Math.min(100, percent));
                waveformSeekBar.setBufferPercent(bufferPercent / 100f);
                // prepare / 播放卡顿时都刷新文案
                if (preparing || buffering) {
                    refreshTimeText(waveformSeekBar.getProgressPercent());
                }
            });
            mediaPlayer.setOnInfoListener((mp, what, extra) -> {
                if (token != prepareToken || !isCurrentPlayer()) {
                    return false;
                }
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    Log.d(TAG, "buffering start");
                    buffering = true;
                    if (isPlaying()) {
                        showBuffering();
                    }
                    refreshTimeText(waveformSeekBar.getProgressPercent());
                    return true;
                }
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    Log.d(TAG, "buffering end percent=" + bufferPercent);
                    buffering = false;
                    if (isPlaying() && isCurrentPlayer()) {
                        showPlaying();
                    }
                    refreshTimeText(waveformSeekBar.getProgressPercent());
                    return true;
                }
                return false;
            });
            mediaPlayer.setOnPreparedListener(mp -> {
                if (token != prepareToken || !TextUtils.equals(audioUrl, targetUrl)) {
                    Log.d(TAG, "onPrepared ignore stale token=" + token);
                    return;
                }
                handler.removeCallbacks(prepareTimeout);
                preparing = false;
                prepared = true;
                prepareRetry = 0;
                durationMs = mp.getDuration();
                Log.d(TAG, "onPrepared ok duration=" + durationMs
                        + " playAfter=" + playAfterPrepare + " current=" + isCurrentPlayer());
                refreshTimeText(waveformSeekBar.getProgressPercent());
                if (playAfterPrepare && isCurrentPlayer() && isAttachedToWindow()) {
                    playAfterPrepare = false;
                    seekThenStart();
                } else {
                    // 已被其它条目抢播 / 取消 / 已 detach
                    playAfterPrepare = false;
                    buffering = false;
                    showIdle();
                }
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (token != prepareToken) {
                    return;
                }
                handler.removeCallbacks(progressTicker);
                buffering = false;
                pendingSeekPercent = -1f;
                bufferPercent = 0;
                waveformSeekBar.setProgressPercent(0f);
                waveformSeekBar.setBufferPercent(0f);
                try {
                    mp.seekTo(0);
                } catch (Exception ignored) {
                }
                AudioPlayManager.get().onStop(this);
                refreshTimeText(0f);
                showIdle();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                if (token != prepareToken) {
                    Log.d(TAG, "onError ignore stale token=" + token);
                    return true;
                }
                // what: 1=MEDIA_ERROR_UNKNOWN, 100=SERVER_DIED
                // extra: -1004=IO, -1007=MALFORMED, -1010=UNSUPPORTED, -110=TIMED_OUT
                Log.e(TAG, "onError what=" + what + " extra=" + extra
                        + " retry=" + prepareRetry + " url=" + targetUrl);
                handler.removeCallbacks(prepareTimeout);
                preparing = false;
                buffering = false;
                prepared = false;
                releasePlayerOnly();

                if (prepareRetry < MAX_PREPARE_RETRY
                        && TextUtils.equals(audioUrl, targetUrl)
                        && isAttachedToWindow()) {
                    prepareRetry++;
                    // 错误时 playAfterPrepare 可能已被清空，以是否仍是当前播放者为准
                    final boolean needPlay = isCurrentPlayer();
                    Log.d(TAG, "auto retry prepare #" + prepareRetry + " needPlay=" + needPlay);
                    handler.postDelayed(() -> {
                        if (token != prepareToken || !TextUtils.equals(audioUrl, targetUrl)) {
                            return;
                        }
                        if (!isAttachedToWindow() || prepared || preparing) {
                            return;
                        }
                        // 已非当前且不需要播：不再重试，避免后台空转
                        if (!needPlay && !isCurrentPlayer()) {
                            showIdle();
                            return;
                        }
                        playAfterPrepare = needPlay || isCurrentPlayer();
                        preparePlayer();
                    }, 350);
                    return true;
                }

                playAfterPrepare = false;
                showIdle();
                if (isCurrentPlayer()) {
                    Toast.makeText(getContext(), "音频播放失败", Toast.LENGTH_SHORT).show();
                    AudioPlayManager.get().onStop(this);
                }
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "prepare exception: " + e.getMessage() + " url=" + targetUrl);
            handler.removeCallbacks(prepareTimeout);
            preparing = false;
            prepared = false;
            playAfterPrepare = false;
            releasePlayerOnly();
            showIdle();
            if (isCurrentPlayer()) {
                Toast.makeText(getContext(), "音频加载失败", Toast.LENGTH_SHORT).show();
                AudioPlayManager.get().onStop(this);
            }
        }
    }

    /**
     * 先 start 再异步 seek。网络流「先 seek 再等 complete」容易长时间转圈甚至 onError。
     */
    private void seekThenStart() {
        if (mediaPlayer == null || !prepared || !isCurrentPlayer() || !isAttachedToWindow()) {
            showIdle();
            return;
        }
        final int session = playSession;
        final float seek = pendingSeekPercent;
        pendingSeekPercent = -1f;

        startPlayback(session);
        if (session != playSession || mediaPlayer == null || !isCurrentPlayer()) {
            return;
        }
        if (seek < 0f) {
            return;
        }
        int duration = mediaPlayer.getDuration();
        if (duration <= 0) {
            return;
        }
        final int position = (int) (duration * seek);
        if (position <= 120) {
            return;
        }
        waveformSeekBar.setProgressPercent(seek);
        Log.d(TAG, "seekAfterStart pos=" + position + "/" + duration + " session=" + session);
        try {
            // 不阻塞 UI；seek 失败也不影响已 start 的播放
            mediaPlayer.seekTo(position);
        } catch (Exception e) {
            Log.e(TAG, "seekTo error: " + e.getMessage());
        }
    }

    private void startPlayback(int session) {
        if (session != playSession || !isCurrentPlayer() || !isAttachedToWindow()) {
            Log.d(TAG, "startPlayback ignored stale session=" + session);
            return;
        }
        try {
            if (mediaPlayer == null) {
                return;
            }
            mediaPlayer.start();
            Log.d(TAG, "start ok session=" + session);
            buffering = false;
            showPlaying();
            handler.removeCallbacks(progressTicker);
            handler.post(progressTicker);
        } catch (Exception e) {
            Log.e(TAG, "start error: " + e.getMessage());
            buffering = false;
            showIdle();
            if (isCurrentPlayer()) {
                AudioPlayManager.get().onStop(this);
            }
        }
    }

    private void updateProgressUi() {
        if (seekDragging || mediaPlayer == null || !prepared) {
            return;
        }
        int duration = mediaPlayer.getDuration();
        int position = mediaPlayer.getCurrentPosition();
        if (duration <= 0) {
            return;
        }
        durationMs = duration;
        waveformSeekBar.setProgressPercent(position * 1f / duration);
        refreshTimeText(position * 1f / duration);
    }

    private void refreshTimeText(float percent) {
        int duration = durationMs;
        if (prepared && mediaPlayer != null) {
            try {
                int d = mediaPlayer.getDuration();
                if (d > 0) {
                    duration = d;
                    durationMs = d;
                }
            } catch (Exception ignored) {
            }
        }
        if (preparing) {
            if (bufferPercent > 0) {
                tvDuration.setText("缓冲中 " + bufferPercent + "%");
            } else {
                tvDuration.setText("加载中...");
            }
            return;
        }
        if (duration <= 0) {
            tvDuration.setText(buffering ? ("缓冲中 " + Math.max(bufferPercent, 0) + "%") : "00:00 / --:--");
            return;
        }
        float p = Math.max(0f, Math.min(1f, percent));
        int position = (int) (duration * p);
        String time = formatTime(position) + " / " + formatTime(duration);
        if (buffering) {
            tvDuration.setText(time + "  缓冲" + bufferPercent + "%");
        } else {
            tvDuration.setText(time);
        }
    }

    private void showIdle() {
        buffering = false;
        pbLoading.animate().cancel();
        ivPlay.setVisibility(View.VISIBLE);
        ivPlay.setAlpha(1f);
        ivPause.setVisibility(View.GONE);
        pbLoading.setVisibility(View.GONE);
        pbLoading.setAlpha(1f);
        setWaveSeekEnabled(true);
    }

    private void showPlaying() {
        buffering = false;
        pbLoading.animate().cancel();
        ivPlay.setVisibility(View.GONE);
        ivPause.setVisibility(View.VISIBLE);
        ivPause.setAlpha(1f);
        pbLoading.setVisibility(View.GONE);
        setWaveSeekEnabled(true);
    }

    private void showLoading() {
        // 圆底始终在 flPlayBtn 上；图标 INVISIBLE 占位，转圈淡入，避免空白闪一下
        ivPlay.setVisibility(View.INVISIBLE);
        ivPause.setVisibility(View.GONE);
        pbLoading.animate().cancel();
        pbLoading.setVisibility(View.VISIBLE);
        pbLoading.setAlpha(0f);
        pbLoading.animate().alpha(1f).setDuration(160).start();
        setWaveSeekEnabled(false);
        refreshTimeText(waveformSeekBar.getProgressPercent());
    }

    /** 播放过程中缓冲：转圈 + 保留可点暂停区域逻辑（转圈盖住） */
    private void showBuffering() {
        ivPlay.setVisibility(View.GONE);
        ivPause.setVisibility(View.INVISIBLE);
        pbLoading.animate().cancel();
        pbLoading.setVisibility(View.VISIBLE);
        pbLoading.setAlpha(1f);
        // 缓冲时仍允许拖进度换位置
        setWaveSeekEnabled(true);
    }

    /** 加载中禁用波形拖动 */
    private void setWaveSeekEnabled(boolean enabled) {
        if (waveformSeekBar != null) {
            waveformSeekBar.setSeekEnabled(enabled);
        }
    }

    private void releasePlayer() {
        cancelPendingStart();
        abortDurationFetch();
        prepareToken++;
        releasePlayerOnly();
        prepared = false;
        preparing = false;
        buffering = false;
        bufferPercent = 0;
        prepareRetry = 0;
        if (waveformSeekBar != null) {
            waveformSeekBar.setBufferPercent(0f);
        }
    }

    private void releasePlayerOnly() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setOnPreparedListener(null);
                mediaPlayer.setOnCompletionListener(null);
                mediaPlayer.setOnErrorListener(null);
                mediaPlayer.setOnBufferingUpdateListener(null);
                mediaPlayer.setOnInfoListener(null);
                mediaPlayer.setOnSeekCompleteListener(null);
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "release error: " + e.getMessage());
            }
            mediaPlayer = null;
        }
    }

    private static String formatTime(int millis) {
        int totalSec = Math.max(0, millis / 1000);
        int m = totalSec / 60;
        int s = totalSec % 60;
        return String.format("%02d:%02d", m, s);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }
}
