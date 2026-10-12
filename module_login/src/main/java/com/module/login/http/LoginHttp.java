package com.module.login.http;

import com.lib.network.RetrofitHelper;
import com.module.login.bean.LoginBean;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Retrofit;

/**
 * PackageName  com.hycg.company.http
 * ProjectName  HYCGCompanyProject
 * @author      xwchen
 * Date         2019/4/16.
 */
public class LoginHttp {
    private final LoginApi mLoginApi;
    private volatile static LoginHttp sInstance;

    private LoginHttp() {
        Retrofit mRetrofit = RetrofitHelper.getInstance()
                .getRetrofit();

        mLoginApi = mRetrofit.create(LoginApi.class);
    }

    public static LoginHttp getInstance() {
        if (sInstance == null) {
            synchronized (LoginHttp.class) {
                if (sInstance == null) {
                    sInstance = new LoginHttp();
                }
            }
        }
        return sInstance;
    }

    /**
     * 账号密码登录
     *
     * @param loginName
     * @param password
     * @return
     */
    public Single<LoginBean/*ResponseBody*/> login(String loginName, String password) {
        return mLoginApi.login(loginName, password);
    }

}
