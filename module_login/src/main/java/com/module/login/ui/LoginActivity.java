package com.module.login.ui;

import android.annotation.SuppressLint;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.greendao.db.util.GsonUtil;
import com.lib.base.rxjava.RxUtils;
import com.lib.router.Router;
import com.lib.base.ui.activity.BaseMvvmActivity;
import com.lib.base.ui.activity.TitleBarTheme;
import com.lib.base.util.LoginUtil;
import com.module.login.R;
import com.lib.router.RoutePath;
import com.module.login.databinding.LoginActivityBinding;
import com.module.login.http.LoginHttp;
import com.module.login.mvvm.LoginViewModel;
/**
 * PackageName  com.bigheadhorse.xscat.view.activity
 * ProjectName  XSCat
 * @author      xwchen
 * Date         2019-12-02.
 */

@Route(path = RoutePath.LOGIN_ACTIVITY)
public class LoginActivity extends BaseMvvmActivity<LoginActivityBinding, LoginViewModel> implements View.OnClickListener {
    public static final String TAG = "LoginActivity_LOG";
    @Autowired
    public String data;

    @Override
    protected Class<LoginViewModel> getViewModelClass() {
        return LoginViewModel.class;
    }

    @Override
    public LoginActivityBinding viewBinding() {
        return LoginActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public int getTitleTheme() {
        return TitleBarTheme.THEME_WHITE;
    }

    @Override
    public int finishAnimType() {
        return ANIM_LOGIN;
    }

    @SuppressLint("InflateParams")
    @Override
    public void inits() {
        setTitleStr("登录");
        Router.inject(this);
        LoginUtil.onLoginShown();
        /*setRightClickViews((position, view) -> {
            // todo
        }, getViewById(R.layout.kefu_layout));*/
    }

    @Override
    public void initView() {
        String str1 = "登录即代表接受";
        String str2 = "《什么值得推用户协议和隐私政策》";
        String content = str1 + str2;
        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(new ForegroundColorSpan(this.getColor(R.color.cl_1692DB)), str1.length(), (str1 + str2).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new BackgroundColorSpan(this.getColor(R.color.cl_white)), str1.length(), (str1 + str2).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);    //去除超链接的下划线
            }

            @Override
            public void onClick(@NonNull View widget) {
                //WebViewActivity.toWebViewActivity(LoginActivity.this, "什么值得推用户协议和隐私政策", AppConfig.APP_PROTOCOL);
            }
        }, str1.length(), (str1 + str2).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mViewBinding.tvWxAgree.setMovementMethod(LinkMovementMethod.getInstance());
        mViewBinding.tvWxAgree.setText(spannableString);
        //
        mViewBinding.tvWxLogin.setOnClickListener(this);
        mViewBinding.tvOneClickLogin.setOnClickListener(this);
    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        logD(TAG, "data=" + data);
       /* mViewModel.getBaseDataLiveData().observe(this, baseData -> {
            DebugUtil.logD(TAG, "msg=" + baseData.message);
            boolean continue1 = resultWx(baseData);
            if (continue1) {
                boolean continue2 = resultDoOnePass(baseData);
            }
        });
        getGlobalUserStateViewModel().getWxCodeLiveData().observe(this, code -> {
            DebugUtil.logD(TAG, "code=" + code);
            mViewModel.getWxUserInfo(code);
        });
        mViewModel.getPrefetchNumberLiveData().observe(this, prefetchNumberData -> {
            if (OUtil.isNotNull(prefetchNumberData.prefetchNumber)) {
                DebugUtil.logD(TAG, "prefetchNumber=" + prefetchNumberData.prefetchNumber);
                mViewModel.getDoOnePass();
            } else {
                DebugUtil.logD(TAG, "getPrefetchNumberLiveData error msg=" + prefetchNumberData.errorMsg);
                DebugUtil.toast("一键登录预取号失败，使用验证码登录！");
                IntentUtil.startActivityWithString(this, NumVerifyLoginActivity.class, "from", "login");
            }
        });*/
    }

    /*public boolean resultWx(BaseData baseData) {
        if (baseData.type == LoginViewModel.GET_WX_INFO_OK) {
            WxInfoBean wxInfoBean = (WxInfoBean) baseData.obj;//wxInfo.wxInfoBean;
            DebugUtil.logD(TAG, "wxInfoBean=" + GsonUtil.getInstance().toJson(wxInfoBean));
            String openid = wxInfoBean.openid;
            String nickname = wxInfoBean.nickname;
            try {
                nickname = new String(nickname.getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String sex = wxInfoBean.sex;//1男 其他女
            //用户信息同步到GlobalUserStateViewModel
            getGlobalUserStateViewModel().getWxOpenIdLiveData().setValue(openid);
            getGlobalUserStateViewModel().getWxOpenNameLiveData().setValue(nickname);
            getGlobalUserStateViewModel().getWxOpenSexLiveData().setValue(sex);
            //验证是否需要绑定手机号等操作
            IntentUtil.startActivity(LoginActivity.this, WxBindingNumActivity.class);
        } else if (baseData.type == LoginViewModel.GET_WX_INFO_ERROR) {
            DebugUtil.logD(TAG, "error msg=" + baseData.message);
        } else {
            return true;
        }
        return false;
    }

    public boolean resultDoOnePass(BaseData baseData) {
        if (baseData.type == LoginViewModel.DO_ONE_PASS_OK) {
            DebugUtil.toast("本机号码验证通过！");
        } else if (baseData.type == LoginViewModel.DO_ONE_PASS_NOT) {
            DebugUtil.toast("本机号码验证不通过，请尝试验证码登录！");
        } else if (baseData.type == LoginViewModel.DO_ONE_PASS_ERROR) {
            DebugUtil.toast("本机号码验证异常，请尝试验证码登录！");
        } else if (baseData.type == LoginViewModel.DO_ONE_PASS_CANCEL) {
            DebugUtil.toast("一键登录已取消！");
        } else {
            return true;
        }
        return false;
    }*/


   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            RetrofitHelper.getInstance().setHasInterceptAndGoLogin(false);
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_one_click_login) {
            login();
        }
    }

    @SuppressLint("CheckResult")
    private void login() {
        LoginHttp
                .getInstance()
                .login("18557532484", "000000")
                /*********************************** 以下写法任选其一，不要叠加 ***********************************/
                //.compose(RxUtils::toSimpleSingle)
                //.compose(RxUtils.toSimpleSingle())//这两个一样
                //.doOnSubscribe(this::addDisposable) //自动处理生命周期方式,以下两种是框架处理方式
                //.compose(bindUntilEvent(ActivityEvent.DESTROY))// 框架自动处理生命周期方式一
                //.compose(RxUtils.bindUntilEvent(this))// 对上面封装
                //.compose(RxUtils.bindUntilEvent(this, ActivityEvent.DESTROY))// 对上面封装
                //.compose(bindToLifecycle())// 框架自动处理生命周期方式二,注意此种方式绑定的生命周期是onResume()/onPause()
                //.compose(RxUtils.bindToLifecycle(this))// 对上面封装
                //.compose(RxUtils.withDialogSingle(this, null, false))
                /******************************************************************************************/
                .compose(RxUtils.toSimpleSingle(this, null, false))//包含上面所有,包括线程调度,绑定生命周期,显示loading
                .subscribe(loginBean -> logD(TAG, "data message=" + GsonUtil.toJson(loginBean)), throwable -> logD(TAG, "data message=" + throwable));
    }

    @Override
    public void onDestroy() {
        if (isFinishing()) {
            LoginUtil.onLoginFinished();
        }
        super.onDestroy();
    }

    /*@Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_wx_login) {
            mViewModel.reqWxOpenCode();
        } else if (id == R.id.tv_one_click_login) {
            doOnePass();
        }
    }

    private void doOnePass() {
        if (NetUtil.isMobile(this)) {
            //IntentUtil.startActivity(this, OneClickLoginActivity.class);
            //getGlobalUserStateViewModel().getDoOnePass();
            mViewModel.getPrefetchNumber();
        } else {
            DebugUtil.toast("一键登录需要打开移动数据并确保移动数据可用！");
        }
    }*/
}
