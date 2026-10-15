package com.lib.base.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version 1.0
 * @class 全局公用线程池
 * @anthor xianggao6
 * @time 2020/6/11
 */
public class GlobalThreadPoolUtil {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * 线程数，核心线程数和最大线程数相同：无界队列下线程数永远不会超过核心线程数
     */
    private static final int POOL_SIZE = CPU_COUNT + 2;
    /**
     * 线程空闲超过该时间会被回收
     */
    private static final int KEEP_ALIVE_TIME = 60;
    /**
     * 无界队列，线程都忙时任务排队等待，不会被丢弃
     */
    private static final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

    /**
     * 线程工厂
     */
    private static final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Dispatcher thread:" + integer.getAndIncrement());
        }
    };

    private static final ThreadPoolExecutor sExecutor;

    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    static {
        sExecutor = new ThreadPoolExecutor(
                POOL_SIZE, POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                workQueue, threadFactory);
        sExecutor.allowCoreThreadTimeOut(true);//不设置,即使核心线程闲置也会一直保持
    }

    public static void postOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    public static void postOnUiThreadDelay(Runnable runnable, long delay) {
        mHandler.postDelayed(runnable, delay);
    }

    public static void postOnSubThread(Runnable runnable) {
        sExecutor.execute(runnable);
    }

}
