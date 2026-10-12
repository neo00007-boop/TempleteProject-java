package com.lib.base.config;


import com.lib.base.util.DebugUtil;

import androidx.annotation.NonNull;

/**
 * PackageName  com.bigheadhorse.xscat.config
 * ProjectName  NumericalCodeProject
 * @author      xwchen
 * Date         2019-11-27.
 */
public interface AppConfig {
    //host ip，多域名切换见 com.lib.network.Hosts
    String COOKIE_URL_PATH = "";//cookie 指定path
    String API_BASE_URL_FINAL1 = "http://192.168.3.6:8888";
    String API_BASE_URL_FINAL2 = "http://app.jnprsc.com";//url2前缀

    /**
     * 切换环境:开发/测试/生产...
     *
     * @return
     */
    @NonNull
    static String getUrl() {
        return DebugUtil.isDebug ? API_BASE_URL_FINAL1 : API_BASE_URL_FINAL2;
    }

    //user
    String USER_TOKEN = "user_token";
    String USER_PHONE_NUM = "user_phone_num";
    //debug
    String LETTER_26 = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    //protocol
    String APP_PROTOCOL = "https://www.baidu.com";
    //易盾
    boolean IS_TEST = true;
    String ONE_PASS_ID = "";//一键登录业务id--------------------------生产环境
    String MOBILE_VERIFY_ID = "";//本机校验业务id----------------------生产环境
    String TRIALONE_PASSID = "";//试用一键登录业务id------------------测试环境
    String TRIAL_MOBILE_VERIFY_ID = "";//试用本机校验业务id-------------测试环境
    String SECRETK_EY = "";
    String SECRET_ID = "";
    String ONE_PASS_URL = "http://ye.dun.163yun.com/v1/oneclick/check";//本机号码一键登录验证操作操作url地址（此操作也可放服务端）
    String VERIFY_URL = "http://ye.dun.163yun.com/v1/check";//本机号码验证url地址
    //微信
    String WX_APP_ID = "wx5fcdeb6d2825e571";
    String WX_SECRET = "";
    String AUTHORIZATION_CODE = "authorization_code";
    //sp
    String SP_NAME = "sp_db";
    //others
    long BIG_TV_SHOW_TIME = 400;//字母索引显示时间
    long AUTO_DELAY = 3 * 1000;//轮播图展示时间
    String DEVICE_ID = "device_id";
}
