package com.lib.base.ui.widget.audio;

import android.text.TextUtils;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * 用「音频 url + 固定盐」确定性生成波形，同一 url 结果不变，无需拉取音频字节。
 */
public final class WaveformGenerator {

    /** 固定长度盐，参与 hash，保证同 url 波形一致 */
    private static final String WAVEFORM_SALT = "post_audio_wave_v2_jagged_32chars!";

    private static final int DEFAULT_BAR_COUNT = 120;
    private static final int SAMPLE_BITS = 5;
    private static final int SAMPLE_MAX = (1 << SAMPLE_BITS) - 1;

    private WaveformGenerator() {
    }

    public static float[] generate(String audioUrl) {
        return generate(audioUrl, DEFAULT_BAR_COUNT);
    }

    /**
     * @return 每根柱高度，范围 [0, 1]，起伏偏剧烈、少平滑
     */
    public static float[] generate(String audioUrl, int barCount) {
        int count = Math.max(8, barCount);
        float[] bars = new float[count];
        long seed = seedOf(audioUrl);
        Random random = new Random(seed);

        // 高低频叠加：低频定段落，高频制造相邻柱落差
        float f1 = 0.12f + random.nextFloat() * 0.18f;
        float f2 = 0.55f + random.nextFloat() * 0.45f;
        float f3 = 1.2f + random.nextFloat() * 0.9f;
        float f4 = 2.4f + random.nextFloat() * 1.6f;
        float p1 = random.nextFloat() * (float) (Math.PI * 2);
        float p2 = random.nextFloat() * (float) (Math.PI * 2);
        float p3 = random.nextFloat() * (float) (Math.PI * 2);
        float p4 = random.nextFloat() * (float) (Math.PI * 2);

        for (int i = 0; i < count; i++) {
            float t = i / (float) (count - 1);
            // 头尾略收，中间可打满
            float envelope = (float) (0.55 + 0.45 * Math.sin(Math.PI * t));

            float base = 0.28f * absSin(f1 * i + p1)
                    + 0.30f * absSin(f2 * i + p2)
                    + 0.22f * absSin(f3 * i + p3)
                    + 0.20f * absSin(f4 * i + p4);

            // 强噪声：柱间落差更明显
            float noise = (random.nextFloat() - 0.5f) * 0.55f;
            // 高频尖峰 / 深谷
            float spike = 0f;
            float r = random.nextFloat();
            if (r < 0.18f) {
                spike = 0.35f + random.nextFloat() * 0.55f;
            } else if (r < 0.32f) {
                spike = -(0.25f + random.nextFloat() * 0.35f);
            }

            // 平方拉伸，压中间、拉高低，视觉更「炸」
            float raw = clamp((base + noise + spike) * envelope, 0f, 1f);
            float shaped = (float) Math.pow(raw, 0.72);
            bars[i] = clamp(shaped, 0.06f, 1f);
        }
        // 不做邻柱平滑，保留锯齿起伏
        return bars;
    }

    /**
     * 兼容旧版 5bit 打包格式（若需对接旧 SeekBar 绘制逻辑）。
     */
    public static byte[] generatePackedBytes(String audioUrl, int barCount) {
        float[] bars = generate(audioUrl, barCount);
        int bitLen = bars.length * SAMPLE_BITS;
        byte[] out = new byte[(bitLen + 7) / 8 + 1];
        for (int i = 0; i < bars.length; i++) {
            int sample = Math.round(bars[i] * SAMPLE_MAX);
            sample = Math.max(1, Math.min(SAMPLE_MAX, sample));
            writeBits(out, i * SAMPLE_BITS, SAMPLE_BITS, sample);
        }
        return out;
    }

    private static long seedOf(String audioUrl) {
        String key = (TextUtils.isEmpty(audioUrl) ? "" : audioUrl) + WAVEFORM_SALT;
        // FNV-1a 64，同输入永远同 seed
        long hash = 0xcbf29ce484222325L;
        byte[] data = key.getBytes(StandardCharsets.UTF_8);
        for (byte b : data) {
            hash ^= (b & 0xffL);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private static float absSin(float x) {
        return Math.abs((float) Math.sin(x));
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static void writeBits(byte[] dest, int bitOffset, int bitCount, int value) {
        for (int i = 0; i < bitCount; i++) {
            int bit = (value >> (bitCount - 1 - i)) & 1;
            int pos = bitOffset + i;
            int byteIndex = pos / 8;
            int bitIndex = pos % 8;
            if (bit == 1) {
                dest[byteIndex] |= (byte) (1 << bitIndex);
            }
        }
    }
}
