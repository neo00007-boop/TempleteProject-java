package com.lib.base.ui.pop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.OneShotPreDrawListener;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.hjq.shape.R;
import com.lib.base.util.ContextUtil;
import com.lib.base.util.DebugUtil;

/**
 * 从锚点底边向下铺开的下拉菜单。遮罩盖住锚点下面这块区域，菜单内容在顶部滑入。
 * <p>
 * PackageName  com.spot.ispot.view.widget
 * ProjectName  Spot1-26-yoga
 * @author      xwchen
 * Date         6/27/21.
 */
public class PopMenuView extends PopupWindow implements DefaultLifecycleObserver {
    public static final String TAG = "PopMenuView";
    private static final int ANIM = 250;

    private final View locationView;
    private final View rootView;
    private final View mask;
    private final View sheet;
    private LifecycleOwner lifecycleOwner;
    private boolean observed;
    private boolean closing;
    private int animToken;

    public PopMenuView(View contentView, View locationView, View rootView) {
        this(contentView, locationView, rootView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public PopMenuView(View contentView, View locationView, View rootView, int width, int height) {
        this(wrapMenu(contentView), checkAnchor(locationView), checkAnchor(rootView), width, height);
    }

    private PopMenuView(MenuShell shell, View locationView, View rootView, int width, int height) {
        super(shell.root, width, height);
        this.locationView = locationView;
        this.rootView = rootView;
        this.mask = shell.mask;
        this.sheet = shell.sheet;
        shell.blank.setOnClickListener(v -> dismiss());
        setFocusable(true);
        setOutsideTouchable(false);
        setClippingEnabled(true);
        // 遮罩和菜单分开动,不用整窗缩放
        setAnimationStyle(0);
        //setBackgroundDrawable(new BitmapDrawable())；已过时
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addObserver(shell.sheet.getContext());
    }

    private static View checkAnchor(View anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("anchor == null");
        }
        return anchor;
    }

    /**
     * 传入的是菜单内容。遮罩和空白点击区在这里补上。
     */
    private static MenuShell wrapMenu(View content) {
        if (content == null) {
            throw new IllegalArgumentException("content == null");
        }
        Context context = content.getContext();
        FrameLayout root = new FrameLayout(context);
        View mask = new View(context);
        mask.setAlpha(0f);
        mask.setBackgroundColor(ContextCompat.getColor(context, R.color.black30));
        root.addView(mask, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        if (content.getParent() instanceof ViewGroup) {
            ((ViewGroup) content.getParent()).removeView(content);
        }
        column.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, contentHeight(content)));
        View blank = new View(context);
        column.addView(blank, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(column, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new MenuShell(root, mask, content, blank);
    }

    private static int contentHeight(View content) {
        ViewGroup.LayoutParams lp = content.getLayoutParams();
        if (lp != null && lp.height > 0) {
            return lp.height;
        }
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private static final class MenuShell {
        private final View root;
        private final View mask;
        private final View sheet;
        private final View blank;

        private MenuShell(View root, View mask, View sheet, View blank) {
            this.root = root;
            this.mask = mask;
            this.sheet = sheet;
            this.blank = blank;
        }
    }

    private void addObserver(Context context) {
        if (observed) {
            return;
        }
        if (lifecycleOwner == null) {
            lifecycleOwner = ContextUtil.getLifecycleOwnerByContext(context);
        }
        if (lifecycleOwner != null) {
            lifecycleOwner.getLifecycle().addObserver(this);
            observed = true;
        }
    }

    private void removeObserver() {
        if (lifecycleOwner != null && observed) {
            lifecycleOwner.getLifecycle().removeObserver(this);
            observed = false;
        }
    }

    public void show() {
        if (!canShow()) {
            return;
        }
        int[] below = belowAnchor();
        if (below[1] <= 0) {
            return;
        }
        closing = false;
        int token = ++animToken;
        mask.animate().cancel();
        sheet.animate().cancel();
        setHeight(below[1]);
        mask.setAlpha(0f);
        sheet.setAlpha(0f);
        sheet.setTranslationY(0f);
        addObserver(locationView.getContext());
        if (!isShowing()) {
            try {
                // NO_GRAVITY 时 y 会被丢掉,窗口贴到顶部,标题栏就被盖住
                showAtLocation(locationView, Gravity.TOP | Gravity.START, 0, below[0]);
            } catch (WindowManager.BadTokenException ex) {
                return;
            }
        }
        OneShotPreDrawListener.add(sheet, () -> open(token));
    }

    private void open(int token) {
        if (token != animToken || closing || !isShowing()) {
            return;
        }
        int distance = sheet.getHeight();
        if (distance > 0) {
            sheet.setTranslationY(-distance);
        }
        sheet.setAlpha(1f);
        mask.animate().alpha(1f).setDuration(ANIM).start();
        if (distance > 0) {
            sheet.animate().translationY(0f).setDuration(ANIM).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    @Override
    public void dismiss() {
        if (!isShowing()) {
            return;
        }
        if (closing || !canAnimate()) {
            mask.animate().cancel();
            sheet.animate().cancel();
            finishDismiss();
            return;
        }
        closing = true;
        int token = ++animToken;
        mask.animate().cancel();
        sheet.animate().cancel();
        mask.animate().alpha(0f).setDuration(ANIM).start();
        sheet.animate().translationY(-sheet.getHeight()).setDuration(ANIM)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    if (token == animToken) {
                        finishDismiss();
                    }
                })
                .start();
    }

    private void finishDismiss() {
        if (!isShowing()) {
            closing = false;
            return;
        }
        animToken++;
        closing = false;
        super.dismiss();
        removeObserver();
    }

    /**
     * @return [锚点在窗口里的底边, 外面传入的根布局高度]
     */
    private int[] belowAnchor() {
        int[] location = new int[2];
        locationView.getLocationInWindow(location);
        int top = location[1] + locationView.getHeight();
        return new int[]{top, rootView.getHeight()};
    }

    private boolean canShow() {
        if (!locationView.isAttachedToWindow()) {
            return false;
        }
        Activity activity = ContextUtil.getActivityByContext(locationView.getContext());
        return activity == null || (!activity.isFinishing() && !activity.isDestroyed());
    }

    private boolean canAnimate() {
        return canShow() && sheet.isAttachedToWindow();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DebugUtil.logD(TAG, "pop onDestroy");
        if (isShowing()) {
            dismiss();
        } else {
            removeObserver();
        }
    }
}
