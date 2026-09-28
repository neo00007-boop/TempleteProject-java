package com.lib.base.util;

import androidx.lifecycle.Observer;

import com.kunminx.architecture.domain.message.MutableResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内事件总线。同一个 key 共用一条通道，新观察者收不到订阅前的旧值。
 * <p>
 * {@code observe(LifecycleOwner)} 跟着页面生命周期，页面销毁时自动解绑。
 * {@code observeForever} 不跟生命周期，不用时必须调用 {@link #removeObserver} 手动解绑，否则观察者会一直留在通道上。
 * <p>
 * 通道是 UnPeek 这一套，非粘性，一条消息可以被多个观察者消费。三个类按读写来分：
 * <ul>
 * <li>{@link com.kunminx.architecture.domain.message.MutableResult}：可写结果。
 * 唯一可信源在业务处理完后 {@code setValue} / {@code postValue}。对外应只给出父类 {@code Result}，页面只能观察。
 * 本总线自己就是发送方，所以通道直接存它，调用方既能发也能订。</li>
 * <li>{@link com.kunminx.architecture.ui.callback.UnPeekLiveData}：可写事件。
 * 语义偏向界面发起，例如按钮点击后页面自己发出去。页面内既要发又要订时用它。</li>
 * <li>{@link com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData}：只读。
 * {@code setValue} / {@code postValue} 不能从外部调用，页面只能 {@code observe}。
 * 把可写对象交给界面时用这个类型。要回放旧值必须显式调用 {@code observeSticky}。</li>
 * </ul>
 */
public final class LiveDataBus {

    private final Map<String, MutableResult<Object>> bus = new ConcurrentHashMap<>();

    private LiveDataBus() {
    }

    private static class SingletonHolder {
        private static final LiveDataBus DEFAULT_BUS = new LiveDataBus();
    }

    public static LiveDataBus get() {
        return SingletonHolder.DEFAULT_BUS;
    }

    public MutableResult<Object> with(String key) {
        return with(key, Object.class);
    }

    public <T> MutableResult<T> with(String key, Class<T> type) {
        if (key == null || type == null) {
            throw new IllegalArgumentException("key or type is null");
        }
        MutableResult<Object> created = new MutableResult<>();
        MutableResult<Object> existing = bus.putIfAbsent(key, created);
        return uncheckedCast(existing != null ? existing : created);
    }

    /**
     * {@code observeForever} 的手动解绑。只摘掉这一个观察者，通道留给其他订阅者。
     */
    public void removeObserver(String key, Observer<?> observer) {
        if (key == null || observer == null) {
            return;
        }
        MutableResult<Object> result = bus.get(key);
        if (result != null) {
            result.removeObserver(castObserver(observer));
        }
    }

    @SuppressWarnings("unchecked")
    private static Observer<? super Object> castObserver(Observer<?> observer) {
        return (Observer<? super Object>) observer;
    }

    @SuppressWarnings("unchecked")
    private static <T> MutableResult<T> uncheckedCast(MutableResult<Object> result) {
        return (MutableResult<T>) result;
    }
}
