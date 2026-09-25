package com.lib.base.ui.dialog.base;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

import com.lib.base.R;
import com.lib.base.databinding.ConfirmDialogBinding;
import com.lib.base.util.ContextUtil;
import com.lib.base.util.DebugUtil;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

@SuppressLint("NonConstantResourceId")
public class ConfirmDialog extends Dialog implements DefaultLifecycleObserver {
    public static final String TAG = "ConfirmDialog";
    private final ConfirmDialogBinding binding;
    private LifecycleOwner lifecycleOwner;
    private boolean observed;
    private int index = 0;

    public ConfirmDialog(@NonNull Context context, String loadingInfo) {
        super(context, R.style.dialog);
        binding = ConfirmDialogBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        setCanceledOnTouchOutside(false);
        Window window = getWindow();//设置窗体位置以及动画
        if (null != window) {
            //window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setWindowAnimations(android.R.style.Animation_Toast);
        }
        if (!TextUtils.isEmpty(loadingInfo)) {
            binding.tvLoading.setText(loadingInfo);
        }
        setOnDismissListener(dialog -> binding.getRoot().removeCallbacks(runnable));
        //感知activity生命周期,自动dismiss
        addObserver(context);
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

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DebugUtil.logD(TAG, "pop onDestroy");
        if (isShowing()) {
            dismiss();
        } else {
            removeObserver();
        }
    }

    @Override
    public void show() {
        addObserver(getContext());
        super.show();
        binding.getRoot().post(runnable);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        removeObserver();
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            binding.getRoot().removeCallbacks(this);
            switch (index) {
                case 0:
                    select(0);
                    //binding.tvLoading.setText("加载中");
                    index = 1;
                    break;
                case 1:
                    select(1);
                    //binding.tvLoading.setText("加载中•");
                    index = 2;
                    break;
                case 2:
                    select(2);
                    //binding.tvLoading.setText("加载中••");
                    index = 3;
                    break;
                default:
                    select(3);
                    //binding.tvLoading.setText("加载中•••");
                    index = 0;
                    break;
            }
            binding.getRoot().postDelayed(this, 100);
        }
    };

    private void select(int i) {
        binding.point1.setSelected(i == 0);
        binding.point2.setSelected(i == 1);
        binding.point3.setSelected(i == 2);
        binding.point4.setSelected(i == 3);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            dismiss();
            if (onKeyBackListener != null) {
                onKeyBackListener.onKeyBack();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public interface OnKeyBackListener {
        void onKeyBack();
    }

    private OnKeyBackListener onKeyBackListener;

    public void setOnKeyBackListener(OnKeyBackListener onKeyBackListener) {
        this.onKeyBackListener = onKeyBackListener;
    }
}
