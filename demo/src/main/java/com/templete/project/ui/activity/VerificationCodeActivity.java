package com.templete.project.ui.activity;

import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.ui.widget.VerificationCodeView;
import com.templete.project.databinding.ActivityVerificationCodeBinding;

/**
 * 分格验证码 Demo：粘贴 / setCode / 密码态。
 */
public class VerificationCodeActivity extends BaseActivity<ActivityVerificationCodeBinding> {

    @Override
    protected ActivityVerificationCodeBinding viewBinding() {
        return ActivityVerificationCodeBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("验证码输入");
    }

    @Override
    public void initView() {
        bindState(mViewBinding.vcvNumber, mViewBinding.tvStateNumber);
        bindState(mViewBinding.vcvPassword, mViewBinding.tvStatePwd);
        mViewBinding.vcvNumber.requestInputFocus();
    }

    @Override
    public void initEvent() {
        mViewBinding.btnPasteDemo.setOnClickListener(v ->
                mViewBinding.vcvNumber.setCode("123456"));
        mViewBinding.btnClear.setOnClickListener(v -> {
            mViewBinding.vcvNumber.clearCode();
            mViewBinding.vcvPassword.clearCode();
        });
    }

    @Override
    public void initData() {
    }

    private void bindState(VerificationCodeView view, android.widget.TextView tv) {
        view.setOnCodeChangeListener(new VerificationCodeView.OnCodeChangeListener() {
            @Override
            public void onTextChange(VerificationCodeView v, String code) {
                tv.setText("当前：" + code + "（" + code.length() + "/" + v.getCellCount() + "）");
            }

            @Override
            public void onComplete(VerificationCodeView v, String code) {
                tv.setText("完成：" + code);
                toast("输入完成：" + code);
            }
        });
    }
}
