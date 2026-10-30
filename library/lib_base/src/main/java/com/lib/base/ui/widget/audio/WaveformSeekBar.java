package com.lib.base.ui.widget.audio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lib.base.R;

/**
 * 帖子音频波形进度条：柱状波形 + 拖动 seek。
 */
public class WaveformSeekBar extends View {

    public interface OnSeekListener {
        void onSeekStart();

        void onSeekChanged(float percent);

        void onSeekStop(float percent);

        /** 手势被打断（如列表滑动），不应触发播放 */
        void onSeekCancel();
    }

    /** 每根柱占用宽度（含间隙），约 2.5dp，保证一屏 100+ 根 */
    private static final float BAR_SLOT_DP = 2.5f;
    private static final int MIN_BARS = 64;
    private static final int MAX_BARS = 220;

    private float[] amplitudes;
    private String boundUrl;
    private float progress; // 0~1 播放进度
    private float bufferProgress; // 0~1 缓冲进度
    private boolean tracking;
    /** 加载中禁止拖动 seek */
    private boolean seekEnabled = true;

    private final Paint playedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bufferPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint remainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private float minBarHeightPx;
    private float cornerRadiusPx;
    /** 柱宽占槽宽比例，默认 0.62 */
    private float barWidthRatio = 0.62f;

    private OnSeekListener seekListener;

    public WaveformSeekBar(Context context) {
        super(context);
        init();
    }

    public WaveformSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveformSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        minBarHeightPx = 3f * density;
        cornerRadiusPx = 0.8f * density;

        playedPaint.setStyle(Paint.Style.FILL);
        bufferPaint.setStyle(Paint.Style.FILL);
        remainPaint.setStyle(Paint.Style.FILL);
        playedPaint.setColor(ContextCompat.getColor(getContext(), R.color.common_accent_color));
        // 已缓冲未播放：半透明主题色
        bufferPaint.setColor(0x805A8DDF);
        remainPaint.setColor(ContextCompat.getColor(getContext(), R.color.cl_ccc));

        setClickable(true);
        setFocusable(true);
    }

    public void setColors(int playedColor, int remainColor) {
        setColors(playedColor, bufferPaint.getColor(), remainColor);
    }

    public void setColors(int playedColor, int bufferColor, int remainColor) {
        playedPaint.setColor(playedColor);
        bufferPaint.setColor(bufferColor);
        remainPaint.setColor(remainColor);
        invalidate();
    }

    public void setPlayedColor(int color) {
        playedPaint.setColor(color);
        invalidate();
    }

    public void setBufferColor(int color) {
        bufferPaint.setColor(color);
        invalidate();
    }

    public void setRemainColor(int color) {
        remainPaint.setColor(color);
        invalidate();
    }

    public void setMinBarHeightPx(float px) {
        minBarHeightPx = Math.max(0f, px);
        invalidate();
    }

    public void setBarCornerRadiusPx(float px) {
        cornerRadiusPx = Math.max(0f, px);
        invalidate();
    }

    public void setBarWidthRatio(float ratio) {
        barWidthRatio = Math.max(0.05f, Math.min(1f, ratio));
        invalidate();
    }

    public void setBufferPercent(float percent) {
        float next = clamp01(percent);
        if (Math.abs(next - bufferProgress) < 0.005f) {
            return;
        }
        bufferProgress = next;
        invalidate();
    }

    public void setAmplitudes(float[] amplitudes) {
        this.amplitudes = amplitudes;
        invalidate();
    }

    public void setWaveFromUrl(String audioUrl) {
        boundUrl = audioUrl == null ? "" : audioUrl;
        rebuildWave();
    }

    public void setProgressPercent(float percent) {
        if (tracking) {
            return;
        }
        float next = clamp01(percent);
        // 进度变化太小时不重绘，避免无意义 invalidate；但阈值足够密时仍要跟手
        if (Math.abs(next - progress) < 0.001f) {
            return;
        }
        progress = next;
        invalidate();
    }

    public float getProgressPercent() {
        return progress;
    }

    public void setOnSeekListener(OnSeekListener listener) {
        this.seekListener = listener;
    }

    public void setSeekEnabled(boolean enabled) {
        this.seekEnabled = enabled;
        if (!enabled && tracking) {
            tracking = false;
            getParent().requestDisallowInterceptTouchEvent(false);
            if (seekListener != null) {
                seekListener.onSeekCancel();
            }
        }
        setAlpha(enabled ? 1f : 0.55f);
    }

    public boolean isSeekEnabled() {
        return seekEnabled;
    }

    private void rebuildWave() {
        String url = boundUrl == null ? "placeholder" : boundUrl;
        setAmplitudes(WaveformGenerator.generate(url, suggestBarCount()));
    }

    private int suggestBarCount() {
        float density = getResources().getDisplayMetrics().density;
        int w = getWidth() > 0
                ? getWidth()
                : (int) (280 * density);
        int count = (int) (w / (BAR_SLOT_DP * density));
        return Math.max(MIN_BARS, Math.min(MAX_BARS, count));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && w != oldw) {
            // 宽度就绪/变化后按真实宽度重建，保证密度铺满
            rebuildWave();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (amplitudes == null || amplitudes.length == 0 || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        int count = amplitudes.length;
        // 铺满整宽：进度与柱一一对应，长音频也能看到细腻推进
        float slot = getWidth() * 1f / count;
        float barW = Math.max(1f, slot * barWidthRatio);
        float centerY = getHeight() / 2f;
        float maxH = getHeight() * 0.92f;
        float playedX = getWidth() * progress;
        float bufferX = getWidth() * Math.max(progress, bufferProgress);

        for (int i = 0; i < count; i++) {
            float h = Math.max(minBarHeightPx, amplitudes[i] * maxH);
            float left = i * slot + (slot - barW) / 2f;
            float top = centerY - h / 2f;
            float right = left + barW;
            float bottom = centerY + h / 2f;
            barRect.set(left, top, right, bottom);
            float mid = left + barW / 2f;
            Paint paint;
            if (mid <= playedX) {
                paint = playedPaint;
            } else if (mid <= bufferX) {
                paint = bufferPaint;
            } else {
                paint = remainPaint;
            }
            canvas.drawRoundRect(barRect, cornerRadiusPx, cornerRadiusPx, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !seekEnabled) {
            return false;
        }
        float percent = clamp01(event.getX() / Math.max(1, getWidth()));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tracking = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                progress = percent;
                invalidate();
                if (seekListener != null) {
                    seekListener.onSeekStart();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                progress = percent;
                invalidate();
                if (seekListener != null) {
                    seekListener.onSeekChanged(progress);
                }
                return true;
            case MotionEvent.ACTION_UP:
                tracking = false;
                progress = percent;
                invalidate();
                getParent().requestDisallowInterceptTouchEvent(false);
                if (seekListener != null) {
                    seekListener.onSeekStop(progress);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                // 列表滑动打断手势：只结束拖动，不触发播放，避免误串点
                tracking = false;
                invalidate();
                getParent().requestDisallowInterceptTouchEvent(false);
                if (seekListener != null) {
                    seekListener.onSeekCancel();
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private static float clamp01(float v) {
        if (v < 0f) {
            return 0f;
        }
        if (v > 1f) {
            return 1f;
        }
        return v;
    }
}
