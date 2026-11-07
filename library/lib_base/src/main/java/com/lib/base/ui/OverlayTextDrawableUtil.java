package com.lib.base.ui;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.lib.base.R;
import com.lib.base.util.ScreenUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * 调试悬浮层：在界面上显示当前 Activity / Fragment / Dialog 类名，方便排查页面层级。
 * <p>
 * 整体能力分三块：
 * <ol>
 *   <li>Activity：{@link Application.ActivityLifecycleCallbacks}，挂到 content root overlay</li>
 *   <li>Fragment：{@link FragmentManager.FragmentLifecycleCallbacks}，挂到 Fragment root view overlay</li>
 *   <li>Dialog：见下方「Dialog 打标逻辑」——系统没有 Dialog 全局生命周期回调，只能监听窗口添加</li>
 * </ol>
 *
 * <h3>Dialog 打标逻辑（无侵入业务 Dialog 实现）</h3>
 * <pre>
 * Dialog.show()
 *   → PhoneWindow / WindowManagerImpl.addView(decor, params)
 *   → WindowManagerGlobal.addView(...)
 *   → mViews.add(decor)          ← 我们 hook 的点
 *   → maybeAttachDialogOverlay
 *   → 在 decor.getOverlay() 上画类名（与 Activity/Fragment 相同风格）
 * </pre>
 * 为何不代理 Activity.WindowManager：PhoneWindow 会把 WM 强转为 WindowManagerImpl，
 * 且 Dialog 实际用的是 createLocalWindowManager 产生的本地 WM，最终仍汇入 WindowManagerGlobal。
 * <p>
 * 名称解析优先级：
 * <ol>
 *   <li>decor 上 {@link R.id#overlay_dialog_class_name}（BaseDialog.Builder.create 写入，如 WaitDialog）</li>
 *   <li>解开 Window.Callback 得到真正的 {@link Dialog}，用其 simpleName（如 LoadingDialog）</li>
 *   <li>按 Window type 兜底（TYPE_APPLICATION / 子窗口），避免 AppCompat 包装导致漏标</li>
 * </ol>
 * Activity 主窗口（host 为 Activity）一律不打 Dialog 标。
 * <p>
 * Date         2026/4/22.
 *
 * @author xxx
 */
public class OverlayTextDrawableUtil {

    private static final String TAG = "OverlayTextDrawableUtil";

    private static boolean ENABLE = false;
    /**
     * 标签配色：false=黑底白字（0x99000000），true=绿底白字（#8C2E7D32），深色主题用绿底更清晰。
     */
    private static boolean sGreenStyle = false;
    /** 是否已成功替换 WindowManagerGlobal.mViews，防止重复 hook */
    private static boolean sGlobalHooked = false;

    public static void setENABLE(boolean ENABLE) {
        OverlayTextDrawableUtil.ENABLE = ENABLE;
    }

    /** 切换 TipsDrawable 配色：true=绿底白字，false=黑底白字 */
    public static void setGreenStyle(boolean greenStyle) {
        sGreenStyle = greenStyle;
    }

    public static boolean isGreenStyle() {
        return sGreenStyle;
    }

    public static void debug(Application app) {
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                if (!ENABLE || !(activity instanceof AppCompatActivity)) return;
                // fragment 添加name
                ((AppCompatActivity) activity)
                        .getSupportFragmentManager()
                        .registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                            @Override
                            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                                super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                                ViewOverlay overlay = v.getOverlay();
                                int topInset = getTopInset(v);
                                int depth = getFragmentDepth(f);
                                int offset = (depth + 1) * dp(v.getContext(), 22); // 跟activity错开，不重叠到一起
                                TipsDrawable drawable = new TipsDrawable(f.getContext(), f.getClass().getSimpleName(), topInset + offset);
                                overlay.add(drawable);
                                // 优化展示
                                v.addOnLayoutChangeListener((v1, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                                    //because  ViewOverlay no update inner Drawable bounds。so myself setBounds and invalidate
                                    drawable.setBounds(left, top, right, bottom);
                                    drawable.invalidateSelf();
                                });
                            }
                        }, true);
                // activity 添加name
                TipsDrawable drawable = new TipsDrawable(activity, activity.getClass().getSimpleName(), ScreenUtil.getStatusBarSize());
                activity.findViewById(android.R.id.content)
                        .getRootView()
                        .getOverlay()
                        .add(drawable);
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }
        });
        // Dialog：统一走 WindowManagerGlobal 窗口监听（不改 LoadingDialog / ConfirmDialog 等业务类）
        hookWindowManagerGlobal();
    }

    // -------------------------------------------------------------------------
    // Dialog：窗口监听入口
    // -------------------------------------------------------------------------

    /**
     * Hook {@code WindowManagerGlobal.mViews}。
     * <p>
     * 所有应用内 Window（Activity Decor、Dialog Decor、部分 Popup）添加时都会
     * {@code mViews.add(view)}。用 {@link DialogViewList} 替换原 ArrayList 后，
     * 在 {@code add} 里拦截，再判断是否为 Dialog 并打标。
     * <p>
     * 注意：依赖反射访问隐藏 API，高版本机型若被限制会打日志失败，不影响业务。
     */
    @SuppressWarnings("unchecked")
    private static void hookWindowManagerGlobal() {
        if (!ENABLE || sGlobalHooked) {
            return;
        }
        try {
            Class<?> wmgClass = Class.forName("android.view.WindowManagerGlobal");
            Object wmg = wmgClass.getMethod("getInstance").invoke(null);
            Field lockField = accessibleField(wmgClass, "mLock");
            Object lock = lockField.get(wmg);

            Field viewsField = accessibleField(wmgClass, "mViews");
            // mViews 在源码里是 final，部分运行时需去掉 FINAL 才能 set 成功
            try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(viewsField, viewsField.getModifiers() & ~Modifier.FINAL);
            } catch (Throwable ignored) {
                // 去 FINAL 失败时仍尝试 set；ART 上多数情况可直接改
            }

            synchronized (lock) {
                ArrayList<View> original = (ArrayList<View>) viewsField.get(wmg);
                if (original instanceof DialogViewList) {
                    sGlobalHooked = true;
                    return;
                }
                DialogViewList hooked = new DialogViewList();
                if (original != null) {
                    // 拷贝已有窗口；addAll 不会走我们重写的 add()，需手动补一次打标扫描
                    hooked.addAll(original);
                    for (View view : original) {
                        maybeAttachDialogOverlay(view);
                    }
                }
                viewsField.set(wmg, hooked);
            }
            sGlobalHooked = true;
            Log.d(TAG, "WindowManagerGlobal.mViews hooked");
        } catch (Throwable t) {
            Log.w(TAG, "hook WindowManagerGlobal failed", t);
        }
    }

    @NonNull
    private static Field accessibleField(@NonNull Class<?> clazz, @NonNull String name) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    /**
     * 窗口加入 mViews 后的统一入口：过滤 → 解析名称 → 延迟挂 TipsDrawable。
     * <p>
     * 使用 {@link View#post(Runnable)}：addView 当下可能尚未完成 layout，宽高为 0。
     */
    private static void maybeAttachDialogOverlay(@Nullable View view) {
        if (!ENABLE || view == null) {
            return;
        }
        // overlay_dialog_tips_tag：表示已经挂过悬浮层，防重复 add
        if (view.getTag(R.id.overlay_dialog_tips_tag) != null) {
            return;
        }
        String name = resolveDialogOverlayName(view);
        // 空名会画出一块「没字的绿/黑条」，像空通知
        if (name == null || name.isEmpty()) {
            return;
        }
        view.post(() -> attachDialogOverlay(view, name));
    }

    /**
     * 判断该 Decor 是否需要 Dialog 标记，并生成展示文案。
     *
     * @return 类名（与 Activity/Fragment 一致）；Activity 主窗口或不识别则返回 null
     */
    @Nullable
    private static String resolveDialogOverlayName(@NonNull View decor) {
        // 1) BaseDialog.Builder.create() 写入的业务名（WaitDialog / TipsDialog 等）
        //    因为 createDialog() 实际 new 的是 BaseDialog，getClass() 只能拿到 BaseDialog
        Object named = decor.getTag(R.id.overlay_dialog_class_name);
        if (named instanceof CharSequence && ((CharSequence) named).length() > 0) {
            return named.toString();
        }

        // 2) 从 Decor → PhoneWindow → Callback 还原真正宿主
        Object host = resolveWindowHost(decor);
        if (host instanceof Activity) {
            // Activity 也是 TYPE 相关窗口的 host，绝不能打成 Dialog
            return null;
        }
        if (host instanceof Dialog) {
            // LoadingDialog / ConfirmDialog / 普通 Dialog、以及 unwrap 成功的 AppCompatDialog
            return host.getClass().getSimpleName();
        }

        // 3) 宿主解析失败时，用 Window type 兜底（兼容 Callback 包装异常的机型）
        ViewGroup.LayoutParams lp = decor.getLayoutParams();
        if (!(lp instanceof WindowManager.LayoutParams)) {
            return null;
        }
        int type = ((WindowManager.LayoutParams) lp).type;
        // 键盘 / Toast / 系统浮层不要打标，否则常出现空条或无意义标签
        if (type == WindowManager.LayoutParams.TYPE_INPUT_METHOD
                || type == WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG
                || type == WindowManager.LayoutParams.TYPE_TOAST
                || type >= 2038 /* TYPE_APPLICATION_OVERLAY，API26+ */) {
            return null;
        }
        // Activity = TYPE_BASE_APPLICATION(1)；Dialog 多为 TYPE_APPLICATION(2)；
        // Popup / 部分浮层落在 FIRST_SUB_WINDOW ~ LAST_SUB_WINDOW
        boolean dialogType = type == WindowManager.LayoutParams.TYPE_APPLICATION
                || (type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW
                && type <= WindowManager.LayoutParams.LAST_SUB_WINDOW);
        if (!dialogType) {
            return null;
        }
        if (host != null) {
            String hostName = host.getClass().getSimpleName();
            return hostName.isEmpty() ? null : hostName;
        }
        String simple = decor.getClass().getSimpleName();
        return simple.isEmpty() ? null : simple;
    }

    /**
     * 把 TipsDrawable 挂到 Dialog Decor 的 {@link ViewOverlay} 上。
     * <p>
     * 必须挂 Decor：Dialog 是独立 Window，画在 Activity content overlay 上不可见。
     */
    private static void attachDialogOverlay(@NonNull View decor, @NonNull String name) {
        if (decor.getTag(R.id.overlay_dialog_tips_tag) != null) {
            return;
        }
        TipsDrawable drawable = new TipsDrawable(decor.getContext(), name, dp(decor.getContext(), 8));
        decor.getOverlay().add(drawable);
        decor.setTag(R.id.overlay_dialog_tips_tag, Boolean.TRUE);
        // ViewOverlay 不会随 layout 自动更新 Drawable bounds，需手动 setBounds
        decor.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            drawable.setBounds(0, 0, right - left, bottom - top);
            drawable.invalidateSelf();
        });
        if (decor.getWidth() > 0 && decor.getHeight() > 0) {
            drawable.setBounds(0, 0, decor.getWidth(), decor.getHeight());
            drawable.invalidateSelf();
        }
    }

    // -------------------------------------------------------------------------
    // Dialog：从 DecorView 反查 Dialog / Activity
    // -------------------------------------------------------------------------

    /**
     * DecorView → PhoneWindow → {@link Window.Callback} → 解开包装后的 Dialog / Activity。
     * <p>
     * 普通 {@link Dialog}：Callback 就是 Dialog 自己。<br>
     * {@link androidx.appcompat.app.AppCompatDialog}（含 BaseDialog）：Callback 往往是
     * AppCompatDelegateImpl 或 WindowCallbackWrapper，必须 {@link #unwrapCallbackHost}，
     * 否则 {@code instanceof Dialog} 恒为 false，BaseDialog 会全部漏标。
     */
    @Nullable
    private static Object resolveWindowHost(@NonNull View decor) {
        try {
            Object phoneWindow = findPhoneWindow(decor);
            if (phoneWindow == null) {
                return null;
            }
            Object callback;
            if (phoneWindow instanceof Window) {
                callback = ((Window) phoneWindow).getCallback();
            } else {
                Method getCallback = phoneWindow.getClass().getMethod("getCallback");
                callback = getCallback.invoke(phoneWindow);
            }
            return unwrapCallbackHost(callback);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * 从 DecorView 找到所属 PhoneWindow。
     * 新系统 DecorView 一般有 {@code mWindow}；老实现可能是 PhoneWindow 非静态内部类（{@code this$0}）。
     */
    @Nullable
    private static Object findPhoneWindow(@NonNull View decor) {
        Class<?> clazz = decor.getClass();
        while (clazz != null) {
            try {
                Field windowField = clazz.getDeclaredField("mWindow");
                windowField.setAccessible(true);
                Object window = windowField.get(decor);
                if (window != null) {
                    return window;
                }
            } catch (NoSuchFieldException ignored) {
                try {
                    Field outer = clazz.getDeclaredField("this$0");
                    outer.setAccessible(true);
                    Object window = outer.get(decor);
                    if (window != null) {
                        return window;
                    }
                } catch (NoSuchFieldException ignored2) {
                    // 继续向父类找
                } catch (Throwable ignored2) {
                    return null;
                }
            } catch (Throwable ignored) {
                return null;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 解开 AppCompat 的 Callback 包装链，拿到真正的 {@link Dialog} 或 {@link Activity}。
     * <p>
     * 常见链路：
     * <ul>
     *   <li>WindowCallbackWrapper.getWrapped() / mWrapped → 下一层 Callback（最终可能是 Dialog）</li>
     *   <li>AppCompatDelegateImpl.mHost → Dialog 或 Activity</li>
     * </ul>
     * 最多解 10 层，防止异常包装成环。
     */
    @Nullable
    private static Object unwrapCallbackHost(@Nullable Object callback) {
        Object current = callback;
        for (int i = 0; i < 10 && current != null; i++) {
            if (current instanceof Dialog || current instanceof Activity) {
                return current;
            }
            Object next = null;
            // androidx.appcompat.view.WindowCallbackWrapper
            try {
                Method getWrapped = current.getClass().getMethod("getWrapped");
                next = getWrapped.invoke(current);
            } catch (Throwable ignored) {
            }
            if (next == null) {
                next = readFieldAnywhere(current, "mWrapped");
            }
            // AppCompatDelegateImpl
            Object host = readFieldAnywhere(current, "mHost");
            if (host instanceof Dialog || host instanceof Activity) {
                return host;
            }
            if (next == null && host != null) {
                next = host;
            }
            if (next == null || next == current) {
                break;
            }
            current = next;
        }
        return current;
    }

    /** 沿类继承链查找实例字段（含 private） */
    @Nullable
    private static Object readFieldAnywhere(@NonNull Object target, @NonNull String name) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Activity / Fragment 辅助
    // -------------------------------------------------------------------------

    private static int getFragmentDepth(@NonNull Fragment f) {
        int depth = 0;
        Fragment parent = f.getParentFragment();
        while (parent != null) {
            depth++;
            parent = parent.getParentFragment();
        }
        return depth;
    }

    private static int getTopInset(@NonNull View v) {
        android.view.WindowInsets insets = v.getRootWindowInsets();
        if (insets != null) {
            return insets.getSystemWindowInsetTop();
        }
        return ScreenUtil.getStatusBarSize();
    }

    private static int dp(@NonNull Context c, int dp) {
        return (int) (dp * c.getResources().getDisplayMetrics().density);
    }

    /**
     * 替换进 WindowManagerGlobal.mViews 的列表。
     * 框架侧只调用 {@link #add(Object)} / {@link #add(int, Object)} 添加窗口，
     * 在此拦截即可覆盖后续所有 Dialog.show()。
     */
    private static final class DialogViewList extends ArrayList<View> {
        @Override
        public boolean add(View view) {
            boolean result = super.add(view);
            maybeAttachDialogOverlay(view);
            return result;
        }

        @Override
        public void add(int index, View element) {
            super.add(index, element);
            maybeAttachDialogOverlay(element);
        }
    }

    /** 调试标签 Drawable：黑底白字 / 绿底白字，由 {@link #sGreenStyle} 切换 */
    private static class TipsDrawable extends ColorDrawable {

        /** 黑底：半透明黑 */
        private static final int BG_BLACK = 0x99000000;
        /** 绿底：#8C2E7D32（半透明 Material Green 800），深色主题可读 */
        private static final int BG_GREEN = 0x8C2E7D32;

        private final String name;
        private final Paint bgPaint;
        private final TextPaint textPaint;
        private final int height;
        private final int padding;
        private int topMargin;

        TipsDrawable(Context context, String name, int topMargin) {
            super(Color.TRANSPARENT);
            this.name = name;
            this.topMargin = topMargin;

            height = dp(context, 20);
            padding = dp(context, 8);

            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            applyStyleColors();

            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(dp(context, 12));
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        private void applyStyleColors() {
            bgPaint.setColor(sGreenStyle ? BG_GREEN : BG_BLACK);
        }

        @Override
        public void draw(Canvas canvas) {
            applyStyleColors();
            float textWidth = textPaint.measureText(name);

            float left = padding;
            float top = topMargin;
            float right = left + textWidth + padding * 2;
            float bottom = top + height;

            canvas.drawRect(left, top, right, bottom, bgPaint);

            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseline = top + height / 2f - (fm.top + fm.bottom) / 2;

            canvas.drawText(name, left + padding, baseline, textPaint);
        }
    }
}
