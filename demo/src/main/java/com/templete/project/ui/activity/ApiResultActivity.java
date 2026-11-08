package com.templete.project.ui.activity;

import com.lib.base.rxjava.RxUtils;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.network.ApiException;
import com.lib.network.ApiResult;
import com.lib.network.ApiResults;
import com.lib.network.RetrofitHelper;
import com.templete.project.bean.UserSample;
import com.templete.project.databinding.ActivityApiResultBinding;
import com.templete.project.http.ApiResultSampleApi;
import com.templete.project.http.ApiResultSamples;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * 统一返回体样例：对象、列表、无 data、空 data、业务失败。
 */
public class ApiResultActivity extends BaseActivity<ActivityApiResultBinding> {

    private ApiResultSampleApi mApi;
    private String mSample = "";

    @Override
    protected ActivityApiResultBinding viewBinding() {
        return ActivityApiResultBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("统一返回体");
        mApi = RetrofitHelper.getInstance().getRetrofit().create(ApiResultSampleApi.class);
    }

    @Override
    public void initView() {
    }

    @Override
    public void initEvent() {
        mViewBinding.tvDetail.setOnClickListener(v -> loadDetail());
        mViewBinding.tvList.setOnClickListener(v -> loadList());
        mViewBinding.tvDelete.setOnClickListener(v -> loadDelete());
        mViewBinding.tvEmpty.setOnClickListener(v -> loadEmpty());
        mViewBinding.tvFail.setOnClickListener(v -> loadFail());
    }

    @Override
    public void initData() {
    }

    private void loadDetail() {
        showSample(ApiResultSamples.DETAIL_JSON);
        mApi.detail("7")
                .compose(RxUtils.toSimpleSingle(this))
                .compose(ApiResults.unwrap())
                .subscribe(this::showUser, this::showError);
    }

    private void loadList() {
        showSample(ApiResultSamples.LIST_JSON);
        mApi.list(1)
                .compose(RxUtils.toSimpleSingle(this))
                .compose(ApiResults.unwrap())
                .subscribe(this::showUsers, this::showError);
    }

    private void loadDelete() {
        showSample(ApiResultSamples.DELETE_JSON);
        mApi.delete("7")
                .compose(RxUtils.toSimpleSingle(this))
                .subscribe(this::showWithoutData, this::showError);
    }

    private void loadEmpty() {
        showSample(ApiResultSamples.EMPTY_JSON);
        mApi.empty("7")
                .compose(RxUtils.toSimpleSingle(this))
                .compose(ApiResults.unwrap())
                .subscribe(this::showUser, this::showError);
    }

    private void loadFail() {
        showSample(ApiResultSamples.FAIL_JSON);
        mApi.fail("7")
                .compose(RxUtils.toSimpleSingle(this))
                .compose(ApiResults.unwrap())
                .subscribe(this::showUser, this::showError);
    }

    private void showSample(@NonNull String json) {
        mSample = json;
        mViewBinding.tvResult.setText("样例\n" + json);
    }

    private void showUser(@NonNull UserSample user) {
        showText("对象\nid=" + user.id + "\nname=" + user.name);
    }

    private void showUsers(@NonNull List<UserSample> users) {
        StringBuilder text = new StringBuilder();
        text.append("列表 ").append(users.size()).append(" 条");
        for (UserSample user : users) {
            text.append("\nid=").append(user.id).append(" name=").append(user.name);
        }
        showText(text.toString());
    }

    private void showWithoutData(@NonNull ApiResult<Object> result) {
        showText(result.isSuccess()
                ? "成功，无 data\ncode=" + result.getCode() + "\nmsg=" + result.getMsg()
                : "失败\ncode=" + result.getCode() + "\nmsg=" + result.getMsg());
    }

    private void showError(@NonNull Throwable throwable) {
        if (throwable instanceof ApiException) {
            ApiException error = (ApiException) throwable;
            String title = "data 为空".equals(error.getMessage()) ? "data 为空" : "业务失败";
            showText(title + "\ncode=" + error.getCode() + "\nmsg=" + error.getMessage());
            return;
        }
        showText(throwable.getMessage() == null ? "请求失败" : throwable.getMessage());
    }

    private void showText(@NonNull String text) {
        mViewBinding.tvResult.setText("样例\n" + mSample + "\n\n" + text);
        logD("ApiResult", text);
    }
}
