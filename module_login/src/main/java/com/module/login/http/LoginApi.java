package com.module.login.http;

import com.lib.network.Hosts;
import com.module.login.bean.LoginBean;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 1.api接口,定义接受数据bean类型;
 * 2.也可以使用ResponeBody来接收数据,responseBody.string()就是返回的数据.
 * @author      xwchen
 * Date         2019-11-27.
 */

public interface LoginApi {

    /*@GET("/MobileLogin/logOn")
     Single<String> login(@Query("loginName") String loginName,
                          @Query("password") String password);

     @GET("/enterpriseOrg/getEmpByOrgId/{organId}")
     Single<String> getOrgUserList(@Path("organId") String organId,
                                  @Query("page") String page,
                                  @Query("pageSize") String pageSize);

    @GET("/RiskInspect/getRiskContent")
    Single<RisksRecord> getRisks(@QueryMap Map<String, String> keyMaps);

    @FormUrlEncoded
    @Headers({Hosts.HOST2})
    @POST("/omma_server/api/121")
    Single<VideoOmma> getVideoUrl(@Field("versionNumber") String versionNumber);

     @Headers({"Content-Type: application/json", "Accept: application/json"})
     @POST("/MobileLogin/updatePassword")
     Single<String> editInfo(@Body LogoutParamsBean paramsBean);*/

    /**
     * 登录
     *
     * @param loginName
     * @param password
     * @return
     */
    @Headers({Hosts.HOST2})
    @POST("/service/user/doLogin")
    Single<LoginBean/*ResponseBody*/> login(@Query("mobile") String loginName,
                                            @Query("userPwd") String password);
}
