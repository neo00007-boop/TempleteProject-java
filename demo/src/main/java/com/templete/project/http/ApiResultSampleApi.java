package com.templete.project.http;

import com.lib.network.ApiResult;
import com.templete.project.bean.UserSample;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 统一返回体的接口样例。对象和列表都是 {@code ApiResult}，只是 data 的泛型不同。
 * 没有 data 的接口看 {@code isSuccess()}，不要 {@code unwrap()}。
 */
public interface ApiResultSampleApi {

    @POST(ApiResultSamples.DETAIL)
    Single<ApiResult<UserSample>> detail(@Query("id") String id);

    @POST(ApiResultSamples.LIST)
    Single<ApiResult<List<UserSample>>> list(@Query("page") int page);

    @POST(ApiResultSamples.DELETE)
    Single<ApiResult<Object>> delete(@Query("id") String id);

    @POST(ApiResultSamples.EMPTY)
    Single<ApiResult<UserSample>> empty(@Query("id") String id);

    @POST(ApiResultSamples.FAIL)
    Single<ApiResult<UserSample>> fail(@Query("id") String id);
}
