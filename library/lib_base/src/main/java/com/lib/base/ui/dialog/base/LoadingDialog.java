package com.lib.base.ui.dialog.base;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

import com.hjq.shape.view.progress.ProgressDrawable;
import com.lib.base.R;
import com.lib.base.databinding.LoadingDialogBinding;
import com.lib.base.util.ContextUtil;
import com.lib.base.util.DebugUtil;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

@SuppressLint("NonConstantResourceId")
public class LoadingDialog extends Dialog implements /*LifecycleObserver*/DefaultLifecycleObserver {
    public static final String TAG = "LoadingDialog";
    private ProgressDrawable progressDrawable;
    private final LoadingDialogBinding binding;
    private LifecycleOwner lifecycleOwner;
    private boolean observed;

    public LoadingDialog(@NonNull Context context, int resourceId, String loadingInfo) {
        super(context, R.style.dialog);
        binding = LoadingDialogBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        setCanceledOnTouchOutside(false);
        Window window = getWindow();//设置窗体位置以及动画
        if (null != window) {
            //window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setWindowAnimations(android.R.style.Animation_Toast);
        }
        initView(resourceId, loadingInfo);
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

    private void initView(int resourceId, String loadingInfo) {
        if (resourceId == -1) {
            progressDrawable = new ProgressDrawable();
            progressDrawable.setColor(Color.parseColor("#ffffff"));
            binding.ivPro.setImageDrawable(progressDrawable);
        } else {
            binding.ivPro.setImageResource(resourceId);
        }
        binding.tvLoadingInfo.setText(!TextUtils.isEmpty(loadingInfo) ? loadingInfo : "加载中...");
    }

    @Override
    public void show() {
        addObserver(getContext());
        super.show();
        if (progressDrawable != null) {
            progressDrawable.start();
        }
    }

    @Override
    public void dismiss() {
        if (progressDrawable != null && progressDrawable.isRunning()) {
            progressDrawable.stop();
        }
        super.dismiss();
        removeObserver();
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

    private ConfirmDialog.OnKeyBackListener onKeyBackListener;

    public void setOnKeyBackListener(ConfirmDialog.OnKeyBackListener onKeyBackListener) {
        this.onKeyBackListener = onKeyBackListener;
    }
}
