package com.lib.base.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.lib.base.R;
import com.lib.base.util.ScreenUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

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
 * ENABLE 为 true 时展示可拖动「显隐」悬浮按钮；单击切换全部已打开页面的类名标签显隐。
 * <p>
 * Date         2026/4/22.
 *
 * @author xxx
 */
public class OverlayTextDrawableUtil {

    private static final String TAG = "OverlayTextDrawableUtil";
    private static final String FLOAT_TAG = "overlay_text_drawable_float";

    private static boolean ENABLE = false;
    /**
     * 标签配色：false=黑底白字（0x99000000），true=绿底白字（#8C2E7D32），深色主题用绿底更清晰。
     */
    private static boolean sGreenStyle = false;
    /** 全局类名标签是否绘制；由悬浮按钮单击切换。 */
    private static boolean sLabelsVisible = true;
    /** 是否已成功替换 WindowManagerGlobal.mViews，防止重复 hook */
    private static boolean sGlobalHooked = false;
    /** 弱引用登记所有类名标签，便于全局显隐时统一刷新。 */
    private static final Set<TipsDrawable> sTipDrawables =
            Collections.newSetFromMap(new WeakHashMap<>());
    /**
     * 悬浮按钮位置（跨 Activity / 横竖屏复用）。
     * 不用绝对像素：旋转后宽高对调，绝对坐标会落在屏外看起来像「消失」。
     * 改为记住吸附左右边 + 竖直可拖区域比例。
     */
    private static boolean sFloatStickRight = true;
    private static float sFloatYRatio = 0.35f;
    private static boolean sFloatPosInitialized = false;

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

    /** 切换全部页面类名标签显隐并刷新。 */
    public static void toggleLabelsVisible() {
        sLabelsVisible = !sLabelsVisible;
        for (TipsDrawable tip : sTipDrawables) {
            tip.invalidateSelf();
            Drawable.Callback callback = tip.getCallback();
            // View.overlay 在 bounds 为空时 invalidateSelf 可能不触发重绘，强制刷新宿主
            if (callback instanceof View) {
                ((View) callback).invalidate();
            }
        }
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
                                sTipDrawables.add(drawable);
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
                View contentRoot = activity.findViewById(android.R.id.content).getRootView();
                TipsDrawable drawable = new TipsDrawable(activity, activity.getClass().getSimpleName(), ScreenUtil.getStatusBarSize());
                sTipDrawables.add(drawable);
                contentRoot.getOverlay().add(drawable);
                contentRoot.addOnLayoutChangeListener((v1, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    drawable.setBounds(left, top, right, bottom);
                    drawable.invalidateSelf();
                });
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (ENABLE) {
                    attachFloatButton(activity);
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                detachFloatButton(activity);
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
        sTipDrawables.add(drawable);
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

    /** 将悬浮按钮挂到当前 Activity 的 content 上。 */
    private static void attachFloatButton(@NonNull Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.findViewWithTag(FLOAT_TAG) != null) {
            return;
        }
        MagnetFloatButton button = new MagnetFloatButton(activity);
        button.setTag(FLOAT_TAG);
        content.addView(button, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /** 从当前 Activity 移除悬浮按钮，并记住相对位置（边 + 竖直比例）。 */
    private static void detachFloatButton(@NonNull Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        View button = content.findViewWithTag(FLOAT_TAG);
        if (button == null) {
            return;
        }
        if (button instanceof MagnetFloatButton) {
            ((MagnetFloatButton) button).persistRelativePosition();
        }
        content.removeView(button);
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
            if (!sLabelsVisible) {
                return;
            }
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

    /**
     * 应用内可拖动悬浮按钮：松手吸附最近左右边缘，带弹簧阻尼；
     * 单击切换全局类名标签显隐（自身消费触控，不穿透）。
     * 横竖屏切换时按「左右边 + 竖直比例」重放位置，避免绝对坐标跑出屏外。
     */
    @SuppressLint("ClickableViewAccessibility")
    private static final class MagnetFloatButton extends TextView {
        private final int touchSlop;
        private float downRawX;
        private float downRawY;
        private float downViewX;
        private float downViewY;
        private boolean dragging;
        private boolean springRunning;
        private float springVelocity;
        private float springTargetX;
        /** 父布局上次宽高，用于识别旋转等尺寸变化。 */
        private int lastParentW;
        private int lastParentH;
        /**
         * 挂载过 layout 监听的 parent。
         * detach 时 {@link #getParent()} 可能已清空，需单独记住才能卸监听。
         */
        @Nullable
        private ViewGroup sizeListenParent;

        private final OnLayoutChangeListener parentSizeListener =
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    int w = right - left;
                    int h = bottom - top;
                    if (w <= 0 || h <= 0) {
                        return;
                    }
                    if (w != lastParentW || h != lastParentH) {
                        lastParentW = w;
                        lastParentH = h;
                        // 尺寸变了（横竖屏等）：立刻贴边，不用弹簧（旋转中 layout 会打断动画）
                        post(this::relayoutForParentSize);
                    }
                };

        private final Runnable springTick = new Runnable() {
            @Override
            public void run() {
                if (!springRunning) {
                    return;
                }
                ViewGroup parent = (ViewGroup) getParent();
                if (parent == null) {
                    springRunning = false;
                    return;
                }
                // 弹簧：F = -k·x - c·v，弹球阻尼感
                float stiffness = 0.18f;
                float damping = 0.72f;
                float delta = springTargetX - getX();
                springVelocity = (springVelocity + delta * stiffness) * damping;
                setX(getX() + springVelocity);
                if (Math.abs(delta) < 0.5f && Math.abs(springVelocity) < 0.5f) {
                    setX(springTargetX);
                    springRunning = false;
                    persistRelativePosition();
                    return;
                }
                postOnAnimation(this);
            }
        };

        MagnetFloatButton(Context context) {
            super(context);
            touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            setText("显隐");
            setTextColor(Color.WHITE);
            setTextSize(12f);
            setTypeface(Typeface.DEFAULT_BOLD);
            setGravity(Gravity.CENTER);
            int hPad = dp(context, 12);
            int vPad = dp(context, 8);
            setPadding(hPad, vPad, hPad, vPad);
            setBackgroundColor(sGreenStyle ? 0xCC2E7D32 : 0x99000000);
            setElevation(dp(context, 4));
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            ViewGroup parent = (ViewGroup) getParent();
            if (parent != null) {
                sizeListenParent = parent;
                parent.addOnLayoutChangeListener(parentSizeListener);
            }
            post(this::restoreOrPlaceDefault);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            // layout 会改 left/top，setX 依赖 translation；空闲时纠回吸附边，避免停在中间
            if (!dragging && !springRunning && (right - left) > 0) {
                post(this::ensureStuckToSavedEdge);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            springRunning = false;
            removeCallbacks(springTick);
            if (sizeListenParent != null) {
                sizeListenParent.removeOnLayoutChangeListener(parentSizeListener);
                sizeListenParent = null;
            }
            super.onDetachedFromWindow();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            ViewGroup parent = (ViewGroup) getParent();
            if (parent == null) {
                return true;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    springRunning = false;
                    removeCallbacks(springTick);
                    downRawX = event.getRawX();
                    downRawY = event.getRawY();
                    downViewX = getX();
                    downViewY = getY();
                    dragging = false;
                    parent.requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_MOVE: {
                    float dx = event.getRawX() - downRawX;
                    float dy = event.getRawY() - downRawY;
                    if (!dragging && dx * dx + dy * dy > touchSlop * touchSlop) {
                        dragging = true;
                    }
                    if (dragging) {
                        setX(clampX(downViewX + dx, parent));
                        setY(clampY(downViewY + dy, parent));
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    parent.requestDisallowInterceptTouchEvent(false);
                    if (dragging) {
                        startEdgeSpring(parent);
                    } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        toggleLabelsVisible();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }

        /**
         * 把当前像素位置写成左右边 + 竖直比例，供跨页 / 横竖屏恢复。
         */
        void persistRelativePosition() {
            ViewGroup parent = (ViewGroup) getParent();
            if (parent == null || parent.getWidth() <= 0 || parent.getHeight() <= 0) {
                return;
            }
            if (getWidth() <= 0 || getHeight() <= 0) {
                return;
            }
            sFloatStickRight = getX() + getWidth() / 2f >= parent.getWidth() / 2f;
            float[] yRange = verticalDragRange(parent);
            float span = yRange[1] - yRange[0];
            if (span > 0f) {
                sFloatYRatio = Math.min(1f, Math.max(0f, (getY() - yRange[0]) / span));
            }
            sFloatPosInitialized = true;
        }

        /** 恢复相对位置并立刻贴边（首次进入 / Activity 切换）。 */
        private void restoreOrPlaceDefault() {
            ViewGroup parent = (ViewGroup) getParent();
            if (parent == null) {
                return;
            }
            if (getWidth() == 0 || getHeight() == 0
                    || parent.getWidth() == 0 || parent.getHeight() == 0) {
                post(this::restoreOrPlaceDefault);
                return;
            }
            lastParentW = parent.getWidth();
            lastParentH = parent.getHeight();
            snapToSavedEdge(parent, false);
        }

        /** 父布局尺寸变化（含横竖屏）：取消弹簧，按记住的边立刻贴死。 */
        private void relayoutForParentSize() {
            ViewGroup parent = (ViewGroup) getParent();
            if (parent == null || getWidth() == 0 || getHeight() == 0
                    || parent.getWidth() == 0 || parent.getHeight() == 0) {
                return;
            }
            if (dragging) {
                setX(clampX(getX(), parent));
                setY(clampY(getY(), parent));
                return;
            }
            snapToSavedEdge(parent, false);
        }

        /**
         * layout 之后纠偏：若水平偏离吸附边，拉回记住的左右边。
         */
        private void ensureStuckToSavedEdge() {
            if (dragging || springRunning) {
                return;
            }
            ViewGroup parent = (ViewGroup) getParent();
            if (parent == null || getWidth() == 0 || parent.getWidth() == 0) {
                return;
            }
            float margin = dp(getContext(), 8);
            boolean stickRight = sFloatPosInitialized ? sFloatStickRight : true;
            float edgeX = stickRight
                    ? parent.getWidth() - getWidth() - margin
                    : margin;
            if (Math.abs(getX() - edgeX) > 1f) {
                setX(edgeX);
                setY(clampY(getY(), parent));
            }
        }

        /**
         * 落到记住的左右边 + 竖直比例。
         *
         * @param animate true=拖拽松手弹簧；false=旋转/恢复立刻贴边
         */
        private void snapToSavedEdge(@NonNull ViewGroup parent, boolean animate) {
            springRunning = false;
            removeCallbacks(springTick);
            float margin = dp(getContext(), 8);
            boolean stickRight = sFloatPosInitialized ? sFloatStickRight : true;
            float yRatio = sFloatPosInitialized ? sFloatYRatio : 0.35f;
            float edgeX = stickRight
                    ? parent.getWidth() - getWidth() - margin
                    : margin;
            float[] yRange = verticalDragRange(parent);
            float span = yRange[1] - yRange[0];
            float edgeY = clampY(yRange[0] + span * yRatio, parent);
            sFloatStickRight = stickRight;
            setY(edgeY);
            if (animate) {
                springTargetX = edgeX;
                springVelocity = 0f;
                springRunning = true;
                post(springTick);
            } else {
                setX(edgeX);
                persistRelativePosition();
            }
        }

        /** 水平吸附到最近的左右边，竖直位置保持，弹簧阻尼逼近（仅拖拽松手）。 */
        private void startEdgeSpring(@NonNull ViewGroup parent) {
            float centerX = getX() + getWidth() / 2f;
            sFloatStickRight = centerX >= parent.getWidth() / 2f;
            float[] yRange = verticalDragRange(parent);
            float span = yRange[1] - yRange[0];
            if (span > 0f) {
                sFloatYRatio = Math.min(1f, Math.max(0f, (getY() - yRange[0]) / span));
            }
            sFloatPosInitialized = true;
            snapToSavedEdge(parent, true);
        }

        private float clampX(float value, @NonNull ViewGroup parent) {
            float margin = dp(getContext(), 8);
            float max = Math.max(margin, parent.getWidth() - getWidth() - margin);
            return Math.min(Math.max(value, margin), max);
        }

        private float clampY(float value, @NonNull ViewGroup parent) {
            float[] range = verticalDragRange(parent);
            return Math.min(Math.max(value, range[0]), range[1]);
        }

        /**
         * 竖直可拖范围 [minY, maxY]，已扣状态栏 / 导航栏 / 刘海与边距。
         */
        @NonNull
        private float[] verticalDragRange(@NonNull ViewGroup parent) {
            WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(parent);
            Insets insets = rootInsets == null ? Insets.NONE : rootInsets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                            | WindowInsetsCompat.Type.navigationBars()
                            | WindowInsetsCompat.Type.displayCutout());
            float top = insets.top + dp(getContext(), 8);
            float bottomMargin = insets.bottom + dp(getContext(), 8);
            float max = Math.max(top, parent.getHeight() - getHeight() - bottomMargin);
            return new float[]{top, max};
        }
    }
}
