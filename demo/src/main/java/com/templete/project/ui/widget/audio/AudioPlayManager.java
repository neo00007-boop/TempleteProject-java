package com.templete.project.ui.widget.audio;

import java.lang.ref.WeakReference;

/**
 * 全局音频互斥：同一时刻只允许一个 {@link PostAudioView} 播放。
 */
public final class AudioPlayManager {

    private static volatile AudioPlayManager sInstance;

    private WeakReference<PostAudioView> currentPlayer;

    private AudioPlayManager() {
    }

    public static AudioPlayManager get() {
        if (sInstance == null) {
            synchronized (AudioPlayManager.class) {
                if (sInstance == null) {
                    sInstance = new AudioPlayManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 申请播放权：若有其它正在播的实例，先暂停它。
     */
    public synchronized void requestPlay(PostAudioView player) {
        if (player == null) {
            return;
        }
        PostAudioView current = currentPlayer == null ? null : currentPlayer.get();
        if (current != null && current != player) {
            current.pauseByManager();
        }
        currentPlayer = new WeakReference<>(player);
    }

    /**
     * 暂停当前实例（页面 onPause 等）：停止播放/取消 prepare，保留引用便于回页续播。
     */
    public synchronized void pause() {
        PostAudioView current = currentPlayer == null ? null : currentPlayer.get();
        if (current != null) {
            current.pauseByManager();
        }
        // WeakReference 目标已被 GC：摘掉脏引用
        if (currentPlayer != null && currentPlayer.get() == null) {
            currentPlayer = null;
        }
    }

    /**
     * 暂停指定实例；若它是当前播放者，同步摘掉引用。
     */
    public synchronized void pause(PostAudioView player) {
        if (player == null) {
            return;
        }
        player.pauseByManager();
        PostAudioView current = currentPlayer == null ? null : currentPlayer.get();
        if (current == player) {
            currentPlayer = null;
        }
    }

    public synchronized PostAudioView getCurrent() {
        return currentPlayer == null ? null : currentPlayer.get();
    }

    public synchronized void onStop(PostAudioView player) {
        PostAudioView current = currentPlayer == null ? null : currentPlayer.get();
        if (current == player) {
            currentPlayer = null;
        }
    }

    /**
     * 页面退出时调用：完整 release，避免 MediaPlayer / Handler 残留。
     */
    public synchronized void releaseAll() {
        PostAudioView current = currentPlayer == null ? null : currentPlayer.get();
        currentPlayer = null;
        if (current != null) {
            current.release();
        }
    }
}
